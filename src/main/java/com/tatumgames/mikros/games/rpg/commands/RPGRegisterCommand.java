package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Command handler for /rpg-register.
 * Allows users to create their RPG character.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGRegisterCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGRegisterCommand.class);
    private final CharacterService characterService;

    /**
     * Creates a new RPGRegisterCommand handler.
     *
     * @param characterService the character service
     */
    public RPGRegisterCommand(CharacterService characterService) {
        this.characterService = characterService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-register", "Create your RPG character and begin your adventure in Nilfheim")
                .addOption(OptionType.STRING, "name", "Your character's name", true)
                .addOption(OptionType.STRING, "class", "Your character class (WARRIOR, KNIGHT, MAGE, ROGUE, NECROMANCER, PRIEST)", true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (guild == null || member == null) {
            event.reply("❌ This command can only be used in a server.")
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

        // Check if user already has a character
        if (characterService.hasCharacter(userId)) {
            event.reply("❌ You already have a character! Use `/rpg-profile` to view it.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get options
        OptionMapping nameOption = event.getOption("name");
        String name = nameOption != null
                ? nameOption.getAsString().trim()
                : "";

        OptionMapping classOption = event.getOption("class");
        String classString = classOption != null
                ? classOption.getAsString().toUpperCase().trim()
                : "";

        // Validate name
        if (name.length() < 2 || name.length() > 20) {
            event.reply("❌ Character name must be between 2 and 20 characters.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Validate class
        CharacterClass characterClass;
        try {
            characterClass = CharacterClass.valueOf(classString);
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid class! Choose from: **WARRIOR**, **KNIGHT**, **MAGE**, **ROGUE**, **NECROMANCER**, or **PRIEST**")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Create character
        try {
            RPGCharacter character = characterService.registerCharacter(userId, name, characterClass);

            // Build welcome embed
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("⚔️ Character Created!");
            embed.setColor(Color.GREEN);
            embed.setDescription(String.format("""
                    Your soul awakens in **Nilfheim** — a realm wrapped in cold twilight, plagued by rising horrors.
                    
                    Heroes are few. Legends are fewer. Yet fate stirs… and your journey begins, **%s**.
                    """, name));

            embed.addField(
                    "Class",
                    String.format("%s **%s**",
                            characterClass.getEmoji(),
                            characterClass.getDisplayName()),
                    true
            );

            embed.addField(
                    "Level",
                    String.valueOf(character.getLevel()),
                    true
            );

            embed.addField(
                    "XP",
                    String.format("%d / %d",
                            character.getXp(),
                            character.getXpToNextLevel()),
                    true
            );

            embed.addField(
                    "Stats",
                    String.format("""
                                    ❤️ HP: %d/%d
                                    ⚔️ STR: %d
                                    🏃 AGI: %d
                                    🧠 INT: %d
                                    🍀 LUCK: %d
                                    """,
                            character.getStats().getCurrentHp(),
                            character.getStats().getMaxHp(),
                            character.getStats().getStrength(),
                            character.getStats().getAgility(),
                            character.getStats().getIntelligence(),
                            character.getStats().getLuck()
                    ),
                    false
            );

            embed.addField(
                    "🎮 Getting Started",
                    """
                            • Use `/rpg-action` to explore, train, or battle
                            • Use `/rpg-profile` to view your stats
                            • Use `/rpg-leaderboard` to see top players
                            
                            Good luck on your journey!
                            """,
                    false
            );

            embed.setTimestamp(java.time.Instant.now());

            event.replyEmbeds(embed.build()).queue();

            logger.info("User {} registered character: {} ({})",
                    userId, name, characterClass.getDisplayName());

        } catch (IllegalStateException e) {
            event.reply("❌ Error creating character: " + e.getMessage())
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public String getCommandName() {
        return "rpg-register";
    }
}

