package com.tatumgames.mikros.rpg.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a player's RPG character.
 * One character per Discord user ID.
 * 
 * TODO: Future Features
 * - Inventory system for items and equipment
 * - Quest progress tracking
 * - Achievement system
 * - Prestige levels after max level
 */
public class RPGCharacter {
    private final String discordId;
    private String name;
    private CharacterClass characterClass;
    private int level;
    private int xp;
    private int xpToNextLevel;
    private RPGStats stats;
    private Instant lastActionTime;
    private Instant createdAt;
    
    /**
     * Creates a new RPG character.
     * 
     * @param discordId the Discord user ID
     * @param name the character name
     * @param characterClass the character class
     */
    public RPGCharacter(String discordId, String name, CharacterClass characterClass) {
        this.discordId = Objects.requireNonNull(discordId);
        this.name = Objects.requireNonNull(name);
        this.characterClass = Objects.requireNonNull(characterClass);
        this.level = 1;
        this.xp = 0;
        this.xpToNextLevel = calculateXpForNextLevel(1);
        this.stats = new RPGStats(characterClass);
        this.lastActionTime = null;
        this.createdAt = Instant.now();
    }
    
    /**
     * Adds experience points and handles leveling up.
     * 
     * @param amount the XP to add
     * @return true if the character leveled up
     */
    public boolean addXp(int amount) {
        this.xp += amount;
        
        boolean leveledUp = false;
        while (this.xp >= this.xpToNextLevel) {
            levelUp();
            leveledUp = true;
        }
        
        return leveledUp;
    }
    
    /**
     * Levels up the character.
     */
    private void levelUp() {
        this.level++;
        this.xp -= this.xpToNextLevel;
        this.xpToNextLevel = calculateXpForNextLevel(this.level);
        this.stats.applyLevelUpGrowth(this.characterClass);
    }
    
    /**
     * Calculates XP required for next level.
     * Uses exponential growth formula.
     * 
     * @param currentLevel the current level
     * @return XP required for next level
     */
    private int calculateXpForNextLevel(int currentLevel) {
        // Formula: 100 * level^1.5
        return (int) (100 * Math.pow(currentLevel, 1.5));
    }
    
    /**
     * Checks if the character can perform an action (cooldown check).
     * 
     * @param cooldownHours the cooldown in hours
     * @return true if action is available
     */
    public boolean canPerformAction(int cooldownHours) {
        if (lastActionTime == null) {
            return true;
        }
        
        Instant cooldownEnd = lastActionTime.plusSeconds(cooldownHours * 3600L);
        return Instant.now().isAfter(cooldownEnd);
    }
    
    /**
     * Gets the time remaining until next action is available.
     * 
     * @param cooldownHours the cooldown in hours
     * @return seconds remaining, or 0 if ready
     */
    public long getSecondsUntilNextAction(int cooldownHours) {
        if (lastActionTime == null) {
            return 0;
        }
        
        Instant cooldownEnd = lastActionTime.plusSeconds(cooldownHours * 3600L);
        long remaining = cooldownEnd.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }
    
    /**
     * Records that an action was performed.
     */
    public void recordAction() {
        this.lastActionTime = Instant.now();
    }
    
    // Getters and setters
    
    public String getDiscordId() {
        return discordId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public CharacterClass getCharacterClass() {
        return characterClass;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getXp() {
        return xp;
    }
    
    public int getXpToNextLevel() {
        return xpToNextLevel;
    }
    
    public RPGStats getStats() {
        return stats;
    }
    
    public Instant getLastActionTime() {
        return lastActionTime;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
}

