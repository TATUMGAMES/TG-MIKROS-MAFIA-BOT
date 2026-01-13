package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.events.NilfheimEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of NilfheimEventService.
 * Tracks active events and last event times per guild.
 */
public class InMemoryNilfheimEventService implements NilfheimEventService {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryNilfheimEventService.class);

    // Map: guildId -> ActiveEvent (contains eventType and expiresAt)
    private final Map<String, ActiveEvent> activeEvents;

    // Map: guildId -> last event time
    private final Map<String, Instant> lastEventTimes;

    public InMemoryNilfheimEventService() {
        this.activeEvents = new ConcurrentHashMap<>();
        this.lastEventTimes = new ConcurrentHashMap<>();
        logger.info("InMemoryNilfheimEventService initialized");
    }

    @Override
    public NilfheimEventType getActiveEvent(String guildId) {
        ActiveEvent activeEvent = activeEvents.get(guildId);
        if (activeEvent == null) {
            return null;
        }

        // Check if event has expired
        if (Instant.now().isAfter(activeEvent.expiresAt)) {
            // Event expired, clear it
            activeEvents.remove(guildId);
            return null;
        }

        return activeEvent.eventType;
    }

    @Override
    public void setActiveEvent(String guildId, NilfheimEventType eventType, Instant expiresAt) {
        activeEvents.put(guildId, new ActiveEvent(eventType, expiresAt));
        logger.info("Set active event {} for guild {} (expires at {})", eventType.getDisplayName(), guildId, expiresAt);
    }

    @Override
    public void clearActiveEvent(String guildId) {
        activeEvents.remove(guildId);
        logger.info("Cleared active event for guild {}", guildId);
    }

    @Override
    public Instant getLastEventTime(String guildId) {
        return lastEventTimes.get(guildId);
    }

    @Override
    public void setLastEventTime(String guildId, Instant eventTime) {
        lastEventTimes.put(guildId, eventTime);
        logger.info("Set last event time for guild {} to {}", guildId, eventTime);
    }

    @Override
    public void clearGuildData(String guildId) {
        activeEvents.remove(guildId);
        lastEventTimes.remove(guildId);
        logger.info("Cleared all event data for guild {}", guildId);
    }

    /**
     * Represents an active event with expiration time.
     */
    private static class ActiveEvent {
        final NilfheimEventType eventType;
        final Instant expiresAt;

        ActiveEvent(NilfheimEventType eventType, Instant expiresAt) {
            this.eventType = eventType;
            this.expiresAt = expiresAt;
        }
    }
}

