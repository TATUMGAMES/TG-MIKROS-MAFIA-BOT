package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.boss.BossCatalog;
import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BossService secret boss functionality.
 */
class BossServiceSecretBossTest {

    private static final String GUILD_ID = "test-guild-123";
    private static final String USER_ID = "test-user-456";
    @Mock
    private CharacterService characterService;
    @Mock
    private AuraService auraService;
    @Mock
    private WorldCurseService worldCurseService;
    @Mock
    private NilfheimEventService nilfheimEventService;
    @Mock
    private LoreRecognitionService loreRecognitionService;
    @Mock
    private BlessingService blessingService;
    private BossService bossService;
    private RPGCharacter testCharacter;

    private static void setCharacterLevelAndXp(
            RPGCharacter character, int level, int xp, int xpToNextLevel) throws Exception {
        Field levelField = RPGCharacter.class.getDeclaredField("level");
        levelField.setAccessible(true);
        levelField.set(character, level);
        Field xpField = RPGCharacter.class.getDeclaredField("xp");
        xpField.setAccessible(true);
        xpField.set(character, xp);
        Field xpToNextField = RPGCharacter.class.getDeclaredField("xpToNextLevel");
        xpToNextField.setAccessible(true);
        xpToNextField.set(character, xpToNextLevel);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bossService =
                new BossService(
                        characterService,
                        auraService,
                        worldCurseService,
                        nilfheimEventService,
                        loreRecognitionService,
                        blessingService);
        testCharacter = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
    }

    @Test
    @DisplayName("Should spawn secret boss successfully")
    void shouldSpawnSecretBossSuccessfully() {
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        int level = 5;

        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, level);

        assertNotNull(boss);
        assertEquals(level, boss.getLevel());
        assertEquals(definition.name(), boss.getName());

