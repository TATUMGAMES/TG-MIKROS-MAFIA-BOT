package com.tatumgames.mikros.games.rpg.exploration;

import com.tatumgames.mikros.games.rpg.model.CharacterClass;

/**
 * Enum representing wandering figures that can be encountered during exploration.
 * These are ultra-rare encounters (0.5% chance) that provide unique rewards.
 */
public enum WanderingFigureType {
    FROSTBOUND_SAGE(
            "The Frostbound Sage",
            "An ancient scholar wrapped in frozen robes, eyes glowing with Astral energy. A survivor from before the Shattering, keeper of the Grand Library's secrets.",
            10, // Level requirement
            CharacterClass.MAGE, // Class preference
            "The Sage shares a fragment of ancient wisdom, restoring your resolve.",
            "The Sage's teachings resonate with you, enhancing your understanding.",
            "The Sage nods knowingly and fades into the blizzard, leaving you with a sense of purpose."
    ),
    ANCIENT_WANDERER(
            "The Ancient Wanderer",
            "A ghostly figure in tattered Frostborne armor, carrying a broken sword. A fallen warrior from the Shattering, bound to wander Nilfheim.",
            5, // Level requirement
            CharacterClass.WARRIOR, // Class preference
            "The Wanderer drops a glowing shard before vanishing.",
            "The Wanderer's spirit lends you strength for one battle.",
            "The Wanderer salutes you and continues their eternal patrol."
    ),
    MYSTERIOUS_MERCHANT(
            "The Mysterious Merchant",
            "A hooded figure with a phantom caravan, wares shimmering with Void energy. A trader who moves between the Mortal and Arcane Veils.",
            1, // Level requirement (any level)
            null, // No class preference
            "The Merchant gifts you a rare reagent before disappearing.",
            "The Merchant's caravan fades into the Spirit Veil, leaving only whispers.",
            "The Merchant offers a trade, then vanishes into the mist."
    ),
    STORMWARDEN_APPRENTICE(
            "The Stormwarden Apprentice",
            "A young Stormwarden practicing Gale techniques, crackling with wind energy. Training to master the Gale element, seeking worthy heroes.",
            1, // Level requirement (any level)
            CharacterClass.ROGUE, // Class preference
            "The Apprentice shares a fragment of Gale energy.",
            "The Apprentice teaches you a wind-dancing technique.",
            "The Apprentice bows and vanishes in a gust of wind."
    );

    private final String displayName;
    private final String description;
    private final int minLevel;
    private final CharacterClass preferredClass;
    private final String outcome1Narrative;
    private final String outcome2Narrative;
    private final String outcome3Narrative;

    WanderingFigureType(String displayName, String description, int minLevel,
                       CharacterClass preferredClass, String outcome1Narrative,
                       String outcome2Narrative, String outcome3Narrative) {
        this.displayName = displayName;
        this.description = description;
        this.minLevel = minLevel;
        this.preferredClass = preferredClass;
        this.outcome1Narrative = outcome1Narrative;
        this.outcome2Narrative = outcome2Narrative;
        this.outcome3Narrative = outcome3Narrative;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public CharacterClass getPreferredClass() {
        return preferredClass;
    }

    public String getOutcome1Narrative() {
        return outcome1Narrative;
    }

    public String getOutcome2Narrative() {
        return outcome2Narrative;
    }

    public String getOutcome3Narrative() {
        return outcome3Narrative;
    }
}

