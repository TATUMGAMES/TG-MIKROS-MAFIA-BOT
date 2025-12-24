package com.tatumgames.mikros.games.word_unscramble.model;

import java.util.Objects;

/**
 * Tracks individual player statistics for Word Unscramble game per guild.
 * Stores cumulative stats across all game sessions.
 */
public class WordUnscramblePlayerStats {
    private final String userId;
    private final String guildId;
    private int totalWordsSolved;
    private int totalPoints;
    private int highestScore;
    private long fastestTimeSeconds; // Fastest solve time in seconds (0 if no solves yet)
    private int totalAttempts;
    private int wrongGuesses;

    /**
     * Creates a new WordUnscramblePlayerStats starting with zero stats.
     *
     * @param userId  the user's Discord ID
     * @param guildId the guild ID
     */
    public WordUnscramblePlayerStats(String userId, String guildId) {
        this.userId = Objects.requireNonNull(userId);
        this.guildId = Objects.requireNonNull(guildId);
        this.totalWordsSolved = 0;
        this.totalPoints = 0;
        this.highestScore = 0;
        this.fastestTimeSeconds = 0; // 0 means no fastest time set yet
        this.totalAttempts = 0;
        this.wrongGuesses = 0;
    }

    /**
     * Creates a WordUnscramblePlayerStats with specific values.
     *
     * @param userId           the user's Discord ID
     * @param guildId          the guild ID
     * @param totalWordsSolved total correct answers
     * @param totalPoints      cumulative points earned
     * @param highestScore     best single-word score
     * @param fastestTimeSeconds fastest solve time in seconds
     * @param totalAttempts    total guesses (correct + incorrect)
     * @param wrongGuesses     total incorrect attempts
     */
    public WordUnscramblePlayerStats(String userId, String guildId, int totalWordsSolved,
                                      int totalPoints, int highestScore, long fastestTimeSeconds,
                                      int totalAttempts, int wrongGuesses) {
        this.userId = Objects.requireNonNull(userId);
        this.guildId = Objects.requireNonNull(guildId);
        this.totalWordsSolved = totalWordsSolved;
        this.totalPoints = totalPoints;
        this.highestScore = highestScore;
        this.fastestTimeSeconds = fastestTimeSeconds;
        this.totalAttempts = totalAttempts;
        this.wrongGuesses = wrongGuesses;
    }

    /**
     * Records a correct answer and updates relevant stats.
     *
     * @param score      the score achieved for this word
     * @param timeSeconds the time taken to solve in seconds
     */
    public void recordCorrectAnswer(int score, long timeSeconds) {
        totalWordsSolved++;
        totalPoints += score;
        totalAttempts++;
        
        // Update highest score if this is better
        if (score > highestScore) {
            highestScore = score;
        }
        
        // Update fastest time if this is faster (or if no fastest time set yet)
        if (fastestTimeSeconds == 0 || timeSeconds < fastestTimeSeconds) {
            fastestTimeSeconds = timeSeconds;
        }
    }

    /**
     * Records a wrong guess.
     */
    public void recordWrongGuess() {
        wrongGuesses++;
        totalAttempts++;
    }

    /**
     * Gets the accuracy percentage (correct answers / total attempts * 100).
     *
     * @return accuracy percentage (0-100), or 0 if no attempts
     */
    public double getAccuracyPercentage() {
        if (totalAttempts == 0) {
            return 0.0;
        }
        return (double) totalWordsSolved / totalAttempts * 100.0;
    }

    /**
     * Gets the average score per correct answer.
     *
     * @return average score, or 0 if no words solved
     */
    public double getAverageScore() {
        if (totalWordsSolved == 0) {
            return 0.0;
        }
        return (double) totalPoints / totalWordsSolved;
    }

    // Getters

    public String getUserId() {
        return userId;
    }

    public String getGuildId() {
        return guildId;
    }

    public int getTotalWordsSolved() {
        return totalWordsSolved;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public long getFastestTimeSeconds() {
        return fastestTimeSeconds;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public int getWrongGuesses() {
        return wrongGuesses;
    }
}

