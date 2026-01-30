package com.tatumgames.mikros.games.rpg.blessing;

import java.time.Instant;

/**
 * Represents an active blessing granted to a guild during boss battles.
 * Blessings provide temporary stat boosts that only apply during boss battles.
 */
public class Blessing {
    private final BlessingType type;
    private final String narrative;
    private final Instant grantedAt;

    // Stat multipliers (1.0 = no change, 1.25 = +25%)
    private final double strMultiplier;
    private final double agiMultiplier;
    private final double intMultiplier;

    /**
     * Creates a new blessing.
     *
     * @param type          the blessing type/tier
     * @param narrative     the narrative text describing the blessing
     * @param strMultiplier strength multiplier
     * @param agiMultiplier agility multiplier
     * @param intMultiplier intelligence multiplier
     */
    public Blessing(BlessingType type, String narrative,
                    double strMultiplier, double agiMultiplier, double intMultiplier) {
        this.type = type;
        this.narrative = narrative;
        this.grantedAt = Instant.now();
        this.strMultiplier = strMultiplier;
        this.agiMultiplier = agiMultiplier;
        this.intMultiplier = intMultiplier;
    }

    public BlessingType getType() {
        return type;
    }

    public String getNarrative() {
        return narrative;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public double getStrMultiplier() {
        return strMultiplier;
    }

    public double getAgiMultiplier() {
        return agiMultiplier;
    }

    public double getIntMultiplier() {
        return intMultiplier;
    }

    /**
     * Gets a formatted description of the blessing effects for display.
     * Only shows stat multipliers that are actually used in boss damage calculation.
     *
     * @return formatted string describing all effects
     */
    public String getEffectsDescription() {
        StringBuilder sb = new StringBuilder();

        if (strMultiplier > 1.0) {
            double percent = (strMultiplier - 1.0) * 100;
            sb.append(String.format("STR +%.0f%%\n", percent));
        }
        if (agiMultiplier > 1.0) {
            double percent = (agiMultiplier - 1.0) * 100;
            sb.append(String.format("AGI +%.0f%%\n", percent));
        }
        if (intMultiplier > 1.0) {
            double percent = (intMultiplier - 1.0) * 100;
            sb.append(String.format("INT +%.0f%%\n", percent));
        }

        return sb.toString().trim();
    }
}
