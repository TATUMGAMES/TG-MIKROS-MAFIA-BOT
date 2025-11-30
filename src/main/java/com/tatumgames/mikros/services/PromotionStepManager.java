package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.AppPromotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Manages the 4-step promotion story format for app promotions.
 * 
 * Step 1: Introduce the game (at campaign start)
 * Step 2: Add more details (33% through campaign)
 * Step 3: Multiple games promotion (66% through campaign, only if multiple games exist)
 * Step 4: Final chance (90% through campaign)
 */
public class PromotionStepManager {
    private static final Logger logger = LoggerFactory.getLogger(PromotionStepManager.class);
    
    // Minimum interval between any two promotions (24 hours)
    private static final long MIN_INTERVAL_HOURS = 24;
    
    /**
     * Determines which promotion step should be posted next for an app.
     * 
     * @param app the app promotion
     * @param lastStep the last step posted (0 if never posted)
     * @param lastPostTime the time when the last step was posted (null if never posted)
     * @param allActiveApps all apps currently in campaign (for step 3)
     * @param now current time
     * @return the step to post (1-4), or 0 if none should be posted yet
     */
    public int determineNextStep(AppPromotion app, int lastStep, Instant lastPostTime,
                                List<AppPromotion> allActiveApps, Instant now) {
        if (app.getCampaign() == null) {
            logger.debug("App {} has no campaign", app.getAppId());
            return 0;
        }
        
        Instant campaignStart = app.getCampaign().getStartDate();
        Instant campaignEnd = app.getCampaign().getEndDate();
        
        // Check if campaign is active
        if (now.isBefore(campaignStart) || now.isAfter(campaignEnd)) {
            logger.debug("App {} campaign not active (start: {}, end: {}, now: {})",
                    app.getAppId(), campaignStart, campaignEnd, now);
            return 0;
        }
        
        // Check minimum interval if we've posted before
        if (lastPostTime != null) {
            Instant nextAllowedTime = lastPostTime.plus(MIN_INTERVAL_HOURS, ChronoUnit.HOURS);
            if (now.isBefore(nextAllowedTime)) {
                logger.debug("App {} too soon to post again (last: {}, next allowed: {})",
                        app.getAppId(), lastPostTime, nextAllowedTime);
                return 0;
            }
        }
        
        // Determine which step should be posted
        Instant step1Time = calculateStepTargetTime(campaignStart, campaignEnd, 1);
        Instant step2Time = calculateStepTargetTime(campaignStart, campaignEnd, 2);
        Instant step3Time = calculateStepTargetTime(campaignStart, campaignEnd, 3);
        Instant step4Time = calculateStepTargetTime(campaignStart, campaignEnd, 4);
        
        // Check if we should post step 1
        if (lastStep == 0 && now.isAfter(step1Time)) {
            return 1;
        }
        
        // Check if we should post step 2
        if (lastStep == 1 && now.isAfter(step2Time)) {
            return 2;
        }
        
        // Check if we should post step 3 (only if multiple games exist)
        if (lastStep == 2 && now.isAfter(step3Time)) {
            // Step 3 requires multiple active apps
            long activeAppCount = allActiveApps.stream()
                    .filter(a -> a.isCampaignActive())
                    .count();
            if (activeAppCount >= 2) {
                return 3;
            } else {
                // Skip step 3 if only one game, go to step 4
                logger.debug("Skipping step 3 for app {} - only {} active app(s)", app.getAppId(), activeAppCount);
                if (now.isAfter(step4Time)) {
                    return 4;
                }
            }
        }
        
        // Check if we should post step 4
        if ((lastStep == 3 || (lastStep == 2 && allActiveApps.stream()
                .filter(AppPromotion::isCampaignActive)
                .count() < 2)) && now.isAfter(step4Time)) {
            return 4;
        }
        
        // No step ready to post yet
        return 0;
    }
    
    /**
     * Calculates when a promotion step should be posted.
     * Distributes 4 promotions across the campaign period.
     * 
     * @param campaignStartDate campaign start date
     * @param campaignEndDate campaign end date
     * @param step the promotion step (1-4)
     * @return the target time for this step
     */
    public Instant calculateStepTargetTime(Instant campaignStartDate, Instant campaignEndDate, int step) {
        long campaignDurationHours = ChronoUnit.HOURS.between(campaignStartDate, campaignEndDate);
        
        switch (step) {
            case 1:
                // Step 1: At campaign start (or as soon as possible)
                return campaignStartDate;
                
            case 2:
                // Step 2: 33% through campaign period
                return campaignStartDate.plus(campaignDurationHours / 3, ChronoUnit.HOURS);
                
            case 3:
                // Step 3: 66% through campaign period
                return campaignStartDate.plus(campaignDurationHours * 2 / 3, ChronoUnit.HOURS);
                
            case 4:
                // Step 4: 90% through campaign period (near end)
                return campaignStartDate.plus(campaignDurationHours * 9 / 10, ChronoUnit.HOURS);
                
            default:
                throw new IllegalArgumentException("Invalid step: " + step);
        }
    }
    
    /**
     * Determines if step 3 (multi-game promotion) should be posted.
     * 
     * @param allActiveApps all apps currently in campaign
     * @param lastStepForApps the last step posted for apps (check first app as representative)
     * @param campaignStartDate campaign start date
     * @param campaignEndDate campaign end date
     * @param now current time
     * @return true if step 3 should be posted
     */
    public boolean shouldPostStep3(List<AppPromotion> allActiveApps, int lastStepForApps,
                                  Instant campaignStartDate, Instant campaignEndDate, Instant now) {
        // Need at least 2 apps for step 3
        if (allActiveApps.size() < 2) {
            return false;
        }
        
        // Check if any app has reached step 2 (prerequisite for step 3)
        if (lastStepForApps < 2) {
            return false;
        }
        
        // Check if step 3 has already been posted
        if (lastStepForApps >= 3) {
            return false;
        }
        
        // Check if it's time for step 3 (66% through campaign)
        Instant step3Time = calculateStepTargetTime(campaignStartDate, campaignEndDate, 3);
        return now.isAfter(step3Time);
    }
}

