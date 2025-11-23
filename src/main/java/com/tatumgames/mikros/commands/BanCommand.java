package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
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
 * Command handler for the /ban command.
 * Bans a user from the server and logs the action.
 */
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
        Member moderator = event.getMember();
        if (moderator == null || !moderator.hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get command options
        User targetUser = event.getOption("user").getAsUser();
        String reason = event.getOption("reason").getAsString();
        int deleteDays = event.getOption("delete_days") != null 
                ? event.getOption("delete_days").getAsInt() 
                : 0;
        
        // Validate delete_days
        if (deleteDays < 0 || deleteDays > 7) {
            event.reply("❌ Delete days must be between 0 and 7.")
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
        
        // Check role hierarchy if member is in guild
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
        
        // Create and log the moderation action
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
        
        // Defer the reply as banning might take a moment
        event.deferReply().queue();
        
        // Perform the ban
        event.getGuild().ban(targetUser, deleteDays, java.util.concurrent.TimeUnit.DAYS)
                .reason(reason)
                .queue(
                        success -> {
                            event.getHook().sendMessage(String.format(
                                    "🔨 **User Banned**\n" +
                                    "User: %s\n" +
                                    "Reason: %s\n" +
                                    "Moderator: %s\n" +
                                    "Messages deleted: %d day(s)",
                                    targetUser.getName(),
                                    reason,
                                    moderator.getAsMention(),
                                    deleteDays
                            )).queue();
                            
                            logger.info("User {} banned by {} in guild {}: {}", 
                                    targetUser.getId(), moderator.getId(), event.getGuild().getId(), reason);
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

