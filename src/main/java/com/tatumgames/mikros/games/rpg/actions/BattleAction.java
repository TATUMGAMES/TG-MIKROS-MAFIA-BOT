package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.EssenceType;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.model.RPGStats;

import java.util.Random;

/**
 * Battle action - player fights an AI enemy.
 * Can result in victory (high XP) or defeat (damage taken, some XP).
 * <p>
 * TODO: Future Features
 * - Enemy variety with different stats and abilities
 * - Boss battles with special rewards
 * - PvP battles between players
 * - Battle items and consumables
 */
public class BattleAction implements CharacterAction {
    private static final Random random = new Random();

    private static final String[] ENEMY_NAMES = {
            // Original 16
            "Goblin Scout", "Wild Wolf", "Bandit Thief", "Slime Monster",
            "Skeleton Warrior", "Dark Mage", "Forest Troll", "Shadow Assassin",
            "Fire Elemental", "Ice Golem", "Corrupted Knight", "Venomous Spider",
            "Orc Berserker", "Necromancer", "Dragon Whelp", "Demon Imp",
            // Additional 20 (Nilfheim-specific)
            "Frost Goblin", "Ice Stalker", "Wailing Wisp", "Wandering Revenant",
            "Dire Bat", "Frost-Bitten Bear", "Marauder", "Crystal Spider",
            "Corrupted Elk", "Shade Assassin", "Blighted Serpent", "Spirit Knight",
            "Snow Golem", "Frost Wisp", "Enraged Wendigo", "Frostfang Lynx",
            "Glacial Slime", "Death-Rattle Skeleton", "Storm Raven", "Shrieking Banshee",
            "Frost Troll", "Rime Drifter", "Blight Raven", "Possessed Armor",
            "Coldshade Phantom", "Bone Warg", "Frostbound Sorcerer", "Mutated Frost Boar",
            "Wraithling", "Corrupted Dryad", "Hollow Knight", "Frozen Ghoul",
            "Spirit Snake", "Skeletal Horse", "Frost Sprite Cluster"
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

            // Increment kill counter
            character.incrementEnemiesKilled();

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

        // Apply Knight's 15% damage reduction
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.KNIGHT) {
            damageTaken = (int) (damageTaken * 0.85); // 15% reduction
        }

        // Apply damage (can now kill character)
        boolean isAlive = stats.takeDamage(damageTaken);

        // Check if character died
        if (!isAlive || stats.getCurrentHp() <= 0) {
            character.die();
            narrative += "\n\n💀 **You have fallen in battle!** A Priest can resurrect you.";
        }

        // Add XP and check for level up (only if alive)
        boolean leveledUp;
        if (isAlive) {
            leveledUp = character.addXp(xpGained);
        } else {
            // Dead characters still get some XP (defeat bonus)
            xpGained = (int) (xpGained * 0.5); // Half XP on death
            leveledUp = character.addXp(xpGained);
        }

        // Roll for item drops
        RPGActionOutcome.Builder outcomeBuilder = RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .damageTaken(damageTaken)
                .hpRestored(0)
                .success(victory);

        // Victory: 20% chance, Defeat: 5% chance
        double dropChance = victory ? 0.20 : 0.05;
        if (random.nextDouble() < dropChance) {
            EssenceType essence = getRandomEssence();
            outcomeBuilder.addItemDrop(essence, 1);
            
            // Add to character inventory
            character.getInventory().addEssence(essence, 1);
        }

        // Record the action
        character.recordAction();

        return outcomeBuilder.build();
    }

    /**
     * Gets a random essence type.
     *
     * @return random essence type
     */
    private EssenceType getRandomEssence() {
        EssenceType[] essences = EssenceType.values();
        return essences[random.nextInt(essences.length)];
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
        return switch (className) {
            case "WARRIOR" -> (stats.getStrength() * 2) + stats.getLuck();
            case "KNIGHT" -> (stats.getStrength() * 2) + stats.getAgility();
            case "MAGE", "PRIEST" -> (stats.getIntelligence() * 2) + stats.getAgility();
            case "ROGUE" -> (stats.getAgility() * 2) + stats.getStrength();
            case "NECROMANCER" -> (stats.getIntelligence() * 2) + stats.getLuck();
            default -> stats.getStrength() + stats.getAgility() + stats.getIntelligence();
        };
    }
}
