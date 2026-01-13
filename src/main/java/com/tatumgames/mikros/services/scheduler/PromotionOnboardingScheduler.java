package com.tatumgames.mikros.services.scheduler;

import com.tatumgames.mikros.models.PromotionVerbosity;
import com.tatumgames.mikros.services.GamePromotionService;
import com.tatumgames.mikros.services.PromotionOnboardingService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Scheduler for promotion channel onboarding phases.
 * Runs every 30 minutes to check guilds and execute appropriate onboarding phases.
 */
public class PromotionOnboardingScheduler {
    private static final Logger logger = LoggerFactory.getLogger(PromotionOnboardingScheduler.class);

    private static final long CHECK_INTERVAL_MINUTES = 30;

    // Channel names to match (case-insensitive, in priority order)
    private static final List<String> PREFERRED_CHANNEL_NAMES = Arrays.asList(
            "announcements",
            "promotions",
            "game-updates",
            "community-news"
    );

    // Channel names to never match
    private static final List<String> EXCLUDED_CHANNEL_NAMES = Arrays.asList(
            "general",
            "chat",
            "off-topic"
    );

    private final PromotionOnboardingService onboardingService;
    private final GamePromotionService gamePromotionService;
    private ScheduledExecutorService scheduler;

    /**
     * Creates a new PromotionOnboardingScheduler.
     *
     * @param onboardingService    the onboarding service
     * @param gamePromotionService the game promotion service
     */
    public PromotionOnboardingScheduler(
            PromotionOnboardingService onboardingService,
            GamePromotionService gamePromotionService) {
        this.onboardingService = onboardingService;
        this.gamePromotionService = gamePromotionService;
    }

    /**
     * Starts the onboarding scheduler.
     *
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        if (scheduler != null && !scheduler.isShutdown()) {
            logger.warn("Onboarding scheduler already started");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "promotion-onboarding-scheduler");
            t.setDaemon(true);
            return t;
        });

        // Run check every 30 minutes
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.debug("Onboarding check triggered");
                checkAllGuilds(jda);
            } catch (Exception e) {
                logger.error("Error in onboarding check", e);
            }
        }, 0, CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);

        logger.info("Promotion onboarding scheduler started (checks every {} minutes)", CHECK_INTERVAL_MINUTES);
    }

    /**
     * Stops the onboarding scheduler.
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Promotion onboarding scheduler stopped");
        }
    }

    /**
     * Checks all guilds for onboarding phases.
     *
     * @param jda the JDA instance
     */
    private void checkAllGuilds(JDA jda) {
        for (Guild guild : jda.getGuilds()) {
            try {
                checkGuildOnboarding(guild);
            } catch (Exception e) {
                logger.error("Error checking onboarding for guild {}", guild.getId(), e);
            }
        }
    }

    /**
     * Checks and executes onboarding phases for a specific guild.
     *
     * @param guild the guild to check
     */
    private void checkGuildOnboarding(Guild guild) {
        String guildId = guild.getId();

        // CRITICAL CHECK: Skip all phases if channel is already configured
        if (gamePromotionService.getPromotionChannel(guildId) != null) {
            logger.debug("Guild {} already has promotion channel configured, skipping onboarding", guildId);
            return;
        }

        // Ensure guild is recorded
        onboardingService.recordGuildFirstSeen(guildId);

        // Check and execute each phase
        PromotionOnboardingService.Phase phase1 = PromotionOnboardingService.Phase.PHASE_1_SOFT_AWARENESS;
        if (onboardingService.shouldProcessPhase(guildId, phase1)) {
            executePhase1(guild);
            onboardingService.markPhaseCompleted(guildId, phase1);
        }

        PromotionOnboardingService.Phase phase2 = PromotionOnboardingService.Phase.PHASE_2_EXPECTATION;
        if (onboardingService.shouldProcessPhase(guildId, phase2)) {
            executePhase2(guild);
            onboardingService.markPhaseCompleted(guildId, phase2);
        }

        PromotionOnboardingService.Phase phase3 = PromotionOnboardingService.Phase.PHASE_3_AUTO_ASSIST;
        if (onboardingService.shouldProcessPhase(guildId, phase3)) {
            executePhase3(guild);
            onboardingService.markPhaseCompleted(guildId, phase3);
        }
    }

