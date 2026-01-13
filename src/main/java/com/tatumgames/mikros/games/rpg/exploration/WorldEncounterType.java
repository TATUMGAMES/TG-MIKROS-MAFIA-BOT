package com.tatumgames.mikros.games.rpg.exploration;

/**
 * Enum representing types of irrevocable world encounters.
 * These are ultra-rare encounters (≤1% chance) that present permanent choices.
 */
public enum WorldEncounterType {
    /**
     * Stonebound Divinities - Ancient gods bound in stone offer blessings.
     */
    STONEBOUND_DIVINITY,

    /**
     * Disguised God Test - Gods walk among mortals in disguise, testing character.
     */
    DISGUISED_GOD_TEST,

    /**
     * Oath of Null - The anti-god path, refusing divine chains.
     */
    OATH_OF_NULL,

    /**
     * Blood Relic - Powerful relics forged from failed gods or dead titans.
     */
    BLOOD_RELIC
}
