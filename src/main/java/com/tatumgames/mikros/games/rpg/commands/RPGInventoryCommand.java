package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.CatalystType;
import com.tatumgames.mikros.games.rpg.model.EssenceType;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.model.RPGInventory;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;

/**
 * Command handler for /rpg-inventory.
 * Displays a character's inventory including essences, catalysts, and crafted bonuses.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGInventoryCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGInventoryCommand.class);
    private final CharacterService characterService;

    /**
     * Creates a new RPGInventoryCommand handler.
     *
     * @param characterService the character service
     */
    public RPGInventoryCommand(CharacterService characterService) {
        this.characterService = characterService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-inventory", "View your RPG inventory and crafted bonuses");
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

        // Check role requirement
        RPGConfig config = characterService.getConfig(guildId);
        if (config != null && !AdminUtils.canUserPlay(member, config.isAllowNoRoleUsers())) {
            event.reply("❌ Users without roles cannot play RPG games in this server. Contact an administrator.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get character
        RPGCharacter character = characterService.getCharacter(userId);

        if (character == null) {
            event.reply("❌ You don't have a character yet! Use `/rpg-register` to create one.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        RPGInventory inventory = character.getInventory();

        // Build inventory embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("💼 Inventory");
        embed.setColor(Color.CYAN);

        // Essences section
        StringBuilder essences = new StringBuilder();
        boolean hasEssences = false;
        for (EssenceType essence : EssenceType.values()) {
            int count = inventory.getEssenceCount(essence);
            if (count > 0) {
                hasEssences = true;
            }
            essences.append(String.format("%s %s: **%d**\n",
                    essence.getEmoji(), essence.getDisplayName(), count));
        }
        if (!hasEssences) {
            essences.append("*No essences*");
        }
        embed.addField("🔥 Essences", essences.toString(), true);

        // Catalysts section
        StringBuilder catalysts = new StringBuilder();
        boolean hasCatalysts = false;
        for (CatalystType catalyst : CatalystType.values()) {
            int count = inventory.getCatalystCount(catalyst);
            if (count > 0) {
                hasCatalysts = true;
            }
            catalysts.append(String.format("%s %s: **%d**\n",
                    catalyst.getEmoji(), catalyst.getDisplayName(), count));
        }
        if (!hasCatalysts) {
            catalysts.append("*No catalysts*");
        }
        embed.addField("⚗️ Catalysts", catalysts.toString(), true);

        // Crafted bonuses section
        StringBuilder bonuses = new StringBuilder();
        bonuses.append(String.format("STR: **+%d/5**\n", inventory.getCraftedBonus("STR")));
        bonuses.append(String.format("AGI: **+%d/5**\n", inventory.getCraftedBonus("AGI")));
        bonuses.append(String.format("INT: **+%d/5**\n", inventory.getCraftedBonus("INT")));
        bonuses.append(String.format("LUCK: **+%d/5**\n", inventory.getCraftedBonus("LUCK")));
        bonuses.append(String.format("HP: **+%d/5**\n", inventory.getCraftedBonus("HP")));
        embed.addField("✨ Crafted Bonuses", bonuses.toString(), false);

        embed.setFooter("Use /rpg-craft to create permanent stat bonuses");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.debug("Inventory requested for character: {}", character.getName());
    }

    @Override
    public String getCommandName() {
        return "rpg-inventory";
    }
}

