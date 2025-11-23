package com.tatumgames.mikros.spelling.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.spelling.model.SpellingLeaderboard;
import com.tatumgames.mikros.spelling.service.SpellingChallengeService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.List;

/**
 * Command handler for /spelling-leaderboard.
 * Shows top players by points in the spelling challenge.
 */
public class SpellingLeaderboardCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SpellingLeaderboardCommand.class);
    private final SpellingChallengeService spellingService;
    
    /**
     * Creates a new SpellingLeaderboardCommand handler.
     * 
     * @param spellingService the spelling challenge service
     */
    public SpellingLeaderboardCommand(SpellingChallengeService spellingService) {
        this.spellingService = spellingService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("spell-leaderboard", "View top spelling challenge players");
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        SpellingLeaderboard leaderboard = spellingService.getLeaderboard();
        List<SpellingLeaderboard.PlayerScore> topPlayers = leaderboard.getTopPlayers(10);
        
        if (topPlayers.isEmpty()) {
            event.reply("📊 No one has solved a spelling challenge yet!\n\n" +
                    "Be the first to solve today's challenge with `/spell-challenge`!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Build leaderboard embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Spelling Challenge - All-Time Leaderboard");
        embed.setColor(new Color(255, 215, 0)); // Gold
        embed.setDescription("Top spellers across all challenges");
        
        StringBuilder board = new StringBuilder();
        int rank = 1;
        
        for (SpellingLeaderboard.PlayerScore score : topPlayers) {
            String medal = getMedal(rank - 1);
            
            board.append(String.format(
                    "%s **#%d** - **%s**\n" +
                    "└ %d pts • %d solves • %d first 🏆\n\n",
                    medal,
                    rank,
                    score.getUsername(),
                    score.getTotalPoints(),
                    score.getTotalSolves(),
                    score.getFirstSolves()
            ));
            
            rank++;
        }
        
        embed.addField("Top Players", board.toString(), false);
        
        embed.addField(
                "Scoring",
                "• First to solve: **3 points**\n" +
                "• Other solvers: **1 point**",
                true
        );
        
        embed.addField(
                "Stats",
                String.format("Total Players: **%d**", leaderboard.getPlayerCount()),
                true
        );
        
        embed.setFooter("Play today's challenge with /spell-challenge");
        embed.setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.debug("Spelling leaderboard displayed - {} players shown", topPlayers.size());
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
        return "spell-leaderboard";
    }
}




