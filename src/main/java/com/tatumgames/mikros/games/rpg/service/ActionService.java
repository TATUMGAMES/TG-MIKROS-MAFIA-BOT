package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.actions.*;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service for managing RPG actions.
 * Handles action execution and validation.
 */
public class ActionService {
    private static final Logger logger = LoggerFactory.getLogger(ActionService.class);

    private final Map<String, CharacterAction> actions;

    /**
     * Creates a new ActionService and registers all actions.
     *
     * @param characterService the character service (needed for DonateAction)
     * @param worldCurseService the world curse service (needed for curse effects)
     * @param auraService the aura service (needed for Song of Nilfheim)
     * @param nilfheimEventService the Nilfheim event service (needed for server-wide events)
     * @param loreRecognitionService the lore recognition service (needed for milestone checks)
     */
    public ActionService(CharacterService characterService, WorldCurseService worldCurseService, AuraService auraService, com.tatumgames.mikros.games.rpg.service.NilfheimEventService nilfheimEventService, com.tatumgames.mikros.games.rpg.service.LoreRecognitionService loreRecognitionService) {
        this.actions = new HashMap<>();

        // Register available actions
        registerAction(new ExploreAction(worldCurseService, auraService, nilfheimEventService, loreRecognitionService));
        registerAction(new TrainAction(nilfheimEventService, loreRecognitionService));
        registerAction(new BattleAction(worldCurseService, auraService, nilfheimEventService, loreRecognitionService));
        registerAction(new RestAction());
        registerAction(new DonateAction(characterService));

        logger.info("ActionService initialized with {} actions", actions.size());
    }

    /**
     * Registers an action.
     *
     * @param action the action to register
     */
    private void registerAction(CharacterAction action) {
        actions.put(action.getActionName().toLowerCase(), action);
        logger.debug("Registered action: {} {}", action.getActionEmoji(), action.getActionName());
    }

    /**
     * Executes an action for a character.
     *
     * @param actionName the action to execute
     * @param character  the character performing the action
     * @param config     the guild RPG configuration
     * @return the action outcome
     * @throws IllegalArgumentException if action doesn't exist
     */
    public RPGActionOutcome executeAction(String actionName, RPGCharacter character, RPGConfig config) {
        CharacterAction action = actions.get(actionName.toLowerCase());

        if (action == null) {
            throw new IllegalArgumentException("Unknown action: " + actionName);
        }

        logger.info("Executing action {} for character {} (Level {})",
                actionName, character.getName(), character.getLevel());

        return action.execute(character, config);
    }

    /**
     * Gets an action by name.
     *
     * @param actionName the action name
     * @return the action, or null if not found
     */
    public CharacterAction getAction(String actionName) {
        return actions.get(actionName.toLowerCase());
    }

    /**
     * Gets all available action names.
     *
     * @return set of action names
     */
    public Set<String> getAvailableActions() {
        return actions.keySet();
    }

    /**
     * Checks if an action exists.
     *
     * @param actionName the action name
     * @return true if the action exists
     */
    public boolean hasAction(String actionName) {
        return actions.containsKey(actionName.toLowerCase());
    }
}

