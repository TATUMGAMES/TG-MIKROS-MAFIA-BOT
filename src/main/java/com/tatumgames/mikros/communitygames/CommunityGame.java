package com.tatumgames.mikros.communitygames;

import com.tatumgames.mikros.communitygames.model.GameResult;
import com.tatumgames.mikros.communitygames.model.GameSession;
import com.tatumgames.mikros.communitygames.model.GameType;

/**
 * Interface for pluggable community games.
 * Each game type implements this interface to provide consistent behavior.
 */
public interface CommunityGame {
    
    /**
     * Gets the type of this game.
     * 
     * @return the game type
     */
    GameType getGameType();
    
    /**
     * Starts a new game session for a guild.
     * 
     * @param guildId the guild ID
     * @return the new game session
     */
    GameSession startNewSession(String guildId);
    
    /**
     * Handles a player's attempt to play the game.
     * 
     * @param session the current game session
     * @param userId the player's user ID
     * @param username the player's username
     * @param input the player's input/answer
     * @return the game result for this attempt
     */
    GameResult handleAttempt(GameSession session, String userId, String username, String input);
    
    /**
     * Generates the announcement message for the game.
     * 
     * @param session the game session
     * @return the announcement message (can include the challenge)
     */
    String generateAnnouncement(GameSession session);
    
    /**
     * Resets the game session (called at daily reset).
     * 
     * @param session the session to reset
     */
    void resetSession(GameSession session);
}

