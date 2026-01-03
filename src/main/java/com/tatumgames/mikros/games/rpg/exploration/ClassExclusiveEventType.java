package com.tatumgames.mikros.games.rpg.exploration;

import com.tatumgames.mikros.games.rpg.model.CharacterClass;

/**
 * Enum representing class-exclusive exploration events.
 * These events are discoverable by anyone (1-2% chance) but can only be fully resolved by specific classes.
 * Non-matching classes receive consolation rewards (minor XP, flavor text, story notes).
 */
public enum ClassExclusiveEventType {
    // Rogue events
    LOCKED_CHEST("🔐", "Locked Chest", CharacterClass.ROGUE, "Beneath the frost-covered rubble, you uncover something unusual…"),
    HIDDEN_PASSAGE("🕳️", "Hidden Passage", CharacterClass.ROGUE, "You notice a narrow opening in the ice that others might miss…"),

    // Warrior events
    CRUMBLING_OBSTACLE("🪨", "Crumbling Obstacle", CharacterClass.WARRIOR, "A massive barrier blocks your path, weakened by time…"),

    // Knight events
    ANCIENT_OATHSTONE("⚖️", "Ancient Oathstone", CharacterClass.KNIGHT, "An ancient stone monument stands before you, pulsing with protective energy…"),

    // Mage events
    ARCANE_SIGIL("📜", "Arcane Sigil", CharacterClass.MAGE, "Strange runes glow faintly on a weathered surface…"),
    MANA_RIFT("🌀", "Mana Rift", CharacterClass.MAGE, "The air shimmers with unstable magical energy…"),

    // Necromancer events
    MASS_GRAVE("🪦", "Mass Grave", CharacterClass.NECROMANCER, "You sense the presence of many souls resting here…"),
    LINGERING_SPIRIT("👁️", "Lingering Spirit", CharacterClass.NECROMANCER, "A spectral form drifts through the frozen mist…"),

    // Priest events
    DESECRATED_SHRINE("🕯️", "Desecrated Shrine", CharacterClass.PRIEST, "A once-sacred place has been tainted by darkness…"),
    LOST_PILGRIM("🕊️", "Lost Pilgrim", CharacterClass.PRIEST, "A weary traveler calls out for help in the distance…");

    private final String emoji;
    private final String displayName;
    private final CharacterClass requiredClass;
    private final String discoveryNarrative;

    ClassExclusiveEventType(String emoji, String displayName, CharacterClass requiredClass, String discoveryNarrative) {
        this.emoji = emoji;
        this.displayName = displayName;
        this.requiredClass = requiredClass;
        this.discoveryNarrative = discoveryNarrative;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CharacterClass getRequiredClass() {
        return requiredClass;
    }

    public String getDiscoveryNarrative() {
        return discoveryNarrative;
    }
}

