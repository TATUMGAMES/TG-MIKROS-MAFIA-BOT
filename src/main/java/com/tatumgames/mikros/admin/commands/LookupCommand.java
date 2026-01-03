package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.config.ConfigLoader;
import com.tatumgames.mikros.models.api.GetUserScoreDetailResponse;
import com.tatumgames.mikros.models.api.TrackPlayerRatingRequest;
import com.tatumgames.mikros.services.ReputationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command handler for the /lookup command.
 * Allows admins to lookup user reputation scores by username.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class LookupCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(LookupCommand.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ReputationService reputationService;
    private final ConfigLoader configLoader;

    /**
     * Creates a new LookupCommand handler.
     *
     * @param reputationService the reputation service
     * @param configLoader      the configuration loader
     */
    public LookupCommand(ReputationService reputationService, ConfigLoader configLoader) {
        this.reputationService = reputationService;
        this.configLoader = configLoader;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("lookup", "Lookup user reputation scores by username (Admin only)")
                .addOption(OptionType.STRING, "usernames", "Comma or space-separated list of usernames to lookup", true)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        Guild guild = event.getGuild();
        if (member == null || guild == null ||
                !member.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get usernames from command option
        String usernamesInput = event.getOption("usernames", OptionMapping::getAsString);
        if (usernamesInput == null || usernamesInput.isBlank()) {
            event.reply("❌ Please provide at least one username to lookup.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Parse usernames (support both comma and space separation)
        List<String> usernames = parseUsernames(usernamesInput);
        if (usernames.isEmpty()) {
            event.reply("❌ No valid usernames found. Please provide comma or space-separated usernames.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Call API to get user score details
        GetUserScoreDetailResponse response = reputationService.getUserScoreDetail(usernames);

        if (response == null || response.getData() == null) {
            event.reply("❌ Failed to retrieve user score details. Please try again later.")
                    .setEphemeral(true)
                    .queue();
            logger.error("Failed to get user score details for usernames: {}", usernames);
            return;
        }

        // Build embed response
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔍 Reputation Score Lookup");
        embed.setColor(Color.BLUE);
        embed.setDescription(String.format("Results for **%d** user(s)", usernames.size()));

        // List of scores (data is now a direct array, not data.scores)
        List<GetUserScoreDetailResponse.UserScore> scores = response.getData();

        if (scores.isEmpty()) {
            embed.addField("⚠️ No Results",
                    "No users found with the provided usernames:\n" +
                            String.join(", ", usernames),
                    false);
        } else {
            // Add field for each user found
            for (GetUserScoreDetailResponse.UserScore score : scores) {
                String fieldValue = String.format(
                        "**Username:** `%s`\n**Reputation Score:** `%s`\n",
                        score.getUsername(),
                        score.getReputationScore()
                );
                embed.addField("👤 " + score.getUsername(), fieldValue, false);
            }

            // Check for usernames not found
            List<String> foundUsernames = scores.stream()
                    .map(GetUserScoreDetailResponse.UserScore::getUsername)
                    .toList();

            List<String> notFound = usernames.stream()
                    .filter(username -> !foundUsernames.contains(username))
                    .toList();

            // Auto-create reputation entries for not-found users
            if (!notFound.isEmpty()) {
                List<String> createdUsernames = new ArrayList<>();
                User adminUser = event.getUser();
                Member adminMember = event.getMember();

                for (String username : notFound) {
                    try {
                        // Create TrackPlayerRatingRequest for auto-creation
                        TrackPlayerRatingRequest request = new TrackPlayerRatingRequest();
                        request.setTimestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER));
                        request.setPlatform("discord");
                        request.setApiKeyType(configLoader.getApiKeyType());

                        // Set sender (admin who executed /lookup)
                        TrackPlayerRatingRequest.Sender sender = new TrackPlayerRatingRequest.Sender();
                        sender.setDiscordUserId(adminUser.getId());
                        sender.setDiscordUsername(adminMember != null ? adminMember.getEffectiveName() : adminUser.getName());
                        request.setSender(sender);

                        // Set participant (the not-found username)
                        TrackPlayerRatingRequest.Participant participant = new TrackPlayerRatingRequest.Participant();
                        participant.setDiscordUsername(username);
                        // Try to find Discord user ID if possible (for now, just use username)
                        participant.setDiscordUserId(""); // Backend will handle this
                        participant.setValue(1); // As specified by user
                        request.setParticipants(List.of(participant));

                        // Call trackPlayerRating API to create entry
                        boolean success = reputationService.trackPlayerRating(request);
                        if (success) {
                            createdUsernames.add(username);
                            logger.info("Auto-created reputation entry for username: {} by admin: {}",
                                    username, adminUser.getId());
                        } else {
                            logger.warn("Failed to auto-create reputation entry for username: {}", username);
                        }
                    } catch (Exception e) {
                        logger.error("Error auto-creating reputation entry for username: {}", username, e);
                    }
                }

                // Update embed to show created users with score = 10
                if (!createdUsernames.isEmpty()) {
                    StringBuilder createdField = new StringBuilder();
                    for (String createdUsername : createdUsernames) {
                        createdField.append("**").append(createdUsername).append("** - Reputation Score: `10`\n");
                    }
                    embed.addField("✅ Auto-Created",
                            "The following users were automatically created with initial reputation score of 10:\n" +
                                    createdField,
                            false);
                }

                // Show remaining not-found users (if any failed to create)
                List<String> stillNotFound = notFound.stream()
                        .filter(username -> !createdUsernames.contains(username))
                        .toList();

                if (!stillNotFound.isEmpty()) {
                    embed.addField("⚠️ Not Found",
                            "The following usernames were not found and could not be created:\n" +
                                    String.join(", ", stillNotFound),
                            false);
                }
            }
        }
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();

        logger.info("User score lookup performed by {} for usernames: {} in guild {}",
                member.getId(), usernames, guild.getId());
    }

    /**
     * Parses usernames from input string, supporting both comma and space separation.
     *
     * @param input the input string containing usernames
     * @return list of parsed usernames
     */
    private List<String> parseUsernames(String input) {
        if (input == null || input.isBlank()) {
            return new ArrayList<>();
        }

        // Try comma separation first
        if (input.contains(",")) {
            return Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        }

        // Otherwise, use space separation
        return Arrays.stream(input.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    @Override
    public String getCommandName() {
        return "lookup";
    }
}
