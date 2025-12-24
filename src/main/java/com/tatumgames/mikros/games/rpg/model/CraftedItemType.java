package com.tatumgames.mikros.games.rpg.model;

/**
 * Enum representing crafted items that grant permanent stat bonuses.
 * Each item requires essences and a catalyst to craft.
 */
public enum CraftedItemType {
    EMBER_INFUSION("Ember Infusion", "🔥", EssenceType.EMBER_SHARD, 5, CatalystType.ANCIENT_VIAL, 1, "STR", 1),
    GALE_ETCHING("Gale Etching", "🌪️", EssenceType.GALE_FRAGMENT, 5, CatalystType.ANCIENT_VIAL, 1, "AGI", 1),
    MIND_SIGIL("Mind Sigil", "🔮", EssenceType.MIND_CRYSTAL, 4, CatalystType.RUNIC_BINDING, 1, "INT", 1),
    CHARM_OF_FORTUNE("Charm of Fortune", "🍀", EssenceType.FATE_CLOVER, 4, CatalystType.RUNIC_BINDING, 1, "LUCK", 1),
    VITAL_RUNE("Vital Rune", "🩸", EssenceType.VITAL_ASH, 3, CatalystType.MONSTER_CORE, 1, "HP", 5);

    private final String displayName;
    private final String emoji;
    private final EssenceType requiredEssence;
    private final int essenceCount;
    private final CatalystType requiredCatalyst;
    private final int catalystCount;
    private final String statName;
    private final int statBonus;

    CraftedItemType(String displayName, String emoji, EssenceType requiredEssence, int essenceCount,
                    CatalystType requiredCatalyst, int catalystCount, String statName, int statBonus) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.requiredEssence = requiredEssence;
        this.essenceCount = essenceCount;
        this.requiredCatalyst = requiredCatalyst;
        this.catalystCount = catalystCount;
        this.statName = statName;
        this.statBonus = statBonus;
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

    public String getStatName() {
        return statName;
    }

    public int getStatBonus() {
        return statBonus;
    }
}

