package com.tatumgames.mikros.models;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a behavior report for reputation score tracking.
 * Used when reporting positive or negative user behavior.
 */
public class BehaviorReport {
    private final String targetUserId;
    private final String targetUsername;
    private final String reporterId;
    private final String reporterUsername;
    private final BehaviorCategory behaviorCategory;
    private final String notes;
    private final Instant timestamp;
    private final String guildId;
    
    /**
     * Creates a new BehaviorReport.
     * 
     * @param targetUserId the ID of the user being reported
     * @param targetUsername the username of the user being reported
     * @param reporterId the ID of the user making the report
     * @param reporterUsername the username of the reporter
     * @param behaviorCategory the behavior category
     * @param notes optional additional notes
     * @param timestamp when the report was created
     * @param guildId the ID of the guild where the behavior occurred
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
    public String getTargetUserId() {
        return targetUserId;
    }
    
    /**
     * Gets the target user's username.
     * 
     * @return the target username
     */
    public String getTargetUsername() {
        return targetUsername;
    }
    
    /**
     * Gets the reporter's ID.
     * 
     * @return the reporter ID
     */
    public String getReporterId() {
        return reporterId;
    }
    
    /**
     * Gets the reporter's username.
     * 
     * @return the reporter username
     */
    public String getReporterUsername() {
        return reporterUsername;
    }
    
    /**
     * Gets the behavior category.
     * 
     * @return the behavior category
     */
    public BehaviorCategory getBehaviorCategory() {
        return behaviorCategory;
    }
    
    /**
     * Gets the optional notes.
     * 
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }
    
    /**
     * Gets the timestamp when the report was created.
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the guild ID where the behavior occurred.
     * 
     * @return the guild ID
     */
    public String getGuildId() {
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

