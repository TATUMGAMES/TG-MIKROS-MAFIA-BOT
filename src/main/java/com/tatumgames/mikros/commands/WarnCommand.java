package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import com.tatumgames.mikros.services.AutoEscalationService;
import com.tatumgames.mikros.services.ModerationLogService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Command handler for the /warn command.
 * Warns a user and logs the warning in the moderation system.
 */
public class WarnCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(WarnCommand.class);
    private final ModerationLogService moderationLogService;
    private final AutoEscalationService autoEscalationService;
    
    /**
     * Creates a new WarnCommand handler.
     * 
     * @param moderationLogService the moderation log service
     * @param autoEscalationService the auto-escalation service
     */
    public WarnCommand(ModerationLogService moderationLogService, AutoEscalationService autoEscalationService) {
        this.moderationLogService = moderationLogService;
        this.autoEscalationService = autoEscalationService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-warn", "Warn a user for violating server rules")
                .addOption(OptionType.USER, "user", "The user to warn", true)
                .addOption(OptionType.STRING, "reason", "The reason for the warning", true)
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
        String reason = event.getOption("reason").getAsString();
        
        // Validate target
        if (targetUser.isBot()) {
            event.reply("❌ You cannot warn a bot.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (targetUser.getId().equals(moderator.getId())) {
            event.reply("❌ You cannot warn yourself.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Create and log the moderation action
        ModerationAction action = new ModerationAction(
                targetUser.getId(),
                targetUser.getEffectiveName(),
                moderator.getId(),
                moderator.getEffectiveName(),
                ActionType.WARN,
                reason,
                Instant.now(),
                event.getGuild().getId()
        );
        
        moderationLogService.logAction(action);
        
        // Check for auto-escalation
        ActionType escalationAction = autoEscalationService.checkEscalation(
                targetUser.getId(), event.getGuild().getId());
        
        String confirmationMessage = String.format(
                "⚠️ **Warning Issued**\n" +
                "User: %s\n" +
                "Reason: %s\n" +
                "Moderator: %s",
                targetUser.getAsMention(),
                reason,
                moderator.getAsMention()
        );
        
        // If escalation is needed, attempt to perform it
        if (escalationAction != null) {
            Member targetMember = event.getGuild().getMember(targetUser);
            if (targetMember != null) {
                boolean escalated = autoEscalationService.performAutoEscalation(
                        targetMember,
                        event.getGuild(),
                        event.getGuild().getSelfMember()
                );
                
                if (escalated) {
                    confirmationMessage += String.format(
                            "\n\n⚠️ **Auto-Escalation Triggered**\n" +
                            "User has reached the warning threshold and has been automatically %s.",
                            escalationAction == ActionType.KICK ? "kicked" : "muted"
                    );
                    logger.info("Auto-escalation triggered for user {} in guild {}: {}",
                            targetUser.getId(), event.getGuild().getId(), escalationAction);
                }
            }
        }
        
        // Send confirmation
        event.reply(confirmationMessage).queue();
        
        logger.info("User {} warned by {} in guild {}: {}", 
                targetUser.getId(), moderator.getId(), event.getGuild().getId(), reason);
    }
    
    @Override
    public String getCommandName() {
        return "admin-warn";
    }
}

