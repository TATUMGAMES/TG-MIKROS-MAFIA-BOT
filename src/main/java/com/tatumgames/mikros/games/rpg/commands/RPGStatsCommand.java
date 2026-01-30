package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
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
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static java.time.ZoneId.systemDefault;

/**
 * Command handler for /rpg-stats.
 * Displays detailed RPG statistics including kill counts and character progress.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGStatsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGStatsCommand.class);
    private final CharacterService characterService;

    /**
     * Creates a new RPGStatsCommand handler.
     *
     * @param characterService the character service
     */
    public RPGStatsCommand(CharacterService characterService) {
        this.characterService = characterService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-stats", "View detailed RPG statistics including kill counts and character progress")
                .addOption(OptionType.USER, "user", "View another user's stats (optional)", false);
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

        // Determine whose stats to show
        User targetUser = event.getOption("user", OptionMapping::getAsUser);
        String targetUserId = (targetUser != null) ? targetUser.getId() : event.getUser().getId();

        // Get character
        RPGCharacter character = characterService.getCharacter(targetUserId);

        if (character == null) {
            String message = targetUserId.equals(event.getUser().getId())
                    ? "❌ You don't have a character yet! Use `/rpg-register` to create one."
                    : "❌ That user doesn't have a character yet.";

            event.reply(message).setEphemeral(true).queue();
            return;
        }

        // Get guild config and check channel
        RPGConfig config = characterService.getConfig(guild.getId());

        // Check if in correct channel (if specified)
        if (config != null && config.getRpgChannelId() != null) {
            if (!event.getChannel().getId().equals(config.getRpgChannelId())) {
                event.reply(String.format(
                        "Please use `/rpg-stats` in <#%s>. RPG commands are restricted to the assigned channel.",
                        config.getRpgChannelId()
                )).setEphemeral(true).queue();
                return;
            }
        }

        // Build stats embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("📊 RPG Statistics - %s", character.getName()));

        embed.setColor(getClassColor(character.getCharacterClass().name()));

        // Combat Statistics
        embed.addField(
                "⚔️ Combat Statistics",
                String.format("""
                                💀 Enemies Defeated: **%d**
                                🐲 Bosses Defeated: **%d**
                                👹 Super Bosses Defeated: **%d**
                                🔮 Secret Bosses Defeated: **%d**""",
                        character.getEnemiesKilled(),
                        character.getBossesKilled(),
                        character.getSuperBossesKilled(),
                        character.getSecretBossesKilled()),
                false
        );

        // Character Progress
        double xpPercent = (double) character.getXp() / character.getXpToNextLevel() * 100;
        embed.addField(
                "📈 Character Progress",
                String.format("""
                                Level: **%d**
                                XP: **%d** / %d (%.1f%%)
                                XP to Next Level: **%d**""",
                        character.getLevel(),
                        character.getXp(),
                        character.getXpToNextLevel(),
                        xpPercent,
                        character.getXpToNextLevel() - character.getXp()),
                false
        );

        // Current Status
        String status;
        if (character.isDead()) {
            status = "Dead";
        } else if (character.isRecovering()) {
            status = "Recovering";
        } else {
            status = "Alive";
        }

        embed.addField(
                "❤️ Current Status",
                String.format("""
                                HP: **%d** / %d
                                Action Charges: **%d** / %d
                                Status: **%s**""",
                        character.getStats().getCurrentHp(),
                        character.getStats().getMaxHp(),
                        character.getActionCharges(),
                        character.getMaxActionCharges(),
                        status),
                false
        );

        // Stats
        embed.addField(
                "📊 Stats",
                String.format("""
                                ⚔️ STR: **%d**
                                🏃 AGI: **%d**
                                🧠 INT: **%d**
                                🍀 LUCK: **%d**""",
                        character.getStats().getStrength(),
                        character.getStats().getAgility(),
                        character.getStats().getIntelligence(),
                        character.getStats().getLuck()),
                true
        );

        // Footer
        embed.setFooter(String.format("Character created %s", formatTimestamp(character.getCreatedAt())));
        embed.setTimestamp(character.getCreatedAt());

        event.replyEmbeds(embed.build()).queue();

        logger.debug("Stats requested for character: {}", character.getName());
    }

    /**
     * Gets color for character class.
     *
     * @param className the character class name
     * @return the color for the class
     */
    private Color getClassColor(String className) {
        return switch (className) {
            case "WARRIOR" -> Color.RED;
            case "KNIGHT" -> Color.GRAY;
            case "MAGE" -> Color.CYAN;
            case "ROGUE" -> Color.ORANGE;
            case "NECROMANCER" -> Color.MAGENTA;
            case "PRIEST" -> Color.WHITE;
            default -> Color.GRAY;
        };
    }

    /**
     * Formats a timestamp for display.
     *
     * @param instant the instant to format
     * @return formatted timestamp string
     */
    private String formatTimestamp(Instant instant) {
        return DateTimeFormatter.ofPattern("MMM dd, yyyy")
                .format(instant.atZone(systemDefault()));
    }

    @Override
    public String getCommandName() {
        return "rpg-stats";
    }
}

