package com.tatumgames.mikros.promo.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a promotional lead request from a user.
 * 
 * TODO: API Integration
 * - Submit to backend lead-capture API
 * - Generate unique MIKROS promo codes
 * - Track conversion and campaign effectiveness
 * - Integrate with CRM (see /docs/API_MIKROS_PROMO_SUBMISSION.md)
 */
public class LeadRequest {
    private final String discordId;
    private final String username;
    private final String guildId;
    private final String campaignInterest;
    private final String email;
    private final Instant timestamp;
    private final String detectedPhrase;
    
    /**
     * Creates a new lead request.
     * 
     * @param discordId the user's Discord ID
     * @param username the username
     * @param guildId the guild ID where request originated
     * @param campaignInterest the type of campaign (e.g., "Game Launch")
     * @param email optional email address
     * @param timestamp when the request was made
     * @param detectedPhrase the phrase that triggered detection (optional)
     */
    public LeadRequest(String discordId, String username, String guildId,
                      String campaignInterest, String email, Instant timestamp,
                      String detectedPhrase) {
        this.discordId = Objects.requireNonNull(discordId);
        this.username = Objects.requireNonNull(username);
        this.guildId = Objects.requireNonNull(guildId);
        this.campaignInterest = campaignInterest;
        this.email = email;
        this.timestamp = Objects.requireNonNull(timestamp);
        this.detectedPhrase = detectedPhrase;
    }
    
    public String getDiscordId() {
        return discordId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getGuildId() {
        return guildId;
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
    
    public String getDetectedPhrase() {
        return detectedPhrase;
    }
}




