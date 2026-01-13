package com.tatumgames.mikros.games.word_unscramble.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a Word Unscramble game result for a single player attempt.
 */
public record WordUnscrambleResult(String userId, String username, String answer, int score, int bonus,
                                   boolean isCorrect,
                                   Instant timestamp) {
    /**
     * Creates a new WordUnscrambleResult.
     *
     * @param userId    the user's Discord ID
     * @param username  the user's username
     * @param answer    the answer provided
     * @param score     the total score achieved (base score + bonus)
     * @param bonus     the bonus points from wrong guesses (0 if no bonus)
     * @param isCorrect whether the answer was correct
     * @param timestamp when the attempt was made
     */
    public WordUnscrambleResult(String userId, String username, String answer,
                                int score, int bonus, boolean isCorrect, Instant timestamp) {
        this.userId = Objects.requireNonNull(userId);
        this.username = Objects.requireNonNull(username);
        this.answer = answer != null ? answer : "";
        this.score = score;
        this.bonus = bonus;
        this.isCorrect = isCorrect;
        this.timestamp = Objects.requireNonNull(timestamp);
    }

    /**
     * Legacy constructor for backward compatibility (bonus defaults to 0).
     *
     * @param userId    the user's Discord ID
     * @param username  the user's username
     * @param answer    the answer provided
     * @param score     the score achieved
     * @param isCorrect whether the answer was correct
     * @param timestamp when the attempt was made
     */
    public WordUnscrambleResult(String userId, String username, String answer,
                                int score, boolean isCorrect, Instant timestamp) {
        this(userId, username, answer, score, 0, isCorrect, timestamp);
    }
}



