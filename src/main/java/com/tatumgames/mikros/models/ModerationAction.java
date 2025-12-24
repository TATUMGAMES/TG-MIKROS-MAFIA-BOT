package com.tatumgames.mikros.models;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a moderation action taken against a user.
 */
public record ModerationAction(String targetUserId, String targetUsername, String moderatorId, String moderatorUsername,
                               ActionType actionType, String reason, Instant timestamp, String guildId) {
    /**
     * Creates a new ModerationAction.
     *
     * @param targetUserId      the ID of the user who was moderated
     * @param targetUsername    the username of the user who was moderated
     * @param moderatorId       the ID of the moderator who performed the action
     * @param moderatorUsername the username of the moderator
     * @param actionType        the type of moderation action
     * @param reason            the reason for the action
     * @param timestamp         when the action occurred
     * @param guildId           the ID of the guild where the action occurred
     */
    public ModerationAction(
            String targetUserId,
            String targetUsername,
            String moderatorId,
            String moderatorUsername,
            ActionType actionType,
            String reason,
            Instant timestamp,
            String guildId
    ) {
        this.targetUserId = Objects.requireNonNull(targetUserId, "targetUserId cannot be null");
        this.targetUsername = Objects.requireNonNull(targetUsername, "targetUsername cannot be null");
        this.moderatorId = Objects.requireNonNull(moderatorId, "moderatorId cannot be null");
        this.moderatorUsername = Objects.requireNonNull(moderatorUsername, "moderatorUsername cannot be null");
        this.actionType = Objects.requireNonNull(actionType, "actionType cannot be null");
        this.reason = Objects.requireNonNull(reason, "reason cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
        this.guildId = Objects.requireNonNull(guildId, "guildId cannot be null");
    }

    /**
     * Gets the target user's ID.
     *
     * @return the target user ID
     */
    @Override
    public String targetUserId() {
        return targetUserId;
    }

    /**
     * Gets the target user's username.
     *
     * @return the target username
     */
    @Override
    public String targetUsername() {
        return targetUsername;
    }

    /**
     * Gets the moderator's ID.
     *
     * @return the moderator ID
     */
    @Override
    public String moderatorId() {
        return moderatorId;
    }

    /**
     * Gets the moderator's username.
     *
     * @return the moderator username
     */
    @Override
    public String moderatorUsername() {
        return moderatorUsername;
    }

    /**
     * Gets the type of action.
     *
     * @return the action type
     */
    @Override
    public ActionType actionType() {
        return actionType;
    }

    /**
     * Gets the reason for the action.
     *
     * @return the reason
     */
    @Override
    public String reason() {
        return reason;
    }

    /**
     * Gets the timestamp when the action occurred.
     *
     * @return the timestamp
     */
    @Override
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Gets the guild ID where the action occurred.
     *
     * @return the guild ID
     */
    @Override
    public String guildId() {
        return guildId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModerationAction that = (ModerationAction) o;
        return Objects.equals(targetUserId, that.targetUserId) &&
                Objects.equals(moderatorId, that.moderatorId) &&
                actionType == that.actionType &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(guildId, that.guildId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetUserId, moderatorId, actionType, timestamp, guildId);
    }

    @Override
    public String toString() {
        return String.format(
                "ModerationAction{type=%s, target=%s (%s), moderator=%s (%s), reason='%s', timestamp=%s, guild=%s}",
                actionType, targetUsername, targetUserId, moderatorUsername, moderatorId, reason, timestamp, guildId
        );
    }
}

