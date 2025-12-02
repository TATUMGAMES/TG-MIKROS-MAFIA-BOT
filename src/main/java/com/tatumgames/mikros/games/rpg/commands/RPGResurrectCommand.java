package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.games.rpg.actions.ResurrectAction;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
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

/**
 * Command handler for /rpg-resurrect.
 * Allows Priests to resurrect dead players (free action, no charge cost).
 */
public class RPGResurrectCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGResurrectCommand.class);
    private final CharacterService characterService;
    private final ResurrectAction resurrectAction;

    /**
     * Creates a new RPGResurrectCommand handler.
     *
     * @param characterService the character service
     */
    public RPGResurrectCommand(CharacterService characterService) {
        this.characterService = characterService;
        this.resurrectAction = new ResurrectAction();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-resurrect", "Resurrect a dead player (Priest-only, free action)")
                .addOption(OptionType.USER, "target", "The dead player to resurrect", true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            return;
        }

        String userId = event.getUser().getId();
        String guildId = guild.getId();

        // Check if user has a character
        RPGCharacter priest = characterService.getCharacter(userId);
        if (priest == null) {
            event.reply("❌ You don't have a character yet! Use `/rpg-register` to create one.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if priest is actually a Priest
        if (priest.getCharacterClass() != CharacterClass.PRIEST) {
            event.reply("❌ Only **Priests** can perform resurrection!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get guild config
        RPGConfig config = characterService.getConfig(guildId);

        // Check if RPG is enabled
        if (!config.isEnabled()) {
            event.reply("❌ The RPG system is currently disabled in this server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get target user
        User targetUser = event.getOption("target", OptionMapping::getAsUser);
        String targetUserId = (targetUser != null)
                ? targetUser.getId()
                : event.getUser().getId();

        // Get character
        RPGCharacter target = characterService.getCharacter(targetUserId);

        if (target == null) {
            event.reply("❌ That user doesn't have a character!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Execute resurrection
        try {
            RPGActionOutcome outcome = resurrectAction.executeWithTarget(priest, target, config);

            // Build result embed
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("✨ Resurrection");
            embed.setColor(outcome.success() ? Color.CYAN : Color.RED);
            embed.setDescription(outcome.narrative());

            if (outcome.xpGained() > 0) {
                embed.addField("Priest Reward", String.format("✨ +%d XP", outcome.xpGained()), false);
            }

            if (target.isDead()) {
                embed.addField("⚠️ Status", "Target is still dead (resurrection failed)", false);
            } else if (target.isRecovering()) {
                embed.addField("⛔ Recovery", "Target is now in recovery for 24 hours", false);
            }

            embed.setFooter("Resurrection is a free action (no charge cost)");
            embed.setTimestamp(java.time.Instant.now());

            event.replyEmbeds(embed.build()).queue();

            logger.info("Priest {} resurrected target {} - Success: {}",
                    userId, targetUserId, outcome.success());

        } catch (Exception e) {
            logger.error("Error performing resurrection for user {}", userId, e);
            event.reply("❌ An error occurred during resurrection. Please try again.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public String getCommandName() {
        return "rpg-resurrect";
    }
}

