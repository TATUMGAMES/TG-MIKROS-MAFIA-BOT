package com.tatumgames.mikros.services.scheduler;

import com.tatumgames.mikros.models.AppPromotion;
import com.tatumgames.mikros.models.PromotionVerbosity;
import com.tatumgames.mikros.promo.manager.PromotionStepManager;
import com.tatumgames.mikros.promo.template.PromotionMessageTemplates;
import com.tatumgames.mikros.services.GamePromotionService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Scheduler service for posting app promotions at configured intervals.
 * Uses 4-step promotion story format while respecting campaign dates and avoiding spam.
 * Checks every 60 minutes and posts promotions based on guild verbosity settings.
 * Implements dynamic cooldown and game rotation to handle multiple apps gracefully.
 */
public class GamePromotionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(GamePromotionScheduler.class);

    // Algorithm parameters
    private static final int MIN_INTERVAL_MINUTES = 5;
    private static final int MAX_INTERVAL_MINUTES = 60;
    private static final int MAX_GAMES_THRESHOLD = 50;
    private static final double RANDOMIZATION_FACTOR_MIN = 0.8;
    private static final double RANDOMIZATION_FACTOR_MAX = 1.2;

    private final GamePromotionService gamePromotionService;
    private final PromotionStepManager stepManager;
    private final PromotionMessageTemplates messageTemplates;
    private final ScheduledExecutorService scheduler;
    private final Random random;
    // Per-guild rotation state
    private final Map<String, GameRotationState> rotationStates = new ConcurrentHashMap<>();
    // Track last check time per guild for verbosity enforcement
    private final Map<String, Instant> lastCheckTimes = new ConcurrentHashMap<>();
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

        // Try TextChannel first, then NewsChannel
        TextChannel textChannel = guild.getTextChannelById(channelId);
        NewsChannel newsChannel = guild.getNewsChannelById(channelId);

        if (textChannel == null && newsChannel == null) {
            logger.warn("Configured promotion channel {} not found in guild {} (tried TextChannel and NewsChannel)", channelId, guildId);
            return;
        }

        // Use whichever channel was found
        MessageChannel channel = textChannel != null ? textChannel : newsChannel;

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

        // Try TextChannel first, then NewsChannel
        TextChannel textChannel = guild.getTextChannelById(channelId);
        NewsChannel newsChannel = guild.getNewsChannelById(channelId);

        if (textChannel == null && newsChannel == null) {
            logger.warn("Configured promotion channel {} not found in guild {} (tried TextChannel and NewsChannel)", channelId, guildId);
            return 0;
        }

        // Use whichever channel was found
        MessageChannel channel = textChannel != null ? textChannel : newsChannel;

        return postPromotionsToChannel(guild, channel);
    }

    /**
     * Posts promotions to a channel using the 4-step story format.
     * Respects guild verbosity settings, dynamic cooldown, and game rotation.
     *
     * @param guild   the guild
     * @param channel the channel to post in
     * @return number of promotions posted
     */
    private int postPromotionsToChannel(Guild guild, MessageChannel channel) {
        String guildId = guild.getId();

        // Check verbosity to determine if we should check for promotions
        PromotionVerbosity verbosity = gamePromotionService.getPromotionVerbosity(guildId);
        Instant lastCheckTime = getLastCheckTime(guildId);
        Instant now = Instant.now();

        if (lastCheckTime != null) {
            long hoursSinceLastCheck = ChronoUnit.HOURS.between(lastCheckTime, now);
            if (hoursSinceLastCheck < verbosity.getHoursInterval()) {
                logger.debug("Guild {} verbosity check: {} hours since last check, need {} hours",
                        guildId, hoursSinceLastCheck, verbosity.getHoursInterval());
                return 0;
            }
        }

        // Record this check time
        recordLastCheckTime(guildId, now);

        // Fetch all apps from API
        List<AppPromotion> allApps = gamePromotionService.fetchAllApps();

        if (allApps.isEmpty()) {
            logger.debug("No apps available for guild {}", guildId);
            return 0;
        }

        // Filter to only active campaigns within campaign window
        List<AppPromotion> activeApps = allApps.stream()
                .filter(app -> isWithinCampaignWindow(app, now))
                .filter(AppPromotion::isCampaignActive)
                .collect(Collectors.toList());

        if (activeApps.isEmpty()) {
            logger.debug("No active campaigns within window for guild {}", guildId);
            return 0;
        }

        // -----------------------------------------
        // Step 3: Multi-game promotion check
        // At this point activeApps is guaranteed NOT EMPTY
        // -----------------------------------------

        AppPromotion firstApp = activeApps.getFirst();
        int lastStepForFirstApp = gamePromotionService.getLastPromotionStep(guildId, firstApp.getAppId());

        if (firstApp.getCampaign() != null && stepManager.shouldPostStep3(
                activeApps,
                lastStepForFirstApp,
                firstApp.getCampaign().getStartDate(),
                firstApp.getCampaign().getEndDate(),
                now)) {

            try {
                postMultiGamePromotion(channel, activeApps);

                // Record step 3 for all apps
                for (AppPromotion app : activeApps) {
                    gamePromotionService.recordPromotionStep(guildId, app.getAppId(), 3, now);
                }

                logger.info("Posted multi-game promotion (step 3) in guild {}", guildId);
                return 1;

            } catch (Exception e) {
                logger.error("Failed to post multi-game promotion", e);
            }
        }

        // -----------------------------------------
        // Step 1 & 2: Individual promotions
        // -----------------------------------------

        // Determine next game to promote
        AppPromotion nextApp = getNextGameToPromote(guildId, activeApps);
        if (nextApp == null) {
            // Cooldown not expired yet or no games in queue
            return 0;
        }

        int lastStep = gamePromotionService.getLastPromotionStep(guildId, nextApp.getAppId());
        Instant lastPostTime = gamePromotionService.getLastAppPostTime(guildId, nextApp.getAppId());
        int nextStep = stepManager.determineNextStep(nextApp, lastStep, lastPostTime, activeApps, now);

        if (nextStep == 0) {
            // No step ready to post yet
            return 0;
        }

        // Skip step 3 (already handled earlier)
        if (nextStep == 3) {
            return 0;
        }

        try {
            postAppPromotion(channel, nextApp, nextStep, activeApps);
            gamePromotionService.recordPromotionStep(guildId, nextApp.getAppId(), nextStep, now);

            logger.info("Posted promotion step {} for app {} in guild {}",
                    nextStep, nextApp.getAppId(), guildId);
            return 1;

        } catch (Exception e) {
            logger.error("Failed to post promotion for app {}", nextApp.getAppId(), e);
            return 0;
        }
    }

    private Instant getLastCheckTime(String guildId) {
        return lastCheckTimes.get(guildId);
    }

    private void recordLastCheckTime(String guildId, Instant checkTime) {
        lastCheckTimes.put(guildId, checkTime);
    }

    /**
     * Calculates dynamic cooldown based on number of active games.
     * Scales from min (few games) to max (many games) with randomization.
     *
     * @param activeGameCount the number of active games
     * @return cooldown in minutes
     */
    private long calculateDynamicCooldown(int activeGameCount) {
        // Base interval calculation
        double baseInterval = MIN_INTERVAL_MINUTES +
                ((MAX_INTERVAL_MINUTES - MIN_INTERVAL_MINUTES) *
                        (activeGameCount / (double) MAX_GAMES_THRESHOLD));

        // Clamp to min/max
        baseInterval = Math.max(MIN_INTERVAL_MINUTES,
                Math.min(MAX_INTERVAL_MINUTES, baseInterval));

        // Add randomization (±20%)
        double randomFactor = RANDOMIZATION_FACTOR_MIN +
                (random.nextDouble() * (RANDOMIZATION_FACTOR_MAX - RANDOMIZATION_FACTOR_MIN));
        long actualInterval = (long) (baseInterval * randomFactor);

        logger.debug("Calculated cooldown for {} games: {} minutes", activeGameCount, actualInterval);
        return actualInterval;
    }

    /**
     * Checks if an app is within its campaign window.
     *
     * @param app the app promotion
     * @param now the current time
     * @return true if within campaign window
     */
    private boolean isWithinCampaignWindow(AppPromotion app, Instant now) {
        if (app.getCampaign() == null) {
            return false;
        }

        Instant startDate = app.getCampaign().getStartDate();
        Instant endDate = app.getCampaign().getEndDate();

        if (startDate == null || endDate == null) {
            return false;
        }

        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    /**
     * Gets the next game to promote based on rotation queue and cooldown.
     *
     * @param guildId    the guild ID
     * @param activeApps list of active apps
     * @return the next app to promote, or null if cooldown not expired
     */
    private AppPromotion getNextGameToPromote(String guildId, List<AppPromotion> activeApps) {
        GameRotationState state = rotationStates.computeIfAbsent(guildId, k -> {
            GameRotationState newState = new GameRotationState();
            newState.gameQueue = new LinkedList<>();
            newState.lastPromotionTime = null;
            newState.currentCooldownMinutes = MIN_INTERVAL_MINUTES;
            return newState;
        });

        // Rebuild queue if empty or apps changed
        if (state.gameQueue.isEmpty() || hasAppsChanged(state, activeApps)) {
            rebuildQueue(state, activeApps);
        }

        // Check if cooldown has passed
        if (state.lastPromotionTime != null) {
            long minutesSinceLast = ChronoUnit.MINUTES.between(state.lastPromotionTime, Instant.now());
            if (minutesSinceLast < state.currentCooldownMinutes) {
                logger.debug("Guild {} cooldown: {} minutes since last, need {} minutes",
                        guildId, minutesSinceLast, state.currentCooldownMinutes);
                return null; // Not time yet
            }
        }

        // Get next game from queue
        String nextAppId = state.gameQueue.poll();
        if (nextAppId == null) {
            rebuildQueue(state, activeApps);
            nextAppId = state.gameQueue.poll();
        }

        if (nextAppId == null) {
            return null;
        }

        // Store in final variable for lambda
        final String finalAppId = nextAppId;

        // Find app and update state
        AppPromotion app = activeApps.stream()
                .filter(a -> a.getAppId().equals(finalAppId))
                .findFirst()
                .orElse(null);

        if (app != null) {
            state.lastPromotionTime = Instant.now();
            state.currentCooldownMinutes = calculateDynamicCooldown(activeApps.size());
            // Re-add to end of queue for rotation
            state.gameQueue.offer(finalAppId);
            logger.debug("Selected app {} for promotion in guild {} (cooldown: {} minutes)",
                    app.getAppId(), guildId, state.currentCooldownMinutes);
        }

        return app;
    }

    /**
     * Rebuilds the rotation queue with current active apps.
     *
     * @param state      the rotation state
     * @param activeApps list of active apps
     */
    private void rebuildQueue(GameRotationState state, List<AppPromotion> activeApps) {
        state.gameQueue.clear();
        List<String> appIds = activeApps.stream()
                .map(AppPromotion::getAppId)
                .collect(Collectors.toList());
        Collections.shuffle(appIds, random);
        state.gameQueue.addAll(appIds);
        logger.debug("Rebuilt rotation queue with {} apps", appIds.size());
    }

    /**
     * Checks if apps have changed (for queue rebuild detection).
     *
     * @param state      the rotation state
     * @param activeApps current active apps
     * @return true if apps have changed
     */
    private boolean hasAppsChanged(GameRotationState state, List<AppPromotion> activeApps) {
        if (state.gameQueue.size() != activeApps.size()) {
            return true;
        }

        List<String> currentAppIds = activeApps.stream()
                .map(AppPromotion::getAppId)
                .sorted()
                .toList();

        List<String> queueAppIds = new java.util.ArrayList<>(state.gameQueue);
        Collections.sort(queueAppIds);

        return !currentAppIds.equals(queueAppIds);
    }

    /**
     * Posts a single app promotion for a specific step.
     *
     * @param channel the channel
     * @param app     the app promotion
     * @param step    the promotion step (1, 2, or 4)
     * @param allApps all active apps (for context)
     */
    private void postAppPromotion(MessageChannel channel, AppPromotion app, int step, List<AppPromotion> allApps) {
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
            String imageUrl = app.getCampaign().getImages().getFirst().getAppLogo();
            if (imageUrl != null && !imageUrl.isBlank() && !imageUrl.contains("...")) {
                embed.setImage(imageUrl);
            }
        }

        // Add MIKROS Marketing footer (always on step 4, randomly on steps 1-2)
        if (messageTemplates.shouldShowMikrosFooter(step)) {
            embed.setFooter(messageTemplates.getRandomMikrosFooter());
        } else {
            embed.setFooter("Powered by MIKROS Marketing");
        }
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
    private void postMultiGamePromotion(MessageChannel channel, List<AppPromotion> apps) {
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
                appInfo.append("\n").append(ctas.getFirst()); // Use first available CTA
            }

            embed.addField(app.getAppName(), appInfo.toString(), false);
        }

        // Add social media links if available (from first app)
        if (!apps.isEmpty() && apps.getFirst().getCampaign() != null &&
                apps.getFirst().getCampaign().getSocialMedia() != null) {
            String socialLink = messageTemplates.getRandomSocialMediaLink(
                    apps.getFirst().getCampaign().getSocialMedia());
            if (socialLink != null) {
                embed.addField("📱 Follow Us", socialLink, false);
            }
        }

        // Always show MIKROS Marketing footer on step 3 (multi-game promotion)
        embed.setFooter(messageTemplates.getRandomMikrosFooter());
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

    /**
     * Rotation state for a guild.
     */
    private static class GameRotationState {
        Queue<String> gameQueue;  // Queue of appIds to promote
        Instant lastPromotionTime;
        long currentCooldownMinutes;
    }
}
