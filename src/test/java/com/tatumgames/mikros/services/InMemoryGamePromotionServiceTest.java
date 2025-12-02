package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.AppPromotion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for InMemoryGamePromotionService to verify JSON loading works.
 */
public class InMemoryGamePromotionServiceTest {

    @Test
    public void testFetchAllApps() {
        InMemoryGamePromotionService service = new InMemoryGamePromotionService();

        List<AppPromotion> apps = service.fetchAllApps();

        assertNotNull(apps, "Apps list should not be null");
        assertFalse(apps.isEmpty(), "Should load at least one app from stub JSON");
        assertTrue(apps.size() >= 2, "Should load at least 2 apps from stub JSON");

        // Verify first app
        AppPromotion firstApp = apps.get(0);
        assertNotNull(firstApp.getAppId(), "App ID should not be null");
        assertNotNull(firstApp.getAppName(), "App name should not be null");
        assertNotNull(firstApp.getShortDescription(), "Short description should not be null");
        assertNotNull(firstApp.getLongDescription(), "Long description should not be null");
        assertNotNull(firstApp.getCampaign(), "Campaign should not be null");

        if (firstApp.getCampaign() != null) {
            assertNotNull(firstApp.getCampaign().getStartDate(), "Campaign start date should not be null");
            assertNotNull(firstApp.getCampaign().getEndDate(), "Campaign end date should not be null");
            assertNotNull(firstApp.getCampaign().getCtas(), "CTAs should not be null");
            assertNotNull(firstApp.getCampaign().getSocialMedia(), "Social media should not be null");

            // Verify CTAs
            if (firstApp.getCampaign().getCtas() != null) {
                assertNotNull(firstApp.getCampaign().getCtas().getWebsite(), "Website CTA should not be null");
            }
        }

        // Verify we can call it multiple times (caching)
        List<AppPromotion> apps2 = service.fetchAllApps();
        assertEquals(apps.size(), apps2.size(), "Cached result should return same number of apps");
        assertEquals(apps.get(0).getAppId(), apps2.get(0).getAppId(), "Cached result should return same apps");
    }

    @Test
    public void testPromotionStepTracking() {
        InMemoryGamePromotionService service = new InMemoryGamePromotionService();

        String guildId = "test-guild-123";
        String appId = "hv-nemesis";

        // Initially no step
        assertEquals(0, service.getLastPromotionStep(guildId, appId), "Should start with step 0");
        assertFalse(service.hasAppBeenPromoted(guildId, appId), "Should not be promoted initially");
        assertNull(service.getLastAppPostTime(guildId, appId), "Should have no post time initially");

        // Record step 1
        java.time.Instant now = java.time.Instant.now();
        service.recordPromotionStep(guildId, appId, 1, now);

        assertEquals(1, service.getLastPromotionStep(guildId, appId), "Should have step 1");
        assertTrue(service.hasAppBeenPromoted(guildId, appId), "Should be marked as promoted");
        assertEquals(now, service.getLastAppPostTime(guildId, appId), "Should have correct post time");
    }
}

