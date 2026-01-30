package com.tatumgames.mikros.games.word_unscramble.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WordUnscrambleUsedWordTracker.
 */
class WordUnscrambleUsedWordTrackerTest {

    private WordUnscrambleUsedWordTracker tracker;
    private static final String GUILD_ID = "test-guild-123";
    private static final String GUILD_ID_2 = "test-guild-456";

    @BeforeEach
    void setUp() {
        tracker = new WordUnscrambleUsedWordTracker();
    }

    // --- recordWordUsed ---

    @Test
    @DisplayName("Records a word for guild and level 1-5")
    void recordWordUsed_recordsForValidLevel() {
        tracker.recordWordUsed(GUILD_ID, 3, "apple");
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 3, "apple"));
    }

    @Test
    @DisplayName("Ignores level 0")
    void recordWordUsed_ignoresLevel0() {
        tracker.recordWordUsed(GUILD_ID, 0, "word");
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 0, "word"));
    }

    @Test
    @DisplayName("Ignores level 6")
    void recordWordUsed_ignoresLevel6() {
        tracker.recordWordUsed(GUILD_ID, 6, "word");
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 6, "word"));
    }

    @Test
    @DisplayName("Same word can be overwritten with new timestamp")
    void recordWordUsed_overwritesSameWord() {
        tracker.recordWordUsed(GUILD_ID, 2, "test");
        tracker.recordWordUsed(GUILD_ID, 2, "test");
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 2, "test"));
    }

    // --- isWordRecentlyUsed ---

    @Test
    @DisplayName("Returns false when word was never recorded")
    void isWordRecentlyUsed_returnsFalseWhenNeverRecorded() {
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 1, "unknown"));
    }

    @Test
    @DisplayName("Returns true when word was just recorded")
    void isWordRecentlyUsed_returnsTrueWhenJustRecorded() {
        tracker.recordWordUsed(GUILD_ID, 1, "hello");
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 1, "hello"));
    }

    @Test
    @DisplayName("Returns false when word was recorded but cleanup removed it")
    void isWordRecentlyUsed_returnsFalseAfterCleanupWithCutoff() {
        tracker.recordWordUsed(GUILD_ID, 1, "cleanme");
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 1, "cleanme"));
        tracker.cleanupOldEntries(GUILD_ID, Instant.now().plusSeconds(1));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 1, "cleanme"));
    }

    @Test
    @DisplayName("Returns false for level outside 1-5")
    void isWordRecentlyUsed_returnsFalseForLevel0() {
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 0, "any"));
    }

    @Test
    @DisplayName("Returns false for level 6")
    void isWordRecentlyUsed_returnsFalseForLevel6() {
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 6, "any"));
    }

    // --- filterRecentlyUsedWords ---

    @Test
    @DisplayName("Filters out recently used words")
    void filterRecentlyUsedWords_filtersUsedWords() {
        tracker.recordWordUsed(GUILD_ID, 2, "used");
        List<String> words = Arrays.asList("used", "fresh", "other");
        List<String> result = tracker.filterRecentlyUsedWords(GUILD_ID, 2, words);
        assertEquals(2, result.size());
        assertTrue(result.contains("fresh"));
        assertTrue(result.contains("other"));
        assertFalse(result.contains("used"));
    }

    @Test
    @DisplayName("When all words recently used, resets level and returns all words")
    void filterRecentlyUsedWords_resetsWhenAllUsed() {
        tracker.recordWordUsed(GUILD_ID, 1, "a");
        tracker.recordWordUsed(GUILD_ID, 1, "b");
        List<String> words = Arrays.asList("a", "b");
        List<String> result = tracker.filterRecentlyUsedWords(GUILD_ID, 1, words);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(words));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 1, "a"));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 1, "b"));
    }

    @Test
    @DisplayName("Level outside 1-5 returns copy of input")
    void filterRecentlyUsedWords_levelOutOfRangeReturnsCopy() {
        List<String> words = Arrays.asList("x", "y");
        List<String> result = tracker.filterRecentlyUsedWords(GUILD_ID, 0, words);
        assertEquals(2, result.size());
        assertEquals(words, result);
        result = tracker.filterRecentlyUsedWords(GUILD_ID, 6, words);
        assertEquals(2, result.size());
        assertEquals(words, result);
    }

    // --- resetLevelTracking ---

    @Test
    @DisplayName("Removes all words for guild and level")
    void resetLevelTracking_removesWords() {
        tracker.recordWordUsed(GUILD_ID, 2, "word1");
        tracker.recordWordUsed(GUILD_ID, 2, "word2");
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 2, "word1"));
        tracker.resetLevelTracking(GUILD_ID, 2);
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 2, "word1"));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 2, "word2"));
    }

    @Test
    @DisplayName("Reset is no-op for unknown guild")
    void resetLevelTracking_noOpForUnknownGuild() {
        tracker.recordWordUsed(GUILD_ID, 1, "word");
        tracker.resetLevelTracking("unknown-guild", 1);
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 1, "word"));
    }

    @Test
    @DisplayName("Reset is no-op for level out of range")
    void resetLevelTracking_noOpForLevel0() {
        tracker.recordWordUsed(GUILD_ID, 1, "word");
        tracker.resetLevelTracking(GUILD_ID, 0);
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 1, "word"));
    }

    // --- cleanupOldEntries(String guildId) ---

    @Test
    @DisplayName("cleanupOldEntries with cutoff removes entries before cutoff")
    void cleanupOldEntries_withCutoffRemovesOldEntries() {
        tracker.recordWordUsed(GUILD_ID, 1, "old");
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 1, "old"));
        tracker.cleanupOldEntries(GUILD_ID, Instant.now().plusSeconds(1));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 1, "old"));
    }

    @Test
    @DisplayName("cleanupOldEntries with null cleans all guilds")
    void cleanupOldEntries_nullCleansAllGuilds() {
        tracker.recordWordUsed(GUILD_ID, 1, "a");
        tracker.recordWordUsed(GUILD_ID_2, 1, "b");
        tracker.cleanupOldEntries(null, Instant.now().plusSeconds(1));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 1, "a"));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID_2, 1, "b"));
    }

    // --- cleanupOldEntries(String guildId, Instant cutoff) ---

    @Test
    @DisplayName("Entries after cutoff remain")
    void cleanupOldEntries_entriesAfterCutoffRemain() {
        tracker.recordWordUsed(GUILD_ID, 1, "recent");
        tracker.cleanupOldEntries(GUILD_ID, Instant.now().minusSeconds(1));
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 1, "recent"));
    }

    @Test
    @DisplayName("Empty level maps removed after cleanup")
    void cleanupOldEntries_removesEmptyLevelMaps() {
        tracker.recordWordUsed(GUILD_ID, 1, "only");
        tracker.cleanupOldEntries(GUILD_ID, Instant.now().plusSeconds(1));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 1, "only"));
        tracker.recordWordUsed(GUILD_ID, 2, "other");
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 2, "other"));
    }

    @Test
    @DisplayName("cleanupOldEntries with null cleans all guilds")
    void cleanupOldEntries_withCutoffNullCleansAllGuilds() {
        tracker.recordWordUsed(GUILD_ID, 1, "g1");
        tracker.recordWordUsed(GUILD_ID_2, 2, "g2");
        tracker.cleanupOldEntries(null, Instant.now().plusSeconds(1));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 1, "g1"));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID_2, 2, "g2"));
    }

    @Test
    @DisplayName("cleanupOldEntries with guildId cleans only that guild")
    void cleanupOldEntries_withGuildIdCleansOnlyThatGuild() {
        tracker.recordWordUsed(GUILD_ID, 1, "g1word");
        tracker.recordWordUsed(GUILD_ID_2, 1, "g2word");
        tracker.cleanupOldEntries(GUILD_ID, Instant.now().plusSeconds(1));
        assertFalse(tracker.isWordRecentlyUsed(GUILD_ID, 1, "g1word"));
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID_2, 1, "g2word"));
    }

    // --- Edge cases ---

    @Test
    @DisplayName("cleanupOldEntries(null) does not throw when no guilds exist")
    void cleanupOldEntries_nullNoNpeWhenNoGuilds() {
        assertDoesNotThrow(() -> tracker.cleanupOldEntries(null));
        assertDoesNotThrow(() -> tracker.cleanupOldEntries(null, Instant.now()));
    }

    @Test
    @DisplayName("cleanupOldEntries(unknownGuild) is no-op")
    void cleanupOldEntries_unknownGuildNoOp() {
        tracker.recordWordUsed(GUILD_ID, 1, "word");
        assertDoesNotThrow(() -> tracker.cleanupOldEntries("unknown-guild"));
        assertDoesNotThrow(() -> tracker.cleanupOldEntries("unknown-guild", Instant.now()));
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 1, "word"));
    }

    @Test
    @DisplayName("cleanupOldEntries with null cutoff does not remove entries")
    void cleanupOldEntries_nullCutoffDoesNotRemove() {
        tracker.recordWordUsed(GUILD_ID, 1, "word");
        tracker.cleanupOldEntries(GUILD_ID, null);
        assertTrue(tracker.isWordRecentlyUsed(GUILD_ID, 1, "word"));
    }
}
