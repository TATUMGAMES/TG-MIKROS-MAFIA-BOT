package com.tatumgames.mikros.promo.template;

import com.tatumgames.mikros.models.AppPromotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Manages message templates for app promotions.
 * <p>
 * Contains 20 templates total (5 per step) with community support and rallying messages.
 */
public class PromotionMessageTemplates {
    private static final Logger logger = LoggerFactory.getLogger(PromotionMessageTemplates.class);
    private final Random random;

    // Step 1: Introduce the game (5 templates total)
    private static final String[] STEP_1_TEMPLATES = {
            "🎮 Introducing <app_name>! <short_description>",
            "Have you heard about <app_name>? <short_description>",
            "Let's all support <app_name>! <short_description>",
            "This game really impressed us - <app_name>! <short_description>",
            "You're going to love <app_name>! <short_description>"
    };

    // Step 2: Add more details (5 templates total)
    private static final String[] STEP_2_TEMPLATES = {
            "Dive deeper into <app_name>: <long_description>",
            "Want to know more about <app_name>? <long_description>",
            "Let's rally behind <app_name> and discover what makes it special: <long_description>",
            "This project put a smile on our face. Here's why <app_name> stands out: <long_description>",
            "Join us in supporting <app_name> - <long_description>"
    };

    // Step 3: Multiple games promotion (5 templates total)
    private static final String[] STEP_3_TEMPLATES = {
            "🌟 MIKROS Top Picks for this month: <game_list>",
            "This month's featured games: <game_list>",
            "Don't miss these MIKROS favorites: <game_list>",
            "Let's rally behind these amazing developers! This month's highlights: <game_list>",
            "These games really impressed us - check them out: <game_list>"
    };

    // Step 4: Final chance (5 templates total)
    private static final String[] STEP_4_TEMPLATES = {
            "⏰ Last chance to check out <app_name>! <short_description>",
            "Don't miss out on <app_name>! <short_description>",
            "Final opportunity: <app_name> - <short_description>",
            "One final rally for <app_name>! <short_description>",
            "Last call to support <app_name> - <short_description>"
    };

    // MIKROS Marketing footer messages
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

    public PromotionMessageTemplates() {
        this.random = new Random();
    }

    /**
     * Gets a random template for a promotion step.
     *
     * @param step the promotion step (1-4)
     * @return a template string
     */
    public String getTemplate(int step) {
        String[] templates = switch (step) {
            case 1 -> STEP_1_TEMPLATES;
            case 2 -> STEP_2_TEMPLATES;
            case 3 -> STEP_3_TEMPLATES;
            case 4 -> STEP_4_TEMPLATES;
            default -> throw new IllegalArgumentException("Invalid step: " + step);
        };

        if (templates.length == 0) {
            logger.warn("No templates available for step {}", step);
            return "";
        }

        return templates[random.nextInt(templates.length)];
    }

    /**
     * Formats a template message by replacing placeholders with actual values.
     *
     * @param template the template string
     * @param app      the app promotion (can be null for step 3)
     * @param allApps  all apps for step 3 (can be null for other steps)
     * @return formatted message
     */
    public String formatMessage(String template, AppPromotion app, List<AppPromotion> allApps) {
        String message = template;

        // Replace app-specific placeholders
        if (app != null) {
            // Make app name bold when it appears in the message
            message = message.replace("<app_name>", "**" + app.getAppName() + "**");
            message = message.replace("<short_description>", app.getShortDescription());
            message = message.replace("<long_description>", app.getLongDescription());
        }

        // Replace game list placeholder (for step 3)
        if (message.contains("<game_list>") && allApps != null && !allApps.isEmpty()) {
            String gameList = allApps.stream()
                    .map(appPromotion -> "**" + appPromotion.getAppName() + "**")
                    .collect(Collectors.joining(", "));
            message = message.replace("<game_list>", gameList);
        }

        return message;
    }

    /**
     * Gets a random CTA (Call to Action) text.
     *
     * @return CTA text
     */
    public String getRandomCta() {
        String[] ctas = {
                "Where to Get It?:",
                "Play It Today:",
                "Try It Out Today:",
                "Play It Here:",
                "Download Now:"
        };
        return ctas[random.nextInt(ctas.length)];
    }

