package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.CraftedItemType;
import com.tatumgames.mikros.games.rpg.model.CraftingResult;
import com.tatumgames.mikros.games.rpg.model.InfusionType;
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
import java.time.Instant;

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

        // Add permanent stat bonus items
        for (CraftedItemType itemType : CraftedItemType.values()) {
            itemOption.addChoice(
                    itemType.getEmoji() + " " + itemType.getDisplayName(),
                    "PERMANENT_" + itemType.name()
            );
        }

        // Add consumable infusions
        for (InfusionType infusionType : InfusionType.values()) {
            itemOption.addChoice(
                    infusionType.getEmoji() + " " + infusionType.getDisplayName(),
                    "INFUSION_" + infusionType.name()
            );
        }

        return Commands.slash("rpg-craft", "Craft items for permanent stat bonuses or consumable infusions")
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

        // Determine if it's a permanent item or infusion
        boolean isInfusion = itemName.startsWith("INFUSION_");
        boolean isPermanent = itemName.startsWith("PERMANENT_");

        CraftingResult result;
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚒️ Crafting");

        if (isInfusion) {
            // Handle infusion crafting
            String infusionName = itemName.substring("INFUSION_".length());
            InfusionType infusionType;
            try {
                infusionType = InfusionType.valueOf(infusionName);
            } catch (IllegalArgumentException e) {
                event.reply("❌ Invalid infusion type.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            result = craftingService.craftInfusion(character, infusionType);

            if (result == CraftingResult.SUCCESS) {
                embed.setColor(Color.GREEN);
                embed.setDescription(String.format(
                        "✨ You successfully crafted **%s %s**!\n\n" +
                                "%s\n\n" +
                                "This infusion is active and will be consumed on your next action (or expire after 24 hours).",
                        infusionType.getEmoji(),
                        infusionType.getDisplayName(),
                        infusionType.getDescription()
                ));
            } else if (result == CraftingResult.INSUFFICIENT_MATERIALS) {
                embed.setColor(Color.RED);
                StringBuilder required = new StringBuilder();
                required.append("❌ **Insufficient Materials or Infusion Already Active**\n\n");

                if (character.getInventory().hasActiveInfusion()) {
                    required.append("You already have an active infusion. Max 1 infusion at a time.\n\n");
                } else {
                    required.append("**Required Materials:**\n");
                    if (infusionType.isElementalConvergence()) {
                        required.append("1x of each essence type (5 total)\n");
                        required.append(String.format("%s %s ×1\n",
                                infusionType.getRequiredCatalyst().getEmoji(),
                                infusionType.getRequiredCatalyst().getDisplayName()));
                    } else {
                        required.append(String.format("%s %s ×%d\n",
                                infusionType.getRequiredEssence().getEmoji(),
                                infusionType.getRequiredEssence().getDisplayName(),
                                infusionType.getEssenceCount()));
                        required.append(String.format("%s %s ×%d\n",
                                infusionType.getRequiredCatalyst().getEmoji(),
                                infusionType.getRequiredCatalyst().getDisplayName(),
                                infusionType.getCatalystCount()));
                    }
                }

                embed.setDescription(required.toString());
            }
        } else if (isPermanent) {
            // Handle permanent item crafting
            String permanentName = itemName.substring("PERMANENT_".length());
            CraftedItemType itemType;
            try {
                itemType = CraftedItemType.valueOf(permanentName);
            } catch (IllegalArgumentException e) {
                event.reply("❌ Invalid item type.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            result = craftingService.craft(character, itemType);

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
        } else {
            // Legacy support: try to parse as old format
            CraftedItemType itemType;
            try {
                itemType = CraftedItemType.valueOf(itemName);
            } catch (IllegalArgumentException e) {
                event.reply("❌ Invalid item type.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            result = craftingService.craft(character, itemType);

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
        }

        embed.setFooter("Use /rpg-inventory to view your materials and bonuses");
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        // Log crafting attempt
        String itemDisplayName = isInfusion ?
                InfusionType.valueOf(itemName.substring("INFUSION_".length())).getDisplayName() :
                (isPermanent ?
                        CraftedItemType.valueOf(itemName.substring("PERMANENT_".length())).getDisplayName() :
                        itemName);
        logger.info("Crafting attempted by {}: item={}, result={}",
                userId, itemDisplayName, result);
    }

    @Override
    public String getCommandName() {
        return "rpg-craft";
    }
}

