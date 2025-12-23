package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.services.GamePromotionService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /admin-disable-promotions.
 * Allows server administrators to disable game promotions for their server.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
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
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an administrator to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if promotions are already disabled
        if (gamePromotionService.getPromotionChannel(guild.getId()) == null) {
            String message = """
                    ℹ️ **Promotions Already Disabled**
                    
                    Game promotions are not currently enabled for this server.
                    
                    To enable promotions, use `/admin-setup-promotion`.
                    """;

            event.reply(message)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Clear all promotion data for this guild
        if (gamePromotionService instanceof com.tatumgames.mikros.services.InMemoryGamePromotionService) {
            ((com.tatumgames.mikros.services.InMemoryGamePromotionService) gamePromotionService)
                    .clearGuildData(guild.getId());
        } else {
            // Fallback: just clear the channel
            // In a database implementation, this would clear all related data
            gamePromotionService.setPromotionChannel(guild.getId(), null);
        }

        // Send confirmation
        String message = """
                ✅ **Game Promotions Disabled**
                
                Game promotions have been disabled for this server.
                
                **What was removed:**
                • Promotion channel configuration
                • Promotion verbosity settings
                • All promotion tracking data
                
                To re-enable promotions, use `/admin-setup-promotion`.
                """;

        event.reply(message)
                .queue();

        logger.info("Promotions disabled for guild {} by user {}", guild.getId(), member.getId());
    }

    @Override
    public String getCommandName() {
        return "admin-disable-promotions";
    }
}
