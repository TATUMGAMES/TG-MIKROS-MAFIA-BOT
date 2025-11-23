package com.tatumgames.mikros.models;

/**
 * Represents statistics for a gameplay type (casual, competitive, hyper-casual, etc.).
 */
public class GameplayTypeStat {
    private final String gameplayType;
    private final double growthPercentage;
    private final long playerCount;
    private final double marketShare;
    private final int rank;
    
    /**
     * Creates a new GameplayTypeStat.
     * 
     * @param gameplayType the gameplay type name
     * @param growthPercentage the growth percentage
     * @param playerCount the number of active players
     * @param marketShare the percentage of total market
     * @param rank the ranking position
     */
    public GameplayTypeStat(String gameplayType, double growthPercentage, 
                            long playerCount, double marketShare, int rank) {
        this.gameplayType = gameplayType;
        this.growthPercentage = growthPercentage;
        this.playerCount = playerCount;
        this.marketShare = marketShare;
        this.rank = rank;
    }
    
    public String getGameplayType() {
        return gameplayType;
    }
    
    public double getGrowthPercentage() {
        return growthPercentage;
    }
    
    public long getPlayerCount() {
        return playerCount;
    }
    
    public double getMarketShare() {
        return marketShare;
    }
    
    public int getRank() {
        return rank;
    }
}

