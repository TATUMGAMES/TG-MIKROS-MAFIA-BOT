package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import com.tatumgames.mikros.services.AutoEscalationService;
import com.tatumgames.mikros.services.ModerationLogService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Command handler for the /warn command.
 * Warns a user and logs the warning in the moderation system.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class WarnCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(WarnCommand.class);
    private final ModerationLogService moderationLogService;
    private final AutoEscalationService autoEscalationService;

    /**
     * Creates a new WarnCommand handler.
     *
     * @param moderationLogService  the moderation log service
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
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get command options
        User targetUser = event.getOption("user", OptionMapping::getAsUser);
        String reason = event.getOption("reason", OptionMapping::getAsString);

        if (AdminUtils.isInvalidTargetUser(member, targetUser, event)) {
            return; // stop executing the command
        }

        if (targetUser == null) {
            event.reply("You must specify a user.").setEphemeral(true).queue();
            return;
        }

        // Create and log the moderation action
        ModerationAction action = new ModerationAction(
                targetUser.getId(),
                targetUser.getEffectiveName(),
                member.getId(),
                member.getEffectiveName(),
                ActionType.WARN,
                reason,
                Instant.now(),
                guild.getId()
        );

        moderationLogService.logAction(action);

        // Check for auto-escalation
        ActionType escalationAction = autoEscalationService.checkEscalation(
                targetUser.getId(), guild.getId());

        String confirmationMessage = String.format("""
                        ️ **Warning Issued**
                        User: %s
                        Reason: %s
                        Moderator: %s
                        """,
                targetUser.getName(),
                reason,
                member.getAsMention()
        );

        // If escalation is needed, attempt to perform it
        if (escalationAction != null) {
            Member targetMember = guild.getMember(targetUser);
            if (targetMember != null) {
                boolean escalated = autoEscalationService.performAutoEscalation(
                        targetMember,
                        guild,
                        guild.getSelfMember()
                );

                if (escalated) {
                    confirmationMessage += String.format("""
                                    ⚠️ **Auto-Escalation Triggered**
                                    User has reached the warning threshold and has been automatically %s.
                                    """,
                            escalationAction == ActionType.KICK ? "kicked" : "muted"
                    );
                    logger.info("Auto-escalation triggered for user {} in guild {}: {}",
                            targetUser.getId(), guild.getId(), escalationAction);
                }
            }
        }

        // Send confirmation
        event.reply(confirmationMessage).queue();

        logger.info("User {} warned by {} in guild {}: {}",
                targetUser.getId(), member.getId(), guild.getId(), reason);
    }

    @Override
    public String getCommandName() {
        return "admin-warn";
    }
}

