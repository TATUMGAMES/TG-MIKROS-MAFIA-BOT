package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
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
            "The sky ripples with aurora lights that form strange, ancient symbols."
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

        // Record the action
        character.recordAction();

        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .hpRestored(0)
                .success(true)
                .build();
    }
}

