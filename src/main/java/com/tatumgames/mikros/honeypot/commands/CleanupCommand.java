package com.tatumgames.mikros.honeypot.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.services.MessageDeletionService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for cleaning up messages from a user without banning.
 */
@SuppressWarnings("ClassCanBeRecord")
public class CleanupCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CleanupCommand.class);
    private final MessageDeletionService messageDeletionService;

    /**
     * Creates a new CleanupCommand handler.
     *
     * @param messageDeletionService the message deletion service
     */
    public CleanupCommand(MessageDeletionService messageDeletionService) {
        this.messageDeletionService = messageDeletionService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("cleanup", "Remove messages from a user without banning")
                .addOption(OptionType.USER, "user", "The user whose messages to remove", true)
                .addOption(OptionType.INTEGER, "days", "Number of days to look back (0-7, or -1 for all)", false)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        User targetUser = event.getOption("user", OptionMapping::getAsUser);

        if (AdminUtils.isInvalidTargetUser(member, targetUser, event)) {
            return; // stop executing the command
        }

        int days = event.getOption("days", 7, OptionMapping::getAsInt);

        // Validate days
        if (days < -1 || days > 7) {
            event.reply("❌ Days must be between -1 (all) and 7.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (targetUser == null) {
            event.getHook().sendMessage("❌ Target user not found.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply().queue();

        // Delete messages
        int daysToDelete = days == -1 ? Integer.MAX_VALUE : days;
        messageDeletionService.deleteAllUserMessages(guild, targetUser, daysToDelete)
                .thenAccept(count -> {
                    String timeRange = days == -1
                            ? "All messages"
                            : String.format("Messages from last %d day(s)", days);

                    event.getHook().sendMessage(String.format("""
                                    🧹 **Messages Cleaned Up**
                                    User: %s
                                    Time Range: %s
                                    Total messages deleted: %d
                                    Moderator: %s
                                    """,
                            targetUser.getName(),
                            timeRange,
                            count,
                            member.getAsMention()
                    )).queue();

                    logger.info("Cleaned up {} messages from user {} in guild {}",
                            count, targetUser.getId(), guild.getId());
                })
                .exceptionally(error -> {
                    logger.error("Error cleaning up messages from user {}: {}", targetUser.getId(), error.getMessage(), error);
                    event.getHook().sendMessage("❌ Failed to clean up messages: " + error.getMessage())
                            .setEphemeral(true)
                            .queue();
                    return null;
                });
    }

    @Override
    public String getCommandName() {
        return "cleanup";
    }
}

