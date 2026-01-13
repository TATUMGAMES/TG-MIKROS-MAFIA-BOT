package com.tatumgames.mikros.games.rpg.exploration;

import com.tatumgames.mikros.games.rpg.model.CharacterClass;

/**
 * Enum representing Stonebound Divinities - ancient gods of Nilfheim bound into stone.
 * Each deity offers a blessing with a corresponding curse.
 */
public enum DeityType {
    /**
     * Vaelgor, The Stone Wolf - Strength-focused deity.
     * Blessing: +15% STR effectiveness
     * Curse: -5% INT effectiveness
     */
    VAELGOR_STONE_WOLF(
            "Vaelgor, The Stone Wolf",
            "A colossal statue of a wolf carved from living stone. Ancient runes glow faintly along its flanks. You feel it watching you.",
            "STR",
            "INT",
            0.15, // +15% STR effectiveness
            -0.05, // -5% INT effectiveness
            "STONE_WOLF_MARKED",
            CharacterClass.WARRIOR,
            CharacterClass.KNIGHT
    ),

    /**
     * Ilyra, The Frostwind - Agility-focused deity.
     * Blessing: +15% AGI effectiveness
     * Curse: -5% STR effectiveness
     */
    ILYRA_FROSTWIND(
            "Ilyra, The Frostwind",
            "A statue of a graceful figure wrapped in frozen winds. The air around it shimmers with movement even in stillness.",
            "AGI",
            "STR",
            0.15, // +15% AGI effectiveness
            -0.05, // -5% STR effectiveness
            "FROSTWIND_MARKED",
            CharacterClass.ROGUE,
            null
    ),

    /**
     * Nereth, The Hollow Mind - Intelligence-focused deity.
     * Blessing: +15% INT effectiveness
     * Curse: -5% AGI effectiveness
     */
    NERETH_HOLLOW_MIND(
            "Nereth, The Hollow Mind",
            "A statue with a featureless face, its surface swirling with arcane symbols. The void within seems to gaze into your soul.",
            "INT",
            "AGI",
            0.15, // +15% INT effectiveness
            -0.05, // -5% AGI effectiveness
            "HOLLOW_MIND_MARKED",
            CharacterClass.MAGE,
            CharacterClass.NECROMANCER,
            CharacterClass.PRIEST
    );

    private final String displayName;
    private final String description;
    private final String blessingStat;
    private final String curseStat;
    private final double blessingModifier;
    private final double curseModifier;
    private final String worldFlag;
    private final CharacterClass[] preferredClasses;

    DeityType(String displayName, String description, String blessingStat, String curseStat,
              double blessingModifier, double curseModifier, String worldFlag,
              CharacterClass... preferredClasses) {
        this.displayName = displayName;
        this.description = description;
        this.blessingStat = blessingStat;
        this.curseStat = curseStat;
        this.blessingModifier = blessingModifier;
        this.curseModifier = curseModifier;
        this.worldFlag = worldFlag;
        this.preferredClasses = preferredClasses;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getBlessingStat() {
        return blessingStat;
    }

    public String getCurseStat() {
        return curseStat;
    }

    public double getBlessingModifier() {
        return blessingModifier;
    }

    public double getCurseModifier() {
        return curseModifier;
    }

    public String getWorldFlag() {
        return worldFlag;
    }

    public CharacterClass[] getPreferredClasses() {
        return preferredClasses;
    }

    /**
     * Checks if a character class is preferred by this deity.
     *
     * @param characterClass the character class to check
     * @return true if the class is preferred (2x encounter chance)
     */
    public boolean isPreferredClass(CharacterClass characterClass) {
        if (preferredClasses == null) {
            return false;
        }
        for (CharacterClass preferred : preferredClasses) {
            if (preferred == characterClass) {
                return true;
            }
        }
        return false;
    }
}
