package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.curse.WorldCurse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing World Curses per guild.
 * Curses are applied when bosses despawn undefeated and affect all players.
 */
public class WorldCurseService {
    // Map: guildId -> List of active curses
    private final Map<String, List<WorldCurse>> activeCurses;

    public WorldCurseService() {
        this.activeCurses = new ConcurrentHashMap<>();
    }

    /**
     * Applies a curse to a guild.
     * Enforces max limits: 1 minor + 1 major curse at a time.
     *
     * @param guildId the guild ID
     * @param curse the curse to apply
     * @return true if curse was applied, false if max limit reached
     */
    public boolean applyCurse(String guildId, WorldCurse curse) {
        List<WorldCurse> guildCurses = activeCurses.computeIfAbsent(guildId, k -> new ArrayList<>());

        // Check max limits
        long minorCount = guildCurses.stream()
                .filter(c -> c.getType() == WorldCurse.CurseType.MINOR)
                .count();
        long majorCount = guildCurses.stream()
                .filter(c -> c.getType() == WorldCurse.CurseType.MAJOR)
                .count();

        // If trying to add minor curse and one already exists, replace it
        if (curse.getType() == WorldCurse.CurseType.MINOR && minorCount >= 1) {
            guildCurses.removeIf(c -> c.getType() == WorldCurse.CurseType.MINOR);
        }

        // If trying to add major curse and one already exists, replace it
        if (curse.getType() == WorldCurse.CurseType.MAJOR && majorCount >= 1) {
            guildCurses.removeIf(c -> c.getType() == WorldCurse.CurseType.MAJOR);
        }

        // Add the new curse
        guildCurses.add(curse);
        return true;
    }

    /**
     * Removes a specific curse from a guild.
     *
     * @param guildId the guild ID
     * @param curse the curse to remove
     */
    public void removeCurse(String guildId, WorldCurse curse) {
        List<WorldCurse> guildCurses = activeCurses.get(guildId);
        if (guildCurses != null) {
            guildCurses.remove(curse);
            if (guildCurses.isEmpty()) {
                activeCurses.remove(guildId);
            }
        }
    }

    /**
     * Clears all curses for a guild.
     *
     * @param guildId the guild ID
     */
    public void clearAllCurses(String guildId) {
        activeCurses.remove(guildId);
    }

    /**
     * Clears curses based on duration type when a boss spawns.
     *
     * @param guildId the guild ID
     */
    public void clearCursesOnSpawn(String guildId) {
        List<WorldCurse> guildCurses = activeCurses.get(guildId);
        if (guildCurses != null) {
            guildCurses.removeIf(c -> c.getDuration() == WorldCurse.CurseDuration.UNTIL_NEXT_SPAWN);
            if (guildCurses.isEmpty()) {
                activeCurses.remove(guildId);
            }
        }
    }

    /**
     * Clears curses based on duration type when a boss is defeated.
     *
     * @param guildId the guild ID
     */
    public void clearCursesOnDefeat(String guildId) {
        List<WorldCurse> guildCurses = activeCurses.get(guildId);
        if (guildCurses != null) {
            guildCurses.removeIf(c -> c.getDuration() == WorldCurse.CurseDuration.UNTIL_NEXT_DEFEAT);
            if (guildCurses.isEmpty()) {
                activeCurses.remove(guildId);
            }
        }
    }

    /**
     * Gets all active curses for a guild.
     *
     * @param guildId the guild ID
     * @return list of active curses (empty if none)
     */
    public List<WorldCurse> getActiveCurses(String guildId) {
        List<WorldCurse> guildCurses = activeCurses.get(guildId);
        return guildCurses != null ? new ArrayList<>(guildCurses) : Collections.emptyList();
    }

    /**
     * Checks if a specific curse is active for a guild.
     *
     * @param guildId the guild ID
     * @param curse the curse to check
     * @return true if the curse is active
     */
    public boolean hasCurse(String guildId, WorldCurse curse) {
        List<WorldCurse> guildCurses = activeCurses.get(guildId);
        return guildCurses != null && guildCurses.contains(curse);
    }

    /**
     * Gets a random minor curse (for normal boss failure).
     *
     * @return random minor curse
     */
    public WorldCurse getRandomMinorCurse() {
        List<WorldCurse> minorCurses = Arrays.stream(WorldCurse.values())
                .filter(c -> c.getType() == WorldCurse.CurseType.MINOR)
                .toList();
        return minorCurses.get(new Random().nextInt(minorCurses.size()));
    }

    /**
     * Gets a random major curse (for super boss failure).
     *
     * @return random major curse
     */
    public WorldCurse getRandomMajorCurse() {
        List<WorldCurse> majorCurses = Arrays.stream(WorldCurse.values())
                .filter(c -> c.getType() == WorldCurse.CurseType.MAJOR)
                .toList();
        return majorCurses.get(new Random().nextInt(majorCurses.size()));
    }

    /**
     * Gets curse resistance multiplier for a character.
     * Oath of Null provides +5% curse resistance (0.95 multiplier = 5% reduction).
     *
     * @param character the character
     * @return resistance multiplier (1.0 = no resistance, 0.95 = 5% resistance)
     */
    public double getCurseResistance(com.tatumgames.mikros.games.rpg.model.RPGCharacter character) {
        if (character == null) {
            return 1.0;
        }
        
        // Oath of Null provides +5% curse resistance
        if (character.hasWorldFlag("OATH_OF_NULL")) {
            return 0.95; // 5% resistance = 95% of curse effect
        }
        
        // Check for Soul Anchor relic (+15% curse resistance)
        Double soulAnchorModifier = character.getStatModifier("CURSE_RESISTANCE");
        if (soulAnchorModifier != null && soulAnchorModifier > 1.0) {
            // soulAnchorModifier is 1.15, so resistance is 0.85 (15% reduction)
            return 1.0 / soulAnchorModifier;
        }
        
        return 1.0; // No resistance
    }
}

