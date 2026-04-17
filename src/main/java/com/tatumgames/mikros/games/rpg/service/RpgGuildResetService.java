package com.tatumgames.mikros.games.rpg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates per-guild RPG state resets for admin setup, channel changes, and full server wipes.
 */
public class RpgGuildResetService {
  private static final Logger logger = LoggerFactory.getLogger(RpgGuildResetService.class);

  private final CharacterService characterService;
  private final BossService bossService;
  private final WorldCurseService worldCurseService;
  private final BlessingService blessingService;
  private final NilfheimEventService nilfheimEventService;
  private final BossScheduler bossScheduler;
  private final AchievementService achievementService;
  private final AuraService auraService;

  /**
   * Creates the reset orchestrator.
   *
   * @param characterService character and config storage
   * @param bossService boss state
   * @param worldCurseService world curses
   * @param blessingService shrine blessings
   * @param nilfheimEventService Nilfheim events
   * @param bossScheduler boss spawn activity
   * @param achievementService first-to claims
   * @param auraService legendary aura holders
   */
  public RpgGuildResetService(
      CharacterService characterService,
      BossService bossService,
      WorldCurseService worldCurseService,
      BlessingService blessingService,
      NilfheimEventService nilfheimEventService,
      BossScheduler bossScheduler,
      AchievementService achievementService,
      AuraService auraService) {
    this.characterService = characterService;
    this.bossService = bossService;
    this.worldCurseService = worldCurseService;
    this.blessingService = blessingService;
    this.nilfheimEventService = nilfheimEventService;
    this.bossScheduler = bossScheduler;
    this.achievementService = achievementService;
    this.auraService = auraService;
  }

  /**
   * Clears all play progress for a guild but keeps the current {@link
   * com.tatumgames.mikros.games.rpg.config.RPGConfig} entry (call after updating config).
   *
   * @param guildId the guild ID
   * @return number of active characters removed
   */
  public int resetPlayProgress(String guildId) {
    int cleared = characterService.clearGuildCharacters(guildId);
    bossService.resetServerData(guildId);
    clearSharedGuildRpgState(guildId);
    logger.info("RPG play progress reset for guild {} ({} characters cleared)", guildId, cleared);
    return cleared;
  }

  /**
   * Full admin reset: removes guild config and characters, then clears all other RPG state for the
   * guild.
   *
   * @param guildId the guild ID
   */
  public void fullServerReset(String guildId) {
    characterService.resetServerData(guildId);
    bossService.resetServerData(guildId);
    clearSharedGuildRpgState(guildId);
    logger.warn("Full RPG reset completed for guild {}", guildId);
  }

  private void clearSharedGuildRpgState(String guildId) {
    worldCurseService.clearAllCurses(guildId);
    blessingService.clearBlessing(guildId);
    nilfheimEventService.clearGuildData(guildId);
    achievementService.clearGuild(guildId);
    auraService.clearGuild(guildId);
    bossScheduler.resetRpgSpawnActivity(guildId);
  }
}
