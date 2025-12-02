package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
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
import java.time.Instant;
import java.util.List;

/**
 * Command handler for /rpg-leaderboard.
 * Shows top characters by level and XP with Mafia Member status and pagination.
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGLeaderboardCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGLeaderboardCommand.class);
    private final CharacterService characterService;
    private static final int ENTRIES_PER_PAGE = 25;

    // MIKROS Mafia Server ID - Loaded from environment variable
    // Set MIKROS_MAFIA_GUILD_ID in .env file or environment variables
    private static final String MIKROS_MAFIA_GUILD_ID = System.getenv("MIKROS_MAFIA_GUILD_ID");

    /**
     * Creates a new RPGLeaderboardCommand handler.
     *
     * @param characterService the character service
     */
    public RPGLeaderboardCommand(CharacterService characterService) {
        this.characterService = characterService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-leaderboard", "View top RPG characters by level and XP")
                .addOption(OptionType.INTEGER, "page", "Page number (default: 1)", false);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Get page number (default: 1)
        OptionMapping pageOption = event.getOption("page");
        int page = (pageOption != null) ? (int) pageOption.getAsLong() : 1;

        if (page < 1) {
            event.reply("❌ Page number must be 1 or greater!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get all characters and calculate pagination
        List<RPGCharacter> allCharacters = characterService.getLeaderboard(Integer.MAX_VALUE);

        if (allCharacters.isEmpty()) {
            String message = """
                    ❌ No characters have been registered yet!
                    
                    Be the first to start your adventure with `/rpg-register`
                    """;

            event.reply(message).setEphemeral(true).queue();
            return;
        }

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) allCharacters.size() / ENTRIES_PER_PAGE);
        if (page > totalPages) {
            event.reply(String.format("❌ Page %d doesn't exist! There are only %d page(s).", page, totalPages))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get characters for this page
        int startIndex = (page - 1) * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, allCharacters.size());
        List<RPGCharacter> pageCharacters = allCharacters.subList(startIndex, endIndex);

        // Get MIKROS Mafia guild for member checking
        Guild mafiaGuild = null;
        if (MIKROS_MAFIA_GUILD_ID != null && !MIKROS_MAFIA_GUILD_ID.isBlank()) {
            mafiaGuild = event.getJDA().getGuildById(MIKROS_MAFIA_GUILD_ID);
            if (mafiaGuild == null) {
                logger.warn("MIKROS Mafia guild not found with ID: {}. Mafia member status will not be checked.", MIKROS_MAFIA_GUILD_ID);
            } else {
                logger.debug("MIKROS Mafia guild found: {} ({}). Checking member status for leaderboard.",
                        mafiaGuild.getName(), MIKROS_MAFIA_GUILD_ID);
            }
        } else {
            logger.debug("MIKROS_MAFIA_GUILD_ID not configured. Mafia member status will not be checked.");
        }

        // Build leaderboard embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 RPG Leaderboard - Top Adventurers");
        embed.setColor(new Color(255, 215, 0)); // Gold color
        embed.setDescription("The strongest characters across all servers");

        StringBuilder leaderboard = new StringBuilder();
        int rank = startIndex + 1;

        for (RPGCharacter character : pageCharacters) {
            String medal = getMedal(rank - 1);
            String classEmoji = character.getCharacterClass().getEmoji();

            // Check if user is in MIKROS Mafia
            String mafiaStatus = "❌ No";
            if (mafiaGuild != null) {
                try {
                    // This makes a Discord API call to check if the user is a member
                    // For 25 entries, this is 25 API calls per leaderboard view
                    // Discord rate limit: ~50 requests/second, so this should be fine for normal usage
                    Member member = mafiaGuild.retrieveMemberById(character.getDiscordId()).complete();
                    if (member != null) {
                        mafiaStatus = "✅ Yes";
                        logger.trace("User {} ({}) is a Mafia member", character.getDiscordId(), character.getName());
                    }
                } catch (Exception e) {
                    // Member not found or error retrieving - not in Mafia
                    // This is expected for most users, so we don't log it at info level
                    logger.trace("User {} ({}) is not a Mafia member or error checking: {}",
                            character.getDiscordId(), character.getName(), e.getMessage());
                }
            }

            leaderboard.append(String.format("""
                            %s **#%d** - %s **%s**
                            └ %s Level %d • %,d XP • HP: %d/%d
                            └ Mafia Member? %s
                            
                            """,
                    medal,
                    rank,
                    classEmoji,
                    character.getName(),
                    character.getCharacterClass().getDisplayName(),
                    character.getLevel(),
                    character.getXp(),
                    character.getStats().getCurrentHp(),
                    character.getStats().getMaxHp(),
                    mafiaStatus
            ));

            rank++;
        }

        embed.addField("Top Characters", leaderboard.toString(), false);

        // Pagination footer
        String footerText = buildFooterText(page, totalPages);
        embed.setFooter(footerText);
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();

        logger.debug("Leaderboard requested - showing page {} ({} characters)", page, pageCharacters.size());
    }

    private String buildFooterText(int page, int totalPages) {
        int totalCharacters = characterService.getCharacterCount();
        if (totalPages > 1) {
            return String.format(
                    "Page %d/%d • Total Characters: %d • Use /rpg-leaderboard page:%d for next page",
                    page,
                    totalPages,
                    totalCharacters,
                    page < totalPages ? page + 1 : page
            );
        } else {
            return String.format(
                    "Total Characters: %d • Join the adventure with /rpg-register",
                    totalCharacters
            );
        }
    }

    /**
     * Gets medal emoji for rank.
     */
    private String getMedal(int rank) {
        return switch (rank) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> "  ";
        };
    }

    @Override
    public String getCommandName() {
        return "rpg-leaderboard";
    }
}
