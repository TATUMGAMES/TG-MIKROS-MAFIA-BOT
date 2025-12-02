package com.tatumgames.mikros.games.word_unscramble.model;

/**
 * Enum representing Word Unscramble game type.
 */
public enum WordUnscrambleType {
    /**
     * Word unscramble game - players guess scrambled words.
     */
    WORD_UNSCRAMBLE("Word Unscramble", "🔤", "Unscramble the word of the day!");

    private final String displayName;
    private final String emoji;
    private final String description;

    /**
     * Creates a WordUnscrambleType.
     *
     * @param displayName the display name of the game
     * @param emoji       the emoji representing the game
     * @param description a short description
     */
    WordUnscrambleType(String displayName, String emoji, String description) {
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




