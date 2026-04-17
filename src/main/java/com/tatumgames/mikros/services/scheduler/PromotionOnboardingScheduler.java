package com.tatumgames.mikros.services.scheduler;

import com.tatumgames.mikros.models.PromotionVerbosity;
import com.tatumgames.mikros.services.GamePromotionService;
import com.tatumgames.mikros.services.PromotionOnboardingService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler for promotion channel onboarding phases. Runs every 30 minutes to check guilds and
 * execute appropriate onboarding phases.
 */
public class PromotionOnboardingScheduler {
  private static final Logger logger = LoggerFactory.getLogger(PromotionOnboardingScheduler.class);

  private static final long CHECK_INTERVAL_MINUTES = 30;

  // Channel names to match (case-insensitive, in priority order)
  private static final List<String> PREFERRED_CHANNEL_NAMES =
      Arrays.asList("announcements", "promotions", "game-updates", "community-news");

  // Channel names to never match
  private static final List<String> EXCLUDED_CHANNEL_NAMES =
      Arrays.asList("general", "chat", "off-topic");

  // Channel name keywords for public nudge (case-insensitive, in priority order)
  private static final List<String> PUBLIC_NUDGE_CHANNEL_KEYWORDS =
      Arrays.asList("announcement", "announcements", "admin", "moderator");

  private final PromotionOnboardingService onboardingService;
  private final GamePromotionService gamePromotionService;
  private ScheduledExecutorService scheduler;

  /**
   * Creates a new PromotionOnboardingScheduler.
   *
   * @param onboardingService the onboarding service
   * @param gamePromotionService the game promotion service
   */
  public PromotionOnboardingScheduler(
      PromotionOnboardingService onboardingService, GamePromotionService gamePromotionService) {
    this.onboardingService = onboardingService;
    this.gamePromotionService = gamePromotionService;
  }

