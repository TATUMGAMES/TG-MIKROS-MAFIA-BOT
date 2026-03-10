package com.tatumgames.mikros.games.word_unscramble.service;

import com.tatumgames.mikros.games.word_unscramble.WordUnscrambleGame;
import com.tatumgames.mikros.games.word_unscramble.interfaces.WordUnscrambleInterface;
import com.tatumgames.mikros.games.word_unscramble.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing Word Unscramble game across guilds. Handles game configuration, sessions,
 * and state management. Tracks Word Unscramble progression (levels, XP) per server.
 *
 * <p>TODO: Future Features - Reward System: MIKROS discounts or Discord roles for winners - Server
 * Persistence: Store settings in database per server - Custom word lists per guild
 */
public class WordUnscrambleService {
    private static final Logger logger = LoggerFactory.getLogger(WordUnscrambleService.class);

    // Game implementation
    private final WordUnscrambleInterface game;

    // Guild configurations: guildId -> WordUnscrambleConfig
    private final Map<String, WordUnscrambleConfig> guildConfigs;

    // Active sessions: guildId -> WordUnscrambleSession
    private final Map<String, WordUnscrambleSession> activeSessions;

    // Word Unscramble progression: guildId -> WordUnscrambleProgression
    private final Map<String, WordUnscrambleProgression> wordUnscrambleProgression;

    // Player statistics: "guildId_userId" -> WordUnscramblePlayerStats
    private final Map<String, WordUnscramblePlayerStats> playerStats;

    // Word rotation tracker for levels 1-5
    private final WordUnscrambleUsedWordTracker usedWordTracker;
    private final Random random = new Random();
    /** Optional: for activity-aware scheduling (recordSolve when a player solves). */
    private volatile WordUnscrambleResetScheduler resetScheduler;

    /**
     * Sets up Word Unscramble game for a guild.
     *
     * @param guildId      the guild ID
     * @param channelId    the game channel ID
     * @param enabledGames the set of enabled game types
     * @param resetTime    the daily reset time
     */
    public void setupGames(
            String guildId, String channelId, Set<WordUnscrambleType> enabledGames, LocalTime resetTime) {
        WordUnscrambleConfig config =
                new WordUnscrambleConfig(guildId, channelId, enabledGames, resetTime);
        guildConfigs.put(guildId, config);
        logger.info(
                "Word Unscramble setup complete for guild {}: channel={}, games={}, resetTime={}",
                guildId,
                channelId,
                enabledGames,
                resetTime);
    }

    /**
     * Creates a new WordUnscrambleService.
     */
    public WordUnscrambleService() {
        this.game = new WordUnscrambleGame();
        this.guildConfigs = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
        this.wordUnscrambleProgression = new ConcurrentHashMap<>();
        this.playerStats = new ConcurrentHashMap<>();
        this.usedWordTracker = new WordUnscrambleUsedWordTracker();

        logger.info("WordUnscrambleService initialized");
    }

    /**
     * Sets the reset scheduler for activity-aware scheduling (recordSolve when a player solves).
     * Called by BotMain after the scheduler is created.
     */
    public void setWordUnscrambleResetScheduler(WordUnscrambleResetScheduler resetScheduler) {
        this.resetScheduler = resetScheduler;
    }

    /**
     * Gets the game configuration for a guild.
     *
     * @param guildId the guild ID
     * @return the game config, or null if not configured
     */
    public WordUnscrambleConfig getConfig(String guildId) {
        return guildConfigs.get(guildId);
    }

    /**
     * Updates a specific configuration setting.
     *
     * @param guildId the guild ID
     * @param config  the new configuration
     */
    public void updateConfig(String guildId, WordUnscrambleConfig config) {
        guildConfigs.put(guildId, config);
        logger.info("Updated Word Unscramble config for guild {}", guildId);
    }

    /**
     * Cleans up used-word tracker entries older than the reuse period (60 days) to prevent memory
     * growth. Does not affect leaderboards, stats, or sessions.
     *
     * @param guildId the guild ID (null to clean all guilds)
     */
    public void cleanupUsedWordTrackerEntries(String guildId) {
        usedWordTracker.cleanupOldEntries(guildId);
    }

    /**
     * Gets the active session for a guild.
     *
     * @param guildId the guild ID
     * @return the active session, or null if none
     */
    public WordUnscrambleSession getActiveSession(String guildId) {
        return activeSessions.get(guildId);
    }

