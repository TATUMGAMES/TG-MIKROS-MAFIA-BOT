package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.models.PromotionVerbosity;
import com.tatumgames.mikros.services.GamePromotionService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /set-promotion-verbosity.
 * Allows administrators to control how often promotions are posted.
 */
public class SetPromotionVerbosityCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetPromotionVerbosityCommand.class);
    private final GamePromotionService gamePromotionService;
    
    /**
     * Creates a new SetPromotionVerbosityCommand handler.
     * 
     * @param gamePromotionService the game promotion service
     */
    public SetPromotionVerbosityCommand(GamePromotionService gamePromotionService) {
        this.gamePromotionService = gamePromotionService;
    }
    
    @Override
    public CommandData getCommandData() {
        OptionData verbosityOption = new OptionData(OptionType.STRING, "level", "Promotion frequency level", true);
        for (PromotionVerbosity verbosity : PromotionVerbosity.values()) {
            verbosityOption.addChoice(
                    verbosity.getLabel() + " (every " + verbosity.getHoursInterval() + "h)",
                    verbosity.name()
            );
        }
        
        return Commands.slash("admin-set-promotion-verbosity", "Set how often game promotions are posted")
                .addOptions(verbosityOption)
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an administrator to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get the verbosity option
        String verbosityName = event.getOption("level").getAsString();
        
        PromotionVerbosity verbosity;
        try {
            verbosity = PromotionVerbosity.valueOf(verbosityName);
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid verbosity level.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Check if promotion channel is set up
        String guildId = event.getGuild().getId();
        if (gamePromotionService.getPromotionChannel(guildId) == null) {
            event.reply("⚠️ **Promotion channel not configured**\n\n" +
                    "Please use `/setup-promotion-channel` first to designate a channel for promotions.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Save the configuration
        gamePromotionService.setPromotionVerbosity(guildId, verbosity);
        
        // Send confirmation
        event.reply(String.format(
                "✅ **Promotion Frequency Updated**\n\n" +
                "**Level:** %s\n" +
                "**Frequency:** Every %d hours\n\n" +
                "The bot will check for new promotions at this interval.",
                verbosity.getLabel(),
                verbosity.getHoursInterval()
        )).queue();
        
        logger.info("Promotion verbosity set to {} for guild {} by user {}",
                verbosity, guildId, member.getId());
    }
    
    @Override
    public String getCommandName() {
        return "admin-set-promotion-verbosity";
    }
}

