package com.tatumgames.mikros.models;

/**
 * Represents statistics for a game genre.
 */
public record GenreStat(String genreName, double growthPercentage, long playerCount, int rank) {
    /**
     * Creates a new GenreStat.
     *
     * @param genreName        the name of the genre
     * @param growthPercentage the growth percentage (can be negative)
     * @param playerCount      the number of active players
     * @param rank             the ranking position (1 = top)
     */
    public GenreStat {
    }
}

