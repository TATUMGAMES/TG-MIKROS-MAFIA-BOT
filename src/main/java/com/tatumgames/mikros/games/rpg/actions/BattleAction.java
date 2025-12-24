package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.CatalystType;
import com.tatumgames.mikros.games.rpg.model.EnemyType;
import com.tatumgames.mikros.games.rpg.model.EssenceType;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.model.RPGStats;

import java.util.HashMap;
import java.util.Map;
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
            "Spirit Snake", "Skeletal Horse", "Frost Sprite Cluster",
            // New 30 enemies (Content Expansion)
            // PHYSICAL (5)
            "Frostbound Berserker", "Ironclad Marauder", "Glacial Brute", "Stonefist Warrior", "Frozen Knight",
            // MAGICAL (5)
            "Void Whisperer", "Arcane Wraith", "Frost Sorcerer", "Shadow Mage", "Crystal Enchanter",
            // AGILE (5)
            "Shadow Stalker", "Wind Dancer", "Frost Sprite", "Swift Reaper", "Blade Phantom",
            // UNDEAD (5)
            "Bone Reaver", "Soul Eater", "Grave Wight", "Necrotic Horror", "Frozen Lich",
            // BEAST (5)
            "Ice Wolf Pack", "Frost Bear", "Dire Frost Wolf", "Glacial Predator", "Tundra Beast",
            // CONSTRUCT (5)
            "Runic Golem", "Ice Sentinel", "Crystal Guardian", "Frost Automaton", "Stone Guardian"
    };

    // Enemy type mappings
    private static final Map<String, EnemyType> ENEMY_TYPE_MAP = new HashMap<>();
    
    static {
        // PHYSICAL enemies (armored, brute force)
        ENEMY_TYPE_MAP.put("Goblin Scout", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Orc Berserker", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Corrupted Knight", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Spirit Knight", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Hollow Knight", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Possessed Armor", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Forest Troll", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Frost Troll", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Marauder", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Bandit Thief", EnemyType.PHYSICAL);
        
        // MAGICAL enemies (spellcasters, magical creatures)
        ENEMY_TYPE_MAP.put("Dark Mage", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Necromancer", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Frostbound Sorcerer", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Ice Golem", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Frost Wisp", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Wailing Wisp", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Fire Elemental", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Wraithling", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Shrieking Banshee", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Frost Sprite Cluster", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Corrupted Dryad", EnemyType.MAGICAL);
        
        // AGILE enemies (fast, evasive)
        ENEMY_TYPE_MAP.put("Shadow Assassin", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Shade Assassin", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Frostfang Lynx", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Dire Bat", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Venomous Spider", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Storm Raven", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Blight Raven", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Rime Drifter", EnemyType.AGILE);
        
        // UNDEAD enemies
        ENEMY_TYPE_MAP.put("Skeleton Warrior", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Wandering Revenant", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Death-Rattle Skeleton", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Frozen Ghoul", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Coldshade Phantom", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Skeletal Horse", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Spirit Snake", EnemyType.UNDEAD);
        
        // BEAST enemies (animalistic)
        ENEMY_TYPE_MAP.put("Wild Wolf", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Frost-Bitten Bear", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Frost Goblin", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Ice Stalker", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Enraged Wendigo", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Bone Warg", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Mutated Frost Boar", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Blighted Serpent", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Corrupted Elk", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Dragon Whelp", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Demon Imp", EnemyType.BEAST);
        
        // CONSTRUCT enemies (mechanical/stone)
        ENEMY_TYPE_MAP.put("Snow Golem", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Crystal Spider", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Slime Monster", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Glacial Slime", EnemyType.CONSTRUCT);
        
        // New PHYSICAL enemies
        ENEMY_TYPE_MAP.put("Frostbound Berserker", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Ironclad Marauder", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Glacial Brute", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Stonefist Warrior", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Frozen Knight", EnemyType.PHYSICAL);
        
        // New MAGICAL enemies
        ENEMY_TYPE_MAP.put("Void Whisperer", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Arcane Wraith", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Frost Sorcerer", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Shadow Mage", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Crystal Enchanter", EnemyType.MAGICAL);
        
        // New AGILE enemies
        ENEMY_TYPE_MAP.put("Shadow Stalker", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Wind Dancer", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Frost Sprite", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Swift Reaper", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Blade Phantom", EnemyType.AGILE);
        
        // New UNDEAD enemies
        ENEMY_TYPE_MAP.put("Bone Reaver", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Soul Eater", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Grave Wight", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Necrotic Horror", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Frozen Lich", EnemyType.UNDEAD);
        
        // New BEAST enemies
        ENEMY_TYPE_MAP.put("Ice Wolf Pack", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Frost Bear", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Dire Frost Wolf", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Glacial Predator", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Tundra Beast", EnemyType.BEAST);
        
        // New CONSTRUCT enemies
        ENEMY_TYPE_MAP.put("Runic Golem", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Ice Sentinel", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Crystal Guardian", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Frost Automaton", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Stone Guardian", EnemyType.CONSTRUCT);
        
        // Default fallback for any unmapped enemies
    }

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
        EnemyType enemyType = ENEMY_TYPE_MAP.getOrDefault(enemyName, EnemyType.PHYSICAL); // Default fallback

        // Calculate enemy strength based on character level
        int enemyLevel = Math.max(1, character.getLevel() + random.nextInt(3) - 1);
        int enemyPower = calculateEnemyPower(enemyLevel);

        // Calculate player power with stat effectiveness
        RPGStats stats = character.getStats();
        int basePlayerPower = calculatePlayerPower(stats, character.getCharacterClass().name());
        double effectiveness = getStatEffectiveness(character.getCharacterClass().name(), enemyType);
        int playerPower = (int) (basePlayerPower * effectiveness);

        // Check for critical hit (AGI-based)
        boolean isCrit = false;
        double critChance = stats.getAgility() / 2.0 / 100.0; // AGI/2% chance
        
        // Mage Arcane Precision: +5% crit chance
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.MAGE) {
            critChance += 0.05;
        }
        
        if (random.nextDouble() < critChance) {
            isCrit = true;
            // Rogue Lethal Strikes: 2.0x damage instead of 1.5x
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.ROGUE) {
                playerPower = (int) (playerPower * 2.0);
            } else {
                playerPower = (int) (playerPower * 1.5); // 1.5x damage on crit
            }
        }
        
        // Warrior Berserker Rage: +10% damage when HP < 50%
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.WARRIOR) {
            double hpPercent = (double) stats.getCurrentHp() / stats.getMaxHp();
            if (hpPercent < 0.50) {
                playerPower = (int) (playerPower * 1.10); // +10% damage
            }
        }

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
            int baseXp = (int) ((50 + (enemyLevel * 10)) * config.getXpMultiplier());
            
            // Apply INT-based XP bonus (INT/10% bonus, capped at 15%)
            double intBonus = Math.min(0.15, stats.getIntelligence() * 0.01);
            xpGained = (int) (baseXp * (1 + intBonus));
            
            // Apply LUCK-based XP floor (prevents extremely bad rolls)
            int minXp = (int) (baseXp * (1 + stats.getLuck() / 20.0));
            xpGained = Math.max(minXp, xpGained);

            // Necromancer Decay: 10% chance to double XP on victory
            boolean decayTriggered = false;
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.NECROMANCER) {
                if (random.nextDouble() < 0.10) { // 10% chance
                    xpGained *= 2; // Double XP
                    decayTriggered = true;
                }
            }

            // Increment kill counter
            character.incrementEnemiesKilled();

            // Generate narrative with stat effectiveness and crit mentions
            String effectivenessNote = "";
            if (effectiveness > 1.0) {
                effectivenessNote = getEffectivenessNarrative(character.getCharacterClass(), enemyType, true);
            } else if (effectiveness < 1.0) {
                effectivenessNote = getEffectivenessNarrative(character.getCharacterClass(), enemyType, false);
            }
            
            String critNote = "";
            if (isCrit) {
                critNote = getCritNarrative(character.getCharacterClass());
            }
            
            String classNote = "";
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.WARRIOR) {
                double hpPercent = (double) stats.getCurrentHp() / stats.getMaxHp();
                if (hpPercent < 0.50) {
                    classNote = " Your rage intensifies as your wounds mount!";
                }
            }
            
            String decayNote = decayTriggered ? 
                    "\n\n💀 **Decay Effect:** Your dark magic saps the enemy's essence, doubling your XP gain!" : "";
            
            String agilityNote = stats.getAgility() >= 15 ? 
                    " Your swift movements helped you avoid the worst of the enemy's attacks." : "";
            
            narrative = String.format(
                    "You encountered a **%s** %s (Level %d) and emerged victorious!%s%s%s " +
                            "Your combat prowess proved superior, though you sustained minor wounds.%s%s",
                    enemyType.getEmoji(), enemyName, enemyLevel, critNote, effectivenessNote, classNote, agilityNote, decayNote
            );
        } else {
            // Defeat: moderate XP, significant damage
            int baseXp = (int) ((20 + (enemyLevel * 4)) * config.getXpMultiplier());
            
            // Apply INT-based XP bonus (INT/10% bonus, capped at 15%)
            double intBonus = Math.min(0.15, stats.getIntelligence() * 0.01);
            xpGained = (int) (baseXp * (1 + intBonus));
            
            // Apply LUCK-based XP floor
            int minXp = (int) (baseXp * (1 + stats.getLuck() / 20.0));
            xpGained = Math.max(minXp, xpGained);

            // Generate narrative emphasizing injury severity
            String injurySeverity = enemyLevel >= 5 ? 
                    " You suffered severe injuries in the encounter." :
                    " You managed to escape, but not without significant injury.";
            
            narrative = String.format(
                    "You encountered a **%s** %s (Level %d) but were defeated.%s " +
                            "Learn from this experience!",
                    enemyType.getEmoji(), enemyName, enemyLevel, injurySeverity
            );
        }

        // Calculate base damage with variance
        int baseDamage = calculateBaseDamage(enemyPower, enemyLevel, victory);
        
        // Apply ±25% random variance
        // Variance range: baseDamage * 0.75 to baseDamage * 1.25
        int minDamage = (int) (baseDamage * 0.75);
        int maxDamage = (int) (baseDamage * 1.25);
        int damageWithVariance = minDamage + random.nextInt(maxDamage - minDamage + 1);
        damageWithVariance = Math.max(1, damageWithVariance); // Ensure at least 1 damage

        // Apply agility-based defense (1% per agility point, capped at 30%)
        double agilityReduction = Math.min(0.30, stats.getAgility() * 0.01);
        damageTaken = (int) (damageWithVariance * (1 - agilityReduction));

        // Apply class bonus (Knight gets 10% additional reduction, stacks with agility)
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.KNIGHT) {
            damageTaken = (int) (damageTaken * 0.90); // 10% reduction (balanced from 15%)
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

        // Roll for item drops with LUCK bonus
        RPGActionOutcome.Builder outcomeBuilder = RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .damageTaken(damageTaken)
                .hpRestored(0)
                .success(victory);

        // Base drop chance: Victory 20%, Defeat 5%
        double baseDropChance = victory ? 0.20 : 0.05;
        // LUCK bonus: +0.3% per LUCK point, capped at +10%
        double luckBonus = Math.min(0.10, stats.getLuck() * 0.003);
        double dropChance = baseDropChance + luckBonus;
        
        if (random.nextDouble() < dropChance) {
            // LUCK-based rare item chance: LUCK/10% chance for catalyst instead of essence
            boolean isCatalyst = random.nextDouble() < (stats.getLuck() / 10.0 / 100.0);
            
            if (isCatalyst) {
                CatalystType catalyst = getRandomCatalyst();
                outcomeBuilder.addCatalystDrop(catalyst, 1);
                character.getInventory().addCatalyst(catalyst, 1);
            } else {
                EssenceType essence = getRandomEssence();
                outcomeBuilder.addItemDrop(essence, 1);
                character.getInventory().addEssence(essence, 1);
            }
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
     * Gets a random catalyst type.
     *
     * @return random catalyst type
     */
    private CatalystType getRandomCatalyst() {
        CatalystType[] catalysts = CatalystType.values();
        return catalysts[random.nextInt(catalysts.length)];
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

    /**
     * Gets stat effectiveness multiplier based on class and enemy type.
     *
     * @param className  the character class name
     * @param enemyType  the enemy type
     * @return effectiveness multiplier (1.3x for effective, 0.85x for weak, 1.0x for neutral)
     */
    private double getStatEffectiveness(String className, EnemyType enemyType) {
        // STR-based classes (Warrior, Knight)
        if (className.equals("WARRIOR") || className.equals("KNIGHT")) {
            return switch (enemyType) {
                case PHYSICAL, CONSTRUCT -> 1.3; // STR effective
                case MAGICAL, AGILE -> 0.85; // STR weak
                default -> 1.0; // Neutral
            };
        }
        // INT-based classes (Mage, Priest, Necromancer)
        else if (className.equals("MAGE") || className.equals("PRIEST") || className.equals("NECROMANCER")) {
            return switch (enemyType) {
                case MAGICAL, UNDEAD -> 1.3; // INT effective
                case CONSTRUCT, PHYSICAL -> 0.85; // INT weak
                default -> 1.0; // Neutral
            };
        }
        // AGI-based classes (Rogue)
        else if (className.equals("ROGUE")) {
            return switch (enemyType) {
                case AGILE, BEAST -> 1.3; // AGI effective
                case PHYSICAL, CONSTRUCT -> 0.85; // AGI weak
                default -> 1.0; // Neutral
            };
        }
        return 1.0; // Default neutral
    }

    /**
     * Gets the primary stat name for a class (for narrative purposes).
     *
     * @param className the character class name
     * @return primary stat name
     */
    private String getPrimaryStatName(String className) {
        return switch (className) {
            case "WARRIOR", "KNIGHT" -> "physical";
            case "MAGE", "PRIEST", "NECROMANCER" -> "magical";
            case "ROGUE" -> "swift";
            default -> "combat";
        };
    }

    /**
     * Gets effectiveness-based narrative text.
     *
     * @param characterClass the character's class
     * @param enemyType      the enemy type
     * @param effective      whether the attack was effective
     * @return narrative text
     */
    private String getEffectivenessNarrative(com.tatumgames.mikros.games.rpg.model.CharacterClass characterClass, EnemyType enemyType, boolean effective) {
        if (effective) {
            return switch (characterClass) {
                case WARRIOR, KNIGHT -> switch (enemyType) {
                    case PHYSICAL -> " Your brute strength shattered the enemy's armor!";
                    case CONSTRUCT -> " Your physical might crushed through the construct's defenses!";
                    default -> " Your strength proved highly effective!";
                };
                case MAGE, PRIEST, NECROMANCER -> switch (enemyType) {
                    case MAGICAL -> " Your magic pierced through the enemy's magical barriers!";
                    case UNDEAD -> " Your magic burned through the undead's dark essence!";
                    default -> " Your magical attacks were highly effective!";
                };
                case ROGUE -> switch (enemyType) {
                    case AGILE -> " Your swift strikes found gaps in the enemy's defenses!";
                    case BEAST -> " Your precision strikes overwhelmed the beast!";
                    default -> " Your agility proved highly effective!";
                };
            };
        } else {
            return switch (characterClass) {
                case WARRIOR, KNIGHT -> switch (enemyType) {
                    case MAGICAL -> " Your physical attacks struggled against the enemy's magical defenses.";
                    case AGILE -> " The enemy's speed made your heavy strikes difficult to land.";
                    default -> " Your attacks were less effective against this enemy type.";
                };
                case MAGE, PRIEST, NECROMANCER -> switch (enemyType) {
                    case CONSTRUCT -> " Your magic had little effect on the construct's mechanical nature.";
                    case PHYSICAL -> " The enemy's heavy armor resisted your magical attacks.";
                    default -> " Your attacks were less effective against this enemy type.";
                };
                case ROGUE -> switch (enemyType) {
                    case PHYSICAL -> " The enemy's heavy armor deflected your precision strikes.";
                    case CONSTRUCT -> " Your blades couldn't pierce the construct's stone shell.";
                    default -> " Your attacks were less effective against this enemy type.";
                };
            };
        }
    }

    /**
     * Gets critical hit narrative text based on class.
     *
     * @param characterClass the character's class
     * @return crit narrative text
     */
    private String getCritNarrative(com.tatumgames.mikros.games.rpg.model.CharacterClass characterClass) {
        return switch (characterClass) {
            case ROGUE -> " A lethal strike finds the perfect opening!";
            case MAGE -> " Your magical precision finds a critical weakness!";
            case WARRIOR -> " A devastating critical blow!";
            case KNIGHT -> " Your shield bash finds a critical opening!";
            case NECROMANCER -> " Your dark magic strikes at the enemy's very soul!";
            case PRIEST -> " Your holy magic finds a critical weakness in the enemy's defenses!";
        };
    }

    /**
     * Calculates base damage based on enemy power and level.
     * Scales with enemy strength and applies different formulas for victory vs defeat.
     *
     * @param enemyPower the calculated enemy power
     * @param enemyLevel the enemy's level
     * @param victory    whether the player won the battle
     * @return base damage before variance and reductions
     */
    private int calculateBaseDamage(int enemyPower, int enemyLevel, boolean victory) {
        if (victory) {
            // Victory: base damage scales with enemy power (15% of power)
            int baseDamage = (int) (enemyPower * 0.15);
            // Minimum: 8 + (enemyLevel * 2) to ensure meaningful damage even at low levels
            int minDamage = 8 + (enemyLevel * 2);
            // Maximum: 25% of enemy power to prevent extreme values
            int maxDamage = (int) (enemyPower * 0.25);
            // Clamp base damage between minimum and maximum
            return Math.max(minDamage, Math.min(baseDamage, maxDamage));
        } else {
            // Defeat: base damage scales with enemy power (35% of power)
            int baseDamage = (int) (enemyPower * 0.35);
            // Minimum: 15 + (enemyLevel * 4) to ensure defeat is costly
            int minDamage = 15 + (enemyLevel * 4);
            // Maximum: 50% of enemy power to prevent extreme values
            int maxDamage = (int) (enemyPower * 0.50);
            // Clamp base damage between minimum and maximum
            return Math.max(minDamage, Math.min(baseDamage, maxDamage));
        }
    }
}
