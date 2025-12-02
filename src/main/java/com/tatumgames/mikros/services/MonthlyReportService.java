package com.tatumgames.mikros.services;

import com.tatumgames.mikros.config.ModerationConfig;
import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating and scheduling monthly moderation reports.
 * <p>
 * TODO: Upgrade with database persistence or cron-style configuration
 */
public class MonthlyReportService {
    private static final Logger logger = LoggerFactory.getLogger(MonthlyReportService.class);

    private final ModerationLogService moderationLogService;
    private final ActivityTrackingService activityTrackingService;
    private final ScheduledExecutorService scheduler;

    // Key: guildId -> report channel ID
    private final Map<String, String> reportChannels;

    /**
     * Creates a new MonthlyReportService.
     *
     * @param moderationLogService    the moderation log service
     * @param activityTrackingService the activity tracking service
     */
    public MonthlyReportService(ModerationLogService moderationLogService,
                                ActivityTrackingService activityTrackingService) {
        this.moderationLogService = moderationLogService;
        this.activityTrackingService = activityTrackingService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.reportChannels = new ConcurrentHashMap<>();
        logger.info("MonthlyReportService initialized");
    }

    /**
     * Starts the monthly report scheduler.
     * Checks every hour if it's time to send reports.
     *
     * @param jda the JDA instance
     */
    public void startScheduler(JDA jda) {
        // Check every hour if we need to send reports
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndSendReports(jda);
            } catch (Exception e) {
                logger.error("Error in monthly report scheduler", e);
            }
        }, 0, 1, TimeUnit.HOURS);

        logger.info("Monthly report scheduler started");
    }

    /**
     * Checks if it's time to send monthly reports and sends them if needed.
     */
    private void checkAndSendReports(JDA jda) {
        LocalDateTime now = LocalDateTime.now();

        // Check if it's the configured day and hour
        if (now.getDayOfMonth() == ModerationConfig.MONTHLY_REPORT_DAY &&
                now.getHour() == ModerationConfig.MONTHLY_REPORT_HOUR) {

            logger.info("Sending monthly reports...");

            for (Guild guild : jda.getGuilds()) {
                try {
                    generateAndSendReport(guild);
                } catch (Exception e) {
                    logger.error("Failed to send monthly report for guild {}", guild.getId(), e);
                }
            }
        }
    }

    /**
     * Generates and sends a monthly report for a guild.
     *
     * @param guild the guild
     */
    public void generateAndSendReport(Guild guild) {
        String guildId = guild.getId();

        // Get report channel
        String channelId = reportChannels.get(guildId);
        TextChannel reportChannel = null;

        if (channelId != null) {
            reportChannel = guild.getTextChannelById(channelId);
        }

        // Fall back to system channel or owner DM
        if (reportChannel == null) {
            reportChannel = guild.getSystemChannel();
        }

        if (reportChannel == null) {
            logger.warn("No suitable channel found for monthly report in guild {}", guildId);
            return;
        }

        // Generate report embed
        EmbedBuilder embed = generateReportEmbed(guild);

        // Send report
        reportChannel.sendMessageEmbeds(embed.build())
                .queue(
                        success -> logger.info("Monthly report sent for guild {}", guildId),
                        error -> logger.error("Failed to send monthly report for guild {}", guildId, error)
                );
    }

    /**
     * Generates a monthly report embed for a guild.
     *
     * @param guild the guild
     * @return the report embed
     */
    private EmbedBuilder generateReportEmbed(Guild guild) {
        String guildId = guild.getId();

        // Get moderation statistics
        Map<String, List<ModerationAction>> userActions = new HashMap<>();
        int totalWarnings = 0;
        int totalKicks = 0;
        int totalBans = 0;

        // Collect data (simplified - in real implementation would filter by month)
        for (net.dv8tion.jda.api.entities.Member member : guild.getMembers()) {
            String userId = member.getId();
            List<ModerationAction> actions = moderationLogService.getUserHistory(userId, guildId);

            if (!actions.isEmpty()) {
                userActions.put(userId, actions);
                totalWarnings += (int) actions.stream().filter(a -> a.actionType() == ActionType.WARN).count();
                totalKicks += (int) actions.stream().filter(a -> a.actionType() == ActionType.KICK).count();
                totalBans += (int) actions.stream().filter(a -> a.actionType() == ActionType.BAN).count();
            }
        }

        // Get top offenders
        List<Map.Entry<String, List<ModerationAction>>> topOffenders = userActions.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(5)
                .toList();

        // Get activity stats
        int totalMessages = activityTrackingService.getTotalMessageCount(guildId);
        int activeUsers = activityTrackingService.getActiveUsersThisMonth(guildId);

        // Build embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📊 Monthly Moderation Report");
        embed.setDescription(String.format(
                "Monthly report for **%s**\n%s",
                guild.getName(),
                LocalDateTime.now().getMonth().toString()
        ));
        embed.setColor(Color.BLUE);
        embed.setThumbnail(guild.getIconUrl());

        // Moderation statistics
        embed.addField("⚠️ Warnings Issued", String.valueOf(totalWarnings), true);
        embed.addField("👢 Kicks Performed", String.valueOf(totalKicks), true);
        embed.addField("🔨 Bans Performed", String.valueOf(totalBans), true);

        // Activity statistics
        embed.addField("💬 Total Messages", String.format("%,d", totalMessages), true);
        embed.addField("👥 Active Users", String.valueOf(activeUsers), true);
        embed.addField("Total Actions", String.valueOf(totalWarnings + totalKicks + totalBans), true);

        // Top offenders
        if (!topOffenders.isEmpty()) {
            StringBuilder offendersText = new StringBuilder();
            int rank = 1;
            for (Map.Entry<String, List<ModerationAction>> entry : topOffenders) {
                offendersText.append(String.format(
                        "%d. <@%s> - %d action(s)\n",
                        rank++,
                        entry.getKey(),
                        entry.getValue().size()
                ));
            }
            embed.addField("🔻 Top Offenders", offendersText.toString(), false);
        }

        embed.addField("ℹ️ Next Report",
                String.format("Next report will be sent on the 1st of next month at %02d:00",
                        ModerationConfig.MONTHLY_REPORT_HOUR),
                false);

        embed.setTimestamp(Instant.now());
        embed.setFooter("TG-MIKROS Bot");

        return embed;
    }

    /**
     * Sets the report channel for a guild.
     *
     * @param guildId   the guild ID
     * @param channelId the channel ID
     */
    public void setReportChannel(String guildId, String channelId) {
        reportChannels.put(guildId, channelId);
        logger.info("Report channel set to {} for guild {}", channelId, guildId);
    }

    /**
     * Stops the scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Monthly report scheduler stopped");
    }
}

