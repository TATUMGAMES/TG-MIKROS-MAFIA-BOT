package com.tatumgames.mikros.botdetection.model;

import com.tatumgames.mikros.botdetection.config.BotDetectionConfig;

/**
 * Result of bot detection analysis.
 */
public record BotDetectionResult(
        boolean isBotDetected,
        DetectionReason detectionReason,
        Confidence confidence,
        BotDetectionConfig.AutoAction recommendedAction,
        String details
) {
    /**
     * Creates a result indicating no bot was detected.
     */
    public static BotDetectionResult noDetection() {
        return new BotDetectionResult(false, null, null, BotDetectionConfig.AutoAction.NONE, null);
    }

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
}

