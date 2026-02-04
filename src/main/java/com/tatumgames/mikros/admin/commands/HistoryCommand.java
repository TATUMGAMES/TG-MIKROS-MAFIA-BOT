package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.handler.CommandHandler;
import com.tatumgames.mikros.models.ModerationAction;
import com.tatumgames.mikros.services.ModerationLogService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command handler for /admin-history. Displays moderation history for a user.
 */
@SuppressWarnings("ClassCanBeRecord")
public class HistoryCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(HistoryCommand.class);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneId.systemDefault());
    private static final int MAX_ENTRIES = 15;

    private final ModerationLogService moderationLogService;

    /**
     * Creates a new HistoryCommand handler.
     *
     * @param moderationLogService the moderation log service
     */
    public HistoryCommand(ModerationLogService moderationLogService) {
        this.moderationLogService = moderationLogService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("admin-history", "View user moderation history")
                .addOption(OptionType.USER, "user", "The user to view history for", true)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null || !member.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        User targetUser = event.getOption("user", OptionMapping::getAsUser);
        if (AdminUtils.isInvalidTargetUser(member, targetUser, event)) {
            return;
        }
        if (targetUser == null) {
            event.reply("❌ You must specify a user.").setEphemeral(true).queue();
            return;
        }

        String guildId = guild.getId();
        String userId = targetUser.getId();

        List<ModerationAction> history = moderationLogService.getUserHistory(userId, guildId);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📋 Moderation History: " + targetUser.getEffectiveName());
        embed.setColor(Color.ORANGE);
        embed.setTimestamp(Instant.now());

        if (history.isEmpty()) {
            embed.setDescription("No moderation history found for this user.");
        } else {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (ModerationAction action : history) {
                if (count >= MAX_ENTRIES) {
                    sb.append("\n_...and ").append(history.size() - MAX_ENTRIES).append(" more entries_");
                    break;
                }
                String typeEmoji =
                        switch (action.actionType().name()) {
                            case "WARN" -> "⚠️";
                            case "KICK" -> "👢";
                            case "BAN" -> "🔨";
                            default -> "•";
                        };
                sb.append(
                        String.format(
                                "%s **%s** by %s\n   Reason: %s\n   %s\n\n",
                                typeEmoji,
                                action.actionType().name(),
                                action.moderatorUsername(),
                                action.reason(),
                                DATE_FORMAT.format(action.timestamp())));
                count++;
            }
            embed.setDescription(sb.toString());
            embed.setFooter("Total: " + history.size() + " moderation action(s)");
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        logger.info(
                "Moderation history requested for user {} in guild {} by {}",
                userId,
                guildId,
                member.getId());
    }

    @Override
    public String getCommandName() {
        return "admin-history";
    }
}
