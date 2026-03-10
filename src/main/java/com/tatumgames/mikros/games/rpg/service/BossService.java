package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.blessing.Blessing;
import com.tatumgames.mikros.games.rpg.boss.BossCatalog;
import com.tatumgames.mikros.games.rpg.events.NilfheimEventType;
import com.tatumgames.mikros.games.rpg.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service for managing boss battles per server. Handles boss spawning, progression, and damage
 * tracking.
 */
public class BossService {
    private static final Logger logger = LoggerFactory.getLogger(BossService.class);
    private static final Random random = new Random();
    // Boss cycle: 24h livable + 24h cooldown = 48h between spawns
    private static final long LIVABLE_INTERVAL_HOURS = 24;
    private static final long COOLDOWN_AFTER_LIVABLE_HOURS = 24;
    private static final long MIN_SPAWN_INTERVAL_HOURS =
            LIVABLE_INTERVAL_HOURS + COOLDOWN_AFTER_LIVABLE_HOURS; // 48
    /** How often the scheduler runs spawn checks (hours). */
    private static final long SPAWN_CHECK_INTERVAL_HOURS = 24;
    // Per-server boss state: guildId -> ServerBossState
    private final Map<String, ServerBossState> serverStates;
    // Per-guild locks to prevent concurrent boss spawning
    private final Map<String, ReentrantLock> guildLocks = new ConcurrentHashMap<>();
    // Damage tracking: guildId -> Map<userId, totalDamage>
    private final Map<String, Map<String, Integer>> damageTracking;
    // Class participation tracking: guildId -> Map<CharacterClass, count>
    private final Map<String, Map<CharacterClass, Integer>> classParticipation;
    // Recent defeats for announcement: guildId -> DefeatInfo
    private final Map<String, DefeatInfo> recentDefeats;
    private final CharacterService characterService;
    private final AuraService auraService;
    private final WorldCurseService worldCurseService;
    private final NilfheimEventService nilfheimEventService;
    private final LoreRecognitionService loreRecognitionService;
    private final BlessingService blessingService;

    /**
     * Creates a new BossService.
     *
     * @param characterService       the character service for tracking kills
     * @param auraService            the aura service for applying legendary aura effects
     * @param worldCurseService      the world curse service for clearing curses on defeat
     * @param nilfheimEventService   the Nilfheim event service for server-wide events
     * @param loreRecognitionService the lore recognition service for milestone checks
     * @param blessingService        the blessing service for applying blessing stat boosts
     */
    public BossService(
            CharacterService characterService,
            AuraService auraService,
            WorldCurseService worldCurseService,
            NilfheimEventService nilfheimEventService,
            LoreRecognitionService loreRecognitionService,
            BlessingService blessingService) {
        this.serverStates = new ConcurrentHashMap<>();
        this.damageTracking = new ConcurrentHashMap<>();
        this.classParticipation = new ConcurrentHashMap<>();
        this.recentDefeats = new ConcurrentHashMap<>();
        this.characterService = characterService;
        this.auraService = auraService;
        this.worldCurseService = worldCurseService;
        this.nilfheimEventService = nilfheimEventService;
        this.loreRecognitionService = loreRecognitionService;
        this.blessingService = blessingService;
        logger.info("BossService initialized");
    }

    /**
     * Returns the livable interval in hours (time window to defeat a boss).
     *
     * @return livable interval in hours
     */
    public long getLivableIntervalHours() {
        return LIVABLE_INTERVAL_HOURS;
    }

    /**
     * Returns the livable interval in seconds (for Boss/SuperBoss expiry).
     *
     * @return livable interval in seconds
     */
    public long getLivableIntervalSeconds() {
        return LIVABLE_INTERVAL_HOURS * 3600;
    }

    /**
     * Returns the minimum spawn interval in hours (livable + cooldown).
     *
     * @return min spawn interval in hours
     */
    public long getMinSpawnIntervalHours() {
        return MIN_SPAWN_INTERVAL_HOURS;
    }

    /**
     * Returns the spawn check interval in hours (how often the scheduler checks for spawns).
     *
     * @return spawn check interval in hours
     */
    public long getSpawnCheckIntervalHours() {
        return SPAWN_CHECK_INTERVAL_HOURS;
    }

    /**
     * Gets or creates boss state for a server.
     *
     * @param guildId the guild ID
     * @return the server boss state
     */
    public ServerBossState getOrCreateState(String guildId) {
        return serverStates.computeIfAbsent(guildId, k -> new ServerBossState());
    }

    /**
     * Gets boss state for a server.
     *
     * @param guildId the guild ID
     * @return the server boss state, or null if not initialized
     */
    public ServerBossState getState(String guildId) {
        return serverStates.get(guildId);
    }

    /**
     * Gets or creates a lock for a specific guild. Ensures thread-safe boss spawning per guild.
     *
     * @param guildId the guild ID
     * @return the lock for this guild
     */
    private ReentrantLock getGuildLock(String guildId) {
        return guildLocks.computeIfAbsent(guildId, k -> new ReentrantLock());
    }

