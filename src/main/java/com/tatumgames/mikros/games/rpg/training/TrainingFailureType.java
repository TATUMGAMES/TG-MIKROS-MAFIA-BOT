package com.tatumgames.mikros.games.rpg.training;

/**
 * Enum representing different types of training failures. Failures are categorized into tiers based
 * on severity.
 */
public enum TrainingFailureType {
  // Tier 1: Minor Failures (Most Common - 70% of failures)
  POOR_FORM("❌", "Poor Form", TrainingFailureTier.TIER_1),

  // Tier 2: Moderate Failures (Rare - 25% of failures)
  EXHAUSTION("😴", "Exhaustion", TrainingFailureTier.TIER_2),

  // Tier 3: Severe Failures (Very Rare - 5% of failures)
  TRAINING_SETBACK("📉", "Training Setback", TrainingFailureTier.TIER_3);

  private final String emoji;
  private final String displayName;
  private final TrainingFailureTier tier;

  TrainingFailureType(String emoji, String displayName, TrainingFailureTier tier) {
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

  public TrainingFailureTier getTier() {
    return tier;
  }

  /** Enum for failure tier classification. */
  public enum TrainingFailureTier {
    TIER_1, // Minor Failures
    TIER_2, // Moderate Failures
    TIER_3 // Severe Failures
  }
}
