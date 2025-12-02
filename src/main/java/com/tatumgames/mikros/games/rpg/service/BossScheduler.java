package com.tatumgames.mikros.games.rpg.service;

import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.SuperBoss;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for boss spawning.
 * Spawns a new boss every 24 hours for all servers with RPG enabled.
 */
public class BossScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BossScheduler.class);

    private final BossService bossService;
    private final CharacterService characterService;
    private final ScheduledExecutorService scheduler;
    private JDA jda;

    /**
     * Creates a new BossScheduler.
     *
     * @param bossService      the boss service
     * @param characterService the character service (to check if RPG is enabled)
     */
    public BossScheduler(BossService bossService, CharacterService characterService) {
        this.bossService = bossService;
        this.characterService = characterService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        logger.info("BossScheduler initialized");
    }

    /**
     * Starts the boss scheduler.
     *
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        this.jda = jda;

        // Spawn bosses every 24 hours
        scheduler.scheduleAtFixedRate(() -> {
            try {
                spawnBossesForAllServers();
            } catch (Exception e) {
                logger.error("Error in boss scheduler", e);
            }
        }, 0, 24, TimeUnit.HOURS);

        logger.info("Boss scheduler started (spawns every 24 hours)");
    }

    /**
     * Spawns bosses for all servers with RPG enabled.
     */
    private void spawnBossesForAllServers() {
        if (jda == null) {
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

                // Check if there's already an active boss
                BossService.ServerBossState state = bossService.getState(guildId);
                if (state != null) {
                    Boss currentBoss = state.getCurrentBoss();
                    SuperBoss currentSuperBoss = state.getCurrentSuperBoss();

                    // Check if current boss expired or was defeated
                    if (currentBoss != null) {
                        if (currentBoss.isExpired() || currentBoss.isDefeated()) {
                            // Boss expired or defeated, spawn new one
                            spawnNewBoss(guild, guildId, state);
                        }
                        continue; // Boss still active
                    }

                    if (currentSuperBoss != null) {
                        if (currentSuperBoss.isExpired() || currentSuperBoss.isDefeated()) {
                            // Super boss expired or defeated, spawn new one
                            spawnNewBoss(guild, guildId, state);
                        }
                        continue; // Super boss still active
                    }
                }

                // No active boss, spawn new one
                spawnNewBoss(guild, guildId, bossService.getOrCreateState(guildId));

            } catch (Exception e) {
                logger.error("Error spawning boss for guild {}", guild.getId(), e);
            }
        }
    }

    /**
     * Spawns a new boss for a guild.
     */
    private void spawnNewBoss(Guild guild, String guildId, BossService.ServerBossState state) {
        // Check if super boss should spawn (every 3 normal bosses)
        if (state.getNormalBossesSinceSuper() >= 3) {
            SuperBoss superBoss = bossService.spawnSuperBoss(guildId);
            if (superBoss != null) {
                announceSuperBoss(guild, superBoss);
            }
        } else {
            Boss boss = bossService.spawnNormalBoss(guildId);
            if (boss != null) {
                announceBoss(guild, boss);
            }
        }
    }

    private static final List<String> NORMAL_BOSS_TEMPLATES = List.of(
            """
                    🐲 **A New Boss Has Appeared!** 🐲
                    
                    **%s** (Level %d) - %s
                    
                    HP: **%,d**
                    
                    The shadows spread across Nilfheim… heroes, unite!
                    
                    Use `/rpg-boss-battle attack` to join the fight!
                    """,

            """
                    ⚔️ **A Fearsome Enemy Emerges!** ⚔️
                    
                    Behold: **%s**, Level %d — %s.
                    
                    HP: **%,d**
                    
                    Darkness rises once more. Champions, prepare for battle!
                    
                    Use `/rpg-boss-battle attack` to strike first!
                    """,

            """
                    🛡️ **A Wild Boss Appears!** 🛡️
                    
                    Name: **%s**
                    Level: **%d**
                    Type: **%s**
                    
                    HP: **%,d**
                    
                    Gather your strength, heroes. A new challenge awaits!
                    
                    Join via `/rpg-boss-battle attack`!
                    """
    );

    /**
     * Announces a new normal boss.
     */
    private void announceBoss(Guild guild, Boss boss) {
        // Try to find RPG channel or general channel
        TextChannel channel = findRpgChannel(guild);
        if (channel == null) {
            logger.warn("Could not find channel to announce boss for guild {}", guild.getId());
            return;
        }

        String template = pickRandom(NORMAL_BOSS_TEMPLATES);

        String announcement = String.format(template,
                boss.getName(),
                boss.getLevel(),
                boss.getType().getDisplayName(),
                boss.getMaxHp()
        );

        channel.sendMessage(announcement).queue();
        logger.info("Announced boss {} for guild {}", boss.getName(), guild.getId());
    }

    private static final List<String> SUPER_BOSS_TEMPLATES = List.of(
            """
                    🔥 **A SUPER BOSS HAS APPEARED!** 🔥
                    
                    **%s** (Level %d) - %s
                    
                    HP: **%,d**
                    
                    Special: %s
                    
                    This is a world-tier threat! All heroes must unite!
                    
                    Use `/rpg-boss-battle attack` to join the fight!
                    """,

            """
                    💀 **A WORLD-ENDING FOE DESCENDS!** 💀
                    
                    **%s**, Level %d — %s
                    
                    HP: **%,d**
                    
                    Special Mechanic: %s
                    
                    Only the strongest can stand against this monster!
                    
                    Join the defense using `/rpg-boss-battle attack`!
                    """,

            """
                    🌌 **A COSMIC BEING INVADES REALITY!** 🌌
                    
                    Target: **%s**
                    Threat Level: %d
                    Classification: %s
                    
                    HP: **%,d**
                    
                    Special Ability: %s
                    
                    The universe trembles. Champions, this is your ultimate test!
                    
                    Use `/rpg-boss-battle attack` to engage!
                    """
    );


    /**
     * Announces a new super boss.
     */
    private void announceSuperBoss(Guild guild, SuperBoss superBoss) {
        TextChannel channel = findRpgChannel(guild);
        if (channel == null) {
            logger.warn("Could not find channel to announce super boss for guild {}", guild.getId());
            return;
        }

        String template = pickRandom(SUPER_BOSS_TEMPLATES);

        String announcement = String.format(template,
                superBoss.getName(),
                superBoss.getLevel(),
                superBoss.getType().getDisplayName(),
                superBoss.getMaxHp(),
                superBoss.getSpecialMechanic()
        );

        channel.sendMessage(announcement).queue();
        logger.info("Announced super boss {} for guild {}", superBoss.getName(), guild.getId());
    }

    private static String pickRandom(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Finds the RPG channel or falls back to system channel.
     */
    private TextChannel findRpgChannel(Guild guild) {
        // Try to find channel from config
        com.tatumgames.mikros.games.rpg.config.RPGConfig config = characterService.getConfig(guild.getId());
        if (config != null && config.getRpgChannelId() != null) {
            TextChannel channel = guild.getTextChannelById(config.getRpgChannelId());
            if (channel != null) {
                return channel;
            }
        }

        // Fall back to system channel
        return guild.getSystemChannel();
    }

    /**
     * Stops the scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Boss scheduler stopped");
    }
}
