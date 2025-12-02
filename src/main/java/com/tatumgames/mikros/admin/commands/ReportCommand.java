package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.models.BehaviorCategory;
import com.tatumgames.mikros.models.BehaviorReport;
import com.tatumgames.mikros.services.ReputationService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Command handler for the /report command.
 * Allows admins to report negative behavior.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
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

        return Commands.slash("report", "Report a user for negative behavior (Admin only)")
                .addOption(OptionType.USER, "user", "The user to report", true)
                .addOptions(behaviorOption)
                .addOption(OptionType.STRING, "notes", "Additional notes (optional)", false)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get command options
        User targetUser = event.getOption("user", OptionMapping::getAsUser);
        String behaviorName = event.getOption("behavior", OptionMapping::getAsString);
        String notes = event.getOption("notes") != null
                ? event.getOption("notes", OptionMapping::getAsString)
                : "";

        if (AdminUtils.isInvalidTargetUser(member, targetUser, event)) {
            return; // stop executing the command
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

        if (targetUser == null) {
            event.reply("You must specify a user.").setEphemeral(true).queue();
            return;
        }

        // Create and record behavior report
        BehaviorReport report = new BehaviorReport(
                targetUser.getId(),
                targetUser.getName(),
                member.getId(),
                member.getEffectiveName(),
                behaviorCategory,
                notes,
                Instant.now(),
                guild.getId()
        );

        reputationService.recordBehavior(report);

        // Call API to track player rating
        boolean apiSuccess = reputationService.reportToExternalAPI(report);

        // Send confirmation
        assert notes != null;
        String message = String.format("""
                        🚨 **Report Submitted**
                        User: %s
                        Behavior: %s (+%d points)
                        %s
                        Reporter: %s
                        
                        """,
                targetUser.getName(),
                behaviorCategory.getLabel(),
                behaviorCategory.getWeight(),
                notes.isEmpty() ? "" : "Notes: " + notes + "\n",
                member.getAsMention()
        );

        if (apiSuccess) {
            message += "\n✅ Report has been recorded and sent to the reputation system.";
        } else {
            message += "\n⚠️ Report recorded locally, but API call failed.";
        }
        event.reply(message).setEphemeral(true).queue();

        logger.info("User {} reported by {} in guild {}: {}",
                targetUser.getId(), member.getId(), event.getGuild().getId(), behaviorCategory);
    }

    @Override
    public String getCommandName() {
        return "report";
    }
}
