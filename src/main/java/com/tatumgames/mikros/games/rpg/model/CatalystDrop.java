package com.tatumgames.mikros.games.rpg.model;

/**
 * Record representing a catalyst drop from an action.
 */
public record CatalystDrop(CatalystType catalyst, int count) {
    public CatalystDrop {
        if (count < 1) {
            throw new IllegalArgumentException("Catalyst drop count must be at least 1");
        }
    }
}

