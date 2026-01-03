package com.tatumgames.mikros.tatumtech.scheduler;

import com.tatumgames.mikros.models.AppPromotion;
import com.tatumgames.mikros.services.GamePromotionService;
import com.tatumgames.mikros.tatumtech.template.TatumTechEventTemplates;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Scheduler for Tatum Tech event promotions.
 * Posts preset event messages on specific dates (4 times per year).
 * Respects active campaigns and only posts when no active campaigns are running.
 */
public class TatumTechEventScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TatumTechEventScheduler.class);

    // Scheduled dates (month and day, year-independent)
    private static final List<EventDate> SCHEDULED_DATES = List.of(
            // Version A: Pre-event awareness
            new EventDate(2, 6, EventVersion.VERSION_A),   // February 6th
            new EventDate(8, 28, EventVersion.VERSION_A),   // August 28th
            // Version B: Exposure-focused
            new EventDate(3, 5, EventVersion.VERSION_B),   // March 5th
            new EventDate(9, 2, EventVersion.VERSION_B)     // September 2nd
    );

    // Post time: 10:00 AM PST (18:00 UTC)
    private static final int POST_HOUR_UTC = 18;
    private static final int POST_MINUTE = 0;

    private final GamePromotionService gamePromotionService;
    private final TatumTechEventTemplates templates;
    private final ScheduledExecutorService scheduler;
    private final String recapMonthYear;
    private final String recapVideoUrl;
    // Track which dates have been posted this year (guildId -> set of dates posted)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> postedDates = new ConcurrentHashMap<>();
    private JDA jda;

    /**
     * Creates a new TatumTechEventScheduler.
     *
     * @param gamePromotionService the game promotion service
     * @param recapMonthYear       the recap month and year (e.g., "October 2025")
     * @param recapVideoUrl        the recap video URL
     */
    public TatumTechEventScheduler(
            GamePromotionService gamePromotionService,
            String recapMonthYear,
            String recapVideoUrl) {
        this.gamePromotionService = gamePromotionService;
        this.templates = new TatumTechEventTemplates();
        this.recapMonthYear = recapMonthYear;
        this.recapVideoUrl = recapVideoUrl;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "tatum-tech-event-scheduler");
            t.setDaemon(true);
            return t;
        });
        logger.info("TatumTechEventScheduler initialized");
    }

    /**
     * Starts the event scheduler.
     * Checks daily at the scheduled post time to see if it's time to post.
     *
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        this.jda = jda;

        // Calculate initial delay to next check (check at top of each hour)
        long initialDelay = calculateInitialDelay();

        // Run check every hour
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndPostEvents();
            } catch (Exception e) {
                logger.error("Error in Tatum Tech event scheduler", e);
            }
        }, initialDelay, 60, TimeUnit.MINUTES);

        logger.info("Tatum Tech event scheduler started (checks every hour, posts at {}:{} UTC on scheduled dates)",
                POST_HOUR_UTC, POST_MINUTE);
    }

    /**
     * Calculates initial delay to next hour boundary.
     *
     * @return delay in minutes
     */
    private long calculateInitialDelay() {
        Instant now = Instant.now();
        Instant nextHour = now.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS);
        return ChronoUnit.MINUTES.between(now, nextHour);
    }

    /**
     * Checks if it's time to post any scheduled events and posts them.
     */
    private void checkAndPostEvents() {
        if (jda == null) {
            logger.warn("JDA not initialized, skipping event check");
            return;
        }

        Instant now = Instant.now();
        ZonedDateTime utcNow = now.atZone(ZoneOffset.UTC);
        int currentMonth = utcNow.getMonthValue();
        int currentDay = utcNow.getDayOfMonth();
        int currentHour = utcNow.getHour();
        int currentMinute = utcNow.getMinute();

        // Check if it's the scheduled post time
        if (currentHour != POST_HOUR_UTC || currentMinute != POST_MINUTE) {
            return;
        }

        // Check if today matches any scheduled date
        for (EventDate eventDate : SCHEDULED_DATES) {
            if (eventDate.month == currentMonth && eventDate.day == currentDay) {
                logger.info("Scheduled Tatum Tech event date detected: {} {} (Version {})",
                        currentMonth, currentDay, eventDate.version);

                // Post to all configured guilds
                for (Guild guild : jda.getGuilds()) {
                    try {
                        postEventToGuild(guild, eventDate);
                    } catch (Exception e) {
                        logger.error("Error posting Tatum Tech event to guild {}", guild.getId(), e);
                    }
                }
            }
        }
    }

    /**
     * Posts a Tatum Tech event message to a guild if conditions are met.
     *
     * @param guild     the guild
     * @param eventDate the event date
     */
    private void postEventToGuild(Guild guild, EventDate eventDate) {
        String guildId = guild.getId();
        String dateKey = eventDate.getKey();

        // Check if promotion channel is configured
        String channelId = gamePromotionService.getPromotionChannel(guildId);
        if (channelId == null) {
            logger.debug("Guild {} has no promotion channel configured, skipping Tatum Tech event", guildId);
            return;
        }

        // Check if we've already posted this event today
        ConcurrentHashMap<String, Boolean> guildPostedDates = postedDates.computeIfAbsent(
                guildId, k -> new ConcurrentHashMap<>());

        String todayKey = LocalDate.now().toString() + "-" + dateKey;
        if (guildPostedDates.containsKey(todayKey)) {
            logger.debug("Already posted Tatum Tech event {} to guild {} today", dateKey, guildId);
            return;
        }

        // Check for active campaigns - don't interrupt active campaigns
        List<AppPromotion> allApps = gamePromotionService.fetchAllApps();
        List<AppPromotion> activeApps = allApps.stream()
                .filter(app -> isWithinCampaignWindow(app, Instant.now()))
                .filter(AppPromotion::isCampaignActive)
                .collect(Collectors.toList());

        if (!activeApps.isEmpty()) {
            logger.info("Guild {} has {} active campaign(s), skipping Tatum Tech event to avoid interruption",
                    guildId, activeApps.size());
            return;
        }

        // Try TextChannel first, then NewsChannel
        TextChannel textChannel = guild.getTextChannelById(channelId);
        NewsChannel newsChannel = guild.getNewsChannelById(channelId);

        if (textChannel == null && newsChannel == null) {
            logger.warn("Configured promotion channel {} not found in guild {} (tried TextChannel and NewsChannel)",
                    channelId, guildId);
            return;
        }

        MessageChannel channel = textChannel != null ? textChannel : newsChannel;

        // Post the event message
        postEventMessage(channel, eventDate);

        // Mark as posted
        guildPostedDates.put(todayKey, true);

        // Clean up old entries (keep only current year)
        cleanupOldEntries(guildId);
    }

    /**
     * Posts a Tatum Tech event message to a channel.
     *
     * @param channel   the channel
     * @param eventDate the event date
     */
    private void postEventMessage(MessageChannel channel, EventDate eventDate) {
        EmbedBuilder embed = new EmbedBuilder();

        // Set title and color based on version
        if (eventDate.version == EventVersion.VERSION_A) {
            embed.setTitle("🎮 TATUM TECH - CALLING ALL GAME LOVERS!");
        } else {
            embed.setTitle("🚀 TATUM TECH - DEVELOPER EXPOSURE & FUN");
        }
        embed.setColor(Color.CYAN); // Same as game promotions

        // Get message content
        String message;
        if (eventDate.version == EventVersion.VERSION_A) {
            message = templates.getVersionA(recapMonthYear, recapVideoUrl);
        } else {
            message = templates.getVersionB(recapVideoUrl);
        }

        embed.setDescription(message);

        // Add MIKROS Marketing footer (randomized like game promotions)
        embed.setFooter(templates.getRandomMikrosFooter());
        embed.setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue(
                success -> logger.info("Successfully posted Tatum Tech event (Version {}) to channel {}",
                        eventDate.version, channel.getId()),
                error -> logger.error("Failed to send Tatum Tech event message", error)
        );
    }

    /**
     * Checks if an app is within its campaign window.
     *
     * @param app the app promotion
     * @param now current time
     * @return true if within window
     */
    private boolean isWithinCampaignWindow(AppPromotion app, Instant now) {
        if (app.getCampaign() == null) {
            return false;
        }
        Instant startDate = app.getCampaign().getStartDate();
        Instant endDate = app.getCampaign().getEndDate();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    /**
     * Cleans up old posted date entries (keeps only current year).
     *
     * @param guildId the guild ID
     */
    private void cleanupOldEntries(String guildId) {
        ConcurrentHashMap<String, Boolean> guildPostedDates = postedDates.get(guildId);
        if (guildPostedDates == null) {
            return;
        }

        String currentYear = String.valueOf(LocalDate.now().getYear());
        guildPostedDates.entrySet().removeIf(entry -> !entry.getKey().startsWith(currentYear));
    }

    /**
     * Stops the scheduler.
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Tatum Tech event scheduler stopped");
        }
    }

    /**
     * Event version enum.
     */
    private enum EventVersion {
        VERSION_A,
        VERSION_B
    }

    /**
     * Represents a scheduled event date.
     */
    private static class EventDate {
        final int month;  // 1-12
        final int day;    // 1-31
        final EventVersion version;

        EventDate(int month, int day, EventVersion version) {
            this.month = month;
            this.day = day;
            this.version = version;
        }

        String getKey() {
            return month + "-" + day;
        }
    }
}

