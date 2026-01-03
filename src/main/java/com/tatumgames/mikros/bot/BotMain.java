package com.tatumgames.mikros.bot;

import com.tatumgames.mikros.admin.commands.*;
import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.api.TatumGamesApiClient;
import com.tatumgames.mikros.config.ConfigLoader;
import com.tatumgames.mikros.games.rpg.commands.*;
import com.tatumgames.mikros.games.rpg.service.ActionService;
import com.tatumgames.mikros.games.rpg.service.BossScheduler;
import com.tatumgames.mikros.games.rpg.service.AchievementService;
import com.tatumgames.mikros.games.rpg.service.AuraService;
import com.tatumgames.mikros.games.rpg.service.BossService;
import com.tatumgames.mikros.games.rpg.service.CharacterService;
import com.tatumgames.mikros.games.rpg.service.CraftingService;
import com.tatumgames.mikros.games.rpg.service.LoreRecognitionService;
import com.tatumgames.mikros.games.rpg.service.NilfheimEventService;
import com.tatumgames.mikros.games.rpg.service.InMemoryNilfheimEventService;
import com.tatumgames.mikros.games.rpg.service.WorldCurseService;
import com.tatumgames.mikros.games.rpg.scheduler.NilfheimEventScheduler;
import com.tatumgames.mikros.games.word_unscramble.commands.GameConfigCommand;
import com.tatumgames.mikros.games.word_unscramble.commands.GameSetupCommand;
import com.tatumgames.mikros.games.word_unscramble.commands.ScrambleGuessCommand;
import com.tatumgames.mikros.games.word_unscramble.commands.ScrambleLeaderboardCommand;
import com.tatumgames.mikros.games.word_unscramble.commands.ScrambleProfileCommand;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleResetScheduler;
import com.tatumgames.mikros.games.word_unscramble.service.WordUnscrambleService;
import com.tatumgames.mikros.honeypot.commands.*;
import com.tatumgames.mikros.honeypot.listener.HoneypotMessageListener;
import com.tatumgames.mikros.honeypot.service.HoneypotService;
import com.tatumgames.mikros.promo.commands.PromoRequestCommand;
import com.tatumgames.mikros.promo.commands.SetPromoFrequencyCommand;
import com.tatumgames.mikros.promo.commands.SetupPromotionsCommand;
import com.tatumgames.mikros.promo.listener.PromoMessageListener;
import com.tatumgames.mikros.promo.service.PromoDetectionService;
import com.tatumgames.mikros.services.*;
import com.tatumgames.mikros.services.RealGamePromotionService;
import com.tatumgames.mikros.services.PromotionOnboardingService;
import com.tatumgames.mikros.services.scheduler.GamePromotionScheduler;
import com.tatumgames.mikros.services.scheduler.PromotionOnboardingScheduler;
import com.tatumgames.mikros.bump.service.BumpService;
import com.tatumgames.mikros.bump.service.InMemoryBumpService;
import com.tatumgames.mikros.bump.scheduler.BumpScheduler;
import com.tatumgames.mikros.bump.commands.BumpSetupCommand;
import com.tatumgames.mikros.bump.commands.BumpConfigCommand;
import com.tatumgames.mikros.bump.commands.BumpStatsCommand;
import com.tatumgames.mikros.bump.listener.BumpDetectionListener;
import com.tatumgames.mikros.bump.model.BumpConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main entry point for the TG-MIKROS Discord Bot.
 * Initializes the bot, registers commands, and handles events.
 */
