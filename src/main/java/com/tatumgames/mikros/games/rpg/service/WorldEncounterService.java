package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.curse.WorldCurse;
import com.tatumgames.mikros.games.rpg.exploration.DeityType;
import com.tatumgames.mikros.games.rpg.exploration.RelicType;
import com.tatumgames.mikros.games.rpg.exploration.WorldEncounterType;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * Service for handling irrevocable world encounters during exploration.
 * These are ultra-rare encounters (≤1% chance) that present permanent choices.
 */
public class WorldEncounterService {
    private static final Logger logger = LoggerFactory.getLogger(WorldEncounterService.class);
    private static final Random random = new Random();
    
    // Encounter chance: ≤1% (0.01)
    private static final double IRREVOCABLE_ENCOUNTER_CHANCE = 0.01;
    
    // Minimum level requirement
    private static final int MIN_LEVEL = 5;
    
    // Narrative variations for deity blessings
    private static final String[] VAELGOR_DISCOVERY = {
        "You find a colossal statue of a stone wolf, cracked with frost. Ancient runes glow faintly. You feel it watching you.",
        "A massive stone monument rises from the ice. The wolf's form is unmistakable, its eyes seeming to track your movements.",
        "Deep in the frozen ruins, you discover a statue of Vaelgor. The stone pulses with ancient power, calling to you.",
        "The Stone Wolf's shrine stands before you, untouched by time. Runes carved into the base shimmer with inner light.",
        "You stumble upon a sacred grove where a stone wolf statue guards the entrance. The air itself seems to whisper ancient secrets."
    };
    
    private static final String[] ILYRA_DISCOVERY = {
        "A statue of a graceful figure wreathed in ice stands before you. The wind seems to sing around it.",
        "You discover Ilyra's shrine, where frost patterns dance across the stone. The air is alive with winter's song.",
        "An ancient monument to the Frostwind rises from the snow. Ice crystals form intricate patterns around its base.",
        "The statue of Ilyra seems to move with the wind. Her form is elegant, her presence calming yet powerful.",
        "You find a hidden alcove where Ilyra's likeness stands. The temperature drops, and you hear whispers on the breeze."
    };
    
    private static final String[] NERETH_DISCOVERY = {
        "A statue with hollow eyes gazes at you. The mind seems to reach out, probing your thoughts.",
        "You discover Nereth's monument, a figure with an empty visage that seems to see beyond the physical world.",
        "The Hollow Mind's shrine stands in eerie silence. The statue's vacant eyes seem to pierce your very soul.",
        "A stone figure with no face watches you. You feel your mind being examined, weighed, and judged.",
        "Nereth's altar appears before you. The statue's featureless head seems to turn, following you with unseen eyes."
    };
    
    // Narrative variations for relic discoveries
    private static final String[] BLOOD_BLADE_DISCOVERY = {
        "A blade forged in blood and fire lies embedded in an ancient altar. It pulses with dark power.",
        "You discover a weapon of legend, its edge still sharp after centuries. Blood-red runes cover its surface.",
        "An altar holds a blade that seems to drink the light. The weapon calls to you, promising strength at a cost.",
        "The Blood-Forged Blade rests in a chamber of sacrifice. Ancient bloodstains mark the ground around it.",
        "A weapon of power awaits in a forgotten tomb. The blade glows with inner fire, hungry for battle."
    };
    
    private static final String[] FROZEN_CROWN_DISCOVERY = {
        "A crown of pure ice sits upon a pedestal of frost. It radiates cold power, beautiful and deadly.",
        "You find the Frozen Crown, its crystalline form perfect and unmelting. The air around it shimmers with cold.",
        "An ancient crown of ice rests in a chamber of eternal winter. Its beauty is matched only by its power.",
        "The Frozen Crown awaits in a shrine of ice. Frost patterns spiral around it, creating an aura of majesty.",
        "A crown forged from the heart of a glacier sits before you. Its presence makes the very air freeze."
    };
    
