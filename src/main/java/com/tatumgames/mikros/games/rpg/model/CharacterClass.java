package com.tatumgames.mikros.games.rpg.model;

/**
 * Enum representing available character classes in the RPG system.
 * Each class has unique base stats and gameplay styles.
 */
public enum CharacterClass {
    /**
     * Warrior - High HP and Strength, moderate agility.
     * Role: Bruiser / Tank
     * Focus: Melee combat and endurance.
     */
    WARRIOR("Warrior", "⚔️",
            110, // Base HP
            17,  // Base STR
            8,   // Base AGI
            5,   // Base INT
            7    // Base LUCK
    ),

    /**
     * Knight - Massive HP and Defense, low agility and luck.
     * Role: Full Tank
     * Focus: Tanking, reduced incoming damage by 15%
     */
    KNIGHT("Knight", "🛡️",
            135, // Base HP
            13,  // Base STR
            6,   // Base AGI
            6,   // Base INT
            5    // Base LUCK
    ),

    /**
     * Mage - High Intelligence, lower HP.
     * Role: Glass Cannon
     * Focus: Magic attacks and strategic gameplay.
     */
    MAGE("Mage", "🔮",
            70,  // Base HP
            5,   // Base STR
            7,   // Base AGI
            20,  // Base INT
            5    // Base LUCK
    ),

    /**
     * Rogue - High Agility and Luck, moderate HP.
     * Role: Crit / Dodge specialist
     * Focus: Speed, critical hits, and evasion.
     */
    ROGUE("Rogue", "🗡️",
            85,  // Base HP
            8,   // Base STR
            16,  // Base AGI
            7,   // Base INT
            12   // Base LUCK
    ),

    /**
     * Necromancer - Hybrid Mage + Rogue.
     * Role: Damage-over-time + crit-magic
     * Special: 10% chance to apply "Decay" (DoT), doubling XP from battles if triggered.
     * Focus: INT + LUCK based combat.
     */
    NECROMANCER("Necromancer", "💀",
            75,  // Base HP
            6,   // Base STR
            10,  // Base AGI
            15,  // Base INT
            10   // Base LUCK
    ),

    /**
     * Priest - Support class with healing and resurrection.
     * Role: Healer + Resurrector
     * Special: Can resurrect dead players (free action).
     * Focus: INT + supportive utility.
     */
    PRIEST("Priest", "🙏",
            90,  // Base HP
            5,   // Base STR
            6,   // Base AGI
            15,  // Base INT
            10   // Base LUCK
    );

    private final String displayName;
    private final String emoji;
    private final int baseHp;
    private final int baseStr;
    private final int baseAgi;
    private final int baseInt;
    private final int baseLuck;

    /**
     * Creates a CharacterClass.
     *
     * @param displayName the display name
     * @param emoji       the emoji representing the class
     * @param baseHp      base hit points
     * @param baseStr     base strength
     * @param baseAgi     base agility
     * @param baseInt     base intelligence
     * @param baseLuck    base luck
     */
    CharacterClass(String displayName, String emoji, int baseHp, int baseStr,
                   int baseAgi, int baseInt, int baseLuck) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.baseHp = baseHp;
        this.baseStr = baseStr;
        this.baseAgi = baseAgi;
        this.baseInt = baseInt;
        this.baseLuck = baseLuck;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public int getBaseStr() {
        return baseStr;
    }

    public int getBaseAgi() {
        return baseAgi;
    }

    public int getBaseInt() {
        return baseInt;
    }

    public int getBaseLuck() {
        return baseLuck;
    }
}

