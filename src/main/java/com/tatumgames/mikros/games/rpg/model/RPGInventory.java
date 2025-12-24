package com.tatumgames.mikros.games.rpg.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a character's inventory containing essences, catalysts, and crafted bonuses.
 */
public class RPGInventory {
    private static final int MAX_CRAFTED_BONUS_PER_STAT = 5;

    private final Map<EssenceType, Integer> essences;
    private final Map<CatalystType, Integer> catalysts;
    private final Map<String, Integer> craftedBonuses; // stat name -> bonus amount (0-5)

    /**
     * Creates a new empty inventory.
     */
    public RPGInventory() {
        this.essences = new HashMap<>();
        this.catalysts = new HashMap<>();
        this.craftedBonuses = new HashMap<>();
        
        // Initialize crafted bonuses to 0
        this.craftedBonuses.put("STR", 0);
        this.craftedBonuses.put("AGI", 0);
        this.craftedBonuses.put("INT", 0);
        this.craftedBonuses.put("LUCK", 0);
        this.craftedBonuses.put("HP", 0);
    }

    /**
     * Adds essences to inventory.
     *
     * @param essence the essence type
     * @param count   the amount to add
     */
    public void addEssence(EssenceType essence, int count) {
        Objects.requireNonNull(essence);
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        essences.put(essence, essences.getOrDefault(essence, 0) + count);
    }

    /**
     * Adds catalysts to inventory.
     *
     * @param catalyst the catalyst type
     * @param count    the amount to add
     */
    public void addCatalyst(CatalystType catalyst, int count) {
        Objects.requireNonNull(catalyst);
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        catalysts.put(catalyst, catalysts.getOrDefault(catalyst, 0) + count);
    }

    /**
     * Gets the count of a specific essence.
     *
     * @param essence the essence type
     * @return the count (0 if not present)
     */
    public int getEssenceCount(EssenceType essence) {
        return essences.getOrDefault(essence, 0);
    }

    /**
     * Gets the count of a specific catalyst.
     *
     * @param catalyst the catalyst type
     * @return the count (0 if not present)
     */
    public int getCatalystCount(CatalystType catalyst) {
        return catalysts.getOrDefault(catalyst, 0);
    }

    /**
     * Gets the crafted bonus for a stat.
     *
     * @param statName the stat name (STR, AGI, INT, LUCK, HP)
     * @return the bonus amount (0-5)
     */
    public int getCraftedBonus(String statName) {
        return craftedBonuses.getOrDefault(statName.toUpperCase(), 0);
    }

    /**
     * Checks if the inventory has the required materials for a crafted item.
     *
     * @param itemType the crafted item type
     * @return true if materials are available
     */
    public boolean hasMaterials(CraftedItemType itemType) {
        int essenceCount = getEssenceCount(itemType.getRequiredEssence());
        int catalystCount = getCatalystCount(itemType.getRequiredCatalyst());
        
        return essenceCount >= itemType.getEssenceCount() &&
               catalystCount >= itemType.getCatalystCount();
    }

    /**
     * Checks if a crafted item can be crafted (has materials and stat not capped).
     *
     * @param itemType the crafted item type
     * @return true if can craft
     */
    public boolean canCraft(CraftedItemType itemType) {
        if (!hasMaterials(itemType)) {
            return false;
        }
        
        int currentBonus = getCraftedBonus(itemType.getStatName());
        return currentBonus < MAX_CRAFTED_BONUS_PER_STAT;
    }

    /**
     * Crafts an item, consuming materials and applying the bonus.
     * Does not validate - use canCraft() first.
     *
     * @param itemType the crafted item type
     */
    public void craft(CraftedItemType itemType) {
        // Consume materials
        int essenceCount = getEssenceCount(itemType.getRequiredEssence());
        int catalystCount = getCatalystCount(itemType.getRequiredCatalyst());
        
        essences.put(itemType.getRequiredEssence(), essenceCount - itemType.getEssenceCount());
        catalysts.put(itemType.getRequiredCatalyst(), catalystCount - itemType.getCatalystCount());
        
        // Apply bonus (enforce cap)
        String statName = itemType.getStatName();
        int currentBonus = getCraftedBonus(statName);
        int newBonus = Math.min(MAX_CRAFTED_BONUS_PER_STAT, currentBonus + itemType.getStatBonus());
        craftedBonuses.put(statName, newBonus);
    }

    // Getters for maps (for serialization if needed)

    public Map<EssenceType, Integer> getEssences() {
        return new HashMap<>(essences);
    }

    public Map<CatalystType, Integer> getCatalysts() {
        return new HashMap<>(catalysts);
    }

    public Map<String, Integer> getCraftedBonuses() {
        return new HashMap<>(craftedBonuses);
    }
}

