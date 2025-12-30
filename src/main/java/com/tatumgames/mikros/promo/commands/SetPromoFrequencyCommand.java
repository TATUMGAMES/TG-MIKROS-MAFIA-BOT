package com.tatumgames.mikros.promo.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.promo.config.PromoConfig;
import com.tatumgames.mikros.promo.service.PromoDetectionService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

/**
 * Command handler for /set-promo-frequency.
 * Allows administrators to set the cooldown for promotional prompts.
 */
@SuppressWarnings("ClassCanBeRecord")
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
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an administrator to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get guild id
        String guildId = guild.getId();
        int days = Optional.ofNullable(event.getOption("days"))
                .map(OptionMapping::getAsInt)
                .orElse(1);

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
                String.format("""
                                Users will receive at most **1 promotional prompt per %d day%s**.
                                
                                This helps prevent spam while still reaching developers who need help.
                                """,
                        days,
                        days == 1 ? "" : "s"
                ),
                false
        );

        embed.setFooter("Use /setup-promotions to enable/disable detection");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.info("Promotional cooldown set to {} days for guild {} by user {}",
                days, guildId, member.getId());
    }

    @Override
    public String getCommandName() {
        return "admin-set-promo-frequency";
    }
}
