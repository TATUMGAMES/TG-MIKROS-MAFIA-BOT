package com.tatumgames.mikros.games.rpg.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    private int eliteKills = 0; // Elite enemy kills

    // Inventory system
    private RPGInventory inventory;

    // Duel system
    private int duelsWon = 0;
    private int duelsLost = 0;
    private Instant lastDuelTime;
    private int duelsInLast24Hours = 0;

    // Heroic charge system (for boss battles)
    private int heroicCharges = 5; // Fixed at 5 charges, refreshed when new boss spawns

    // Achievement system
    private String title; // Current equipped title (nullable)
    private String legendaryAura; // Current legendary aura (nullable)
    private List<String> storyFlags; // Personal story flags (max 2)
    private int totalExplores = 0; // Total explore actions performed
    private int totalRests = 0; // Total rest actions performed
    private int totalDeaths = 0; // Total death count
    private int totalResurrections = 0; // Times resurrected (as target)
    private int totalChargesDonated = 0; // Total charges donated to others
    private int exploreStreak = 0; // Sequences of 3x explore in a row
    private int trainStreak = 0; // Sequences of 3x train in a row
    private int restStreak = 0; // Sequences of 3x rest in a row
    private int battleStreak = 0; // Sequences of 3x battle in a row
    private String lastActionType; // Last action performed (for pattern tracking)
    private int consecutiveSameAction = 0; // Current streak of same action
    private int timesRaisedFallen = 0; // Times Necromancer raised fallen (for Gravebound Presence)
    private int topDamageBossKills = 0; // Count of being top damage dealer on boss defeats
    private boolean raisedFallenThisBoss = false; // Reset when new boss spawns
    private int temporaryCharges = 0; // Donated charges (separate from regular charges)
    private Instant lastDonationReceived; // Track donation cooldown
    
    // Cursed world tracking (for failure-based titles)
    private int cursedBossFights = 0; // Participated in boss fights while curses were active
    private int cursedResurrections = 0; // Resurrections performed during cursed worlds (Priest)
    private boolean actedDuringBothCurses = false; // Acted during both Minor + Major curse simultaneously
    
    // Exploration event temporary debuffs
    private boolean hasFrostbite = false; // Frostbite reduces max HP by 5%, removed by rest
    private int darkRelicActionsRemaining = 0; // Dark Relic: +5% XP for next 3 actions, +10% damage taken
    private double darkRelicXpBonus = 0.0; // XP bonus multiplier (0.05 = +5%)
    private double darkRelicDamagePenalty = 0.0; // Damage penalty multiplier (0.10 = +10% damage taken)
    
    // Elite enemy system tracking
    private Instant temporaryCurseExpiresAt = null; // Temporary curse expiration (12h)
    private boolean loseChargeOnNextRefresh = false; // Flag for losing charge on next refresh
    
    // Oathbreaker corruption system
    private int corruption = 0; // Current corruption (0-15 base, 0-20 if Embraced, 0-10 if Purged)
    private int corruptionCap = 15; // Max corruption (15 base, 20 if Embraced, 10 if Purged)
    private String oathbreakerPath = null; // Path choice: "EMBRACE", "PURGE", or null
    private int oathFragments = 0; // Oath fragments collected from elites
    private int backlashEventsTriggered = 0; // Track for achievements
    private boolean hasRefusedDeity = false; // Track deity refusal
    
    // Lore recognition tracking
    private int timesResurrectedOthers = 0; // Times Priest resurrected others (for The Rescuer recognition)
    private java.util.Set<com.tatumgames.mikros.games.rpg.model.InfusionType> infusionsCrafted; // Set of infusion types crafted (for Master of Elements recognition)
    
    // Irrevocable World Encounters tracking
    private String deityBlessing; // Which deity blessed them (enum name or null)
    private String relicChoice; // Which relic they took (enum name or null)
    private String philosophicalPath; // "UNBOUND", "GODMARKED", or null
    private java.util.Set<String> worldFlags; // Permanent world flags (separate from story flags)
    private java.util.Map<String, Double> statModifiers; // Permanent multiplicative stat modifiers (e.g., "STR_EFFECTIVENESS" -> 1.15)
    
    // Encounter/Interaction count tracking (max 3 per type)
    private java.util.Map<String, Integer> worldEncounterCounts; // Map: encounter type name -> count (max 3)
    private java.util.Map<String, Integer> statInteractionCounts; // Map: interaction type name -> count (max 3)

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
        this.eliteKills = 0;

        // Initialize inventory
        this.inventory = new RPGInventory();

        // Initialize duel tracking
        this.duelsWon = 0;
        this.duelsLost = 0;
        this.lastDuelTime = null;
        this.duelsInLast24Hours = 0;

        // Initialize heroic charges (for boss battles)
        this.heroicCharges = 5;

        // Initialize achievement system
        this.title = null;
        this.legendaryAura = null;
        this.storyFlags = new ArrayList<>();
        this.totalExplores = 0;
        this.totalRests = 0;
        this.totalDeaths = 0;
        this.totalResurrections = 0;
        this.totalChargesDonated = 0;
        this.exploreStreak = 0;
        this.trainStreak = 0;
        this.restStreak = 0;
        this.battleStreak = 0;
        this.lastActionType = null;
        this.consecutiveSameAction = 0;
        this.timesRaisedFallen = 0;
        this.topDamageBossKills = 0;
        this.raisedFallenThisBoss = false;
        this.temporaryCharges = 0;
        this.lastDonationReceived = null;
        this.cursedBossFights = 0;
        this.cursedResurrections = 0;
        this.actedDuringBothCurses = false;
        
        // Initialize exploration event debuffs
        this.hasFrostbite = false;
        this.darkRelicActionsRemaining = 0;
        this.darkRelicXpBonus = 0.0;
        this.darkRelicDamagePenalty = 0.0;
        
        // Initialize elite enemy system tracking
        this.temporaryCurseExpiresAt = null;
        this.loseChargeOnNextRefresh = false;
        
        // Initialize Oathbreaker corruption system
        this.corruption = 0;
        this.corruptionCap = 15;
        this.oathbreakerPath = null;
        this.oathFragments = 0;
        this.backlashEventsTriggered = 0;
        this.hasRefusedDeity = false;
        
        // Initialize lore recognition tracking
        this.timesResurrectedOthers = 0;
        this.infusionsCrafted = new java.util.HashSet<>();
        
        // Initialize irrevocable world encounters tracking
        this.deityBlessing = null;
        this.relicChoice = null;
        this.philosophicalPath = null;
        this.worldFlags = new java.util.HashSet<>();
        this.statModifiers = new java.util.HashMap<>();
        this.worldEncounterCounts = new java.util.HashMap<>();
        this.statInteractionCounts = new java.util.HashMap<>();
    }

    /**
     * Adds experience points and handles leveling up.
     *
     * @param amount the XP to add
     * @return true if the character leveled up
     */
    public boolean addXp(int amount) {
        return addXp(amount, null);
    }
    
    /**
     * Adds experience points and handles leveling up.
     *
     * @param amount the XP to add
     * @param loreRecognitionService optional service for checking milestones (can be null)
     * @return true if the character leveled up
     */
    public boolean addXp(int amount, com.tatumgames.mikros.games.rpg.service.LoreRecognitionService loreRecognitionService) {
        this.xp += amount;

        boolean leveledUp = false;
        while (this.xp >= this.xpToNextLevel) {
            levelUp(loreRecognitionService);
            leveledUp = true;
        }

        return leveledUp;
    }

    /**
     * Levels up the character.
     * 
     * @param loreRecognitionService optional service for checking milestones (can be null)
     */
    private void levelUp(com.tatumgames.mikros.games.rpg.service.LoreRecognitionService loreRecognitionService) {
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
        
        // Check for lore recognition milestones (level-based)
        if (loreRecognitionService != null) {
            loreRecognitionService.checkMilestones(this);
        }
    }
    
    /**
     * Levels up the character (overload without service for backward compatibility).
     */
    private void levelUp() {
        levelUp(null);
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
     * @param activeCurses list of active world curses (for Frozen Time curse)
     */
    public void refreshCharges(int refreshHours, java.util.List<com.tatumgames.mikros.games.rpg.curse.WorldCurse> activeCurses) {
        // Apply Frozen Time curse (+2 hours to refresh timer)
        int effectiveRefreshHours = refreshHours;
        if (activeCurses != null && activeCurses.contains(com.tatumgames.mikros.games.rpg.curse.WorldCurse.MAJOR_FROZEN_TIME)) {
            effectiveRefreshHours = refreshHours + 2;
        }
        
        int maxCharges = getMaxActionCharges();
        
        if (lastChargeRefreshTime == null) {
            lastChargeRefreshTime = Instant.now();
            actionCharges = maxCharges;
            return;
        }

        Instant now = Instant.now();
        long hoursSinceRefresh = (now.getEpochSecond() - lastChargeRefreshTime.getEpochSecond()) / 3600;

        if (hoursSinceRefresh >= effectiveRefreshHours) {
            // Calculate how many full refresh cycles have passed
            int refreshCycles = (int) (hoursSinceRefresh / effectiveRefreshHours);
            actionCharges = Math.min(maxCharges, actionCharges + refreshCycles * maxCharges);
            
            // Apply elite defeat penalty (lose one charge on refresh)
            if (loseChargeOnNextRefresh && refreshCycles > 0) {
                actionCharges = Math.max(0, actionCharges - 1);
                loseChargeOnNextRefresh = false; // Clear flag after applying
            }
            
            lastChargeRefreshTime = now;
        }
    }

    /**
     * Refreshes action charges if the refresh period has passed (backward compatibility).
     *
     * @param refreshHours the charge refresh period in hours (default: 12)
     */
    public void refreshCharges(int refreshHours) {
        refreshCharges(refreshHours, null);
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

    /**
     * Gets the number of elite enemies killed.
     *
     * @return the number of elite enemies killed
     */
    public int getEliteKills() {
        return eliteKills;
    }

    /**
     * Increments the elite enemies killed counter.
     */
    public void incrementEliteKills() {
        this.eliteKills++;
    }

    // Inventory system getters/setters

    public RPGInventory getInventory() {
        return inventory;
    }

    public void setInventory(RPGInventory inventory) {
        this.inventory = Objects.requireNonNull(inventory);
    }

    // Duel system getters/setters

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

    // Heroic charge system getters/setters

    /**
     * Gets the current number of heroic charges.
     *
     * @return current heroic charges (0-3)
     */
    public int getHeroicCharges() {
        return heroicCharges;
    }

    /**
     * Gets the maximum number of heroic charges.
     *
     * @return maximum heroic charges (always 5)
     */
    public int getMaxHeroicCharges() {
        return 5;
    }

    /**
     * Checks if the character can perform a heroic action (boss battle).
     *
     * @return true if has heroic charges and is not dead/recovering
     */
    public boolean canPerformHeroicAction() {
        if (isDead || isRecovering) {
            return false;
        }
        return heroicCharges > 0;
    }

    /**
     * Uses a heroic charge for a boss battle.
     *
     * @return true if charge was used successfully
     */
    public boolean useHeroicCharge() {
        if (heroicCharges > 0) {
            heroicCharges--;
            return true;
        }
        return false;
    }

    /**
     * Refreshes heroic charges to maximum (5).
     * Called when a new boss spawns.
     */
    public void refreshHeroicCharges() {
        this.heroicCharges = getMaxHeroicCharges();
        // Reset Raise Fallen tracking when new boss spawns
        this.raisedFallenThisBoss = false;
        this.timesRaisedFallen = 0;
    }

    // Achievement system getters/setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLegendaryAura() {
        return legendaryAura;
    }

    public void setLegendaryAura(String legendaryAura) {
        this.legendaryAura = legendaryAura;
    }

    public List<String> getStoryFlags() {
        return storyFlags;
    }

    public void setStoryFlags(List<String> storyFlags) {
        this.storyFlags = storyFlags != null ? new ArrayList<>(storyFlags) : new ArrayList<>();
    }

    /**
     * Adds a story flag. If already at max (2), removes the oldest flag.
     *
     * @param flag the story flag to add
     */
    public void addStoryFlag(String flag) {
        if (storyFlags.size() >= 2) {
            storyFlags.remove(0); // Remove oldest
        }
        storyFlags.add(flag);
    }

    public int getTotalExplores() {
        return totalExplores;
    }

    public void incrementExploreCount() {
        this.totalExplores++;
    }

    public int getTotalRests() {
        return totalRests;
    }

    public void incrementRestCount() {
        this.totalRests++;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void incrementDeathCount() {
        this.totalDeaths++;
    }

    public int getTotalResurrections() {
        return totalResurrections;
    }

    public void incrementResurrectionCount() {
        this.totalResurrections++;
    }

    public int getTotalChargesDonated() {
        return totalChargesDonated;
    }

    public void incrementChargesDonated() {
        this.totalChargesDonated++;
    }

    public int getExploreStreak() {
        return exploreStreak;
    }

    public void incrementExploreStreak() {
        this.exploreStreak++;
    }

    public int getTrainStreak() {
        return trainStreak;
    }

    public void incrementTrainStreak() {
        this.trainStreak++;
    }

    public int getRestStreak() {
        return restStreak;
    }

    public void incrementRestStreak() {
        this.restStreak++;
    }

    public int getBattleStreak() {
        return battleStreak;
    }

    public void incrementBattleStreak() {
        this.battleStreak++;
    }

    public String getLastActionType() {
        return lastActionType;
    }

    public int getConsecutiveSameAction() {
        return consecutiveSameAction;
    }

    /**
     * Records an action type for pattern tracking.
     * Tracks consecutive same actions and increments streak counters when pattern detected.
     *
     * @param actionType the action type (explore, train, rest, battle)
     */
    public void recordActionType(String actionType) {
        if (actionType == null) {
            return;
        }

        if (actionType.equals(lastActionType)) {
            consecutiveSameAction++;
        } else {
            consecutiveSameAction = 1;
            lastActionType = actionType;
        }

        // When we reach 3 consecutive same actions, increment streak counter
        if (consecutiveSameAction >= 3) {
            switch (actionType.toLowerCase()) {
                case "explore":
                    incrementExploreStreak();
                    break;
                case "train":
                    incrementTrainStreak();
                    break;
                case "rest":
                    incrementRestStreak();
                    break;
                case "battle":
                    incrementBattleStreak();
                    break;
            }
            consecutiveSameAction = 0; // Reset after pattern detected
        }
    }

    public int getTimesRaisedFallen() {
        return timesRaisedFallen;
    }

    public void incrementTimesRaisedFallen() {
        this.timesRaisedFallen++;
    }

    public int getTopDamageBossKills() {
        return topDamageBossKills;
    }

    public void incrementTopDamageBossKills() {
        this.topDamageBossKills++;
    }

    public boolean isRaisedFallenThisBoss() {
        return raisedFallenThisBoss;
    }

    public void setRaisedFallenThisBoss(boolean raisedFallenThisBoss) {
        this.raisedFallenThisBoss = raisedFallenThisBoss;
    }

    public int getTemporaryCharges() {
        return temporaryCharges;
    }

    public void setTemporaryCharges(int temporaryCharges) {
        this.temporaryCharges = Math.max(0, temporaryCharges);
    }

    public void addTemporaryCharge() {
        this.temporaryCharges++;
    }

    public boolean useTemporaryCharge() {
        if (temporaryCharges > 0) {
            temporaryCharges--;
            return true;
        }
        return false;
    }

    public Instant getLastDonationReceived() {
        return lastDonationReceived;
    }

    public void setLastDonationReceived(Instant lastDonationReceived) {
        this.lastDonationReceived = lastDonationReceived;
    }

    // Cursed world tracking getters/setters

    public int getCursedBossFights() {
        return cursedBossFights;
    }

    public void incrementCursedBossFights() {
        this.cursedBossFights++;
    }

    public int getCursedResurrections() {
        return cursedResurrections;
    }

    public void incrementCursedResurrections() {
        this.cursedResurrections++;
    }

    public boolean hasActedDuringBothCurses() {
        return actedDuringBothCurses;
    }

    public void setActedDuringBothCurses(boolean actedDuringBothCurses) {
        this.actedDuringBothCurses = actedDuringBothCurses;
    }

    /**
     * Gets the total number of bosses killed (normal + super).
     *
     * @return total boss kills
     */
    public int getTotalBossKills() {
        return bossesKilled + superBossesKilled;
    }

    // Exploration event debuff getters/setters

    public boolean hasFrostbite() {
        return hasFrostbite;
    }

    public void setHasFrostbite(boolean hasFrostbite) {
        this.hasFrostbite = hasFrostbite;
    }

    public int getDarkRelicActionsRemaining() {
        return darkRelicActionsRemaining;
    }

    public void setDarkRelicActionsRemaining(int darkRelicActionsRemaining) {
        this.darkRelicActionsRemaining = Math.max(0, darkRelicActionsRemaining);
    }

    public void decrementDarkRelicActions() {
        if (darkRelicActionsRemaining > 0) {
            darkRelicActionsRemaining--;
            if (darkRelicActionsRemaining == 0) {
                // Clear debuff when actions expire
                darkRelicXpBonus = 0.0;
                darkRelicDamagePenalty = 0.0;
            }
        }
    }

    public double getDarkRelicXpBonus() {
        return darkRelicXpBonus;
    }

    public void setDarkRelicXpBonus(double darkRelicXpBonus) {
        this.darkRelicXpBonus = darkRelicXpBonus;
    }

    public double getDarkRelicDamagePenalty() {
        return darkRelicDamagePenalty;
    }

    public void setDarkRelicDamagePenalty(double darkRelicDamagePenalty) {
        this.darkRelicDamagePenalty = darkRelicDamagePenalty;
    }

    // Elite enemy system getters/setters

    public Instant getTemporaryCurseExpiresAt() {
        return temporaryCurseExpiresAt;
    }

    public void setTemporaryCurseExpiresAt(Instant temporaryCurseExpiresAt) {
        this.temporaryCurseExpiresAt = temporaryCurseExpiresAt;
    }

    public boolean hasTemporaryCurse() {
        if (temporaryCurseExpiresAt == null) {
            return false;
        }
        if (Instant.now().isAfter(temporaryCurseExpiresAt)) {
            // Curse expired, clear it
            temporaryCurseExpiresAt = null;
            return false;
        }
        return true;
    }

    public boolean isLoseChargeOnNextRefresh() {
        return loseChargeOnNextRefresh;
    }

    public void setLoseChargeOnNextRefresh(boolean loseChargeOnNextRefresh) {
        this.loseChargeOnNextRefresh = loseChargeOnNextRefresh;
    }

    // Oathbreaker corruption system getters/setters

    public int getCorruption() {
        return corruption;
    }

    public void addCorruption(int amount) {
        this.corruption = Math.min(corruptionCap, Math.max(0, corruption + amount));
    }

    public void removeCorruption(int amount) {
        this.corruption = Math.max(0, corruption - amount);
    }

    public int getCorruptionCap() {
        return corruptionCap;
    }

    public void setCorruptionCap(int corruptionCap) {
        this.corruptionCap = corruptionCap;
        // Clamp current corruption to new cap
        if (corruption > corruptionCap) {
            corruption = corruptionCap;
        }
    }

    public String getOathbreakerPath() {
        return oathbreakerPath;
    }

    public void setOathbreakerPath(String path) {
        if (path != null && !path.equals("EMBRACE") && !path.equals("PURGE")) {
            throw new IllegalArgumentException("Oathbreaker path must be EMBRACE, PURGE, or null");
        }
        this.oathbreakerPath = path;
        // Update corruption cap based on path
        if ("EMBRACE".equals(path)) {
            setCorruptionCap(20);
        } else if ("PURGE".equals(path)) {
            setCorruptionCap(10);
        } else {
            setCorruptionCap(15); // Default
        }
    }

    public int getOathFragments() {
        return oathFragments;
    }

    public void incrementOathFragments() {
        this.oathFragments++;
    }

    public int getBacklashEventsTriggered() {
        return backlashEventsTriggered;
    }

    public void incrementBacklashEvents() {
        this.backlashEventsTriggered++;
    }

    public boolean hasRefusedDeity() {
        return hasRefusedDeity;
    }

    public void setHasRefusedDeity(boolean hasRefusedDeity) {
        this.hasRefusedDeity = hasRefusedDeity;
    }

    // Lore recognition tracking getters/setters

    public int getTimesResurrectedOthers() {
        return timesResurrectedOthers;
    }

    public void incrementTimesResurrectedOthers() {
        this.timesResurrectedOthers++;
    }

    public java.util.Set<com.tatumgames.mikros.games.rpg.model.InfusionType> getInfusionsCrafted() {
        return new java.util.HashSet<>(infusionsCrafted);
    }

    public void addInfusionCrafted(com.tatumgames.mikros.games.rpg.model.InfusionType infusionType) {
        this.infusionsCrafted.add(infusionType);
    }

    // Irrevocable World Encounters getters/setters

    public String getDeityBlessing() {
        return deityBlessing;
    }

    public void setDeityBlessing(String deityBlessing) {
        this.deityBlessing = deityBlessing;
    }

    public String getRelicChoice() {
        return relicChoice;
    }

    public void setRelicChoice(String relicChoice) {
        this.relicChoice = relicChoice;
    }

    public String getPhilosophicalPath() {
        return philosophicalPath;
    }

    public void setPhilosophicalPath(String philosophicalPath) {
        this.philosophicalPath = philosophicalPath;
    }

    public java.util.Set<String> getWorldFlags() {
        return new java.util.HashSet<>(worldFlags);
    }

    public void setWorldFlags(java.util.Set<String> worldFlags) {
        this.worldFlags = worldFlags != null ? new java.util.HashSet<>(worldFlags) : new java.util.HashSet<>();
    }

    public void addWorldFlag(String flag) {
        this.worldFlags.add(flag);
    }

    public boolean hasWorldFlag(String flag) {
        return this.worldFlags.contains(flag);
    }

    public java.util.Map<String, Double> getStatModifiers() {
        return new java.util.HashMap<>(statModifiers);
    }

    public void setStatModifiers(java.util.Map<String, Double> statModifiers) {
        this.statModifiers = statModifiers != null ? new java.util.HashMap<>(statModifiers) : new java.util.HashMap<>();
    }

    public void addStatModifier(String key, Double value) {
        this.statModifiers.put(key, value);
    }

    public Double getStatModifier(String key) {
        return this.statModifiers.get(key);
    }

    // Encounter/Interaction count tracking getters/setters

    public java.util.Map<String, Integer> getWorldEncounterCounts() {
        return new java.util.HashMap<>(worldEncounterCounts);
    }

    public void setWorldEncounterCounts(java.util.Map<String, Integer> counts) {
        this.worldEncounterCounts = counts != null ? new java.util.HashMap<>(counts) : new java.util.HashMap<>();
    }

    public int getWorldEncounterCount(String encounterType) {
        return worldEncounterCounts.getOrDefault(encounterType, 0);
    }

    public void incrementWorldEncounterCount(String encounterType) {
        int current = worldEncounterCounts.getOrDefault(encounterType, 0);
        worldEncounterCounts.put(encounterType, current + 1);
    }

    public boolean canTriggerWorldEncounter(String encounterType) {
        return getWorldEncounterCount(encounterType) < 3;
    }

    public java.util.Map<String, Integer> getStatInteractionCounts() {
        return new java.util.HashMap<>(statInteractionCounts);
    }

    public void setStatInteractionCounts(java.util.Map<String, Integer> counts) {
        this.statInteractionCounts = counts != null ? new java.util.HashMap<>(counts) : new java.util.HashMap<>();
    }

    public int getStatInteractionCount(String interactionType) {
        return statInteractionCounts.getOrDefault(interactionType, 0);
    }

    public void incrementStatInteractionCount(String interactionType) {
        int current = statInteractionCounts.getOrDefault(interactionType, 0);
        statInteractionCounts.put(interactionType, current + 1);
    }

    public boolean canTriggerStatInteraction(String interactionType) {
        return getStatInteractionCount(interactionType) < 3;
    }
}
