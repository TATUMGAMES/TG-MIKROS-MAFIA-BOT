package com.tatumgames.mikros.games.word_unscramble.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleResult;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleSession;
import com.tatumgames.mikros.games.word_unscramble.model.WordUnscrambleType;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Command handler for /scramble-guess.
 * Allows players to guess words in word unscramble games.
 */
@SuppressWarnings("ClassCanBeRecord")
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
        Guild guild = event.getGuild();

        if (member == null || guild == null) {
            return;
        }

        String guildId = guild.getId();
        String guess = event.getOption("word", OptionMapping::getAsString);

        // Check for active Word Unscramble game
        WordUnscrambleSession session = wordUnscrambleService.getActiveSession(guildId);
        if (session == null || session.getGameType() != WordUnscrambleType.WORD_UNSCRAMBLE || !session.isActive()) {
            event.reply("""
                            ❌ No active word unscramble game!
                            
                            • Check `/game-stats` for community games
                            • Wait for the next hourly game reset
                            """)
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

            long timeToSolve =
                    result.timestamp().getEpochSecond() - session.getStartTime().getEpochSecond();

            event.reply(String.format("""
                            🎉 **CORRECT!** 🎉
                            
                            %s guessed it right: **%s**!
                            
                            Score: %d points
                            Time: %d seconds
                            """,
                    member.getAsMention(),
                    guess,
                    result.score(),
                    timeToSolve
            )).queue();

            session.setActive(false);

        } else {
            event.reply(String.format("""
                            ❌ **Incorrect!**
                            
                            Your guess: %s
                            Try again!
                            """,
                    guess
            )).setEphemeral(true).queue();
        }
    }

    @Override
    public String getCommandName() {
        return "scramble-guess";
    }
}
