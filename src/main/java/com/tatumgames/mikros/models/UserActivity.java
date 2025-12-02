package com.tatumgames.mikros.models;

/**
 * Represents user activity statistics.
 */
public record UserActivity(String userId, String username, int messageCount, long lastActiveTimestamp) {
    /**
     * Creates a new UserActivity.
     *
     * @param userId              the user ID
     * @param username            the username
     * @param messageCount        the number of messages sent
     * @param lastActiveTimestamp when the user was last active
     */
    public UserActivity {
    }
}

