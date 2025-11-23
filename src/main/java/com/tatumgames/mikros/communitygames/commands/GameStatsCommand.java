package com.tatumgames.mikros.communitygames.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.communitygames.model.GameConfig;
import com.tatumgames.mikros.communitygames.model.GameResult;
import com.tatumgames.mikros.communitygames.model.GameSession;
import com.tatumgames.mikros.communitygames.model.GameType;
import com.tatumgames.mikros.communitygames.service.CommunityGameService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for /game-stats.
 * Shows the current game status and leaderboard.
 */
public class GameStatsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(GameStatsCommand.class);
    private final CommunityGameService communityGameService;
    
    /**
     * Creates a new GameStatsCommand handler.
     * 
     * @param communityGameService the community game service
     */
    public GameStatsCommand(CommunityGameService communityGameService) {
        this.communityGameService = communityGameService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("game-stats", "View today's game status and leaderboard (unified)")
                .setGuildOnly(true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        
        // Check if games are configured
        GameConfig config = communityGameService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Community games are not set up yet!\n\n" +
                    "An administrator can set them up with `/admin-game-setup`")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get active session
        GameSession session = communityGameService.getActiveSession(guildId);
        if (session == null) {
            event.reply("❌ No active game today. Wait for the next reset!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Build stats embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("%s %s - Game of the Day",
                session.getGameType().getEmoji(),
                session.getGameType().getDisplayName()));
        embed.setColor(Color.CYAN);
        
        // Game status
        String status = session.isActive() ? "🟢 Active" : "🔴 Ended";
        embed.addField("Status", status, true);
        
        // Time remaining until reset
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime nextReset = now.toLocalDate().atTime(config.getResetTime());
        if (now.toLocalTime().isAfter(config.getResetTime())) {
            nextReset = nextReset.plusDays(1);
        }
        
        Duration timeUntilReset = Duration.between(now, nextReset);
        long hoursLeft = timeUntilReset.toHours();
        long minutesLeft = timeUntilReset.toMinutes() % 60;
        
        embed.addField("Reset In", String.format("%dh %dm", hoursLeft, minutesLeft), true);
        
        // Participation stats
        embed.addField("Participants", String.valueOf(session.getResults().size()), true);
        
        // Build leaderboard based on game type
        if (session.getGameType() == GameType.DICE_ROLL) {
            buildDiceLeaderboard(embed, session);
        } else {
            buildStandardLeaderboard(embed, session);
        }
        
        embed.setFooter("Game resets daily at " + config.getResetTime().toString() + " UTC");
        embed.setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.info("Game stats requested in guild {}", guildId);
    }
    
    /**
     * Builds leaderboard for dice game (sorted by score).
     */
    private void buildDiceLeaderboard(EmbedBuilder embed, GameSession session) {
        List<GameResult> results = session.getResults();
        if (results.isEmpty()) {
            embed.addField("🏆 Leaderboard", "No one has rolled yet! Be the first with `/roll`", false);
            return;
        }
        
        // Sort by score descending
        List<GameResult> sorted = results.stream()
                .sorted(Comparator.comparingInt(GameResult::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        StringBuilder leaderboard = new StringBuilder();
        int rank = 1;
        for (GameResult result : sorted) {
            String medal = rank <= 3 ? getMedal(rank - 1) : "   ";
            leaderboard.append(String.format(
                    "%s **#%d** - %s: **%d**%s\n",
                    medal,
                    rank,
                    result.getUsername(),
                    result.getScore(),
                    result.getScore() == 20 ? " 🔥" : ""
            ));
            rank++;
        }
        
        embed.addField("🏆 Leaderboard (Highest Roll)", leaderboard.toString(), false);
    }
    
    /**
     * Builds leaderboard for word/emoji games (winner + attempts).
     */
    private void buildStandardLeaderboard(EmbedBuilder embed, GameSession session) {
        GameResult winner = session.getWinner();
        
        if (winner != null) {
            long timeToWin = winner.getTimestamp().getEpochSecond() - session.getStartTime().getEpochSecond();
            
            embed.addField("🏆 Winner",
                    String.format("**%s**\nSolved in %.1f seconds with %d points!",
                            winner.getUsername(),
                            timeToWin / 1.0,
                            winner.getScore()),
                    false);
        } else if (session.isActive()) {
            embed.addField("🏆 Status",
                    String.format("No winner yet! %d attempts made.\n\nBe the first to solve it!",
                            session.getResults().size()),
                    false);
        } else {
            embed.addField("🏆 Result",
                    String.format("No one solved it! %d attempts were made.",
                            session.getResults().size()),
                    false);
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
        return "game-stats";
    }
}

