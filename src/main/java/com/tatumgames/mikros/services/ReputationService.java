package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.BehaviorReport;
import com.tatumgames.mikros.models.api.GetUserScoreDetailResponse;
import com.tatumgames.mikros.models.api.TrackPlayerRatingRequest;

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
     * Reports behavior to the external reputation API.
     * 
     * TODO: Integrate with Tatum Games Reputation Score Update API
     * 
     * @param report the behavior report to submit
     * @return true if successful, false otherwise
     */
    boolean reportToExternalAPI(BehaviorReport report);
    
    /**
     * Tracks player rating by calling /trackPlayerRating API.
     * 
     * @param request the track player rating request
     * @return true if successful, false otherwise
     */
    boolean trackPlayerRating(TrackPlayerRatingRequest request);
    
    /**
     * Gets user score details by calling /getUserScoreDetail API.
     * 
     * @param usernames list of Discord usernames to lookup
     * @return response containing user scores, or null if error
     */
    GetUserScoreDetailResponse getUserScoreDetail(List<String> usernames);
}

