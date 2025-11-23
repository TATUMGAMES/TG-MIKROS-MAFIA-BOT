package com.tatumgames.mikros.spelling.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.spelling.model.ChallengeSession;
import com.tatumgames.mikros.spelling.service.SpellingChallengeService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;

/**
 * Command handler for /spelling-challenge.
 * Posts or shows the current daily spelling challenge.
 */
public class SpellingChallengeCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SpellingChallengeCommand.class);
    private final SpellingChallengeService spellingService;
    
    /**
     * Creates a new SpellingChallengeCommand handler.
     * 
     * @param spellingService the spelling challenge service
     */
    public SpellingChallengeCommand(SpellingChallengeService spellingService) {
        this.spellingService = spellingService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("spell-challenge", "Show today's daily spelling challenge")
                .setGuildOnly(true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        
        // Get or create active session
        ChallengeSession session = spellingService.getActiveSession(guildId);
        
        if (session == null || !session.isActive()) {
            // Start new challenge
            session = spellingService.startNewChallenge(guildId);
        }
        
        // Build challenge embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🧠 DAILY SPELLING CHALLENGE");
        embed.setColor(Color.BLUE);
        
        embed.setDescription(String.format(
                "**Unscramble this word:**\n\n" +
                "```\n%s\n```\n" +
                "**%d letters**\n\n" +
                "Use `/guess <word>` to try!\n" +
                "You have **3 attempts** per day.",
                session.getScrambledWord(),
                session.getCorrectWord().length()
        ));
        
        // Show challenge stats
        int totalAttempts = session.getAttempts().size();
        int solvers = session.getAllSolvers().size();
        
        embed.addField(
                "📊 Today's Stats",
                String.format(
                        "**%d** attempts made\n" +
                        "**%d** solved it",
                        totalAttempts,
                        solvers
                ),
                true
        );
        
        // Show points info
        embed.addField(
                "🏆 Scoring",
                "First solver: **3 pts**\n" +
                "Other solvers: **1 pt**",
                true
        );
        
        // Show time since start
        Duration duration = Duration.between(session.getStartTime(), Instant.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        
        embed.addField(
                "⏱️ Time",
                String.format("Challenge started %dh %dm ago", hours, minutes),
                true
        );
        
        embed.setFooter("Tip: Use /spelling-leaderboard to see top players");
        embed.setTimestamp(Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.debug("Spelling challenge displayed for guild {}", guildId);
    }
    
    @Override
    public String getCommandName() {
        return "spell-challenge";
    }
}




