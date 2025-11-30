package com.tatumgames.mikros.games.word_unscramble.games;

import com.tatumgames.mikros.games.word_unscramble.WordUnscrambleInterface;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleResult;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleSession;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * Word unscramble game implementation with 20-level progression system.
 * Players guess the correct unscrambled word or phrase.
 * Levels 1-9: Words only
 * Levels 10-14: Short phrases (2-3 words)
 * Levels 15-20: Longer phrases
 */
public class WordUnscrambleGame implements WordUnscrambleInterface {
    private static final Logger logger = LoggerFactory.getLogger(WordUnscrambleGame.class);
    private static final Random random = new Random();
    private static final int MAX_LEVEL = 20;
    
    // 20 levels of words and phrases
    private static final Map<Integer, List<String>> LEVEL_WORDS = new HashMap<>();
    
    static {
        // Level 1: Easy (4-5 letters)
        LEVEL_WORDS.put(1, Arrays.asList(
            "PLAY", "GAME", "TEAM", "LOOT", "BOSS", "QUEST", "BONUS", "LEVEL", "SCORE", "SKILL",
            "ROLE", "DICE", "CARD", "MODE", "MAP", "ZONE", "ITEM", "GOLD", "COIN", "RANK"
        ));
        
        // Level 2: Easy (5-6 letters)
        LEVEL_WORDS.put(2, Arrays.asList(
            "ARENA", "POWER", "MAGIC", "SPEED", "GRIND", "PARTY", "WEAPON", "ARMOR", "HEROES", "COMBO",
            "SKILL", "SPELL", "BUILD", "CLASS", "GUILD", "TRADE", "CRAFT", "FORGE", "SMITH", "MERGE"
        ));
        
        // Level 3: Easy-Medium (6-7 letters)
        LEVEL_WORDS.put(3, Arrays.asList(
            "ACTION", "PLAYER", "TARGET", "DAMAGE", "ATTACK", "HEALTH", "RANKED", "MODELS", "COINS", "BOOST",
            "SHIELD", "SWORD", "BOW", "STAFF", "WAND", "POTION", "SCROLL", "GEM", "RUNE", "ENCHANT"
        ));
        
        // Level 4: Medium (7-8 letters)
        LEVEL_WORDS.put(4, Arrays.asList(
            "MISSION", "ENEMIES", "PORTAL", "FANTASY", "REWARD", "JOURNEY", "ROGUELIKE", "SANDBOX", "VICTORY", "UPGRADE",
            "DUNGEON", "DRAGON", "WIZARD", "KNIGHT", "RANGER", "PRIEST", "MONK", "ROGUE", "MAGE", "WARRIOR"
        ));
        
        // Level 5: Medium (8-9 letters)
        LEVEL_WORDS.put(5, Arrays.asList(
            "SURVIVAL", "MULTIPLE", "GRAPHICS", "CREATIVE", "ADVENTURE", "PLATFORM", "CHALLENGE", "WARRIORS", "TACTICAL", "STRATEGY",
            "CAMPAIGN", "BATTLE", "COMBAT", "DEFENSE", "OFFENSE", "SUPPORT", "HEALER", "TANK", "DPS", "BUFFER"
        ));
        
        // Level 6: Medium-Long (8-10 letters)
        LEVEL_WORDS.put(6, Arrays.asList(
            "CHARACTER", "INVENTORY", "SOUNDTRACK", "ENCHANTING", "DUNGEON", "MINIMAP", "CHECKPOINT", "PLAYTHROUGH",
            "NARRATIVE", "STORYLINE", "PROTAGONIST", "ANTAGONIST", "QUESTLINE", "SIDEMISSION", "MAINQUEST", "SIDEQUEST"
        ));
        
        // Level 7: Long (9-11 letters)
        LEVEL_WORDS.put(7, Arrays.asList(
            "MULTIPLAYER", "PROGRESSION", "OVERDRIVEN", "IMMERSION", "BATTLEGROUND", "SPEEDRUNNER", "ALCHEMIST",
            "BLACKSMITH", "MERCHANT", "GUARDIAN", "SENTINEL", "CHAMPION", "LEGENDARY", "EPIC", "RARE", "COMMON"
        ));
        
        // Level 8: Long (10-12 letters)
        LEVEL_WORDS.put(8, Arrays.asList(
            "CONTROLLER", "DIFFICULTY", "SIMULATION", "EXPLORATION", "TIMETRAVEL", "AGGREGATION", "STEALTHMODE",
            "NIGHTMODE", "DAYMODE", "WEATHER", "SEASONS", "DYNAMIC", "STATIC", "PROCEDURAL", "GENERATED", "RANDOMIZED"
        ));
        
        // Level 9: Long (11-13 letters)
        LEVEL_WORDS.put(9, Arrays.asList(
            "PROCEDURAL", "TRANSMISSION", "COMPETITIVE", "INNOVATION", "ADVENTURING", "STREAMSNIPING",
            "CROSSPLAY", "CROSSPLATFORM", "MULTIPLATFORM", "EXCLUSIVE", "TIMEDEXCLUSIVE", "LAUNCHTITLE", "INDIE", "AAA"
        ));
        
        // Level 10: Very Long (12+ letters) - Last word-only level
        LEVEL_WORDS.put(10, Arrays.asList(
            "HYPERREALISM", "MICROTRANSACTIONS", "CROSSPLATFORM", "REBALANCING", "METAGAMING",
            "OPTIMIZATION", "PERFORMANCE", "FRAMERATE", "RESOLUTION", "TEXTURE", "SHADER", "LIGHTING", "RENDERING"
        ));
        
        // Level 11: Short phrases (2 words)
        LEVEL_WORDS.put(11, Arrays.asList(
            "FINAL BOSS", "LEVEL UP", "SIDE QUEST", "NEW GAME", "TOP SCORE", "HARD MODE",
            "EASY MODE", "NORMAL MODE", "BOSS FIGHT", "CUT SCENE", "LOAD SCREEN", "SAVE POINT",
            "CHECK POINT", "SPAWN POINT", "RESPAWN", "GAME OVER", "CONTINUE", "RESTART", "QUIT GAME", "PAUSE MENU"
        ));
        
        // Level 12: Short phrases (2 words)
        LEVEL_WORDS.put(12, Arrays.asList(
            "SECRET AREA", "MAGIC ATTACK", "PLAYER STATS", "SPEED RUN", "DOUBLE JUMP",
            "TRIPLE JUMP", "AIR DASH", "WALL JUMP", "GRAPPLE HOOK", "HEALTH BAR", "MANA BAR", "STAMINA BAR",
            "EXPERIENCE POINTS", "SKILL POINTS", "ATTRIBUTE POINTS", "TALENT TREE", "SKILL TREE", "UPGRADE PATH"
        ));
        
        // Level 13: Medium phrases (2-3 words)
        LEVEL_WORDS.put(13, Arrays.asList(
            "ULTIMATE WEAPON", "COOPERATIVE MODE", "RANDOM ENCOUNTER", "LEGENDARY DROP",
            "RARE ITEM", "EPIC LOOT", "COMMON DROP", "UNCOMMON FIND", "LEGENDARY GEAR", "EPIC ARMOR",
            "QUEST ITEM", "KEY ITEM", "CONSUMABLE", "PERMANENT UPGRADE", "TEMPORARY BUFF", "PERMANENT BUFF"
        ));
        
        // Level 14: Medium phrases (2-3 words)
        LEVEL_WORDS.put(14, Arrays.asList(
            "CRITICAL DAMAGE", "CHARACTER CUSTOMIZATION", "POST GAME CONTENT",
            "NEW GAME PLUS", "SECOND PLAYTHROUGH", "THIRD PLAYTHROUGH", "MULTIPLE ENDINGS", "SECRET ENDING",
            "TRUE ENDING", "BAD ENDING", "GOOD ENDING", "NEUTRAL ENDING", "CHAOS ENDING", "ORDER ENDING"
        ));
        
        // Level 15: Longer phrases (3-4 words)
        LEVEL_WORDS.put(15, Arrays.asList(
            "PROCEDURALLY GENERATED LEVELS", "OPEN WORLD EXPLORATION", "COMPETITIVE MULTIPLAYER MATCH",
            "PLAYER VERSUS PLAYER", "PLAYER VERSUS ENVIRONMENT", "PLAYER VERSUS PLAYER VERSUS ENVIRONMENT",
            "MASSIVELY MULTIPLAYER ONLINE", "MASSIVELY MULTIPLAYER ONLINE ROLE PLAYING GAME", "REAL TIME STRATEGY",
            "TURN BASED STRATEGY", "REAL TIME COMBAT", "TURN BASED COMBAT", "ACTION ROLE PLAYING GAME"
        ));
        
        // Level 16: Longer phrases (3-4 words)
        LEVEL_WORDS.put(16, Arrays.asList(
            "REAL TIME STRATEGY BATTLE", "DYNAMIC WEATHER SYSTEM", "FULL CHARACTER PROGRESSION",
            "NON LINEAR STORYLINE", "BRANCHING NARRATIVE", "MULTIPLE STORY PATHS", "PLAYER CHOICE MATTERS",
            "MORALITY SYSTEM", "REPUTATION SYSTEM", "FACTION SYSTEM", "GUILD SYSTEM", "ALLIANCE SYSTEM", "TRADE SYSTEM"
        ));
        
        // Level 17: Long phrases (4-5 words)
        LEVEL_WORDS.put(17, Arrays.asList(
            "VIRTUAL REALITY COMBAT EXPERIENCE", "ASYMMETRICAL MULTIPLAYER SURVIVAL",
            "FULLY VOICED CHARACTER DIALOGUE", "MOTION CAPTURE ANIMATION SYSTEM",
            "PHYSICS BASED INTERACTION MECHANICS", "DESTRUCTIBLE ENVIRONMENT SYSTEM",
            "ADAPTIVE DIFFICULTY SCALING", "DYNAMIC ENEMY SPAWNING", "INTELLIGENT AI BEHAVIOR"
        ));
        
        // Level 18: Long phrases (4-5 words)
        LEVEL_WORDS.put(18, Arrays.asList(
            "ADVANCED ARTIFICIAL INTELLIGENCE ENEMIES", "AUTHENTIC PHYSICS BASED MECHANICS",
            "PHOTOREALISTIC GRAPHICS RENDERING ENGINE", "REALISTIC PARTICLE EFFECT SYSTEM",
            "ADVANCED SHADOW AND LIGHTING", "GLOBAL ILLUMINATION TECHNIQUES", "RAY TRACING TECHNOLOGY",
            "DYNAMIC TIME OF DAY", "SEASONAL WEATHER PATTERNS", "ECOSYSTEM SIMULATION"
        ));
        
        // Level 19: Very long phrases (5-6 words)
        LEVEL_WORDS.put(19, Arrays.asList(
            "EXTENSIVE CHARACTER ABILITY TREE", "OPEN WORLD ROLE PLAYING EXPERIENCE",
            "DEEP CRAFTING AND ENCHANTING SYSTEM", "COMPREHENSIVE QUEST AND MISSION GENERATOR",
            "ADVANCED COMBAT MECHANICS AND COMBOS", "ELABORATE STORYTELLING AND NARRATIVE DESIGN",
            "IMMERSIVE WORLD BUILDING AND LORE", "COMPLEX ECONOMY AND TRADING SYSTEMS",
            "SOPHISTICATED RESOURCE MANAGEMENT", "INTELLIGENT NPC BEHAVIOR PATTERNS"
        ));
        
        // Level 20: Maximum difficulty phrases (6+ words)
        LEVEL_WORDS.put(20, Arrays.asList(
            "PROCEDURAL OPEN WORLD GENERATION SYSTEM", "MULTIDIMENSIONAL TIME TRAVEL STORYLINE",
            "INFINITELY GENERATED QUEST AND MISSION CONTENT", "ADAPTIVE NARRATIVE THAT RESPONDS TO PLAYER ACTIONS",
            "REALISTIC PHYSICS SIMULATION WITH FULL ENVIRONMENTAL INTERACTION", "CUTTING EDGE GRAPHICS WITH RAY TRACING AND GLOBAL ILLUMINATION",
            "ADVANCED ARTIFICIAL INTELLIGENCE WITH MACHINE LEARNING CAPABILITIES", "SEAMLESS MULTIPLAYER EXPERIENCE ACROSS ALL PLATFORMS",
            "COMPREHENSIVE CHARACTER CUSTOMIZATION WITH THOUSANDS OF OPTIONS", "DEEP STRATEGIC COMBAT SYSTEM WITH COUNTLESS POSSIBILITIES"
        ));
    }
    
