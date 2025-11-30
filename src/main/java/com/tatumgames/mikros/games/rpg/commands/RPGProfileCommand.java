package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
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
 * Command handler for /rpg-profile.
 * Displays a character's stats and information.
 */
public class RPGProfileCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGProfileCommand.class);
    private final CharacterService characterService;
    
    /**
     * Creates a new RPGProfileCommand handler.
     * 
     * @param characterService the character service
     */
    public RPGProfileCommand(CharacterService characterService) {
        this.characterService = characterService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-profile", "View your RPG character profile and stats")
                .addOption(OptionType.USER, "user", "View another user's profile (optional)", false);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Determine whose profile to show
        String targetUserId = event.getOption("user") != null
                ? event.getOption("user").getAsUser().getId()
                : event.getUser().getId();
        
        // Get character
        RPGCharacter character = characterService.getCharacter(targetUserId);
        
        if (character == null) {
            String message = targetUserId.equals(event.getUser().getId())
                    ? "❌ You don't have a character yet! Use `/rpg-register` to create one."
                    : "❌ That user doesn't have a character yet.";
            
            event.reply(message).setEphemeral(true).queue();
            return;
        }
        
        // Get guild config for cooldown info
        RPGConfig config = characterService.getConfig(event.getGuild().getId());
        
        // Build profile embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format(
                "%s %s - Level %d %s",
                character.getCharacterClass().getEmoji(),
                character.getName(),
                character.getLevel(),
                character.getCharacterClass().getDisplayName()
        ));
        
        embed.setColor(getClassColor(character.getCharacterClass().name()));
        
        // XP Progress
        double xpPercent = (double) character.getXp() / character.getXpToNextLevel() * 100;
        embed.addField(
                "📊 Experience",
                String.format(
                        "**%d** / %d XP (%.1f%%)\n" +
                        "%d XP to next level",
                        character.getXp(),
                        character.getXpToNextLevel(),
                        xpPercent,
                        character.getXpToNextLevel() - character.getXp()
                ),
                false
        );
        
        // Stats
        embed.addField(
                "📈 Stats",
                String.format(
                        "❤️ HP: **%d** / %d\n" +
                        "⚔️ STR: **%d**\n" +
                        "🏃 AGI: **%d**\n" +
                        "🧠 INT: **%d**\n" +
                        "🍀 LUCK: **%d**",
                        character.getStats().getCurrentHp(),
                        character.getStats().getMaxHp(),
                        character.getStats().getStrength(),
                        character.getStats().getAgility(),
                        character.getStats().getIntelligence(),
                        character.getStats().getLuck()
                ),
                true
        );
        
        // Cooldown Status
        boolean canAct = character.canPerformAction(config.getCooldownHours());
        String cooldownStatus;
        
        if (canAct) {
            cooldownStatus = "✅ **Ready to act!**\n\nUse `/rpg-action` to continue your adventure";
        } else {
            long secondsRemaining = character.getSecondsUntilNextAction(config.getCooldownHours());
            Duration duration = Duration.ofSeconds(secondsRemaining);
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            
            cooldownStatus = String.format(
                    "⏳ **On cooldown**\n\nNext action available in:\n**%dh %dm**",
                    hours, minutes
            );
        }
        
        embed.addField("⚡ Action Status", cooldownStatus, true);
        
        // Footer
        embed.setFooter(String.format(
                "Character created • Total: %d characters",
                characterService.getCharacterCount()
        ));
        embed.setTimestamp(character.getCreatedAt());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.debug("Profile requested for character: {}", character.getName());
    }
    
    /**
     * Gets color for character class.
     */
    private Color getClassColor(String className) {
        return switch (className) {
            case "WARRIOR" -> Color.RED;
            case "MAGE" -> Color.CYAN;
            case "ROGUE" -> Color.ORANGE;
            default -> Color.GRAY;
        };
    }
    
    @Override
    public String getCommandName() {
        return "rpg-profile";
    }
}

