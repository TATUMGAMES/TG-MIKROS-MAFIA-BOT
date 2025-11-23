package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.models.BehaviorCategory;
import com.tatumgames.mikros.models.BehaviorReport;
import com.tatumgames.mikros.services.ReputationService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Command handler for the /report command.
 * Allows users to report negative behavior.
 */
public class ReportCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ReportCommand.class);
    private final ReputationService reputationService;
    
    /**
     * Creates a new ReportCommand handler.
     * 
     * @param reputationService the reputation service
     */
    public ReportCommand(ReputationService reputationService) {
        this.reputationService = reputationService;
    }
    
    @Override
    public CommandData getCommandData() {
        OptionData behaviorOption = new OptionData(OptionType.STRING, "behavior", "Type of negative behavior", true);
        for (BehaviorCategory category : BehaviorCategory.getNegativeBehaviors()) {
            behaviorOption.addChoice(category.getLabel(), category.name());
        }
        
        return Commands.slash("report", "Report a user for negative behavior")
                .addOption(OptionType.USER, "user", "The user to report", true)
                .addOptions(behaviorOption)
                .addOption(OptionType.STRING, "notes", "Additional notes (optional)", false)
                .setGuildOnly(true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member reporter = event.getMember();
        if (reporter == null) {
            return;
        }
        
        // Get command options
        User targetUser = event.getOption("user").getAsUser();
        String behaviorName = event.getOption("behavior").getAsString();
        String notes = event.getOption("notes") != null
                ? event.getOption("notes").getAsString()
                : "";
        
        // Validate target
        if (targetUser.isBot()) {
            event.reply("❌ You cannot report a bot.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (targetUser.getId().equals(reporter.getId())) {
            event.reply("❌ You cannot report yourself.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Parse behavior category
        BehaviorCategory behaviorCategory;
        try {
            behaviorCategory = BehaviorCategory.valueOf(behaviorName);
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid behavior category.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Create and record behavior report
        BehaviorReport report = new BehaviorReport(
                targetUser.getId(),
                targetUser.getName(),
                reporter.getId(),
                reporter.getEffectiveName(),
                behaviorCategory,
                notes,
                Instant.now(),
                event.getGuild().getId()
        );
        
        reputationService.recordBehavior(report);
        
        // TODO: Call Tatum Games Reputation Score Update API
        // reputationService.reportToExternalAPI(report);
        
        // Calculate new local reputation
        int newReputation = reputationService.calculateLocalReputation(
                targetUser.getId(), event.getGuild().getId());
        
        // Send confirmation (ephemeral for privacy)
        event.reply(String.format(
                "🚨 **Report Submitted**\n" +
                "User: %s\n" +
                "Behavior: %s (%d points)\n" +
                "%s" +
                "Reporter: %s\n" +
                "Local Reputation: %d\n\n" +
                "⚠️ This report has been recorded. Moderators will review if needed.",
                targetUser.getAsMention(),
                behaviorCategory.getLabel(),
                behaviorCategory.getWeight(),
                notes.isEmpty() ? "" : "Notes: " + notes + "\n",
                reporter.getAsMention(),
                newReputation
        )).setEphemeral(true).queue();
        
        logger.info("User {} reported by {} in guild {}: {}",
                targetUser.getId(), reporter.getId(), event.getGuild().getId(), behaviorCategory);
    }
    
    @Override
    public String getCommandName() {
        return "report";
    }
}