    /**
     * Gets a random word/phrase for a specific level.
     * 
     * @param level the level (1-20)
     * @return a random word or phrase from that level
     */
    public static String getRandomWordForLevel(int level) {
        int actualLevel = Math.max(1, Math.min(MAX_LEVEL, level));
        List<String> words = LEVEL_WORDS.get(actualLevel);
        if (words == null || words.isEmpty()) {
            // Fallback to level 1 if level not found
            words = LEVEL_WORDS.get(1);
        }
        return words.get(random.nextInt(words.size()));
    }
    
    @Override
    public WordUnscrambleType getGameType() {
        return WordUnscrambleType.WORD_UNSCRAMBLE;
    }
    
    @Override
    public WordUnscrambleSession startNewSession(String guildId) {
        // This will be called with level from WordUnscrambleService
        // For now, default to level 1 - will be updated by service
        String word = getRandomWordForLevel(1);
        WordUnscrambleSession session = new WordUnscrambleSession(guildId, WordUnscrambleType.WORD_UNSCRAMBLE, Instant.now(), word);
        logger.info("Started Word Unscramble session for guild {} - word: {}", guildId, word);
        return session;
    }
    
    /**
     * Starts a new session with a specific level.
     * 
     * @param guildId the guild ID
     * @param level the level (1-20)
     * @return the new game session
     */
    public WordUnscrambleSession startNewSession(String guildId, int level) {
        String word = getRandomWordForLevel(level);
        WordUnscrambleSession session = new WordUnscrambleSession(guildId, WordUnscrambleType.WORD_UNSCRAMBLE, Instant.now(), word);
        logger.info("Started Word Unscramble session for guild {} at level {} - word: {}", guildId, level, word);
        return session;
    }
    
