package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.SuperBoss;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Scheduler for boss spawning.
 * Spawns a new boss every 24 hours for all servers with RPG enabled.
 */
public class BossScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BossScheduler.class);
    // Warning check interval: check every 30 minutes
    private static final long WARNING_CHECK_INTERVAL_MINUTES = 30;
    // Warning thresholds: warn when 1-2 hours remain
    private static final long WARNING_THRESHOLD_MIN_HOURS = 1;
    private static final long WARNING_THRESHOLD_MAX_HOURS = 2;
    private static final List<String> NORMAL_BOSS_TEMPLATES = List.of(
            """
                    🐲 **A New Boss Has Appeared!** 🐲
                    
                    **%s** (Level %d) - %s
                    
                    HP: **%,d**
                    
                    The shadows spread across Nilfheim… heroes, unite!
                    
                    Use `/rpg-boss-battle battle` to join the fight!
                    """,

            """
                    ⚔️ **A Fearsome Enemy Emerges!** ⚔️
                    
                    Behold: **%s**, Level %d — %s.
                    
                    HP: **%,d**
                    
                    Darkness rises once more. Champions, prepare for battle!
                    
                    Use `/rpg-boss-battle battle` to strike first!
                    """,

            """
                    🛡️ **A Wild Boss Appears!** 🛡️
                    
                    Name: **%s**
                    Level: **%d**
                    Type: **%s**
                    
                    HP: **%,d**
                    
                    Gather your strength, heroes. A new challenge awaits!
                    
                    Join via `/rpg-boss-battle battle`!
                    """
    );
    private static final List<String> SUPER_BOSS_TEMPLATES = List.of(
            """
                    🔥 **A SUPER BOSS HAS APPEARED!** 🔥
                    
                    **%s** (Level %d) - %s
                    
                    HP: **%,d**
                    
                    Special: %s
                    
                    This is a world-tier threat! All heroes must unite!
                    
                    Use `/rpg-boss-battle battle` to join the fight!
                    """,

            """
                    💀 **A WORLD-ENDING FOE DESCENDS!** 💀
                    
                    **%s**, Level %d — %s
                    
                    HP: **%,d**
                    
                    Special Mechanic: %s
                    
                    Only the strongest can stand against this monster!
                    
                    Join the defense using `/rpg-boss-battle battle`!
                    """,

            """
                    🌌 **A COSMIC BEING INVADES REALITY!** 🌌
                    
                    Target: **%s**
                    Threat Level: %d
                    Classification: %s
                    
                    HP: **%,d**
                    
                    Special Ability: %s
                    
                    The universe trembles. Champions, this is your ultimate test!
                    
                    Use `/rpg-boss-battle battle` to engage!
                    """
    );
    private static final List<String> BOSS_WARNING_TEMPLATES = List.of(
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
                    """
    );
    private static final List<String> SUPER_BOSS_WARNING_TEMPLATES = List.of(
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
                    """
    );
    private final BossService bossService;
    private final CharacterService characterService;
    private final WorldCurseService worldCurseService;
    private final ScheduledExecutorService scheduler;
    // Track last warning sent per boss to avoid spam: "guildId_bossId" -> Instant
    private final Map<String, Instant> lastWarningSent;
    private JDA jda;

    /**
     * Creates a new BossScheduler.
     *
     * @param bossService       the boss service
     * @param characterService  the character service (to check if RPG is enabled)
     * @param worldCurseService the world curse service (for applying curses on boss expiration)
     */
    public BossScheduler(BossService bossService, CharacterService characterService, WorldCurseService worldCurseService) {
        this.bossService = bossService;
        this.characterService = characterService;
        this.worldCurseService = worldCurseService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.lastWarningSent = new ConcurrentHashMap<>();
        logger.info("BossScheduler initialized");
    }

    private static String pickRandom(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Starts the boss scheduler.
     *
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        this.jda = jda;

        // Spawn bosses every 24 hours (initial delay: 0, meaning immediate first spawn)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("Boss scheduler triggered - checking all servers for boss spawns");
                spawnBossesForAllServers();
            } catch (Exception e) {
                logger.error("Error in boss scheduler", e);
            }
        }, 0, 24, TimeUnit.HOURS);

        // Check for expiration warnings every 30 minutes
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.debug("Boss expiration warning check triggered");
                checkBossExpirationWarnings();
            } catch (Exception e) {
                logger.error("Error in boss expiration warning check", e);
            }
        }, 0, WARNING_CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);

        logger.info("Boss scheduler started (spawns every 24 hours, warnings checked every {} minutes)",
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

                // Check if RPG is enabled
                com.tatumgames.mikros.games.rpg.config.RPGConfig config = characterService.getConfig(guildId);
                if (config == null || !config.isEnabled()) {
                    logger.debug("Boss scheduler: Skipping guild {} (RPG disabled or not configured)", guildName);
                    continue;
                }

                // Check if there's already an active boss
                BossService.ServerBossState state = bossService.getState(guildId);
                if (state != null) {
                    Boss currentBoss = state.getCurrentBoss();
                    SuperBoss currentSuperBoss = state.getCurrentSuperBoss();

                    // Check if current boss expired or was defeated
                    // Note: Expiration is also checked in checkBossExpirationWarnings() which runs every 30 minutes
                    // This 24-hour check serves as a backup/safety net
                    if (currentBoss != null) {
                        if (currentBoss.isExpired() || currentBoss.isDefeated()) {
                            // Check if boss expired without being defeated (apply curse)
                            if (currentBoss.isExpired() && !currentBoss.isDefeated()) {
                                logger.debug("Boss {} expired in 24h spawn check for guild {}", currentBoss.getName(), guild.getName());
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
                            // Note: Expiration is also checked in checkBossExpirationWarnings() which runs every 30 minutes
                            if (currentSuperBoss.isExpired() && !currentSuperBoss.isDefeated()) {
                                logger.debug("Super boss {} expired in 24h spawn check for guild {}", currentSuperBoss.getName(), guild.getName());
                                applyBossFailureCurse(guild, guildId, true); // true = super boss
                            }
                            // Super boss expired or defeated, spawn new one
                            spawnNewBoss(guild, guildId, state);
                        }
                        continue; // Super boss still active
                    }
                }

                // No active boss, spawn new one
                logger.debug("Boss scheduler: No active boss found for guild {}, spawning new boss", guildName);
                spawnNewBoss(guild, guildId, bossService.getOrCreateState(guildId));

            } catch (Exception e) {
                logger.error("Error spawning boss for guild {} ({})", guild.getName(), guild.getId(), e);
            }
        }

        logger.info("Boss scheduler: Finished checking all guilds");
    }

    /**
     * Spawns a new boss for a guild.
     * Clears curses that expire on spawn.
     */
    private void spawnNewBoss(Guild guild, String guildId, BossService.ServerBossState state) {
        // Clear curses that expire on spawn
        worldCurseService.clearCursesOnSpawn(guildId);
        // Check if super boss should spawn (every 3 normal bosses)
        if (state.getNormalBossesSinceSuper() >= 3) {
            logger.info("Boss scheduler: Spawning super boss for guild {} (normal bosses since super: {})",
                    guild.getName(), state.getNormalBossesSinceSuper());
            SuperBoss superBoss = bossService.spawnSuperBoss(guildId);
            if (superBoss != null) {
                announceSuperBoss(guild, superBoss);
            } else {
                logger.warn("Boss scheduler: Failed to spawn super boss for guild {}", guild.getName());
            }
        } else {
            logger.info("Boss scheduler: Spawning normal boss for guild {} (normal bosses since super: {})",
                    guild.getName(), state.getNormalBossesSinceSuper());
            Boss boss = bossService.spawnNormalBoss(guildId);
            if (boss != null) {
                announceBoss(guild, boss);
            } else {
                logger.warn("Boss scheduler: Failed to spawn normal boss for guild {}", guild.getName());
            }
        }
    }

    /**
     * Announces a new normal boss.
     */
    private void announceBoss(Guild guild, Boss boss) {
        // Try to find RPG channel or general channel
        TextChannel channel = findRpgChannel(guild);
        if (channel == null) {
            logger.warn("Boss scheduler: Could not find channel to announce boss {} for guild {} ({})",
                    boss.getName(), guild.getName(), guild.getId());
            return;
        }

        // Validate bot can send messages
        if (!channel.canTalk()) {
            logger.warn("Boss scheduler: Bot cannot send messages in channel {} for guild {}",
                    channel.getName(), guild.getName());
            return;
        }

        String announcement;
        
        // Check for Class Harmony mechanic (Unity Devourer)
        if (boss.hasClassHarmonyMechanic()) {
            announcement = String.format("""
                    ❄️ **The Unity Devourer** has awakened.
                    
                    A fragment of broken harmony stirs in the frozen wastes. It senses when too many move as one—and it grows stronger. Only discordant forces can truly harm it.
                    
                    **%s** (Level %d) - %s
                    HP: **%,d**
                    
                    Use `/rpg-boss-battle battle` to join the fight!
                    """,
                    boss.getName(),
                    boss.getLevel(),
                    boss.getType().getDisplayName(),
                    boss.getMaxHp()
            );
        } else {
            String template = pickRandom(NORMAL_BOSS_TEMPLATES);
            announcement = String.format(template,
                    boss.getName(),
                    boss.getLevel(),
                    boss.getType().getDisplayName(),
                    boss.getMaxHp()
            );
        }

        channel.sendMessage(announcement).queue(
                success -> logger.info("Boss scheduler: Successfully announced boss {} (Level {}) in channel {} for guild {}",
                        boss.getName(), boss.getLevel(), channel.getName(), guild.getName()),
                failure -> logger.error("Boss scheduler: Failed to send boss announcement for guild {}", guild.getName(), failure)
        );
    }

    /**
     * Announces a new super boss.
     */
    private void announceSuperBoss(Guild guild, SuperBoss superBoss) {
        TextChannel channel = findRpgChannel(guild);
        if (channel == null) {
            logger.warn("Boss scheduler: Could not find channel to announce super boss {} for guild {} ({})",
                    superBoss.getName(), guild.getName(), guild.getId());
            return;
        }

        // Validate bot can send messages
        if (!channel.canTalk()) {
            logger.warn("Boss scheduler: Bot cannot send messages in channel {} for guild {}",
                    channel.getName(), guild.getName());
            return;
        }

        String announcement;
        
        // Check for Class Harmony mechanic (Shattered Balance)
        if (superBoss.hasClassHarmonyMechanic()) {
            announcement = String.format("""
                    🌌 **The Shattered Balance** emerges.
                    
                    A cosmic entity born from Nilfheim's original cataclysm. It feeds on dominance and certainty. Only when power is evenly divided across all paths does its armor fracture.
                    
                    **%s** (Level %d) - %s
                    HP: **%,d**
                    Special: %s
                    
                    This is a world-tier threat! All heroes must unite!
                    
                    Use `/rpg-boss-battle battle` to join the fight!
                    """,
                    superBoss.getName(),
                    superBoss.getLevel(),
                    superBoss.getType().getDisplayName(),
                    superBoss.getMaxHp(),
                    superBoss.getSpecialMechanic()
            );
        } else {
            String template = pickRandom(SUPER_BOSS_TEMPLATES);
            announcement = String.format(template,
                    superBoss.getName(),
                    superBoss.getLevel(),
                    superBoss.getType().getDisplayName(),
                    superBoss.getMaxHp(),
                    superBoss.getSpecialMechanic()
            );
        }

        channel.sendMessage(announcement).queue(
                success -> logger.info("Boss scheduler: Successfully announced super boss {} (Level {}) in channel {} for guild {}",
                        superBoss.getName(), superBoss.getLevel(), channel.getName(), guild.getName()),
                failure -> logger.error("Boss scheduler: Failed to send super boss announcement for guild {}", guild.getName(), failure)
        );
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

        if (isSuperBoss) {
            curse = worldCurseService.getRandomMajorCurse();
            announcementTemplate = """
                    🌑 **The Super Boss endures.**
                    The sky darkens as **%s** descends upon the realm.
                    
                    %s
                    """;
        } else {
            curse = worldCurseService.getRandomMinorCurse();
            announcementTemplate = """
                    ❄️ **The beast is not slain.**
                    Nilfheim shudders beneath the **%s**.
                    
                    %s
                    """;
        }

        // Apply the curse
        worldCurseService.applyCurse(guildId, curse);

        // Announce the curse
        TextChannel channel = findRpgChannel(guild);
        if (channel != null && channel.canTalk()) {
            String announcement = String.format(announcementTemplate,
                    curse.getDisplayName(),
                    curse.getDescription()
            );

            channel.sendMessage(announcement).queue(
                    success -> logger.info("Boss scheduler: Applied and announced curse {} for guild {}",
                            curse.getDisplayName(), guild.getName()),
                    failure -> logger.error("Boss scheduler: Failed to announce curse for guild {}", guild.getName(), failure)
            );
        } else {
            logger.warn("Boss scheduler: Applied curse {} for guild {} but could not announce (no channel)",
                    curse.getDisplayName(), guild.getName());
        }
    }

    /**
     * Finds the RPG channel or falls back to system channel.
     * Validates that the channel exists and bot has permission to send messages.
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
                    logger.debug("Boss scheduler: Using configured RPG channel {} for guild {}", channel.getName(), guildName);
                    return channel;
                } else {
                    logger.warn("Boss scheduler: Configured RPG channel {} exists but bot cannot send messages for guild {}",
                            channel.getName(), guildName);
                }
            } else {
                logger.warn("Boss scheduler: Configured RPG channel ID {} not found for guild {} (channel may have been deleted)",
                        channelId, guildName);
            }
        } else {
            logger.debug("Boss scheduler: No RPG channel configured for guild {}, trying system channel", guildName);
        }

        // Fall back to system channel
        TextChannel systemChannel = guild.getSystemChannel();
        if (systemChannel != null) {
            if (systemChannel.canTalk()) {
                logger.debug("Boss scheduler: Using system channel {} for guild {}", systemChannel.getName(), guildName);
                return systemChannel;
            } else {
                logger.warn("Boss scheduler: System channel exists but bot cannot send messages for guild {}", guildName);
            }
        } else {
            logger.warn("Boss scheduler: No system channel found for guild {}", guildName);
        }

        // No valid channel found
        logger.error("Boss scheduler: No valid channel found for guild {} - boss will not be announced", guildName);
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

                // Check if RPG is enabled
                com.tatumgames.mikros.games.rpg.config.RPGConfig config = characterService.getConfig(guildId);
                if (config == null || !config.isEnabled()) {
                    continue;
                }

                BossService.ServerBossState state = bossService.getState(guildId);
                if (state == null) {
                    continue;
                }

                Boss currentBoss = state.getCurrentBoss();
                SuperBoss currentSuperBoss = state.getCurrentSuperBoss();

                // Check normal boss - both warnings AND expiration
                if (currentBoss != null && !currentBoss.isDefeated()) {
                    if (currentBoss.isExpired()) {
                        // Boss expired! Apply curse and spawn new one
                        logger.info("Boss {} expired in guild {}, applying curse", currentBoss.getName(), guild.getName());
                        applyBossFailureCurse(guild, guildId, false);
                        spawnNewBoss(guild, guildId, state);
                    } else {
                        // Boss still active, check for warnings
                        checkAndSendBossWarning(guild, guildId, currentBoss);
                    }
                }

                // Check super boss - both warnings AND expiration
                if (currentSuperBoss != null && !currentSuperBoss.isDefeated()) {
                    if (currentSuperBoss.isExpired()) {
                        // Super boss expired! Apply curse and spawn new one
                        logger.info("Super boss {} expired in guild {}, applying curse", currentSuperBoss.getName(), guild.getName());
                        applyBossFailureCurse(guild, guildId, true);
                        spawnNewBoss(guild, guildId, state);
                    } else {
                        // Super boss still active, check for warnings
                        checkAndSendSuperBossWarning(guild, guildId, currentSuperBoss);
                    }
                }

            } catch (Exception e) {
                logger.error("Error checking boss expiration warning for guild {}", guild.getName(), e);
            }
        }
    }

    /**
     * Checks if a normal boss needs a warning and sends it.
     */
    private void checkAndSendBossWarning(Guild guild, String guildId, Boss boss) {
        Instant now = Instant.now();
        Instant expiresAt = boss.getExpiresAt();

        long secondsRemaining = java.time.Duration.between(now, expiresAt).getSeconds();
        long hoursRemaining = secondsRemaining / 3600;
        long minutesRemaining = (secondsRemaining % 3600) / 60;

        // Check if within warning threshold (1-2 hours)
        if (hoursRemaining >= WARNING_THRESHOLD_MIN_HOURS && hoursRemaining <= WARNING_THRESHOLD_MAX_HOURS) {
            String warningKey = guildId + "_boss_" + boss.getBossId();
            Instant lastWarning = lastWarningSent.get(warningKey);

            // Only send warning if we haven't sent one in the last hour (to avoid spam)
            if (lastWarning == null || java.time.Duration.between(lastWarning, now).toHours() >= 1) {
                sendBossExpirationWarning(guild, boss, hoursRemaining, minutesRemaining);
                lastWarningSent.put(warningKey, now);
            }
        }
    }

    /**
     * Checks if a super boss needs a warning and sends it.
     */
    private void checkAndSendSuperBossWarning(Guild guild, String guildId, SuperBoss superBoss) {
        Instant now = Instant.now();
        Instant expiresAt = superBoss.getExpiresAt();

        long secondsRemaining = java.time.Duration.between(now, expiresAt).getSeconds();
        long hoursRemaining = secondsRemaining / 3600;
        long minutesRemaining = (secondsRemaining % 3600) / 60;

        // Check if within warning threshold (1-2 hours)
        if (hoursRemaining >= WARNING_THRESHOLD_MIN_HOURS && hoursRemaining <= WARNING_THRESHOLD_MAX_HOURS) {
            String warningKey = guildId + "_superboss_" + superBoss.getBossId();
            Instant lastWarning = lastWarningSent.get(warningKey);

            // Only send warning if we haven't sent one in the last hour (to avoid spam)
            if (lastWarning == null || java.time.Duration.between(lastWarning, now).toHours() >= 1) {
                sendSuperBossExpirationWarning(guild, superBoss, hoursRemaining, minutesRemaining);
                lastWarningSent.put(warningKey, now);
            }
        }
    }

    /**
     * Sends an expiration warning for a normal boss.
     */
    private void sendBossExpirationWarning(Guild guild, Boss boss, long hoursRemaining, long minutesRemaining) {
        TextChannel channel = findRpgChannel(guild);
        if (channel == null || !channel.canTalk()) {
            logger.warn("Boss expiration warning: Could not send warning for boss {} in guild {} (no channel)",
                    boss.getName(), guild.getName());
            return;
        }

        String template = pickRandom(BOSS_WARNING_TEMPLATES);
        double hpPercent = (double) boss.getCurrentHp() / boss.getMaxHp() * 100.0;
        String hoursText = hoursRemaining != 1 ? "s" : "";
        String minutesText = minutesRemaining != 1 ? "s" : "";

        String warning = String.format(template,
                boss.getName(),
                boss.getLevel(),
                boss.getType().getDisplayName(),
                boss.getCurrentHp(),
                boss.getMaxHp(),
                hpPercent,
                hoursRemaining,
                hoursText,
                minutesRemaining,
                minutesText
        );

        channel.sendMessage(warning).queue(
                success -> logger.info("Boss expiration warning sent for {} (Level {}) in guild {} - {}h {}m remaining",
                        boss.getName(), boss.getLevel(), guild.getName(), hoursRemaining, minutesRemaining),
                failure -> logger.error("Failed to send boss expiration warning for guild {}", guild.getName(), failure)
        );
    }

    /**
     * Sends an expiration warning for a super boss.
     */
    private void sendSuperBossExpirationWarning(Guild guild, SuperBoss superBoss, long hoursRemaining, long minutesRemaining) {
        TextChannel channel = findRpgChannel(guild);
        if (channel == null || !channel.canTalk()) {
            logger.warn("Boss expiration warning: Could not send warning for super boss {} in guild {} (no channel)",
                    superBoss.getName(), guild.getName());
            return;
        }

        String template = pickRandom(SUPER_BOSS_WARNING_TEMPLATES);
        double hpPercent = (double) superBoss.getCurrentHp() / superBoss.getMaxHp() * 100.0;
        String hoursText = hoursRemaining != 1 ? "s" : "";
        String minutesText = minutesRemaining != 1 ? "s" : "";

        String warning = String.format(template,
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
                minutesText
        );

        channel.sendMessage(warning).queue(
                success -> logger.info("Super boss expiration warning sent for {} (Level {}) in guild {} - {}h {}m remaining",
                        superBoss.getName(), superBoss.getLevel(), guild.getName(), hoursRemaining, minutesRemaining),
                failure -> logger.error("Failed to send super boss expiration warning for guild {}", guild.getName(), failure)
        );
    }

    /**
     * Stops the scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Boss scheduler stopped");
    }
}
