package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
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
 * Command handler for /admin-rpg-setup.
 * Allows administrators to configure RPG system for their server.
 */
public class RPGSetupCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGSetupCommand.class);
    private final CharacterService characterService;

    /**
     * Creates a new RPGSetupCommand handler.
     *
     * @param characterService the character service
     */
    public RPGSetupCommand(CharacterService characterService) {
        this.characterService = characterService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-rpg-setup", "Configure RPG system for your server")
                .addOption(OptionType.CHANNEL, "channel", "Channel for RPG commands and boss battles", true)
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

        // Validate bot can post in channel
        if (!channel.canTalk()) {
            event.reply("❌ I don't have permission to send messages in " + channel.getAsMention() + ".")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get or create RPG config
        String guildId = guild.getId();
        RPGConfig config = characterService.getConfig(guildId);

        // Set channel and enable RPG
        config.setRpgChannelId(channel.getId());
        config.setEnabled(true);
        // Keep default values: chargeRefreshHours = 12, xpMultiplier = 1.0
        characterService.updateConfig(config);

        // Send confirmation
        event.reply(String.format("""
                        ✅ **RPG System Configured!**
                        
                        **RPG Channel:** %s
                        **Status:** Enabled
                        **Charge Refresh:** 12 hours (default)
                        **XP Multiplier:** 1.0x (default)
                        
                        ⚔️ The RPG system is now active!
                        
                        **Next Steps:**
                        • Use `/admin-rpg-config` to customize settings
                        • Players can register with `/rpg-register`
                        • Bosses will spawn automatically in this channel
                        """,
                channel.getAsMention()
        )).queue();

        logger.info("RPG setup for guild {} by user {}: channel={}",
                guildId, member.getId(), channel.getId());
    }

    @Override
    public String getCommandName() {
        return "admin-rpg-setup";
    }
}

