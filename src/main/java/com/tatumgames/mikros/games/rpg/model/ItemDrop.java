package com.tatumgames.mikros.games.rpg.model;

/**
 * Record representing an essence drop from an action.
 */
public record ItemDrop(EssenceType essence, int count) {
    public ItemDrop {
        if (count < 1) {
            throw new IllegalArgumentException("Item drop count must be at least 1");
        }
    }
}

