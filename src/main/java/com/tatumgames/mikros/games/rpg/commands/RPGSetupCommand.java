package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.service.BossService;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
    private final BossService bossService;

    /**
     * Creates a new RPGSetupCommand handler.
     *
     * @param characterService the character service
     * @param bossService      the boss service
     */
    public RPGSetupCommand(CharacterService characterService, BossService bossService) {
        this.characterService = characterService;
        this.bossService = bossService;
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

        // Spawn initial boss immediately
        Boss boss = bossService.spawnNormalBoss(guildId);
        if (boss != null && channel instanceof TextChannel textChannel) {
            // Announce the boss in the configured channel
            String announcement = String.format("""
                            🐲 **A New Boss Has Appeared!** 🐲
                            
                            **%s** (Level %d) - %s
                            
                            HP: **%,d**
                            
                            The shadows spread across Nilfheim… heroes, unite!
                            
                            Use `/rpg-boss-battle battle` to join the fight!
                            """,
                    boss.getName(),
                    boss.getLevel(),
                    boss.getType().getDisplayName(),
                    boss.getMaxHp()
            );

            textChannel.sendMessage(announcement).queue(
                    success -> logger.info("RPG setup: Spawned and announced initial boss {} for guild {}",
                            boss.getName(), guildId),
                    failure -> logger.error("RPG setup: Failed to announce initial boss for guild {}", guildId, failure)
            );
        } else if (boss == null) {
            logger.warn("RPG setup: Failed to spawn initial boss for guild {}", guildId);
        }

        // Send confirmation
        event.reply(String.format("""
                        ✅ **RPG System Configured!**
                        
                        **RPG Channel:** %s
                        **Status:** Enabled
                        **Charge Refresh:** 12 hours (default)
                        **XP Multiplier:** 1.0x (default)
                        
                        ⚔️ The RPG system is now active!
                        %s
                        
                        **Next Steps:**
                        • Use `/admin-rpg-config` to customize settings
                        • Players can register with `/rpg-register`
                        • Bosses will spawn automatically every 24 hours
                        """,
                channel.getAsMention(),
                boss != null ? "\n🐲 **A boss has spawned in the channel!**" : ""
        )).queue();

        logger.info("RPG setup for guild {} by user {}: channel={}, bossSpawned={}",
                guildId, member.getId(), channel.getId(), boss != null);
    }

    @Override
    public String getCommandName() {
        return "admin-rpg-setup";
    }
}

