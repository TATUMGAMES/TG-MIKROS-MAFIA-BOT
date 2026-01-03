package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.achievements.AchievementType;
import com.tatumgames.mikros.games.rpg.actions.CharacterAction;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.curse.WorldCurse;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.AchievementService;
import com.tatumgames.mikros.games.rpg.service.ActionService;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import com.tatumgames.mikros.games.rpg.service.WorldCurseService;
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
import java.time.Duration;
import java.time.Instant;

/**
 * Command handler for /rpg-action.
 * Allows players to perform daily actions (explore, train, battle).
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGActionCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGActionCommand.class);
    private final CharacterService characterService;
    private final ActionService actionService;
    private final AchievementService achievementService;
    private final WorldCurseService worldCurseService;

    /**
     * Creates a new RPGActionCommand handler.
     *
     * @param characterService   the character service
     * @param actionService      the action service
     * @param achievementService the achievement service for checking first-to achievements
     * @param worldCurseService  the world curse service for tracking cursed world participation
     */
    public RPGActionCommand(CharacterService characterService, ActionService actionService, AchievementService achievementService, WorldCurseService worldCurseService) {
        this.characterService = characterService;
        this.actionService = actionService;
        this.achievementService = achievementService;
        this.worldCurseService = worldCurseService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-action", "Perform an action with your character (3 charges, refresh every 12h)")
                .addOption(OptionType.STRING, "type", "Action type (explore, train, battle, rest, donate)", true);
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

        // Check if user has a character
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

        // Check if in correct channel (if specified)
        if (config.getRpgChannelId() != null) {
            if (!event.getChannel().getId().equals(config.getRpgChannelId())) {
                event.reply(String.format(
                        "❌ RPG commands must be used in <#%s>",
                        config.getRpgChannelId()
                )).setEphemeral(true).queue();
                return;
            }
        }

        // Check death/recovery status
        character.checkRecovery(); // Auto-update recovery status
        if (character.isDead()) {
            event.reply("💀 **You are dead!** A Priest can resurrect you with `/rpg-resurrect`.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (character.isRecovering()) {
            long secondsRemaining = character.getRecoverUntil().getEpochSecond() - java.time.Instant.now().getEpochSecond();
            Duration duration = Duration.ofSeconds(Math.max(0, secondsRemaining));
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();

            event.reply(String.format("""
                            ⛔ **You are in Recovery**
                            
                            Recovery time remaining: **%dh %dm**
                            
                            You cannot take actions during recovery. A Priest can resurrect you to start recovery.
                            """, hours, minutes))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check action charges
        int refreshHours = config.getChargeRefreshHours();
        if (!character.canPerformAction(refreshHours)) {
            long secondsRemaining = character.getSecondsUntilChargeRefresh(refreshHours);
            Duration duration = Duration.ofSeconds(secondsRemaining);
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();

            event.reply(String.format("""
                            ⏳ **No Action Charges Available**
                            
                            Charges remaining: **%d/%d**
                            Next charge refresh in: **%dh %dm**
                            
                            Use this time to check `/rpg-profile` or `/rpg-leaderboard`
                            """,
                    character.getActionCharges(),
                    character.getMaxActionCharges(),
                    hours, minutes
            )).setEphemeral(true).queue();
            return;
        }

        // Get action type
        OptionMapping typeOption = event.getOption("type");
        if (typeOption == null) {
            event.reply("❌ You must specify a type.").setEphemeral(true).queue();
            return;
        }

        String actionType = typeOption.getAsString().toLowerCase();

        // Validate action
        if (!actionService.hasAction(actionType)) {
            event.reply("❌ Invalid action! Choose from: **explore**, **train**, **battle**, **rest**, or **donate**")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Track cursed world participation (Cursewalker and Bound to Death titles)
        var activeCurses = worldCurseService.getActiveCurses(config.getGuildId());
        boolean hasMinor = activeCurses.stream().anyMatch(c -> c.getType() == WorldCurse.CurseType.MINOR);
        boolean hasMajor = activeCurses.stream().anyMatch(c -> c.getType() == WorldCurse.CurseType.MAJOR);
        if (hasMinor && hasMajor) {
            character.setActedDuringBothCurses(true);
        }
        // Bound to Death: Necromancer active during March of the Dead
        if (activeCurses.contains(WorldCurse.MAJOR_MARCH_OF_THE_DEAD) &&
                character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.NECROMANCER) {
            character.addStoryFlag("Bound to Death"); // Track via story flag for now
        }

        // Execute action
        try {
            RPGActionOutcome outcome = actionService.executeAction(actionType, character, config);
            CharacterAction action = actionService.getAction(actionType);

            // Build result embed
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(String.format(
                    "%s %s - Action Complete!",
                    action.getActionEmoji(),
                    capitalize(actionType)
            ));

            embed.setColor(outcome.success() ? Color.GREEN : Color.ORANGE);

            // Narrative
            embed.setDescription(outcome.narrative());

            // Results
            StringBuilder results = new StringBuilder();
            results.append(String.format("✨ **+%d XP**", outcome.xpGained()));

            if (outcome.statIncreased() != null) {
                results.append(String.format("\n💪 **+%d %s**",
                        outcome.statAmount(),
                        outcome.statIncreased()));
            }

            if (outcome.damageTaken() > 0) {
                results.append(String.format("\n💔 **-%d HP**", outcome.damageTaken()));
            }

            if (outcome.hpRestored() > 0) {
                results.append(String.format("\n💚 **+%d HP Restored**", outcome.hpRestored()));
            }

            // Display concise kill count for battle victories
            if (actionType.equals("battle") && outcome.success()) {
                results.append(String.format("\n💀 Enemies Defeated: %d", character.getEnemiesKilled()));
            }

            if (character.isDead()) {
                results.append("\n\n💀 **YOU HAVE DIED!** A Priest can resurrect you.");
            }

            if (outcome.leveledUp()) {
                int newLevel = character.getLevel();
                results.append(String.format("\n\n🎉 **LEVEL UP!** You are now Level %d!",
                        newLevel));

                // Check for first-to level achievements
                checkLevelAchievements(guildId, userId, newLevel);
            }

            embed.addField("📊 Results", results.toString(), false);

            // Display item drops inline
            if (!outcome.itemDrops().isEmpty() || !outcome.catalystDrops().isEmpty()) {
                StringBuilder loot = new StringBuilder();
                for (com.tatumgames.mikros.games.rpg.model.ItemDrop drop : outcome.itemDrops()) {
                    loot.append(String.format("%s %s ×%d\n",
                            drop.essence().getEmoji(),
                            drop.essence().getDisplayName(),
                            drop.count()));
                }
                for (com.tatumgames.mikros.games.rpg.model.CatalystDrop drop : outcome.catalystDrops()) {
                    loot.append(String.format("%s %s ×%d\n",
                            drop.catalyst().getEmoji(),
                            drop.catalyst().getDisplayName(),
                            drop.count()));
                }
                embed.addField("💎 Loot Found", loot.toString().trim(), false);
            }

            // Current stats
            embed.addField(
                    "Character Status",
                    String.format(
                            "**Level %d** • %d/%d XP\n" +
                                    "❤️ HP: %d/%d",
                            character.getLevel(),
                            character.getXp(),
                            character.getXpToNextLevel(),
                            character.getStats().getCurrentHp(),
                            character.getStats().getMaxHp()
                    ),
                    false
            );

            embed.setFooter(String.format(
                    "Action Charges: %d/%d • Next refresh in %d hours",
                    character.getActionCharges(),
                    character.getMaxActionCharges(),
                    refreshHours
            ));
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).queue();

            logger.info("User {} performed action {} with character {} - XP: +{}, Level: {}",
                    userId, actionType, character.getName(), outcome.xpGained(), character.getLevel());

        } catch (Exception e) {
            logger.error("Error executing action {} for user {}", actionType, userId, e);
            event.reply("❌ An error occurred while performing the action. Please try again.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Checks for first-to level achievements.
     *
     * @param guildId the guild ID
     * @param userId  the user ID
     * @param level   the new level
     */
    private void checkLevelAchievements(String guildId, String userId, int level) {
        AchievementType achievementType = null;

        if (level == 20) {
            achievementType = AchievementType.FIRST_TO_LEVEL_20;
        } else if (level == 30) {
            achievementType = AchievementType.FIRST_TO_LEVEL_30;
        } else if (level == 50) {
            achievementType = AchievementType.FIRST_TO_LEVEL_50;
        }

        if (achievementType != null) {
            boolean claimed = achievementService.checkAndClaimFirstTo(guildId, achievementType, userId);
            if (claimed) {
                logger.info("User {} claimed first-to achievement {} at level {} in guild {}",
                        userId, achievementType, level, guildId);
                // TODO: Announce achievement (will be handled by AchievementAnnouncementService)
            }
        }
    }

    @Override
    public String getCommandName() {
        return "rpg-action";
    }
}