    @Override
    public WordUnscrambleResult handleAttempt(WordUnscrambleSession session, String userId, String username, String input) {
        if (!session.isActive()) {
            return new WordUnscrambleResult(userId, username, input, 0, false, Instant.now());
        }
        
        // Check if user already guessed correctly
        boolean alreadyWon = session.getResults().stream()
                .anyMatch(r -> r.getUserId().equals(userId) && r.isCorrect());
        
        if (alreadyWon) {
            return new WordUnscrambleResult(userId, username, input, 0, false, Instant.now());
        }
        
        // Check if answer is correct (normalize both for comparison)
        String correctAnswer = session.getCorrectAnswer();
        String normalizedInput = normalizeAnswer(input);
        String normalizedCorrect = normalizeAnswer(correctAnswer);
        boolean isCorrect = normalizedInput.equals(normalizedCorrect);
        
        // Calculate score based on time (earlier = higher score)
        int score = 0;
        if (isCorrect) {
            long secondsSinceStart = Instant.now().getEpochSecond() - session.getStartTime().getEpochSecond();
            score = Math.max(100, 1000 - (int) secondsSinceStart);
        }
        
        WordUnscrambleResult result = new WordUnscrambleResult(userId, username, input, score, isCorrect, Instant.now());
        session.addResult(result);
        
        logger.info("Word Unscramble attempt by {} in guild {}: {} - {}",
                username, session.getGuildId(), input, isCorrect ? "CORRECT" : "INCORRECT");
        
        return result;
    }
    
