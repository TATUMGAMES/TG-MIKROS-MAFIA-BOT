package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.CraftedItemType;
import com.tatumgames.mikros.games.rpg.model.CraftingResult;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import com.tatumgames.mikros.games.rpg.service.CraftingService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Command handler for /rpg-craft.
 * Allows players to craft items that grant permanent stat bonuses.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGCraftCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGCraftCommand.class);
    private final CharacterService characterService;
    private final CraftingService craftingService;

    /**
     * Creates a new RPGCraftCommand handler.
     *
     * @param characterService the character service
     * @param craftingService  the crafting service
     */
    public RPGCraftCommand(CharacterService characterService, CraftingService craftingService) {
        this.characterService = characterService;
        this.craftingService = craftingService;
    }

    @Override
    public CommandData getCommandData() {
        OptionData itemOption = new OptionData(OptionType.STRING, "item", "Item to craft", true);
        for (CraftedItemType itemType : CraftedItemType.values()) {
            itemOption.addChoice(
                    itemType.getEmoji() + " " + itemType.getDisplayName(),
                    itemType.name()
            );
        }

        return Commands.slash("rpg-craft", "Craft items for permanent stat bonuses")
                .addOptions(itemOption);
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

        // Check if RPG is enabled
        if (!config.isEnabled()) {
            event.reply("❌ The RPG system is currently disabled in this server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get item option
        OptionMapping itemOption = event.getOption("item");
        if (itemOption == null) {
            event.reply("❌ You must specify an item to craft.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String itemName = itemOption.getAsString();
        CraftedItemType itemType;
        try {
            itemType = CraftedItemType.valueOf(itemName);
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid item type.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Attempt to craft
        CraftingResult result = craftingService.craft(character, itemType);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚒️ Crafting");

        if (result == CraftingResult.SUCCESS) {
            embed.setColor(Color.GREEN);
            embed.setDescription(String.format(
                    "✨ You successfully crafted **%s %s**!\n\n" +
                            "**Bonus Applied:** +%d %s\n\n" +
                            "This bonus is permanent and will persist through death and resurrection.",
                    itemType.getEmoji(),
                    itemType.getDisplayName(),
                    itemType.getStatBonus(),
                    itemType.getStatName()
            ));
        } else if (result == CraftingResult.INSUFFICIENT_MATERIALS) {
            embed.setColor(Color.RED);
            StringBuilder required = new StringBuilder();
            required.append(String.format("**Required Materials:**\n"));
            required.append(String.format("%s %s ×%d\n",
                    itemType.getRequiredEssence().getEmoji(),
                    itemType.getRequiredEssence().getDisplayName(),
                    itemType.getEssenceCount()));
            required.append(String.format("%s %s ×%d\n",
                    itemType.getRequiredCatalyst().getEmoji(),
                    itemType.getRequiredCatalyst().getDisplayName(),
                    itemType.getCatalystCount()));

            var inventory = character.getInventory();
            required.append(String.format("\n**You have:**\n"));
            required.append(String.format("%s %s ×%d\n",
                    itemType.getRequiredEssence().getEmoji(),
                    itemType.getRequiredEssence().getDisplayName(),
                    inventory.getEssenceCount(itemType.getRequiredEssence())));
            required.append(String.format("%s %s ×%d",
                    itemType.getRequiredCatalyst().getEmoji(),
                    itemType.getRequiredCatalyst().getDisplayName(),
                    inventory.getCatalystCount(itemType.getRequiredCatalyst())));

            embed.setDescription("❌ **Insufficient Materials**\n\n" + required.toString());
        } else if (result == CraftingResult.STAT_CAPPED) {
            embed.setColor(Color.ORANGE);
            embed.setDescription(String.format(
                    "⚠️ **Stat Already Capped**\n\n" +
                            "Your %s bonus is already at the maximum (+5/5).\n\n" +
                            "You cannot craft more items that boost this stat.",
                    itemType.getStatName()
            ));
        }

        embed.setFooter("Use /rpg-inventory to view your materials and bonuses");
        embed.setTimestamp(java.time.Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.info("Crafting attempted by {}: item={}, result={}",
                userId, itemType.getDisplayName(), result);
    }

    @Override
    public String getCommandName() {
        return "rpg-craft";
    }
}

