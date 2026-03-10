package com.tatumgames.mikros.games.rpg.blessing;

import java.util.Random;

/**
 * Utility class providing narrative templates for blessing announcements. Provides variety in
 * storytelling to keep the world immersive.
 */
public class BlessingNarratives {
    private static final Random random = new Random();

    // Narratives from the Gods/Deities
    private static final String[] GOD_NARRATIVES = {
            "The Gods have sensed your struggle. All heroes are imbued with divine strength.",
            "Divine intervention stirs in Nilfheim. The pantheon grants their favor to the worthy.",
            "Ancient deities watch from the celestial realm. They bestow their power upon you.",
            "The divine council has taken notice. Their blessing flows through every warrior.",
            "Gods of old awaken to your plight. Their essence strengthens your resolve."
    };

    // Narratives from Ancient Spirits of Nilfheim
    private static final String[] SPIRIT_NARRATIVES = {
            "The Ancients have sensed your struggle. This time, all heroes are imbued with the power of the Ancients.",
            "Echoes of the First Winter whisper through the frozen wastes. Their strength becomes yours.",
            "Ancient spirits of Nilfheim rise from the depths. They share their power with the living.",
            "The spirits of fallen heroes stir. Their legacy empowers those who fight on.",
            "Whispers from the Spirit Veil reach across the boundary. Ancient power flows into your veins."
    };

    // Narratives from Celestial Events
    private static final String[] CELESTIAL_NARRATIVES = {
            "The twin moons align in rare harmony. Celestial energy bathes all heroes in power.",
            "A cosmic event unfolds above Nilfheim. Stellar forces grant temporary strength.",
            "The aurora lights dance with unusual intensity. Their radiance empowers the worthy.",
            "Stars align in a pattern unseen for centuries. Their light strengthens your resolve.",
            "A meteor shower illuminates the sky. Each falling star grants a fragment of power."
    };

    // Narratives from Echoes of Nilfheim
    private static final String[] ECHO_NARRATIVES = {
            "The very land of Nilfheim responds to your struggle. The realm itself grants its blessing.",
            "Echoes of the First Winter resonate through the ice. Ancient power awakens.",
            "The frozen wastes remember your valor. They share their enduring strength.",
            "Nilfheim's memory stirs. The realm recognizes your determination and offers aid.",
            "The land itself feels your struggle. Ancient magic flows from the permafrost."
    };

    /**
     * Gets a random narrative for a blessing based on the narrative style.
     *
     * @param style the narrative style
     * @return a random narrative string
     */
    public static String getRandomNarrative(NarrativeStyle style) {
        return switch (style) {
            case GODS -> GOD_NARRATIVES[random.nextInt(GOD_NARRATIVES.length)];
            case SPIRITS -> SPIRIT_NARRATIVES[random.nextInt(SPIRIT_NARRATIVES.length)];
            case CELESTIAL -> CELESTIAL_NARRATIVES[random.nextInt(CELESTIAL_NARRATIVES.length)];
            case ECHOES -> ECHO_NARRATIVES[random.nextInt(ECHO_NARRATIVES.length)];
        };
    }

    /**
     * Gets a random narrative style.
     *
     * @return a random narrative style
     */
    public static NarrativeStyle getRandomStyle() {
        NarrativeStyle[] styles = NarrativeStyle.values();
        return styles[random.nextInt(styles.length)];
    }

    /**
     * Enum representing different narrative styles for blessings.
     */
    public enum NarrativeStyle {
        GODS,
        SPIRITS,
        CELESTIAL,
        ECHOES
    }
}
