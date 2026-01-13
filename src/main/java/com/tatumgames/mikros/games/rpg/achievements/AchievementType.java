package com.tatumgames.mikros.games.rpg.achievements;

/**
 * Enum defining first-to achievement types.
 * These achievements can only be claimed once per server.
 */
public enum AchievementType {
    FIRST_TO_LEVEL_20,
    FIRST_TO_LEVEL_30,
    FIRST_TO_LEVEL_50,
    FIRST_TO_KILL_100_BOSSES,
    FIRST_TO_KILL_10_SUPER_BOSSES,
    FIRST_TO_DIE_10_TIMES,
    FIRST_TO_REST_100_TIMES,
    FIRST_TO_DUEL_100_TIMES,
    FIRST_TO_1000_EXPLORES, // Song of Nilfheim
    FIRST_TO_TOP_DAMAGE_100_BOSSES, // Hero's Mark (100 normal OR 10 super)
    FIRST_NECROMANCER_250_BOSSES_10_RESURRECTIONS, // Gravebound Presence
    FIRST_TO_KILL_10_ELITES, // Elite Slayer
    FIRST_OATHBREAKER_10_CORRUPTION, // Reach 10 corruption without embracing
    FIRST_OATHBREAKER_REFUSE_ALL_GODS, // Refuse all gods and survive to level 15
    FIRST_OATHBREAKER_MAX_CORRUPTION_BOSS, // Defeat super boss at max corruption
    FIRST_OATHBREAKER_3_BACKLASHES // Trigger 3 backlash events and survive
}

