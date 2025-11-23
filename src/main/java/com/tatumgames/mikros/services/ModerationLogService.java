package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;

import java.util.List;

/**
 * Service interface for managing moderation action logs.
 */
public interface ModerationLogService {
    
    /**
     * Logs a moderation action.
     * 
     * @param action the moderation action to log
     */
    void logAction(ModerationAction action);
    
    /**
     * Retrieves all moderation actions for a specific user in a guild.
     * 
     * @param userId the ID of the user
     * @param guildId the ID of the guild
     * @return list of moderation actions, ordered by timestamp (newest first)
     */
    List<ModerationAction> getUserHistory(String userId, String guildId);
    
    /**
     * Retrieves all moderation actions of a specific type for a user in a guild.
     * 
     * @param userId the ID of the user
     * @param guildId the ID of the guild
     * @param actionType the type of action to filter by
     * @return list of moderation actions of the specified type
     */
    List<ModerationAction> getUserHistoryByType(String userId, String guildId, ActionType actionType);
    
    /**
     * Gets the total number of moderation actions for a user in a guild.
     * 
     * @param userId the ID of the user
     * @param guildId the ID of the guild
     * @return the total count of actions
     */
    int getUserActionCount(String userId, String guildId);
    
    /**
     * Retrieves all moderation actions for a guild.
     * 
     * @param guildId the ID of the guild
     * @return list of all moderation actions in the guild
     */
    List<ModerationAction> getAllActions(String guildId);
    
    /**
     * Clears all moderation history.
     * Note: This is primarily for testing purposes.
     */
    void clearAllHistory();
}

