package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.InfusionType;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Service for checking character milestones and awarding story flags.
 * Provides narrative recognition for significant achievements.
 */
public class LoreRecognitionService {
    private static final Logger logger = LoggerFactory.getLogger(LoreRecognitionService.class);
    private BossService bossService;
    private CharacterService characterService;

    /**
     * Creates a new LoreRecognitionService.
     */
    public LoreRecognitionService() {
        // Dependencies can be set later via setter methods
    }

    /**
     * Sets the BossService dependency (called after BossService is created).
     *
     * @param bossService the boss service
     */
    public void setBossService(BossService bossService) {
        this.bossService = bossService;
    }

    /**
     * Sets the CharacterService dependency (called after CharacterService is created).
     *
     * @param characterService the character service
     */
    public void setCharacterService(CharacterService characterService) {
        this.characterService = characterService;
    }

    /**
     * Checks all milestones for a character and awards story flags if conditions are met.
     * Only awards flags if story flag slot is available (max 2).
     *
     * @param character the character to check
     */
    public void checkMilestones(RPGCharacter character) {
        checkMilestones(character, null);
    }

    /**
     * Checks all milestones for a character and awards story flags if conditions are met.
     * Only awards flags if story flag slot is available (max 2).
     *
     * @param character the character to check
     * @param guildId   the guild ID (optional, needed for secret boss spawning)
     */
    public void checkMilestones(RPGCharacter character, String guildId) {
        // Check each recognition type
        checkNilfheimsPersistence(character);
        checkTheResurrected(character);
        checkTheRescuer(character);
        checkMasterOfTheElements(character);
        checkFrostbornesLegacy(character);
        checkTheShatteringsEcho(character);
        checkStormwardensRespect(character);
        checkTheGrandLibrarysScholar(character);

        // Check irrevocable encounter milestones
        checkDeityPathCompletion(character);
        checkRelicBearer(character);
        checkOathOfNull(character);

        // Check Oathbreaker path choice (level 10-12)
        checkOathbreakerPathChoice(character);

        // Check secret boss milestones (only if guildId is provided)
        if (guildId != null) {
            checkSecretBossMilestones(character, guildId);
        }
    }

