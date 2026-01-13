package com.tatumgames.mikros.botdetection.service;

import com.tatumgames.mikros.botdetection.config.BotDetectionConfig;
import com.tatumgames.mikros.botdetection.model.BotDetectionResult;
import com.tatumgames.mikros.botdetection.model.SuspiciousDomainList;
import com.tatumgames.mikros.botdetection.tracker.MessagePatternTracker;
import com.tatumgames.mikros.botdetection.util.LinkDetectionUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for detecting bot behavior and spam patterns.
 */
public class BotDetectionService {
    private static final Logger logger = LoggerFactory.getLogger(BotDetectionService.class);
    private static final long REPORT_COOLDOWN_SECONDS = 300; // 5 minutes
    // Per-guild configuration
    private final Map<String, BotDetectionConfig> configs;
    // Bot prevention count tracking per guild
    private final Map<String, Integer> botPreventionCounts;
    // Message pattern tracker for multi-channel spam detection
    private final MessagePatternTracker patternTracker;
    // Suspicious domain list
    private final SuspiciousDomainList domainList;
    // Cooldown tracking: "guildId:userId" -> last report timestamp
    private final Map<String, Long> reportCooldowns;

    public BotDetectionService() {
        this.configs = new ConcurrentHashMap<>();
        this.botPreventionCounts = new ConcurrentHashMap<>();
        this.patternTracker = new MessagePatternTracker();
        this.domainList = SuspiciousDomainList.getInstance();
        this.reportCooldowns = new ConcurrentHashMap<>();
        logger.info("BotDetectionService initialized");
    }

    /**
     * Gets or creates configuration for a guild.
     *
     * @param guildId the guild ID
     * @return the configuration
     */
    public BotDetectionConfig getConfig(String guildId) {
        return configs.computeIfAbsent(guildId, k -> new BotDetectionConfig());
    }

    /**
     * Updates configuration for a guild.
     *
     * @param guildId the guild ID
     * @param config  the configuration
     */
    public void updateConfig(String guildId, BotDetectionConfig config) {
        configs.put(guildId, config);
        logger.info("Updated bot detection config for guild {}", guildId);
    }

    /**
     * Detects bot behavior in a message event.
     *
     * @param event the message event
     * @return the detection result
     */
    public BotDetectionResult detectBotBehavior(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild()) {
            return BotDetectionResult.noDetection();
        }

        String guildId = event.getGuild().getId();
        BotDetectionConfig config = getConfig(guildId);

        if (!config.isEnabled()) {
            return BotDetectionResult.noDetection();
        }

        User user = event.getAuthor();
        Member member = event.getMember();
        String messageContent = event.getMessage().getContentRaw();
        String userId = user.getId();
        String channelId = event.getChannel().getId();

        // Check cooldown to prevent spam reporting
        if (isOnCooldown(guildId, userId)) {
            return BotDetectionResult.noDetection();
        }

        // 1. Check account age + link
        if (isAccountTooNew(user, config.getAccountAgeThresholdDays()) &&
                LinkDetectionUtil.containsLink(messageContent)) {
            String details = String.format("Account age: %d days, posted link",
                    getAccountAgeDays(user));
            return new BotDetectionResult(
                    true,
                    BotDetectionResult.DetectionReason.ACCOUNT_TOO_NEW,
                    BotDetectionResult.Confidence.HIGH,
                    config.getAutoAction(),
                    details
            );
        }

        // 2. Check join + immediate link
        if (member != null && isJoinAndLink(member, messageContent, config.getJoinAndLinkTimeWindowSeconds())) {
            String details = String.format("Joined %d seconds ago, posted link",
                    getSecondsSinceJoin(member));
            return new BotDetectionResult(
                    true,
                    BotDetectionResult.DetectionReason.JOIN_AND_LINK,
                    BotDetectionResult.Confidence.HIGH,
                    config.getAutoAction(),
                    details
            );
        }

        // 3. Check multi-channel spam
        String contentHash = hashContent(messageContent);
        patternTracker.recordMessage(userId, channelId, messageContent);
        if (patternTracker.isMultiChannelSpam(userId, contentHash,
                config.getMultiChannelSpamThreshold(),
                config.getMultiChannelTimeWindowSeconds())) {
            String details = String.format("Same message posted in %d+ channels within %d seconds",
                    config.getMultiChannelSpamThreshold(),
                    config.getMultiChannelTimeWindowSeconds());
            return new BotDetectionResult(
                    true,
                    BotDetectionResult.DetectionReason.MULTI_CHANNEL_SPAM,
                    BotDetectionResult.Confidence.HIGH,
                    config.getAutoAction(),
                    details
            );
        }

