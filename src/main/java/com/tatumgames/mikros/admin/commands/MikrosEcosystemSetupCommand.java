package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.services.GameStatsService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /admin-mikros-ecosystem-setup.
 * Allows administrators to configure the MIKROS Ecosystem channel for their server.
 */
@SuppressWarnings("ClassCanBeRecord")
public class MikrosEcosystemSetupCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(MikrosEcosystemSetupCommand.class);
    private final GameStatsService gameStatsService;

    /**
     * Creates a new MikrosEcosystemSetupCommand handler.
     *
     * @param gameStatsService the game stats service
     */
    public MikrosEcosystemSetupCommand(GameStatsService gameStatsService) {
        this.gameStatsService = gameStatsService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-mikros-ecosystem-setup", "Configure the MIKROS Ecosystem channel for your server")
                .addOption(OptionType.CHANNEL, "channel", "Channel for MIKROS Ecosystem analytics commands", true)
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

        // Get the channel option
        MessageChannel channel = AdminUtils.getValidTextChannel(event, "channel");
        if (channel == null) return;

        // Validate bot can post in channel
        if (!channel.canTalk()) {
            event.reply("❌ I don't have permission to send messages in " + channel.getAsMention() + ".")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Save the configuration
        String guildId = guild.getId();
        String channelId = channel.getId();

        gameStatsService.setupEcosystem(guildId, channelId);

        // Send confirmation
        event.reply(String.format("""
                        ✅ **MIKROS Ecosystem Configured!**
                        
                        **Ecosystem Channel:** %s
                        **Status:** Enabled
                        
                        📊 The MIKROS Ecosystem is now active!
                        
                        **Next Steps:**
                        • Use `/mikros-ecosystem` commands in this channel to view analytics
                        • All 13 analytics subcommands are available
                        """,
                channel.getAsMention()
        )).queue();

        logger.info("MIKROS Ecosystem setup for guild {} by user {}: channel={}",
                guildId, member.getId(), channelId);
    }

    @Override
    public String getCommandName() {
        return "admin-mikros-ecosystem-setup";
    }
}

