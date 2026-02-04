package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.games.rpg.biome.BiomeType;
import com.tatumgames.mikros.games.rpg.blessing.Blessing;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.curse.WorldCurse;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.BlessingService;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import com.tatumgames.mikros.games.rpg.service.WorldCurseService;
import com.tatumgames.mikros.handler.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command handler for /rpg-profile. Displays a character's stats and information.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGProfileCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGProfileCommand.class);
    private final CharacterService characterService;
    private final WorldCurseService worldCurseService;
    private final BlessingService blessingService;

    /**
     * Creates a new RPGProfileCommand handler.
     *
     * @param characterService  the character service
     * @param worldCurseService the world curse service
     * @param blessingService   the blessing service
     */
    public RPGProfileCommand(
            CharacterService characterService,
            WorldCurseService worldCurseService,
            BlessingService blessingService) {
        this.characterService = characterService;
        this.worldCurseService = worldCurseService;
        this.blessingService = blessingService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-profile", "View your RPG character profile and stats")
                .addOption(OptionType.USER, "user", "View another user's profile (optional)", false);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        // Determine whose profile to show
        User targetUser = event.getOption("user", OptionMapping::getAsUser);
        String targetUserId = (targetUser != null) ? targetUser.getId() : event.getUser().getId();

        // Get character
        RPGCharacter character = characterService.getCharacter(targetUserId);

        if (character == null) {
            String message =
                    targetUserId.equals(event.getUser().getId())
                            ? "❌ You don't have a character yet! Use `/rpg-register` to create one."
                            : "❌ That user doesn't have a character yet.";

            event.reply(message).setEphemeral(true).queue();
            return;
        }

        // Get guild config for cooldown info
        RPGConfig config = characterService.getConfig(guild.getId());

        // Require setup before RPG commands work
        if (config.getRpgChannelId() == null) {
            event
                    .reply(
                            "❌ RPG is not set up for this server. An administrator must run `/admin-rpg-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if in correct channel (if specified)
        if (config != null && config.getRpgChannelId() != null) {
            if (!event.getChannel().getId().equals(config.getRpgChannelId())) {
                event
                        .reply(
                                String.format(
                                        "Please use `/rpg-profile` in <#%s>. RPG commands are restricted to the assigned channel.",
                                        config.getRpgChannelId()))
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        // Build profile embed
        EmbedBuilder embed = new EmbedBuilder();

        // Build title with character title prefix if present
        String titlePrefix = character.getTitle() != null ? character.getTitle() + " " : "";
        embed.setTitle(
                String.format(
                        "%s %s%s - Level %d %s",
                        character.getCharacterClass().getEmoji(),
                        titlePrefix,
                        character.getName(),
                        character.getLevel(),
                        character.getCharacterClass().getDisplayName()));

        embed.setColor(getClassColor(character.getCharacterClass().name()));

        // XP Progress
        double xpPercent = (double) character.getXp() / character.getXpToNextLevel() * 100;
        embed.addField(
                "📊 Experience",
                String.format(
                        "**%d** / %d XP (%.1f%%)\n" + "%d XP to next level",
                        character.getXp(),
                        character.getXpToNextLevel(),
                        xpPercent,
                        character.getXpToNextLevel() - character.getXp()),
                false);

        // Get active curses for effective HP calculation
        String guildId = config != null ? config.getGuildId() : guild.getId();
        var activeCurses = worldCurseService.getActiveCurses(guildId);
        if (activeCurses == null) {
            activeCurses = Collections.emptyList();
        }
        int effectiveMaxHp =
                character.getStats().getEffectiveMaxHp(activeCurses, character.hasFrostbite());
        int originalMaxHp = character.getStats().getMaxHp();

        // Calculate HP reduction percentage if cursed
        String hpModifier = "";
        if (effectiveMaxHp < originalMaxHp) {
            double reductionPercent = ((double) (originalMaxHp - effectiveMaxHp) / originalMaxHp) * 100.0;
            hpModifier = String.format(" ⚠️ (-%.0f%%)", reductionPercent);
        }

        // Stats
        embed.addField(
                "📈 Stats",
                String.format(
                        """
                                ❤️ HP: **%d** / %d%s
                                ⚔️ STR: **%d**
                                🏃 AGI: **%d**
                                🧠 INT: **%d**
                                🍀 LUCK: **%d**""",
                        character.getStats().getCurrentHp(),
                        effectiveMaxHp,
                        hpModifier,
                        character.getStats().getStrength(),
                        character.getStats().getAgility(),
                        character.getStats().getIntelligence(),
                        character.getStats().getLuck()),
                true);

        // Cooldown Status
        boolean canAct = character.canPerformAction(config.getChargeRefreshHours());
        String cooldownStatus;

        if (canAct) {
            cooldownStatus = "✅ **Ready to act!**\n\nUse `/rpg-action` to continue your adventure";
        } else {
            long secondsRemaining =
                    character.getSecondsUntilChargeRefresh(config.getChargeRefreshHours());
            Duration duration = Duration.ofSeconds(secondsRemaining);
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();

            cooldownStatus =
                    String.format(
                            "⏳ **On cooldown**\n\nNext action available in:\n**%dh %dm**", hours, minutes);
        }

        embed.addField("⚡ Action Status", cooldownStatus, true);

        // Heroic charges (for boss battles)
        String heroicStatus =
                String.format(
                        "⚔️ **%d/%d Heroic Charges**\n\nCharges refresh when a new boss spawns",
                        character.getHeroicCharges(), character.getMaxHeroicCharges());
        embed.addField("🛡️ Heroic Charges", heroicStatus, true);

        // Biome information
        BiomeType currentBiome = character.getCurrentBiome();
        int explorationsInBiome = character.getExplorationsInCurrentBiome();
        String biomeStatus =
                String.format(
                        "%s **%s**\n\n%d/10 explorations\nAdvance to next biome after 10 explorations",
                        currentBiome.getEmoji(), currentBiome.getDisplayName(), explorationsInBiome);
        embed.addField("🗺️ Current Biome", biomeStatus, true);

        // Crafted bonuses
        var inventory = character.getInventory();
        StringBuilder craftedBonuses = new StringBuilder();
        craftedBonuses.append(String.format("STR: **+%d/5** | ", inventory.getCraftedBonus("STR")));
        craftedBonuses.append(String.format("AGI: **+%d/5** | ", inventory.getCraftedBonus("AGI")));
        craftedBonuses.append(String.format("INT: **+%d/5**\n", inventory.getCraftedBonus("INT")));
        craftedBonuses.append(String.format("LUCK: **+%d/5** | ", inventory.getCraftedBonus("LUCK")));
        craftedBonuses.append(String.format("HP: **+%d/5**", inventory.getCraftedBonus("HP")));

        embed.addField("✨ Crafted Bonuses", craftedBonuses.toString(), false);

        // Temporary Debuffs and Curses
        StringBuilder debuffs = new StringBuilder();
        boolean hasDebuffs = false;

        // Add curse information with boss names
        if (!activeCurses.isEmpty()) {
            for (WorldCurse curse : activeCurses) {
                String bossName = worldCurseService.getBossNameForCurse(guildId, curse);
                String curseLine;
                if (bossName != null) {
                    curseLine =
                            String.format(
                                    "You have been cursed by **%s** for not defeating them.\n%s: %s\n\n",
                                    bossName, curse.getDisplayName(), curse.getDescription());
                } else {
                    curseLine = String.format("%s: %s\n\n", curse.getDisplayName(), curse.getDescription());
                }
                debuffs.append(curseLine);
                hasDebuffs = true;
            }
        }

        if (character.hasFrostbite()) {
            debuffs.append("🩸 **Frostbite:** Max HP reduced by 5% (removed by rest)\n");
            hasDebuffs = true;
        }

        if (character.getDarkRelicActionsRemaining() > 0) {
            debuffs.append(
                    String.format(
                            "🕯️ **Dark Relic:** +5%% XP, +10%% damage taken (%d actions remaining)\n",
                            character.getDarkRelicActionsRemaining()));
            hasDebuffs = true;
        }

        if (hasDebuffs) {
            embed.addField("⚠️ Temporary Effects", debuffs.toString().trim(), false);
        }

        // Duel record
        embed.addField(
                "⚔️ Duels",
                String.format(
                        "**%d Wins** | **%d Losses**", character.getDuelsWon(), character.getDuelsLost()),
                true);

        // Boss kills (including secret bosses)
        embed.addField(
                "🐲 Boss Defeats",
                String.format(
                        "Normal: **%d** | Super: **%d** | Secret: **%d**",
                        character.getBossesKilled(),
                        character.getSuperBossesKilled(),
                        character.getSecretBossesKilled()),
                true);

        // Legendary Aura
        if (character.getLegendaryAura() != null) {
            String auraName = character.getLegendaryAura();
            String auraDisplay =
                    switch (auraName) {
                        case "SONG_OF_NILFHEIM" -> "🌟 Song of Nilfheim";
                        case "HEROS_MARK" -> "⚔️ Hero's Mark";
                        case "GRAVEBOUND_PRESENCE" -> "💀 Gravebound Presence";
                        default -> auraName;
                    };
            embed.addField("✨ Legendary Aura", auraDisplay, false);
        }

        // Story Flags
        if (!character.getStoryFlags().isEmpty()) {
            String flags = String.join(" | ", character.getStoryFlags());
            embed.addField("📜 Legend", flags, false);
        }

        // Irrevocable World Encounters
        StringBuilder irrevocableInfo = new StringBuilder();
        boolean hasIrrevocable = false;

        if (character.getDeityBlessing() != null) {
            String deityName = character.getDeityBlessing().replace("_", " ");
            irrevocableInfo.append(String.format("🏛️ **Deity:** %s\n", deityName));
            hasIrrevocable = true;
        }

        if (character.getRelicChoice() != null) {
            String relicName = character.getRelicChoice().replace("_", " ");
            irrevocableInfo.append(String.format("⚔️ **Relic:** %s\n", relicName));
            hasIrrevocable = true;
        }

        if (character.getPhilosophicalPath() != null) {
            String pathName = character.getPhilosophicalPath();
            if ("UNBOUND".equals(pathName)) {
                irrevocableInfo.append("⚖️ **Path:** Unbound - Rejected the path of the gods\n");
            } else if ("GODMARKED".equals(pathName)) {
                irrevocableInfo.append("👤 **Path:** God-Marked - Passed the test of the gods\n");
            } else {
                irrevocableInfo.append(String.format("📿 **Path:** %s - Unknown origins\n", pathName));
            }
            hasIrrevocable = true;
        }

        if (hasIrrevocable) {
            embed.addField("🔮 Irrevocable Choices", irrevocableInfo.toString().trim(), false);
        }

        // World Flags (separate from story flags) - filter out test flags (e.g. INT_TEST_PASSED,
        // STR_TEST_ATTEMPTED)
        Set<String> displayFlags =
                character.getWorldFlags().stream()
                        .filter(flag -> !flag.contains("TEST_PASSED") && !flag.contains("TEST_ATTEMPTED"))
                        .collect(Collectors.toSet());
        if (!displayFlags.isEmpty()) {
            String worldFlags = String.join(" | ", displayFlags);
            embed.addField("🌍 World Flags", worldFlags, false);
        }

        // Active Stat Modifiers
        var statModifiers = character.getStatModifiers();
        if (!statModifiers.isEmpty()) {
            StringBuilder modifierInfo = new StringBuilder();
            for (var entry : statModifiers.entrySet()) {
                String statName = entry.getKey().replace("_EFFECTIVENESS", "").replace("_", " ");
                double modifier = entry.getValue();
                double percentChange = (modifier - 1.0) * 100;
                String sign = percentChange >= 0 ? "+" : "";
                modifierInfo.append(String.format("%s: **%s%.0f%%**\n", statName, sign, percentChange));
            }
            embed.addField("⚡ Active Modifiers", modifierInfo.toString().trim(), false);
        }

        // Active Blessings (class-specific, only during boss battles)
        Blessing blessing = blessingService.getBlessingForClass(guildId, character.getCharacterClass());
        if (blessing != null) {
            // Get base blessing to access original grantedAt timestamp
            Blessing baseBlessing = blessingService.getActiveBlessing(guildId);
            StringBuilder blessingInfo = new StringBuilder();
            blessingInfo.append(blessing.getEffectsDescription());

            // Add timestamp if base blessing is available
            if (baseBlessing != null) {
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").withZone(ZoneId.systemDefault());
                String timestamp = formatter.format(baseBlessing.getGrantedAt());
                blessingInfo.append("\n\nActive since: ").append(timestamp);
            }

            blessingInfo.append("\n\n*Active only during boss battles*");
            embed.addField("💫 Active Blessings", blessingInfo.toString().trim(), false);
        }

        // Active World Curses
        if (!activeCurses.isEmpty()) {
            StringBuilder curseDisplay = new StringBuilder();
            for (WorldCurse curse : activeCurses) {
                curseDisplay.append(
                        String.format("%s\n*%s*\n\n", curse.getDisplayName(), curse.getDescription()));
            }
            embed.addField("🌑 Active World Curses", curseDisplay.toString().trim(), false);
            embed.setColor(Color.ORANGE); // Change color to indicate cursed state
        }

        // Footer
        embed.setFooter(
                String.format(
                        "Character created • Total: %d characters", characterService.getCharacterCount()));
        embed.setTimestamp(character.getCreatedAt());

        event.replyEmbeds(embed.build()).queue();

        logger.debug("Profile requested for character: {}", character.getName());
    }

    /**
     * Gets color for character class.
     */
    private Color getClassColor(String className) {
        return switch (className) {
            case "WARRIOR" -> Color.RED;
            case "MAGE" -> Color.CYAN;
            case "ROGUE" -> Color.ORANGE;
            default -> Color.GRAY;
        };
    }

    @Override
    public String getCommandName() {
        return "rpg-profile";
    }
}