    /**
     * Formats a CTA link as a Markdown link.
     *
     * @param storeName the name of the store
     * @param url       the URL
     * @return formatted link
     */
    public String formatCtaLink(String storeName, String url) {
        if (url == null || url.isBlank() || url.contains("<")) {
            return null; // Skip placeholder URLs
        }
        return String.format("[%s](%s)", storeName, url);
    }

    /**
     * Gets a random social media link from available social media.
     * Returns null if no social media available or randomly skipped (~70% chance).
     *
     * @param socialMedia the social media object
     * @return formatted social media link, or null
     */
    public String getRandomSocialMediaLink(AppPromotion.SocialMedia socialMedia) {
        if (socialMedia == null) {
            return null;
        }

        // 30% chance to include social media
        if (random.nextInt(100) >= 30) {
            return null;
        }

        // Collect available social media links
        List<String> availableLinks = new java.util.ArrayList<>();
        if (socialMedia.getFacebook() != null && !socialMedia.getFacebook().contains("<")) {
            availableLinks.add("[Facebook](" + socialMedia.getFacebook() + ")");
        }
        if (socialMedia.getX() != null && !socialMedia.getX().contains("<")) {
            availableLinks.add("[Twitter/X](" + socialMedia.getX() + ")");
        }
        if (socialMedia.getInstagram() != null && !socialMedia.getInstagram().contains("<")) {
            availableLinks.add("[Instagram](" + socialMedia.getInstagram() + ")");
        }
        if (socialMedia.getYoutube() != null && !socialMedia.getYoutube().contains("<")) {
            availableLinks.add("[YouTube](" + socialMedia.getYoutube() + ")");
        }
        if (socialMedia.getDiscord() != null && !socialMedia.getDiscord().contains("<")) {
            availableLinks.add("[Discord](" + socialMedia.getDiscord() + ")");
        }

        if (availableLinks.isEmpty()) {
            return null;
        }

        return availableLinks.get(random.nextInt(availableLinks.size()));
    }

    /**
     * Gets a list of available CTA links from the app's campaign.
     * Filters out placeholder URLs.
     *
     * @param app the app promotion
     * @return list of formatted CTA links
     */
    public List<String> getAvailableCtas(AppPromotion app) {
        if (app.getCampaign() == null || app.getCampaign().getCtas() == null) {
            return List.of();
        }

        AppPromotion.CTAs ctas = app.getCampaign().getCtas();
        List<String> available = new java.util.ArrayList<>();

        if (ctas.getWebsite() != null && !ctas.getWebsite().contains("<")) {
            available.add(formatCtaLink("Website", ctas.getWebsite()));
        }
        if (ctas.getGoogleStore() != null && !ctas.getGoogleStore().contains("<")) {
            available.add(formatCtaLink("Google Play", ctas.getGoogleStore()));
        }
        if (ctas.getAppleStore() != null && !ctas.getAppleStore().contains("<")) {
            available.add(formatCtaLink("App Store", ctas.getAppleStore()));
        }
        if (ctas.getSteamStore() != null && !ctas.getSteamStore().contains("<")) {
            available.add(formatCtaLink("Steam", ctas.getSteamStore()));
        }
        if (ctas.getSamsungStore() != null && !ctas.getSamsungStore().contains("<")) {
            available.add(formatCtaLink("Samsung Store", ctas.getSamsungStore()));
        }
        if (ctas.getAmazonStore() != null && !ctas.getAmazonStore().contains("<")) {
            available.add(formatCtaLink("Amazon Appstore", ctas.getAmazonStore()));
        }
        if (ctas.getOther() != null && !ctas.getOther().contains("<")) {
            available.add(formatCtaLink("Other", ctas.getOther()));
        }

        return available.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Gets a random MIKROS Marketing footer message.
     *
     * @return a random footer message
     */
    public String getRandomMikrosFooter() {
        return MIKROS_FOOTER_MESSAGES[random.nextInt(MIKROS_FOOTER_MESSAGES.length)];
    }

    /**
     * Determines if the MIKROS Marketing footer should be shown for a given step.
     * Always shows on step 4 (final step) and step 3 (multi-game).
     * Shows randomly (~35% chance) on steps 1 and 2.
     *
     * @param step the promotion step (1-4)
     * @return true if footer should be shown
     */
    public boolean shouldShowMikrosFooter(int step) {
        // Always show on step 3 (multi-game) and step 4 (final step)
        if (step == 3 || step == 4) {
            return true;
        }
        // 35% chance on steps 1 and 2
        return random.nextInt(100) < 35;
    }
}

