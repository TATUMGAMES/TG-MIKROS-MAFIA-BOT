package com.tatumgames.mikros.games.rpg.events;

/**
 * Enum representing server-wide Nilfheim events that affect all players.
 * These events occur every 48-96 hours (randomized) and last for 12 hours.
 */
public enum NilfheimEventType {
    TWIN_MOONS_ALIGN(
            "The Twin Moons Align",
            "The twin moons of Nilfheim align, their light revealing hidden treasures. Essences shimmer more brightly in the frozen landscape.",
            60, 72, // Hours between events (60-72)
            0.10, // +10% essence drop chance on Explore
            EventEffectType.EXPLORE_DROP_BOOST
    ),
    STORMWARDEN_BLESSING(
            "A Stormwarden's Blessing",
            "A powerful Stormwarden passes through Nilfheim, leaving behind crackling Gale energy. Heroes feel the wind's favor in battle.",
            48, 60, // Hours between events (48-60)
            0.05, // +5% damage on Battle
            EventEffectType.BATTLE_DAMAGE_BOOST
    ),
    GRAND_LIBRARY_OPENS(
            "The Grand Library Opens",
            "The Grand Library of Nil City opens its forbidden section. Ancient knowledge flows through Nilfheim, enhancing all training.",
            72, 84, // Hours between events (72-84)
            1, // +1 guaranteed stat point on next Train
            EventEffectType.TRAIN_STAT_BOOST
    ),
    FROSTBORNE_ECHOES(
            "Frostborne Echoes",
            "The spirits of ancient Frostborne warriors echo through Nilfheim. Their battle cries strengthen all heroes fighting bosses.",
            54, 66, // Hours between events (54-66)
            0.08, // +8% damage to bosses
            EventEffectType.BOSS_DAMAGE_BOOST
    ),
    SPIRIT_VEIL_THINS(
            "The Spirit Veil Thins",
            "The boundary between the Mortal and Arcane Veils weakens. Rare catalysts drift into the mortal realm.",
            66, 78, // Hours between events (66-78)
            0.15, // +15% catalyst drop chance on Explore
            EventEffectType.EXPLORE_CATALYST_BOOST
    ),
    STARFALL_RIDGE_LIGHT(
            "Starfall Ridge's Light",
            "Star fragments from Starfall Ridge rain down, their cosmic energy empowering all heroes. Experience flows more freely.",
            84, 96, // Hours between events (84-96, rarest)
            0.15, // +15% XP on all actions
            EventEffectType.ALL_XP_BOOST
    );

    private final String displayName;
    private final String description;
    private final int minHoursBetween;
    private final int maxHoursBetween;
    private final double effectValue;
    private final EventEffectType effectType;

    NilfheimEventType(String displayName, String description, int minHoursBetween, int maxHoursBetween,
                     double effectValue, EventEffectType effectType) {
        this.displayName = displayName;
        this.description = description;
        this.minHoursBetween = minHoursBetween;
        this.maxHoursBetween = maxHoursBetween;
        this.effectValue = effectValue;
        this.effectType = effectType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getMinHoursBetween() {
        return minHoursBetween;
    }

    public int getMaxHoursBetween() {
        return maxHoursBetween;
    }

    public double getEffectValue() {
        return effectValue;
    }

    public EventEffectType getEffectType() {
        return effectType;
    }

    /**
     * Enum representing the type of effect an event has.
     */
    public enum EventEffectType {
        EXPLORE_DROP_BOOST,      // Increases essence drop chance on Explore
        BATTLE_DAMAGE_BOOST,     // Increases damage on Battle
        TRAIN_STAT_BOOST,        // Grants +1 stat point on Train
        BOSS_DAMAGE_BOOST,       // Increases damage to bosses
        EXPLORE_CATALYST_BOOST,  // Increases catalyst drop chance on Explore
        ALL_XP_BOOST             // Increases XP on all actions
    }
}