    /**
     * Starts a new game session for a guild.
     *
     * @param guildId  the guild ID
     * @param gameType the type of game to start
     * @return the new game session
     */
    public WordUnscrambleSession startNewGame(String guildId, WordUnscrambleType gameType) {
        if (gameType != WordUnscrambleType.WORD_UNSCRAMBLE) {
            throw new IllegalArgumentException("Unknown game type: " + gameType);
        }

        WordUnscrambleProgression progression = getOrCreateProgression(guildId);
        int level = progression.getLevel();
        WordUnscrambleSession session =
                ((WordUnscrambleGame) game).startNewSession(guildId, level, usedWordTracker);

        // Record word usage for levels 1-5
        if (level >= 1 && level <= 5) {
            usedWordTracker.recordWordUsed(guildId, level, session.getCorrectAnswer());
        }

        activeSessions.put(guildId, session);

        logger.info("Started new Word Unscramble game for guild {} at level {}", guildId, level);
        return session;
    }

    /**
     * Gets the announcement message for the current game.
     *
     * @param guildId the guild ID
     * @return the announcement message, or null if no active game
     */
    public String getGameAnnouncement(String guildId) {
        WordUnscrambleSession session = activeSessions.get(guildId);
        if (session == null) {
            return null;
        }

        return game.generateAnnouncement(session);
    }

    /**
     * Resets the game for a guild.
     *
     * @param guildId the guild ID
     */
    public void resetGame(String guildId) {
        WordUnscrambleSession session = activeSessions.get(guildId);
        if (session != null) {
            game.resetSession(session);
        }

        // Remove the active session
        activeSessions.remove(guildId);

        logger.info("Reset Word Unscramble game for guild {}", guildId);
    }

    /**
     * Handles a player's attempt to play the current game.
     *
     * @param guildId  the guild ID
     * @param userId   the user ID
     * @param username the username
     * @param input    the player's input
     * @return the game result
     */
    public WordUnscrambleResult handleAttempt(
            String guildId, String userId, String username, String input) {
        WordUnscrambleSession session = activeSessions.get(guildId);
        if (session == null || !session.isActive()) {
            return null;
        }

        WordUnscrambleResult result = game.handleAttempt(session, userId, username, input);

        // Update player statistics and apply additional scoring factors
        if (result != null) {
            WordUnscramblePlayerStats stats = getOrCreatePlayerStats(guildId, userId);

            if (result.isCorrect()) {
                // Calculate time in seconds
                long timeSeconds =
                        result.timestamp().getEpochSecond() - session.getStartTime().getEpochSecond();

                // Calculate accuracy BEFORE recording this solve
                // Accuracy = (wordsSolved / totalAttempts) * 100
                // But we need to account for this solve, so: (wordsSolved / (totalAttempts + 1)) * 100
                double accuracy = 100.0;
                if (stats.getTotalAttempts() > 0) {
                    accuracy =
                            ((double) stats.getTotalWordsSolved() / (stats.getTotalAttempts() + 1)) * 100.0;
                }

                // Apply accuracy factor to score (minimum 0.1 to prevent zero scores)
                double accuracyFactor = Math.max(0.1, accuracy / 100.0);
                int accuracyAdjustedScore = (int) (result.score() * accuracyFactor);

                // Calculate volume bonuses before recording (need old stats)
                int volumeBonus = calculateVolumeBonuses(stats);

                // Add volume bonus
                int finalScore = accuracyAdjustedScore + volumeBonus;

                // Create new result with final score
                WordUnscrambleResult finalResult =
                        new WordUnscrambleResult(
                                result.userId(),
                                result.username(),
                                result.answer(),
                                finalScore,
                                result.bonus() + volumeBonus,
                                result.isCorrect(),
                                result.timestamp());

                // Record with final score
                stats.recordCorrectAnswer(finalScore, timeSeconds);

                // Also add XP to community progression and check for level up
                WordUnscrambleProgression progression = getOrCreateProgression(guildId);
                boolean leveledUp = progression.addXp();

                if (leveledUp) {
                    logger.info(
                            "Word Unscramble level up for guild {}: Level {} reached!",
                            guildId,
                            progression.getLevel());
                }

                // Activity-aware scheduling: next word at base interval
                if (resetScheduler != null) {
                    resetScheduler.recordSolve(guildId);
                }

                return finalResult;
            } else {
                stats.recordWrongGuess();
            }
        }

        return result;
    }

    /**
     * Gets all configured guild IDs.
     *
     * @return set of guild IDs
     */
    public Set<String> getConfiguredGuilds() {
        return new HashSet<>(guildConfigs.keySet());
    }

    /**
     * Gets the game implementation.
     *
     * @return the game implementation
     */
    public WordUnscrambleInterface getGame() {
        return game;
    }

    /**
     * Gets or creates progression for a guild.
     *
     * @param guildId the guild ID
     * @return the server progression
     */
    public WordUnscrambleProgression getOrCreateProgression(String guildId) {
        return wordUnscrambleProgression.computeIfAbsent(guildId, WordUnscrambleProgression::new);
    }

