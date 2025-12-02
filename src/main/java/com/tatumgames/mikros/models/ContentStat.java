package com.tatumgames.mikros.models;

/**
 * Represents statistics for in-game content or content types.
 */
public record ContentStat(String contentName, String contentType, double growthPercentage, long usageCount, int rank) {
    /**
     * Creates a new ContentStat.
     *
     * @param contentName      the name of the content
     * @param contentType      the type of content (e.g., "Boss", "Level", "Character")
     * @param growthPercentage the growth percentage
     * @param usageCount       the number of times used/played
     * @param rank             the ranking position
     */
    public ContentStat {
    }
}

