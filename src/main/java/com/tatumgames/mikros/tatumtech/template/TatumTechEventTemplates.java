package com.tatumgames.mikros.tatumtech.template;

import java.util.Random;

/**
 * Manages message templates for Tatum Tech event promotions.
 * Contains Version A (pre-event awareness) and Version B (exposure-focused) templates.
 */
public class TatumTechEventTemplates {
    // Version A: Pre-event awareness (most important) - includes donation support
    private static final String VERSION_A_TEMPLATE = """
            🎮 TATUM TECH - CALLING ALL GAME LOVERS!
            
            **Tatum Tech** is a real-world gaming & tech event based in South LA.
            
            📍 What is it?
            • Bi-annual gaming + tech showcase (April and October)
            • 500-600+ attendees
            • 100+ free computers given away to kids/teens in need
            • Videogame workshops led by professional game developers and major game studios
            • Indie games featured on-site & digitally
            
            🎯 Why this matters to you:
            • Real exposure (not ads) for you and your game(s)
            • Games running MIKROS campaigns during the event automatically get featured!
            • Opportunity for sponsorships & partnerships
            
            💝 Support Tatum Tech:
            Help us continue providing free computers and workshops to underserved communities in South LA. Every contribution makes a difference!
            Donate here: {DONATION_URL}
            
            📽️ {RECAP_MONTH_YEAR} Recap Video:
            {RECAP_VIDEO_URL}
            
            📬 Interested in future events, featuring your game, or partnering?
            Learn more through the Tatum Tech app: https://forms.gle/cm9wMsYY6ZpPauWR8
            """;
    // Version B: Exposure-focused (for dev-heavy servers)
    private static final String VERSION_B_TEMPLATE = """
            🚀 TATUM TECH - DEVELOPER EXPOSURE & FUN
            
            Quick heads up:
            Games in the MIKROS ecosystem automatically get exposure through **Tatum Tech**.
            Tatum Tech is a real-world + digital gaming showcase hosted in South LA.
            
            What to expect?
            ✔️ Live events
            ✔️ Game discovery through the Tatum Tech app
            ✔️ Community-driven visibility
            ✔️ Not pay-per-click ads
            
            📽️ See what it looks like:
            {RECAP_VIDEO_URL}
            
            📬 If you want updates, early access, or to explore featuring your game download the
            Tatum Tech app. Early registration is open: https://forms.gle/cm9wMsYY6ZpPauWR8
            """;
    // Donation URL for Tatum Tech support
    private static final String DONATION_URL = "https://buy.stripe.com/7sI3cH8m6bmd5ck4gj";
    // MIKROS Marketing footer messages (same as game promotions)
    private static final String[] MIKROS_FOOTER_MESSAGES = {
            "Powered by MIKROS Marketing — a developer-first platform helping indie games reach real players. Learn more: https://developer.tatumgames.com/",
            "This discovery is powered by MIKROS, a marketing ecosystem built for indie game developers. https://developer.tatumgames.com/",
            "Indie devs: this campaign was distributed using MIKROS Marketing. Get your game discovered here: https://developer.tatumgames.com/",
            "Distributed via MIKROS Marketing, tools and tech that help indie games break through. https://developer.tatumgames.com/",
            "Want visibility like this? MIKROS Marketing helps indie games reach engaged communities. https://developer.tatumgames.com/",
            "Part of the MIKROS Ecosystem, connecting indie games with real players across Discord. https://developer.tatumgames.com/",
            "This campaign is running through MIKROS, a platform built to improve game discovery and reach. https://developer.tatumgames.com/",
            "FYI for developers: campaigns like this are powered by MIKROS Marketing. Learn more: https://developer.tatumgames.com/",
            "Shared via MIKROS Marketing, supporting indie devs through community-driven discovery. https://developer.tatumgames.com/",
            "Powered by MIKROS! Modern marketing tools for indie game developers and small game studios. https://developer.tatumgames.com/"
    };
    private final Random random;

    /**
     * Creates a new TatumTechEventTemplates instance.
     */
    public TatumTechEventTemplates() {
        this.random = new Random();
    }

    /**
     * Gets the Version A template (pre-event awareness).
     *
     * @param recapMonthYear the recap month and year (e.g., "October 2025")
     * @param recapVideoUrl  the recap video URL
     * @return the formatted Version A message
     */
    public String getVersionA(String recapMonthYear, String recapVideoUrl) {
        String message = VERSION_A_TEMPLATE
                .replace("{RECAP_MONTH_YEAR}", recapMonthYear)
                .replace("{RECAP_VIDEO_URL}", recapVideoUrl)
                .replace("{DONATION_URL}", DONATION_URL);
        return message.trim();
    }

    /**
     * Gets the Version B template (exposure-focused).
     *
     * @param recapVideoUrl the recap video URL
     * @return the formatted Version B message
     */
    public String getVersionB(String recapVideoUrl) {
        String message = VERSION_B_TEMPLATE.replace("{RECAP_VIDEO_URL}", recapVideoUrl);
        return message.trim();
    }

    /**
     * Gets a random MIKROS Marketing footer message.
     *
     * @return a random footer message
     */
    public String getRandomMikrosFooter() {
        return MIKROS_FOOTER_MESSAGES[random.nextInt(MIKROS_FOOTER_MESSAGES.length)];
    }
}

