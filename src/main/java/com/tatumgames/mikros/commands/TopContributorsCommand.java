package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.config.ModerationConfig;
import com.tatumgames.mikros.models.UserActivity;
import com.tatumgames.mikros.services.ActivityTrackingService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command handler for the /top-contributors command.
 * Displays a leaderboard of the most active users.
 */
public class TopContributorsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(TopContributorsCommand.class);
    private final ActivityTrackingService activityTrackingService;
    
    /**
     * Creates a new TopContributorsCommand handler.
     * 
     * @param activityTrackingService the activity tracking service
     */
    public TopContributorsCommand(ActivityTrackingService activityTrackingService) {
        this.activityTrackingService = activityTrackingService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("top-contributors", "View the most active users in the server")
                .addOption(OptionType.INTEGER, "limit", "Number of users to show (default: 10, max: 25)", false)
                .setGuildOnly(true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        
        // Get limit option
        int limit = event.getOption("limit") != null
                ? Math.min(event.getOption("limit").getAsInt(), 25)
                : ModerationConfig.TOP_CONTRIBUTORS_COUNT;
        
        if (limit < 1) {
            event.reply("❌ Limit must be at least 1.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get top contributors
        List<UserActivity> topContributors = activityTrackingService.getTopContributors(guildId, limit);
        
        if (topContributors.isEmpty()) {
            event.reply("📊 No activity data available yet. Start chatting to appear on the leaderboard!")
                    .queue();
            return;
        }
        
        // Build leaderboard embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Top Contributors Leaderboard");
        embed.setDescription(String.format("Most active users in **%s**", event.getGuild().getName()));
        embed.setColor(new Color(255, 215, 0)); // Gold color
        
        // Add leaderboard entries
        StringBuilder leaderboard = new StringBuilder();
        for (int i = 0; i < topContributors.size(); i++) {
            UserActivity activity = topContributors.get(i);
            String medal = getMedal(i);
            String lastActive = DateTimeFormatter.ofPattern("MMM dd")
                    .format(Instant.ofEpochMilli(activity.getLastActiveTimestamp())
                            .atZone(ZoneId.systemDefault()));
            
            leaderboard.append(String.format(
                    "%s **#%d** - <@%s>\n" +
                    "       💬 **%,d messages** | Last active: %s\n\n",
                    medal,
                    i + 1,
                    activity.getUserId(),
                    activity.getMessageCount(),
                    lastActive
            ));
        }
        
        embed.addField("Rankings", leaderboard.toString(), false);
        
        embed.addField("ℹ️ Note",
                "Activity is tracked from when the bot joined. Historical messages are not counted.",
                false);
        
        embed.setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.info("Top contributors requested in guild {}", guildId);
    }
    
    @Override
    public String getCommandName() {
        return "top-contributors";
    }
    
    /**
     * Gets a medal emoji for the rank.
     */
    private String getMedal(int rank) {
        return switch (rank) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> "  ";
        };
    }
}

