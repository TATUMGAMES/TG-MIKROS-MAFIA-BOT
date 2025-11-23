package com.tatumgames.mikros.rpg.config;

/**
 * Configuration for RPG system per guild.
 * 
 * TODO: Server Persistence
 * - Store configuration in database
 * - Add difficulty modifiers (easy, normal, hard)
 * - Add custom XP multipliers
 * - Add server-specific events or quests
 */
public class RPGConfig {
    private String guildId;
    private boolean enabled;
    private String rpgChannelId;
    private int cooldownHours;
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
        this.cooldownHours = 24;   // 24 hour cooldown by default
        this.xpMultiplier = 1.0;   // Normal XP rate
    }
    
    /**
     * Creates an RPG configuration with specific settings.
     * 
     * @param guildId the guild ID
     * @param enabled whether RPG is enabled
     * @param rpgChannelId the channel ID (optional)
     * @param cooldownHours cooldown in hours
     * @param xpMultiplier XP multiplier
     */
    public RPGConfig(String guildId, boolean enabled, String rpgChannelId, 
                    int cooldownHours, double xpMultiplier) {
        this.guildId = guildId;
        this.enabled = enabled;
        this.rpgChannelId = rpgChannelId;
        this.cooldownHours = cooldownHours;
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
    
    public int getCooldownHours() {
        return cooldownHours;
    }
    
    public void setCooldownHours(int cooldownHours) {
        this.cooldownHours = Math.max(1, cooldownHours);
    }
    
    public double getXpMultiplier() {
        return xpMultiplier;
    }
    
    public void setXpMultiplier(double xpMultiplier) {
        this.xpMultiplier = Math.max(0.1, xpMultiplier);
    }
}