public class BotMain extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BotMain.class);

    private final Map<String, CommandHandler> commandHandlers;
    private final ConfigLoader config;
    private final ModerationLogService moderationLogService;
    private final ReputationService reputationService;
    private final ActivityTrackingService activityTrackingService;
    private final MessageAnalysisService messageAnalysisService;
    @SuppressWarnings("unused")
    private final AutoEscalationService autoEscalationService; // Reserved for future auto-escalation features
    private final MonthlyReportService monthlyReportService;
    private final GamePromotionService gamePromotionService;
    private final GamePromotionScheduler gamePromotionScheduler;
    private final PromotionOnboardingService promotionOnboardingService;
    private final PromotionOnboardingScheduler promotionOnboardingScheduler;
    private final com.tatumgames.mikros.tatumtech.scheduler.TatumTechEventScheduler tatumTechEventScheduler;
    private final GameStatsService gameStatsService;
    private final WordUnscrambleService wordUnscrambleService;
    private final WordUnscrambleResetScheduler wordUnscrambleResetScheduler;
    private final CharacterService characterService;
    private final ActionService actionService;
    private final AchievementService achievementService;
    private final AuraService auraService;
    private final WorldCurseService worldCurseService;
    private final BossService bossService;
    private final BossScheduler bossScheduler;
    private final PromoDetectionService promoService;
    private final PromoMessageListener promoListener;
    private final HoneypotService honeypotService;
    private final MessageDeletionService messageDeletionService;
    private final HoneypotMessageListener honeypotListener;
    private final com.tatumgames.mikros.botdetection.service.BotDetectionService botDetectionService;
    private final com.tatumgames.mikros.botdetection.listener.BotDetectionMessageListener botDetectionListener;
    private final BumpService bumpService;
    private final BumpScheduler bumpScheduler;
    private final BumpDetectionListener bumpDetectionListener;
    private final NilfheimEventService nilfheimEventService;
    private final NilfheimEventScheduler nilfheimEventScheduler;
    private final LoreRecognitionService loreRecognitionService;

    /**
     * Creates a new BotMain instance.
     */
    public BotMain() {
        this.commandHandlers = new HashMap<>();

        // Load configuration
        this.config = new ConfigLoader();

        // Initialize API client
        TatumGamesApiClient apiClient = new TatumGamesApiClient(
                config.getMikrosApiUrl(),
                config.getMikrosApiKey()
        );

        // Initialize services
        this.moderationLogService = new InMemoryModerationLogService();
        this.reputationService = new InMemoryReputationService(
                apiClient,
                config.getMikrosApiBaseUrl(), // Use new method instead of getReputationApiUrl()
                config.getReputationApiKey(),
                config.getApiKeyType()
        );
        this.activityTrackingService = new ActivityTrackingService();
        this.messageAnalysisService = new MessageAnalysisService();
        this.autoEscalationService = new AutoEscalationService(moderationLogService);
        this.monthlyReportService = new MonthlyReportService(moderationLogService, activityTrackingService);

        // Initialize game promotion service (use real API if key is configured, otherwise use mock)
        if (config.getMikrosApiKey() != null && !config.getMikrosApiKey().isBlank()) {
            this.gamePromotionService = new RealGamePromotionService(
                    apiClient, 
                    config.getMikrosApiKey(),
                    config.getMikrosApiBaseUrl() // Pass base URL
            );
            logger.info("Using RealGamePromotionService with API integration");
        } else {
            logger.warn("MIKROS_API_KEY not set, using InMemoryGamePromotionService (mock mode)");
            this.gamePromotionService = new InMemoryGamePromotionService();
        }

        this.gamePromotionScheduler = new GamePromotionScheduler(gamePromotionService);
        this.promotionOnboardingService = new PromotionOnboardingService();
        this.promotionOnboardingScheduler = new PromotionOnboardingScheduler(
                promotionOnboardingService,
                gamePromotionService
        );
        this.tatumTechEventScheduler = new com.tatumgames.mikros.tatumtech.scheduler.TatumTechEventScheduler(
                gamePromotionService,
                config.getTatumTechRecapMonthYear(),
                config.getTatumTechRecapVideoUrl()
        );
        this.gameStatsService = new MockGameStatsService();
        this.wordUnscrambleService = new WordUnscrambleService();
        this.wordUnscrambleResetScheduler = new WordUnscrambleResetScheduler(wordUnscrambleService);
        this.characterService = new CharacterService();
        this.achievementService = new AchievementService();
        this.auraService = new AuraService();
        this.worldCurseService = new WorldCurseService();
        this.nilfheimEventService = new InMemoryNilfheimEventService();
        this.loreRecognitionService = new LoreRecognitionService();
        this.bossService = new BossService(characterService, auraService, worldCurseService, nilfheimEventService, loreRecognitionService);
        this.actionService = new ActionService(characterService, worldCurseService, auraService, nilfheimEventService, loreRecognitionService, bossService);
        this.bossScheduler = new BossScheduler(bossService, characterService, worldCurseService);
        this.nilfheimEventScheduler = new NilfheimEventScheduler(nilfheimEventService, characterService);
        this.promoService = new PromoDetectionService();
        this.promoListener = new PromoMessageListener(promoService);
        this.honeypotService = new HoneypotService();
        this.messageDeletionService = new MessageDeletionService();
        this.honeypotListener = new HoneypotMessageListener(honeypotService, moderationLogService, messageDeletionService);
        this.botDetectionService = new com.tatumgames.mikros.botdetection.service.BotDetectionService();
        this.botDetectionListener = new com.tatumgames.mikros.botdetection.listener.BotDetectionMessageListener(
                botDetectionService, reputationService);
        this.bumpService = new InMemoryBumpService();
        this.bumpScheduler = new BumpScheduler(bumpService);
        this.bumpDetectionListener = new BumpDetectionListener(bumpService);

        // Register command handlers
        registerCommandHandlers();
    }

    /**
     * Main method - entry point for the application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        logger.info("Starting TG-MIKROS Discord Bot...");

        try {
            // Load configuration
            ConfigLoader config = new ConfigLoader();

            // Create bot instance
            BotMain bot = new BotMain();

            // Build and start JDA
            JDA jda = JDABuilder.createDefault(config.getBotToken())
                    .enableIntents(
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MODERATION,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .setActivity(Activity.playing("Moderating with style 🎮"))
                    .addEventListeners(bot, bot.promoListener, bot.honeypotListener, bot.botDetectionListener, bot.bumpDetectionListener)
                    .build();

            // Wait for JDA to be ready
            jda.awaitReady();

            logger.info("Bot is now online and ready");

        } catch (Exception e) {
            logger.error("Failed to start bot", e);
            System.exit(1);
        }
    }

    /**
     * Registers all command handlers.
     */
    private void registerCommandHandlers() {
        // Standard Admin moderation commands
        registerHandler(new WarnCommand(moderationLogService, autoEscalationService));
        registerHandler(new KickCommand(moderationLogService));
        registerHandler(new BanCommand(moderationLogService));

        // Admin & Server commands
        registerHandler(new WarnSuggestionsCommand(messageAnalysisService));
        registerHandler(new BanSuggestionsCommand(messageAnalysisService));
        registerHandler(new ServerStatsCommand(activityTrackingService, botDetectionService));
        registerHandler(new TopContributorsCommand(activityTrackingService));
        registerHandler(new PraiseCommand(reputationService));
        registerHandler(new ReportCommand(reputationService));
        registerHandler(new LookupCommand(reputationService, config));

        // Game Promotion commands
        registerHandler(new SetupPromotionChannelCommand(gamePromotionService));
        registerHandler(new PromotionConfigCommand(gamePromotionService, gamePromotionScheduler));

        // Auto-Bump commands
        registerHandler(new BumpSetupCommand(bumpService));
        registerHandler(new BumpConfigCommand(bumpService));
        registerHandler(new BumpStatsCommand(bumpService));

        // Game Stats/Analytics commands
        // TODO: Re-enable when MIKROS Analytics API integration is complete
        // registerHandler(new com.tatumgames.mikros.admin.commands.MikrosEcosystemSetupCommand(gameStatsService));
        // registerHandler(new com.tatumgames.mikros.admin.commands.GameStatsCommand(gameStatsService));

        // Word Unscramble commands
        registerHandler(new GameSetupCommand(wordUnscrambleService, wordUnscrambleResetScheduler));
        registerHandler(new ScrambleGuessCommand(wordUnscrambleService));
        registerHandler(new com.tatumgames.mikros.games.word_unscramble.commands.GameStatsCommand(wordUnscrambleService));
        registerHandler(new ScrambleProfileCommand(wordUnscrambleService));
        registerHandler(new ScrambleLeaderboardCommand(wordUnscrambleService));
        registerHandler(new GameConfigCommand(wordUnscrambleService));

        // RPG System commands
        registerHandler(new RPGRegisterCommand(characterService));
        registerHandler(new RPGProfileCommand(characterService, worldCurseService));
        registerHandler(new RPGActionCommand(characterService, actionService, achievementService, worldCurseService));
        registerHandler(new RPGResurrectCommand(characterService, worldCurseService, loreRecognitionService));
        registerHandler(new RPGBossBattleCommand(characterService, bossService, worldCurseService));
        registerHandler(new RPGLeaderboardCommand(characterService, config));
        registerHandler(new RPGSetupCommand(characterService, bossService));
        registerHandler(new RPGConfigCommand(characterService));
        registerHandler(new RPGResetCommand(characterService, bossService));
        registerHandler(new RPGStatsCommand(characterService));
        registerHandler(new RPGDuelCommand(characterService));
        registerHandler(new RPGInventoryCommand(characterService));
        registerHandler(new RPGCraftCommand(characterService, new CraftingService(loreRecognitionService)));
        // Note: Charge donation is now part of /rpg-action, not a separate command

        // Promo commands
        registerHandler(new SetupPromotionsCommand(promoService));
        registerHandler(new SetPromoFrequencyCommand(promoService));
        registerHandler(new PromoRequestCommand());

        // Support commands
        registerHandler(new com.tatumgames.mikros.support.commands.SupportCommand());
        registerHandler(new com.tatumgames.mikros.support.commands.InfoCommand());

        // Honeypot System commands
        registerHandler(new HoneypotCommand(honeypotService));
        registerHandler(new BanAndRemoveCommand(moderationLogService, messageDeletionService));
        registerHandler(new CleanupCommand(messageDeletionService));
        registerHandler(new AlertChannelCommand(honeypotService));
        registerHandler(new ListBansCommand(moderationLogService));

        // Bot Detection commands
        registerHandler(new com.tatumgames.mikros.botdetection.commands.BotDetectionSetupCommand(botDetectionService));
        registerHandler(new com.tatumgames.mikros.botdetection.commands.BotDetectionConfigCommand(botDetectionService));

        logger.info("Registered {} command handlers", commandHandlers.size());
    }

    /**
     * Registers a single command handler.
     *
     * @param handler the command handler to register
     */
    private void registerHandler(CommandHandler handler) {
        commandHandlers.put(handler.getCommandName(), handler);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("Bot logged in as: {}", event.getJDA().getSelfUser().getName());
        logger.info("Bot is in {} guilds", event.getGuildAvailableCount());

        // Register slash commands globally
        registerSlashCommands(event.getJDA());

        // Start monthly report scheduler
        monthlyReportService.startScheduler(event.getJDA());
        logger.info("Monthly report scheduler started");

        // Start game promotion scheduler
        gamePromotionScheduler.start(event.getJDA());
        logger.info("Game promotion scheduler started");

        // Start Word Unscramble reset scheduler
        wordUnscrambleResetScheduler.start(event.getJDA());
        logger.info("Word Unscramble reset scheduler started");

        // Start boss scheduler
        bossScheduler.start(event.getJDA());
        logger.info("Boss scheduler started");

        // Record all guilds as first seen (if not already recorded)
        for (Guild guild : event.getJDA().getGuilds()) {
            promotionOnboardingService.recordGuildFirstSeen(guild.getId());
        }

        // Start onboarding scheduler
        promotionOnboardingScheduler.start(event.getJDA());
        logger.info("Promotion onboarding scheduler started");

        // Start Tatum Tech event scheduler
        tatumTechEventScheduler.start(event.getJDA());
        logger.info("Tatum Tech event scheduler started");

        // Start bump scheduler
        bumpScheduler.start(event.getJDA());
        logger.info("Bump scheduler started");

        // Start Nilfheim event scheduler
        nilfheimEventScheduler.start(event.getJDA());
        logger.info("Nilfheim event scheduler started");
    }

    /**
     * Registers all slash commands with Discord.
     *
     * @param jda the JDA instance
     */
    private void registerSlashCommands(JDA jda) {
        logger.info("Registering slash commands...");

        try {
            jda.updateCommands()
                    .addCommands(
                            commandHandlers.values().stream()
                                    .map(CommandHandler::getCommandData)
                                    .collect(Collectors.toList())
                    )
                    .queue(
                            success -> logger.info("Successfully registered {} slash commands", commandHandlers.size()),
                            error -> logger.error("Failed to register slash commands", error)
                    );
        } catch (Exception e) {
            logger.error("Error registering slash commands", e);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        CommandHandler handler = commandHandlers.get(commandName);

        if (handler != null) {
            try {
                logger.debug("Handling command: {} from user: {}", commandName, event.getUser().getName());
                handler.handle(event);
            } catch (Exception e) {
                logger.error("Error handling command: {}", commandName, e);

                // Send error message to user
                String errorMessage = "An error occurred while processing your command.";
                if (event.isAcknowledged()) {
                    event.getHook().sendMessage(errorMessage).setEphemeral(true).queue();
                } else {
                    event.reply(errorMessage).setEphemeral(true).queue();
                }
            }
        } else {
            logger.warn("Unknown command: {}", commandName);
            event.reply("Unknown command.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Skip bot messages
        if (event.getAuthor().isBot() || !event.isFromGuild()) {
            return;
        }

        // Track activity for server stats
        activityTrackingService.recordMessage(
                event.getGuild().getId(),
                event.getAuthor().getId(),
                event.getAuthor().getName(),
                event.getChannel().getId()
        );

        // Promotional detection is handled by PromoMessageListener
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        
        if (buttonId.startsWith("bump_copy_")) {
            // Copy command button
            String botName = buttonId.replace("bump_copy_", "");
            BumpConfig.BumpBot bot = BumpConfig.BumpBot.valueOf(botName.toUpperCase());
            
            event.reply(String.format(
                    "📋 **Command to copy:**\n```%s```\n\n" +
                    "Paste this in the channel to bump the server!",
                    bot.getCommand()
            )).setEphemeral(true).queue();
            
        } else if (buttonId.startsWith("bump_mention_")) {
            // Mention bot button
            String botName = buttonId.replace("bump_mention_", "");
            BumpConfig.BumpBot bot = BumpConfig.BumpBot.valueOf(botName.toUpperCase());
            
            String botMention = bot == BumpConfig.BumpBot.DISBOARD
                    ? "<@302050872383242240>"
                    : "<@823495039178932224>";
            
            event.reply(String.format(
                    "👤 **%s Bot:**\n%s\n\n" +
                    "You can mention them or use their slash command!",
                    bot.getDisplayName(),
                    botMention
            )).setEphemeral(true).queue();
        }
    }

    /**
     * Gets the moderation log service instance.
     *
     * @return the moderation log service
     */
    public ModerationLogService getModerationLogService() {
        return moderationLogService;
    }

    /**
     * Gets the reputation service instance.
     *
     * @return the reputation service
     */
    public ReputationService getReputationService() {
        return reputationService;
    }

    /**
     * Gets the activity tracking service instance.
     *
     * @return the activity tracking service
     */
    public ActivityTrackingService getActivityTrackingService() {
        return activityTrackingService;
    }

    /**
     * Gets the game promotion service instance.
     *
     * @return the game promotion service
     */
    public GamePromotionService getGamePromotionService() {
        return gamePromotionService;
    }

    /**
     * Gets the game promotion scheduler instance.
     *
     * @return the game promotion scheduler
     */
    public GamePromotionScheduler getGamePromotionScheduler() {
        return gamePromotionScheduler;
    }
}
