package com.tatumgames.mikros.bump.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Statistics about server bumps.
 */
public class BumpStats {
    private final String guildId;
    private final int totalBumps;
    private final int bumpsThisMonth;
    private final int bumpsThisWeek;
    private final Map<BumpConfig.BumpBot, Integer> bumpsPerBot;
    private final Map<BumpConfig.BumpBot, Instant> lastBumpTime;
    private final List<BumpRecord> recentBumps;

    public BumpStats(String guildId, int totalBumps, int bumpsThisMonth, int bumpsThisWeek,
                     Map<BumpConfig.BumpBot, Integer> bumpsPerBot,
                     Map<BumpConfig.BumpBot, Instant> lastBumpTime,
                     List<BumpRecord> recentBumps) {
        this.guildId = guildId;
        this.totalBumps = totalBumps;
        this.bumpsThisMonth = bumpsThisMonth;
        this.bumpsThisWeek = bumpsThisWeek;
        this.bumpsPerBot = bumpsPerBot;
        this.lastBumpTime = lastBumpTime;
        this.recentBumps = recentBumps;
    }

    // Getters
    public String getGuildId() {
        return guildId;
    }

    public int getTotalBumps() {
        return totalBumps;
    }

    public int getBumpsThisMonth() {
        return bumpsThisMonth;
    }

    public int getBumpsThisWeek() {
        return bumpsThisWeek;
    }

    public Map<BumpConfig.BumpBot, Integer> getBumpsPerBot() {
        return bumpsPerBot;
    }

    public Map<BumpConfig.BumpBot, Instant> getLastBumpTime() {
        return lastBumpTime;
    }

    public List<BumpRecord> getRecentBumps() {
        return recentBumps;
    }

    /**
     * Record of a single bump.
     */
    public static class BumpRecord {
        private final BumpConfig.BumpBot bot;
        private final String userId;
        private final Instant time;

        public BumpRecord(BumpConfig.BumpBot bot, String userId, Instant time) {
            this.bot = bot;
            this.userId = userId;
            this.time = time;
        }

        public BumpConfig.BumpBot getBot() {
            return bot;
        }

        public String getUserId() {
            return userId;
        }

        public Instant getTime() {
            return time;
        }
    }
}

