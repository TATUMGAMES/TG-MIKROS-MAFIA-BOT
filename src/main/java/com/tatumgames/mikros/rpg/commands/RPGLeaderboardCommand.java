package com.tatumgames.mikros.rpg.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.rpg.model.RPGCharacter;
import com.tatumgames.mikros.rpg.service.CharacterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.List;

/**
 * Command handler for /rpg-leaderboard.
 * Shows top characters by level and XP.
 */
public class RPGLeaderboardCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RPGLeaderboardCommand.class);
    private final CharacterService characterService;
    
    /**
     * Creates a new RPGLeaderboardCommand handler.
     * 
     * @param characterService the character service
     */
    public RPGLeaderboardCommand(CharacterService characterService) {
        this.characterService = characterService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("rpg-leaderboard", "View top RPG characters by level and XP");
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Get top 10 characters
        List<RPGCharacter> topCharacters = characterService.getLeaderboard(10);
        
        if (topCharacters.isEmpty()) {
            event.reply("❌ No characters have been registered yet!\n\n" +
                    "Be the first to start your adventure with `/rpg-register`")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Build leaderboard embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 RPG Leaderboard - Top Adventurers");
        embed.setColor(new Color(255, 215, 0)); // Gold color
        embed.setDescription("The strongest characters across all servers");
        
        StringBuilder leaderboard = new StringBuilder();
        int rank = 1;
        
        for (RPGCharacter character : topCharacters) {
            String medal = getMedal(rank - 1);
            String classEmoji = character.getCharacterClass().getEmoji();
            
            leaderboard.append(String.format(
                    "%s **#%d** - %s **%s**\n" +
                    "└ %s Level %d • %,d XP • HP: %d/%d\n\n",
                    medal,
                    rank,
                    classEmoji,
                    character.getName(),
                    character.getCharacterClass().getDisplayName(),
                    character.getLevel(),
                    character.getXp(),
                    character.getStats().getCurrentHp(),
                    character.getStats().getMaxHp()
            ));
            
            rank++;
        }
        
        embed.addField("Top Characters", leaderboard.toString(), false);
        
        // Stats footer
        embed.setFooter(String.format(
                "Total Characters: %d • Join the adventure with /rpg-register",
                characterService.getCharacterCount()
        ));
        embed.setTimestamp(java.time.Instant.now());
        
        event.replyEmbeds(embed.build()).queue();
        
        logger.debug("Leaderboard requested - showing {} characters", topCharacters.size());
    }
    
    /**
     * Gets medal emoji for rank.
     */
    private String getMedal(int rank) {
        return switch (rank) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> "  ";
        };
    }
    
    @Override
    public String getCommandName() {
        return "rpg-leaderboard";
    }
}

