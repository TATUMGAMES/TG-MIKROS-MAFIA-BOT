package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.models.ModerationAction;
import com.tatumgames.mikros.services.ModerationLogService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command handler for the /history command.
 * Displays moderation history for a user.
 */
public class HistoryCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(HistoryCommand.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ModerationLogService moderationLogService;
    private final com.tatumgames.mikros.services.ReputationService reputationService;
    
    /**
     * Creates a new HistoryCommand handler.
     * 
     * @param moderationLogService the moderation log service
     * @param reputationService the reputation service
     */
    public HistoryCommand(ModerationLogService moderationLogService, 
                         com.tatumgames.mikros.services.ReputationService reputationService) {
        this.moderationLogService = moderationLogService;
        this.reputationService = reputationService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-history", "View moderation history for a user")
                .addOption(OptionType.USER, "user", "The user to check", true)
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member moderator = event.getMember();
        if (moderator == null || !moderator.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get command options
        User targetUser = event.getOption("user").getAsUser();
        String guildId = event.getGuild().getId();
        
        // Retrieve moderation history
        List<ModerationAction> history = moderationLogService.getUserHistory(targetUser.getId(), guildId);
        
        // Build embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📋 Moderation History");
        embed.setColor(Color.ORANGE);
        embed.setThumbnail(targetUser.getAvatarUrl());
        embed.addField("User", targetUser.getName(), true);
        embed.addField("User ID", targetUser.getId(), true);
        embed.addField("Total Actions", String.valueOf(history.size()), true);
        
        // Add reputation score (placeholder until API is integrated)
        int localReputation = reputationService.calculateLocalReputation(targetUser.getId(), guildId);
        int globalReputation = reputationService.getGlobalReputation(targetUser.getId());
        
        String reputationDisplay = String.format("Local: %d", localReputation);
        if (globalReputation >= 0) {
            reputationDisplay += String.format(" | Global: %d", globalReputation);
        } else {
            reputationDisplay += " | Global: API not available";
        }
        
        embed.addField("🎯 Reputation Score", reputationDisplay, false);
        
        if (history.isEmpty()) {
            embed.setDescription("This user has no moderation history.");
        } else {
            // Count actions by type
            long warns = history.stream().filter(a -> a.getActionType() == com.tatumgames.mikros.models.ActionType.WARN).count();
            long kicks = history.stream().filter(a -> a.getActionType() == com.tatumgames.mikros.models.ActionType.KICK).count();
            long bans = history.stream().filter(a -> a.getActionType() == com.tatumgames.mikros.models.ActionType.BAN).count();
            
            embed.addField("⚠️ Warnings", String.valueOf(warns), true);
            embed.addField("👢 Kicks", String.valueOf(kicks), true);
            embed.addField("🔨 Bans", String.valueOf(bans), true);
            
            // Add recent actions (limit to 5 most recent)
            StringBuilder recentActions = new StringBuilder();
            int count = 0;
            for (ModerationAction action : history) {
                if (count >= 5) break;
                
                String emoji = getActionEmoji(action.getActionType());
                String timestamp = DATE_FORMATTER.format(
                        action.getTimestamp().atZone(java.time.ZoneId.systemDefault())
                );
                
                recentActions.append(String.format(
                        "%s **%s** - %s\n" +
                        "Reason: %s\n" +
                        "By: %s | %s\n\n",
                        emoji,
                        action.getActionType(),
                        timestamp,
                        action.getReason(),
                        action.getModeratorUsername(),
                        action.getTimestamp()
                ));
                
                count++;
            }
            
            embed.addField("Recent Actions", recentActions.toString(), false);
            
            if (history.size() > 5) {
                embed.setFooter(String.format("Showing 5 of %d total actions", history.size()));
            }
        }
        
        embed.setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.info("Moderation history requested for user {} by {} in guild {}", 
                targetUser.getId(), moderator.getId(), guildId);
    }
    
    @Override
    public String getCommandName() {
        return "admin-history";
    }
    
    /**
     * Gets the emoji representation for an action type.
     * 
     * @param actionType the action type
     * @return the emoji string
     */
    private String getActionEmoji(com.tatumgames.mikros.models.ActionType actionType) {
        return switch (actionType) {
            case WARN -> "⚠️";
            case KICK -> "👢";
            case BAN -> "🔨";
        };
    }
}

