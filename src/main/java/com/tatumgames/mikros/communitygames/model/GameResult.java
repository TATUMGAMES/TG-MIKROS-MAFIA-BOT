package com.tatumgames.mikros.communitygames.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a game result for a single player attempt.
 */
public class GameResult {
    private final String userId;
    private final String username;
    private final String answer;
    private final int score;
    private final boolean isCorrect;
    private final Instant timestamp;
    
    /**
     * Creates a new GameResult.
     * 
     * @param userId the user's Discord ID
     * @param username the user's username
     * @param answer the answer provided
     * @param score the score achieved
     * @param isCorrect whether the answer was correct
     * @param timestamp when the attempt was made
     */
    public GameResult(String userId, String username, String answer, 
                      int score, boolean isCorrect, Instant timestamp) {
        this.userId = Objects.requireNonNull(userId);
        this.username = Objects.requireNonNull(username);
        this.answer = answer != null ? answer : "";
        this.score = score;
        this.isCorrect = isCorrect;
        this.timestamp = Objects.requireNonNull(timestamp);
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public int getScore() {
        return score;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
}

