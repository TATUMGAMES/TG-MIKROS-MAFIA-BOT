package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.botdetection.service.BotDetectionService;
import com.tatumgames.mikros.services.ActivityTrackingService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Map;

/**
 * Command handler for the /server-stats command.
 * Displays server activity statistics.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ServerStatsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerStatsCommand.class);
    private final ActivityTrackingService activityTrackingService;
    private final BotDetectionService botDetectionService;

    /**
     * Creates a new ServerStatsCommand handler.
     *
     * @param activityTrackingService the activity tracking service
     * @param botDetectionService     the bot detection service
     */
    public ServerStatsCommand(ActivityTrackingService activityTrackingService,
                              BotDetectionService botDetectionService) {
        this.activityTrackingService = activityTrackingService;
        this.botDetectionService = botDetectionService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("server-stats", "View server activity statistics")
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get guild id
        String guildId = guild.getId();

        // Gather statistics
        int totalMembers = guild.getMemberCount();
        int activeUsersThisMonth = activityTrackingService.getActiveUsersThisMonth(guildId);
        int totalMessages = activityTrackingService.getTotalMessageCount(guildId);
        double avgMessagesPerUser = activityTrackingService.getAverageMessagesPerUser(guildId);
        Map<String, Integer> topChannels = activityTrackingService.getMostActiveChannels(guildId, 5);

        // Build response embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📊 Server Statistics");
        embed.setDescription(String.format("Statistics for **%s**", guild.getName()));
        embed.setColor(Color.CYAN);
        embed.setThumbnail(guild.getIconUrl());

        // Member statistics
        embed.addField("👥 Total Members", String.valueOf(totalMembers), true);
        embed.addField("✅ Active This Month", String.valueOf(activeUsersThisMonth), true);
        embed.addField("📈 Activity Rate",
                String.format("%.1f%%", (totalMembers > 0 ? (activeUsersThisMonth * 100.0 / totalMembers) : 0)),
                true);

        // Message statistics
        embed.addField("💬 Total Messages Tracked", String.valueOf(totalMessages), true);
        embed.addField("📊 Avg Messages/User", String.format("%.1f", avgMessagesPerUser), true);
        int botsPrevented = botDetectionService.getBotPreventionCount(guildId);
        embed.addField("🛡️ Bots Prevented", String.valueOf(botsPrevented), true);

        // Most active channels
        if (!topChannels.isEmpty()) {
            StringBuilder channelStats = new StringBuilder();
            int rank = 1;
            for (Map.Entry<String, Integer> entry : topChannels.entrySet()) {
                channelStats.append(String.format("%d. <#%s> - %d messages\n",
                        rank++, entry.getKey(), entry.getValue()));
            }
            embed.addField("🔥 Most Active Channels", channelStats.toString(), false);
        } else {
            embed.addField("🔥 Most Active Channels", "No activity data available yet", false);
        }

        embed.addField("ℹ️ Note",
                "Statistics are based on messages sent while the bot was online. " +
                        "Historical data is not included.",
                false);

        embed.setTimestamp(java.time.Instant.now());
        embed.setFooter("Requested by " + member.getEffectiveName());

        // Send a reply message for this interaction
        event.replyEmbeds(embed.build()).queue();

        logger.info("Server stats requested by {} in guild {}", member.getId(), guildId);
    }

    @Override
    public String getCommandName() {
        return "server-stats";
    }
}

