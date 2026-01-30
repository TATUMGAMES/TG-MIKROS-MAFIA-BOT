package com.tatumgames.mikros.games.rpg.training;

/**
 * Enum representing different types of risky training methods.
 * Risky methods offer high rewards but come with significant risks.
 */
public enum RiskyTrainingMethodType {
    // Moderate Risk/Reward
    PUSH_BEYOND_LIMITS("🔥", "Push Beyond Limits", 0.50, 0.50, 0.05, 0.10),

    // High Risk/Reward
    DANGEROUS_TECHNIQUE("⚔️", "Dangerous Technique", 0.75, 0.75, 0.10, 0.0),

    // Extreme Risk/Reward
    EXTREME_TRAINING("💀", "Extreme Training", 1.00, 1.00, 0.08, 0.12);

    private final String emoji;
    private final String displayName;
    private final double xpMultiplier; // XP bonus multiplier (0.50 = +50% XP)
    private final double statBonusMultiplier; // Stat bonus multiplier (0.50 = +1 extra stat point)
    private final double minHpLossPercent; // Minimum HP loss percentage
    private final double maxHpLossPercent; // Maximum HP loss percentage

    RiskyTrainingMethodType(String emoji, String displayName, double xpMultiplier,
                            double statBonusMultiplier, double minHpLossPercent, double maxHpLossPercent) {
        this.emoji = emoji;
        this.displayName = displayName;
        this.xpMultiplier = xpMultiplier;
        this.statBonusMultiplier = statBonusMultiplier;
        this.minHpLossPercent = minHpLossPercent;
        this.maxHpLossPercent = maxHpLossPercent;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getXpMultiplier() {
        return xpMultiplier;
    }

    public double getStatBonusMultiplier() {
        return statBonusMultiplier;
    }

    public double getMinHpLossPercent() {
        return minHpLossPercent;
    }

    public double getMaxHpLossPercent() {
        return maxHpLossPercent;
    }
}
