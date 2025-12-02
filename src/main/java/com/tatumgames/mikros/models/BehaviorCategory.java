package com.tatumgames.mikros.models;

/**
 * Enum representing behavior categories with weighted values.
 * Used for reputation score calculations and moderation actions.
 * <p>
 * Negative weights represent poor behavior, positive weights represent good behavior.
 */
public enum BehaviorCategory {
    // NEGATIVE BEHAVIOR
    /**
     * Spammer behavior.
     */
    SPAMMER(-1, "Spammer", "Posting spam or unwanted content"),

    /**
     * Toxic behavior.
     */
    TOXIC_BEHAVIOR(-2, "Toxic Behavior", "Engaging in toxic or harmful behavior"),

    /**
     * Harassing behavior.
     */
    HARRASSING(-3, "Harassing", "Harassing or bullying other users"),

    /**
     * Ignoring rules.
     */
    IGNORING_RULES(-2, "Ignoring Rules", "Repeatedly ignoring server rules"),

    /**
     * Ban evasion.
     */
    BAN_EVASION(-5, "Ban Evasion", "Attempting to evade a ban"),

    /**
     * Trolling behavior.
     */
    TROLL(-3, "Troll", "Trolling or deliberately annoying others"),

    /**
     * Excessive pinging.
     */
    EXCESSIVE_PINGING(-3, "Excessive Pinging", "Excessive use of mentions or pings"),

    /**
     * NSFW content in non-NSFW space.
     */
    NSFW_IN_NON_NSFW_SPACE(-5, "NSFW in Non-NSFW Space", "Posting NSFW content in inappropriate channels"),

    // POSITIVE BEHAVIOR
    /**
     * Active participation.
     */
    ACTIVE_PARTICIPATE(5, "Active Participate", "Actively participating in community activities"),

    /**
     * Good helper behavior.
     */
    GOOD_HELPER(2, "Good Helper", "Helping other community members"),

    /**
     * Positive influencer.
     */
    POSITIVE_INFLUENCER(3, "Positive Influencer", "Positively influencing the community"),

    /**
     * Friendly greeter.
     */
    FRIENDLY_GREETER(1, "Friendly Greeter", "Welcoming new members in a friendly manner");

    private final int weight;
    private final String label;
    private final String description;

    /**
     * Creates a BehaviorCategory with the specified weight and label.
     *
     * @param weight      the Fibonacci-based weight for reputation calculations
     * @param label       the display label
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
        return new BehaviorCategory[]{
                SPAMMER,
                TOXIC_BEHAVIOR,
                HARRASSING,
                IGNORING_RULES,
                BAN_EVASION,
                TROLL,
                EXCESSIVE_PINGING,
                NSFW_IN_NON_NSFW_SPACE
        };
    }

    /**
     * Gets all positive behavior categories.
     *
     * @return array of positive behavior categories
     */
    public static BehaviorCategory[] getPositiveBehaviors() {
        return new BehaviorCategory[]{
                ACTIVE_PARTICIPATE,
                GOOD_HELPER,
                POSITIVE_INFLUENCER,
                FRIENDLY_GREETER
        };
    }
}

