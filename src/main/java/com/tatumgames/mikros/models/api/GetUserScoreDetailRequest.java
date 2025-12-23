package com.tatumgames.mikros.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request model for /getUserScoreDetails API endpoint.
 */
public class GetUserScoreDetailRequest {
    @JsonProperty("apiKeyType")
    private String apiKeyType;

    @JsonProperty("discordUserId")
    private String discordUserId;

    @JsonProperty("discordUsernames")
    private List<String> discordUsernames;

    /**
     * Gets the API key type (dev or prod).
     *
     * @return the API key type
     */
    public String getApiKeyType() {
        return apiKeyType;
    }

    /**
     * Sets the API key type (dev or prod).
     *
     * @param apiKeyType the API key type
     */
    public void setApiKeyType(String apiKeyType) {
        this.apiKeyType = apiKeyType;
    }

    /**
     * Gets the Discord user ID (optional).
     *
     * @return the Discord user ID, or null if not set
     */
    public String getDiscordUserId() {
        return discordUserId;
    }

    /**
     * Sets the Discord user ID (optional).
     *
     * @param discordUserId the Discord user ID
     */
    public void setDiscordUserId(String discordUserId) {
        this.discordUserId = discordUserId;
    }

    /**
     * Gets the list of Discord usernames to lookup.
     *
     * @return the list of Discord usernames
     */
    public List<String> getDiscordUsernames() {
        return discordUsernames;
    }

    /**
     * Sets the list of Discord usernames to lookup.
     *
     * @param discordUsernames the list of Discord usernames
     */
    public void setDiscordUsernames(List<String> discordUsernames) {
        this.discordUsernames = discordUsernames;
    }
}

