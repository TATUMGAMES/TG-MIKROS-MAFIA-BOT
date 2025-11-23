package com.tatumgames.mikros.communitygames.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.communitygames.model.GameType;
import com.tatumgames.mikros.communitygames.service.CommunityGameService;
import com.tatumgames.mikros.communitygames.service.GameResetScheduler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Command handler for /game-setup.
 * Allows administrators to configure community games for their server.
 */
public class GameSetupCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(GameSetupCommand.class);
    private final CommunityGameService communityGameService;
    
    /**
     * Creates a new GameSetupCommand handler.
     * 
     * @param communityGameService the community game service
     * @param gameResetScheduler the game reset scheduler (unused, kept for future use)
     */
    public GameSetupCommand(CommunityGameService communityGameService, GameResetScheduler gameResetScheduler) {
        this.communityGameService = communityGameService;
        // gameResetScheduler reserved for future manual reset functionality
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-game-setup", "Configure daily community games for your server")
                .addOption(OptionType.CHANNEL, "channel", "Channel for daily games", true)
                .addOption(OptionType.INTEGER, "reset_hour", "Daily reset hour (0-23 UTC, default: 0)", false)
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
        
        // Get options
        TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
        int resetHour = event.getOption("reset_hour") != null
                ? event.getOption("reset_hour").getAsInt()
                : 0;
        
        // Validate reset hour
        if (resetHour < 0 || resetHour > 23) {
            event.reply("❌ Reset hour must be between 0 and 23.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Validate bot can post in channel
        if (!channel.canTalk()) {
            event.reply("❌ I don't have permission to send messages in " + channel.getAsMention() + ".")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Setup games with all types enabled by default
        String guildId = event.getGuild().getId();
        Set<GameType> allGames = new HashSet<>(Set.of(GameType.values()));
        LocalTime resetTime = LocalTime.of(resetHour, 0);
        
        communityGameService.setupGames(guildId, channel.getId(), allGames, resetTime);
        
        // Start the first game immediately
        communityGameService.startRandomEnabledGame(guildId);
        String announcement = communityGameService.getGameAnnouncement(guildId);
        
        if (announcement != null) {
            channel.sendMessage(announcement).queue();
        }
        
        // Send confirmation
        event.reply(String.format(
                "✅ **Community Games Configured!**\n\n" +
                "**Game Channel:** %s\n" +
                "**Reset Time:** %02d:00 UTC (daily)\n" +
                "**Enabled Games:** All (%d games)\n\n" +
                "🎮 The first game has been posted!\n\n" +
                "**Next Steps:**\n" +
                "• Use `/admin-game-config` to customize settings\n" +
                "• Use `/game-stats` to view the leaderboard\n" +
                "• Players can join with `/scramble-guess`, `/dicefury-roll`, or `/emojihunt-match`",
                channel.getAsMention(),
                resetHour,
                allGames.size()
        )).queue();
        
        logger.info("Community games setup for guild {} by user {}: channel={}, resetHour={}",
                guildId, member.getId(), channel.getId(), resetHour);
    }
    
    @Override
    public String getCommandName() {
        return "admin-game-setup";
    }
}

