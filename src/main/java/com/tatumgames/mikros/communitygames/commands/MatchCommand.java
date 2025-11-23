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
 * Command handler for /match.
 * Allows players to match emoji patterns in the emoji match game.
 */
public class MatchCommand implements CommandHandler {
    private final CommunityGameService communityGameService;
    
    /**
     * Creates a new MatchCommand handler.
     * 
     * @param communityGameService the community game service
     */
    public MatchCommand(CommunityGameService communityGameService) {
        this.communityGameService = communityGameService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("emojihunt-match", "Match the emoji pattern in today's emoji match game")
                .addOption(OptionType.STRING, "emojis", "The emoji pattern", true)
                .setGuildOnly(true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            return;
        }
        
        String guildId = event.getGuild().getId();
        GameSession session = communityGameService.getActiveSession(guildId);
        
        // Check if there's an active emoji match game
        if (session == null || session.getGameType() != GameType.EMOJI_MATCH) {
            event.reply("❌ No active Emoji Match game! Check `/game-stats` to see today's game.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (!session.isActive()) {
            event.reply("❌ Today's game has ended. Wait for tomorrow's reset!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get the emoji input
        String emojis = event.getOption("emojis").getAsString();
        
        // Handle the attempt
        GameResult result = communityGameService.handleAttempt(
                guildId,
                member.getId(),
                member.getEffectiveName(),
                emojis
        );
        
        if (result == null) {
            event.reply("❌ Something went wrong. Try again!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Check if already won
        GameResult winner = session.getWinner();
        if (winner != null && !winner.getUserId().equals(member.getId())) {
            event.reply("❌ Someone already matched the pattern! Better luck tomorrow.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Check if correct
        if (result.isCorrect()) {
            // Announce to everyone
            event.reply(String.format(
                    "🎉 **PERFECT MATCH!** 🎉\n\n" +
                    "%s matched the pattern!\n\n" +
                    "Pattern: %s\n" +
                    "Score: %d points",
                    member.getAsMention(),
                    emojis,
                    result.getScore()
            )).queue();
            
            // Mark session as won
            session.setActive(false);
            
        } else {
            // Wrong answer - private response
            event.reply(String.format(
                    "❌ **Not quite!**\n\n" +
                    "Your attempt: %s\n" +
                    "Try again!",
                    emojis
            )).setEphemeral(true).queue();
        }
    }
    
    @Override
    public String getCommandName() {
        return "emojihunt-match";
    }
}

