package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.curse.WorldCurse;
import com.tatumgames.mikros.games.rpg.exploration.StatInteractionType;
import com.tatumgames.mikros.games.rpg.model.CatalystType;
import com.tatumgames.mikros.games.rpg.model.EssenceType;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;

import java.util.List;
import java.util.Random;

/**
 * Service for handling stat-gated world interactions during exploration.
 * These encounters (10-15% chance) require stat checks and provide meaningful success/failure outcomes.
 */
public class StatInteractionService {
    private static final Random random = new Random();
    
    // Interaction chance: 10-15%
    private static final double MIN_INTERACTION_CHANCE = 0.10;
    private static final double MAX_INTERACTION_CHANCE = 0.15;
    
    // Minimum level requirement
    private static final int MIN_LEVEL = 10; // Changed from 5 to 10
    
    // Narrative variations for success outcomes
    private static final String[] FROSTBOUND_BOULDER_SUCCESS = {
        "🪨 **Frostbound Boulder**\n\nYou channel your strength and shatter the frozen boulder! The path clears before you.\n\n",
        "🪨 **Frostbound Boulder**\n\nWith a mighty heave, you break through the ice-encrusted barrier. The way forward opens.\n\n",
        "🪨 **Frostbound Boulder**\n\nYour muscles strain as you push against the ancient stone. With a crack, it gives way.\n\n"
    };
    
    private static final String[] FROZEN_GATE_SUCCESS = {
        "🚪 **Frozen Gate**\n\nYou force the ancient gate open with raw strength! A hidden passage is revealed.\n\n",
        "🚪 **Frozen Gate**\n\nThe massive gate groans as you push it open. Beyond lies a secret path.\n\n",
        "🚪 **Frozen Gate**\n\nWith tremendous effort, you break the ice seal. The gate swings wide.\n\n"
    };
    
    private static final String[] COLLAPSING_ICE_BRIDGE_SUCCESS = {
        "🌉 **Collapsing Ice Bridge**\n\nYou dash across the splintering ice with perfect timing! The bridge collapses behind you.\n\n",
        "🌉 **Collapsing Ice Bridge**\n\nYour feet barely touch the cracking surface as you leap to safety.\n\n",
        "🌉 **Collapsing Ice Bridge**\n\nWith incredible speed, you cross before the ice gives way completely.\n\n"
    };
    
    private static final String[] NARROW_CREVICE_SUCCESS = {
        "🕳️ **Narrow Crevice**\n\nYou slip through the narrow opening with ease! A hidden chamber awaits.\n\n",
        "🕳️ **Narrow Crevice**\n\nYour lithe form slides through the tight passage effortlessly.\n\n",
        "🕳️ **Narrow Crevice**\n\nWith practiced grace, you navigate the cramped space and emerge into a secret room.\n\n"
    };
    
    private static final String[] WHISPERING_BARRIER_SUCCESS = {
        "🌀 **Whispering Barrier**\n\nYou decipher the layered runes! The barrier fades, revealing ancient knowledge.\n\n",
        "🌀 **Whispering Barrier**\n\nThe runes respond to your understanding. The barrier dissolves before you.\n\n",
        "🌀 **Whispering Barrier**\n\nYour mind unlocks the pattern. The magical wall shimmers and disappears.\n\n"
    };
    
    private static final String[] ANCIENT_LIBRARY_SUCCESS = {
        "📚 **Ancient Library**\n\nYou decipher the forgotten texts! Ancient wisdom flows into you.\n\n",
        "📚 **Ancient Library**\n\nThe ancient script yields its secrets to your scholarly mind.\n\n",
        "📚 **Ancient Library**\n\nYour knowledge unlocks the library's mysteries. Forgotten lore is yours.\n\n"
    };
    
