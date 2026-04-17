package com.tatumgames.mikros.bump.service;

import static org.junit.jupiter.api.Assertions.*;

import com.tatumgames.mikros.bump.model.BumpConfig;
import com.tatumgames.mikros.bump.model.BumpStats;
import java.time.Instant;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for InMemoryBumpService. */
class InMemoryBumpServiceTest {

  private InMemoryBumpService service;
  private static final String GUILD_ID = "123456789";
  private static final String CHANNEL_ID = "111222333";

  @BeforeEach
  void setUp() {
    service = new InMemoryBumpService();
  }

  @Test
  @DisplayName("Should set and get bump channel")
  void shouldSetAndGetBumpChannel() {
    service.setBumpChannel(GUILD_ID, CHANNEL_ID);
    assertEquals(CHANNEL_ID, service.getBumpChannel(GUILD_ID));
  }

  @Test
  @DisplayName("Should return null for getBumpChannel when guild has no config")
  void shouldReturnNullWhenNoConfig() {
    assertNull(service.getBumpChannel(GUILD_ID));
  }

  @Test
  @DisplayName("Should throw when setBumpChannel with null guildId")
  void shouldThrowForSetBumpChannelNullGuildId() {
    assertThrows(IllegalArgumentException.class, () -> service.setBumpChannel(null, CHANNEL_ID));
  }

  @Test
  @DisplayName("Should throw when setBumpChannel with blank guildId")
  void shouldThrowForSetBumpChannelBlankGuildId() {
    assertThrows(IllegalArgumentException.class, () -> service.setBumpChannel("  ", CHANNEL_ID));
  }

  @Test
  @DisplayName("Should return null for getBumpChannel with null guildId")
  void shouldReturnNullForGetBumpChannelNullGuildId() {
    assertNull(service.getBumpChannel(null));
  }

  @Test
  @DisplayName("Should return null for getBumpChannel with blank guildId")
  void shouldReturnNullForGetBumpChannelBlankGuildId() {
    assertNull(service.getBumpChannel(""));
  }

  @Test
  @DisplayName("Should set and get enabled bots")
  void shouldSetAndGetEnabledBots() {
    EnumSet<BumpConfig.BumpBot> bots =
        EnumSet.of(BumpConfig.BumpBot.DISBOARD, BumpConfig.BumpBot.DISURL);
    service.setEnabledBots(GUILD_ID, bots);
    assertEquals(bots, service.getEnabledBots(GUILD_ID));
  }

  @Test
  @DisplayName("Should return empty set for getEnabledBots when no config")
  void shouldReturnEmptySetWhenNoConfig() {
    assertTrue(service.getEnabledBots(GUILD_ID).isEmpty());
  }

