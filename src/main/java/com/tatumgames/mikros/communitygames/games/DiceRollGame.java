package com.tatumgames.mikros.communitygames.games;

import com.tatumgames.mikros.communitygames.CommunityGame;
import com.tatumgames.mikros.communitygames.model.GameResult;
import com.tatumgames.mikros.communitygames.model.GameSession;
import com.tatumgames.mikros.communitygames.model.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Random;

/**
 * Dice roll game implementation.
 * Players roll a D20 to try to get the highest score of the day.
 */
public class DiceRollGame implements CommunityGame {
    private static final Logger logger = LoggerFactory.getLogger(DiceRollGame.class);
    private static final Random random = new Random();
    private static final int DICE_SIDES = 20;
    
    @Override
    public GameType getGameType() {
        return GameType.DICE_ROLL;
    }
    
    @Override
    public GameSession startNewSession(String guildId) {
        GameSession session = new GameSession(guildId, GameType.DICE_ROLL, Instant.now(), null);
        
        logger.info("Started Dice Roll session for guild {}", guildId);
        return session;
    }
    
    @Override
    public GameResult handleAttempt(GameSession session, String userId, String username, String input) {
        if (!session.isActive()) {
            return new GameResult(userId, username, "", 0, false, Instant.now());
        }
        
        // Check if user already rolled today
        boolean alreadyRolled = session.getResults().stream()
                .anyMatch(r -> r.getUserId().equals(userId));
        
        if (alreadyRolled) {
            // Return a result indicating they already rolled
            return new GameResult(userId, username, "already_rolled", 0, false, Instant.now());
        }
        
        // Roll the dice
        int roll = random.nextInt(DICE_SIDES) + 1;
        
        // Create result
        GameResult result = new GameResult(
                userId,
                username,
                String.valueOf(roll),
                roll,
                roll == DICE_SIDES, // Critical hit on natural 20
                Instant.now()
        );
        
        session.addResult(result);
        
        logger.info("Dice Roll attempt by {} in guild {}: rolled {}",
                username, session.getGuildId(), roll);
        
        return result;
    }
    
    @Override
    public String generateAnnouncement(GameSession session) {
        return String.format(
                "🎲 **Dice Battle Challenge**\n\n" +
                "Roll a D%d and try to get the highest score!\n\n" +
                "Use `/roll` to roll your dice!\n" +
                "One roll per person - highest roller wins! 🏆",
                DICE_SIDES
        );
    }
    
    @Override
    public void resetSession(GameSession session) {
        session.setActive(false);
        logger.info("Reset Dice Roll session for guild {}", session.getGuildId());
    }
}

