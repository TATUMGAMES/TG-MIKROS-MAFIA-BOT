package com.tatumgames.mikros.bump.service;

import com.tatumgames.mikros.bump.model.BumpConfig;
import com.tatumgames.mikros.bump.model.BumpStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of BumpService.
 * Stores configuration in memory (expandable to database).
 */
public class InMemoryBumpService implements BumpService {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryBumpService.class);
    
    // Guild configuration storage: guildId -> BumpConfig
    private final Map<String, BumpConfig> configs;
    
    // Bump history storage: guildId -> List of BumpRecord
    private final Map<String, List<BumpStats.BumpRecord>> bumpHistory;
    
    /**
     * Creates a new InMemoryBumpService.
     */
    public InMemoryBumpService() {
        this.configs = new ConcurrentHashMap<>();
        this.bumpHistory = new ConcurrentHashMap<>();
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
        bumpHistory.remove(guildId);
        logger.info("Cleared bump data for guild {}", guildId);
    }
    
    @Override
    public void recordSuccessfulBump(String guildId, BumpConfig.BumpBot bot, String userId, Instant time) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }
        if (bot == null) {
            throw new IllegalArgumentException("bot cannot be null");
        }
        if (time == null) {
            throw new IllegalArgumentException("time cannot be null");
        }
        
        // Record in history
        List<BumpStats.BumpRecord> history = bumpHistory.computeIfAbsent(guildId, k -> new ArrayList<>());
        history.add(new BumpStats.BumpRecord(bot, userId, time));
        
        // Also update last bump time (for compatibility)
        recordBumpTime(guildId, bot, time);
        
        logger.info("Recorded successful bump for {} in guild {} by user {}",
                bot.getDisplayName(), guildId, userId != null ? userId : "unknown");
    }
    
    @Override
    public BumpStats getBumpStats(String guildId) {
        return getBumpStats(guildId, Instant.ofEpochMilli(0), Instant.now());
    }
    
    @Override
    public BumpStats getBumpStats(String guildId, Instant startTime, Instant endTime) {
        List<BumpStats.BumpRecord> allBumps = bumpHistory.getOrDefault(guildId, Collections.emptyList());
        
        int totalBumps = allBumps.size();
        
        // Calculate monthly and weekly stats
        Instant monthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        
        int bumpsThisMonth = (int) allBumps.stream()
                .filter(bump -> !bump.getTime().isBefore(monthAgo))
                .count();
        
        int bumpsThisWeek = (int) allBumps.stream()
                .filter(bump -> !bump.getTime().isBefore(weekAgo))
                .count();
        
        // Count per bot
        Map<BumpConfig.BumpBot, Integer> bumpsPerBot = new HashMap<>();
        for (BumpStats.BumpRecord bump : allBumps) {
            bumpsPerBot.merge(bump.getBot(), 1, Integer::sum);
        }
        
        // Get last bump time per bot
        Map<BumpConfig.BumpBot, Instant> lastBumpTime = new HashMap<>();
        BumpConfig config = configs.get(guildId);
        if (config != null) {
            for (BumpConfig.BumpBot bot : BumpConfig.BumpBot.values()) {
                Instant lastTime = config.getLastBumpTime(bot);
                if (lastTime != null) {
                    lastBumpTime.put(bot, lastTime);
                }
            }
        }
        
        // Get recent bumps (last 10)
        List<BumpStats.BumpRecord> recentBumps = allBumps.stream()
                .sorted((a, b) -> b.getTime().compareTo(a.getTime()))
                .limit(10)
                .collect(Collectors.toList());
        
        return new BumpStats(guildId, totalBumps, bumpsThisMonth, bumpsThisWeek,
                bumpsPerBot, lastBumpTime, recentBumps);
    }
}

