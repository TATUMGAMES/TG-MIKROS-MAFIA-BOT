package com.tatumgames.mikros.games.rpg.commands;

import com.tatumgames.mikros.admin.utils.AdminUtils;
import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.service.AchievementService;
import com.tatumgames.mikros.games.rpg.service.AuraService;
import com.tatumgames.mikros.games.rpg.service.BossService;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import com.tatumgames.mikros.handler.CommandHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /rpg-reset-account. Lets a user delete their own RPG character and related
 * per-guild progression (with confirmation).
 */
@SuppressWarnings("ClassCanBeRecord")
public class RPGResetAccountCommand implements CommandHandler {
  private static final Logger logger = LoggerFactory.getLogger(RPGResetAccountCommand.class);
  private static final Map<String, Long> pendingConfirmations = new ConcurrentHashMap<>();
  private static final long CONFIRMATION_TIMEOUT_MS = 30000;

  private final CharacterService characterService;
  private final BossService bossService;
  private final AchievementService achievementService;
  private final AuraService auraService;

  /**
   * Creates a new RPGResetAccountCommand handler.
   *
   * @param characterService character storage
   * @param bossService boss damage tracking
   * @param achievementService first-to claims
   * @param auraService aura holders
   */
  public RPGResetAccountCommand(
      CharacterService characterService,
      BossService bossService,
      AchievementService achievementService,
      AuraService auraService) {
    this.characterService = characterService;
    this.bossService = bossService;
    this.achievementService = achievementService;
    this.auraService = auraService;
  }

  @Override
  public CommandData getCommandData() {
    return Commands.slash(
            "rpg-reset-account", "Delete your RPG character and progression in this server")
        .addOption(
            OptionType.STRING,
            "confirm",
            "Must be \"confirm\" to execute after the warning (within 30 seconds)",
            false)
        .setGuildOnly(true);
  }

  @Override
  public void handle(SlashCommandInteractionEvent event) {
    Guild guild = event.getGuild();
    Member member = event.getMember();

    if (guild == null || member == null) {
      event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
      return;
    }

    String guildId = guild.getId();
    String userId = event.getUser().getId();
    RPGConfig config = characterService.getConfig(guildId);

    if (config.getRpgChannelId() == null) {
      event
          .reply(
              "❌ RPG is not set up for this server. An administrator must run `/admin-rpg-setup` first.")
          .setEphemeral(true)
          .queue();
      return;
    }

    if (!AdminUtils.canUserPlay(member, config.isAllowNoRoleUsers())) {
      event
          .reply(
              "❌ Users without roles cannot use RPG commands in this server. Contact an administrator.")
          .setEphemeral(true)
          .queue();
      return;
    }

    if (!event.getChannel().getId().equals(config.getRpgChannelId())) {
      event
          .reply(
              String.format(
                  "Please use `/rpg-reset-account` in <#%s>. RPG commands are restricted to the assigned channel.",
                  config.getRpgChannelId()))
          .setEphemeral(true)
          .queue();
      return;
    }

    OptionMapping confirmOption = event.getOption("confirm");
    String confirm = confirmOption != null ? confirmOption.getAsString() : null;
    String pendingKey = guildId + ":" + userId;

    if (confirm != null && confirm.equalsIgnoreCase("confirm")) {
      Long timestamp = pendingConfirmations.get(pendingKey);
      if (timestamp == null || (System.currentTimeMillis() - timestamp) > CONFIRMATION_TIMEOUT_MS) {
        event
            .reply(
                """
                                ❌ No active reset request found or confirmation timed out (30 seconds).

                                Run `/rpg-reset-account` again to start over.
                                """)
            .setEphemeral(true)
            .queue();
        return;
      }

      pendingConfirmations.remove(pendingKey);

      boolean hadCharacter = characterService.deleteAllCharactersForUser(guildId, userId);
      bossService.removePlayerDamage(guildId, userId);
      achievementService.releaseClaimsHeldByUser(guildId, userId);
      auraService.removeUserFromAllAuras(guildId, userId);

      event
          .reply(
              String.format(
                  """
                                    🧹 **Your RPG account was reset** in this server.

                                    • Character and history: **%s**
                                    • Boss damage / first-to claims / aura lists: cleaned for your user

                                    Use `/rpg-register` when you are ready to start again.
                                    """,
                  hadCharacter ? "removed" : "nothing to remove (already fresh)"))
          .setEphemeral(true)
          .queue();

      logger.warn("User {} reset their RPG account in guild {}", userId, guildId);
      return;
    }

    pendingConfirmations.put(pendingKey, System.currentTimeMillis());

    boolean hasCharacter = characterService.hasCharacter(guildId, userId);

    event
        .reply(
            String.format(
                """
                                ⚠️ **Reset your RPG account?**

                                This will permanently delete **your** character, stats, inventory, titles, and related data **in this server only**. First-to achievements you hold here will become available again.

                                You have a character: **%s**

                                **This cannot be undone.**

                                To confirm within **30 seconds**, run:
                                `/rpg-reset-account confirm:confirm`
                                """,
                hasCharacter ? "yes" : "no (orphaned data will still be cleaned)"))
        .setEphemeral(true)
        .queue();

    logger.info("User {} pending RPG account reset in guild {}", userId, guildId);
  }

  @Override
  public String getCommandName() {
    return "rpg-reset-account";
  }
}
