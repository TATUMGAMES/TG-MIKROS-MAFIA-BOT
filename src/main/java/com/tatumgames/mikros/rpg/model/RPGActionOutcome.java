package com.tatumgames.mikros.rpg.model;

/**
 * Represents the outcome of an RPG action.
 * Used to generate Discord message responses.
 */
public class RPGActionOutcome {
    private final String narrative;
    private final int xpGained;
    private final boolean leveledUp;
    private final String statIncreased;
    private final int statAmount;
    private final int damageTaken;
    private final boolean success;
    
    /**
     * Creates an RPG action outcome.
     * 
     * @param narrative the story/narrative text
     * @param xpGained experience points gained
     * @param leveledUp whether the character leveled up
     * @param statIncreased name of stat increased (or null)
     * @param statAmount amount stat was increased
     * @param damageTaken damage taken during action
     * @param success whether the action was successful
     */
    public RPGActionOutcome(String narrative, int xpGained, boolean leveledUp,
                           String statIncreased, int statAmount, int damageTaken, boolean success) {
        this.narrative = narrative;
        this.xpGained = xpGained;
        this.leveledUp = leveledUp;
        this.statIncreased = statIncreased;
        this.statAmount = statAmount;
        this.damageTaken = damageTaken;
        this.success = success;
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
        private boolean success = true;
        
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
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public RPGActionOutcome build() {
            return new RPGActionOutcome(narrative, xpGained, leveledUp,
                    statIncreased, statAmount, damageTaken, success);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    
    public String getNarrative() {
        return narrative;
    }
    
    public int getXpGained() {
        return xpGained;
    }
    
    public boolean isLeveledUp() {
        return leveledUp;
    }
    
    public String getStatIncreased() {
        return statIncreased;
    }
    
    public int getStatAmount() {
        return statAmount;
    }
    
    public int getDamageTaken() {
        return damageTaken;
    }
    
    public boolean isSuccess() {
        return success;
    }
}