    private static final String[] SOUL_ANCHOR_DISCOVERY = {
        "An anchor of dark metal lies in a chamber of shadows. It seems to anchor not ships, but souls.",
        "You discover the Soul Anchor, a relic that binds the spirit to the mortal realm. Its power is palpable.",
        "A mystical anchor rests in an ancient tomb. It pulses with necromantic energy, promising life beyond death.",
        "The Soul Anchor waits in a chamber of the dead. Its chains seem to reach into the void itself.",
        "An anchor forged from soul-steel lies before you. It promises to tether your spirit to this world."
    };
    
    // Narrative variations for Oath of Null
    private static final String[] OATH_OF_NULL_DISCOVERY = {
        "You find a stone circle, ancient and untouched. A voice whispers: 'Some believe Nilfheim survives because mortals refuse divine chains.'",
        "An ancient circle of standing stones surrounds you. The air itself seems to reject the touch of gods.",
        "You discover a place where the gods' power wanes. A voice speaks of freedom from divine influence.",
        "A sacred grove untouched by divine power calls to you. Here, mortals choose their own path.",
        "The Oathstone stands before you, a monument to mortal independence. The gods' influence cannot reach this place."
    };
    
    // Narrative variations for Disguised God Test
    private static final String[] DISGUISED_GOD_APPEARANCE = {
        "A weary traveler asks for help. Their eyes linger too long… as if measuring you.",
        "You encounter a stranger on the road. Something about them feels… more than human.",
        "A figure approaches, asking for aid. Their gaze seems to see through you, weighing your worth.",
        "A traveler in need crosses your path. Their presence feels ancient, powerful, yet hidden.",
        "Someone asks for your help. Their eyes flash with something beyond mortal understanding."
    };

    /**
     * Rolls for an irrevocable world encounter.
     * Only triggers if character is Level 5+ and hasn't exceeded the limit (3 per encounter type).
     *
     * @param character the character exploring
     * @return encounter type or null if no encounter
     */
    public WorldEncounterType rollForIrrevocableEncounter(RPGCharacter character) {
        // Level check
        if (character.getLevel() < MIN_LEVEL) {
            return null;
        }
        
        // Get all encounter types and filter by availability
        WorldEncounterType[] allTypes = WorldEncounterType.values();
        java.util.List<WorldEncounterType> availableTypes = new java.util.ArrayList<>();
        
        for (WorldEncounterType type : allTypes) {
            // Check if this encounter type can still be triggered (max 3 times)
            if (character.canTriggerWorldEncounter(type.name())) {
                // For first-time encounters, also check if they've made any irrevocable choices
                // (This maintains the "once per character" logic for the first encounter)
                int count = character.getWorldEncounterCount(type.name());
                if (count == 0) {
                    // First time - check if they've made any irrevocable choices
                    if (character.getDeityBlessing() != null || 
                        character.getRelicChoice() != null || 
                        character.getPhilosophicalPath() != null) {
                        continue; // Skip if they've already made irrevocable choices
                    }
                }
                availableTypes.add(type);
            }
        }
        
        // If no available types, return null
        if (availableTypes.isEmpty()) {
            return null;
        }
        
        // Roll for encounter (≤1% chance)
        if (random.nextDouble() >= IRREVOCABLE_ENCOUNTER_CHANCE) {
            return null;
        }
        
        // Randomly select from available encounter types
        return availableTypes.get(random.nextInt(availableTypes.size()));
    }

    /**
     * Handles a world encounter and returns the outcome.
     *
     * @param encounterType the type of encounter
     * @param character the character
     * @param config the RPG config
     * @param activeCurses list of active world curses
     * @param songReduction Song of Nilfheim curse reduction (0.98-0.99)
     * @return the action outcome
     */
    public RPGActionOutcome handleEncounter(WorldEncounterType encounterType, RPGCharacter character,
                                           RPGConfig config, List<WorldCurse> activeCurses, double songReduction) {
        // Increment count for this encounter type
        character.incrementWorldEncounterCount(encounterType.name());
        
        return switch (encounterType) {
            case STONEBOUND_DIVINITY -> handleStoneboundDivinity(character, config, activeCurses, songReduction);
            case DISGUISED_GOD_TEST -> handleDisguisedGodTest(character, config, activeCurses, songReduction);
            case OATH_OF_NULL -> handleOathOfNull(character, config, activeCurses, songReduction);
            case BLOOD_RELIC -> handleBloodRelic(character, config, activeCurses, songReduction);
        };
    }

