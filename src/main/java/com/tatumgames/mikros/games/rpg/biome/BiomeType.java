package com.tatumgames.mikros.games.rpg.biome;

/**
 * Enum representing the different biomes/zones in Nilfheim.
 * Players progress through biomes in a cycle after completing 10 explorations in each biome.
 */
public enum BiomeType {
    FROZEN_WASTES("Frozen Wastes", "❄️"),
    ANCIENT_RUINS("Ancient Ruins", "🏛️"),
    SHADOWED_FORESTS("Shadowed Forests", "🌲"),
    VOLCANIC_DEPTHS("Volcanic Depths", "🌋"),
    MYSTICAL_HEIGHTS("Mystical Heights", "⛰️"),
    TWISTED_REALMS("Twisted Realms", "🌀");

    private final String displayName;
    private final String emoji;

    BiomeType(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    /**
     * Gets a random biome type.
     *
     * @return a random biome
     */
    public static BiomeType getRandom() {
        BiomeType[] biomes = values();
        return biomes[(int) (Math.random() * biomes.length)];
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    /**
     * Gets the next biome in the cycle.
     *
     * @return the next biome type
     */
    public BiomeType getNext() {
        BiomeType[] biomes = values();
        int nextIndex = (this.ordinal() + 1) % biomes.length;
        return biomes[nextIndex];
    }
}
