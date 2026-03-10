package com.tatumgames.mikros.games.word_unscramble.commands;

import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleConfig;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleProgression;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleResult;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleSession;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleService;
import com.tatumgames.mikros.handler.CommandHandler;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Command handler for /scramble-stats. Shows the current Word Unscramble game status and
 * leaderboard.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ScrambleStatsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScrambleStatsCommand.class);
    private final WordUnscrambleService wordUnscrambleService;

    /**
     * Creates a new ScrambleStatsCommand handler.
     *
     * @param wordUnscrambleService the Word Unscramble service
     */
    public ScrambleStatsCommand(WordUnscrambleService wordUnscrambleService) {
        this.wordUnscrambleService = wordUnscrambleService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(
                        "scramble-stats", "View current Word Unscramble game status and leaderboard")
                .setGuildOnly(true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            event.reply("❌ Unable to get member information.").setEphemeral(true).queue();
            return;
        }

        // Get guild id
        String guildId = guild.getId();

        // Require setup before Word Unscramble commands work
        WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
        if (config == null || config.getGameChannelId() == null) {
            event
                    .reply(
                            "❌ Word Unscramble is not set up for this server. An administrator must run `/admin-scramble-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check role requirement
        if (!AdminUtils.canUserPlay(member, config.isAllowNoRoleUsers())) {
            event
                    .reply(
                            "❌ Users without roles cannot play Word Unscramble games in this server. Contact an administrator.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get active session
        WordUnscrambleSession session = wordUnscrambleService.getActiveSession(guildId);
        if (session == null) {
            event.reply("❌ No active game. Wait for the next hourly reset!").setEphemeral(true).queue();
            return;
        }

        // Build stats embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(
                String.format(
                        "%s %s - Game of the Hour",
                        session.getGameType().getEmoji(), session.getGameType().getDisplayName()));
        embed.setColor(Color.CYAN);

        // Game status
        String status = session.isActive() ? "🟢 Active" : "🔴 Ended";
        embed.addField("Status", status, true);

        // Time remaining until next hourly reset
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime nextReset = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);

        Duration timeUntilReset = Duration.between(now, nextReset);
        long hoursLeft = timeUntilReset.toHours();
        long minutesLeft = timeUntilReset.toMinutes() % 60;

        embed.addField("Reset In", String.format("%dh %dm", hoursLeft, minutesLeft), true);

        // Word Unscramble progression info
        WordUnscrambleProgression progression = wordUnscrambleService.getProgression(guildId);
        if (progression != null) {
            String progressBar = buildProgressBar(progression.getProgressPercentage());
            int wordsRemaining = progression.getWordsRemaining();
            int nextLevel = progression.getLevel() + 1;

            String progressText;
            if (progression.isMaxLevel()) {
                progressText =
                        String.format(
                                "**Level %d**\nXP: %d / %d\n%s\n%.1f%%\n\n**Progress:** Max level reached!",
                                progression.getLevel(),
                                progression.getXp(),
                                progression.getXpRequired(),
                                progressBar,
                                progression.getProgressPercentage());
            } else {
                progressText =
                        String.format(
                                "**Level %d**\nXP: %d / %d\n%s\n%.1f%%\n\n**Progress:** %d more words needed to reach Level %d",
                                progression.getLevel(),
                                progression.getXp(),
                                progression.getXpRequired(),
                                progressBar,
                                progression.getProgressPercentage(),
                                wordsRemaining,
                                nextLevel);
            }

            embed.addField("📊 Progression", progressText, false);
        }

        // Participation stats
        embed.addField("Participants", String.valueOf(session.getResults().size()), true);

        // Build leaderboard
        buildStandardLeaderboard(embed, session);

        embed.setFooter("Game resets hourly");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.info("Word Unscramble stats requested in guild {}", guildId);
    }

    /**
     * Builds leaderboard for word games (winner + attempts).
     */
    private void buildStandardLeaderboard(EmbedBuilder embed, WordUnscrambleSession session) {
        WordUnscrambleResult winner = session.getWinner();

        if (winner != null) {
            long timeToWin =
                    winner.timestamp().getEpochSecond() - session.getStartTime().getEpochSecond();

            // Display score with bonus breakdown if bonus > 0
            String scoreText;
            if (winner.bonus() > 0) {
                int baseScore = winner.score() - winner.bonus();
                scoreText =
                        String.format(
                                "%d points (%d base + %d bonus)", winner.score(), baseScore, winner.bonus());
            } else {
                scoreText = String.format("%d points", winner.score());
            }

            embed.addField(
                    "🏆 Winner",
                    String.format(
                            "**%s**\nSolved in %d seconds with %s!", winner.username(), timeToWin, scoreText),
                    false);
        } else if (session.isActive()) {
            embed.addField(
                    "🏆 Status",
                    String.format(
                            "No winner yet! %d attempts made.\n\nBe the first to solve it!",
                            session.getResults().size()),
                    false);
        } else {
            embed.addField(
                    "🏆 Result",
                    String.format("No one solved it! %d attempts were made.", session.getResults().size()),
                    false);
        }
    }

    /**
     * Builds a progress bar for XP.
     *
     * @param percent the percentage (0-100)
     * @return a visual progress bar
     */
    private String buildProgressBar(double percent) {
        int barLength = 10;
        int filled = (int) Math.round(percent / 100.0 * barLength);
        filled = Math.max(0, Math.min(barLength, filled));

        String filledPart = "█".repeat(filled);
        String emptyPart = "░".repeat(barLength - filled);

        return filledPart + emptyPart;
    }

    @Override
    public String getCommandName() {
        return "scramble-stats";
    }
}
