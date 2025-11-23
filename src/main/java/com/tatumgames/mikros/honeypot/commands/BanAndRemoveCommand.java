package com.tatumgames.mikros.honeypot.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import com.tatumgames.mikros.services.MessageDeletionService;
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
 * Command handler for banning a user and removing all their messages.
 */
public class BanAndRemoveCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(BanAndRemoveCommand.class);
    private final ModerationLogService moderationLogService;
    private final MessageDeletionService messageDeletionService;
    
    /**
     * Creates a new BanAndRemoveCommand handler.
     * 
     * @param moderationLogService the moderation log service
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
        Member moderator = event.getMember();
        if (moderator == null || !moderator.hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        User targetUser = event.getOption("user").getAsUser();
        String reason = event.getOption("reason").getAsString();
        int deleteDays = event.getOption("delete_days") != null
                ? event.getOption("delete_days").getAsInt()
                : 7; // Default: 7 days
        
        // Validate delete_days
        if (deleteDays < -1 || deleteDays > 7) {
            event.reply("❌ Delete days must be between -1 (all) and 7.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Validate target
        if (targetUser.isBot()) {
            event.reply("❌ You cannot ban a bot.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (targetUser.getId().equals(moderator.getId())) {
            event.reply("❌ You cannot ban yourself.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        Member targetMember = event.getGuild().getMember(targetUser);
        
        // Check role hierarchy
        if (targetMember != null) {
            if (!moderator.canInteract(targetMember)) {
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
                moderator.getId(),
                moderator.getEffectiveName(),
                ActionType.BAN,
                reason,
                Instant.now(),
                event.getGuild().getId()
        );
        moderationLogService.logAction(action);
        
        // Defer reply
        event.deferReply().queue();
        
        // Ban the user
        int finalDeleteDays = deleteDays;
        event.getGuild().ban(targetUser, finalDeleteDays >= 0 ? finalDeleteDays : 0, java.util.concurrent.TimeUnit.DAYS)
                .reason(reason)
                .queue(
                        success -> {
                            // Delete all messages
                            int daysToDelete = finalDeleteDays == -1 ? Integer.MAX_VALUE : finalDeleteDays;
                            messageDeletionService.deleteAllUserMessages(event.getGuild(), targetUser, daysToDelete)
                                    .thenAccept(count -> {
                                        String deleteInfo = finalDeleteDays == -1
                                                ? "All messages deleted"
                                                : String.format("%d day(s) of messages deleted", finalDeleteDays);
                                        
                                        event.getHook().sendMessage(String.format(
                                                "🔨 **User Banned and Messages Removed**\n" +
                                                "User: %s\n" +
                                                "Reason: %s\n" +
                                                "Moderator: %s\n" +
                                                "%s\n" +
                                                "Total messages deleted: %d",
                                                targetUser.getName(),
                                                reason,
                                                moderator.getAsMention(),
                                                deleteInfo,
                                                count
                                        )).queue();
                                        
                                        logger.info("Banned user {} and deleted {} messages in guild {}",
                                                targetUser.getId(), count, event.getGuild().getId());
                                    })
                                    .exceptionally(error -> {
                                        logger.error("Error deleting messages from user {}: {}", targetUser.getId(), error.getMessage(), error);
                                        event.getHook().sendMessage(String.format(
                                                "🔨 **User Banned**\n" +
                                                "User: %s\n" +
                                                "Reason: %s\n" +
                                                "⚠️ Warning: Failed to delete some messages. Check logs for details.",
                                                targetUser.getName(),
                                                reason
                                        )).queue();
                                        return null;
                                    });
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
        return "ban_and_remove_all_messages";
    }
}