    private static final String[] BURIED_CACHE_SUCCESS = {
        "💎 **Buried Cache**\n\nYou dig through the snow and find a hidden cache! Fortune smiles upon you.\n\n",
        "💎 **Buried Cache**\n\nA lucky stumble reveals a buried treasure trove beneath the ice.\n\n",
        "💎 **Buried Cache**\n\nYour keen eye spots something glinting in the snow. A hidden hoard awaits.\n\n"
    };
    
    private static final String[] MYSTERIOUS_GLIMMER_SUCCESS = {
        "✨ **Mysterious Glimmer**\n\nThe glimmer reveals itself! A treasure trove of essences awaits.\n\n",
        "✨ **Mysterious Glimmer**\n\nFollowing the light leads you to a cache of rare materials.\n\n",
        "✨ **Mysterious Glimmer**\n\nThe shimmering light guides you to a hidden stash of valuable essences.\n\n"
    };
    
    private static final String[] BLIZZARD_PASSAGE_SUCCESS = {
        "❄️ **Blizzard Passage**\n\nYou endure the howling blizzard! Your vitality proves unbreakable.\n\n",
        "❄️ **Blizzard Passage**\n\nThe storm rages, but you push through. Your endurance is unmatched.\n\n",
        "❄️ **Blizzard Passage**\n\nThrough wind and ice, you march forward. Nothing can stop you.\n\n"
    };
    
    private static final String[] TOXIC_MIASMA_SUCCESS = {
        "☁️ **Toxic Miasma**\n\nYou resist the poisonous mist! Your vitality overcomes the toxin.\n\n",
        "☁️ **Toxic Miasma**\n\nThe toxic cloud parts before your resilience. You emerge unscathed.\n\n",
        "☁️ **Toxic Miasma**\n\nYour body fights off the poison. The miasma cannot harm you.\n\n"
    };
    
    // Narrative variations for failure outcomes
    private static final String[] FROSTBOUND_BOULDER_FAILURE = {
        "🪨 **Frostbound Boulder**\n\nThe boulder doesn't budge. You strain against it, but brute force wasn't enough.\n\n",
        "🪨 **Frostbound Boulder**\n\nThe frozen stone resists your efforts. You'll need more strength.\n\n",
        "🪨 **Frostbound Boulder**\n\nDespite your best efforts, the boulder remains unmoved. Your muscles ache from the attempt.\n\n"
    };
    
    private static final String[] FROZEN_GATE_FAILURE = {
        "🚪 **Frozen Gate**\n\nThe gate resists your efforts. The ancient ice is too strong.\n\n",
        "🚪 **Frozen Gate**\n\nYou push with all your might, but the gate won't yield. More power is needed.\n\n",
        "🚪 **Frozen Gate**\n\nThe massive door remains sealed. Your strength isn't enough to break the ice lock.\n\n"
    };
    
    private static final String[] COLLAPSING_ICE_BRIDGE_FAILURE = {
        "🌉 **Collapsing Ice Bridge**\n\nThe ice gives way! You fall into the freezing water below.\n\n",
        "🌉 **Collapsing Ice Bridge**\n\nYour footing fails as the bridge crumbles. The cold water shocks you.\n\n",
        "🌉 **Collapsing Ice Bridge**\n\nYou're too slow! The bridge collapses beneath you, plunging you into icy depths.\n\n"
    };
    
    private static final String[] NARROW_CREVICE_FAILURE = {
        "🕳️ **Narrow Crevice**\n\nYou get stuck in the opening! It takes time to free yourself.\n\n",
        "🕳️ **Narrow Crevice**\n\nThe passage is too tight. You struggle to extract yourself.\n\n",
        "🕳️ **Narrow Crevice**\n\nYou misjudge the opening and become wedged. Freedom comes slowly.\n\n"
    };
    
    private static final String[] WHISPERING_BARRIER_FAILURE = {
        "🌀 **Whispering Barrier**\n\nThe runes overwhelm your mind! You retreat, but gain a fragment of understanding.\n\n",
        "🌀 **Whispering Barrier**\n\nThe magical symbols are too complex. Your mind reels from the attempt.\n\n",
        "🌀 **Whispering Barrier**\n\nYou can't decipher the pattern. The barrier's magic is beyond your current knowledge.\n\n"
    };
    
