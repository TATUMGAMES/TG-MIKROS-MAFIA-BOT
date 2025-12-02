package com.tatumgames.mikros.promo.service;

import com.tatumgames.mikros.promo.config.PromoConfig;
import com.tatumgames.mikros.promo.model.PromoTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Service for detecting promotional triggers in messages.
 * Uses regex patterns to identify launch-related phrases.
 * <p>
 * TODO: Future Features
 * - Integrate Google Generative AI API for NLP message classification
 * - More sophisticated pattern matching
 * - Context-aware detection
 * - Sentiment analysis
 * - Multi-language support
 */
public class PromoDetectionService {
    private static final Logger logger = LoggerFactory.getLogger(PromoDetectionService.class);

    // Launch-related trigger patterns (case-insensitive)
    private static final Pattern[] TRIGGER_PATTERNS = {
            // Game launch patterns
            Pattern.compile("(?i)\\b(we'?re|we are|i'?m|i am)\\s+(launching|releasing|publishing)\\s+(our|my|a|the)\\s+(game|title|project)"),
            Pattern.compile("(?i)\\b(launch|release|publish|go live|going live)\\s+(on|in|at|this|next|tomorrow|today)"),
            Pattern.compile("(?i)\\b(steam|epic|itch|gog)\\s+(page|store|listing)\\s+(is|will be|goes)\\s+(live|up|available)"),
            Pattern.compile("(?i)\\b(kickstarter|indiegogo|patreon)\\s+(is|ends|ending|closes)\\s+(in|on|at)"),
            Pattern.compile("(?i)\\b(need|looking for|want|seeking)\\s+(help|assistance|support)\\s+(promoting|marketing|advertising|with promotion)"),
            Pattern.compile("(?i)\\b(game|title|project)\\s+(launch|release|launches|releases)\\s+(on|in|at|this|next)"),
            Pattern.compile("(?i)\\b(coming|releasing|launching)\\s+(soon|this|next|tomorrow|today)"),
            Pattern.compile("(?i)\\b(beta|alpha|early access|demo)\\s+(is|starts|begins|goes)\\s+(live|up|available)"),
            Pattern.compile("(?i)\\b(pre-?order|preorder|pre-?purchase)\\s+(is|starts|begins|now)\\s+(available|open|live)"),
            Pattern.compile("(?i)\\b(trailer|announcement|reveal)\\s+(drops|releases|is|goes)\\s+(live|up|out)")
    };

    // Pattern descriptions for logging
    private static final String[] PATTERN_DESCRIPTIONS = {
            "Game launch announcement",
            "Launch date/time mention",
            "Store page launch",
            "Crowdfunding campaign",
            "Promotion help request",
            "Game launch event",
            "Coming soon announcement",
            "Beta/alpha release",
            "Pre-order availability",
            "Trailer/announcement release"
    };

    // Guild configurations: guildId -> PromoConfig
    private final Map<String, PromoConfig> guildConfigs;

    // User cooldown tracking: userId -> last prompt timestamp
    private final Map<String, Instant> userCooldowns;

    // Recent triggers: userId -> list of recent triggers (for duplicate prevention)
    private final Map<String, List<PromoTrigger>> recentTriggers;

    /**
     * Creates a new PromoDetectionService.
     */
    public PromoDetectionService() {
        this.guildConfigs = new ConcurrentHashMap<>();
        this.userCooldowns = new ConcurrentHashMap<>();
        this.recentTriggers = new ConcurrentHashMap<>();
        logger.info("PromoDetectionService initialized with {} trigger patterns", TRIGGER_PATTERNS.length);
    }

    /**
     * Checks if a message contains promotional triggers.
     *
     * @param messageContent the message content
     * @return the matched pattern description, or null if no match
     */
    public String detectTrigger(String messageContent) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            return null;
        }

        for (int i = 0; i < TRIGGER_PATTERNS.length; i++) {
            if (TRIGGER_PATTERNS[i].matcher(messageContent).find()) {
                return PATTERN_DESCRIPTIONS[i];
            }
        }

        return null;
    }

    /**
     * Checks if a user is on cooldown for promotional prompts.
     *
     * @param userId  the user ID
     * @param guildId the guild ID
     * @return true if user can receive a prompt
     */
    public boolean canSendPrompt(String userId, String guildId) {
        PromoConfig config = getConfig(guildId);
        if (!config.isEnabled()) {
            return false;
        }

        Instant lastPrompt = userCooldowns.get(userId);
        if (lastPrompt == null) {
            return true;
        }

        Instant cooldownEnd = lastPrompt.plusSeconds(config.getCooldownDays() * 86400L);
        return Instant.now().isAfter(cooldownEnd);
    }

    /**
     * Records that a prompt was sent to a user.
     *
     * @param userId the user ID
     */
    public void recordPromptSent(String userId) {
        userCooldowns.put(userId, Instant.now());
    }

    /**
     * Creates a promotional trigger from a detected message.
     *
     * @param userId          the user ID
     * @param username        the username
     * @param guildId         the guild ID
     * @param channelId       the channel ID
     * @param messageContent  the message content
     * @param detectedPattern the detected pattern
     * @return the promo trigger
     */
    public PromoTrigger createTrigger(String userId, String username, String guildId,
                                      String channelId, String messageContent, String detectedPattern) {
        PromoTrigger trigger = new PromoTrigger(userId, username, guildId, channelId,
                messageContent, detectedPattern, Instant.now());

        // Track recent triggers for duplicate prevention
        recentTriggers.computeIfAbsent(userId, k -> new ArrayList<>()).add(trigger);

        // Keep only last 5 triggers per user
        List<PromoTrigger> triggers = recentTriggers.get(userId);
        if (triggers.size() > 5) {
            triggers.remove(0);
        }

        return trigger;
    }

    /**
     * Gets the promotional configuration for a guild.
     * Creates default config if none exists.
     *
     * @param guildId the guild ID
     * @return the promo config
     */
    public PromoConfig getConfig(String guildId) {
        return guildConfigs.computeIfAbsent(guildId, PromoConfig::new);
    }

    /**
     * Updates the promotional configuration for a guild.
     *
     * @param config the new configuration
     */
    public void updateConfig(PromoConfig config) {
        guildConfigs.put(config.getGuildId(), config);
        logger.info("Updated promo config for guild {}", config.getGuildId());
    }

    /**
     * Gets the time remaining until a user can receive another prompt.
     *
     * @param userId  the user ID
     * @param guildId the guild ID
     * @return seconds remaining, or 0 if ready
     */
    public long getSecondsUntilNextPrompt(String userId, String guildId) {
        Instant lastPrompt = userCooldowns.get(userId);
        if (lastPrompt == null) {
            return 0;
        }

        PromoConfig config = getConfig(guildId);
        Instant cooldownEnd = lastPrompt.plusSeconds(config.getCooldownDays() * 86400L);
        long remaining = cooldownEnd.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }
}
