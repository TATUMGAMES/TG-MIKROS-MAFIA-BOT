package com.tatumgames.mikros.botdetection.listener;

import com.tatumgames.mikros.botdetection.config.BotDetectionConfig;
import com.tatumgames.mikros.botdetection.model.BotDetectionResult;
import com.tatumgames.mikros.botdetection.service.BotDetectionService;
import com.tatumgames.mikros.models.BehaviorCategory;
import com.tatumgames.mikros.models.BehaviorReport;
import com.tatumgames.mikros.services.ReputationService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Listener for detecting bot behavior and automatically reporting to reputation system.
 */
public class BotDetectionMessageListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BotDetectionMessageListener.class);
    private static final String BOT_DETECTION_SYSTEM_ID = "BOT_DETECTION_SYSTEM";
    private static final String BOT_DETECTION_SYSTEM_NAME = "Bot Detection System";
    private final BotDetectionService botDetectionService;
    private final ReputationService reputationService;

    /**
     * Creates a new BotDetectionMessageListener.
     *
     * @param botDetectionService the bot detection service
     * @param reputationService   the reputation service
     */
    public BotDetectionMessageListener(BotDetectionService botDetectionService,
                                       ReputationService reputationService) {
        this.botDetectionService = botDetectionService;
        this.reputationService = reputationService;
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

        // Skip admins (they can bypass detection)
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        String guildId = event.getGuild().getId();
        BotDetectionConfig config = botDetectionService.getConfig(guildId);

        // Check if detection is enabled for this guild
        if (!config.isEnabled()) {
            return;
        }

        // Detect bot behavior
        BotDetectionResult result = botDetectionService.detectBotBehavior(event);

        if (!result.isBotDetected()) {
            return;
        }

        // Only take action on HIGH confidence detections (or if configured)
        if (result.confidence() != BotDetectionResult.Confidence.HIGH &&
                config.getAutoAction() == BotDetectionConfig.AutoAction.NONE) {
            logger.debug("Bot detected with {} confidence, but auto-action is NONE", result.confidence());
            return;
        }

        User user = event.getAuthor();
        logger.warn("Bot detected: user {} in guild {} - Reason: {}, Confidence: {}",
                user.getId(), guildId, result.detectionReason(), result.confidence());

        // Take action based on config
        handleBotDetection(event, result, config);

        // Record prevention count
        botDetectionService.recordBotPrevention(guildId);

        // Report to reputation system
        if (config.isReportToReputation()) {
            reportBotToReputation(event, result);
        }
    }

    /**
     * Handles bot detection by taking the configured action.
     *
     * @param event  the message event
     * @param result the detection result
     * @param config the bot detection config
     */
    private void handleBotDetection(MessageReceivedEvent event, BotDetectionResult result,
                                    BotDetectionConfig config) {
        BotDetectionConfig.AutoAction action = config.getAutoAction();
        if (action == BotDetectionConfig.AutoAction.NONE) {
            return;
        }

        User user = event.getAuthor();
        MessageChannel channel = event.getChannel();
        String guildId = event.getGuild().getId();

        // Declare member variable before switch to avoid scope issues
        Member member = event.getMember();
        
        switch (action) {
            case NONE:
                // No action taken
                break;
            case DELETE:
                event.getMessage().delete().queue(
                        success -> logger.info("Deleted bot message from user {} in guild {}", user.getId(), guildId),
                        error -> logger.warn("Failed to delete bot message: {}", error.getMessage())
                );
                // Send warning message
                channel.sendMessage(String.format(
                        "⚠️ **%s**, links are restricted for new accounts to prevent spam. " +
                                "Please wait %d minutes after joining before posting links.",
                        user.getAsMention(), config.getLinkRestrictionMinutes()
                )).queue(
                        msg -> msg.delete().queueAfter(10, java.util.concurrent.TimeUnit.SECONDS),
                        error -> logger.warn("Failed to send warning message: {}", error.getMessage())
                );
                break;

            case WARN:
                channel.sendMessage(String.format(
                        "⚠️ **%s**, your message was flagged as potential spam. " +
                                "Please review our server rules.",
                        user.getAsMention()
                )).queue();
                break;

            case MUTE:
                if (member != null && event.getGuild().getSelfMember().canInteract(member)) {
                    // Note: Mute requires a timeout role or timeout API
                    // For simplicity, we'll use timeout (1 hour)
                    member.timeoutFor(1, java.util.concurrent.TimeUnit.HOURS)
                            .reason("Bot detection: " + result.details())
                            .queue(
                                    success -> logger.info("Muted user {} for bot detection", user.getId()),
                                    error -> logger.warn("Failed to mute user: {}", error.getMessage())
                            );
                }
                event.getMessage().delete().queue();
                break;

            case KICK:
                member = event.getMember();
                if (member != null && event.getGuild().getSelfMember().canInteract(member)) {
                    event.getGuild().kick(member)
                            .reason("Bot detection: " + result.details())
                            .queue(
                                    success -> logger.info("Kicked user {} for bot detection", user.getId()),
                                    error -> logger.warn("Failed to kick user: {}", error.getMessage())
                            );
                }
                event.getMessage().delete().queue();
                break;
        }
    }

    /**
     * Reports detected bot to the reputation system.
     *
     * @param event  the message event
     * @param result the detection result
     */
    private void reportBotToReputation(MessageReceivedEvent event, BotDetectionResult result) {
        User user = event.getAuthor();
        String guildId = event.getGuild().getId();

        // Check cooldown to prevent spam reporting
        if (botDetectionService.getConfig(guildId).isReportToReputation()) {
            // Create behavior report
            BehaviorReport report = new BehaviorReport(
                    user.getId(),
                    user.getName(),
                    BOT_DETECTION_SYSTEM_ID,
                    BOT_DETECTION_SYSTEM_NAME,
                    BehaviorCategory.SPAMMER,
                    String.format("Auto-detected: %s - %s",
                            result.detectionReason(),
                            result.details()),
                    Instant.now(),
                    guildId
            );

            // Record and report
            reputationService.recordBehavior(report);
            boolean apiSuccess = reputationService.reportToExternalAPI(report);

            if (apiSuccess) {
                logger.info("Reported bot to reputation system: user {} in guild {}",
                        user.getId(), guildId);
            } else {
                logger.warn("Failed to report bot to reputation API: user {} in guild {}",
                        user.getId(), guildId);
            }

            // Record report for cooldown
            botDetectionService.recordReport(guildId, user.getId());
        }
    }
}