    /**
     * Gets the progression for a guild.
     *
     * @param guildId the guild ID
     * @return the server progression, or null if not initialized
     */
    public WordUnscrambleProgression getProgression(String guildId) {
        return wordUnscrambleProgression.get(guildId);
    }

    /**
     * Gets the announcement message for the current game with level info.
     *
     * @param guildId the guild ID
     * @return the announcement message, or null if no active game
     */
    public String getGameAnnouncementWithLevel(String guildId) {
        WordUnscrambleSession session = activeSessions.get(guildId);
        if (session == null) {
            return null;
        }

        WordUnscrambleProgression progression = getOrCreateProgression(guildId);
        WordUnscrambleGame wordGame = (WordUnscrambleGame) game;
        return wordGame.generateAnnouncement(session, progression.getLevel());
    }

    /**
     * Gets or creates player statistics for a user in a guild.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @return the player statistics
     */
    public WordUnscramblePlayerStats getOrCreatePlayerStats(String guildId, String userId) {
        String key = guildId + "_" + userId;
        return playerStats.computeIfAbsent(key, k -> new WordUnscramblePlayerStats(userId, guildId));
    }

    /**
     * Gets player statistics for a user in a guild.
     *
     * @param guildId the guild ID
     * @param userId the user ID
     * @return the player statistics, or null if not found
     */
    public WordUnscramblePlayerStats getPlayerStats(String guildId, String userId) {
        String key = guildId + "_" + userId;
        return playerStats.get(key);
    }

    /**
     * Gets all player statistics for a guild. Only returns players who have made at least one
     * attempt.
     *
     * @param guildId the guild ID
     * @return list of player statistics, sorted by total points (descending)
     */
    public List<WordUnscramblePlayerStats> getAllPlayerStats(String guildId) {
        String prefix = guildId + "_";
        return playerStats.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .filter(stats -> stats.getTotalAttempts() > 0) // Only players who have attempted
                .sorted(
                        (a, b) -> {
                            // Sort by total points (descending)
                            int pointsCompare = Integer.compare(b.getTotalPoints(), a.getTotalPoints());
                            if (pointsCompare != 0) {
                                return pointsCompare;
                            }
                            // Then by words solved (descending)
                            int wordsCompare = Integer.compare(b.getTotalWordsSolved(), a.getTotalWordsSolved());
                            if (wordsCompare != 0) {
                                return wordsCompare;
                            }
                            // Then by highest score (descending)
                            return Integer.compare(b.getHighestScore(), a.getHighestScore());
                        })
                .collect(Collectors.toList());
    }

    /**
     * Starts a random game from the enabled games for a guild.
     *
     * @param guildId the guild ID
     * @return the new session, or null if no games are enabled
     */
    public WordUnscrambleSession startRandomEnabledGame(String guildId) {
        WordUnscrambleConfig config = guildConfigs.get(guildId);
        if (config == null || config.getEnabledGames().isEmpty()) {
            return null;
        }

        // Pick a random enabled game (currently only WORD_UNSCRAMBLE)
        List<WordUnscrambleType> enabledList = new ArrayList<>(config.getEnabledGames());
        WordUnscrambleType randomType = enabledList.get(random.nextInt(enabledList.size()));

        return startNewGame(guildId, randomType);
    }

    /**
     * Calculates volume bonuses (10th word milestone, 3-streak bonus).
     *
     * @param stats the player stats (before recording this solve)
     * @return the volume bonus points
     */
    private int calculateVolumeBonuses(WordUnscramblePlayerStats stats) {
        int bonus = 0;

        // Check for 10th word milestone (words solved will be incremented after this, so check if next
        // will be 10th)
        int wordsSolved = stats.getTotalWordsSolved();
        if ((wordsSolved + 1) % 10 == 0) {
            // Every 10th word: +100 to +200 points (randomized)
            bonus += 100 + random.nextInt(101); // 100-200
            logger.debug("10th word milestone bonus: {}", bonus);
        }

        // Check for 3-streak bonus (current streak will be incremented, so check if next will be 3)
        int currentStreak = stats.getCurrentStreak();
        if (currentStreak + 1 == 3) {
            bonus += 50;
            logger.debug("3-streak bonus: 50");
        }

        return bonus;
    }

    /**
     * Generates a hint for the current word.
     *
     * @param guildId the guild ID
     * @param session the game session
     * @return a hint string
     */
    public String generateHint(String guildId, WordUnscrambleSession session) {
        WordUnscrambleGame wordGame = (WordUnscrambleGame) game;
        return wordGame.generateHintForWord(session.getCorrectAnswer(), session.getLevel());
    }
}
