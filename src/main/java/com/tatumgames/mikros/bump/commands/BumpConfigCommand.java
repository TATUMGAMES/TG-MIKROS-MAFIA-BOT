package com.tatumgames.mikros.bump.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.bump.model.BumpConfig;
import com.tatumgames.mikros.bump.service.BumpService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;

/**
 * Command handler for /admin-bump-config.
 * Allows administrators to configure auto-bump settings for their server.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BumpConfigCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(BumpConfigCommand.class);
    private final BumpService bumpService;

    /**
     * Creates a new BumpConfigCommand handler.
     *
     * @param bumpService the bump service
     */
    public BumpConfigCommand(BumpService bumpService) {
        this.bumpService = bumpService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-bump-config", "Configure auto-bump settings (admin only)")
                .addSubcommands(
                        new SubcommandData("view", "View current bump configuration"),
                        new SubcommandData("set-interval", "Set the bump interval (1-24 hours)")
                                .addOption(OptionType.INTEGER, "interval", "Interval in hours (1-24)", true),
                        new SubcommandData("update-bots", "Update which bots to bump")
                                .addOptions(
                                        new net.dv8tion.jda.api.interactions.commands.build.OptionData(OptionType.STRING, "bots", "Which bots to bump (disboard, disurl, both)", true)
                                                .addChoice("Disboard only", "disboard")
                                                .addChoice("Disurl only", "disurl")
                                                .addChoice("Both", "both")
                                ),
                        new SubcommandData("disable", "Disable auto-bump for this server")
                )
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

        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("❌ Please specify a subcommand.").setEphemeral(true).queue();
            return;
        }

        // Get guild id
        String guildId = guild.getId();

        switch (subcommand) {
            case "view" -> handleView(event, guildId);
            case "set-interval" -> handleSetInterval(event, guildId);
            case "update-bots" -> handleUpdateBots(event, guildId);
            case "disable" -> handleDisable(event, guildId);
            default -> event.reply("❌ Unknown subcommand.").setEphemeral(true).queue();
        }
    }

    private void handleView(SlashCommandInteractionEvent event, String guildId) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚙️ Auto-Bump Configuration");
        embed.setColor(Color.CYAN);

        BumpConfig config = bumpService.getConfig(guildId);
        String channelId = config.getChannelId();
        EnumSet<BumpConfig.BumpBot> enabledBots = config.getEnabledBots();
        int intervalHours = config.getIntervalHours();

        // Status field
        embed.addField(
                "Status",
                config.isEnabled() ? "✅ Enabled" : "❌ Not configured",
                true
        );

        // Bump channel field
        embed.addField(
                "Bump Channel",
                channelId != null ? "<#" + channelId + ">" : "❌ Not configured",
                true
        );

        // Enabled bots field
        String botsDisplay = enabledBots.isEmpty()
                ? "❌ None"
                : String.join(", ", enabledBots.stream()
                .map(BumpConfig.BumpBot::getDisplayName)
                .toList());
        embed.addField(
                "Enabled Bots",
                botsDisplay,
                true
        );

        // Interval field
        embed.addField(
                "Bump Interval",
                intervalHours + " hour(s)",
                true
        );

        // Last bump times
        if (!enabledBots.isEmpty()) {
            StringBuilder lastBumps = new StringBuilder();
            for (BumpConfig.BumpBot bot : enabledBots) {
                Instant lastBump = config.getLastBumpTime(bot);
                if (lastBump != null) {
                    long hoursAgo = ChronoUnit.HOURS.between(lastBump, Instant.now());
                    lastBumps.append(String.format("%s: %d hour(s) ago\n", bot.getDisplayName(), hoursAgo));
                } else {
                    lastBumps.append(String.format("%s: Never\n", bot.getDisplayName()));
                }
            }
            embed.addField(
                    "Last Bump Times",
                    lastBumps.toString().trim(),
                    false
            );
        }

        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleSetInterval(SlashCommandInteractionEvent event, String guildId) {
        // Check if bump is set up
        if (bumpService.getBumpChannel(guildId) == null) {
            event.reply("❌ Auto-bump not set up yet. Use `/admin-bump-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get the interval option
        Integer interval = event.getOption("interval", OptionMapping::getAsInt);
        if (interval == null) {
            event.reply("❌ You must specify an interval.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Validate interval
        if (interval < 1 || interval > 24) {
            event.reply("❌ Interval must be between 1 and 24 hours.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Save the configuration
        bumpService.setBumpInterval(guildId, interval);

        // Send confirmation
        event.reply(String.format(
                "✅ **Bump Interval Updated**\n\n" +
                        "**New Interval:** %d hour(s)\n\n" +
                        "The bot will automatically bump your server every %d hour(s).",
                interval,
                interval
        )).queue();

        logger.info("Bump interval set to {} hours for guild {} by user {}",
                interval, guildId, event.getUser().getId());
    }

    private void handleUpdateBots(SlashCommandInteractionEvent event, String guildId) {
        // Check if bump is set up
        if (bumpService.getBumpChannel(guildId) == null) {
            event.reply("❌ Auto-bump not set up yet. Use `/admin-bump-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get the bots option
        String botsValue = event.getOption("bots", OptionMapping::getAsString);
        if (botsValue == null) {
            event.reply("❌ You must select which bots to bump.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Parse bots selection
        EnumSet<BumpConfig.BumpBot> enabledBots = parseBotsSelection(botsValue);
        if (enabledBots.isEmpty()) {
            event.reply("❌ Invalid bot selection.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Save the configuration
        bumpService.setEnabledBots(guildId, enabledBots);

        // Build bot list string
        String botsList = String.join(", ", enabledBots.stream()
                .map(BumpConfig.BumpBot::getDisplayName)
                .toList());

        // Send confirmation
        event.reply(String.format(
                "✅ **Enabled Bots Updated**\n\n" +
                        "**Enabled Bots:** %s\n\n" +
                        "The bot will now bump using these services.",
                botsList
        )).queue();

        logger.info("Enabled bots updated for guild {} by user {}: {}",
                guildId, event.getUser().getId(), enabledBots);
    }

    private void handleDisable(SlashCommandInteractionEvent event, String guildId) {
        // Check if bump is already disabled
        if (bumpService.getBumpChannel(guildId) == null) {
            event.reply("ℹ️ **Auto-Bump Already Disabled**\n\n" +
                            "Auto-bump is not currently enabled for this server.\n\n" +
                            "To enable, use `/admin-bump-setup`.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Clear all bump data for this guild
        bumpService.clearGuildData(guildId);

        // Send confirmation
        event.reply("✅ **Auto-Bump Disabled**\n\n" +
                        "Auto-bump has been disabled for this server.\n\n" +
                        "**What was removed:**\n" +
                        "• Bump channel configuration\n" +
                        "• Enabled bots settings\n" +
                        "• Bump interval settings\n" +
                        "• All bump tracking data\n\n" +
                        "To re-enable, use `/admin-bump-setup`.")
                .queue();

        logger.info("Auto-bump disabled for guild {} by user {}", guildId, event.getUser().getId());
    }

    /**
     * Parses the bots selection string into an EnumSet.
     *
     * @param botsValue the selection value ("disboard", "disurl", or "both")
     * @return the set of enabled bots
     */
    private EnumSet<BumpConfig.BumpBot> parseBotsSelection(String botsValue) {
        return switch (botsValue.toLowerCase()) {
            case "disboard" -> EnumSet.of(BumpConfig.BumpBot.DISBOARD);
            case "disurl" -> EnumSet.of(BumpConfig.BumpBot.DISURL);
            case "both" -> EnumSet.of(BumpConfig.BumpBot.DISBOARD, BumpConfig.BumpBot.DISURL);
            default -> EnumSet.noneOf(BumpConfig.BumpBot.class);
        };
    }

    @Override
    public String getCommandName() {
        return "admin-bump-config";
    }
}

