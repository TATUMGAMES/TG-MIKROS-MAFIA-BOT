package com.tatumgames.mikros.services.scheduler;

import com.tatumgames.mikros.models.AppPromotion;
import com.tatumgames.mikros.models.PromotionVerbosity;
import com.tatumgames.mikros.promo.manager.PromotionStepManager;
import com.tatumgames.mikros.promo.template.PromotionMessageTemplates;
import com.tatumgames.mikros.services.GamePromotionService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Scheduler service for posting app promotions at configured intervals.
 * Uses 4-step promotion story format while respecting campaign dates and avoiding spam.
 * Checks every 60 minutes and posts promotions based on guild verbosity settings.
 */
public class GamePromotionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(GamePromotionScheduler.class);

    private final GamePromotionService gamePromotionService;
    private final PromotionStepManager stepManager;
    private final PromotionMessageTemplates messageTemplates;
    private final ScheduledExecutorService scheduler;
    private final Random random;
    private JDA jda;

    /**
     * Creates a new GamePromotionScheduler.
     *
     * @param gamePromotionService the game promotion service
     */
    public GamePromotionScheduler(GamePromotionService gamePromotionService) {
        this.gamePromotionService = gamePromotionService;
        this.stepManager = new PromotionStepManager();
        this.messageTemplates = new PromotionMessageTemplates();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.random = new Random();
        logger.info("GamePromotionScheduler initialized");
    }

    /**
     * Starts the promotion scheduler.
     * Checks at intervals based on guild verbosity settings (LOW: 24h, MEDIUM: 12h, HIGH: 6h).
     * Default check interval is 60 minutes to ensure all verbosity levels are respected.
     *
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        this.jda = jda;

        // Run check every 60 minutes (ensures we catch all verbosity levels)
        // Actual posting respects verbosity settings per guild
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndPostPromotions();
            } catch (Exception e) {
                logger.error("Error in promotion scheduler", e);
            }
        }, 0, 60, TimeUnit.MINUTES);

        logger.info("Game promotion scheduler started (checks every 60 minutes, respects verbosity per guild)");
    }

    /**
     * Checks all guilds and posts promotions if it's time based on verbosity.
     */
    private void checkAndPostPromotions() {
        if (jda == null) {
            logger.warn("JDA not initialized, skipping promotion check");
            return;
        }

        logger.info("Running scheduled promotion check...");

        for (Guild guild : jda.getGuilds()) {
            try {
                checkGuildPromotions(guild);
            } catch (Exception e) {
                logger.error("Error checking promotions for guild {}", guild.getId(), e);
            }
        }
    }

    /**
     * Checks and posts promotions for a specific guild.
     *
     * @param guild the guild to check
     */
    private void checkGuildPromotions(Guild guild) {
        String guildId = guild.getId();

        // Get configured promotion channel
        String channelId = gamePromotionService.getPromotionChannel(guildId);
        if (channelId == null) {
            // Guild hasn't set up promotions
            return;
        }

        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            logger.warn("Configured promotion channel {} not found in guild {}", channelId, guildId);
            return;
        }

        postPromotionsToChannel(guild, channel);
    }

    /**
     * Manually triggers promotion check for a specific guild.
     * Used by the /force-promotion-check command.
     *
     * @param guild the guild
     * @return number of promotions posted
     */
    public int forceCheckGuild(Guild guild) {
        String guildId = guild.getId();

        String channelId = gamePromotionService.getPromotionChannel(guildId);
        if (channelId == null) {
            logger.info("Guild {} has no promotion channel configured", guildId);
            return 0;
        }

        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            logger.warn("Configured promotion channel {} not found in guild {}", channelId, guildId);
            return 0;
        }

        return postPromotionsToChannel(guild, channel);
    }

    /**
     * Posts promotions to a channel using the 4-step story format.
     * Respects guild verbosity settings to determine if enough time has passed.
     *
     * @param guild   the guild
     * @param channel the channel to post in
     * @return number of promotions posted
     */
    private int postPromotionsToChannel(Guild guild, TextChannel channel) {
        String guildId = guild.getId();

        // Check verbosity to determine if we should check for promotions
        PromotionVerbosity verbosity = gamePromotionService.getPromotionVerbosity(guildId);
        Instant lastCheckTime = getLastCheckTime(guildId);
        Instant now = Instant.now();

        if (lastCheckTime != null) {
            long hoursSinceLastCheck = java.time.temporal.ChronoUnit.HOURS.between(lastCheckTime, now);
            if (hoursSinceLastCheck < verbosity.getHoursInterval()) {
                logger.debug("Guild {} verbosity check: {} hours since last check, need {} hours",
                        guildId, hoursSinceLastCheck, verbosity.getHoursInterval());
                return 0;
            }
        }

        // Record this check time
        recordLastCheckTime(guildId, now);

        // Fetch all apps from /getAllApps (stub for now)
        List<AppPromotion> allApps = gamePromotionService.fetchAllApps();

        if (allApps.isEmpty()) {
            logger.debug("No apps available for guild {}", guildId);
            return 0;
        }

        // Filter to only active campaigns
        List<AppPromotion> activeApps = allApps.stream()
                .filter(AppPromotion::isCampaignActive)
                .collect(Collectors.toList());

        if (activeApps.isEmpty()) {
            logger.debug("No active campaigns for guild {}", guildId);
            return 0;
        }

        int posted = 0;

        // Check if we should post step 3 (multi-game promotion) using consolidated logic
        if (!activeApps.isEmpty()) {
            AppPromotion firstApp = activeApps.get(0);
            int lastStepForFirstApp = gamePromotionService.getLastPromotionStep(guildId, firstApp.getAppId());

            if (firstApp.getCampaign() != null && stepManager.shouldPostStep3(
                    activeApps, lastStepForFirstApp,
                    firstApp.getCampaign().getStartDate(),
                    firstApp.getCampaign().getEndDate(),
                    now)) {
                try {
                    postMultiGamePromotion(channel, activeApps);
                    // Record step 3 for all apps (so we don't post it again)
                    for (AppPromotion app : activeApps) {
                        gamePromotionService.recordPromotionStep(guildId, app.getAppId(), 3, now);
                    }
                    posted++;
                    logger.info("Posted multi-game promotion (step 3) in guild {}", guildId);
                } catch (Exception e) {
                    logger.error("Failed to post multi-game promotion", e);
                }
            }
        }

        // Process each app for individual promotions (steps 1, 2, 4)
        for (AppPromotion app : activeApps) {
            try {
                int lastStep = gamePromotionService.getLastPromotionStep(guildId, app.getAppId());
                Instant lastPostTime = gamePromotionService.getLastAppPostTime(guildId, app.getAppId());

                // Determine next step to post
                int nextStep = stepManager.determineNextStep(app, lastStep, lastPostTime, activeApps, now);

                if (nextStep == 0) {
                    // No step ready to post yet
                    continue;
                }

                // Skip step 3 here (already handled above)
                if (nextStep == 3) {
                    continue;
                }

                // Post the promotion
                postAppPromotion(channel, app, nextStep, activeApps);

                // Record the step
                gamePromotionService.recordPromotionStep(guildId, app.getAppId(), nextStep, now);

                posted++;
                logger.info("Posted promotion step {} for app {} in guild {}",
                        nextStep, app.getAppId(), guildId);

            } catch (Exception e) {
                logger.error("Failed to post promotion for app {}", app.getAppId(), e);
            }
        }

        return posted;
    }

    // Track last check time per guild for verbosity enforcement
    private final Map<String, Instant> lastCheckTimes = new ConcurrentHashMap<>();

    private Instant getLastCheckTime(String guildId) {
        return lastCheckTimes.get(guildId);
    }

    private void recordLastCheckTime(String guildId, Instant checkTime) {
        lastCheckTimes.put(guildId, checkTime);
    }

    /**
     * Posts a single app promotion for a specific step.
     *
     * @param channel the channel
     * @param app     the app promotion
     * @param step    the promotion step (1, 2, or 4)
     * @param allApps all active apps (for context)
     */
    private void postAppPromotion(TextChannel channel, AppPromotion app, int step, List<AppPromotion> allApps) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎮 " + app.getAppName());
        embed.setColor(Color.CYAN);

        // Get message template for this step
        String template = messageTemplates.getTemplate(step);
        String message = messageTemplates.formatMessage(template, app, allApps);

        embed.setDescription(message);

        // Add CTAs (at least one required)
        List<String> availableCtas = messageTemplates.getAvailableCtas(app);
        if (!availableCtas.isEmpty()) {
            String ctaText = messageTemplates.getRandomCta();
            StringBuilder ctaSection = new StringBuilder(ctaText + "\n");

            // Include at least one CTA, randomly select from available
            int ctaCount = Math.min(availableCtas.size(), random.nextInt(3) + 1); // 1-3 CTAs
            for (int i = 0; i < ctaCount && i < availableCtas.size(); i++) {
                ctaSection.append(availableCtas.get(i));
                if (i < ctaCount - 1) {
                    ctaSection.append(" | ");
                }
            }

            embed.addField("🔗 Links", ctaSection.toString(), false);
        }

        // Optionally add social media links (~30% chance)
        if (app.getCampaign() != null && app.getCampaign().getSocialMedia() != null) {
            String socialLink = messageTemplates.getRandomSocialMediaLink(app.getCampaign().getSocialMedia());
            if (socialLink != null) {
                embed.addField("📱 Follow Us", socialLink, false);
            }
        }

        // Add image if available
        if (app.getCampaign() != null &&
                app.getCampaign().getImages() != null &&
                !app.getCampaign().getImages().isEmpty()) {
            String imageUrl = app.getCampaign().getImages().get(0).getAppLogo();
            if (imageUrl != null && !imageUrl.isBlank() && !imageUrl.contains("...")) {
                embed.setImage(imageUrl);
            }
        }

        embed.setFooter("Powered by MIKROS Marketing");
        embed.setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue(
                success -> logger.debug("Successfully posted promotion step {} for app {}", step, app.getAppId()),
                error -> logger.error("Failed to send promotion message", error)
        );
    }

    /**
     * Posts a multi-game promotion (step 3).
     *
     * @param channel the channel
     * @param apps    list of active apps to promote
     */
    private void postMultiGamePromotion(TextChannel channel, List<AppPromotion> apps) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🌟 MIKROS Top Picks for this month");
        embed.setColor(Color.MAGENTA);

        // Get template for step 3
        String template = messageTemplates.getTemplate(3);
        String message = messageTemplates.formatMessage(template, null, apps);

        embed.setDescription(message);

        // Add each app with its description and CTA
        for (AppPromotion app : apps) {
            StringBuilder appInfo = new StringBuilder(app.getShortDescription());

            // Add primary CTA for this app
            List<String> ctas = messageTemplates.getAvailableCtas(app);
            if (!ctas.isEmpty()) {
                appInfo.append("\n").append(ctas.get(0)); // Use first available CTA
            }

            embed.addField(app.getAppName(), appInfo.toString(), false);
        }

        // Add social media links if available (from first app)
        if (!apps.isEmpty() && apps.get(0).getCampaign() != null &&
                apps.get(0).getCampaign().getSocialMedia() != null) {
            String socialLink = messageTemplates.getRandomSocialMediaLink(
                    apps.get(0).getCampaign().getSocialMedia());
            if (socialLink != null) {
                embed.addField("📱 Follow Us", socialLink, false);
            }
        }

        embed.setFooter("Powered by MIKROS Marketing");
        embed.setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue(
                success -> logger.debug("Successfully posted multi-game promotion"),
                error -> logger.error("Failed to send multi-game promotion message", error)
        );
    }

    /**
     * Stops the scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Game promotion scheduler stopped");
    }
}