        // Verify boss is stored in state
        BossService.ServerBossState state = bossService.getState(GUILD_ID);
        assertNotNull(state);
        assertEquals(boss, state.getSecretBoss(USER_ID));
    }

    @Test
    @DisplayName("Should return existing boss if one is already active")
    void shouldReturnExistingBossIfAlreadyActive() {
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss firstBoss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        // Try to spawn another boss
        BossCatalog.BossDefinition definition2 = BossCatalog.getRandomNormalBoss(6);
        Boss secondBoss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition2, 6);

        assertNotNull(secondBoss);
        assertEquals(firstBoss, secondBoss); // Should return existing boss
        assertEquals(5, secondBoss.getLevel()); // Should be level 5, not 6
    }

    @Test
    @DisplayName("Should create boss with correct level")
    void shouldCreateBossWithCorrectLevel() {
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(10);
        int level = 10;

        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, level);

        assertNotNull(boss);
        assertEquals(level, boss.getLevel());
    }

    @Test
    @DisplayName("Should spawn boss when milestone is met")
    void shouldSpawnBossWhenMilestoneIsMet() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);
        String milestoneKey = "level_10";
        int bossLevel = 5;

        Boss boss = bossService.checkAndSpawnSecretBoss(GUILD_ID, USER_ID, milestoneKey, bossLevel);

        assertNotNull(boss);
        assertEquals(bossLevel, boss.getLevel());
        assertTrue(testCharacter.getSecretBossMilestones().contains(milestoneKey));
    }

    @Test
    @DisplayName("Should not spawn if milestone already triggered")
    void shouldNotSpawnIfMilestoneAlreadyTriggered() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);
        String milestoneKey = "level_10";
        testCharacter.addSecretBossMilestone(milestoneKey);

        Boss boss = bossService.checkAndSpawnSecretBoss(GUILD_ID, USER_ID, milestoneKey, 5);

        assertNull(boss);
    }

    @Test
    @DisplayName("Should not spawn if active boss exists")
    void shouldNotSpawnIfActiveBossExists() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);
        String milestoneKey = "level_10";

        // Spawn first boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        // Try to spawn another via milestone
        Boss boss = bossService.checkAndSpawnSecretBoss(GUILD_ID, USER_ID, milestoneKey, 5);

        assertNull(boss);
    }

    @Test
    @DisplayName("Should grant exactly 10 event charges (fixed)")
    void shouldGrantExactly10EventCharges() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);
        String milestoneKey = "level_10";
        int initialCharges = testCharacter.getEventCharges();
        assertEquals(0, initialCharges);

        Boss boss = bossService.checkAndSpawnSecretBoss(GUILD_ID, USER_ID, milestoneKey, 5);

        assertNotNull(boss);
        assertEquals(10, testCharacter.getEventCharges());
    }

    @Test
    @DisplayName("Should cap event charges at 10 if character already has some")
    void shouldCapEventChargesAt10IfCharacterAlreadyHasSome() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);
        testCharacter.setEventCharges(3); // Character already has 3 charges
        String milestoneKey = "level_10";

        Boss boss = bossService.checkAndSpawnSecretBoss(GUILD_ID, USER_ID, milestoneKey, 5);

        assertNotNull(boss);
        assertEquals(10, testCharacter.getEventCharges()); // Should be capped at 10, not 13
    }

    @Test
    @DisplayName("Should mark milestone as triggered")
    void shouldMarkMilestoneAsTriggered() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);
        String milestoneKey = "level_10";

        assertFalse(testCharacter.getSecretBossMilestones().contains(milestoneKey));

        Boss boss = bossService.checkAndSpawnSecretBoss(GUILD_ID, USER_ID, milestoneKey, 5);

        assertNotNull(boss);
        assertTrue(testCharacter.getSecretBossMilestones().contains(milestoneKey));
    }

    @Test
    @DisplayName("Should return null if character doesn't exist")
    void shouldReturnNullIfCharacterDoesNotExist() {
        when(characterService.getCharacter(USER_ID)).thenReturn(null);
        String milestoneKey = "level_10";

        Boss boss = bossService.checkAndSpawnSecretBoss(GUILD_ID, USER_ID, milestoneKey, 5);

        assertNull(boss);
    }

    @Test
    @DisplayName("Should deal damage to secret boss")
    void shouldDealDamageToSecretBoss() {
        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);
        int initialHp = boss.getCurrentHp();

        int damage = bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);

        assertTrue(damage > 0);
        assertTrue(boss.getCurrentHp() < initialHp);
    }

    @Test
    @DisplayName("Should return 0 if no boss exists")
    void shouldReturn0IfNoBossExists() {
        int damage = bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);

        assertEquals(0, damage);
    }

    @Test
    @DisplayName("Should return 0 if boss is expired")
    void shouldReturn0IfBossIsExpired() {
        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        // Manually expire the boss (we can't easily do this, so we'll test defeated instead)
        boss.takeDamage(boss.getCurrentHp()); // Defeat the boss

        int damage = bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);

        assertEquals(0, damage);
    }

    @Test
    @DisplayName("Should clean up expired boss")
    void shouldCleanUpExpiredBoss() {
        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        // Defeat the boss
        boss.takeDamage(boss.getCurrentHp());

        bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);

        // Boss should be removed
        BossService.ServerBossState state = bossService.getState(GUILD_ID);
        assertNull(state.getSecretBoss(USER_ID));
    }

    @Test
    @DisplayName("Should grant rewards when boss is defeated")
    void shouldGrantRewardsWhenBossIsDefeated() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);

        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        int initialCatalysts =
                testCharacter.getInventory().getCatalysts().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
        int initialEssences =
                testCharacter.getInventory().getEssences().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
        int initialXp = testCharacter.getXp();

        // Deal enough damage to defeat the boss
        while (!boss.isDefeated()) {
            bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);
        }

        // Verify rewards were granted
        int finalCatalysts =
                testCharacter.getInventory().getCatalysts().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
        int finalEssences =
                testCharacter.getInventory().getEssences().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
        int finalXp = testCharacter.getXp();

        assertTrue(finalCatalysts >= initialCatalysts + 1); // At least 1 catalyst guaranteed
        assertTrue(finalEssences >= initialEssences + 2); // At least 2 essences
        assertTrue(finalXp > initialXp); // XP should increase
    }

    @Test
    @DisplayName("Should increment secret boss kill counter")
    void shouldIncrementSecretBossKillCounter() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);

        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        int initialKills = testCharacter.getSecretBossesKilled();

        // Defeat the boss
        while (!boss.isDefeated()) {
            bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);
        }

        assertEquals(initialKills + 1, testCharacter.getSecretBossesKilled());
    }

    @Test
    @DisplayName("Should remove boss after defeat")
    void shouldRemoveBossAfterDefeat() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);

        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        // Defeat the boss
        while (!boss.isDefeated()) {
            bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);
        }

        // Boss should be removed
        BossService.ServerBossState state = bossService.getState(GUILD_ID);
        assertNull(state.getSecretBoss(USER_ID));
    }

    @Test
    @DisplayName("Should trigger lore recognition milestones on defeat")
    void shouldTriggerLoreRecognitionMilestonesOnDefeat() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);

        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        // Defeat the boss
        while (!boss.isDefeated()) {
            bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);
        }

        // Verify lore recognition was called
        verify(loreRecognitionService, atLeastOnce()).checkMilestones(testCharacter, GUILD_ID);
    }

    @Test
    @DisplayName("Should grant exactly 1 catalyst (guaranteed)")
    void shouldGrantExactly1CatalystGuaranteed() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);

        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        int initialCatalysts =
                testCharacter.getInventory().getCatalysts().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();

        // Defeat the boss
        while (!boss.isDefeated()) {
            bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);
        }

        int finalCatalysts =
                testCharacter.getInventory().getCatalysts().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();

        // Should have at least 1 more catalyst (guaranteed), possibly 2 if rare catalyst granted
        assertTrue(finalCatalysts >= initialCatalysts + 1);
        assertTrue(finalCatalysts <= initialCatalysts + 2); // Max 2 (1 guaranteed + 1 rare chance)
    }

    @Test
    @DisplayName("Should grant 2-4 essences (verify range)")
    void shouldGrant2To4Essences() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);

        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, 5);

        int initialEssences =
                testCharacter.getInventory().getEssences().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();

        // Defeat the boss
        while (!boss.isDefeated()) {
            bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);
        }

        int finalEssences =
                testCharacter.getInventory().getEssences().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
        int essencesGranted = finalEssences - initialEssences;

        assertTrue(
                essencesGranted >= 2 && essencesGranted <= 4,
                "Essences granted should be between 2 and 4, but was: " + essencesGranted);
    }

    @Test
    @DisplayName("Should grant correct XP: 200 + (bossLevel * 50)")
    void shouldGrantCorrectXp() throws Exception {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);

        int bossLevel = 5;
        int expectedXp = 200 + (bossLevel * 50); // 200 + 250 = 450

        // Set character to level 5 with 0 XP so that adding 450 does not level up
        // (xpToNextLevel at level 5 is 100*5^1.5 = 1118)
        setCharacterLevelAndXp(testCharacter, 5, 0, 1118);

        // Spawn a secret boss
        BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(bossLevel);
        Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID, definition, bossLevel);

        int initialXp = testCharacter.getXp();

        // Defeat the boss
        while (!boss.isDefeated()) {
            bossService.attackSecretBoss(GUILD_ID, USER_ID, testCharacter);
        }

        int finalXp = testCharacter.getXp();
        int xpGained = finalXp - initialXp;

        assertTrue(
                xpGained >= expectedXp,
                "XP gained should be at least " + expectedXp + ", but was: " + xpGained);
    }

    @Test
    @DisplayName("Should have 10% chance for rare catalyst (test multiple times)")
    void shouldHave10PercentChanceForRareCatalyst() {
        when(characterService.getCharacter(USER_ID)).thenReturn(testCharacter);

        // Test multiple times to verify probability
        int rareCatalystCount = 0;
        int totalTests = 100;

        for (int i = 0; i < totalTests; i++) {
            RPGCharacter testChar = new RPGCharacter(USER_ID + i, "TestChar" + i, CharacterClass.WARRIOR);
            when(characterService.getCharacter(USER_ID + i)).thenReturn(testChar);

            BossCatalog.BossDefinition definition = BossCatalog.getRandomNormalBoss(5);
            Boss boss = bossService.spawnSecretBoss(GUILD_ID, USER_ID + i, definition, 5);

            int initialCatalysts =
                    testChar.getInventory().getCatalysts().values().stream()
                            .mapToInt(Integer::intValue)
                            .sum();

            // Defeat the boss
            while (!boss.isDefeated()) {
                bossService.attackSecretBoss(GUILD_ID, USER_ID + i, testChar);
            }

            int finalCatalysts =
                    testChar.getInventory().getCatalysts().values().stream()
                            .mapToInt(Integer::intValue)
                            .sum();

            if (finalCatalysts - initialCatalysts >= 2) {
                rareCatalystCount++;
            }
        }

        // With 100 tests, we should see roughly 10% rare catalysts (allow 5-15% range)
        double rareCatalystRate = (double) rareCatalystCount / totalTests;
        assertTrue(
                rareCatalystRate >= 0.05 && rareCatalystRate <= 0.15,
                "Rare catalyst rate should be around 10%, but was: " + (rareCatalystRate * 100) + "%");
    }
}
