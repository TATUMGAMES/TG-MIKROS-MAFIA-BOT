package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.EssenceType;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;

import java.util.Random;

/**
 * Explore action - player explores the world and encounters random events.
 * Rewards XP and generates narrative encounters.
 */
public class ExploreAction implements CharacterAction {
    private static final Random random = new Random();

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

    @Override
    public RPGActionOutcome execute(RPGCharacter character, RPGConfig config) {
        // Select random narrative
        String narrative = NARRATIVES[random.nextInt(NARRATIVES.length)];

        // Calculate XP gain (scales with level and config multiplier)
        int baseXp = 30 + (character.getLevel() * 5);
        int variance = random.nextInt(20) - 10; // +/- 10
        int xpGained = (int) ((baseXp + variance) * config.getXpMultiplier());

        // Add XP and check for level up
        boolean leveledUp = character.addXp(xpGained);

        // AGI-based item drop chance bonus
        // Base: 12.5% chance
        // AGI bonus: +0.5% per AGI point (capped at +15%)
        double baseDropChance = 0.125;
        int agility = character.getStats().getAgility();
        double agilityBonus = Math.min(0.15, agility * 0.005);
        double dropChance = baseDropChance + agilityBonus; // 12.5% to 27.5%

        RPGActionOutcome.Builder outcomeBuilder = RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .hpRestored(0)
                .success(true);

        // Roll for item drops with AGI bonus
        if (random.nextDouble() < dropChance) {
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

        return outcomeBuilder.build();
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
     * Gets a random essence type with class-specific bonuses.
     *
     * @param characterClass the character's class
     * @return random essence type (biased by class)
     */
    private EssenceType getRandomEssenceWithClassBonus(com.tatumgames.mikros.games.rpg.model.CharacterClass characterClass) {
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
        }
        
        // Priest also gets +3% chance for LUCK-aligned (Fate Clover)
        if (characterClass == com.tatumgames.mikros.games.rpg.model.CharacterClass.PRIEST) {
            if (random.nextDouble() < 0.03) {
                return EssenceType.FATE_CLOVER;
            }
        }
        
        // Default: random essence
        return allEssences[random.nextInt(allEssences.length)];
    }
}

