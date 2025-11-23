package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.BehaviorReport;

import java.util.List;

/**
 * Service interface for managing user reputation and behavior reports.
 */
public interface ReputationService {
    
    /**
     * Records a behavior report for a user.
     * 
     * @param report the behavior report to record
     */
    void recordBehavior(BehaviorReport report);
    
    /**
     * Gets all behavior reports for a specific user in a guild.
     * 
     * @param userId the user ID
     * @param guildId the guild ID
     * @return list of behavior reports
     */
    List<BehaviorReport> getUserBehaviorReports(String userId, String guildId);
    
    /**
     * Calculates the local reputation score for a user based on behavior reports.
     * This is a placeholder until the external API is integrated.
     * 
     * @param userId the user ID
     * @param guildId the guild ID
     * @return the calculated reputation score (default 100, modified by behavior)
     */
    int calculateLocalReputation(String userId, String guildId);
    
    /**
     * Gets the global reputation score for a user from the external API.
     * 
     * TODO: Integrate with Tatum Games Reputation Score API
     * 
     * @param userId the user ID
     * @return the global reputation score, or -1 if unavailable
     */
    int getGlobalReputation(String userId);
    
    /**
     * Reports behavior to the external reputation API.
     * 
     * TODO: Integrate with Tatum Games Reputation Score Update API
     * 
     * @param report the behavior report to submit
     * @return true if successful, false otherwise
     */
    boolean reportToExternalAPI(BehaviorReport report);
}

