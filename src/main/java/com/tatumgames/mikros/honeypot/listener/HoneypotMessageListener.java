package com.tatumgames.mikros.honeypot.listener;

import com.tatumgames.mikros.honeypot.model.HoneypotConfig;
import com.tatumgames.mikros.honeypot.service.HoneypotService;
import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import com.tatumgames.mikros.services.MessageDeletionService;
import com.tatumgames.mikros.services.ModerationLogService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;

/**
 * Listener for monitoring honeypot channels and auto-banning users who post in them.
 */
public class HoneypotMessageListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HoneypotMessageListener.class);
    
    private final HoneypotService honeypotService;
    private final ModerationLogService moderationLogService;
    private final MessageDeletionService messageDeletionService;
    
    /**
     * Creates a new HoneypotMessageListener.
     * 
     * @param honeypotService the honeypot service
     * @param moderationLogService the moderation log service
     * @param messageDeletionService the message deletion service
     */
    public HoneypotMessageListener(HoneypotService honeypotService,
                                   ModerationLogService moderationLogService,
                                   MessageDeletionService messageDeletionService) {
        this.honeypotService = honeypotService;
        this.moderationLogService = moderationLogService;
        this.messageDeletionService = messageDeletionService;
    }
    
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Skip bot messages
        if (event.getAuthor().isBot()) {
            return;
        }
        
        // Skip DMs
        if (!event.isFromGuild()) {
            return;
        }
        
        String guildId = event.getGuild().getId();
        String channelId = event.getChannel().getId();
        
        // Check if this is a honeypot channel
        if (!honeypotService.isHoneypotChannel(guildId, channelId)) {
            return;
        }
        
        HoneypotConfig config = honeypotService.getConfig(guildId);
        if (!config.isEnabled()) {
            return;
        }
        
        User user = event.getAuthor();
        
        logger.warn("Honeypot triggered by user {} in guild {} (channel: {})", 
                user.getId(), guildId, channelId);
        
        // Log the action
        ModerationAction action = new ModerationAction(
                user.getId(),
                user.getName(),
                event.getJDA().getSelfUser().getId(),
                event.getJDA().getSelfUser().getName(),
                ActionType.BAN,
                "Honeypot trigger - Posted in honeypot channel",
                Instant.now(),
                guildId
        );
        moderationLogService.logAction(action);
        
        // Send alert to admin channel if configured
        sendAlert(event, user, config);
        
        // Handle based on silent mode
        if (config.isSilentMode()) {
            // Silent mode: just log, don't ban
            logger.info("Silent mode enabled - logging honeypot trigger for user {} without banning", user.getId());
            return;
        }
        
        // Auto-ban the user
        final int deleteDays = config.getDeleteDays() < 0 ? 7 : config.getDeleteDays();
        
        // Ban the user
        event.getGuild().ban(user, deleteDays, java.util.concurrent.TimeUnit.DAYS)
                .reason("Honeypot trigger - Posted in honeypot channel")
                .queue(
                        success -> {
                            logger.info("Auto-banned user {} for honeypot trigger in guild {}", user.getId(), guildId);
                            
                            // Delete all messages from the user
                            final int finalDeleteDays = deleteDays;
                            if (finalDeleteDays > 0) {
                                messageDeletionService.deleteAllUserMessages(event.getGuild(), user, finalDeleteDays)
                                        .thenAccept(count -> {
                                            logger.info("Deleted {} messages from user {} after honeypot ban", count, user.getId());
                                        })
                                        .exceptionally(error -> {
                                            logger.error("Error deleting messages from user {}: {}", user.getId(), error.getMessage(), error);
                                            return null;
                                        });
                            }
                        },
                        error -> {
                            logger.error("Failed to ban user {} for honeypot trigger: {}", user.getId(), error.getMessage(), error);
                        }
                );
    }
    
    /**
     * Sends an alert to the configured admin channel.
     * 
     * @param event the message event
     * @param user the user who triggered the honeypot
     * @param config the honeypot configuration
     */
    private void sendAlert(MessageReceivedEvent event, User user, HoneypotConfig config) {
        if (config.getAlertChannelId() == null) {
            return;
        }
        
        TextChannel alertChannel = event.getGuild().getTextChannelById(config.getAlertChannelId());
        if (alertChannel == null) {
            logger.warn("Alert channel {} not found for guild {}", config.getAlertChannelId(), event.getGuild().getId());
            return;
        }
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚠️ Honeypot Trigger")
                .setColor(Color.RED)
                .setDescription(String.format(
                        "User **%s** (`%s`) posted in honeypot channel **%s**",
                        user.getName(),
                        user.getId(),
                        event.getChannel().getName()
                ))
                .addField("Action", config.isSilentMode() ? "Logged (Silent Mode)" : "Banned + Messages Deleted", false)
                .addField("Delete Days", String.valueOf(config.getDeleteDays()), true)
                .setTimestamp(Instant.now())
                .setFooter("Honeypot System", null);
        
        alertChannel.sendMessageEmbeds(embed.build())
                .queue(
                        success -> logger.debug("Sent honeypot alert to channel {}", alertChannel.getName()),
                        error -> logger.error("Failed to send honeypot alert: {}", error.getMessage(), error)
                );
    }
}

