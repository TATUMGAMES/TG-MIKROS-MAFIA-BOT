package com.tatumgames.mikros.games.rpg.blessing;

/**
 * Enum representing the tier/type of blessing.
 * Blessings are granted based on consecutive boss failures.
 */
public enum BlessingType {
    /**
     * Minor Blessing - granted after 5 consecutive failures.
     */
    MINOR("Minor Blessing", "✨", 5),

    /**
     * Major Blessing - granted after 10 consecutive failures.
     */
    MAJOR("Major Blessing", "🌟", 10),

    /**
     * Legendary Blessing - granted after 15+ consecutive failures.
     */
    LEGENDARY("Legendary Blessing", "💫", 15);

    private final String displayName;
    private final String emoji;
    private final int requiredFailures;

    BlessingType(String displayName, String emoji, int requiredFailures) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.requiredFailures = requiredFailures;
    }

    /**
     * Gets the blessing type for a given number of consecutive failures.
     *
     * @param consecutiveFailures the number of consecutive failures
     * @return the appropriate blessing type, or null if not enough failures
     */
    public static BlessingType forFailures(int consecutiveFailures) {
        if (consecutiveFailures >= LEGENDARY.requiredFailures) {
            return LEGENDARY;
        } else if (consecutiveFailures >= MAJOR.requiredFailures) {
            return MAJOR;
        } else if (consecutiveFailures >= MINOR.requiredFailures) {
            return MINOR;
        }
        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getRequiredFailures() {
        return requiredFailures;
    }
}
