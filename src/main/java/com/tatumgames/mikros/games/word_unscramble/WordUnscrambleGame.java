package com.tatumgames.mikros.games.word_unscramble;

import com.tatumgames.mikros.games.word_unscramble.interfaces.WordUnscrambleInterface;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleResult;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleSession;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleType;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleUsedWordTracker;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Word unscramble game implementation with 20-level progression system. Players guess the correct
 * unscrambled word or phrase. Levels 1-9: Words only Levels 10-14: Short phrases (2-3 words) Levels
 * 15-20: Longer phrases
 */
public class WordUnscrambleGame implements WordUnscrambleInterface {
  private static final Logger logger = LoggerFactory.getLogger(WordUnscrambleGame.class);
  private static final Random random = new Random();
  private static final int MAX_LEVEL = 20;

  // 20 levels of words and phrases
  private static final Map<Integer, List<String>> LEVEL_WORDS = new HashMap<>();

  static {
    // Level 1: Easy (4-5 letters)
    LEVEL_WORDS.put(
        1,
        Arrays.asList(
            "PLAY", "GAME", "TEAM", "LOOT", "BOSS", "QUEST", "BONUS", "LEVEL", "SCORE", "SKILL",
            "ROLE", "DICE", "CARD", "MODE", "MAP", "ZONE", "ITEM", "GOLD", "COIN", "RANK", "FARM",
            "RAID", "PVP", "PVE", "HEAL", "BUFF", "DEBUFF", "TANK", "DPS", "CAST", "HIT", "MISS",
            "CRIT", "DODGE", "BLOCK", "PARRY", "STUN", "SLOW", "FAST", "WEAK", "STRONG", "SELL"));

    // Level 2: Easy (5-6 letters)
    LEVEL_WORDS.put(
        2,
        Arrays.asList(
            "ARENA", "POWER", "MAGIC", "SPEED", "GRIND", "PARTY", "WEAPON", "ARMOR", "HEROES",
            "COMBO", "SPELL", "BUILD", "CLASS", "GUILD", "TRADE", "CRAFT", "FORGE", "MERGE",
            "SWORD", "SHIELD", "BOW", "STAFF", "WAND", "AXE", "MACE", "DAGGER", "SPEAR", "HAMMER",
            "HELMET", "CHEST", "BOOTS", "GLOVES", "PANTS", "RING", "AMULET", "BRACER", "BELT",
            "CAPE", "POTION", "ELIXIR", "SCROLL", "GEM", "RUNE", "TOKEN", "KEY", "DROPS",
            "MIKROS"));

    // Level 3: Easy-Medium (6-7 letters)
    LEVEL_WORDS.put(
        3,
        Arrays.asList(
            "ACTION",
            "PLAYER",
            "TARGET",
            "DAMAGE",
            "ATTACK",
            "HEALTH",
            "RANKED",
            "MODELS",
            "COINS",
            "BOOST",
            "ENCHANT",
            "BATTLE",
            "COMBAT",
            "FIGHT",
            "STRIKE",
            "SLASH",
            "PIERCE",
            "CRUSH",
            "BURN",
            "FREEZE",
            "SHOCK",
            "HEALER",
            "TANKER",
            "DAMAGER",
            "SUPPORT",
            "BUFFER",
            "DEBUFFER",
            "CONTROL",
            "AOE",
            "SINGLE",
            "LEGEND",
            "EPIC",
            "RARE",
            "COMMON",
            "UNIQUE",
            "MYTHIC",
            "ARTIFACT",
            "RELIC",
            "TREASURE",
            "HOARD",
            "HORDE"));

    // Level 4: Medium (7-8 letters)
    LEVEL_WORDS.put(
        4,
        Arrays.asList(
            "MISSION",
            "ENEMIES",
            "PORTAL",
            "FANTASY",
            "REWARD",
            "JOURNEY",
            "ROGUELIKE",
            "SANDBOX",
            "VICTORY",
            "UPGRADE",
            "DUNGEON",
            "DRAGON",
            "WIZARD",
            "KNIGHT",
            "RANGER",
            "PRIEST",
            "MONK",
            "ROGUE",
            "MAGE",
            "WARRIOR"));

    // Level 5: Medium (8-9 letters)
    LEVEL_WORDS.put(
        5,
        Arrays.asList(
            "SURVIVAL",
            "MULTIPLE",
            "GRAPHICS",
            "CREATIVE",
            "ADVENTURE",
            "PLATFORM",
            "CHALLENGE",
            "WARRIORS",
            "TACTICAL",
            "STRATEGY",
            "CAMPAIGN",
            "DEFENSE",
            "OFFENSE",
            "POWER",
            "ABILITY"));

    // Level 6: Medium-Long (8-10 letters)
    LEVEL_WORDS.put(
        6,
        Arrays.asList(
            "CHARACTER",
            "INVENTORY",
            "SOUNDTRACK",
            "ENCHANTING",
            "MINIMAP",
            "CHECKPOINT",
            "PLAYTHROUGH",
            "NARRATIVE",
            "STORYLINE",
            "PROTAGONIST",
            "ANTAGONIST",
            "QUESTLINE",
            "SIDEMISSION",
            "MAINQUEST",
            "SIDEQUEST",
            "EQUIPMENT",
            "WEAPONRY",
            "ACCESSORY",
            "CONSUMABLE",
            "MATERIAL",
            "RESOURCE",
            "CURRENCY"));

    // Level 7: Long (9-11 letters)
    LEVEL_WORDS.put(
        7,
        Arrays.asList(
            "MULTIPLAYER",
            "PROGRESSION",
            "OVERDRIVEN",
            "IMMERSION",
            "BATTLEGROUND",
            "SPEEDRUNNER",
            "ALCHEMIST",
            "BLACKSMITH",
            "MERCHANT",
            "GUARDIAN",
            "SENTINEL",
            "CHAMPION",
            "LEGENDARY",
            "ADVENTURER",
            "EXPLORER",
            "HOARDER",
            "COLLECTOR",
            "GATHERER",
            "HUNTER",
            "TRACKER",
            "SURVIVOR",
            "VETERAN",
            "MASTER",
            "EXPERT",
            "NOVICE",
            "BEGINNER",
            "INTERMEDIATE",
            "ADVANCED"));

    // Level 8: Long (10-12 letters)
    LEVEL_WORDS.put(
        8,
        Arrays.asList(
            "CONTROLLER",
            "DIFFICULTY",
            "SIMULATION",
            "EXPLORATION",
            "TIMETRAVEL",
            "AGGREGATION",
            "STEALTHMODE",
            "NIGHTMODE",
            "DAYMODE",
            "WEATHER",
            "SEASONS",
            "DYNAMIC",
            "STATIC",
            "PROCEDURAL",
            "GENERATED",
            "RANDOMIZED"));

    // Level 9: Long (11-13 letters)
    LEVEL_WORDS.put(
        9,
        Arrays.asList(
            "TRANSMISSION",
            "COMPETITIVE",
            "INNOVATION",
            "ADVENTURING",
            "STREAMSNIPING",
            "CROSSPLAY",
            "MULTIPLATFORM",
            "EXCLUSIVE",
            "TIMEDEXCLUSIVE",
            "LAUNCHTITLE",
            "INDIE",
            "AAA",
            "CUSTOMIZATION",
            "PERSONALIZATION",
            "SPECIALIZATION",
            "OPTIMIZATION",
            "MAXIMIZATION",
            "MINIMIZATION",
            "ENHANCEMENT",
            "IMPROVEMENT",
            "DEVELOPMENT",
            "EVOLUTION",
            "ADAPTATION",
            "TRANSFORMATION",
            "REVOLUTION"));

    // Level 10: Very Long (12+ letters) - Last word-only level
    LEVEL_WORDS.put(
        10,
        Arrays.asList(
            "HYPERREALISM",
            "MICROTRANSACTIONS",
            "CROSSPLATFORM",
            "REBALANCING",
            "METAGAMING",
            "PERFORMANCE",
            "FRAMERATE",
            "RESOLUTION",
            "TEXTURE",
            "SHADER",
            "LIGHTING",
            "RENDERING"));

    // Level 11: Short phrases (2 words)
    LEVEL_WORDS.put(
        11,
        Arrays.asList(
            "FINAL BOSS",
            "LEVEL UP",
            "SIDE QUEST",
            "NEW GAME",
            "TOP SCORE",
            "HARD MODE",
            "EASY MODE",
            "NORMAL MODE",
            "BOSS FIGHT",
            "CUT SCENE",
            "LOAD SCREEN",
            "SAVE POINT",
            "CHECK POINT",
            "SPAWN POINT",
            "RESPAWN",
            "GAME OVER",
            "CONTINUE",
            "RESTART",
            "QUIT GAME",
            "PAUSE MENU",
            "TATUM GAMES",
            "TATUM TECH",
            "DPS RATE"));

    // Level 12: Short phrases (2 words)
    LEVEL_WORDS.put(
        12,
        Arrays.asList(
            "SECRET AREA",
            "MAGIC ATTACK",
            "PLAYER STATS",
            "SPEED RUN",
            "DOUBLE JUMP",
            "TRIPLE JUMP",
            "AIR DASH",
            "WALL JUMP",
            "GRAPPLE HOOK",
            "HEALTH BAR",
            "MANA BAR",
            "STAMINA BAR",
            "EXPERIENCE POINTS",
            "SKILL POINTS",
            "ATTRIBUTE POINTS",
            "TALENT TREE",
            "SKILL TREE",
            "UPGRADE PATH",
            "OPEN WORLD MAP",
            "LOOT DROP RATE"));

    // Level 13: Medium phrases (2-3 words)
    LEVEL_WORDS.put(
        13,
        Arrays.asList(
            "ULTIMATE WEAPON",
            "COOPERATIVE MODE",
            "RANDOM ENCOUNTER",
            "LEGENDARY DROP",
            "RARE ITEM",
            "EPIC LOOT",
            "COMMON DROP",
            "UNCOMMON FIND",
            "LEGENDARY GEAR",
            "EPIC ARMOR",
            "QUEST ITEM",
            "KEY ITEM",
            "CONSUMABLE",
            "PERMANENT UPGRADE",
            "TEMPORARY BUFF",
            "PERMANENT BUFF",
            "LOOKING FOR GROUP",
            "LOOKING FOR TANK",
            "LOOKING FOR HEALER",
            "FINAL BOSS FIGHT",
            "GAME OVER SCREEN",
            "PRESS START BUTTON",
            "PLAYER ONE READY"));

    // Level 14: Medium phrases (2-3 words)
    LEVEL_WORDS.put(
        14,
        Arrays.asList(
            "CRITICAL DAMAGE",
            "CHARACTER CUSTOMIZATION",
            "POST GAME CONTENT",
            "NEW GAME PLUS",
            "SECOND PLAYTHROUGH",
            "THIRD PLAYTHROUGH",
            "MULTIPLE ENDINGS",
            "SECRET ENDING",
            "TRUE ENDING",
            "BAD ENDING",
            "GOOD ENDING",
            "NEUTRAL ENDING",
            "CHAOS ENDING",
            "ORDER ENDING"));

    // Level 15: Longer phrases (3-4 words)
    LEVEL_WORDS.put(
        15,
        Arrays.asList(
            "PROCEDURALLY GENERATED LEVELS",
            "OPEN WORLD EXPLORATION",
            "COMPETITIVE MULTIPLAYER MATCH",
            "PLAYER VERSUS PLAYER",
            "PLAYER VERSUS ENVIRONMENT",
            "PLAYER VERSUS PLAYER VERSUS ENVIRONMENT",
            "MASSIVELY MULTIPLAYER ONLINE",
            "MASSIVELY MULTIPLAYER ONLINE ROLE PLAYING GAME",
            "REAL TIME STRATEGY",
            "TURN BASED STRATEGY",
            "REAL TIME COMBAT",
            "TURN BASED COMBAT",
            "ACTION ROLE PLAYING GAME",
            "I NEED HEALING",
            "DPS CHECK PHASE",
            "HARD MODE UNLOCKED",
            "PATCH NOTES RELEASED",
            "NERF THIS CHARACTER",
            "BUFF THAT SKILL",
            "RANDOM NUMBER GENERATOR",
            "EARLY ACCESS RELEASE",
            "PROCEDURALLY GENERATED DUNGEONS",
            "PERMADEATH IRONMAN MODE"));

    // Level 16: Longer phrases (3-4 words)
    LEVEL_WORDS.put(
        16,
        Arrays.asList(
            "REAL TIME STRATEGY BATTLE",
            "DYNAMIC WEATHER SYSTEM",
            "FULL CHARACTER PROGRESSION",
            "NON LINEAR STORYLINE",
            "BRANCHING NARRATIVE",
            "MULTIPLE STORY PATHS",
            "PLAYER CHOICE MATTERS",
            "MORALITY SYSTEM",
            "REPUTATION SYSTEM",
            "FACTION SYSTEM",
            "GUILD SYSTEM",
            "ALLIANCE SYSTEM",
            "TRADE SYSTEM"));

    // Level 17: Long phrases (4-5 words)
    LEVEL_WORDS.put(
        17,
        Arrays.asList(
            "VIRTUAL REALITY COMBAT EXPERIENCE",
            "ASYMMETRICAL MULTIPLAYER SURVIVAL",
            "FULLY VOICED CHARACTER DIALOGUE",
            "MOTION CAPTURE ANIMATION SYSTEM",
            "PHYSICS BASED INTERACTION MECHANICS",
            "DESTRUCTIBLE ENVIRONMENT SYSTEM",
            "ADAPTIVE DIFFICULTY SCALING",
            "DYNAMIC ENEMY SPAWNING",
            "INTELLIGENT AI BEHAVIOR",
            "YOU DIED TRY AGAIN",
            "ONE MORE TURN SYNDROME",
            "PLAYER VERSUS PLAYER MODE",
            "MASSIVELY MULTIPLAYER ONLINE ROLEPLAYING",
            "BALANCE PATCH BROKE EVERYTHING"));

    // Level 18: Long phrases (4-5 words)
    LEVEL_WORDS.put(
        18,
        Arrays.asList(
            "ADVANCED ARTIFICIAL INTELLIGENCE ENEMIES",
            "AUTHENTIC PHYSICS BASED MECHANICS",
            "PHOTOREALISTIC GRAPHICS RENDERING ENGINE",
            "REALISTIC PARTICLE EFFECT SYSTEM",
            "ADVANCED SHADOW AND LIGHTING",
            "GLOBAL ILLUMINATION TECHNIQUES",
            "RAY TRACING TECHNOLOGY",
            "DYNAMIC TIME OF DAY",
            "SEASONAL WEATHER PATTERNS",
            "ECOSYSTEM SIMULATION"));

    // Level 19: Very long phrases (5-6 words)
    LEVEL_WORDS.put(
        19,
        Arrays.asList(
            "EXTENSIVE CHARACTER ABILITY TREE",
            "OPEN WORLD ROLE PLAYING EXPERIENCE",
            "DEEP CRAFTING AND ENCHANTING SYSTEM",
            "COMPREHENSIVE QUEST AND MISSION GENERATOR",
            "ADVANCED COMBAT MECHANICS AND COMBOS",
            "ELABORATE STORYTELLING AND NARRATIVE DESIGN",
            "IMMERSIVE WORLD BUILDING AND LORE",
            "COMPLEX ECONOMY AND TRADING SYSTEMS",
            "SOPHISTICATED RESOURCE MANAGEMENT",
            "INTELLIGENT NPC BEHAVIOR PATTERNS",
            "LOOKING FOR RAID GROUP TONIGHT",
            "THIS GAME NEEDS BETTER MATCHMAKING",
            "META DEFINES THE WHOLE GAME",
            "ADAPTIVE NARRATIVE RESPONDS TO PLAYER CHOICES",
            "ALL YOUR BASES BELONG TO US"));

    // Level 20: Maximum difficulty phrases (6+ words)
    LEVEL_WORDS.put(
        20,
        Arrays.asList(
            "PROCEDURAL OPEN WORLD GENERATION SYSTEM",
            "MULTIDIMENSIONAL TIME TRAVEL STORYLINE",
            "INFINITELY GENERATED QUEST AND MISSION CONTENT",
            "ADAPTIVE NARRATIVE THAT RESPONDS TO PLAYER ACTIONS",
            "REALISTIC PHYSICS SIMULATION WITH FULL ENVIRONMENTAL INTERACTION",
            "CUTTING EDGE GRAPHICS WITH RAY TRACING AND GLOBAL ILLUMINATION",
            "ADVANCED ARTIFICIAL INTELLIGENCE WITH MACHINE LEARNING CAPABILITIES",
            "SEAMLESS MULTIPLAYER EXPERIENCE ACROSS ALL PLATFORMS",
            "COMPREHENSIVE CHARACTER CUSTOMIZATION WITH THOUSANDS OF OPTIONS",
            "DEEP STRATEGIC COMBAT SYSTEM WITH COUNTLESS POSSIBILITIES",
            "ALL YOUR BASE ARE BELONG TO US NOW",
            "REALISTIC PHYSICS SIMULATION WITH ENVIRONMENTAL INTERACTION",
            "MASSIVE OPEN WORLD WITH SEAMLESS MULTIPLAYER INTEGRATION",
            "FULLY PROCEDURAL CONTENT WITH DYNAMIC STORY OUTCOMES"));
  }

