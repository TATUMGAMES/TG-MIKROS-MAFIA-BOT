package com.tatumgames.mikros.promo.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a detected promotional trigger event.
 * Tracks when a user's message matches launch-related patterns.
 */
public class PromoTrigger {
    private final String userId;
    private final String username;
    private final String guildId;
    private final String channelId;
    private final String messageContent;
    private final String detectedPattern;
    private final Instant timestamp;
    private boolean promptSent;

    /**
     * Creates a new promotional trigger.
     *
     * @param userId          the user's Discord ID
     * @param username        the user's username
     * @param guildId         the guild ID
     * @param channelId       the channel ID
     * @param messageContent  the message that triggered detection
     * @param detectedPattern the pattern that matched
     * @param timestamp       when the trigger was detected
     */
    public PromoTrigger(String userId, String username, String guildId, String channelId,
                        String messageContent, String detectedPattern, Instant timestamp) {
        this.userId = Objects.requireNonNull(userId);
        this.username = Objects.requireNonNull(username);
        this.guildId = Objects.requireNonNull(guildId);
        this.channelId = Objects.requireNonNull(channelId);
        this.messageContent = Objects.requireNonNull(messageContent);
        this.detectedPattern = Objects.requireNonNull(detectedPattern);
        this.timestamp = Objects.requireNonNull(timestamp);
        this.promptSent = false;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getDetectedPattern() {
        return detectedPattern;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isPromptSent() {
        return promptSent;
    }

    public void setPromptSent(boolean promptSent) {
        this.promptSent = promptSent;
    }
}





