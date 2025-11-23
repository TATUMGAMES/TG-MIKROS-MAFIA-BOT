package com.tatumgames.mikros.spelling.service;

import com.tatumgames.mikros.spelling.model.ChallengeSession;
import com.tatumgames.mikros.spelling.model.PlayerAttempt;
import com.tatumgames.mikros.spelling.model.SpellingLeaderboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing daily spelling challenges.
 * 
 * TODO: Future Features
 * - Hint system (/hint command showing first letter + length)
 * - RPG integration (award XP for correct answers)
 * - Difficulty levels (easy 4-5 letters, medium 6-7, hard 8+ letters)
 * - Custom word lists per server
 * - Persistent storage for challenges and leaderboards
 */
public class SpellingChallengeService {
    private static final Logger logger = LoggerFactory.getLogger(SpellingChallengeService.class);
    private static final Random random = new Random();
    private static final int MAX_ATTEMPTS_PER_DAY = 3;
    private static final int FIRST_SOLVER_POINTS = 3;
    private static final int SOLVER_POINTS = 1;
    
    // Word dictionary for challenges
    private static final String[] WORDS = {
            // 4 letters
            "GAME", "PLAY", "CODE", "TEAM", "CHAT", "HERO", "STAR", "RAGE",
            "MOON", "FIRE", "WIND", "STORM", "BOLT", "RUSH", "DAWN", "DUSK",
            // 5 letters
            "QUEST", "MAGIC", "SWORD", "SHIELD", "CROWN", "TOWER", "GUILD",
            "BEAST", "ARENA", "CHAOS", "CRAFT", "DREAM", "FLAME", "GHOST",
            "HASTE", "LIGHT", "METAL", "NIGHT", "OCEAN", "POWER", "REALM",
            // 6 letters
            "DRAGON", "KNIGHT", "WIZARD", "BATTLE", "CASTLE", "DUNGEON",
            "EMPIRE", "FOREST", "HEALER", "LEGEND", "METEOR", "ORACLE",
            "PIRATE", "QUIVER", "RANGER", "SHADOW", "TEMPLE", "UNDEAD",
            // 7 letters
            "VICTORY", "CENTURY", "MONSTER", "CRYSTAL", "ALCHEMY", "WARRIOR",
            "SCHOLAR", "FANTASY", "JOURNEY", "KINGDOM", "MYSTERY", "PHOENIX",
            "SORCERY", "ELEMENT", "DESTINY", "ETERNAL", "FREEDOM", "HARMONY",
            // 8 letters
            "CHAMPION", "TREASURE", "CONQUEST", "DEFENDER", "FORTRESS", "MYSTICAL",
            "ARTIFACT", "CREATION", "IMMORTAL", "ILLUSION", "GUARDIAN", "SURVIVAL"
    };
    
    // Active sessions: guildId -> ChallengeSession
    private final Map<String, ChallengeSession> activeSessions;
    
    // Global leaderboard
    private final SpellingLeaderboard leaderboard;
    
    /**
     * Creates a new SpellingChallengeService.
     */
    public SpellingChallengeService() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.leaderboard = new SpellingLeaderboard();
        logger.info("SpellingChallengeService initialized with {} words", WORDS.length);
    }
    
    /**
     * Starts a new daily challenge for a guild.
     * 
     * @param guildId the guild ID
     * @return the new challenge session
     */
    public ChallengeSession startNewChallenge(String guildId) {
        // Pick random word
        String word = WORDS[random.nextInt(WORDS.length)];
        
        // Scramble it
        String scrambled = scrambleWord(word);
        
        // Create session
        ChallengeSession session = new ChallengeSession(guildId, word, scrambled, Instant.now());
        activeSessions.put(guildId, session);
        
        logger.info("Started new spelling challenge for guild {}: {} (scrambled: {})",
                guildId, word, scrambled);
        
        return session;
    }
    
    /**
     * Gets the active challenge session for a guild.
     * 
     * @param guildId the guild ID
     * @return the active session, or null if none
     */
    public ChallengeSession getActiveSession(String guildId) {
        return activeSessions.get(guildId);
    }
    
    /**
     * Processes a guess attempt.
     * 
     * @param guildId the guild ID
     * @param userId the user ID
     * @param username the username
     * @param guess the guessed word
     * @return the attempt result
     */
    public AttemptResult processGuess(String guildId, String userId, String username, String guess) {
        ChallengeSession session = activeSessions.get(guildId);
        
        if (session == null || !session.isActive()) {
            return new AttemptResult(false, false, 0, 0, "No active challenge");
        }
        
        // Check if user already solved
        if (session.hasUserSolved(userId)) {
            return new AttemptResult(false, false, 0, 0, "Already solved");
        }
        
        // Check attempt limit
        List<PlayerAttempt> userAttempts = session.getAttemptsByUser(userId);
        if (userAttempts.size() >= MAX_ATTEMPTS_PER_DAY) {
            return new AttemptResult(false, false, 0, userAttempts.size(), "Max attempts reached");
        }
        
        // Check if correct
        boolean isCorrect = guess.trim().equalsIgnoreCase(session.getCorrectWord());
        
        // Create attempt
        PlayerAttempt attempt = new PlayerAttempt(userId, username, guess, isCorrect, Instant.now());
        session.addAttempt(attempt);
        
        int attemptsUsed = userAttempts.size() + 1;
        int points = 0;
        
        if (isCorrect) {
            // Award points
            boolean wasFirst = session.getFirstSolver().equals(attempt);
            points = wasFirst ? FIRST_SOLVER_POINTS : SOLVER_POINTS;
            leaderboard.awardPoints(userId, username, points, wasFirst);
            
            logger.info("User {} solved spelling challenge in guild {} (attempt {}/{}): {} points",
                    username, guildId, attemptsUsed, MAX_ATTEMPTS_PER_DAY, points);
        }
        
        return new AttemptResult(isCorrect, isCorrect && session.getFirstSolver().equals(attempt),
                points, attemptsUsed, isCorrect ? "Correct" : "Incorrect");
    }
    
    /**
     * Resets the challenge for a guild.
     * 
     * @param guildId the guild ID
     */
    public void resetChallenge(String guildId) {
        activeSessions.remove(guildId);
        logger.info("Reset spelling challenge for guild {}", guildId);
    }
    
    /**
     * Gets the global leaderboard.
     * 
     * @return the leaderboard
     */
    public SpellingLeaderboard getLeaderboard() {
        return leaderboard;
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
        
        // Shuffle until different from original
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
    
    /**
     * Result of a guess attempt.
     */
    public static class AttemptResult {
        private final boolean isCorrect;
        private final boolean wasFirst;
        private final int pointsAwarded;
        private final int attemptsUsed;
        private final String message;
        
        public AttemptResult(boolean isCorrect, boolean wasFirst, int pointsAwarded,
                           int attemptsUsed, String message) {
            this.isCorrect = isCorrect;
            this.wasFirst = wasFirst;
            this.pointsAwarded = pointsAwarded;
            this.attemptsUsed = attemptsUsed;
            this.message = message;
        }
        
        public boolean isCorrect() {
            return isCorrect;
        }
        
        public boolean wasFirst() {
            return wasFirst;
        }
        
        public int getPointsAwarded() {
            return pointsAwarded;
        }
        
        public int getAttemptsUsed() {
            return attemptsUsed;
        }
        
        public String getMessage() {
            return message;
        }
    }
}








