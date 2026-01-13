package com.tatumgames.mikros.games.rpg.model;

/**
 * Enum representing elite enemy traits.
 * Each elite enemy can have 1-2 traits that modify their combat behavior.
 */
public enum EliteTrait {
    FROST_HARDENED(
            "Frost-Hardened",
            "The enemy's hide is encased in ancient ice, deflecting physical strikes.",
            TraitEffectType.DAMAGE_REDUCTION_STR
    ),
    SAVAGE_PACK_LEADER(
            "Savage Pack Leader",
            "This creature leads with overwhelming ferocity, striking first with devastating force.",
            TraitEffectType.FIRST_STRIKE_BOOST
    ),
    RUNE_TOUCHED(
            "Rune-Touched",
            "Ancient runes shimmer across the enemy's form, weakening magical attacks.",
            TraitEffectType.RESISTANCE_INT
    ),
    BLOOD_FRENZIED(
            "Blood Frenzied",
            "As wounds mount, the enemy's rage intensifies, dealing more damage.",
            TraitEffectType.DAMAGE_BOOST_LOW_HP
    ),
    UNSTABLE_ESSENCE(
            "Unstable Essence",
            "The enemy's very essence is unstable, exploding violently upon death.",
            TraitEffectType.DEATH_EXPLOSION
    ),
    SHADOW_BOUND(
            "Shadow-Bound",
            "Wrapped in shadows, the enemy is harder to hit with precision strikes.",
            TraitEffectType.RESISTANCE_AGI
    ),
    VOID_WHISPERER(
            "Void Whisperer",
            "The enemy channels void energy, strengthening magical attacks while resisting them.",
            TraitEffectType.MAGICAL_AMPLIFICATION
    ),
    IRONCLAD(
            "Ironclad",
            "Heavy armor and reinforced hide make physical attacks nearly useless.",
            TraitEffectType.DAMAGE_REDUCTION_STR_HEAVY
    ),
    CURSED_BLOOD(
            "Cursed Blood",
            "Dark magic flows through the enemy's veins, empowering attacks but leaving them vulnerable.",
            TraitEffectType.CURSED_POWER
    ),
    ANCIENT_WARD(
            "Ancient Ward",
            "An ancient protective ward grants resistance to all forms of attack.",
            TraitEffectType.UNIVERSAL_RESISTANCE
    );

    private final String displayName;
    private final String description;
    private final TraitEffectType effectType;

    EliteTrait(String displayName, String description, TraitEffectType effectType) {
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public TraitEffectType getEffectType() {
        return effectType;
    }

    /**
     * Enum representing the type of effect a trait has.
     */
    public enum TraitEffectType {
        DAMAGE_REDUCTION_STR,        // Takes 15% less damage from STR
        DAMAGE_REDUCTION_STR_HEAVY,   // Takes 20% less damage from STR
        RESISTANCE_INT,               // INT attacks 10% weaker
        RESISTANCE_AGI,               // AGI attacks 10% weaker
        DAMAGE_BOOST_LOW_HP,          // Gains +10% damage when below 50% HP
        FIRST_STRIKE_BOOST,           // First attack deals +50% damage
        DEATH_EXPLOSION,              // On death, explodes (minor unavoidable damage)
        MAGICAL_AMPLIFICATION,        // INT attacks 15% stronger, but also resists them
        CURSED_POWER,                 // Deals 5% more damage, but takes 5% more damage
        UNIVERSAL_RESISTANCE           // 15% resistance to all stat types
    }
}
