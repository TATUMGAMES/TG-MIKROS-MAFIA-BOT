package com.tatumgames.mikros.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents an app promotion from the /getAllApps API endpoint.
 * Matches the structure returned by the Tatum Games API.
 */
@JsonDeserialize(builder = AppPromotion.Builder.class)
public class AppPromotion {
    private final String appId;
    private final String appGameId;
    private final String appName;
    private final String shortDescription;
    private final String longDescription;
    private final String gameGenre;
    private final String gameplayType;
    private final String contentGenre;
    private final String contentTheme;
    private final Campaign campaign;

    private AppPromotion(Builder builder) {
        this.appId = builder.appId;
        this.appGameId = builder.appGameId;
        this.appName = builder.appName;
        this.shortDescription = builder.shortDescription;
        this.longDescription = builder.longDescription;
        this.gameGenre = builder.gameGenre;
        this.gameplayType = builder.gameplayType;
        this.contentGenre = builder.contentGenre;
        this.contentTheme = builder.contentTheme;
        this.campaign = builder.campaign;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppGameId() {
        return appGameId;
    }

    public String getAppName() {
        return appName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getGameGenre() {
        return gameGenre;
    }

    public String getGameplayType() {
        return gameplayType;
    }

    public String getContentGenre() {
        return contentGenre;
    }

    public String getContentTheme() {
        return contentTheme;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    /**
     * Checks if this app's campaign is currently active.
     *
     * @return true if current time is between campaign start and end dates
     */
    public boolean isCampaignActive() {
        if (campaign == null) {
            return false;
        }
        Instant now = Instant.now();
        return now.isAfter(campaign.getStartDate()) && now.isBefore(campaign.getEndDate());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        @JsonProperty("appId")
        private String appId;

        @JsonProperty("appGameId")
        private String appGameId;

        @JsonProperty("appName")
        private String appName;

        @JsonProperty("shortDescription")
        private String shortDescription;

        @JsonProperty("longDescription")
        private String longDescription;

        @JsonProperty("gameGenre")
        private String gameGenre;

        @JsonProperty("gameplayType")
        private String gameplayType;

        @JsonProperty("contentGenre")
        private String contentGenre;

        @JsonProperty("contentTheme")
        private String contentTheme;

        @JsonProperty("campaign")
        private Campaign campaign;

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder appGameId(String appGameId) {
            this.appGameId = appGameId;
            return this;
        }

        public Builder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder shortDescription(String shortDescription) {
            this.shortDescription = shortDescription;
            return this;
        }

        public Builder longDescription(String longDescription) {
            this.longDescription = longDescription;
            return this;
        }

        public Builder gameGenre(String gameGenre) {
            this.gameGenre = gameGenre;
            return this;
        }

        public Builder gameplayType(String gameplayType) {
            this.gameplayType = gameplayType;
            return this;
        }

        public Builder contentGenre(String contentGenre) {
            this.contentGenre = contentGenre;
            return this;
        }

        public Builder contentTheme(String contentTheme) {
            this.contentTheme = contentTheme;
            return this;
        }

        public Builder campaign(Campaign campaign) {
            this.campaign = campaign;
            return this;
        }

        public AppPromotion build() {
            return new AppPromotion(this);
        }
    }

    /**
     * Represents a campaign for an app promotion.
     */
    @JsonDeserialize(builder = Campaign.Builder.class)
    public static class Campaign {
        private final String campaignId;
        private final String campaignName;
        private final Instant startDate;
        private final Instant endDate;
        private final List<ImageInfo> images;
        private final CTAs ctas;
        private final List<String> screenshotUrls;
        private final List<String> videoUrls;
        private final SocialMedia socialMedia;

        private Campaign(Builder builder) {
            this.campaignId = builder.campaignId;
            this.campaignName = builder.campaignName;
            this.startDate = builder.startDate;
            this.endDate = builder.endDate;
            this.images = builder.images;
            this.ctas = builder.ctas;
            this.screenshotUrls = builder.screenshotUrls;
            this.videoUrls = builder.videoUrls;
            this.socialMedia = builder.socialMedia;
        }

        public String getCampaignId() {
            return campaignId;
        }

        public String getCampaignName() {
            return campaignName;
        }

        public Instant getStartDate() {
            return startDate;
        }

        public Instant getEndDate() {
            return endDate;
        }

        public List<ImageInfo> getImages() {
            return images;
        }

        public CTAs getCtas() {
            return ctas;
        }

        public List<String> getScreenshotUrls() {
            return screenshotUrls;
        }

        public List<String> getVideoUrls() {
            return videoUrls;
        }

        public SocialMedia getSocialMedia() {
            return socialMedia;
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            @JsonProperty("campaignId")
            private String campaignId;

            @JsonProperty("campaignName")
            private String campaignName;

            private Instant startDate;
            private Instant endDate;

            @JsonProperty("images")
            private List<ImageInfo> images;

            @JsonProperty("ctas")
            private CTAs ctas;

            @JsonProperty("screenshotUrls")
            private List<String> screenshotUrls;

            @JsonProperty("videoUrls")
            private List<String> videoUrls;

            @JsonProperty("socialMedia")
            private SocialMedia socialMedia;

            public Builder campaignId(String campaignId) {
                this.campaignId = campaignId;
                return this;
            }

            public Builder campaignName(String campaignName) {
                this.campaignName = campaignName;
                return this;
            }

            @JsonProperty("startDate")
            @JsonDeserialize(using = UnixTimestampDeserializer.class)
            public Builder startDate(Instant startDate) {
                this.startDate = startDate;
                return this;
            }

            @JsonProperty("endDate")
            @JsonDeserialize(using = UnixTimestampDeserializer.class)
            public Builder endDate(Instant endDate) {
                this.endDate = endDate;
                return this;
            }

            public Builder images(List<ImageInfo> images) {
                this.images = images;
                return this;
            }

            public Builder ctas(CTAs ctas) {
                this.ctas = ctas;
                return this;
            }

            public Builder screenshotUrls(List<String> screenshotUrls) {
                this.screenshotUrls = screenshotUrls;
                return this;
            }

            public Builder videoUrls(List<String> videoUrls) {
                this.videoUrls = videoUrls;
                return this;
            }

            public Builder socialMedia(SocialMedia socialMedia) {
                this.socialMedia = socialMedia;
                return this;
            }

            public Campaign build() {
                return new Campaign(this);
            }
        }
    }

    /**
     * Represents image information in a campaign.
     */
    @JsonDeserialize(builder = ImageInfo.Builder.class)
    public static class ImageInfo {
        private final String appLogo;

        private ImageInfo(Builder builder) {
            this.appLogo = builder.appLogo;
        }

        public String getAppLogo() {
            return appLogo;
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            @JsonProperty("appLogo")
            private String appLogo;

            public Builder appLogo(String appLogo) {
                this.appLogo = appLogo;
                return this;
            }

            public ImageInfo build() {
                return new ImageInfo(this);
            }
        }
    }

    /**
     * Represents call-to-action links for various stores.
     */
    @JsonDeserialize(builder = CTAs.Builder.class)
    public static class CTAs {
        private final String googleStore;
        private final String appleStore;
        private final String steamStore;
        private final String samsungStore;
        private final String amazonStore;
        private final String website;
        private final String other;

        private CTAs(Builder builder) {
            this.googleStore = builder.googleStore;
            this.appleStore = builder.appleStore;
            this.steamStore = builder.steamStore;
            this.samsungStore = builder.samsungStore;
            this.amazonStore = builder.amazonStore;
            this.website = builder.website;
            this.other = builder.other;
        }

        @JsonProperty("google_store")
        public String getGoogleStore() {
            return googleStore;
        }

        @JsonProperty("apple_store")
        public String getAppleStore() {
            return appleStore;
        }

        @JsonProperty("steam_store")
        public String getSteamStore() {
            return steamStore;
        }

        @JsonProperty("samsung_store")
        public String getSamsungStore() {
            return samsungStore;
        }

        @JsonProperty("amazon_store")
        public String getAmazonStore() {
            return amazonStore;
        }

        public String getWebsite() {
            return website;
        }

        public String getOther() {
            return other;
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private String googleStore;
            private String appleStore;
            private String steamStore;
            private String samsungStore;
            private String amazonStore;
            private String website;
            private String other;

            @JsonProperty("google_store")
            public Builder googleStore(String googleStore) {
                this.googleStore = googleStore;
                return this;
            }

            @JsonProperty("apple_store")
            public Builder appleStore(String appleStore) {
                this.appleStore = appleStore;
                return this;
            }

            @JsonProperty("steam_store")
            public Builder steamStore(String steamStore) {
                this.steamStore = steamStore;
                return this;
            }

            @JsonProperty("samsung_store")
            public Builder samsungStore(String samsungStore) {
                this.samsungStore = samsungStore;
                return this;
            }

            @JsonProperty("amazon_store")
            public Builder amazonStore(String amazonStore) {
                this.amazonStore = amazonStore;
                return this;
            }

            @JsonProperty("website")
            public Builder website(String website) {
                this.website = website;
                return this;
            }

            @JsonProperty("other")
            public Builder other(String other) {
                this.other = other;
                return this;
            }

            public CTAs build() {
                return new CTAs(this);
            }
        }
    }

    /**
     * Represents social media links for a campaign.
     */
    @JsonDeserialize(builder = SocialMedia.Builder.class)
    public static class SocialMedia {
        private final String facebook;
        private final String x;
        private final String instagram;
        private final String linkedin;
        private final String tiktok;
        private final String youtube;
        private final String discord;
        private final String twitch;

        private SocialMedia(Builder builder) {
            this.facebook = builder.facebook;
            this.x = builder.x;
            this.instagram = builder.instagram;
            this.linkedin = builder.linkedin;
            this.tiktok = builder.tiktok;
            this.youtube = builder.youtube;
            this.discord = builder.discord;
            this.twitch = builder.twitch;
        }

        public String getFacebook() {
            return facebook;
        }

        public String getX() {
            return x;
        }

        public String getInstagram() {
            return instagram;
        }

        public String getLinkedin() {
            return linkedin;
        }

        public String getTiktok() {
            return tiktok;
        }

        public String getYoutube() {
            return youtube;
        }

        public String getDiscord() {
            return discord;
        }

        public String getTwitch() {
            return twitch;
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            @JsonProperty("facebook")
            private String facebook;

            @JsonProperty("x")
            private String x;

            @JsonProperty("instagram")
            private String instagram;

            @JsonProperty("linkedin")
            private String linkedin;

            @JsonProperty("tiktok")
            private String tiktok;

            @JsonProperty("youtube")
            private String youtube;

            @JsonProperty("discord")
            private String discord;

            @JsonProperty("twitch")
            private String twitch;

            public Builder facebook(String facebook) {
                this.facebook = facebook;
                return this;
            }

            public Builder x(String x) {
                this.x = x;
                return this;
            }

            public Builder instagram(String instagram) {
                this.instagram = instagram;
                return this;
            }

            public Builder linkedin(String linkedin) {
                this.linkedin = linkedin;
                return this;
            }

            public Builder tiktok(String tiktok) {
                this.tiktok = tiktok;
                return this;
            }

            public Builder youtube(String youtube) {
                this.youtube = youtube;
                return this;
            }

            public Builder discord(String discord) {
                this.discord = discord;
                return this;
            }

            public Builder twitch(String twitch) {
                this.twitch = twitch;
                return this;
            }

            public SocialMedia build() {
                return new SocialMedia(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppPromotion that = (AppPromotion) o;
        return Objects.equals(appId, that.appId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId);
    }

    @Override
    public String toString() {
        return String.format("AppPromotion{appId='%s', appName='%s'}", appId, appName);
    }
}

