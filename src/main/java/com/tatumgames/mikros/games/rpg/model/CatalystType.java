package com.tatumgames.mikros.games.rpg.model;

/**
 * Enum representing catalyst types used in crafting.
 * Catalysts are rarer than essences and required for crafting.
 */
public enum CatalystType {
    ANCIENT_VIAL("Ancient Vial", "⚗️"),
    RUNIC_BINDING("Runic Binding", "📜"),
    MONSTER_CORE("Monster Core", "💎"),
    FROZEN_REAGENT("Frozen Reagent", "❄️");

    private final String displayName;
    private final String emoji;

    CatalystType(String displayName, String emoji) {
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

