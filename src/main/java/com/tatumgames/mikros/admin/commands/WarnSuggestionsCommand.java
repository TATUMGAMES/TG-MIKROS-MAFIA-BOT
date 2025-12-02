package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.config.ModerationConfig;
import com.tatumgames.mikros.models.MessageSuggestion;
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
import java.util.List;

/**
 * Command handler for the /warn-suggestions command.
 * Analyzes recent messages and suggests users who may need warnings.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class WarnSuggestionsCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(WarnSuggestionsCommand.class);
    private final MessageAnalysisService messageAnalysisService;

    /**
     * Creates a new WarnSuggestionsCommand handler.
     *
     * @param messageAnalysisService the message analysis service
     */
    public WarnSuggestionsCommand(MessageAnalysisService messageAnalysisService) {
        this.messageAnalysisService = messageAnalysisService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("warn-suggestions", "Get suggestions for users who may need warnings")
                .addOption(OptionType.CHANNEL, "channel", "Specific channel to analyze (optional)", false)
                .addOption(OptionType.INTEGER, "limit", "Number of messages to analyze (default: 200)", false)
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member moderator = event.getMember();
        Guild guild = event.getGuild();

        if (moderator == null || guild == null ||
                !moderator.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Defer reply as this might take a moment
        event.deferReply().queue();

        // Get the channel option
        TextChannel channel = AdminUtils.getValidTextChannel(event, "channel");
        if (channel == null) return;

        // Get limit option
        OptionMapping limitOption = event.getOption("limit");
        int limit = (limitOption != null)
                ? limitOption.getAsInt()
                : ModerationConfig.MESSAGE_ANALYSIS_LIMIT;

        // Validate limit
        if (limit < 10 || limit > 500) {
            event.getHook().sendMessage("❌ Limit must be between 10 and 500.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        logger.info("Analyzing {} messages in channel {} for warn suggestions",
                limit, channel.getName());

        // Fetch and analyze messages
        channel.getHistory().retrievePast(limit).queue(
                messages -> {
                    List<MessageSuggestion> suggestions = messageAnalysisService.analyzeMessages(
                            messages, ModerationConfig.MAX_SUGGESTIONS);

                    if (suggestions.isEmpty()) {
                        event.getHook().sendMessage("✅ No concerning messages found!")
                                .queue();
                        return;
                    }

                    // Build response embed
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("⚠️ Warning Suggestions");
                    embed.setDescription(String.format(
                            "Analyzed %d messages in %s\nFound %d concerning messages",
                            messages.size(),
                            channel.getAsMention(),
                            suggestions.size()
                    ));
                    embed.setColor(Color.YELLOW);

                    // Add suggestions as fields
                    int count = 0;
                    for (MessageSuggestion suggestion : suggestions) {
                        if (count >= 10) { // Limit to 10 fields (embed limit is 25)
                            break;
                        }

                        String fieldValue = String.format("""
                                        **User:** <@%s>
                                        **Severity:** %s %s
                                        **Reason:** %s
                                        **Snippet:** `%s`
                                        M**[Jump to Message](%s)**
                                        """,
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

                    if (suggestions.size() > 10) {
                        embed.setFooter(String.format("Showing 10 of %d suggestions", suggestions.size()));
                    }

                    embed.setTimestamp(Instant.now());

                    event.getHook().sendMessageEmbeds(embed.build()).queue();

                    logger.info("Sent {} warning suggestions to moderator {} in guild {}",
                            suggestions.size(), moderator.getId(), guild.getId());
                },
                error -> {
                    event.getHook().sendMessage("❌ Failed to fetch messages: " + error.getMessage())
                            .setEphemeral(true)
                            .queue();
                    logger.error("Failed to fetch messages for warn suggestions", error);
                }
        );
    }

    @Override
    public String getCommandName() {
        return "warn-suggestions";
    }
}