    /**
     * Nilfheim's Persistence: After 5+ curses survived.
     */
    private void checkNilfheimsPersistence(RPGCharacter character) {
        if (character.getCursedBossFights() >= 5) {
            String flag = "Persevered through darkness";
            if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                character.addStoryFlag(flag);
                logger.info("Character {} earned story flag: {}", character.getName(), flag);
            }
        }
    }

    /**
     * The Resurrected: After 3+ resurrections.
     */
    private void checkTheResurrected(RPGCharacter character) {
        if (character.getTotalResurrections() >= 3) {
            String flag = "Death's familiar";
            if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                character.addStoryFlag(flag);
                logger.info("Character {} earned story flag: {}", character.getName(), flag);
            }
        }
    }

    /**
     * The Rescuer (Priest only): After 5+ resurrections performed.
     */
    private void checkTheRescuer(RPGCharacter character) {
        if (character.getCharacterClass() == CharacterClass.PRIEST) {
            if (character.getTimesResurrectedOthers() >= 5) {
                String flag = "Savior of the fallen";
                if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                    character.addStoryFlag(flag);
                    logger.info("Character {} earned story flag: {}", character.getName(), flag);
                }
            }
        }
    }

    /**
     * Master of the Elements: After crafting all 5 base infusion types at least once.
     */
    private void checkMasterOfTheElements(RPGCharacter character) {
        Set<InfusionType> infusionsCrafted = character.getInfusionsCrafted();
        if (infusionsCrafted.size() >= 5) {
            // Check if all 5 base infusion types are crafted (excluding Elemental Convergence)
            boolean hasFrostClarity = infusionsCrafted.contains(InfusionType.FROST_CLARITY);
            boolean hasGaleFortune = infusionsCrafted.contains(InfusionType.GALE_FORTUNE);
            boolean hasEmberEndurance = infusionsCrafted.contains(InfusionType.EMBER_ENDURANCE);
            boolean hasAstralInsight = infusionsCrafted.contains(InfusionType.ASTRAL_INSIGHT);
            boolean hasVoidPrecision = infusionsCrafted.contains(InfusionType.VOID_PRECISION);

            if (hasFrostClarity && hasGaleFortune && hasEmberEndurance && hasAstralInsight && hasVoidPrecision) {
                String flag = "Elemental master";
                if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                    character.addStoryFlag(flag);
                    logger.info("Character {} earned story flag: {}", character.getName(), flag);
                }
            }
        }
    }

    /**
     * Frostborne's Legacy: After 10+ boss victories.
     */
    private void checkFrostbornesLegacy(RPGCharacter character) {
        int totalBossKills = character.getBossesKilled() + character.getSuperBossesKilled();
        if (totalBossKills >= 10) {
            String flag = "Frostborne's chosen";
            if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                character.addStoryFlag(flag);
                logger.info("Character {} earned story flag: {}", character.getName(), flag);
            }
        }
    }

    /**
     * The Shattering's Echo: After reaching Level 20.
     */
    private void checkTheShatteringsEcho(RPGCharacter character) {
        if (character.getLevel() >= 20) {
            String flag = "Echo of the Shattering";
            if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                character.addStoryFlag(flag);
                logger.info("Character {} earned story flag: {}", character.getName(), flag);
            }
        }
    }

    /**
     * Stormwarden's Respect (AGI-focused): After reaching 50+ AGI (base + crafted).
     */
    private void checkStormwardensRespect(RPGCharacter character) {
        int totalAgi = character.getStats().getAgility();
        if (totalAgi >= 50) {
            String flag = "Wind's chosen";
            if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                character.addStoryFlag(flag);
                logger.info("Character {} earned story flag: {}", character.getName(), flag);
            }
        }
    }

    /**
     * The Grand Library's Scholar (INT-focused): After reaching 50+ INT (base + crafted).
     */
    private void checkTheGrandLibrarysScholar(RPGCharacter character) {
        int totalInt = character.getStats().getIntelligence();
        if (totalInt >= 50) {
            String flag = "Library's chosen";
            if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                character.addStoryFlag(flag);
                logger.info("Character {} earned story flag: {}", character.getName(), flag);
            }
        }
    }

    /**
     * Deity Path Completion: After receiving a deity blessing.
     */
    private void checkDeityPathCompletion(RPGCharacter character) {
        if (character.getDeityBlessing() != null) {
            String flag = "Chosen by the gods";
            if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                character.addStoryFlag(flag);
                logger.info("Character {} earned story flag: {}", character.getName(), flag);
            }
        }
    }

    /**
     * Relic Bearer: After taking a blood relic.
     */
    private void checkRelicBearer(RPGCharacter character) {
        if (character.getRelicChoice() != null) {
            String flag = "Bearer of ancient power";
            if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                character.addStoryFlag(flag);
                logger.info("Character {} earned story flag: {}", character.getName(), flag);
            }
        }
    }

    /**
     * Oath of Null: After taking the anti-god path.
     */
    private void checkOathOfNull(RPGCharacter character) {
        if ("UNBOUND".equals(character.getPhilosophicalPath())) {
            String flag = "Unbound by choice";
            if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                character.addStoryFlag(flag);
                logger.info("Character {} earned story flag: {}", character.getName(), flag);
            }
        }
    }

    /**
     * Oathbreaker Path Choice: At level 10-12, Oathbreakers must choose Embrace or Purge.
     * This is a permanent choice that affects corruption cap and bonuses.
     */
    private void checkOathbreakerPathChoice(RPGCharacter character) {
        // Only for Oathbreakers who haven't chosen a path yet
        if (character.getCharacterClass() != CharacterClass.OATHBREAKER || character.getOathbreakerPath() != null) {
            return;
        }

        // Trigger at level 10-12 (randomly within this range)
        int level = character.getLevel();
        if (level >= 10 && level <= 12) {
            // 30% chance per milestone check to trigger path choice
            // This ensures it happens eventually but not immediately
            if (Math.random() < 0.30) {
                // Auto-choose based on corruption (high = Embrace, low = Purge)
                // In full implementation, this would be a player choice
                int corruption = character.getCorruption();
                String chosenPath;

                if (corruption >= 8) {
                    // High corruption -> Embrace
                    chosenPath = "EMBRACE";
                    character.setOathbreakerPath(chosenPath);
                    logger.info("Oathbreaker {} chose path: {} at level {} - Narrative: The Broken Oath Calls - fully embraced",
                            character.getName(), chosenPath, level);
                } else {
                    // Low corruption -> Purge
                    chosenPath = "PURGE";
                    character.setOathbreakerPath(chosenPath);
                    logger.info("Oathbreaker {} chose path: {} at level {} - Narrative: Seeking Redemption - purge path chosen",
                            character.getName(), chosenPath, level);
                }

                // Note: Path choice is permanent and affects future gameplay
            }
        }
    }

    /**
     * Checks for secret boss spawn milestones and triggers secret boss spawns.
     * Secret bosses spawn based on level milestones, boss kill milestones, story flags, and first-time achievements.
     *
     * @param character the character to check
     * @param guildId   the guild ID where the character is active
     */
    private void checkSecretBossMilestones(RPGCharacter character, String guildId) {
        if (bossService == null) {
            return; // Dependencies not set yet
        }

        // Level milestones: Every 10 levels (10, 20, 30, 40, 50)
        int level = character.getLevel();
        if (level >= 10 && level % 10 == 0) {
            String milestoneKey = "level_" + level;
            if (!character.getSecretBossMilestones().contains(milestoneKey)) {
                int bossLevel = Math.max(1, level / 2);
                Boss boss = bossService.checkAndSpawnSecretBoss(guildId, character.getDiscordId(), milestoneKey, bossLevel);
                if (boss != null) {
                    logger.info("Secret boss spawned for {} at level milestone {}", character.getName(), level);
                }
            }
        }

        // Boss kill milestones: Every 10 total boss kills (10, 20, 30, etc.)
        int totalBossKills = character.getBossesKilled() + character.getSuperBossesKilled();
        if (totalBossKills >= 10 && totalBossKills % 10 == 0) {
            String milestoneKey = "boss_kills_" + totalBossKills;
            if (!character.getSecretBossMilestones().contains(milestoneKey)) {
                int bossLevel = (totalBossKills / 10) + 1;
                Boss boss = bossService.checkAndSpawnSecretBoss(guildId, character.getDiscordId(), milestoneKey, bossLevel);
                if (boss != null) {
                    logger.info("Secret boss spawned for {} at boss kill milestone {}", character.getName(), totalBossKills);
                }
            }
        }

        // Story flag milestones
        if (character.getStoryFlags().contains("Frostborne's chosen")) {
            String milestoneKey = "story_flag_frostborne";
            if (!character.getSecretBossMilestones().contains(milestoneKey)) {
                int bossLevel = Math.max(1, level / 2);
                Boss boss = bossService.checkAndSpawnSecretBoss(guildId, character.getDiscordId(), milestoneKey, bossLevel);
                if (boss != null) {
                    logger.info("Secret boss spawned for {} for story flag: Frostborne's chosen", character.getName());
                }
            }
        }

        if (character.getStoryFlags().contains("Echo of the Shattering")) {
            String milestoneKey = "story_flag_shattering";
            if (!character.getSecretBossMilestones().contains(milestoneKey)) {
                int bossLevel = Math.max(1, level / 2);
                Boss boss = bossService.checkAndSpawnSecretBoss(guildId, character.getDiscordId(), milestoneKey, bossLevel);
                if (boss != null) {
                    logger.info("Secret boss spawned for {} for story flag: Echo of the Shattering", character.getName());
                }
            }
        }

        if (character.getStoryFlags().contains("Elemental master")) {
            String milestoneKey = "story_flag_elemental";
            if (!character.getSecretBossMilestones().contains(milestoneKey)) {
                int bossLevel = Math.max(1, level / 2);
                Boss boss = bossService.checkAndSpawnSecretBoss(guildId, character.getDiscordId(), milestoneKey, bossLevel);
                if (boss != null) {
                    logger.info("Secret boss spawned for {} for story flag: Elemental master", character.getName());
                }
            }
        }

        // First-time achievements
        if (character.getBossesKilled() == 1 && character.getSuperBossesKilled() == 0) {
            String milestoneKey = "first_boss_kill";
            if (!character.getSecretBossMilestones().contains(milestoneKey)) {
                int bossLevel = level;
                Boss boss = bossService.checkAndSpawnSecretBoss(guildId, character.getDiscordId(), milestoneKey, bossLevel);
                if (boss != null) {
                    logger.info("Secret boss spawned for {} for first boss kill", character.getName());
                }
            }
        }

        if (character.getSuperBossesKilled() == 1) {
            String milestoneKey = "first_super_boss_kill";
            if (!character.getSecretBossMilestones().contains(milestoneKey)) {
                int bossLevel = level;
                Boss boss = bossService.checkAndSpawnSecretBoss(guildId, character.getDiscordId(), milestoneKey, bossLevel);
                if (boss != null) {
                    logger.info("Secret boss spawned for {} for first super boss kill", character.getName());
                }
            }
        }

        if (character.getTotalResurrections() == 1 && character.getTotalDeaths() >= 1) {
            String milestoneKey = "first_death_resurrection";
            if (!character.getSecretBossMilestones().contains(milestoneKey)) {
                int bossLevel = level;
                Boss boss = bossService.checkAndSpawnSecretBoss(guildId, character.getDiscordId(), milestoneKey, bossLevel);
                if (boss != null) {
                    logger.info("Secret boss spawned for {} for first death and resurrection", character.getName());
                }
            }
        }
    }
}

