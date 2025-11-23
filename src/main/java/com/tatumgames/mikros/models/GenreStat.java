package com.tatumgames.mikros.models;

/**
 * Represents statistics for a game genre.
 */
public class GenreStat {
    private final String genreName;
    private final double growthPercentage;
    private final long playerCount;
    private final int rank;
    
    /**
     * Creates a new GenreStat.
     * 
     * @param genreName the name of the genre
     * @param growthPercentage the growth percentage (can be negative)
     * @param playerCount the number of active players
     * @param rank the ranking position (1 = top)
     */
    public GenreStat(String genreName, double growthPercentage, long playerCount, int rank) {
        this.genreName = genreName;
        this.growthPercentage = growthPercentage;
        this.playerCount = playerCount;
        this.rank = rank;
    }
    
    public String getGenreName() {
        return genreName;
    }
    
    public double getGrowthPercentage() {
        return growthPercentage;
    }
    
    public long getPlayerCount() {
        return playerCount;
    }
    
    public int getRank() {
        return rank;
    }
}

