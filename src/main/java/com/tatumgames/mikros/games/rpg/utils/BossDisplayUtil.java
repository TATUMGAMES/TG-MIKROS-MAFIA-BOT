package com.tatumgames.mikros.games.rpg.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Utility for formatting boss-related timestamps for display. */
public final class BossDisplayUtil {

  private static final DateTimeFormatter BOSS_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a z").withZone(ZoneId.systemDefault());

  private BossDisplayUtil() {}

  /**
   * Formats an instant as a human-readable timestamp for boss display (e.g. "Feb 3, 2026 3:30 PM
   * PST").
   *
   * @param instant the instant to format
   * @return formatted string, or "Unknown" if null
   */
  public static String formatBossTimestamp(Instant instant) {
    if (instant == null) {
      return "Unknown";
    }
    return BOSS_TIMESTAMP_FORMATTER.format(instant);
  }
}
