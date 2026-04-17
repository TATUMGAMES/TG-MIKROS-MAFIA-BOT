package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.blessing.Blessing;
import com.tatumgames.mikros.games.rpg.blessing.BlessingNarratives;
import com.tatumgames.mikros.games.rpg.blessing.BlessingType;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing blessings granted during boss battles. Blessings provide temporary stat
 * boosts when players fail to defeat bosses repeatedly.
 */
public class BlessingService {
  private static final Logger logger = LoggerFactory.getLogger(BlessingService.class);

  // Map: guildId -> active blessing
  private final Map<String, Blessing> activeBlessings;

  public BlessingService() {
    this.activeBlessings = new ConcurrentHashMap<>();
  }

  /**
   * Checks if a blessing should be granted based on consecutive failures and grants it.
   *
   * @param guildId the guild ID
   * @param consecutiveFailures the number of consecutive boss failures
   * @return the granted blessing, or null if no blessing should be granted
   */
  public Blessing checkAndGrantBlessing(String guildId, int consecutiveFailures) {
    BlessingType type = BlessingType.forFailures(consecutiveFailures);

    if (type == null) {
      // Not enough failures for any blessing
      return null;
    }

    // Check if we already have a blessing of this tier or higher
    Blessing existing = activeBlessings.get(guildId);
    if (existing != null) {
      // Only upgrade if new tier is higher
      if (type.ordinal() > existing.getType().ordinal()) {
        logger.info(
            "Upgrading blessing from {} to {} for guild {} ({} failures)",
            existing.getType().getDisplayName(),
            type.getDisplayName(),
            guildId,
            consecutiveFailures);
      } else {
        // Already have equal or higher tier blessing
        return existing;
      }
    }

    // Grant blessing (we'll need to determine class, but for now grant a generic one)
    // Note: Blessings are class-specific, but we grant them per guild
    // Each character will get class-specific effects when they attack
    Blessing blessing = createBlessingForTier(type);
    activeBlessings.put(guildId, blessing);

    logger.info(
        "Granted {} to guild {} after {} consecutive failures",
        type.getDisplayName(),
        guildId,
        consecutiveFailures);

    return blessing;
  }

  /**
   * Gets the active blessing for a guild.
   *
   * @param guildId the guild ID
   * @return the active blessing, or null if none
   */
  public Blessing getActiveBlessing(String guildId) {
    return activeBlessings.get(guildId);
  }

  /**
   * Clears the active blessing for a guild (called when boss is defeated).
   *
   * @param guildId the guild ID
   */
  public void clearBlessing(String guildId) {
    Blessing removed = activeBlessings.remove(guildId);
    if (removed != null) {
      logger.info(
          "Cleared {} for guild {} (boss defeated)", removed.getType().getDisplayName(), guildId);
    }
  }

  /**
   * Gets blessing effects for a specific character class. Blessings are class-specific, so this
   * method returns the appropriate effects.
   *
   * @param guildId the guild ID
   * @param characterClass the character class
   * @return blessing effects for the class, or null if no active blessing
   */
  public Blessing getBlessingForClass(String guildId, CharacterClass characterClass) {
    Blessing baseBlessing = activeBlessings.get(guildId);
    if (baseBlessing == null) {
      return null;
    }

    // Create class-specific blessing based on base blessing tier
    return createClassSpecificBlessing(baseBlessing.getType(), characterClass);
  }

  /**
   * Creates a generic blessing for a tier (used for announcements).
   *
   * @param type the blessing type
   * @return a blessing with generic effects
   */
  private Blessing createBlessingForTier(BlessingType type) {
    // Use a default class (WARRIOR) for generic blessing display
    return createClassSpecificBlessing(type, CharacterClass.WARRIOR);
  }

  /**
   * Creates a class-specific blessing based on tier and class. Only includes stat multipliers that
   * are actually used in boss damage calculation.
   *
   * @param type the blessing type/tier
   * @param characterClass the character class
   * @return a blessing with class-specific effects
   */
  private Blessing createClassSpecificBlessing(BlessingType type, CharacterClass characterClass) {
    // Base values for Minor tier
    double baseStrMultiplier = 1.0;
    double baseAgiMultiplier = 1.0;
    double baseIntMultiplier = 1.0;

    // Tier scaling: Minor = base, Major = +50%, Legendary = +100%
    double tierMultiplier =
        switch (type) {
          case MINOR -> 1.0;
          case MAJOR -> 1.5;
          case LEGENDARY -> 2.0;
        };

    // Class-specific base effects (Minor tier values)
    // Only stat multipliers are relevant for boss battles
    switch (characterClass) {
      case WARRIOR:
      case KNIGHT:
        baseStrMultiplier = 1.25; // +25% STR
        break;
      case MAGE:
      case NECROMANCER:
      case PRIEST:
        baseIntMultiplier = 1.25; // +25% INT
        break;
      case ROGUE:
        baseAgiMultiplier = 1.25; // +25% AGI
        break;
      case OATHBREAKER:
        baseStrMultiplier = 1.20; // +20% STR
        baseIntMultiplier = 1.20; // +20% INT
        break;
    }

    // Apply tier scaling
    double strMultiplier = 1.0 + (baseStrMultiplier - 1.0) * tierMultiplier;
    double agiMultiplier = 1.0 + (baseAgiMultiplier - 1.0) * tierMultiplier;
    double intMultiplier = 1.0 + (baseIntMultiplier - 1.0) * tierMultiplier;

    // Get random narrative
    BlessingNarratives.NarrativeStyle style = BlessingNarratives.getRandomStyle();
    String narrative = BlessingNarratives.getRandomNarrative(style);

    return new Blessing(type, narrative, strMultiplier, agiMultiplier, intMultiplier);
  }
}
