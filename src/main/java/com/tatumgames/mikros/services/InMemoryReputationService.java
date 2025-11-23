package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.BehaviorReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ReputationService.
 * Stores behavior reports and calculates local reputation scores.
 */
public class InMemoryReputationService implements ReputationService {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryReputationService.class);
    private static final int DEFAULT_REPUTATION_SCORE = 100;
    
    // Key format: "guildId:userId" -> List of behavior reports
    private final Map<String, List<BehaviorReport>> reportStore;
    
    /**
     * Creates a new InMemoryReputationService.
     */
    public InMemoryReputationService() {
        this.reportStore = new ConcurrentHashMap<>();
        logger.info("InMemoryReputationService initialized");
    }
    
    @Override
    public void recordBehavior(BehaviorReport report) {
        if (report == null) {
            throw new IllegalArgumentException("Report cannot be null");
        }
        
        String key = buildKey(report.getGuildId(), report.getTargetUserId());
        reportStore.computeIfAbsent(key, k -> new ArrayList<>()).add(report);
        
        logger.info("Recorded behavior report: {}", report);
        
        // TODO: Call Tatum Games Reputation Score Update API
        // reportToExternalAPI(report);
    }
    
    @Override
    public List<BehaviorReport> getUserBehaviorReports(String userId, String guildId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        
        String key = buildKey(guildId, userId);
        List<BehaviorReport> reports = reportStore.getOrDefault(key, new ArrayList<>());
        
        // Return sorted by timestamp (newest first)
        return reports.stream()
                .sorted(Comparator.comparing(BehaviorReport::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public int calculateLocalReputation(String userId, String guildId) {
        List<BehaviorReport> reports = getUserBehaviorReports(userId, guildId);
        
        // Start with default score and apply behavior weights
        int score = DEFAULT_REPUTATION_SCORE;
        for (BehaviorReport report : reports) {
            score += report.getBehaviorCategory().getWeight();
        }
        
        // Ensure score stays within reasonable bounds (0-200)
        score = Math.max(0, Math.min(200, score));
        
        return score;
    }
    
    @Override
    public int getGlobalReputation(String userId) {
        // TODO: Integrate with Tatum Games Reputation Score API
        // This would make a GET request to: https://api.tatumgames.com/reputation-score/{userId}
        
        logger.debug("getGlobalReputation called for user {} - API not yet integrated", userId);
        return -1; // Return -1 to indicate API not available
    }
    
    @Override
    public boolean reportToExternalAPI(BehaviorReport report) {
        // TODO: Integrate with Tatum Games Reputation Score Update API
        // This would make a POST request to: https://api.tatumgames.com/reputation-score
        // with the behavior report data
        
        logger.debug("reportToExternalAPI called for report {} - API not yet integrated", report);
        return false; // Return false to indicate API not available
    }
    
    /**
     * Builds a unique key for the report store.
     * 
     * @param guildId the guild ID
     * @param userId the user ID
     * @return the composite key
     */
    private String buildKey(String guildId, String userId) {
        return guildId + ":" + userId;
    }
    
    /**
     * Clears all behavior reports (for testing).
     */
    public void clearAllReports() {
        reportStore.clear();
        logger.warn("All behavior reports have been cleared");
    }
}

