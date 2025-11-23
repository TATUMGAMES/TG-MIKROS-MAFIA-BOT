package com.tatumgames.mikros.communitygames.model;

/**
 * Enum representing available community game types.
 */
public enum GameType {
    /**
     * Word unscramble game - players guess scrambled words.
     */
    WORD_UNSCRAMBLE("Word Unscramble", "🔤", "Unscramble the word of the day!"),
    
    /**
     * Dice roll game - players roll dice to get the highest score.
     */
    DICE_ROLL("Dice Battle", "🎲", "Roll for the highest score!"),
    
    /**
     * Emoji match game - players match emoji patterns.
     */
    EMOJI_MATCH("Emoji Match", "😊", "Match the emoji pattern!");
    
    private final String displayName;
    private final String emoji;
    private final String description;
    
    /**
     * Creates a GameType.
     * 
     * @param displayName the display name of the game
     * @param emoji the emoji representing the game
     * @param description a short description
     */
    GameType(String displayName, String emoji, String description) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.description = description;
    }
    
    /**
     * Gets the display name.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the emoji.
     * 
     * @return the emoji
     */
    public String getEmoji() {
        return emoji;
    }
    
    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}

