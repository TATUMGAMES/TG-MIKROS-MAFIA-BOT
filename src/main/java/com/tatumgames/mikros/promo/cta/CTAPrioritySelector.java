package com.tatumgames.mikros.promo.cta;

import com.tatumgames.mikros.models.AppPromotion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles CTA selection based on priority tiers for conversion optimization. Prioritizes
 * download/install links over social links.
 */
public class CTAPrioritySelector {
    private static final Random random = new Random();

    /**
     * Selects primary CTAs (Tier 1) based on priority rules. For mobile games with both Google Play
     * and App Store, returns both. Otherwise returns the highest priority single CTA.
     *
     * @param ctas the CTAs object
     * @return list of primary CTA links (empty if none available)
     */
    public List<CTALink> selectPrimaryCTAs(AppPromotion.CTAs ctas) {
        if (ctas == null) {
            return List.of();
        }

        List<CTALink> primaryCTAs = new ArrayList<>();

        // Check if this is a mobile game (both Google Play and App Store exist)
        boolean hasGooglePlay = isValidUrl(ctas.getGoogleStore());
        boolean hasAppStore = isValidUrl(ctas.getAppleStore());

        if (hasGooglePlay && hasAppStore) {
            // Mobile game: show both stores
            primaryCTAs.add(new CTALink("Download on Google Play", ctas.getGoogleStore()));
            primaryCTAs.add(new CTALink("Download on App Store", ctas.getAppleStore()));
            return primaryCTAs;
        }

        // Single mobile platform or non-mobile game
        if (hasGooglePlay) {
            primaryCTAs.add(new CTALink("Get It on Google Play", ctas.getGoogleStore()));
            return primaryCTAs;
        }

        if (hasAppStore) {
            primaryCTAs.add(new CTALink("Download on App Store", ctas.getAppleStore()));
            return primaryCTAs;
        }

        // Check for Steam
        if (isValidUrl(ctas.getSteamStore())) {
            primaryCTAs.add(new CTALink("Wishlist on Steam", ctas.getSteamStore()));
            return primaryCTAs;
        }

        // Check for other Tier 1 stores (Samsung, Amazon, Itch.io)
        if (isValidUrl(ctas.getSamsungStore())) {
            primaryCTAs.add(new CTALink("Get It on Samsung Store", ctas.getSamsungStore()));
            return primaryCTAs;
        }

        if (isValidUrl(ctas.getAmazonStore())) {
            primaryCTAs.add(new CTALink("Get It on Amazon Appstore", ctas.getAmazonStore()));
            return primaryCTAs;
        }

        if (isValidUrl(ctas.getItchStore())) {
            primaryCTAs.add(new CTALink("Get It on Itch.io", ctas.getItchStore()));
            return primaryCTAs;
        }

        // Special case: If no Tier 1 URLs and no Website URL, only 'other' exists
        // Treat 'other' as Tier 1 (as specified in BUGS_01.md)
        if (!isValidUrl(ctas.getWebsite()) && isValidUrl(ctas.getOther())) {
            primaryCTAs.add(new CTALink("Get It Here", ctas.getOther()));
            return primaryCTAs;
        }

        return primaryCTAs;
    }

    /**
     * Selects an optional secondary CTA (Tier 2) if allowed. Only returns a CTA if: - No Tier 1 CTAs
     * exist, OR - allowSecondary is true and random chance (30-40%) passes
     *
     * @param ctas           the CTAs object
     * @param allowSecondary whether secondary CTAs are allowed
     * @return optional secondary CTA link, or null
     */
    public CTALink selectSecondaryCTA(AppPromotion.CTAs ctas, boolean allowSecondary) {
        if (ctas == null) {
            return null;
        }

        // Check if we have Tier 1 CTAs
        boolean hasTier1 = hasTier1CTAs(ctas);

        // If we have Tier 1 CTAs, only show secondary if allowed and random chance passes
        if (hasTier1) {
            if (!allowSecondary) {
                return null;
            }
            // 30-40% chance for secondary CTA
            double chance = 0.30 + (random.nextDouble() * 0.10); // 30-40%
            if (random.nextDouble() >= chance) {
                return null;
            }
        }

        // Select secondary CTA (website or other, but not if other was used as Tier 1)
        if (isValidUrl(ctas.getWebsite())) {
            return new CTALink("Official Website", ctas.getWebsite());
        }

        // Only use 'other' as secondary if it wasn't used as Tier 1
        if (isValidUrl(ctas.getOther()) && hasTier1) {
            return new CTALink("Learn More", ctas.getOther());
        }

        return null;
    }

    /**
     * Checks if the game has both Google Play and App Store (mobile game).
     *
     * @param ctas the CTAs object
     * @return true if both mobile stores exist
     */
    public boolean isMobileGame(AppPromotion.CTAs ctas) {
        if (ctas == null) {
            return false;
        }
        return isValidUrl(ctas.getGoogleStore()) && isValidUrl(ctas.getAppleStore());
    }

    /**
     * Checks if any Tier 1 CTAs exist.
     *
     * @param ctas the CTAs object
     * @return true if any Tier 1 CTA exists
     */
    public boolean hasTier1CTAs(AppPromotion.CTAs ctas) {
        if (ctas == null) {
            return false;
        }
        return isValidUrl(ctas.getGoogleStore())
                || isValidUrl(ctas.getAppleStore())
                || isValidUrl(ctas.getSteamStore())
                || isValidUrl(ctas.getSamsungStore())
                || isValidUrl(ctas.getAmazonStore())
                || isValidUrl(ctas.getItchStore())
                || (!isValidUrl(ctas.getWebsite())
                && isValidUrl(ctas.getOther())); // Special case: other as Tier 1
    }

    /**
     * Gets intent-driven CTA header text based on available primary CTAs.
     *
     * @param ctas the CTAs object
     * @return CTA header text
     */
    public String getIntentDrivenCtaHeader(AppPromotion.CTAs ctas) {
        if (ctas == null) {
            return "🎮 Available Now";
        }

        List<CTALink> primaryCTAs = selectPrimaryCTAs(ctas);
        if (primaryCTAs.isEmpty()) {
            return "🎮 Available Now";
        }

        CTALink firstCTA = primaryCTAs.get(0);
        String displayName = firstCTA.getDisplayName().toLowerCase();

        // Determine header based on primary CTA type
        if (displayName.contains("google play") || displayName.contains("app store")) {
            if (primaryCTAs.size() == 2) {
                // Both mobile stores
                return "⬇️ Download Free";
            }
            return "📱 Get It Now";
        }

        if (displayName.contains("steam")) {
            return "⭐ Wishlist on Steam";
        }

        if (displayName.contains("samsung")
                || displayName.contains("amazon")
                || displayName.contains("itch")) {
            return "🎮 Available Now";
        }

        // Default
        return "🎯 Play Now";
    }

    /**
     * Checks if a URL is valid (not null, not blank, not a placeholder).
     *
     * @param url the URL to check
     * @return true if valid
     */
    private boolean isValidUrl(String url) {
        return url != null && !url.isBlank() && !url.contains("<");
    }

    /**
     * Represents a CTA link with its display name and URL.
     */
    public static class CTALink {
        private final String displayName;
        private final String url;

        public CTALink(String displayName, String url) {
            this.displayName = displayName;
            this.url = url;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getUrl() {
            return url;
        }

        /**
         * Formats the CTA link as a Markdown link.
         *
         * @return formatted link string
         */
        public String toMarkdown() {
            return String.format("[%s](%s)", displayName, url);
        }
    }
}
