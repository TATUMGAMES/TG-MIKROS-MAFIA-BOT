package com.tatumgames.mikros.games.rpg.biome;

import java.util.*;

/**
 * Utility class for biome-specific exploration narratives. Categorizes existing exploration
 * narratives by biome theme.
 */
public class BiomeLore {
    private static final Random random = new Random();

    // Lore organized by biome - all narratives from existing code
    private static final Map<BiomeType, List<String>> LORE_BY_BIOME = new HashMap<>();

    static {
        // FROZEN_WASTES - All ice/frost/snow/frozen themed narratives
        LORE_BY_BIOME.put(
                BiomeType.FROZEN_WASTES,
                Arrays.asList(
                        "You discover a frozen shrine emitting faint blue light.",
                        "Tracks in the snow lead you to an abandoned campsite.",
                        "A wandering merchant greets you, then vanishes in a flurry of snow.",
                        "A mysterious whisper echoes through a frost cavern.",
                        "A sudden blizzard almost blinds you, but you push onward.",
                        "You spot a distant figure watching you… then it disappears.",
                        "You wander into a hollow tree filled with shimmering frost-bugs.",
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
                        "You find a torn cloak clasp made of dragonbone.",
                        "The sky ripples with aurora lights that form strange, ancient symbols.",
                        "A fragment of the Shattering of the First Winter drifts past, frozen in time.",
                        "You discover a training ground used by Frostborne warriors, their techniques still visible in the ice.",
                        "You discover the remains of a Frostgate outpost, its banners still fluttering in the eternal wind.",
                        "Ancient Frostborne runes carved into a glacier begin to glow as you approach.",
                        "You discover a hidden hot spring that never freezes, said to be blessed by the Frost element.",
                        "You find a cache of weapons forged by the Frostborne, still sharp after centuries.",
                        "You find a map leading to Frostgate, marked with routes through the Ice Wastes.",
                        "A fragment of the first winter's ice contains memories of the Shattering itself.",
                        "You find a training manual from the Frostborne warriors, detailing their combat techniques."));

        // ANCIENT_RUINS - References to ruins, Shattering, Grand Library, Nil City, ancient
        // civilizations
        LORE_BY_BIOME.put(
                BiomeType.ANCIENT_RUINS,
                Arrays.asList(
                        "You discover ancient ruins from before the Shattering, their runes still glowing faintly.",
                        "You find a hidden entrance to the Grand Library of Nil City, filled with forbidden knowledge.",
                        "You stumble upon the Moonspire Obelisk, its ancient runic inscriptions pulsing with power.",
                        "You discovered ruins of an old civilization and learned from their forgotten wisdom.",
                        "In the depths of a library, you uncovered secrets that broaden your understanding.",
                        "A library of the first civilizations after the Shattering reveals secrets of the Eight Elements.",
                        "Ancient battlefields from the Shattering still echo with the sounds of war.",
                        "A Stormwarden's journal reveals techniques for mastering the Gale element.",
                        "You discover a shrine to the Eight Elements, each one represented by glowing crystals.",
                        "The Grand Library's forbidden section opens to you, revealing knowledge of the Void element.",
                        "Ancient prophecies carved into the Moonspire Obelisk begin to make sense as you read them.",
                        "You discover a hidden passage to Nil City, its spires visible through the mist.",
                        "Ancient runes from Starfall Ridge tell the story of the first civilizations after the Shattering.",
                        "You find a hidden chamber beneath the Grand Library, filled with knowledge of the Eight Elements.",
                        "You solved an ancient puzzle in a long-forgotten temple, unlocking hidden knowledge.",
                        "You discover the Eight Elements converge here - you feel Frost, Gale, Ember, Void, and Astral energies mixing."));

        // SHADOWED_FORESTS - Forest, grove, wilderness, nature-themed narratives
        LORE_BY_BIOME.put(
                BiomeType.SHADOWED_FORESTS,
                Arrays.asList(
                        "You ventured into an ancient forest and discovered a hidden shrine. The spirits blessed your journey.",
                        "You found a peaceful grove where time seems to stand still. Meditation here brings growth.",
                        "While foraging in the wilderness, you found rare herbs that boost your vitality.",
                        "You helped villagers defend against bandits, earning their gratitude and respect.",
                        "While exploring a misty valley, you helped a lost traveler find their way home.",
                        "You crossed paths with a friendly bard who shared tales of legendary heroes.",
                        "In a bustling market square, you learned valuable techniques from a traveling merchant.",
                        "A chance encounter with a wise hermit taught you valuable life lessons.",
                        "While resting by a enchanted spring, you felt your potential grow stronger.",
                        "You find a rune-inscribed stone warm to the touch.",
                        "A strange glowing feather lands in your palm.",
                        "You discover an ancient tree with runes carved into its bark, pulsing with faint green light.",
                        "A forest path shifts before your eyes, leading you to a hidden grove you've never seen before.",
                        "You find a circle of standing stones where the Spirit Veil feels unusually thin.",
                        "Moonlight filters through the canopy, illuminating a clearing filled with glowing mushrooms.",
                        "You encounter a pack of forest wolves that watch you warily before vanishing into the shadows.",
                        "An old tree hollow contains a cache of rare forest herbs, carefully preserved.",
                        "You discover a small village hidden deep in the woods, its inhabitants welcoming but cautious.",
                        "A stream flows through the forest, its waters shimmering with traces of nature magic.",
                        "You find a druid's staff leaning against a tree, its wood still warm to the touch.",
                        "Ancient forest spirits whisper to you, sharing fragments of forgotten wisdom.",
                        "You stumble upon a sacred grove where the twin moons' light creates patterns on the ground.",
                        "A forest guardian made of living wood watches you pass, then returns to stillness.",
                        "You find a hidden path marked by glowing moss that leads to a secret clearing.",
                        "The forest seems to guide you, branches shifting to reveal a safe route forward.",
                        "You discover a tree that bears fruit glowing with inner light, said to grant clarity.",
                        "A misty fog rolls through the forest, and for a moment, you see spirits dancing between the trees.",
                        "You find a stone circle where villagers once gathered, now overgrown but still holding power.",
                        "An ancient oak tree's roots form a natural shelter, and you rest beneath its protective canopy.",
                        "You encounter a traveling merchant who specializes in forest-crafted goods and rare materials.",
                        "The forest floor is covered in fallen leaves that crunch softly, revealing hidden paths.",
                        "You find a grove where flowers bloom even in the harshest conditions, their petals shimmering.",
                        "A forest spirit appears briefly, leaving behind a gift of rare crafting materials before fading away."));

        // VOLCANIC_DEPTHS - Caves, dungeons, underground, volcanic themes
        LORE_BY_BIOME.put(
                BiomeType.VOLCANIC_DEPTHS,
                Arrays.asList(
                        "You stumbled upon a mysterious cave filled with glowing crystals. Their energy invigorates you.",
                        "Deep in a dungeon, you found ancient texts that expanded your knowledge.",
                        "A faint trail of warmth leads you to a buried emberstone.",
                        "You witness a Stormwarden passes by, leaving behind a trail of crackling Gale energy.",
                        "You witness a Stormwarden training with the Gale element, their movements a blur of wind and lightning.",
                        "You discover a lava flow that glows with the power of the Ember element, its heat invigorating.",
                        "Deep underground, you find an ancient forge where Ember element crystals were once crafted into weapons.",
                        "A volcanic vent spews hot air, and you feel the Ember element's energy coursing through the chamber.",
                        "You stumble upon a network of underground tunnels, their walls lined with volcanic glass formations.",
                        "An underground river of magma flows nearby, casting an orange glow that reveals hidden passages.",
                        "You discover a cache of emberstones buried in volcanic rock, still warm after centuries.",
                        "Deep in the volcanic depths, you find an abandoned mining settlement, its tools still sharp.",
                        "A geyser erupts nearby, its steam carrying traces of the Ember element that strengthen you.",
                        "You find a chamber where the Ember element has crystallized into beautiful, glowing formations.",
                        "An ancient underground city carved into volcanic rock reveals secrets of the Eight Elements.",
                        "You discover a forge where Stormwardens once worked, combining Gale and Ember elements.",
                        "A pool of molten rock bubbles gently, and you sense the Ember element's raw power within.",
                        "You find a tunnel system that leads deeper underground, where heat-resistant creatures dwell.",
                        "Volcanic crystals embedded in the walls pulse with inner fire, their light guiding your path.",
                        "You discover an ancient text carved into volcanic glass, detailing Ember element techniques.",
                        "A hidden chamber contains weapons forged in volcanic fires, still radiating heat.",
                        "You find a natural hot spring fed by volcanic heat, its waters infused with Ember energy.",
                        "Deep underground, you encounter a Stormwarden who teaches you about combining Gale and Ember elements.",
                        "A volcanic vent releases steam that forms patterns in the air, revealing hidden runes.",
                        "You discover a buried treasure chest protected by volcanic rock, its contents still intact.",
                        "An underground cavern glows with the light of Ember element crystals, creating a warm sanctuary.",
                        "You find a passage that leads to a massive underground chamber filled with volcanic activity.",
                        "A heat-resistant creature guides you through the volcanic depths, showing you safe paths.",
                        "You discover an ancient ritual site where the Ember element was once channeled for crafting.",
                        "Volcanic glass formations reflect the light of your torch, revealing hidden symbols carved into the walls.",
                        "You find a deep chasm where lava flows far below, its glow illuminating ancient carvings on the walls."));

        // MYSTICAL_HEIGHTS - Mountains, high places, sky/astral themes, Starfall Ridge
        LORE_BY_BIOME.put(
                BiomeType.MYSTICAL_HEIGHTS,
                Arrays.asList(
                        "You climbed a towering mountain and witnessed a breathtaking sunrise. Clarity washes over you.",
                        "You find a hidden chamber beneath Starfall Ridge, filled with star fragments and cosmic energy.",
                        "You find a meditation circle where the Astral element is strongest, showing you possible futures.",
                        "You discover a cache of Astral element crystals, their power showing you glimpses of fate.",
                        "You catch a glimpse of a shadow that mirrors your movements perfectly.",
                        "At the peak of a mountain, you witness the twin moons align, their light revealing hidden paths.",
                        "You discover a celestial observatory carved into the mountainside, its instruments still functional.",
                        "Star fragments rain down from Starfall Ridge, and you collect several that pulse with Astral energy.",
                        "You find a high-altitude meditation site where the Astral element flows freely, granting visions.",
                        "A mountain pass reveals a breathtaking view of Nilfheim, and you feel a sense of clarity.",
                        "You discover a pool of cosmic energy at the summit, its surface reflecting possible futures.",
                        "The Astral element manifests as shimmering lights in the sky, guiding you to a hidden location.",
                        "You find an ancient stone circle at the mountain peak, aligned with the stars above.",
                        "A meteor streaks across the sky, and you track its path to a crater filled with star fragments.",
                        "You climb to a high ledge where the Astral element is so strong, you see glimpses of other timelines.",
                        "The twin moons are clearly visible from this height, their alignment creating patterns of light.",
                        "You discover a cache of Astral element crystals embedded in the mountain rock, still glowing.",
                        "A mountain peak offers a view of Starfall Ridge in the distance, its cosmic energy visible.",
                        "You find a meditation circle where ancient seers once practiced, their presence still felt.",
                        "The Astral element shows you a vision of a future battle, giving you insight into strategy.",
                        "You discover a hidden cave filled with star fragments that fell from Starfall Ridge long ago.",
                        "At this altitude, you feel closer to the cosmic forces, and the Astral element responds to your presence.",
                        "You find a celestial map carved into stone, showing the alignment of stars and moons.",
                        "A mountain spring flows with water infused with Astral energy, granting you clarity of thought.",
                        "You witness an aurora in the sky that forms patterns matching ancient Astral runes.",
                        "The shadow you see moves independently, showing you possible actions you might take.",
                        "You discover a high-altitude shrine to the Astral element, its power still active.",
                        "Star fragments embedded in the mountain path glow brighter as you approach, guiding your way.",
                        "You find a meditation spot where the Astral element reveals the true nature of nearby threats.",
                        "A cosmic event occurs overhead, and you feel the Astral element's power surge through you.",
                        "You discover an ancient telescope pointing toward Starfall Ridge, still functional after centuries."));

        // TWISTED_REALMS - Mystical, magical, spirit-themed, portals, Spirit Veil, Arcane Veil
        LORE_BY_BIOME.put(
                BiomeType.TWISTED_REALMS,
                Arrays.asList(
                        "The twin moons align, revealing a path through the Spirit Veil that wasn't there before.",
                        "A portal to the Arcane Veil flickers before you, offering glimpses of other realities.",
                        "The Mortal and Arcane Veils thin here, allowing you to see spirits from both realms.",
                        "The Spirit Veil parts briefly, allowing you to communicate with a long-lost spirit.",
                        "You witness two spirits dancing in the moonlight before fading away.",
                        "You hear soft music carried by the wind — but no musician in sight.",
                        "The Spirit Veil thickens and thins in waves, and you see glimpses of the spirit realm beyond.",
                        "A portal to the Arcane Veil stabilizes for a moment, showing you a version of Nilfheim where the Shattering never occurred.",
                        "The boundary between Mortal and Arcane Veils becomes so thin, you can hear spirits speaking from the other side.",
                        "You find yourself in a liminal space where time moves differently, caught between the two veils.",
                        "A spirit from the Arcane Veil reaches through the thinning barrier, leaving behind a gift before retreating.",
                        "The twin moons' light creates a bridge through the Spirit Veil, and you see spirits crossing it.",
                        "Reality glitches around you, and for a moment, you see yourself from another timeline.",
                        "A portal flickers erratically, showing you rapid glimpses of multiple realities before stabilizing.",
                        "You discover an artifact that exists in both the Mortal and Arcane Veils simultaneously.",
                        "The Spirit Veil parts like a curtain, revealing a spirit marketplace where the living and dead trade.",
                        "You hear echoes of conversations from the Arcane Veil, fragments of knowledge drifting through.",
                        "A spirit guide appears, offering to show you paths through the veils that mortals rarely see.",
                        "The Mortal and Arcane Veils overlap here, creating a space where both realms are visible.",
                        "You find a portal that cycles through different realities, each showing a variation of Nilfheim.",
                        "Spirits from both veils gather here, and you witness a rare convergence of the two realms.",
                        "The Spirit Veil becomes transparent, and you see the spirit realm's landscape stretching beyond.",
                        "A temporal anomaly occurs, and you experience a moment from the past before the Shattering.",
                        "You discover a stable portal to the Arcane Veil, its edges shimmering with otherworldly energy.",
                        "The twin moons' alignment causes the veils to resonate, creating harmonic frequencies that reveal hidden paths.",
                        "Spirits manifest more clearly here, their forms solid enough to interact with the mortal realm.",
                        "You find yourself in a space where the rules of reality bend, and the veils themselves seem alive.",
                        "A portal to the Arcane Veil shows you a future where Nilfheim has been restored, giving you hope.",
                        "The Spirit Veil parts like water, and you can step partially into the spirit realm before returning.",
                        "You witness spirits from different eras converging, sharing knowledge across the boundaries of time.",
                        "The Mortal and Arcane Veils create interference patterns, revealing truths hidden in normal reality.",
                        "A spirit's voice carries through the thinning veils, sharing ancient wisdom before fading away.",
                        "You discover a permanent gateway between the veils, its existence defying normal laws of reality.",
                        "The twin moons' light refracts through the veils, creating prismatic effects that reveal hidden knowledge."));
    }

    /**
     * Gets a random exploration narrative for the specified biome.
     *
     * @param biome the biome type
     * @return a random narrative from that biome's pool
     */
    public static String getRandomLore(BiomeType biome) {
        List<String> lore = LORE_BY_BIOME.get(biome);
        if (lore == null || lore.isEmpty()) {
            // Fallback to first biome if biome not found
            lore = LORE_BY_BIOME.get(BiomeType.FROZEN_WASTES);
        }
        return lore.get(random.nextInt(lore.size()));
    }

    /**
     * Gets all narratives for a biome (for testing/debugging).
     *
     * @param biome the biome type
     * @return list of all narratives for that biome
     */
    public static List<String> getAllLore(BiomeType biome) {
        return new ArrayList<>(LORE_BY_BIOME.getOrDefault(biome, Collections.emptyList()));
    }
}
