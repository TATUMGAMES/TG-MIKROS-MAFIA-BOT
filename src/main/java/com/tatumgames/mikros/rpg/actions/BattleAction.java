package com.tatumgames.mikros.rpg.actions;

import com.tatumgames.mikros.rpg.config.RPGConfig;
import com.tatumgames.mikros.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.rpg.model.RPGCharacter;
import com.tatumgames.mikros.rpg.model.RPGStats;

import java.util.Random;

/**
 * Battle action - player fights an AI enemy.
 * Can result in victory (high XP) or defeat (damage taken, some XP).
 * 
 * TODO: Future Features
 * - Enemy variety with different stats and abilities
 * - Boss battles with special rewards
 * - PvP battles between players
 * - Battle items and consumables
 */
public class BattleAction implements CharacterAction {
    private static final Random random = new Random();
    
    private static final String[] ENEMY_NAMES = {
            "Goblin Scout", "Wild Wolf", "Bandit Thief", "Slime Monster",
            "Skeleton Warrior", "Dark Mage", "Forest Troll", "Shadow Assassin",
            "Fire Elemental", "Ice Golem", "Corrupted Knight", "Venomous Spider",
            "Orc Berserker", "Necromancer", "Dragon Whelp", "Demon Imp"
    };
    
    @Override
    public String getActionName() {
        return "battle";
    }
    
    @Override
    public String getActionEmoji() {
        return "⚔️";
    }
    
    @Override
    public String getDescription() {
        return "Battle an enemy for high XP rewards";
    }
    
    @Override
    public RPGActionOutcome execute(RPGCharacter character, RPGConfig config) {
        // Select random enemy
        String enemyName = ENEMY_NAMES[random.nextInt(ENEMY_NAMES.length)];
        
        // Calculate enemy strength based on character level
        int enemyLevel = Math.max(1, character.getLevel() + random.nextInt(3) - 1);
        int enemyPower = calculateEnemyPower(enemyLevel);
        
        // Calculate player power
        RPGStats stats = character.getStats();
        int playerPower = calculatePlayerPower(stats, character.getCharacterClass().name());
        
        // Determine battle outcome
        int playerRoll = random.nextInt(playerPower) + (stats.getLuck() * 2);
        int enemyRoll = random.nextInt(enemyPower);
        
        boolean victory = playerRoll > enemyRoll;
        
        // Calculate results
        int xpGained;
        int damageTaken;
        String narrative;
        
        if (victory) {
            // Victory: high XP, minimal damage
            xpGained = (int) ((50 + (enemyLevel * 10)) * config.getXpMultiplier());
            damageTaken = Math.max(5, enemyLevel * 3);
            
            narrative = String.format(
                    "You encountered a **%s** (Level %d) and emerged victorious! " +
                    "Your combat prowess proved superior.",
                    enemyName, enemyLevel
            );
        } else {
            // Defeat: moderate XP, significant damage
            xpGained = (int) ((20 + (enemyLevel * 4)) * config.getXpMultiplier());
            damageTaken = Math.max(10, enemyLevel * 8);
            
            narrative = String.format(
                    "You encountered a **%s** (Level %d) but were defeated. " +
                    "You managed to escape, but not without injury. Learn from this experience!",
                    enemyName, enemyLevel
            );
        }
        
        // Apply damage (but don't kill character)
        stats.takeDamage(damageTaken);
        if (stats.getCurrentHp() <= 0) {
            stats.setCurrentHp(1); // Always leave at least 1 HP
        }
        
        // Add XP and check for level up
        boolean leveledUp = character.addXp(xpGained);
        
        // Record the action
        character.recordAction();
        
        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .damageTaken(damageTaken)
                .success(victory)
                .build();
    }
    
    /**
     * Calculates enemy power based on level.
     */
    private int calculateEnemyPower(int level) {
        return 20 + (level * 8);
    }
    
    /**
     * Calculates player power from stats.
     */
    private int calculatePlayerPower(RPGStats stats, String className) {
        int basePower = 0;
        
        // Different classes use different stats for combat
        switch (className) {
            case "WARRIOR":
                basePower = (stats.getStrength() * 2) + stats.getAgility();
                break;
            case "MAGE":
                basePower = (stats.getIntelligence() * 2) + stats.getAgility();
                break;
            case "ROGUE":
                basePower = (stats.getAgility() * 2) + stats.getStrength();
                break;
            default:
                basePower = stats.getStrength() + stats.getAgility() + stats.getIntelligence();
        }
        
        return basePower;
    }
}

