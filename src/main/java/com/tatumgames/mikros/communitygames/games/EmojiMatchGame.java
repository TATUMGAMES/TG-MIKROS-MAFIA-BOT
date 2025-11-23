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
 * Emoji match game implementation.
 * Players match emoji patterns to win.
 * 
 * TODO: Custom Emoji Sets
 * - Allow admins to define custom emoji pools per guild
 * - Add difficulty levels by varying pattern length
 * - Add themed emoji sets (animals, food, sports, etc.)
 * - Add time-based scoring for faster solves
 */
public class EmojiMatchGame implements CommunityGame {
    private static final Logger logger = LoggerFactory.getLogger(EmojiMatchGame.class);
    private static final Random random = new Random();
    
    // Emoji pool for generating patterns
    private static final String[] EMOJIS = {
            "🎮", "🎲", "🎯", "🎪", "🎨", "🎭", "🎬", "🎤",
            "🏆", "🏅", "⭐", "✨", "💎", "🔥", "⚡", "🌟",
            "🐉", "🦁", "🦅", "🐺", "🦊", "🐻", "🐯", "🦈",
            "⚔️", "🛡️", "🏹", "🔱", "💣", "🧨", "🎁", "🎉"
    };
    
    private static final int PATTERN_LENGTH = 3;
    
    @Override
    public GameType getGameType() {
        return GameType.EMOJI_MATCH;
    }
    
    @Override
    public GameSession startNewSession(String guildId) {
        // Generate random emoji pattern
        String pattern = generateEmojiPattern();
        
        GameSession session = new GameSession(guildId, GameType.EMOJI_MATCH, Instant.now(), pattern);
        
        logger.info("Started Emoji Match session for guild {} - pattern: {}", guildId, pattern);
        return session;
    }
    
    @Override
    public GameResult handleAttempt(GameSession session, String userId, String username, String input) {
        if (!session.isActive()) {
            return new GameResult(userId, username, input, 0, false, Instant.now());
        }
        
        // Check if user already matched correctly
        boolean alreadyWon = session.getResults().stream()
                .anyMatch(r -> r.getUserId().equals(userId) && r.isCorrect());
        
        if (alreadyWon) {
            return new GameResult(userId, username, input, 0, false, Instant.now());
        }
        
        // Check if pattern matches
        String correctPattern = session.getCorrectAnswer();
        boolean isCorrect = input.trim().equals(correctPattern);
        
        // Calculate score based on time (earlier = higher score)
        int score = 0;
        if (isCorrect) {
            long secondsSinceStart = Instant.now().getEpochSecond() - session.getStartTime().getEpochSecond();
            score = Math.max(100, 1000 - (int) secondsSinceStart);
        }
        
        GameResult result = new GameResult(userId, username, input, score, isCorrect, Instant.now());
        session.addResult(result);
        
        logger.info("Emoji Match attempt by {} in guild {}: {} - {}",
                username, session.getGuildId(), input, isCorrect ? "CORRECT" : "INCORRECT");
        
        return result;
    }
    
    @Override
    public String generateAnnouncement(GameSession session) {
        String pattern = session.getCorrectAnswer();
        
        return String.format(
                "😊 **Emoji Match Challenge**\n\n" +
                "Match this emoji pattern: %s\n\n" +
                "Use `/match <emojis>` to submit your answer!\n" +
                "First correct match wins! 🏆",
                pattern
        );
    }
    
    @Override
    public void resetSession(GameSession session) {
        session.setActive(false);
        logger.info("Reset Emoji Match session for guild {}", session.getGuildId());
    }
    
    /**
     * Generates a random emoji pattern.
     * 
     * @return emoji pattern string
     */
    private String generateEmojiPattern() {
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < PATTERN_LENGTH; i++) {
            pattern.append(EMOJIS[random.nextInt(EMOJIS.length)]);
        }
        return pattern.toString();
    }
}

