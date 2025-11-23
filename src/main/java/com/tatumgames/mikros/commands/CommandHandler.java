package com.tatumgames.mikros.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Interface for handling slash commands.
 */
public interface CommandHandler {
    
    /**
     * Gets the command data for registration with Discord.
     * 
     * @return the command data
     */
    CommandData getCommandData();
    
    /**
     * Handles the slash command interaction.
     * 
     * @param event the slash command event
     */
    void handle(SlashCommandInteractionEvent event);
    
    /**
     * Gets the name of the command.
     * 
     * @return the command name
     */
    String getCommandName();
}

