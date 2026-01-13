package com.tatumgames.mikros.games.rpg.service;

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

    /**
     * Checks all milestones for a character and awards story flags if conditions are met.
     * Only awards flags if story flag slot is available (max 2).
     *
     * @param character the character to check
     */
    public void checkMilestones(RPGCharacter character) {
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
                String narrative;

                if (corruption >= 8) {
                    // High corruption -> Embrace
                    chosenPath = "EMBRACE";
                    character.setOathbreakerPath(chosenPath);
                    narrative = "⚔️💀 **The Broken Oath Calls:** You have fully embraced the broken oath. " +
                            "Demons whisper your name, and corruption flows freely. Your corruption cap increases to 20. " +
                            "The path of defiance is yours.";
                } else {
                    // Low corruption -> Purge
                    chosenPath = "PURGE";
                    character.setOathbreakerPath(chosenPath);
                    narrative = "⚔️💀 **Seeking Redemption:** You seek to purge the broken oath, finding resilience. " +
                            "Your corruption cap is reduced to 10, but you gain +5% damage reduction and -10% curse penalties. " +
                            "The path of redemption is yours.";
                }

                logger.info("Oathbreaker {} chose path: {} at level {}", character.getName(), chosenPath, level);
                // Note: Narrative is logged but not returned - this happens during milestone check
                // The path choice is permanent and affects future gameplay
            }
        }
    }
}

