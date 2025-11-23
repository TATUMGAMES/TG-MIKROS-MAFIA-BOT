package com.tatumgames.mikros.promo.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a lead submission from a user requesting promotional help.
 * 
 * TODO: Future Features
 * - Submit to lead-capture API endpoint
 * - Integration with CRM systems (Hubspot, etc.)
 * - Track conversion rates
 * - Email validation
 */
public class LeadSubmission {
    private final String discordId;
    private final String username;
    private final String serverId;
    private final String campaignInterest;
    private final String email;
    private final Instant timestamp;
    
    /**
     * Creates a new lead submission.
     * 
     * @param discordId the Discord user ID
     * @param username the username
     * @param serverId the server ID
     * @param campaignInterest the type of campaign (e.g., "Game Launch")
     * @param email optional email address
     * @param timestamp when the submission was made
     */
    public LeadSubmission(String discordId, String username, String serverId,
                         String campaignInterest, String email, Instant timestamp) {
        this.discordId = Objects.requireNonNull(discordId);
        this.username = Objects.requireNonNull(username);
        this.serverId = Objects.requireNonNull(serverId);
        this.campaignInterest = campaignInterest != null ? campaignInterest : "General";
        this.email = email;
        this.timestamp = Objects.requireNonNull(timestamp);
    }
    
    public String getDiscordId() {
        return discordId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public String getCampaignInterest() {
        return campaignInterest;
    }
    
    public String getEmail() {
        return email;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
}





