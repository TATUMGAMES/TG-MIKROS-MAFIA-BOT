package com.tatumgames.mikros.models;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a game promotion from the MIKROS Marketing API.
 */
public class GamePromotion {
    private final int gameId;
    private final String gameName;
    private final String description;
    private final String promotionUrl;
    private final String promotionMessage;
    private final String imageUrl;
    private final Instant campaignStartDate;
    private final Instant campaignEndDate;
    private final int frequencyDays;
    
    /**
     * Creates a new GamePromotion.
     * 
     * @param gameId the unique game ID
     * @param gameName the game title
     * @param description short marketing summary
     * @param promotionUrl Steam or MIKROS marketing link
     * @param promotionMessage optional pre-written message
     * @param imageUrl optional cover art URL
     * @param campaignStartDate UTC datetime - campaign start date
     * @param campaignEndDate UTC datetime - campaign end date
     * @param frequencyDays how often to post (every X days, provided by backend)
     */
    public GamePromotion(
            int gameId,
            String gameName,
            String description,
            String promotionUrl,
            String promotionMessage,
            String imageUrl,
            Instant campaignStartDate,
            Instant campaignEndDate,
            int frequencyDays
    ) {
        this.gameId = gameId;
        this.gameName = Objects.requireNonNull(gameName, "gameName cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.promotionUrl = Objects.requireNonNull(promotionUrl, "promotionUrl cannot be null");
        this.promotionMessage = promotionMessage; // Can be null
        this.imageUrl = imageUrl; // Can be null
        this.campaignStartDate = Objects.requireNonNull(campaignStartDate, "campaignStartDate cannot be null");
        this.campaignEndDate = Objects.requireNonNull(campaignEndDate, "campaignEndDate cannot be null");
        this.frequencyDays = frequencyDays;
    }
    
    /**
     * Gets the game ID.
     * 
     * @return the game ID
     */
    public int getGameId() {
        return gameId;
    }
    
    /**
     * Gets the game name.
     * 
     * @return the game name
     */
    public String getGameName() {
        return gameName;
    }
    
    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the promotion URL.
     * 
     * @return the promotion URL
     */
    public String getPromotionUrl() {
        return promotionUrl;
    }
    
    /**
     * Gets the pre-written promotion message (if any).
     * 
     * @return the promotion message, or null
     */
    public String getPromotionMessage() {
        return promotionMessage;
    }
    
    /**
     * Gets the image URL (if any).
     * 
     * @return the image URL, or null
     */
    public String getImageUrl() {
        return imageUrl;
    }
    
    /**
     * Gets the campaign start date.
     * 
     * @return the campaign start date
     */
    public Instant getCampaignStartDate() {
        return campaignStartDate;
    }
    
    /**
     * Gets the campaign end date.
     * 
     * @return the campaign end date
     */
    public Instant getCampaignEndDate() {
        return campaignEndDate;
    }
    
    /**
     * Gets the frequency in days (how often to post this promotion).
     * 
     * @return the frequency in days
     */
    public int getFrequencyDays() {
        return frequencyDays;
    }
    
    /**
     * Checks if this promotion is within its active campaign period.
     * Returns true if current time is after campaign start and before campaign end.
     * 
     * @return true if within campaign period, false otherwise
     */
    public boolean isWithinCampaignPeriod() {
        Instant now = Instant.now();
        return now.isAfter(campaignStartDate) && now.isBefore(campaignEndDate);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamePromotion that = (GamePromotion) o;
        return gameId == that.gameId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(gameId);
    }
    
    @Override
    public String toString() {
        return String.format(
                "GamePromotion{gameId=%d, gameName='%s', campaignStartDate=%s, campaignEndDate=%s, frequencyDays=%d}",
                gameId, gameName, campaignStartDate, campaignEndDate, frequencyDays
        );
    }
}

