package com.tatumgames.mikros.games.word_unscramble.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks used words per guild and level to prevent repetition.
 * Used for levels 1-5 to ensure words don't repeat for a certain period (default: 2 months).
 */
public class WordUnscrambleUsedWordTracker {
    private static final Logger logger = LoggerFactory.getLogger(WordUnscrambleUsedWordTracker.class);

    // Period in days before a word can be reused (default: 60 days / 2 months)
    private static final long REUSE_PERIOD_DAYS = 60;

    // Storage: guildId -> level -> word -> lastUsedTime
    private final Map<String, Map<Integer, Map<String, Instant>>> usedWords;

    public WordUnscrambleUsedWordTracker() {
        this.usedWords = new ConcurrentHashMap<>();
        logger.info("WordUnscrambleUsedWordTracker initialized");
    }

    /**
     * Records that a word was used for a specific guild and level.
     *
     * @param guildId the guild ID
     * @param level   the level (1-5)
     * @param word    the word that was used
     */
    public void recordWordUsed(String guildId, int level, String word) {
        if (level < 1 || level > 5) {
            // Only track levels 1-5
            return;
        }

        usedWords.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(level, k -> new ConcurrentHashMap<>())
                .put(word, Instant.now());

        logger.debug("Recorded word '{}' used for guild {} at level {}", word, guildId, level);
    }

    /**
     * Checks if a word was recently used (within the reuse period).
     *
     * @param guildId the guild ID
     * @param level   the level (1-5)
     * @param word    the word to check
     * @return true if word was used recently, false otherwise
     */
    public boolean isWordRecentlyUsed(String guildId, int level, String word) {
        if (level < 1 || level > 5) {
            // Only track levels 1-5
            return false;
        }

        Map<Integer, Map<String, Instant>> guildLevels = usedWords.get(guildId);
        if (guildLevels == null) {
            return false;
        }

        Map<String, Instant> levelWords = guildLevels.get(level);
        if (levelWords == null) {
            return false;
        }

        Instant lastUsed = levelWords.get(word);
        if (lastUsed == null) {
            return false;
        }

        long daysSinceUsed = ChronoUnit.DAYS.between(lastUsed, Instant.now());
        return daysSinceUsed < REUSE_PERIOD_DAYS;
    }

    /**
     * Filters out recently used words from a list.
     *
     * @param guildId the guild ID
     * @param level   the level (1-5)
     * @param words   the list of words to filter
     * @return list of words that haven't been used recently
     */
    public List<String> filterRecentlyUsedWords(String guildId, int level, List<String> words) {
        if (level < 1 || level > 5) {
            // Only filter levels 1-5
            return new ArrayList<>(words);
        }

        List<String> availableWords = new ArrayList<>();
        for (String word : words) {
            if (!isWordRecentlyUsed(guildId, level, word)) {
                availableWords.add(word);
            }
        }

        // If all words have been used, reset tracking for this level and return all words
        if (availableWords.isEmpty() && !words.isEmpty()) {
            logger.info("All words used for guild {} at level {}, resetting tracking", guildId, level);
            resetLevelTracking(guildId, level);
            return new ArrayList<>(words);
        }

        return availableWords;
    }

    /**
     * Resets tracking for a specific guild and level.
     *
     * @param guildId the guild ID
     * @param level   the level (1-5)
     */
    public void resetLevelTracking(String guildId, int level) {
        if (level < 1 || level > 5) {
            return;
        }

        Map<Integer, Map<String, Instant>> guildLevels = usedWords.get(guildId);
        if (guildLevels != null) {
            guildLevels.remove(level);
            logger.info("Reset word tracking for guild {} at level {}", guildId, level);
        }
    }

    /**
     * Cleans up old entries (words older than reuse period).
     * Should be called periodically to prevent memory bloat.
     *
     * @param guildId the guild ID (null to clean all guilds)
     */
    public void cleanupOldEntries(String guildId) {
        cleanupOldEntries(guildId, Instant.now().minus(REUSE_PERIOD_DAYS, ChronoUnit.DAYS));
    }

    /**
     * Cleans up entries with lastUsed before the given cutoff.
     * Useful for testing (e.g. pass Instant.now() to remove all entries).
     *
     * @param guildId the guild ID (null to clean all guilds)
     * @param cutoff  entries with lastUsed before this time are removed
     */
    public void cleanupOldEntries(String guildId, Instant cutoff) {
        if (cutoff == null) {
            return;
        }
        if (guildId != null) {
            Map<Integer, Map<String, Instant>> guildLevels = usedWords.get(guildId);
            if (guildLevels != null) {
                cleanupGuildLevels(guildLevels, cutoff);
            }
        } else {
            // Clean all guilds
            for (Map<Integer, Map<String, Instant>> guildLevels : usedWords.values()) {
                cleanupGuildLevels(guildLevels, cutoff);
            }
        }
    }

    /**
     * Cleans up old entries for a guild's levels.
     *
     * @param guildLevels the guild's level map
     * @param cutoff      the cutoff time
     */
    private void cleanupGuildLevels(Map<Integer, Map<String, Instant>> guildLevels, Instant cutoff) {
        for (Map<String, Instant> levelWords : guildLevels.values()) {
            levelWords.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        }

        // Remove empty level maps
        guildLevels.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
