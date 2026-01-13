package com.tatumgames.mikros.botdetection.tracker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks message patterns to detect multi-channel spam.
 */
public class MessagePatternTracker {
    // Key: "userId:contentHash" -> List of MessageRecord
    private final Map<String, List<MessageRecord>> messagePatterns;

    public MessagePatternTracker() {
        this.messagePatterns = new ConcurrentHashMap<>();
    }

    /**
     * Records a message for pattern tracking.
     *
     * @param userId    the user ID
     * @param channelId the channel ID
     * @param content   the message content
     */
    public void recordMessage(String userId, String channelId, String content) {
        if (userId == null || channelId == null || content == null) {
            return;
        }

        String contentHash = hashContent(content);
        String key = userId + ":" + contentHash;

        messagePatterns.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new MessageRecord(channelId, Instant.now(), contentHash));

        // Cleanup old records periodically (every 100 messages per key)
        List<MessageRecord> records = messagePatterns.get(key);
        if (records != null && records.size() > 100) {
            cleanupOldRecords(30); // Keep last 30 minutes
        }
    }

    /**
     * Checks if a message pattern indicates multi-channel spam.
     *
     * @param userId            the user ID
     * @param contentHash       the content hash
     * @param threshold         the number of channels threshold
     * @param timeWindowSeconds the time window in seconds
     * @return true if multi-channel spam detected, false otherwise
     */
    public boolean isMultiChannelSpam(String userId, String contentHash, int threshold, int timeWindowSeconds) {
        if (userId == null || contentHash == null) {
            return false;
        }

        String key = userId + ":" + contentHash;
        List<MessageRecord> records = messagePatterns.get(key);

        if (records == null || records.size() < threshold) {
            return false;
        }

        Instant cutoff = Instant.now().minusSeconds(timeWindowSeconds);
        List<String> uniqueChannels = records.stream()
                .filter(r -> r.timestamp().isAfter(cutoff))
                .map(MessageRecord::channelId)
                .distinct()
                .toList();

        return uniqueChannels.size() >= threshold;
    }

    /**
     * Cleans up old message records.
     *
     * @param maxAgeMinutes maximum age in minutes
     */
    public void cleanupOldRecords(int maxAgeMinutes) {
        Instant cutoff = Instant.now().minusSeconds(maxAgeMinutes * 60L);

        messagePatterns.entrySet().removeIf(entry -> {
            List<MessageRecord> records = entry.getValue();
            records.removeIf(r -> r.timestamp().isBefore(cutoff));
            return records.isEmpty();
        });
    }

    /**
     * Hashes message content for comparison.
     *
     * @param content the message content
     * @return the hash
     */
    private String hashContent(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash code
            return String.valueOf(content.hashCode());
        }
    }

    /**
     * Record of a message posting.
     */
    public record MessageRecord(
            String channelId,
            Instant timestamp,
            String contentHash
    ) {}
}

