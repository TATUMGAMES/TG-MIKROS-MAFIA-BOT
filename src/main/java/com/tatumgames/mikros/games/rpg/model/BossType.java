package com.tatumgames.mikros.games.rpg.model;

/** Enum representing boss types. Determines which classes get bonuses against this boss. */
public enum BossType {
  BEAST("Beast", "🐺"),
  GIANT("Giant", "👹"),
  UNDEAD("Undead", "💀"),
  SPIRIT("Spirit", "👻"),
  ELEMENTAL("Elemental", "⚡"),
  HUMANOID("Humanoid", "⚔️"),
  ELDRITCH("Eldritch", "🌌"),
  CONSTRUCT("Construct", "🤖"),
  DRAGON("Dragon", "🐉"),
  DEMON("Demon", "😈");

  private final String displayName;
  private final String emoji;

  BossType(String displayName, String emoji) {
    this.displayName = displayName;
    this.emoji = emoji;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getEmoji() {
    return emoji;
  }
}
