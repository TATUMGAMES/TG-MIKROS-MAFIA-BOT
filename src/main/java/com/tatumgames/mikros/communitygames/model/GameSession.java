package com.tatumgames.mikros.communitygames.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an active game session for a specific day.
 */
public class GameSession {
    private final String guildId;
    private final GameType gameType;
    private final Instant startTime;
    private final List<GameResult> results;
    private String correctAnswer;
    private boolean isActive;
    
    /**
     * Creates a new GameSession.
     * 
     * @param guildId the guild ID
     * @param gameType the type of game
     * @param startTime when the session started
     * @param correctAnswer the correct answer for this session (if applicable)
     */
    public GameSession(String guildId, GameType gameType, Instant startTime, String correctAnswer) {
        this.guildId = Objects.requireNonNull(guildId);
        this.gameType = Objects.requireNonNull(gameType);
        this.startTime = Objects.requireNonNull(startTime);
        this.correctAnswer = correctAnswer;
        this.results = new ArrayList<>();
        this.isActive = true;
    }
    
    public String getGuildId() {
        return guildId;
    }
    
    public GameType getGameType() {
        return gameType;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public List<GameResult> getResults() {
        return new ArrayList<>(results);
    }
    
    public void addResult(GameResult result) {
        results.add(result);
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    /**
     * Gets the winner (first correct result).
     * 
     * @return the winning result, or null if none
     */
    public GameResult getWinner() {
        return results.stream()
                .filter(GameResult::isCorrect)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Gets the top scorer (highest score result).
     * 
     * @return the top scoring result, or null if none
     */
    public GameResult getTopScorer() {
        return results.stream()
                .max((r1, r2) -> Integer.compare(r1.getScore(), r2.getScore()))
                .orElse(null);
    }
}

