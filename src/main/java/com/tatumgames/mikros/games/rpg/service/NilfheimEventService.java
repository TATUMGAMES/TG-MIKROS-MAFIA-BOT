package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.events.NilfheimEventType;

import java.time.Instant;

/**
 * Service interface for managing server-wide Nilfheim events.
 */
public interface NilfheimEventService {
    /**
     * Gets the currently active event for a guild, if any.
     *
     * @param guildId the guild ID
     * @return the active event type, or null if no event is active
     */
    NilfheimEventType getActiveEvent(String guildId);

    /**
     * Sets an active event for a guild.
     *
     * @param guildId the guild ID
     * @param eventType the event type
     * @param expiresAt when the event expires
     */
    void setActiveEvent(String guildId, NilfheimEventType eventType, Instant expiresAt);

    /**
     * Clears the active event for a guild.
     *
     * @param guildId the guild ID
     */
    void clearActiveEvent(String guildId);

    /**
     * Gets the time when the last event was triggered for a guild.
     *
     * @param guildId the guild ID
     * @return the last event time, or null if no event has been triggered
     */
    Instant getLastEventTime(String guildId);

    /**
     * Sets the time when an event was last triggered for a guild.
     *
     * @param guildId the guild ID
     * @param eventTime the event time
     */
    void setLastEventTime(String guildId, Instant eventTime);

    /**
     * Clears all data for a guild (for cleanup).
     *
     * @param guildId the guild ID
     */
    void clearGuildData(String guildId);
}

