package com.tatumgames.mikros.spelling.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a daily spelling challenge session for a guild.
 */
public class ChallengeSession {
    private final String guildId;
    private final String correctWord;
    private final String scrambledWord;
    private final Instant startTime;
    private final List<PlayerAttempt> attempts;
    private boolean isActive;
    
    /**
     * Creates a new challenge session.
     * 
     * @param guildId the guild ID
     * @param correctWord the correct word
     * @param scrambledWord the scrambled version
     * @param startTime when the challenge started
     */
    public ChallengeSession(String guildId, String correctWord, String scrambledWord, Instant startTime) {
        this.guildId = Objects.requireNonNull(guildId);
        this.correctWord = Objects.requireNonNull(correctWord);
        this.scrambledWord = Objects.requireNonNull(scrambledWord);
        this.startTime = Objects.requireNonNull(startTime);
        this.attempts = new ArrayList<>();
        this.isActive = true;
    }
    
    /**
     * Adds an attempt to the session.
     * 
     * @param attempt the player attempt
     */
    public void addAttempt(PlayerAttempt attempt) {
        attempts.add(attempt);
    }
    
    /**
     * Gets attempts by a specific user.
     * 
     * @param userId the user ID
     * @return list of attempts by that user
     */
    public List<PlayerAttempt> getAttemptsByUser(String userId) {
        return attempts.stream()
                .filter(a -> a.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the first successful solver.
     * 
     * @return the first correct attempt, or null if none
     */
    public PlayerAttempt getFirstSolver() {
        return attempts.stream()
                .filter(PlayerAttempt::isCorrect)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Gets all successful solvers.
     * 
     * @return list of correct attempts
     */
    public List<PlayerAttempt> getAllSolvers() {
        return attempts.stream()
                .filter(PlayerAttempt::isCorrect)
                .distinct() // Ensure one entry per user
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if a user has already solved the challenge.
     * 
     * @param userId the user ID
     * @return true if user solved it
     */
    public boolean hasUserSolved(String userId) {
        return attempts.stream()
                .anyMatch(a -> a.getUserId().equals(userId) && a.isCorrect());
    }
    
    // Getters
    
    public String getGuildId() {
        return guildId;
    }
    
    public String getCorrectWord() {
        return correctWord;
    }
    
    public String getScrambledWord() {
        return scrambledWord;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public List<PlayerAttempt> getAttempts() {
        return new ArrayList<>(attempts);
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}






