package com.tatumgames.mikros.promo.manager;

import com.tatumgames.mikros.models.AppPromotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Manages dynamic promotion steps based on campaign duration.
 *
 * <p>Calculates step count based on campaign duration: - 1 month (≤30 days): 3 steps - 2 months
 * (31-60 days): 6 steps - 3 months (61-90 days): 9 steps - 4+ months: 3 + (3 × months), capped at
 * 15 steps
 *
 * <p>Steps are distributed evenly from 0% to 90% of campaign duration, with story progression:
 * Introduction → Deep Dive → Reminder → Multi-Game → Final Chance
 */
public class PromotionStepManager {
    private static final Logger logger = LoggerFactory.getLogger(PromotionStepManager.class);

    // Minimum interval between any two promotions (24 hours)
    private static final long MIN_INTERVAL_HOURS = 24;

    // Maximum step count cap
    private static final int MAX_STEP_COUNT = 15;

    /**
     * Determines which promotion step should be posted next for an app. Supports variable step counts
     * based on campaign duration.
     *
     * @param app           the app promotion
     * @param lastStep      the last step posted (0 if never posted)
     * @param lastPostTime  the time when the last step was posted (null if never posted)
     * @param allActiveApps all apps currently in campaign (for multi-game step)
     * @param now           current time
     * @return the step to post (1 to totalSteps), or 0 if none should be posted yet
     */
    public int determineNextStep(
            AppPromotion app,
            int lastStep,
            Instant lastPostTime,
            List<AppPromotion> allActiveApps,
            Instant now) {
        if (app.getCampaign() == null) {
            logger.debug("App {} has no campaign", app.getAppId());
            return 0;
        }

        Instant campaignStart = app.getCampaign().getStartDate();
        Instant campaignEnd = app.getCampaign().getEndDate();

        // Check if campaign is active
        if (now.isBefore(campaignStart) || now.isAfter(campaignEnd)) {
            logger.debug(
                    "App {} campaign not active (start: {}, end: {}, now: {})",
                    app.getAppId(),
                    campaignStart,
                    campaignEnd,
                    now);
            return 0;
        }

        // Check minimum interval if we've posted before
        if (lastPostTime != null) {
            Instant nextAllowedTime = lastPostTime.plus(MIN_INTERVAL_HOURS, ChronoUnit.HOURS);
            if (now.isBefore(nextAllowedTime)) {
                logger.debug(
                        "App {} too soon to post again (last: {}, next allowed: {})",
                        app.getAppId(),
                        lastPostTime,
                        nextAllowedTime);
                return 0;
            }
        }

        // Calculate total step count based on campaign duration
        int totalSteps = calculateStepCount(campaignStart, campaignEnd);

        // Calculate multi-game step position
        int multiGameStepPosition = calculateMultiGameStepPosition(totalSteps);
        boolean hasMultipleApps =
                allActiveApps.stream().filter(AppPromotion::isCampaignActive).count() >= 2;

        // Check each step in sequence to see if it's ready to post
        for (int step = 1; step <= totalSteps; step++) {
            // Skip if we've already posted this step or beyond
            if (lastStep >= step) {
                continue;
            }

            // Skip multi-game step if only one app
            if (step == multiGameStepPosition && !hasMultipleApps) {
                // Skip multi-game step, continue to next
                continue;
            }

            // Calculate target time for this step
            Instant stepTargetTime =
                    calculateStepTargetTime(campaignStart, campaignEnd, step, totalSteps);

            // Check if it's time to post this step
            if (now.isAfter(stepTargetTime) || now.equals(stepTargetTime)) {
                // For multi-game step, ensure prerequisite steps are done
                if (step == multiGameStepPosition && hasMultipleApps) {
                    // Multi-game step requires at least step 2 to be completed
                    if (lastStep >= 2) {
                        return step;
                    }
                } else {
                    // Regular step - check if we can post it
                    // Step 1 can always be posted if time has come
                    if (step == 1) {
                        return step;
                    }

                    // For other steps, check if previous step was completed
                    // But account for skipped steps (like multi-game)
                    int previousStep = step - 1;
                    if (previousStep == multiGameStepPosition && !hasMultipleApps) {
                        // Previous step was skipped multi-game, check step before that
                        previousStep = step - 2;
                    }

                    if (lastStep >= previousStep) {
                        return step;
                    }
                }
            }
        }

        // No step ready to post yet
        return 0;
    }

