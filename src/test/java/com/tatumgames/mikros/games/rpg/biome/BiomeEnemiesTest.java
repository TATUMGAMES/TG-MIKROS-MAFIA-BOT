package com.tatumgames.mikros.games.rpg.biome;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BiomeEnemies. Uses getAllEnemies() to verify data integrity and that
 * getRandomEnemy() returns valid enemies from the correct biome.
 */
class BiomeEnemiesTest {

    @Test
    void everyBiomeHasEnemies() {
        for (BiomeType biome : BiomeType.values()) {
            List<String> enemies = BiomeEnemies.getAllEnemies(biome);
            assertNotNull(enemies, "Enemies list should not be null for " + biome);
            assertFalse(enemies.isEmpty(), "Biome " + biome + " should have at least one enemy");
        }
    }

    @Test
    void getAllEnemiesReturnsDefensiveCopy() {
        BiomeType biome = BiomeType.FROZEN_WASTES;
        List<String> first = BiomeEnemies.getAllEnemies(biome);
        List<String> second = BiomeEnemies.getAllEnemies(biome);
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.size(), second.size(), "Both calls should return same size");
        // Modify the first list; second call should be unaffected (different instance)
        first.clear();
        assertFalse(second.isEmpty(), "Modifying returned list should not affect subsequent call");
    }

    @Test
    void noNullOrBlankEnemyNames() {
        for (BiomeType biome : BiomeType.values()) {
            List<String> enemies = BiomeEnemies.getAllEnemies(biome);
            for (String name : enemies) {
                assertNotNull(name, "Enemy name should not be null in " + biome);
                assertFalse(
                        name.isBlank(), "Enemy name should not be blank in " + biome + ": '" + name + "'");
            }
        }
    }

    @Test
    void getRandomEnemyReturnsEnemyFromBiome() {
        for (BiomeType biome : BiomeType.values()) {
            List<String> validEnemies = BiomeEnemies.getAllEnemies(biome);
            assertFalse(validEnemies.isEmpty(), "Biome " + biome + " must have enemies for this test");
            for (int i = 0; i < 50; i++) {
                String result = BiomeEnemies.getRandomEnemy(biome);
                assertNotNull(result, "getRandomEnemy should not return null");
                assertTrue(
                        validEnemies.contains(result),
                        "getRandomEnemy("
                                + biome
                                + ") returned '"
                                + result
                                + "' which is not in getAllEnemies");
            }
        }
    }

    @Test
    void allBiomeTypesHaveEnemies() {
        for (BiomeType biome : BiomeType.values()) {
            int size = BiomeEnemies.getAllEnemies(biome).size();
            assertTrue(size >= 1, "Biome " + biome + " should have at least one enemy, got " + size);
        }
    }
}
