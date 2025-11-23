package com.tatumgames.mikros.models;

/**
 * Enum representing behavior categories with Fibonacci-weighted values.
 * Used for reputation score calculations and moderation actions.
 * 
 * Negative weights represent poor behavior, positive weights represent good behavior.
 */
public enum BehaviorCategory {
    // NEGATIVE BEHAVIOR (Fibonacci-weighted)
    /**
     * Poor sportsmanship behavior.
     */
    POOR_SPORTSMANSHIP(-1, "Poor Sportsmanship", "Unsportsmanlike conduct or attitude"),
    
    /**
     * Trolling or constant pinging behavior.
     */
    TROLLING(-2, "Trolling / Constant Pinging", "Deliberately annoying others or excessive mentions"),
    
    /**
     * AFK or complaining behavior.
     */
    AFK_COMPLAINING(-3, "AFK / Complaining", "Frequently AFK or excessive complaining"),
    
    /**
     * Bad language or cheating behavior.
     */
    BAD_LANGUAGE_CHEATING(-5, "Bad Language / Cheating", "Profanity, slurs, or cheating"),
    
    // POSITIVE BEHAVIOR (Fibonacci-weighted)
    /**
     * Good sportsmanship behavior.
     */
    GOOD_SPORTSMANSHIP(1, "Good Sportsmanship", "Positive attitude and fair play"),
    
    /**
     * Great leadership qualities.
     */
    GREAT_LEADERSHIP(2, "Great Leadership", "Shows leadership and guides others"),
    
    /**
     * Excellent teammate qualities.
     */
    EXCELLENT_TEAMMATE(3, "Excellent Teammate", "Supportive and cooperative team player"),
    
    /**
     * MVP level behavior.
     */
    MVP(5, "MVP", "Exceptional contribution and behavior");
    
    private final int weight;
    private final String label;
    private final String description;
    
    /**
     * Creates a BehaviorCategory with the specified weight and label.
     * 
     * @param weight the Fibonacci-based weight for reputation calculations
     * @param label the display label
     * @param description the detailed description
     */
    BehaviorCategory(int weight, String label, String description) {
        this.weight = weight;
        this.label = label;
        this.description = description;
    }
    
    /**
     * Gets the weight of this behavior category.
     * 
     * @return the weight value
     */
    public int getWeight() {
        return weight;
    }
    
    /**
     * Gets the display label of this behavior category.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Gets the detailed description of this behavior category.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this behavior is negative (weight < 0).
     * 
     * @return true if negative behavior, false otherwise
     */
    public boolean isNegative() {
        return weight < 0;
    }
    
    /**
     * Checks if this behavior is positive (weight > 0).
     * 
     * @return true if positive behavior, false otherwise
     */
    public boolean isPositive() {
        return weight > 0;
    }
    
    /**
     * Gets all negative behavior categories.
     * 
     * @return array of negative behavior categories
     */
    public static BehaviorCategory[] getNegativeBehaviors() {
        return new BehaviorCategory[] {
            POOR_SPORTSMANSHIP,
            TROLLING,
            AFK_COMPLAINING,
            BAD_LANGUAGE_CHEATING
        };
    }
    
    /**
     * Gets all positive behavior categories.
     * 
     * @return array of positive behavior categories
     */
    public static BehaviorCategory[] getPositiveBehaviors() {
        return new BehaviorCategory[] {
            GOOD_SPORTSMANSHIP,
            GREAT_LEADERSHIP,
            EXCELLENT_TEAMMATE,
            MVP
        };
    }
}

