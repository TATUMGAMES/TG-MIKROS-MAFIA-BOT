package com.tatumgames.mikros.botdetection.config;

/**
 * Configuration for bot detection per guild.
 */
public class BotDetectionConfig {
    private boolean enabled;
    private int accountAgeThresholdDays;
    private int linkRestrictionMinutes;
    private int multiChannelSpamThreshold;
    private int multiChannelTimeWindowSeconds;
    private int joinAndLinkTimeWindowSeconds;
    private AutoAction autoAction;
    private boolean reportToReputation;

    /**
     * Creates a new BotDetectionConfig with default values.
     */
    public BotDetectionConfig() {
        this.enabled = false;
        this.accountAgeThresholdDays = 30;
        this.linkRestrictionMinutes = 20;
        this.multiChannelSpamThreshold = 3;
        this.multiChannelTimeWindowSeconds = 30;
        this.joinAndLinkTimeWindowSeconds = 60;
        this.autoAction = AutoAction.DELETE;
        this.reportToReputation = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAccountAgeThresholdDays() {
        return accountAgeThresholdDays;
    }

    public void setAccountAgeThresholdDays(int accountAgeThresholdDays) {
        this.accountAgeThresholdDays = accountAgeThresholdDays;
    }

    public int getLinkRestrictionMinutes() {
        return linkRestrictionMinutes;
    }

    public void setLinkRestrictionMinutes(int linkRestrictionMinutes) {
        this.linkRestrictionMinutes = linkRestrictionMinutes;
    }

    public int getMultiChannelSpamThreshold() {
        return multiChannelSpamThreshold;
    }

    public void setMultiChannelSpamThreshold(int multiChannelSpamThreshold) {
        this.multiChannelSpamThreshold = multiChannelSpamThreshold;
    }

    public int getMultiChannelTimeWindowSeconds() {
        return multiChannelTimeWindowSeconds;
    }

    public void setMultiChannelTimeWindowSeconds(int multiChannelTimeWindowSeconds) {
        this.multiChannelTimeWindowSeconds = multiChannelTimeWindowSeconds;
    }

    public int getJoinAndLinkTimeWindowSeconds() {
        return joinAndLinkTimeWindowSeconds;
    }

    public void setJoinAndLinkTimeWindowSeconds(int joinAndLinkTimeWindowSeconds) {
        this.joinAndLinkTimeWindowSeconds = joinAndLinkTimeWindowSeconds;
    }

    public AutoAction getAutoAction() {
        return autoAction;
    }

    public void setAutoAction(AutoAction autoAction) {
        this.autoAction = autoAction;
    }

    public boolean isReportToReputation() {
        return reportToReputation;
    }

    public void setReportToReputation(boolean reportToReputation) {
        this.reportToReputation = reportToReputation;
    }

    /**
     * Enum representing automatic actions to take when bot is detected.
     */
    public enum AutoAction {
        NONE,
        DELETE,
        WARN,
        MUTE,
        KICK
    }
}

