package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.GamePromotion;
import com.tatumgames.mikros.models.PromotionVerbosity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of GamePromotionService.
 * Stores configuration in memory (expandable to database).
 */
public class InMemoryGamePromotionService implements GamePromotionService {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryGamePromotionService.class);
    
    // Guild configuration storage
    private final Map<String, String> promotionChannels; // guildId -> channelId
    private final Map<String, PromotionVerbosity> promotionVerbosity; // guildId -> verbosity
    private final Map<String, Map<Integer, Instant>> lastPostTimes; // guildId -> (gameId -> lastPostTime)
    
    /**
     * Creates a new InMemoryGamePromotionService.
     */
    public InMemoryGamePromotionService() {
        this.promotionChannels = new ConcurrentHashMap<>();
        this.promotionVerbosity = new ConcurrentHashMap<>();
        this.lastPostTimes = new ConcurrentHashMap<>();
        logger.info("InMemoryGamePromotionService initialized");
    }
    
    @Override
    public void setPromotionChannel(String guildId, String channelId) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        if (channelId == null || channelId.isBlank()) {
            throw new IllegalArgumentException("channelId cannot be null or blank");
        }
        
        promotionChannels.put(guildId, channelId);
        logger.info("Promotion channel set to {} for guild {}", channelId, guildId);
    }
    
    @Override
    public String getPromotionChannel(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return null;
        }
        return promotionChannels.get(guildId);
    }
    
    @Override
    public void setPromotionVerbosity(String guildId, PromotionVerbosity verbosity) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        if (verbosity == null) {
            throw new IllegalArgumentException("verbosity cannot be null");
        }
        
        promotionVerbosity.put(guildId, verbosity);
        logger.info("Promotion verbosity set to {} for guild {}", verbosity, guildId);
    }
    
    @Override
    public PromotionVerbosity getPromotionVerbosity(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return PromotionVerbosity.MEDIUM; // Default
        }
        return promotionVerbosity.getOrDefault(guildId, PromotionVerbosity.MEDIUM);
    }
    
    @Override
    public List<GamePromotion> fetchActivePromotions() {
        // TODO: Integrate with Tatum Games API
        // An API call is needed to fetch active promotions from the Tatum Games API.
        // Expected response structure:
        //   - game_name: String
        //   - game_description: String
        //   - url: String (download link)
        //   - campaign_start_date: ISO 8601 string
        //   - campaign_end_date: ISO 8601 string
        //   - frequency_days: int (provided by backend - how often to post this promotion)
        // When API is integrated, parse the JSON response and create GamePromotion objects.
        
        logger.debug("fetchActivePromotions called - API not yet integrated, returning test data");
        
        // TEMPORARY TEST DATA - This is test metadata to showcase how the feature would work
        // when provided with information from the API.
        // TODO: Remove this test data once API integration is complete
        Instant now = Instant.now();
        GamePromotion testPromotion = new GamePromotion(
                999, // test game ID
                "Heroes Vs Villains: Rise of Nemesis",
                "The core mechanics focus on guiding your character, a Guardian (superhero), through various levels where you face waves of enemies. As you progress through the game, you engage in combat, defeat enemies, and collect rewards like coins and gear.",
                "https://developer.tatumgames.com/",
                null, // no custom message
                null, // no image URL
                now, // campaign start: today
                now.plus(24, ChronoUnit.HOURS), // campaign end: 24 hours later
                1 // frequency: 1 day (provided by backend, for testing set to 1 day)
        );
        
        return List.of(testPromotion);
    }
    
    @Override
    public boolean hasBeenPromoted(String guildId, int gameId) {
        // This method is kept for backward compatibility but now uses last post time
        // Check if there's a last post time recorded
        return getLastPostTime(guildId, gameId) != null;
    }
    
    @Override
    public void markAsPromoted(String guildId, int gameId) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        
        // Record the post time
        recordPostTime(guildId, gameId, Instant.now());
        logger.info("Game {} marked as promoted in guild {} at {}", gameId, guildId, Instant.now());
    }
    
    /**
     * Gets the last post time for a promotion in a guild.
     * 
     * @param guildId the guild ID
     * @param gameId the game ID
     * @return the last post time, or null if never posted
     */
    @Override
    public Instant getLastPostTime(String guildId, int gameId) {
        if (guildId == null || guildId.isBlank()) {
            return null;
        }
        
        Map<Integer, Instant> guildPosts = lastPostTimes.get(guildId);
        if (guildPosts == null) {
            return null;
        }
        
        return guildPosts.get(gameId);
    }
    
    /**
     * Records the post time for a promotion in a guild.
     * 
     * @param guildId the guild ID
     * @param gameId the game ID
     * @param postTime the time when the promotion was posted
     */
    public void recordPostTime(String guildId, int gameId, Instant postTime) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        if (postTime == null) {
            throw new IllegalArgumentException("postTime cannot be null");
        }
        
        lastPostTimes.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>())
                .put(gameId, postTime);
        logger.debug("Recorded post time for game {} in guild {}: {}", gameId, guildId, postTime);
    }
    
    @Override
    public boolean notifyGamePushed(int gameId) {
        // TODO: Integrate with MIKROS API to mark game as pushed
        // This would make a POST request to: https://api.tatumgames.com/mark-pushed
        // with the gameId in the request body
        
        logger.debug("notifyGamePushed called for gameId {} - API not yet integrated", gameId);
        
        // Return false to indicate API not available
        return false;
    }
    
    /**
     * Clears all promotion data for a guild (for testing or when guild opts out).
     * 
     * @param guildId the guild ID
     */
    public void clearGuildData(String guildId) {
        promotionChannels.remove(guildId);
        promotionVerbosity.remove(guildId);
        lastPostTimes.remove(guildId);
        logger.info("Cleared all promotion data for guild {}", guildId);
    }
    
    /**
     * Gets statistics about configured guilds.
     * 
     * @return map of statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("guilds_with_channel_configured", promotionChannels.size());
        stats.put("guilds_with_custom_verbosity", promotionVerbosity.size());
        stats.put("total_promoted_games", lastPostTimes.values().stream()
                .mapToInt(Map::size)
                .sum());
        return stats;
    }
}

