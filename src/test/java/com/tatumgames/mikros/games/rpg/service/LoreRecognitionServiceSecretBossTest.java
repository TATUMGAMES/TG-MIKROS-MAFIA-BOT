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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoreRecognitionService secret boss milestone checking.
 */
class LoreRecognitionServiceSecretBossTest {

    private static final String GUILD_ID = "test-guild-123";
    private static final String USER_ID = "test-user-456";
    @Mock
    private BossService bossService;
    @Mock
    private CharacterService characterService;
    private LoreRecognitionService loreRecognitionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loreRecognitionService = new LoreRecognitionService();
        loreRecognitionService.setBossService(bossService);
        loreRecognitionService.setCharacterService(characterService);
    }

    @Test
    @DisplayName("Should trigger secret boss at level 10 milestone")
    void shouldTriggerSecretBossAtLevel10Milestone() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Set level to 10
        for (int i = 1; i < 10; i++) {
            character.addXp(character.getXpToNextLevel(), null);
        }
        assertEquals(10, character.getLevel());

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(5), 5);
        when(bossService.checkAndSpawnSecretBoss(eq(GUILD_ID), eq(USER_ID), eq("level_10"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "level_10", 5);
    }

    @Test
    @DisplayName("Should trigger secret boss at level 20 milestone")
    void shouldTriggerSecretBossAtLevel20Milestone() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Set level to 20
        for (int i = 1; i < 20; i++) {
            character.addXp(character.getXpToNextLevel(), null);
        }
        assertEquals(20, character.getLevel());

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(10), 10);
        when(bossService.checkAndSpawnSecretBoss(eq(GUILD_ID), eq(USER_ID), eq("level_20"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "level_20", 10);
    }

    @Test
    @DisplayName("Should trigger secret boss at level 30 milestone")
    void shouldTriggerSecretBossAtLevel30Milestone() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Set level to 30
        for (int i = 1; i < 30; i++) {
            character.addXp(character.getXpToNextLevel(), null);
        }
        assertEquals(30, character.getLevel());

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(15), 15);
        when(bossService.checkAndSpawnSecretBoss(eq(GUILD_ID), eq(USER_ID), eq("level_30"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "level_30", 15);
    }

    @Test
    @DisplayName("Should trigger secret boss at boss kill milestone (10 kills)")
    void shouldTriggerSecretBossAtBossKillMilestone10() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Set boss kills to 10
        for (int i = 0; i < 10; i++) {
            character.incrementBossesKilled();
        }
        assertEquals(10, character.getBossesKilled());

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(2), 2);
        when(bossService.checkAndSpawnSecretBoss(
                eq(GUILD_ID), eq(USER_ID), eq("boss_kills_10"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "boss_kills_10", 2);
    }

    @Test
    @DisplayName("Should trigger secret boss at boss kill milestone (20 kills)")
    void shouldTriggerSecretBossAtBossKillMilestone20() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Set boss kills to 20
        for (int i = 0; i < 20; i++) {
            character.incrementBossesKilled();
        }
        assertEquals(20, character.getBossesKilled());

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(3), 3);
        when(bossService.checkAndSpawnSecretBoss(
                eq(GUILD_ID), eq(USER_ID), eq("boss_kills_20"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "boss_kills_20", 3);
    }

    @Test
    @DisplayName("Should trigger secret boss for story flag 'Frostborne's chosen'")
    void shouldTriggerSecretBossForStoryFlagFrostborne() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        character.addStoryFlag("Frostborne's chosen");

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(1), 1);
        when(bossService.checkAndSpawnSecretBoss(
                eq(GUILD_ID), eq(USER_ID), eq("story_flag_frostborne"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "story_flag_frostborne", 1);
    }

    @Test
    @DisplayName("Should trigger secret boss for story flag 'Echo of the Shattering'")
    void shouldTriggerSecretBossForStoryFlagShattering() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        character.addStoryFlag("Echo of the Shattering");

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(1), 1);
        when(bossService.checkAndSpawnSecretBoss(
                eq(GUILD_ID), eq(USER_ID), eq("story_flag_shattering"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "story_flag_shattering", 1);
    }

    @Test
    @DisplayName("Should trigger secret boss for story flag 'Elemental master'")
    void shouldTriggerSecretBossForStoryFlagElemental() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        character.addStoryFlag("Elemental master");

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(1), 1);
        when(bossService.checkAndSpawnSecretBoss(
                eq(GUILD_ID), eq(USER_ID), eq("story_flag_elemental"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "story_flag_elemental", 1);
    }

    @Test
    @DisplayName("Should trigger secret boss for first boss kill")
    void shouldTriggerSecretBossForFirstBossKill() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        character.incrementBossesKilled();
        assertEquals(1, character.getBossesKilled());
        assertEquals(0, character.getSuperBossesKilled());

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(1), 1);
        when(bossService.checkAndSpawnSecretBoss(
                eq(GUILD_ID), eq(USER_ID), eq("first_boss_kill"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "first_boss_kill", 1);
    }

    @Test
    @DisplayName("Should trigger secret boss for first super boss kill")
    void shouldTriggerSecretBossForFirstSuperBossKill() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        character.incrementSuperBossesKilled();
        assertEquals(1, character.getSuperBossesKilled());

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(1), 1);
        when(bossService.checkAndSpawnSecretBoss(
                eq(GUILD_ID), eq(USER_ID), eq("first_super_boss_kill"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "first_super_boss_kill", 1);
    }

    @Test
    @DisplayName("Should trigger secret boss for first death and resurrection")
    void shouldTriggerSecretBossForFirstDeathAndResurrection() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Simulate death and resurrection
        character.incrementDeathCount();
        character.incrementResurrectionCount();
        assertEquals(1, character.getTotalResurrections());
        assertEquals(1, character.getTotalDeaths());

        Boss mockBoss = BossCatalog.createBoss(BossCatalog.getRandomNormalBoss(1), 1);
        when(bossService.checkAndSpawnSecretBoss(
                eq(GUILD_ID), eq(USER_ID), eq("first_death_resurrection"), anyInt()))
                .thenReturn(mockBoss);

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService).checkAndSpawnSecretBoss(GUILD_ID, USER_ID, "first_death_resurrection", 1);
    }

    @Test
    @DisplayName("Should not trigger if milestone already processed")
    void shouldNotTriggerIfMilestoneAlreadyProcessed() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Set level to 10
        for (int i = 1; i < 10; i++) {
            character.addXp(character.getXpToNextLevel(), null);
        }
        // Mark milestone as already processed
        character.addSecretBossMilestone("level_10");

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService, never())
                .checkAndSpawnSecretBoss(anyString(), anyString(), eq("level_10"), anyInt());
    }

    @Test
    @DisplayName("Should not trigger if no guildId provided")
    void shouldNotTriggerIfNoGuildIdProvided() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Set level to 10
        for (int i = 1; i < 10; i++) {
            character.addXp(character.getXpToNextLevel(), null);
        }

        loreRecognitionService.checkMilestones(character, null);

        verify(bossService, never())
                .checkAndSpawnSecretBoss(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should not trigger if bossService is null")
    void shouldNotTriggerIfBossServiceIsNull() {
        LoreRecognitionService service = new LoreRecognitionService();
        // Don't set bossService

        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Set level to 10
        for (int i = 1; i < 10; i++) {
            character.addXp(character.getXpToNextLevel(), null);
        }

        // Should not throw exception
        assertDoesNotThrow(() -> service.checkMilestones(character, GUILD_ID));
    }

    @Test
    @DisplayName("Should not trigger for level below 10")
    void shouldNotTriggerForLevelBelow10() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        assertEquals(1, character.getLevel()); // Level 1

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService, never())
                .checkAndSpawnSecretBoss(anyString(), anyString(), startsWith("level_"), anyInt());
    }

    @Test
    @DisplayName("Should not trigger for boss kills below 10")
    void shouldNotTriggerForBossKillsBelow10() {
        RPGCharacter character = new RPGCharacter(USER_ID, "TestCharacter", CharacterClass.WARRIOR);
        // Set boss kills to 9
        for (int i = 0; i < 9; i++) {
            character.incrementBossesKilled();
        }
        assertEquals(9, character.getBossesKilled());

        loreRecognitionService.checkMilestones(character, GUILD_ID);

        verify(bossService, never())
                .checkAndSpawnSecretBoss(anyString(), anyString(), startsWith("boss_kills_"), anyInt());
    }
}
