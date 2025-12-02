package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryModerationLogService.
 */
class InMemoryModerationLogServiceTest {

    private InMemoryModerationLogService service;
    private static final String GUILD_ID = "123456789";
    private static final String USER_ID = "987654321";
    private static final String MODERATOR_ID = "111222333";

    @BeforeEach
    void setUp() {
        service = new InMemoryModerationLogService();
    }

    @Test
    @DisplayName("Should log a moderation action successfully")
    void shouldLogAction() {
        ModerationAction action = createModerationAction(ActionType.WARN, "Test warning");

        assertDoesNotThrow(() -> service.logAction(action));

        List<ModerationAction> history = service.getUserHistory(USER_ID, GUILD_ID);
        assertEquals(1, history.size());
        assertEquals(action, history.get(0));
    }

    @Test
    @DisplayName("Should throw exception when logging null action")
    void shouldThrowExceptionForNullAction() {
        assertThrows(IllegalArgumentException.class, () -> service.logAction(null));
    }

    @Test
    @DisplayName("Should retrieve user history correctly")
    void shouldRetrieveUserHistory() {
        // Log multiple actions
        service.logAction(createModerationAction(ActionType.WARN, "First warning"));
        service.logAction(createModerationAction(ActionType.WARN, "Second warning"));
        service.logAction(createModerationAction(ActionType.KICK, "Kicked"));

        List<ModerationAction> history = service.getUserHistory(USER_ID, GUILD_ID);

        assertEquals(3, history.size());
    }

