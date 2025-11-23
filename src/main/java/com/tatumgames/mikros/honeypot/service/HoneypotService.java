package com.tatumgames.mikros.honeypot.service;

import com.tatumgames.mikros.honeypot.model.HoneypotConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing honeypot channels and configurations.
 * Handles creation, deletion, and configuration of honeypot channels.
 */
public class HoneypotService {
    private static final Logger logger = LoggerFactory.getLogger(HoneypotService.class);
    
    // Per-guild configuration: guildId -> HoneypotConfig
    private final Map<String, HoneypotConfig> configs;
    
    /**
     * Creates a new HoneypotService.
     */
    public HoneypotService() {
        this.configs = new ConcurrentHashMap<>();
        logger.info("HoneypotService initialized");
    }
    
    /**
     * Gets or creates configuration for a guild.
     * 
     * @param guildId the guild ID
     * @return the honeypot configuration
     */
    public HoneypotConfig getConfig(String guildId) {
        return configs.computeIfAbsent(guildId, HoneypotConfig::new);
    }
    
    /**
     * Enables honeypot mode for a guild and creates the channel if needed.
     * 
     * @param guild the guild
     * @param channelName the name for the honeypot channel
     * @return the created or existing honeypot channel, or null if creation failed
     */
    public TextChannel enableHoneypot(Guild guild, String channelName) {
        HoneypotConfig config = getConfig(guild.getId());
        config.setEnabled(true);
        config.setChannelName(channelName);
        
        // Check if channel already exists
        if (config.getChannelId() != null) {
            TextChannel existing = guild.getTextChannelById(config.getChannelId());
            if (existing != null) {
                logger.info("Honeypot channel already exists for guild {}: {}", guild.getId(), existing.getName());
                return existing;
            }
        }
        
        // Create new honeypot channel
        try {
            TextChannel channel = guild.createTextChannel(channelName)
                    .setTopic("⚠️ DO NOT POST HERE - This is a honeypot channel. Posting here will result in an automatic ban.")
                    .setNSFW(false)
                    .complete();
            
            config.setChannelId(channel.getId());
            logger.info("Created honeypot channel {} for guild {}", channel.getName(), guild.getId());
            return channel;
        } catch (Exception e) {
            logger.error("Failed to create honeypot channel for guild {}: {}", guild.getId(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Disables honeypot mode for a guild and optionally deletes the channel.
     * 
     * @param guild the guild
     * @param deleteChannel whether to delete the honeypot channel
     */
    public void disableHoneypot(Guild guild, boolean deleteChannel) {
        HoneypotConfig config = getConfig(guild.getId());
        config.setEnabled(false);
        
        if (deleteChannel && config.getChannelId() != null) {
            TextChannel channel = guild.getTextChannelById(config.getChannelId());
            if (channel != null) {
                try {
                    channel.delete().queue(
                            success -> logger.info("Deleted honeypot channel {} for guild {}", channel.getName(), guild.getId()),
                            error -> logger.error("Failed to delete honeypot channel for guild {}: {}", guild.getId(), error.getMessage())
                    );
                } catch (Exception e) {
                    logger.error("Error deleting honeypot channel for guild {}: {}", guild.getId(), e.getMessage(), e);
                }
            }
            config.setChannelId(null);
        }
        
        logger.info("Disabled honeypot for guild {}", guild.getId());
    }
    
    /**
     * Checks if a channel is the honeypot channel for a guild.
     * 
     * @param guildId the guild ID
     * @param channelId the channel ID to check
     * @return true if the channel is the honeypot channel
     */
    public boolean isHoneypotChannel(String guildId, String channelId) {
        HoneypotConfig config = configs.get(guildId);
        if (config == null || !config.isEnabled()) {
            return false;
        }
        return channelId.equals(config.getChannelId());
    }
}





