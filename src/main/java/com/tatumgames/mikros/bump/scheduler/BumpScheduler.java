package com.tatumgames.mikros.bump.scheduler;

import com.tatumgames.mikros.bump.model.BumpConfig;
import com.tatumgames.mikros.bump.service.BumpService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for automatic server bumping. Checks every 15 minutes and executes bumps when intervals
 * are met. Respects external bot rate limits and handles errors gracefully.
 */
public class BumpScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BumpScheduler.class);

    private static final long CHECK_INTERVAL_MINUTES = 15;

    // External bot rate limits (conservative estimates)
    private static final long DISBOARD_MIN_COOLDOWN_HOURS = 2; // Disboard requires 2h between bumps
    private static final long DISURL_MIN_COOLDOWN_HOURS = 1; // Disurl typically 1h

    // Bot user IDs (these are well-known bot IDs)
    private static final long DISBOARD_BOT_ID = 302050872383242240L; // Disboard bot ID
    private static final long DISURL_BOT_ID =
            823495039178932224L; // Disurl bot ID (approximate, may need verification)

    private final BumpService bumpService;
    private final ScheduledExecutorService scheduler;
    private volatile boolean started = false;
    private JDA jda;

    /**
     * Creates a new BumpScheduler.
     *
     * @param bumpService the bump service
     */
    public BumpScheduler(BumpService bumpService) {
        this.bumpService = bumpService;
        this.scheduler =
                Executors.newSingleThreadScheduledExecutor(
                        r -> {
                            Thread t = new Thread(r, "bump-scheduler");
                            t.setDaemon(true);
                            return t;
                        });
        logger.info("BumpScheduler initialized");
    }

    /**
     * Starts the bump scheduler if not already started. Idempotent: safe to call multiple times.
     *
     * @param jda the JDA instance
     */
    public void startIfNeeded(JDA jda) {
        start(jda);
    }

    /**
     * Starts the bump scheduler.
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

        // Run check every 15 minutes
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        logger.debug("Bump scheduler check triggered");
                        checkAndExecuteBumps();
                    } catch (Exception e) {
                        logger.error("Error in bump scheduler", e);
                    }
                },
                0,
                CHECK_INTERVAL_MINUTES,
                TimeUnit.MINUTES);

        logger.info("Bump scheduler started (checks every {} minutes)", CHECK_INTERVAL_MINUTES);
    }

    /**
     * Stops the bump scheduler.
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Bump scheduler stopped");
        }
    }

    /**
     * Executes bumps for all configured servers.
     */
    private void checkAndExecuteBumps() {
        if (jda == null) {
            return;
        }

        for (Guild guild : jda.getGuilds()) {
            try {
                checkGuildBumps(guild);
            } catch (Exception e) {
                logger.error("Error checking bumps for guild {}", guild.getId(), e);
            }
        }
    }

    /**
     * Checks and executes bumps for a specific guild.
     *
     * @param guild the guild to check
     */
    private void checkGuildBumps(Guild guild) {
        String guildId = guild.getId();
        BumpConfig config = bumpService.getConfig(guildId);

        // Skip if not enabled
        if (!config.isEnabled()) {
            return;
        }

        String channelId = config.getChannelId();
        TextChannel channel = guild.getTextChannelById(channelId);

        if (channel == null) {
            logger.warn("Bump channel {} not found in guild {}", channelId, guildId);
            return;
        }

        // Check if bot can send messages
        if (!channel.canTalk()) {
            logger.warn("Cannot send messages in bump channel {} for guild {}", channelId, guildId);
            return;
        }

        EnumSet<BumpConfig.BumpBot> enabledBots = config.getEnabledBots();
        int intervalHours = config.getIntervalHours();
        Instant now = Instant.now();

        // Collect all bots that need bumping
        EnumSet<BumpConfig.BumpBot> botsToBump = EnumSet.noneOf(BumpConfig.BumpBot.class);
        for (BumpConfig.BumpBot bot : enabledBots) {
            try {
                if (shouldBump(guild, bot, config, intervalHours, now)) {
                    botsToBump.add(bot);
                }
            } catch (Exception e) {
                logger.error("Error checking bump for bot {} in guild {}", bot, guildId, e);
            }
        }

        // Send reminder only when at least one bot needs bumping
        if (botsToBump.isEmpty()) {
            return;
        }

        // When both bots are enabled, always send combined message listing both (one shared reminder)
        if (enabledBots.size() >= 2) {
            try {
                sendCombinedBumpReminder(channel, enabledBots);
                for (BumpConfig.BumpBot bot : botsToBump) {
                    bumpService.recordBumpTime(guildId, bot, now);
                }
                logger.info(
                        "Executed combined bump reminder for enabled bots {} in guild {}",
                        enabledBots.stream().map(BumpConfig.BumpBot::getDisplayName).toList(),
                        guildId);
            } catch (Exception e) {
                logger.error("Error executing combined bump reminder in guild {}", guildId, e);
            }
        } else {
            // Single bot enabled - send single bot message
            BumpConfig.BumpBot bot = botsToBump.iterator().next();
            try {
                sendBumpCommand(channel, bot);
                bumpService.recordBumpTime(guildId, bot, now);
                logger.info("Executed bump for bot {} in guild {}", bot.getDisplayName(), guildId);
            } catch (Exception e) {
                logger.error("Error executing bump for bot {} in guild {}", bot, guildId, e);
            }
        }
    }

    /**
     * Determines if a bump should be executed for a bot.
     *
     * @param guild         the guild
     * @param bot           the bot to check
     * @param config        the bump config
     * @param intervalHours the configured interval
     * @param now           the current time
     * @return true if bump should be executed
     */
    private boolean shouldBump(
            Guild guild, BumpConfig.BumpBot bot, BumpConfig config, int intervalHours, Instant now) {
        // Check if bot is present in the server
        if (!isBotPresent(guild, bot)) {
            logger.debug(
                    "Bot {} not present in guild {}, skipping bump", bot.getDisplayName(), guild.getId());
            return false;
        }

        // Check last bump time
        Instant lastBump = config.getLastBumpTime(bot);
        if (lastBump == null) {
            // Never bumped before, allow it
            return true;
        }

        // Check configured interval
        Duration timeSinceLastBump = Duration.between(lastBump, now);
        long hoursSinceLastBump = timeSinceLastBump.toHours();

        if (hoursSinceLastBump < intervalHours) {
            logger.debug(
                    "Interval not met for bot {} in guild {} ({}h < {}h)",
                    bot.getDisplayName(),
                    guild.getId(),
                    hoursSinceLastBump,
                    intervalHours);
            return false;
        }

        // Check external bot rate limits
        long minCooldownHours = getMinCooldownHours(bot);
        if (hoursSinceLastBump < minCooldownHours) {
            logger.debug(
                    "Rate limit not met for bot {} in guild {} ({}h < {}h min cooldown)",
                    bot.getDisplayName(),
                    guild.getId(),
                    hoursSinceLastBump,
                    minCooldownHours);
            return false;
        }

        return true;
    }

    /**
     * Checks if a bot is present in the guild. Uses an API call to verify actual membership (not just
     * cache).
     *
     * @param guild the guild
     * @param bot   the bot to check
     * @return true if bot is present
     */
    private boolean isBotPresent(Guild guild, BumpConfig.BumpBot bot) {
        long botId = getBotId(bot);
        if (botId == 0) {
            // Unknown bot ID, assume present (will fail gracefully if not)
            return true;
        }

        try {
            // Use retrieveMemberById which makes an API call instead of checking cache
            // This ensures we get accurate membership status
            Member botMember = guild.retrieveMemberById(botId).complete();
            return botMember != null;
        } catch (Exception e) {
            // If API call fails (e.g., bot not in server, network error), assume not present
            logger.debug(
                    "Could not retrieve bot {} from guild {}: {}",
                    bot.getDisplayName(),
                    guild.getId(),
                    e.getMessage());
            return false;
        }
    }

    /**
     * Gets the Discord user ID for a bot.
     *
     * @param bot the bot
     * @return the bot ID, or 0 if unknown
     */
    private long getBotId(BumpConfig.BumpBot bot) {
        return switch (bot) {
            case DISBOARD -> DISBOARD_BOT_ID;
            case DISURL -> DISURL_BOT_ID;
        };
    }

    /**
     * Gets the minimum cooldown hours for a bot.
     *
     * @param bot the bot
     * @return minimum cooldown in hours
     */
    private long getMinCooldownHours(BumpConfig.BumpBot bot) {
        return switch (bot) {
            case DISBOARD -> DISBOARD_MIN_COOLDOWN_HOURS;
            case DISURL -> DISURL_MIN_COOLDOWN_HOURS;
        };
    }

    /**
     * Sends a combined bump reminder for multiple bots.
     *
     * @param channel the channel to send in
     * @param bots    the set of bots to bump
     */
    private void sendCombinedBumpReminder(TextChannel channel, EnumSet<BumpConfig.BumpBot> bots) {
        // Build instruction text: list each bot with its command (e.g. /bump for Disboard, /bump disurl
        // for Disurl)
        String instructions =
                bots.stream()
                        .map(bot -> String.format("**%s**: `%s`", bot.getDisplayName(), bot.getCommand()))
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse("");
        String botNames =
                bots.stream()
                        .map(BumpConfig.BumpBot::getDisplayName)
                        .reduce((a, b) -> a + " and " + b)
                        .orElse("");

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⏰ Time to Bump the Server!");
        embed.setDescription(
                String.format(
                        "Please bump the server on **%s**:\n%s\n\n"
                                + "This helps keep the server visible on server listing sites! 🚀",
                        botNames, instructions));
        embed.setColor(Color.CYAN);
        embed.setFooter("Click the buttons below for quick actions");
        embed.setTimestamp(Instant.now());

        // Create buttons for each bot
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder().setEmbeds(embed.build());

        // Add button rows for each bot (Discord allows up to 5 buttons per row, 5 rows max)
        int buttonCount = 0;
        for (BumpConfig.BumpBot bot : bots) {
            if (buttonCount >= 15) { // Max 15 buttons total (3 per bot * 5 bots max)
                break;
            }

            Button copyCommandButton =
                    Button.secondary(
                                    "bump_copy_" + bot.name().toLowerCase(), "📋 Copy " + bot.getDisplayName())
                            .withEmoji(Emoji.fromUnicode("📋"));

            Button instructionsButton =
                    Button.link(
                                    bot == BumpConfig.BumpBot.DISBOARD
                                            ? "https://disboard.org/help"
                                            : "https://disurl.com/help",
                                    "📖 " + bot.getDisplayName() + " Help")
                            .withEmoji(Emoji.fromUnicode("📖"));

            Button botMentionButton =
                    Button.secondary(
                                    "bump_mention_" + bot.name().toLowerCase(), "👤 Mention " + bot.getDisplayName())
                            .withEmoji(Emoji.fromUnicode("👤"));

            messageBuilder.addActionRow(copyCommandButton, instructionsButton, botMentionButton);
            buttonCount += 3;
        }

        channel
                .sendMessage(messageBuilder.build())
                .queue(
                        success ->
                                logger.info(
                                        "Sent combined bump reminder for {} in channel {}", botNames, channel.getId()),
                        error ->
                                logger.warn(
                                        "Failed to send combined bump reminder to channel {}: {}",
                                        channel.getId(),
                                        error.getMessage()));
    }

    /**
     * Sends a bump reminder with interactive buttons to a specific bot in a channel. Discord bots
     * cannot invoke slash commands of other bots, so we send a reminder with buttons.
     *
     * @param channel the channel to send in
     * @param bot     the bot to bump
     */
    private void sendBumpCommand(TextChannel channel, BumpConfig.BumpBot bot) {
        // Discord bots cannot invoke slash commands of other bots
        // Instead, send a reminder message with buttons that help users bump

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⏰ Time to Bump the Server!");
        embed.setDescription(
                String.format(
                        "Please run %s to bump the server on **%s**.\n\n"
                                + "This helps keep the server visible on server listing sites! 🚀",
                        bot.getCommand(), bot.getDisplayName()));
        embed.setColor(Color.CYAN);
        embed.setFooter("Click the buttons below for quick actions");
        embed.setTimestamp(Instant.now());

        // Create buttons
        Button copyCommandButton =
                Button.secondary("bump_copy_" + bot.name().toLowerCase(), "📋 Copy Command")
                        .withEmoji(Emoji.fromUnicode("📋"));

        Button instructionsButton =
                Button.link(
                                bot == BumpConfig.BumpBot.DISBOARD
                                        ? "https://disboard.org/help"
                                        : "https://disurl.com/help",
                                "📖 Instructions")
                        .withEmoji(Emoji.fromUnicode("📖"));

        Button botMentionButton =
                Button.secondary(
                                "bump_mention_" + bot.name().toLowerCase(), "👤 Mention " + bot.getDisplayName())
                        .withEmoji(Emoji.fromUnicode("👤"));

        MessageCreateData message =
                new MessageCreateBuilder()
                        .setEmbeds(embed.build())
                        .setActionRow(copyCommandButton, instructionsButton, botMentionButton)
                        .build();

        channel
                .sendMessage(message)
                .queue(
                        success ->
                                logger.info(
                                        "Sent bump reminder for {} in channel {}",
                                        bot.getDisplayName(),
                                        channel.getId()),
                        error ->
                                logger.warn(
                                        "Failed to send bump reminder {} to channel {}: {}",
                                        bot.getDisplayName(),
                                        channel.getId(),
                                        error.getMessage()));
    }
}
