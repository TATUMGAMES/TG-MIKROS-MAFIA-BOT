package com.tatumgames.mikros.games.word_unscramble.model;

import java.util.Objects;

/**
 * Tracks community-wide progression for Word Unscramble game per server.
 * Level N requires (10 × N) correctly solved words.
 */
public class WordUnscrambleProgression {
    private final String guildId;
    private int level;
    private int xp;
    private int xpRequired;

    /**
     * Creates a new WordUnscrambleProgression starting at level 1.
     *
     * @param guildId the guild ID
     */
    public WordUnscrambleProgression(String guildId) {
        this.guildId = Objects.requireNonNull(guildId);
        this.level = 1;
        this.xp = 0;
        this.xpRequired = calculateXpRequired(1);
    }

    /**
     * Creates a WordUnscrambleProgression with specific values.
     *
     * @param guildId the guild ID
     * @param level   the current level
     * @param xp      the current XP
     */
    public WordUnscrambleProgression(String guildId, int level, int xp) {
        this.guildId = Objects.requireNonNull(guildId);
        this.level = level;
        this.xp = xp;
        this.xpRequired = calculateXpRequired(level);
    }

    /**
     * Calculates XP required for a level.
     * Level N requires (10 × N) solves.
     *
     * @param level the level
     * @return the XP required
     */
    public static int calculateXpRequired(int level) {
        return 10 * level;
    }

    /**
     * Adds XP from a correct solve.
     *
     * @return true if level up occurred, false otherwise
     */
    public boolean addXp() {
        xp++;

        // Check if level up
        if (xp >= xpRequired) {
            level++;
            xp = 0; // Reset XP for new level
            xpRequired = calculateXpRequired(level);
            return true;
        }

        return false;
    }

    /**
     * Gets the current level (1-20).
     *
     * @return the level
     */
    public int getLevel() {
        return Math.min(20, Math.max(1, level)); // Clamp between 1 and 20
    }

    /**
     * Gets the current XP.
     *
     * @return the XP
     */
    public int getXp() {
        return xp;
    }

    /**
     * Gets the XP required for current level.
     *
     * @return the XP required
     */
    public int getXpRequired() {
        return xpRequired;
    }

    /**
     * Gets the guild ID.
     *
     * @return the guild ID
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Gets progress percentage (0-100).
     *
     * @return progress percentage
     */
    public double getProgressPercentage() {
        if (xpRequired == 0) {
            return 100.0;
        }
        return (xp * 100.0) / xpRequired;
    }

    /**
     * Checks if at max level (20).
     *
     * @return true if at max level
     */
    public boolean isMaxLevel() {
        return level >= 20;
    }
}
