package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.curse.WorldCurse;
import com.tatumgames.mikros.games.rpg.exploration.ClassExclusiveEventType;
import com.tatumgames.mikros.games.rpg.exploration.ExplorationEventType;
import com.tatumgames.mikros.games.rpg.exploration.WanderingFigureType;
import com.tatumgames.mikros.games.rpg.model.CatalystType;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.EssenceType;
import com.tatumgames.mikros.games.rpg.model.InfusionType;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.AuraService;
import com.tatumgames.mikros.games.rpg.service.LoreRecognitionService;
import com.tatumgames.mikros.games.rpg.service.NilfheimEventService;
import com.tatumgames.mikros.games.rpg.service.WorldCurseService;
import com.tatumgames.mikros.games.rpg.events.NilfheimEventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Explore action - player explores the world and encounters random events.
 * Rewards XP and generates narrative encounters.
 */
public class ExploreAction implements CharacterAction {
    private static final Random random = new Random();
    private final WorldCurseService worldCurseService;
    private final AuraService auraService;
    private final NilfheimEventService nilfheimEventService;
    private final LoreRecognitionService loreRecognitionService;
    private final com.tatumgames.mikros.games.rpg.service.WorldEncounterService worldEncounterService;
    private final com.tatumgames.mikros.games.rpg.service.StatInteractionService statInteractionService;

    /**
     * Creates a new ExploreAction.
     *
     * @param worldCurseService the world curse service for applying curse effects
     * @param auraService the aura service for Song of Nilfheim curse reduction
     * @param nilfheimEventService the Nilfheim event service for server-wide events
     * @param loreRecognitionService the lore recognition service for milestone checks
     * @param worldEncounterService the world encounter service for irrevocable encounters
     * @param statInteractionService the stat interaction service for stat-gated interactions
     */
    public ExploreAction(WorldCurseService worldCurseService, AuraService auraService, NilfheimEventService nilfheimEventService, LoreRecognitionService loreRecognitionService,
                         com.tatumgames.mikros.games.rpg.service.WorldEncounterService worldEncounterService,
                         com.tatumgames.mikros.games.rpg.service.StatInteractionService statInteractionService) {
        this.worldCurseService = worldCurseService;
        this.auraService = auraService;
        this.nilfheimEventService = nilfheimEventService;
        this.loreRecognitionService = loreRecognitionService;
        this.worldEncounterService = worldEncounterService;
        this.statInteractionService = statInteractionService;
    }

    private static final String[] NARRATIVES = {
            // Original 15 narratives
            "You ventured into an ancient forest and discovered a hidden shrine. The spirits blessed your journey.",
            "While exploring a misty valley, you helped a lost traveler find their way home.",
            "You stumbled upon a mysterious cave filled with glowing crystals. Their energy invigorates you.",
            "In a bustling market square, you learned valuable techniques from a traveling merchant.",
            "You climbed a towering mountain and witnessed a breathtaking sunrise. Clarity washes over you.",
            "Deep in a dungeon, you found ancient texts that expanded your knowledge.",
            "You crossed paths with a friendly bard who shared tales of legendary heroes.",
            "While resting by a enchanted spring, you felt your potential grow stronger.",
            "You discovered ruins of an old civilization and learned from their forgotten wisdom.",
            "A chance encounter with a wise hermit taught you valuable life lessons.",
            "You found a peaceful grove where time seems to stand still. Meditation here brings growth.",
            "In the depths of a library, you uncovered secrets that broaden your understanding.",
            "You helped villagers defend against bandits, earning their gratitude and respect.",
            "While foraging in the wilderness, you found rare herbs that boost your vitality.",
            "You solved an ancient puzzle in a long-forgotten temple, unlocking hidden knowledge.",
            // Nilfheim-specific narratives (20 additional)
            "You discover a frozen shrine emitting faint blue light.",
            "Tracks in the snow lead you to an abandoned campsite.",
            "A wandering merchant greets you, then vanishes in a flurry of snow.",
            "You find a rune-inscribed stone warm to the touch.",
            "A mysterious whisper echoes through a frost cavern.",
            "You witness two spirits dancing in the moonlight before fading away.",
            "A sudden blizzard almost blinds you, but you push onward.",
            "You spot a distant figure watching you… then it disappears.",
            "A strange glowing feather lands in your palm.",
            "You wander into a hollow tree filled with shimmering frost-bugs.",
            "You hear soft music carried by the wind — but no musician in sight.",
            "A frozen river cracks beneath you, revealing runes below.",
            "You find a broken sword half-buried in the ice.",
            "A ghostly wolf follows you for miles, then stops and howls.",
            "You discover a frostflower blooming defiantly in the snow.",
            "A cavern wall glitters with crystals containing trapped wisps.",
            "You find a torn page describing an ancient Nilfheim prophecy.",
            "A glowing moth guides you safely through a twisting ravine.",
            "You uncover footprints that abruptly stop mid-stride.",
            "You stumble onto a frozen battlefield where echoes of war linger.",
            "You trace ancient runes carved into an iceberg shaped like a giant's skull.",
            "A faint trail of warmth leads you to a buried emberstone.",
            "You glimpse a mythical frost stag before it bounds into the blizzard.",
            "A forgotten watchtower creaks as the wind pushes against its frozen wood.",
            "You find a shattered mirror that reflects a version of you that doesn't move.",
            "A hidden hot spring steams gently in the cold air.",
            "You hear distant drums echoing from beneath the ground.",
            "A shard of pale crystal pulses faintly as you approach.",
            "You witness a meteor streak across the sky, embedding itself into a glacier.",
            "Frozen statues line a canyon, each face twisted in terror.",
            "A phantom caravan trudges by, fading as it passes.",
            "You encounter a whispering fissure that seems to respond to your thoughts.",
            "An eerie silence descends — even the wind stops.",
            "You find a glowing rune marking the next lunar eclipse.",
            "A frozen clocktower ticks once as you walk past, then stops again.",
            "You hear a lullaby sung by an unseen voice.",
            "Strange footprints circle around you… and disappear.",
            "You catch a glimpse of a shadow that mirrors your movements perfectly.",
            "You find a torn cloak clasp made of dragonbone.",
            "The sky ripples with aurora lights that form strange, ancient symbols.",
            // New Nilfheim lore narratives (30 additional)
            "You discover ancient ruins from before the Shattering, their runes still glowing faintly.",
            "A Stormwarden passes by, leaving behind a trail of crackling Gale energy.",
            "You find a hidden entrance to the Grand Library of Nil City, filled with forbidden knowledge.",
            "The twin moons align, revealing a path through the Spirit Veil that wasn't there before.",
            "You stumble upon the Moonspire Obelisk, its ancient runic inscriptions pulsing with power.",
            "A fragment of the Shattering of the First Winter drifts past, frozen in time.",
            "You discover a training ground used by Frostborne warriors, their techniques still visible in the ice.",
            "The Eight Elements converge here - you feel Frost, Gale, Ember, Void, and Astral energies mixing.",
            "You find a hidden chamber beneath Starfall Ridge, filled with star fragments and cosmic energy.",
            "A portal to the Arcane Veil flickers before you, offering glimpses of other realities.",
            "You discover the remains of a Frostgate outpost, its banners still fluttering in the eternal wind.",
            "Ancient Frostborne runes carved into a glacier begin to glow as you approach.",
            "You witness a Stormwarden training with the Gale element, their movements a blur of wind and lightning.",
            "A library of the first civilizations after the Shattering reveals secrets of the Eight Elements.",
            "You find a meditation circle where the Astral element is strongest, showing you possible futures.",
            "The Mortal and Arcane Veils thin here, allowing you to see spirits from both realms.",
            "You discover a hidden hot spring that never freezes, said to be blessed by the Frost element.",
            "Ancient battlefields from the Shattering still echo with the sounds of war.",
            "You find a cache of weapons forged by the Frostborne, still sharp after centuries.",
            "A Stormwarden's journal reveals techniques for mastering the Gale element.",
            "You discover a shrine to the Eight Elements, each one represented by glowing crystals.",
            "The Grand Library's forbidden section opens to you, revealing knowledge of the Void element.",
            "You find a map leading to Frostgate, marked with routes through the Ice Wastes.",
            "Ancient prophecies carved into the Moonspire Obelisk begin to make sense as you read them.",
            "You discover a hidden passage to Nil City, its spires visible through the mist.",
            "A fragment of the first winter's ice contains memories of the Shattering itself.",
            "You find a training manual from the Frostborne warriors, detailing their combat techniques.",
            "The Spirit Veil parts briefly, allowing you to communicate with a long-lost spirit.",
            "You discover a cache of Astral element crystals, their power showing you glimpses of fate.",
            "Ancient runes from Starfall Ridge tell the story of the first civilizations after the Shattering.",
            "You find a hidden chamber beneath the Grand Library, filled with knowledge of the Eight Elements."
    };

