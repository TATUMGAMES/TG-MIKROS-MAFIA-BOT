package com.tatumgames.mikros.games.rpg.model;

/**
 * Enum representing backlash event types that can occur for Oathbreakers at high corruption.
 * These events represent the consequences of the broken oath.
 */
public enum BacklashEventType {
    ELITE_REINFORCEMENT(
            "Elite Reinforcement",
            "The broken oath draws another elite to the fight! Your corruption has attracted dangerous attention.",
            BacklashEffectType.ELITE_SPAWN
    ),
    TEMPORARY_CURSE(
            "Temporary Curse",
            "Corruption surges, cursing you with dark energy. The broken oath's power turns against you.",
            BacklashEffectType.TEMPORARY_CURSE
    ),
    DEMON_WHISPER(
            "Demon Whisper",
            "A demon's voice echoes in your mind, offering power in exchange for deeper corruption.",
            BacklashEffectType.POWER_OFFER
    ),
    CORRUPTION_SURGE(
            "Corruption Surge",
            "The broken oath lashes out violently, dealing damage as corruption overwhelms you.",
            BacklashEffectType.DAMAGE
    ),
    GODS_WRATH(
            "God's Wrath",
            "The gods punish your broken oath, weakening your resolve and power.",
            BacklashEffectType.STAT_PENALTY
    );

    private final String displayName;
    private final String description;
    private final BacklashEffectType effectType;

    BacklashEventType(String displayName, String description, BacklashEffectType effectType) {
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public BacklashEffectType getEffectType() {
        return effectType;
    }

    /**
     * Enum representing the type of effect a backlash event has.
     */
    public enum BacklashEffectType {
        ELITE_SPAWN,        // Spawns additional elite enemy
        TEMPORARY_CURSE,    // Applies temporary curse (12h)
        POWER_OFFER,        // Offers power (+corruption, +damage this battle)
        DAMAGE,             // Deals damage (5-10% max HP)
        STAT_PENALTY        // Reduces stats this battle (-5% to all)
    }
}
