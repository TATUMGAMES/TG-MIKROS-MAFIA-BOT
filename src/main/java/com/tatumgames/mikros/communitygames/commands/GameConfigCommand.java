package com.tatumgames.mikros.communitygames.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.communitygames.model.GameConfig;
import com.tatumgames.mikros.communitygames.model.GameType;
import com.tatumgames.mikros.communitygames.service.CommunityGameService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.LocalTime;

/**
 * Command handler for /game-config.
 * Allows administrators to modify game settings.
 */
public class GameConfigCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(GameConfigCommand.class);
    private final CommunityGameService communityGameService;
    
    /**
     * Creates a new GameConfigCommand handler.
     * 
     * @param communityGameService the community game service
     */
    public GameConfigCommand(CommunityGameService communityGameService) {
        this.communityGameService = communityGameService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-game-config", "Configure community game settings (admin only)")
                .addSubcommands(
                        new SubcommandData("view", "View current game configuration"),
                        new SubcommandData("set-channel", "Change the game channel")
                                .addOption(OptionType.CHANNEL, "channel", "New game channel", true),
                        new SubcommandData("set-reset-time", "Change daily reset time")
                                .addOption(OptionType.INTEGER, "hour", "Reset hour (0-23 UTC)", true),
                        new SubcommandData("enable-game", "Enable a specific game")
                                .addOption(OptionType.STRING, "game", "Game to enable", true),
                        new SubcommandData("disable-game", "Disable a specific game")
                                .addOption(OptionType.STRING, "game", "Game to disable", true)
                )
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
        
        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("❌ Please specify a subcommand.").setEphemeral(true).queue();
            return;
        }
        
        String guildId = event.getGuild().getId();
        
        switch (subcommand) {
            case "view" -> handleView(event, guildId);
            case "set-channel" -> handleSetChannel(event, guildId);
            case "set-reset-time" -> handleSetResetTime(event, guildId);
            case "enable-game" -> handleEnableGame(event, guildId);
            case "disable-game" -> handleDisableGame(event, guildId);
            default -> event.reply("❌ Unknown subcommand.").setEphemeral(true).queue();
        }
    }
    
    private void handleView(SlashCommandInteractionEvent event, String guildId) {
        GameConfig config = communityGameService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-game-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎮 Community Game Configuration");
        embed.setColor(Color.CYAN);
        
        embed.addField("Game Channel", "<#" + config.getGameChannelId() + ">", true);
        embed.addField("Reset Time", config.getResetTime().toString() + " UTC", true);
        embed.addField("Enabled Games", String.valueOf(config.getEnabledGames().size()), true);
        
        StringBuilder games = new StringBuilder();
        for (GameType type : GameType.values()) {
            String status = config.isGameEnabled(type) ? "✅" : "❌";
            games.append(String.format("%s %s %s\n",
                    status,
                    type.getEmoji(),
                    type.getDisplayName()));
        }
        
        embed.addField("Game Types", games.toString(), false);
        embed.setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    private void handleSetChannel(SlashCommandInteractionEvent event, String guildId) {
        GameConfig config = communityGameService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-game-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
        
        if (!channel.canTalk()) {
            event.reply("❌ I don't have permission to send messages in " + channel.getAsMention() + ".")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        config.setGameChannelId(channel.getId());
        communityGameService.updateConfig(guildId, config);
        
        event.reply(String.format("✅ Game channel updated to %s", channel.getAsMention())).queue();
        logger.info("Game channel updated to {} for guild {}", channel.getId(), guildId);
    }
    
    private void handleSetResetTime(SlashCommandInteractionEvent event, String guildId) {
        GameConfig config = communityGameService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-game-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        int hour = event.getOption("hour").getAsInt();
        
        if (hour < 0 || hour > 23) {
            event.reply("❌ Hour must be between 0 and 23.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        config.setResetTime(LocalTime.of(hour, 0));
        communityGameService.updateConfig(guildId, config);
        
        event.reply(String.format("✅ Reset time updated to %02d:00 UTC", hour)).queue();
        logger.info("Reset time updated to {}:00 for guild {}", hour, guildId);
    }
    
    private void handleEnableGame(SlashCommandInteractionEvent event, String guildId) {
        GameConfig config = communityGameService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-game-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        String gameName = event.getOption("game").getAsString().toUpperCase().replace(" ", "_");
        
        try {
            GameType gameType = GameType.valueOf(gameName);
            config.enableGame(gameType);
            communityGameService.updateConfig(guildId, config);
            
            event.reply(String.format("✅ Enabled: %s %s",
                    gameType.getEmoji(),
                    gameType.getDisplayName())).queue();
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid game type. Options: WORD_UNSCRAMBLE, DICE_ROLL, EMOJI_MATCH")
                    .setEphemeral(true)
                    .queue();
        }
    }
    
    private void handleDisableGame(SlashCommandInteractionEvent event, String guildId) {
        GameConfig config = communityGameService.getConfig(guildId);
        if (config == null) {
            event.reply("❌ Games not configured yet. Use `/admin-game-setup` first.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        String gameName = event.getOption("game").getAsString().toUpperCase().replace(" ", "_");
        
        try {
            GameType gameType = GameType.valueOf(gameName);
            
            if (config.getEnabledGames().size() <= 1) {
                event.reply("❌ Cannot disable the last game! Enable another game first.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
            
            config.disableGame(gameType);
            communityGameService.updateConfig(guildId, config);
            
            event.reply(String.format("✅ Disabled: %s %s",
                    gameType.getEmoji(),
                    gameType.getDisplayName())).queue();
        } catch (IllegalArgumentException e) {
            event.reply("❌ Invalid game type. Options: WORD_UNSCRAMBLE, DICE_ROLL, EMOJI_MATCH")
                    .setEphemeral(true)
                    .queue();
        }
    }
    
    @Override
    public String getCommandName() {
        return "admin-game-config";
    }
}

