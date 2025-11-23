package com.tatumgames.mikros.promo.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.promo.config.PromoConfig;
import com.tatumgames.mikros.promo.service.PromoDetectionService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

/**
 * Command handler for /setup-promotions.
 * Allows administrators to enable/disable smart promotional detection.
 */
public class SetupPromotionsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetupPromotionsCommand.class);
    private final PromoDetectionService promoService;
    
    /**
     * Creates a new SetupPromotionsCommand handler.
     * 
     * @param promoService the promotional detection service
     */
    public SetupPromotionsCommand(PromoDetectionService promoService) {
        this.promoService = promoService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-setup-promotions", "Enable or disable smart promotional detection (admin only)")
                .addOption(OptionType.BOOLEAN, "enabled", "Enable promotional detection?", true)
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
        boolean enabled = event.getOption("enabled").getAsBoolean();
        
        PromoConfig config = promoService.getConfig(guildId);
        config.setEnabled(enabled);
        promoService.updateConfig(config);
        
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚙️ Promotional Detection Configuration");
        embed.setColor(enabled ? Color.GREEN : Color.RED);
        
        embed.setDescription(String.format(
                "Smart promotional detection is now **%s**",
                enabled ? "ENABLED" : "DISABLED"
        ));
        
        embed.addField(
                "Current Settings",
                String.format(
                        "**Status:** %s\n" +
                        "**Cooldown:** %d days\n" +
                        "**DM Prompts:** %s\n" +
                        "**Channel Prompts:** %s",
                        enabled ? "✅ Enabled" : "❌ Disabled",
                        config.getCooldownDays(),
                        config.isSendDm() ? "Yes" : "No",
                        config.isSendInChannel() ? "Yes" : "No"
                ),
                false
        );
        
        if (enabled) {
            embed.addField(
                    "📝 How It Works",
                    "The bot will detect messages containing launch-related phrases like:\n" +
                    "• \"We're launching our game...\"\n" +
                    "• \"Steam page is live\"\n" +
                    "• \"Need help promoting\"\n\n" +
                    "Users will receive a gentle prompt offering MIKROS promotional help.",
                    false
            );
        }
        
        embed.setFooter("Use /set-promo-frequency to adjust cooldown settings");
        embed.setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.info("Promotional detection {} for guild {} by user {}",
                enabled ? "enabled" : "disabled", guildId, member.getId());
    }
    
    @Override
    public String getCommandName() {
        return "admin-setup-promotions";
    }
}
