package com.tatumgames.mikros.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tatumgames.mikros.api.TatumGamesApiClient;
import com.tatumgames.mikros.models.BehaviorReport;
import com.tatumgames.mikros.models.api.GetUserScoreDetailRequest;
import com.tatumgames.mikros.models.api.GetUserScoreDetailResponse;
import com.tatumgames.mikros.models.api.TrackPlayerRatingRequest;
import com.tatumgames.mikros.models.api.TrackPlayerRatingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ReputationService.
 * Stores behavior reports and integrates with reputation API endpoints.
 */
public class InMemoryReputationService implements ReputationService {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryReputationService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Key format: "guildId:userId" -> List of behavior reports
    private final Map<String, List<BehaviorReport>> reportStore;
    private final ObjectMapper objectMapper;
    private final TatumGamesApiClient apiClient;
    private final String reputationApiBaseUrl; // Base URL (without /mikros/discord)
    private final String reputationApiKey;
    private final String apiKeyType;

    /**
     * Creates a new InMemoryReputationService.
     *
     * @param apiClient        the API client for making requests
     * @param reputationApiBaseUrl the base URL for the reputation API (e.g., https://tg-api-new.uc.r.appspot.com)
     * @param reputationApiKey the reputation API key
     * @param apiKeyType       the API key type (dev or prod)
     */
    public InMemoryReputationService(TatumGamesApiClient apiClient, String reputationApiBaseUrl,
                                     String reputationApiKey, String apiKeyType) {
        this.reportStore = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
        this.apiClient = apiClient;
        this.reputationApiBaseUrl = reputationApiBaseUrl;
        this.reputationApiKey = reputationApiKey;
        this.apiKeyType = apiKeyType;
        logger.info("InMemoryReputationService initialized with API key type: {} and base URL: {}", 
                apiKeyType, reputationApiBaseUrl);
    }

