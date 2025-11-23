package com.tatumgames.mikros.communitygames.games;

import com.tatumgames.mikros.communitygames.CommunityGame;
import com.tatumgames.mikros.communitygames.model.GameResult;
import com.tatumgames.mikros.communitygames.model.GameSession;
import com.tatumgames.mikros.communitygames.model.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Word unscramble game implementation.
 * Players guess the correct unscrambled word.
 * 
 * TODO: Custom Word Lists
 * - Allow admins to upload custom word lists per guild
 * - Add difficulty levels (easy, medium, hard) based on word length
 * - Add themed word packs (gaming, tech, fantasy, etc.)
 * - Track most difficult words (fewest correct guesses)
 */
public class WordUnscrambleGame implements CommunityGame {
    private static final Logger logger = LoggerFactory.getLogger(WordUnscrambleGame.class);
    private static final Random random = new Random();
    
    // Word list for the game (expandable)
    private static final String[] WORDS = {
            "GAMEPLAY", "STREAMER", "GIVEAWAY", "CHAMPION", "TREASURE",
            "ADVENTURE", "VICTORY", "PLATFORM", "CHALLENGE", "COMMUNITY",
            "DISCORD", "CREATIVE", "STRATEGY", "TOURNAMENT", "LEGENDARY",
            "MULTIPLAYER", "CAMPAIGN", "CHARACTER", "ACHIEVEMENT", "DEVELOPER"
    };
    
    @Override
    public GameType getGameType() {
        return GameType.WORD_UNSCRAMBLE;
    }
    
    @Override
    public GameSession startNewSession(String guildId) {
        // Pick a random word
        String word = WORDS[random.nextInt(WORDS.length)];
        
        // Scramble it
        String scrambled = scrambleWord(word);
        
        // Create session with the scrambled version stored, correct answer is the original
        GameSession session = new GameSession(guildId, GameType.WORD_UNSCRAMBLE, Instant.now(), word);
        
        // Store scrambled version in a way we can access it (we'll use the session differently)
        // For now, we'll generate it fresh in the announcement
        
        logger.info("Started Word Unscramble session for guild {} - word: {} (scrambled: {})",
                guildId, word, scrambled);
        
        return session;
    }
    
    @Override
    public GameResult handleAttempt(GameSession session, String userId, String username, String input) {
        if (!session.isActive()) {
            return new GameResult(userId, username, input, 0, false, Instant.now());
        }
        
        // Check if user already guessed correctly
        boolean alreadyWon = session.getResults().stream()
                .anyMatch(r -> r.getUserId().equals(userId) && r.isCorrect());
        
        if (alreadyWon) {
            return new GameResult(userId, username, input, 0, false, Instant.now());
        }
        
        // Check if answer is correct
        String correctAnswer = session.getCorrectAnswer();
        boolean isCorrect = input.trim().equalsIgnoreCase(correctAnswer);
        
        // Calculate score based on time (earlier = higher score)
        int score = 0;
        if (isCorrect) {
            long secondsSinceStart = Instant.now().getEpochSecond() - session.getStartTime().getEpochSecond();
            score = Math.max(100, 1000 - (int) secondsSinceStart);
        }
        
        GameResult result = new GameResult(userId, username, input, score, isCorrect, Instant.now());
        session.addResult(result);
        
        logger.info("Word Unscramble attempt by {} in guild {}: {} - {}",
                username, session.getGuildId(), input, isCorrect ? "CORRECT" : "INCORRECT");
        
        return result;
    }
    
    @Override
    public String generateAnnouncement(GameSession session) {
        String word = session.getCorrectAnswer();
        String scrambled = scrambleWord(word);
        
        return String.format(
                "🔤 **Word Unscramble Challenge**\n\n" +
                "Unscramble this word: **%s** (%d letters)\n\n" +
                "Use `/guess <word>` to submit your answer!\n" +
                "First correct guess wins! 🏆",
                scrambled,
                word.length()
        );
    }
    
    @Override
    public void resetSession(GameSession session) {
        session.setActive(false);
        logger.info("Reset Word Unscramble session for guild {}", session.getGuildId());
    }
    
    /**
     * Scrambles a word by shuffling its letters.
     * 
     * @param word the word to scramble
     * @return the scrambled word
     */
    private String scrambleWord(String word) {
        List<Character> chars = new ArrayList<>();
        for (char c : word.toCharArray()) {
            chars.add(c);
        }
        
        // Shuffle until it's different from the original
        String scrambled;
        int attempts = 0;
        do {
            Collections.shuffle(chars, random);
            StringBuilder sb = new StringBuilder();
            for (char c : chars) {
                sb.append(c);
            }
            scrambled = sb.toString();
            attempts++;
        } while (scrambled.equals(word) && attempts < 10);
        
        return scrambled;
    }
}

