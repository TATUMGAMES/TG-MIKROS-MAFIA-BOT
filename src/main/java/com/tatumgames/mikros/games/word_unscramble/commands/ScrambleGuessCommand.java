package com.tatumgames.mikros.games.word_unscramble.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleResult;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleSession;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleType;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleService;
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
    private final WordUnscrambleService wordUnscrambleService;
    
    /**
     * Creates a new ScrambleGuessCommand handler.
     * 
     * @param wordUnscrambleService the Word Unscramble service
     */
    public ScrambleGuessCommand(WordUnscrambleService wordUnscrambleService) {
        this.wordUnscrambleService = wordUnscrambleService;
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
        WordUnscrambleSession session = wordUnscrambleService.getActiveSession(guildId);
        if (session == null || session.getGameType() != WordUnscrambleType.WORD_UNSCRAMBLE || !session.isActive()) {
            event.reply("❌ No active word unscramble game!\n\n" +
                    "• Check `/game-stats` for community games\n" +
                    "• Wait for the next hourly game reset")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        WordUnscrambleResult result = wordUnscrambleService.handleAttempt(
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

