package com.tatumgames.mikros.models;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a behavior report for reputation score tracking.
 * Used when reporting positive or negative user behavior.
 */
public record BehaviorReport(String targetUserId, String targetUsername, String reporterId, String reporterUsername,
                             BehaviorCategory behaviorCategory, String notes, Instant timestamp, String guildId) {
    /**
     * Creates a new BehaviorReport.
     *
     * @param targetUserId     the ID of the user being reported
     * @param targetUsername   the username of the user being reported
     * @param reporterId       the ID of the user making the report
     * @param reporterUsername the username of the reporter
     * @param behaviorCategory the behavior category
     * @param notes            optional additional notes
     * @param timestamp        when the report was created
     * @param guildId          the ID of the guild where the behavior occurred
     */
    public BehaviorReport(
            String targetUserId,
            String targetUsername,
            String reporterId,
            String reporterUsername,
            BehaviorCategory behaviorCategory,
            String notes,
            Instant timestamp,
            String guildId
    ) {
        this.targetUserId = Objects.requireNonNull(targetUserId, "targetUserId cannot be null");
        this.targetUsername = Objects.requireNonNull(targetUsername, "targetUsername cannot be null");
        this.reporterId = Objects.requireNonNull(reporterId, "reporterId cannot be null");
        this.reporterUsername = Objects.requireNonNull(reporterUsername, "reporterUsername cannot be null");
        this.behaviorCategory = Objects.requireNonNull(behaviorCategory, "behaviorCategory cannot be null");
        this.notes = notes != null ? notes : "";
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
     * Gets the reporter's ID.
     *
     * @return the reporter ID
     */
    @Override
    public String reporterId() {
        return reporterId;
    }

    /**
     * Gets the reporter's username.
     *
     * @return the reporter username
     */
    @Override
    public String reporterUsername() {
        return reporterUsername;
    }

    /**
     * Gets the behavior category.
     *
     * @return the behavior category
     */
    @Override
    public BehaviorCategory behaviorCategory() {
        return behaviorCategory;
    }

    /**
     * Gets the optional notes.
     *
     * @return the notes
     */
    @Override
    public String notes() {
        return notes;
    }

    /**
     * Gets the timestamp when the report was created.
     *
     * @return the timestamp
     */
    @Override
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Gets the guild ID where the behavior occurred.
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
        BehaviorReport that = (BehaviorReport) o;
        return Objects.equals(targetUserId, that.targetUserId) &&
                Objects.equals(reporterId, that.reporterId) &&
                behaviorCategory == that.behaviorCategory &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(guildId, that.guildId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetUserId, reporterId, behaviorCategory, timestamp, guildId);
    }

    @Override
    public String toString() {
        return String.format(
                "BehaviorReport{category=%s (weight=%d), target=%s (%s), reporter=%s (%s), notes='%s', timestamp=%s, guild=%s}",
                behaviorCategory.getLabel(), behaviorCategory.getWeight(),
                targetUsername, targetUserId, reporterUsername, reporterId,
                notes, timestamp, guildId
        );
    }
}

