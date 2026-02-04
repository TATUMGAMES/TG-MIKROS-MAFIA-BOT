package com.tatumgames.mikros.models;

/**
 * Enum representing the severity level of a moderation suggestion.
 */
public enum SuggestionSeverity {
    /**
     * Low severity - minor issues that should be monitored.
     */
    LOW("⚠️", "Low", "Minor issues - monitor"),

    /**
     * Medium severity - issues that warrant a warning.
     */
    MEDIUM("⚠️", "Medium", "Warning recommended"),

    /**
     * High severity - serious issues that may warrant a kick.
     */
    HIGH("🔸", "High", "Consider kick"),

    /**
     * Critical severity - severe issues that warrant a ban.
     */
    CRITICAL("🔴", "Critical", "Ban recommended");

    private final String emoji;
    private final String label;
    private final String description;

    SuggestionSeverity(String emoji, String label, String description) {
        this.emoji = emoji;
        this.label = label;
        this.description = description;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