    /**
     * Calculates the number of promotion steps based on campaign duration. Formula: 3 steps for 1
     * month, +3 steps per additional month.
     *
     * @param campaignStartDate campaign start date
     * @param campaignEndDate   campaign end date
     * @return the number of steps (3-15)
     */
    public int calculateStepCount(Instant campaignStartDate, Instant campaignEndDate) {
        long campaignDurationDays = ChronoUnit.DAYS.between(campaignStartDate, campaignEndDate);
        double months = campaignDurationDays / 30.0;

        // Formula: 3 + (3 × (months - 1))
        // For 1 month: 3 + (3 × 0) = 3
        // For 2 months: 3 + (3 × 1) = 6
        // For 3 months: 3 + (3 × 2) = 9
        int stepCount = 3 + (int) Math.round(3 * (months - 1));

        // Ensure minimum of 3 and maximum cap
        stepCount = Math.max(3, Math.min(stepCount, MAX_STEP_COUNT));

        logger.debug(
                "Calculated step count: {} steps for {} days ({} months)",
                stepCount,
                campaignDurationDays,
                String.format("%.1f", months));

        return stepCount;
    }

    /**
     * Calculates when a promotion step should be posted. Distributes steps evenly from 0% to 90% of
     * campaign duration.
     *
     * @param campaignStartDate campaign start date
     * @param campaignEndDate   campaign end date
     * @param step              the promotion step (1 to totalSteps)
     * @param totalSteps        total number of steps for this campaign
     * @return the target time for this step
     */
    public Instant calculateStepTargetTime(
            Instant campaignStartDate, Instant campaignEndDate, int step, int totalSteps) {
        long campaignDurationHours = ChronoUnit.HOURS.between(campaignStartDate, campaignEndDate);

        if (step < 1 || step > totalSteps) {
            throw new IllegalArgumentException(
                    "Step " + step + " is out of range (1-" + totalSteps + ")");
        }

        // Step 1: Always at 0% (campaign start)
        if (step == 1) {
            return campaignStartDate;
        }

        // Final step: Always at 90%
        if (step == totalSteps) {
            return campaignStartDate.plus(campaignDurationHours * 9 / 10, ChronoUnit.HOURS);
        }

        // Steps 2 to N-1: Evenly distributed between 0% and 90%
        // Formula: startDate + (duration * (step - 1) / (totalSteps - 1)) * 0.9
        // This distributes steps evenly from 0% to 90%
        double progress = (double) (step - 1) / (totalSteps - 1) * 0.9;
        long hoursOffset = (long) (campaignDurationHours * progress);

        return campaignStartDate.plus(hoursOffset, ChronoUnit.HOURS);
    }

    /**
     * Legacy method for backward compatibility (assumes 4 steps).
     *
     * @deprecated Use calculateStepTargetTime(Instant, Instant, int, int) instead
     */
    @Deprecated
    public Instant calculateStepTargetTime(
            Instant campaignStartDate, Instant campaignEndDate, int step) {
        return calculateStepTargetTime(campaignStartDate, campaignEndDate, step, 4);
    }

    /**
     * Determines the step type based on step number and total steps.
     *
     * @param step            the step number (1 to totalSteps)
     * @param totalSteps      total number of steps
     * @param isMultiGameStep whether this is the multi-game step
     * @return the step type
     */
    public StepType getStepType(int step, int totalSteps, boolean isMultiGameStep) {
        if (step < 1 || step > totalSteps) {
            throw new IllegalArgumentException(
                    "Step " + step + " is out of range (1-" + totalSteps + ")");
        }

        // Step 1: Always introduction
        if (step == 1) {
            return StepType.INTRODUCTION;
        }

        // Final step: Always final chance
        if (step == totalSteps) {
            return StepType.FINAL_CHANCE;
        }

        // Multi-game step: Special type
        if (isMultiGameStep) {
            return StepType.MULTI_GAME;
        }

        // Even steps (2, 4, 6...): Deep dive (long description)
        // Odd steps (3, 5, 7...): Reminder (short description)
        if (step % 2 == 0) {
            return StepType.DEEP_DIVE;
        } else {
            return StepType.REMINDER;
        }
    }

