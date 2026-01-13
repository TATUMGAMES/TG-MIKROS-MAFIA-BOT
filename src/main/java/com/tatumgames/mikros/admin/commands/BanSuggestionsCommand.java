package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.config.ModerationConfig;
import com.tatumgames.mikros.models.MessageSuggestion;
import com.tatumgames.mikros.models.SuggestionSeverity;
import com.tatumgames.mikros.services.MessageAnalysisService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Command handler for the /ban-suggestions command.
 * Analyzes recent messages with stricter rules for ban-worthy content.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BanSuggestionsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(BanSuggestionsCommand.class);
    private static final Set<SuggestionSeverity> BAN_SEVERITIES =
            EnumSet.of(SuggestionSeverity.HIGH, SuggestionSeverity.CRITICAL);
    private final MessageAnalysisService messageAnalysisService;

    /**
     * Creates a new BanSuggestionsCommand handler.
     *
     * @param messageAnalysisService the message analysis service
     */
    public BanSuggestionsCommand(MessageAnalysisService messageAnalysisService) {
        this.messageAnalysisService = messageAnalysisService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("ban-suggestions", "Get suggestions for users who may need bans")
                .addOption(OptionType.CHANNEL, "channel", "Specific channel to analyze (optional)", false)
                .addOption(OptionType.INTEGER, "limit", "Number of messages to analyze (default: 200)", false)
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Defer reply as this might take a moment
        event.deferReply().queue();

        // Get options
        TextChannel targetChannel =
                event.getOption("channel",
                        event.getChannel().asTextChannel(),
                        optionMappingEvent -> optionMappingEvent.getAsChannel().asTextChannel()
                );
        int limit = event.getOption(
                "limit",
                ModerationConfig.MESSAGE_ANALYSIS_LIMIT,
                OptionMapping::getAsInt
        );

        // Validate limit
        if (limit < 10 || limit > 500) {
            event.getHook().sendMessage("❌ Limit must be between 10 and 500.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        logger.info("Analyzing {} messages in channel {} for ban suggestions",
                limit, targetChannel.getName());

        // Fetch and analyze messages
        targetChannel.getHistory().retrievePast(limit).queue(
                messages -> {
                    List<MessageSuggestion> allSuggestions = messageAnalysisService.analyzeMessages(
                            messages, limit);

                    // Filter for HIGH and CRITICAL severity only
                    List<MessageSuggestion> banSuggestions = allSuggestions.stream()
                            .filter(s -> BAN_SEVERITIES.contains(s.severity()))
                            .limit(ModerationConfig.MAX_SUGGESTIONS)
                            .toList();

                    if (banSuggestions.isEmpty()) {
                        event.getHook().sendMessage("✅ No severe violations found!")
                                .queue();
                        return;
                    }

                    // Build response embed
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("🔴 Ban Suggestions - CRITICAL REVIEW REQUIRED");
                    embed.setDescription(String.format(
                            "Analyzed %d messages in %s\nFound %d severe violations requiring review",
                            messages.size(),
                            targetChannel.getAsMention(),
                            banSuggestions.size()
                    ));
                    embed.setColor(Color.RED);

                    // Add suggestions as fields
                    int count = 0;
                    for (MessageSuggestion suggestion : banSuggestions) {
                        if (count >= 10) { // Limit to 10 fields
                            break;
                        }

                        String fieldValue = String.format(
                                "**User:** <@%s>\n" +
                                        "**Severity:** %s %s\n" +
                                        "**Reason:** %s\n" +
                                        "**Snippet:** ||%s||\n" +  // Spoiler tag for potentially offensive content
                                        "**[Jump to Message](%s)**\n\n" +
                                        "⚠️ **Manual review required before taking action**",
                                suggestion.userId(),
                                suggestion.severity().getEmoji(),
                                suggestion.severity().getLabel(),
                                suggestion.reason(),
                                suggestion.snippet(),
                                suggestion.messageLink()
                        );

                        embed.addField(
                                String.format("#%d - %s", count + 1, suggestion.username()),
                                fieldValue,
                                false
                        );
                        count++;
                    }

                    if (banSuggestions.size() > 10) {
                        embed.setFooter(String.format("Showing 10 of %d suggestions", banSuggestions.size()));
                    }

                    embed.setTimestamp(Instant.now());

                    event.getHook().sendMessageEmbeds(embed.build()).queue();

                    logger.info("Sent {} ban suggestions to moderator {} in guild {}",
                            banSuggestions.size(), member.getId(), guild.getId());
                },
                error -> {
                    event.getHook().sendMessage("❌ Failed to fetch messages: " + error.getMessage())
                            .setEphemeral(true)
                            .queue();
                    logger.error("Failed to fetch messages for ban suggestions", error);
                }
        );
    }

    @Override
    public String getCommandName() {
        return "ban-suggestions";
    }
}

