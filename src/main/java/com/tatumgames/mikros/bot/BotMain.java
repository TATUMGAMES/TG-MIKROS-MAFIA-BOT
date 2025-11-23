package com.tatumgames.mikros.bot;

import com.tatumgames.mikros.commands.*;
import com.tatumgames.mikros.communitygames.commands.*;
import com.tatumgames.mikros.spelling.commands.SpellGuessCommand;
import com.tatumgames.mikros.rpg.commands.*;
import com.tatumgames.mikros.spelling.commands.*;
import com.tatumgames.mikros.promo.commands.*;
import com.tatumgames.mikros.promo.listener.*;
import com.tatumgames.mikros.honeypot.commands.*;
import com.tatumgames.mikros.honeypot.listener.*;
import com.tatumgames.mikros.honeypot.service.*;
import com.tatumgames.mikros.config.ConfigLoader;
import com.tatumgames.mikros.services.*;
import com.tatumgames.mikros.communitygames.service.*;
import com.tatumgames.mikros.rpg.service.*;
import com.tatumgames.mikros.spelling.service.*;
import com.tatumgames.mikros.promo.service.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
    private final ModerationLogService moderationLogService;
    private final ReputationService reputationService;
    private final ActivityTrackingService activityTrackingService;
    private final MessageAnalysisService messageAnalysisService;
    @SuppressWarnings("unused")
    private final AutoEscalationService autoEscalationService; // Reserved for future auto-escalation features
    private final MonthlyReportService monthlyReportService;
    private final GamePromotionService gamePromotionService;
    private final GamePromotionScheduler gamePromotionScheduler;
    private final GameStatsService gameStatsService;
    private final CommunityGameService communityGameService;
    private final GameResetScheduler gameResetScheduler;
    private final CharacterService characterService;
    private final ActionService actionService;
    private final SpellingChallengeService spellingService;
    private final PromoDetectionService promoService;
    private final PromoMessageListener promoListener;
    private final HoneypotService honeypotService;
    private final MessageDeletionService messageDeletionService;
    private final HoneypotMessageListener honeypotListener;
    
    /**
     * Creates a new BotMain instance.
     */
    public BotMain() {
        this.commandHandlers = new HashMap<>();
        
        // Initialize services
        this.moderationLogService = new InMemoryModerationLogService();
        this.reputationService = new InMemoryReputationService();
        this.activityTrackingService = new ActivityTrackingService();
        this.messageAnalysisService = new MessageAnalysisService();
        this.autoEscalationService = new AutoEscalationService(moderationLogService);
        this.monthlyReportService = new MonthlyReportService(moderationLogService, activityTrackingService);
        this.gamePromotionService = new InMemoryGamePromotionService();
        this.gamePromotionScheduler = new GamePromotionScheduler(gamePromotionService);
        this.gameStatsService = new MockGameStatsService();
        this.communityGameService = new CommunityGameService();
        this.gameResetScheduler = new GameResetScheduler(communityGameService);
        this.characterService = new CharacterService();
        this.actionService = new ActionService();
        this.spellingService = new SpellingChallengeService();
        this.promoService = new PromoDetectionService();
        this.promoListener = new PromoMessageListener(promoService);
        this.honeypotService = new HoneypotService();
        this.messageDeletionService = new MessageDeletionService();
        this.honeypotListener = new HoneypotMessageListener(honeypotService, moderationLogService, messageDeletionService);
        
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
                    .addEventListeners(bot, bot.promoListener, bot.honeypotListener)
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
        registerHandler(new HistoryCommand(moderationLogService, reputationService));
        
        // Admin & Server commands
        registerHandler(new WarnSuggestionsCommand(messageAnalysisService));
        registerHandler(new BanSuggestionsCommand(messageAnalysisService));
        registerHandler(new ServerStatsCommand(activityTrackingService));
        registerHandler(new TopContributorsCommand(activityTrackingService));
        registerHandler(new PraiseCommand(reputationService));
        registerHandler(new ReportCommand(reputationService));
        registerHandler(new ScoreCommand(reputationService));
        
        // Game Promotion commands
        registerHandler(new SetupPromotionChannelCommand(gamePromotionService));
        registerHandler(new SetPromotionVerbosityCommand(gamePromotionService));
        registerHandler(new ForcePromotionCheckCommand(gamePromotionScheduler, gamePromotionService));
        
        // Game Stats/Analytics commands
        registerHandler(new com.tatumgames.mikros.commands.GameStatsCommand(gameStatsService));
        
        // Community Games commands
        registerHandler(new GameSetupCommand(communityGameService, gameResetScheduler));
        registerHandler(new ScrambleGuessCommand(communityGameService));
        registerHandler(new RollCommand(communityGameService));
        registerHandler(new MatchCommand(communityGameService));
        registerHandler(new com.tatumgames.mikros.communitygames.commands.GameStatsCommand(communityGameService));
        registerHandler(new GameConfigCommand(communityGameService));
        
        // RPG System commands
        registerHandler(new RPGRegisterCommand(characterService));
        registerHandler(new RPGProfileCommand(characterService));
        registerHandler(new RPGActionCommand(characterService, actionService));
        registerHandler(new RPGLeaderboardCommand(characterService));
        registerHandler(new RPGConfigCommand(characterService));
        
        // Spelling Challenge commands
        registerHandler(new SpellingChallengeCommand(spellingService));
        registerHandler(new SpellingLeaderboardCommand(spellingService));
        registerHandler(new SpellGuessCommand(spellingService));
        
        // Promo commands
        registerHandler(new PromoHelpCommand(promoService));
        registerHandler(new SetupPromotionsCommand(promoService));
        registerHandler(new SetPromoFrequencyCommand(promoService));
        
        // Honeypot System commands
        registerHandler(new HoneypotCommand(honeypotService));
        registerHandler(new BanAndRemoveCommand(moderationLogService, messageDeletionService));
        registerHandler(new CleanupCommand(messageDeletionService));
        registerHandler(new AlertChannelCommand(honeypotService));
        registerHandler(new ListBansCommand(moderationLogService));
        
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
        
        // Start community game reset scheduler
        gameResetScheduler.start(event.getJDA());
        logger.info("Community game reset scheduler started");
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

