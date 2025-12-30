package com.tatumgames.mikros.bump.service;

import com.tatumgames.mikros.bump.model.BumpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of BumpService.
 * Stores configuration in memory (expandable to database).
 */
public class InMemoryBumpService implements BumpService {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryBumpService.class);
    
    // Guild configuration storage: guildId -> BumpConfig
    private final Map<String, BumpConfig> configs;
    
    /**
     * Creates a new InMemoryBumpService.
     */
    public InMemoryBumpService() {
        this.configs = new ConcurrentHashMap<>();
        logger.info("InMemoryBumpService initialized");
    }
    
    @Override
    public void setBumpChannel(String guildId, String channelId) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        
        BumpConfig config = getConfig(guildId);
        config.setChannelId(channelId);
        logger.info("Bump channel set to {} for guild {}", channelId, guildId);
    }
    
    @Override
    public String getBumpChannel(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return null;
        }
        BumpConfig config = configs.get(guildId);
        return config != null ? config.getChannelId() : null;
    }
    
    @Override
    public void setEnabledBots(String guildId, EnumSet<BumpConfig.BumpBot> enabledBots) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        
        BumpConfig config = getConfig(guildId);
        config.setEnabledBots(enabledBots);
        logger.info("Enabled bots set to {} for guild {}", enabledBots, guildId);
    }
    
    @Override
    public EnumSet<BumpConfig.BumpBot> getEnabledBots(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return EnumSet.noneOf(BumpConfig.BumpBot.class);
        }
        BumpConfig config = configs.get(guildId);
        return config != null ? config.getEnabledBots() : EnumSet.noneOf(BumpConfig.BumpBot.class);
    }
    
    @Override
    public void setBumpInterval(String guildId, int intervalHours) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        if (intervalHours < 1 || intervalHours > 24) {
            throw new IllegalArgumentException("intervalHours must be between 1 and 24");
        }
        
        BumpConfig config = getConfig(guildId);
        config.setIntervalHours(intervalHours);
        logger.info("Bump interval set to {} hours for guild {}", intervalHours, guildId);
    }
    
    @Override
    public int getBumpInterval(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return 4; // Default
        }
        BumpConfig config = configs.get(guildId);
        return config != null ? config.getIntervalHours() : 4;
    }
    
    @Override
    public void recordBumpTime(String guildId, BumpConfig.BumpBot bot, Instant time) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        if (bot == null) {
            throw new IllegalArgumentException("bot cannot be null");
        }
        if (time == null) {
            throw new IllegalArgumentException("time cannot be null");
        }
        
        BumpConfig config = getConfig(guildId);
        config.recordBumpTime(bot, time);
        logger.debug("Recorded bump time for bot {} in guild {} at {}", bot, guildId, time);
    }
    
    @Override
    public Instant getLastBumpTime(String guildId, BumpConfig.BumpBot bot) {
        if (guildId == null || guildId.isBlank() || bot == null) {
            return null;
        }
        BumpConfig config = configs.get(guildId);
        return config != null ? config.getLastBumpTime(bot) : null;
    }
    
    @Override
    public BumpConfig getConfig(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        return configs.computeIfAbsent(guildId, BumpConfig::new);
    }
    
    @Override
    public void clearGuildData(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return;
        }
        configs.remove(guildId);
        logger.info("Cleared bump data for guild {}", guildId);
    }
}