    @Test
    @DisplayName("Should return empty list for user with no history")
    void shouldReturnEmptyListForNoHistory() {
        List<ModerationAction> history = service.getUserHistory("nonexistent", GUILD_ID);

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("Should return history sorted by timestamp (newest first)")
    void shouldReturnHistorySortedByTimestamp() throws InterruptedException {
        // Create actions with different timestamps
        ModerationAction action1 = createModerationAction(ActionType.WARN, "First");
        Thread.sleep(10); // Small delay to ensure different timestamps
        ModerationAction action2 = createModerationAction(ActionType.WARN, "Second");
        Thread.sleep(10);
        ModerationAction action3 = createModerationAction(ActionType.WARN, "Third");

        service.logAction(action1);
        service.logAction(action2);
        service.logAction(action3);

        List<ModerationAction> history = service.getUserHistory(USER_ID, GUILD_ID);

        // Should be sorted newest first
        assertEquals("Third", history.get(0).reason());
        assertEquals("Second", history.get(1).reason());
        assertEquals("First", history.get(2).reason());
    }

    @Test
    @DisplayName("Should filter history by action type")
    void shouldFilterHistoryByActionType() {
        service.logAction(createModerationAction(ActionType.WARN, "Warning 1"));
        service.logAction(createModerationAction(ActionType.WARN, "Warning 2"));
        service.logAction(createModerationAction(ActionType.KICK, "Kick"));
        service.logAction(createModerationAction(ActionType.BAN, "Ban"));

        List<ModerationAction> warns = service.getUserHistoryByType(USER_ID, GUILD_ID, ActionType.WARN);
        List<ModerationAction> kicks = service.getUserHistoryByType(USER_ID, GUILD_ID, ActionType.KICK);
        List<ModerationAction> bans = service.getUserHistoryByType(USER_ID, GUILD_ID, ActionType.BAN);

        assertEquals(2, warns.size());
        assertEquals(1, kicks.size());
        assertEquals(1, bans.size());

        // Verify all warns are actually WARN type
        warns.forEach(action -> assertEquals(ActionType.WARN, action.actionType()));
    }

    @Test
    @DisplayName("Should throw exception for null action type filter")
    void shouldThrowExceptionForNullActionType() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getUserHistoryByType(USER_ID, GUILD_ID, null));
    }

    @Test
    @DisplayName("Should count user actions correctly")
    void shouldCountUserActions() {
        assertEquals(0, service.getUserActionCount(USER_ID, GUILD_ID));

        service.logAction(createModerationAction(ActionType.WARN, "Warning"));
        assertEquals(1, service.getUserActionCount(USER_ID, GUILD_ID));

        service.logAction(createModerationAction(ActionType.KICK, "Kick"));
        assertEquals(2, service.getUserActionCount(USER_ID, GUILD_ID));
    }

    @Test
    @DisplayName("Should isolate history by guild")
    void shouldIsolateHistoryByGuild() {
        String guild1 = "guild1";
        String guild2 = "guild2";

        ModerationAction action1 = createModerationActionWithGuild(ActionType.WARN, "Warning", guild1);
        ModerationAction action2 = createModerationActionWithGuild(ActionType.WARN, "Warning", guild2);

        service.logAction(action1);
        service.logAction(action2);

        List<ModerationAction> guild1History = service.getUserHistory(USER_ID, guild1);
        List<ModerationAction> guild2History = service.getUserHistory(USER_ID, guild2);

        assertEquals(1, guild1History.size());
        assertEquals(1, guild2History.size());
        assertEquals(guild1, guild1History.get(0).guildId());
        assertEquals(guild2, guild2History.get(0).guildId());
    }

    @Test
    @DisplayName("Should isolate history by user")
    void shouldIsolateHistoryByUser() {
        String user1 = "user1";
        String user2 = "user2";

        ModerationAction action1 = createModerationActionWithUser(ActionType.WARN, "Warning", user1);
        ModerationAction action2 = createModerationActionWithUser(ActionType.WARN, "Warning", user2);

        service.logAction(action1);
        service.logAction(action2);

        List<ModerationAction> user1History = service.getUserHistory(user1, GUILD_ID);
        List<ModerationAction> user2History = service.getUserHistory(user2, GUILD_ID);

        assertEquals(1, user1History.size());
        assertEquals(1, user2History.size());
        assertEquals(user1, user1History.get(0).targetUserId());
        assertEquals(user2, user2History.get(0).targetUserId());
    }

    @Test
    @DisplayName("Should clear all history")
    void shouldClearAllHistory() {
        service.logAction(createModerationAction(ActionType.WARN, "Warning"));
        service.logAction(createModerationAction(ActionType.KICK, "Kick"));

        assertEquals(2, service.getUserActionCount(USER_ID, GUILD_ID));

        service.clearAllHistory();

        assertEquals(0, service.getUserActionCount(USER_ID, GUILD_ID));
        assertTrue(service.getUserHistory(USER_ID, GUILD_ID).isEmpty());
    }

    @Test
    @DisplayName("Should throw exception for null or blank userId")
    void shouldThrowExceptionForInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getUserHistory(null, GUILD_ID));
        assertThrows(IllegalArgumentException.class,
                () -> service.getUserHistory("", GUILD_ID));
        assertThrows(IllegalArgumentException.class,
                () -> service.getUserHistory("   ", GUILD_ID));
    }

    @Test
    @DisplayName("Should throw exception for null or blank guildId")
    void shouldThrowExceptionForInvalidGuildId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getUserHistory(USER_ID, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.getUserHistory(USER_ID, ""));
        assertThrows(IllegalArgumentException.class,
                () -> service.getUserHistory(USER_ID, "   "));
    }

    @Test
    @DisplayName("Should handle concurrent operations safely")
    void shouldHandleConcurrentOperations() throws InterruptedException {
        int threadCount = 10;
        int actionsPerThread = 10;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < actionsPerThread; j++) {
                    service.logAction(createModerationActionWithUser(
                            ActionType.WARN,
                            "Warning from thread " + threadId,
                            "user" + threadId
                    ));
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify that all actions were logged
        int totalActions = 0;
        for (int i = 0; i < threadCount; i++) {
            totalActions += service.getUserActionCount("user" + i, GUILD_ID);
        }

        assertEquals(threadCount * actionsPerThread, totalActions);
    }

    /**
     * Helper method to create a test moderation action.
     */
    private ModerationAction createModerationAction(ActionType actionType, String reason) {
        return new ModerationAction(
                USER_ID,
                "TestUser",
                MODERATOR_ID,
                "TestModerator",
                actionType,
                reason,
                Instant.now(),
                GUILD_ID
        );
    }

    /**
     * Helper method to create a test moderation action with specific guild.
     */
    private ModerationAction createModerationActionWithGuild(ActionType actionType, String reason, String guildId) {
        return new ModerationAction(
                USER_ID,
                "TestUser",
                MODERATOR_ID,
                "TestModerator",
                actionType,
                reason,
                Instant.now(),
                guildId
        );
    }

    /**
     * Helper method to create a test moderation action with specific user.
     */
    private ModerationAction createModerationActionWithUser(ActionType actionType, String reason, String userId) {
        return new ModerationAction(
                userId,
                "TestUser",
                MODERATOR_ID,
                "TestModerator",
                actionType,
                reason,
                Instant.now(),
                GUILD_ID
        );
    }
}