  /**
   * Starts the onboarding scheduler.
   *
   * @param jda the JDA instance
   */
  public void start(JDA jda) {
    if (scheduler != null && !scheduler.isShutdown()) {
      logger.warn("Onboarding scheduler already started");
      return;
    }

    scheduler =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "promotion-onboarding-scheduler");
              t.setDaemon(true);
              return t;
            });

    // Run check every 30 minutes
    scheduler.scheduleAtFixedRate(
        () -> {
          try {
            logger.debug("Onboarding check triggered");
            checkAllGuilds(jda);
          } catch (Exception e) {
            logger.error("Error in onboarding check", e);
          }
        },
        0,
        CHECK_INTERVAL_MINUTES,
        TimeUnit.MINUTES);

    logger.info(
        "Promotion onboarding scheduler started (checks every {} minutes)", CHECK_INTERVAL_MINUTES);
  }

  /** Stops the onboarding scheduler. */
  public void stop() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
      logger.info("Promotion onboarding scheduler stopped");
    }
  }

  /**
   * Checks all guilds for onboarding phases.
   *
   * @param jda the JDA instance
   */
  private void checkAllGuilds(JDA jda) {
    for (Guild guild : jda.getGuilds()) {
      try {
        checkGuildOnboarding(guild);
      } catch (Exception e) {
        logger.error("Error checking onboarding for guild {}", guild.getId(), e);
      }
    }
  }

  /**
   * Checks and executes onboarding phases for a specific guild.
   *
   * @param guild the guild to check
   */
  private void checkGuildOnboarding(Guild guild) {
    String guildId = guild.getId();

    // CRITICAL CHECK: Skip all phases if channel is already configured
    if (gamePromotionService.getPromotionChannel(guildId) != null) {
      logger.debug(
          "Guild {} already has promotion channel configured, skipping onboarding", guildId);
      return;
    }

    // Ensure guild is recorded
    onboardingService.recordGuildFirstSeen(guildId);

    // Check and execute each phase
    PromotionOnboardingService.Phase phase1 =
        PromotionOnboardingService.Phase.PHASE_1_SOFT_AWARENESS;
    if (onboardingService.shouldProcessPhase(guildId, phase1)) {
      executePhase1(guild);
      onboardingService.markPhaseCompleted(guildId, phase1);
    }

    PromotionOnboardingService.Phase phase2 = PromotionOnboardingService.Phase.PHASE_2_EXPECTATION;
    if (onboardingService.shouldProcessPhase(guildId, phase2)) {
      executePhase2(guild);
      onboardingService.markPhaseCompleted(guildId, phase2);
    }

    PromotionOnboardingService.Phase phase3 = PromotionOnboardingService.Phase.PHASE_3_AUTO_ASSIST;
    if (onboardingService.shouldProcessPhase(guildId, phase3)) {
      executePhase3(guild);
      onboardingService.markPhaseCompleted(guildId, phase3);
    }

    PromotionOnboardingService.Phase phase4 = PromotionOnboardingService.Phase.PHASE_4_PUBLIC_NUDGE;
    if (onboardingService.shouldProcessPhase(guildId, phase4)) {
      executePhase4(guild);
      onboardingService.markPhaseCompleted(guildId, phase4);
    }

    PromotionOnboardingService.Phase phase5 = PromotionOnboardingService.Phase.PHASE_5_FINAL_DM;
    if (onboardingService.shouldProcessPhase(guildId, phase5)) {
      executePhase5(guild);
      onboardingService.markPhaseCompleted(guildId, phase5);
    }
  }

  /**
   * Executes Phase 1: Soft Awareness (1 hour after first seen). Sends a gentle DM to admins about
   * opt-in promotions.
   *
   * @param guild the guild
   */
  private void executePhase1(Guild guild) {
    String message =
        """
                👋 Thanks for installing MIKROS

                Promotions are opt-in and won't post unless a channel is set.

                When ready, use /admin-promotion-setup to choose a channel.

                You can control frequency anytime.
                """;

    sendDmToAdmins(guild, message);
    logger.info("Executed Phase 1 (Soft Awareness) for guild {}", guild.getId());
  }

  /**
   * Executes Phase 2: Expectation Setting (24 hours after first seen). Informs admins about
   * upcoming auto-assist feature.
   *
   * @param guild the guild
   */
  private void executePhase2(Guild guild) {
    String message =
        """
                We noticed you haven't configured promotions yet.

                We can help by auto-selecting a channel if you have one named:
                • #announcements
                • #promotions
                • #game-updates
                • #community-news

                You're still in control - you can change it anytime with /admin-promotion-setup.
                """;

    sendDmToAdmins(guild, message);
    logger.info("Executed Phase 2 (Expectation Setting) for guild {}", guild.getId());
  }

  /**
   * Executes Phase 3: Auto-Assist (48 hours after first seen). Attempts to auto-assign a promotion
   * channel if a matching name is found.
   *
   * @param guild the guild
   */
  private void executePhase3(Guild guild) {
    String guildId = guild.getId();

    // Double-check channel not configured (could have been set manually)
    if (gamePromotionService.getPromotionChannel(guildId) != null) {
      logger.debug("Guild {} channel configured before Phase 3, skipping auto-assign", guildId);
      return;
    }

    // Search for matching channel
    MessageChannel matchedChannel = findMatchingChannel(guild);

    if (matchedChannel != null) {
      // Auto-assign channel
      gamePromotionService.setPromotionChannel(guildId, matchedChannel.getId());

      // Set default verbosity to MEDIUM
      gamePromotionService.setPromotionVerbosity(guildId, PromotionVerbosity.MEDIUM);

      // Send confirmation DM
      String confirmMessage =
          String.format(
              """
                            ✅ Auto-configured promotion channel: %s

                                    You can change this anytime with /admin-promotion-setup.""",
              matchedChannel.getAsMention());
      sendDmToAdmins(guild, confirmMessage);

      logger.info(
          "Executed Phase 3 (Auto-Assist) for guild {} - auto-assigned channel {}",
          guildId,
          matchedChannel.getId());
    } else {
      logger.info(
          "Executed Phase 3 (Auto-Assist) for guild {} - no matching channel found", guildId);
    }
  }

  /**
   * Finds a matching channel in the guild based on preferred names. Returns the first match found
   * in priority order.
   *
   * @param guild the guild to search
   * @return the matching channel, or null if none found
   */
  private MessageChannel findMatchingChannel(Guild guild) {
    List<TextChannel> textChannels = guild.getTextChannels();

    // Search in priority order
    for (String preferredName : PREFERRED_CHANNEL_NAMES) {
      for (TextChannel channel : textChannels) {
        String channelName = channel.getName().toLowerCase();

        // Check if name matches (case-insensitive)
        if (channelName.equals(preferredName.toLowerCase())) {
          // Verify not in excluded list (shouldn't happen, but double-check)
          if (!EXCLUDED_CHANNEL_NAMES.contains(channelName)) {
            // Verify bot can send messages
            if (channel.canTalk()) {
              return channel;
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * Executes Phase 4: Public Admin Nudge (72 hours after first seen). Sends a public admin-visible
   * message in an appropriate channel.
   *
   * @param guild the guild
   */
  private void executePhase4(Guild guild) {
    String guildId = guild.getId();

    // Double-check channel not configured (could have been set manually)
    if (gamePromotionService.getPromotionChannel(guildId) != null) {
      logger.debug("Guild {} channel configured before Phase 4, skipping public nudge", guildId);
      return;
    }

    // Find appropriate channel for public nudge
    MessageChannel nudgeChannel = findPublicNudgeChannel(guild);
    if (nudgeChannel == null) {
      logger.warn("Could not find appropriate channel for public nudge in guild {}", guildId);
      return;
    }

    // Send public admin nudge
    sendPublicAdminNudge(guild, nudgeChannel);
    logger.info("Executed Phase 4 (Public Admin Nudge) for guild {}", guildId);
  }

  /**
   * Executes Phase 5: Final DM (14 days after first seen). Sends one-time final DM to admins as
   * last reminder.
   *
   * @param guild the guild
   */
  private void executePhase5(Guild guild) {
    String guildId = guild.getId();

    // Double-check channel not configured (could have been set manually)
    if (gamePromotionService.getPromotionChannel(guildId) != null) {
      logger.debug("Guild {} channel configured before Phase 5, skipping final DM", guildId);
      return;
    }

    String message =
        """
                Hey there 👋

                Just following up once — and this will be our last nudge.

                MIKROS has an **optional promotion feature** that helps indie game developers and small studios reach real players through community discovery, not ads.

                If your server enjoys learning about new games, setting up a promotions channel is a small action that can have a big impact.

                If not, no worries at all. We will stay completely quiet unless you decide otherwise.

                You can enable it anytime with:
                **`/admin-promotion-setup`**

                Thanks for supporting healthy game communities 💙
                """;

    sendDmToAdmins(guild, message);
    logger.info("Executed Phase 5 (Final DM) for guild {}", guildId);
  }

  /**
   * Finds an appropriate channel for public admin nudge. Priority order: 1. Channels containing
   * "announcement" or "announcements" 2. Channels containing "admin" or "moderator" 3. First
   * writable system channel 4. First writable text channel (avoid "general" if possible)
   *
   * @param guild the guild to search
   * @return the appropriate channel, or null if none found
   */
  private MessageChannel findPublicNudgeChannel(Guild guild) {
    List<TextChannel> textChannels = guild.getTextChannels();

    // Priority 1: Channels containing announcement keywords
    for (String keyword : PUBLIC_NUDGE_CHANNEL_KEYWORDS) {
      for (TextChannel channel : textChannels) {
        String channelName = channel.getName().toLowerCase();
        if (channelName.contains(keyword.toLowerCase())
            && !EXCLUDED_CHANNEL_NAMES.contains(channelName)
            && channel.canTalk()) {
          return channel;
        }
      }
    }

    // Priority 2: System channel (if writable)
    TextChannel systemChannel = guild.getSystemChannel();
    if (systemChannel != null && systemChannel.canTalk()) {
      return systemChannel;
    }

    // Priority 3: First writable text channel (avoid "general" if possible)
    for (TextChannel channel : textChannels) {
      String channelName = channel.getName().toLowerCase();
      if (!EXCLUDED_CHANNEL_NAMES.contains(channelName) && channel.canTalk()) {
        return channel;
      }
    }

    // Last resort: any writable channel
    for (TextChannel channel : textChannels) {
      if (channel.canTalk()) {
        return channel;
      }
    }

    return null;
  }

  /**
   * Sends a public admin-visible nudge message to a channel. Mentions admins using @admin role if
   * available, otherwise mentions individual admins (limited).
   *
   * @param guild the guild
   * @param channel the channel to send the message to
   */
  private void sendPublicAdminNudge(Guild guild, MessageChannel channel) {
    // Try to find @admin role
    String adminMention = null;
    List<Role> roles = guild.getRolesByName("admin", true);
    if (!roles.isEmpty()) {
      adminMention = roles.get(0).getAsMention();
    } else {
      // Fallback: mention up to 3 admins
      List<Member> admins =
          guild.getMembers().stream()
              .filter(m -> m.hasPermission(Permission.ADMINISTRATOR))
              .filter(m -> !m.getUser().isBot())
              .limit(3)
              .collect(Collectors.toList());

      if (!admins.isEmpty()) {
        adminMention = admins.stream().map(Member::getAsMention).collect(Collectors.joining(" "));
      }
    }

    String header =
        adminMention != null
            ? "👋 " + adminMention + " — Quick Heads-Up from MIKROS"
            : "👋 Hey Admins — Quick Heads-Up from MIKROS";

    String message =
        header
            + "\n\n"
            + "MIKROS includes an **opt-in game discovery feature** that helps **indie game developers and small studios** get visibility they often can't access through traditional ads.\n\n"
            + "By setting up a dedicated promotions channel, you're:\n\n"
            + "• Supporting indie devs building passion projects\n"
            + "• Giving your community a place to discover new games early\n"
            + "• Keeping all promotions organized and non-intrusive\n\n"
            + "Nothing is posted without your approval.\n"
            + "If you'd like to enable it, just run:\n\n"
            + "**`/admin-promotion-setup`**\n\n"
            + "Totally optional but if you care about indie games and discovery, this makes a real difference. 💙🎮";

    channel
        .sendMessage(message)
        .queue(
            success ->
                logger.info(
                    "Sent public admin nudge to channel {} in guild {}",
                    channel.getName(),
                    guild.getId()),
            error ->
                logger.warn(
                    "Failed to send public admin nudge to channel {} in guild {}: {}",
                    channel.getName(),
                    guild.getId(),
                    error.getMessage()));
  }

  /**
   * Sends a DM to all administrators in a guild.
   *
   * @param guild the guild
   * @param message the message to send
   */
  private void sendDmToAdmins(Guild guild, String message) {
    List<Member> admins =
        guild.getMembers().stream()
            .filter(m -> m.hasPermission(Permission.ADMINISTRATOR))
            .filter(m -> !m.getUser().isBot())
            .toList();

    if (admins.isEmpty()) {
      logger.warn("No administrators found in guild {} to send onboarding DM", guild.getId());
      return;
    }

    for (Member admin : admins) {
      admin
          .getUser()
          .openPrivateChannel()
          .queue(
              channel ->
                  channel
                      .sendMessage(message)
                      .queue(
                          success ->
                              logger.debug(
                                  "Sent onboarding DM to admin {} in guild {}",
                                  admin.getId(),
                                  guild.getId()),
                          error ->
                              logger.warn(
                                  "Failed to send onboarding DM to admin {}: {}",
                                  admin.getId(),
                                  error.getMessage())),
              error ->
                  logger.warn(
                      "Failed to open DM channel for admin {}: {}",
                      admin.getId(),
                      error.getMessage()));
    }
  }
}