    @Override
    public void recordBehavior(BehaviorReport report) {
        if (report == null) {
            throw new IllegalArgumentException("Report cannot be null");
        }

        String key = buildKey(report.guildId(), report.targetUserId());
        reportStore.computeIfAbsent(key, k -> new ArrayList<>()).add(report);

        logger.info("Recorded behavior report: {}", report);
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
                .sorted(Comparator.comparing(BehaviorReport::timestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean reportToExternalAPI(BehaviorReport report) {
        // Create TrackPlayerRatingRequest from BehaviorReport
        TrackPlayerRatingRequest request = new TrackPlayerRatingRequest();

        // Set timestamp
        request.setTimestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER));

        // Set sender (reporter)
        TrackPlayerRatingRequest.Sender sender = new TrackPlayerRatingRequest.Sender();
        sender.setDiscordUserId(report.reporterId());
        sender.setDiscordUsername(report.reporterUsername());
        request.setSender(sender);

        // Set participant (target user)
        TrackPlayerRatingRequest.Participant participant = new TrackPlayerRatingRequest.Participant();
        participant.setDiscordUserId(report.targetUserId());
        participant.setDiscordUsername(report.targetUsername());
        participant.setValue(report.behaviorCategory().getWeight());
        request.setParticipants(List.of(participant));

        // Set platform to "discord"
        request.setPlatform("discord");

        // Set apiKeyType based on environment
        request.setApiKeyType(apiKeyType);

        // Call trackPlayerRating API
        return trackPlayerRating(request);
    }

    @Override
    public boolean trackPlayerRating(TrackPlayerRatingRequest request) {
        // If API key not configured, fall back to stub
        if (reputationApiKey == null || reputationApiKey.isBlank()) {
            logger.warn("Reputation API key not configured, using stub response");
            return loadStubResponse();
        }

        try {
            TrackPlayerRatingResponse response = apiClient.postWithApiKey(
                    reputationApiBaseUrl + "/mikros/discord", // Base URL with path
                    "/trackUserRating", // Endpoint
                    request,
                    reputationApiKey,
                    TrackPlayerRatingResponse.class
            );

            if (response != null && response.getStatus() != null &&
                    response.getStatus().getStatusCode() == 200) {
                logger.info("Successfully tracked player rating via API: {}", request);
                return true;
            } else {
                logger.warn("API returned non-200 status: {}",
                        response != null ? response.getStatus() : "null response");
                return false;
            }

        } catch (TatumGamesApiClient.ApiException e) {
            logger.error("Error calling trackUserRating API (status: {}): {}",
                    e.getStatusCode(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error calling trackUserRating API", e);
            return false;
        }
    }

    /**
     * Loads stub response as fallback when API is not available.
     *
     * @return true if stub response indicates success, false otherwise
     */
    private boolean loadStubResponse() {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("stubs/trackPlayerRating.json");

            if (inputStream == null) {
                logger.error("Could not find stub JSON file: stubs/trackPlayerRating.json");
                return false;
            }

            TrackPlayerRatingResponse response = objectMapper.readValue(inputStream, TrackPlayerRatingResponse.class);

            if (response.getStatus() != null && response.getStatus().getStatusCode() == 200) {
                logger.info("Successfully tracked player rating using stub response");
                return true;
            } else {
                logger.warn("Stub response returned non-200 status: {}", response.getStatus());
                return false;
            }

        } catch (Exception e) {
            logger.error("Failed to load stub JSON file for trackPlayerRating", e);
            return false;
        }
    }

    @Override
    public GetUserScoreDetailResponse getUserScoreDetail(List<String> usernames) {
        // If API key not configured, fall back to stub
        if (reputationApiKey == null || reputationApiKey.isBlank()) {
            logger.warn("Reputation API key not configured, using stub response");
            return loadStubResponse(usernames);
        }

        try {
            // Build request
            GetUserScoreDetailRequest request = new GetUserScoreDetailRequest();
            request.setApiKeyType(apiKeyType);

            // Set discordUserId (optional - can be null)
            // API may use first username if it's a user ID format, but for now leave null
            request.setDiscordUserId(null);

            // Set usernames list
            request.setDiscordUsernames(usernames);

            // Make API call
            GetUserScoreDetailResponse response = apiClient.postWithApiKey(
                    reputationApiBaseUrl + "/mikros/discord", // Base URL with path
                    "/getUserScoreDetails", // Endpoint (Note: plural "Details")
                    request,
                    reputationApiKey,
                    GetUserScoreDetailResponse.class
            );

            if (response != null && response.getStatus() != null &&
                    response.getStatus().getStatusCode() == 200) {
                logger.info("Successfully retrieved user score details for {} usernames",
                        usernames != null ? usernames.size() : 0);
                return response;
            } else {
                logger.warn("API returned non-200 status: {}",
                        response != null ? response.getStatus() : "null response");
                // Fall back to stub on non-200 response
                return loadStubResponse(usernames);
            }

        } catch (TatumGamesApiClient.ApiException e) {
            logger.error("Error calling getUserScoreDetails API (status: {}): {}",
                    e.getStatusCode(), e.getMessage(), e);
            // Fall back to stub on API error
            return loadStubResponse(usernames);
        } catch (Exception e) {
            logger.error("Unexpected error calling getUserScoreDetails API", e);
            // Fall back to stub on unexpected error
            return loadStubResponse(usernames);
        }
    }

    /**
     * Loads stub response as fallback when API is not available.
     *
     * @param usernames the list of usernames to filter (optional)
     * @return the stub response, or null if stub loading fails
     */
    private GetUserScoreDetailResponse loadStubResponse(List<String> usernames) {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("stubs/getUserScoreDetail.json");

            if (inputStream == null) {
                logger.error("Could not find stub JSON file: stubs/getUserScoreDetail.json");
                return null;
            }

            GetUserScoreDetailResponse response = objectMapper.readValue(
                    inputStream, GetUserScoreDetailResponse.class);

            // Filter results by requested usernames if provided
            if (usernames != null && !usernames.isEmpty() && response.getData() != null) {
                List<GetUserScoreDetailResponse.UserScore> filteredScores = response.getData().stream()
                        .filter(score -> usernames.contains(score.getUsername()))
                        .collect(Collectors.toList());

                // Create new response with filtered scores
                GetUserScoreDetailResponse filteredResponse = new GetUserScoreDetailResponse();
                filteredResponse.setStatus(response.getStatus());
                filteredResponse.setData(filteredScores);

                logger.info("Found {} matching scores for usernames: {} (from stub)",
                        filteredScores.size(), usernames);
                return filteredResponse;
            }

            logger.info("Loaded user score details from stub JSON");
            return response;

        } catch (Exception e) {
            logger.error("Failed to load stub JSON file for getUserScoreDetail", e);
            return null;
        }
    }

    /**
     * Builds a unique key for the report store.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
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
