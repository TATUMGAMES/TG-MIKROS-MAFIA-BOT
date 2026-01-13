package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.achievements.AchievementType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking first-to achievements per server.
 * Achievements can only be claimed once per server (guild).
 */
public class AchievementService {
    // Map: guildId -> (AchievementType -> userId)
    private final Map<String, Map<AchievementType, String>> achievementClaims;

    public AchievementService() {
        this.achievementClaims = new ConcurrentHashMap<>();
    }

    /**
     * Checks if a first-to achievement can be claimed and claims it if available.
     *
     * @param guildId the Discord guild ID
     * @param type    the achievement type
     * @param userId  the user ID attempting to claim
     * @return true if the achievement was successfully claimed (first time), false if already claimed
     */
    public boolean checkAndClaimFirstTo(String guildId, AchievementType type, String userId) {
        Map<AchievementType, String> guildAchievements = achievementClaims.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>());

        // Check if already claimed
        if (guildAchievements.containsKey(type)) {
            return false;
        }

        // Claim the achievement
        guildAchievements.put(type, userId);
        return true;
    }

    /**
     * Checks if a first-to achievement has already been claimed.
     *
     * @param guildId the Discord guild ID
     * @param type    the achievement type
     * @return true if already claimed
     */
    public boolean isFirstToClaimed(String guildId, AchievementType type) {
        Map<AchievementType, String> guildAchievements = achievementClaims.get(guildId);
        if (guildAchievements == null) {
            return false;
        }
        return guildAchievements.containsKey(type);
    }

    /**
     * Gets the user ID who claimed a first-to achievement.
     *
     * @param guildId the Discord guild ID
     * @param type    the achievement type
     * @return the user ID who claimed it, or null if not claimed
     */
    public String getFirstToHolder(String guildId, AchievementType type) {
        Map<AchievementType, String> guildAchievements = achievementClaims.get(guildId);
        if (guildAchievements == null) {
            return null;
        }
        return guildAchievements.get(type);
    }

    /**
     * Gets all achievement claims for a guild.
     *
     * @param guildId the Discord guild ID
     * @return map of achievement type to user ID who claimed it
     */
    public Map<AchievementType, String> getAllClaims(String guildId) {
        Map<AchievementType, String> guildAchievements = achievementClaims.get(guildId);
        if (guildAchievements == null) {
            return Collections.emptyMap();
        }
        return new HashMap<>(guildAchievements);
    }
}

