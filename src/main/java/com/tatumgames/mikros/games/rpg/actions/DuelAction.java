package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.model.RPGStats;

import java.util.Random;

/**
 * Duel action - player vs player combat.
 * No charge cost, rate limited to 3 per 24 hours.
 * No HP damage, no XP rewards - just win/loss tracking.
 */
public class DuelAction {
    private static final Random random = new Random();
    private final DuelNarrativeGenerator narrativeGenerator;

    public DuelAction() {
        this.narrativeGenerator = new DuelNarrativeGenerator();
    }

    /**
     * Executes a duel between two characters.
     *
     * @param challenger the challenging character
     * @param target     the target character
     * @param config     the guild RPG configuration
     * @return the action outcome
     */
    public RPGActionOutcome executeDuel(RPGCharacter challenger, RPGCharacter target, RPGConfig config) {
        // Calculate player power
        int challengerPower = calculatePower(challenger);
        int targetPower = calculatePower(target);

        // Add luck modifiers
        RPGStats challengerStats = challenger.getStats();
        RPGStats targetStats = target.getStats();
        int challengerRoll = challengerPower + (challengerStats.getLuck() * 2);
        int targetRoll = targetPower + (targetStats.getLuck() * 2);

        // Add some randomness
        challengerRoll += random.nextInt(20) - 10;
        targetRoll += random.nextInt(20) - 10;

        boolean challengerWins = challengerRoll > targetRoll;

        // Generate narrative
        String narrative = narrativeGenerator.generateNarrative(challenger, target, challengerWins);

        // No XP, no damage - just narrative and outcome
        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(0)
                .leveledUp(false)
                .damageTaken(0)
                .hpRestored(0)
                .success(challengerWins)
                .build();
    }

    /**
     * Calculates player power from stats based on class.
     *
     * @param character the character
     * @return calculated power
     */
    private int calculatePower(RPGCharacter character) {
        RPGStats stats = character.getStats();
        String className = character.getCharacterClass().name();

        return switch (className) {
            case "WARRIOR", "KNIGHT" -> (stats.getStrength() * 2) + stats.getAgility();
            case "MAGE", "PRIEST" -> (stats.getIntelligence() * 2) + stats.getAgility();
            case "ROGUE" -> (stats.getAgility() * 2) + stats.getStrength();
            case "NECROMANCER" -> (stats.getIntelligence() * 2) + stats.getLuck();
            default -> stats.getStrength() + stats.getAgility() + stats.getIntelligence();
        };
    }
}

