package com.tatumgames.mikros.honeypot.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.honeypot.service.HoneypotService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for managing honeypot system.
 * Subcommands: enable, disable, config
 */
public class HoneypotCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(HoneypotCommand.class);
    private final HoneypotService honeypotService;
    
    /**
     * Creates a new HoneypotCommand handler.
     * 
     * @param honeypotService the honeypot service
     */
    public HoneypotCommand(HoneypotService honeypotService) {
        this.honeypotService = honeypotService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("honeypot", "Manage honeypot system for spam detection")
                .addSubcommands(
                        new SubcommandData("enable", "Enable honeypot mode and create channel")
                                .addOption(OptionType.STRING, "channel_name", "Name for honeypot channel (default: honeypot)", false),
                        new SubcommandData("disable", "Disable honeypot mode")
                                .addOption(OptionType.BOOLEAN, "delete_channel", "Delete the honeypot channel (default: false)", false),
                        new SubcommandData("config", "View or modify honeypot configuration")
                                .addOption(OptionType.STRING, "setting", "Setting to modify (silent_mode, delete_days, channel_name)", false)
                                .addOption(OptionType.STRING, "value", "New value for the setting", false)
                )
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        String subcommand = event.getSubcommandName();
        
        switch (subcommand) {
            case "enable":
                handleEnable(event);
                break;
            case "disable":
                handleDisable(event);
                break;
            case "config":
                handleConfig(event);
                break;
            default:
                event.reply("❌ Unknown subcommand.").setEphemeral(true).queue();
        }
    }
    
    private void handleEnable(SlashCommandInteractionEvent event) {
        String channelName = event.getOption("channel_name") != null
                ? event.getOption("channel_name").getAsString()
                : "honeypot";
        
        // Validate channel name
        if (channelName.length() < 1 || channelName.length() > 100) {
            event.reply("❌ Channel name must be between 1 and 100 characters.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        event.deferReply().queue();
        
        TextChannel channel = honeypotService.enableHoneypot(event.getGuild(), channelName);
        
        if (channel != null) {
            event.getHook().sendMessage(String.format(
                    "✅ **Honeypot Mode Enabled**\n" +
                    "Channel: %s\n" +
                    "⚠️ Users who post in this channel will be automatically banned.",
                    channel.getAsMention()
            )).queue();
            logger.info("Honeypot enabled for guild {} with channel {}", event.getGuild().getId(), channelName);
        } else {
            event.getHook().sendMessage("❌ Failed to create honeypot channel. Check bot permissions.")
                    .setEphemeral(true)
                    .queue();
        }
    }
    
    private void handleDisable(SlashCommandInteractionEvent event) {
        boolean deleteChannel = event.getOption("delete_channel") != null
                && event.getOption("delete_channel").getAsBoolean();
        
        honeypotService.disableHoneypot(event.getGuild(), deleteChannel);
        
        String message = deleteChannel
                ? "✅ **Honeypot Mode Disabled**\nHoneypot channel has been deleted."
                : "✅ **Honeypot Mode Disabled**\nHoneypot channel remains but is inactive.";
        
        event.reply(message).setEphemeral(true).queue();
        logger.info("Honeypot disabled for guild {} (delete channel: {})", event.getGuild().getId(), deleteChannel);
    }
    
    private void handleConfig(SlashCommandInteractionEvent event) {
        var config = honeypotService.getConfig(event.getGuild().getId());
        
        // If no setting provided, show current config
        if (event.getOption("setting") == null) {
        net.dv8tion.jda.api.EmbedBuilder embed = new net.dv8tion.jda.api.EmbedBuilder()
                .setTitle("🍯 Honeypot Configuration")
                .setColor(0x5865F2) // Discord blurple color
                    .addField("Enabled", config.isEnabled() ? "✅ Yes" : "❌ No", true)
                    .addField("Channel Name", config.getChannelName(), true)
                    .addField("Channel", config.getChannelId() != null 
                            ? event.getGuild().getTextChannelById(config.getChannelId()) != null
                                    ? event.getGuild().getTextChannelById(config.getChannelId()).getAsMention()
                                    : "Not found"
                            : "Not created", true)
                    .addField("Silent Mode", config.isSilentMode() ? "✅ Yes (log only)" : "❌ No (auto-ban)", true)
                    .addField("Delete Days", config.getDeleteDays() == -1 ? "All" : String.valueOf(config.getDeleteDays()), true)
                    .addField("Alert Channel", config.getAlertChannelId() != null
                            ? event.getGuild().getTextChannelById(config.getAlertChannelId()) != null
                                    ? event.getGuild().getTextChannelById(config.getAlertChannelId()).getAsMention()
                                    : "Not found"
                            : "Not set", true);
            
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        
        // Modify setting
        String setting = event.getOption("setting").getAsString().toLowerCase();
        String value = event.getOption("value") != null ? event.getOption("value").getAsString() : null;
        
        switch (setting) {
            case "silent_mode":
                if (value == null) {
                    event.reply("❌ Please provide a value (true/false).").setEphemeral(true).queue();
                    return;
                }
                boolean silentMode = Boolean.parseBoolean(value);
                config.setSilentMode(silentMode);
                event.reply(String.format("✅ Silent mode set to: %s", silentMode ? "Enabled (log only)" : "Disabled (auto-ban)"))
                        .setEphemeral(true).queue();
                break;
                
            case "delete_days":
                if (value == null) {
                    event.reply("❌ Please provide a value (0-7, or -1 for all).").setEphemeral(true).queue();
                    return;
                }
                try {
                    int days = Integer.parseInt(value);
                    if (days < -1 || days > 7) {
                        event.reply("❌ Delete days must be between -1 (all) and 7.").setEphemeral(true).queue();
                        return;
                    }
                    config.setDeleteDays(days);
                    event.reply(String.format("✅ Delete days set to: %s", days == -1 ? "All" : String.valueOf(days)))
                            .setEphemeral(true).queue();
                } catch (NumberFormatException e) {
                    event.reply("❌ Invalid number format.").setEphemeral(true).queue();
                }
                break;
                
            case "channel_name":
                if (value == null) {
                    event.reply("❌ Please provide a channel name.").setEphemeral(true).queue();
                    return;
                }
                if (value.length() < 1 || value.length() > 100) {
                    event.reply("❌ Channel name must be between 1 and 100 characters.").setEphemeral(true).queue();
                    return;
                }
                config.setChannelName(value);
                event.reply(String.format("✅ Channel name set to: %s", value)).setEphemeral(true).queue();
                break;
                
            default:
                event.reply("❌ Unknown setting. Available: silent_mode, delete_days, channel_name")
                        .setEphemeral(true).queue();
        }
    }
    
    @Override
    public String getCommandName() {
        return "honeypot";
    }
}

