package com.tatumgames.mikros.admin.config;

/**
 * Configuration for MIKROS Ecosystem analytics per guild.
 * <p>
 * TODO: Server Persistence
 * - Store configuration in database
 * - Add additional settings (refresh intervals, display preferences)
 */
public class MikrosEcosystemConfig {
    private final String guildId;
    private String channelId;
    private boolean enabled;

    /**
     * Creates a MIKROS Ecosystem configuration.
     *
     * @param guildId the guild ID
     */
    public MikrosEcosystemConfig(String guildId) {
        this.guildId = guildId;
        this.enabled = true;
        this.channelId = null; // No specific channel by default
    }

    /**
     * Creates a MIKROS Ecosystem configuration with specific settings.
     *
     * @param guildId   the guild ID
     * @param enabled   whether MIKROS Ecosystem is enabled
     * @param channelId the channel ID for ecosystem commands
     */
    public MikrosEcosystemConfig(String guildId, boolean enabled, String channelId) {
        this.guildId = guildId;
        this.enabled = enabled;
        this.channelId = channelId;
    }

    // Getters and setters

    public String getGuildId() {
        return guildId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}

