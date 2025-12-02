package com.tatumgames.mikros.models;

/**
 * Enum representing promotion frequency levels.
 * Controls how often game promotions are posted.
 */
public enum PromotionVerbosity {
    /**
     * Low verbosity - posts every 24 hours.
     */
    LOW("Low", 24),

    /**
     * Medium verbosity - posts every 12 hours (default).
     */
    MEDIUM("Medium", 12),

    /**
     * High verbosity - posts every 6 hours.
     */
    HIGH("High", 6);

    private final String label;
    private final int hoursInterval;

    /**
     * Creates a PromotionVerbosity level.
     *
     * @param label         the display label
     * @param hoursInterval hours between promotion checks
     */
    PromotionVerbosity(String label, int hoursInterval) {
        this.label = label;
        this.hoursInterval = hoursInterval;
    }

    /**
     * Gets the display label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the hours interval for this verbosity level.
     *
     * @return hours between promotion checks
     */
    public int getHoursInterval() {
        return hoursInterval;
    }
}