    private static final String[] ANCIENT_LIBRARY_FAILURE = {
        "📚 **Ancient Library**\n\nThe texts are too complex for your current knowledge. You leave with a hint for the future.\n\n",
        "📚 **Ancient Library**\n\nThe ancient script is indecipherable. You'll need more learning.\n\n",
        "📚 **Ancient Library**\n\nThe library's secrets remain locked. Your intelligence isn't quite enough yet.\n\n"
    };
    
    private static final String[] BURIED_CACHE_FAILURE = {
        "💎 **Buried Cache**\n\nThe cache crumbles into dust as you reach for it. Your luck wasn't quite enough.\n\n",
        "💎 **Buried Cache**\n\nYou find nothing but empty space. Fortune wasn't on your side.\n\n",
        "💎 **Buried Cache**\n\nThe treasure eludes you. Your luck needs to improve.\n\n"
    };
    
    private static final String[] MYSTERIOUS_GLIMMER_FAILURE = {
        "✨ **Mysterious Glimmer**\n\nThe glimmer fades as you approach. It was just a trick of the light.\n\n",
        "✨ **Mysterious Glimmer**\n\nYou reach for it, but it vanishes. Your luck wasn't strong enough.\n\n",
        "✨ **Mysterious Glimmer**\n\nThe light disappears before you can claim it. Fortune slips away.\n\n"
    };
    
    private static final String[] BLIZZARD_PASSAGE_FAILURE = {
        "❄️ **Blizzard Passage**\n\nThe blizzard forces you to retreat! The cold is too harsh.\n\n",
        "❄️ **Blizzard Passage**\n\nYou're driven back by the storm. Your endurance isn't enough.\n\n",
        "❄️ **Blizzard Passage**\n\nThe freezing winds overwhelm you. You must turn back.\n\n"
    };
    
    private static final String[] TOXIC_MIASMA_FAILURE = {
        "☁️ **Toxic Miasma**\n\nThe poison seeps into your bones! You retreat, weakened.\n\n",
        "☁️ **Toxic Miasma**\n\nThe toxic cloud overcomes your resistance. You're forced to withdraw.\n\n",
        "☁️ **Toxic Miasma**\n\nYour body can't handle the poison. You stumble away, feeling the toxin's effects.\n\n"
    };

    /**
     * Rolls for a stat-gated interaction.
     * Only triggers if character is Level 10+ and hasn't exceeded the limit (3 per interaction type).
     *
     * @param character the character exploring
     * @return interaction type or null if no interaction
     */
    public StatInteractionType rollForStatInteraction(RPGCharacter character) {
        // Level check
        if (character.getLevel() < MIN_LEVEL) {
            return null;
        }
        
        // Roll for interaction (10-15% chance)
        double chance = MIN_INTERACTION_CHANCE + (random.nextDouble() * (MAX_INTERACTION_CHANCE - MIN_INTERACTION_CHANCE));
        if (random.nextDouble() >= chance) {
            return null;
        }
        
        // Get all interaction types and filter by availability
        StatInteractionType[] allTypes = StatInteractionType.values();
        java.util.List<StatInteractionType> availableTypes = new java.util.ArrayList<>();
        
        for (StatInteractionType type : allTypes) {
            if (character.canTriggerStatInteraction(type.name())) {
                availableTypes.add(type);
            }
        }
        
        // If no available types, return null
        if (availableTypes.isEmpty()) {
            return null;
        }
        
        // Randomly select from available interaction types
        return availableTypes.get(random.nextInt(availableTypes.size()));
    }

    /**
     * Checks if character meets stat requirement for interaction.
     *
     * @param character the character
     * @param interaction the interaction type
     * @return true if requirement is met
     */
    public boolean checkStatRequirement(RPGCharacter character, StatInteractionType interaction) {
        int required = interaction.getRequiredStatValue(character.getLevel());
        int actual = getCharacterStat(character, interaction.getRequiredStat());
        return actual >= required;
    }

