package com.tatumgames.mikros.games.word_unscramble.model;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Configuration for Word Unscramble game in a guild.
 * <p>
 * TODO: Server Persistence
 * - Add database persistence for guild configurations
 * - Store cumulative leaderboard data
 * - Add custom word lists per guild
 * - Add difficulty level settings
 */
public class WordUnscrambleConfig {
    private final String guildId;
    private String gameChannelId;
    private Set<WordUnscrambleType> enabledGames;
    private LocalTime resetTime;
    private WordUnscrambleType activeGameType;

    /**
     * Creates a new WordUnscrambleConfig.
     *
     * @param guildId       the guild ID
     * @param gameChannelId the channel ID for games
     * @param enabledGames  the set of enabled game types
     * @param resetTime     the daily reset time (UTC)
     */
    public WordUnscrambleConfig(String guildId, String gameChannelId, Set<WordUnscrambleType> enabledGames, LocalTime resetTime) {
        this.guildId = Objects.requireNonNull(guildId);
        this.gameChannelId = Objects.requireNonNull(gameChannelId);
        this.enabledGames = new HashSet<>(enabledGames);
        this.resetTime = resetTime != null ? resetTime : LocalTime.of(0, 0); // Default midnight UTC
        this.activeGameType = enabledGames.isEmpty() ? null : enabledGames.iterator().next();
    }

    /**
     * Creates a default WordUnscrambleConfig with all games enabled.
     *
     * @param guildId       the guild ID
     * @param gameChannelId the channel ID
     */
    public WordUnscrambleConfig(String guildId, String gameChannelId) {
        this(guildId, gameChannelId, Set.of(WordUnscrambleType.values()), LocalTime.of(0, 0));
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

    public Set<WordUnscrambleType> getEnabledGames() {
        return new HashSet<>(enabledGames);
    }

    public void setEnabledGames(Set<WordUnscrambleType> enabledGames) {
        this.enabledGames = new HashSet<>(enabledGames);
    }

    public boolean isGameEnabled(WordUnscrambleType gameType) {
        return enabledGames.contains(gameType);
    }

    public void enableGame(WordUnscrambleType gameType) {
        enabledGames.add(gameType);
    }

    public void disableGame(WordUnscrambleType gameType) {
        enabledGames.remove(gameType);
    }

    public LocalTime getResetTime() {
        return resetTime;
    }

    public void setResetTime(LocalTime resetTime) {
        this.resetTime = resetTime;
    }

    public WordUnscrambleType getActiveGameType() {
        return activeGameType;
    }

    public void setActiveGameType(WordUnscrambleType activeGameType) {
        this.activeGameType = activeGameType;
    }
}




