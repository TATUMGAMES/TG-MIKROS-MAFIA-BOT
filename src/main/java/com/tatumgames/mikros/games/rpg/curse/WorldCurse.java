package com.tatumgames.mikros.games.rpg.curse;

/**
 * Enum representing World Curses that affect all players when a boss despawns undefeated.
 * Curses are temporary and affect everyone equally.
 */
public enum WorldCurse {
    // Minor World Curses (Normal Boss Failure)
    /**
     * Curse of Frailty
     * Max HP temporarily reduced by 10%
     */
    MINOR_CURSE_OF_FRAILTY("❄️ Curse of Frailty",
            "The cold seeps into bone and marrow.",
            CurseType.MINOR,
            CurseDuration.UNTIL_NEXT_SPAWN),

    /**
     * Curse of Weakness
     * STR effectiveness -10%
     */
    MINOR_CURSE_OF_WEAKNESS("🗡️ Curse of Weakness",
            "Steel feels heavier in your grasp.",
            CurseType.MINOR,
            CurseDuration.UNTIL_NEXT_SPAWN),

    /**
     * Curse of Sluggish Steps
     * AGI damage reduction cap reduced: 30% → 25%
     */
    MINOR_CURSE_OF_SLUGGISH_STEPS("🌪️ Curse of Sluggish Steps",
            "The winds of Nilfheim resist every movement.",
            CurseType.MINOR,
            CurseDuration.UNTIL_NEXT_SPAWN),

    /**
     * Curse of Clouded Mind
     * XP gained -5%
     */
    MINOR_CURSE_OF_CLOUDED_MIND("🔮 Curse of Clouded Mind",
            "Thoughts scatter like frostbitten ash.",
            CurseType.MINOR,
            CurseDuration.UNTIL_NEXT_SPAWN),

    /**
     * Curse of Ill Fortune
     * Item drop chance -5%
     */
    MINOR_CURSE_OF_ILL_FORTUNE("🍀 Curse of Ill Fortune",
            "Luck turns its gaze away.",
            CurseType.MINOR,
            CurseDuration.UNTIL_NEXT_SPAWN),

    /**
     * Curse of Bleeding Wounds
     * Battle defeat damage +10%
     */
    MINOR_CURSE_OF_BLEEDING_WOUNDS("🩸 Curse of Bleeding Wounds",
            "Wounds refuse to close.",
            CurseType.MINOR,
            CurseDuration.UNTIL_NEXT_SPAWN),

    /**
     * Curse of Waning Resolve
     * Battle victory XP variance shifts lower
     */
    MINOR_CURSE_OF_WANING_RESOLVE("🌫️ Curse of Waning Resolve",
            "Doubt gnaws at the spirit.",
            CurseType.MINOR,
            CurseDuration.UNTIL_NEXT_SPAWN),

    // Major World Curses (Super Boss Failure)
    /**
     * Eclipse of Nilfheim
     * Enemies deal +10% damage, next boss HP +10%
     */
    MAJOR_ECLIPSE_OF_NILFHEIM("🌑 Eclipse of Nilfheim",
            "The sky darkens. Hope thins.",
            CurseType.MAJOR,
            CurseDuration.UNTIL_NEXT_DEFEAT),

    /**
     * March of the Dead
     * Undead enemies appear more frequently, defeat damage +15%
     */
    MAJOR_MARCH_OF_THE_DEAD("💀 March of the Dead",
            "The fallen refuse to rest.",
            CurseType.MAJOR,
            CurseDuration.UNTIL_NEXT_DEFEAT),

    /**
     * Fading Hope
     * Resurrection recovery 24h → 36h, Priest resurrection XP doubled
     */
    MAJOR_FADING_HOPE("🕯️ Fading Hope",
            "The light grows harder to summon.",
            CurseType.MAJOR,
            CurseDuration.UNTIL_NEXT_DEFEAT),

    /**
     * Frozen Time
     * Action charge refresh +2 hours slower
     */
    MAJOR_FROZEN_TIME("🧊 Frozen Time",
            "Time itself slows beneath the frost.",
            CurseType.MAJOR,
            CurseDuration.UNTIL_NEXT_SPAWN),

    /**
     * Shattered Reality
     * Stat effectiveness modifiers fluctuate: 1.3x → 1.25x, 0.85x → 0.8x
     */
    MAJOR_SHATTERED_REALITY("🌌 Shattered Reality",
            "Reality fractures under eldritch strain.",
            CurseType.MAJOR,
            CurseDuration.UNTIL_NEXT_DEFEAT),

    /**
     * World Aflame
     * Boss damage variance ±25% → ±35%, critical hits +0.1x damage
     */
    MAJOR_WORLD_AFLAME("🔥 World Aflame",
            "Nilfheim burns with unnatural fury.",
            CurseType.MAJOR,
            CurseDuration.UNTIL_NEXT_DEFEAT),

    /**
     * Price of Survival
     * Battle victories restore less HP
     */
    MAJOR_PRICE_OF_SURVIVAL("🩸 Price of Survival",
            "Every victory exacts a toll.",
            CurseType.MAJOR,
            CurseDuration.UNTIL_NEXT_DEFEAT);

    private final String displayName;
    private final String description;
    private final CurseType type;
    private final CurseDuration duration;

    WorldCurse(String displayName, String description, CurseType type, CurseDuration duration) {
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.duration = duration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public CurseType getType() {
        return type;
    }

    public CurseDuration getDuration() {
        return duration;
    }

    /**
     * Curse type (Minor or Major).
     */
    public enum CurseType {
        MINOR,  // From normal boss failure
        MAJOR   // From super boss failure
    }

    /**
     * Curse duration type.
     */
    public enum CurseDuration {
        UNTIL_NEXT_SPAWN,   // Cleared when next boss spawns
        UNTIL_NEXT_DEFEAT   // Cleared when next boss is defeated
    }
}

