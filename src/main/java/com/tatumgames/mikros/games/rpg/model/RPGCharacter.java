package com.tatumgames.mikros.games.rpg.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a player's RPG character.
 * One character per Discord user ID.
 * <p>
 * TODO: Future Features
 * - Inventory system for items and equipment
 * - Quest progress tracking
 * - Achievement system
 * - Prestige levels after max level
 */
public class RPGCharacter {
    private final String discordId;
    private String name;
    private final CharacterClass characterClass;
    private int level;
    private int xp;
    private int xpToNextLevel;
    private final RPGStats stats;
    private Instant lastActionTime; // Deprecated - kept for migration
    private final Instant createdAt;

    // Action charge system (3 charges, refresh every 12h)
    private int actionCharges;
    private Instant lastChargeRefreshTime;

    // Death and recovery system
    private boolean isDead;
    private boolean isRecovering;
    private Instant recoverUntil;

    // Kill counter system
    private int enemiesKilled = 0;
    private int bossesKilled = 0;
    private int superBossesKilled = 0;

    // Inventory system
    private RPGInventory inventory;

    // Dual system
    private int duelsWon = 0;
    private int duelsLost = 0;
    private Instant lastDuelTime;
    private int duelsInLast24Hours = 0;

    /**
     * Creates a new RPG character.
     *
     * @param discordId      the Discord user ID
     * @param name           the character name
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

        // Initialize action charge system (dynamic max based on level)
        this.actionCharges = getMaxActionCharges(); // Will return 3 for level 1
        this.lastChargeRefreshTime = Instant.now();

        // Initialize death/recovery system
        this.isDead = false;
        this.isRecovering = false;
        this.recoverUntil = null;

        // Initialize kill counters
        this.enemiesKilled = 0;
        this.bossesKilled = 0;
        this.superBossesKilled = 0;

        // Initialize inventory
        this.inventory = new RPGInventory();

        // Initialize dual tracking
        this.duelsWon = 0;
        this.duelsLost = 0;
        this.lastDuelTime = null;
        this.duelsInLast24Hours = 0;
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
        int oldMaxCharges = getMaxActionCharges();
        
        this.level++;
        this.xp -= this.xpToNextLevel;
        this.xpToNextLevel = calculateXpForNextLevel(this.level);
        this.stats.applyLevelUpGrowth(this.characterClass);
        
        // Check if max charges increased (Fibonacci threshold reached)
        int newMaxCharges = getMaxActionCharges();
        if (newMaxCharges > oldMaxCharges) {
            // Player gained a charge slot - give +1 charge immediately as level-up bonus
            this.actionCharges = Math.min(newMaxCharges, this.actionCharges + 1);
        }
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
     * Calculates the maximum action charges based on character level.
     * Uses Fibonacci sequence thresholds: 3, 5, 8, 13, 21, 34, 55
     * Max level is 55, so maximum charges is 10.
     *
     * @return maximum charges for current level (3-10)
     */
    public int getMaxActionCharges() {
        int[] fibonacciThresholds = {3, 5, 8, 13, 21, 34, 55};
        int baseCharges = 3;

        for (int threshold : fibonacciThresholds) {
            if (level >= threshold) {
                baseCharges++;
            } else {
                break;
            }
        }

        return baseCharges;
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
        int maxCharges = getMaxActionCharges();
        
        if (lastChargeRefreshTime == null) {
            lastChargeRefreshTime = Instant.now();
            actionCharges = maxCharges;
            return;
        }

        Instant now = Instant.now();
        long hoursSinceRefresh = (now.getEpochSecond() - lastChargeRefreshTime.getEpochSecond()) / 3600;

        if (hoursSinceRefresh >= refreshHours) {
            // Calculate how many full refresh cycles have passed
            int refreshCycles = (int) (hoursSinceRefresh / refreshHours);
            actionCharges = Math.min(maxCharges, actionCharges + refreshCycles * maxCharges);
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
        int maxCharges = getMaxActionCharges();
        if (actionCharges >= maxCharges) {
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
        int maxCharges = getMaxActionCharges();
        this.actionCharges = Math.max(0, Math.min(maxCharges, actionCharges));
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

    // Kill counter system getters

    /**
     * Gets the number of regular enemies killed.
     *
     * @return the number of enemies killed
     */
    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    /**
     * Gets the number of bosses killed.
     *
     * @return the number of bosses killed
     */
    public int getBossesKilled() {
        return bossesKilled;
    }

    /**
     * Gets the number of super bosses killed.
     *
     * @return the number of super bosses killed
     */
    public int getSuperBossesKilled() {
        return superBossesKilled;
    }

    /**
     * Increments the enemies killed counter.
     */
    public void incrementEnemiesKilled() {
        this.enemiesKilled++;
    }

    /**
     * Increments the bosses killed counter.
     */
    public void incrementBossesKilled() {
        this.bossesKilled++;
    }

    /**
     * Increments the super bosses killed counter.
     */
    public void incrementSuperBossesKilled() {
        this.superBossesKilled++;
    }

    // Inventory system getters/setters

    public RPGInventory getInventory() {
        return inventory;
    }

    public void setInventory(RPGInventory inventory) {
        this.inventory = Objects.requireNonNull(inventory);
    }

    // Dual system getters/setters

    public int getDuelsWon() {
        return duelsWon;
    }

    public int getDuelsLost() {
        return duelsLost;
    }

    public Instant getLastDuelTime() {
        return lastDuelTime;
    }

    public int getDuelsInLast24Hours() {
        refreshDuelCount();
        return duelsInLast24Hours;
    }

    /**
     * Checks if the character can perform a duel.
     *
     * @return true if can duel (rate limit not exceeded, alive, not recovering)
     */
    public boolean canDuel() {
        refreshDuelCount();
        return duelsInLast24Hours < 3 && !isDead && !isRecovering;
    }

    /**
     * Records a duel result.
     *
     * @param won whether the character won
     */
    public void recordDuel(boolean won) {
        if (won) {
            duelsWon++;
        } else {
            duelsLost++;
        }
        lastDuelTime = Instant.now();
        duelsInLast24Hours++;
    }

    /**
     * Refreshes the duel count if 24 hours have passed.
     */
    public void refreshDuelCount() {
        if (lastDuelTime == null) {
            return;
        }
        long hoursSince = java.time.temporal.ChronoUnit.HOURS.between(lastDuelTime, Instant.now());
        if (hoursSince >= 24) {
            duelsInLast24Hours = 0;
        }
    }
}
