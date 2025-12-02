package com.tatumgames.mikros.models;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a game promotion from the MIKROS Marketing API.
 */
public record GamePromotion(int gameId, String gameName, String description, String promotionUrl,
                            String promotionMessage, String imageUrl, Instant campaignStartDate,
                            Instant campaignEndDate, int frequencyDays) {
    /**
     * Creates a new GamePromotion.
     *
     * @param gameId            the unique game ID
     * @param gameName          the game title
     * @param description       short marketing summary
     * @param promotionUrl      Steam or MIKROS marketing link
     * @param promotionMessage  optional pre-written message
     * @param imageUrl          optional cover art URL
     * @param campaignStartDate UTC datetime - campaign start date
     * @param campaignEndDate   UTC datetime - campaign end date
     * @param frequencyDays     how often to post (every X days, provided by backend)
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
    @Override
    public int gameId() {
        return gameId;
    }

    /**
     * Gets the game name.
     *
     * @return the game name
     */
    @Override
    public String gameName() {
        return gameName;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return description;
    }

    /**
     * Gets the promotion URL.
     *
     * @return the promotion URL
     */
    @Override
    public String promotionUrl() {
        return promotionUrl;
    }

    /**
     * Gets the pre-written promotion message (if any).
     *
     * @return the promotion message, or null
     */
    @Override
    public String promotionMessage() {
        return promotionMessage;
    }

    /**
     * Gets the image URL (if any).
     *
     * @return the image URL, or null
     */
    @Override
    public String imageUrl() {
        return imageUrl;
    }

    /**
     * Gets the campaign start date.
     *
     * @return the campaign start date
     */
    @Override
    public Instant campaignStartDate() {
        return campaignStartDate;
    }

    /**
     * Gets the campaign end date.
     *
     * @return the campaign end date
     */
    @Override
    public Instant campaignEndDate() {
        return campaignEndDate;
    }

    /**
     * Gets the frequency in days (how often to post this promotion).
     *
     * @return the frequency in days
     */
    @Override
    public int frequencyDays() {
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

