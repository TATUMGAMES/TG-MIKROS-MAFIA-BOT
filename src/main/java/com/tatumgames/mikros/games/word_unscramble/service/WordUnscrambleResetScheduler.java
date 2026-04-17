package com.tatumgames.mikros.games.word_unscramble.service;

import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleConfig;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleResult;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleSession;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler for Word Unscramble game resets. Uses activity-aware scheduling: when no one solves a
 * word, backoff increases and the game can pause; when someone solves or uses a scramble command,
 * scheduling resumes with the base interval.
 */
public class WordUnscrambleResetScheduler {
  private static final Logger logger = LoggerFactory.getLogger(WordUnscrambleResetScheduler.class);

  /**
   * Default base interval in hours between word posts when activity is normal (admin setup uses
   * this).
   */
  public static final int DEFAULT_BASE_INTERVAL_HOURS = 4;

  /** How often the scheduler checks whether to post the next word (per guild). */
  private static final long CHECK_INTERVAL_MINUTES = 15;

  private final WordUnscrambleService wordUnscrambleService;
  private final ScheduledExecutorService scheduler;
  private volatile boolean started = false;
  private JDA jda;

  /** Per-guild activity state for backoff and pause. */
  private static final class ScrambleActivityState {
    Instant nextRunTime;
    int backoffHours;
    int unansweredCount;
    int unansweredStreakSets;
    boolean gamePaused;
  }

  private final java.util.Map<String, ScrambleActivityState> scrambleActivity =
      new ConcurrentHashMap<>();

  /**
   * Creates a new WordUnscrambleResetScheduler.
   *
   * @param wordUnscrambleService the Word Unscramble service
   */
  public WordUnscrambleResetScheduler(WordUnscrambleService wordUnscrambleService) {
    this.wordUnscrambleService = wordUnscrambleService;
    this.scheduler = Executors.newScheduledThreadPool(1);
    logger.info("WordUnscrambleResetScheduler initialized");
  }

  /**
   * Starts the reset scheduler if not already started. Idempotent: safe to call multiple times.
   *
   * @param jda the JDA instance
   */
  public void startIfNeeded(JDA jda) {
    start(jda);
  }

  /**
   * Starts the reset scheduler. Checks every 15 minutes; each guild's next word is scheduled by
   * activity (base interval + backoff when no one solves, or pause after 3 unanswered streaks).
   *
   * @param jda the JDA instance
   */
  public void start(JDA jda) {
    synchronized (this) {
      if (started) {
        return;
      }
      started = true;
    }
    this.jda = jda;

    scheduler.scheduleAtFixedRate(
        () -> {
          try {
            checkAndResetGames();
          } catch (Exception e) {
            logger.error("Error in Word Unscramble reset scheduler", e);
          }
        },
        0,
        CHECK_INTERVAL_MINUTES,
        TimeUnit.MINUTES);

    logger.info(
        "Word Unscramble reset scheduler started (activity-aware, check every {} minutes)",
        CHECK_INTERVAL_MINUTES);
  }

  /**
   * Checks all guilds; for each, if not paused and now >= nextRunTime, runs reset and updates
   * backoff/pause/nextRunTime. Cleanup runs periodically.
   */
  private void checkAndResetGames() {
    if (jda == null) {
      return;
    }

    Instant now = Instant.now();

    for (String guildId : wordUnscrambleService.getConfiguredGuilds()) {
      try {
        WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
        if (config == null) {
          continue;
        }

        ScrambleActivityState state =
            scrambleActivity.computeIfAbsent(guildId, k -> new ScrambleActivityState());

        synchronized (state) {
          if (state.gamePaused) {
            continue;
          }
          if (state.nextRunTime == null) {
            state.nextRunTime = now;
          }
          if (now.isBefore(state.nextRunTime)) {
            continue;
          }

          WordUnscrambleSession previousSession = wordUnscrambleService.getActiveSession(guildId);
          boolean hadWinner = previousSession != null && previousSession.getWinner() != null;

          resetAndStartNewGame(guildId);

          int baseHours = config.getBaseIntervalHours();
          if (hadWinner) {
            state.nextRunTime = now.plusSeconds(baseHours * 3600L);
          } else {
            state.unansweredCount++;
            if (state.unansweredCount >= 5) {
              state.unansweredStreakSets++;
              state.unansweredCount = 0;
              state.backoffHours += 2;
            }
            if (state.unansweredStreakSets >= 3) {
              state.gamePaused = true;
              state.nextRunTime = null;
              logger.info(
                  "Word Unscramble: Pausing game for guild {} (3 unanswered streak sets)", guildId);
            } else {
              long delayHours = baseHours + state.backoffHours;
              state.nextRunTime = now.plusSeconds(delayHours * 3600L);
            }
          }

          logger.info(
              "Word Unscramble rollover guildId={} hadWinner={} unansweredCount={} unansweredStreakSets={} backoffHours={} gamePaused={} nextRunTime={}",
              guildId,
              hadWinner,
              state.unansweredCount,
              state.unansweredStreakSets,
              state.backoffHours,
              state.gamePaused,
              state.nextRunTime);
        }
      } catch (Exception e) {
        logger.error("Error resetting Word Unscramble game for guild {}", guildId, e);
      }
    }

    wordUnscrambleService.cleanupUsedWordTrackerEntries(null);
  }

  /**
   * Records that a player solved the word in this guild. Resets backoff and pause, and schedules
   * the next word at base interval. Call from WordUnscrambleService when a correct answer is
   * recorded.
   *
   * @param guildId the guild ID
   */
  public void recordSolve(String guildId) {
    WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
    int baseHours = config != null ? config.getBaseIntervalHours() : DEFAULT_BASE_INTERVAL_HOURS;
    ScrambleActivityState state =
        scrambleActivity.computeIfAbsent(guildId, k -> new ScrambleActivityState());
    synchronized (state) {
      state.unansweredCount = 0;
      state.unansweredStreakSets = 0;
      state.backoffHours = 0;
      state.gamePaused = false;
      state.nextRunTime = Instant.now().plusSeconds(baseHours * 3600L);
    }
  }

