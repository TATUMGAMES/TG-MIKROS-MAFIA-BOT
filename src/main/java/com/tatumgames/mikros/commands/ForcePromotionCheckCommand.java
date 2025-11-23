package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.services.GamePromotionScheduler;
import com.tatumgames.mikros.services.GamePromotionService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /force-promotion-check.
 * Manually triggers the promotion check and posting logic.
 */
public class ForcePromotionCheckCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ForcePromotionCheckCommand.class);
    private final GamePromotionScheduler gamePromotionScheduler;
    private final GamePromotionService gamePromotionService;
    
    /**
     * Creates a new ForcePromotionCheckCommand handler.
     * 
     * @param gamePromotionScheduler the promotion scheduler
     * @param gamePromotionService the promotion service
     */
    public ForcePromotionCheckCommand(GamePromotionScheduler gamePromotionScheduler,
                                     GamePromotionService gamePromotionService) {
        this.gamePromotionScheduler = gamePromotionScheduler;
        this.gamePromotionService = gamePromotionService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-force-promotion-check", "Manually trigger a game promotion check")
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an administrator to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Check if promotion channel is configured
        String guildId = event.getGuild().getId();
        if (gamePromotionService.getPromotionChannel(guildId) == null) {
            event.reply("⚠️ **Promotion channel not configured**\n\n" +
                    "Please use `/setup-promotion-channel` first to designate a channel for promotions.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Defer reply as this might take a moment
        event.deferReply().queue();
        
        // Trigger promotion check
        int promotionsPosted = gamePromotionScheduler.forceCheckGuild(event.getGuild());
        
        // Send result
        if (promotionsPosted > 0) {
            event.getHook().sendMessage(String.format(
                    "✅ **Promotion Check Complete**\n\n" +
                    "Posted %d game promotion(s) to the configured channel.\n\n" +
                    "Check your promotion channel to see the new posts!",
                    promotionsPosted
            )).queue();
        } else {
            event.getHook().sendMessage(
                    "ℹ️ **No Promotions Available**\n\n" +
                    "There are currently no new game promotions to post.\n\n" +
                    "Possible reasons:\n" +
                    "• All available promotions have already been posted\n" +
                    "• No promotions have passed their deadline yet\n" +
                    "• The promotion API is not yet integrated\n\n" +
                    "The scheduler will continue checking automatically."
            ).queue();
        }
        
        logger.info("Forced promotion check for guild {} by user {}, posted {} promotions",
                guildId, member.getId(), promotionsPosted);
    }
    
    @Override
    public String getCommandName() {
        return "admin-force-promotion-check";
    }
}

