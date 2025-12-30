package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
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

/**
 * Command handler for /admin-rpg-config.
 * Allows administrators to configure RPG settings for their server.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGConfigCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGConfigCommand.class);
    private final CharacterService characterService;

    /**
     * Creates a new RPGConfigCommand handler.
     *
     * @param characterService the character service
     */
    public RPGConfigCommand(CharacterService characterService) {
        this.characterService = characterService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-rpg-config", "Configure RPG settings (admin only)")
                .addSubcommands(
                        new SubcommandData("view", "View current RPG configuration"),
                        new SubcommandData("toggle", "Enable or disable RPG system")
                                .addOption(OptionType.BOOLEAN, "enabled", "Enable RPG?", true),
                        new SubcommandData("update-channel", "Update RPG-specific channel")
                                .addOption(OptionType.CHANNEL, "channel", "RPG channel (or leave empty for any)", false),
                        new SubcommandData("set-charge-refresh", "Set charge refresh period in hours")
                                .addOption(OptionType.INTEGER, "hours", "Charge refresh period in hours (1-168)", true),
                        new SubcommandData("set-xp-multiplier", "Set XP gain multiplier")
                                .addOption(OptionType.NUMBER, "multiplier", "XP multiplier (0.1-10.0)", true),
                        new SubcommandData("set-allow-no-role", "Allow or disallow users without roles to play")
                                .addOption(OptionType.BOOLEAN, "enabled", "Allow users without roles?", true)
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
            case "toggle" -> handleToggle(event, guildId);
            case "update-channel" -> handleSetChannel(event, guildId);
            case "set-charge-refresh" -> handleSetChargeRefresh(event, guildId);
            case "set-xp-multiplier" -> handleSetXpMultiplier(event, guildId);
            case "set-allow-no-role" -> handleSetAllowNoRole(event, guildId);
            default -> event.reply("❌ Unknown subcommand.").setEphemeral(true).queue();
        }
    }

    private void handleView(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚙️ RPG Configuration");
        embed.setColor(Color.CYAN);

        embed.addField(
                "Status",
                config.isEnabled() ? "✅ Enabled" : "❌ Disabled",
                true
        );

        embed.addField(
                "RPG Channel",
                config.getRpgChannelId() != null
                        ? "<#" + config.getRpgChannelId() + ">"
                        : "Any channel",
                true
        );

        embed.addField(
                "Charge Refresh Period",
                config.getChargeRefreshHours() + " hours",
                true
        );

        embed.addField(
                "XP Multiplier",
                String.format("%.1fx", config.getXpMultiplier()),
                true
        );

        embed.addField(
                "Allow No-Role Users",
                config.isAllowNoRoleUsers() ? "✅ Enabled" : "❌ Disabled",
                true
        );

        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleToggle(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);
        OptionMapping enabledOption = event.getOption("enabled");
        boolean enabled = (enabledOption != null) && enabledOption.getAsBoolean();

        config.setEnabled(enabled);
        characterService.updateConfig(config);

        event.reply(String.format(
                "✅ RPG system %s",
                enabled ? "**enabled**" : "**disabled**"
        )).queue();

        logger.info("RPG {} for guild {}", enabled ? "enabled" : "disabled", guildId);
    }

    private void handleSetChannel(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);

        // Check if RPG system was set up first (channel must be set)
        if (config.getRpgChannelId() == null) {
            event.reply("❌ RPG system not set up yet. Use `/admin-rpg-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (event.getOption("channel") == null) {
            // Clear channel restriction
            config.setRpgChannelId(null);
            characterService.updateConfig(config);

            event.reply("✅ RPG commands can now be used in any channel").queue();
            return;
        }

        // Get the channel option
        MessageChannel channel = AdminUtils.getValidTextChannel(event, "channel");
        if (channel == null) return;

        config.setRpgChannelId(channel.getId());
        characterService.updateConfig(config);

        event.reply(String.format(
                "✅ RPG channel updated to %s",
                channel.getAsMention()
        )).queue();

        logger.info("RPG channel updated to {} for guild {}", channel.getId(), guildId);
    }

    private void handleSetChargeRefresh(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);
        OptionMapping hoursOption = event.getOption("hours");
        if (hoursOption == null) {
            event.reply("❌ You must provide the number of hours.").setEphemeral(true).queue();
            return;
        }

        int hours = hoursOption.getAsInt();

        if (hours < 1 || hours > 168) {
            event.reply("❌ Charge refresh period must be between 1 and 168 hours (1 week)")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        config.setChargeRefreshHours(hours);
        characterService.updateConfig(config);

        event.reply(String.format(
                "✅ Charge refresh period set to **%d hours**",
                hours
        )).queue();

        logger.info("RPG charge refresh period set to {} hours for guild {}", hours, guildId);
    }

    private void handleSetXpMultiplier(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);
        OptionMapping multiplierOption = event.getOption("multiplier");
        double multiplier = (multiplierOption != null) ? multiplierOption.getAsDouble() : 1.0;

        if (multiplier < 0.1 || multiplier > 10.0) {
            event.reply("❌ XP multiplier must be between 0.1 and 10.0")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        config.setXpMultiplier(multiplier);
        characterService.updateConfig(config);

        event.reply(String.format(
                "✅ XP multiplier set to **%.1fx**",
                multiplier
        )).queue();

        logger.info("RPG XP multiplier set to {}x for guild {}", multiplier, guildId);
    }

    private void handleSetAllowNoRole(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);
        OptionMapping enabledOption = event.getOption("enabled");
        boolean enabled = (enabledOption != null) && enabledOption.getAsBoolean();

        config.setAllowNoRoleUsers(enabled);
        characterService.updateConfig(config);

        event.reply(String.format(
                "✅ Users without roles are now **%s** to play RPG games.",
                enabled ? "allowed" : "not allowed"
        )).queue();

        logger.info("RPG allowNoRoleUsers set to {} for guild {}", enabled, guildId);
    }

    @Override
    public String getCommandName() {
        return "admin-rpg-config";
    }
}

