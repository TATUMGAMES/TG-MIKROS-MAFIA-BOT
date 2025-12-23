package com.tatumgames.mikros.games.word_unscramble.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleConfig;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleType;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleService;
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
import java.time.LocalTime;

/**
 * Command handler for /admin-scramble-config.
 * Allows administrators to modify Word Unscramble game settings.
 */
public class GameConfigCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(GameConfigCommand.class);
    private final WordUnscrambleService wordUnscrambleService;

    /**
     * Creates a new GameConfigCommand handler.
     *
     * @param wordUnscrambleService the Word Unscramble service
     */
    public GameConfigCommand(WordUnscrambleService wordUnscrambleService) {
        this.wordUnscrambleService = wordUnscrambleService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-scramble-config", "Configure Word Unscramble game settings (admin only)")
                .addSubcommands(
                        new SubcommandData("view", "View current game configuration"),
                        new SubcommandData("set-channel", "Change the game channel")
                                .addOption(OptionType.CHANNEL, "channel", "New game channel", true),
                        new SubcommandData("set-reset-time", "Change daily reset time")
                                .addOption(OptionType.INTEGER, "hour", "Reset hour (0-23 UTC)", true),
                        new SubcommandData("enable-game", "Enable a specific game")
                                .addOption(OptionType.STRING, "game", "Game to enable", true),
                        new SubcommandData("disable-game", "Disable a specific game")
                                .addOption(OptionType.STRING, "game", "Game to disable", true)
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
            case "set-channel" -> handleSetChannel(event, guildId);
            case "set-reset-time" -> handleSetResetTime(event, guildId);
            case "enable-game" -> handleEnableGame(event, guildId);
            case "disable-game" -> handleDisableGame(event, guildId);
            default -> event.reply("❌ Unknown subcommand.").setEphemeral(true).queue();
        }
    }

    private void handleView(SlashCommandInteractionEvent event, String guildId) {
        WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-scramble-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎮 Word Unscramble Game Configuration");
        embed.setColor(Color.CYAN);

        embed.addField("Game Channel", "<#" + config.getGameChannelId() + ">", true);
        embed.addField("Reset Time", config.getResetTime().toString() + " UTC", true);
        embed.addField("Enabled Games", String.valueOf(config.getEnabledGames().size()), true);

        StringBuilder games = new StringBuilder();
        for (WordUnscrambleType type : WordUnscrambleType.values()) {
            String status = config.isGameEnabled(type) ? "✅" : "❌";
            games.append(String.format("%s %s %s\n",
                    status,
                    type.getEmoji(),
                    type.getDisplayName()));
        }

        embed.addField("Game Types", games.toString(), false);
        embed.setTimestamp(java.time.Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleSetChannel(SlashCommandInteractionEvent event, String guildId) {
        WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-scramble-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get the channel option
        MessageChannel channel = AdminUtils.getValidTextChannel(event, "channel");
        if (channel == null) return;

        config.setGameChannelId(channel.getId());
        wordUnscrambleService.updateConfig(guildId, config);

        event.reply(String.format("✅ Game channel updated to %s", channel.getAsMention())).queue();
        logger.info("Word Unscramble channel updated to {} for guild {}", channel.getId(), guildId);
    }

    private void handleSetResetTime(SlashCommandInteractionEvent event, String guildId) {
        WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-scramble-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Integer hour = event.getOption("hour", OptionMapping::getAsInt);

        if (hour == null || hour < 0 || hour > 23) {
            event.reply("❌ Hour must be between 0 and 23.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        config.setResetTime(LocalTime.of(hour, 0));
        wordUnscrambleService.updateConfig(guildId, config);

        event.reply(String.format("✅ Reset time updated to %02d:00 UTC", hour)).queue();
        logger.info("Word Unscramble reset time updated to {}:00 for guild {}", hour, guildId);
    }

    private void handleEnableGame(SlashCommandInteractionEvent event, String guildId) {
        WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-scramble-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String gameName = event.getOption("game", OptionMapping::getAsString);

        if (gameName == null) {
            event.reply("❌ You must provide a game name.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        gameName = gameName.toUpperCase().replace(" ", "_");

        try {
            WordUnscrambleType gameType = WordUnscrambleType.valueOf(gameName);
            config.enableGame(gameType);
            wordUnscrambleService.updateConfig(guildId, config);

            event.reply(String.format("✅ Enabled: %s %s",
                    gameType.getEmoji(),
                    gameType.getDisplayName())).queue();
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid game type. Options: WORD_UNSCRAMBLE")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleDisableGame(SlashCommandInteractionEvent event, String guildId) {
        WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-scramble-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String gameName = event.getOption("game", OptionMapping::getAsString);

        if (gameName == null) {
            event.reply("❌ You must provide a game name.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        gameName = gameName.toUpperCase().replace(" ", "_");

        try {
            WordUnscrambleType gameType = WordUnscrambleType.valueOf(gameName);

            if (config.getEnabledGames().size() <= 1) {
                event.reply("❌ Cannot disable the last game! Enable another game first.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            config.disableGame(gameType);
            wordUnscrambleService.updateConfig(guildId, config);

            event.reply(String.format("✅ Disabled: %s %s",
                    gameType.getEmoji(),
                    gameType.getDisplayName())).queue();
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid game type. Options: WORD_UNSCRAMBLE")
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public String getCommandName() {
        return "admin-scramble-config";
    }
}