    /**
     * Gets character stat value by name.
     */
    private int getCharacterStat(RPGCharacter character, String statName) {
        return switch (statName) {
            case "STR" -> character.getStats().getStrength();
            case "AGI" -> character.getStats().getAgility();
            case "INT" -> character.getStats().getIntelligence();
            case "LUCK" -> character.getStats().getLuck();
            case "HP" -> character.getStats().getMaxHp();
            default -> 0;
        };
    }

    /**
     * Handles a stat interaction and returns the outcome.
     *
     * @param interaction the interaction type
     * @param character the character
     * @param config the RPG config
     * @param activeCurses list of active world curses
     * @param songReduction Song of Nilfheim curse reduction
     * @return the action outcome
     */
    public RPGActionOutcome handleStatInteraction(StatInteractionType interaction, RPGCharacter character,
                                                  RPGConfig config, List<WorldCurse> activeCurses, double songReduction) {
        // Increment count for this interaction type
        character.incrementStatInteractionCount(interaction.name());
        
        boolean success = checkStatRequirement(character, interaction);
        
        if (success) {
            return getSuccessOutcome(interaction, character, config, activeCurses, songReduction);
        } else {
            return getFailureOutcome(interaction, character, config, activeCurses, songReduction);
        }
    }

    /**
     * Generates success outcome for stat interaction.
     */
    private RPGActionOutcome getSuccessOutcome(StatInteractionType interaction, RPGCharacter character,
                                               RPGConfig config, List<WorldCurse> activeCurses, double songReduction) {
        int baseXp = 30 + (character.getLevel() * 5);
        int xpGained = (int) ((baseXp * 1.3) * config.getXpMultiplier());
        
        RPGActionOutcome.Builder builder = RPGActionOutcome.builder()
                .xpGained(xpGained)
                .leveledUp(false)
                .success(true);
        
        String baseNarrative = switch (interaction) {
            case FROSTBOUND_BOULDER -> FROSTBOUND_BOULDER_SUCCESS[random.nextInt(FROSTBOUND_BOULDER_SUCCESS.length)];
            case FROZEN_GATE -> FROZEN_GATE_SUCCESS[random.nextInt(FROZEN_GATE_SUCCESS.length)];
            case COLLAPSING_ICE_BRIDGE -> COLLAPSING_ICE_BRIDGE_SUCCESS[random.nextInt(COLLAPSING_ICE_BRIDGE_SUCCESS.length)];
            case NARROW_CREVICE -> NARROW_CREVICE_SUCCESS[random.nextInt(NARROW_CREVICE_SUCCESS.length)];
            case WHISPERING_BARRIER -> WHISPERING_BARRIER_SUCCESS[random.nextInt(WHISPERING_BARRIER_SUCCESS.length)];
            case ANCIENT_LIBRARY -> ANCIENT_LIBRARY_SUCCESS[random.nextInt(ANCIENT_LIBRARY_SUCCESS.length)];
            case BURIED_CACHE -> BURIED_CACHE_SUCCESS[random.nextInt(BURIED_CACHE_SUCCESS.length)];
            case MYSTERIOUS_GLIMMER -> MYSTERIOUS_GLIMMER_SUCCESS[random.nextInt(MYSTERIOUS_GLIMMER_SUCCESS.length)];
            case BLIZZARD_PASSAGE -> BLIZZARD_PASSAGE_SUCCESS[random.nextInt(BLIZZARD_PASSAGE_SUCCESS.length)];
            case TOXIC_MIASMA -> TOXIC_MIASMA_SUCCESS[random.nextInt(TOXIC_MIASMA_SUCCESS.length)];
        };
        
        String narrative = switch (interaction) {
            case FROSTBOUND_BOULDER -> {
                // Success: Clear path, bonus XP, chance for hidden enemy/rune
                int essenceCount = random.nextInt(2) + 1;
                EssenceType essence = EssenceType.EMBER_SHARD; // STR-aligned
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n\n" +
                      "Your warrior's might proved sufficient.";
            }
            case FROZEN_GATE -> {
                // Success: Force open, bonus materials, shortcut unlocked
                int essenceCount = random.nextInt(2) + 2;
                EssenceType essence = EssenceType.EMBER_SHARD;
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                if (random.nextDouble() < 0.15) {
                    CatalystType catalyst = CatalystType.values()[random.nextInt(CatalystType.values().length)];
                    builder.addCatalystDrop(catalyst, 1);
                    character.getInventory().addCatalyst(catalyst, 1);
                    yield baseNarrative +
                          "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n" +
                          "**Found:** " + catalyst.getEmoji() + " " + catalyst.getDisplayName() + "\n\n" +
                          "The shortcut is yours.";
                }
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n\n" +
                      "The shortcut is yours.";
            }
            case COLLAPSING_ICE_BRIDGE -> {
                // Success: Cross safely, bonus materials
                int essenceCount = random.nextInt(2) + 1;
                EssenceType essence = EssenceType.GALE_FRAGMENT; // AGI-aligned
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n\n" +
                      "Your agility saved you.";
            }
            case NARROW_CREVICE -> {
                // Success: Slip through, rare catalyst, hidden area unlocked
                EssenceType essence = EssenceType.GALE_FRAGMENT;
                builder.addItemDrop(essence, 2);
                character.getInventory().addEssence(essence, 2);
                if (random.nextDouble() < 0.25) {
                    CatalystType catalyst = CatalystType.values()[random.nextInt(CatalystType.values().length)];
                    builder.addCatalystDrop(catalyst, 1);
                    character.getInventory().addCatalyst(catalyst, 1);
                    yield baseNarrative +
                          "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×2\n" +
                          "**Found:** " + catalyst.getEmoji() + " " + catalyst.getDisplayName() + "\n\n" +
                          "Your dexterity unlocked treasures.";
                }
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×2\n\n" +
                      "Your dexterity unlocked treasures.";
            }
            case WHISPERING_BARRIER -> {
                // Success: Decode runes, barrier fades, rare lore fragment, bonus exploration roll
                int essenceCount = random.nextInt(2) + 1;
                EssenceType essence = EssenceType.MIND_CRYSTAL; // INT-aligned
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n\n" +
                      "Your intelligence unlocked the secrets.";
            }
            case ANCIENT_LIBRARY -> {
                // Success: Decipher texts, +1 stat point, crafting bonus catalyst
                int essenceCount = random.nextInt(2) + 2;
                EssenceType essence = EssenceType.MIND_CRYSTAL;
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                if (random.nextDouble() < 0.20) {
                    CatalystType catalyst = CatalystType.RUNIC_BINDING;
                    builder.addCatalystDrop(catalyst, 1);
                    character.getInventory().addCatalyst(catalyst, 1);
                    yield baseNarrative +
                          "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n" +
                          "**Found:** " + catalyst.getEmoji() + " " + catalyst.getDisplayName() + "\n\n" +
                          "Your knowledge unlocked the library's secrets.";
                }
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n\n" +
                      "Your knowledge unlocked the library's secrets.";
            }
            case BURIED_CACHE -> {
                // Success: Rare catalyst, infusion, small chance at relic fragment
                EssenceType essence = EssenceType.FATE_CLOVER; // LUCK-aligned
                int essenceCount = random.nextInt(2) + 2;
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                if (random.nextDouble() < 0.30) {
                    CatalystType catalyst = CatalystType.values()[random.nextInt(CatalystType.values().length)];
                    builder.addCatalystDrop(catalyst, 1);
                    character.getInventory().addCatalyst(catalyst, 1);
                    yield baseNarrative +
                          "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n" +
                          "**Found:** " + catalyst.getEmoji() + " " + catalyst.getDisplayName() + "\n\n" +
                          "Your luck led you to treasure.";
                }
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n\n" +
                      "Your luck led you to treasure.";
            }
            case MYSTERIOUS_GLIMMER -> {
                // Success: Random rare item, +1 essence of each type, story flag chance
                EssenceType[] essences = EssenceType.values();
                for (EssenceType e : essences) {
                    builder.addItemDrop(e, 1);
                    character.getInventory().addEssence(e, 1);
                }
                yield baseNarrative +
                      "**Found:** One of each essence type!\n\n" +
                      "Fortune truly favors you.";
            }
            case BLIZZARD_PASSAGE -> {
                // Success: Endure the storm, +10% max HP (temporary 24h), bonus XP
                int essenceCount = random.nextInt(2) + 1;
                EssenceType essence = EssenceType.VITAL_ASH; // HP-aligned
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n\n" +
                      "The storm could not break you.";
            }
            case TOXIC_MIASMA -> {
                // Success: Resist poison, gain immunity to next negative event, bonus materials
                int essenceCount = random.nextInt(2) + 2;
                EssenceType essence = EssenceType.VITAL_ASH;
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×" + essenceCount + "\n\n" +
                      "Your endurance saved you.";
            }
        };
        
        builder.narrative(narrative);
        return builder.build();
    }