    /**
     * Handles Stonebound Divinity encounter - deity blessing choice.
     */
    private RPGActionOutcome handleStoneboundDivinity(RPGCharacter character, RPGConfig config,
                                                      List<WorldCurse> activeCurses, double songReduction) {
        // Oathbreaker: Special handling - deity may refuse or offer choice
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
            // 30% chance deity refuses to bless
            if (random.nextDouble() < 0.30) {
                DeityType[] deities = DeityType.values();
                DeityType refusingDeity = deities[random.nextInt(deities.length)];
                
                String refusalNarrative = getOathbreakerDeityRefusalNarrative(refusingDeity);
                
                int baseXp = 30 + (character.getLevel() * 5);
                int xpGained = (int) ((baseXp * 0.5) * config.getXpMultiplier()); // Reduced XP for refusal
                
                return RPGActionOutcome.builder()
                        .narrative(refusalNarrative)
                        .xpGained(xpGained)
                        .leveledUp(false)
                        .success(false)
                        .build();
            }
            
            // 70% chance: Offer choice (for now, randomly choose accept/refuse)
            // In full implementation, this would be a player choice
            boolean acceptBlessing = random.nextDouble() < 0.7; // 70% accept, 30% refuse
            
            if (!acceptBlessing) {
                // Oathbreaker refuses blessing
                character.setHasRefusedDeity(true);
                character.addCorruption(2);
                
                String refusalNarrative = getOathbreakerRefusalChoiceNarrative();
                
                int baseXp = 30 + (character.getLevel() * 5);
                int xpGained = (int) ((baseXp * 0.5) * config.getXpMultiplier()); // Reduced XP for refusal
                
                return RPGActionOutcome.builder()
                        .narrative(refusalNarrative)
                        .xpGained(xpGained)
                        .leveledUp(false)
                        .success(false)
                        .build();
            }
        }
        
        // Select a deity (prefer class-aligned deities with 2x chance)
        DeityType[] deities = DeityType.values();
        DeityType selectedDeity = deities[random.nextInt(deities.length)];
        
        // If character's class is preferred, 2x chance for that deity
        if (random.nextDouble() < 0.5) {
            for (DeityType deity : deities) {
                if (deity.isPreferredClass(character.getCharacterClass())) {
                    selectedDeity = deity;
                    break;
                }
            }
        }
        
        // For now, we'll present the encounter and apply the blessing
        // In a full implementation, this would present choices to the player
        // For now, we'll randomly select a deity blessing
        
