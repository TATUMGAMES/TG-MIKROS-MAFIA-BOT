package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.model.SuperBoss;
import com.tatumgames.mikros.games.rpg.service.BossService;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import com.tatumgames.mikros.handler.CommandHandler;
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
import java.time.Instant;

/**
 * Command handler for /rpg-register. Allows users to create their RPG character.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGRegisterCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGRegisterCommand.class);
    private final CharacterService characterService;
    private final BossService bossService;

    /**
     * Creates a new RPGRegisterCommand handler.
     *
     * @param characterService the character service
     * @param bossService      the boss service for checking boss status
     */
    public RPGRegisterCommand(CharacterService characterService, BossService bossService) {
        this.characterService = characterService;
        this.bossService = bossService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(
                        "rpg-register", "Create your RPG character and begin your adventure in Nilfheim")
                .addOption(OptionType.STRING, "name", "Your character's name", true)
                .addOption(
                        OptionType.STRING,
                        "class",
                        "Your character class (WARRIOR, KNIGHT, MAGE, ROGUE, NECROMANCER, PRIEST, OATHBREAKER)",
                        true);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (guild == null || member == null) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();
        String guildId = guild.getId();

        RPGConfig config = characterService.getConfig(guildId);

        // Require setup before RPG commands work
        if (config.getRpgChannelId() == null) {
            event
                    .reply(
                            "❌ RPG is not set up for this server. An administrator must run `/admin-rpg-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check role requirement
        if (!AdminUtils.canUserPlay(member, config.isAllowNoRoleUsers())) {
            event
                    .reply(
                            "❌ Users without roles cannot play RPG games in this server. Contact an administrator.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if in correct channel (if specified)
        if (config.getRpgChannelId() != null) {
            if (!event.getChannel().getId().equals(config.getRpgChannelId())) {
                event
                        .reply(
                                String.format(
                                        "Please use `/rpg-register` in <#%s>. RPG commands are restricted to the assigned channel.",
                                        config.getRpgChannelId()))
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        // Check if user already has a character (or can re-register after 24h dead)
        if (characterService.hasCharacter(userId)) {
            if (characterService.canReregisterAfterDeath(userId)) {
                characterService.archiveAndRemoveActive(userId);
                // Fall through to register new character
            } else {
                event
                        .reply(
                                "❌ You already have a character! Use `/rpg-profile` to view it. If you are dead and not resurrected within 24 hours, you can re-register a new character.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        // Get options
        OptionMapping nameOption = event.getOption("name");
        String name = nameOption != null ? nameOption.getAsString().trim() : "";

        OptionMapping classOption = event.getOption("class");
        String classString = classOption != null ? classOption.getAsString().toUpperCase().trim() : "";

        // Validate name
        if (name.length() < 2 || name.length() > 20) {
            event
                    .reply("❌ Character name must be between 2 and 20 characters.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Validate class
        CharacterClass characterClass;
        try {
            characterClass = CharacterClass.valueOf(classString);
        } catch (IllegalArgumentException e) {
            event
                    .reply(
                            "❌ Invalid class! Choose from: **WARRIOR**, **KNIGHT**, **MAGE**, **ROGUE**, **NECROMANCER**, **PRIEST**, or **OATHBREAKER**")
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
            embed.setDescription(
                    String.format(
                            """
                    Your soul awakens in **Nilfheim** — a realm wrapped in cold twilight, plagued by rising horrors.

                    Heroes are few. Legends are fewer. Yet fate stirs… and your journey begins, **%s**.
                                    """,
                            name));

            embed.addField(
                    "Class",
                    String.format("%s **%s**", characterClass.getEmoji(), characterClass.getDisplayName()),
                    true);

            embed.addField("Level", String.valueOf(character.getLevel()), true);

            embed.addField(
                    "XP", String.format("%d / %d", character.getXp(), character.getXpToNextLevel()), true);

            embed.addField(
                    "Stats",
                    String.format(
                            """
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
                            character.getStats().getLuck()),
                    false);

            embed.addField(
                    "🎮 Getting Started",
                    """
                            • Use `/rpg-action` to explore, train, or battle
                            • Use `/rpg-profile` to view your stats
                            • Use `/rpg-leaderboard` to see top players

                            Good luck on your journey!
                            """,
                    false);

            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).queue();

            // Send tutorial message as ephemeral (private)
            sendTutorialMessage(event, character, guildId);

            logger.info(
                    "User {} registered character: {} ({})", userId, name, characterClass.getDisplayName());

        } catch (IllegalStateException e) {
            event.reply("❌ Error creating character: " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    /**
     * Sends a tutorial message to the user explaining how to play.
     *
     * @param event     the command event
     * @param character the newly created character
     * @param guildId   the guild ID
     */
    private void sendTutorialMessage(
            SlashCommandInteractionEvent event, RPGCharacter character, String guildId) {
        StringBuilder tutorial = new StringBuilder();
        tutorial.append("📚 **Welcome to Nilfheim! Here's how to get started:**\n\n");

        // Action commands explanation
        tutorial.append("**🎮 Core Actions:**\n");
        tutorial.append("Use `/rpg-action type:<action>` to perform actions:\n");
        tutorial.append("• **explore** - Discover the world, encounter events, gain XP\n");
        tutorial.append("• **train** - Improve stats and gain XP\n");
        tutorial.append("• **battle** - Fight enemies, gain XP (risk of damage)\n");
        tutorial.append("• **rest** - Fully restore HP\n\n");

        // Action charges
        int actionCharges = character.getActionCharges();
        int maxActionCharges = character.getMaxActionCharges();
        tutorial.append(
                String.format(
                        "**⚡ Action Charges:** You have **%d/%d** action charges.\n",
                        actionCharges, maxActionCharges));
        tutorial.append("Charges refresh every 12 hours. Use them wisely!\n\n");

        // Boss information
        BossService.ServerBossState bossState = bossService.getState(guildId);
        if (bossState != null) {
            Boss boss = bossState.getCurrentBoss();
            SuperBoss superBoss = bossState.getCurrentSuperBoss();

            if (boss != null || superBoss != null) {
                String bossName = boss != null ? boss.getName() : superBoss.getName();
                int currentHp = boss != null ? boss.getCurrentHp() : superBoss.getCurrentHp();
                int maxHp = boss != null ? boss.getMaxHp() : superBoss.getMaxHp();
                double hpPercent = (currentHp * 100.0) / maxHp;
                boolean isSuperBoss = superBoss != null;

                if (isSuperBoss) {
                    tutorial.append(
                            String.format(
                                    "**🔥 Active SUPER BOSS:** **%s** is currently in play with **%.0f%%** health.\n",
                                    bossName, hpPercent));
                    tutorial.append(
                            "(A Super Boss is much more difficult and replaces the normal boss for this cycle.)\n");
                } else {
                    tutorial.append(
                            String.format(
                                    "**🐲 Active Boss:** **%s** is currently in play with **%.0f%%** health.\n",
                                    bossName, hpPercent));
                }
                tutorial.append("Use `/rpg-boss-battle battle` to attack!\n\n");
            } else {
                tutorial.append(
                        "**🐲 Boss Status:** No boss is currently active. Bosses have a 24-hour window to defeat, then a 24-hour cooldown before the next spawn.\n\n");
            }
        } else {
            tutorial.append(
                    "**🐲 Boss Status:** No boss is currently active. Bosses have a 24-hour window to defeat, then a 24-hour cooldown before the next spawn.\n\n");
        }

        // Heroic charges explanation
        tutorial.append(
                "**⚔️ Heroic Charges:** You have **5 heroic charges** (separate from your daily action charges).\n");
        tutorial.append("These can only be used to attack bosses via `/rpg-boss-battle`.\n");
        tutorial.append("Heroic charges refresh to 5 when a new boss spawns.\n\n");

        // Boss battle explanation
        tutorial.append(
                "**🌍 Boss Battles:** Bosses are community-wide events where everyone fights together.\n");
        tutorial.append(
                "Bosses have a 24-hour window to defeat. If not defeated in time, world curses are applied.\n");
        tutorial.append(
                "The next boss spawns 48 hours after the previous one (24h livable + 24h cooldown).\n");
        tutorial.append("Work together to defeat bosses and avoid consequences!\n\n");

        tutorial.append(
                "**💡 Tip:** Use `/rpg-profile` to view your stats and `/rpg-leaderboard` to see top players!");

        event.getHook().sendMessage(tutorial.toString()).setEphemeral(true).queue();
    }

    @Override
    public String getCommandName() {
        return "rpg-register";
    }
}
