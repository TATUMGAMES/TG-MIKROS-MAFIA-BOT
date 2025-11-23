package com.tatumgames.mikros.rpg.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.rpg.model.CharacterClass;
import com.tatumgames.mikros.rpg.model.RPGCharacter;
import com.tatumgames.mikros.rpg.service.CharacterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

/**
 * Command handler for /rpg-register.
 * Allows users to create their RPG character.
 */
public class RPGRegisterCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGRegisterCommand.class);
    private final CharacterService characterService;
    
    /**
     * Creates a new RPGRegisterCommand handler.
     * 
     * @param characterService the character service
     */
    public RPGRegisterCommand(CharacterService characterService) {
        this.characterService = characterService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-register", "Create your RPG character and begin your adventure")
                .addOption(OptionType.STRING, "name", "Your character's name", true)
                .addOption(OptionType.STRING, "class", "Your character class (WARRIOR, MAGE, ROGUE)", true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        
        // Check if user already has a character
        if (characterService.hasCharacter(userId)) {
            event.reply("❌ You already have a character! Use `/rpg-profile` to view it.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get options
        String name = event.getOption("name").getAsString().trim();
        String classString = event.getOption("class").getAsString().toUpperCase().trim();
        
        // Validate name
        if (name.length() < 2 || name.length() > 20) {
            event.reply("❌ Character name must be between 2 and 20 characters.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Validate class
        CharacterClass characterClass;
        try {
            characterClass = CharacterClass.valueOf(classString);
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid class! Choose from: **WARRIOR**, **MAGE**, or **ROGUE**")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Create character
        try {
            RPGCharacter character = characterService.registerCharacter(userId, name, characterClass);
            
            // Build welcome embed
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("⚔️ Character Created!");
            embed.setColor(Color.GREEN);
            embed.setDescription(String.format(
                    "Welcome, **%s**! Your adventure begins now.",
                    name
            ));
            
            embed.addField(
                    "Class",
                    String.format("%s **%s**",
                            characterClass.getEmoji(),
                            characterClass.getDisplayName()),
                    true
            );
            
            embed.addField(
                    "Level",
                    String.valueOf(character.getLevel()),
                    true
            );
            
            embed.addField(
                    "XP",
                    String.format("%d / %d",
                            character.getXp(),
                            character.getXpToNextLevel()),
                    true
            );
            
            embed.addField(
                    "Stats",
                    String.format(
                            "❤️ HP: %d/%d\n" +
                            "⚔️ STR: %d\n" +
                            "🏃 AGI: %d\n" +
                            "🧠 INT: %d\n" +
                            "🍀 LUCK: %d",
                            character.getStats().getCurrentHp(),
                            character.getStats().getMaxHp(),
                            character.getStats().getStrength(),
                            character.getStats().getAgility(),
                            character.getStats().getIntelligence(),
                            character.getStats().getLuck()
                    ),
                    false
            );
            
            embed.addField(
                    "🎮 Getting Started",
                    "• Use `/rpg-action` to explore, train, or battle\n" +
                    "• Use `/rpg-profile` to view your stats\n" +
                    "• Use `/rpg-leaderboard` to see top players\n\n" +
                    "Good luck on your journey!",
                    false
            );
            
            embed.setTimestamp(java.time.Instant.now());
            
            event.replyEmbeds(embed.build()).queue();
            
            logger.info("User {} registered character: {} ({})",
                    userId, name, characterClass.getDisplayName());
            
        } catch (IllegalStateException e) {
            event.reply("❌ Error creating character: " + e.getMessage())
                    .setEphemeral(true)
                    .queue();
        }
    }
    
    @Override
    public String getCommandName() {
        return "rpg-register";
    }
}

