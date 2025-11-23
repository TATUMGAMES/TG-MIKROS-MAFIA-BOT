package com.tatumgames.mikros.rpg.model;

/**
 * Enum representing available character classes in the RPG system.
 * Each class has unique base stats and gameplay styles.
 */
public enum CharacterClass {
    /**
     * Warrior - High HP and Strength, moderate agility.
     * Focus: Melee combat and endurance.
     */
    WARRIOR("Warrior", "⚔️", 
            120, // Base HP
            15,  // Base STR
            10,  // Base AGI
            8,   // Base INT
            10   // Base LUCK
    ),
    
    /**
     * Mage - High Intelligence, lower HP, moderate stats.
     * Focus: Magic attacks and strategic gameplay.
     */
    MAGE("Mage", "🔮",
            80,  // Base HP
            8,   // Base STR
            12,  // Base AGI
            18,  // Base INT
            12   // Base LUCK
    ),
    
    /**
     * Rogue - High Agility and Luck, moderate HP.
     * Focus: Speed, critical hits, and evasion.
     */
    ROGUE("Rogue", "🗡️",
            100, // Base HP
            12,  // Base STR
            18,  // Base AGI
            10,  // Base INT
            15   // Base LUCK
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
     * @param emoji the emoji representing the class
     * @param baseHp base hit points
     * @param baseStr base strength
     * @param baseAgi base agility
     * @param baseInt base intelligence
     * @param baseLuck base luck
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

