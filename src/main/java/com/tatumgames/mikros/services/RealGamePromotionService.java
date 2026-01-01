package com.tatumgames.mikros.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tatumgames.mikros.api.TatumGamesApiClient;
import com.tatumgames.mikros.models.AppPromotion;
import com.tatumgames.mikros.models.GetAllAppsResponse;
import com.tatumgames.mikros.models.PromotionVerbosity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real implementation of GamePromotionService that uses API client for fetching apps.
 * Maintains in-memory storage for guild configuration (channels, verbosity, step tracking).
 * Falls back to stub JSON if API call fails for resilience.
 */
public class RealGamePromotionService implements GamePromotionService {
    private static final Logger logger = LoggerFactory.getLogger(RealGamePromotionService.class);

    private final TatumGamesApiClient apiClient;
    private final String promotionApiKey;
    private final String promotionApiBaseUrl; // Store base URL (without /mikros/discord)
    private final ObjectMapper objectMapper;

    // Guild configuration storage
    private final Map<String, String> promotionChannels; // guildId -> channelId
    private final Map<String, PromotionVerbosity> promotionVerbosity; // guildId -> verbosity

    // App promotion step tracking: guildId -> (appId -> PromotionStepRecord)
    private final Map<String, Map<String, PromotionStepRecord>> promotionSteps;

    /**
     * Record of promotion step and last post time for an app.
     */
    private static class PromotionStepRecord {
        int lastStep;           // 1-4, or 0 if never posted
        Instant lastPostTime;   // When last step was posted

        PromotionStepRecord(int lastStep, Instant lastPostTime) {
            this.lastStep = lastStep;
            this.lastPostTime = lastPostTime;
        }
    }

    /**
     * Creates a new RealGamePromotionService.
     *
     * @param apiClient      the API client for making requests
     * @param promotionApiKey the API key for promotion API calls
     * @param promotionApiBaseUrl the base URL for the API (e.g., https://tg-api-new.uc.r.appspot.com)
     */
    public RealGamePromotionService(TatumGamesApiClient apiClient, String promotionApiKey, String promotionApiBaseUrl) {
        this.apiClient = apiClient;
        this.promotionApiKey = promotionApiKey;
        this.promotionApiBaseUrl = promotionApiBaseUrl;
        this.promotionChannels = new ConcurrentHashMap<>();
        this.promotionVerbosity = new ConcurrentHashMap<>();
        this.promotionSteps = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        logger.info("RealGamePromotionService initialized with base URL: {}", promotionApiBaseUrl);
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

    /**
     * Clears all promotion data for a guild (for testing or when guild opts out).
     *
     * @param guildId the guild ID
     */
    public void clearGuildData(String guildId) {
        promotionChannels.remove(guildId);
        promotionVerbosity.remove(guildId);
        promotionSteps.remove(guildId);
        logger.info("Cleared all promotion data for guild {}", guildId);
    }

    /**
     * Loads apps from stub JSON file as fallback.
     *
     * @return list of app promotions
     */
    private List<AppPromotion> loadStubApps() {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("stubs/getAllApps.json");

            if (inputStream == null) {
                logger.error("Could not find stub JSON file: stubs/getAllApps.json");
                return List.of();
            }

            GetAllAppsResponse response = objectMapper.readValue(inputStream, GetAllAppsResponse.class);

            if (response.getData() == null || response.getData().getApps() == null) {
                logger.warn("Stub JSON file has no apps data");
                return List.of();
            }

            logger.info("Loaded {} apps from stub JSON (fallback)", response.getData().getApps().size());
            return response.getData().getApps();

        } catch (Exception e) {
            logger.error("Failed to load stub JSON file", e);
            return List.of();
        }
    }

    @Override
    public List<AppPromotion> fetchAllApps() {
        if (promotionApiKey == null || promotionApiKey.isBlank()) {
            logger.warn("Promotion API key not configured, using stub response");
            return loadStubApps();
        }

        try {
            // Construct full URL: baseUrl + /mikros/discord/getAllApps
            GetAllAppsResponse response = apiClient.getWithApiKey(
                    promotionApiBaseUrl + "/mikros/discord", // Base URL with path
                    "/getAllApps", // Endpoint
                    promotionApiKey,
                    GetAllAppsResponse.class
            );
            if (response != null && response.getData() != null && response.getData().getApps() != null) {
                List<AppPromotion> apps = response.getData().getApps();
                logger.info("Fetched {} apps from API", apps.size());
                return apps;
            }
            logger.warn("API returned empty or invalid response, falling back to stub");
            return loadStubApps();
        } catch (TatumGamesApiClient.ApiException e) {
            logger.error("Failed to fetch apps from API (status: {}), falling back to stub: {}", 
                    e.getStatusCode(), e.getMessage());
            return loadStubApps();
        } catch (Exception e) {
            logger.error("Unexpected error fetching apps from API, falling back to stub", e);
            return loadStubApps();
        }
    }

    @Override
    public int getLastPromotionStep(String guildId, String appId) {
        if (guildId == null || guildId.isBlank() || appId == null || appId.isBlank()) {
            return 0;
        }

        Map<String, PromotionStepRecord> guildSteps = promotionSteps.get(guildId);
        if (guildSteps == null) {
            return 0;
        }

        PromotionStepRecord record = guildSteps.get(appId);
        return record != null ? record.lastStep : 0;
    }

    @Override
    public void recordPromotionStep(String guildId, String appId, int step, Instant postTime) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        if (appId == null || appId.isBlank()) {
            throw new IllegalArgumentException("appId cannot be null or blank");
        }
        if (step < 1 || step > 4) {
            throw new IllegalArgumentException("step must be between 1 and 4");
        }
        if (postTime == null) {
            throw new IllegalArgumentException("postTime cannot be null");
        }

        promotionSteps.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>())
                .put(appId, new PromotionStepRecord(step, postTime));

        logger.debug("Recorded promotion step {} for app {} in guild {} at {}",
                step, appId, guildId, postTime);
    }

    @Override
    public boolean hasAppBeenPromoted(String guildId, String appId) {
        return getLastPromotionStep(guildId, appId) > 0;
    }

    @Override
    public Instant getLastAppPostTime(String guildId, String appId) {
        if (guildId == null || guildId.isBlank() || appId == null || appId.isBlank()) {
            return null;
        }

        Map<String, PromotionStepRecord> guildSteps = promotionSteps.get(guildId);
        if (guildSteps == null) {
            return null;
        }

        PromotionStepRecord record = guildSteps.get(appId);
        return record != null ? record.lastPostTime : null;
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
        stats.put("total_promoted_apps", promotionSteps.values().stream()
                .mapToInt(Map::size)
                .sum());
        return stats;
    }
}
