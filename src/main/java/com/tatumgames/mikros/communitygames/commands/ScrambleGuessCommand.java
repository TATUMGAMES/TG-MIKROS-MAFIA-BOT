package com.tatumgames.mikros.communitygames.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.communitygames.model.GameResult;
import com.tatumgames.mikros.communitygames.model.GameSession;
import com.tatumgames.mikros.communitygames.model.GameType;
import com.tatumgames.mikros.communitygames.service.CommunityGameService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Command handler for /scramble-guess.
 * Allows players to guess words in word unscramble games.
 */
public class ScrambleGuessCommand implements CommandHandler {
    private final CommunityGameService communityGameService;
    
    /**
     * Creates a new ScrambleGuessCommand handler.
     * 
     * @param communityGameService the community game service
     */
    public ScrambleGuessCommand(CommunityGameService communityGameService) {
        this.communityGameService = communityGameService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("scramble-guess", "Guess the word in word unscramble games")
                .addOption(OptionType.STRING, "word", "Your guess", true)
                .setGuildOnly(true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            return;
        }
        
        String guildId = event.getGuild().getId();
        String guess = event.getOption("word").getAsString();
        
        // Check for active Word Unscramble game
        GameSession session = communityGameService.getActiveSession(guildId);
        if (session == null || session.getGameType() != GameType.WORD_UNSCRAMBLE || !session.isActive()) {
            event.reply("❌ No active word unscramble game!\n\n" +
                    "• Check `/game-stats` for community games\n" +
                    "• Wait for the next daily game reset")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        GameResult result = communityGameService.handleAttempt(
                guildId,
                member.getId(),
                member.getEffectiveName(),
                guess
        );
        
        if (result == null) {
            event.reply("❌ Something went wrong. Try again!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Check if correct
        if (result.isCorrect()) {
            // Announce to everyone
            event.reply(String.format(
                    "🎉 **CORRECT!** 🎉\n\n" +
                    "%s guessed it right: **%s**!\n\n" +
                    "Score: %d points\n" +
                    "Time: %.1f seconds",
                    member.getAsMention(),
                    guess.toUpperCase(),
                    result.getScore(),
                    (result.getTimestamp().getEpochSecond() - session.getStartTime().getEpochSecond()) / 1.0
            )).queue();
            
            // Mark session as won
            session.setActive(false);
            
        } else {
            // Wrong answer - private response
            event.reply(String.format(
                    "❌ **Incorrect!**\n\n" +
                    "Your guess: %s\n" +
                    "Try again!",
                    guess.toUpperCase()
            )).setEphemeral(true).queue();
        }
    }
    
    @Override
    public String getCommandName() {
        return "scramble-guess";
    }
}





