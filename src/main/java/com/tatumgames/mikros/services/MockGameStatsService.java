package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.ContentStat;
import com.tatumgames.mikros.models.GameplayTypeStat;
import com.tatumgames.mikros.models.GenreStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Mock implementation of GameStatsService.
 * Returns placeholder/mock data until the MIKROS Analytics API is integrated.
 */
public class MockGameStatsService implements GameStatsService {
    private static final Logger logger = LoggerFactory.getLogger(MockGameStatsService.class);

    /**
     * Creates a new MockGameStatsService.
     */
    public MockGameStatsService() {
        logger.info("MockGameStatsService initialized (using placeholder data)");
    }

    @Override
    public List<GenreStat> getTrendingGameGenres(int limit) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/trending-genres
        logger.debug("getTrendingGameGenres called - returning mock data");

        return Arrays.asList(
                new GenreStat("Roguelike", 43.2, 185000, 1),
                new GenreStat("Puzzle", 31.5, 142000, 2),
                new GenreStat("Sandbox", 29.1, 198000, 3)
        ).subList(0, Math.min(limit, 3));
    }

    @Override
    public List<GenreStat> getTrendingContentGenres(int limit) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/trending-content-genres
        logger.debug("getTrendingContentGenres called - returning mock data");

        return Arrays.asList(
                new GenreStat("Co-op Multiplayer", 38.7, 220000, 1),
                new GenreStat("Story-Driven", 35.2, 175000, 2),
                new GenreStat("Action RPG", 27.8, 210000, 3)
        ).subList(0, Math.min(limit, 3));
    }

    @Override
    public List<ContentStat> getTrendingContent(int limit) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/trending-content
        logger.debug("getTrendingContent called - returning mock data");

        return Arrays.asList(
                new ContentStat("Nightmare Dungeon", "Boss", 52.3, 45000, 1),
                new ContentStat("Crystal Caves", "Level", 47.1, 38000, 2),
                new ContentStat("Shadow Assassin", "Character", 41.8, 32000, 3),
                new ContentStat("Dragon's Lair", "Raid", 39.5, 29000, 4),
                new ContentStat("Time Trial Mode", "Game Mode", 35.2, 27000, 5)
        ).subList(0, Math.min(limit, 5));
    }

    @Override
    public List<GameplayTypeStat> getTrendingGameplayTypes(int limit) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/trending-gameplay-types
        logger.debug("getTrendingGameplayTypes called - returning mock data");

        return Arrays.asList(
                new GameplayTypeStat("Competitive", 41.5, 320000, 35.2, 1),
                new GameplayTypeStat("Casual", 28.3, 450000, 49.5, 2),
                new GameplayTypeStat("Hyper-Casual", 22.1, 140000, 15.3, 3)
        ).subList(0, Math.min(limit, 3));
    }

    @Override
    public List<GenreStat> getPopularGameGenres(int limit) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/popular-genres
        logger.debug("getPopularGameGenres called - returning mock data");

        return Arrays.asList(
                new GenreStat("Action", 12.5, 520000, 1),
                new GenreStat("RPG", 8.3, 485000, 2),
                new GenreStat("Strategy", 5.7, 340000, 3)
        ).subList(0, Math.min(limit, 3));
    }

    @Override
    public List<GenreStat> getPopularContentGenres(int limit) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/popular-content-genres
        logger.debug("getPopularContentGenres called - returning mock data");

        return Arrays.asList(
                new GenreStat("Multiplayer", 15.2, 680000, 1),
                new GenreStat("Open World", 11.8, 530000, 2),
                new GenreStat("Story Mode", 9.4, 420000, 3)
        ).subList(0, Math.min(limit, 3));
    }

    @Override
    public List<ContentStat> getPopularContent(int limit) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/popular-content
        logger.debug("getPopularContent called - returning mock data");

        return Arrays.asList(
                new ContentStat("Battle Royale Mode", "Game Mode", 18.5, 125000, 1),
                new ContentStat("Ancient Temple", "Level", 14.2, 98000, 2),
                new ContentStat("Knight Class", "Character", 12.7, 87000, 3),
                new ContentStat("Siege Warfare", "Game Mode", 11.3, 76000, 4),
                new ContentStat("Mystic Forest", "Level", 10.1, 65000, 5)
        ).subList(0, Math.min(limit, 5));
    }

    @Override
    public List<GameplayTypeStat> getPopularGameplayTypes(int limit) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/popular-gameplay-types
        logger.debug("getPopularGameplayTypes called - returning mock data");

        return Arrays.asList(
                new GameplayTypeStat("Casual", 8.5, 750000, 52.3, 1),
                new GameplayTypeStat("Competitive", 6.2, 480000, 33.5, 2),
                new GameplayTypeStat("Hyper-Casual", 3.1, 205000, 14.2, 3)
        ).subList(0, Math.min(limit, 3));
    }

    @Override
    public long getTotalMikrosApps() {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/total-apps
        logger.debug("getTotalMikrosApps called - returning mock data");
        return 1247L; // Mock value
    }

    @Override
    public long getTotalMikrosContributors() {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/total-contributors
        logger.debug("getTotalMikrosContributors called - returning mock data");
        return 45823L; // Mock value
    }

    @Override
    public long getTotalUsers() {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/total-users
        logger.debug("getTotalUsers called - returning mock data");
        return 3240567L; // Mock value
    }

    @Override
    public double getAverageGameplayTime(String genre) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/avg-gameplay-time
        logger.debug("getAverageGameplayTime called for genre '{}' - returning mock data", genre);

        // Mock: return different values based on genre
        if (genre != null && !genre.isBlank()) {
            return switch (genre.toLowerCase()) {
                case "rpg" -> 24.5;
                case "strategy" -> 18.3;
                case "action" -> 12.7;
                case "puzzle" -> 8.2;
                default -> 15.4;
            };
        }

        return 15.4; // Average across all genres
    }

    @Override
    public double getAverageSessionTime(String genre) {
        // TODO: Integrate with MIKROS Analytics API - GET /api/gamestats/avg-session-time
        logger.debug("getAverageSessionTime called for genre '{}' - returning mock data", genre);

        // Mock: return different values based on genre
        if (genre != null && !genre.isBlank()) {
            return switch (genre.toLowerCase()) {
                case "rpg" -> 45.2;
                case "strategy" -> 38.7;
                case "action" -> 28.5;
                case "puzzle" -> 15.3;
                case "hyper-casual" -> 8.1;
                default -> 32.4;
            };
        }

        return 32.4; // Average across all genres
    }
}

