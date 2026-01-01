package com.tatumgames.mikros.bump.listener;

import com.tatumgames.mikros.bump.model.BumpConfig;
import com.tatumgames.mikros.bump.service.BumpService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Listens for successful bump messages from Disboard and Disurl bots.
 * Tracks bump history and statistics.
 */
public class BumpDetectionListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BumpDetectionListener.class);
    
    // Bot IDs
    private static final long DISBOARD_BOT_ID = 302050872383242240L;
    private static final long DISURL_BOT_ID = 823495039178932224L;
    
    // Patterns to detect successful bump messages
    private static final Pattern DISBOARD_SUCCESS_PATTERN = Pattern.compile(
        "(?i)(bump done|successfully bumped|bumped your server|check it out on disboard)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern DISURL_SUCCESS_PATTERN = Pattern.compile(
        "(?i)(bump done|successfully bumped|bumped your server|check it out on disurl)",
        Pattern.CASE_INSENSITIVE
    );
    
    private final BumpService bumpService;
    
    public BumpDetectionListener(BumpService bumpService) {
        this.bumpService = bumpService;
    }
    
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Only check messages from Disboard/Disurl bots
        if (!event.getAuthor().isBot()) {
            return;
        }
        
        // Only check in guilds
        if (!event.isFromGuild()) {
            return;
        }
        
        long authorId = event.getAuthor().getIdLong();
        String guildId = event.getGuild().getId();
        String messageContent = event.getMessage().getContentRaw();
        
        // Check if this is a Disboard success message
        if (authorId == DISBOARD_BOT_ID) {
            if (DISBOARD_SUCCESS_PATTERN.matcher(messageContent).find()) {
                handleSuccessfulBump(event, guildId, BumpConfig.BumpBot.DISBOARD);
                return;
            }
        }
        
        // Check if this is a Disurl success message
        if (authorId == DISURL_BOT_ID) {
            if (DISURL_SUCCESS_PATTERN.matcher(messageContent).find()) {
                handleSuccessfulBump(event, guildId, BumpConfig.BumpBot.DISURL);
                return;
            }
        }
    }
    
    /**
     * Handles a successful bump detection.
     */
    private void handleSuccessfulBump(MessageReceivedEvent event, String guildId, BumpConfig.BumpBot bot) {
        try {
            // Try to find who triggered the bump by checking recent messages
            // (The user who ran /bump would have sent a message just before this)
            String userId = findBumpUser(event);
            
            Instant now = Instant.now();
            
            // Record the bump
            bumpService.recordSuccessfulBump(guildId, bot, userId, now);
            
            logger.info("Detected successful bump for {} in guild {} by user {}",
                    bot.getDisplayName(), guildId, userId != null ? userId : "unknown");
            
        } catch (Exception e) {
            logger.error("Error handling successful bump detection for {} in guild {}",
                    bot.getDisplayName(), guildId, e);
        }
    }
    
    /**
     * Attempts to find the user who triggered the bump by checking recent messages.
     * This is a best-effort approach since we can't directly detect slash command invocations.
     */
    private String findBumpUser(MessageReceivedEvent event) {
        try {
            // Check the last 10 messages in the channel
            var history = event.getChannel().getHistoryBefore(event.getMessage(), 10).complete();
            
            for (Message message : history.getRetrievedHistory()) {
                // Look for messages that might indicate a bump command
                String content = message.getContentRaw().toLowerCase();
                if (content.contains("/bump") && !message.getAuthor().isBot()) {
                    return message.getAuthor().getId();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not retrieve message history to find bump user", e);
        }
        
        return null; // Unknown user
    }
}

