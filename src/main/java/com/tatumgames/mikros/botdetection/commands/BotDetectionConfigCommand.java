package com.tatumgames.mikros.botdetection.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.botdetection.config.BotDetectionConfig;
import com.tatumgames.mikros.botdetection.service.BotDetectionService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Command handler for /admin-bot-detection-config.
 * Allows administrators to configure bot detection settings.
 */
public class BotDetectionConfigCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(BotDetectionConfigCommand.class);
    private final BotDetectionService botDetectionService;

    /**
     * Creates a new BotDetectionConfigCommand handler.
     *
     * @param botDetectionService the bot detection service
     */
    public BotDetectionConfigCommand(BotDetectionService botDetectionService) {
        this.botDetectionService = botDetectionService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-bot-detection-config", "Configure bot detection settings (admin only)")
                .addSubcommands(
                        new SubcommandData("view", "View current bot detection configuration"),
                        new SubcommandData("set-account-age-threshold", "Set account age threshold in days")
                                .addOption(OptionType.INTEGER, "days", "Account age threshold (1-365)", true),
                        new SubcommandData("set-link-restriction-minutes", "Set link restriction time after joining")
                                .addOption(OptionType.INTEGER, "minutes", "Minutes before links allowed (1-1440)", true),
                        new SubcommandData("set-multi-channel-threshold", "Set multi-channel spam threshold")
                                .addOption(OptionType.INTEGER, "threshold", "Number of channels for spam detection (2-10)", true),
                        new SubcommandData("set-auto-action", "Set automatic action when bot detected")
                                .addOptions(createActionOption()),
                        new SubcommandData("toggle-reputation-reporting", "Enable or disable auto-reporting to reputation system")
                                .addOption(OptionType.BOOLEAN, "enabled", "Enable reputation reporting?", true),
                        new SubcommandData("add-suspicious-domain", "Manually add a suspicious domain")
                                .addOption(OptionType.STRING, "domain", "Domain to add", true)
                                .addOption(OptionType.INTEGER, "risk-score", "Risk score (1-10)", true),
                        new SubcommandData("remove-suspicious-domain", "Remove a domain from suspicious list")
                                .addOption(OptionType.STRING, "domain", "Domain to remove", true)
                )
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

        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("❌ Please specify a subcommand.").setEphemeral(true).queue();
            return;
        }

        String guildId = guild.getId();

        switch (subcommand) {
            case "view" -> handleView(event, guildId);
            case "set-account-age-threshold" -> handleSetAccountAgeThreshold(event, guildId);
            case "set-link-restriction-minutes" -> handleSetLinkRestrictionMinutes(event, guildId);
            case "set-multi-channel-threshold" -> handleSetMultiChannelThreshold(event, guildId);
            case "set-auto-action" -> handleSetAutoAction(event, guildId);
            case "toggle-reputation-reporting" -> handleToggleReputationReporting(event, guildId);
            case "add-suspicious-domain" -> handleAddSuspiciousDomain(event, guildId);
            case "remove-suspicious-domain" -> handleRemoveSuspiciousDomain(event, guildId);
            default -> event.reply("❌ Unknown subcommand.").setEphemeral(true).queue();
        }
    }

    private void handleView(SlashCommandInteractionEvent event, String guildId) {
        BotDetectionConfig config = botDetectionService.getConfig(guildId);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚙️ Bot Detection Configuration");
        embed.setColor(Color.CYAN);

        embed.addField("Status", config.isEnabled() ? "✅ Enabled" : "❌ Disabled", true);
        embed.addField("Account Age Threshold", config.getAccountAgeThresholdDays() + " days", true);
        embed.addField("Link Restriction", config.getLinkRestrictionMinutes() + " minutes", true);
        embed.addField("Multi-Channel Spam Threshold", config.getMultiChannelSpamThreshold() + " channels", true);
        embed.addField("Multi-Channel Time Window", config.getMultiChannelTimeWindowSeconds() + " seconds", true);
        embed.addField("Join + Link Time Window", config.getJoinAndLinkTimeWindowSeconds() + " seconds", true);
        embed.addField("Auto Action", config.getAutoAction().toString(), true);
        embed.addField("Reputation Reporting", config.isReportToReputation() ? "✅ Enabled" : "❌ Disabled", true);

        embed.setFooter("Use subcommands to modify settings");
        embed.setTimestamp(java.time.Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleSetAccountAgeThreshold(SlashCommandInteractionEvent event, String guildId) {
        Long days = event.getOption("days", l -> l.getAsLong());
        if (days == null || days < 1 || days > 365) {
            event.reply("❌ Account age threshold must be between 1 and 365 days.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        BotDetectionConfig config = botDetectionService.getConfig(guildId);
        config.setAccountAgeThresholdDays(days.intValue());
        botDetectionService.updateConfig(guildId, config);

        event.reply(String.format("✅ Account age threshold set to **%d days**", days.intValue())).queue();
        logger.info("Account age threshold set to {} days for guild {}", days, guildId);
    }

    private void handleSetLinkRestrictionMinutes(SlashCommandInteractionEvent event, String guildId) {
        Long minutes = event.getOption("minutes", l -> l.getAsLong());
        if (minutes == null || minutes < 1 || minutes > 1440) {
            event.reply("❌ Link restriction must be between 1 and 1440 minutes (24 hours).")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        BotDetectionConfig config = botDetectionService.getConfig(guildId);
        config.setLinkRestrictionMinutes(minutes.intValue());
        botDetectionService.updateConfig(guildId, config);

        event.reply(String.format("✅ Link restriction set to **%d minutes**", minutes.intValue())).queue();
        logger.info("Link restriction set to {} minutes for guild {}", minutes, guildId);
    }

    private void handleSetMultiChannelThreshold(SlashCommandInteractionEvent event, String guildId) {
        Long threshold = event.getOption("threshold", l -> l.getAsLong());
        if (threshold == null || threshold < 2 || threshold > 10) {
            event.reply("❌ Multi-channel threshold must be between 2 and 10 channels.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        BotDetectionConfig config = botDetectionService.getConfig(guildId);
        config.setMultiChannelSpamThreshold(threshold.intValue());
        botDetectionService.updateConfig(guildId, config);

        event.reply(String.format("✅ Multi-channel spam threshold set to **%d channels**", threshold.intValue())).queue();
        logger.info("Multi-channel threshold set to {} for guild {}", threshold, guildId);
    }

    private void handleSetAutoAction(SlashCommandInteractionEvent event, String guildId) {
        String actionStr = event.getOption("action", s -> s.getAsString());
        if (actionStr == null) {
            event.reply("❌ Please specify an action.").setEphemeral(true).queue();
            return;
        }

        BotDetectionConfig.AutoAction action;
        try {
            action = BotDetectionConfig.AutoAction.valueOf(actionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid action. Must be: NONE, DELETE, WARN, MUTE, or KICK.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        BotDetectionConfig config = botDetectionService.getConfig(guildId);
        config.setAutoAction(action);
        botDetectionService.updateConfig(guildId, config);

        event.reply(String.format("✅ Auto action set to **%s**", action)).queue();
        logger.info("Auto action set to {} for guild {}", action, guildId);
    }

    private void handleToggleReputationReporting(SlashCommandInteractionEvent event, String guildId) {
        Boolean enabled = event.getOption("enabled", b -> b.getAsBoolean());
        if (enabled == null) {
            event.reply("❌ Please specify enabled (true/false).").setEphemeral(true).queue();
            return;
        }

        BotDetectionConfig config = botDetectionService.getConfig(guildId);
        config.setReportToReputation(enabled);
        botDetectionService.updateConfig(guildId, config);

        event.reply(String.format("✅ Reputation reporting **%s**", enabled ? "enabled" : "disabled")).queue();
        logger.info("Reputation reporting {} for guild {}", enabled ? "enabled" : "disabled", guildId);
    }

    private void handleAddSuspiciousDomain(SlashCommandInteractionEvent event, String guildId) {
        String domain = event.getOption("domain", s -> s.getAsString());
        Long riskScore = event.getOption("risk-score", l -> l.getAsLong());

        if (domain == null || domain.isBlank()) {
            event.reply("❌ Please specify a domain.").setEphemeral(true).queue();
            return;
        }

        if (riskScore == null || riskScore < 1 || riskScore > 10) {
            event.reply("❌ Risk score must be between 1 and 10.").setEphemeral(true).queue();
            return;
        }

        botDetectionService.addSuspiciousDomain(domain, riskScore.intValue());

        event.reply(String.format("✅ Added suspicious domain: **%s** (risk: %d)", domain, riskScore.intValue())).queue();
        logger.info("Added suspicious domain {} (risk: {}) for guild {}", domain, riskScore, guildId);
    }

    private void handleRemoveSuspiciousDomain(SlashCommandInteractionEvent event, String guildId) {
        String domain = event.getOption("domain", s -> s.getAsString());

        if (domain == null || domain.isBlank()) {
            event.reply("❌ Please specify a domain.").setEphemeral(true).queue();
            return;
        }

        botDetectionService.removeSuspiciousDomain(domain);

        event.reply(String.format("✅ Removed suspicious domain: **%s**", domain)).queue();
        logger.info("Removed suspicious domain {} for guild {}", domain, guildId);
    }

    /**
     * Creates an OptionData for the auto-action setting with choices.
     *
     * @return the OptionData
     */
    private OptionData createActionOption() {
        OptionData actionOption = new OptionData(OptionType.STRING, "action", "Action: NONE, DELETE, WARN, MUTE, KICK", true);
        actionOption.addChoice("NONE", "NONE");
        actionOption.addChoice("DELETE", "DELETE");
        actionOption.addChoice("WARN", "WARN");
        actionOption.addChoice("MUTE", "MUTE");
        actionOption.addChoice("KICK", "KICK");
        return actionOption;
    }

    @Override
    public String getCommandName() {
        return "admin-bot-detection-config";
    }
}

