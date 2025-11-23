package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.GamePromotion;
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
     * Fetches active game promotions from the external API.
     * 
     * TODO: Integrate with MIKROS Game Promotion API
     * 
     * @return list of active game promotions
     */
    List<GamePromotion> fetchActivePromotions();
    
    /**
     * Checks if a game has already been promoted in a guild.
     * 
     * @param guildId the guild ID
     * @param gameId the game ID
     * @return true if already promoted, false otherwise
     */
    boolean hasBeenPromoted(String guildId, int gameId);
    
    /**
     * Marks a game as promoted in a guild.
     * 
     * @param guildId the guild ID
     * @param gameId the game ID
     */
    void markAsPromoted(String guildId, int gameId);
    
    /**
     * Gets the last post time for a promotion in a guild.
     * 
     * @param guildId the guild ID
     * @param gameId the game ID
     * @return the last post time, or null if never posted
     */
    Instant getLastPostTime(String guildId, int gameId);
    
    /**
     * Notifies the backend API that a game was promoted.
     * 
     * TODO: Integrate with MIKROS API to mark game as pushed
     * 
     * @param gameId the game ID
     * @return true if successful, false otherwise
     */
    boolean notifyGamePushed(int gameId);
}

