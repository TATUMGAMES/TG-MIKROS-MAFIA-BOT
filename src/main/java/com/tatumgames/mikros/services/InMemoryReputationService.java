package com.tatumgames.mikros.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tatumgames.mikros.models.BehaviorReport;
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

    /**
     * Creates a new InMemoryReputationService.
     */
    public InMemoryReputationService() {
        this.reportStore = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
        logger.info("InMemoryReputationService initialized");
    }

    @Override
    public void recordBehavior(BehaviorReport report) {
        if (report == null) {
            throw new IllegalArgumentException("Report cannot be null");
        }

        String key = buildKey(report.guildId(), report.targetUserId());
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

        // Call trackPlayerRating API
        return trackPlayerRating(request);
    }

    @Override
    public boolean trackPlayerRating(TrackPlayerRatingRequest request) {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("stubs/trackPlayerRating.json");

            if (inputStream == null) {
                logger.error("Could not find stub JSON file: stubs/trackPlayerRating.json");
                return false;
            }

            TrackPlayerRatingResponse response = objectMapper.readValue(inputStream, TrackPlayerRatingResponse.class);

            if (response.getStatus() != null && response.getStatus().getStatusCode() == 200) {
                logger.info("Successfully tracked player rating for request: {}", request);
                return true;
            } else {
                logger.warn("Track player rating returned non-200 status: {}", response.getStatus());
                return false;
            }

        } catch (Exception e) {
            logger.error("Failed to load stub JSON file for trackPlayerRating", e);
            return false;
        }
    }

    @Override
    public GetUserScoreDetailResponse getUserScoreDetail(List<String> usernames) {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("stubs/getUserScoreDetail.json");

            if (inputStream == null) {
                logger.error("Could not find stub JSON file: stubs/getUserScoreDetail.json");
                return null;
            }

            GetUserScoreDetailResponse response = objectMapper.readValue(inputStream, GetUserScoreDetailResponse.class);

            // Filter results by requested usernames if provided
            if (usernames != null && !usernames.isEmpty() && response.getData() != null && response.getData().getScores() != null) {
                List<GetUserScoreDetailResponse.UserScore> filteredScores = response.getData().getScores().stream()
                        .filter(score -> usernames.contains(score.getDiscordUsername()))
                        .collect(Collectors.toList());

                // Create new response with filtered scores
                GetUserScoreDetailResponse filteredResponse = new GetUserScoreDetailResponse();
                filteredResponse.setStatus(response.getStatus());
                GetUserScoreDetailResponse.Data data = new GetUserScoreDetailResponse.Data();
                data.setScores(filteredScores);
                filteredResponse.setData(data);

                logger.info("Found {} matching scores for usernames: {}", filteredScores.size(), usernames);
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

