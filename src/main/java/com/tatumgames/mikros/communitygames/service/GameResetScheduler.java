package com.tatumgames.mikros.communitygames.service;

import com.tatumgames.mikros.communitygames.CommunityGame;
import com.tatumgames.mikros.communitygames.model.GameConfig;
import com.tatumgames.mikros.communitygames.model.GameSession;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for daily game resets.
 * Checks every hour and resets games at their configured times.
 * 
 * TODO: Reward System Integration
 * - Award MIKROS discounts to winners
 * - Grant special Discord roles to champions
 * - Implement streak tracking for consecutive wins
 * - Add monthly leaderboard for cumulative winners
 */
public class GameResetScheduler {
    private static final Logger logger = LoggerFactory.getLogger(GameResetScheduler.class);
    
    private final CommunityGameService communityGameService;
    private final ScheduledExecutorService scheduler;
    private JDA jda;
    
    /**
     * Creates a new GameResetScheduler.
     * 
     * @param communityGameService the community game service
     */
    public GameResetScheduler(CommunityGameService communityGameService) {
        this.communityGameService = communityGameService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        logger.info("GameResetScheduler initialized");
    }
    
    /**
     * Starts the reset scheduler.
     * 
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        this.jda = jda;
        
        // Check every hour for games that need to be reset
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndResetGames();
            } catch (Exception e) {
                logger.error("Error in game reset scheduler", e);
            }
        }, 0, 1, TimeUnit.HOURS);
        
        logger.info("Game reset scheduler started (checks every hour)");
    }
    
    /**
     * Checks all guilds and resets games if it's time.
     */
    private void checkAndResetGames() {
        if (jda == null) {
            return;
        }
        
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        
        for (String guildId : communityGameService.getConfiguredGuilds()) {
            try {
                GameConfig config = communityGameService.getConfig(guildId);
                if (config == null) {
                    continue;
                }
                
                // Check if it's time to reset (within the current hour)
                LocalTime resetTime = config.getResetTime();
                if (resetTime.getHour() == currentHour) {
                    resetAndStartNewGame(guildId);
                }
            } catch (Exception e) {
                logger.error("Error resetting game for guild {}", guildId, e);
            }
        }
    }
    
    /**
     * Resets the current game and starts a new one for a guild.
     * 
     * @param guildId the guild ID
     */
    public void resetAndStartNewGame(String guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            logger.warn("Guild {} not found for game reset", guildId);
            return;
        }
        
        GameConfig config = communityGameService.getConfig(guildId);
        if (config == null) {
            return;
        }
        
        // Get the game channel
        TextChannel channel = guild.getTextChannelById(config.getGameChannelId());
        if (channel == null) {
            logger.warn("Game channel {} not found in guild {}", config.getGameChannelId(), guildId);
            return;
        }
        
        // Announce winner of previous game (if any)
        GameSession previousSession = communityGameService.getActiveSession(guildId);
        if (previousSession != null && previousSession.isActive()) {
            announceWinner(channel, previousSession);
        }
        
        // Reset the game
        communityGameService.resetGame(guildId);
        
        // Start a new game
        GameSession newSession = communityGameService.startRandomEnabledGame(guildId);
        if (newSession != null) {
            announceNewGame(channel, newSession);
        }
        
        logger.info("Reset and started new game for guild {}", guildId);
    }
    
    /**
     * Announces the winner of the previous game.
     */
    private void announceWinner(TextChannel channel, GameSession session) {
        String announcement;
        
        if (session.getGameType() == com.tatumgames.mikros.communitygames.model.GameType.DICE_ROLL) {
            com.tatumgames.mikros.communitygames.model.GameResult topScorer = session.getTopScorer();
            if (topScorer != null) {
                announcement = String.format(
                        "🎲 **Dice Battle Winner**\n\n" +
                        "🏆 **%s** rolled a **%d**!\n\n" +
                        "Congratulations on being today's champion!",
                        topScorer.getUsername(),
                        topScorer.getScore()
                );
            } else {
                announcement = "🎲 **Dice Battle Ended**\n\nNo participants today!";
            }
        } else {
            com.tatumgames.mikros.communitygames.model.GameResult winner = session.getWinner();
            if (winner != null) {
                announcement = String.format(
                        "%s **%s Winner**\n\n" +
                        "🏆 **%s** solved it first!\n\n" +
                        "Congratulations!",
                        session.getGameType().getEmoji(),
                        session.getGameType().getDisplayName(),
                        winner.getUsername()
                );
            } else {
                announcement = String.format(
                        "%s **%s Ended**\n\nNo one solved it today!\nAnswer was: **%s**",
                        session.getGameType().getEmoji(),
                        session.getGameType().getDisplayName(),
                        session.getCorrectAnswer() != null ? session.getCorrectAnswer() : "N/A"
                );
            }
        }
        
        channel.sendMessage(announcement).queue();
    }
    
    /**
     * Announces a new game in the channel.
     */
    private void announceNewGame(TextChannel channel, GameSession session) {
        CommunityGame game = communityGameService.getGame(session.getGameType());
        if (game == null) {
            return;
        }
        
        String announcement = game.generateAnnouncement(session);
        channel.sendMessage(announcement).queue();
    }
    
    /**
     * Stops the scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Game reset scheduler stopped");
    }
}

