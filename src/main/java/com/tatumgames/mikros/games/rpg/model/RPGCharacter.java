package com.tatumgames.mikros.games.rpg.model;

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
    private Instant lastActionTime; // Deprecated - kept for migration
    private Instant createdAt;
    
    // Action charge system (3 charges, refresh every 12h)
    private int actionCharges;
    private Instant lastChargeRefreshTime;
    
    // Death and recovery system
    private boolean isDead;
    private boolean isRecovering;
    private Instant recoverUntil;
    
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
        
        // Initialize action charge system (3 charges max)
        this.actionCharges = 3;
        this.lastChargeRefreshTime = Instant.now();
        
        // Initialize death/recovery system
        this.isDead = false;
        this.isRecovering = false;
        this.recoverUntil = null;
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
     * Checks if the character can perform an action (charge-based system).
     * 
     * @param refreshHours the charge refresh period in hours (default: 12)
     * @return true if action is available
     */
    public boolean canPerformAction(int refreshHours) {
        // Refresh charges if needed
        refreshCharges(refreshHours);
        
        // Cannot act if dead or recovering
        if (isDead || isRecovering) {
            return false;
        }
        
        // Check if has charges
        return actionCharges > 0;
    }
    
    /**
     * Refreshes action charges if the refresh period has passed.
     * 
     * @param refreshHours the charge refresh period in hours (default: 12)
     */
    public void refreshCharges(int refreshHours) {
        if (lastChargeRefreshTime == null) {
            lastChargeRefreshTime = Instant.now();
            actionCharges = 3;
            return;
        }
        
        Instant now = Instant.now();
        long hoursSinceRefresh = (now.getEpochSecond() - lastChargeRefreshTime.getEpochSecond()) / 3600;
        
        if (hoursSinceRefresh >= refreshHours) {
            // Calculate how many full refresh cycles have passed
            int refreshCycles = (int) (hoursSinceRefresh / refreshHours);
            actionCharges = Math.min(3, actionCharges + refreshCycles * 3);
            lastChargeRefreshTime = now;
        }
    }
    
    /**
     * Uses an action charge.
     * 
     * @return true if charge was used successfully
     */
    public boolean useActionCharge() {
        if (actionCharges > 0) {
            actionCharges--;
            // Keep lastActionTime for backward compatibility
            this.lastActionTime = Instant.now();
            return true;
        }
        return false;
    }
    
    /**
     * Gets the time remaining until next charge refresh.
     * 
     * @param refreshHours the charge refresh period in hours
     * @return seconds remaining, or 0 if charges are full
     */
    public long getSecondsUntilChargeRefresh(int refreshHours) {
        if (actionCharges >= 3) {
            return 0;
        }
        
        if (lastChargeRefreshTime == null) {
            return 0;
        }
        
        Instant nextRefresh = lastChargeRefreshTime.plusSeconds(refreshHours * 3600L);
        long remaining = nextRefresh.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }
    
    /**
     * Checks if the character can perform an action (cooldown check) - DEPRECATED.
     * Kept for backward compatibility during migration.
     * 
     * @param cooldownHours the cooldown in hours
     * @return true if action is available
     * @deprecated Use canPerformAction(int refreshHours) instead
     */
    @Deprecated
    public boolean canPerformActionOld(int cooldownHours) {
        if (lastActionTime == null) {
            return true;
        }
        
        Instant cooldownEnd = lastActionTime.plusSeconds(cooldownHours * 3600L);
        return Instant.now().isAfter(cooldownEnd);
    }
    
    /**
     * Gets the time remaining until next action is available - DEPRECATED.
     * 
     * @param cooldownHours the cooldown in hours
     * @return seconds remaining, or 0 if ready
     * @deprecated Use getSecondsUntilChargeRefresh(int) instead
     */
    @Deprecated
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
     * Now uses charge system instead of cooldown.
     */
    public void recordAction() {
        useActionCharge();
    }
    
    /**
     * Marks the character as dead.
     */
    public void die() {
        this.isDead = true;
        this.stats.setCurrentHp(0);
    }
    
    /**
     * Revives the character at 50% HP and starts recovery period.
     * 
     * @param recoveryHours the recovery period in hours (default: 24)
     */
    public void resurrect(int recoveryHours) {
        this.isDead = false;
        this.isRecovering = true;
        // Set HP to 50% of max
        this.stats.setCurrentHp(this.stats.getMaxHp() / 2);
        this.recoverUntil = Instant.now().plusSeconds(recoveryHours * 3600L);
    }
    
    /**
     * Checks if recovery period has ended and clears it if so.
     */
    public void checkRecovery() {
        if (isRecovering && recoverUntil != null) {
            if (Instant.now().isAfter(recoverUntil)) {
                this.isRecovering = false;
                this.recoverUntil = null;
            }
        }
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
    
    // Action charge system getters/setters
    
    public int getActionCharges() {
        return actionCharges;
    }
    
    public void setActionCharges(int actionCharges) {
        this.actionCharges = Math.max(0, Math.min(3, actionCharges));
    }
    
    public Instant getLastChargeRefreshTime() {
        return lastChargeRefreshTime;
    }
    
    public void setLastChargeRefreshTime(Instant lastChargeRefreshTime) {
        this.lastChargeRefreshTime = lastChargeRefreshTime;
    }
    
    // Death/recovery system getters/setters
    
    public boolean isDead() {
        return isDead;
    }
    
    public void setIsDead(boolean isDead) {
        this.isDead = isDead;
    }
    
    public boolean isRecovering() {
        checkRecovery(); // Auto-check recovery status
        return isRecovering;
    }
    
    public void setIsRecovering(boolean isRecovering) {
        this.isRecovering = isRecovering;
    }
    
    public Instant getRecoverUntil() {
        return recoverUntil;
    }
    
    public void setRecoverUntil(Instant recoverUntil) {
        this.recoverUntil = recoverUntil;
    }
}

