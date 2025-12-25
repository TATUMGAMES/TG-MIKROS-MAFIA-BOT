package com.tatumgames.mikros.games.rpg.exploration;

/**
 * Enum representing different types of negative exploration events.
 * Events are categorized into three tiers based on severity.
 */
public enum ExplorationEventType {
    // Tier 1: Minor Setbacks (Most Common - 75% of negative events)
    SLIPPED_ON_ICE("🧊", "Slipped on Ice", ExplorationEventTier.TIER_1),
    PICKPOCKETED_BY_THIEVES("🧤", "Pickpocketed by Thieves", ExplorationEventTier.TIER_1),
    MOCKED_BY_ANGRY_MOB("😠", "Mocked by Angry Mob", ExplorationEventTier.TIER_1),
    LOST_IN_THE_FOG("🌫️", "Lost in the Fog", ExplorationEventTier.TIER_1),
    
    // Tier 2: Dangerous Encounters (Rare - 20% of negative events)
    AMBUSHED_BY_CREATURE("🐺", "Ambushed by a Lore Creature", ExplorationEventTier.TIER_2),
    FROSTBITE("🩸", "Frostbite", ExplorationEventTier.TIER_2),
    
    // Tier 3: Legendary Events (Ultra-Rare - 5% of negative events)
    TOUCHED_BY_DARK_RELIC("🕯️", "Touched by a Dark Relic", ExplorationEventTier.TIER_3),
    SEEN_BY_ANCIENT("👁️", "Seen by Something Ancient", ExplorationEventTier.TIER_3);
    
    private final String emoji;
    private final String displayName;
    private final ExplorationEventTier tier;
    
    ExplorationEventType(String emoji, String displayName, ExplorationEventTier tier) {
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
    
    public ExplorationEventTier getTier() {
        return tier;
    }
    
    /**
     * Enum for event tier classification.
     */
    public enum ExplorationEventTier {
        TIER_1, // Minor Setbacks
        TIER_2, // Dangerous Encounters
        TIER_3  // Legendary Events
    }
}

