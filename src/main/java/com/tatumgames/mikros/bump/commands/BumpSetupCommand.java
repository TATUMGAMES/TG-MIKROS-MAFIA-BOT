package com.tatumgames.mikros.bump.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.bump.model.BumpConfig;
import com.tatumgames.mikros.bump.service.BumpService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

/**
 * Command handler for /admin-bump-setup.
 * Allows server administrators to set up automatic server bumping.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class BumpSetupCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(BumpSetupCommand.class);
    private final BumpService bumpService;
    
    /**
     * Creates a new BumpSetupCommand handler.
     *
     * @param bumpService the bump service
     */
    public BumpSetupCommand(BumpService bumpService) {
        this.bumpService = bumpService;
    }
    
    @Override
    public CommandData getCommandData() {
        OptionData botsOption = new OptionData(OptionType.STRING, "bots", "Which bots to bump", true);
        botsOption.addChoice("Disboard only", "disboard");
        botsOption.addChoice("Disurl only", "disurl");
        botsOption.addChoice("Both", "both");
        
        return Commands.slash("admin-bump-setup", "Set up automatic server bumping (admin only)")
                .addOption(OptionType.CHANNEL, "channel", "The channel to send bump commands in", true)
                .addOptions(botsOption)
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
        
        // Get the channel option
        MessageChannel channel = AdminUtils.getValidTextChannel(event, "channel");
        if (channel == null) return;
        
        // Get the bots option
        String botsValue = event.getOption("bots", OptionMapping::getAsString);
        if (botsValue == null) {
            event.reply("❌ You must select which bots to bump.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Parse bots selection
        EnumSet<BumpConfig.BumpBot> enabledBots = parseBotsSelection(botsValue);
        if (enabledBots.isEmpty()) {
            event.reply("❌ Invalid bot selection.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Save the configuration
        String guildId = guild.getId();
        String channelId = channel.getId();
        
        bumpService.setBumpChannel(guildId, channelId);
        bumpService.setEnabledBots(guildId, enabledBots);
        
        // Build bot list string
        String botsList = String.join(", ", enabledBots.stream()
                .map(BumpConfig.BumpBot::getDisplayName)
                .toList());
        
        // Send confirmation
        event.reply(String.format(
                "✅ **Auto-Bump Configured**\n\n" +
                        "Bump channel: %s\n" +
                        "Enabled bots: %s\n" +
                        "Default interval: **4 hours**\n\n" +
                        "**Next Steps:**\n" +
                        "• Use `/admin-bump-config set-interval` to change the bump interval (1-24 hours)\n" +
                        "• Use `/admin-bump-config view` to see current settings\n\n" +
                        "The bot will automatically bump your server at the configured interval!",
                channel.getAsMention(),
                botsList
        )).queue();
        
        logger.info("Bump setup completed for guild {} by user {}: channel={}, bots={}",
                guildId, member.getId(), channelId, enabledBots);
    }
    
    /**
     * Parses the bots selection string into an EnumSet.
     *
     * @param botsValue the selection value ("disboard", "disurl", or "both")
     * @return the set of enabled bots
     */
    private EnumSet<BumpConfig.BumpBot> parseBotsSelection(String botsValue) {
        return switch (botsValue.toLowerCase()) {
            case "disboard" -> EnumSet.of(BumpConfig.BumpBot.DISBOARD);
            case "disurl" -> EnumSet.of(BumpConfig.BumpBot.DISURL);
            case "both" -> EnumSet.of(BumpConfig.BumpBot.DISBOARD, BumpConfig.BumpBot.DISURL);
            default -> EnumSet.noneOf(BumpConfig.BumpBot.class);
        };
    }
    
    @Override
    public String getCommandName() {
        return "admin-bump-setup";
    }
}

