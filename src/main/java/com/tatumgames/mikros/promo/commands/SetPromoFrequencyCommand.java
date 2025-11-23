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
 * Command handler for /set-promo-frequency.
 * Allows administrators to set the cooldown for promotional prompts.
 */
public class SetPromoFrequencyCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetPromoFrequencyCommand.class);
    private final PromoDetectionService promoService;
    
    /**
     * Creates a new SetPromoFrequencyCommand handler.
     * 
     * @param promoService the promotional detection service
     */
    public SetPromoFrequencyCommand(PromoDetectionService promoService) {
        this.promoService = promoService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-set-promo-frequency", "Set promotional prompt cooldown (admin only)")
                .addOption(OptionType.INTEGER, "days", "Cooldown in days (1-30, default: 7)", true)
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
        int days = event.getOption("days").getAsInt();
        
        if (days < 1 || days > 30) {
            event.reply("❌ Cooldown must be between 1 and 30 days.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        PromoConfig config = promoService.getConfig(guildId);
        config.setCooldownDays(days);
        promoService.updateConfig(config);
        
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⏱️ Promotional Frequency Updated");
        embed.setColor(Color.CYAN);
        
        embed.setDescription(String.format(
                "Promotional prompt cooldown set to **%d day%s**",
                days,
                days == 1 ? "" : "s"
        ));
        
        embed.addField(
                "What This Means",
                String.format(
                        "Users will receive at most **1 promotional prompt per %d day%s**.\n\n" +
                        "This helps prevent spam while still reaching developers who need help.",
                        days,
                        days == 1 ? "" : "s"
                ),
                false
        );
        
        embed.setFooter("Use /setup-promotions to enable/disable detection");
        embed.setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.info("Promotional cooldown set to {} days for guild {} by user {}",
                days, guildId, member.getId());
    }
    
    @Override
    public String getCommandName() {
        return "admin-set-promo-frequency";
    }
}
