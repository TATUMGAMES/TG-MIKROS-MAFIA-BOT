package com.tatumgames.mikros.games.rpg.exploration;

/**
 * Enum representing Blood Relics - powerful artifacts forged from failed gods or dead titans.
 * Each relic grants power at a cost.
 */
public enum RelicType {
    /**
     * Blood-Forged Blade - Increases boss damage but reduces max HP.
     * Effect: +10% boss damage, -5% max HP
     */
    BLOOD_FORGED_BLADE(
            "Blood-Forged Blade",
            "A weapon embedded in ice hums with life. The blade seems to pulse with a heartbeat. Removing it will change you.",
            "BLOOD_BLADE_BEARER",
            0.10, // +10% boss damage
            -0.05, // -5% max HP
            "BOSS_DAMAGE",
            "MAX_HP"
    ),

    /**
     * Frozen Crown - Increases AGI defense but slows charge refresh.
     * Effect: +10% AGI defense, charge refresh +2 hours slower
     */
    FROZEN_CROWN(
            "Frozen Crown",
            "A crown of pure ice, shimmering with ancient power. The cold seeps into your bones as you reach for it.",
            "CROWNED_IN_ICE",
            0.10, // +10% AGI defense
            -0.0, // Charge refresh penalty (handled separately)
            "AGI_DEFENSE",
            "CHARGE_REFRESH"
    ),

    /**
     * Soul Anchor - Increases curse resistance but reduces XP gain.
     * Effect: +15% curse resistance, -10% XP gain
     */
    SOUL_ANCHOR(
            "Soul Anchor",
            "An anchor forged from the souls of the fallen. It radiates protection but weighs heavily on your spirit.",
            "ANCHORED_SOUL",
            0.15, // +15% curse resistance
            -0.10, // -10% XP gain
            "CURSE_RESISTANCE",
            "XP_GAIN"
    );

    private final String displayName;
    private final String description;
    private final String worldFlag;
    private final double positiveModifier;
    private final double negativeModifier;
    private final String positiveStat;
    private final String negativeStat;

    RelicType(String displayName, String description, String worldFlag,
              double positiveModifier, double negativeModifier,
              String positiveStat, String negativeStat) {
        this.displayName = displayName;
        this.description = description;
        this.worldFlag = worldFlag;
        this.positiveModifier = positiveModifier;
        this.negativeModifier = negativeModifier;
        this.positiveStat = positiveStat;
        this.negativeStat = negativeStat;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getWorldFlag() {
        return worldFlag;
    }

    public double getPositiveModifier() {
        return positiveModifier;
    }

    public double getNegativeModifier() {
        return negativeModifier;
    }

    public String getPositiveStat() {
        return positiveStat;
    }

    public String getNegativeStat() {
        return negativeStat;
    }
}
