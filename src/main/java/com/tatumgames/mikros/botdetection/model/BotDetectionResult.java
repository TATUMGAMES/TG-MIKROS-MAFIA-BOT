package com.tatumgames.mikros.botdetection.model;

import com.tatumgames.mikros.botdetection.config.BotDetectionConfig;

/**
 * Result of bot detection analysis.
 */
public class BotDetectionResult {
    private final boolean isBotDetected;
    private final DetectionReason detectionReason;
    private final Confidence confidence;
    private final BotDetectionConfig.AutoAction recommendedAction;
    private final String details;

    /**
     * Enum representing the reason for bot detection.
     */
    public enum DetectionReason {
        ACCOUNT_TOO_NEW,
        MULTI_CHANNEL_SPAM,
        JOIN_AND_LINK,
        SUSPICIOUS_DOMAIN,
        URL_SHORTENER
    }

    /**
     * Enum representing confidence level of detection.
     */
    public enum Confidence {
        LOW,
        MEDIUM,
        HIGH
    }

    /**
     * Creates a new BotDetectionResult.
     *
     * @param isBotDetected      whether a bot was detected
     * @param detectionReason    the reason for detection
     * @param confidence         the confidence level
     * @param recommendedAction  the recommended action
     * @param details           additional details
     */
    public BotDetectionResult(boolean isBotDetected, DetectionReason detectionReason,
                              Confidence confidence, BotDetectionConfig.AutoAction recommendedAction,
                              String details) {
        this.isBotDetected = isBotDetected;
        this.detectionReason = detectionReason;
        this.confidence = confidence;
        this.recommendedAction = recommendedAction;
        this.details = details;
    }

    /**
     * Creates a result indicating no bot was detected.
     */
    public static BotDetectionResult noDetection() {
        return new BotDetectionResult(false, null, null, BotDetectionConfig.AutoAction.NONE, null);
    }

    public boolean isBotDetected() {
        return isBotDetected;
    }

    public DetectionReason getDetectionReason() {
        return detectionReason;
    }

    public Confidence getConfidence() {
        return confidence;
    }

    public BotDetectionConfig.AutoAction getRecommendedAction() {
        return recommendedAction;
    }

    public String getDetails() {
        return details;
    }
}

