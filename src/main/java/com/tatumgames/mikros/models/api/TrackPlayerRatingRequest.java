package com.tatumgames.mikros.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request model for /trackPlayerRating API endpoint.
 */
public class TrackPlayerRatingRequest {
    @JsonProperty("appGameId")
    private String appGameId = "tg-644a4ae401486";

    @JsonProperty("apiKeyType")
    private String apiKeyType = "prod";

    @JsonProperty("appVersion")
    private String appVersion = "1.0.0";

    @JsonProperty("sdkVersion")
    private String sdkVersion = "1.0.0";

    @JsonProperty("platform")
    private String platform = "ios";

    @JsonProperty("deviceId")
    private String deviceId = "nfg547e5-184f-1g1b-b6ra-dfnyrt6565";

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("sender")
    private Sender sender;

    @JsonProperty("participants")
    private List<Participant> participants;

    public String getAppGameId() {
        return appGameId;
    }

    public void setAppGameId(String appGameId) {
        this.appGameId = appGameId;
    }

    public String getApiKeyType() {
        return apiKeyType;
    }

    public void setApiKeyType(String apiKeyType) {
        this.apiKeyType = apiKeyType;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public static class Sender {
        @JsonProperty("discordUserId")
        private String discordUserId;

        @JsonProperty("discordUsername")
        private String discordUsername;

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
    }

    public static class Participant {
        @JsonProperty("discordUserId")
        private String discordUserId;

        @JsonProperty("discordUsername")
        private String discordUsername;

        @JsonProperty("value")
        private int value;

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

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}