  /**
   * Records that a player used a scramble command in this guild. If the game was paused, resumes
   * and schedules the next word at base interval.
   *
   * @param guildId the guild ID
   */
  public void recordScrambleActivity(String guildId) {
    WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
    if (config == null) {
      return;
    }
    ScrambleActivityState state = scrambleActivity.get(guildId);
    if (state == null) {
      return;
    }
    synchronized (state) {
      if (!state.gamePaused) {
        return;
      }
      int baseHours = config.getBaseIntervalHours();
      state.gamePaused = false;
      state.unansweredCount = 0;
      state.unansweredStreakSets = 0;
      state.backoffHours = 0;
      state.nextRunTime = Instant.now().plusSeconds(baseHours * 3600L);
      logger.info("Word Unscramble: Resuming game for guild {} (scramble command used)", guildId);
    }
  }

  /**
   * Resets activity gating/backoff for a guild and sets the next check time to the normal base
   * interval.
   *
   * <p>Used by `/admin-scramble-setup` to ensure a clean re-setup doesn't immediately trigger an
   * extra round due to stale pause/backoff state.
   *
   * @param guildId the guild ID
   * @param baseIntervalHours base interval in hours
   */
  public void resetGuildActivity(String guildId, int baseIntervalHours) {
    if (guildId == null) {
      return;
    }

    int safeBaseInterval = baseIntervalHours > 0 ? baseIntervalHours : DEFAULT_BASE_INTERVAL_HOURS;

    ScrambleActivityState state =
        scrambleActivity.computeIfAbsent(guildId, k -> new ScrambleActivityState());
    synchronized (state) {
      state.unansweredCount = 0;
      state.unansweredStreakSets = 0;
      state.backoffHours = 0;
      state.gamePaused = false;
      state.nextRunTime = Instant.now().plusSeconds(safeBaseInterval * 3600L);
    }

    logger.info(
        "Word Unscramble: Reset guild activity for {} (base interval {}h)",
        guildId,
        safeBaseInterval);
  }

  /**
   * Resets the current game and starts a new one for a guild.
   *
   * @param guildId the guild ID
   */
  public void resetAndStartNewGame(String guildId) {
    Guild guild = jda.getGuildById(guildId);
    if (guild == null) {
      logger.warn("Guild {} not found for Word Unscramble reset", guildId);
      return;
    }

    WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
    if (config == null) {
      return;
    }

    // Get the game channel
    TextChannel channel = guild.getTextChannelById(config.getGameChannelId());
    if (channel == null) {
      logger.warn(
          "Word Unscramble channel {} not found in guild {}", config.getGameChannelId(), guildId);
      return;
    }

    // Get current progression level before reset (for level-up detection)
    com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleProgression progression =
        wordUnscrambleService.getProgression(guildId);
    int previousLevel = progression != null ? progression.getLevel() : 1;

    // Announce winner of previous game (if any)
    WordUnscrambleSession previousSession = wordUnscrambleService.getActiveSession(guildId);
    if (previousSession != null && previousSession.isActive()) {
      announceWinner(channel, previousSession);
    }

    // Reset the game
    wordUnscrambleService.resetGame(guildId);

    // Check for level-up after reset (level-up happens during handleAttempt, so check now)
    progression = wordUnscrambleService.getProgression(guildId);
    if (progression != null && progression.getLevel() > previousLevel) {
      announceLevelUp(channel, progression.getLevel());
    }

    // Start a new game
    WordUnscrambleSession newSession = wordUnscrambleService.startRandomEnabledGame(guildId);
    if (newSession != null) {
      announceNewGame(channel, newSession);
    }

    logger.info("Reset and started new Word Unscramble game for guild {}", guildId);
  }

  /** Announces the winner of the previous game. */
  private void announceWinner(TextChannel channel, WordUnscrambleSession session) {
    String announcement;

    WordUnscrambleResult winner = session.getWinner();
    if (winner != null) {
      announcement =
          String.format(
              """
                            %s **%s Winner**

                            🏆 **%s** solved it first!

                                    Congratulations!
                                    """,
              session.getGameType().getEmoji(),
              session.getGameType().getDisplayName(),
              winner.username());
    } else {
      announcement =
          String.format(
              "%s **%s Ended**\n\nNo one solved it this round!\nAnswer was: **%s**",
              session.getGameType().getEmoji(),
              session.getGameType().getDisplayName(),
              session.getCorrectAnswer() != null ? session.getCorrectAnswer() : "N/A");
    }

    channel.sendMessage(announcement).queue();
  }

  /** Announces a new game in the channel. */
  private void announceNewGame(TextChannel channel, WordUnscrambleSession session) {
    String announcement = wordUnscrambleService.getGameAnnouncementWithLevel(session.getGuildId());

    if (announcement != null) {
      channel.sendMessage(announcement).queue();
    }
  }

  /** Announces a level-up for Word Unscramble. */
  private void announceLevelUp(TextChannel channel, int level) {
    String announcement =
        String.format(
            """
                        🎉 **Your community leveled up!** 🎉

                        Welcome to **Level %d** — expect more challenging words!

                                Keep solving to reach the next level! 🚀
                                """,
            level);
    channel.sendMessage(announcement).queue();
  }

  /** Stops the scheduler. */
  public void shutdown() {
    scheduler.shutdown();
    logger.info("Word Unscramble reset scheduler stopped");
  }
}
