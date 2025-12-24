package com.tatumgames.mikros.games.rpg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the outcome of an RPG action.
 * Used to generate Discord message responses.
 */
public record RPGActionOutcome(String narrative, int xpGained, boolean leveledUp, String statIncreased, int statAmount,
                               int damageTaken, int hpRestored, boolean success,
                               List<ItemDrop> itemDrops, List<CatalystDrop> catalystDrops) {
    /**
     * Creates an RPG action outcome.
     *
     * @param narrative      the story/narrative text
     * @param xpGained       experience points gained
     * @param leveledUp      whether the character leveled up
     * @param statIncreased  name of stat increased (or null)
     * @param statAmount     amount stat was increased
     * @param damageTaken    damage taken during action
     * @param hpRestored     HP restored during action (e.g., from rest)
     * @param success        whether the action was successful
     * @param itemDrops      list of essence drops
     * @param catalystDrops  list of catalyst drops
     */
    public RPGActionOutcome {
        if (itemDrops == null) {
            itemDrops = new ArrayList<>();
        }
        if (catalystDrops == null) {
            catalystDrops = new ArrayList<>();
        }
    }

    /**
     * Builder for creating RPGActionOutcome instances.
     */
    public static class Builder {
        private String narrative = "";
        private int xpGained = 0;
        private boolean leveledUp = false;
        private String statIncreased = null;
        private int statAmount = 0;
        private int damageTaken = 0;
        private int hpRestored = 0;
        private boolean success = true;
        private List<ItemDrop> itemDrops = new ArrayList<>();
        private List<CatalystDrop> catalystDrops = new ArrayList<>();

        public Builder narrative(String narrative) {
            this.narrative = narrative;
            return this;
        }

        public Builder xpGained(int xpGained) {
            this.xpGained = xpGained;
            return this;
        }

        public Builder leveledUp(boolean leveledUp) {
            this.leveledUp = leveledUp;
            return this;
        }

        public Builder statIncreased(String statIncreased, int amount) {
            this.statIncreased = statIncreased;
            this.statAmount = amount;
            return this;
        }

        public Builder damageTaken(int damageTaken) {
            this.damageTaken = damageTaken;
            return this;
        }

        public Builder hpRestored(int hpRestored) {
            this.hpRestored = hpRestored;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder addItemDrop(EssenceType essence, int count) {
            this.itemDrops.add(new ItemDrop(essence, count));
            return this;
        }

        public Builder addCatalystDrop(CatalystType catalyst, int count) {
            this.catalystDrops.add(new CatalystDrop(catalyst, count));
            return this;
        }

        public RPGActionOutcome build() {
            return new RPGActionOutcome(narrative, xpGained, leveledUp,
                    statIncreased, statAmount, damageTaken, hpRestored, success,
                    itemDrops, catalystDrops);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
}

