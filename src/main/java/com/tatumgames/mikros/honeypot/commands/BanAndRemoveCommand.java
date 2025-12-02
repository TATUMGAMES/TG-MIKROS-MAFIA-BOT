package com.tatumgames.mikros.honeypot.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import com.tatumgames.mikros.services.MessageDeletionService;
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
 * Command handler for banning a user and removing all their messages.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BanAndRemoveCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(BanAndRemoveCommand.class);
    private final ModerationLogService moderationLogService;
    private final MessageDeletionService messageDeletionService;

    /**
     * Creates a new BanAndRemoveCommand handler.
     *
     * @param moderationLogService   the moderation log service
     * @param messageDeletionService the message deletion service
     */
    public BanAndRemoveCommand(ModerationLogService moderationLogService,
                               MessageDeletionService messageDeletionService) {
        this.moderationLogService = moderationLogService;
        this.messageDeletionService = messageDeletionService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("ban_and_remove_all_messages", "Ban a user and delete all their messages")
                .addOption(OptionType.USER, "user", "The user to ban", true)
                .addOption(OptionType.STRING, "reason", "Reason for the ban", true)
                .addOption(OptionType.INTEGER, "delete_days", "Days of messages to delete (0-7, or -1 for all)", false)
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
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
        Member targetMember = event.getGuild().getMember(targetUser);

        if (AdminUtils.isInvalidTargetUser(member, targetUser, event)) {
            return; // stop executing the command
        }

        int deleteDays = event.getOption("delete_days", 7, OptionMapping::getAsInt);

        // Validate delete_days
        if (deleteDays < -1 || deleteDays > 7) {
            event.reply("❌ Delete days must be between -1 (all) and 7.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (targetUser == null) {
            event.reply("You must specify a user.").setEphemeral(true).queue();
            return;
        }

        // Check role hierarchy
        if (targetMember != null) {
            if (!member.canInteract(targetMember)) {
                event.reply("❌ You cannot ban this user due to role hierarchy.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (!event.getGuild().getSelfMember().canInteract(targetMember)) {
                event.reply("❌ I cannot ban this user due to role hierarchy.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        // Log the action
        ModerationAction action = new ModerationAction(
                targetUser.getId(),
                targetUser.getEffectiveName(),
                member.getId(),
                member.getEffectiveName(),
                ActionType.BAN,
                reason,
                Instant.now(),
                event.getGuild().getId()
        );
        moderationLogService.logAction(action);

        // Defer reply
        event.deferReply().queue();

        // Ban the user
        int daysToBan = Math.max(deleteDays, 0);
        event.getGuild().ban(targetUser, daysToBan, java.util.concurrent.TimeUnit.DAYS)
                .reason(reason)
                .queue(
                        success -> {
                            // Determine how many days of messages to delete
                            int daysToDelete = (deleteDays == -1) ? Integer.MAX_VALUE : deleteDays;

                            messageDeletionService.deleteAllUserMessages(event.getGuild(), targetUser, daysToDelete)
                                    .thenAccept(count -> {
                                        String deleteInfo = (deleteDays == -1)
                                                ? "All messages deleted"
                                                : String.format("%d day(s) of messages deleted", deleteDays);

                                        event.getHook().sendMessage(String.format("""
                                                        🔨 **User Banned and Messages Removed**
                                                        User: %s
                                                        Reason: %s
                                                        Moderator: %s
                                                        %s
                                                        Total messages deleted: %d
                                                        """,
                                                targetUser.getName(),
                                                reason,
                                                member.getAsMention(),
                                                deleteInfo,
                                                count
                                        )).queue();

                                        logger.info("Banned user {} and deleted {} messages in guild {}",
                                                targetUser.getId(), count, event.getGuild().getId());
                                    })
                                    .exceptionally(error -> {
                                        logger.error("Error deleting messages from user {}: {}", targetUser.getId(), error.getMessage(), error);
                                        event.getHook().sendMessage(String.format("""
                                                        🔨 **User Banned**
                                                        User: %s
                                                        Reason: %s
                                                        ⚠️ Warning: Failed to delete some messages. Check logs for details.
                                                        """,
                                                targetUser.getName(),
                                                reason
                                        )).queue();
                                        return null;
                                    });
                        },
                        error -> {
                            event.getHook().sendMessage(String.format("""
                                            ❌ Failed to ban user
                                            User: %s
                                            Error: %s
                                            """,
                                    targetUser.getName(),
                                    error.getMessage()
                            )).setEphemeral(true).queue();

                            logger.error("Failed to ban user {}: {}", targetUser.getId(), error.getMessage(), error);
                        }
                );
    }

    @Override
    public String getCommandName() {
        return "ban_and_remove_all_messages";
    }
}
