package com.tatumgames.mikros.promo.listener;

import com.tatumgames.mikros.promo.model.PromoTrigger;
import com.tatumgames.mikros.promo.service.PromoDetectionService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for detecting promotional triggers in messages.
 * Sends gentle prompts to users when launch-related phrases are detected.
 */
public class PromoMessageListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PromoMessageListener.class);
    private final PromoDetectionService promoService;

    /**
     * Creates a new PromoMessageListener.
     *
     * @param promoService the promotional detection service
     */
    public PromoMessageListener(PromoDetectionService promoService) {
        this.promoService = promoService;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Skip bot messages
        if (event.getAuthor().isBot()) {
            return;
        }

        // Skip DMs (only check guild messages)
        if (!event.isFromGuild()) {
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            return;
        }

        String guildId = event.getGuild().getId();
        String userId = event.getAuthor().getId();
        String messageContent = event.getMessage().getContentRaw();

        // Check if detection is enabled for this guild
        if (!promoService.getConfig(guildId).isEnabled()) {
            return;
        }

        // Detect trigger patterns
        String detectedPattern = promoService.detectTrigger(messageContent);
        if (detectedPattern == null) {
            return;
        }

        // Check cooldown
        if (!promoService.canSendPrompt(userId, guildId)) {
            logger.debug("User {} is on cooldown for promotional prompts", userId);
            return;
        }

        // Create trigger
        PromoTrigger trigger = promoService.createTrigger(
                userId,
                event.getAuthor().getName(),
                guildId,
                event.getChannel().getId(),
                messageContent,
                detectedPattern
        );

        // Send prompt
        sendPromoPrompt(event, trigger);

        // Record that prompt was sent
        promoService.recordPromptSent(userId);
        trigger.setPromptSent(true);

        logger.info("Sent promotional prompt to user {} in guild {} (pattern: {})",
                userId, guildId, detectedPattern);
    }

    /**
     * Sends a promotional prompt to the user.
     *
     * @param event   the message event
     * @param trigger the promotional trigger
     */
    private void sendPromoPrompt(MessageReceivedEvent event, PromoTrigger trigger) {
        User user = event.getAuthor();
        PromoDetectionService promoService = this.promoService;
        String guildId = trigger.getGuildId();

        String promptMessage = """
                🚀 **Looks like you're launching a game!**
                
                Want help promoting your game with MIKROS? Type `/promo-request` to get a free promo code or speak with a partner.
                
                We can help with:
                • Game launch promotions
                • Marketing campaigns
                • Advanced analytics & performance insights
                • Community building
                """;

        // Check config for where to send
        var config = promoService.getConfig(guildId);

        if (config.isSendDm()) {
            // Try DM first
            user.openPrivateChannel().queue(
                    channel -> channel.sendMessage(promptMessage).queue(
                            success -> logger.debug("Sent promo prompt DM to user {}", user.getId()),
                            error -> {
                                // Fallback to channel if DM fails
                                if (config.isSendInChannel()) {
                                    sendChannelPrompt(event, promptMessage);
                                }
                            }
                    ),
                    error -> {
                        // Fallback to channel if DM fails
                        if (config.isSendInChannel()) {
                            sendChannelPrompt(event, promptMessage);
                        }
                    }
            );
        } else if (config.isSendInChannel()) {
            // Send in channel
            sendChannelPrompt(event, promptMessage);
        }
    }

    /**
     * Sends a promotional prompt in the channel.
     *
     * @param event   the message event
     * @param message the prompt message
     */
    private void sendChannelPrompt(MessageReceivedEvent event, String message) {
        if (event.getChannel() instanceof TextChannel channel) {
            channel.sendMessage(event.getAuthor().getAsMention() + " " + message).queue(
                    success -> logger.debug("Sent promo prompt in channel {} for user {}",
                            channel.getId(), event.getAuthor().getId()),
                    error -> logger.warn("Failed to send promo prompt in channel", error)
            );
        }
    }
}





