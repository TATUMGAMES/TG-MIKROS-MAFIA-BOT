package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.boss.BossCatalog;
import com.tatumgames.mikros.games.rpg.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing boss battles per server.
 * Handles boss spawning, progression, and damage tracking.
 */
public class BossService {
    private static final Logger logger = LoggerFactory.getLogger(BossService.class);

    // Per-server boss state: guildId -> ServerBossState
    private final Map<String, ServerBossState> serverStates;

    // Damage tracking: guildId -> Map<userId, totalDamage>
    private final Map<String, Map<String, Integer>> damageTracking;

    private final CharacterService characterService;
    private static final Random random = new Random();

    /**
     * Creates a new BossService.
     *
     * @param characterService the character service for tracking kills
     */
    public BossService(CharacterService characterService) {
        this.serverStates = new ConcurrentHashMap<>();
        this.damageTracking = new ConcurrentHashMap<>();
        this.characterService = characterService;
        logger.info("BossService initialized");
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
     * Spawns a new normal boss for a server.
     *
     * @param guildId the guild ID
     * @return the spawned boss
     */
    public Boss spawnNormalBoss(String guildId) {
        ServerBossState state = getOrCreateState(guildId);

        // Check if super boss should spawn instead
        if (state.getNormalBossesSinceSuper() >= 3) {
            return null; // Signal to spawn super boss instead
        }

        int level = state.getBossLevel();
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(level);
        Boss boss = BossCatalog.createBoss(definition, level);

        state.setCurrentBoss(boss);
        damageTracking.put(guildId, new ConcurrentHashMap<>());

        logger.info("Spawned normal boss {} (Level {}) for guild {}", boss.getName(), level, guildId);
        return boss;
    }

    /**
     * Spawns a new super boss for a server.
     *
     * @param guildId the guild ID
     * @return the spawned super boss
     */
    public SuperBoss spawnSuperBoss(String guildId) {
        ServerBossState state = getOrCreateState(guildId);

        int level = state.getSuperBossLevel();
        BossCatalog.SuperBossDefinition definition = BossCatalog.getSuperBoss(level);
        SuperBoss superBoss = BossCatalog.createSuperBoss(definition, level);

        state.setCurrentSuperBoss(superBoss);
        state.setNormalBossesSinceSuper(0); // Reset counter
        damageTracking.put(guildId, new ConcurrentHashMap<>());

        logger.info("Spawned super boss {} (Level {}) for guild {}", superBoss.getName(), level, guildId);
        return superBoss;
    }

    /**
     * Attacks a boss with a character.
     *
     * @param guildId   the guild ID
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

        // Calculate damage based on character stats and class
        int baseDamage = calculateDamage(character, boss != null ? boss.getType() : superBoss.getType());

        // Apply class bonuses
        double multiplier = getClassBonus(character.getCharacterClass(), boss != null ? boss.getType() : superBoss.getType());
        int damage = (int) (baseDamage * multiplier);

        // Apply damage
        boolean defeated;
        if (boss != null) {
            defeated = boss.takeDamage(damage);
        } else {
            defeated = superBoss.takeDamage(damage);
        }

        // Track damage
        damageTracking.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>())
                .merge(character.getDiscordId(), damage, Integer::sum);

        if (defeated) {
            handleBossDefeat(guildId, boss != null);
        }

        return damage;
    }

    /**
     * Calculates damage based on character stats.
     */
    private int calculateDamage(RPGCharacter character, BossType bossType) {
        int baseDamage = 100 + (character.getLevel() * 50);

        // Add stat bonuses
        switch (character.getCharacterClass()) {
            case WARRIOR, KNIGHT -> baseDamage += character.getStats().getStrength() * 10;
            case MAGE, NECROMANCER, PRIEST -> baseDamage += character.getStats().getIntelligence() * 10;
            case ROGUE -> baseDamage += character.getStats().getAgility() * 10;
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
        switch (characterClass) {
            case WARRIOR:
                return bossType == BossType.BEAST ? 1.2 : 1.0;
            case KNIGHT:
                return (bossType == BossType.GIANT || bossType == BossType.UNDEAD) ? 1.2 : 1.0;
            case MAGE:
                return (bossType == BossType.SPIRIT || bossType == BossType.ELEMENTAL) ? 1.2 : 1.0;
            case ROGUE:
                return (bossType == BossType.HUMANOID || bossType == BossType.BEAST) ? 1.2 : 1.0;
            case NECROMANCER:
                return (bossType == BossType.SPIRIT || bossType == BossType.UNDEAD) ? 1.2 : 1.0;
            case PRIEST:
                return (bossType == BossType.UNDEAD || bossType == BossType.DEMON) ? 1.2 : 1.0;
            default:
                return 1.0;
        }
    }

    /**
     * Handles boss defeat and progression.
     */
    private void handleBossDefeat(String guildId, boolean isNormalBoss) {
        ServerBossState state = getState(guildId);
        if (state == null) {
            return;
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
                }
            }
        }

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
                logger.info("Super boss level increased to {} for guild {}", state.getSuperBossLevel(), guildId);
            }
        }

        // Clear damage tracking
        damageTracking.remove(guildId);
    }

    /**
     * Gets top damage dealers for a boss battle.
     *
     * @param guildId the guild ID
     * @param limit   maximum number of players to return
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
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    /**
     * Resets all boss data for a specific server.
     * This clears:
     * - Boss state (level, progression, current boss)
     * - Damage tracking
     *
     * @param guildId the guild ID
     */
    public void resetServerData(String guildId) {
        serverStates.remove(guildId);
        damageTracking.remove(guildId);
        logger.warn("Reset boss data for server {}", guildId);
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
        private Boss currentBoss;
        private SuperBoss currentSuperBoss;

        // Getters and setters

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
    }

    /**
     * Distributes boss rewards to a character.
     * Normal boss: guaranteed 1 essence + 25% catalyst
     * Super boss: guaranteed catalyst + 1-3 essences
     *
     * @param character  the character receiving rewards
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
}

