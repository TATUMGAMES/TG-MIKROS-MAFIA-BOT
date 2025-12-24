package com.tatumgames.mikros.games.word_unscramble.interfaces;

import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleResult;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleSession;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleType;

/**
 * Interface for Word Unscramble game implementation.
 * Defines the contract for Word Unscramble game behavior.
 */
public interface WordUnscrambleInterface {

    /**
     * Gets the type of this game.
     *
     * @return the game type
     */
    WordUnscrambleType getGameType();

    /**
     * Starts a new game session for a guild.
     *
     * @param guildId the guild ID
     * @return the new game session
     */
    WordUnscrambleSession startNewSession(String guildId);

    /**
     * Handles a player's attempt to play the game.
     *
     * @param session  the current game session
     * @param userId   the player's user ID
     * @param username the player's username
     * @param input    the player's input/answer
     * @return the game result for this attempt
     */
    WordUnscrambleResult handleAttempt(WordUnscrambleSession session, String userId, String username, String input);

    /**
     * Generates the announcement message for the game.
     *
     * @param session the game session
     * @return the announcement message (can include the challenge)
     */
    String generateAnnouncement(WordUnscrambleSession session);

    /**
     * Resets the game session (called at hourly reset).
     *
     * @param session the session to reset
     */
    void resetSession(WordUnscrambleSession session);
}


