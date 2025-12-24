package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ModerationLogService.
 * Stores moderation actions in memory using a concurrent hash map.
 * <p>
 * Note: This implementation does not persist data across restarts.
 * Future versions will integrate with a database.
 */
public class InMemoryModerationLogService implements ModerationLogService {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryModerationLogService.class);

    // Key format: "guildId:userId" -> List of actions
    private final Map<String, List<ModerationAction>> actionStore;

    /**
     * Creates a new InMemoryModerationLogService.
     */
    public InMemoryModerationLogService() {
        this.actionStore = new ConcurrentHashMap<>();
        logger.info("InMemoryModerationLogService initialized");
    }

    @Override
    public void logAction(ModerationAction action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        String key = buildKey(action.guildId(), action.targetUserId());
        actionStore.computeIfAbsent(key, k -> new ArrayList<>()).add(action);

        logger.info("Logged moderation action: {}", action);

        // TODO: Call Tatum Games Reputation Score API to update user reputation
        // TODO: Implement local reputation score tracking (mock/stub for now)
    }

    @Override
    public List<ModerationAction> getUserHistory(String userId, String guildId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }

        String key = buildKey(guildId, userId);
        List<ModerationAction> actions = actionStore.getOrDefault(key, new ArrayList<>());

        // Return a sorted copy (newest first)
        return actions.stream()
                .sorted(Comparator.comparing(ModerationAction::timestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationAction> getUserHistoryByType(String userId, String guildId, ActionType actionType) {
        if (actionType == null) {
            throw new IllegalArgumentException("actionType cannot be null");
        }

        return getUserHistory(userId, guildId).stream()
                .filter(action -> action.actionType() == actionType)
                .collect(Collectors.toList());
    }

    @Override
    public int getUserActionCount(String userId, String guildId) {
        return getUserHistory(userId, guildId).size();
    }

    @Override
    public List<ModerationAction> getAllActions(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            throw new IllegalArgumentException("guildId cannot be null or blank");
        }

        // Get all actions for this guild
        return actionStore.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(guildId + ":"))
                .flatMap(entry -> entry.getValue().stream())
                .sorted(Comparator.comparing(ModerationAction::timestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void clearAllHistory() {
        actionStore.clear();
        logger.warn("All moderation history has been cleared");
    }

    /**
     * Builds a unique key for the action store.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @return the composite key
     */
    private String buildKey(String guildId, String userId) {
        return guildId + ":" + userId;
    }
}

