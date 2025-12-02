package com.tatumgames.mikros.games.word_unscramble.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleType;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleResetScheduler;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Command handler for /admin-game-setup.
 * Allows administrators to configure Word Unscramble game for their server.
 */
public class GameSetupCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(GameSetupCommand.class);
    private final WordUnscrambleService wordUnscrambleService;

    /**
     * Creates a new GameSetupCommand handler.
     *
     * @param wordUnscrambleService        the Word Unscramble service
     * @param wordUnscrambleResetScheduler the reset scheduler (unused, kept for future use)
     */
    public GameSetupCommand(WordUnscrambleService wordUnscrambleService, WordUnscrambleResetScheduler wordUnscrambleResetScheduler) {
        this.wordUnscrambleService = wordUnscrambleService;
        // wordUnscrambleResetScheduler reserved for future manual reset functionality
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-game-setup", "Configure Word Unscramble game for your server")
                .addOption(OptionType.CHANNEL, "channel", "Channel for hourly games", true)
                .addOption(OptionType.INTEGER, "reset_hour", "Daily reset hour (0-23 UTC, default: 0)", false)
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
        TextChannel channel = AdminUtils.getValidTextChannel(event, "channel");
        if (channel == null) return;

        OptionMapping resetHourOption = event.getOption("reset_hour");
        int resetHour = (resetHourOption != null)
                ? resetHourOption.getAsInt()
                : 0;

        // Validate reset hour
        if (resetHour < 0 || resetHour > 23) {
            event.reply("❌ Reset hour must be between 0 and 23.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Validate bot can post in channel
        if (!channel.canTalk()) {
            event.reply("❌ I don't have permission to send messages in " + channel.getAsMention() + ".")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Setup games with all types enabled by default
        String guildId = guild.getId();
        Set<WordUnscrambleType> allGames = new HashSet<>(Set.of(WordUnscrambleType.values()));
        LocalTime resetTime = LocalTime.of(resetHour, 0);

        wordUnscrambleService.setupGames(guildId, channel.getId(), allGames, resetTime);

        // Start the first game immediately
        wordUnscrambleService.startRandomEnabledGame(guildId);
        String announcement = wordUnscrambleService.getGameAnnouncement(guildId);

        if (announcement != null) {
            channel.sendMessage(announcement).queue();
        }

        // Send confirmation
        event.reply(String.format("""
                        ✅ **Word Unscramble Game Configured!**
                        
                        **Game Channel:** %s
                        **Reset Time:** %02d:00 UTC (hourly)
                        **Enabled Games:** All (%d games)
                        
                        🎮 The first game has been posted!
                        
                        **Next Steps:**
                        • Use `/admin-game-config` to customize settings
                        • Use `/game-stats` to view the leaderboard
                        • Players can join with `/scramble-guess`
                        """,
                channel.getAsMention(),
                resetHour,
                allGames.size()
        )).queue();

        logger.info("Word Unscramble setup for guild {} by user {}: channel={}, resetHour={}",
                guildId, member.getId(), channel.getId(), resetHour);
    }

    @Override
    public String getCommandName() {
        return "admin-game-setup";
    }
}
