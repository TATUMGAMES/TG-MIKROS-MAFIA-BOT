package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.games.rpg.service.BossService;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command handler for /rpg-reset.
 * Allows administrators to reset all RPG data for their server.
 * Requires double confirmation to prevent accidental wipes.
 */
public class RPGResetCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGResetCommand.class);
    
    private final CharacterService characterService;
    private final BossService bossService;
    
    // Pending reset confirmations: guildId -> timestamp
    private static final Map<String, Long> pendingConfirmations = new ConcurrentHashMap<>();
    private static final long CONFIRMATION_TIMEOUT_MS = 30000; // 30 seconds
    
    /**
     * Creates a new RPGResetCommand handler.
     * 
     * @param characterService the character service
     * @param bossService the boss service
     */
    public RPGResetCommand(CharacterService characterService, BossService bossService) {
        this.characterService = characterService;
        this.bossService = bossService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-reset", "Reset all RPG data for this server (Admin only)")
                .addOption(OptionType.STRING, "type", "Type of reset (must be 'full')", true, true)
                .addOption(OptionType.STRING, "confirm", "Confirmation (must be 'confirm' to execute)", false)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            return;
        }
        
        // Check if user has administrator permission
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an **Administrator** to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        String guildId = event.getGuild().getId();
        String type = event.getOption("type").getAsString();
        String confirm = event.getOption("confirm") != null
                ? event.getOption("confirm").getAsString()
                : null;
        
        // Handle ONLY the full reset version
        if (type.equalsIgnoreCase("full")) {
            handleFullReset(event, guildId, confirm);
        } else {
            event.reply("❌ Invalid reset type! Use: **full**")
                    .setEphemeral(true)
                    .queue();
        }
    }
    
    /**
     * Handles the full reset with double confirmation.
     * 
     * @param event the slash command event
     * @param guildId the guild ID
     * @param confirm the confirmation string (null if first command)
     */
    private void handleFullReset(SlashCommandInteractionEvent event, String guildId, String confirm) {
        // If user typed /rpg-reset full confirm
        if (confirm != null && confirm.equalsIgnoreCase("confirm")) {
            // Check if there was a pending reset
            Long timestamp = pendingConfirmations.get(guildId);
            
            if (timestamp == null || (System.currentTimeMillis() - timestamp) > CONFIRMATION_TIMEOUT_MS) {
                event.reply("❌ No active reset request found or confirmation timed out (30 seconds).\n\n" +
                        "Please run `/rpg-reset full` again to start a new reset request.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
            
            // Reset server-specific data
            characterService.resetServerData(guildId);
            bossService.resetServerData(guildId);
            
            // Note: Characters are stored globally, not per-server
            // In a future version with server tracking, we would only clear characters for this server
            // For now, we clear all characters globally (affects all servers)
            int clearedCharacters = characterService.clearAllCharacters();
            
            // Remove pending confirmation
            pendingConfirmations.remove(guildId);
            
            event.reply(String.format(
                    "🧹 **Full RPG Reset Complete!**\n\n" +
                    "All RPG data for this server has been cleared:\n" +
                    "• Boss progression reset\n" +
                    "• World state reset\n" +
                    "• RPG configuration reset\n" +
                    "• %d character profile(s) deleted (global reset)\n\n" +
                    "⚠️ **Note:** Characters are stored globally, so this reset affects all servers.\n\n" +
                    "Your server starts fresh. Players must register again using `/rpg-register`.",
                    clearedCharacters
            )).queue();
            
            logger.warn("Admin {} performed full RPG reset for guild {} - Cleared {} characters",
                    event.getUser().getId(), guildId, clearedCharacters);
            
            return;
        }
        
        // FIRST COMMAND: ask for confirmation
        pendingConfirmations.put(guildId, System.currentTimeMillis());
        
        int characterCount = characterService.getCharacterCount();
        int serverCharacterCount = characterService.getServerCharacterCount(guildId);
        
        event.reply(String.format(
                "⚠️ **Warning: Full RPG Reset Requested!**\n\n" +
                "This will DELETE **ALL** RPG data for this server:\n" +
                "• All player profiles (%d total characters)\n" +
                "• All levels, XP, and stats\n" +
                "• World state and boss progression\n" +
                "• Boss levels and defeat counts\n\n" +
                "**This action cannot be undone!**\n\n" +
                "To confirm, type:\n" +
                "`/rpg-reset full confirm`\n\n" +
                "⏰ You have **30 seconds** to confirm.",
                characterCount
        )).setEphemeral(true).queue();
        
        logger.info("Admin {} requested full RPG reset for guild {} (pending confirmation)",
                event.getUser().getId(), guildId);
    }
    
    @Override
    public String getCommandName() {
        return "rpg-reset";
    }
}

