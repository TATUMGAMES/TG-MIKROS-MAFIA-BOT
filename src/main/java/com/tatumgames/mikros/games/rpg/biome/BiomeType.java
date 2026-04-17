package com.tatumgames.mikros.games.rpg.biome;

/**
 * Enum representing the different biomes/zones in Nilfheim. Players progress through biomes in a
 * cycle after completing 10 explorations in each biome.
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

  /**
   * Returns the sensory narrative when a player advances into this biome. Used to add destination-
   * specific flavor to biome advancement messages.
   *
   * @return flavor text describing the transition into this biome (e.g. "the air grows heavy with
   *     heat... molten rock glows in the distance")
   */
  public String getAdvancementNarrative() {
    return switch (this) {
      case FROZEN_WASTES -> "a biting cold cuts through the air... ice and snow stretch before you";
      case ANCIENT_RUINS ->
          "crumbling stone and forgotten runes emerge... history echoes around you";
      case SHADOWED_FORESTS -> "ancient trees rise around you... shadows deepen between the boughs";
      case VOLCANIC_DEPTHS -> "the air grows heavy with heat... molten rock glows in the distance";
      case MYSTICAL_HEIGHTS ->
          "the path climbs into mist and jagged peaks... the arcane hums in the air";
      case TWISTED_REALMS ->
          "reality bends and warps around you... portals flicker at the edges of vision";
    };
  }
}
