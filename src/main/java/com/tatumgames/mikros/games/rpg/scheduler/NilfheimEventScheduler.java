package com.tatumgames.mikros.games.rpg.scheduler;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.events.NilfheimEventType;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import com.tatumgames.mikros.games.rpg.service.NilfheimEventService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for server-wide Nilfheim events.
 * Checks every 6 hours and triggers events every 48-96 hours (randomized per event type).
 */
public class NilfheimEventScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NilfheimEventScheduler.class);

    // Check interval: every 6 hours
    private static final long CHECK_INTERVAL_HOURS = 6;

    // Event duration: 12 hours
    private static final long EVENT_DURATION_HOURS = 12;

    private final NilfheimEventService eventService;
    private final CharacterService characterService;
    private final ScheduledExecutorService scheduler;
    private final Random random;
    private JDA jda;

    public NilfheimEventScheduler(NilfheimEventService eventService, CharacterService characterService) {
        this.eventService = eventService;
        this.characterService = characterService;
        this.random = new Random();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "nilfheim-event-scheduler");
            t.setDaemon(true);
            return t;
        });
        logger.info("NilfheimEventScheduler initialized");
    }

    /**
     * Starts the event scheduler.
     * Checks every 6 hours to see if it's time to trigger a new event.
     *
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        this.jda = jda;

        // Calculate initial delay to next 6-hour boundary
        long initialDelay = calculateInitialDelay();

        // Run check every 6 hours
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndTriggerEvents();
            } catch (Exception e) {
                logger.error("Error in Nilfheim event scheduler", e);
            }
        }, initialDelay, CHECK_INTERVAL_HOURS, TimeUnit.HOURS);

        logger.info("Nilfheim event scheduler started (checks every {} hours)", CHECK_INTERVAL_HOURS);
    }

    /**
     * Calculates initial delay to next 6-hour boundary.
     *
     * @return delay in hours
     */
    private long calculateInitialDelay() {
        Instant now = Instant.now();
        long hoursSinceEpoch = now.getEpochSecond() / 3600;
        long hoursUntilNextBoundary = CHECK_INTERVAL_HOURS - (hoursSinceEpoch % CHECK_INTERVAL_HOURS);
        return hoursUntilNextBoundary;
    }

    /**
     * Checks all servers and triggers events if enough time has passed.
     */
    private void checkAndTriggerEvents() {
        if (jda == null) {
            logger.warn("JDA not initialized, skipping event check");
            return;
        }

        Instant now = Instant.now();

        for (Guild guild : jda.getGuilds()) {
            try {
                String guildId = guild.getId();

                // Check if RPG is enabled for this guild
                RPGConfig config = characterService.getConfig(guildId);
                if (config == null || !config.isEnabled()) {
                    continue;
                }

                // Check if there's already an active event
                NilfheimEventType activeEvent = eventService.getActiveEvent(guildId);
                if (activeEvent != null) {
                    // Event already active, skip
                    continue;
                }

                // Check if enough time has passed since last event
                Instant lastEventTime = eventService.getLastEventTime(guildId);
                if (lastEventTime != null) {
                    // Check if enough time has passed (randomized per event type)
                    // For now, use a simple approach: check if 48-96 hours have passed
                    long hoursSinceLastEvent = (now.getEpochSecond() - lastEventTime.getEpochSecond()) / 3600;

                    // Randomize next event time (48-96 hours)
                    // We'll use a simple check: if 48+ hours have passed, roll for event
                    if (hoursSinceLastEvent < 48) {
                        continue; // Not enough time has passed
                    }

                    // Roll for event (higher chance as more time passes)
                    // At 48 hours: 10% chance, at 96 hours: 100% chance
                    double chance = Math.min(1.0, (hoursSinceLastEvent - 48.0) / 48.0);
                    if (random.nextDouble() > chance) {
                        continue; // Event not triggered this time
                    }
                }

                // Trigger a random event
                triggerRandomEvent(guild, guildId, now);

            } catch (Exception e) {
                logger.error("Error checking events for guild {}", guild.getId(), e);
            }
        }
    }

    /**
     * Triggers a random Nilfheim event for a guild.
     *
     * @param guild   the guild
     * @param guildId the guild ID
     * @param now     the current time
     */
    private void triggerRandomEvent(Guild guild, String guildId, Instant now) {
        // Select random event type (weighted: common events more likely)
        NilfheimEventType[] allEvents = NilfheimEventType.values();
        NilfheimEventType selectedEvent = allEvents[random.nextInt(allEvents.length)];

        // Set active event (expires in 12 hours)
        Instant expiresAt = now.plusSeconds(EVENT_DURATION_HOURS * 3600);
        eventService.setActiveEvent(guildId, selectedEvent, expiresAt);
        eventService.setLastEventTime(guildId, now);

        logger.info("Triggered Nilfheim event {} for guild {} (expires at {})",
                selectedEvent.getDisplayName(), guildId, expiresAt);

        // Post announcement in RPG channel (if configured)
        postEventAnnouncement(guild, guildId, selectedEvent);
    }

    /**
     * Posts an event announcement to the RPG channel.
     *
     * @param guild     the guild
     * @param guildId   the guild ID
     * @param eventType the event type
     */
    private void postEventAnnouncement(Guild guild, String guildId, NilfheimEventType eventType) {
        RPGConfig config = characterService.getConfig(guildId);
        if (config == null) {
            return;
        }

        String channelId = config.getRpgChannelId();
        if (channelId == null) {
            // No RPG channel configured, skip announcement
            return;
        }

        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            logger.warn("RPG channel {} not found in guild {}", channelId, guildId);
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🌟 " + eventType.getDisplayName());
        embed.setDescription(eventType.getDescription());
        embed.setColor(Color.CYAN);
        embed.setFooter("This event will last for 12 hours");
        embed.setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue(
                success -> logger.info("Posted Nilfheim event announcement for {} in guild {}",
                        eventType.getDisplayName(), guildId),
                error -> logger.warn("Failed to post Nilfheim event announcement for {} in guild {}: {}",
                        eventType.getDisplayName(), guildId, error.getMessage())
        );
    }
}

