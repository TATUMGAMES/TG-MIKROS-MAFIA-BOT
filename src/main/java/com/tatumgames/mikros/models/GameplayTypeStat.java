package com.tatumgames.mikros.models;

/**
 * Represents statistics for a gameplay type (casual, competitive, hyper-casual, etc.).
 */
public record GameplayTypeStat(String gameplayType, double growthPercentage, long playerCount, double marketShare,
                               int rank) {
    /**
     * Creates a new GameplayTypeStat.
     *
     * @param gameplayType     the gameplay type name
     * @param growthPercentage the growth percentage
     * @param playerCount      the number of active players
     * @param marketShare      the percentage of total market
     * @param rank             the ranking position
     */
    public GameplayTypeStat {
    }
}

