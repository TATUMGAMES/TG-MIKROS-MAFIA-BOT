package com.tatumgames.mikros.promo.model;

import java.time.Duration;

/**
 * Configuration for smart promotional detection per guild.
 * <p>
 * TODO: Future Features
 * - Custom trigger phrases per server
 * - Different cooldowns per campaign type
 * - Integration with CRM systems (Hubspot, etc.)
 * - A/B testing for different promo messages
 */
public class PromoConfig {
    private final String guildId;
    private boolean enabled;
    private Duration userCooldown;
    private String promoChannelId; // Optional: specific channel for promos

    /**
     * Creates a PromoConfig with defaults.
     *
     * @param guildId the guild ID
     */
    public PromoConfig(String guildId) {
        this.guildId = guildId;
        this.enabled = false; // Disabled by default (opt-in)
        this.userCooldown = Duration.ofDays(7); // 1 week default
        this.promoChannelId = null;
    }

    /**
     * Creates a PromoConfig with specific settings.
     *
     * @param guildId        the guild ID
     * @param enabled        whether promo detection is enabled
     * @param userCooldown   cooldown between prompts per user
     * @param promoChannelId optional channel restriction
     */
    public PromoConfig(String guildId, boolean enabled, Duration userCooldown, String promoChannelId) {
        this.guildId = guildId;
        this.enabled = enabled;
        this.userCooldown = userCooldown;
        this.promoChannelId = promoChannelId;
    }

    public String getGuildId() {
        return guildId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getUserCooldown() {
        return userCooldown;
    }

    public void setUserCooldown(Duration userCooldown) {
        this.userCooldown = userCooldown;
    }

    public String getPromoChannelId() {
        return promoChannelId;
    }

    public void setPromoChannelId(String promoChannelId) {
        this.promoChannelId = promoChannelId;
    }
}




