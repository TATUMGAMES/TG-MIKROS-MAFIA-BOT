package com.tatumgames.mikros.games.word_unscramble.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleConfig;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscramblePlayerStats;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

/**
 * Command handler for /scramble-profile.
 * Displays individual player statistics for Word Unscramble game.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ScrambleProfileCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScrambleProfileCommand.class);
    private final WordUnscrambleService wordUnscrambleService;

    /**
     * Creates a new ScrambleProfileCommand handler.
     *
     * @param wordUnscrambleService the Word Unscramble service
     */
    public ScrambleProfileCommand(WordUnscrambleService wordUnscrambleService) {
        this.wordUnscrambleService = wordUnscrambleService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("scramble-profile", "View your Word Unscramble statistics")
                .setGuildOnly(true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("❌ This command can only be used in a server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            event.reply("❌ Unable to get member information.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String guildId = guild.getId();
        String userId = event.getUser().getId();

        // Check if games are configured
        WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
        if (config == null) {
            event.reply("""
                            ❌ Word Unscramble game is not set up yet!
                            
                            An administrator can set it up with `/admin-scramble-setup`
                            """)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check role requirement
        if (!AdminUtils.canUserPlay(member, config.isAllowNoRoleUsers())) {
            event.reply("❌ Users without roles cannot play Word Unscramble games in this server. Contact an administrator.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get player stats (or create empty stats if none exist)
        WordUnscramblePlayerStats stats = wordUnscrambleService.getOrCreatePlayerStats(guildId, userId);

        // Build profile embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("📊 %s's Word Unscramble Profile", member.getEffectiveName()));
        embed.setColor(new Color(100, 149, 237)); // Cornflower Blue

        // Basic stats
        embed.addField("✅ Words Solved", String.format("%,d", stats.getTotalWordsSolved()), true);
        embed.addField("🎯 Total Points", String.format("%,d", stats.getTotalPoints()), true);
        embed.addField("⭐ Highest Score", String.format("%,d", stats.getHighestScore()), true);

        // Time stats
        String fastestTimeText;
        if (stats.getFastestTimeSeconds() == 0) {
            fastestTimeText = "No solves yet";
        } else {
            Duration fastestDuration = Duration.ofSeconds(stats.getFastestTimeSeconds());
            long minutes = fastestDuration.toMinutes();
            long seconds = fastestDuration.toSecondsPart();
            if (minutes > 0) {
                fastestTimeText = String.format("%d min %d sec", minutes, seconds);
            } else {
                fastestTimeText = String.format("%d sec", seconds);
            }
        }
        embed.addField("⚡ Fastest Time", fastestTimeText, true);

        // Attempt stats
        embed.addField("📝 Total Attempts", String.format("%,d", stats.getTotalAttempts()), true);
        embed.addField("❌ Wrong Guesses", String.format("%,d", stats.getWrongGuesses()), true);

        // Calculated stats
        double accuracy = stats.getAccuracyPercentage();
        double avgScore = stats.getAverageScore();
        embed.addField("🎯 Accuracy", String.format("%.1f%%", accuracy), true);
        embed.addField("📊 Average Score", String.format("%.1f", avgScore), true);

        embed.setFooter("Keep playing to improve your stats!");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.info("Word Unscramble profile requested for user {} in guild {}", userId, guildId);
    }

    @Override
    public String getCommandName() {
        return "scramble-profile";
    }
}

