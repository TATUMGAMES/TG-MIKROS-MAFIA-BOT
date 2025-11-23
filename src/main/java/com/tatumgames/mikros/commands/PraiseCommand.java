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
 * Command handler for the /praise command.
 * Allows users to report positive behavior.
 */
public class PraiseCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(PraiseCommand.class);
    private final ReputationService reputationService;
    
    /**
     * Creates a new PraiseCommand handler.
     * 
     * @param reputationService the reputation service
     */
    public PraiseCommand(ReputationService reputationService) {
        this.reputationService = reputationService;
    }
    
    @Override
    public CommandData getCommandData() {
        OptionData behaviorOption = new OptionData(OptionType.STRING, "behavior", "Type of positive behavior", true);
        for (BehaviorCategory category : BehaviorCategory.getPositiveBehaviors()) {
            behaviorOption.addChoice(category.getLabel(), category.name());
        }
        
        return Commands.slash("praise", "Praise a user for positive behavior")
                .addOption(OptionType.USER, "user", "The user to praise", true)
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
            event.reply("❌ You cannot praise a bot.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (targetUser.getId().equals(reporter.getId())) {
            event.reply("❌ You cannot praise yourself.")
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
        
        // Send confirmation
        event.reply(String.format(
                "✨ **Praise Recorded**\n" +
                "User: %s\n" +
                "Behavior: %s (+%d points)\n" +
                "%s" +
                "Reporter: %s\n" +
                "Local Reputation: %d",
                targetUser.getAsMention(),
                behaviorCategory.getLabel(),
                behaviorCategory.getWeight(),
                notes.isEmpty() ? "" : "Notes: " + notes + "\n",
                reporter.getAsMention(),
                newReputation
        )).queue();
        
        logger.info("User {} praised by {} in guild {}: {}",
                targetUser.getId(), reporter.getId(), event.getGuild().getId(), behaviorCategory);
    }
    
    @Override
    public String getCommandName() {
        return "praise";
    }
}

