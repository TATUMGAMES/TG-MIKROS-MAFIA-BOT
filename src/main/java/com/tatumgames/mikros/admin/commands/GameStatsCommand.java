package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.models.ContentStat;
import com.tatumgames.mikros.models.GameplayTypeStat;
import com.tatumgames.mikros.models.GenreStat;
import com.tatumgames.mikros.services.GameStatsService;
import net.dv8tion.jda.api.EmbedBuilder;
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
import java.util.List;

/**
 * Command handler for /gamestats with multiple subcommands.
 * Provides real-time industry metrics powered by MIKROS Analytics.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class GameStatsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(GameStatsCommand.class);
    private final GameStatsService gameStatsService;

    /**
     * Creates a new GameStatsCommand handler.
     *
     * @param gameStatsService the game stats service
     */
    public GameStatsCommand(GameStatsService gameStatsService) {
        this.gameStatsService = gameStatsService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("gamestats", "View MIKROS Analytics game industry statistics")
                .addSubcommands(
                        new SubcommandData("trending-game-genres", "Top 3 fastest-growing game genres"),
                        new SubcommandData("trending-content-genres", "Top 3 fastest-growing content types"),
                        new SubcommandData("trending-content", "Top 5 in-game content seeing spikes"),
                        new SubcommandData("trending-gameplay-types", "Trending gameplay types (casual, competitive)"),
                        new SubcommandData("popular-game-genres", "Most played game genres overall"),
                        new SubcommandData("popular-content-genres", "Most engaging content genres"),
                        new SubcommandData("popular-content", "Top 5 in-game content experiences"),
                        new SubcommandData("popular-gameplay-types", "Most popular gameplay types"),
                        new SubcommandData("total-mikros-apps", "Total apps using MIKROS Analytics"),
                        new SubcommandData("total-mikros-contributors", "Total MIKROS ecosystem contributors"),
                        new SubcommandData("total-users", "Unique user profiles tracked"),
                        new SubcommandData("avg-gameplay-time", "Average gameplay time per app")
                                .addOption(OptionType.STRING, "genre", "Filter by genre (optional)", false),
                        new SubcommandData("avg-session-time", "Average session length")
                                .addOption(OptionType.STRING, "genre", "Filter by genre (optional)", false)
                );
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("❌ Please specify a subcommand.").setEphemeral(true).queue();
            return;
        }

        try {
            switch (subcommand) {
                case "trending-game-genres" -> handleTrendingGameGenres(event);
                case "trending-content-genres" -> handleTrendingContentGenres(event);
                case "trending-content" -> handleTrendingContent(event);
                case "trending-gameplay-types" -> handleTrendingGameplayTypes(event);
                case "popular-game-genres" -> handlePopularGameGenres(event);
                case "popular-content-genres" -> handlePopularContentGenres(event);
                case "popular-content" -> handlePopularContent(event);
                case "popular-gameplay-types" -> handlePopularGameplayTypes(event);
                case "total-mikros-apps" -> handleTotalMikrosApps(event);
                case "total-mikros-contributors" -> handleTotalMikrosContributors(event);
                case "total-users" -> handleTotalUsers(event);
                case "avg-gameplay-time" -> handleAvgGameplayTime(event);
                case "avg-session-time" -> handleAvgSessionTime(event);
                default -> event.reply("❌ Unknown subcommand.").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            logger.error("Error handling gamestats subcommand: {}", subcommand, e);
            event.reply("❌ An error occurred while fetching statistics.").setEphemeral(true).queue();
        }
    }

    /**
     * Handles trending-game-genres subcommand.
     */
    private void handleTrendingGameGenres(SlashCommandInteractionEvent event) {
        List<GenreStat> genres = gameStatsService.getTrendingGameGenres(3);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔥 Top Trending Game Genres");
        embed.setDescription("Fastest-growing genres based on 30-day player engagement");
        embed.setColor(new Color(255, 69, 0)); // Orange-red for trending

        StringBuilder content = new StringBuilder();
        for (GenreStat genre : genres) {
            content.append(String.format(
                    "**%d. %s** — +%.1f%%\n",
                    genre.rank(),
                    genre.genreName(),
                    genre.growthPercentage()
            ));
        }

        embed.addField("📈 Growth Rankings", content.toString(), false);
        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Trending game genres requested by {} in guild {}",
                event.getUser().getId(), event.getGuild() != null ? event.getGuild().getId() : "DM");
    }

    /**
     * Handles trending-content-genres subcommand.
     */
    private void handleTrendingContentGenres(SlashCommandInteractionEvent event) {
        List<GenreStat> genres = gameStatsService.getTrendingContentGenres(3);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔥 Top Trending Content Genres");
        embed.setDescription("Fastest-growing content types (action, story, co-op)");
        embed.setColor(new Color(255, 69, 0));

        StringBuilder content = new StringBuilder();
        for (GenreStat genre : genres) {
            content.append(String.format(
                    "**%d. %s** — +%.1f%%\n",
                    genre.rank(),
                    genre.genreName(),
                    genre.growthPercentage()
            ));
        }

        embed.addField("📈 Growth Rankings", content.toString(), false);
        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Trending content genres requested");
    }

    /**
     * Handles trending-content subcommand.
     */
    private void handleTrendingContent(SlashCommandInteractionEvent event) {
        List<ContentStat> content = gameStatsService.getTrendingContent(5);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔥 Top Trending In-Game Content");
        embed.setDescription("Specific content experiencing engagement spikes");
        embed.setColor(new Color(255, 69, 0));

        StringBuilder contentText = new StringBuilder();
        for (ContentStat stat : content) {
            contentText.append(String.format("""
                            **%d. %s** (%s) — +%.1f%%
                                   %,d plays
                            
                            """,
                    stat.rank(),
                    stat.contentName(),
                    stat.contentType(),
                    stat.growthPercentage(),
                    stat.usageCount()
            ));
        }

        embed.addField("📈 Hot Content", contentText.toString(), false);
        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Trending content requested");
    }

    /**
     * Handles trending-gameplay-types subcommand.
     */
    private void handleTrendingGameplayTypes(SlashCommandInteractionEvent event) {
        List<GameplayTypeStat> types = gameStatsService.getTrendingGameplayTypes(3);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔥 Top Trending Gameplay Types");
        embed.setDescription("Casual, competitive, and hyper-casual trends");
        embed.setColor(new Color(255, 69, 0));

        StringBuilder content = new StringBuilder();
        for (GameplayTypeStat type : types) {
            content.append(String.format("""
                            **%d. %s** — +%.1f%%
                                   %,d players | %.1f%% market share
                            
                            """,
                    type.rank(),
                    type.gameplayType(),
                    type.growthPercentage(),
                    type.playerCount(),
                    type.marketShare()
            ));
        }

        embed.addField("📈 Growth Rankings", content.toString(), false);
        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Trending gameplay types requested");
    }

    /**
     * Handles popular-game-genres subcommand.
     */
    private void handlePopularGameGenres(SlashCommandInteractionEvent event) {
        List<GenreStat> genres = gameStatsService.getPopularGameGenres(3);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⭐ Most Popular Game Genres");
        embed.setDescription("Top genres by total player count");
        embed.setColor(new Color(255, 215, 0)); // Gold

        StringBuilder content = new StringBuilder();
        for (GenreStat genre : genres) {
            content.append(String.format("""
                            **%d. %s**
                                   %,d players | +%.1f%% growth
                            
                            """,
                    genre.rank(),
                    genre.genreName(),
                    genre.playerCount(),
                    genre.growthPercentage()
            ));
        }

        embed.addField("🏆 Top Genres", content.toString(), false);
        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Popular game genres requested");
    }

    /**
     * Handles popular-content-genres subcommand.
     */
    private void handlePopularContentGenres(SlashCommandInteractionEvent event) {
        List<GenreStat> genres = gameStatsService.getPopularContentGenres(3);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⭐ Most Popular Content Genres");
        embed.setDescription("Top content types by engagement");
        embed.setColor(new Color(255, 215, 0));

        StringBuilder content = new StringBuilder();
        for (GenreStat genre : genres) {
            content.append(String.format("""
                            **%d. %s**
                                   %,d players | +%.1f%% growth
                            
                            """,
                    genre.rank(),
                    genre.genreName(),
                    genre.playerCount(),
                    genre.growthPercentage()
            ));
        }

        embed.addField("🏆 Top Content Types", content.toString(), false);
        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Popular content genres requested");
    }

    /**
     * Handles popular-content subcommand.
     */
    private void handlePopularContent(SlashCommandInteractionEvent event) {
        List<ContentStat> content = gameStatsService.getPopularContent(5);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⭐ Most Popular In-Game Content");
        embed.setDescription("Top content by total engagement");
        embed.setColor(new Color(255, 215, 0));

        StringBuilder contentText = new StringBuilder();
        for (ContentStat stat : content) {
            contentText.append(String.format("""
                            **%d. %s** (%s)
                                   %,d plays | +%.1f%% growth
                            
                            """,
                    stat.rank(),
                    stat.contentName(),
                    stat.contentType(),
                    stat.usageCount(),
                    stat.growthPercentage()
            ));
        }

        embed.addField("🏆 Top Content", contentText.toString(), false);
        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Popular content requested");
    }

    /**
     * Handles popular-gameplay-types subcommand.
     */
    private void handlePopularGameplayTypes(SlashCommandInteractionEvent event) {
        List<GameplayTypeStat> types = gameStatsService.getPopularGameplayTypes(3);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⭐ Most Popular Gameplay Types");
        embed.setDescription("Top gameplay styles by player preference");
        embed.setColor(new Color(255, 215, 0));

        StringBuilder content = new StringBuilder();
        for (GameplayTypeStat type : types) {
            content.append(String.format("""
                            **%d. %s**
                                   %,d players | %.1f%% market share | +%.1f%% growth
                            
                            """,
                    type.rank(),
                    type.gameplayType(),
                    type.playerCount(),
                    type.marketShare(),
                    type.growthPercentage()
            ));
        }

        embed.addField("🏆 Top Types", content.toString(), false);
        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Popular gameplay types requested");
    }

    /**
     * Handles total-mikros-apps subcommand.
     */
    private void handleTotalMikrosApps(SlashCommandInteractionEvent event) {
        long totalApps = gameStatsService.getTotalMikrosApps();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📱 Total MIKROS Apps");
        embed.setDescription(String.format(
                "**%,d** games and apps are currently using MIKROS Analytics",
                totalApps
        ));
        embed.setColor(Color.BLUE);

        embed.addField("🎮 Platform Breakdown",
                "This includes indie games, mobile apps, and web games integrated with the MIKROS SDK.",
                false);

        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Total MIKROS apps requested");
    }

    /**
     * Handles total-mikros-contributors subcommand.
     */
    private void handleTotalMikrosContributors(SlashCommandInteractionEvent event) {
        long totalContributors = gameStatsService.getTotalMikrosContributors();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("👥 Total MIKROS Contributors");
        embed.setDescription(String.format(
                "**%,d** developers, testers, and players in the MIKROS ecosystem",
                totalContributors
        ));
        embed.setColor(Color.BLUE);

        embed.addField("📊 Contributor Types",
                "• Game Developers\n• QA Testers\n• Beta Players\n• Community Members\n• Content Creators",
                false);

        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Total MIKROS contributors requested");
    }

    /**
     * Handles total-users subcommand.
     */
    private void handleTotalUsers(SlashCommandInteractionEvent event) {
        long totalUsers = gameStatsService.getTotalUsers();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🌍 Total Users Tracked");
        embed.setDescription(String.format(
                "**%,d** unique user profiles tracked across MIKROS-enabled games",
                totalUsers
        ));
        embed.setColor(Color.BLUE);

        embed.addField("📊 Network Scale",
                "MIKROS Analytics provides unified player tracking across multiple games, " +
                        "enabling cross-game analytics and community insights.",
                false);

        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Total users requested");
    }

    /**
     * Handles avg-gameplay-time subcommand.
     */
    private void handleAvgGameplayTime(SlashCommandInteractionEvent event) {
        OptionMapping genreOption = event.getOption("genre");
        String genre = genreOption != null ? genreOption.getAsString() : null;
        double avgTime = gameStatsService.getAverageGameplayTime(genre);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⏱️ Average Gameplay Time");

        if (genre != null && !genre.isBlank()) {
            embed.setDescription(String.format(
                    "Average playtime for **%s** games: **%.1f hours**",
                    genre,
                    avgTime
            ));
        } else {
            embed.setDescription(String.format(
                    "Average playtime across all games: **%.1f hours**",
                    avgTime
            ));
        }

        embed.setColor(Color.GREEN);

        embed.addField("📊 What This Means",
                "This metric helps developers understand typical player engagement duration " +
                        "and benchmark their games against industry averages.",
                false);

        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Average gameplay time requested for genre: {}", genre);
    }

    /**
     * Handles avg-session-time subcommand.
     */
    private void handleAvgSessionTime(SlashCommandInteractionEvent event) {
        OptionMapping genreOption = event.getOption("genre");
        String genre = genreOption != null ? genreOption.getAsString() : null;
        double avgTime = gameStatsService.getAverageSessionTime(genre);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⏱️ Average Session Time");

        if (genre != null && !genre.isBlank()) {
            embed.setDescription(String.format(
                    "Average session length for **%s** games: **%.1f minutes**",
                    genre,
                    avgTime
            ));
        } else {
            embed.setDescription(String.format(
                    "Average session length across all games: **%.1f minutes**",
                    avgTime
            ));
        }

        embed.setColor(Color.GREEN);

        embed.addField("📊 What This Means",
                "Session time indicates how long players typically play in a single sitting. " +
                        "Shorter sessions may indicate mobile/casual games, while longer sessions suggest immersive experiences.",
                false);

        embed.setFooter("Data provided by MIKROS Analytics");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        logger.info("Average session time requested for genre: {}", genre);
    }

    @Override
    public String getCommandName() {
        return "gamestats";
    }
}

