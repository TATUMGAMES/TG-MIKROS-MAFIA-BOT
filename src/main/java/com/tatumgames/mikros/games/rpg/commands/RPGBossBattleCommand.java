package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.model.SuperBoss;
import com.tatumgames.mikros.games.rpg.service.BossService;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Command handler for /rpg-boss-battle.
 * Allows players to attack community bosses.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGBossBattleCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGBossBattleCommand.class);
    private final CharacterService characterService;
    private final BossService bossService;

    /**
     * Creates a new RPGBossBattleCommand handler.
     *
     * @param characterService the character service
     * @param bossService      the boss service
     */
    public RPGBossBattleCommand(CharacterService characterService, BossService bossService) {
        this.characterService = characterService;
        this.bossService = bossService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-boss-battle", "Attack the current community boss")
                .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "action", "Action: attack, status, or leaderboard", false);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("❌ This command can only be used in a server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String userId = event.getUser().getId();
        String guildId = guild.getId();

        // Get guild config
        RPGConfig config = characterService.getConfig(guildId);
        if (!config.isEnabled()) {
            event.reply("❌ The RPG system is currently disabled in this server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        OptionMapping actionOption = event.getOption("action");
        String action = (actionOption != null)
                ? actionOption.getAsString().toLowerCase()
                : "attack";

        switch (action) {
            case "attack":
                handleAttack(event, userId, guildId);
                break;
            case "status":
                handleStatus(event, guildId);
                break;
            case "leaderboard":
                handleLeaderboard(event, guildId);
                break;
            default:
                event.reply("❌ Invalid action! Use: **attack**, **status**, or **leaderboard**")
                        .setEphemeral(true)
                        .queue();
        }
    }

    private void handleAttack(SlashCommandInteractionEvent event, String userId, String guildId) {
        // Check if user has a character
        RPGCharacter character = characterService.getCharacter(userId);
        if (character == null) {
            event.reply("❌ You don't have a character yet! Use `/rpg-register` to create one.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if character can act
        if (character.isDead() || character.isRecovering()) {
            event.reply("❌ You cannot attack bosses while dead or recovering!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check for active boss
        BossService.ServerBossState state = bossService.getState(guildId);
        if (state == null) {
            event.reply("❌ No active boss! Wait for the next boss spawn (every 24 hours).")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Boss boss = state.getCurrentBoss();
        SuperBoss superBoss = state.getCurrentSuperBoss();

        if (boss == null && superBoss == null) {
            event.reply("❌ No active boss! Wait for the next boss spawn (every 24 hours).")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Attack boss
        int damage = bossService.attackBoss(guildId, character);

        if (damage == 0) {
            event.reply("❌ Failed to attack boss. Please try again.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Build response
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚔️ Boss Attack!");
        embed.setColor(Color.RED);

        String bossName = boss != null ? boss.getName() : superBoss.getName();
        int currentHp = boss != null ? boss.getCurrentHp() : superBoss.getCurrentHp();
        int maxHp = boss != null ? boss.getMaxHp() : superBoss.getMaxHp();
        boolean defeated = boss != null ? boss.isDefeated() : superBoss.isDefeated();

        embed.setDescription(String.format("""
                        **%s** attacks **%s**!
                        
                        💥 **Damage Dealt: %d**
                        """,
                character.getName(),
                bossName,
                damage
        ));

        // HP bar
        double hpPercent = (currentHp * 100.0) / maxHp;
        String hpBar = buildHpBar(hpPercent);

        embed.addField(
                "Boss HP",
                String.format("%s\n**%d / %d** (%.1f%%)",
                        hpBar,
                        currentHp,
                        maxHp,
                        hpPercent),
                false
        );

        if (defeated) {
            embed.setColor(Color.GREEN);
            embed.addField("🎉 Victory!",
                    "The shadows spread across Nilfheim… but this boss has fallen! A heroic roar echoes through the realm as hope flickers brighter.",
                    false);
        } else {
            // Time remaining
            Instant expiresAt = boss != null ? boss.getExpiresAt() : superBoss.getExpiresAt();
            long secondsRemaining = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
            Duration duration = Duration.ofSeconds(Math.max(0, secondsRemaining));
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();

            embed.addField("⏰ Time Remaining",
                    String.format("%dh %dm until boss expires", hours, minutes),
                    false);
        }

        embed.setFooter("Keep attacking to defeat the boss!");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.info("User {} attacked boss in guild {} - Damage: {}", userId, guildId, damage);
    }

    private void handleStatus(SlashCommandInteractionEvent event, String guildId) {
        BossService.ServerBossState state = bossService.getState(guildId);
        if (state == null) {
            event.reply("❌ No boss state found for this server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Boss boss = state.getCurrentBoss();
        SuperBoss superBoss = state.getCurrentSuperBoss();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🐲 Boss Status");
        embed.setColor(Color.ORANGE);

        if (boss == null && superBoss == null) {
            embed.setDescription("No active boss. Next boss spawns in 24 hours.");
        } else {
            String bossName = boss != null ? boss.getName() : superBoss.getName();
            int currentHp = boss != null ? boss.getCurrentHp() : superBoss.getCurrentHp();
            int maxHp = boss != null ? boss.getMaxHp() : superBoss.getMaxHp();
            int level = boss != null ? boss.getLevel() : superBoss.getLevel();
            String type = (boss != null ? boss.getType() : superBoss.getType()).getDisplayName();

            double hpPercent = (currentHp * 100.0) / maxHp;
            String hpBar = buildHpBar(hpPercent);

            embed.setDescription(String.format("**%s** (Level %d) - %s", bossName, level, type));
            embed.addField("HP", String.format("%s\n**%d / %d** (%.1f%%)", hpBar, currentHp, maxHp, hpPercent), false);

            if (superBoss != null) {
                embed.addField("Special Mechanic", superBoss.getSpecialMechanic(), false);
            }
        }

        embed.addField("Progression",
                String.format("Boss Level: **%d**\nSuper Boss Level: **%d**\nNormal Bosses Defeated: **%d**\nSuper Bosses Defeated: **%d**",
                        state.getBossLevel(),
                        state.getSuperBossLevel(),
                        state.getNormalBossesDefeated(),
                        state.getSuperBossesDefeated()),
                false);

        embed.setTimestamp(Instant.now());
        event.replyEmbeds(embed.build()).queue();
    }

    private void handleLeaderboard(SlashCommandInteractionEvent event, String guildId) {
        Map<String, Integer> topDamage = bossService.getTopDamageDealers(guildId, 10);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Boss Battle Leaderboard");
        embed.setColor(Color.YELLOW);

        if (topDamage.isEmpty()) {
            embed.setDescription("No damage dealt yet. Be the first to attack!");
        } else {
            StringBuilder leaderboard = new StringBuilder();
            int rank = 1;
            for (Map.Entry<String, Integer> entry : topDamage.entrySet()) {
                String medal = rank <= 3 ? getMedal(rank - 1) : "   ";
                // Get character name
                RPGCharacter character = characterService.getCharacter(entry.getKey());
                String name = character != null ? character.getName() : "Unknown";
                leaderboard.append(String.format("%s **#%d** - %s: **%,d** damage\n",
                        medal, rank, name, entry.getValue()));
                rank++;
            }
            embed.setDescription(leaderboard.toString());
        }

        embed.setTimestamp(Instant.now());
        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * Builds an HP bar for damage representation.
     *
     * @param percent the percentage (0-100)
     * @return a visual HP bar
     */
    private String buildHpBar(double percent) {
        int barLength = 20;
        int filled = (int) Math.round(percent / 100.0 * barLength);
        filled = Math.max(0, Math.min(barLength, filled));

        String filledPart = "█".repeat(filled);
        String emptyPart = "░".repeat(barLength - filled);

        return filledPart + emptyPart;
    }

    private String getMedal(int rank) {
        return switch (rank) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> "  ";
        };
    }

    @Override
    public String getCommandName() {
        return "rpg-boss-battle";
    }
}