    @Override
    public String getActionName() {
        return "explore";
    }

    @Override
    public String getActionEmoji() {
        return "🧭";
    }

    @Override
    public String getDescription() {
        return "Explore the world and encounter random events";
    }

    // Ambush enemy names (lore creatures for Tier 2 events)
    private static final String[] AMBUSH_ENEMIES = {
            "Frost Wraith", "Ice Troll", "Shadow Stalker", "Frozen Revenant",
            "Frost Wolf Pack", "Ice Stalker", "Wailing Wisp", "Coldshade Phantom"
    };

    @Override
    public RPGActionOutcome execute(RPGCharacter character, RPGConfig config) {
        // Get active curses for this guild
        String guildId = config.getGuildId();
        List<WorldCurse> activeCurses = worldCurseService.getActiveCurses(guildId);
        
        // Check for Song of Nilfheim aura (reduces curse penalties by 1-2%)
        double songReduction = auraService.getSongOfNilfheimCurseReduction(guildId);

        // Check for class-exclusive exploration event (very rare, 1-2% base chance, separate from negative events)
        ClassExclusiveEventType classEvent = rollForClassExclusiveEvent(character);
        if (classEvent != null) {
            // Class-exclusive event triggered - handle it and return (doesn't interfere with normal exploration)
            return handleClassExclusiveEvent(classEvent, character, config, activeCurses, songReduction);
        }

        // Check for wandering figure encounter (ultra-rare, 0.5% chance, after class-exclusive, before negative events)
        WanderingFigureType wanderingFigure = rollForWanderingFigure(character);
        if (wanderingFigure != null) {
            // Wandering figure encountered - handle it and return
            return handleWanderingFigure(wanderingFigure, character, config, activeCurses, songReduction);
        }

        // Check for irrevocable world encounter (Level 5+, once per character, ≤1% chance)
        if (character.getLevel() >= 5 && character.getDeityBlessing() == null && 
            character.getRelicChoice() == null && character.getPhilosophicalPath() == null) {
            com.tatumgames.mikros.games.rpg.exploration.WorldEncounterType encounter = worldEncounterService.rollForIrrevocableEncounter(character);
            if (encounter != null) {
                return worldEncounterService.handleEncounter(encounter, character, config, activeCurses, songReduction);
            }
        }

        // Check for stat-gated interaction (Level 10+, 10-15% chance, max 3 per type)
        if (character.getLevel() >= 10) {
            com.tatumgames.mikros.games.rpg.exploration.StatInteractionType interaction = statInteractionService.rollForStatInteraction(character);
            if (interaction != null) {
                return statInteractionService.handleStatInteraction(interaction, character, config, activeCurses, songReduction);
            }
        }

        // Check for negative exploration event (rare, 5% base chance, reduced by AGI/LUCK)
        ExplorationEventType negativeEvent = rollForNegativeEvent(character);
        String narrative;
        int xpGained = 0;
        int damageTaken = 0;
        boolean leveledUp = false;
        boolean eventTriggered = false;
        
        if (negativeEvent != null) {
            // Negative event triggered - show warning and apply event
            eventTriggered = true;
            narrative = "As you push deeper into the frozen ruins, something feels… off.\n\n";
            RPGActionOutcome eventOutcome = handleNegativeEvent(negativeEvent, character, config, activeCurses, songReduction);
            narrative += eventOutcome.narrative();
            xpGained = eventOutcome.xpGained();
            damageTaken = eventOutcome.damageTaken();
            leveledUp = eventOutcome.leveledUp();
        } else {
            // Normal exploration - select random narrative
            narrative = NARRATIVES[random.nextInt(NARRATIVES.length)];
            
            // Calculate XP gain (scales with level and config multiplier)
            int baseXp = 30 + (character.getLevel() * 5);
            int variance = random.nextInt(20) - 10; // +/- 10
            xpGained = (int) ((baseXp + variance) * config.getXpMultiplier());
        }
        
        // Apply Curse of Clouded Mind (-5% XP, but ensure minimum 90%) - only if not already handled by event
        if (!eventTriggered && activeCurses.contains(WorldCurse.MINOR_CURSE_OF_CLOUDED_MIND)) {
            int baseXp = 30 + (character.getLevel() * 5);
            int variance = random.nextInt(20) - 10;
            double cloudedPenalty = 0.95 * songReduction; // Apply Song reduction
            xpGained = (int) (xpGained * cloudedPenalty);
            // Ensure minimum 90% of original
            int minXpWithCurse = (int) ((baseXp + variance) * config.getXpMultiplier() * 0.90);
            xpGained = Math.max(minXpWithCurse, xpGained);
        }

        // Apply Nilfheim event effects
        NilfheimEventType activeEvent = nilfheimEventService.getActiveEvent(guildId);
        if (activeEvent != null) {
            if (activeEvent.getEffectType() == NilfheimEventType.EventEffectType.ALL_XP_BOOST) {
                // Starfall Ridge's Light: +15% XP on all actions
                xpGained = (int) (xpGained * (1.0 + activeEvent.getEffectValue()));
            }
        }

        // Apply infusion effects
        InfusionType activeInfusion = character.getInventory().getActiveInfusion();
        if (activeInfusion != null) {
            if (activeInfusion == InfusionType.FROST_CLARITY) {
                // Frost Clarity: +10% XP on next action
                xpGained = (int) (xpGained * 1.10);
            } else if (activeInfusion == InfusionType.ELEMENTAL_CONVERGENCE) {
                // Elemental Convergence: +15% XP on next action
                xpGained = (int) (xpGained * 1.15);
            }
        }

        // Apply dark relic XP bonus if active (before adding XP)
        if (character.getDarkRelicActionsRemaining() > 0 && xpGained > 0) {
            int bonusXp = (int) (xpGained * character.getDarkRelicXpBonus());
            xpGained += bonusXp;
            character.decrementDarkRelicActions();
        }
        
        // Add XP and check for level up (if not already done by event)
        if (!eventTriggered) {
            leveledUp = character.addXp(xpGained, loreRecognitionService);
        }

        // AGI-based item drop chance bonus
        // Base: 12.5% chance
        // AGI bonus: +0.5% per AGI point (capped at +15%)
        double baseDropChance = 0.125;
        int agility = character.getStats().getAgility();
        double agilityBonus = Math.min(0.15, agility * 0.005);
        double dropChance = baseDropChance + agilityBonus; // 12.5% to 27.5%
        
        // Apply Curse of Ill Fortune (-5% item drop chance)
        // Song of Nilfheim reduces the penalty
        if (activeCurses.contains(WorldCurse.MINOR_CURSE_OF_ILL_FORTUNE)) {
            double illFortunePenalty = 0.05 * songReduction; // Apply Song reduction
            dropChance = Math.max(0.0, dropChance - illFortunePenalty);
        }

        // Apply Nilfheim event effects for drops
        if (activeEvent != null) {
            if (activeEvent.getEffectType() == NilfheimEventType.EventEffectType.EXPLORE_DROP_BOOST) {
                // Twin Moons Align: +10% essence drop chance
                dropChance += activeEvent.getEffectValue();
            }
        }

        RPGActionOutcome.Builder outcomeBuilder = RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .damageTaken(damageTaken)
                .hpRestored(0)
                .success(!eventTriggered || damageTaken == 0 || character.getStats().getCurrentHp() > 0);

        // Check for infusion-guaranteed drop
        boolean guaranteedDrop = false;
        if (activeInfusion != null) {
            if (activeInfusion == InfusionType.GALE_FORTUNE || activeInfusion == InfusionType.ELEMENTAL_CONVERGENCE) {
                guaranteedDrop = true;
            }
        }

        // Roll for item drops with AGI bonus (or guaranteed by infusion)
        if (guaranteedDrop || random.nextDouble() < dropChance) {
            int essenceCount = random.nextInt(2) + 1; // Base: 1-2 essences
            // AGI bonus: +1 essence if AGI >= 20
            if (agility >= 20) {
                essenceCount += 1; // Now 2-3 essences
            }
            
            // Class-specific essence bonuses
            EssenceType essence = getRandomEssenceWithClassBonus(character.getCharacterClass());
            outcomeBuilder.addItemDrop(essence, essenceCount);
            
            // Add to character inventory
            character.getInventory().addEssence(essence, essenceCount);
            
            // Update narrative to mention benefits
            StringBuilder benefitNote = new StringBuilder();
            if (agility >= 15) {
                benefitNote.append(" Your agility helped you find better loot!");
            }
            if (essence == EssenceType.EMBER_SHARD && character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.WARRIOR) {
                benefitNote.append(" Your warrior instincts led you to strength-aligned materials!");
            } else if (essence == EssenceType.MIND_CRYSTAL && (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.MAGE ||
                    character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.PRIEST ||
                    character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.NECROMANCER)) {
                benefitNote.append(" Your magical affinity drew you to intelligence-aligned materials!");
            } else if (essence == EssenceType.VITAL_ASH && character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.KNIGHT) {
                benefitNote.append(" Your defensive nature led you to vitality-aligned materials!");
            } else if (essence == EssenceType.FATE_CLOVER && character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.PRIEST) {
                benefitNote.append(" Your supportive nature drew you to luck-aligned materials!");
            }
            
            if (benefitNote.length() > 0) {
                outcomeBuilder.narrative(narrative + benefitNote.toString());
            }
        }

        // Record the action
        character.recordAction();
        
        // Oathbreaker: Gain corruption from acting during world curses
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER && !activeCurses.isEmpty()) {
            character.addCorruption(1);
            // Update narrative if not already updated
            String currentNarrative = outcomeBuilder.build().narrative();
            if (!currentNarrative.contains("⚔️💀 **Corruption:**")) {
                outcomeBuilder.narrative(currentNarrative + "\n\n⚔️💀 **Corruption:** The world's curses resonate with your broken oath, increasing your corruption.");
            }
        }
        
