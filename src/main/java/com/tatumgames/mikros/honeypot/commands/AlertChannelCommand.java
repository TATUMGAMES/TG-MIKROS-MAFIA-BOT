package com.tatumgames.mikros.honeypot.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.honeypot.service.HoneypotService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for setting the admin alert channel for honeypot triggers.
 */
public class AlertChannelCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(AlertChannelCommand.class);
    private final HoneypotService honeypotService;

    /**
     * Creates a new AlertChannelCommand handler.
     *
     * @param honeypotService the honeypot service
     */
    public AlertChannelCommand(HoneypotService honeypotService) {
        this.honeypotService = honeypotService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("alert_channel", "Set the admin channel for bot alerts (e.g., honeypot triggers)")
                .addOption(OptionType.CHANNEL, "channel", "The channel to send alerts to (leave empty to clear)", false)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        var config = honeypotService.getConfig(event.getGuild().getId());

        if (event.getOption("channel") == null) {
            // Clear alert channel
            config.setAlertChannelId(null);
            event.reply("✅ Alert channel cleared. Honeypot triggers will no longer send alerts.")
                    .setEphemeral(true)
                    .queue();
            logger.info("Alert channel cleared for guild {}", event.getGuild().getId());
            return;
        }

        // Get the channel option
        TextChannel channel = AdminUtils.getValidTextChannel(event, "channel");
        if (channel == null) return;

        // Check bot permissions
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS)) {
            event.reply("❌ I don't have permission to send messages in that channel.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        config.setAlertChannelId(channel.getId());
        event.reply(String.format("""
                        ✅ **Alert Channel Set**
                        Alerts will be sent to: %s
                        Honeypot triggers and other admin events will be logged here.
                        """,
                channel.getAsMention()
        )).setEphemeral(true).queue();

        logger.info("Alert channel set to {} for guild {}", channel.getName(), event.getGuild().getId());
    }

    @Override
    public String getCommandName() {
        return "alert_channel";
    }
}
