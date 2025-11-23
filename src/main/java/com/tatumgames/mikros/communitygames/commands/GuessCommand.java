package com.tatumgames.mikros.communitygames.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.communitygames.model.GameResult;
import com.tatumgames.mikros.communitygames.model.GameSession;
import com.tatumgames.mikros.communitygames.model.GameType;
import com.tatumgames.mikros.communitygames.service.CommunityGameService;
import com.tatumgames.mikros.spelling.model.ChallengeSession;
import com.tatumgames.mikros.spelling.service.SpellingChallengeService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Command handler for /guess.
 * Allows players to guess words in word unscramble games and spelling challenges.
 */
public class GuessCommand implements CommandHandler {
    private final CommunityGameService communityGameService;
    private final SpellingChallengeService spellingService;
    
    /**
     * Creates a new GuessCommand handler.
     * 
     * @param communityGameService the community game service
     * @param spellingService the spelling challenge service
     */
    public GuessCommand(CommunityGameService communityGameService, SpellingChallengeService spellingService) {
        this.communityGameService = communityGameService;
        this.spellingService = spellingService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("guess", "Guess the word in word games or spelling challenge")
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
        
        // Try Community Games Word Unscramble first
        GameSession session = communityGameService.getActiveSession(guildId);
        if (session != null && session.getGameType() == GameType.WORD_UNSCRAMBLE && session.isActive()) {
            handleCommunityGameGuess(event, member, guildId, session, guess);
            return;
        }
        
        // Try Spelling Challenge
        ChallengeSession spellingSession = spellingService.getActiveSession(guildId);
        if (spellingSession != null && spellingSession.isActive()) {
            handleSpellingChallengeGuess(event, member, guildId, guess);
            return;
        }
        
        // No active game
        event.reply("❌ No active word game or spelling challenge!\n\n" +
                "• Check `/game-stats` for community games\n" +
                "• Use `/spelling-challenge` to see the daily challenge")
                .setEphemeral(true)
                .queue();
    }
    
    /**
     * Handles guess for Community Games Word Unscramble.
     */
    private void handleCommunityGameGuess(SlashCommandInteractionEvent event, Member member, 
                                         String guildId, GameSession session, String guess) {
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
    
    /**
     * Handles guess for Spelling Challenge.
     */
    private void handleSpellingChallengeGuess(SlashCommandInteractionEvent event, Member member,
                                              String guildId, String guess) {
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
            event.reply("❌ No active spelling challenge! Use `/spelling-challenge` to start one.")
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
        return "guess";
    }
}

