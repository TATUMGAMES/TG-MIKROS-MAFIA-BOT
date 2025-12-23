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

    /**
     * Environment type for the application.
     */
    public enum Environment {
        /**
         * Development environment.
         */
        DEV("dev"),

        /**
         * Production environment.
         */
        PROD("prod");

        private final String value;

        Environment(String value) {
            this.value = value;
        }

        /**
         * Gets the string value of the environment.
         *
         * @return the environment value
         */
        public String getValue() {
            return value;
        }

        /**
         * Converts a string to an Environment enum.
         * Defaults to PROD if value is null, blank, or unrecognized.
         *
         * @param value the string value to convert
         * @return the Environment enum, defaults to PROD
         */
        public static Environment fromString(String value) {
            if (value == null || value.isBlank()) {
                return PROD; // Default to prod for safety
            }
            String lower = value.toLowerCase().trim();
            return "dev".equals(lower) ? DEV : PROD;
        }
    }

    private final Dotenv dotenv;
    private final Environment environment;
    private final String botToken;
    private final String botOwnerId;
    private final String mafiaGuildId;
    private final String mikrosApiKey;
    private final String mikrosApiUrl;
    private final String reputationApiKeyDev;
    private final String reputationApiKeyProd;
    private final String reputationApiKey;
    private final String reputationApiUrl;

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

        // Load environment configuration
        String envValue = getEnv("ENVIRONMENT", "prod");
        this.environment = Environment.fromString(envValue);

        // Load required configuration
        this.botToken = getRequiredEnv("DISCORD_BOT_TOKEN");
        this.botOwnerId = getEnv("BOT_OWNER_ID", "");
        this.mafiaGuildId = getEnv("MIKROS_MAFIA_GUILD_ID", "");

        // Load API configuration (optional - can be empty for mock mode)
        this.mikrosApiKey = getEnv("MIKROS_API_KEY", "");
        this.mikrosApiUrl = getEnv("MIKROS_API_URL", "https://api.tatumgames.com");

        if (mikrosApiKey == null || mikrosApiKey.isBlank()) {
            logger.warn("MIKROS_API_KEY not set - API client will operate in mock mode");
        } else {
            logger.info("MIKROS API configuration loaded");
        }

        // Load reputation API configuration (optional - can be empty for mock mode)
        // Load dev and prod keys separately
        this.reputationApiKeyDev = getEnv("REPUTATION_API_KEY_DEV",
                "e12afd908f7c19a64bca41e6657fae90e001bd55");
        this.reputationApiKeyProd = getEnv("REPUTATION_API_KEY_PROD",
                "a37f9c1de42b5089f6c2d8e14bb97f30e5ab47cc");

        // Select key based on environment, but allow override with legacy variable
        String legacyKey = getEnv("REPUTATION_API_KEY", "");
        if (legacyKey != null && !legacyKey.isBlank()) {
            // Legacy variable takes precedence for backward compatibility
            this.reputationApiKey = legacyKey;
            logger.info("Using REPUTATION_API_KEY from legacy environment variable");
        } else {
            // Use environment-based key
            this.reputationApiKey = environment == Environment.DEV
                    ? reputationApiKeyDev
                    : reputationApiKeyProd;
        }

        this.reputationApiUrl = getEnv("REPUTATION_API_URL",
                "https://tg-api-new-stage.uc.r.appspot.com/mikros/marketing/discord");

        if (reputationApiKey == null || reputationApiKey.isBlank()) {
            logger.warn("REPUTATION_API_KEY not set - reputation service will use stub responses");
        } else {
            logger.info("Reputation API configuration loaded - Environment: {} | Using {} API key",
                    environment.getValue(), environment == Environment.DEV ? "DEV" : "PROD");
        }

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
     * @param key          the environment variable key
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

    /**
     * Gets the MIKROS API key for authentication.
     *
     * @return the API key, or empty string if not configured
     */
    public String getMikrosApiKey() {
        return mikrosApiKey;
    }

    /**
     * Gets the MIKROS API base URL.
     *
     * @return the API base URL (defaults to https://api.tatumgames.com)
     */
    public String getMikrosApiUrl() {
        return mikrosApiUrl;
    }

    /**
     * Gets the reputation API key for authentication.
     *
     * @return the API key, or empty string if not configured
     */
    public String getReputationApiKey() {
        return reputationApiKey;
    }

    /**
     * Gets the reputation API base URL.
     *
     * @return the API base URL (defaults to https://tg-api-new-stage.uc.r.appspot.com/mikros/marketing/discord)
     */
    public String getReputationApiUrl() {
        return reputationApiUrl;
    }

    /**
     * Gets the current environment (dev or prod).
     *
     * @return the environment
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Gets the API key type string based on the current environment.
     *
     * @return "dev" or "prod"
     */
    public String getApiKeyType() {
        return environment.getValue();
    }
}
