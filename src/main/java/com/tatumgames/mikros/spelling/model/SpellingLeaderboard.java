package com.tatumgames.mikros.spelling.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks spelling challenge scores across all time.
 * 
 * TODO: Future Features
 * - Persistent storage in database
 * - Monthly/yearly leaderboard resets
 * - Web dashboard export
 * - Integration with MIKROS rewards
 */
public class SpellingLeaderboard {
    private final Map<String, PlayerScore> scores;
    
    /**
     * Creates a new leaderboard.
     */
    public SpellingLeaderboard() {
        this.scores = new ConcurrentHashMap<>();
    }
    
    /**
     * Awards points to a player.
     * 
     * @param userId the user ID
     * @param username the username
     * @param points the points to award
     * @param wasFirst whether they were first solver
     */
    public void awardPoints(String userId, String username, int points, boolean wasFirst) {
        PlayerScore score = scores.computeIfAbsent(userId, id -> new PlayerScore(userId, username));
        score.addPoints(points);
        if (wasFirst) {
            score.incrementFirstSolves();
        } else {
            score.incrementSolves();
        }
    }
    
    /**
     * Gets a player's score.
     * 
     * @param userId the user ID
     * @return the player score, or null if not found
     */
    public PlayerScore getScore(String userId) {
        return scores.get(userId);
    }
    
    /**
     * Gets the top players by points.
     * 
     * @param limit the maximum number of players
     * @return list of top players
     */
    public List<PlayerScore> getTopPlayers(int limit) {
        return scores.values().stream()
                .sorted(Comparator.comparingInt(PlayerScore::getTotalPoints)
                        .thenComparingInt(PlayerScore::getFirstSolves)
                        .reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the total number of players.
     * 
     * @return player count
     */
    public int getPlayerCount() {
        return scores.size();
    }
    
    /**
     * Represents a player's cumulative score.
     */
    public static class PlayerScore {
        private final String userId;
        private String username;
        private int totalPoints;
        private int totalSolves;
        private int firstSolves;
        
        public PlayerScore(String userId, String username) {
            this.userId = userId;
            this.username = username;
            this.totalPoints = 0;
            this.totalSolves = 0;
            this.firstSolves = 0;
        }
        
        public void addPoints(int points) {
            this.totalPoints += points;
        }
        
        public void incrementSolves() {
            this.totalSolves++;
        }
        
        public void incrementFirstSolves() {
            this.firstSolves++;
            this.totalSolves++; // First solves count as solves too
        }
        
        public void updateUsername(String username) {
            this.username = username;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public int getTotalPoints() {
            return totalPoints;
        }
        
        public int getTotalSolves() {
            return totalSolves;
        }
        
        public int getFirstSolves() {
            return firstSolves;
        }
    }
}