    /**
     * Calculates the position (as step number) where multi-game promotion should appear. Returns 50%
     * position for campaigns ≥2 months, closest to 66% for 1-month campaigns.
     *
     * @param totalSteps total number of steps
     * @return the step number where multi-game should appear, or 0 if not applicable
     */
    public int calculateMultiGameStepPosition(int totalSteps) {
        if (totalSteps < 3) {
            return 0; // Not enough steps for multi-game
        }

        // For 1 month (3 steps): Multi-game at step 2 (closest to 66%, actually at 45%)
        // For 2+ months (6+ steps): Multi-game at 50% position
        if (totalSteps == 3) {
            // With 3 steps: step 1=0%, step 2=45%, step 3=90%
            // 66% is closer to step 2 (45%) than step 3 (90%), so use step 2
            return 2;
        } else {
            // Calculate step at 50% position
            // Steps are distributed 0% to 90%, so 50% is at: (50/90) * (totalSteps - 1) + 1
            double position = (50.0 / 90.0) * (totalSteps - 1) + 1;
            return (int) Math.round(position);
        }
    }

    /**
     * Determines if multi-game promotion should be posted. Uses dynamic step position based on
     * campaign duration.
     *
     * @param allActiveApps     all apps currently in campaign
     * @param lastStepForApps   the last step posted for apps (check first app as representative)
     * @param campaignStartDate campaign start date
     * @param campaignEndDate   campaign end date
     * @param now               current time
     * @return true if multi-game step should be posted
     */
    public boolean shouldPostStep3(
            List<AppPromotion> allActiveApps,
            int lastStepForApps,
            Instant campaignStartDate,
            Instant campaignEndDate,
            Instant now) {
        // Need at least 2 apps for multi-game step
        if (allActiveApps.size() < 2) {
            return false;
        }

        // Calculate total steps and multi-game position
        int totalSteps = calculateStepCount(campaignStartDate, campaignEndDate);
        int multiGameStepPosition = calculateMultiGameStepPosition(totalSteps);

        if (multiGameStepPosition == 0) {
            return false; // Not enough steps
        }

        // Check if prerequisite steps are done (need at least step 2)
        if (lastStepForApps < 2) {
            return false;
        }

        // Check if multi-game step has already been posted
        if (lastStepForApps >= multiGameStepPosition) {
            return false;
        }

        // Check if it's time for multi-game step
        Instant multiGameTime =
                calculateStepTargetTime(
                        campaignStartDate, campaignEndDate, multiGameStepPosition, totalSteps);
        return now.isAfter(multiGameTime) || now.equals(multiGameTime);
    }

    /**
     * Gets the step number for multi-game promotion based on campaign duration.
     *
     * @param campaignStartDate campaign start date
     * @param campaignEndDate   campaign end date
     * @return the step number for multi-game, or 0 if not applicable
     */
    public int getMultiGameStepPosition(Instant campaignStartDate, Instant campaignEndDate) {
        int totalSteps = calculateStepCount(campaignStartDate, campaignEndDate);
        return calculateMultiGameStepPosition(totalSteps);
    }

    /**
     * Step type enum for determining message content and templates.
     */
    public enum StepType {
        INTRODUCTION, // Step 1: Introduction at campaign start
        DEEP_DIVE, // Steps with long description (even steps: 2, 4, 6...)
        REMINDER, // Steps with short description (odd steps: 3, 5, 7...)
        MULTI_GAME, // Special step for multi-game promotion
        FINAL_CHANCE // Final step at 90%
    }
}
