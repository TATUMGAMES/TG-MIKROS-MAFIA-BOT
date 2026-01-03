package com.tatumgames.mikros.bump.service;

import com.tatumgames.mikros.bump.model.BumpConfig;
import com.tatumgames.mikros.bump.model.BumpStats;

import java.time.Instant;
import java.util.EnumSet;

/**
 * Service interface for managing server auto-bump configurations.
 */
public interface BumpService {

    /**
     * Sets the bump channel for a guild.
     *
     * @param guildId   the guild ID
     * @param channelId the channel ID where bumps should be sent
     */
    void setBumpChannel(String guildId, String channelId);

    /**
     * Gets the bump channel for a guild.
     *
     * @param guildId the guild ID
     * @return the channel ID, or null if not set
     */
    String getBumpChannel(String guildId);

    /**
     * Sets the enabled bots for a guild.
     *
     * @param guildId     the guild ID
     * @param enabledBots the set of bots to bump
     */
    void setEnabledBots(String guildId, EnumSet<BumpConfig.BumpBot> enabledBots);

    /**
     * Gets the enabled bots for a guild.
     *
     * @param guildId the guild ID
     * @return the set of enabled bots (empty if none)
     */
    EnumSet<BumpConfig.BumpBot> getEnabledBots(String guildId);

    /**
     * Sets the bump interval for a guild.
     *
     * @param guildId       the guild ID
     * @param intervalHours the interval in hours (1-24)
     */
    void setBumpInterval(String guildId, int intervalHours);

    /**
     * Gets the bump interval for a guild.
     *
     * @param guildId the guild ID
     * @return the interval in hours (default: 4)
     */
    int getBumpInterval(String guildId);

    /**
     * Records that a bump was executed for a bot.
     *
     * @param guildId the guild ID
     * @param bot     the bot that was bumped
     * @param time    the time when bumped
     */
    void recordBumpTime(String guildId, BumpConfig.BumpBot bot, Instant time);

    /**
     * Gets the last bump time for a bot in a guild.
     *
     * @param guildId the guild ID
     * @param bot     the bot
     * @return the last bump time, or null if never bumped
     */
    Instant getLastBumpTime(String guildId, BumpConfig.BumpBot bot);

    /**
     * Gets the bump configuration for a guild.
     * Creates a default config if none exists.
     *
     * @param guildId the guild ID
     * @return the bump config
     */
    BumpConfig getConfig(String guildId);

    /**
     * Clears all bump data for a guild.
     *
     * @param guildId the guild ID
     */
    void clearGuildData(String guildId);

    /**
     * Records a successful bump that was detected.
     *
     * @param guildId the guild ID
     * @param bot     the bot that was bumped
     * @param userId  the user who triggered the bump (null if unknown)
     * @param time    the time when bumped
     */
    void recordSuccessfulBump(String guildId, BumpConfig.BumpBot bot, String userId, Instant time);

    /**
     * Gets bump statistics for a guild.
     *
     * @param guildId the guild ID
     * @return bump statistics
     */
    BumpStats getBumpStats(String guildId);

    /**
     * Gets bump statistics for a specific time period.
     *
     * @param guildId   the guild ID
     * @param startTime start of time period
     * @param endTime   end of time period
     * @return bump statistics for the period
     */
    BumpStats getBumpStats(String guildId, Instant startTime, Instant endTime);
}

