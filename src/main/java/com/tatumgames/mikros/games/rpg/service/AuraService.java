package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.achievements.LegendaryAura;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing legendary auras and applying their effects.
 */
public class AuraService {
    // Map: guildId -> (LegendaryAura -> List of userIds)
    private final Map<String, Map<LegendaryAura, List<String>>> auraHolders;

    public AuraService() {
        this.auraHolders = new ConcurrentHashMap<>();
    }

    /**
     * Gets all holders of a specific aura in a guild.
     *
     * @param guildId the Discord guild ID
     * @param aura the legendary aura
     * @return list of user IDs who hold the aura
     */
    public List<String> getAuraHolders(String guildId, LegendaryAura aura) {
        Map<LegendaryAura, List<String>> guildAuras = auraHolders.get(guildId);
        if (guildAuras == null) {
            return Collections.emptyList();
        }
        List<String> holders = guildAuras.get(aura);
        return holders != null ? new ArrayList<>(holders) : Collections.emptyList();
    }

    /**
     * Checks if a character can acquire a legendary aura.
     *
     * @param guildId the Discord guild ID
     * @param aura the legendary aura
     * @param character the character attempting to acquire
     * @return true if can acquire
     */
    public boolean canAcquireAura(String guildId, LegendaryAura aura, RPGCharacter character) {
        Map<LegendaryAura, List<String>> guildAuras = auraHolders.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>());
        List<String> holders = guildAuras.computeIfAbsent(aura, k -> new ArrayList<>());

        // Check max holders
        int maxHolders = getMaxHolders(aura);
        if (holders.size() >= maxHolders) {
            return false;
        }

        // Check class requirement (Gravebound Presence requires Necromancer)
        if (aura == LegendaryAura.GRAVEBOUND_PRESENCE) {
            if (character.getCharacterClass() != CharacterClass.NECROMANCER) {
                return false;
            }
            // Check if another Necromancer already has it
            // Note: We'd need CharacterService to check class, but for now we'll allow it
            // The actual check should be done before calling this method
        }

        return true;
    }

    /**
     * Acquires a legendary aura for a character.
     *
     * @param guildId the Discord guild ID
     * @param aura the legendary aura
     * @param userId the user ID acquiring the aura
     * @return true if successfully acquired
     */
    public boolean acquireAura(String guildId, LegendaryAura aura, String userId) {
        Map<LegendaryAura, List<String>> guildAuras = auraHolders.computeIfAbsent(guildId, k -> new ConcurrentHashMap<>());
        List<String> holders = guildAuras.computeIfAbsent(aura, k -> new ArrayList<>());

        // Check max holders
        int maxHolders = getMaxHolders(aura);
        if (holders.size() >= maxHolders) {
            return false;
        }

        // Add holder if not already present
        if (!holders.contains(userId)) {
            holders.add(userId);
            return true;
        }

        return false;
    }

    /**
     * Gets the maximum number of holders for an aura.
     *
     * @param aura the legendary aura
     * @return maximum holders
     */
    private int getMaxHolders(LegendaryAura aura) {
        switch (aura) {
            case SONG_OF_NILFHEIM:
                return 2;
            case HEROS_MARK:
                return 1;
            case GRAVEBOUND_PRESENCE:
                return 1;
            default:
                return 1;
        }
    }

    /**
     * Applies Song of Nilfheim aura effect (+5% damage to all in boss battles).
     *
     * @param guildId the Discord guild ID
     * @param participants list of participant user IDs
     * @param baseDamage the base damage before aura
     * @return damage with aura bonus applied (if aura holder present)
     */
    public int applyAuraEffects(String guildId, List<String> participants, int baseDamage) {
        List<String> songHolders = getAuraHolders(guildId, LegendaryAura.SONG_OF_NILFHEIM);
        
        // Check if any participant has the aura
        boolean hasAuraHolder = false;
        for (String participantId : participants) {
            if (songHolders.contains(participantId)) {
                hasAuraHolder = true;
                break;
            }
        }

        if (hasAuraHolder) {
            // Apply +5% damage bonus
            return (int) (baseDamage * 1.05);
        }

        return baseDamage;
    }

    /**
     * Applies Hero's Mark penalty (+10% damage from bosses).
     *
     * @param character the character with Hero's Mark
     * @param baseDamage the base damage from boss
     * @return damage with penalty applied
     */
    public int applyHerosMarkPenalty(RPGCharacter character, int baseDamage) {
        if (character.getLegendaryAura() != null && 
            character.getLegendaryAura().equals(LegendaryAura.HEROS_MARK.name())) {
            // Apply +10% damage penalty
            return (int) (baseDamage * 1.10);
        }
        return baseDamage;
    }

    /**
     * Checks if a character has a specific aura.
     *
     * @param character the character
     * @param aura the aura to check
     * @return true if character has the aura
     */
    public boolean hasAura(RPGCharacter character, LegendaryAura aura) {
        if (character.getLegendaryAura() == null) {
            return false;
        }
        return character.getLegendaryAura().equals(aura.name());
    }

    /**
     * Checks if Song of Nilfheim is active in a guild (reduces curse penalties by 1-2%).
     *
     * @param guildId the Discord guild ID
     * @return curse penalty reduction multiplier (0.98-0.99, or 1.0 if no aura)
     */
    public double getSongOfNilfheimCurseReduction(String guildId) {
        List<String> songHolders = getAuraHolders(guildId, LegendaryAura.SONG_OF_NILFHEIM);
        if (!songHolders.isEmpty()) {
            // Reduce curse penalties by 1-2% (random between 0.98 and 0.99)
            // This means curses are 98-99% effective instead of 100%
            return 0.98 + (Math.random() * 0.01); // Random between 0.98 and 0.99
        }
        return 1.0; // No reduction
    }

    /**
     * Removes an aura from a character (if they lose it somehow).
     *
     * @param guildId the Discord guild ID
     * @param aura the legendary aura
     * @param userId the user ID to remove
     */
    public void removeAura(String guildId, LegendaryAura aura, String userId) {
        Map<LegendaryAura, List<String>> guildAuras = auraHolders.get(guildId);
        if (guildAuras == null) {
            return;
        }
        List<String> holders = guildAuras.get(aura);
        if (holders != null) {
            holders.remove(userId);
        }
    }
}

