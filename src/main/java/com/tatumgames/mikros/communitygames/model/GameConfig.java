package com.tatumgames.mikros.communitygames.model;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Configuration for community games in a guild.
 * 
 * TODO: Server Persistence
 * - Add database persistence for guild configurations
 * - Store cumulative leaderboard data
 * - Add custom word lists and emoji sets per guild
 * - Add difficulty level settings
 */
public class GameConfig {
    private String guildId;
    private String gameChannelId;
    private Set<GameType> enabledGames;
    private LocalTime resetTime;
    private GameType activeGameType;
    
    /**
     * Creates a new GameConfig.
     * 
     * @param guildId the guild ID
     * @param gameChannelId the channel ID for games
     * @param enabledGames the set of enabled game types
     * @param resetTime the daily reset time (UTC)
     */
    public GameConfig(String guildId, String gameChannelId, Set<GameType> enabledGames, LocalTime resetTime) {
        this.guildId = Objects.requireNonNull(guildId);
        this.gameChannelId = Objects.requireNonNull(gameChannelId);
        this.enabledGames = new HashSet<>(enabledGames);
        this.resetTime = resetTime != null ? resetTime : LocalTime.of(0, 0); // Default midnight UTC
        this.activeGameType = enabledGames.isEmpty() ? null : enabledGames.iterator().next();
    }
    
    /**
     * Creates a default GameConfig with all games enabled.
     * 
     * @param guildId the guild ID
     * @param gameChannelId the channel ID
     */
    public GameConfig(String guildId, String gameChannelId) {
        this(guildId, gameChannelId, Set.of(GameType.values()), LocalTime.of(0, 0));
    }
    
    public String getGuildId() {
        return guildId;
    }
    
    public String getGameChannelId() {
        return gameChannelId;
    }
    
    public void setGameChannelId(String gameChannelId) {
        this.gameChannelId = gameChannelId;
    }
    
    public Set<GameType> getEnabledGames() {
        return new HashSet<>(enabledGames);
    }
    
    public void setEnabledGames(Set<GameType> enabledGames) {
        this.enabledGames = new HashSet<>(enabledGames);
    }
    
    public boolean isGameEnabled(GameType gameType) {
        return enabledGames.contains(gameType);
    }
    
    public void enableGame(GameType gameType) {
        enabledGames.add(gameType);
    }
    
    public void disableGame(GameType gameType) {
        enabledGames.remove(gameType);
    }
    
    public LocalTime getResetTime() {
        return resetTime;
    }
    
    public void setResetTime(LocalTime resetTime) {
        this.resetTime = resetTime;
    }
    
    public GameType getActiveGameType() {
        return activeGameType;
    }
    
    public void setActiveGameType(GameType activeGameType) {
        this.activeGameType = activeGameType;
    }
}

