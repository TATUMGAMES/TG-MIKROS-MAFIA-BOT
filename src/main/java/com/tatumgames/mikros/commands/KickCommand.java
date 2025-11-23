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
 * Command handler for the /kick command.
 * Kicks a user from the server and logs the action.
 */
public class KickCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(KickCommand.class);
    private final ModerationLogService moderationLogService;
    
    /**
     * Creates a new KickCommand handler.
     * 
     * @param moderationLogService the moderation log service
     */
    public KickCommand(ModerationLogService moderationLogService) {
        this.moderationLogService = moderationLogService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-kick", "Kick a user from the server")
                .addOption(OptionType.USER, "user", "The user to kick", true)
                .addOption(OptionType.STRING, "reason", "The reason for the kick", true)
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member moderator = event.getMember();
        if (moderator == null || !moderator.hasPermission(Permission.KICK_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get command options
        User targetUser = event.getOption("user").getAsUser();
        String reason = event.getOption("reason").getAsString();
        Member targetMember = event.getGuild().getMember(targetUser);
        
        // Validate target
        if (targetUser.isBot()) {
            event.reply("❌ You cannot kick a bot.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (targetUser.getId().equals(moderator.getId())) {
            event.reply("❌ You cannot kick yourself.")
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
        if (!moderator.canInteract(targetMember)) {
            event.reply("❌ You cannot kick this user due to role hierarchy.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Check bot permissions
        if (!event.getGuild().getSelfMember().canInteract(targetMember)) {
            event.reply("❌ I cannot kick this user due to role hierarchy.")
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
                ActionType.KICK,
                reason,
                Instant.now(),
                event.getGuild().getId()
        );
        
        moderationLogService.logAction(action);
        
        // Defer the reply as kicking might take a moment
        event.deferReply().queue();
        
        // Perform the kick
        event.getGuild().kick(targetMember)
                .reason(reason)
                .queue(
                        success -> {
                            event.getHook().sendMessage(String.format(
                                    "👢 **User Kicked**\n" +
                                    "User: %s\n" +
                                    "Reason: %s\n" +
                                    "Moderator: %s",
                                    targetUser.getName(),
                                    reason,
                                    moderator.getAsMention()
                            )).queue();
                            
                            logger.info("User {} kicked by {} in guild {}: {}", 
                                    targetUser.getId(), moderator.getId(), event.getGuild().getId(), reason);
                        },
                        error -> {
                            event.getHook().sendMessage("❌ Failed to kick user: " + error.getMessage())
                                    .setEphemeral(true)
                                    .queue();
                            logger.error("Failed to kick user {}: {}", targetUser.getId(), error.getMessage(), error);
                        }
                );
    }
    
    @Override
    public String getCommandName() {
        return "admin-kick";
    }
}

