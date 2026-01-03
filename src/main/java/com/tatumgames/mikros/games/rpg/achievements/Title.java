package com.tatumgames.mikros.games.rpg.achievements;

/**
 * Enum defining all available titles in the RPG system.
 * Titles are categorized by type: Progress, Feat, First-To, Pattern, and Community.
 */
public enum Title {
    // Progress Titles (Level-based, cosmetic only)
    INITIATE_OF_NILFHEIM("Initiate of Nilfheim", 5, TitleType.PROGRESS, 0.0),
    WARDEN_OF_THE_COLD("Warden of the Cold", 20, TitleType.PROGRESS, 0.0),
    CHAMPION_OF_NILFHEIM("Champion of Nilfheim", 30, TitleType.PROGRESS, 0.0),
    LEGEND_OF_THE_FROZEN_REALM("Legend of the Frozen Realm", 50, TitleType.PROGRESS, 0.0),

    // Feat Titles (Optional tiny bonuses ≤2-3%)
    THE_UNBROKEN("The Unbroken", 0, TitleType.FEAT, 0.02), // 100 battles without dying (2% XP bonus)
    GRAVECALLER("Gravecaller", 0, TitleType.FEAT, 0.02), // 50 resurrections performed (2% drop chance bonus)
    WANDERER("Wanderer", 0, TitleType.FEAT, 0.02), // 500 explores (2% exploration drop bonus)
    THE_DISCIPLINED("The Disciplined", 0, TitleType.FEAT, 0.02), // Trainer pattern (2% training stat bonus)
    THE_EXPLORER("The Explorer", 0, TitleType.FEAT, 0.02), // Explorer pattern (2% exploration XP bonus)
    THE_WARRIOR("The Warrior", 0, TitleType.FEAT, 0.02), // Battler pattern (2% battle damage bonus)
    THE_RESTFUL("The Restful", 0, TitleType.FEAT, 0.02), // Rester pattern (2% rest HP recovery bonus)

    // First-To Titles (Myth tier, cosmetic + aura unlock)
    FIRST_BLOOD_OF_NILFHEIM("First Blood of Nilfheim", 0, TitleType.FIRST_TO, 0.0), // First boss kill ever
    THE_ONE_WHO_FELL_AND_RETURNED("The One Who Fell and Returned", 0, TitleType.FIRST_TO, 0.0), // First to die 10 times
    VOICE_OF_THE_FROZEN_WORLD("Voice of the Frozen World", 0, TitleType.FIRST_TO, 0.0), // Song of Nilfheim holder
    THE_MARKED_HERO("The Marked Hero", 0, TitleType.FIRST_TO, 0.0), // Hero's Mark holder
    GRAVEBOUND("Gravebound", 0, TitleType.FIRST_TO, 0.0), // Gravebound Presence holder

    // Community Titles
    BEARER_OF_BURDENS("Bearer of Burdens", 0, TitleType.COMMUNITY, 0.0), // 10+ charges donated (cosmetic)

    // Failure-Based Titles (Cursed Worlds)
    HOPE_UNBROKEN("Hope Unbroken", 0, TitleType.FEAT, 0.0), // Participated in 5 cursed boss fights
    CURSEWALKER("Cursewalker", 0, TitleType.FEAT, 0.0), // Acted during both Minor + Major curse simultaneously
    LIGHTBEARER("Lightbearer", 0, TitleType.FEAT, 0.02), // Priest: 10 resurrections during cursed worlds (2% resurrection XP bonus)
    BOUND_TO_DEATH("Bound to Death", 0, TitleType.FEAT, 0.0), // Necromancer: Active during March of the Dead

    // Irrevocable World Encounter Titles
    UNBOUND("Unbound", 0, TitleType.FEAT, 0.0), // Oath of Null path
    STONE_WOLF_CHOSEN("Stone Wolf's Chosen", 0, TitleType.FEAT, 0.0), // Vaelgor blessing
    ICEWALKER("Icewalker", 0, TitleType.FEAT, 0.0), // AGI interaction mastery
    RUNE_SEER("Rune-Seer", 0, TitleType.FEAT, 0.0), // INT interaction mastery
    FORTUNES_FAVORITE("Fortune's Favorite", 0, TitleType.FEAT, 0.0), // LUCK interaction mastery

    // Oathbreaker Titles
    THE_UNSWORN("The Unsworn", 0, TitleType.FEAT, 0.02), // Reach 10 corruption without embracing (2% damage bonus)
    MARKED_BY_SILENCE("Marked by Silence", 0, TitleType.FEAT, 0.02), // Refuse all gods and survive to level 15 (2% XP bonus)
    BREAKER_OF_CHAINS("Breaker of Chains", 0, TitleType.FEAT, 0.02), // Defeat super boss at max corruption (2% boss damage bonus)
    THE_CONTESTED("The Contested", 0, TitleType.FEAT, 0.02); // Trigger 3 backlash events and survive (2% corruption bonus)

    private final String displayName;
    private final int requiredLevel;
    private final TitleType type;
    private final double bonus; // Bonus multiplier (0.02 = 2%)

    Title(String displayName, int requiredLevel, TitleType type, double bonus) {
        this.displayName = displayName;
        this.requiredLevel = requiredLevel;
        this.type = type;
        this.bonus = bonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public TitleType getType() {
        return type;
    }

    public double getBonus() {
        return bonus;
    }

    /**
     * Title type categories.
     */
    public enum TitleType {
        PROGRESS,    // Level-based, cosmetic only
        FEAT,        // Achievement-based, optional tiny bonuses
        FIRST_TO,    // Myth tier, cosmetic + aura unlock
        COMMUNITY    // Community contribution, cosmetic
    }
}

