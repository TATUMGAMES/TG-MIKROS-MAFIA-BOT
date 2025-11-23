package com.tatumgames.mikros.rpg.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.rpg.config.RPGConfig;
import com.tatumgames.mikros.rpg.service.CharacterService;
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

/**
 * Command handler for /rpg-config.
 * Allows administrators to configure RPG settings for their server.
 */
public class RPGConfigCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGConfigCommand.class);
    private final CharacterService characterService;
    
    /**
     * Creates a new RPGConfigCommand handler.
     * 
     * @param characterService the character service
     */
    public RPGConfigCommand(CharacterService characterService) {
        this.characterService = characterService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-config", "Configure RPG settings (admin only)")
                .addSubcommands(
                        new SubcommandData("view", "View current RPG configuration"),
                        new SubcommandData("toggle", "Enable or disable RPG system")
                                .addOption(OptionType.BOOLEAN, "enabled", "Enable RPG?", true),
                        new SubcommandData("set-channel", "Set RPG-specific channel")
                                .addOption(OptionType.CHANNEL, "channel", "RPG channel (or leave empty for any)", false),
                        new SubcommandData("set-cooldown", "Set action cooldown hours")
                                .addOption(OptionType.INTEGER, "hours", "Cooldown in hours (1-168)", true),
                        new SubcommandData("set-xp-multiplier", "Set XP gain multiplier")
                                .addOption(OptionType.NUMBER, "multiplier", "XP multiplier (0.1-10.0)", true)
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
            case "toggle" -> handleToggle(event, guildId);
            case "set-channel" -> handleSetChannel(event, guildId);
            case "set-cooldown" -> handleSetCooldown(event, guildId);
            case "set-xp-multiplier" -> handleSetXpMultiplier(event, guildId);
            default -> event.reply("❌ Unknown subcommand.").setEphemeral(true).queue();
        }
    }
    
    private void handleView(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);
        
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚙️ RPG Configuration");
        embed.setColor(Color.CYAN);
        
        embed.addField(
                "Status",
                config.isEnabled() ? "✅ Enabled" : "❌ Disabled",
                true
        );
        
        embed.addField(
                "RPG Channel",
                config.getRpgChannelId() != null
                        ? "<#" + config.getRpgChannelId() + ">"
                        : "Any channel",
                true
        );
        
        embed.addField(
                "Action Cooldown",
                config.getCooldownHours() + " hours",
                true
        );
        
        embed.addField(
                "XP Multiplier",
                String.format("%.1fx", config.getXpMultiplier()),
                true
        );
        
        embed.setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    private void handleToggle(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);
        boolean enabled = event.getOption("enabled").getAsBoolean();
        
        config.setEnabled(enabled);
        characterService.updateConfig(config);
        
        event.reply(String.format(
                "✅ RPG system %s",
                enabled ? "**enabled**" : "**disabled**"
        )).queue();
        
        logger.info("RPG {} for guild {}", enabled ? "enabled" : "disabled", guildId);
    }
    
    private void handleSetChannel(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);
        
        if (event.getOption("channel") == null) {
            // Clear channel restriction
            config.setRpgChannelId(null);
            characterService.updateConfig(config);
            
            event.reply("✅ RPG commands can now be used in any channel").queue();
            return;
        }
        
        TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
        
        config.setRpgChannelId(channel.getId());
        characterService.updateConfig(config);
        
        event.reply(String.format(
                "✅ RPG commands restricted to %s",
                channel.getAsMention()
        )).queue();
        
        logger.info("RPG channel set to {} for guild {}", channel.getId(), guildId);
    }
    
    private void handleSetCooldown(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);
        int hours = event.getOption("hours").getAsInt();
        
        if (hours < 1 || hours > 168) {
            event.reply("❌ Cooldown must be between 1 and 168 hours (1 week)")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        config.setCooldownHours(hours);
        characterService.updateConfig(config);
        
        event.reply(String.format(
                "✅ Action cooldown set to **%d hours**",
                hours
        )).queue();
        
        logger.info("RPG cooldown set to {} hours for guild {}", hours, guildId);
    }
    
    private void handleSetXpMultiplier(SlashCommandInteractionEvent event, String guildId) {
        RPGConfig config = characterService.getConfig(guildId);
        double multiplier = event.getOption("multiplier").getAsDouble();
        
        if (multiplier < 0.1 || multiplier > 10.0) {
            event.reply("❌ XP multiplier must be between 0.1 and 10.0")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        config.setXpMultiplier(multiplier);
        characterService.updateConfig(config);
        
        event.reply(String.format(
                "✅ XP multiplier set to **%.1fx**",
                multiplier
        )).queue();
        
        logger.info("RPG XP multiplier set to {}x for guild {}", multiplier, guildId);
    }
    
    @Override
    public String getCommandName() {
        return "admin-rpg-config";
    }
}

