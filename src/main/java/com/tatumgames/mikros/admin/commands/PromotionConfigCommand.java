package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.models.PromotionVerbosity;
import com.tatumgames.mikros.services.GamePromotionService;
import com.tatumgames.mikros.services.scheduler.GamePromotionScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Command handler for /admin-promotion-config.
 * Allows administrators to configure game promotion settings for their server.
 */
@SuppressWarnings("ClassCanBeRecord")
public class PromotionConfigCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(PromotionConfigCommand.class);
    private final GamePromotionService gamePromotionService;
    private final GamePromotionScheduler gamePromotionScheduler;

    /**
     * Creates a new PromotionConfigCommand handler.
     *
     * @param gamePromotionService   the game promotion service
     * @param gamePromotionScheduler the game promotion scheduler
     */
    public PromotionConfigCommand(GamePromotionService gamePromotionService,
                                  GamePromotionScheduler gamePromotionScheduler) {
        this.gamePromotionService = gamePromotionService;
        this.gamePromotionScheduler = gamePromotionScheduler;
    }

    @Override
    public CommandData getCommandData() {
        OptionData verbosityOption = new OptionData(OptionType.STRING, "level", "Promotion frequency level", true);
        for (PromotionVerbosity verbosity : PromotionVerbosity.values()) {
            verbosityOption.addChoice(
                    verbosity.getLabel() + " (every " + verbosity.getHoursInterval() + "h)",
                    verbosity.name()
            );
        }

        return Commands.slash("admin-promotion-config", "Configure game promotion settings (admin only)")
                .addSubcommands(
                        new SubcommandData("view", "View current promotion configuration"),
                        new SubcommandData("update-channel", "Update the promotion channel (requires setup first)")
                                .addOption(OptionType.CHANNEL, "channel", "New promotion channel", true),
                        new SubcommandData("set-verbosity", "Set promotion frequency")
                                .addOptions(verbosityOption),
                        new SubcommandData("disable", "Disable game promotions for this server"),
                        new SubcommandData("force-check", "Manually trigger a game promotion check")
                )
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an administrator to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("❌ Please specify a subcommand.").setEphemeral(true).queue();
            return;
        }

        // Get guild id
        String guildId = guild.getId();

        switch (subcommand) {
            case "view" -> handleView(event, guildId);
            case "update-channel" -> handleUpdateChannel(event, guildId);
            case "set-verbosity" -> handleSetVerbosity(event, guildId);
            case "disable" -> handleDisable(event, guildId);
            case "force-check" -> handleForceCheck(event, guild);
            default -> event.reply("❌ Unknown subcommand.").setEphemeral(true).queue();
        }
    }

    private void handleView(SlashCommandInteractionEvent event, String guildId) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚙️ Promotion Configuration");
        embed.setColor(Color.CYAN);

        String channelId = gamePromotionService.getPromotionChannel(guildId);
        PromotionVerbosity verbosity = gamePromotionService.getPromotionVerbosity(guildId);

        // Status field
        embed.addField(
                "Status",
                channelId != null ? "✅ Enabled" : "❌ Not configured",
                true
        );

        // Promotion channel field
        embed.addField(
                "Promotion Channel",
                channelId != null ? "<#" + channelId + ">" : "❌ Not configured",
                true
        );

        // Verbosity field
        embed.addField(
                "Verbosity",
                verbosity.getLabel() + " (every " + verbosity.getHoursInterval() + "h)",
                true
        );

        // Promotions active field
        embed.addField(
                "Promotions Active",
                channelId != null ? "✅ Yes" : "❌ No",
                true
        );

        // Last check field (if channel configured)
        if (channelId != null) {
            // Get last check time from scheduler if available
            // For now, show "Active" - can be enhanced later to get actual timestamp
            embed.addField(
                    "Last Check",
                    "Active",
                    true
            );
        }

        embed.setTimestamp(java.time.Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleUpdateChannel(SlashCommandInteractionEvent event, String guildId) {
        // Check if promotion system was set up first (channel must be set)
        if (gamePromotionService.getPromotionChannel(guildId) == null) {
            event.reply("❌ Promotion system not set up yet. Use `/admin-promotion-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get the channel option
        MessageChannel channel = AdminUtils.getValidTextChannel(event, "channel");
        if (channel == null) return;

        // Validate bot can post in channel
        if (!channel.canTalk()) {
            event.reply("❌ I don't have permission to send messages in " + channel.getAsMention() + ".")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Update channel
        gamePromotionService.setPromotionChannel(guildId, channel.getId());

        event.reply(String.format("✅ Promotion channel updated to %s", channel.getAsMention())).queue();
        logger.info("Promotion channel updated to {} for guild {}", channel.getId(), guildId);
    }

    private void handleSetVerbosity(SlashCommandInteractionEvent event, String guildId) {
        // Check if promotion channel is set up
        if (gamePromotionService.getPromotionChannel(guildId) == null) {
            String message = """
                    ⚠️ **Promotion channel not configured**
                    
                    Please use `/admin-promotion-setup` first to designate a channel for promotions.
                    """;
            event.reply(message)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get the verbosity option
        String verbosityName = event.getOption("level", OptionMapping::getAsString);

        PromotionVerbosity verbosity;
        try {
            verbosity = PromotionVerbosity.valueOf(verbosityName);
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid verbosity level.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Save the configuration
        gamePromotionService.setPromotionVerbosity(guildId, verbosity);

        // Send confirmation
        String message = String.format("""
                        ✅ **Promotion Frequency Updated**
                        
                        **Level:** %s
                        **Frequency:** Every %d hours
                        
                        The bot will check for new promotions at this interval.
                        """,
                verbosity.getLabel(),
                verbosity.getHoursInterval()
        );
        event.reply(message).queue();

        logger.info("Promotion verbosity set to {} for guild {} by user {}",
                verbosity, guildId, event.getUser().getId());
    }

    private void handleDisable(SlashCommandInteractionEvent event, String guildId) {
        // Check if promotions are already disabled
        if (gamePromotionService.getPromotionChannel(guildId) == null) {
            String message = """
                    ℹ️ **Promotions Already Disabled**
                    
                    Game promotions are not currently enabled for this server.
                    
                    To enable promotions, use `/admin-promotion-setup`.
                    """;

            event.reply(message)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Clear all promotion data for this guild
        if (gamePromotionService instanceof com.tatumgames.mikros.services.InMemoryGamePromotionService) {
            ((com.tatumgames.mikros.services.InMemoryGamePromotionService) gamePromotionService)
                    .clearGuildData(guildId);
        } else {
            // Fallback: just clear the channel
            // In a database implementation, this would clear all related data
            gamePromotionService.setPromotionChannel(guildId, null);
        }

        // Send confirmation
        String message = """
                ✅ **Game Promotions Disabled**
                
                Game promotions have been disabled for this server.
                
                **What was removed:**
                • Promotion channel configuration
                • Promotion verbosity settings
                • All promotion tracking data
                
                To re-enable promotions, use `/admin-promotion-setup`.
                """;

        event.reply(message)
                .queue();

        logger.info("Promotions disabled for guild {} by user {}", guildId, event.getUser().getId());
    }

    private void handleForceCheck(SlashCommandInteractionEvent event, Guild guild) {
        // Check if promotion channel is configured
        if (gamePromotionService.getPromotionChannel(guild.getId()) == null) {
            String message = """
                    ⚠️ **Promotion channel not configured**
                    
                    Please use `/admin-promotion-setup` first to designate a channel for promotions.
                    """;

            event.reply(message)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Defer reply as this might take a moment
        event.deferReply().queue();

        // Trigger promotion check
        int promotionsPosted = gamePromotionScheduler.forceCheckGuild(guild);

        // Send result
        String message = buildPromotionMessage(promotionsPosted);
        event.getHook().sendMessage(message).queue();

        logger.info("Forced promotion check for guild {} by user {}, posted {} promotions",
                guild.getId(), event.getUser().getId(), promotionsPosted);
    }

    /**
     * Builds a user-facing status message describing the result of the promotion
     * posting process. If one or more promotions were posted, the message includes
     * the count. If no promotions were available, the message explains the possible
     * reasons and informs the user that the scheduler will continue checking.
     *
     * @param promotionsPosted the number of promotions successfully posted
     * @return a formatted, multi-line message appropriate to the posting result
     */
    private String buildPromotionMessage(int promotionsPosted) {
        if (promotionsPosted > 0) {
            return String.format("""
                    ✅ **Promotion Check Complete**
                    
                    Posted %d game promotion(s) to the configured channel.
                    
                    Check your promotion channel to see the new posts!
                    """, promotionsPosted);
        } else {
            return """
                    **No Promotions Available**
                    
                    There are currently no new game promotions to post.
                    
                    Possible reasons:
                    • All available promotions have already been posted
                    • No promotions have passed their deadline yet
                    • The promotion API is not yet integrated
                    
                    The scheduler will continue checking automatically.
                    """;
        }
    }

    @Override
    public String getCommandName() {
        return "admin-promotion-config";
    }
}

