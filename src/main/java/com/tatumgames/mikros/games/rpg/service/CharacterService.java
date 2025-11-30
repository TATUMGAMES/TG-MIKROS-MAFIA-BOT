package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing RPG characters.
 * Handles character creation, retrieval, and state management.
 * 
 * TODO: Future Features
 * - Database persistence for characters
 * - Character deletion/reset functionality
 * - Character transfer between servers
 * - Backup and restore functionality
 */
public class CharacterService {
    private static final Logger logger = LoggerFactory.getLogger(CharacterService.class);
    
    // Character storage: discordId -> RPGCharacter
    private final Map<String, RPGCharacter> characters;
    
    // Guild configurations: guildId -> RPGConfig
    private final Map<String, RPGConfig> guildConfigs;
    
    /**
     * Creates a new CharacterService.
     */
    public CharacterService() {
        this.characters = new ConcurrentHashMap<>();
        this.guildConfigs = new ConcurrentHashMap<>();
        logger.info("CharacterService initialized");
    }
    
    /**
     * Registers a new character for a user.
     * 
     * @param discordId the Discord user ID
     * @param name the character name
     * @param characterClass the character class
     * @return the created character
     * @throws IllegalStateException if user already has a character
     */
    public RPGCharacter registerCharacter(String discordId, String name, CharacterClass characterClass) {
        if (characters.containsKey(discordId)) {
            throw new IllegalStateException("User already has a character");
        }
        
        RPGCharacter character = new RPGCharacter(discordId, name, characterClass);
        characters.put(discordId, character);
        
        logger.info("Registered new character for user {}: {} ({})",
                discordId, name, characterClass.getDisplayName());
        
        return character;
    }
    
    /**
     * Gets a character by Discord ID.
     * 
     * @param discordId the Discord user ID
     * @return the character, or null if not found
     */
    public RPGCharacter getCharacter(String discordId) {
        return characters.get(discordId);
    }
    
    /**
     * Checks if a user has a character.
     * 
     * @param discordId the Discord user ID
     * @return true if the user has a character
     */
    public boolean hasCharacter(String discordId) {
        return characters.containsKey(discordId);
    }
    
    /**
     * Gets all characters sorted by a criteria.
     * 
     * @param comparator the comparator for sorting
     * @param limit the maximum number of characters to return
     * @return list of characters
     */
    public List<RPGCharacter> getTopCharacters(Comparator<RPGCharacter> comparator, int limit) {
        return characters.values().stream()
                .sorted(comparator)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the leaderboard (top characters by level and XP).
     * 
     * @param limit the maximum number of characters
     * @return list of top characters
     */
    public List<RPGCharacter> getLeaderboard(int limit) {
        return getTopCharacters(
                Comparator.comparingInt(RPGCharacter::getLevel)
                        .thenComparingInt(RPGCharacter::getXp)
                        .reversed(),
                limit
        );
    }
    
    /**
     * Gets the RPG configuration for a guild.
     * Creates a default config if none exists.
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
     * Gets the total number of registered characters.
     * 
     * @return character count
     */
    public int getCharacterCount() {
        return characters.size();
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
     * Resets all RPG data for a specific server.
     * This clears:
     * - RPG configuration (resets to defaults)
     * - Note: Characters are stored globally, not per-server
     * 
     * @param guildId the guild ID
     */
    public void resetServerData(String guildId) {
        // Remove or reset config to defaults
        guildConfigs.remove(guildId);
        logger.warn("Reset RPG data for server {}", guildId);
    }
    
    /**
     * Gets the number of characters registered.
     * Note: Characters are global, not per-server.
     * 
     * @return character count
     */
    public int getServerCharacterCount(String guildId) {
        // Since characters are global, we return total count
        // In a future version with server tracking, this would filter by server
        return characters.size();
    }
    
    /**
     * Clears all characters (global reset).
     * WARNING: This affects all servers.
     * 
     * @return number of characters cleared
     */
    public int clearAllCharacters() {
        int count = characters.size();
        characters.clear();
        logger.warn("Cleared all {} characters (global reset)", count);
        return count;
    }
}

