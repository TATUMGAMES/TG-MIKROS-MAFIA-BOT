package com.tatumgames.mikros.commands;

import com.tatumgames.mikros.services.GamePromotionService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /setup-promotion-channel.
 * Allows server administrators to designate a channel for game promotions.
 */
public class SetupPromotionChannelCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetupPromotionChannelCommand.class);
    private final GamePromotionService gamePromotionService;
    
    /**
     * Creates a new SetupPromotionChannelCommand handler.
     * 
     * @param gamePromotionService the game promotion service
     */
    public SetupPromotionChannelCommand(GamePromotionService gamePromotionService) {
        this.gamePromotionService = gamePromotionService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-setup-promotion-channel", "Set the channel for game promotion posts")
                .addOption(OptionType.CHANNEL, "channel", "The channel to post promotions in", true)
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an administrator to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get the channel option
        TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
        
        // Validate that bot can post in the channel
        if (!channel.canTalk()) {
            event.reply("❌ I don't have permission to send messages in " + channel.getAsMention() + ".")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Save the configuration
        String guildId = event.getGuild().getId();
        String channelId = channel.getId();
        
        gamePromotionService.setPromotionChannel(guildId, channelId);
        
        // Send confirmation
        event.reply(String.format(
                "✅ **Game Promotion Channel Configured**\n\n" +
                "Promotions will now be posted in %s\n\n" +
                "**Next Steps:**\n" +
                "• Use `/set-promotion-verbosity` to control posting frequency\n" +
                "• Use `/force-promotion-check` to test immediately\n\n" +
                "Default frequency: **MEDIUM** (every 12 hours)",
                channel.getAsMention()
        )).queue();
        
        logger.info("Promotion channel set to {} for guild {} by user {}",
                channelId, guildId, member.getId());
    }
    
    @Override
    public String getCommandName() {
        return "admin-setup-promotion-channel";
    }
}

