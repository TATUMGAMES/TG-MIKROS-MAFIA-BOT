package com.tatumgames.mikros.bump.model;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for server auto-bump feature per guild.
 * <p>
 * TODO: Server Persistence
 * - Store configuration in database
 * - Add additional settings (custom bump messages, timezone support)
 */
public class BumpConfig {
    private final String guildId;
    private String channelId;
    private EnumSet<BumpBot> enabledBots;
    private int intervalHours; // 1-24 hours, default: 4
    
    // Track last bump time per bot to respect rate limits
    private final Map<BumpBot, Instant> lastBumpTime;
    
    /**
     * External advertising bots that can be bumped.
     */
    public enum BumpBot {
        DISBOARD("Disboard", "/bump"),
        DISURL("Disurl", "/bump disurl");
        
        private final String displayName;
        private final String command;
        
        BumpBot(String displayName, String command) {
            this.displayName = displayName;
            this.command = command;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getCommand() {
            return command;
        }
    }
    
    /**
     * Creates a default BumpConfig.
     *
     * @param guildId the guild ID
     */
    public BumpConfig(String guildId) {
        this.guildId = Objects.requireNonNull(guildId);
        this.channelId = null;
        this.enabledBots = EnumSet.noneOf(BumpBot.class);
        this.intervalHours = 4; // Default: 4 hours
        this.lastBumpTime = new HashMap<>();
    }
    
    /**
     * Creates a BumpConfig with specific settings.
     *
     * @param guildId      the guild ID
     * @param channelId     the channel ID for bumping
     * @param enabledBots   the set of enabled bots
     * @param intervalHours the bump interval in hours (1-24)
     */
    public BumpConfig(String guildId, String channelId, EnumSet<BumpBot> enabledBots, int intervalHours) {
        this.guildId = Objects.requireNonNull(guildId);
        this.channelId = channelId;
        this.enabledBots = enabledBots != null ? EnumSet.copyOf(enabledBots) : EnumSet.noneOf(BumpBot.class);
        this.intervalHours = Math.max(1, Math.min(24, intervalHours)); // Clamp to 1-24
        this.lastBumpTime = new HashMap<>();
    }
    
    // Getters and setters
    
    public String getGuildId() {
        return guildId;
    }
    
    public String getChannelId() {
        return channelId;
    }
    
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    
    public EnumSet<BumpBot> getEnabledBots() {
        return EnumSet.copyOf(enabledBots);
    }
    
    public void setEnabledBots(EnumSet<BumpBot> enabledBots) {
        this.enabledBots = enabledBots != null ? EnumSet.copyOf(enabledBots) : EnumSet.noneOf(BumpBot.class);
    }
    
    public int getIntervalHours() {
        return intervalHours;
    }
    
    public void setIntervalHours(int intervalHours) {
        this.intervalHours = Math.max(1, Math.min(24, intervalHours)); // Clamp to 1-24
    }
    
    /**
     * Records the last bump time for a specific bot.
     *
     * @param bot  the bot that was bumped
     * @param time the time when bumped
     */
    public void recordBumpTime(BumpBot bot, Instant time) {
        lastBumpTime.put(bot, time);
    }
    
    /**
     * Gets the last bump time for a specific bot.
     *
     * @param bot the bot
     * @return the last bump time, or null if never bumped
     */
    public Instant getLastBumpTime(BumpBot bot) {
        return lastBumpTime.get(bot);
    }
    
    /**
     * Checks if bumping is enabled (has channel and at least one bot).
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return channelId != null && !channelId.isBlank() && !enabledBots.isEmpty();
    }
}

