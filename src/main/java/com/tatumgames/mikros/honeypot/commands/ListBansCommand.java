package com.tatumgames.mikros.honeypot.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import com.tatumgames.mikros.services.ModerationLogService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for listing recent bans.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ListBansCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListBansCommand.class);
    private final ModerationLogService moderationLogService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Creates a new ListBansCommand handler.
     *
     * @param moderationLogService the moderation log service
     */
    public ListBansCommand(ModerationLogService moderationLogService) {
        this.moderationLogService = moderationLogService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("list_bans", "Display recent bans and reasons")
                .setGuildOnly(true)
                .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (member == null || guild == null ||
                !member.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ You don't have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Get guild id
        String guildId = guild.getId();

        // Get all bans from moderation log
        List<ModerationAction> allActions = moderationLogService.getAllActions(guildId);
        List<ModerationAction> bans = allActions.stream()
                .filter(action -> action.actionType() == ActionType.BAN)
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp())) // Most recent first
                .limit(20) // Show last 20 bans
                .collect(Collectors.toList());

        if (bans.isEmpty()) {
            event.reply("📋 **Recent Bans**\n\nNo bans found in moderation log.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Build embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 Recent Bans")
                .setColor(0x5865F2) // Discord blurple color
                .setDescription(String.format("Showing last %d ban(s):", bans.size()));

        StringBuilder bansList = new StringBuilder();
        for (int i = 0; i < bans.size(); i++) {
            ModerationAction ban = bans.get(i);
            String timestamp = ban.timestamp().atZone(ZoneId.systemDefault()).format(DATE_FORMATTER);
            bansList.append(String.format("""
                            %d. **%s** (`%s`)
                               Reason: %s
                               Moderator: %s
                               Time: %s
                            
                            """,
                    i + 1,
                    ban.targetUsername(),
                    ban.targetUserId(),
                    ban.reason(),
                    ban.moderatorUsername(),
                    timestamp
            ));
        }

        embed.setDescription(bansList.toString());

        // Discord embed field limit is 1024 characters, so we might need to split
        if (bansList.length() > 2000) {
            // Split into multiple embeds or use pagination (simplified: just show first 10)
            bans = bans.stream().limit(10).toList();
            bansList = new StringBuilder();
            for (int i = 0; i < bans.size(); i++) {
                ModerationAction ban = bans.get(i);
                String timestamp = ban.timestamp().atZone(ZoneId.systemDefault()).format(DATE_FORMATTER);
                bansList.append(String.format(
                        "%d. **%s** - %s (%s)\n",
                        i + 1,
                        ban.targetUsername(),
                        ban.reason(),
                        timestamp
                ));
            }
            embed.setDescription(bansList.toString());
            embed.setFooter("Showing first 10 bans. Use /history for more details.", null);
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        logger.debug("Listed {} bans for guild {}", bans.size(), guildId);
    }

    @Override
    public String getCommandName() {
        return "list_bans";
    }
}

