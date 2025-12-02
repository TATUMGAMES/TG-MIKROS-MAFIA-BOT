package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.models.api.GetUserScoreDetailResponse;
import com.tatumgames.mikros.services.ReputationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for the /lookup command.
 * Allows admins to lookup user reputation scores by username.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class LookupCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(LookupCommand.class);
    private final ReputationService reputationService;

    /**
     * Creates a new LookupCommand handler.
     *
     * @param reputationService the reputation service
     */
    public LookupCommand(ReputationService reputationService) {
        this.reputationService = reputationService;
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

        if (response == null || response.getData() == null || response.getData().getScores() == null) {
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
        // List of scores
        List<GetUserScoreDetailResponse.UserScore> scores = response.getData().getScores();

        if (scores.isEmpty()) {
            embed.addField("⚠️ No Results",
                    "No users found with the provided usernames:\n" +
                            String.join(", ", usernames),
                    false);
        } else {
            // Add field for each user found
            for (GetUserScoreDetailResponse.UserScore score : scores) {
                StringBuilder fieldValue = new StringBuilder();
                fieldValue.append("**Discord ID:** `").append(score.getDiscordUserId()).append("`\n");
                fieldValue.append("**Reputation Score:** `").append(score.getReputationScore()).append("`\n");

                if (score.getEmail() != null && !score.getEmail().isBlank()) {
                    fieldValue.append("**Email:** `").append(score.getEmail()).append("`\n");
                }

                if (score.getDiscordServers() != null && !score.getDiscordServers().isEmpty()) {
                    String serversList = score.getDiscordServers().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                    fieldValue.append("**Servers:** `").append(serversList).append("`\n");
                }

                embed.addField("👤 " + score.getDiscordUsername(), fieldValue.toString(), false);
            }

            // Check for usernames not found
            List<String> foundUsernames = scores.stream()
                    .map(GetUserScoreDetailResponse.UserScore::getDiscordUsername)
                    .toList();

            List<String> notFound = usernames.stream()
                    .filter(username -> !foundUsernames.contains(username))
                    .collect(Collectors.toList());

            if (!notFound.isEmpty()) {
                embed.addField("⚠️ Not Found",
                        "The following usernames were not found in the system:\n" +
                                String.join(", ", notFound),
                        false);
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
                    .collect(Collectors.toList());
        }

        // Otherwise, use space separation
        return Arrays.stream(input.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    @Override
    public String getCommandName() {
        return "lookup";
    }
}
