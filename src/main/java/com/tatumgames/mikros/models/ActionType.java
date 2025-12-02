package com.tatumgames.mikros.models;

/**
 * Enum representing different types of moderation actions.
 */
public enum ActionType {
    /**
     * Warning action - warns a user without removal.
     */
    WARN,

    /**
     * Kick action - removes a user temporarily from the server.
     */
    KICK,

    /**
     * Ban action - permanently removes a user from the server.
     */
    BAN
}

