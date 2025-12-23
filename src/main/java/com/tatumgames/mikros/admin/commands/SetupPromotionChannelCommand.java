package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.services.GamePromotionService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /admin-setup-promotion.
 * Allows server administrators to designate a channel for game promotions.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
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
        return Commands.slash("admin-setup-promotion", "Set the channel for game promotion posts")
                .addOption(OptionType.CHANNEL, "channel", "The channel to post promotions in", true)
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

        // Save the configuration
        String guildId = guild.getId();
        String channelId = channel.getId();

        gamePromotionService.setPromotionChannel(guildId, channelId);

        // Send confirmation
        event.reply(String.format(
                "✅ **Game Promotion Channel Configured**\n\n" +
                        "Promotions will now be posted in %s\n\n" +
                        "**Next Steps:**\n" +
                        "• Use `/admin-promotion-config set-verbosity` to control posting frequency\n" +
                        "• Use `/admin-promotion-config force-check` to test immediately\n\n" +
                        "Default frequency: **MEDIUM** (every 12 hours)",
                channel.getAsMention()
        )).queue();

        logger.info("Promotion channel set to {} for guild {} by user {}",
                channelId, guildId, member.getId());
    }

    @Override
    public String getCommandName() {
        return "admin-setup-promotion";
    }
}