  /**
   * Gets a random word/phrase for a specific level.
   *
   * @param level the level (1-20)
   * @return a random word or phrase from that level
   */
  public static String getRandomWordForLevel(int level) {
    return getRandomWordForLevel(level, null, null);
  }

  /**
   * Gets a random word/phrase for a specific level, with optional word rotation tracking.
   *
   * @param level the level (1-20)
   * @param guildId the guild ID (for word rotation, can be null)
   * @param wordTracker the word tracker (for levels 1-5, can be null)
   * @return a random word or phrase from that level
   */
  public static String getRandomWordForLevel(
      int level, String guildId, WordUnscrambleUsedWordTracker wordTracker) {
    int actualLevel = Math.max(1, Math.min(MAX_LEVEL, level));
    List<String> words = LEVEL_WORDS.get(actualLevel);
    if (words == null || words.isEmpty()) {
      // Fallback to level 1 if level not found
      words = LEVEL_WORDS.get(1);
    }

    // For levels 1-5, filter out recently used words
    if (actualLevel >= 1 && actualLevel <= 5 && guildId != null && wordTracker != null) {
      words = wordTracker.filterRecentlyUsedWords(guildId, actualLevel, words);
    }

    if (words.isEmpty()) {
      // Fallback to level 1 if filtered list is empty
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
    WordUnscrambleSession session =
        new WordUnscrambleSession(guildId, WordUnscrambleType.WORD_UNSCRAMBLE, Instant.now(), word);
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
    return startNewSession(guildId, level, null);
  }

  /**
   * Starts a new session with a specific level and word tracker.
   *
   * @param guildId the guild ID
   * @param level the level (1-20)
   * @param wordTracker the word tracker for rotation (can be null)
   * @return the new game session
   */
  public WordUnscrambleSession startNewSession(
      String guildId, int level, WordUnscrambleUsedWordTracker wordTracker) {
    String word = getRandomWordForLevel(level, guildId, wordTracker);
    WordUnscrambleSession session =
        new WordUnscrambleSession(
            guildId, WordUnscrambleType.WORD_UNSCRAMBLE, Instant.now(), word, level);
    logger.info(
        "Started Word Unscramble session for guild {} at level {} - word: {}",
        guildId,
        level,
        word);
    return session;
  }

  @Override
  public WordUnscrambleResult handleAttempt(
      WordUnscrambleSession session, String userId, String username, String input) {
    if (!session.isActive()) {
      return new WordUnscrambleResult(userId, username, input, 0, false, Instant.now());
    }

    // Check if user already guessed correctly
    boolean alreadyWon =
        session.getResults().stream().anyMatch(r -> r.userId().equals(userId) && r.isCorrect());

    if (alreadyWon) {
      return new WordUnscrambleResult(userId, username, input, 0, false, Instant.now());
    }

    // Check if user has already made 3 incorrect guesses for this word
    long incorrectGuesses =
        session.getResults().stream()
            .filter(r -> r.userId().equals(userId) && !r.isCorrect())
            .count();

    if (incorrectGuesses >= 3) {
      // User has used all 3 incorrect guesses - don't process this attempt
      logger.info(
          "User {} in guild {} attempted to guess after using all 3 incorrect guesses",
          username,
          session.getGuildId());
      return null; // Return null to indicate limit reached
    }

    // Check if answer is correct (normalize both for comparison)
    String correctAnswer = session.getCorrectAnswer();
    String normalizedInput = normalizeAnswer(input);
    String normalizedCorrect = normalizeAnswer(correctAnswer);
    boolean isCorrect = normalizedInput.equals(normalizedCorrect);

    // Calculate base score based on time (earlier = higher score)
    int baseScore = 0;
    int bonus = 0;
    int firstSolverBonus = 0;

    if (isCorrect) {
      long secondsSinceStart =
          Instant.now().getEpochSecond() - session.getStartTime().getEpochSecond();
      baseScore = Math.max(100, 1000 - (int) secondsSinceStart);

      // Check if this is the first solver
      boolean isFirstSolver = session.getResults().stream().noneMatch(r -> r.isCorrect());

      // Scaled first solver bonus based on level
      if (isFirstSolver) {
        int level = session.getLevel();
        if (level >= 1 && level <= 5) {
          firstSolverBonus = 50;
        } else if (level >= 6 && level <= 10) {
          firstSolverBonus = 100;
        } else if (level >= 11 && level <= 14) {
          firstSolverBonus = 150;
        } else if (level >= 15 && level <= 20) {
          firstSolverBonus = 200;
        }
      }

      // Calculate bonus based on wrong guesses from OTHER users before this correct answer
      // Only count wrong guesses from different users (not the current user's own wrong guesses)
      long wrongGuessesBefore =
          session.getResults().stream()
              .filter(r -> !r.isCorrect() && !r.userId().equals(userId))
              .count();

      // Diminishing returns bonus calculation:
      // - First 5 wrong guesses: 15 points each
      // - Next 10 wrong guesses: 10 points each
      // - After that: 5 points each
      // - Cap at 250 points
      if (wrongGuessesBefore > 0) {
        long firstTier = Math.min(5, wrongGuessesBefore);
        long secondTier = Math.min(10, Math.max(0, wrongGuessesBefore - 5));
        long thirdTier = Math.max(0, wrongGuessesBefore - 15);

        bonus = (int) ((firstTier * 15) + (secondTier * 10) + (thirdTier * 5));
        bonus = Math.min(250, bonus); // Cap at 250
      }

      // Apply level multiplier to base score
      int level = session.getLevel();
      double levelMultiplier = getLevelMultiplier(level);
      baseScore = (int) (baseScore * levelMultiplier);
    }

    int totalScore = baseScore + bonus + firstSolverBonus;
    WordUnscrambleResult result =
        new WordUnscrambleResult(
            userId,
            username,
            input,
            totalScore,
            bonus + firstSolverBonus,
            isCorrect,
            Instant.now());
    session.addResult(result);

    logger.info(
        "Word Unscramble attempt by {} in guild {}: {} - {}",
        username,
        session.getGuildId(),
        input,
        isCorrect ? "CORRECT" : "INCORRECT");

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
    String scrambled = scrambleWordOrPhrase(word, level);
    boolean isPhrase = word.contains(" ");

    String contentType = isPhrase ? "phrase" : "word";
    int length = word.replaceAll(" ", "").length(); // Total character count

    // For levels 6+, add hints instead of showing full scrambled word prominently
    if (level >= 6) {
      String hint = generateHint(word, level);
      return String.format(
          "⏰ **It's that time again!** ⏰\n\n"
              + "🔤 **New Unscramble Challenge!**\n\n"
              + "**Level %d** | Unscramble this %s: **%s** (%d letters)\n\n"
              + "💡 **Hint:** %s\n\n"
              + "Use `/scramble-guess` to submit your answer!\n"
              + "First correct player wins! 🏆",
          level, contentType, scrambled, length, hint);
    } else {
      return String.format(
          "⏰ **It's that time again!** ⏰\n\n"
              + "🔤 **New Unscramble Challenge!**\n\n"
              + "**Level %d** | Unscramble this %s: **%s** (%d letters)\n\n"
              + "Use `/scramble-guess` to submit your answer!\n"
              + "First correct player wins! 🏆",
          level, contentType, scrambled, length);
    }
  }

  /**
   * Generates a hint for a word/phrase. Randomly selects from: first letter, last letter, word
   * length, vowels, or category.
   *
   * @param word the word/phrase
   * @param level the level
   * @return a hint string
   */
  public String generateHintForWord(String word, int level) {
    return generateHint(word, level);
  }

  /**
   * Generates a hint for a word/phrase (for levels 6+). Randomly selects from: first letter, last
   * letter, word length, vowels, or category.
   *
   * @param word the word/phrase
   * @param level the level
   * @return a hint string
   */
  private String generateHint(String word, int level) {
    // Remove spaces for letter operations
    String wordNoSpaces = word.replaceAll(" ", "");
    int hintType = random.nextInt(5); // 0-4

    switch (hintType) {
      case 0:
        // First letter
        return String.format("Starts with **%s**", wordNoSpaces.charAt(0));
      case 1:
        // Last letter
        return String.format("Ends with **%s**", wordNoSpaces.charAt(wordNoSpaces.length() - 1));
      case 2:
        // Word length
        if (word.contains(" ")) {
          int wordCount = word.split(" ").length;
          return String.format("Contains **%d word%s**", wordCount, wordCount > 1 ? "s" : "");
        } else {
          return String.format("**%d letters** long", wordNoSpaces.length());
        }
      case 3:
        // Vowel positions
        return generateVowelHint(wordNoSpaces);
      case 4:
      default:
        // Category hint
        return generateCategoryHint(word, level);
    }
  }

  /**
   * Generates a hint showing vowel positions.
   *
   * @param wordNoSpaces the word without spaces
   * @return a vowel hint
   */
  private String generateVowelHint(String wordNoSpaces) {
    StringBuilder vowels = new StringBuilder();
    for (char c : wordNoSpaces.toCharArray()) {
      if ("AEIOU".indexOf(Character.toUpperCase(c)) >= 0) {
        if (vowels.length() > 0) {
          vowels.append(", ");
        }
        vowels.append(c);
      }
    }

    if (vowels.length() > 0) {
      return String.format("Contains vowels: **%s**", vowels.toString());
    } else {
      return "Contains **no vowels**";
    }
  }

  /**
   * Generates a category-based hint.
   *
   * @param word the word/phrase
   * @param level the level
   * @return a category hint
   */
  private String generateCategoryHint(String word, int level) {
    String lowerWord = word.toLowerCase();

    // Game-related categories
    if (lowerWord.contains("boss") || lowerWord.contains("enemy") || lowerWord.contains("dragon")) {
      return "Related to **enemies or bosses**";
    } else if (lowerWord.contains("weapon")
        || lowerWord.contains("sword")
        || lowerWord.contains("armor")) {
      return "Related to **equipment or weapons**";
    } else if (lowerWord.contains("quest")
        || lowerWord.contains("mission")
        || lowerWord.contains("story")) {
      return "Related to **quests or story**";
    } else if (lowerWord.contains("player")
        || lowerWord.contains("character")
        || lowerWord.contains("class")) {
      return "Related to **characters or classes**";
    } else if (lowerWord.contains("skill")
        || lowerWord.contains("ability")
        || lowerWord.contains("spell")) {
      return "Related to **skills or abilities**";
    } else if (lowerWord.contains("game")
        || lowerWord.contains("mode")
        || lowerWord.contains("play")) {
      return "Related to **gameplay or modes**";
    } else if (lowerWord.contains("level")
        || lowerWord.contains("dungeon")
        || lowerWord.contains("zone")) {
      return "Related to **locations or levels**";
    } else if (lowerWord.contains("item")
        || lowerWord.contains("loot")
        || lowerWord.contains("treasure")) {
      return "Related to **items or loot**";
    } else {
      return "Gaming-related term";
    }
  }

  @Override
  public void resetSession(WordUnscrambleSession session) {
    session.setActive(false);
    logger.info("Reset Word Unscramble session for guild {}", session.getGuildId());
  }

  /**
   * Scrambles a word or phrase with level-aware scrambling.
   *
   * @param wordOrPhrase the word or phrase to scramble
   * @param level the current level
   * @return the scrambled word or phrase
   */
  private String scrambleWordOrPhrase(String wordOrPhrase, int level) {
    if (wordOrPhrase.contains(" ")) {
      // Phrase: for levels 6+, also shuffle word order
      String[] words = wordOrPhrase.split(" ");
      if (level >= 6) {
        // Shuffle word order for higher levels
        List<String> wordList = new ArrayList<>(Arrays.asList(words));
        Collections.shuffle(wordList, random);
        words = wordList.toArray(new String[0]);
      }

      StringBuilder result = new StringBuilder();
      for (int i = 0; i < words.length; i++) {
        if (i > 0) {
          result.append(" ");
        }
        result.append(scrambleWord(words[i], level));
      }
      return result.toString();
    } else {
      // Single word: scramble with level-aware algorithm
      return scrambleWord(wordOrPhrase, level);
    }
  }

  /**
   * Scrambles a single word with level-aware algorithm. For levels 6+, uses enhanced scrambling
   * with minimum displacement.
   *
   * @param word the word to scramble
   * @param level the current level
   * @return the scrambled word
   */
  private String scrambleWord(String word, int level) {
    if (level < 6) {
      // Simple shuffle for lower levels
      return simpleScramble(word);
    } else {
      // Enhanced scrambling for higher levels
      return enhancedScramble(word);
    }
  }

  /**
   * Simple scrambling algorithm (levels 1-5).
   *
   * @param word the word to scramble
   * @return the scrambled word
   */
  private String simpleScramble(String word) {
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
   * Enhanced scrambling algorithm for levels 6+. Ensures minimum character displacement and uses
   * multiple shuffle passes.
   *
   * @param word the word to scramble
   * @return the scrambled word
   */
  private String enhancedScramble(String word) {
    if (word.length() <= 2) {
      return simpleScramble(word); // Too short for enhanced scrambling
    }

    char[] chars = word.toCharArray();
    int length = chars.length;

    // Multiple shuffle passes for better randomization
    for (int pass = 0; pass < 3; pass++) {
      // Fisher-Yates shuffle with minimum displacement check
      for (int i = length - 1; i > 0; i--) {
        int j = random.nextInt(i + 1);
        // Ensure minimum displacement (at least 2 positions away for longer words)
        if (length > 4 && Math.abs(i - j) < 2) {
          // Find a better swap candidate
          int attempts = 0;
          while (Math.abs(i - j) < 2 && attempts < 10) {
            j = random.nextInt(i + 1);
            attempts++;
          }
        }
        // Swap
        char temp = chars[i];
        chars[i] = chars[j];
        chars[j] = temp;
      }
    }

    String scrambled = new String(chars);

    // Ensure it's different from original
    int attempts = 0;
    while (scrambled.equals(word) && attempts < 20) {
      // Additional shuffle if same as original
      List<Character> charList = new ArrayList<>();
      for (char c : chars) {
        charList.add(c);
      }
      Collections.shuffle(charList, random);
      StringBuilder sb = new StringBuilder();
      for (char c : charList) {
        sb.append(c);
      }
      scrambled = sb.toString();
      attempts++;
    }

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

  /**
   * Gets the level multiplier for scoring.
   *
   * @param level the level (1-20)
   * @return the multiplier
   */
  private double getLevelMultiplier(int level) {
    if (level >= 1 && level <= 5) {
      return 1.0;
    } else if (level >= 6 && level <= 10) {
      return 1.2;
    } else if (level >= 11 && level <= 14) {
      return 1.5;
    } else if (level >= 15 && level <= 20) {
      return 2.0;
    }
    return 1.0; // Default
  }
}
