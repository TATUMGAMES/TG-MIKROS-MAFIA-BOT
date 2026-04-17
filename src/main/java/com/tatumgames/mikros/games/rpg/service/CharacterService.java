package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing RPG characters. Handles character creation, retrieval, and state management.
 *
 * <p>TODO: Future Features - Database persistence for characters - Character deletion/reset
 * functionality - Character transfer between servers - Backup and restore functionality
 */
public class CharacterService {
  private static final Logger logger = LoggerFactory.getLogger(CharacterService.class);

  // Active characters per guild: guildId -> discordId -> RPGCharacter
  private final Map<String, Map<String, RPGCharacter>> activeCharactersByGuild;

  // Retired character history per guild: guildId -> discordId -> archived characters
  private final Map<String, Map<String, List<RPGCharacter>>> characterHistoryByGuild;

  // Guild configurations: guildId -> RPGConfig
  private final Map<String, RPGConfig> guildConfigs;

  private Map<String, RPGCharacter> getActiveCharactersMap(String guildId) {
    return activeCharactersByGuild.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>());
  }

  private Map<String, List<RPGCharacter>> getCharacterHistoryMap(String guildId) {
    return characterHistoryByGuild.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>());
  }

  /** Creates a new CharacterService. */
  public CharacterService() {
    this.activeCharactersByGuild = new ConcurrentHashMap<>();
    this.characterHistoryByGuild = new ConcurrentHashMap<>();
    this.guildConfigs = new ConcurrentHashMap<>();
    logger.info("CharacterService initialized");
  }

  /**
   * Registers a new character for a user.
   *
   * @param guildId the guild ID
   * @param discordId the Discord user ID
   * @param name the character name
   * @param characterClass the character class
   * @return the created character
   * @throws IllegalStateException if user already has a character
   */
  public RPGCharacter registerCharacter(
      String guildId, String discordId, String name, CharacterClass characterClass) {
    Map<String, RPGCharacter> active = getActiveCharactersMap(guildId);
    if (active.containsKey(discordId)) {
      throw new IllegalStateException("User already has a character");
    }

    RPGCharacter character = new RPGCharacter(discordId, name, characterClass);
    active.put(discordId, character);

    logger.info(
        "Registered new character for user {}: {} ({})",
        discordId,
        name,
        characterClass.getDisplayName());

    return character;
  }

  /**
   * Gets a character by Discord ID.
   *
   * @param guildId the guild ID
   * @param discordId the Discord user ID
   * @return the character, or null if not found
   */
  public RPGCharacter getCharacter(String guildId, String discordId) {
    Map<String, RPGCharacter> active = activeCharactersByGuild.get(guildId);
    return active != null ? active.get(discordId) : null;
  }

  /**
   * Checks if a user has a character.
   *
   * @param guildId the guild ID
   * @param discordId the Discord user ID
   * @return true if the user has a character
   */
  public boolean hasCharacter(String guildId, String discordId) {
    Map<String, RPGCharacter> active = activeCharactersByGuild.get(guildId);
    return active != null && active.containsKey(discordId);
  }

  /**
   * Returns true if the user has an active character that is dead and has been dead for at least 24
   * hours (so they are allowed to re-register a new character; the old one will be archived).
   *
   * @param guildId the guild ID
   * @param discordId the Discord user ID
   * @return true if the user can re-register after death (dead 24h+ without resurrection)
   */
  public boolean canReregisterAfterDeath(String guildId, String discordId) {
    RPGCharacter character = getCharacter(guildId, discordId);
    if (character == null || !character.isDead()) {
      return false;
    }
    Instant diedAt = character.getDiedAt();
    if (diedAt == null) {
      return false;
    }
    return Instant.now().isAfter(diedAt.plus(24, ChronoUnit.HOURS));
  }

  /**
   * Archives the user's current active character to history and removes it from active storage.
   * Call this before allowing re-registration when the user has been dead 24h+.
   *
   * @param guildId the guild ID
   * @param discordId the Discord user ID
   */
  public void archiveAndRemoveActive(String guildId, String discordId) {
    Map<String, RPGCharacter> active = activeCharactersByGuild.get(guildId);
    if (active == null) {
      return;
    }
    RPGCharacter character = active.remove(discordId);
    if (character != null) {
      Map<String, List<RPGCharacter>> history = getCharacterHistoryMap(guildId);
      history.computeIfAbsent(discordId, k -> new ArrayList<>()).add(character);
      logger.info(
          "Archived character {} ({}) for user {} (re-register after 24h dead)",
          character.getName(),
          character.getCharacterClass().getDisplayName(),
          discordId);
    }
  }

  /**
   * Gets all characters sorted by a criteria.
   *
   * @param guildId the guild ID
   * @param comparator the comparator for sorting
   * @param limit the maximum number of characters to return
   * @return list of characters
   */
  public List<RPGCharacter> getTopCharacters(
      String guildId, Comparator<RPGCharacter> comparator, int limit) {
    Map<String, RPGCharacter> active = activeCharactersByGuild.get(guildId);
    if (active == null || active.isEmpty()) {
      return Collections.emptyList();
    }
    return active.values().stream().sorted(comparator).limit(limit).collect(Collectors.toList());
  }

  /**
   * Gets all registered characters.
   *
   * @param guildId the guild ID
   * @return collection of all characters
   */
  public Collection<RPGCharacter> getAllCharacters(String guildId) {
    Map<String, RPGCharacter> active = activeCharactersByGuild.get(guildId);
    if (active == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(active.values());
  }

  /**
   * Gets the leaderboard (top characters by level and XP).
   *
   * @param guildId the guild ID
   * @param limit the maximum number of characters
   * @return list of top characters
   */
  public List<RPGCharacter> getLeaderboard(String guildId, int limit) {
    return getTopCharacters(
        guildId,
        Comparator.comparingInt(RPGCharacter::getLevel)
            .thenComparingInt(RPGCharacter::getXp)
            .reversed(),
        limit);
  }

  /**
   * Gets the RPG configuration for a guild. Creates a default config if none exists.
   *
   * @param guildId the guild ID
   * @return the RPG config
   */
  public RPGConfig getConfig(String guildId) {
    return guildConfigs.computeIfAbsent(guildId, RPGConfig::new);
  }

  /**
   * Updates the RPG configuration for a guild.
   *
   * @param config the new configuration
   */
  public void updateConfig(RPGConfig config) {
    guildConfigs.put(config.getGuildId(), config);
    logger.info("Updated RPG config for guild {}", config.getGuildId());
  }

  /**
   * Gets the total number of registered characters across all guilds.
   *
   * @return character count
   */
  public int getCharacterCount() {
    int total = 0;
    for (Map<String, RPGCharacter> active : activeCharactersByGuild.values()) {
      total += active.size();
    }
    return total;
  }

  /**
   * Gets the number of registered characters for a guild.
   *
   * @param guildId the guild ID
   * @return active character count
   */
  public int getCharacterCount(String guildId) {
    Map<String, RPGCharacter> active = activeCharactersByGuild.get(guildId);
    return active != null ? active.size() : 0;
  }

  /**
   * Gets all guild IDs with RPG configured.
   *
   * @return set of guild IDs
   */
  public Set<String> getConfiguredGuilds() {
    return new HashSet<>(guildConfigs.keySet());
  }

  /**
   * Resets all RPG data for a specific server (configuration + active characters + history).
   *
   * @param guildId the guild ID
   */
  public void resetServerData(String guildId) {
    guildConfigs.remove(guildId);
    activeCharactersByGuild.remove(guildId);
    characterHistoryByGuild.remove(guildId);
    logger.warn("Reset RPG data for server {}", guildId);
  }

  /**
   * Gets the number of active characters registered for a guild.
   *
   * @param guildId the guild ID
   * @return character count
   */
  public int getServerCharacterCount(String guildId) {
    return getCharacterCount(guildId);
  }

  /**
   * Clears all characters (global reset). WARNING: This affects all servers.
   *
   * @return number of characters cleared
   */
  public int clearAllCharacters() {
    int count = getCharacterCount();
    activeCharactersByGuild.clear();
    characterHistoryByGuild.clear();
    logger.warn("Cleared all {} characters (global reset)", count);
    return count;
  }

  /**
   * Clears active characters + archived history for a guild.
   *
   * @param guildId the guild ID
   * @return number of active characters cleared
   */
  public int clearGuildCharacters(String guildId) {
    Map<String, RPGCharacter> active = activeCharactersByGuild.remove(guildId);
    int count = active != null ? active.size() : 0;
    characterHistoryByGuild.remove(guildId);
    return count;
  }

  /**
   * Deletes a user's active character and archived history for this guild.
   *
   * @param guildId the guild ID
   * @param discordId the Discord user ID
   * @return true if anything was removed
   */
  public boolean deleteAllCharactersForUser(String guildId, String discordId) {
    if (guildId == null || discordId == null) {
      return false;
    }
    boolean changed = false;
    Map<String, RPGCharacter> active = activeCharactersByGuild.get(guildId);
    if (active != null && active.remove(discordId) != null) {
      changed = true;
    }
    Map<String, List<RPGCharacter>> historyMap = characterHistoryByGuild.get(guildId);
    if (historyMap != null && historyMap.remove(discordId) != null) {
      changed = true;
    }
    if (changed) {
      logger.info("Deleted RPG character data for user {} in guild {}", discordId, guildId);
    }
    return changed;
  }
}
