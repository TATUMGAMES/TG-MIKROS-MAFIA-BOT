package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.services.GamePromotionService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /admin-disable-promotions.
 * Allows server administrators to disable game promotions for their server.
 */
public class DisablePromotionsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(DisablePromotionsCommand.class);
    private final GamePromotionService gamePromotionService;
    
    /**
     * Creates a new DisablePromotionsCommand handler.
     * 
     * @param gamePromotionService the game promotion service
     */
    public DisablePromotionsCommand(GamePromotionService gamePromotionService) {
        this.gamePromotionService = gamePromotionService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-disable-promotions", "Disable game promotions for this server")
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
        
        String guildId = event.getGuild().getId();
        
        // Check if promotions are already disabled
        if (gamePromotionService.getPromotionChannel(guildId) == null) {
            event.reply("ℹ️ **Promotions Already Disabled**\n\n" +
                    "Game promotions are not currently enabled for this server.\n\n" +
                    "To enable promotions, use `/admin-setup-promotion-channel`.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Clear all promotion data for this guild
        if (gamePromotionService instanceof com.tatumgames.mikros.services.InMemoryGamePromotionService) {
            ((com.tatumgames.mikros.services.InMemoryGamePromotionService) gamePromotionService)
                    .clearGuildData(guildId);
        } else {
            // Fallback: just clear the channel
            // In a database implementation, this would clear all related data
            gamePromotionService.setPromotionChannel(guildId, null);
        }
        
        // Send confirmation
        event.reply("✅ **Game Promotions Disabled**\n\n" +
                "Game promotions have been disabled for this server.\n\n" +
                "**What was removed:**\n" +
                "• Promotion channel configuration\n" +
                "• Promotion verbosity settings\n" +
                "• All promotion tracking data\n\n" +
                "To re-enable promotions, use `/admin-setup-promotion-channel`.")
                .queue();
        
        logger.info("Promotions disabled for guild {} by user {}", guildId, member.getId());
    }
    
    @Override
    public String getCommandName() {
        return "admin-disable-promotions";
    }
}