  @Test
  @DisplayName("Should throw when setEnabledBots with null guildId")
  void shouldThrowForSetEnabledBotsNullGuildId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.setEnabledBots(null, EnumSet.of(BumpConfig.BumpBot.DISBOARD)));
  }

  @Test
  @DisplayName("Should set and get bump interval")
  void shouldSetAndGetBumpInterval() {
    service.setBumpInterval(GUILD_ID, 4);
    assertEquals(4, service.getBumpInterval(GUILD_ID));
  }

  @Test
  @DisplayName("Should return default interval 8 when no config")
  void shouldReturnDefaultIntervalWhenNoConfig() {
    assertEquals(8, service.getBumpInterval(GUILD_ID));
  }

  @Test
  @DisplayName("Should return default interval 8 for null guildId")
  void shouldReturnDefaultIntervalForNullGuildId() {
    assertEquals(8, service.getBumpInterval(null));
  }

  @Test
  @DisplayName("Should throw when setBumpInterval with invalid hours")
  void shouldThrowForInvalidInterval() {
    assertThrows(IllegalArgumentException.class, () -> service.setBumpInterval(GUILD_ID, 0));
    assertThrows(IllegalArgumentException.class, () -> service.setBumpInterval(GUILD_ID, 25));
  }

  @Test
  @DisplayName("Should record bump time and retrieve last bump time")
  void shouldRecordAndGetLastBumpTime() {
    Instant now = Instant.now();
    service.recordBumpTime(GUILD_ID, BumpConfig.BumpBot.DISBOARD, now);
    assertEquals(now, service.getLastBumpTime(GUILD_ID, BumpConfig.BumpBot.DISBOARD));
  }

  @Test
  @DisplayName("Should return null for getLastBumpTime when never bumped")
  void shouldReturnNullWhenNeverBumped() {
    assertNull(service.getLastBumpTime(GUILD_ID, BumpConfig.BumpBot.DISBOARD));
  }

  @Test
  @DisplayName("Should throw when recordBumpTime with null guildId or bot or time")
  void shouldThrowForRecordBumpTimeNullArgs() {
    Instant now = Instant.now();
    assertThrows(
        IllegalArgumentException.class,
        () -> service.recordBumpTime(null, BumpConfig.BumpBot.DISBOARD, now));
    assertThrows(IllegalArgumentException.class, () -> service.recordBumpTime(GUILD_ID, null, now));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.recordBumpTime(GUILD_ID, BumpConfig.BumpBot.DISBOARD, null));
  }

  @Test
  @DisplayName("Should record successful bump and reflect in stats")
  void shouldRecordSuccessfulBumpAndReflectInStats() {
    Instant now = Instant.now();
    service.recordSuccessfulBump(GUILD_ID, BumpConfig.BumpBot.DISBOARD, "user1", now);

    BumpStats stats = service.getBumpStats(GUILD_ID);
    assertNotNull(stats);
    assertEquals(GUILD_ID, stats.getGuildId());
    assertEquals(1, stats.getTotalBumps());
    assertEquals(1, stats.getBumpsThisMonth());
    assertEquals(1, stats.getBumpsThisWeek());
    assertEquals(1, stats.getBumpsPerBot().getOrDefault(BumpConfig.BumpBot.DISBOARD, 0));
    assertEquals(1, stats.getRecentBumps().size());
    assertEquals(BumpConfig.BumpBot.DISBOARD, stats.getRecentBumps().get(0).getBot());
    assertEquals("user1", stats.getRecentBumps().get(0).getUserId());
  }

  @Test
  @DisplayName("Should throw when recordSuccessfulBump with null guildId or bot or time")
  void shouldThrowForRecordSuccessfulBumpNullArgs() {
    Instant now = Instant.now();
    assertThrows(
        IllegalArgumentException.class,
        () -> service.recordSuccessfulBump(null, BumpConfig.BumpBot.DISBOARD, "u", now));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.recordSuccessfulBump(GUILD_ID, null, "u", now));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.recordSuccessfulBump(GUILD_ID, BumpConfig.BumpBot.DISBOARD, "u", null));
  }

  @Test
  @DisplayName("Should clear guild data and remove config and history")
  void shouldClearGuildData() {
    service.setBumpChannel(GUILD_ID, CHANNEL_ID);
    service.recordSuccessfulBump(GUILD_ID, BumpConfig.BumpBot.DISBOARD, "user1", Instant.now());

    service.clearGuildData(GUILD_ID);

    assertNull(service.getBumpChannel(GUILD_ID));
    BumpStats stats = service.getBumpStats(GUILD_ID);
    assertEquals(0, stats.getTotalBumps());
  }

  @Test
  @DisplayName("Should no-op clearGuildData for null or blank guildId")
  void shouldNoOpClearGuildDataForNullOrBlank() {
    service.setBumpChannel(GUILD_ID, CHANNEL_ID);
    assertDoesNotThrow(() -> service.clearGuildData(null));
    assertDoesNotThrow(() -> service.clearGuildData("  "));
    assertEquals(CHANNEL_ID, service.getBumpChannel(GUILD_ID));
  }

  @Test
  @DisplayName("Should get config and create if absent")
  void shouldGetConfigAndCreateIfAbsent() {
    BumpConfig config = service.getConfig(GUILD_ID);
    assertNotNull(config);
    assertEquals(GUILD_ID, config.getGuildId());
  }

  @Test
  @DisplayName("Should throw when getConfig with null or blank guildId")
  void shouldThrowForGetConfigNullOrBlankGuildId() {
    assertThrows(IllegalArgumentException.class, () -> service.getConfig(null));
    assertThrows(IllegalArgumentException.class, () -> service.getConfig("  "));
  }

  @Test
  @DisplayName("Should return empty stats for guild with no bumps")
  void shouldReturnEmptyStatsForGuildWithNoBumps() {
    BumpStats stats = service.getBumpStats(GUILD_ID);
    assertNotNull(stats);
    assertEquals(GUILD_ID, stats.getGuildId());
    assertEquals(0, stats.getTotalBumps());
    assertTrue(stats.getRecentBumps().isEmpty());
  }

  @Test
  @DisplayName("Should return same stats from getBumpStats overload with time range")
  void shouldReturnStatsFromOverloadWithTimeRange() {
    service.recordSuccessfulBump(GUILD_ID, BumpConfig.BumpBot.DISBOARD, "u1", Instant.now());

    BumpStats noArg = service.getBumpStats(GUILD_ID);
    BumpStats withRange = service.getBumpStats(GUILD_ID, Instant.EPOCH, Instant.now());

    assertNotNull(withRange);
    assertEquals(noArg.getGuildId(), withRange.getGuildId());
    assertEquals(noArg.getTotalBumps(), withRange.getTotalBumps());
  }
}
