package com.tatumgames.mikros.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response model for /getUserScoreDetail API endpoint.
 */
public class GetUserScoreDetailResponse {
    @JsonProperty("status")
    private Status status;

    @JsonProperty("data")
    private Data data;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Status {
        @JsonProperty("statusCode")
        private int statusCode;

        @JsonProperty("statusMessage")
        private String statusMessage;

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }
    }

    public static class Data {
        @JsonProperty("scores")
        private List<UserScore> scores;

        public List<UserScore> getScores() {
            return scores;
        }

        public void setScores(List<UserScore> scores) {
            this.scores = scores;
        }
    }

    public static class UserScore {
        @JsonProperty("id")
        private String id;

        @JsonProperty("email")
        private String email;

        @JsonProperty("discordUserId")
        private String discordUserId;

        @JsonProperty("discordUsername")
        private String discordUsername;

        @JsonProperty("reputationScore")
        private int reputationScore;

        @JsonProperty("discordServers")
        private List<Long> discordServers;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDiscordUserId() {
            return discordUserId;
        }

        public void setDiscordUserId(String discordUserId) {
            this.discordUserId = discordUserId;
        }

        public String getDiscordUsername() {
            return discordUsername;
        }

        public void setDiscordUsername(String discordUsername) {
            this.discordUsername = discordUsername;
        }

        public int getReputationScore() {
            return reputationScore;
        }

        public void setReputationScore(int reputationScore) {
            this.reputationScore = reputationScore;
        }

        public List<Long> getDiscordServers() {
            return discordServers;
        }

        public void setDiscordServers(List<Long> discordServers) {
            this.discordServers = discordServers;
        }
    }
}




