package com.tatumgames.mikros.games.rpg.model;

/**
 * Enum representing enemy types for regular battles.
 * Determines stat effectiveness multipliers (STR/INT/AGI).
 */
public enum EnemyType {
    PHYSICAL("Physical", "⚔️"),
    MAGICAL("Magical", "🔮"),
    AGILE("Agile", "🌪️"),
    UNDEAD("Undead", "💀"),
    BEAST("Beast", "🐺"),
    CONSTRUCT("Construct", "🤖");

    private final String displayName;
    private final String emoji;

    EnemyType(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }
}