        // Track action type and increment explore count for achievements
        character.recordActionType("explore");
        character.incrementExploreCount();

        return outcomeBuilder.build();
    }

    /**
     * Gets a random essence type with class-specific bonuses.
     *
     * @param characterClass the character's class
     * @return random essence type (biased by class)
     */
    private EssenceType getRandomEssenceWithClassBonus(CharacterClass characterClass) {
        EssenceType[] allEssences = EssenceType.values();
        
        // Class-specific essence bonuses
        switch (characterClass) {
            case WARRIOR:
                // +5% chance for STR-aligned (Ember Shard)
                if (random.nextDouble() < 0.05) {
                    return EssenceType.EMBER_SHARD;
                }
                break;
            case MAGE:
            case PRIEST:
            case NECROMANCER:
                // +5% chance for INT-aligned (Mind Crystal)
                if (random.nextDouble() < 0.05) {
                    return EssenceType.MIND_CRYSTAL;
                }
                break;
            case KNIGHT:
                // +5% chance for HP-aligned (Vital Ash)
                if (random.nextDouble() < 0.05) {
                    return EssenceType.VITAL_ASH;
                }
                break;
            case ROGUE:
                // Already has AGI benefits, no additional bonus needed
                break;
            case OATHBREAKER:
                // +5% chance for INT-aligned (Mind Crystal)
                if (random.nextDouble() < 0.05) {
                    return EssenceType.MIND_CRYSTAL;
                }
                break;
        }
        
        // Priest and Oathbreaker also get +3% chance for LUCK-aligned (Fate Clover)
        if (characterClass == CharacterClass.PRIEST || characterClass == CharacterClass.OATHBREAKER) {
            if (random.nextDouble() < 0.03) {
                return EssenceType.FATE_CLOVER;
            }
        }
        
        // Default: random essence
        return allEssences[random.nextInt(allEssences.length)];
    }

    /**
     * Rolls for a negative exploration event.
     * Base chance: 5%, reduced by AGI (-0.2% per AGI) and LUCK (-0.1% per LUCK), minimum 1%.
     *
     * @param character the character exploring
     * @return the event type if triggered, null otherwise
     */
    private ExplorationEventType rollForNegativeEvent(RPGCharacter character) {
        double baseChance = 0.05; // 5%
        int agility = character.getStats().getAgility();
        int luck = character.getStats().getLuck();
        
        // AGI reduction: -0.2% per AGI (max -4% at 20 AGI)
        double agiReduction = Math.min(0.04, agility * 0.002);
        
        // LUCK reduction: -0.1% per LUCK (max -2% at 20 LUCK)
        double luckReduction = Math.min(0.02, luck * 0.001);
        
        // Final chance: base - reductions, minimum 1%
        double finalChance = Math.max(0.01, baseChance - agiReduction - luckReduction);
        
        if (random.nextDouble() < finalChance) {
            // Event triggered - select tier (Tier 1: 75%, Tier 2: 20%, Tier 3: 5%)
            double tierRoll = random.nextDouble();
            if (tierRoll < 0.75) {
                // Tier 1: Minor Setbacks
                ExplorationEventType[] tier1Events = {
                    ExplorationEventType.SLIPPED_ON_ICE,
                    ExplorationEventType.PICKPOCKETED_BY_THIEVES,
                    ExplorationEventType.MOCKED_BY_ANGRY_MOB,
                    ExplorationEventType.LOST_IN_THE_FOG
                };
                return tier1Events[random.nextInt(tier1Events.length)];
            } else if (tierRoll < 0.95) {
                // Tier 2: Dangerous Encounters
                ExplorationEventType[] tier2Events = {
                    ExplorationEventType.AMBUSHED_BY_CREATURE,
                    ExplorationEventType.FROSTBITE
                };
                return tier2Events[random.nextInt(tier2Events.length)];
            } else {
                // Tier 3: Legendary Events
                ExplorationEventType[] tier3Events = {
                    ExplorationEventType.TOUCHED_BY_DARK_RELIC,
                    ExplorationEventType.SEEN_BY_ANCIENT
                };
                return tier3Events[random.nextInt(tier3Events.length)];
            }
        }
        
        return null; // No event
    }

    /**
     * Handles a negative exploration event and returns the outcome.
     *
     * @param eventType the type of negative event
     * @param character the character affected
     * @param config the RPG config
     * @param activeCurses list of active world curses
     * @param songReduction Song of Nilfheim curse reduction multiplier
     * @return the action outcome
     */
    private RPGActionOutcome handleNegativeEvent(ExplorationEventType eventType, RPGCharacter character,
                                                 RPGConfig config, List<WorldCurse> activeCurses, double songReduction) {
        String narrative = "";
        int xpGained = 0;
        int damageTaken = 0;
        boolean leveledUp = false;
        
        switch (eventType) {
            case SLIPPED_ON_ICE -> {
                // Lose 5-10% HP, cannot kill
                int maxHp = character.getStats().getMaxHp();
                int hpLoss = (int) (maxHp * (0.05 + random.nextDouble() * 0.05)); // 5-10%
                int currentHp = character.getStats().getCurrentHp();
                damageTaken = Math.min(hpLoss, currentHp - 1); // Ensure at least 1 HP remains
                character.getStats().takeDamage(damageTaken);
                narrative = "🧊 **Slipped on Ice:** You lose your footing on a patch of black ice, taking " + damageTaken + " damage. At least you're still alive!";
            }
            
            case PICKPOCKETED_BY_THIEVES -> {
                // Lose 1 random essence (cannot lose catalysts, cannot go below 0)
                EssenceType[] allEssences = EssenceType.values();
                List<EssenceType> availableEssences = new ArrayList<>();
                for (EssenceType essence : allEssences) {
                    if (character.getInventory().getEssenceCount(essence) > 0) {
                        availableEssences.add(essence);
                    }
                }
                
                if (!availableEssences.isEmpty()) {
                    EssenceType stolenEssence = availableEssences.get(random.nextInt(availableEssences.size()));
                    character.getInventory().removeEssence(stolenEssence, 1); // Remove 1
                    narrative = "🧤 **Pickpocketed by Thieves:** A group of shadowy figures slips past you in the fog. You notice " + 
                               stolenEssence.getDisplayName() + " is missing from your inventory!";
                } else {
                    narrative = "🧤 **Pickpocketed by Thieves:** A group of shadowy figures slips past you, but your pockets are already empty. Lucky you!";
                }
            }
            
            case MOCKED_BY_ANGRY_MOB -> {
                // Only triggers if curses are active
                if (!activeCurses.isEmpty()) {
                    EssenceType[] allEssences = EssenceType.values();
                    List<EssenceType> availableEssences = new ArrayList<>();
                    for (EssenceType essence : allEssences) {
                        if (character.getInventory().getEssenceCount(essence) > 0) {
                            availableEssences.add(essence);
                        }
                    }
                    
                    if (!availableEssences.isEmpty()) {
                        EssenceType stolenEssence = availableEssences.get(random.nextInt(availableEssences.size()));
                        character.getInventory().removeEssence(stolenEssence, 1); // Remove 1
                        narrative = "😠 **Mocked by Angry Mob:** The citizens of Nilfheim blame you for the world's curses! They throw stones and steal " + 
                                   stolenEssence.getDisplayName() + " from your pack.";
                    } else {
                        narrative = "😠 **Mocked by Angry Mob:** The citizens of Nilfheim blame you for the world's curses! They throw stones, but you have nothing left to lose.";
                    }
                } else {
                    // No curses active - treat as normal exploration
                    narrative = "You explore peacefully, but something feels off...";
                }
            }
            
            case LOST_IN_THE_FOG -> {
                // XP reduced by 25% for this action only
                int baseXp = 30 + (character.getLevel() * 5);
                int variance = random.nextInt(20) - 10;
                int normalXp = (int) ((baseXp + variance) * config.getXpMultiplier());
                xpGained = (int) (normalXp * 0.75); // 25% reduction
                
                // Apply dark relic XP bonus if active
                if (character.getDarkRelicActionsRemaining() > 0) {
                    int bonusXp = (int) (xpGained * character.getDarkRelicXpBonus());
                    xpGained += bonusXp;
                    character.decrementDarkRelicActions();
                }
                
                leveledUp = character.addXp(xpGained, loreRecognitionService);
                narrative = "🌫️ **Lost in the Fog:** A thick mist obscures your path. You eventually find your way, but the disorientation cost you some experience.";
            }
            
            case AMBUSHED_BY_CREATURE -> {
                // Treated like a battle defeat - use BattleAction logic
                String enemyName = AMBUSH_ENEMIES[random.nextInt(AMBUSH_ENEMIES.length)];
                int enemyLevel = Math.max(1, character.getLevel() - 2 + random.nextInt(5)); // Level-appropriate enemy
                
                // Calculate enemy power (similar to BattleAction)
                int enemyPower = 20 + (enemyLevel * 10);
                int playerPower = calculatePlayerPower(character, enemyLevel);
                
                // Roll battle (player likely to lose in ambush)
                int playerRoll = random.nextInt(playerPower) + (character.getStats().getLuck() * 2);
                int enemyRoll = random.nextInt(enemyPower) + (enemyLevel * 3); // Enemy advantage
                
                boolean victory = playerRoll > enemyRoll;
                
                // Check for dark relic before applying effects
                boolean hasDarkRelic = character.getDarkRelicActionsRemaining() > 0;
                
                if (victory) {
                    // Victory: minimal damage, some XP
                    int baseXp = (int) ((50 + (enemyLevel * 10)) * config.getXpMultiplier() * 0.5); // Half XP for ambush
                    xpGained = baseXp;
                    
                    // Apply dark relic XP bonus if active
                    if (hasDarkRelic) {
                        int bonusXp = (int) (xpGained * character.getDarkRelicXpBonus());
                        xpGained += bonusXp;
                        character.decrementDarkRelicActions();
                    }
                    
                    damageTaken = random.nextInt(5) + 1; // 1-5 damage
                    // Apply dark relic damage penalty (check before decrementing)
                    if (hasDarkRelic) {
                        damageTaken = (int) (damageTaken * (1.0 + character.getDarkRelicDamagePenalty()));
                    }
                    character.getStats().takeDamage(damageTaken);
                    leveledUp = character.addXp(xpGained, loreRecognitionService);
                    narrative = "🐺 **Ambushed by " + enemyName + ":** You were caught off guard, but managed to fight back! You take " + 
                               damageTaken + " damage but gain " + xpGained + " XP.";
                } else {
                    // Defeat: significant damage, minimal XP
                    int baseXp = (int) ((50 + (enemyLevel * 10)) * config.getXpMultiplier() * 0.25); // Quarter XP for defeat
                    xpGained = baseXp;
                    
                    // Apply dark relic XP bonus if active
                    if (hasDarkRelic) {
                        int bonusXp = (int) (xpGained * character.getDarkRelicXpBonus());
                        xpGained += bonusXp;
                        character.decrementDarkRelicActions();
                    }
                    
                    damageTaken = (int) (enemyPower * (0.15 + random.nextDouble() * 0.10)); // 15-25% of enemy power
                    // Apply dark relic damage penalty (check before decrementing)
                    if (hasDarkRelic) {
                        damageTaken = (int) (damageTaken * (1.0 + character.getDarkRelicDamagePenalty()));
                    }
                    boolean survived = character.getStats().takeDamage(damageTaken);
                    leveledUp = character.addXp(xpGained, loreRecognitionService);
                    
                    if (!survived) {
                        character.setIsDead(true);
                        narrative = "🐺 **Ambushed by " + enemyName + ":** The ambush was too much! You fall, defeated. " + 
                                   "You gain " + xpGained + " XP from the experience, but death claims you.";
                    } else {
                        narrative = "🐺 **Ambushed by " + enemyName + ":** You were caught off guard and take " + damageTaken + 
                                   " damage! You barely escape with " + xpGained + " XP.";
                    }
                }
            }
            
            case FROSTBITE -> {
                // Max HP reduced by 5%, removed by rest
                character.setHasFrostbite(true);
                narrative = "🩸 **Frostbite:** The biting cold seeps into your bones. Your maximum HP is reduced by 5% until you rest and warm up.";
            }
            
            case TOUCHED_BY_DARK_RELIC -> {
                // +5% XP for next 3 actions, +10% damage taken until rested
                character.setDarkRelicActionsRemaining(3);
                character.setDarkRelicXpBonus(0.05); // +5% XP
                character.setDarkRelicDamagePenalty(0.10); // +10% damage taken
                narrative = "🕯️ **Touched by a Dark Relic:** An ancient artifact pulses with dark energy as you touch it. " + 
                           "You feel a surge of power (+5% XP for 3 actions) but also vulnerability (+10% damage taken).";
            }
            
            case SEEN_BY_ANCIENT -> {
                // No mechanical penalty, adds story flag
                List<String> storyFlags = character.getStoryFlags();
                String flag = "Watched by something beyond the veil";
                if (!storyFlags.contains(flag) && storyFlags.size() < 2) {
                    character.addStoryFlag(flag);
                }
                narrative = "👁️ **Seen by Something Ancient:** You feel an otherworldly presence watching you from the shadows. " + 
                           "Something beyond mortal understanding has noticed you. This moment will be remembered.";
            }
        }
        
        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .damageTaken(damageTaken)
                .hpRestored(0)
                .success(damageTaken == 0 || character.getStats().getCurrentHp() > 0)
                .build();
    }

    /**
     * Calculates player power for ambush battles (simplified version of BattleAction logic).
     *
     * @param character the character
     * @param enemyLevel the enemy level
     * @return player power value
     */
    private int calculatePlayerPower(RPGCharacter character, int enemyLevel) {
        int level = character.getLevel();
        int str = character.getStats().getStrength();
        int agi = character.getStats().getAgility();
        int intel = character.getStats().getIntelligence();
        int luck = character.getStats().getLuck();
        
        // Simplified power calculation (similar to BattleAction)
        int basePower = 20 + (level * 5);
        int statPower = (str * 2) + agi + intel + (luck * 2);
        
        return basePower + statPower;
    }

    /**
     * Rolls for a class-exclusive exploration event.
     * Base chance: 1.5% (very rare, separate from negative events).
     *
     * @param character the character exploring
     * @return the event type if triggered, null otherwise
     */
    private ClassExclusiveEventType rollForClassExclusiveEvent(RPGCharacter character) {
        double baseChance = 0.015; // 1.5% base chance (between 1-2% as per TASKS_01.md)
        
        if (random.nextDouble() < baseChance) {
            // Event triggered - select random event from all 10 events
            ClassExclusiveEventType[] allEvents = ClassExclusiveEventType.values();
            return allEvents[random.nextInt(allEvents.length)];
        }
        
        return null; // No event
    }

    /**
     * Rolls for a wandering figure encounter.
     * Base chance: 0.5% (ultra-rare, after class-exclusive, before negative events).
     *
     * @param character the character exploring
     * @return the wandering figure type if encountered, null otherwise
     */
    private WanderingFigureType rollForWanderingFigure(RPGCharacter character) {
        double baseChance = 0.005; // 0.5% base chance
        
        if (random.nextDouble() < baseChance) {
            // Encounter triggered - filter by level requirements and class preferences
            List<WanderingFigureType> availableFigures = new ArrayList<>();
            
            for (WanderingFigureType figure : WanderingFigureType.values()) {
                // Check level requirement
                if (character.getLevel() >= figure.getMinLevel()) {
                    availableFigures.add(figure);
                }
            }
            
            if (availableFigures.isEmpty()) {
                return null; // No figures available for this level
            }
            
            // Class-specific flavor: preferred class has 2x chance
            List<WanderingFigureType> weightedFigures = new ArrayList<>();
            for (WanderingFigureType figure : availableFigures) {
                weightedFigures.add(figure);
                // If character's class matches preferred class, add again (2x weight)
                if (figure.getPreferredClass() != null && 
                    character.getCharacterClass() == figure.getPreferredClass()) {
                    weightedFigures.add(figure);
                }
            }
            
            // Select random figure from weighted list
            return weightedFigures.get(random.nextInt(weightedFigures.size()));
        }
        
        return null; // No encounter
    }

    /**
     * Handles a wandering figure encounter.
     *
     * @param figureType the type of wandering figure
     * @param character the character affected
     * @param config the RPG config
     * @param activeCurses list of active world curses
     * @param songReduction Song of Nilfheim curse reduction multiplier
     * @return the action outcome
     */
    private RPGActionOutcome handleWanderingFigure(WanderingFigureType figureType, RPGCharacter character,
                                         RPGConfig config, List<WorldCurse> activeCurses, double songReduction) {
        String narrative = String.format("**%s**\n\n%s\n\n", figureType.getDisplayName(), figureType.getDescription());
        int xpGained = 0;
        boolean leveledUp = false;
        RPGActionOutcome.Builder outcomeBuilder = RPGActionOutcome.builder();
        
        // Calculate base XP (normal explore XP)
        int baseXp = 30 + (character.getLevel() * 5);
        int variance = random.nextInt(20) - 10;
        int normalXp = (int) ((baseXp + variance) * config.getXpMultiplier());
        
        // Randomly select one of three outcomes
        int outcomeRoll = random.nextInt(3);
        
        switch (figureType) {
            case FROSTBOUND_SAGE -> {
                switch (outcomeRoll) {
                    case 0 -> {
                        // Restore 1 charge (if at 0 charges)
                        if (character.getActionCharges() == 0) {
                            int maxCharges = character.getMaxActionCharges();
                            character.setActionCharges(Math.min(maxCharges, 1));
                            narrative += figureType.getOutcome1Narrative() + " You feel your resolve restored.";
                        } else {
                            // Already have charges, give XP bonus instead
                            xpGained = (int) (normalXp * 1.10); // +10% XP
                            narrative += figureType.getOutcome2Narrative();
                        }
                    }
                    case 1 -> {
                        // +10% XP on next action
                        xpGained = (int) (normalXp * 1.10); // +10% XP
                        narrative += figureType.getOutcome2Narrative();
                    }
                    case 2 -> {
                        // Story flag only
                        String flag = "Encountered the Frostbound Sage";
                        if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                            character.addStoryFlag(flag);
                            narrative += figureType.getOutcome3Narrative() + " This moment will be remembered.";
                        } else {
                            narrative += figureType.getOutcome3Narrative();
                        }
                    }
                }
            }
            case ANCIENT_WANDERER -> {
                switch (outcomeRoll) {
                    case 0 -> {
                        // +1 random essence
                        EssenceType essence = getRandomEssenceWithClassBonus(character.getCharacterClass());
                        outcomeBuilder.addItemDrop(essence, 1);
                        character.getInventory().addEssence(essence, 1);
                        narrative += figureType.getOutcome1Narrative();
                    }
                    case 1 -> {
                        // Temporary +5% damage on next battle (narrative only, no mechanical benefit for now)
                        xpGained = normalXp;
                        narrative += figureType.getOutcome2Narrative();
                    }
                    case 2 -> {
                        // Story flag only
                        String flag = "Aided by the Ancient Wanderer";
                        if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                            character.addStoryFlag(flag);
                            narrative += figureType.getOutcome3Narrative() + " This moment will be remembered.";
                        } else {
                            narrative += figureType.getOutcome3Narrative();
                        }
                    }
                }
            }
            case MYSTERIOUS_MERCHANT -> {
                switch (outcomeRoll) {
                    case 0 -> {
                        // Trade: Exchange 2 essences of one type → 1 essence of another type (if possible)
                        EssenceType[] allEssences = EssenceType.values();
                        List<EssenceType> availableEssences = new ArrayList<>();
                        for (EssenceType essence : allEssences) {
                            if (character.getInventory().getEssenceCount(essence) >= 2) {
                                availableEssences.add(essence);
                            }
                        }
                        
                        if (!availableEssences.isEmpty()) {
                            EssenceType sourceEssence = availableEssences.get(random.nextInt(availableEssences.size()));
                            character.getInventory().removeEssence(sourceEssence, 2);
                            
                            // Get random different essence
                            EssenceType targetEssence = allEssences[random.nextInt(allEssences.length)];
                            while (targetEssence == sourceEssence) {
                                targetEssence = allEssences[random.nextInt(allEssences.length)];
                            }
                            
                            outcomeBuilder.addItemDrop(targetEssence, 1);
                            character.getInventory().addEssence(targetEssence, 1);
                            narrative += String.format("The Merchant trades 2x %s for 1x %s.", 
                                    sourceEssence.getDisplayName(), targetEssence.getDisplayName());
                        } else {
                            // No essences to trade, give catalyst instead
                            CatalystType[] catalysts = CatalystType.values();
                            CatalystType catalyst = catalysts[random.nextInt(catalysts.length)];
                            outcomeBuilder.addCatalystDrop(catalyst, 1);
                            character.getInventory().addCatalyst(catalyst, 1);
                            narrative += figureType.getOutcome1Narrative();
                        }
                    }
                    case 1 -> {
                        // +1 random catalyst
                        CatalystType[] catalysts = CatalystType.values();
                        CatalystType catalyst = catalysts[random.nextInt(catalysts.length)];
                        outcomeBuilder.addCatalystDrop(catalyst, 1);
                        character.getInventory().addCatalyst(catalyst, 1);
                        narrative += figureType.getOutcome1Narrative();
                    }
                    case 2 -> {
                        // Story flag only
                        String flag = "Traded with the Mysterious Merchant";
                        if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                            character.addStoryFlag(flag);
                            narrative += figureType.getOutcome2Narrative() + " This moment will be remembered.";
                        } else {
                            narrative += figureType.getOutcome2Narrative();
                        }
                    }
                }
            }
            case STORMWARDEN_APPRENTICE -> {
                switch (outcomeRoll) {
                    case 0 -> {
                        // +1 Gale Fragment
                        outcomeBuilder.addItemDrop(EssenceType.GALE_FRAGMENT, 1);
                        character.getInventory().addEssence(EssenceType.GALE_FRAGMENT, 1);
                        narrative += figureType.getOutcome1Narrative();
                    }
                    case 1 -> {
                        // Temporary +10% AGI on next action (narrative only, no mechanical benefit for now)
                        xpGained = normalXp;
                        narrative += figureType.getOutcome2Narrative();
                    }
                    case 2 -> {
                        // Story flag only
                        String flag = "Learned from a Stormwarden";
                        if (!character.getStoryFlags().contains(flag) && character.getStoryFlags().size() < 2) {
                            character.addStoryFlag(flag);
                            narrative += figureType.getOutcome3Narrative() + " This moment will be remembered.";
                        } else {
                            narrative += figureType.getOutcome3Narrative();
                        }
                    }
                }
            }
        }
        
        // Default XP if not set
        if (xpGained == 0) {
            xpGained = normalXp;
        }
        
        // Apply dark relic XP bonus if active
        if (character.getDarkRelicActionsRemaining() > 0 && xpGained > 0) {
            int bonusXp = (int) (xpGained * character.getDarkRelicXpBonus());
            xpGained += bonusXp;
            character.decrementDarkRelicActions();
        }
        
        // Add XP and check for level up
        leveledUp = character.addXp(xpGained, loreRecognitionService);
        
        // Check and consume active infusion if present (infusions apply to all explore actions)
        InfusionType activeInfusion = character.getInventory().getActiveInfusion();
        if (activeInfusion != null) {
            character.getInventory().consumeActiveInfusion();
        }
        
        // Record the action
        character.recordAction();
        character.recordActionType("explore");
        character.incrementExploreCount();
        
        return outcomeBuilder
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .success(true)
                .build();
    }

    /**
     * Handles a class-exclusive exploration event.
     * Matching classes get full rewards, non-matching classes get consolation.
     *
     * @param eventType the type of class-exclusive event
     * @param character the character affected
     * @param config the RPG config
     * @param activeCurses list of active world curses
     * @param songReduction Song of Nilfheim curse reduction multiplier
     * @return the action outcome
     */
    private RPGActionOutcome handleClassExclusiveEvent(ClassExclusiveEventType eventType, RPGCharacter character,
                                                       RPGConfig config, List<WorldCurse> activeCurses, double songReduction) {
        CharacterClass requiredClass = eventType.getRequiredClass();
        boolean isMatchingClass = character.getCharacterClass() == requiredClass;
        
        String narrative = eventType.getDiscoveryNarrative() + "\n\n";
        int xpGained = 0;
        boolean leveledUp = false;
        RPGActionOutcome.Builder outcomeBuilder = RPGActionOutcome.builder();
        
        if (isMatchingClass) {
            // Matching class - full rewards
            RPGActionOutcome matchingOutcome = handleMatchingClassEvent(eventType, character, config);
            narrative += matchingOutcome.narrative();
            xpGained = matchingOutcome.xpGained();
            leveledUp = matchingOutcome.leveledUp();
            outcomeBuilder = RPGActionOutcome.builder()
                    .narrative(narrative)
                    .xpGained(xpGained)
                    .leveledUp(leveledUp)
                    .success(true);
            
            // Copy item drops
            for (var itemDrop : matchingOutcome.itemDrops()) {
                outcomeBuilder.addItemDrop(itemDrop.essence(), itemDrop.count());
            }
            for (var catalystDrop : matchingOutcome.catalystDrops()) {
                outcomeBuilder.addCatalystDrop(catalystDrop.catalyst(), catalystDrop.count());
            }
        } else {
            // Non-matching class - consolation rewards
            RPGActionOutcome consolationOutcome = handleNonMatchingClassEvent(eventType, character, config);
            narrative += consolationOutcome.narrative();
            xpGained = consolationOutcome.xpGained();
            leveledUp = consolationOutcome.leveledUp();
            outcomeBuilder = RPGActionOutcome.builder()
                    .narrative(narrative)
                    .xpGained(xpGained)
                    .leveledUp(leveledUp)
                    .success(true);
        }
        
        // Check and consume active infusion if present (infusions apply to all explore actions)
        InfusionType activeInfusion = character.getInventory().getActiveInfusion();
        if (activeInfusion != null) {
            character.getInventory().consumeActiveInfusion();
        }

        // Record the action
        character.recordAction();
        character.recordActionType("explore");
        character.incrementExploreCount();

        return outcomeBuilder.build();
    }

    /**
     * Handles a class-exclusive event for a matching class (full rewards).
     */
    private RPGActionOutcome handleMatchingClassEvent(ClassExclusiveEventType eventType, RPGCharacter character,
                                                     RPGConfig config) {
        String narrative = "";
        int xpGained = 0;
        RPGActionOutcome.Builder outcomeBuilder = RPGActionOutcome.builder();
        
        // Calculate base XP (normal explore XP)
        int baseXp = 30 + (character.getLevel() * 5);
        int variance = random.nextInt(20) - 10;
        int normalXp = (int) ((baseXp + variance) * config.getXpMultiplier());
        
        switch (eventType) {
            case LOCKED_CHEST -> {
                // Rogue: Extra essences, catalyst chance, possible rare cosmetic
                narrative = "🔐 **Locked Chest:** Your nimble fingers work the lock with practiced ease. The chest opens!";
                xpGained = (int) (normalXp * 1.25); // 25% XP bonus
                
                // Extra essences (2-4 random)
                int essenceCount = random.nextInt(3) + 2;
                EssenceType essence = getRandomEssenceWithClassBonus(character.getCharacterClass());
                outcomeBuilder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                
                // 15% chance for catalyst
                if (random.nextDouble() < 0.15) {
                    CatalystType[] catalysts = CatalystType.values();
                    CatalystType catalyst = catalysts[random.nextInt(catalysts.length)];
                    outcomeBuilder.addCatalystDrop(catalyst, 1);
                    character.getInventory().addCatalyst(catalyst, 1);
                    narrative += " You also find a rare " + catalyst.getDisplayName() + "!";
                }
                
                narrative += " Your expertise as a Rogue paid off!";
            }
            
            case HIDDEN_PASSAGE -> {
                // Rogue: Bonus explore XP, +1 essence
                narrative = "🕳️ **Hidden Passage:** You slip through the narrow opening with ease, discovering a hidden route!";
                xpGained = (int) (normalXp * 1.30); // 30% XP bonus
                
                EssenceType essence = getRandomEssenceWithClassBonus(character.getCharacterClass());
                outcomeBuilder.addItemDrop(essence, 1);
                character.getInventory().addEssence(essence, 1);
                
                narrative += " Your agility led you to treasures others would miss!";
            }
            
            case CRUMBLING_OBSTACLE -> {
                // Warrior: Bonus STR essence, small XP burst
                narrative = "🪨 **Crumbling Obstacle:** You channel your strength and shatter the barrier!";
                xpGained = (int) (normalXp * 1.20); // 20% XP bonus
                
                // STR-aligned essence (Ember Shard) - 2-3 essences
                int essenceCount = random.nextInt(2) + 2;
                outcomeBuilder.addItemDrop(EssenceType.EMBER_SHARD, essenceCount);
                character.getInventory().addEssence(EssenceType.EMBER_SHARD, essenceCount);
                
                narrative += " Your warrior's might cleared the path!";
            }
            
            case ANCIENT_OATHSTONE -> {
                // Knight: Temporary defense bonus (next 3 battles: +5% damage reduction)
                narrative = "⚖️ **Ancient Oathstone:** You swear an oath of protection, and the stone responds with ancient power!";
                xpGained = normalXp;
                
                // Add story flag
                character.addStoryFlag("Swore oath at Ancient Oathstone");
                
                // Temporary defense bonus would require new tracking field - for now, just XP and story flag
                narrative += " You feel a protective aura surrounding you. The oath binds you to this place.";
            }
            
            case ARCANE_SIGIL -> {
                // Mage: INT essence, catalyst chance
                narrative = "📜 **Arcane Sigil:** You decipher the ancient runes, unlocking their secrets!";
                xpGained = (int) (normalXp * 1.25); // 25% XP bonus
                
                // INT-aligned essence (Mind Crystal) - 2-3 essences
                int essenceCount = random.nextInt(2) + 2;
                outcomeBuilder.addItemDrop(EssenceType.MIND_CRYSTAL, essenceCount);
                character.getInventory().addEssence(EssenceType.MIND_CRYSTAL, essenceCount);
                
                // 12% chance for catalyst
                if (random.nextDouble() < 0.12) {
                    CatalystType[] catalysts = CatalystType.values();
                    CatalystType catalyst = catalysts[random.nextInt(catalysts.length)];
                    outcomeBuilder.addCatalystDrop(catalyst, 1);
                    character.getInventory().addCatalyst(catalyst, 1);
                    narrative += " The sigil also reveals a hidden " + catalyst.getDisplayName() + "!";
                }
                
                narrative += " Your magical knowledge unlocked ancient wisdom!";
            }
            
            case MANA_RIFT -> {
                // Mage: Bonus XP, reduced damage taken next battle (temporary buff)
                narrative = "🌀 **Mana Rift:** You stabilize the chaotic energy, gaining control over the rift!";
                xpGained = (int) (normalXp * 1.30); // 30% XP bonus
                
                // Add story flag
                character.addStoryFlag("Stabilized a Mana Rift");
                
                // Temporary buff would require new tracking - for now, just XP and story flag
                narrative += " The stabilized energy will protect you in your next battle.";
            }
            
            case MASS_GRAVE -> {
                // Necromancer: Grave XP, story flag
                narrative = "🪦 **Mass Grave:** You commune with the spirits, learning from their memories!";
                xpGained = (int) (normalXp * 1.25); // 25% XP bonus
                
                // Add story flag
                character.addStoryFlag("Communed with mass grave");
                
                narrative += " The dead have shared their knowledge with you.";
            }
            
            case LINGERING_SPIRIT -> {
                // Necromancer: Temporary XP bonus (next 2 actions: +10% XP)
                narrative = "👁️ **Lingering Spirit:** You extract knowledge from the spectral form!";
                xpGained = (int) (normalXp * 1.20); // 20% XP bonus
                
                // Add story flag
                character.addStoryFlag("Extracted knowledge from spirit");
                
                // Temporary XP bonus would require new tracking - for now, just XP and story flag
                narrative += " The spirit's knowledge will enhance your next actions.";
            }
            
            case DESECRATED_SHRINE -> {
                // Priest: Blessing (small XP buff), title progress
                narrative = "🕯️ **Desecrated Shrine:** You cleanse the darkness, restoring the shrine's sanctity!";
                xpGained = (int) (normalXp * 1.25); // 25% XP bonus
                
                // Add story flag
                character.addStoryFlag("Cleansed a desecrated shrine");
                
                // LUCK-aligned essence chance (Fate Clover)
                if (random.nextDouble() < 0.20) {
                    outcomeBuilder.addItemDrop(EssenceType.FATE_CLOVER, 1);
                    character.getInventory().addEssence(EssenceType.FATE_CLOVER, 1);
                    narrative += " The restored shrine blesses you with a " + EssenceType.FATE_CLOVER.getDisplayName() + "!";
                }
                
                narrative += " Your faith has purified this place.";
            }
            
            case LOST_PILGRIM -> {
                // Priest: Bonus XP, LUCK essence chance
                narrative = "🕊️ **Lost Pilgrim:** You comfort the weary traveler, guiding them to safety!";
                xpGained = (int) (normalXp * 1.30); // 30% XP bonus
                
                // LUCK-aligned essence (Fate Clover) - 1-2 essences
                int essenceCount = random.nextInt(2) + 1;
                outcomeBuilder.addItemDrop(EssenceType.FATE_CLOVER, essenceCount);
                character.getInventory().addEssence(EssenceType.FATE_CLOVER, essenceCount);
                
                narrative += " Your compassion has been rewarded!";
            }
        }
        
        // Apply dark relic XP bonus if active
        if (character.getDarkRelicActionsRemaining() > 0 && xpGained > 0) {
            int bonusXp = (int) (xpGained * character.getDarkRelicXpBonus());
            xpGained += bonusXp;
            character.decrementDarkRelicActions();
        }
        
        // Add XP and check for level up
        boolean leveledUp = character.addXp(xpGained);
        
        return outcomeBuilder
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .success(true)
                .build();
    }

    /**
     * Handles a class-exclusive event for a non-matching class (consolation rewards).
     */
    private RPGActionOutcome handleNonMatchingClassEvent(ClassExclusiveEventType eventType, RPGCharacter character,
                                                        RPGConfig config) {
        String narrative = "";
        int xpGained = 0;
        
        // Consolation: 10-15% of normal explore XP
        int baseXp = 30 + (character.getLevel() * 5);
        int variance = random.nextInt(20) - 10;
        int normalXp = (int) ((baseXp + variance) * config.getXpMultiplier());
        xpGained = (int) (normalXp * (0.10 + random.nextDouble() * 0.05)); // 10-15% of normal XP
        
        CharacterClass requiredClass = eventType.getRequiredClass();
        String requiredClassName = requiredClass.getDisplayName();
        
        switch (eventType) {
            case LOCKED_CHEST -> {
                narrative = "🔐 **Locked Chest:** You examine the lock, but it's beyond your skill. If only someone more… dexterous were here.";
                character.addStoryFlag("Discovered locked chest but couldn't open it");
            }
            
            case HIDDEN_PASSAGE -> {
                narrative = "🕳️ **Hidden Passage:** The opening is too narrow for you to slip through. The passage collapses as you attempt to enter.";
            }
            
            case CRUMBLING_OBSTACLE -> {
                narrative = "🪨 **Crumbling Obstacle:** The barrier is too strong for you to break. You're forced to turn back.";
            }
            
            case ANCIENT_OATHSTONE -> {
                narrative = "⚖️ **Ancient Oathstone:** The stone doesn't respond to your touch. Only a " + requiredClassName + " could activate its power.";
            }
            
            case ARCANE_SIGIL -> {
                narrative = "📜 **Arcane Sigil:** The glyphs are incomprehensible to you. Their secrets remain locked to those with magical knowledge.";
            }
            
            case MANA_RIFT -> {
                narrative = "🌀 **Mana Rift:** The unstable energy is too dangerous for you to approach. A " + requiredClassName + " might be able to stabilize it.";
            }
            
            case MASS_GRAVE -> {
                narrative = "🪦 **Mass Grave:** You feel unease in this place. The spirits here don't respond to you. You leave quickly.";
            }
            
            case LINGERING_SPIRIT -> {
                narrative = "👁️ **Lingering Spirit:** The spectral form vanishes as you approach. It seems to only respond to those who understand death.";
            }
            
            case DESECRATED_SHRINE -> {
                narrative = "🕯️ **Desecrated Shrine:** The darkness here is too strong for you to cleanse. The shrine remains tainted.";
            }
            
            case LOST_PILGRIM -> {
                narrative = "🕊️ **Lost Pilgrim:** You try to help, but the traveler needs spiritual guidance beyond your ability. A " + requiredClassName + " might be able to comfort them.";
            }
        }
        
        // Add XP and check for level up
        boolean leveledUp = character.addXp(xpGained, loreRecognitionService);
        
        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .success(true)
                .build();
    }
}

