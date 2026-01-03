package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
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
import java.util.concurrent.TimeUnit;

/**
 * Command handler for the /ban command.
 * Bans a user from the server and logs the action.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BanCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(BanCommand.class);
    private final ModerationLogService moderationLogService;

    /**
     * Creates a new BanCommand handler.
     *
     * @param moderationLogService the moderation log service
     */
    public BanCommand(ModerationLogService moderationLogService) {
        this.moderationLogService = moderationLogService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-ban", "Ban a user from the server")
                .addOption(OptionType.USER, "user", "The user to ban", true)
                .addOption(OptionType.STRING, "reason", "The reason for the ban", true)
                .addOption(OptionType.INTEGER, "delete_days", "Number of days of messages to delete (0-7)", false)
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get command options
        User targetUser = event.getOption("user", OptionMapping::getAsUser);
        String reason = event.getOption("reason", OptionMapping::getAsString);
        Member targetMember = guild.getMember(targetUser);

        if (AdminUtils.isInvalidTargetUser(member, targetUser, event)) {
            return; // stop executing the command
        }

        int deleteDays = event.getOption("delete_days", 0, OptionMapping::getAsInt);

        // Validate delete_days
        if (deleteDays < 0 || deleteDays > 7) {
            event.reply("❌ Delete days must be between 0 and 7.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (targetMember == null) {
            event.reply("❌ User is not a member of this server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check role hierarchy
        if (!member.canInteract(targetMember)) {
            event.reply("❌ You cannot ban this user due to role hierarchy.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check bot permissions
        if (!guild.getSelfMember().canInteract(targetMember)) {
            event.reply("❌ I cannot ban this user due to role hierarchy.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Create and log the moderation action
        ModerationAction action = new ModerationAction(
                targetUser.getId(),
                targetUser.getEffectiveName(),
                member.getId(),
                member.getEffectiveName(),
                ActionType.BAN,
                reason,
                Instant.now(),
                guild.getId()
        );

        moderationLogService.logAction(action);

        // Defer the reply as banning might take a moment
        event.deferReply().queue();

        // Perform the ban
        guild.ban(targetUser, deleteDays, TimeUnit.DAYS)
                .reason(reason)
                .queue(success -> {
                            String message = String.format("""
                                            🔨 **User Banned**
                                            User: %s
                                            Reason: %s
                                            Moderator: %s
                                            Messages deleted: %d day(s)
                                            """,
                                    targetUser.getName(),
                                    reason,
                                    member.getAsMention(),
                                    deleteDays
                            );
                            event.getHook().sendMessage(message).queue();

                            logger.info("User {} banned by {} in guild {}: {}",
                                    targetUser.getId(), member.getId(), guild.getId(), reason);
                        },
                        error -> {
                            event.getHook().sendMessage("❌ Failed to ban user: " + error.getMessage())
                                    .setEphemeral(true)
                                    .queue();
                            logger.error("Failed to ban user {}: {}", targetUser.getId(), error.getMessage(), error);
                        }
                );
    }

    @Override
    public String getCommandName() {
        return "admin-ban";
    }
}
