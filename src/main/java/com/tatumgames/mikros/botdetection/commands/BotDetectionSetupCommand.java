package com.tatumgames.mikros.botdetection.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.botdetection.config.BotDetectionConfig;
import com.tatumgames.mikros.botdetection.service.BotDetectionService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /admin-bot-detection-setup.
 * Allows administrators to enable/disable bot detection.
 */
public class BotDetectionSetupCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(BotDetectionSetupCommand.class);
    private final BotDetectionService botDetectionService;

    /**
     * Creates a new BotDetectionSetupCommand handler.
     *
     * @param botDetectionService the bot detection service
     */
    public BotDetectionSetupCommand(BotDetectionService botDetectionService) {
        this.botDetectionService = botDetectionService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-bot-detection-setup", "Enable or disable bot detection system")
                .addOption(OptionType.BOOLEAN, "enabled", "Enable bot detection (true/false)", true)
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an administrator to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Boolean enabled = event.getOption("enabled", b -> b.getAsBoolean());
        if (enabled == null) {
            event.reply("❌ You must specify enabled (true/false).")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String guildId = guild.getId();
        BotDetectionConfig config = botDetectionService.getConfig(guildId);
        config.setEnabled(enabled);
        botDetectionService.updateConfig(guildId, config);

        event.reply(String.format("""
                        ✅ **Bot Detection System %s**
                        
                        **Status:** %s
                        **Account Age Threshold:** %d days
                        **Link Restriction:** %d minutes after join
                        **Multi-Channel Spam Threshold:** %d channels
                        **Auto Action:** %s
                        **Reputation Reporting:** %s
                        
                        Use `/admin-bot-detection-config` to customize settings.
                        """,
                enabled ? "Enabled" : "Disabled",
                enabled ? "Active" : "Inactive",
                config.getAccountAgeThresholdDays(),
                config.getLinkRestrictionMinutes(),
                config.getMultiChannelSpamThreshold(),
                config.getAutoAction(),
                config.isReportToReputation() ? "Enabled" : "Disabled"
        )).queue();

        logger.info("Bot detection {} for guild {} by user {}",
                enabled ? "enabled" : "disabled", guildId, member.getId());
    }

    @Override
    public String getCommandName() {
        return "admin-bot-detection-setup";
    }
}

