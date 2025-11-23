package com.tatumgames.mikros.honeypot.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.services.MessageDeletionService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for cleaning up messages from a user without banning.
 */
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
        Member moderator = event.getMember();
        if (moderator == null || !moderator.hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        User targetUser = event.getOption("user").getAsUser();
        int days = event.getOption("days") != null
                ? event.getOption("days").getAsInt()
                : 7; // Default: 7 days
        
        // Validate days
        if (days < -1 || days > 7) {
            event.reply("❌ Days must be between -1 (all) and 7.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Validate target
        if (targetUser.isBot() && !targetUser.equals(event.getJDA().getSelfUser())) {
            event.reply("❌ You cannot clean up messages from other bots.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        event.deferReply().queue();
        
        // Delete messages
        int daysToDelete = days == -1 ? Integer.MAX_VALUE : days;
        messageDeletionService.deleteAllUserMessages(event.getGuild(), targetUser, daysToDelete)
                .thenAccept(count -> {
                    String timeRange = days == -1
                            ? "All messages"
                            : String.format("Messages from last %d day(s)", days);
                    
                    event.getHook().sendMessage(String.format(
                            "🧹 **Messages Cleaned Up**\n" +
                            "User: %s\n" +
                            "Time Range: %s\n" +
                            "Total messages deleted: %d\n" +
                            "Moderator: %s",
                            targetUser.getName(),
                            timeRange,
                            count,
                            moderator.getAsMention()
                    )).queue();
                    
                    logger.info("Cleaned up {} messages from user {} in guild {}",
                            count, targetUser.getId(), event.getGuild().getId());
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

