package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.models.BehaviorReport;
import com.tatumgames.mikros.services.ReputationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.List;

/**
 * Command handler for the /score command.
 * Displays a user's reputation score.
 */
public class ScoreCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScoreCommand.class);
    private final ReputationService reputationService;
    
    /**
     * Creates a new ScoreCommand handler.
     * 
     * @param reputationService the reputation service
     */
    public ScoreCommand(ReputationService reputationService) {
        this.reputationService = reputationService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("score", "Check a user's reputation score")
                .addOption(OptionType.USER, "user", "The user to check (leave empty for yourself)", false)
                .setGuildOnly(true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Get target user (defaults to command user)
        User targetUser = event.getOption("user") != null
                ? event.getOption("user").getAsUser()
                : event.getUser();
        
        String guildId = event.getGuild().getId();
        
        // Get reputation scores
        int localReputation = reputationService.calculateLocalReputation(targetUser.getId(), guildId);
        int globalReputation = reputationService.getGlobalReputation(targetUser.getId());
        
        // TODO: Integrate with Tatum Games Reputation Score API
        // Currently using placeholder values
        
        // Get behavior reports
        List<BehaviorReport> reports = reputationService.getUserBehaviorReports(targetUser.getId(), guildId);
        
        // Count positive and negative behaviors
        long positiveCount = reports.stream().filter(r -> r.getBehaviorCategory().isPositive()).count();
        long negativeCount = reports.stream().filter(r -> r.getBehaviorCategory().isNegative()).count();
        
        // Build embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎯 Reputation Score");
        embed.setDescription(String.format("Reputation for **%s**", targetUser.getName()));
        embed.setThumbnail(targetUser.getAvatarUrl());
        
        // Determine color based on score
        Color embedColor;
        if (localReputation >= 120) {
            embedColor = Color.GREEN;
        } else if (localReputation >= 80) {
            embedColor = Color.YELLOW;
        } else {
            embedColor = Color.RED;
        }
        embed.setColor(embedColor);
        
        // Local reputation
        String localScoreDisplay = String.format("%d / 100", localReputation);
        String localBar = generateProgressBar(localReputation, 100);
        embed.addField("📊 Local Reputation (This Server)",
                localScoreDisplay + "\n" + localBar,
                false);
        
        // Global reputation (if available)
        if (globalReputation >= 0) {
            String globalBar = generateProgressBar(globalReputation, 100);
            embed.addField("🌍 Global Reputation (MIKROS Network)",
                    String.format("%d / 100\n%s", globalReputation, globalBar),
                    false);
        } else {
            embed.addField("🌍 Global Reputation (MIKROS Network)",
                    "API not yet available\nLocal score is based on behavior in this server only.",
                    false);
        }
        
        // Behavior statistics
        embed.addField("✅ Positive Behaviors", String.valueOf(positiveCount), true);
        embed.addField("⚠️ Negative Behaviors", String.valueOf(negativeCount), true);
        embed.addField("📈 Total Reports", String.valueOf(reports.size()), true);
        
        // Interpretation
        String interpretation;
        if (localReputation >= 120) {
            interpretation = "🌟 **Excellent!** This user is a model community member.";
        } else if (localReputation >= 100) {
            interpretation = "✅ **Good Standing.** No significant issues.";
        } else if (localReputation >= 80) {
            interpretation = "⚠️ **Caution.** Some negative behavior recorded.";
        } else {
            interpretation = "🚫 **Concern.** Multiple infractions or serious violations.";
        }
        
        embed.addField("📝 Interpretation", interpretation, false);
        
        embed.setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.info("Reputation score checked for user {} by {} in guild {}",
                targetUser.getId(), event.getUser().getId(), guildId);
    }
    
    @Override
    public String getCommandName() {
        return "score";
    }
    
    /**
     * Generates a visual progress bar.
     * 
     * @param value the current value
     * @param max the maximum value
     * @return a visual progress bar string
     */
    private String generateProgressBar(int value, int max) {
        int barLength = 10;
        int filled = Math.min(barLength, Math.max(0, (value * barLength) / max));
        int empty = barLength - filled;
        
        StringBuilder bar = new StringBuilder("`[");
        bar.append("█".repeat(filled));
        bar.append("░".repeat(empty));
        bar.append("]`");
        
        return bar.toString();
    }
}

