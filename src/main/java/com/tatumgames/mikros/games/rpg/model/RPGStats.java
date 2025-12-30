package com.tatumgames.mikros.games.rpg.model;

/**
 * Represents the stats for an RPG character.
 * Stats grow with leveling and affect action outcomes.
 */
public class RPGStats {
    private int maxHp;
    private int currentHp;
    private int strength;
    private int agility;
    private int intelligence;
    private int luck;

    /**
     * Creates RPG stats from a character class.
     *
     * @param characterClass the character class
     */
    public RPGStats(CharacterClass characterClass) {
        this.maxHp = characterClass.getBaseHp();
        this.currentHp = this.maxHp;
        this.strength = characterClass.getBaseStr();
        this.agility = characterClass.getBaseAgi();
        this.intelligence = characterClass.getBaseInt();
        this.luck = characterClass.getBaseLuck();
    }

    /**
     * Creates RPG stats with specific values.
     *
     * @param maxHp        maximum hit points
     * @param currentHp    current hit points
     * @param strength     strength stat
     * @param agility      agility stat
     * @param intelligence intelligence stat
     * @param luck         luck stat
     */
    public RPGStats(int maxHp, int currentHp, int strength, int agility, int intelligence, int luck) {
        this.maxHp = maxHp;
        this.currentHp = currentHp;
        this.strength = strength;
        this.agility = agility;
        this.intelligence = intelligence;
        this.luck = luck;
    }

    /**
     * Applies stat growth when leveling up.
     * Level up: +5 HP, +1 to all stats (as per TASKS_23.md).
     *
     * @param characterClass the character's class
     */
    public void applyLevelUpGrowth(CharacterClass characterClass) {
        // +5 HP on level up
        this.maxHp += 5;
        this.currentHp = this.maxHp; // Heal on level up

        // +1 to all stats
        this.strength += 1;
        this.agility += 1;
        this.intelligence += 1;
        this.luck += 1;
    }

    /**
     * Randomly increases a stat (used in training).
     *
     * @param statName the stat to increase (HP, STR, AGI, INT, LUCK)
     * @param amount   the amount to increase
     */
    public void increaseStat(String statName, int amount) {
        switch (statName.toUpperCase()) {
            case "HP" -> {
                this.maxHp += amount;
                this.currentHp += amount;
            }
            case "STR", "STRENGTH" -> this.strength += amount;
            case "AGI", "AGILITY" -> this.agility += amount;
            case "INT", "INTELLIGENCE" -> this.intelligence += amount;
            case "LUCK" -> this.luck += amount;
        }
    }

    /**
     * Damages the character.
     *
     * @param damage the damage amount
     * @return true if character is still alive
     */
    public boolean takeDamage(int damage) {
        this.currentHp = Math.max(0, this.currentHp - damage);
        return this.currentHp > 0;
    }

    /**
     * Heals the character.
     *
     * @param amount the heal amount
     */
    public void heal(int amount) {
        this.currentHp = Math.min(this.maxHp, this.currentHp + amount);
    }

    /**
     * Fully heals the character.
     */
    public void fullHeal() {
        this.currentHp = this.maxHp;
    }

    // Getters and setters

    public int getMaxHp() {
        return maxHp;
    }

    /**
     * Gets the effective max HP after applying curse effects.
     *
     * @param activeCurses list of active world curses
     * @return effective max HP (cannot go below 1)
     */
    public int getEffectiveMaxHp(java.util.List<com.tatumgames.mikros.games.rpg.curse.WorldCurse> activeCurses) {
        return getEffectiveMaxHp(activeCurses, false);
    }

    /**
     * Gets the effective max HP after applying curse effects and frostbite.
     *
     * @param activeCurses list of active world curses
     * @param hasFrostbite whether the character has frostbite (-5% max HP)
     * @return effective max HP (cannot go below 1)
     */
    public int getEffectiveMaxHp(java.util.List<com.tatumgames.mikros.games.rpg.curse.WorldCurse> activeCurses, boolean hasFrostbite) {
        int effectiveMaxHp = maxHp;
        
        // Apply Curse of Frailty (-10% HP)
        if (activeCurses != null && activeCurses.contains(com.tatumgames.mikros.games.rpg.curse.WorldCurse.MINOR_CURSE_OF_FRAILTY)) {
            effectiveMaxHp = (int) (effectiveMaxHp * 0.90);
        }
        
        // Apply Frostbite (-5% max HP)
        if (hasFrostbite) {
            effectiveMaxHp = (int) (effectiveMaxHp * 0.95);
        }
        
        // Ensure minimum 1 HP
        return Math.max(1, effectiveMaxHp);
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = Math.max(0, Math.min(maxHp, currentHp));
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getAgility() {
        return agility;
    }

    public void setAgility(int agility) {
        this.agility = agility;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }
}

