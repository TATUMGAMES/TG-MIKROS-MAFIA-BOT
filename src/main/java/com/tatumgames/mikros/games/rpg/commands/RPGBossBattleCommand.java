package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.curse.WorldCurse;
import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.model.SuperBoss;
import com.tatumgames.mikros.games.rpg.service.BossService;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import com.tatumgames.mikros.games.rpg.service.WorldCurseService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Command handler for /rpg-boss-battle.
 * Allows players to battle community bosses.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGBossBattleCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGBossBattleCommand.class);
    private final CharacterService characterService;
    private final BossService bossService;
    private final WorldCurseService worldCurseService;

    /**
     * Creates a new RPGBossBattleCommand handler.
     *
     * @param characterService  the character service
     * @param bossService       the boss service
     * @param worldCurseService the world curse service
     */
    public RPGBossBattleCommand(CharacterService characterService, BossService bossService, WorldCurseService worldCurseService) {
        this.characterService = characterService;
        this.bossService = bossService;
        this.worldCurseService = worldCurseService;
    }

    @Override
    public CommandData getCommandData() {
        net.dv8tion.jda.api.interactions.commands.build.OptionData actionOption =
                new net.dv8tion.jda.api.interactions.commands.build.OptionData(
                        net.dv8tion.jda.api.interactions.commands.OptionType.STRING,
                        "action",
                        "Action to perform",
                        true)
                        .addChoice("Battle", "battle")
                        .addChoice("Status", "status")
                        .addChoice("Leaderboard", "leaderboard")
                        .addChoice("Secret Boss", "secret-boss");

        return Commands.slash("rpg-boss-battle", "Battle the current community boss")
                .addOptions(actionOption);
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

        Member member = event.getMember();
        if (member == null) {
            event.reply("❌ Unable to get member information.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String userId = event.getUser().getId();
        String guildId = guild.getId();

        // Get guild config
        RPGConfig config = characterService.getConfig(guildId);

        // Check role requirement
        if (config != null && !AdminUtils.canUserPlay(member, config.isAllowNoRoleUsers())) {
            event.reply("❌ Users without roles cannot play RPG games in this server. Contact an administrator.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!config.isEnabled()) {
            event.reply("❌ The RPG system is currently disabled in this server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if in correct channel (if specified)
        if (config != null && config.getRpgChannelId() != null) {
            if (!event.getChannel().getId().equals(config.getRpgChannelId())) {
                event.reply(String.format(
                        "Please use `/rpg-boss-battle` in <#%s>. RPG commands are restricted to the assigned channel.",
                        config.getRpgChannelId()
                )).setEphemeral(true).queue();
                return;
            }
        }

        OptionMapping actionOption = event.getOption("action");
        if (actionOption == null) {
            event.reply("❌ You must specify an action.").setEphemeral(true).queue();
            return;
        }
        String action = actionOption.getAsString().toLowerCase();

        switch (action) {
            case "battle":
                handleBattle(event, userId, guildId);
                break;
            case "status":
                handleStatus(event, guildId);
                break;
            case "leaderboard":
                handleLeaderboard(event, guildId);
                break;
            case "secret-boss":
                handleSecretBossBattle(event, userId, guildId);
                break;
            default:
                event.reply("❌ Invalid action! Use: **battle**, **status**, **leaderboard**, or **secret-boss**")
                        .setEphemeral(true)
                        .queue();
        }
    }

    private void handleBattle(SlashCommandInteractionEvent event, String userId, String guildId) {
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
            event.reply("❌ You cannot battle bosses while dead or recovering!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if character has heroic charges
        if (!character.canPerformHeroicAction()) {
            int remaining = character.getHeroicCharges();
            event.reply(String.format("""
                            ⚔️ **No Heroic Charges Available**
                            
                            Heroic charges remaining: **%d/%d**
                            
                            Heroic charges refresh when a new boss spawns. Wait for the next boss to get more battles!
                            """,
                    remaining,
                    character.getMaxHeroicCharges()
            )).setEphemeral(true).queue();
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

        // Battle boss (this consumes a heroic charge)
        int damage = bossService.attackBoss(guildId, character);

        if (damage == 0) {
            event.reply("❌ Failed to battle boss. Please try again.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Consume heroic charge after successful battle
        character.useHeroicCharge();
        int remainingCharges = character.getHeroicCharges();

        // Build response
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚔️ Boss Battle!");
        embed.setColor(Color.RED);

        String bossName = boss != null ? boss.getName() : superBoss.getName();
        int bossLevel = boss != null ? boss.getLevel() : superBoss.getLevel();
        String bossNameWithLevel = String.format("%s (level %d)", bossName, bossLevel);
        int currentHp = boss != null ? boss.getCurrentHp() : superBoss.getCurrentHp();
        int maxHp = boss != null ? boss.getMaxHp() : superBoss.getMaxHp();
        boolean defeated = boss != null ? boss.isDefeated() : superBoss.isDefeated();

        // Use "slain" when defeated, "attacks" when not defeated
        String actionVerb = defeated ? "has slain" : "attacks";

        // Add deity-specific dialogue if character has world flags
        String deityDialogue = "";
        if (character.hasWorldFlag("STONE_WOLF_MARKED")) {
            deityDialogue = "\n\n🐺 *The Stone Wolf's mark glows as you face the beast...*";
        } else if (character.hasWorldFlag("FROSTWIND_MARKED")) {
            deityDialogue = "\n\n🌪️ *Ilyra's winds guide your strikes...*";
        } else if (character.hasWorldFlag("HOLLOW_MIND_MARKED")) {
            deityDialogue = "\n\n🔮 *Nereth's power flows through your mind...*";
        }

        embed.setDescription(String.format("""
                        **%s** %s **%s**!%s
                        
                        💥 **Damage Dealt: %d**
                        """,
                character.getName(),
                actionVerb,
                bossNameWithLevel,
                deityDialogue,
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

            // Enhanced defeat message with lore and deity-specific dialogue
            String deityVictoryText = "";
            if (character.hasWorldFlag("STONE_WOLF_MARKED")) {
                deityVictoryText = " The Stone Wolf's blessing empowered your final strike!";
            } else if (character.hasWorldFlag("FROSTWIND_MARKED")) {
                deityVictoryText = " Ilyra's winds carried your blade true!";
            } else if (character.hasWorldFlag("HOLLOW_MIND_MARKED")) {
                deityVictoryText = " Nereth's wisdom guided your victory!";
            }

            String loreMessage = String.format("""
                            **%s** has etched their name into the annals of Nilfheim's history!
                            
                            The shadows spread across the realm… but this boss has fallen! A heroic roar echoes through the frozen wastes as hope flickers brighter. The people of Nilfheim sing songs of **%s**'s valor, and bards will tell this tale for generations to come.%s
                            
                            🏛️ **Legacy:** Your name is now whispered in the halls of heroes.
                            """,
                    character.getName(),
                    character.getName(),
                    deityVictoryText
            );

            embed.addField("🎉 Victory!", loreMessage, false);

            // Add XP reward info (if this player is in top 30% of participants)
            // Calculate 30% of participants (same logic as BossService)
            // Get all damage dealers to calculate total participants
            Map<String, Integer> allDamage = bossService.getTopDamageDealers(guildId, Integer.MAX_VALUE);
            int totalParticipants = allDamage.size();
            int rewardCount = (int) Math.ceil(totalParticipants * 0.30); // Top 30%, rounded up
            int limit = Math.max(1, rewardCount); // At least 1 person gets rewarded

            Map<String, Integer> topDamage = bossService.getTopDamageDealers(guildId, limit);
            int playerRank = -1;
            int playerXpReward = 0;

            // Find player's rank and calculate their XP reward
            if (!topDamage.isEmpty() && topDamage.containsKey(userId)) {
                int rank = 1;
                int totalTopDamage = topDamage.values().stream().mapToInt(Integer::intValue).sum();

                // Calculate XP pool (same as in BossService)
                int totalXpPool = boss != null
                        ? 500 + (bossLevel * 100)
                        : 1000 + (bossLevel * 200);

                for (Map.Entry<String, Integer> entry : topDamage.entrySet()) {
                    if (entry.getKey().equals(userId)) {
                        playerRank = rank;
                        int playerDamage = entry.getValue();
                        double damageRatio = (double) playerDamage / totalTopDamage;
                        int baseXp = (int) (totalXpPool * damageRatio);
                        double rankBonus = (rank == 1) ? 1.20 : (rank == 2) ? 1.10 : 1.0;
                        playerXpReward = (int) (baseXp * rankBonus);
                        break;
                    }
                    rank++;
                }
            }

            if (playerRank > 0 && playerRank <= limit) {
                embed.addField("✨ XP Reward",
                        String.format("You ranked **#%d** in damage dealt!\n**+%,d XP** awarded for your contribution.",
                                playerRank, playerXpReward),
                        true);
            }

            // Add concise kill count
            if (boss != null) {
                embed.addField("🏆 Bosses Defeated",
                        String.format("%d", character.getBossesKilled()),
                        true);
            } else {
                embed.addField("👹 Super Bosses Defeated",
                        String.format("%d", character.getSuperBossesKilled()),
                        true);
            }
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

        embed.setFooter(String.format("Heroic Charges: %d/%d | Keep battling to defeat the boss!",
                remainingCharges,
                character.getMaxHeroicCharges()));
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.info("User {} battled boss in guild {} - Damage: {}", userId, guildId, damage);
    }

    private void handleSecretBossBattle(SlashCommandInteractionEvent event, String userId, String guildId) {
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
            event.reply("❌ You cannot battle bosses while dead or recovering!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if character has event charges
        if (!character.canPerformEventAction()) {
            int remaining = character.getEventCharges();
            event.reply(String.format("""
                            ⚔️ **No Event Charges Available**
                            
                            Event charges remaining: **%d/%d**
                            
                            Event charges are granted when secret bosses appear.
                            """,
                    remaining,
                    character.getMaxEventCharges()
            )).setEphemeral(true).queue();
            return;
        }

        // Check for active secret boss
        BossService.ServerBossState state = bossService.getState(guildId);
        if (state == null) {
            event.reply("❌ No secret boss active! Secret bosses appear through mysterious triggers.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Boss secretBoss = state.getSecretBoss(userId);
        if (secretBoss == null) {
            event.reply("❌ No secret boss active! Secret bosses appear through mysterious triggers.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (secretBoss.isDefeated() || secretBoss.isExpired()) {
            state.removeSecretBoss(userId);
            event.reply("❌ Your secret boss has expired or been defeated.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Battle secret boss (this consumes an event charge)
        int damage = bossService.attackSecretBoss(guildId, userId, character);

        if (damage == 0) {
            event.reply("❌ Failed to battle secret boss. Please try again.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Consume event charge after successful battle
        character.useEventCharge();
        int remainingCharges = character.getEventCharges();

        // Build response
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔮 " + character.getName() + " Has Encountered a Secret Boss!");
        embed.setColor(Color.MAGENTA);

        String bossName = secretBoss.getName();
        int bossLevel = secretBoss.getLevel();
        String bossNameWithLevel = String.format("%s (level %d)", bossName, bossLevel);
        int currentHp = secretBoss.getCurrentHp();
        int maxHp = secretBoss.getMaxHp();
        boolean defeated = secretBoss.isDefeated();

        String actionVerb = defeated ? "has slain" : "attacks";

        embed.setDescription(String.format("""
                        **%s** %s **%s**!
                        
                        💥 **Damage Dealt: %d**
                        """,
                character.getName(),
                actionVerb,
                bossNameWithLevel,
                damage
        ));

        // Boss HP bar
        double hpPercent = (double) currentHp / maxHp;
        int filledBlocks = (int) (hpPercent * 20);
        StringBuilder hpBar = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            hpBar.append(i < filledBlocks ? "█" : "░");
        }

        embed.addField("Secret Boss HP", String.format("%s\n%d / %d (%.1f%%)",
                hpBar.toString(), currentHp, maxHp, hpPercent * 100), false);

        if (!defeated) {
            // Time remaining
            Instant expiresAt = secretBoss.getExpiresAt();
            Duration timeRemaining = Duration.between(Instant.now(), expiresAt);
            long hours = timeRemaining.toHours();
            long minutes = timeRemaining.toMinutesPart();

            embed.addField("⏰ Time Remaining",
                    String.format("%dh %dm until secret boss expires", hours, minutes), false);
        } else {
            embed.addField("🎉 Victory!", "The secret boss has been defeated!", false);
        }

        embed.addField("Secret Boss Charges",
                String.format("**%d/%d** Charges Remaining | You have 10 attempts total to defeat this secret boss!",
                        remainingCharges, character.getMaxEventCharges()), false);

        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.debug("Secret boss battle: {} dealt {} damage to {} in guild {}",
                character.getName(), damage, bossName, guildId);
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

            // Check for Class Harmony mechanic
            boolean hasHarmonyMechanic = (boss != null && boss.hasClassHarmonyMechanic()) ||
                    (superBoss != null && superBoss.hasClassHarmonyMechanic());
            
            if (hasHarmonyMechanic) {
                Map<CharacterClass, Double> classPercentages = bossService.getClassParticipationPercentages(guildId);
                String harmonyMessage = bossService.getHarmonyFeedbackMessage(guildId, superBoss != null);
                
                if (!classPercentages.isEmpty()) {
                    StringBuilder classDistribution = new StringBuilder();
                    for (Map.Entry<CharacterClass, Double> entry : classPercentages.entrySet()) {
                        classDistribution.append(String.format("%s: **%.1f%%**\n", 
                                entry.getKey().getDisplayName(), entry.getValue()));
                    }
                    embed.addField("⚖️ Class Distribution", classDistribution.toString().trim(), false);
                }
                
                embed.addField("🌌 Harmony Status", harmonyMessage, false);
            }
        }

        // Time remaining and losing streak
        if (boss != null || superBoss != null) {
            Instant expiresAt = boss != null ? boss.getExpiresAt() : superBoss.getExpiresAt();
            long secondsRemaining = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
            Duration duration = Duration.ofSeconds(Math.max(0, secondsRemaining));
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();

            String timeRemaining = String.format("%dh %dm", hours, minutes);
            embed.addField("⏰ Time Remaining", timeRemaining, true);

            // Losing streak
            int consecutiveFailures = state.getConsecutiveFailures();
            if (consecutiveFailures > 0) {
                embed.addField("📉 Losing Streak", String.format("**%d** consecutive failure%s",
                        consecutiveFailures, consecutiveFailures > 1 ? "s" : ""), true);
            }

            // Empowerment level
            int empowermentLevel = 0;
            if (consecutiveFailures >= 5) {
                empowermentLevel = 2;
            } else if (consecutiveFailures >= 3) {
                empowermentLevel = 1;
            }
            if (empowermentLevel > 0) {
                embed.addField("⚡ Empowerment", String.format("Level **%d** (stats boosted)", empowermentLevel), true);
            }
        }

        // Active World Curses
        var activeCurses = worldCurseService.getActiveCurses(guildId);
        if (!activeCurses.isEmpty()) {
            StringBuilder curseDisplay = new StringBuilder();
            for (WorldCurse curse : activeCurses) {
                String bossName = worldCurseService.getBossNameForCurse(guildId, curse);
                if (bossName != null) {
                    curseDisplay.append(String.format("%s (from **%s**)\n*%s*\n\n",
                            curse.getDisplayName(), bossName, curse.getDescription()));
                } else {
                    curseDisplay.append(String.format("%s\n*%s*\n\n", curse.getDisplayName(), curse.getDescription()));
                }
            }
            embed.addField("🌑 Active World Curses", curseDisplay.toString().trim(), false);
            embed.setColor(Color.RED); // Change color to indicate cursed state
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

        // Check if there's an active boss
        BossService.ServerBossState state = bossService.getState(guildId);
        Boss currentBoss = state != null ? state.getCurrentBoss() : null;
        SuperBoss currentSuperBoss = state != null ? state.getCurrentSuperBoss() : null;

        boolean hasActiveBoss = (currentBoss != null && !currentBoss.isDefeated() && !currentBoss.isExpired()) ||
                (currentSuperBoss != null && !currentSuperBoss.isDefeated() && !currentSuperBoss.isExpired());

        if (topDamage.isEmpty()) {
            if (hasActiveBoss) {
                // Active boss exists but no damage dealt yet
                embed.setDescription("No damage dealt yet. Be the first to battle!");
            } else {
                // No active boss - show lore-friendly message
                embed.setDescription(formatLoreFriendlyNoBossMessage(state));
            }
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

    /**
     * Calculates the next boss spawn time based on current boss state.
     * Bosses spawn every 24 hours.
     *
     * @param state the boss state for the guild
     * @return the next spawn time, or null if cannot be determined
     */
    private Instant calculateNextBossSpawnTime(BossService.ServerBossState state) {
        if (state == null) {
            return null;
        }

        Instant now = Instant.now();
        Boss currentBoss = state.getCurrentBoss();
        SuperBoss currentSuperBoss = state.getCurrentSuperBoss();

        // If there's an active boss, next spawn is 24 hours from its spawn time
        if (currentBoss != null && !currentBoss.isDefeated() && !currentBoss.isExpired()) {
            return currentBoss.getSpawnTime().plus(24, ChronoUnit.HOURS);
        }

        if (currentSuperBoss != null && !currentSuperBoss.isDefeated() && !currentSuperBoss.isExpired()) {
            return currentSuperBoss.getSpawnTime().plus(24, ChronoUnit.HOURS);
        }

        // If boss exists but is expired or defeated, calculate from expiration/defeat time
        if (currentBoss != null) {
            Instant referenceTime = currentBoss.isExpired() ? currentBoss.getExpiresAt() : currentBoss.getSpawnTime();
            Instant nextSpawn = referenceTime.plus(24, ChronoUnit.HOURS);
            // If next spawn is in the past, it should have already spawned, so return null
            return nextSpawn.isAfter(now) ? nextSpawn : null;
        }

        if (currentSuperBoss != null) {
            Instant referenceTime = currentSuperBoss.isExpired() ? currentSuperBoss.getExpiresAt() : currentSuperBoss.getSpawnTime();
            Instant nextSpawn = referenceTime.plus(24, ChronoUnit.HOURS);
            return nextSpawn.isAfter(now) ? nextSpawn : null;
        }

        // No boss state - cannot determine next spawn
        return null;
    }

    /**
     * Formats a lore-friendly message when no active boss exists.
     * Includes time until next boss spawn if calculable.
     *
     * @param state the boss state for the guild
     * @return formatted lore-friendly message
     */
    private String formatLoreFriendlyNoBossMessage(BossService.ServerBossState state) {
        Instant nextSpawn = calculateNextBossSpawnTime(state);

        String baseMessage = "While danger has retreated to the shadows, it's a beautiful day out in Nilfheim. " +
                "No world ending dangers today.";

        if (nextSpawn != null) {
            Instant now = Instant.now();
            Duration timeUntilSpawn = Duration.between(now, nextSpawn);
            long hours = timeUntilSpawn.toHours();
            long minutes = timeUntilSpawn.toMinutes() % 60;

            if (hours > 0) {
                if (minutes > 0) {
                    return baseMessage + String.format("\n\n⏰ The next threat emerges in **%d hours and %d minutes**.", hours, minutes);
                } else {
                    return baseMessage + String.format("\n\n⏰ The next threat emerges in **%d hour%s**.", hours, hours != 1 ? "s" : "");
                }
            } else if (minutes > 0) {
                return baseMessage + String.format("\n\n⏰ The next threat emerges in **%d minute%s**.", minutes, minutes != 1 ? "s" : "");
            } else {
                return baseMessage + "\n\n⏰ A new threat will emerge soon.";
            }
        }

        // No time estimate available
        return baseMessage + "\n\n⏰ Bosses spawn every 24 hours. Stay vigilant, heroes.";
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