    @Override
    public String generateAnnouncement(WordUnscrambleSession session) {
        return generateAnnouncement(session, 1); // Default level 1
    }
    
    /**
     * Generates announcement with level information.
     * 
     * @param session the game session
     * @param level the current level
     * @return the announcement message
     */
    public String generateAnnouncement(WordUnscrambleSession session, int level) {
        String word = session.getCorrectAnswer();
        String scrambled = scrambleWordOrPhrase(word);
        boolean isPhrase = word.contains(" ");
        
        String contentType = isPhrase ? "phrase" : "word";
        int length = word.replaceAll(" ", "").length(); // Total character count
        
        return String.format(
                "⏰ **It's that time again!** ⏰\n\n" +
                "🔤 **New Unscramble Challenge!**\n\n" +
                "**Level %d** | Unscramble this %s: **%s** (%d letters)\n\n" +
                "Use `/scramble-guess` to submit your answer!\n" +
                "First correct player wins! 🏆",
                level,
                contentType,
                scrambled,
                length
        );
    }
    
    @Override
    public void resetSession(WordUnscrambleSession session) {
        session.setActive(false);
        logger.info("Reset Word Unscramble session for guild {}", session.getGuildId());
    }
    
    /**
     * Scrambles a word or phrase.
     * For phrases (levels 10+), scrambles each word independently (Option B).
     * 
     * @param wordOrPhrase the word or phrase to scramble
     * @return the scrambled word or phrase
     */
    private String scrambleWordOrPhrase(String wordOrPhrase) {
        if (wordOrPhrase.contains(" ")) {
            // Phrase: scramble each word independently
            String[] words = wordOrPhrase.split(" ");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                if (i > 0) {
                    result.append(" ");
                }
                result.append(scrambleWord(words[i]));
            }
            return result.toString();
        } else {
            // Single word: scramble normally
            return scrambleWord(wordOrPhrase);
        }
    }
    
    /**
     * Scrambles a single word by shuffling its letters.
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
    
    /**
     * Normalizes an answer for comparison (uppercase, trim, normalize spaces).
     * 
     * @param answer the answer to normalize
     * @return the normalized answer
     */
    private String normalizeAnswer(String answer) {
        return answer.trim().toUpperCase().replaceAll("\\s+", " ");
    }
}

