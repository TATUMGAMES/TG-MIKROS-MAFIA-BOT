package com.tatumgames.mikros.games.word_unscramble.service;

import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleConfig;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleResult;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleSession;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for hourly Word Unscramble game resets.
 * Resets games every hour for all configured guilds.
 * <p>
 * TODO: Reward System Integration
 * - Award MIKROS discounts to winners
 * - Grant special Discord roles to champions
 * - Implement streak tracking for consecutive wins
 * - Add monthly leaderboard for cumulative winners
 */
public class WordUnscrambleResetScheduler {
    private static final Logger logger = LoggerFactory.getLogger(WordUnscrambleResetScheduler.class);

    private final WordUnscrambleService wordUnscrambleService;
    private final ScheduledExecutorService scheduler;
    private JDA jda;

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
     * Starts the reset scheduler.
     * Resets games every hour for all configured guilds.
     *
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        this.jda = jda;

        // Reset games every hour
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndResetGames();
            } catch (Exception e) {
                logger.error("Error in Word Unscramble reset scheduler", e);
            }
        }, 0, 1, TimeUnit.HOURS);

        logger.info("Word Unscramble reset scheduler started (hourly resets)");
    }

    /**
     * Checks all guilds and resets games hourly.
     */
    private void checkAndResetGames() {
        if (jda == null) {
            return;
        }

        for (String guildId : wordUnscrambleService.getConfiguredGuilds()) {
            try {
                WordUnscrambleConfig config = wordUnscrambleService.getConfig(guildId);
                if (config == null) {
                    continue;
                }

                // Reset game every hour
                resetAndStartNewGame(guildId);
            } catch (Exception e) {
                logger.error("Error resetting Word Unscramble game for guild {}", guildId, e);
            }
        }
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
            logger.warn("Word Unscramble channel {} not found in guild {}", config.getGameChannelId(), guildId);
            return;
        }

        // Get current progression level before reset (for level-up detection)
        com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleProgression progression = wordUnscrambleService.getProgression(guildId);
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

    /**
     * Announces the winner of the previous game.
     */
    private void announceWinner(TextChannel channel, WordUnscrambleSession session) {
        String announcement;

        WordUnscrambleResult winner = session.getWinner();
        if (winner != null) {
            announcement = String.format("""
                            %s **%s Winner**
                            
                            🏆 **%s** solved it first!
                            
                            Congratulations!
                            """,
                    session.getGameType().getEmoji(),
                    session.getGameType().getDisplayName(),
                    winner.username()
            );
        } else {
            announcement = String.format(
                    "%s **%s Ended**\n\nNo one solved it this hour!\nAnswer was: **%s**",
                    session.getGameType().getEmoji(),
                    session.getGameType().getDisplayName(),
                    session.getCorrectAnswer() != null ? session.getCorrectAnswer() : "N/A"
            );
        }

        channel.sendMessage(announcement).queue();
    }

    /**
     * Announces a new game in the channel.
     */
    private void announceNewGame(TextChannel channel, WordUnscrambleSession session) {
        String announcement = wordUnscrambleService.getGameAnnouncementWithLevel(session.getGuildId());

        if (announcement != null) {
            channel.sendMessage(announcement).queue();
        }
    }

    /**
     * Announces a level-up for Word Unscramble.
     */
    private void announceLevelUp(TextChannel channel, int level) {
        String announcement = String.format("""
                        🎉 **Your community leveled up!** 🎉
                        
                        Welcome to **Level %d** — expect more challenging words!
                        
                        Keep solving to reach the next level! 🚀
                        """,
                level
        );
        channel.sendMessage(announcement).queue();
    }

    /**
     * Stops the scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Word Unscramble reset scheduler stopped");
    }
}




