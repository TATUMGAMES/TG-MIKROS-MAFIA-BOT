package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.model.CraftedItemType;
import com.tatumgames.mikros.games.rpg.model.CraftingResult;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.model.RPGStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling item crafting.
 * Validates materials, checks stat caps, and applies permanent stat bonuses.
 */
public class CraftingService {
    private static final Logger logger = LoggerFactory.getLogger(CraftingService.class);
    private static final int MAX_CRAFTED_BONUS_PER_STAT = 5;

    /**
     * Crafts an item for a character.
     *
     * @param character the character crafting
     * @param itemType  the item to craft
     * @return the crafting result
     */
    public CraftingResult craft(RPGCharacter character, CraftedItemType itemType) {
        var inventory = character.getInventory();

        // Check materials
        if (!inventory.hasMaterials(itemType)) {
            return CraftingResult.INSUFFICIENT_MATERIALS;
        }

        // Check stat cap
        String statName = itemType.getStatName();
        int currentBonus = inventory.getCraftedBonus(statName);
        if (currentBonus >= MAX_CRAFTED_BONUS_PER_STAT) {
            return CraftingResult.STAT_CAPPED;
        }

        // Craft item
        inventory.craft(itemType);

        // Apply bonus to character stats
        RPGStats stats = character.getStats();
        stats.increaseStat(statName, itemType.getStatBonus());

        logger.info("Character {} crafted {} - applied +{} {}",
                character.getName(), itemType.getDisplayName(),
                itemType.getStatBonus(), statName);

        return CraftingResult.SUCCESS;
    }
}

