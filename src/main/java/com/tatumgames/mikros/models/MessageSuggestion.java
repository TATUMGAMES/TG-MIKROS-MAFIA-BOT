package com.tatumgames.mikros.models;

import java.util.Objects;

/**
 * Represents a message that has been flagged for moderation review.
 */
public record MessageSuggestion(String messageId, String userId, String username, String channelId, String channelName,
                                String messageContent, String snippet, String messageLink, SuggestionSeverity severity,
                                String reason) {
    /**
     * Creates a new MessageSuggestion.
     *
     * @param messageId      the ID of the message
     * @param userId         the ID of the message author
     * @param username       the username of the message author
     * @param channelId      the ID of the channel
     * @param channelName    the name of the channel
     * @param messageContent the full message content
     * @param snippet        a snippet of the problematic content
     * @param messageLink    a direct link to the message
     * @param severity       the severity level
     * @param reason         the reason for flagging
     */
    public MessageSuggestion(
            String messageId,
            String userId,
            String username,
            String channelId,
            String channelName,
            String messageContent,
            String snippet,
            String messageLink,
            SuggestionSeverity severity,
            String reason
    ) {
        this.messageId = Objects.requireNonNull(messageId);
        this.userId = Objects.requireNonNull(userId);
        this.username = Objects.requireNonNull(username);
        this.channelId = Objects.requireNonNull(channelId);
        this.channelName = Objects.requireNonNull(channelName);
        this.messageContent = Objects.requireNonNull(messageContent);
        this.snippet = Objects.requireNonNull(snippet);
        this.messageLink = Objects.requireNonNull(messageLink);
        this.severity = Objects.requireNonNull(severity);
        this.reason = Objects.requireNonNull(reason);
    }
}

