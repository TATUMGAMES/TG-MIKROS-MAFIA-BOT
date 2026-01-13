package com.tatumgames.mikros.games.rpg.exploration;

/**
 * Enum representing stat-gated world interactions during exploration.
 * These encounters require minimum stat thresholds and provide meaningful success/failure outcomes.
 */
public enum StatInteractionType {
    // STR-based interactions
    FROSTBOUND_BOULDER(
            "Frostbound Boulder",
            "A massive boulder blocks the frozen pass. Frost cracks ripple through it.",
            "STR",
            10
    ),
    FROZEN_GATE(
            "Frozen Gate",
            "An ancient gate sealed with ice. Runes of power glow faintly beneath the frost.",
            "STR",
            12
    ),

    // AGI-based interactions
    COLLAPSING_ICE_BRIDGE(
            "Collapsing Ice Bridge",
            "The ice beneath your feet begins to splinter. A chasm opens below.",
            "AGI",
            10
    ),
    NARROW_CREVICE(
            "Narrow Crevice",
            "A narrow opening in the ice wall. Only the swift can slip through.",
            "AGI",
            15
    ),

    // INT-based interactions
    WHISPERING_BARRIER(
            "Whispering Barrier",
            "A translucent barrier hums with layered runes. They shift when you focus.",
            "INT",
            12
    ),
    ANCIENT_LIBRARY(
            "Ancient Library",
            "Ruins of a library, texts written in forgotten tongues. Knowledge waits for those who can decipher it.",
            "INT",
            18
    ),

    // LUCK-based interactions
    BURIED_CACHE(
            "Buried Cache",
            "Something glints beneath the snow… or maybe it's nothing.",
            "LUCK",
            10
    ),
    MYSTERIOUS_GLIMMER(
            "Mysterious Glimmer",
            "A faint light catches your eye. Fortune favors the bold, but is it real?",
            "LUCK",
            15
    ),

    // HP-based interactions (survivability)
    BLIZZARD_PASSAGE(
            "Blizzard Passage",
            "A howling blizzard blocks your path. Only the hardy can endure.",
            "HP",
            100
    ),
    TOXIC_MIASMA(
            "Toxic Miasma",
            "A cloud of poisonous mist drifts across the path. Your vitality will be tested.",
            "HP",
            120
    );

    private final String displayName;
    private final String description;
    private final String requiredStat;
    private final int baseRequirement;

    StatInteractionType(String displayName, String description, String requiredStat, int baseRequirement) {
        this.displayName = displayName;
        this.description = description;
        this.requiredStat = requiredStat;
        this.baseRequirement = baseRequirement;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getRequiredStat() {
        return requiredStat;
    }

    public int getBaseRequirement() {
        return baseRequirement;
    }

    /**
     * Calculates the required stat value based on character level.
     * Formula: baseRequirement + (level × 1.5)
     *
     * @param level the character's level
     * @return the required stat value
     */
    public int getRequiredStatValue(int level) {
        return (int) (baseRequirement + (level * 1.5));
    }
}
