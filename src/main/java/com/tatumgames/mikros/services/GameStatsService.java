package com.tatumgames.mikros.services;

import com.tatumgames.mikros.admin.config.MikrosEcosystemConfig;
import com.tatumgames.mikros.models.ContentStat;
import com.tatumgames.mikros.models.GameplayTypeStat;
import com.tatumgames.mikros.models.GenreStat;

import java.util.List;

/**
 * Service interface for fetching game statistics from MIKROS Analytics.
 */
public interface GameStatsService {

    /**
     * Gets trending game genres (fastest-growing by player engagement).
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/trending-genres
     *
     * @param limit the maximum number of genres to return
     * @return list of trending genre statistics
     */
    List<GenreStat> getTrendingGameGenres(int limit);

    /**
     * Gets trending content genres (e.g., action, story, co-op).
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/trending-content-genres
     *
     * @param limit the maximum number of content genres to return
     * @return list of trending content genre statistics
     */
    List<GenreStat> getTrendingContentGenres(int limit);

    /**
     * Gets trending in-game content (specific levels, bosses, characters, etc.).
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/trending-content
     *
     * @param limit the maximum number of content items to return
     * @return list of trending content statistics
     */
    List<ContentStat> getTrendingContent(int limit);

    /**
     * Gets trending gameplay types (casual, competitive, hyper-casual).
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/trending-gameplay-types
     *
     * @param limit the maximum number of gameplay types to return
     * @return list of trending gameplay type statistics
     */
    List<GameplayTypeStat> getTrendingGameplayTypes(int limit);

    /**
     * Gets most popular game genres overall.
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/popular-genres
     *
     * @param limit the maximum number of genres to return
     * @return list of popular genre statistics
     */
    List<GenreStat> getPopularGameGenres(int limit);

    /**
     * Gets most popular content genres.
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/popular-content-genres
     *
     * @param limit the maximum number of content genres to return
     * @return list of popular content genre statistics
     */
    List<GenreStat> getPopularContentGenres(int limit);

    /**
     * Gets most popular in-game content.
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/popular-content
     *
     * @param limit the maximum number of content items to return
     * @return list of popular content statistics
     */
    List<ContentStat> getPopularContent(int limit);

    /**
     * Gets most popular gameplay types.
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/popular-gameplay-types
     *
     * @param limit the maximum number of gameplay types to return
     * @return list of popular gameplay type statistics
     */
    List<GameplayTypeStat> getPopularGameplayTypes(int limit);

    /**
     * Gets total number of apps using MIKROS Analytics.
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/total-apps
     *
     * @return total number of apps
     */
    long getTotalMikrosApps();

    /**
     * Gets total number of contributors (devs, testers, players) in MIKROS ecosystem.
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/total-contributors
     *
     * @return total number of contributors
     */
    long getTotalMikrosContributors();

    /**
     * Gets total unique user profiles tracked across MIKROS-enabled games.
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/total-users
     *
     * @return total number of unique users
     */
    long getTotalUsers();

    /**
     * Gets average gameplay time per app.
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/avg-gameplay-time
     *
     * @param genre optional genre filter
     * @return average gameplay time in hours
     */
    double getAverageGameplayTime(String genre);

    /**
     * Gets average session length across all games or by genre.
     * <p>
     * TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/avg-session-time
     *
     * @param genre optional genre filter
     * @return average session time in minutes
     */
    double getAverageSessionTime(String genre);

    /**
     * Sets up MIKROS Ecosystem for a guild.
     *
     * @param guildId   the guild ID
     * @param channelId the channel ID for ecosystem commands
     */
    void setupEcosystem(String guildId, String channelId);

    /**
     * Gets the MIKROS Ecosystem configuration for a guild.
     *
     * @param guildId the guild ID
     * @return the config, or null if not configured
     */
    MikrosEcosystemConfig getConfig(String guildId);

    /**
     * Updates the MIKROS Ecosystem configuration for a guild.
     *
     * @param config the configuration to update
     */
    void updateConfig(MikrosEcosystemConfig config);

    /**
     * Gets the ecosystem channel ID for a guild.
     *
     * @param guildId the guild ID
     * @return the channel ID, or null if not configured
     */
    String getEcosystemChannel(String guildId);
}

