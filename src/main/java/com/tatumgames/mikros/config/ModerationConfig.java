package com.tatumgames.mikros.config;

/**
 * Configuration constants for the moderation system.
 */
public class ModerationConfig {

    /**
     * Number of warnings before auto-escalation is triggered.
     */
    public static final int AUTO_ESCALATION_WARNING_THRESHOLD = 3;

    /**
     * Whether auto-escalation is enabled by default.
     */
    public static final boolean AUTO_ESCALATION_ENABLED_DEFAULT = true;

    /**
     * Number of recent messages to analyze for warn/ban suggestions.
     */
    public static final int MESSAGE_ANALYSIS_LIMIT = 200;

    /**
     * Maximum number of suggestions to return.
     */
    public static final int MAX_SUGGESTIONS = 10;

    /**
     * Day of month to send monthly reports (1-28).
     */
    public static final int MONTHLY_REPORT_DAY = 1;

    /**
     * Hour of day to send monthly reports (0-23, in server timezone).
     */
    public static final int MONTHLY_REPORT_HOUR = 9;

    /**
     * Number of top contributors to show in leaderboard.
     */
    public static final int TOP_CONTRIBUTORS_COUNT = 10;

    private ModerationConfig() {
        // Private constructor to prevent instantiation
    }
}

