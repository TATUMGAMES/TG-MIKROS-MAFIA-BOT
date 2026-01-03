package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.achievements.Title;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing title definitions and unlock validation.
 */
public class TitleService {

    /**
     * Checks which titles a character has newly unlocked.
     *
     * @param character the character to check
     * @return list of newly unlocked titles
     */
    public List<Title> checkTitleUnlocks(RPGCharacter character) {
        List<Title> newlyUnlocked = new ArrayList<>();

        // Check progress titles (level-based)
        for (Title title : Title.values()) {
            if (title.getType() == Title.TitleType.PROGRESS) {
                if (character.getLevel() >= title.getRequiredLevel()) {
                    // Check if character already has this title (we'll check this later when we add title storage)
                    // For now, we'll return all eligible titles
                    newlyUnlocked.add(title);
                }
            }
        }

        // Check feat titles
        if (character.getTotalExplores() >= 500 && !hasTitle(character, Title.WANDERER)) {
            newlyUnlocked.add(Title.WANDERER);
        }

        if (character.getTotalChargesDonated() >= 10 && !hasTitle(character, Title.BEARER_OF_BURDENS)) {
            newlyUnlocked.add(Title.BEARER_OF_BURDENS);
        }

        // Pattern titles (checked separately via pattern detection)
        if (character.getExploreStreak() >= 15 && !hasTitle(character, Title.THE_EXPLORER)) {
            newlyUnlocked.add(Title.THE_EXPLORER);
        }
        if (character.getTrainStreak() >= 15 && !hasTitle(character, Title.THE_DISCIPLINED)) {
            newlyUnlocked.add(Title.THE_DISCIPLINED);
        }
        if (character.getRestStreak() >= 10 && !hasTitle(character, Title.THE_RESTFUL)) {
            newlyUnlocked.add(Title.THE_RESTFUL);
        }
        if (character.getBattleStreak() >= 15 && !hasTitle(character, Title.THE_WARRIOR)) {
            newlyUnlocked.add(Title.THE_WARRIOR);
        }

        // Failure-based titles (cursed worlds)
        if (character.getCursedBossFights() >= 5 && !hasTitle(character, Title.HOPE_UNBROKEN)) {
            newlyUnlocked.add(Title.HOPE_UNBROKEN);
        }
        if (character.hasActedDuringBothCurses() && !hasTitle(character, Title.CURSEWALKER)) {
            newlyUnlocked.add(Title.CURSEWALKER);
        }
        // Lightbearer (Priest) and Bound to Death (Necromancer) are checked separately
        // when resurrections are performed or March of the Dead is active

        // Irrevocable World Encounter titles
        if ("UNBOUND".equals(character.getPhilosophicalPath()) && !hasTitle(character, Title.UNBOUND)) {
            newlyUnlocked.add(Title.UNBOUND);
        }
        if (character.hasWorldFlag("STONE_WOLF_MARKED") && !hasTitle(character, Title.STONE_WOLF_CHOSEN)) {
            newlyUnlocked.add(Title.STONE_WOLF_CHOSEN);
        }
        // Icewalker, Rune-Seer, Fortune's Favorite are awarded based on stat interaction successes
        // (tracked via world flags or story flags)

        // Note: First-to titles and feat titles like "The Unbroken" and "Gravecaller" are handled
        // by achievement system and resurrection tracking, not here

        return newlyUnlocked;
    }

    /**
     * Gets all titles a character can equip (has unlocked).
     *
     * @param character the character
     * @return list of available titles
     */
    public List<Title> getAvailableTitles(RPGCharacter character) {
        List<Title> available = new ArrayList<>();

        // Progress titles
        for (Title title : Title.values()) {
            if (title.getType() == Title.TitleType.PROGRESS) {
                if (character.getLevel() >= title.getRequiredLevel()) {
                    available.add(title);
                }
            }
        }

        // Feat titles
        if (character.getTotalExplores() >= 500) {
            available.add(Title.WANDERER);
        }
        if (character.getTotalChargesDonated() >= 10) {
            available.add(Title.BEARER_OF_BURDENS);
        }

        // Pattern titles
        if (character.getExploreStreak() >= 15) {
            available.add(Title.THE_EXPLORER);
        }
        if (character.getTrainStreak() >= 15) {
            available.add(Title.THE_DISCIPLINED);
        }
        if (character.getRestStreak() >= 10) {
            available.add(Title.THE_RESTFUL);
        }
        if (character.getBattleStreak() >= 15) {
            available.add(Title.THE_WARRIOR);
        }

        // Failure-based titles (cursed worlds)
        if (character.getCursedBossFights() >= 5) {
            available.add(Title.HOPE_UNBROKEN);
        }
        if (character.hasActedDuringBothCurses()) {
            available.add(Title.CURSEWALKER);
        }
        // Lightbearer (Priest: 10 resurrections during cursed worlds)
        if (character.getCursedResurrections() >= 10 && 
            character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.PRIEST) {
            available.add(Title.LIGHTBEARER);
        }
        // Bound to Death (Necromancer: Active during March of the Dead)
        // Checked via story flag "Bound to Death" when March of the Dead is active
        if (character.getStoryFlags().contains("Bound to Death") &&
            character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.NECROMANCER) {
            available.add(Title.BOUND_TO_DEATH);
        }

        // Irrevocable World Encounter titles
        if ("UNBOUND".equals(character.getPhilosophicalPath())) {
            available.add(Title.UNBOUND);
        }
        if (character.hasWorldFlag("STONE_WOLF_MARKED")) {
            available.add(Title.STONE_WOLF_CHOSEN);
        }
        if (character.hasWorldFlag("FROSTWIND_MARKED")) {
            // Could add "Frostwind's Chosen" title if needed
        }
        if (character.hasWorldFlag("HOLLOW_MIND_MARKED")) {
            // Could add "Hollow Mind's Chosen" title if needed
        }
        // Icewalker, Rune-Seer, Fortune's Favorite can be added based on stat interaction tracking

        // First-to titles (if character has the title set, they have it)
        if (character.getTitle() != null) {
            try {
                Title currentTitle = Title.valueOf(character.getTitle().toUpperCase().replace(" ", "_"));
                if (currentTitle.getType() == Title.TitleType.FIRST_TO) {
                    available.add(currentTitle);
                }
            } catch (IllegalArgumentException e) {
                // Title not found in enum, skip
            }
        }

        return available;
    }

    /**
     * Gets the bonus multiplier for a title.
     *
     * @param title the title name (display name or enum name)
     * @return bonus multiplier (0.02 = 2%), or 0.0 if no bonus
     */
    public double getTitleBonus(String title) {
        if (title == null) {
            return 0.0;
        }

        try {
            // Try to find by display name first
            for (Title t : Title.values()) {
                if (t.getDisplayName().equalsIgnoreCase(title)) {
                    return t.getBonus();
                }
            }

            // Try enum name
            Title t = Title.valueOf(title.toUpperCase().replace(" ", "_"));
            return t.getBonus();
        } catch (IllegalArgumentException e) {
            return 0.0;
        }
    }

    /**
     * Checks if a character has a specific title.
     *
     * @param character the character
     * @param title the title to check
     * @return true if character has the title
     */
    private boolean hasTitle(RPGCharacter character, Title title) {
        String currentTitle = character.getTitle();
        if (currentTitle == null) {
            return false;
        }
        return currentTitle.equalsIgnoreCase(title.getDisplayName()) ||
               currentTitle.equalsIgnoreCase(title.name());
    }
}

