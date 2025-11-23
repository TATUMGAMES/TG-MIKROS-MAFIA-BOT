package com.tatumgames.mikros.communitygames.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.communitygames.model.GameResult;
import com.tatumgames.mikros.communitygames.model.GameSession;
import com.tatumgames.mikros.communitygames.model.GameType;
import com.tatumgames.mikros.communitygames.service.CommunityGameService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Command handler for /roll.
 * Allows players to roll dice in the dice battle game.
 */
public class RollCommand implements CommandHandler {
    private final CommunityGameService communityGameService;
    
    /**
     * Creates a new RollCommand handler.
     * 
     * @param communityGameService the community game service
     */
    public RollCommand(CommunityGameService communityGameService) {
        this.communityGameService = communityGameService;
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("dicefury-roll", "Roll the dice in today's dice battle game")
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
        
        // Check if there's an active dice game
        if (session == null || session.getGameType() != GameType.DICE_ROLL) {
            event.reply("❌ No active Dice Battle game! Check `/game-stats` to see today's game.")
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
        
        // Handle the dice roll
        GameResult result = communityGameService.handleAttempt(
                guildId,
                member.getId(),
                member.getEffectiveName(),
                ""
        );
        
        if (result == null) {
            event.reply("❌ Something went wrong. Try again!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Check if already rolled
        if (result.getAnswer().equals("already_rolled")) {
            event.reply("❌ You already rolled today! One roll per person.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        // Get current top scorer
        GameResult topScorer = session.getTopScorer();
        boolean isNewLeader = topScorer != null && topScorer.getUserId().equals(member.getId());
        
        // Announce the roll
        String announcement = String.format(
                "🎲 **%s rolled a %d!**%s%s",
                member.getAsMention(),
                result.getScore(),
                result.getScore() == 20 ? " 🔥 **CRITICAL HIT!**" : "",
                isNewLeader ? " 👑 **New Leader!**" : ""
        );
        
        event.reply(announcement).queue();
    }
    
    @Override
    public String getCommandName() {
        return "dicefury-roll";
    }
}