        // 4. Check suspicious domains
        List<String> urls = LinkDetectionUtil.extractUrls(messageContent);
        for (String url : urls) {
            String domain = LinkDetectionUtil.extractDomain(url);
            if (domain != null) {
                if (LinkDetectionUtil.isUrlShortener(domain)) {
                    return new BotDetectionResult(
                            true,
                            BotDetectionResult.DetectionReason.URL_SHORTENER,
                            BotDetectionResult.Confidence.MEDIUM,
                            config.getAutoAction(),
                            "URL shortener detected: " + domain
                    );
                }
                if (domainList.isSuspicious(domain)) {
                    int riskScore = domainList.getDomainRiskScore(domain);
                    return new BotDetectionResult(
                            true,
                            BotDetectionResult.DetectionReason.SUSPICIOUS_DOMAIN,
                            BotDetectionResult.Confidence.MEDIUM,
                            config.getAutoAction(),
                            String.format("Suspicious domain: %s (risk: %d)", domain, riskScore)
                    );
                }
            }
        }

        return BotDetectionResult.noDetection();
    }

    /**
     * Checks if an account is too new.
     *
     * @param user      the user
     * @param threshold the age threshold in days
     * @return true if account is too new, false otherwise
     */
    public boolean isAccountTooNew(User user, int threshold) {
        return getAccountAgeDays(user) < threshold;
    }

    /**
     * Gets the age of an account in days.
     *
     * @param user the user
     * @return the age in days
     */
    private int getAccountAgeDays(User user) {
        return (int) ChronoUnit.DAYS.between(user.getTimeCreated().toInstant(), Instant.now());
    }

    /**
     * Checks if a member joined recently and posted a link.
     *
     * @param member            the member
     * @param messageContent    the message content
     * @param timeWindowSeconds the time window in seconds
     * @return true if join + link detected, false otherwise
     */
    public boolean isJoinAndLink(Member member, String messageContent, int timeWindowSeconds) {
        // Note: member.getTimeJoined() is guaranteed to be non-null by JDA API

        long secondsSinceJoin = ChronoUnit.SECONDS.between(
                member.getTimeJoined().toInstant(),
                Instant.now()
        );

        return secondsSinceJoin < timeWindowSeconds && LinkDetectionUtil.containsLink(messageContent);
    }

    /**
     * Records a bot prevention (increments count).
     *
     * @param guildId the guild ID
     */
    public void recordBotPrevention(String guildId) {
        botPreventionCounts.merge(guildId, 1, Integer::sum);
        logger.info("Recorded bot prevention for guild {} (total: {})",
                guildId, botPreventionCounts.get(guildId));
    }

    /**
     * Gets the bot prevention count for a guild.
     *
     * @param guildId the guild ID
     * @return the prevention count
     */
    public int getBotPreventionCount(String guildId) {
        return botPreventionCounts.getOrDefault(guildId, 0);
    }

    /**
     * Checks if a user is on cooldown for reporting.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @return true if on cooldown, false otherwise
     */
    private boolean isOnCooldown(String guildId, String userId) {
        String key = guildId + ":" + userId;
        Long lastReport = reportCooldowns.get(key);
        if (lastReport == null) {
            return false;
        }

        long secondsSinceReport = (System.currentTimeMillis() - lastReport) / 1000;
        if (secondsSinceReport >= REPORT_COOLDOWN_SECONDS) {
            reportCooldowns.remove(key);
            return false;
        }
        return true;
    }

    /**
     * Records that a report was made (for cooldown).
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     */
    public void recordReport(String guildId, String userId) {
        String key = guildId + ":" + userId;
        reportCooldowns.put(key, System.currentTimeMillis());
    }

    /**
     * Gets seconds since member joined.
     *
     * @param member the member
     * @return seconds since join, or 0 if unknown
     */
    private long getSecondsSinceJoin(Member member) {
        // Note: member.getTimeJoined() is guaranteed to be non-null by JDA API
        return ChronoUnit.SECONDS.between(member.getTimeJoined().toInstant(), Instant.now());
    }

    /**
     * Hashes content for pattern tracking.
     *
     * @param content the content
     * @return the hash
     */
    private String hashContent(String content) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return String.valueOf(content.hashCode());
        }
    }

    /**
     * Adds a suspicious domain to the list.
     *
     * @param domain    the domain
     * @param riskScore the risk score
     */
    public void addSuspiciousDomain(String domain, int riskScore) {
        domainList.addSuspiciousDomain(domain, riskScore);
        logger.info("Added suspicious domain: {} (risk: {})", domain, riskScore);
    }

    /**
     * Removes a suspicious domain from the list.
     *
     * @param domain the domain
     */
    public void removeSuspiciousDomain(String domain) {
        domainList.removeSuspiciousDomain(domain);
        logger.info("Removed suspicious domain: {}", domain);
    }
}

