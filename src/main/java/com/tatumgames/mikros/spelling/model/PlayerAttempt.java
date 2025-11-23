package com.tatumgames.mikros.spelling.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a player's attempt at the spelling challenge.
 */
public class PlayerAttempt {
    private final String userId;
    private final String username;
    private final String guess;
    private final boolean isCorrect;
    private final Instant timestamp;
    
    /**
     * Creates a new player attempt.
     * 
     * @param userId the user's Discord ID
     * @param username the user's username
     * @param guess the guessed word
     * @param isCorrect whether the guess was correct
     * @param timestamp when the attempt was made
     */
    public PlayerAttempt(String userId, String username, String guess, boolean isCorrect, Instant timestamp) {
        this.userId = Objects.requireNonNull(userId);
        this.username = Objects.requireNonNull(username);
        this.guess = Objects.requireNonNull(guess);
        this.isCorrect = isCorrect;
        this.timestamp = Objects.requireNonNull(timestamp);
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getGuess() {
        return guess;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerAttempt that = (PlayerAttempt) o;
        return userId.equals(that.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}








