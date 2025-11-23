package com.tatumgames.mikros.services;

import com.tatumgames.mikros.config.ModerationConfig;
import com.tatumgames.mikros.models.ActionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for automatic escalation of moderation actions based on warning thresholds.
 */
public class AutoEscalationService {
    private static final Logger logger = LoggerFactory.getLogger(AutoEscalationService.class);
    
    private final ModerationLogService moderationLogService;
    
    // Key: "guildId" -> enabled status
    private final Map<String, Boolean> guildEscalationEnabled;
    
    // Key: "guildId" -> warning threshold
    private final Map<String, Integer> guildEscalationThresholds;
    
    /**
     * Creates a new AutoEscalationService.
     * 
     * @param moderationLogService the moderation log service
     */
    public AutoEscalationService(ModerationLogService moderationLogService) {
        this.moderationLogService = moderationLogService;
        this.guildEscalationEnabled = new ConcurrentHashMap<>();
        this.guildEscalationThresholds = new ConcurrentHashMap<>();
        logger.info("AutoEscalationService initialized");
    }
    
    /**
     * Checks if a user should be auto-escalated based on their warning history.
     * 
     * @param userId the user ID
     * @param guildId the guild ID
     * @return the recommended action, or null if no escalation needed
     */
    public ActionType checkEscalation(String userId, String guildId) {
        if (!isEscalationEnabled(guildId)) {
            return null;
        }
        
        int warningCount = moderationLogService.getUserHistoryByType(userId, guildId, ActionType.WARN).size();
        int threshold = getEscalationThreshold(guildId);
        
        if (warningCount >= threshold) {
            logger.info("User {} in guild {} has {} warnings, auto-escalation triggered (threshold: {})",
                    userId, guildId, warningCount, threshold);
            return ActionType.KICK;
        }
        
        return null;
    }
    
    /**
     * Performs auto-escalation for a user if thresholds are met.
     * 
     * @param member the member to potentially escalate
     * @param guild the guild
     * @param botMember the bot's member object in the guild
     * @return true if escalation was performed, false otherwise
     */
    public boolean performAutoEscalation(Member member, Guild guild, Member botMember) {
        if (member == null || guild == null) {
            return false;
        }
        
        ActionType escalationAction = checkEscalation(member.getId(), guild.getId());
        
        if (escalationAction == ActionType.KICK) {
            // Check if bot can kick the member
            if (botMember.canInteract(member)) {
                String reason = String.format(
                        "Auto-escalation: User reached %d warnings threshold",
                        getEscalationThreshold(guild.getId())
                );
                
                guild.kick(member)
                        .reason(reason)
                        .queue(
                                success -> logger.info("Auto-escalated user {} in guild {}: kicked",
                                        member.getId(), guild.getId()),
                                error -> logger.error("Failed to auto-escalate user {} in guild {}: {}",
                                        member.getId(), guild.getId(), error.getMessage())
                        );
                return true;
            } else {
                logger.warn("Cannot auto-escalate user {} in guild {}: role hierarchy prevents kick",
                        member.getId(), guild.getId());
            }
        }
        
        return false;
    }
    
    /**
     * Enables or disables auto-escalation for a guild.
     * 
     * @param guildId the guild ID
     * @param enabled whether to enable auto-escalation
     */
    public void setEscalationEnabled(String guildId, boolean enabled) {
        guildEscalationEnabled.put(guildId, enabled);
        logger.info("Auto-escalation {} for guild {}", enabled ? "enabled" : "disabled", guildId);
    }
    
    /**
     * Checks if auto-escalation is enabled for a guild.
     * 
     * @param guildId the guild ID
     * @return true if enabled, false otherwise
     */
    public boolean isEscalationEnabled(String guildId) {
        return guildEscalationEnabled.getOrDefault(guildId, ModerationConfig.AUTO_ESCALATION_ENABLED_DEFAULT);
    }
    
    /**
     * Sets the warning threshold for auto-escalation in a guild.
     * 
     * @param guildId the guild ID
     * @param threshold the warning threshold
     */
    public void setEscalationThreshold(String guildId, int threshold) {
        if (threshold < 1) {
            throw new IllegalArgumentException("Threshold must be at least 1");
        }
        guildEscalationThresholds.put(guildId, threshold);
        logger.info("Auto-escalation threshold set to {} for guild {}", threshold, guildId);
    }
    
    /**
     * Gets the warning threshold for auto-escalation in a guild.
     * 
     * @param guildId the guild ID
     * @return the warning threshold
     */
    public int getEscalationThreshold(String guildId) {
        return guildEscalationThresholds.getOrDefault(guildId, ModerationConfig.AUTO_ESCALATION_WARNING_THRESHOLD);
    }
}

