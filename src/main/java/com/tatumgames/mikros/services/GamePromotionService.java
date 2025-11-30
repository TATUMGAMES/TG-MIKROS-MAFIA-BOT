package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.AppPromotion;
import com.tatumgames.mikros.models.PromotionVerbosity;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for managing game promotions.
 */
public interface GamePromotionService {
    
    /**
     * Sets the promotion channel for a guild.
     * 
     * @param guildId the guild ID
     * @param channelId the channel ID where promotions should be posted
     */
    void setPromotionChannel(String guildId, String channelId);
    
    /**
     * Gets the promotion channel for a guild.
     * 
     * @param guildId the guild ID
     * @return the channel ID, or null if not set
     */
    String getPromotionChannel(String guildId);
    
    /**
     * Sets the promotion verbosity for a guild.
     * 
     * @param guildId the guild ID
     * @param verbosity the verbosity level
     */
    void setPromotionVerbosity(String guildId, PromotionVerbosity verbosity);
    
    /**
     * Gets the promotion verbosity for a guild.
     * 
     * @param guildId the guild ID
     * @return the verbosity level (defaults to MEDIUM if not set)
     */
    PromotionVerbosity getPromotionVerbosity(String guildId);
    
    /**
     * Fetches all apps from /getAllApps endpoint (stub for now).
     * TODO: Replace with real API call when /getAllApps is live.
     * 
     * @return list of app promotions
     */
    List<AppPromotion> fetchAllApps();
    
    /**
     * Gets the last promotion step posted for an app in a guild.
     * Returns 0 if never promoted, or 1-4 for the last step posted.
     * 
     * @param guildId the guild ID
     * @param appId the app ID
     * @return promotion step (0-4)
     */
    int getLastPromotionStep(String guildId, String appId);
    
    /**
     * Records that a promotion step was posted for an app in a guild.
     * 
     * @param guildId the guild ID
     * @param appId the app ID
     * @param step the promotion step (1-4)
     * @param postTime the time when posted
     */
    void recordPromotionStep(String guildId, String appId, int step, Instant postTime);
    
    /**
     * Checks if an app has been promoted in a guild (any step).
     * 
     * @param guildId the guild ID
     * @param appId the app ID
     * @return true if app has been promoted
     */
    boolean hasAppBeenPromoted(String guildId, String appId);
    
    /**
     * Gets the last post time for an app in a guild.
     * 
     * @param guildId the guild ID
     * @param appId the app ID
     * @return the last post time, or null if never posted
     */
    Instant getLastAppPostTime(String guildId, String appId);
}

