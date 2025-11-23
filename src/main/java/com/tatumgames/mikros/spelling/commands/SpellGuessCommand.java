package com.tatumgames.mikros.spelling.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.spelling.service.SpellingChallengeService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Command handler for /spell-guess.
 * Allows players to guess words in spelling challenges.
 */
public class SpellGuessCommand implements CommandHandler {
    private final SpellingChallengeService spellingService;
    
    /**
     * Creates a new SpellGuessCommand handler.
     * 
     * @param spellingService the spelling challenge service
     */
    public SpellGuessCommand(SpellingChallengeService spellingService) {
        this.spellingService = spellingService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("spell-guess", "Guess the word in spelling challenges")
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
        
        // Check for active Spelling Challenge
        var spellingSession = spellingService.getActiveSession(guildId);
        if (spellingSession == null || !spellingSession.isActive()) {
            event.reply("❌ No active spelling challenge!\n\n" +
                    "• Use `/spell-challenge` to see the daily challenge")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        SpellingChallengeService.AttemptResult result = spellingService.processGuess(
                guildId,
                member.getId(),
                member.getEffectiveName(),
                guess
        );
        
        // Handle different result scenarios
        if (result.getMessage().equals("Already solved")) {
            event.reply("✅ You already solved today's spelling challenge!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (result.getMessage().equals("Max attempts reached")) {
            event.reply("❌ You've used all 3 attempts for today! Try again tomorrow.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (result.getMessage().equals("No active challenge")) {
            event.reply("❌ No active spelling challenge! Use `/spell-challenge` to start one.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Show result
        if (result.isCorrect()) {
            String bonus = result.wasFirst() ? " (First solver! 🏆)" : "";
            event.reply(String.format(
                    "✅ **CORRECT!** %s\n\n" +
                    "%s solved it: **%s**!\n\n" +
                    "**+%d points** awarded\n" +
                    "Attempt: %d/3",
                    bonus,
                    member.getAsMention(),
                    guess.toUpperCase(),
                    result.getPointsAwarded(),
                    result.getAttemptsUsed()
            )).queue();
        } else {
            event.reply(String.format(
                    "❌ **Incorrect!**\n\n" +
                    "Your guess: %s\n" +
                    "Attempts used: %d/3\n\n" +
                    "Try again!",
                    guess.toUpperCase(),
                    result.getAttemptsUsed()
            )).setEphemeral(true).queue();
        }
    }
    
    @Override
    public String getCommandName() {
        return "spell-guess";
    }
}





