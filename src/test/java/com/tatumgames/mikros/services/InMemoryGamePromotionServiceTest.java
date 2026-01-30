package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.AppPromotion;
import org.junit.jupiter.api.Test;

import java.time.Instant;
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
        String campaignId = "cmp_hv_nemesis_jan";

        // Initially no step (without campaignId - backward compatibility)
        assertEquals(0, service.getLastPromotionStep(guildId, appId), "Should start with step 0");
        assertFalse(service.hasAppBeenPromoted(guildId, appId), "Should not be promoted initially");
        assertNull(service.getLastAppPostTime(guildId, appId), "Should have no post time initially");

        // Initially no step (with campaignId)
        assertEquals(0, service.getLastPromotionStep(guildId, appId, campaignId), "Should start with step 0");
        assertFalse(service.hasAppBeenPromoted(guildId, appId, campaignId), "Should not be promoted initially");
        assertNull(service.getLastAppPostTime(guildId, appId, campaignId), "Should have no post time initially");

        // Record step 1 with campaignId
        java.time.Instant now = Instant.now();
        service.recordPromotionStep(guildId, appId, campaignId, 1, now);

        assertEquals(1, service.getLastPromotionStep(guildId, appId, campaignId), "Should have step 1");
        assertTrue(service.hasAppBeenPromoted(guildId, appId, campaignId), "Should be marked as promoted");
        assertEquals(now, service.getLastAppPostTime(guildId, appId, campaignId), "Should have correct post time");

        // Verify backward compatibility - appId-only methods still work
        assertEquals(0, service.getLastPromotionStep(guildId, appId), "AppId-only should still be 0 (different key)");
        assertFalse(service.hasAppBeenPromoted(guildId, appId), "AppId-only should not be promoted");
    }

    @Test
    public void testMultipleCampaignsSameAppId() {
        InMemoryGamePromotionService service = new InMemoryGamePromotionService();

        String guildId = "test-guild-456";
        String appId = "hv-nemesis";
        String campaignIdJan = "cmp_hv_nemesis_jan";
        String campaignIdFeb = "cmp_hv_nemesis_feb";

        Instant now = Instant.now();

        // Record step 4 for January campaign
        service.recordPromotionStep(guildId, appId, campaignIdJan, 4, now);

        // Verify January campaign is complete
        assertEquals(4, service.getLastPromotionStep(guildId, appId, campaignIdJan), "January campaign should be at step 4");
        assertTrue(service.hasAppBeenPromoted(guildId, appId, campaignIdJan), "January campaign should be promoted");

        // Verify February campaign starts fresh (step 0)
        assertEquals(0, service.getLastPromotionStep(guildId, appId, campaignIdFeb), "February campaign should start at step 0");
        assertFalse(service.hasAppBeenPromoted(guildId, appId, campaignIdFeb), "February campaign should not be promoted yet");

        // Record step 1 for February campaign
        Instant febTime = now.plusSeconds(3600);
        service.recordPromotionStep(guildId, appId, campaignIdFeb, 1, febTime);

        // Verify both campaigns are tracked independently
        assertEquals(4, service.getLastPromotionStep(guildId, appId, campaignIdJan), "January campaign should still be at step 4");
        assertEquals(1, service.getLastPromotionStep(guildId, appId, campaignIdFeb), "February campaign should be at step 1");
        assertEquals(now, service.getLastAppPostTime(guildId, appId, campaignIdJan), "January should have original time");
        assertEquals(febTime, service.getLastAppPostTime(guildId, appId, campaignIdFeb), "February should have new time");

        // Complete February campaign
        service.recordPromotionStep(guildId, appId, campaignIdFeb, 2, febTime.plusSeconds(3600));
        service.recordPromotionStep(guildId, appId, campaignIdFeb, 3, febTime.plusSeconds(7200));
        service.recordPromotionStep(guildId, appId, campaignIdFeb, 4, febTime.plusSeconds(10800));

        // Verify both campaigns completed independently
        assertEquals(4, service.getLastPromotionStep(guildId, appId, campaignIdJan), "January campaign should still be at step 4");
        assertEquals(4, service.getLastPromotionStep(guildId, appId, campaignIdFeb), "February campaign should be at step 4");
    }
}

