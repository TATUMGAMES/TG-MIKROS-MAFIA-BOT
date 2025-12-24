package com.tatumgames.mikros.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response model for /getUserScoreDetails API endpoint.
 */
public class GetUserScoreDetailResponse {
    @JsonProperty("status")
    private Status status;

    @JsonProperty("data")
    private List<UserScore> data;

    /**
     * Gets the status of the response.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the response.
     *
     * @param status the status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Gets the list of user scores.
     * Note: API returns data as a direct array, not wrapped in a Data object.
     *
     * @return the list of user scores
     */
    public List<UserScore> getData() {
        return data;
    }

    /**
     * Sets the list of user scores.
     *
     * @param data the list of user scores
     */
    public void setData(List<UserScore> data) {
        this.data = data;
    }

    /**
     * Gets the list of user scores (convenience method for backward compatibility).
     * This method provides access to scores through the same interface as before.
     *
     * @return the list of user scores, or empty list if data is null
     */
    public List<UserScore> getScores() {
        return data != null ? data : List.of();
    }

    /**
     * Status information for the API response.
     */
    public static class Status {
        @JsonProperty("statusCode")
        private int statusCode;

        @JsonProperty("statusMessage")
        private String statusMessage;

        /**
         * Gets the status code.
         *
         * @return the status code
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * Sets the status code.
         *
         * @param statusCode the status code
         */
        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        /**
         * Gets the status message.
         *
         * @return the status message
         */
        public String getStatusMessage() {
            return statusMessage;
        }

        /**
         * Sets the status message.
         *
         * @param statusMessage the status message
         */
        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }
    }

    /**
     * User score information from the API.
     * Note: Actual API only returns username and reputationScore.
     */
    public static class UserScore {
        @JsonProperty("username")
        private String username;

        @JsonProperty("reputationScore")
        private int reputationScore;

        /**
         * Gets the username (Discord username or user ID).
         *
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        /**
         * Sets the username.
         *
         * @param username the username
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * Gets the reputation score.
         *
         * @return the reputation score
         */
        public int getReputationScore() {
            return reputationScore;
        }

        /**
         * Sets the reputation score.
         *
         * @param reputationScore the reputation score
         */
        public void setReputationScore(int reputationScore) {
            this.reputationScore = reputationScore;
        }

        /**
         * Gets the Discord username (alias for getUsername for backward compatibility).
         *
         * @return the username
         */
        public String getDiscordUsername() {
            return username;
        }
    }
}

