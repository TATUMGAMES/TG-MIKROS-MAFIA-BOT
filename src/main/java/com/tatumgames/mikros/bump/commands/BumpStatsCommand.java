package com.tatumgames.mikros.bump.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.bump.model.BumpConfig;
import com.tatumgames.mikros.bump.model.BumpStats;
import com.tatumgames.mikros.bump.service.BumpService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Command to view server bump statistics.
 * Admin-only command.
 */
public class BumpStatsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(BumpStatsCommand.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
            .withZone(ZoneId.systemDefault());
    
    private final BumpService bumpService;
    
    public BumpStatsCommand(BumpService bumpService) {
        this.bumpService = bumpService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-bump-stats", "View server bump statistics and history (admin only)")
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        Guild guild = event.getGuild();
        
        if (member == null || guild == null ||
                !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an administrator to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        String guildId = guild.getId();
        BumpStats stats = bumpService.getBumpStats(guildId);
        
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📊 Server Bump Statistics");
        embed.setColor(Color.CYAN);
        
        // Overall stats
        embed.addField("📈 Overall Statistics",
                String.format(
                        "**Total Bumps:** %d\n" +
                        "**This Month:** %d\n" +
                        "**This Week:** %d",
                        stats.getTotalBumps(),
                        stats.getBumpsThisMonth(),
                        stats.getBumpsThisWeek()
                ),
                false
        );
        
        // Per-bot stats
        StringBuilder botStats = new StringBuilder();
        for (Map.Entry<BumpConfig.BumpBot, Integer> entry : stats.getBumpsPerBot().entrySet()) {
            BumpConfig.BumpBot bot = entry.getKey();
            int count = entry.getValue();
            Instant lastBump = stats.getLastBumpTime().get(bot);
            
            botStats.append(String.format("**%s:** %d bumps", bot.getDisplayName(), count));
            if (lastBump != null) {
                botStats.append(String.format(" (Last: %s)", DATE_FORMAT.format(lastBump)));
            }
            botStats.append("\n");
        }
        
        if (botStats.length() > 0) {
            embed.addField("🤖 Per-Bot Statistics", botStats.toString(), false);
        } else {
            embed.addField("🤖 Per-Bot Statistics", "No bumps recorded yet", false);
        }
        
        // Recent bumps
        if (!stats.getRecentBumps().isEmpty()) {
            StringBuilder recent = new StringBuilder();
            int count = 0;
            for (BumpStats.BumpRecord bump : stats.getRecentBumps()) {
                if (count >= 5) break; // Show last 5
                recent.append(String.format("• %s - %s\n",
                        bump.getBot().getDisplayName(),
                        DATE_FORMAT.format(bump.getTime())));
                count++;
            }
            embed.addField("🕐 Recent Bumps", recent.toString(), false);
        }
        
        embed.setFooter("Keep your server visible by bumping regularly!");
        embed.setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue(
                success -> logger.debug("Sent bump stats to user {} in guild {}", 
                        event.getUser().getId(), guildId),
                error -> logger.error("Failed to send bump stats", error)
        );
    }
    
    @Override
    public String getCommandName() {
        return "admin-bump-stats";
    }
}

