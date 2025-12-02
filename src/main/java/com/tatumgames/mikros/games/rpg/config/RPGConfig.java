package com.tatumgames.mikros.games.rpg.config;

/**
 * Configuration for RPG system per guild.
 * <p>
 * TODO: Server Persistence
 * - Store configuration in database
 * - Add difficulty modifiers (easy, normal, hard)
 * - Add custom XP multipliers
 * - Add server-specific events or quests
 */
public class RPGConfig {
    private final String guildId;
    private boolean enabled;
    private String rpgChannelId;
    private int chargeRefreshHours; // New: hours until charges refresh (default: 12)
    private double xpMultiplier;

    /**
     * Creates an RPG configuration.
     *
     * @param guildId the guild ID
     */
    public RPGConfig(String guildId) {
        this.guildId = guildId;
        this.enabled = true;
        this.rpgChannelId = null; // No specific channel by default
        this.chargeRefreshHours = 12; // 12 hour charge refresh by default
        this.xpMultiplier = 1.0;   // Normal XP rate
    }

    /**
     * Creates an RPG configuration with specific settings.
     *
     * @param guildId            the guild ID
     * @param enabled            whether RPG is enabled
     * @param rpgChannelId       the channel ID (optional)
     * @param chargeRefreshHours charge refresh period in hours (default: 12)
     * @param xpMultiplier       XP multiplier
     */
    public RPGConfig(String guildId, boolean enabled, String rpgChannelId,
                     int chargeRefreshHours, double xpMultiplier) {
        this.guildId = guildId;
        this.enabled = enabled;
        this.rpgChannelId = rpgChannelId;
        this.chargeRefreshHours = chargeRefreshHours;
        this.xpMultiplier = xpMultiplier;
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

    public String getRpgChannelId() {
        return rpgChannelId;
    }

    public void setRpgChannelId(String rpgChannelId) {
        this.rpgChannelId = rpgChannelId;
    }

    /**
     * Gets the charge refresh period in hours.
     *
     * @return hours until charges refresh
     */
    public int getChargeRefreshHours() {
        return chargeRefreshHours;
    }

    /**
     * Sets the charge refresh period in hours.
     *
     * @param chargeRefreshHours hours until charges refresh (min: 1)
     */
    public void setChargeRefreshHours(int chargeRefreshHours) {
        this.chargeRefreshHours = Math.max(1, chargeRefreshHours);
    }

    public double getXpMultiplier() {
        return xpMultiplier;
    }

    public void setXpMultiplier(double xpMultiplier) {
        this.xpMultiplier = Math.max(0.1, xpMultiplier);
    }
}

