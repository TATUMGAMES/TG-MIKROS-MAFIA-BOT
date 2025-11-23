package com.tatumgames.mikros.rpg.model;

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
     * @param maxHp maximum hit points
     * @param currentHp current hit points
     * @param strength strength stat
     * @param agility agility stat
     * @param intelligence intelligence stat
     * @param luck luck stat
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
     * Growth is influenced by character class.
     * 
     * @param characterClass the character's class
     */
    public void applyLevelUpGrowth(CharacterClass characterClass) {
        // HP grows most
        this.maxHp += 10 + (int) (Math.random() * 5);
        this.currentHp = this.maxHp; // Heal on level up
        
        // Other stats grow based on class strengths
        switch (characterClass) {
            case WARRIOR -> {
                this.strength += 2 + (int) (Math.random() * 2);
                this.agility += 1;
                this.intelligence += 1;
                this.luck += 1;
            }
            case MAGE -> {
                this.strength += 1;
                this.agility += 1;
                this.intelligence += 2 + (int) (Math.random() * 2);
                this.luck += 1;
            }
            case ROGUE -> {
                this.strength += 1;
                this.agility += 2 + (int) (Math.random() * 2);
                this.intelligence += 1;
                this.luck += 1 + (int) (Math.random() * 2);
            }
        }
    }
    
    /**
     * Randomly increases a stat (used in training).
     * 
     * @param statName the stat to increase (HP, STR, AGI, INT, LUCK)
     * @param amount the amount to increase
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

