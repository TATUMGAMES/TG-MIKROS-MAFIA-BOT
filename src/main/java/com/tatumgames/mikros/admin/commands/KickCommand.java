package com.tatumgames.mikros.admin.commands;

import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.handler.CommandHandler;
import com.tatumgames.mikros.models.ActionType;
import com.tatumgames.mikros.models.ModerationAction;
import com.tatumgames.mikros.services.ModerationLogService;
import java.time.Instant;
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

/**
 * Command handler for the /kick command. Kicks a user from the server and logs the action.
 * Admin-only command.
 */
@SuppressWarnings("ClassCanBeRecord")
public class KickCommand implements CommandHandler {
  private static final Logger logger = LoggerFactory.getLogger(KickCommand.class);
  private final ModerationLogService moderationLogService;

  /**
   * Creates a new KickCommand handler.
   *
   * @param moderationLogService the moderation log service
   */
  public KickCommand(ModerationLogService moderationLogService) {
    this.moderationLogService = moderationLogService;
  }

  @Override
  public CommandData getCommandData() {
    return Commands.slash("admin-kick", "Kick a user from the server")
        .addOption(OptionType.USER, "user", "The user to kick", true)
        .addOption(OptionType.STRING, "reason", "The reason for the kick", true)
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));
  }

  @Override
  public void handle(SlashCommandInteractionEvent event) {
    // Check if user has permission
    Member member = event.getMember();
    Guild guild = event.getGuild();

    if (member == null || guild == null || !member.hasPermission(Permission.KICK_MEMBERS)) {
      event.reply("❌ You don't have permission to use this command.").setEphemeral(true).queue();
      return;
    }

    // Get command options
    User targetUser = event.getOption("user", OptionMapping::getAsUser);
    String reason = event.getOption("reason", OptionMapping::getAsString);
    Member targetMember = guild.getMember(targetUser);

    if (AdminUtils.isInvalidTargetUser(member, targetUser, event)) {
      return; // stop executing the command
    }

    if (targetMember == null) {
      event
          .reply(
              "❌ User is not in this server or could not be found. They may have already left. Only current members can be kicked.")
          .setEphemeral(true)
          .queue();
      return;
    }

    // Check role hierarchy
    if (!member.canInteract(targetMember)) {
      event.reply("❌ You cannot kick this user due to role hierarchy.").setEphemeral(true).queue();
      return;
    }

    // Check bot permissions
    if (!guild.getSelfMember().canInteract(targetMember)) {
      event.reply("❌ I cannot kick this user due to role hierarchy.").setEphemeral(true).queue();
      return;
    }

    // Create and log the moderation action
    assert targetUser != null;
    ModerationAction action =
        new ModerationAction(
            targetUser.getId(),
            targetUser.getEffectiveName(),
            member.getId(),
            member.getEffectiveName(),
            ActionType.KICK,
            reason,
            Instant.now(),
            guild.getId());

    moderationLogService.logAction(action);

    // Defer the reply as kicking might take a moment
    event.deferReply().queue();

    // Perform the kick
    guild
        .kick(targetMember)
        .reason(reason)
        .queue(
            success -> {
              String message =
                  String.format(
                      """
                                            👢 **User Kicked**
                                            User: %s
                                            Reason: %s
                                            Moderator: %s
                                                   \s""",
                      targetUser.getName(), reason, member.getAsMention());
              event.getHook().sendMessage(message).queue();

              logger.info(
                  "User {} kicked by {} in guild {}: {}",
                  targetUser.getId(),
                  member.getId(),
                  guild.getId(),
                  reason);
            },
            error -> {
              event
                  .getHook()
                  .sendMessage("❌ Failed to kick user: " + error.getMessage())
                  .setEphemeral(true)
                  .queue();
              logger.error(
                  "Failed to kick user {}: {}", targetUser.getId(), error.getMessage(), error);
            });
  }

  @Override
  public String getCommandName() {
    return "admin-kick";
  }
}
