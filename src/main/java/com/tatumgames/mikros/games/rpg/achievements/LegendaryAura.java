package com.tatumgames.mikros.games.rpg.achievements;

/**
 * Enum for legendary auras - ultra-rare server-defining effects.
 */
public enum LegendaryAura {
    /**
     * Song of Nilfheim
     * Acquisition: First to 1,000 explores (max 2 holders)
     * Effect: +5% damage to all participants in boss battles
     */
    SONG_OF_NILFHEIM,

    /**
     * Hero's Mark
     * Acquisition: First to be top damage dealer on 100 normal bosses OR 10 super bosses (max 1 holder)
     * Effect: Bosses deal +10% damage to you, your damage always shown publicly
     */
    HEROS_MARK,

    /**
     * Gravebound Presence
     * Acquisition: First Necromancer to kill 250 bosses AND be resurrected 10 times (max 1 Necromancer)
     * Effect: Boss HP +10%, +1 guaranteed essence, +10% catalyst chance, Raise Fallen passive
     */
    GRAVEBOUND_PRESENCE
}

