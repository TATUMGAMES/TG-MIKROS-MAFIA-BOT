package com.tatumgames.mikros.games.word_unscramble.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleConfig;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscramblePlayerStats;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.List;

/**
 * Command handler for /scramble-leaderboard.
 * Shows top Word Unscramble players by total points with pagination.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ScrambleLeaderboardCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScrambleLeaderboardCommand.class);
    private final WordUnscrambleService wordUnscrambleService;
    private static final int ENTRIES_PER_PAGE = 25;

    /**
     * Creates a new ScrambleLeaderboardCommand handler.
     *
     * @param wordUnscrambleService the Word Unscramble service
     */
    public ScrambleLeaderboardCommand(WordUnscrambleService wordUnscrambleService) {
        this.wordUnscrambleService = wordUnscrambleService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("scramble-leaderboard", "View top Word Unscramble players by total points")
                .addOption(OptionType.INTEGER, "page", "Page number (default: 1)", false)
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
            event.reply("❌ Users without roles cannot view Word Unscramble leaderboards in this server. Contact an administrator.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get page number (default: 1)
        OptionMapping pageOption = event.getOption("page");
        int page = (pageOption != null) ? (int) pageOption.getAsLong() : 1;

        if (page < 1) {
            event.reply("❌ Page number must be 1 or greater!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get all player stats for this guild
        List<WordUnscramblePlayerStats> allStats = wordUnscrambleService.getAllPlayerStats(guildId);

        if (allStats.isEmpty()) {
            String message = """
                    ❌ No players have attempted any words yet!
                    
                    Be the first to play and solve a word!
                    """;

            event.reply(message).setEphemeral(true).queue();
            return;
        }

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) allStats.size() / ENTRIES_PER_PAGE);
        if (page > totalPages) {
            event.reply(String.format("❌ Page %d doesn't exist! There are only %d page(s).", page, totalPages))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get stats for this page
        int startIndex = (page - 1) * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, allStats.size());
        List<WordUnscramblePlayerStats> pageStats = allStats.subList(startIndex, endIndex);

        // Build leaderboard embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Word Unscramble Leaderboard - Top Solvers");
        embed.setColor(new Color(100, 149, 237)); // Cornflower Blue
        embed.setDescription("The best word unscramblers in this server");

        StringBuilder leaderboard = new StringBuilder();
        int rank = startIndex + 1;

        for (WordUnscramblePlayerStats stats : pageStats) {
            String medal = getMedal(rank - 1);

            // Try to get username from Discord
            String displayName;
            try {
                User user = event.getJDA().retrieveUserById(stats.getUserId()).complete();
                if (user != null) {
                    displayName = user.getName();
                } else {
                    displayName = "Unknown User";
                }
            } catch (Exception e) {
                // User not found or error retrieving - use ID
                displayName = "User " + stats.getUserId().substring(0, Math.min(8, stats.getUserId().length()));
                logger.debug("Could not retrieve username for user {}: {}", stats.getUserId(), e.getMessage());
            }

            double accuracy = stats.getAccuracyPercentage();

            leaderboard.append(String.format("""
                            %s **#%d** - **%s**
                            └ Points: **%,d** • Words Solved: **%,d** • High Score: **%,d**
                            └ Accuracy: **%.1f%%** • Attempts: **%,d**
                            
                            """,
                    medal,
                    rank,
                    displayName,
                    stats.getTotalPoints(),
                    stats.getTotalWordsSolved(),
                    stats.getHighestScore(),
                    accuracy,
                    stats.getTotalAttempts()
            ));

            rank++;
        }

        embed.addField("Top Players", leaderboard.toString(), false);

        // Pagination footer
        String footerText = buildFooterText(page, totalPages, allStats.size());
        embed.setFooter(footerText);
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.debug("Word Unscramble leaderboard requested for guild {} - showing page {} ({} players)",
                guildId, page, pageStats.size());
    }

    private String buildFooterText(int page, int totalPages, int totalPlayers) {
        if (totalPages > 1) {
            return String.format(
                    "Page %d/%d • Total Players: %d • Use /scramble-leaderboard page:%d for next page",
                    page,
                    totalPages,
                    totalPlayers,
                    page < totalPages ? page + 1 : page
            );
        } else {
            return String.format(
                    "Total Players: %d • Play to improve your rank!",
                    totalPlayers
            );
        }
    }

    /**
     * Gets medal emoji for rank.
     */
    private String getMedal(int rank) {
        return switch (rank) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> "  ";
        };
    }

    @Override
    public String getCommandName() {
        return "scramble-leaderboard";
    }
}