    /**
     * Executes Phase 1: Soft Awareness (1 hour after first seen).
     * Sends a gentle DM to admins about opt-in promotions.
     *
     * @param guild the guild
     */
    private void executePhase1(Guild guild) {
        String message = """
                👋 Thanks for installing MIKROS
                
                Promotions are opt-in and won't post unless a channel is set.
                
                When ready, use /admin-promotion-setup to choose a channel.
                
                You can control frequency anytime.
                """;

        sendDmToAdmins(guild, message);
        logger.info("Executed Phase 1 (Soft Awareness) for guild {}", guild.getId());
    }

    /**
     * Executes Phase 2: Expectation Setting (24 hours after first seen).
     * Informs admins about upcoming auto-assist feature.
     *
     * @param guild the guild
     */
    private void executePhase2(Guild guild) {
        String message = """
                We noticed you haven't configured promotions yet.
                
                We can help by auto-selecting a channel if you have one named:
                • #announcements
                • #promotions
                • #game-updates
                • #community-news
                
                You're still in control - you can change it anytime with /admin-promotion-setup.
                """;

        sendDmToAdmins(guild, message);
        logger.info("Executed Phase 2 (Expectation Setting) for guild {}", guild.getId());
    }

    /**
     * Executes Phase 3: Auto-Assist (48 hours after first seen).
     * Attempts to auto-assign a promotion channel if a matching name is found.
     *
     * @param guild the guild
     */
    private void executePhase3(Guild guild) {
        String guildId = guild.getId();

        // Double-check channel not configured (could have been set manually)
        if (gamePromotionService.getPromotionChannel(guildId) != null) {
            logger.debug("Guild {} channel configured before Phase 3, skipping auto-assign", guildId);
            return;
        }

        // Search for matching channel
        MessageChannel matchedChannel = findMatchingChannel(guild);

        if (matchedChannel != null) {
            // Auto-assign channel
            gamePromotionService.setPromotionChannel(guildId, matchedChannel.getId());

            // Set default verbosity to MEDIUM
            gamePromotionService.setPromotionVerbosity(guildId, PromotionVerbosity.MEDIUM);

            // Send confirmation DM
            String confirmMessage = String.format(
                    "✅ Auto-configured promotion channel: %s\n\n" +
                            "You can change this anytime with /admin-promotion-setup.",
                    matchedChannel.getAsMention()
            );
            sendDmToAdmins(guild, confirmMessage);

            logger.info("Executed Phase 3 (Auto-Assist) for guild {} - auto-assigned channel {}",
                    guildId, matchedChannel.getId());
        } else {
            logger.info("Executed Phase 3 (Auto-Assist) for guild {} - no matching channel found", guildId);
        }
    }

    /**
     * Finds a matching channel in the guild based on preferred names.
     * Returns the first match found in priority order.
     *
     * @param guild the guild to search
     * @return the matching channel, or null if none found
     */
    private MessageChannel findMatchingChannel(Guild guild) {
        List<TextChannel> textChannels = guild.getTextChannels();

        // Search in priority order
        for (String preferredName : PREFERRED_CHANNEL_NAMES) {
            for (TextChannel channel : textChannels) {
                String channelName = channel.getName().toLowerCase();

                // Check if name matches (case-insensitive)
                if (channelName.equals(preferredName.toLowerCase())) {
                    // Verify not in excluded list (shouldn't happen, but double-check)
                    if (!EXCLUDED_CHANNEL_NAMES.contains(channelName)) {
                        // Verify bot can send messages
                        if (channel.canTalk()) {
                            return channel;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Sends a DM to all administrators in a guild.
     *
     * @param guild   the guild
     * @param message the message to send
     */
    private void sendDmToAdmins(Guild guild, String message) {
        List<Member> admins = guild.getMembers().stream()
                .filter(m -> m.hasPermission(Permission.ADMINISTRATOR))
                .filter(m -> !m.getUser().isBot())
                .collect(Collectors.toList());

        if (admins.isEmpty()) {
            logger.warn("No administrators found in guild {} to send onboarding DM", guild.getId());
            return;
        }

        for (Member admin : admins) {
            admin.getUser().openPrivateChannel().queue(
                    channel -> channel.sendMessage(message).queue(
                            success -> logger.debug("Sent onboarding DM to admin {} in guild {}",
                                    admin.getId(), guild.getId()),
                            error -> logger.warn("Failed to send onboarding DM to admin {}: {}",
                                    admin.getId(), error.getMessage())
                    ),
                    error -> logger.warn("Failed to open DM channel for admin {}: {}",
                            admin.getId(), error.getMessage())
            );
        }
    }
}

