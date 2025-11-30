package com.tatumgames.mikros.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration loader for the Discord bot.
 * Reads configuration values from environment variables and .env file.
 */
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    
    private final Dotenv dotenv;
    private final String botToken;
    private final String botOwnerId;
    private final String mafiaGuildId;
    
    /**
     * Creates a new ConfigLoader instance and loads configuration.
     * 
     * @throws IllegalStateException if required configuration is missing
     */
    public ConfigLoader() {
        // Try to load .env file, but don't fail if it doesn't exist
        Dotenv tempDotenv;
        try {
            tempDotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            logger.warn("Could not load .env file, will use system environment variables only", e);
            tempDotenv = null;
        }
        this.dotenv = tempDotenv;
        
        // Load required configuration
        this.botToken = getRequiredEnv("DISCORD_BOT_TOKEN");
        this.botOwnerId = getEnv("BOT_OWNER_ID", "");
        this.mafiaGuildId = getEnv("MIKROS_MAFIA_GUILD_ID", "");
        
        logger.info("Configuration loaded successfully");
    }
    
    /**
     * Gets a required environment variable.
     * 
     * @param key the environment variable key
     * @return the value
     * @throws IllegalStateException if the variable is not set
     */
    private String getRequiredEnv(String key) {
        String value = getEnv(key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required configuration missing: " + key);
        }
        return value;
    }
    
    /**
     * Gets an environment variable with a default value.
     * 
     * @param key the environment variable key
     * @param defaultValue the default value if not found
     * @return the value or default
     */
    private String getEnv(String key, String defaultValue) {
        // First try .env file
        if (dotenv != null) {
            String value = dotenv.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        
        // Fall back to system environment variables
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        
        return defaultValue;
    }
    
    /**
     * Gets the Discord bot token.
     * 
     * @return the bot token
     */
    public String getBotToken() {
        return botToken;
    }
    
    /**
     * Gets the bot owner's Discord user ID.
     * 
     * @return the owner ID, or empty string if not configured
     */
    public String getBotOwnerId() {
        return botOwnerId;
    }
    
    /**
     * Gets the MIKROS Mafia Discord server ID.
     * Used for leaderboard member status checks.
     * 
     * @return the Mafia server ID, or empty string if not configured
     */
    public String getMafiaGuildId() {
        return mafiaGuildId;
    }
}

