package com.tatumgames.mikros.games.rpg.training;

/**
 * Enum representing different types of training accidents. Accidents are categorized into tiers
 * based on severity.
 */
public enum TrainingAccidentType {
    // Tier 1: Minor Accidents (Most Common - 70% of accidents)
    OVEREXERTION("💥", "Overexertion", TrainingAccidentTier.TIER_1),

    // Tier 2: Moderate Accidents (Rare - 25% of accidents)
    TRAINING_INJURY("🩹", "Training Injury", TrainingAccidentTier.TIER_2),

    // Tier 3: Severe Accidents (Very Rare - 5% of accidents)
    MUSCLE_STRAIN("⚡", "Muscle Strain", TrainingAccidentTier.TIER_3);

    private final String emoji;
    private final String displayName;
    private final TrainingAccidentTier tier;

    TrainingAccidentType(String emoji, String displayName, TrainingAccidentTier tier) {
        this.emoji = emoji;
        this.displayName = displayName;
        this.tier = tier;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TrainingAccidentTier getTier() {
        return tier;
    }

    /**
     * Enum for accident tier classification.
     */
    public enum TrainingAccidentTier {
        TIER_1, // Minor Accidents
        TIER_2, // Moderate Accidents
        TIER_3 // Severe Accidents
    }
}
