package com.tatumgames.mikros.games.rpg.model;

/**
 * Enum representing essence types that can be collected from actions.
 * Each essence is aligned with a specific stat.
 */
public enum EssenceType {
    EMBER_SHARD("Ember Shard", "🔥", "STR"),
    GALE_FRAGMENT("Gale Fragment", "🌪️", "AGI"),
    MIND_CRYSTAL("Mind Crystal", "🔮", "INT"),
    FATE_CLOVER("Fate Clover", "🍀", "LUCK"),
    VITAL_ASH("Vital Ash", "🩸", "HP");

    private final String displayName;
    private final String emoji;
    private final String statAlignment;

    EssenceType(String displayName, String emoji, String statAlignment) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.statAlignment = statAlignment;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getStatAlignment() {
        return statAlignment;
    }
}

