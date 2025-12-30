package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.actions.DuelAction;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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

/**
 * Command handler for /rpg-duel.
 * Allows players to challenge each other to duels (free action, 3 per 24h).
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGDuelCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGDuelCommand.class);
    private final CharacterService characterService;
    private final DuelAction duelAction;

    /**
     * Creates a new RPGDuelCommand handler.
     *
     * @param characterService the character service
     */
    public RPGDuelCommand(CharacterService characterService) {
        this.characterService = characterService;
        this.duelAction = new DuelAction();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-duel", "Challenge another player to a duel (free action, 3 per 24h)")
                .addOption(OptionType.USER, "target", "The player to challenge", true);
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

        String challengerId = event.getUser().getId();
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

        // Get challenger character
        RPGCharacter challenger = characterService.getCharacter(challengerId);
        if (challenger == null) {
            event.reply("❌ You don't have a character yet! Use `/rpg-register` to create one.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if RPG is enabled
        if (!config.isEnabled()) {
            event.reply("❌ The RPG system is currently disabled in this server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if challenger can duel
        if (!challenger.canDuel()) {
            if (challenger.isDead() || challenger.isRecovering()) {
                event.reply("❌ You cannot duel while dead or recovering.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
            if (challenger.getDuelsInLast24Hours() >= 3) {
                event.reply("❌ You have reached the maximum of 3 duels per 24 hours. Try again later.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        // Get target user
        User targetUser = event.getOption("target", OptionMapping::getAsUser);
        if (targetUser == null) {
            event.reply("❌ You must specify a target player.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String targetId = targetUser.getId();

        // Cannot duel self
        if (challengerId.equals(targetId)) {
            event.reply("❌ You cannot duel yourself!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get target character
        RPGCharacter target = characterService.getCharacter(targetId);
        if (target == null) {
            event.reply("❌ That user doesn't have a character!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if target is alive/not recovering
        if (target.isDead() || target.isRecovering()) {
            event.reply("❌ That player is dead or recovering and cannot be challenged.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Execute duel
        RPGActionOutcome outcome = duelAction.executeDuel(challenger, target, config);

        // Update records
        challenger.recordDuel(outcome.success());
        target.recordDuel(!outcome.success());

        // Build result embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚔️ Duel");
        embed.setColor(outcome.success() ? Color.GREEN : Color.RED);
        embed.setDescription(outcome.narrative());

        // Win/loss records
        embed.addField("📊 Duel Records",
                String.format("**%s:** %d Wins | %d Losses\n**%s:** %d Wins | %d Losses",
                        challenger.getName(), challenger.getDuelsWon(), challenger.getDuelsLost(),
                        target.getName(), target.getDuelsWon(), target.getDuelsLost()),
                false);

        embed.setFooter(String.format("Duels remaining today: %d/3", 3 - challenger.getDuelsInLast24Hours()));
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.info("Duel between {} and {} - Winner: {}",
                challengerId, targetId, outcome.success() ? challenger.getName() : target.getName());
    }

    @Override
    public String getCommandName() {
        return "rpg-duel";
    }
}

