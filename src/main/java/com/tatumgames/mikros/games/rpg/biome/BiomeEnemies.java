package com.tatumgames.mikros.games.rpg.biome;

import java.util.*;

/**
 * Utility class for biome-specific enemy pools. Categorizes existing enemies by biome theme.
 */
public class BiomeEnemies {
    private static final Random random = new Random();

    // Enemies organized by biome - all enemies from existing code
    private static final Map<BiomeType, List<String>> ENEMIES_BY_BIOME = new HashMap<>();

    static {
        // FROZEN_WASTES - All frost/ice/snow/cold themed enemies
        ENEMIES_BY_BIOME.put(
                BiomeType.FROZEN_WASTES,
                Arrays.asList(
                        "Frost Goblin",
                        "Ice Stalker",
                        "Frost Wisp",
                        "Snow Golem",
                        "Frost Troll",
                        "Frost-Bitten Bear",
                        "Glacial Slime",
                        "Ice Golem",
                        "Frozen Knight",
                        "Frost Sprite",
                        "Frost Sorcerer",
                        "Ice Wolf Pack",
                        "Frost Bear",
                        "Dire Frost Wolf",
                        "Glacial Predator",
                        "Tundra Beast",
                        "Frostbound Berserker",
                        "Glacial Brute",
                        "Ice Sentinel",
                        "Frost Automaton",
                        "Frostbound Sorcerer",
                        "Rime Drifter",
                        "Frostfang Lynx",
                        "Frost Sprite Cluster",
                        "Ice Sentinel"));

        // ANCIENT_RUINS - Undead and constructs (ruins-themed)
        ENEMIES_BY_BIOME.put(
                BiomeType.ANCIENT_RUINS,
                Arrays.asList(
                        "Skeleton Warrior",
                        "Wandering Revenant",
                        "Death-Rattle Skeleton",
                        "Frozen Ghoul",
                        "Bone Reaver",
                        "Soul Eater",
                        "Grave Wight",
                        "Frozen Lich",
                        "Skeletal Horse",
                        "Bone Warg",
                        "Spirit Snake",
                        "Necrotic Horror",
                        "Snow Golem",
                        "Crystal Spider",
                        "Runic Golem",
                        "Stone Guardian",
                        "Crystal Guardian",
                        "Possessed Armor",
                        "Hollow Knight",
                        "Spirit Knight",
                        "Stonefist Warrior",
                        "Ironclad Marauder"));

        // SHADOWED_FORESTS - Forest/nature and shadow/dark themed
        ENEMIES_BY_BIOME.put(
                BiomeType.SHADOWED_FORESTS,
                Arrays.asList(
                        "Forest Troll",
                        "Corrupted Dryad",
                        "Wild Wolf",
                        "Corrupted Elk",
                        "Blighted Serpent",
                        "Venomous Spider",
                        "Shadow Assassin",
                        "Shade Assassin",
                        "Shadow Stalker",
                        "Dark Mage",
                        "Shadow Mage",
                        "Blight Raven",
                        "Enraged Wendigo",
                        "Mutated Frost Boar",
                        "Goblin Scout",
                        "Bandit Thief",
                        "Marauder",
                        "Orc Berserker"));

        // VOLCANIC_DEPTHS - Fire/magical/void themed
        ENEMIES_BY_BIOME.put(
                BiomeType.VOLCANIC_DEPTHS,
                Arrays.asList(
                        "Fire Elemental",
                        "Demon Imp",
                        "Void Whisperer",
                        "Arcane Wraith",
                        "Necromancer",
                        "Shrieking Banshee",
                        "Corrupted Knight",
                        "Slime Monster"));

        // MYSTICAL_HEIGHTS - Flying/agile/beast themed (mountains/sky)
        ENEMIES_BY_BIOME.put(
                BiomeType.MYSTICAL_HEIGHTS,
                Arrays.asList(
                        "Dire Bat",
                        "Storm Raven",
                        "Wind Dancer",
                        "Swift Reaper",
                        "Dragon Whelp",
                        "Enraged Wendigo",
                        "Frostfang Lynx",
                        "Blade Phantom",
                        "Ice Stalker"));

        // TWISTED_REALMS - Magical/wraith/spirit/phantom themed
        ENEMIES_BY_BIOME.put(
                BiomeType.TWISTED_REALMS,
                Arrays.asList(
                        "Wailing Wisp",
                        "Wraithling",
                        "Coldshade Phantom",
                        "Blade Phantom",
                        "Spirit Knight",
                        "Hollow Knight",
                        "Crystal Enchanter",
                        "Spirit Snake",
                        "Frost Wisp",
                        "Frost Sprite Cluster",
                        "Shadow Stalker",
                        "Arcane Wraith"));
    }

    /**
     * Gets a random enemy name for the specified biome.
     *
     * @param biome the biome type
     * @return a random enemy name from that biome's pool
     */
    public static String getRandomEnemy(BiomeType biome) {
        List<String> enemies = ENEMIES_BY_BIOME.get(biome);
        if (enemies == null || enemies.isEmpty()) {
            // Fallback to first biome if biome not found
            enemies = ENEMIES_BY_BIOME.get(BiomeType.FROZEN_WASTES);
        }
        return enemies.get(random.nextInt(enemies.size()));
    }

    /**
     * Gets all enemies for a biome (for testing/debugging).
     *
     * @param biome the biome type
     * @return list of all enemy names for that biome
     */
    public static List<String> getAllEnemies(BiomeType biome) {
        return new ArrayList<>(ENEMIES_BY_BIOME.getOrDefault(biome, Collections.emptyList()));
    }
}
