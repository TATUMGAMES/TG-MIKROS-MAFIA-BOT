package com.tatumgames.mikros.games.rpg.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RPGCharacter secret boss functionality.
 */
class RPGCharacterSecretBossTest {

    private static final String USER_ID = "test-user-123";
    private static final String CHARACTER_NAME = "TestCharacter";
    private RPGCharacter character;

    @BeforeEach
    void setUp() {
        character = new RPGCharacter(USER_ID, CHARACTER_NAME, CharacterClass.WARRIOR);
    }

    @Test
    @DisplayName("Should return 0 initially for secret bosses killed")
    void shouldReturn0InitiallyForSecretBossesKilled() {
        assertEquals(0, character.getSecretBossesKilled());
    }

    @Test
    @DisplayName("Should return correct count after increments")
    void shouldReturnCorrectCountAfterIncrements() {
        assertEquals(0, character.getSecretBossesKilled());

        character.incrementSecretBossesKilled();
        assertEquals(1, character.getSecretBossesKilled());

        character.incrementSecretBossesKilled();
        assertEquals(2, character.getSecretBossesKilled());

        character.incrementSecretBossesKilled();
        assertEquals(3, character.getSecretBossesKilled());
    }

    @Test
    @DisplayName("Should increment counter correctly")
    void shouldIncrementCounterCorrectly() {
        int initial = character.getSecretBossesKilled();

        character.incrementSecretBossesKilled();
        assertEquals(initial + 1, character.getSecretBossesKilled());

        character.incrementSecretBossesKilled();
        assertEquals(initial + 2, character.getSecretBossesKilled());
    }

    @Test
    @DisplayName("Should handle multiple increments")
    void shouldHandleMultipleIncrements() {
        for (int i = 0; i < 10; i++) {
            character.incrementSecretBossesKilled();
        }

        assertEquals(10, character.getSecretBossesKilled());
    }

    @Test
    @DisplayName("Should return empty set initially for secret boss milestones")
    void shouldReturnEmptySetInitiallyForSecretBossMilestones() {
        Set<String> milestones = character.getSecretBossMilestones();
        assertNotNull(milestones);
        assertTrue(milestones.isEmpty());
    }

    @Test
    @DisplayName("Should return defensive copy of milestones")
    void shouldReturnDefensiveCopyOfMilestones() {
        character.addSecretBossMilestone("existing_milestone");
        Set<String> milestones1 = character.getSecretBossMilestones();
        Set<String> milestones2 = character.getSecretBossMilestones();

        // They should be different instances
        assertNotSame(milestones1, milestones2);

        // Both should reflect the character's milestones
        assertTrue(milestones1.contains("existing_milestone"));
        assertTrue(milestones2.contains("existing_milestone"));

        // Modifying one should not affect the other
        milestones1.add("test_milestone");
        assertFalse(milestones2.contains("test_milestone"));

        // Character's internal set unchanged by modifying the copy
        Set<String> milestones3 = character.getSecretBossMilestones();
        assertFalse(milestones3.contains("test_milestone"));
        assertTrue(milestones3.contains("existing_milestone"));
    }

    @Test
    @DisplayName("Should add milestone to set")
    void shouldAddMilestoneToSet() {
        String milestoneKey = "level_10";

        assertFalse(character.getSecretBossMilestones().contains(milestoneKey));

        character.addSecretBossMilestone(milestoneKey);

        assertTrue(character.getSecretBossMilestones().contains(milestoneKey));
    }

    @Test
    @DisplayName("Should allow multiple milestones")
    void shouldAllowMultipleMilestones() {
        character.addSecretBossMilestone("level_10");
        character.addSecretBossMilestone("level_20");
        character.addSecretBossMilestone("boss_kills_10");

        Set<String> milestones = character.getSecretBossMilestones();
        assertEquals(3, milestones.size());
        assertTrue(milestones.contains("level_10"));
        assertTrue(milestones.contains("level_20"));
        assertTrue(milestones.contains("boss_kills_10"));
    }

    @Test
    @DisplayName("Should handle null milestone key")
    void shouldHandleNullMilestoneKey() {
        // Adding null should not throw exception (HashSet allows null)
        assertDoesNotThrow(() -> character.addSecretBossMilestone(null));

        Set<String> milestones = character.getSecretBossMilestones();
        assertTrue(milestones.contains(null));
    }

    @Test
    @DisplayName("canPerformEventAction should return false when dead")
    void canPerformEventActionShouldReturnFalseWhenDead() {
        character.setEventCharges(5);
        character.setIsDead(true);

        assertFalse(character.canPerformEventAction());
    }

    @Test
    @DisplayName("canPerformEventAction should return false when recovering")
    void canPerformEventActionShouldReturnFalseWhenRecovering() {
        character.setEventCharges(5);
        character.setIsRecovering(true);

        assertFalse(character.canPerformEventAction());
    }

    @Test
    @DisplayName("canPerformEventAction should return false when no charges")
    void canPerformEventActionShouldReturnFalseWhenNoCharges() {
        character.setEventCharges(0);
        character.setIsDead(false);
        character.setIsRecovering(false);

        assertFalse(character.canPerformEventAction());
    }

    @Test
    @DisplayName("canPerformEventAction should return true when has charges and not dead")
    void canPerformEventActionShouldReturnTrueWhenHasChargesAndNotDead() {
        character.setEventCharges(5);
        character.setIsDead(false);
        character.setIsRecovering(false);

        assertTrue(character.canPerformEventAction());
    }

    @Test
    @DisplayName("useEventCharge should decrement charges")
    void useEventChargeShouldDecrementCharges() {
        character.setEventCharges(5);

        assertTrue(character.useEventCharge());
        assertEquals(4, character.getEventCharges());

        assertTrue(character.useEventCharge());
        assertEquals(3, character.getEventCharges());
    }

    @Test
    @DisplayName("useEventCharge should not go below 0")
    void useEventChargeShouldNotGoBelow0() {
        character.setEventCharges(1);

        assertTrue(character.useEventCharge());
        assertEquals(0, character.getEventCharges());

        assertFalse(character.useEventCharge());
        assertEquals(0, character.getEventCharges());
    }

    @Test
    @DisplayName("Event charges should be capped at 10")
    void eventChargesShouldBeCappedAt10() {
        character.setEventCharges(10);
        assertEquals(10, character.getEventCharges());

        // Try to set above 10
        character.setEventCharges(15);
        assertEquals(10, character.getEventCharges());

        // Try to set to max
        character.setEventCharges(character.getMaxEventCharges());
        assertEquals(10, character.getEventCharges());
    }

    @Test
    @DisplayName("Event charges should not go below 0")
    void eventChargesShouldNotGoBelow0() {
        character.setEventCharges(0);
        assertEquals(0, character.getEventCharges());

        // Try to set below 0
        character.setEventCharges(-5);
        assertEquals(0, character.getEventCharges());
    }

    @Test
    @DisplayName("getMaxEventCharges should return 10")
    void getMaxEventChargesShouldReturn10() {
        assertEquals(10, character.getMaxEventCharges());
    }

    @Test
    @DisplayName("Should handle event charges at boundary values")
    void shouldHandleEventChargesAtBoundaryValues() {
        // Test at 0
        character.setEventCharges(0);
        assertEquals(0, character.getEventCharges());
        assertFalse(character.canPerformEventAction());

        // Test at 1
        character.setEventCharges(1);
        assertEquals(1, character.getEventCharges());
        assertTrue(character.canPerformEventAction());

        // Test at 10
        character.setEventCharges(10);
        assertEquals(10, character.getEventCharges());
        assertTrue(character.canPerformEventAction());
    }
}