    /**
     * Spawns the appropriate boss type for a guild. Thread-safe: handles super boss vs normal boss
     * decision inside lock. Applies spawn cooldown to prevent rapid spawning.
     *
     * @param guildId the guild ID
     * @return the spawned boss (Boss or SuperBoss), or null if spawn failed
     */
    public Object spawnAppropriateBoss(String guildId) {
        ReentrantLock lock = getGuildLock(guildId);
        lock.lock();
        try {
            // Cleanup first
            validateAndCleanupBossState(guildId);

            ServerBossState state = getOrCreateState(guildId);

            // Check cooldown
            Instant lastSpawn = state.getLastBossSpawnTime();
            if (lastSpawn != null) {
                long hoursSinceLastSpawn = Duration.between(lastSpawn, Instant.now()).toHours();
                if (hoursSinceLastSpawn < MIN_SPAWN_INTERVAL_HOURS) {
                    // Return existing boss if any
                    if (state.getCurrentBoss() != null) {
                        return state.getCurrentBoss();
                    }
                    if (state.getCurrentSuperBoss() != null) {
                        return state.getCurrentSuperBoss();
                    }
                    logger.warn(
                            "Spawn rejected - 48h cooldown not elapsed for guild {} ({} hours since last spawn)",
                            guildId,
                            hoursSinceLastSpawn);
                    return null;
                }
            }

            // Check if already has active boss
            if (state.getCurrentBoss() != null
                    && !state.getCurrentBoss().isExpired()
                    && !state.getCurrentBoss().isDefeated()) {
                return state.getCurrentBoss();
            }
            if (state.getCurrentSuperBoss() != null
                    && !state.getCurrentSuperBoss().isExpired()
                    && !state.getCurrentSuperBoss().isDefeated()) {
                return state.getCurrentSuperBoss();
            }

            // Decide which type to spawn (decision made inside lock!)
            if (state.getNormalBossesSinceSuper() >= 3) {
                return spawnSuperBossInternal(guildId, state);
            } else {
                return spawnNormalBossInternal(guildId, state);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Spawns a new normal boss for a server. Thread-safe: Uses per-guild locks to prevent concurrent
     * spawning. Applies spawn cooldown. Returns null if super boss should spawn instead.
     *
     * @param guildId the guild ID
     * @return the spawned boss, or null if spawn failed / super boss needed
     */
    public Boss spawnNormalBoss(String guildId) {
        ReentrantLock lock = getGuildLock(guildId);
        lock.lock();
        try {
            validateAndCleanupBossState(guildId);
            ServerBossState state = getOrCreateState(guildId);

            Boss currentBoss = state.getCurrentBoss();
            SuperBoss currentSuperBoss = state.getCurrentSuperBoss();

            if (currentBoss != null && !currentBoss.isDefeated() && !currentBoss.isExpired()) {
                logger.warn(
                        "Attempted to spawn normal boss when one already active for guild {} (existing bossId: {}, name: {})",
                        guildId,
                        currentBoss.getBossId(),
                        currentBoss.getName());
                return currentBoss;
            }
            if (currentSuperBoss != null
                    && !currentSuperBoss.isDefeated()
                    && !currentSuperBoss.isExpired()) {
                logger.warn(
                        "Attempted to spawn normal boss when super boss already active for guild {} (existing bossId: {}, name: {})",
                        guildId,
                        currentSuperBoss.getBossId(),
                        currentSuperBoss.getName());
                return null;
            }

            // Spawn cooldown check
            Instant lastSpawn = state.getLastBossSpawnTime();
            if (lastSpawn != null) {
                long hoursSinceLastSpawn = Duration.between(lastSpawn, Instant.now()).toHours();
                if (hoursSinceLastSpawn < MIN_SPAWN_INTERVAL_HOURS) {
                    logger.warn(
                            "Spawn attempt rejected - 48h cooldown not elapsed for guild {} ({} hours since last spawn)",
                            guildId,
                            hoursSinceLastSpawn);
                    return currentBoss;
                }
            }

            if (state.getNormalBossesSinceSuper() >= 3) {
                return null; // Signal to spawn super boss instead
            }

            return spawnNormalBossInternal(guildId, state);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Spawns a new super boss for a server. Thread-safe: Uses per-guild locks to prevent concurrent
     * spawning. Applies spawn cooldown.
     *
     * @param guildId the guild ID
     * @return the spawned super boss, or null if spawn failed
     */
    public SuperBoss spawnSuperBoss(String guildId) {
        ReentrantLock lock = getGuildLock(guildId);
        lock.lock();
        try {
            validateAndCleanupBossState(guildId);
            ServerBossState state = getOrCreateState(guildId);

            Boss currentBoss = state.getCurrentBoss();
            SuperBoss currentSuperBoss = state.getCurrentSuperBoss();

            if (currentBoss != null && !currentBoss.isDefeated() && !currentBoss.isExpired()) {
                logger.warn(
                        "Attempted to spawn super boss when normal boss already active for guild {} (existing bossId: {}, name: {})",
                        guildId,
                        currentBoss.getBossId(),
                        currentBoss.getName());
                return null;
            }
            if (currentSuperBoss != null
                    && !currentSuperBoss.isDefeated()
                    && !currentSuperBoss.isExpired()) {
                logger.warn(
                        "Attempted to spawn super boss when one already active for guild {} (existing bossId: {}, name: {})",
                        guildId,
                        currentSuperBoss.getBossId(),
                        currentSuperBoss.getName());
                return currentSuperBoss;
            }

            // Spawn cooldown check
            Instant lastSpawn = state.getLastBossSpawnTime();
            if (lastSpawn != null) {
                long hoursSinceLastSpawn = Duration.between(lastSpawn, Instant.now()).toHours();
                if (hoursSinceLastSpawn < MIN_SPAWN_INTERVAL_HOURS) {
                    logger.warn(
                            "Spawn attempt rejected - 48h cooldown not elapsed for guild {} ({} hours since last spawn)",
                            guildId,
                            hoursSinceLastSpawn);
                    return currentSuperBoss;
                }
            }

            return spawnSuperBossInternal(guildId, state);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Internal: spawns a normal boss. Caller must hold guild lock. Assumes no active boss exists.
     */
    private Boss spawnNormalBossInternal(String guildId, ServerBossState state) {
        // Clear old boss state atomically before spawning new one
        state.setCurrentBoss(null);
        state.setCurrentSuperBoss(null);

        int level = state.getBossLevel();
        BossCatalog.BossDefinition definition;

        if (state.getNormalBossesDefeated() > 0 && state.getNormalBossesDefeated() % 10 == 9) {
            definition = BossCatalog.getUnityDevourer(level);
            logger.info(
                    "Unity Devourer spawn triggered ({} normal bosses defeated, spawning 10th boss) for guild {}",
                    state.getNormalBossesDefeated(),
                    guildId);
        } else {
            definition = BossCatalog.getRandomNormalBoss(level);
        }

        Boss boss = BossCatalog.createBoss(definition, level, getLivableIntervalSeconds());

        int consecutiveFailures = state.getConsecutiveFailures();
        int empowermentLevel = 0;
        if (consecutiveFailures >= 5) {
            empowermentLevel = 2;
        } else if (consecutiveFailures >= 3) {
            empowermentLevel = 1;
        }
        boss.setEmpowermentLevel(empowermentLevel);

        if (empowermentLevel > 0) {
            applyEmpowerment(boss, empowermentLevel);
        }

        state.setCurrentBoss(boss);
        state.setLastBossSpawnTime(Instant.now());
        state.setLastSpawnedBossId(boss.getBossId());
        damageTracking.put(guildId, new ConcurrentHashMap<>());
        classParticipation.put(guildId, new ConcurrentHashMap<>());

        refreshHeroicChargesForAllCharacters();

        logger.info(
                "Spawned normal boss {} (Level {}, bossId: {}) with empowerment level {} for guild {}",
                boss.getName(),
                level,
                boss.getBossId(),
                empowermentLevel,
                guildId);
        return boss;
    }

    /**
     * Internal: spawns a super boss. Caller must hold guild lock. Assumes no active boss exists.
     */
    private SuperBoss spawnSuperBossInternal(String guildId, ServerBossState state) {
        state.setCurrentBoss(null);
        state.setCurrentSuperBoss(null);

        int level = state.getSuperBossLevel();
        BossCatalog.SuperBossDefinition definition = BossCatalog.getSuperBoss(level);
        SuperBoss superBoss = BossCatalog.createSuperBoss(definition, level, getLivableIntervalSeconds());

        int consecutiveFailures = state.getConsecutiveFailures();
        int empowermentLevel = 0;
        if (consecutiveFailures >= 5) {
            empowermentLevel = 2;
        } else if (consecutiveFailures >= 3) {
            empowermentLevel = 1;
        }
        superBoss.setEmpowermentLevel(empowermentLevel);

        if (empowermentLevel > 0) {
            applyEmpowerment(superBoss, empowermentLevel);
        }

        state.setCurrentSuperBoss(superBoss);
        state.setNormalBossesSinceSuper(0);
        state.setLastBossSpawnTime(Instant.now());
        state.setLastSpawnedBossId(superBoss.getBossId());
        damageTracking.put(guildId, new ConcurrentHashMap<>());
        classParticipation.put(guildId, new ConcurrentHashMap<>());

        refreshHeroicChargesForAllCharacters();

        logger.info(
                "Spawned super boss {} (Level {}, bossId: {}) with empowerment level {} for guild {}",
                superBoss.getName(),
                level,
                superBoss.getBossId(),
                empowermentLevel,
                guildId);
        return superBoss;
    }

    /**
     * Spawns a secret boss for a specific player.
     *
     * @param guildId    the guild ID
     * @param userId     the user ID
     * @param definition the boss definition
     * @param level      the boss level
     * @return the spawned secret boss
     */
    public Boss spawnSecretBoss(
            String guildId, String userId, BossCatalog.BossDefinition definition, int level) {
        ServerBossState state = getOrCreateState(guildId);

        // Check if player already has an active secret boss
        Boss existingBoss = state.getSecretBoss(userId);
        if (existingBoss != null && !existingBoss.isDefeated() && !existingBoss.isExpired()) {
            logger.warn(
                    "Attempted to spawn secret boss when one already active for user {} in guild {}",
                    userId,
                    guildId);
            return existingBoss; // Return existing boss
        }

        Boss boss = BossCatalog.createBoss(definition, level, getLivableIntervalSeconds());
        state.setSecretBoss(userId, boss);

        logger.info(
                "Spawned secret boss {} (Level {}) for user {} in guild {}",
                boss.getName(),
                level,
                userId,
                guildId);
        return boss;
    }

    /**
     * Checks if a character qualifies for a secret boss spawn based on a milestone, and spawns one if
     * conditions are met. Grants event charges when spawning.
     *
     * @param guildId      the guild ID
     * @param userId       the user ID
     * @param milestoneKey the milestone key (e.g., "level_10", "boss_kills_10")
     * @param bossLevel    the level for the secret boss
     * @return the spawned secret boss, or null if not spawned
     */
    public Boss checkAndSpawnSecretBoss(
            String guildId, String userId, String milestoneKey, int bossLevel) {
        RPGCharacter character = characterService.getCharacter(userId);
        if (character == null) {
            return null;
        }

        // Check if this milestone has already triggered a secret boss
        if (character.getSecretBossMilestones().contains(milestoneKey)) {
            return null; // Already triggered
        }

        // Check if player already has an active secret boss
        ServerBossState state = getState(guildId);
        if (state != null) {
            Boss existingBoss = state.getSecretBoss(userId);
            if (existingBoss != null && !existingBoss.isDefeated() && !existingBoss.isExpired()) {
                return null; // Already has active secret boss
            }
        }

        // Spawn secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(bossLevel);
        Boss boss = spawnSecretBoss(guildId, userId, definition, bossLevel);

        if (boss != null) {
            // Mark milestone as triggered
            character.addSecretBossMilestone(milestoneKey);

            // Grant event charges (fixed 10, capped at 10 total)
            // If character already has charges, we grant up to 10 total (not add 10 to existing)
            int eventChargesToGrant = 10; // Fixed 10 charges
            int currentCharges = character.getEventCharges();
            int newCharges = Math.min(10, currentCharges + eventChargesToGrant);
            character.setEventCharges(newCharges);

            logger.info(
                    "Granted {} event charges to {} for secret boss milestone {} (total: {})",
                    eventChargesToGrant,
                    character.getName(),
                    milestoneKey,
                    newCharges);
        }

        return boss;
    }

    /**
     * Attacks a secret boss with a character.
     *
     * @param guildId the guild ID
     * @param userId the user ID
     * @param character the attacking character
     * @return damage dealt
     */
    public int attackSecretBoss(String guildId, String userId, RPGCharacter character) {
        ServerBossState state = getState(guildId);
        if (state == null) {
            return 0;
        }

        Boss boss = state.getSecretBoss(userId);
        if (boss == null) {
            return 0; // No secret boss
        }

        if (boss.isDefeated() || boss.isExpired()) {
            // Clean up expired/defeated secret boss
            state.removeSecretBoss(userId);
            return 0;
        }

        // Calculate damage (similar to normal boss but without class harmony or aura effects)
        int baseDamage = calculateDamage(character, boss.getType(), null);
        double multiplier = getClassBonus(character.getCharacterClass(), boss.getType());
        int damage = (int) (baseDamage * multiplier);

        // Apply damage
        boolean defeated = boss.takeDamage(damage);

        if (defeated) {
            logger.info(
                    "Secret boss {} defeated by {} in guild {}",
                    boss.getName(),
                    character.getName(),
                    guildId);
            // Grant special rewards for secret boss
            distributeSecretBossRewards(character, boss.getLevel());
            character.incrementSecretBossesKilled();

            // Check for lore recognition milestones
            if (loreRecognitionService != null) {
                loreRecognitionService.checkMilestones(character, guildId);
            }

            // Remove defeated boss
            state.removeSecretBoss(userId);
        }

        return damage;
    }

    /**
     * Applies empowerment stat boosts to a boss. Empowerment level 1 (3 failures): +15% HP, +10%
     * Attack Empowerment level 2 (5 failures): +30% HP, +20% Attack
     *
     * @param boss             the boss to empower
     * @param empowermentLevel the empowerment level (1 or 2)
     */
    private void applyEmpowerment(Boss boss, int empowermentLevel) {
        int originalMaxHp = boss.getMaxHp();
        int originalCurrentHp = boss.getCurrentHp();
        double hpRatio = originalMaxHp > 0 ? (double) originalCurrentHp / originalMaxHp : 1.0;

        if (empowermentLevel == 1) {
            // Level 1: +15% HP, +10% Attack
            int newMaxHp = (int) (originalMaxHp * 1.15);
            int newAttack = (int) (boss.getAttack() * 1.10);
            int newCurrentHp = (int) (newMaxHp * hpRatio);
            boss.setMaxHp(newMaxHp);
            boss.setAttack(newAttack);
            // Set current HP to maintain the same percentage
            boss.takeDamage(originalCurrentHp - newCurrentHp);
        } else if (empowermentLevel == 2) {
            // Level 2: +30% HP, +20% Attack
            int newMaxHp = (int) (originalMaxHp * 1.30);
            int newAttack = (int) (boss.getAttack() * 1.20);
            int newCurrentHp = (int) (newMaxHp * hpRatio);
            boss.setMaxHp(newMaxHp);
            boss.setAttack(newAttack);
            // Set current HP to maintain the same percentage
            boss.takeDamage(originalCurrentHp - newCurrentHp);
        }
    }

    /**
     * Applies empowerment stat boosts to a super boss. Empowerment level 1 (3 failures): +15% HP,
     * +10% Attack Empowerment level 2 (5 failures): +30% HP, +20% Attack
     *
     * @param superBoss        the super boss to empower
     * @param empowermentLevel the empowerment level (1 or 2)
     */
    private void applyEmpowerment(SuperBoss superBoss, int empowermentLevel) {
        int originalMaxHp = superBoss.getMaxHp();
        int originalCurrentHp = superBoss.getCurrentHp();
        double hpRatio = originalMaxHp > 0 ? (double) originalCurrentHp / originalMaxHp : 1.0;

        if (empowermentLevel == 1) {
            // Level 1: +15% HP, +10% Attack
            int newMaxHp = (int) (originalMaxHp * 1.15);
            int newAttack = (int) (superBoss.getAttack() * 1.10);
            int newCurrentHp = (int) (newMaxHp * hpRatio);
            superBoss.setMaxHp(newMaxHp);
            superBoss.setAttack(newAttack);
            // Set current HP to maintain the same percentage
            superBoss.takeDamage(originalCurrentHp - newCurrentHp);
        } else if (empowermentLevel == 2) {
            // Level 2: +30% HP, +20% Attack
            int newMaxHp = (int) (originalMaxHp * 1.30);
            int newAttack = (int) (superBoss.getAttack() * 1.20);
            int newCurrentHp = (int) (newMaxHp * hpRatio);
            superBoss.setMaxHp(newMaxHp);
            superBoss.setAttack(newAttack);
            // Set current HP to maintain the same percentage
            superBoss.takeDamage(originalCurrentHp - newCurrentHp);
        }
    }

    /**
     * Attacks a boss with a character.
     *
     * @param guildId the guild ID
     * @param character the attacking character
     * @return damage dealt
     */
    public int attackBoss(String guildId, RPGCharacter character) {
        ServerBossState state = getState(guildId);
        if (state == null) {
            return 0;
        }

        Boss boss = state.getCurrentBoss();
        SuperBoss superBoss = state.getCurrentSuperBoss();

        if (boss == null && superBoss == null) {
            return 0; // No active boss
        }

        // Check for Gravebound Presence Raise Fallen mechanic
        // If character has Gravebound Presence and would die, set HP to 1 instead
        // Note: Currently boss battles don't deal damage to characters, but this is ready for future
        // implementation
        if (character.getLegendaryAura() != null
                && character
                .getLegendaryAura()
                .equals(
                        com.tatumgames.mikros.games.rpg.achievements.LegendaryAura.GRAVEBOUND_PRESENCE
                                .name())) {
            // Check if character would die (HP would go to 0 or below)
            // This would be checked if boss damage mechanics are added
            // For now, we just ensure the flag is set correctly
            if (!character.isRaisedFallenThisBoss()) {
                // Character can be raised once per boss fight
                // TODO: When boss damage is implemented, check if HP would go to 0, then:
                // - Set HP to 1 instead of 0
                // - Set raisedFallenThisBoss = true
                // - Increment timesRaisedFallen
                // - Add flavor text: "Dark sigils flare as the fallen hero is bound to the fight by
                // forbidden magic…"
            }
        }

        // Get active blessing for this character's class
        Blessing blessing = blessingService.getBlessingForClass(guildId, character.getCharacterClass());

        // Calculate damage based on character stats and class (with blessing if active)
        int baseDamage =
                calculateDamage(character, boss != null ? boss.getType() : superBoss.getType(), blessing);

        // Apply class bonuses
        double multiplier =
                getClassBonus(
                        character.getCharacterClass(), boss != null ? boss.getType() : superBoss.getType());
        int damage = (int) (baseDamage * multiplier);

        // Apply Song of Nilfheim aura effect (+5% damage if aura holder present)
        // Get all participants who have dealt damage
        Map<String, Integer> allDamage = damageTracking.get(guildId);
        if (allDamage != null) {
            java.util.List<String> participants = new java.util.ArrayList<>(allDamage.keySet());
            participants.add(character.getDiscordId()); // Include current attacker
            damage = auraService.applyAuraEffects(guildId, participants, damage);
        }

        // Apply Nilfheim event effects
        NilfheimEventType activeEvent = nilfheimEventService.getActiveEvent(guildId);
        if (activeEvent != null) {
            if (activeEvent.getEffectType() == NilfheimEventType.EventEffectType.BOSS_DAMAGE_BOOST) {
                // Frostborne Echoes: +8% damage to bosses
                damage = (int) (damage * (1.0 + activeEvent.getEffectValue()));
            }
        }

        // Check for Class Harmony mechanic and track class participation
        boolean hasHarmonyMechanic =
                (boss != null && boss.hasClassHarmonyMechanic())
                        || (superBoss != null && superBoss.hasClassHarmonyMechanic());

        if (hasHarmonyMechanic) {
            // Track class participation
            classParticipation
                    .computeIfAbsent(guildId, k -> new ConcurrentHashMap<>())
                    .merge(character.getCharacterClass(), 1, Integer::sum);

            // Calculate and apply class harmony resistance
            double resistance = calculateClassHarmonyResistance(guildId);
            damage = (int) (damage * (1.0 - resistance));
        }

        // Apply damage
        boolean defeated;
        if (boss != null) {
            defeated = boss.takeDamage(damage);
        } else {
            defeated = superBoss.takeDamage(damage);
        }

        // Track damage
        damageTracking
                .computeIfAbsent(guildId, k -> new ConcurrentHashMap<>())
                .merge(character.getDiscordId(), damage, Integer::sum);

        // Oathbreaker: Gain corruption from boss damage
        if (character.getCharacterClass()
                == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
            int corruptionGain = 0;
            if (superBoss != null) {
                // Super boss: +1 per 150 damage
                corruptionGain = damage / 150;
            } else {
                // Normal boss: +1 per 200 damage
                corruptionGain = damage / 200;
            }
            if (corruptionGain > 0) {
                character.addCorruption(corruptionGain);
            }
        }

        // Track cursed boss fight participation
        var activeCurses = worldCurseService.getActiveCurses(guildId);
        if (!activeCurses.isEmpty()) {
            character.incrementCursedBossFights();

            // Oathbreaker: Gain corruption from acting during world curses
            if (character.getCharacterClass()
                    == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
                character.addCorruption(1);
            }
        }

        if (defeated) {
            // Track last hitter before handling defeat
            String lastHitterId = character.getDiscordId();
            String lastHitterName = character.getName();
            handleBossDefeat(guildId, boss != null, lastHitterId, lastHitterName);
        }

        return damage;
    }

    /**
     * Calculates damage based on character stats. Applies blessing stat multipliers if a blessing is
     * active.
     *
     * @param character the character
     * @param bossType the boss type
     * @param blessing the active blessing (can be null)
     * @return calculated damage
     */
    private int calculateDamage(RPGCharacter character, BossType bossType, Blessing blessing) {
        int baseDamage = 100 + (character.getLevel() * 50);

        // Apply blessing stat multipliers if active
        double strMultiplier = blessing != null ? blessing.getStrMultiplier() : 1.0;
        double agiMultiplier = blessing != null ? blessing.getAgiMultiplier() : 1.0;
        double intMultiplier = blessing != null ? blessing.getIntMultiplier() : 1.0;

        // Add stat bonuses (with blessing multipliers)
        switch (character.getCharacterClass()) {
            case WARRIOR, KNIGHT, OATHBREAKER -> {
                int effectiveStr = (int) character.getStats().getEffectiveStrength(strMultiplier);
                baseDamage += effectiveStr * 10;
            }
            case MAGE, NECROMANCER, PRIEST -> {
                int effectiveInt = (int) character.getStats().getEffectiveIntelligence(intMultiplier);
                baseDamage += effectiveInt * 10;
            }
            case ROGUE -> {
                int effectiveAgi = (int) character.getStats().getEffectiveAgility(agiMultiplier);
                baseDamage += effectiveAgi * 10;
            }
        }

        // Add luck bonus
        baseDamage += character.getStats().getLuck() * 5;

        // Random variance
        int variance = (int) (baseDamage * 0.2); // ±20%
        baseDamage += (int) (Math.random() * variance * 2) - variance;

        return Math.max(50, baseDamage); // Minimum 50 damage
    }

    /**
     * Gets class bonus multiplier against a boss type.
     */
    private double getClassBonus(CharacterClass characterClass, BossType bossType) {
        // Class bonuses based on boss type
        return switch (characterClass) {
            case WARRIOR -> bossType == BossType.BEAST ? 1.2 : 1.0;
            case KNIGHT -> (bossType == BossType.GIANT || bossType == BossType.UNDEAD) ? 1.2 : 1.0;
            case MAGE -> (bossType == BossType.SPIRIT || bossType == BossType.ELEMENTAL) ? 1.2 : 1.0;
            case ROGUE -> (bossType == BossType.HUMANOID || bossType == BossType.BEAST) ? 1.2 : 1.0;
            case NECROMANCER -> (bossType == BossType.SPIRIT || bossType == BossType.UNDEAD) ? 1.2 : 1.0;
            case PRIEST -> (bossType == BossType.UNDEAD || bossType == BossType.DEMON) ? 1.2 : 1.0;
            case OATHBREAKER -> (bossType == BossType.UNDEAD || bossType == BossType.DEMON) ? 1.2 : 1.0;
            default -> 1.0;
        };
    }

    /**
     * Handles boss defeat and progression.
     *
     * @param guildId the guild ID
     * @param isNormalBoss whether it was a normal boss
     * @param lastHitterId the user ID of the last hitter
     * @param lastHitterName the name of the last hitter
     */
    private void handleBossDefeat(
            String guildId, boolean isNormalBoss, String lastHitterId, String lastHitterName) {
        ServerBossState state = getState(guildId);
        if (state == null) {
            return;
        }

        // Clear curses that expire on defeat (victory removes curses)
        worldCurseService.clearCursesOnDefeat(guildId);

        // Clear active blessing (blessings expire on boss defeat)
        blessingService.clearBlessing(guildId);

        // Reset consecutive failures on victory
        state.resetConsecutiveFailures();

        // Calculate 30% of participants (rounded up) for XP rewards
        Map<String, Integer> allDamage = damageTracking.get(guildId);
        int totalParticipants = (allDamage != null) ? allDamage.size() : 0;
        int rewardCount = (int) Math.ceil(totalParticipants * 0.30); // Top 30%, rounded up
        int limit = Math.max(1, rewardCount); // At least 1 person gets rewarded

        // Get top damage dealers for XP rewards (top 30% of participants)
        Map<String, Integer> topDamage = getTopDamageDealers(guildId, limit);

        // Calculate total XP pool based on boss type and level
        int totalXpPool;
        int bossLevel;
        if (isNormalBoss) {
            bossLevel = state.getBossLevel();
            totalXpPool = 500 + (bossLevel * 100); // Base 500 + 100 per level
        } else {
            bossLevel = state.getSuperBossLevel();
            totalXpPool = 1000 + (bossLevel * 200); // Base 1000 + 200 per level
        }

        // Store XP rewards for announcement
        Map<String, Integer> xpRewards = new LinkedHashMap<>();

        // Distribute XP to top damage dealers proportionally
        if (!topDamage.isEmpty()) {
            // Calculate total damage from top damage dealers
            int totalTopDamage = topDamage.values().stream().mapToInt(Integer::intValue).sum();

            if (totalTopDamage > 0) {
                int rank = 1;
                String topDamageDealerId = null;
                for (Map.Entry<String, Integer> entry : topDamage.entrySet()) {
                    String userId = entry.getKey();
                    int playerDamage = entry.getValue();

                    // Track top damage dealer (rank 1)
                    if (rank == 1) {
                        topDamageDealerId = userId;
                    }

                    RPGCharacter character = characterService.getCharacter(userId);
                    if (character != null) {
                        // Calculate proportional XP
                        double damageRatio = (double) playerDamage / totalTopDamage;
                        int baseXp = (int) (totalXpPool * damageRatio);

                        // Apply rank bonus: #1 gets 20% bonus, #2 gets 10% bonus
                        double rankBonus = 1.0;
                        if (rank == 1) {
                            rankBonus = 1.20; // 20% bonus for top damage dealer
                        } else if (rank == 2) {
                            rankBonus = 1.10; // 10% bonus for 2nd place
                        }

                        int finalXp = (int) (baseXp * rankBonus);
                        xpRewards.put(userId, finalXp);

                        // Award XP
                        boolean leveledUp = character.addXp(finalXp);

                        logger.info(
                                "Awarded {} XP to {} (rank #{}, {} damage) for boss defeat. Leveled up: {}",
                                finalXp,
                                character.getName(),
                                rank,
                                playerDamage,
                                leveledUp);
                    }
                    rank++;
                }

                // Track top damage dealer for Hero's Mark achievement
                if (topDamageDealerId != null) {
                    RPGCharacter topDealer = characterService.getCharacter(topDamageDealerId);
                    if (topDealer != null) {
                        topDealer.incrementTopDamageBossKills();

                        // TODO: Check for Hero's Mark achievement (100 normal OR 10 super)
                        // Hero's Mark: 100 top damage kills on normal bosses OR 10 on super bosses
                        // This will be implemented when we add achievement checking logic
                    }
                }
            }
        }

        // Credit kills and distribute rewards to all participants who dealt damage
        Map<String, Integer> damage = damageTracking.get(guildId);
        if (damage != null) {
            for (String userId : damage.keySet()) {
                RPGCharacter character = characterService.getCharacter(userId);
                if (character != null) {
                    if (isNormalBoss) {
                        character.incrementBossesKilled();
                    } else {
                        character.incrementSuperBossesKilled();
                    }

                    // Distribute boss rewards
                    distributeBossRewards(character, isNormalBoss);

                    // Check for lore recognition milestones (boss victory)
                    if (loreRecognitionService != null) {
                        loreRecognitionService.checkMilestones(character, guildId);
                    }
                }
            }
        }

        // Reset consecutive failures on victory
        state.resetConsecutiveFailures();

        if (isNormalBoss) {
            state.setNormalBossesDefeated(state.getNormalBossesDefeated() + 1);
            state.setNormalBossesSinceSuper(state.getNormalBossesSinceSuper() + 1);
            state.setCurrentBoss(null);

            // Check for level up: TotalDefeated >= 6 × currentBossLevel
            int required = 6 * state.getBossLevel();
            if (state.getNormalBossesDefeated() >= required) {
                state.setBossLevel(state.getBossLevel() + 1);
                logger.info("Boss level increased to {} for guild {}", state.getBossLevel(), guildId);
            }
        } else {
            state.setSuperBossesDefeated(state.getSuperBossesDefeated() + 1);
            state.setNormalBossesSinceSuper(0);
            state.setCurrentSuperBoss(null);

            // Check for super boss level up: SuperBossesDefeated >= 2 × superBossLevel
            int required = 2 * state.getSuperBossLevel();
            if (state.getSuperBossesDefeated() >= required) {
                state.setSuperBossLevel(state.getSuperBossLevel() + 1);
                logger.info(
                        "Super boss level increased to {} for guild {}", state.getSuperBossLevel(), guildId);
            }
        }

        // Store defeat info for announcement (before clearing boss state)
        String bossName =
                isNormalBoss
                        ? (state.getCurrentBoss() != null ? state.getCurrentBoss().getName() : "Unknown Boss")
                        : (state.getCurrentSuperBoss() != null
                        ? state.getCurrentSuperBoss().getName()
                        : "Unknown Super Boss");

        // Get all participants
        Map<String, Integer> allParticipants = damageTracking.get(guildId);

        // Store defeat info for announcement
        recentDefeats.put(
                guildId,
                new DefeatInfo(
                        bossName,
                        isNormalBoss,
                        lastHitterId,
                        lastHitterName,
                        allParticipants != null ? new LinkedHashMap<>(allParticipants) : new LinkedHashMap<>(),
                        new LinkedHashMap<>(xpRewards),
                        bossLevel));

        // Clear damage tracking and class participation
        damageTracking.remove(guildId);
        classParticipation.remove(guildId);
    }

    /**
     * Gets and removes recent defeat info for a guild (for announcement).
     *
     * @param guildId the guild ID
     * @return defeat info, or null if none
     */
    public DefeatInfo getAndClearRecentDefeat(String guildId) {
        return recentDefeats.remove(guildId);
    }

    /**
     * Gets top damage dealers for a boss battle.
     *
     * @param guildId the guild ID
     * @param limit maximum number of players to return
     * @return map of userId -> total damage
     */
    public Map<String, Integer> getTopDamageDealers(String guildId, int limit) {
        Map<String, Integer> damage = damageTracking.get(guildId);
        if (damage == null || damage.isEmpty()) {
            return new LinkedHashMap<>();
        }

        return damage.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(
                        LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    /**
     * Resets all boss data for a specific server. This clears: - Boss state (level, progression,
     * current boss) - Damage tracking
     *
     * @param guildId the guild ID
     */
    public void resetServerData(String guildId) {
        serverStates.remove(guildId);
        damageTracking.remove(guildId);
        logger.warn("Reset boss data for server {}", guildId);
    }

    /**
     * Validates and cleans up boss state for a guild. Called during spawn attempts and periodically
     * to ensure consistency.
     *
     * <p>Cleanup actions: - Removes expired/defeated bosses from state - Ensures only one boss is
     * active at a time - Resets damage tracking if no active boss
     *
     * @param guildId the guild ID
     */
    public void validateAndCleanupBossState(String guildId) {
        ReentrantLock lock = getGuildLock(guildId);
        lock.lock();
        try {
            ServerBossState state = getState(guildId);
            if (state == null) {
                return;
            }

            Boss currentBoss = state.getCurrentBoss();
            SuperBoss currentSuperBoss = state.getCurrentSuperBoss();

            // Clean up expired/defeated normal boss
            if (currentBoss != null && (currentBoss.isExpired() || currentBoss.isDefeated())) {
                logger.info(
                        "Cleanup: Clearing expired/defeated boss {} for guild {}",
                        currentBoss.getName(),
                        guildId);
                state.setCurrentBoss(null);
                currentBoss = null;
            }

            // Clean up expired/defeated super boss
            if (currentSuperBoss != null
                    && (currentSuperBoss.isExpired() || currentSuperBoss.isDefeated())) {
                logger.info(
                        "Cleanup: Clearing expired/defeated super boss {} for guild {}",
                        currentSuperBoss.getName(),
                        guildId);
                state.setCurrentSuperBoss(null);
                currentSuperBoss = null;
            }

            // If both exist somehow, keep the most recently spawned one
            if (currentBoss != null && currentSuperBoss != null) {
                logger.warn(
                        "Cleanup: Both normal and super boss active for guild {}, keeping newer", guildId);
                if (currentBoss.getSpawnTime().isAfter(currentSuperBoss.getSpawnTime())) {
                    state.setCurrentSuperBoss(null);
                } else {
                    state.setCurrentBoss(null);
                }
            }

            // Clear damage tracking if no active boss
            if (state.getCurrentBoss() == null && state.getCurrentSuperBoss() == null) {
                damageTracking.remove(guildId);
                classParticipation.remove(guildId);
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Distributes boss rewards to a character. Normal boss: guaranteed 1 essence + 25% catalyst Super
     * boss: guaranteed catalyst + 1-3 essences
     *
     * @param character the character receiving rewards
     * @param isNormalBoss whether it was a normal boss
     */
    private void distributeBossRewards(RPGCharacter character, boolean isNormalBoss) {
        RPGInventory inventory = character.getInventory();

        if (isNormalBoss) {
            // Normal boss: guaranteed 1 essence + 25% catalyst
            EssenceType essence = getRandomEssence();
            inventory.addEssence(essence, 1);

            if (random.nextDouble() < 0.25) {
                CatalystType catalyst = getRandomCatalyst();
                inventory.addCatalyst(catalyst, 1);
            }
        } else {
            // Super boss: guaranteed catalyst + 1-3 essences
            CatalystType catalyst = getRandomCatalyst();
            inventory.addCatalyst(catalyst, 1);

            int essenceCount = random.nextInt(3) + 1; // 1-3 essences
            for (int i = 0; i < essenceCount; i++) {
                EssenceType essence = getRandomEssence();
                inventory.addEssence(essence, 1);
            }
        }
    }

    /**
     * Distributes special rewards for secret boss defeat. Better rewards than normal or super bosses:
     * - Guaranteed 1 catalyst - 2-4 essences (random) - Bonus XP: 200 + (bossLevel * 50) - 10% chance
     * for additional rare catalyst
     *
     * @param character the character receiving rewards
     * @param bossLevel the level of the defeated secret boss
     */
    private void distributeSecretBossRewards(RPGCharacter character, int bossLevel) {
        RPGInventory inventory = character.getInventory();

        // Guaranteed catalyst
        CatalystType catalyst = getRandomCatalyst();
        inventory.addCatalyst(catalyst, 1);

        // 2-4 essences (better than super boss)
        int essenceCount = random.nextInt(3) + 2; // 2-4 essences
        for (int i = 0; i < essenceCount; i++) {
            EssenceType essence = getRandomEssence();
            inventory.addEssence(essence, 1);
        }

        // 10% chance for additional rare catalyst
        boolean rareCatalystGranted = random.nextDouble() < 0.10;
        if (rareCatalystGranted) {
            CatalystType rareCatalyst = getRandomCatalyst();
            inventory.addCatalyst(rareCatalyst, 1);
        }

        // Grant bonus XP: 200 + (bossLevel * 50)
        int bonusXp = 200 + (bossLevel * 50);
        character.addXp(bonusXp, loreRecognitionService);

        int totalCatalysts = rareCatalystGranted ? 2 : 1;
        logger.info(
                "Granted secret boss rewards to {}: {} catalyst(s), {} essences, {} XP",
                character.getName(),
                totalCatalysts,
                essenceCount,
                bonusXp);
    }

    /**
     * Gets a random essence type.
     *
     * @return random essence type
     */
    private EssenceType getRandomEssence() {
        EssenceType[] essences = EssenceType.values();
        return essences[random.nextInt(essences.length)];
    }

    /**
     * Gets a random catalyst type.
     *
     * @return random catalyst type
     */
    private CatalystType getRandomCatalyst() {
        CatalystType[] catalysts = CatalystType.values();
        return catalysts[random.nextInt(catalysts.length)];
    }

    /**
     * Refreshes heroic charges for all characters when a new boss spawns.
     */
    private void refreshHeroicChargesForAllCharacters() {
        Collection<RPGCharacter> allCharacters = characterService.getAllCharacters();
        int refreshedCount = 0;
        for (RPGCharacter character : allCharacters) {
            character.refreshHeroicCharges();
            refreshedCount++;
        }
        logger.info("Refreshed heroic charges for {} characters (new boss spawned)", refreshedCount);
    }

    /**
     * Calculates class harmony resistance based on class participation balance. Returns a resistance
     * value between 0.15 (15% resistance) and 0.90 (90% resistance).
     *
     * @param guildId the guild ID
     * @return resistance multiplier (0.15 to 0.90)
     */
    private double calculateClassHarmonyResistance(String guildId) {
        Map<CharacterClass, Integer> participation = classParticipation.get(guildId);
        if (participation == null || participation.isEmpty()) {
            // No participants yet, return minimum resistance
            return 0.15;
        }

        // Calculate total participants
        int totalParticipants = participation.values().stream().mapToInt(Integer::intValue).sum();
        if (totalParticipants == 0) {
            return 0.15;
        }

        // Calculate percentages for each class
        Map<CharacterClass, Double> percentages = new LinkedHashMap<>();
        for (Map.Entry<CharacterClass, Integer> entry : participation.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalParticipants;
            percentages.put(entry.getKey(), percentage);
        }

        // Filter classes with <5% participation (don't count toward balance)
        Map<CharacterClass, Double> validPercentages = new LinkedHashMap<>();
        for (Map.Entry<CharacterClass, Double> entry : percentages.entrySet()) {
            if (entry.getValue() >= 5.0) {
                validPercentages.put(entry.getKey(), entry.getValue());
            }
        }

        if (validPercentages.isEmpty()) {
            // All classes below 5%, return minimum resistance
            return 0.15;
        }

        // Calculate dominance gap (highest % - lowest %)
        double maxPercentage =
                validPercentages.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double minPercentage =
                validPercentages.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double dominanceGap = maxPercentage - minPercentage;

        // Map gap to resistance tier
        if (dominanceGap >= 60.0) {
            return 0.90; // 90% resistance
        } else if (dominanceGap >= 45.0) {
            return 0.75; // 75% resistance
        } else if (dominanceGap >= 30.0) {
            return 0.60; // 60% resistance
        } else if (dominanceGap >= 20.0) {
            return 0.45; // 45% resistance
        } else if (dominanceGap >= 10.0) {
            return 0.30; // 30% resistance
        } else {
            return 0.15; // 15% resistance (floor)
        }
    }

    /**
     * Gets class participation percentages for display.
     *
     * @param guildId the guild ID
     * @return map of class -> percentage
     */
    public Map<CharacterClass, Double> getClassParticipationPercentages(String guildId) {
        Map<CharacterClass, Integer> participation = classParticipation.get(guildId);
        if (participation == null || participation.isEmpty()) {
            return new LinkedHashMap<>();
        }

        int totalParticipants = participation.values().stream().mapToInt(Integer::intValue).sum();
        if (totalParticipants == 0) {
            return new LinkedHashMap<>();
        }

        Map<CharacterClass, Double> percentages = new LinkedHashMap<>();
        for (Map.Entry<CharacterClass, Integer> entry : participation.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalParticipants;
            percentages.put(entry.getKey(), percentage);
        }

        return percentages;
    }

    /**
     * Gets harmony feedback message based on dominance gap.
     *
     * @param guildId the guild ID
     * @param isSuperBoss whether this is a super boss
     * @return narrative feedback message
     */
    public String getHarmonyFeedbackMessage(String guildId, boolean isSuperBoss) {
        Map<CharacterClass, Integer> participation = classParticipation.get(guildId);
        if (participation == null || participation.isEmpty()) {
            return "The creature awaits its challengers...";
        }

        int totalParticipants = participation.values().stream().mapToInt(Integer::intValue).sum();
        if (totalParticipants == 0) {
            return "The creature awaits its challengers...";
        }

        // Calculate percentages
        Map<CharacterClass, Double> percentages = new LinkedHashMap<>();
        for (Map.Entry<CharacterClass, Integer> entry : participation.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalParticipants;
            percentages.put(entry.getKey(), percentage);
        }

        // Filter classes with <5% participation
        Map<CharacterClass, Double> validPercentages = new LinkedHashMap<>();
        for (Map.Entry<CharacterClass, Double> entry : percentages.entrySet()) {
            if (entry.getValue() >= 5.0) {
                validPercentages.put(entry.getKey(), entry.getValue());
            }
        }

        if (validPercentages.isEmpty()) {
            return "The creature awaits its challengers...";
        }

        // Calculate dominance gap
        double maxPercentage =
                validPercentages.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double minPercentage =
                validPercentages.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double dominanceGap = maxPercentage - minPercentage;

        // Return message based on gap tier
        if (dominanceGap >= 60.0) {
            return "The creature stabilizes, feeding on overwhelming uniformity. Its form grows more solid with each unified strike.";
        } else if (dominanceGap >= 30.0) {
            return "Discordant forces begin to crack its defenses. The creature's form wavers between stability and chaos.";
        } else {
            return "Conflicting forces tear at the creature's core. Its form destabilizes violently—the balance shifts against it!";
        }
    }

    /**
     * Server-specific boss state.
     */
    public static class ServerBossState {
        private int bossLevel = 1;
        private int superBossLevel = 1;
        private int normalBossesDefeated = 0;
        private int superBossesDefeated = 0;
        private int normalBossesSinceSuper = 0;
        // Secret bosses per player (userId -> Boss)
        private final Map<String, Boss> secretBosses = new ConcurrentHashMap<>();
        private Boss currentBoss;
        private SuperBoss currentSuperBoss;
        private int consecutiveFailures = 0; // Track consecutive boss failures for empowerment
        private Instant lastBossSpawnTime; // Cooldown tracking for spawn rate limiting
        private String lastSpawnedBossId; // Track to prevent duplicate spawn attempts

        // Getters and setters

        public Instant getLastBossSpawnTime() {
            return lastBossSpawnTime;
        }

        public void setLastBossSpawnTime(Instant lastBossSpawnTime) {
            this.lastBossSpawnTime = lastBossSpawnTime;
        }

        public String getLastSpawnedBossId() {
            return lastSpawnedBossId;
        }

        public void setLastSpawnedBossId(String lastSpawnedBossId) {
            this.lastSpawnedBossId = lastSpawnedBossId;
        }

        public int getBossLevel() {
            return bossLevel;
        }

        public void setBossLevel(int bossLevel) {
            this.bossLevel = bossLevel;
        }

        public int getSuperBossLevel() {
            return superBossLevel;
        }

        public void setSuperBossLevel(int superBossLevel) {
            this.superBossLevel = superBossLevel;
        }

        public int getNormalBossesDefeated() {
            return normalBossesDefeated;
        }

        public void setNormalBossesDefeated(int normalBossesDefeated) {
            this.normalBossesDefeated = normalBossesDefeated;
        }

        public int getSuperBossesDefeated() {
            return superBossesDefeated;
        }

        public void setSuperBossesDefeated(int superBossesDefeated) {
            this.superBossesDefeated = superBossesDefeated;
        }

        public int getNormalBossesSinceSuper() {
            return normalBossesSinceSuper;
        }

        public void setNormalBossesSinceSuper(int normalBossesSinceSuper) {
            this.normalBossesSinceSuper = normalBossesSinceSuper;
        }

        public Boss getCurrentBoss() {
            return currentBoss;
        }

        public void setCurrentBoss(Boss currentBoss) {
            this.currentBoss = currentBoss;
        }

        public SuperBoss getCurrentSuperBoss() {
            return currentSuperBoss;
        }

        public void setCurrentSuperBoss(SuperBoss currentSuperBoss) {
            this.currentSuperBoss = currentSuperBoss;
        }

        public int getConsecutiveFailures() {
            return consecutiveFailures;
        }

        public void setConsecutiveFailures(int consecutiveFailures) {
            this.consecutiveFailures = consecutiveFailures;
        }

        public void incrementConsecutiveFailures() {
            this.consecutiveFailures++;
        }

        public void resetConsecutiveFailures() {
            this.consecutiveFailures = 0;
        }

        /**
         * Gets the secret boss for a specific user.
         *
         * @param userId the user ID
         * @return the secret boss, or null if none
         */
        public Boss getSecretBoss(String userId) {
            return secretBosses.get(userId);
        }

        /**
         * Sets a secret boss for a specific user.
         *
         * @param userId the user ID
         * @param boss   the secret boss
         */
        public void setSecretBoss(String userId, Boss boss) {
            if (boss == null) {
                secretBosses.remove(userId);
            } else {
                secretBosses.put(userId, boss);
            }
        }

        /**
         * Removes a secret boss for a specific user.
         *
         * @param userId the user ID
         */
        public void removeSecretBoss(String userId) {
            secretBosses.remove(userId);
        }
    }

    /**
     * Information about a recent boss defeat for announcement purposes.
     */
    public static class DefeatInfo {
        private final String bossName;
        private final boolean isNormalBoss;
        private final String lastHitterId;
        private final String lastHitterName;
        private final Map<String, Integer> participants; // userId -> damage
        private final Map<String, Integer> xpRewards; // userId -> XP awarded
        private final int bossLevel;

        public DefeatInfo(
                String bossName,
                boolean isNormalBoss,
                String lastHitterId,
                String lastHitterName,
                Map<String, Integer> participants,
                Map<String, Integer> xpRewards,
                int bossLevel) {
            this.bossName = bossName;
            this.isNormalBoss = isNormalBoss;
            this.lastHitterId = lastHitterId;
            this.lastHitterName = lastHitterName;
            this.participants = participants;
            this.xpRewards = xpRewards;
            this.bossLevel = bossLevel;
        }

        public String getBossName() {
            return bossName;
        }

        public boolean isNormalBoss() {
            return isNormalBoss;
        }

        public String getLastHitterId() {
            return lastHitterId;
        }

        public String getLastHitterName() {
            return lastHitterName;
        }

        public Map<String, Integer> getParticipants() {
            return participants;
        }

        public Map<String, Integer> getXpRewards() {
            return xpRewards;
        }

        public int getBossLevel() {
            return bossLevel;
    }
    }
}
