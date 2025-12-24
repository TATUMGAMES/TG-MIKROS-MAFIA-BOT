package com.tatumgames.mikros.honeypot.model;

/**
 * Configuration for honeypot system per guild.
 * Stores settings like channel name, enabled status, and alert channel.
 */
public class HoneypotConfig {
    private final String guildId;
    private boolean enabled;
    private String channelName;
    private String channelId;
    private String alertChannelId;
    private boolean silentMode; // Log only, don't auto-ban
    private int deleteDays; // Days of messages to delete (0-7, or -1 for all)

    /**
     * Creates a new HoneypotConfig with default values.
     *
     * @param guildId the guild ID
     */
    public HoneypotConfig(String guildId) {
        this.guildId = guildId;
        this.enabled = false;
        this.channelName = "honeypot";
        this.channelId = null;
        this.alertChannelId = null;
        this.silentMode = false;
        this.deleteDays = 7; // Default: delete last 7 days
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

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getAlertChannelId() {
        return alertChannelId;
    }

    public void setAlertChannelId(String alertChannelId) {
        this.alertChannelId = alertChannelId;
    }

    public boolean isSilentMode() {
        return silentMode;
    }

    public void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public int getDeleteDays() {
        return deleteDays;
    }

    public void setDeleteDays(int deleteDays) {
        this.deleteDays = deleteDays;
    }
}





