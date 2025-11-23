package com.tatumgames.mikros.rpg.actions;

import com.tatumgames.mikros.rpg.config.RPGConfig;
import com.tatumgames.mikros.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.rpg.model.RPGCharacter;

/**
 * Interface for RPG character actions.
 * Allows pluggable action implementations.
 */
public interface CharacterAction {
    
    /**
     * Gets the action type name.
     * 
     * @return the action name (e.g., "explore", "train", "battle")
     */
    String getActionName();
    
    /**
     * Gets the action emoji.
     * 
     * @return the emoji representing this action
     */
    String getActionEmoji();
    
    /**
     * Gets the action description.
     * 
     * @return a brief description of what this action does
     */
    String getDescription();
    
    /**
     * Executes the action for a character.
     * 
     * @param character the character performing the action
     * @param config the guild's RPG configuration
     * @return the outcome of the action
     */
    RPGActionOutcome execute(RPGCharacter character, RPGConfig config);
}

