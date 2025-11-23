package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.UserActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for tracking user activity and message statistics.
 * Stores activity data in memory (expandable to database).
 */
public class ActivityTrackingService {
    private static final Logger logger = LoggerFactory.getLogger(ActivityTrackingService.class);
    
    // Key: "guildId:userId" -> MessageCount
    private final Map<String, Integer> messageCountMap;
    
    // Key: "guildId:userId" -> Last active timestamp
    private final Map<String, Long> lastActiveMap;
    
    // Key: "guildId:channelId" -> MessageCount
    private final Map<String, Integer> channelActivityMap;
    
    // Cache for usernames (guildId:userId -> username)
    private final Map<String, String> usernameCache;
    
    /**
     * Creates a new ActivityTrackingService.
     */
    public ActivityTrackingService() {
        this.messageCountMap = new ConcurrentHashMap<>();
        this.lastActiveMap = new ConcurrentHashMap<>();
        this.channelActivityMap = new ConcurrentHashMap<>();
        this.usernameCache = new ConcurrentHashMap<>();
        logger.info("ActivityTrackingService initialized");
    }
    
    /**
     * Records a message sent by a user.
     * 
     * @param guildId the guild ID
     * @param userId the user ID
     * @param username the username
     * @param channelId the channel ID
     */
    public void recordMessage(String guildId, String userId, String username, String channelId) {
        String userKey = buildKey(guildId, userId);
        String channelKey = buildKey(guildId, channelId);
        
        messageCountMap.merge(userKey, 1, Integer::sum);
        lastActiveMap.put(userKey, System.currentTimeMillis());
        channelActivityMap.merge(channelKey, 1, Integer::sum);
        usernameCache.put(userKey, username);
    }
    
    /**
     * Gets the total message count for a guild.
     * 
     * @param guildId the guild ID
     * @return the total message count
     */
    public int getTotalMessageCount(String guildId) {
        return messageCountMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(guildId + ":"))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }
    
    /**
     * Gets the number of active users in the current month.
     * 
     * @param guildId the guild ID
     * @return the count of active users this month
     */
    public int getActiveUsersThisMonth(String guildId) {
        long monthStart = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli();
        
        return (int) lastActiveMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(guildId + ":"))
                .filter(entry -> entry.getValue() >= monthStart)
                .count();
    }
    
    /**
     * Gets the message count for a specific user.
     * 
     * @param guildId the guild ID
     * @param userId the user ID
     * @return the message count
     */
    public int getUserMessageCount(String guildId, String userId) {
        String key = buildKey(guildId, userId);
        return messageCountMap.getOrDefault(key, 0);
    }
    
    /**
     * Gets the average messages per user.
     * 
     * @param guildId the guild ID
     * @return the average message count
     */
    public double getAverageMessagesPerUser(String guildId) {
        List<Integer> counts = messageCountMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(guildId + ":"))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        
        if (counts.isEmpty()) {
            return 0.0;
        }
        
        return counts.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
    
    /**
     * Gets the most active channels.
     * 
     * @param guildId the guild ID
     * @param limit the maximum number of channels to return
     * @return map of channel ID to message count
     */
    public Map<String, Integer> getMostActiveChannels(String guildId, int limit) {
        return channelActivityMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(guildId + ":"))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(guildId.length() + 1),
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Gets the top contributors (users with most messages).
     * 
     * @param guildId the guild ID
     * @param limit the maximum number of users to return
     * @return list of UserActivity objects
     */
    public List<UserActivity> getTopContributors(String guildId, int limit) {
        return messageCountMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(guildId + ":"))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    String userId = entry.getKey().substring(guildId.length() + 1);
                    String username = usernameCache.getOrDefault(entry.getKey(), "Unknown");
                    int messageCount = entry.getValue();
                    long lastActive = lastActiveMap.getOrDefault(entry.getKey(), 0L);
                    return new UserActivity(userId, username, messageCount, lastActive);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Gets user activity statistics for a specific user.
     * 
     * @param guildId the guild ID
     * @param userId the user ID
     * @return UserActivity object or null if not found
     */
    public UserActivity getUserActivity(String guildId, String userId) {
        String key = buildKey(guildId, userId);
        if (!messageCountMap.containsKey(key)) {
            return null;
        }
        
        String username = usernameCache.getOrDefault(key, "Unknown");
        int messageCount = messageCountMap.get(key);
        long lastActive = lastActiveMap.getOrDefault(key, 0L);
        
        return new UserActivity(userId, username, messageCount, lastActive);
    }
    
    /**
     * Resets statistics for a guild (useful for monthly reports).
     * 
     * @param guildId the guild ID
     */
    public void resetGuildStats(String guildId) {
        messageCountMap.keySet().removeIf(key -> key.startsWith(guildId + ":"));
        lastActiveMap.keySet().removeIf(key -> key.startsWith(guildId + ":"));
        channelActivityMap.keySet().removeIf(key -> key.startsWith(guildId + ":"));
        usernameCache.keySet().removeIf(key -> key.startsWith(guildId + ":"));
        logger.info("Reset statistics for guild {}", guildId);
    }
    
    /**
     * Builds a composite key from guild and entity IDs.
     */
    private String buildKey(String guildId, String entityId) {
        return guildId + ":" + entityId;
    }
}

