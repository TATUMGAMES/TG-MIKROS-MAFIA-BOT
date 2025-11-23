package com.tatumgames.mikros.communitygames.service;

import com.tatumgames.mikros.communitygames.CommunityGame;
import com.tatumgames.mikros.communitygames.games.DiceRollGame;
import com.tatumgames.mikros.communitygames.games.EmojiMatchGame;
import com.tatumgames.mikros.communitygames.games.WordUnscrambleGame;
import com.tatumgames.mikros.communitygames.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing community games across guilds.
 * Handles game configuration, sessions, and state management.
 * 
 * TODO: Future Features
 * - Game Rotation: Randomize daily game or rotate between enabled games
 * - Reward System: MIKROS discounts or Discord roles for winners
 * - Server Persistence: Store settings in database per server
 * - Emoji Leaderboard: Track cumulative wins over time
 * - Custom Games: Admins can define their own word lists or emoji sets
 */
public class CommunityGameService {
    private static final Logger logger = LoggerFactory.getLogger(CommunityGameService.class);
    
    // Game implementations
    private final Map<GameType, CommunityGame> games;
    
    // Guild configurations: guildId -> GameConfig
    private final Map<String, GameConfig> guildConfigs;
    
    // Active sessions: guildId -> GameSession
    private final Map<String, GameSession> activeSessions;
    
    /**
     * Creates a new CommunityGameService.
     */
    public CommunityGameService() {
        this.games = new HashMap<>();
        this.guildConfigs = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
        
        // Register available games
        registerGame(new WordUnscrambleGame());
        registerGame(new DiceRollGame());
        registerGame(new EmojiMatchGame());
        
        logger.info("CommunityGameService initialized with {} games", games.size());
    }
    
    /**
     * Registers a game implementation.
     * 
     * @param game the game to register
     */
    private void registerGame(CommunityGame game) {
        games.put(game.getGameType(), game);
        logger.debug("Registered game: {}", game.getGameType().getDisplayName());
    }
    
    /**
     * Sets up games for a guild.
     * 
     * @param guildId the guild ID
     * @param channelId the game channel ID
     * @param enabledGames the set of enabled games
     * @param resetTime the daily reset time
     */
    public void setupGames(String guildId, String channelId, Set<GameType> enabledGames, LocalTime resetTime) {
        GameConfig config = new GameConfig(guildId, channelId, enabledGames, resetTime);
        guildConfigs.put(guildId, config);
        logger.info("Game setup complete for guild {}: channel={}, games={}, resetTime={}",
                guildId, channelId, enabledGames, resetTime);
    }
    
    /**
     * Gets the game configuration for a guild.
     * 
     * @param guildId the guild ID
     * @return the game config, or null if not configured
     */
    public GameConfig getConfig(String guildId) {
        return guildConfigs.get(guildId);
    }
    
    /**
     * Updates a specific configuration setting.
     * 
     * @param guildId the guild ID
     * @param config the new configuration
     */
    public void updateConfig(String guildId, GameConfig config) {
        guildConfigs.put(guildId, config);
        logger.info("Updated game config for guild {}", guildId);
    }
    
    /**
     * Starts a new game session for a guild.
     * 
     * @param guildId the guild ID
     * @param gameType the type of game to start
     * @return the new game session
     */
    public GameSession startNewGame(String guildId, GameType gameType) {
        CommunityGame game = games.get(gameType);
        if (game == null) {
            throw new IllegalArgumentException("Unknown game type: " + gameType);
        }
        
        GameSession session = game.startNewSession(guildId);
        activeSessions.put(guildId, session);
        
        logger.info("Started new {} game for guild {}", gameType.getDisplayName(), guildId);
        return session;
    }
    
    /**
     * Gets the active session for a guild.
     * 
     * @param guildId the guild ID
     * @return the active session, or null if none
     */
    public GameSession getActiveSession(String guildId) {
        return activeSessions.get(guildId);
    }
    
    /**
     * Handles a player's attempt to play the current game.
     * 
     * @param guildId the guild ID
     * @param userId the user ID
     * @param username the username
     * @param input the player's input
     * @return the game result
     */
    public GameResult handleAttempt(String guildId, String userId, String username, String input) {
        GameSession session = activeSessions.get(guildId);
        if (session == null || !session.isActive()) {
            return null;
        }
        
        CommunityGame game = games.get(session.getGameType());
        if (game == null) {
            return null;
        }
        
        return game.handleAttempt(session, userId, username, input);
    }
    
    /**
     * Gets the announcement message for the current game.
     * 
     * @param guildId the guild ID
     * @return the announcement message, or null if no active game
     */
    public String getGameAnnouncement(String guildId) {
        GameSession session = activeSessions.get(guildId);
        if (session == null) {
            return null;
        }
        
        CommunityGame game = games.get(session.getGameType());
        if (game == null) {
            return null;
        }
        
        return game.generateAnnouncement(session);
    }
    
    /**
     * Resets the game for a guild.
     * 
     * @param guildId the guild ID
     */
    public void resetGame(String guildId) {
        GameSession session = activeSessions.get(guildId);
        if (session != null) {
            CommunityGame game = games.get(session.getGameType());
            if (game != null) {
                game.resetSession(session);
            }
        }
        
        // Remove the active session
        activeSessions.remove(guildId);
        
        logger.info("Reset game for guild {}", guildId);
    }
    
    /**
     * Starts a random game from the enabled games for a guild.
     * 
     * @param guildId the guild ID
     * @return the new session, or null if no games are enabled
     */
    public GameSession startRandomEnabledGame(String guildId) {
        GameConfig config = guildConfigs.get(guildId);
        if (config == null || config.getEnabledGames().isEmpty()) {
            return null;
        }
        
        // Pick a random enabled game
        List<GameType> enabledList = new ArrayList<>(config.getEnabledGames());
        GameType randomType = enabledList.get(new Random().nextInt(enabledList.size()));
        
        return startNewGame(guildId, randomType);
    }
    
    /**
     * Gets all configured guild IDs.
     * 
     * @return set of guild IDs
     */
    public Set<String> getConfiguredGuilds() {
        return new HashSet<>(guildConfigs.keySet());
    }
    
    /**
     * Gets the game implementation for a specific type.
     * 
     * @param gameType the game type
     * @return the game implementation
     */
    public CommunityGame getGame(GameType gameType) {
        return games.get(gameType);
    }
}

