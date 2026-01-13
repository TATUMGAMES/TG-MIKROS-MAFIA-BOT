package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Service for handling item crafting.
 * Validates materials, checks stat caps, and applies permanent stat bonuses.
 */
public class CraftingService {
    private static final Logger logger = LoggerFactory.getLogger(CraftingService.class);
    private static final int MAX_CRAFTED_BONUS_PER_STAT = 5;
    private static final Random random = new Random();
    private final LoreRecognitionService loreRecognitionService;

    /**
     * Creates a new CraftingService.
     *
     * @param loreRecognitionService the lore recognition service for milestone checks
     */
    public CraftingService(LoreRecognitionService loreRecognitionService) {
        this.loreRecognitionService = loreRecognitionService;
    }

    /**
     * Crafts an item for a character.
     *
     * @param character the character crafting
     * @param itemType  the item to craft
     * @return the crafting result with catalyst preservation info
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

        // INT-based catalyst preservation chance (INT/2% chance)
        RPGStats stats = character.getStats();
        int intelligence = stats.getIntelligence();
        double catalystPreserveChance = intelligence / 2.0 / 100.0; // INT/2%

        // Apply infusion effects for catalyst preservation
        InfusionType activeInfusion = character.getInventory().getActiveInfusion();
        boolean infusionConsumed = false;
        if (activeInfusion != null && activeInfusion == InfusionType.ASTRAL_INSIGHT) {
            // Astral Insight: +5% catalyst preservation chance
            catalystPreserveChance += 0.05;
            infusionConsumed = true;
        }

        boolean catalystPreserved = random.nextDouble() < catalystPreserveChance;

        // Craft item (consumes materials)
        inventory.craft(itemType);

        // If catalyst was preserved, restore it
        if (catalystPreserved) {
            inventory.addCatalyst(itemType.getRequiredCatalyst(), itemType.getCatalystCount());
            logger.info("Character {} crafted {} - catalyst preserved due to high intelligence!",
                    character.getName(), itemType.getDisplayName());
        }

        // Apply bonus to character stats
        stats.increaseStat(statName, itemType.getStatBonus());

        // Consume active infusion if used
        if (infusionConsumed) {
            character.getInventory().consumeActiveInfusion();
        }

        logger.info("Character {} crafted {} - applied +{} {}",
                character.getName(), itemType.getDisplayName(),
                itemType.getStatBonus(), statName);

        return CraftingResult.SUCCESS;
    }

    /**
     * Crafts an infusion for a character.
     * Infusions are temporary single-use items that auto-consume on next action.
     *
     * @param character    the character crafting
     * @param infusionType the infusion type to craft
     * @return the crafting result
     */
    public CraftingResult craftInfusion(RPGCharacter character, InfusionType infusionType) {
        var inventory = character.getInventory();

        // Check if infusion already active (max 1 at a time)
        if (inventory.hasActiveInfusion()) {
            return CraftingResult.INSUFFICIENT_MATERIALS; // Reuse this result for "infusion already active"
        }

        // Check materials
        if (infusionType.isElementalConvergence()) {
            // Elemental Convergence requires 1x of each essence type (5 total) + 1x Runic Binding
            boolean hasAllEssences = true;
            for (EssenceType essence : EssenceType.values()) {
                if (inventory.getEssenceCount(essence) < 1) {
                    hasAllEssences = false;
                    break;
                }
            }
            if (!hasAllEssences || inventory.getCatalystCount(com.tatumgames.mikros.games.rpg.model.CatalystType.RUNIC_BINDING) < 1) {
                return CraftingResult.INSUFFICIENT_MATERIALS;
            }

            // Consume materials
            for (EssenceType essence : EssenceType.values()) {
                inventory.removeEssence(essence, 1);
            }
            inventory.removeCatalyst(com.tatumgames.mikros.games.rpg.model.CatalystType.RUNIC_BINDING, 1);
        } else {
            // Regular infusion: check standard materials
            int essenceCount = inventory.getEssenceCount(infusionType.getRequiredEssence());
            int catalystCount = inventory.getCatalystCount(infusionType.getRequiredCatalyst());

            if (essenceCount < infusionType.getEssenceCount() ||
                    catalystCount < infusionType.getCatalystCount()) {
                return CraftingResult.INSUFFICIENT_MATERIALS;
            }

            // Consume materials
            inventory.removeEssence(infusionType.getRequiredEssence(), infusionType.getEssenceCount());
            inventory.removeCatalyst(infusionType.getRequiredCatalyst(), infusionType.getCatalystCount());
        }

        // Set active infusion (expires in 24 hours)
        inventory.setActiveInfusion(infusionType);

        // Track infusion crafting for lore recognition
        character.addInfusionCrafted(infusionType);

        // Check for lore recognition milestones
        if (loreRecognitionService != null) {
            loreRecognitionService.checkMilestones(character);
        }

        logger.info("Character {} crafted {} - infusion active (expires in 24 hours)",
                character.getName(), infusionType.getDisplayName());

        return CraftingResult.SUCCESS;
    }

}

