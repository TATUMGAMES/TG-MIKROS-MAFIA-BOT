package com.tatumgames.mikros.models;

/**
 * Represents user activity statistics.
 */
public class UserActivity {
    private final String userId;
    private final String username;
    private final int messageCount;
    private final long lastActiveTimestamp;
    
    /**
     * Creates a new UserActivity.
     * 
     * @param userId the user ID
     * @param username the username
     * @param messageCount the number of messages sent
     * @param lastActiveTimestamp when the user was last active
     */
    public UserActivity(String userId, String username, int messageCount, long lastActiveTimestamp) {
        this.userId = userId;
        this.username = username;
        this.messageCount = messageCount;
        this.lastActiveTimestamp = lastActiveTimestamp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public int getMessageCount() {
        return messageCount;
    }
    
    public long getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }
}

