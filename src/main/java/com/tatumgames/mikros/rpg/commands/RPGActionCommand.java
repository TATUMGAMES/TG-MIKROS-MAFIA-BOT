package com.tatumgames.mikros.rpg.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.rpg.actions.CharacterAction;
import com.tatumgames.mikros.rpg.config.RPGConfig;
import com.tatumgames.mikros.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.rpg.model.RPGCharacter;
import com.tatumgames.mikros.rpg.service.ActionService;
import com.tatumgames.mikros.rpg.service.CharacterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;

/**
 * Command handler for /rpg-action.
 * Allows players to perform daily actions (explore, train, battle).
 */
public class RPGActionCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGActionCommand.class);
    private final CharacterService characterService;
    private final ActionService actionService;
    
    /**
     * Creates a new RPGActionCommand handler.
     * 
     * @param characterService the character service
     * @param actionService the action service
     */
    public RPGActionCommand(CharacterService characterService, ActionService actionService) {
        this.characterService = characterService;
        this.actionService = actionService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-action", "Perform a daily action with your character")
                .addOption(OptionType.STRING, "type", "Action type (explore, train, battle)", true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String guildId = event.getGuild().getId();
        
        // Check if user has a character
        RPGCharacter character = characterService.getCharacter(userId);
        if (character == null) {
            event.reply("❌ You don't have a character yet! Use `/rpg-register` to create one.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get guild config
        RPGConfig config = characterService.getConfig(guildId);
        
        // Check if RPG is enabled
        if (!config.isEnabled()) {
            event.reply("❌ The RPG system is currently disabled in this server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Check if in correct channel (if specified)
        if (config.getRpgChannelId() != null) {
            if (!event.getChannel().getId().equals(config.getRpgChannelId())) {
                event.reply(String.format(
                        "❌ RPG commands must be used in <#%s>",
                        config.getRpgChannelId()
                )).setEphemeral(true).queue();
                return;
            }
        }
        
        // Check cooldown
        if (!character.canPerformAction(config.getCooldownHours())) {
            long secondsRemaining = character.getSecondsUntilNextAction(config.getCooldownHours());
            Duration duration = Duration.ofSeconds(secondsRemaining);
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            
            event.reply(String.format(
                    "⏳ Your character is resting!\n\n" +
                    "Next action available in: **%dh %dm**\n\n" +
                    "Use this time to check `/rpg-profile` or `/rpg-leaderboard`",
                    hours, minutes
            )).setEphemeral(true).queue();
            return;
        }
        
        // Get action type
        String actionType = event.getOption("type").getAsString().toLowerCase();
        
        // Validate action
        if (!actionService.hasAction(actionType)) {
            event.reply("❌ Invalid action! Choose from: **explore**, **train**, or **battle**")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Execute action
        try {
            RPGActionOutcome outcome = actionService.executeAction(actionType, character, config);
            CharacterAction action = actionService.getAction(actionType);
            
            // Build result embed
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(String.format(
                    "%s %s - Action Complete!",
                    action.getActionEmoji(),
                    capitalize(actionType)
            ));
            
            embed.setColor(outcome.isSuccess() ? Color.GREEN : Color.ORANGE);
            
            // Narrative
            embed.setDescription(outcome.getNarrative());
            
            // Results
            StringBuilder results = new StringBuilder();
            results.append(String.format("✨ **+%d XP**", outcome.getXpGained()));
            
            if (outcome.getStatIncreased() != null) {
                results.append(String.format("\n💪 **+%d %s**",
                        outcome.getStatAmount(),
                        outcome.getStatIncreased()));
            }
            
            if (outcome.getDamageTaken() > 0) {
                results.append(String.format("\n💔 **-%d HP**", outcome.getDamageTaken()));
            }
            
            if (outcome.isLeveledUp()) {
                results.append(String.format("\n\n🎉 **LEVEL UP!** You are now Level %d!",
                        character.getLevel()));
            }
            
            embed.addField("📊 Results", results.toString(), false);
            
            // Current stats
            embed.addField(
                    "Character Status",
                    String.format(
                            "**Level %d** • %d/%d XP\n" +
                            "❤️ HP: %d/%d",
                            character.getLevel(),
                            character.getXp(),
                            character.getXpToNextLevel(),
                            character.getStats().getCurrentHp(),
                            character.getStats().getMaxHp()
                    ),
                    false
            );
            
            embed.setFooter(String.format(
                    "Next action available in %d hours",
                    config.getCooldownHours()
            ));
            embed.setTimestamp(java.time.Instant.now());
            
            event.replyEmbeds(embed.build()).queue();
            
            logger.info("User {} performed action {} with character {} - XP: +{}, Level: {}",
                    userId, actionType, character.getName(), outcome.getXpGained(), character.getLevel());
            
        } catch (Exception e) {
            logger.error("Error executing action {} for user {}", actionType, userId, e);
            event.reply("❌ An error occurred while performing the action. Please try again.")
                    .setEphemeral(true)
                    .queue();
        }
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    @Override
    public String getCommandName() {
        return "rpg-action";
    }
}