    /**
     * Generates failure outcome for stat interaction.
     */
    private RPGActionOutcome getFailureOutcome(StatInteractionType interaction, RPGCharacter character,
                                              RPGConfig config, List<WorldCurse> activeCurses, double songReduction) {
        int baseXp = 30 + (character.getLevel() * 5);
        // Reduced XP for failure (but never below 90% of original)
        int xpGained = (int) ((baseXp * 0.7) * config.getXpMultiplier());
        int minXp = (int) (baseXp * 0.9 * config.getXpMultiplier());
        xpGained = Math.max(minXp, xpGained);
        
        RPGActionOutcome.Builder builder = RPGActionOutcome.builder()
                .xpGained(xpGained)
                .leveledUp(false)
                .success(false);
        
        String baseNarrative = switch (interaction) {
            case FROSTBOUND_BOULDER -> FROSTBOUND_BOULDER_FAILURE[random.nextInt(FROSTBOUND_BOULDER_FAILURE.length)];
            case FROZEN_GATE -> FROZEN_GATE_FAILURE[random.nextInt(FROZEN_GATE_FAILURE.length)];
            case COLLAPSING_ICE_BRIDGE -> COLLAPSING_ICE_BRIDGE_FAILURE[random.nextInt(COLLAPSING_ICE_BRIDGE_FAILURE.length)];
            case NARROW_CREVICE -> NARROW_CREVICE_FAILURE[random.nextInt(NARROW_CREVICE_FAILURE.length)];
            case WHISPERING_BARRIER -> WHISPERING_BARRIER_FAILURE[random.nextInt(WHISPERING_BARRIER_FAILURE.length)];
            case ANCIENT_LIBRARY -> ANCIENT_LIBRARY_FAILURE[random.nextInt(ANCIENT_LIBRARY_FAILURE.length)];
            case BURIED_CACHE -> BURIED_CACHE_FAILURE[random.nextInt(BURIED_CACHE_FAILURE.length)];
            case MYSTERIOUS_GLIMMER -> MYSTERIOUS_GLIMMER_FAILURE[random.nextInt(MYSTERIOUS_GLIMMER_FAILURE.length)];
            case BLIZZARD_PASSAGE -> BLIZZARD_PASSAGE_FAILURE[random.nextInt(BLIZZARD_PASSAGE_FAILURE.length)];
            case TOXIC_MIASMA -> TOXIC_MIASMA_FAILURE[random.nextInt(TOXIC_MIASMA_FAILURE.length)];
        };
        
        String narrative = switch (interaction) {
            case FROSTBOUND_BOULDER -> {
                // Failure: Minor HP damage, reduced XP, narrative
                int damage = (int) (character.getStats().getMaxHp() * 0.05); // 5% max HP
                builder.damageTaken(damage);
                character.getStats().takeDamage(damage);
                yield baseNarrative +
                      "**Damage Taken:** " + damage + " HP\n\n" +
                      "You leave knowing you need more strength.";
            }
            case FROZEN_GATE -> {
                // Failure: Gate resists, take environmental damage, forced rest next action
                int damage = (int) (character.getStats().getMaxHp() * 0.08);
                builder.damageTaken(damage);
                character.getStats().takeDamage(damage);
                yield baseNarrative +
                      "**Damage Taken:** " + damage + " HP\n\n" +
                      "You're forced to turn back. Rest may help you recover.";
            }
            case COLLAPSING_ICE_BRIDGE -> {
                // Failure: Fall into water, HP loss, forced rest state
                int damage = (int) (character.getStats().getMaxHp() * 0.10);
                builder.damageTaken(damage);
                character.getStats().takeDamage(damage);
                yield baseNarrative +
                      "**Damage Taken:** " + damage + " HP\n\n" +
                      "You need to rest and recover from the cold.";
            }
            case NARROW_CREVICE -> {
                // Failure: Get stuck, lose 1 charge next refresh, partial reward
                int essenceCount = 1;
                EssenceType essence = EssenceType.GALE_FRAGMENT;
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×1\n" +
                      "**Penalty:** You'll lose 1 action charge on next refresh.\n\n" +
                      "Your agility wasn't quite enough.";
            }
            case WHISPERING_BARRIER -> {
                // Failure: Runes overwhelm, lose 1 charge next refresh, partial lore fragment
                int essenceCount = 1;
                EssenceType essence = EssenceType.MIND_CRYSTAL;
                builder.addItemDrop(essence, essenceCount);
                character.getInventory().addEssence(essence, essenceCount);
                yield baseNarrative +
                      "**Found:** " + essence.getEmoji() + " " + essence.getDisplayName() + " ×1\n" +
                      "**Penalty:** You'll lose 1 action charge on next refresh.\n\n" +
                      "You need more intelligence to fully understand the barrier.";
            }
            case ANCIENT_LIBRARY -> {
                // Failure: Texts too complex, temporary -5% XP for 24h, hint for future
                yield baseNarrative +
                      "**Penalty:** -5% XP gain for 24 hours.\n\n" +
                      "Return when your intelligence has grown.";
            }
            case BURIED_CACHE -> {
                // Failure: Cache crumbles, small XP, increased LUCK check odds for 24h (pity)
                yield baseNarrative +
                      "**Bonus:** Increased LUCK check odds for next 24 hours.\n\n" +
                      "Fortune may favor you next time.";
            }
            case MYSTERIOUS_GLIMMER -> {
                // Failure: Glimmer fades, +5% item drop chance for next 3 explores
                yield baseNarrative +
                      "**Bonus:** +5% item drop chance for your next 3 explorations.\n\n" +
                      "Your luck will improve.";
            }
            case BLIZZARD_PASSAGE -> {
                // Failure: Forced retreat, -10% HP, delayed charge refresh
                int damage = (int) (character.getStats().getMaxHp() * 0.10);
                builder.damageTaken(damage);
                character.getStats().takeDamage(damage);
                yield baseNarrative +
                      "**Damage Taken:** " + damage + " HP\n" +
                      "**Penalty:** Charge refresh delayed by 2 hours.\n\n" +
                      "You need more vitality to endure such storms.";
            }
            case TOXIC_MIASMA -> {
                // Failure: Poisoned, -15% HP, -5% all stats for 24h
                int damage = (int) (character.getStats().getMaxHp() * 0.15);
                builder.damageTaken(damage);
                character.getStats().takeDamage(damage);
                yield baseNarrative +
                      "**Damage Taken:** " + damage + " HP\n" +
                      "**Penalty:** -5% all stats for 24 hours.\n\n" +
                      "You need more vitality to resist such toxins.";
            }
        };
        
        builder.narrative(narrative);
        return builder.build();
    }
}
