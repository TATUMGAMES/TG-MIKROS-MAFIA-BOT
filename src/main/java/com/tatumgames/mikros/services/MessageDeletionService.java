package com.tatumgames.mikros.services;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for deleting messages across channels.
 * Handles bulk message deletion with rate limit awareness.
 */
public class MessageDeletionService {
    private static final Logger logger = LoggerFactory.getLogger(MessageDeletionService.class);
    
    // Discord bulk delete limit: 2-100 messages, max 14 days old
    private static final int BULK_DELETE_LIMIT = 100;
    private static final int BULK_DELETE_MAX_DAYS = 14;
    
    /**
     * Deletes all messages from a user across all channels in a guild.
     * 
     * @param guild the guild
     * @param user the user whose messages to delete
     * @param days the number of days to look back (-1 for all messages)
     * @return a CompletableFuture that completes with the total number of messages deleted
     */
    public CompletableFuture<Integer> deleteAllUserMessages(Guild guild, User user, int days) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        AtomicInteger totalDeleted = new AtomicInteger(0);
        List<CompletableFuture<Void>> channelFutures = new ArrayList<>();
        
        Instant cutoffTime = (days > 0 && days <= BULK_DELETE_MAX_DAYS)
                ? Instant.now().minus(days, ChronoUnit.DAYS)
                : null;
        
        // Iterate through all text channels
        for (TextChannel channel : guild.getTextChannels()) {
            CompletableFuture<Void> channelFuture = deleteUserMessagesInChannel(channel, user, cutoffTime)
                    .thenAccept(count -> {
                        totalDeleted.addAndGet(count);
                        logger.debug("Deleted {} messages from user {} in channel {}", count, user.getId(), channel.getName());
                    });
            channelFutures.add(channelFuture);
        }
        
        // Wait for all channels to complete
        CompletableFuture.allOf(channelFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    int total = totalDeleted.get();
                    logger.info("Deleted {} total messages from user {} in guild {}", total, user.getId(), guild.getId());
                    result.complete(total);
                })
                .exceptionally(error -> {
                    logger.error("Error deleting messages from user {}: {}", user.getId(), error.getMessage(), error);
                    result.completeExceptionally(error);
                    return null;
                });
        
        return result;
    }
    
    /**
     * Deletes messages from a user in a specific channel.
     * 
     * @param channel the channel
     * @param user the user whose messages to delete
     * @param cutoffTime messages older than this will be skipped (null for all)
     * @return a CompletableFuture that completes with the number of messages deleted
     */
    private CompletableFuture<Integer> deleteUserMessagesInChannel(TextChannel channel, User user, Instant cutoffTime) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        AtomicInteger deletedCount = new AtomicInteger(0);
        List<Message> messagesToDelete = new ArrayList<>();
        
        // Fetch messages in batches
        channel.getIterableHistory()
                .forEachAsync(message -> {
                    // Skip if message is too old for bulk delete
                    if (cutoffTime != null && message.getTimeCreated().toInstant().isBefore(cutoffTime)) {
                        return true; // Continue iteration
                    }
                    
                    // Check if message is from the target user
                    if (message.getAuthor().equals(user)) {
                        messagesToDelete.add(message);
                    }
                    
                    return true; // Continue iteration
                })
                .thenRun(() -> {
                    // Delete messages in batches
                    deleteMessagesInBatches(channel, messagesToDelete, deletedCount)
                            .thenRun(() -> result.complete(deletedCount.get()))
                            .exceptionally(error -> {
                                logger.error("Error deleting messages in channel {}: {}", channel.getName(), error.getMessage(), error);
                                result.completeExceptionally(error);
                                return null;
                            });
                })
                .exceptionally(error -> {
                    logger.error("Error fetching messages in channel {}: {}", channel.getName(), error.getMessage(), error);
                    result.completeExceptionally(error);
                    return null;
                });
        
        return result;
    }
    
    /**
     * Deletes messages in batches, respecting Discord's bulk delete limits.
     * 
     * @param channel the channel
     * @param messages the messages to delete
     * @param deletedCount counter for deleted messages
     * @return a CompletableFuture that completes when all batches are processed
     */
    private CompletableFuture<Void> deleteMessagesInBatches(TextChannel channel, List<Message> messages, AtomicInteger deletedCount) {
        if (messages.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        // Filter messages that are eligible for bulk delete (2-100 messages, max 14 days old)
        Instant bulkDeleteCutoff = Instant.now().minus(BULK_DELETE_MAX_DAYS, ChronoUnit.DAYS);
        List<Message> bulkDeleteMessages = new ArrayList<>();
        List<Message> individualDeleteMessages = new ArrayList<>();
        
        for (Message message : messages) {
            if (message.getTimeCreated().toInstant().isAfter(bulkDeleteCutoff)) {
                bulkDeleteMessages.add(message);
            } else {
                individualDeleteMessages.add(message);
            }
        }
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // Delete in bulk batches (2-100 messages)
        for (int i = 0; i < bulkDeleteMessages.size(); i += BULK_DELETE_LIMIT) {
            int end = Math.min(i + BULK_DELETE_LIMIT, bulkDeleteMessages.size());
            List<Message> batch = bulkDeleteMessages.subList(i, end);
            
            // Bulk delete requires at least 2 messages
            if (batch.size() >= 2) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    channel.deleteMessages(batch).queue(
                            success -> {
                                deletedCount.addAndGet(batch.size());
                                logger.debug("Bulk deleted {} messages in channel {}", batch.size(), channel.getName());
                            },
                            error -> {
                                logger.warn("Bulk delete failed, falling back to individual deletion: {}", error.getMessage());
                                // Fall back to individual deletion
                                deleteIndividually(channel, batch, deletedCount);
                            }
                    );
                });
                futures.add(future);
            } else {
                // Too few messages for bulk delete, delete individually
                individualDeleteMessages.addAll(batch);
            }
        }
        
        // Delete old messages individually
        if (!individualDeleteMessages.isEmpty()) {
            deleteIndividually(channel, individualDeleteMessages, deletedCount);
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    /**
     * Deletes messages individually (for messages older than 14 days).
     * 
     * @param channel the channel
     * @param messages the messages to delete
     * @param deletedCount counter for deleted messages
     */
    private void deleteIndividually(MessageChannel channel, List<Message> messages, AtomicInteger deletedCount) {
        for (Message message : messages) {
            message.delete()
                    .queue(
                            success -> deletedCount.incrementAndGet(),
                            error -> logger.warn("Failed to delete individual message {}: {}", message.getId(), error.getMessage())
                    );
        }
    }
}

