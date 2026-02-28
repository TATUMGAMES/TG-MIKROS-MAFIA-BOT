package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.blessing.Blessing;
import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.model.SuperBoss;
import com.tatumgames.mikros.games.rpg.utils.BossDisplayUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Scheduler for boss spawning. Spawns a new boss every 48 hours (24h livable + 24h cooldown) for all
 * servers with RPG enabled.
 */
public class BossScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BossScheduler.class);
    // Warning check interval: check every 30 minutes
    private static final long WARNING_CHECK_INTERVAL_MINUTES = 30;
    // Warning thresholds in hours (will send warnings at these times)
    private static final List<Long> WARNING_THRESHOLDS_HOURS = List.of(4L, 2L, 1L);
    private static final long WARNING_THRESHOLD_30_MINUTES = 30; // in minutes
    private static final List<String> NORMAL_BOSS_TEMPLATES =
            List.of(
                    """
                    🐲 **A New Boss Has Appeared!** 🐲

                    **%s** (Level %d) - %s

                    HP: **%,d**

                    The shadows spread across Nilfheim… heroes, unite! We need champions to defeat this monster before **24 hours**.

                    Use `/rpg-boss-battle battle` to join the fight!
                    """,
                    """
                    ⚔️ **A Fearsome Enemy Emerges!** ⚔️

                    Behold: **%s**, Level %d — %s.

                    HP: **%,d**

                    Darkness rises once more. Champions, prepare for battle! Defeat this beast before **24 hours** or the realm will suffer.

                    Use `/rpg-boss-battle battle` to strike first!
                    """,
                    """
                    🛡️ **A Wild Boss Appears!** 🛡️

                    Name: **%s**
                    Level: **%d**
                    Type: **%s**

                    HP: **%,d**

                    Gather your strength, heroes. A new challenge awaits! You have **24 hours** to defeat this monster.

                    Join via `/rpg-boss-battle battle`!
                    """);
    private static final List<String> SUPER_BOSS_TEMPLATES =
            List.of(
                    """
                    🔥 **A SUPER BOSS HAS APPEARED!** 🔥

                    **%s** (Level %d) - %s

                    HP: **%,d**

                    Special: %s

                    This is a world-tier threat! All heroes must unite! Defeat it before **24 hours**.

                    Use `/rpg-boss-battle battle` to join the fight!
                    """,
                    """
                    💀 **A WORLD-ENDING FOE DESCENDS!** 💀

                    **%s**, Level %d — %s

                    HP: **%,d**

                    Special Mechanic: %s

                    Only the strongest can stand against this monster! You have **24 hours** to save the realm.

                    Join the defense using `/rpg-boss-battle battle`!
                    """,
                    """
                    🌌 **A COSMIC BEING INVADES REALITY!** 🌌

                    Target: **%s**
                    Threat Level: %d
                    Classification: %s

                    HP: **%,d**

                    Special Ability: %s

                    The universe trembles. Champions, this is your ultimate test! Defeat this foe before **24 hours**.

                    Use `/rpg-boss-battle battle` to engage!
                    """);
    private static final List<String> BOSS_WARNING_TEMPLATES =
            List.of(
                    """
                    ⏰ **Time is almost up, where are the heroes?**

                    **%s** (Level %d) - %s
                    HP: **%,d** / %,d (%.1f%% remaining)

                    Only **%d hour%s %d minute%s** left before the shadows consume Nilfheim!

                    Use `/rpg-boss-battle battle` to join the fight!
                    """,
                    """
                    🚨 **Calling all heroes of Nilfheim, the world needs you!**

                    **%s** (Level %d) - %s
                    HP: **%,d** / %,d (%.1f%% remaining)

                    Time remaining: **%d hour%s %d minute%s**
                    The realm depends on your courage!

                    Join the battle with `/rpg-boss-battle battle`!
                    """,
                    """
                    ⚔️ **The battle rages on, but time grows short!**

                    **%s** (Level %d) - %s
                    Current HP: **%,d** / %,d (%.1f%% remaining)

                    **%d hour%s %d minute%s** remain before darkness falls!

                    Heroes, unite! `/rpg-boss-battle battle`
                    """,
                    """
                    🌑 **The shadows lengthen... will you answer the call?**

                    **%s** (Level %d) - %s
                    HP: **%,d** / %,d (%.1f%% remaining)

                    **%d hour%s %d minute%s** until the beast escapes!

                    Stand with your fellow heroes: `/rpg-boss-battle battle`
                    """,
                    """
                    🔥 **The final hour approaches!**

                    **%s** (Level %d) - %s
                    HP: **%,d** / %,d (%.1f%% remaining)

                    Only **%d hour%s %d minute%s** left!

                    Nilfheim needs you now! `/rpg-boss-battle battle`
                            """);
    private static final List<String> SUPER_BOSS_WARNING_TEMPLATES =
            List.of(
                    """
                    ⏰ **Time is almost up, where are the heroes?**

                    🔥 **%s** (Level %d) - %s 🔥
                    HP: **%,d** / %,d (%.1f%% remaining)
                    Special: %s

                    Only **%d hour%s %d minute%s** left before the world-tier threat escapes!

                    Use `/rpg-boss-battle battle` to join the fight!
                    """,
                    """
                    🚨 **Calling all heroes of Nilfheim, the world needs you!**

                    💀 **%s** (Level %d) - %s 💀
                    HP: **%,d** / %,d (%.1f%% remaining)
                    Special Mechanic: %s

                    Time remaining: **%d hour%s %d minute%s**
                    This is a world-ending threat!

                    Join the defense using `/rpg-boss-battle battle`!
                    """,
                    """
                    ⚔️ **The ultimate battle rages on, but time grows short!**

                    🌌 **%s** (Level %d) - %s 🌌
                    Current HP: **%,d** / %,d (%.1f%% remaining)
                    Special Ability: %s

                    **%d hour%s %d minute%s** remain before reality collapses!

                    Champions, this is your moment! `/rpg-boss-battle battle`
                    """,
                    """
                    🌑 **The cosmic shadows lengthen... will you answer the call?**

                    🔥 **%s** (Level %d) - %s 🔥
                    HP: **%,d** / %,d (%.1f%% remaining)
                    Special: %s

                    **%d hour%s %d minute%s** until the super boss escapes!

                    Stand with your fellow heroes: `/rpg-boss-battle battle`
                    """,
                    """
                    🔥 **The final hour approaches for the world-tier threat!**

                    💀 **%s** (Level %d) - %s 💀
                    HP: **%,d** / %,d (%.1f%% remaining)
                    Special Mechanic: %s

                    Only **%d hour%s %d minute%s** left!

                    The universe needs you now! `/rpg-boss-battle battle`
                            """);
    private final BossService bossService;
    private final CharacterService characterService;
    private final WorldCurseService worldCurseService;
    private final BlessingService blessingService;
    private final ScheduledExecutorService scheduler;
    // Track last warning sent per boss to avoid spam: "guildId_bossId" -> Instant
    private final Map<String, Instant> lastWarningSent;
    // Track announced boss IDs to prevent duplicate announcements: "guildId_bossId" -> Instant
    private final Map<String, Instant> announcedBossIds = new ConcurrentHashMap<>();
    private volatile boolean started = false;
    private JDA jda;

    /**
     * Creates a new BossScheduler.
     *
     * @param bossService       the boss service
     * @param characterService  the character service (to check if RPG is enabled)
     * @param worldCurseService the world curse service (for applying curses on boss expiration)
     * @param blessingService   the blessing service (for granting blessings on consecutive failures)
     */
    public BossScheduler(
            BossService bossService,
            CharacterService characterService,
            WorldCurseService worldCurseService,
            BlessingService blessingService) {
        this.bossService = bossService;
        this.characterService = characterService;
        this.worldCurseService = worldCurseService;
        this.blessingService = blessingService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.lastWarningSent = new ConcurrentHashMap<>();
        logger.info("BossScheduler initialized");
    }

    private static String pickRandom(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Builds the schedule section for boss announcements (spawned at, livable until, next spawn).
     */
    private static String formatBossScheduleSection(Instant spawnTime, Instant expiresAt) {
        Instant nextSpawn = spawnTime.plus(48, ChronoUnit.HOURS);
        return String.format(
                """
                        **Schedule:**
                        • **Spawned at:** %s
                        • **Livable until:** %s (this boss cycle ends)
                        • **Next boss spawns:** %s
                        """,
                BossDisplayUtil.formatBossTimestamp(spawnTime),
                BossDisplayUtil.formatBossTimestamp(expiresAt),
                BossDisplayUtil.formatBossTimestamp(nextSpawn));
    }

    /**
     * Starts the boss scheduler if not already started. Idempotent: safe to call multiple times.
     *
     * @param jda the JDA instance
     */
    public void startIfNeeded(JDA jda) {
        start(jda);
    }

    /**
     * Starts the boss scheduler.
     *
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        synchronized (this) {
            if (started) {
                return;
            }
            started = true;
        }
        this.jda = jda;

        // Spawn check every 24 hours (48h cooldown enforced in BossService)
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        logger.info("Boss scheduler triggered - checking all servers for boss spawns");
                        spawnBossesForAllServers();
                    } catch (Exception e) {
                        logger.error("Error in boss scheduler", e);
                    }
                },
                0,
                24,
                TimeUnit.HOURS);

        // Check for expiration warnings every 30 minutes
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        logger.debug("Boss expiration warning check triggered");
                        checkBossExpirationWarnings();
                    } catch (Exception e) {
                        logger.error("Error in boss expiration warning check", e);
                    }
                },
                0,
                WARNING_CHECK_INTERVAL_MINUTES,
                TimeUnit.MINUTES);

        // Check for recent defeats to announce rewards every 1 minute
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        checkAndAnnounceRecentDefeats();
                    } catch (Exception e) {
                        logger.error("Error checking for recent defeats", e);
                    }
                },
                0,
                1,
                TimeUnit.MINUTES);

        // Periodic cleanup: validate boss state across all guilds every hour
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        if (jda != null) {
                            logger.debug("Boss state cleanup check triggered");
                            for (Guild guild : jda.getGuilds()) {
                                bossService.validateAndCleanupBossState(guild.getId());
                            }
                            // Prune old announcement tracking entries (older than 48 hours)
                            pruneOldAnnouncementTracking();
                        }
                    } catch (Exception e) {
                        logger.error("Error in boss state cleanup", e);
                    }
                },
                30,
                60,
                TimeUnit.MINUTES);

        logger.info(
                "Boss scheduler started (48h spawn cycle, warnings checked every {} minutes)",
                WARNING_CHECK_INTERVAL_MINUTES);
    }

    /**
     * Spawns bosses for all servers with RPG enabled.
     */
    private void spawnBossesForAllServers() {
        if (jda == null) {
            logger.warn("Boss scheduler: JDA instance is null, cannot spawn bosses");
            return;
        }

        int totalGuilds = jda.getGuilds().size();
        logger.debug("Boss scheduler: Checking {} guilds for boss spawns", totalGuilds);

        for (Guild guild : jda.getGuilds()) {
            try {
                String guildId = guild.getId();
                String guildName = guild.getName();

                // Check if RPG is enabled and channel configured
                com.tatumgames.mikros.games.rpg.config.RPGConfig config =
                        characterService.getConfig(guildId);
                if (config == null || !config.isEnabled() || config.getRpgChannelId() == null) {
                    logger.debug(
                            "Boss scheduler: Skipping guild {} (RPG disabled or not configured)", guildName);
                    continue;
                }

                // Check if there's already an active boss
                BossService.ServerBossState state = bossService.getState(guildId);
                if (state != null) {
                    Boss currentBoss = state.getCurrentBoss();
                    SuperBoss currentSuperBoss = state.getCurrentSuperBoss();

                    // Check if current boss expired or was defeated
                    // Note: Expiration is also checked in checkBossExpirationWarnings() which runs every 30
                    // minutes
                    // Periodic check serves as backup for expiration/defeat handling
                    if (currentBoss != null) {
                        if (currentBoss.isExpired() || currentBoss.isDefeated()) {
                            // Check if boss expired without being defeated (apply curse)
                            if (currentBoss.isExpired() && !currentBoss.isDefeated()) {
                                logger.debug(
                                        "Boss {} expired in spawn check for guild {}",
                                        currentBoss.getName(),
                                        guild.getName());
                                applyBossFailureCurse(guild, guildId, false); // false = normal boss
                            }
                            // Boss expired or defeated, spawn new one
                            spawnNewBoss(guild, guildId, state);
                        }
                        continue; // Boss still active
                    }

                    if (currentSuperBoss != null) {
                        if (currentSuperBoss.isExpired() || currentSuperBoss.isDefeated()) {
                            // Check if super boss expired without being defeated (apply curse)
                            // Note: Expiration is also checked in checkBossExpirationWarnings() which runs every
                            // 30 minutes
                            if (currentSuperBoss.isExpired() && !currentSuperBoss.isDefeated()) {
                                logger.debug(
                                        "Super boss {} expired in spawn check for guild {}",
                                        currentSuperBoss.getName(),
                                        guild.getName());
                                applyBossFailureCurse(guild, guildId, true); // true = super boss
                            }
                            // Super boss expired or defeated, spawn new one
                            spawnNewBoss(guild, guildId, state);
                        }
                        continue; // Super boss still active
                    }
                }

                // No active boss, spawn new one
                logger.debug(
                        "Boss scheduler: No active boss found for guild {}, spawning new boss", guildName);
                spawnNewBoss(guild, guildId, bossService.getOrCreateState(guildId));

            } catch (Exception e) {
                logger.error("Error spawning boss for guild {} ({})", guild.getName(), guild.getId(), e);
            }
        }

        logger.info("Boss scheduler: Finished checking all guilds");
    }

    /**
     * Spawns a new boss for a guild using consolidated spawn logic. Clears curses that expire on
     * spawn. Checks and grants blessings based on consecutive failures. Always attempts to announce
     * the boss after spawning.
     */
    private void spawnNewBoss(Guild guild, String guildId, BossService.ServerBossState state) {
        // Clear curses that expire on spawn
        worldCurseService.clearCursesOnSpawn(guildId);

        // Check and grant blessing based on consecutive failures
        int consecutiveFailures = state.getConsecutiveFailures();
        if (consecutiveFailures > 0) {
            blessingService.checkAndGrantBlessing(guildId, consecutiveFailures);
        }

        // Use consolidated spawn method (handles super vs normal boss decision inside lock)
        Object spawned = bossService.spawnAppropriateBoss(guildId);

        if (spawned instanceof SuperBoss superBoss) {
            logger.info(
                    "Boss scheduler: Successfully spawned super boss {} for guild {}, attempting announcement",
                    superBoss.getName(),
                    guild.getName());
            announceSuperBoss(guild, superBoss);
        } else if (spawned instanceof Boss boss) {
            logger.info(
                    "Boss scheduler: Successfully spawned normal boss {} for guild {}, attempting announcement",
                    boss.getName(),
                    guild.getName());
            announceBoss(guild, boss);
        } else {
            logger.warn(
                    "Boss scheduler: No boss spawned for guild {} (spawnAppropriateBoss returned null - may be cooldown)",
                    guild.getName());
        }
    }

    /**
     * Prunes old entries from announcement tracking (older than 48 hours).
     */
    private void pruneOldAnnouncementTracking() {
        Instant cutoff = Instant.now().minus(48, java.time.temporal.ChronoUnit.HOURS);
        announcedBossIds
                .entrySet()
                .removeIf(entry -> entry.getValue() != null && entry.getValue().isBefore(cutoff));
    }

    /**
     * Announces a new normal boss. Skips if already announced for this boss. Always attempts to send
     * and logs result.
     */
    private void announceBoss(Guild guild, Boss boss) {
        String guildId = guild.getId();
        String announcementKey = guildId + "_" + boss.getBossId();
        if (announcedBossIds.putIfAbsent(announcementKey, Instant.now()) != null) {
            logger.debug(
                    "Boss {} already announced for guild {}, skipping duplicate",
                    boss.getName(),
                    guild.getName());
            return;
        }

        // Try to find RPG channel or general channel
        TextChannel channel = findRpgChannel(guild);
        if (channel == null) {
            logger.error(
                    "Boss scheduler: CRITICAL - Could not find channel to announce boss {} for guild {} ({}). "
                            + "Boss was spawned but players will not see the announcement!",
                    boss.getName(),
                    guild.getName(),
                    guild.getId());
            return;
        }

        // Validate bot can send messages
        if (!channel.canTalk()) {
            logger.error(
                    "Boss scheduler: CRITICAL - Bot cannot send messages in channel {} for guild {}. "
                            + "Boss {} was spawned but announcement failed!",
                    channel.getName(),
                    guild.getName(),
                    boss.getName());
            return;
        }

        String announcement;

        // Check for Class Harmony mechanic (Unity Devourer)
        if (boss.hasClassHarmonyMechanic()) {
            announcement =
                    String.format(
                            """
                    ❄️ **The Unity Devourer** has awakened.

                    A fragment of broken harmony stirs in the frozen wastes. It senses when too many move as one—and it grows stronger. Only discordant forces can truly harm it.

                    **%s** (Level %d) - %s
                    HP: **%,d**
                                    
                                    We need heroes to defeat this monster before **24 hours**.
                                    
                    Use `/rpg-boss-battle battle` to join the fight!
                    """,
                            boss.getName(),
                            boss.getLevel(),
                            boss.getType().getDisplayName(),
                            boss.getMaxHp());
        } else {
            String template = pickRandom(NORMAL_BOSS_TEMPLATES);
            announcement =
                    String.format(
                            template,
                            boss.getName(),
                            boss.getLevel(),
                            boss.getType().getDisplayName(),
                            boss.getMaxHp());
        }

        announcement += "\n\n" + formatBossScheduleSection(boss.getSpawnTime(), boss.getExpiresAt());

        // Add blessing announcement if active
        Blessing blessing = blessingService.getActiveBlessing(guildId);
        if (blessing != null) {
            String blessingSection = formatBlessingAnnouncement(blessing);
            announcement += "\n\n" + blessingSection;
        }

        channel
                .sendMessage(announcement)
                .queue(
                        success ->
                                logger.info(
                                        "Boss scheduler: Successfully announced boss {} (Level {}) in channel {} for guild {}",
                                        boss.getName(),
                                        boss.getLevel(),
                                        channel.getName(),
                                        guild.getName()),
                        failure -> {
                            announcedBossIds.remove(announcementKey); // Allow retry on next spawn attempt
                            logger.error(
                                    "Boss scheduler: CRITICAL - Failed to send boss announcement for guild {} (boss: {}). "
                                            + "Error: {}",
                                    guild.getName(),
                                    boss.getName(),
                                    failure.getMessage(),
                                    failure);
                            if (failure.getCause() != null) {
                                logger.error("Boss announcement failure cause", failure.getCause());
                            }
                        });
    }

    /**
     * Announces a new super boss. Skips if already announced for this boss. Always attempts to send
     * and logs result.
     */
    private void announceSuperBoss(Guild guild, SuperBoss superBoss) {
        String guildId = guild.getId();
        String announcementKey = guildId + "_superboss_" + superBoss.getBossId();
        if (announcedBossIds.putIfAbsent(announcementKey, Instant.now()) != null) {
            logger.debug(
                    "Super boss {} already announced for guild {}, skipping duplicate",
                    superBoss.getName(),
                    guild.getName());
            return;
        }

        TextChannel channel = findRpgChannel(guild);
        if (channel == null) {
            logger.error(
                    "Boss scheduler: CRITICAL - Could not find channel to announce super boss {} for guild {} ({}). "
                            + "Super boss was spawned but players will not see the announcement!",
                    superBoss.getName(),
                    guild.getName(),
                    guild.getId());
            return;
        }

        // Validate bot can send messages
        if (!channel.canTalk()) {
            logger.error(
                    "Boss scheduler: CRITICAL - Bot cannot send messages in channel {} for guild {}. "
                            + "Super boss {} was spawned but announcement failed!",
                    channel.getName(),
                    guild.getName(),
                    superBoss.getName());
            return;
        }

        String announcement;

        // Check for Class Harmony mechanic (Shattered Balance)
        if (superBoss.hasClassHarmonyMechanic()) {
            announcement =
                    String.format(
                            """
                    🌌 **The Shattered Balance** emerges.

                    A cosmic entity born from Nilfheim's original cataclysm. It feeds on dominance and certainty. Only when power is evenly divided across all paths does its armor fracture.

                    **%s** (Level %d) - %s
                                      HP: **%,d**
                                      Special: %s
                                    
                                      This is a world-tier threat! All heroes must unite! Defeat it before **24 hours**.
                                    
                                      Use `/rpg-boss-battle battle` to join the fight!
                                    """,
                            superBoss.getName(),
                            superBoss.getLevel(),
                            superBoss.getType().getDisplayName(),
                            superBoss.getMaxHp(),
                            superBoss.getSpecialMechanic());
        } else {
            String template = pickRandom(SUPER_BOSS_TEMPLATES);
            announcement =
                    String.format(
                            template,
                            superBoss.getName(),
                            superBoss.getLevel(),
                            superBoss.getType().getDisplayName(),
                            superBoss.getMaxHp(),
                            superBoss.getSpecialMechanic());
        }

        announcement += "\n\n**This is a SUPER BOSS** – significantly harder than normal bosses. It replaces the normal boss for this cycle; only one world boss is active at a time.";
        announcement += "\n\n" + formatBossScheduleSection(superBoss.getSpawnTime(), superBoss.getExpiresAt());

        // Add blessing announcement if active
        Blessing blessing = blessingService.getActiveBlessing(guildId);
        if (blessing != null) {
            String blessingSection = formatBlessingAnnouncement(blessing);
            announcement += "\n\n" + blessingSection;
        }

        channel
                .sendMessage(announcement)
                .queue(
                        success ->
                                logger.info(
                                        "Boss scheduler: Successfully announced super boss {} (Level {}) in channel {} for guild {}",
                                        superBoss.getName(),
                                        superBoss.getLevel(),
                                        channel.getName(),
                                        guild.getName()),
                        failure -> {
                            announcedBossIds.remove(announcementKey); // Allow retry on next spawn attempt
                            logger.error(
                                    "Boss scheduler: CRITICAL - Failed to send super boss announcement for guild {} (boss: {}). "
                                            + "Error: {}",
                                    guild.getName(),
                                    superBoss.getName(),
                                    failure.getMessage(),
                                    failure);
                            if (failure.getCause() != null) {
                                logger.error("Super boss announcement failure cause", failure.getCause());
                            }
                        });
    }

    /**
     * Handles boss expiration: applies curse, announces, and clears boss state. Does NOT spawn a new
     * boss - the scheduler handles spawning on the 48h cycle.
     *
     * @param guild       the guild
     * @param guildId     the guild ID
     * @param isSuperBoss whether it was a super boss
     */
    private void handleBossExpiration(Guild guild, String guildId, boolean isSuperBoss) {
        applyBossFailureCurse(guild, guildId, isSuperBoss);
        bossService.validateAndCleanupBossState(guildId);
    }

    /**
     * Applies a world curse when a boss expires undefeated.
     *
     * @param guild       the guild
     * @param guildId     the guild ID
     * @param isSuperBoss whether it was a super boss
     */
    private void applyBossFailureCurse(Guild guild, String guildId, boolean isSuperBoss) {
        com.tatumgames.mikros.games.rpg.curse.WorldCurse curse;
        String announcementTemplate;
        String bossName = null;

        // Get boss name for tracking
        BossService.ServerBossState state = bossService.getState(guildId);
        if (state != null) {
            if (isSuperBoss && state.getCurrentSuperBoss() != null) {
                bossName = state.getCurrentSuperBoss().getName();
            } else if (!isSuperBoss && state.getCurrentBoss() != null) {
                bossName = state.getCurrentBoss().getName();
            }
        }

        if (isSuperBoss) {
            curse = worldCurseService.getRandomMajorCurse();
            announcementTemplate =
                    """
                            🌑 **The Super Boss endures.**
                            The sky darkens as **%s** descends upon the realm.
                            
                            %s
                            
                            Champions can try to save the world again after **24 hours**.
                            """;
        } else {
            curse = worldCurseService.getRandomMinorCurse();
            announcementTemplate =
                    """
                            ❄️ **The beast is not slain.**
                            Nilfheim shudders beneath the **%s**.
                            
                            %s
                            
                            Heroes can try to save the world again after **24 hours**.
                            """;
        }

        // Apply the curse (pass characterService to adjust HP if needed, and boss name for tracking)
        worldCurseService.applyCurse(guildId, curse, characterService, bossName);

        // Increment consecutive failures for empowerment
        BossService.ServerBossState bossState = bossService.getState(guildId);
        if (bossState != null) {
            bossState.incrementConsecutiveFailures();
            logger.info(
                    "Boss failure for guild {} - consecutive failures: {}",
                    guild.getName(),
                    bossState.getConsecutiveFailures());
        }

        // Announce the curse
        TextChannel channel = findRpgChannel(guild);
        if (channel != null && channel.canTalk()) {
            String announcement =
                    String.format(announcementTemplate, curse.getDisplayName(), curse.getDescription());

            channel
                    .sendMessage(announcement)
                    .queue(
                            success ->
                                    logger.info(
                                            "Boss scheduler: Applied and announced curse {} for guild {}",
                                            curse.getDisplayName(),
                                            guild.getName()),
                            failure ->
                                    logger.error(
                                            "Boss scheduler: Failed to announce curse for guild {}",
                                            guild.getName(),
                                            failure));
        } else {
            logger.warn(
                    "Boss scheduler: Applied curse {} for guild {} but could not announce (no channel)",
                    curse.getDisplayName(),
                    guild.getName());
        }
    }

    /**
     * Finds the RPG channel or falls back to system channel. Validates that the channel exists and
     * bot has permission to send messages.
     *
     * @param guild the guild to find channel for
     * @return the TextChannel to use, or null if no valid channel found
     */
    private TextChannel findRpgChannel(Guild guild) {
        String guildId = guild.getId();
        String guildName = guild.getName();

        // Try to find channel from config
        com.tatumgames.mikros.games.rpg.config.RPGConfig config = characterService.getConfig(guildId);
        if (config != null && config.getRpgChannelId() != null) {
            String channelId = config.getRpgChannelId();
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel != null) {
                // Validate bot can send messages
                if (channel.canTalk()) {
                    logger.debug(
                            "Boss scheduler: Using configured RPG channel {} for guild {}",
                            channel.getName(),
                            guildName);
                    return channel;
                } else {
                    logger.warn(
                            "Boss scheduler: Configured RPG channel {} exists but bot cannot send messages for guild {}",
                            channel.getName(),
                            guildName);
                }
            } else {
                logger.warn(
                        "Boss scheduler: Configured RPG channel ID {} not found for guild {} (channel may have been deleted)",
                        channelId,
                        guildName);
            }
        } else {
            logger.debug(
                    "Boss scheduler: No RPG channel configured for guild {}, trying system channel",
                    guildName);
        }

        // Fall back to system channel
        TextChannel systemChannel = guild.getSystemChannel();
        if (systemChannel != null) {
            if (systemChannel.canTalk()) {
                logger.debug(
                        "Boss scheduler: Using system channel {} for guild {}",
                        systemChannel.getName(),
                        guildName);
                return systemChannel;
            } else {
                logger.warn(
                        "Boss scheduler: System channel exists but bot cannot send messages for guild {}",
                        guildName);
            }
        } else {
            logger.warn("Boss scheduler: No system channel found for guild {}", guildName);
        }

        // No valid channel found
        logger.error(
                "Boss scheduler: No valid channel found for guild {} - boss will not be announced",
                guildName);
        return null;
    }

    /**
     * Checks all active bosses and sends expiration warnings if needed.
     */
    private void checkBossExpirationWarnings() {
        if (jda == null) {
            logger.warn("Boss expiration warning: JDA instance is null");
            return;
        }

        for (Guild guild : jda.getGuilds()) {
            try {
                String guildId = guild.getId();

                // Check if RPG is enabled and channel configured
                com.tatumgames.mikros.games.rpg.config.RPGConfig config =
                        characterService.getConfig(guildId);
                if (config == null || !config.isEnabled() || config.getRpgChannelId() == null) {
                    continue;
                }

                BossService.ServerBossState state = bossService.getState(guildId);
                if (state == null) {
                    continue;
                }

                Boss currentBoss = state.getCurrentBoss();
                SuperBoss currentSuperBoss = state.getCurrentSuperBoss();

                // Check normal boss - both warnings AND expiration (no spawn - scheduler handles 48h cycle)
                if (currentBoss != null && !currentBoss.isDefeated()) {
                    if (currentBoss.isExpired()) {
                        logger.info(
                                "Boss {} expired in guild {}, applying curse",
                                currentBoss.getName(),
                                guild.getName());
                        handleBossExpiration(guild, guildId, false);
                    } else {
                        checkAndSendBossWarning(guild, guildId, currentBoss);
                    }
                }

                // Check super boss - both warnings AND expiration (no spawn - scheduler handles 48h cycle)
                if (currentSuperBoss != null && !currentSuperBoss.isDefeated()) {
                    if (currentSuperBoss.isExpired()) {
                        logger.info(
                                "Super boss {} expired in guild {}, applying curse",
                                currentSuperBoss.getName(),
                                guild.getName());
                        handleBossExpiration(guild, guildId, true);
                    } else {
                        checkAndSendSuperBossWarning(guild, guildId, currentSuperBoss);
                    }
                }

            } catch (Exception e) {
                logger.error("Error checking boss expiration warning for guild {}", guild.getName(), e);
            }
        }
    }

    /**
     * Checks if a normal boss needs a warning and sends it. Sends warnings at 4h, 2h, 1h, and 30m
     * remaining.
     */
    private void checkAndSendBossWarning(Guild guild, String guildId, Boss boss) {
        Instant now = Instant.now();
        Instant expiresAt = boss.getExpiresAt();

        long secondsRemaining = java.time.Duration.between(now, expiresAt).getSeconds();
        if (secondsRemaining <= 0) {
            return; // Already expired, will be handled by expiration check
        }

        long hoursRemaining = secondsRemaining / 3600;
        long minutesRemaining = (secondsRemaining % 3600) / 60;
        long totalMinutesRemaining = secondsRemaining / 60;

        String warningKey = guildId + "_boss_" + boss.getBossId();
        Instant lastWarning = lastWarningSent.get(warningKey);

        // Check for 30-minute warning
        if (totalMinutesRemaining <= WARNING_THRESHOLD_30_MINUTES && totalMinutesRemaining > 15) {
            // Check if we haven't sent a 30m warning yet (check last warning was more than 15 minutes ago
            // or null)
            if (lastWarning == null || java.time.Duration.between(lastWarning, now).toMinutes() >= 15) {
                sendBossExpirationWarning(guild, boss, 0, (int) minutesRemaining);
                lastWarningSent.put(warningKey, now);
                return;
            }
        }

        // Check for hour-based warnings (4h, 2h, 1h)
        for (long thresholdHours : WARNING_THRESHOLDS_HOURS) {
            // Check if we're within 30 minutes of this threshold (to account for check interval)
            long thresholdMinutes = thresholdHours * 60;
            if (totalMinutesRemaining <= thresholdMinutes + 30
                    && totalMinutesRemaining >= thresholdMinutes - 30) {
                // Check if we haven't sent a warning for this threshold yet
                // Use threshold-specific key to allow multiple warnings
                String thresholdKey = warningKey + "_" + thresholdHours + "h";
                Instant lastThresholdWarning = lastWarningSent.get(thresholdKey);

                if (lastThresholdWarning == null
                        || java.time.Duration.between(lastThresholdWarning, now).toHours() >= 1) {
                    sendBossExpirationWarning(guild, boss, hoursRemaining, (int) minutesRemaining);
                    lastWarningSent.put(thresholdKey, now);
                    lastWarningSent.put(warningKey, now); // Also update main key
                    return;
                }
            }
        }
    }

    /**
     * Checks if a super boss needs a warning and sends it. Sends warnings at 4h, 2h, 1h, and 30m
     * remaining.
     */
    private void checkAndSendSuperBossWarning(Guild guild, String guildId, SuperBoss superBoss) {
        Instant now = Instant.now();
        Instant expiresAt = superBoss.getExpiresAt();

        long secondsRemaining = java.time.Duration.between(now, expiresAt).getSeconds();
        if (secondsRemaining <= 0) {
            return; // Already expired, will be handled by expiration check
        }

        long hoursRemaining = secondsRemaining / 3600;
        long minutesRemaining = (secondsRemaining % 3600) / 60;
        long totalMinutesRemaining = secondsRemaining / 60;

        String warningKey = guildId + "_superboss_" + superBoss.getBossId();
        Instant lastWarning = lastWarningSent.get(warningKey);

        // Check for 30-minute warning
        if (totalMinutesRemaining <= WARNING_THRESHOLD_30_MINUTES && totalMinutesRemaining > 15) {
            // Check if we haven't sent a 30m warning yet (check last warning was more than 15 minutes ago
            // or null)
            if (lastWarning == null || java.time.Duration.between(lastWarning, now).toMinutes() >= 15) {
                sendSuperBossExpirationWarning(guild, superBoss, 0, (int) minutesRemaining);
                lastWarningSent.put(warningKey, now);
                return;
            }
        }

        // Check for hour-based warnings (4h, 2h, 1h)
        for (long thresholdHours : WARNING_THRESHOLDS_HOURS) {
            // Check if we're within 30 minutes of this threshold (to account for check interval)
            long thresholdMinutes = thresholdHours * 60;
            if (totalMinutesRemaining <= thresholdMinutes + 30
                    && totalMinutesRemaining >= thresholdMinutes - 30) {
                // Check if we haven't sent a warning for this threshold yet
                // Use threshold-specific key to allow multiple warnings
                String thresholdKey = warningKey + "_" + thresholdHours + "h";
                Instant lastThresholdWarning = lastWarningSent.get(thresholdKey);

                if (lastThresholdWarning == null
                        || java.time.Duration.between(lastThresholdWarning, now).toHours() >= 1) {
                    sendSuperBossExpirationWarning(guild, superBoss, hoursRemaining, (int) minutesRemaining);
                    lastWarningSent.put(thresholdKey, now);
                    lastWarningSent.put(warningKey, now); // Also update main key
                    return;
                }
            }
        }
    }

    /**
     * Sends an expiration warning for a normal boss.
     */
    private void sendBossExpirationWarning(
            Guild guild, Boss boss, long hoursRemaining, long minutesRemaining) {
        TextChannel channel = findRpgChannel(guild);
        if (channel == null || !channel.canTalk()) {
            logger.warn(
                    "Boss expiration warning: Could not send warning for boss {} in guild {} (no channel)",
                    boss.getName(),
                    guild.getName());
            return;
        }

        String template = pickRandom(BOSS_WARNING_TEMPLATES);
        double hpPercent = (double) boss.getCurrentHp() / boss.getMaxHp() * 100.0;
        String hoursText = hoursRemaining != 1 ? "s" : "";
        String minutesText = minutesRemaining != 1 ? "s" : "";

        String warning =
                String.format(
                        template,
                        boss.getName(),
                        boss.getLevel(),
                        boss.getType().getDisplayName(),
                        boss.getCurrentHp(),
                        boss.getMaxHp(),
                        hpPercent,
                        hoursRemaining,
                        hoursText,
                        minutesRemaining,
                        minutesText);

        channel
                .sendMessage(warning)
                .queue(
                        success ->
                                logger.info(
                                        "Boss expiration warning sent for {} (Level {}) in guild {} - {}h {}m remaining",
                                        boss.getName(),
                                        boss.getLevel(),
                                        guild.getName(),
                                        hoursRemaining,
                                        minutesRemaining),
                        failure ->
                                logger.error(
                                        "Failed to send boss expiration warning for guild {}",
                                        guild.getName(),
                                        failure));
    }

    /**
     * Sends an expiration warning for a super boss.
     */
    private void sendSuperBossExpirationWarning(
            Guild guild, SuperBoss superBoss, long hoursRemaining, long minutesRemaining) {
        TextChannel channel = findRpgChannel(guild);
        if (channel == null || !channel.canTalk()) {
            logger.warn(
                    "Boss expiration warning: Could not send warning for super boss {} in guild {} (no channel)",
                    superBoss.getName(),
                    guild.getName());
            return;
        }

        String template = pickRandom(SUPER_BOSS_WARNING_TEMPLATES);
        double hpPercent = (double) superBoss.getCurrentHp() / superBoss.getMaxHp() * 100.0;
        String hoursText = hoursRemaining != 1 ? "s" : "";
        String minutesText = minutesRemaining != 1 ? "s" : "";

        String warning =
                String.format(
                        template,
                        superBoss.getName(),
                        superBoss.getLevel(),
                        superBoss.getType().getDisplayName(),
                        superBoss.getCurrentHp(),
                        superBoss.getMaxHp(),
                        hpPercent,
                        superBoss.getSpecialMechanic(),
                        hoursRemaining,
                        hoursText,
                        minutesRemaining,
                        minutesText);

        channel
                .sendMessage(warning)
                .queue(
                        success ->
                                logger.info(
                                        "Super boss expiration warning sent for {} (Level {}) in guild {} - {}h {}m remaining",
                                        superBoss.getName(),
                                        superBoss.getLevel(),
                                        guild.getName(),
                                        hoursRemaining,
                                        minutesRemaining),
                        failure ->
                                logger.error(
                                        "Failed to send super boss expiration warning for guild {}",
                                        guild.getName(),
                                        failure));
    }

    /**
     * Checks for recent boss defeats and announces rewards to participants.
     */
    private void checkAndAnnounceRecentDefeats() {
        if (jda == null) {
            return;
        }

        for (Guild guild : jda.getGuilds()) {
            try {
                String guildId = guild.getId();
                BossService.DefeatInfo defeatInfo = bossService.getAndClearRecentDefeat(guildId);

                if (defeatInfo != null) {
                    announceBossDefeatRewards(guild, defeatInfo);
                }
            } catch (Exception e) {
                logger.error("Error checking for recent defeats in guild {}", guild.getName(), e);
            }
        }
    }

    /**
     * Announces boss defeat rewards to all participants.
     *
     * @param guild      the guild
     * @param defeatInfo the defeat information
     */
    private void announceBossDefeatRewards(Guild guild, BossService.DefeatInfo defeatInfo) {
        TextChannel channel = findRpgChannel(guild);
        if (channel == null || !channel.canTalk()) {
            logger.warn(
                    "Boss defeat rewards: Could not announce rewards for guild {} (no channel)",
                    guild.getName());
            return;
        }

        String bossName = defeatInfo.getBossName();
        boolean isNormalBoss = defeatInfo.isNormalBoss();
        String lastHitterName = defeatInfo.getLastHitterName();
        Map<String, Integer> xpRewards = defeatInfo.getXpRewards();

        // Build announcement
        StringBuilder announcement = new StringBuilder();
        announcement.append(
                String.format(
                        """
                                🎉 **Victory! %s has been defeated!** 🎉
                                
                                All heroes who participated in the battle have received rewards!
                                
                                """,
                        bossName));

        // List participants who received XP (top 30%)
        if (!xpRewards.isEmpty()) {
            announcement.append("**✨ XP Rewards (Top Performers):**\n");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : xpRewards.entrySet()) {
                String userId = entry.getKey();
                int xp = entry.getValue();
                RPGCharacter character = characterService.getCharacter(userId);
                String playerName = character != null ? character.getName() : "Unknown";
                String medal = rank <= 3 ? (rank == 1 ? "🥇" : rank == 2 ? "🥈" : "🥉") : "";
                announcement.append(String.format("%s **%s**: +%,d XP\n", medal, playerName, xp));
                rank++;
            }
            announcement.append("\n");
        }

        // Special reward for last hitter
        announcement.append(
                String.format(
                        """
                                **⚔️ Final Blow:** **%s** dealt the finishing strike!
                                
                                """,
                        lastHitterName));

        // Last hitter gets special bonus - full action restore
        String lastHitterId = defeatInfo.getLastHitterId();
        RPGCharacter lastHitter = characterService.getCharacter(lastHitterId);
        if (lastHitter != null) {
            // Restore all action charges
            int maxCharges = lastHitter.getMaxActionCharges();
            lastHitter.setActionCharges(maxCharges);
            lastHitter.setLastChargeRefreshTime(Instant.now());

            announcement.append(
                    String.format(
                            """
                                    **%s** feels charged up! All daily actions have been fully restored! ⚡
                                    
                                    """,
                            lastHitterName));
        }

        // Item rewards info
        if (isNormalBoss) {
            announcement.append(
                    """
                    **📦 Item Rewards:**
                    • All participants: 1 Essence
                            • 25% chance: 1 Catalyst
                            """);
        } else {
            announcement.append(
                    """
                            **📦 Item Rewards:**
                            • All participants: 1 Catalyst
                            • All participants: 1-3 Essences
                            """);
        }

        channel
                .sendMessage(announcement.toString())
                .queue(
                        success ->
                                logger.info(
                                        "Boss defeat rewards announced for {} in guild {}", bossName, guild.getName()),
                        failure ->
                                logger.error(
                                        "Failed to announce boss defeat rewards for guild {}",
                                        guild.getName(),
                                        failure));
    }

    /**
     * Formats a blessing announcement section for boss announcements. Only displays stat multipliers
     * that are actually used in boss damage calculation.
     *
     * @param blessing the active blessing
     * @return formatted blessing announcement text
     */
    private String formatBlessingAnnouncement(Blessing blessing) {
        StringBuilder sb = new StringBuilder();
        sb.append("✨ **BLESSINGS FROM THE ANCIENTS** ✨\n");
        sb.append(blessing.getNarrative()).append("\n\n");
        sb.append("All heroes are imbued with divine strength:\n");

        // Add stat bonuses (only relevant effects for boss battles)
        if (blessing.getStrMultiplier() > 1.0) {
            double percent = (blessing.getStrMultiplier() - 1.0) * 100;
            sb.append(String.format("- STR +%.0f%%\n", percent));
        }
        if (blessing.getAgiMultiplier() > 1.0) {
            double percent = (blessing.getAgiMultiplier() - 1.0) * 100;
            sb.append(String.format("- AGI +%.0f%%\n", percent));
        }
        if (blessing.getIntMultiplier() > 1.0) {
            double percent = (blessing.getIntMultiplier() - 1.0) * 100;
            sb.append(String.format("- INT +%.0f%%\n", percent));
        }

        // Add timestamp
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").withZone(ZoneId.systemDefault());
        String timestamp = formatter.format(blessing.getGrantedAt());
        sb.append("\nActive since: ").append(timestamp);

        sb.append("\n\n*Use this power wisely; it will vanish when the boss falls!*");

        return sb.toString();
    }

    /**
     * Stops the scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Boss scheduler stopped");
  }
}
