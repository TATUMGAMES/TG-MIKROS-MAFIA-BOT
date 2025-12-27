package com.tatumgames.mikros.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking promotion channel onboarding phases per guild.
 * Tracks when bot first sees each guild and which onboarding phases have been completed.
 */
public class PromotionOnboardingService {
    private static final Logger logger = LoggerFactory.getLogger(PromotionOnboardingService.class);

    /**
     * Enum representing onboarding phases.
     */
    public enum Phase {
        PHASE_1_SOFT_AWARENESS(1),  // 1 hour
        PHASE_2_EXPECTATION(24),    // 24 hours
        PHASE_3_AUTO_ASSIST(48);     // 48 hours

        private final int hoursDelay;

        Phase(int hoursDelay) {
            this.hoursDelay = hoursDelay;
        }

        public int getHoursDelay() {
            return hoursDelay;
        }
    }

    // Map of guildId -> first seen timestamp
    private final Map<String, Instant> guildFirstSeen;
    
    // Map of guildId -> set of completed phases
    private final Map<String, Set<Phase>> completedPhases;

    /**
     * Creates a new PromotionOnboardingService.
     */
    public PromotionOnboardingService() {
        this.guildFirstSeen = new ConcurrentHashMap<>();
        this.completedPhases = new ConcurrentHashMap<>();
        logger.info("PromotionOnboardingService initialized");
    }

    /**
     * Records when the bot first sees a guild.
     * Only records if not already recorded (idempotent).
     *
     * @param guildId the guild ID
     */
    public void recordGuildFirstSeen(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return;
        }

        guildFirstSeen.putIfAbsent(guildId, Instant.now());
        logger.debug("Recorded first seen time for guild {}", guildId);
    }

    /**
     * Gets the first seen time for a guild.
     *
     * @param guildId the guild ID
     * @return the first seen timestamp, or null if not recorded
     */
    public Instant getGuildFirstSeenTime(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return null;
        }
        return guildFirstSeen.get(guildId);
    }

    /**
     * Marks a phase as completed for a guild.
     *
     * @param guildId the guild ID
     * @param phase   the phase that was completed
     */
    public void markPhaseCompleted(String guildId, Phase phase) {
        if (guildId == null || guildId.isBlank() || phase == null) {
            return;
        }

        completedPhases.computeIfAbsent(guildId, k -> new HashSet<>()).add(phase);
        logger.debug("Marked phase {} as completed for guild {}", phase, guildId);
    }

    /**
     * Checks if a phase has been completed for a guild.
     *
     * @param guildId the guild ID
     * @param phase   the phase to check
     * @return true if the phase has been completed, false otherwise
     */
    public boolean hasPhaseCompleted(String guildId, Phase phase) {
        if (guildId == null || guildId.isBlank() || phase == null) {
            return false;
        }

        Set<Phase> completed = completedPhases.get(guildId);
        return completed != null && completed.contains(phase);
    }

    /**
     * Checks if a phase should be processed for a guild.
     * A phase should be processed if:
     * - Enough time has elapsed since first seen
     * - The phase hasn't been completed yet
     *
     * @param guildId the guild ID
     * @param phase   the phase to check
     * @return true if the phase should be processed, false otherwise
     */
    public boolean shouldProcessPhase(String guildId, Phase phase) {
        if (guildId == null || guildId.isBlank() || phase == null) {
            return false;
        }

        Instant firstSeen = getGuildFirstSeenTime(guildId);
        if (firstSeen == null) {
            // Guild not recorded yet - record it now
            recordGuildFirstSeen(guildId);
            return false; // Not enough time has passed
        }

        // Check if phase already completed
        if (hasPhaseCompleted(guildId, phase)) {
            return false;
        }

        // Check if enough time has elapsed
        Instant now = Instant.now();
        long hoursElapsed = java.time.temporal.ChronoUnit.HOURS.between(firstSeen, now);
        
        return hoursElapsed >= phase.getHoursDelay();
    }

    /**
     * Gets all completed phases for a guild.
     *
     * @param guildId the guild ID
     * @return set of completed phases, or empty set if none
     */
    public Set<Phase> getCompletedPhases(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return Collections.emptySet();
        }
        return completedPhases.getOrDefault(guildId, Collections.emptySet());
    }

    /**
     * Clears all onboarding data for a guild (for testing or cleanup).
     *
     * @param guildId the guild ID
     */
    public void clearGuildData(String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return;
        }
        guildFirstSeen.remove(guildId);
        completedPhases.remove(guildId);
        logger.debug("Cleared onboarding data for guild {}", guildId);
    }
}

