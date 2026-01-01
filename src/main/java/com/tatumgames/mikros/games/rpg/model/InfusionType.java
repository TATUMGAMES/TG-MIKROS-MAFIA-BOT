package com.tatumgames.mikros.games.rpg.model;

/**
 * Enum representing consumable infusions that provide temporary single-use effects.
 * Infusions are crafted items that auto-consume on the next action or expire after 24 hours.
 */
public enum InfusionType {
    FROST_CLARITY(
            "Infusion of Frost Clarity",
            "❄️",
            EssenceType.MIND_CRYSTAL,
            2,
            CatalystType.FROZEN_REAGENT,
            1,
            "A crystal infused with Frost element energy, sharpening your mind. Next action grants +10% XP."
    ),
    GALE_FORTUNE(
            "Infusion of Gale Fortune",
            "🌪️",
            EssenceType.FATE_CLOVER,
            2,
            CatalystType.ANCIENT_VIAL,
            1,
            "Gale winds carry fortune to you, ensuring your next discovery. Next drop is guaranteed to be an essence."
    ),
    EMBER_ENDURANCE(
            "Infusion of Ember Endurance",
            "🔥",
            EssenceType.VITAL_ASH,
            2,
            CatalystType.MONSTER_CORE,
            1,
            "Ember's warmth shields you from the worst of defeat. Next defeat damage is reduced by 20%."
    ),
    ASTRAL_INSIGHT(
            "Infusion of Astral Insight",
            "🔮",
            EssenceType.MIND_CRYSTAL,
            2,
            CatalystType.RUNIC_BINDING,
            1,
            "Astral energy reveals the secrets of preservation, making your crafting more efficient. Next craft has +5% catalyst preservation chance."
    ),
    VOID_PRECISION(
            "Infusion of Void Precision",
            "⚫",
            EssenceType.GALE_FRAGMENT,
            2,
            CatalystType.FROZEN_REAGENT,
            1,
            "Void energy sharpens your strikes, finding weaknesses others miss. Next battle deals +8% damage."
    ),
    ELEMENTAL_CONVERGENCE(
            "Infusion of Elemental Convergence",
            "✨",
            null, // Requires 1x of each essence type (5 total)
            0, // Special handling
            CatalystType.RUNIC_BINDING,
            1,
            "All Eight Elements converge in this rare infusion, granting immense power. Next action grants +15% XP AND guaranteed drop."
    );

    private final String displayName;
    private final String emoji;
    private final EssenceType requiredEssence;
    private final int essenceCount;
    private final CatalystType requiredCatalyst;
    private final int catalystCount;
    private final String description;

    InfusionType(String displayName, String emoji, EssenceType requiredEssence, int essenceCount,
                 CatalystType requiredCatalyst, int catalystCount, String description) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.requiredEssence = requiredEssence;
        this.essenceCount = essenceCount;
        this.requiredCatalyst = requiredCatalyst;
        this.catalystCount = catalystCount;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public EssenceType getRequiredEssence() {
        return requiredEssence;
    }

    public int getEssenceCount() {
        return essenceCount;
    }

    public CatalystType getRequiredCatalyst() {
        return requiredCatalyst;
    }

    public int getCatalystCount() {
        return catalystCount;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if this is the Elemental Convergence infusion (requires special handling).
     *
     * @return true if this is Elemental Convergence
     */
    public boolean isElementalConvergence() {
        return this == ELEMENTAL_CONVERGENCE;
    }
}

