package com.tatumgames.mikros.models;

/**
 * Represents statistics for in-game content or content types.
 */
public class ContentStat {
    private final String contentName;
    private final String contentType;
    private final double growthPercentage;
    private final long usageCount;
    private final int rank;
    
    /**
     * Creates a new ContentStat.
     * 
     * @param contentName the name of the content
     * @param contentType the type of content (e.g., "Boss", "Level", "Character")
     * @param growthPercentage the growth percentage
     * @param usageCount the number of times used/played
     * @param rank the ranking position
     */
    public ContentStat(String contentName, String contentType, double growthPercentage, 
                       long usageCount, int rank) {
        this.contentName = contentName;
        this.contentType = contentType;
        this.growthPercentage = growthPercentage;
        this.usageCount = usageCount;
        this.rank = rank;
    }
    
    public String getContentName() {
        return contentName;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public double getGrowthPercentage() {
        return growthPercentage;
    }
    
    public long getUsageCount() {
        return usageCount;
    }
    
    public int getRank() {
        return rank;
    }
}

