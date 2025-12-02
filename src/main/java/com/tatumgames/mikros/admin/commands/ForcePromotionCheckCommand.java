package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.services.GamePromotionService;
import com.tatumgames.mikros.services.scheduler.GamePromotionScheduler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /force-promotion-check.
 * Manually triggers the promotion check and posting logic.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ForcePromotionCheckCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ForcePromotionCheckCommand.class);
    private final GamePromotionScheduler gamePromotionScheduler;
    private final GamePromotionService gamePromotionService;

    /**
     * Creates a new ForcePromotionCheckCommand handler.
     *
     * @param gamePromotionScheduler the promotion scheduler
     * @param gamePromotionService   the promotion service
     */
    public ForcePromotionCheckCommand(GamePromotionScheduler gamePromotionScheduler,
                                      GamePromotionService gamePromotionService) {
        this.gamePromotionScheduler = gamePromotionScheduler;
        this.gamePromotionService = gamePromotionService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-force-promotion-check", "Manually trigger a game promotion check")
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Check if user has permission
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You must be an administrator to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Check if promotion channel is configured
        if (gamePromotionService.getPromotionChannel(guild.getId()) == null) {
            String message = """
                    ️⚠️ **Promotion channel not configured**
                    
                    Please use `/setup-promotion-channel` first to designate a channel for promotions.
                    """;

            event.reply(message)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Defer reply as this might take a moment
        event.deferReply().queue();

        // Trigger promotion check
        int promotionsPosted = gamePromotionScheduler.forceCheckGuild(event.getGuild());

        // Send result
        String message = buildPromotionMessage(promotionsPosted);
        event.getHook().sendMessage(message).queue();

        logger.info("Forced promotion check for guild {} by user {}, posted {} promotions",
                guild.getId(), member.getId(), promotionsPosted);
    }

    /**
     * Builds a user-facing status message describing the result of the promotion
     * posting process. If one or more promotions were posted, the message includes
     * the count. If no promotions were available, the message explains the possible
     * reasons and informs the user that the scheduler will continue checking.
     *
     * @param promotionsPosted the number of promotions successfully posted
     * @return a formatted, multi-line message appropriate to the posting result
     */
    private String buildPromotionMessage(int promotionsPosted) {
        if (promotionsPosted > 0) {
            return String.format("""
                    ✅ **Promotion Check Complete**
                    
                    Posted %d game promotion(s) to the configured channel.
                    
                    Check your promotion channel to see the new posts!
                    """, promotionsPosted);
        } else {
            return """
                    **No Promotions Available**
                    
                    There are currently no new game promotions to post.
                    
                    Possible reasons:
                    • All available promotions have already been posted
                    • No promotions have passed their deadline yet
                    • The promotion API is not yet integrated
                    
                    The scheduler will continue checking automatically.
                    """;
        }
    }

    @Override
    public String getCommandName() {
        return "admin-force-promotion-check";
    }
}

