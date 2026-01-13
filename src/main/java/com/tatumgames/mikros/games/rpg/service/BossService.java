package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.boss.BossCatalog;
import com.tatumgames.mikros.games.rpg.events.NilfheimEventType;
import com.tatumgames.mikros.games.rpg.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
    private static final Random random = new Random();
    // Per-server boss state: guildId -> ServerBossState
    private final Map<String, ServerBossState> serverStates;
    // Damage tracking: guildId -> Map<userId, totalDamage>
    private final Map<String, Map<String, Integer>> damageTracking;
    // Class participation tracking: guildId -> Map<CharacterClass, count>
    private final Map<String, Map<CharacterClass, Integer>> classParticipation;
    private final CharacterService characterService;
    private final AuraService auraService;
    private final WorldCurseService worldCurseService;
    private final NilfheimEventService nilfheimEventService;
    private final LoreRecognitionService loreRecognitionService;

    /**
     * Creates a new BossService.
     *
     * @param characterService       the character service for tracking kills
     * @param auraService            the aura service for applying legendary aura effects
     * @param worldCurseService      the world curse service for clearing curses on defeat
     * @param nilfheimEventService   the Nilfheim event service for server-wide events
     * @param loreRecognitionService the lore recognition service for milestone checks
     */
    public BossService(CharacterService characterService, AuraService auraService, WorldCurseService worldCurseService, NilfheimEventService nilfheimEventService, LoreRecognitionService loreRecognitionService) {
        this.serverStates = new ConcurrentHashMap<>();
        this.damageTracking = new ConcurrentHashMap<>();
        this.classParticipation = new ConcurrentHashMap<>();
        this.characterService = characterService;
        this.auraService = auraService;
        this.worldCurseService = worldCurseService;
        this.nilfheimEventService = nilfheimEventService;
        this.loreRecognitionService = loreRecognitionService;
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
        BossCatalog.BossDefinition definition;
        
        // Check if Unity Devourer should spawn (every 10th normal boss)
        // After 9 defeats, the next spawn (10th boss) should be Unity Devourer
        if (state.getNormalBossesDefeated() > 0 && state.getNormalBossesDefeated() % 10 == 9) {
            definition = BossCatalog.getUnityDevourer(level);
            logger.info("Unity Devourer spawn triggered ({} normal bosses defeated, spawning 10th boss) for guild {}", 
                    state.getNormalBossesDefeated(), guildId);
        } else {
            definition = BossCatalog.getRandomNormalBoss(level);
        }
        
        Boss boss = BossCatalog.createBoss(definition, level);

        state.setCurrentBoss(boss);
        damageTracking.put(guildId, new ConcurrentHashMap<>());
        classParticipation.put(guildId, new ConcurrentHashMap<>());

        // Refresh heroic charges for all characters when new boss spawns
        refreshHeroicChargesForAllCharacters();

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
        classParticipation.put(guildId, new ConcurrentHashMap<>());

        // Refresh heroic charges for all characters when new boss spawns
        refreshHeroicChargesForAllCharacters();

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

        // Check for Gravebound Presence Raise Fallen mechanic
        // If character has Gravebound Presence and would die, set HP to 1 instead
        // Note: Currently boss battles don't deal damage to characters, but this is ready for future implementation
        if (character.getLegendaryAura() != null &&
                character.getLegendaryAura().equals(com.tatumgames.mikros.games.rpg.achievements.LegendaryAura.GRAVEBOUND_PRESENCE.name())) {
            // Check if character would die (HP would go to 0 or below)
            // This would be checked if boss damage mechanics are added
            // For now, we just ensure the flag is set correctly
            if (!character.isRaisedFallenThisBoss()) {
                // Character can be raised once per boss fight
                // TODO: When boss damage is implemented, check if HP would go to 0, then:
                // - Set HP to 1 instead of 0
                // - Set raisedFallenThisBoss = true
                // - Increment timesRaisedFallen
                // - Add flavor text: "Dark sigils flare as the fallen hero is bound to the fight by forbidden magic…"
            }
        }

        // Calculate damage based on character stats and class
        int baseDamage = calculateDamage(character, boss != null ? boss.getType() : superBoss.getType());

        // Apply class bonuses
        double multiplier = getClassBonus(character.getCharacterClass(), boss != null ? boss.getType() : superBoss.getType());
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
        boolean hasHarmonyMechanic = (boss != null && boss.hasClassHarmonyMechanic()) ||
                (superBoss != null && superBoss.hasClassHarmonyMechanic());
        
        if (hasHarmonyMechanic) {
            // Track class participation
            classParticipation.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>())
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
        damageTracking.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>())
                .merge(character.getDiscordId(), damage, Integer::sum);

        // Oathbreaker: Gain corruption from boss damage
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
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
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
                character.addCorruption(1);
            }
        }

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
            case WARRIOR, KNIGHT, OATHBREAKER -> baseDamage += character.getStats().getStrength() * 10;
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
            case OATHBREAKER:
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

        // Clear curses that expire on defeat (victory removes curses)
        worldCurseService.clearCursesOnDefeat(guildId);

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

        // Distribute XP to top damage dealers proportionally
        if (!topDamage.isEmpty()) {
            // Calculate total damage from top damage dealers
            int totalTopDamage = topDamage.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();

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

                        // Award XP
                        boolean leveledUp = character.addXp(finalXp);

                        logger.info("Awarded {} XP to {} (rank #{}, {} damage) for boss defeat. Leveled up: {}",
                                finalXp, character.getName(), rank, playerDamage, leveledUp);
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
                        loreRecognitionService.checkMilestones(character);
                    }
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

        // Clear damage tracking and class participation
        damageTracking.remove(guildId);
        classParticipation.remove(guildId);
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
     * Distributes boss rewards to a character.
     * Normal boss: guaranteed 1 essence + 25% catalyst
     * Super boss: guaranteed catalyst + 1-3 essences
     *
     * @param character    the character receiving rewards
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
     * Calculates class harmony resistance based on class participation balance.
     * Returns a resistance value between 0.15 (15% resistance) and 0.90 (90% resistance).
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
        double maxPercentage = validPercentages.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double minPercentage = validPercentages.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
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
        double maxPercentage = validPercentages.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double minPercentage = validPercentages.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
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
}