        return handleDeityBlessing(selectedDeity, character, config, activeCurses, songReduction);
    }

    /**
     * Handles a deity blessing choice.
     */
    public RPGActionOutcome handleDeityBlessing(DeityType deity, RPGCharacter character, RPGConfig config,
                                                List<WorldCurse> activeCurses, double songReduction) {
        // Apply deity blessing
        character.setDeityBlessing(deity.name());
        character.addWorldFlag(deity.getWorldFlag());
        
        // Apply stat modifiers
        String blessingStatKey = deity.getBlessingStat() + "_EFFECTIVENESS";
        String curseStatKey = deity.getCurseStat() + "_EFFECTIVENESS";
        character.addStatModifier(blessingStatKey, 1.0 + deity.getBlessingModifier());
        character.addStatModifier(curseStatKey, 1.0 + deity.getCurseModifier());
        
        // Calculate XP (bonus for encounter)
        int baseXp = 30 + (character.getLevel() * 5);
        int xpGained = (int) ((baseXp * 1.5) * config.getXpMultiplier());
        
        // Select discovery narrative based on deity
        String discoveryNarrative = switch (deity) {
            case VAELGOR_STONE_WOLF -> VAELGOR_DISCOVERY[random.nextInt(VAELGOR_DISCOVERY.length)];
            case ILYRA_FROSTWIND -> ILYRA_DISCOVERY[random.nextInt(ILYRA_DISCOVERY.length)];
            case NERETH_HOLLOW_MIND -> NERETH_DISCOVERY[random.nextInt(NERETH_DISCOVERY.length)];
        };
        
        // Oathbreaker: Special narrative for deity acceptance
        String oathbreakerFlavor = "";
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
            oathbreakerFlavor = getOathbreakerDeityAcceptanceNarrative(deity);
        }
        
        String narrative = String.format(
            "🏛️ **%s**\n\n%s\n\n" +
            "You feel the deity's power flow through you. Ancient runes glow brighter, marking you as chosen.\n\n" +
            "**Blessing:** +%.0f%% %s effectiveness\n" +
            "**Curse:** %.0f%% %s effectiveness\n\n" +
            "The mark is permanent. Your path is set.%s",
            deity.getDisplayName(),
            discoveryNarrative,
            deity.getBlessingModifier() * 100,
            deity.getBlessingStat(),
            deity.getCurseModifier() * 100,
            deity.getCurseStat(),
            oathbreakerFlavor
        );
        
        logger.info("Character {} received deity blessing: {}", character.getName(), deity.getDisplayName());
        
        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(false)
                .success(true)
                .build();
    }

    /**
     * Handles Disguised God Test encounter.
     */
    private RPGActionOutcome handleDisguisedGodTest(RPGCharacter character, RPGConfig config,
                                                    List<WorldCurse> activeCurses, double songReduction) {
        // Determine which stat test based on character's highest stat
        int str = character.getStats().getStrength();
        int agi = character.getStats().getAgility();
        int intel = character.getStats().getIntelligence();
        
        String testType;
        String statName;
        boolean success;
        
        if (str >= agi && str >= intel) {
            testType = "protection";
            statName = "STR";
            success = str >= 18;
        } else if (agi >= intel) {
            testType = "scout";
            statName = "AGI";
            success = agi >= 17;
        } else {
            testType = "strategy";
            statName = "INT";
            success = intel >= 20;
        }
        
        int baseXp = 30 + (character.getLevel() * 5);
        int xpGained = (int) ((baseXp * 1.3) * config.getXpMultiplier());
        
        // Select random appearance narrative
        String appearanceNarrative = DISGUISED_GOD_APPEARANCE[random.nextInt(DISGUISED_GOD_APPEARANCE.length)];
        
        String narrative;
        String worldFlag;
        double modifier = 0.0;
        
        if (success) {
            narrative = String.format(
                "👤 **A Weary Traveler**\n\n" +
                "%s\n\n" +
                "You offer %s. The traveler's eyes flash with something ancient.\n\n" +
                "**Later, you dream of frost cracking under iron claws. You have been seen.**\n\n" +
                "**Blessing:** +10%% %s effectiveness\n\n" +
                "The test is passed. You are marked.",
                appearanceNarrative,
                testType,
                statName
            );
            worldFlag = statName + "_TEST_PASSED";
            modifier = 0.10;
            character.addStatModifier(statName + "_EFFECTIVENESS", 1.0 + modifier);
        } else {
            narrative = String.format(
                "👤 **A Weary Traveler**\n\n" +
                "%s\n\n" +
                "You offer %s, but your %s is not enough. The traveler nods respectfully and moves on.\n\n" +
                "**Later, you dream of frost cracking under iron claws. You have been seen.**\n\n" +
                "You are marked, but the blessing is lesser.",
                appearanceNarrative,
                testType,
                statName
            );
            worldFlag = statName + "_TEST_ATTEMPTED";
        }
        
        character.addWorldFlag(worldFlag);
        character.setPhilosophicalPath("GODMARKED");
        
        logger.info("Character {} encountered disguised god test: {} (success: {})", 
                   character.getName(), statName, success);
        
        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(false)
                .success(success)
                .build();
    }

    /**
     * Handles Oath of Null encounter - anti-god path.
     */
    private RPGActionOutcome handleOathOfNull(RPGCharacter character, RPGConfig config,
                                              List<WorldCurse> activeCurses, double songReduction) {
        character.setPhilosophicalPath("UNBOUND");
        character.addWorldFlag("OATH_OF_NULL");
        
        // +5% curse resistance
        character.addStatModifier("CURSE_RESISTANCE", 1.05);
        
        int baseXp = 30 + (character.getLevel() * 5);
        int xpGained = (int) ((baseXp * 1.2) * config.getXpMultiplier());
        
        // Select random discovery narrative
        String discoveryNarrative = OATH_OF_NULL_DISCOVERY[random.nextInt(OATH_OF_NULL_DISCOVERY.length)];
        
        String narrative = 
            "⚖️ **The Oath of Null**\n\n" +
            discoveryNarrative + "\n\n" +
            "You refuse the path of the gods. You choose freedom.\n\n" +
            "**Blessing:** +5%% resistance to world curses\n" +
            "**Title:** Unbound\n\n" +
            "You are immune to future god-marked debuffs. Certain gods will not assist you, but others secretly respect your choice.\n\n" +
            "The oath is permanent. You walk your own path.";
        
        character.setTitle("Unbound");
        
        logger.info("Character {} took the Oath of Null", character.getName());
        
        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(false)
                .success(true)
                .build();
    }

    /**
     * Handles Blood Relic encounter.
     */
    private RPGActionOutcome handleBloodRelic(RPGCharacter character, RPGConfig config,
                                              List<WorldCurse> activeCurses, double songReduction) {
        // Select a relic
        RelicType[] relics = RelicType.values();
        RelicType selectedRelic = relics[random.nextInt(relics.length)];
        
        return handleRelicChoice(selectedRelic, character, config, activeCurses, songReduction);
    }

    /**
     * Handles a relic choice.
     */
    public RPGActionOutcome handleRelicChoice(RelicType relic, RPGCharacter character, RPGConfig config,
                                             List<WorldCurse> activeCurses, double songReduction) {
        character.setRelicChoice(relic.name());
        character.addWorldFlag(relic.getWorldFlag());
        
        // Apply modifiers
        if (relic.getPositiveModifier() > 0) {
            character.addStatModifier(relic.getPositiveStat(), 1.0 + relic.getPositiveModifier());
        }
        if (relic.getNegativeModifier() < 0) {
            character.addStatModifier(relic.getNegativeStat(), 1.0 + relic.getNegativeModifier());
        }
        
        int baseXp = 30 + (character.getLevel() * 5);
        int xpGained = (int) ((baseXp * 1.4) * config.getXpMultiplier());
        
        // Select discovery narrative based on relic
        String discoveryNarrative = switch (relic) {
            case BLOOD_FORGED_BLADE -> BLOOD_BLADE_DISCOVERY[random.nextInt(BLOOD_BLADE_DISCOVERY.length)];
            case FROZEN_CROWN -> FROZEN_CROWN_DISCOVERY[random.nextInt(FROZEN_CROWN_DISCOVERY.length)];
            case SOUL_ANCHOR -> SOUL_ANCHOR_DISCOVERY[random.nextInt(SOUL_ANCHOR_DISCOVERY.length)];
        };
        
        String narrative = String.format(
            "⚔️ **%s**\n\n%s\n\n" +
            "You take the relic. Power flows through you, but at a cost.\n\n" +
            "**Power:** +%.0f%% %s\n" +
            "**Cost:** %.0f%% %s\n\n" +
            "The relic is bound to you. The change is permanent.",
            relic.getDisplayName(),
            discoveryNarrative,
            relic.getPositiveModifier() * 100,
            relic.getPositiveStat(),
            Math.abs(relic.getNegativeModifier()) * 100,
            relic.getNegativeStat()
        );
        
        logger.info("Character {} took relic: {}", character.getName(), relic.getDisplayName());
        
        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(false)
                .success(true)
                .build();
    }

    /**
     * Gets Oathbreaker-specific narrative when a deity refuses to bless them.
     */
    private String getOathbreakerDeityRefusalNarrative(DeityType deity) {
        return switch (deity) {
            case VAELGOR_STONE_WOLF -> String.format(
                "🏛️ **%s**\n\n" +
                "The Stone Wolf's statue turns its head. Ancient runes dim. 'The broken oath marks you,' a voice rumbles. " +
                "The deity recognizes your broken oath and turns away. No blessing is offered to one who has broken their word.\n\n" +
                "You feel the weight of the refusal, but also a strange sense of freedom.",
                deity.getDisplayName()
            );
            case ILYRA_FROSTWIND -> String.format(
                "🏛️ **%s**\n\n" +
                "The Frostwind's statue shivers. Ice forms around its base. 'Your broken oath echoes in the wind,' whispers Ilyra. " +
                "The deity recognizes your broken oath and turns away, though there's a hint of... admiration? in the refusal.\n\n" +
                "The broken oath makes you unworthy, yet the refusal feels less harsh than expected.",
                deity.getDisplayName()
            );
            case NERETH_HOLLOW_MIND -> String.format(
                "🏛️ **%s**\n\n" +
                "The Hollow Mind's featureless face seems to gaze deeper. 'The broken oath... interesting,' Nereth's voice echoes. " +
                "The deity recognizes your broken oath. There's a moment of consideration, then refusal. " +
                "Yet you sense curiosity rather than condemnation.\n\n" +
                "The broken oath makes you an anomaly, and anomalies are... intriguing.",
                deity.getDisplayName()
            );
        };
    }

    /**
     * Gets Oathbreaker-specific narrative when they refuse a deity blessing.
     */
    private String getOathbreakerRefusalChoiceNarrative() {
        String[] refusalNarratives = {
            "You stand before the deity's statue, feeling the weight of the broken oath. You refuse the blessing. " +
            "The broken oath grows stronger, and you gain corruption. Some paths are meant to be walked alone.",
            
            "The deity offers power, but you remember the oath you broke. You refuse. " +
            "The broken oath resonates with your choice, increasing your corruption. " +
            "You walk a path of your own making, unbound by divine will.",
            
            "You look into the deity's eyes and see judgment. You refuse the blessing. " +
            "The broken oath answers your defiance, granting corruption. " +
            "You are contested—neither blessed nor cursed, but something else entirely.",
            
            "The deity's power calls to you, but the broken oath whispers louder. You refuse. " +
            "Corruption flows through you as the broken oath strengthens. " +
            "You choose to remain unbound, even if it means walking alone.",
            
            "You feel the deity's blessing would bind you further. You refuse. " +
            "The broken oath recognizes your choice and grants corruption. " +
            "You are unfinished business in Nilfheim, and you will remain so."
        };
        return "🏛️ **Divine Encounter**\n\n" + refusalNarratives[random.nextInt(refusalNarratives.length)];
    }

    /**
     * Gets Oathbreaker-specific narrative when they accept a deity blessing.
     */
    private String getOathbreakerDeityAcceptanceNarrative(DeityType deity) {
        return switch (deity) {
            case VAELGOR_STONE_WOLF -> 
                "\n\n⚔️💀 *The Stone Wolf tests you harshly, sensing the broken oath. The blessing comes, but it feels... conditional. " +
                "The deity watches closely, as if waiting for you to break this oath too.*";
            case ILYRA_FROSTWIND -> 
                "\n\n⚔️💀 *The Frostwind admires your defiance, offering a different path. The blessing flows, but with understanding. " +
                "Ilyra recognizes the broken oath and offers power anyway, perhaps seeing something others do not.*";
            case NERETH_HOLLOW_MIND -> 
                "\n\n⚔️💀 *The Hollow Mind is intrigued by your broken oath. The blessing comes with curiosity. " +
                "Nereth sees the broken oath not as failure, but as... data. Interesting data.*";
        };
    }
}
