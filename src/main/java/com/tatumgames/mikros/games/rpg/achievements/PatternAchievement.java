package com.tatumgames.mikros.games.rpg.achievements;

/**
 * Enum for secret pattern achievements.
 * These are unlocked by performing the same action 3 times in a row, multiple times.
 */
public enum PatternAchievement {
    EXPLORER_PATTERN, // 15 sequences of 3x explore in a row
    TRAINER_PATTERN,  // 15 sequences of 3x train in a row
    RESTER_PATTERN,   // 10 sequences of 3x rest in a row
    BATTLE_PATTERN    // 15 sequences of 3x battle in a row
}

