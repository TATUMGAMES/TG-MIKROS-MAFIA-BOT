package com.tatumgames.mikros.promo.config;

/**
 * Configuration for promotional detection per guild.
 * <p>
 * TODO: Future Features
 * - Custom trigger phrases per server
 * - Channel whitelist/blacklist
 * - Role-based targeting
 * - A/B testing for prompt messages
 */
public class PromoConfig {
    private final String guildId;
    private boolean enabled;
    private int cooldownDays;
    private boolean sendDm;
    private boolean sendInChannel;

    /**
     * Creates a promotional configuration.
     *
     * @param guildId the guild ID
     */
    public PromoConfig(String guildId) {
        this.guildId = guildId;
        this.enabled = true;
        this.cooldownDays = 7; // Default: 1 prompt per user per week
        this.sendDm = true;
        this.sendInChannel = false;
    }

    /**
     * Creates a promotional configuration with specific settings.
     *
     * @param guildId       the guild ID
     * @param enabled       whether detection is enabled
     * @param cooldownDays  cooldown in days
     * @param sendDm        whether to send DM prompts
     * @param sendInChannel whether to send channel prompts
     */
    public PromoConfig(String guildId, boolean enabled, int cooldownDays,
                       boolean sendDm, boolean sendInChannel) {
        this.guildId = guildId;
        this.enabled = enabled;
        this.cooldownDays = cooldownDays;
        this.sendDm = sendDm;
        this.sendInChannel = sendInChannel;
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

    public int getCooldownDays() {
        return cooldownDays;
    }

    public void setCooldownDays(int cooldownDays) {
        this.cooldownDays = Math.max(1, cooldownDays);
    }

    public boolean isSendDm() {
        return sendDm;
    }

    public void setSendDm(boolean sendDm) {
        this.sendDm = sendDm;
    }

    public boolean isSendInChannel() {
        return sendInChannel;
    }

    public void setSendInChannel(boolean sendInChannel) {
        this.sendInChannel = sendInChannel;
    }
}





