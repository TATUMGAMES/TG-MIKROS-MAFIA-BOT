package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.GamePromotion;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler service for posting game promotions at configured intervals.
 * Frequency is based on the guild's promotion verbosity setting.
 */
public class GamePromotionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(GamePromotionScheduler.class);
    
    private final GamePromotionService gamePromotionService;
    private final ScheduledExecutorService scheduler;
    private final Random random;
    private JDA jda;
    
    // Intro message templates
    private static final String[] INTRO_TEMPLATES = {
        "This game is making waves! Have you heard of <game_name>?",
        "If you have not checked out <game_name> do it now.",
        "Here is one of MIKROS' favorite games, are you playing <game_name> already?",
        "Let's support <game_name>. Another amazing game within the MIKROS Ecosystem."
    };
    
    // CTA (Call to Action) templates
    private static final String[] CTA_TEMPLATES = {
        "Where to Get It?:",
        "Play It Today:",
        "Try It Out Today:",
        "Play It Here:"
    };
    
    // Test message intro and CTA
    private static final String TEST_INTRO = "This game is EPIC! Let's rally behind <game_name>";
    private static final String TEST_CTA = "Download It Today:";
    
    /**
     * Creates a new GamePromotionScheduler.
     * 
     * @param gamePromotionService the game promotion service
     */
    public GamePromotionScheduler(GamePromotionService gamePromotionService) {
        this.gamePromotionService = gamePromotionService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.random = new Random();
        logger.info("GamePromotionScheduler initialized");
    }
    
    /**
     * Starts the promotion scheduler.
     * Checks every hour and posts promotions based on guild verbosity settings.
     * 
     * @param jda the JDA instance
     */
    public void start(JDA jda) {
        this.jda = jda;
        
        // Run check every hour
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndPostPromotions();
            } catch (Exception e) {
                logger.error("Error in promotion scheduler", e);
            }
        }, 0, 1, TimeUnit.HOURS);
        
        logger.info("Game promotion scheduler started (checks every hour)");
    }
    
    /**
     * Checks all guilds and posts promotions if it's time based on verbosity.
     */
    private void checkAndPostPromotions() {
        if (jda == null) {
            logger.warn("JDA not initialized, skipping promotion check");
            return;
        }
        
        logger.info("Running scheduled promotion check...");
        
        for (Guild guild : jda.getGuilds()) {
            try {
                checkGuildPromotions(guild);
            } catch (Exception e) {
                logger.error("Error checking promotions for guild {}", guild.getId(), e);
            }
        }
    }
    
    /**
     * Checks and posts promotions for a specific guild.
     * 
     * @param guild the guild to check
     */
    private void checkGuildPromotions(Guild guild) {
        String guildId = guild.getId();
        
        // Get configured promotion channel
        String channelId = gamePromotionService.getPromotionChannel(guildId);
        if (channelId == null) {
            // Guild hasn't set up promotions
            return;
        }
        
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            logger.warn("Configured promotion channel {} not found in guild {}", channelId, guildId);
            return;
        }
        
        // Check if it's time to post based on verbosity
        // For now, we'll post every time the scheduler runs
        // In a production system, you'd track last post time per guild
        
        postPromotionsToChannel(guild, channel);
    }
    
    /**
     * Manually triggers promotion check for a specific guild.
     * Used by the /force-promotion-check command.
     * 
     * @param guild the guild
     * @return number of promotions posted
     */
    public int forceCheckGuild(Guild guild) {
        String guildId = guild.getId();
        
        String channelId = gamePromotionService.getPromotionChannel(guildId);
        if (channelId == null) {
            logger.info("Guild {} has no promotion channel configured", guildId);
            return 0;
        }
        
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            logger.warn("Configured promotion channel {} not found in guild {}", channelId, guildId);
            return 0;
        }
        
        return postPromotionsToChannel(guild, channel);
    }
    
    /**
     * Posts promotions to a channel.
     * 
     * @param guild the guild
     * @param channel the channel to post in
     * @return number of promotions posted
     */
    private int postPromotionsToChannel(Guild guild, TextChannel channel) {
        String guildId = guild.getId();
        
        // Fetch active promotions from API
        List<GamePromotion> promotions = gamePromotionService.fetchActivePromotions();
        
        if (promotions.isEmpty()) {
            logger.debug("No active promotions available for guild {}", guildId);
            return 0;
        }
        
        int posted = 0;
        Instant now = Instant.now();
        
        for (GamePromotion promotion : promotions) {
            // Check if promotion is within campaign date range
            if (!promotion.isWithinCampaignPeriod()) {
                logger.debug("Game {} not within campaign period (start: {}, end: {}, now: {})", 
                        promotion.getGameId(), promotion.getCampaignStartDate(), 
                        promotion.getCampaignEndDate(), now);
                continue;
            }
            
            // Check if enough time has passed since last post (frequency check)
            Instant lastPostTime = gamePromotionService.getLastPostTime(guildId, promotion.getGameId());
            
            if (lastPostTime != null) {
                Instant nextPostTime = lastPostTime.plus(promotion.getFrequencyDays(), ChronoUnit.DAYS);
                if (now.isBefore(nextPostTime)) {
                    logger.debug("Game {} not ready to post yet (last post: {}, next post: {}, frequency: {} days)", 
                            promotion.getGameId(), lastPostTime, nextPostTime, promotion.getFrequencyDays());
                    continue;
                }
            }
            
            // Post the promotion
            try {
                postPromotion(channel, promotion);
                
                // Record post time (this also marks as promoted)
                gamePromotionService.markAsPromoted(guildId, promotion.getGameId());
                
                // Notify backend API (if available)
                gamePromotionService.notifyGamePushed(promotion.getGameId());
                
                posted++;
                
                logger.info("Posted promotion for game {} in guild {}", 
                        promotion.getGameId(), guildId);
                
            } catch (Exception e) {
                logger.error("Failed to post promotion for game {}", promotion.getGameId(), e);
            }
        }
        
        return posted;
    }
    
    /**
     * Posts a single promotion to a channel.
     * 
     * @param channel the channel
     * @param promotion the promotion to post
     */
    private void postPromotion(TextChannel channel, GamePromotion promotion) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎮 " + promotion.getGameName());
        embed.setColor(Color.CYAN);
        
        // Build message using templates
        String introText;
        String ctaText;
        
        // Check if this is the test promotion (game ID 999)
        if (promotion.getGameId() == 999) {
            introText = TEST_INTRO;
            ctaText = TEST_CTA;
        } else {
            introText = selectIntroTemplate();
            ctaText = selectCtaTemplate();
        }
        
        // Replace placeholders in intro
        introText = introText.replace("<game_name>", promotion.getGameName());
        
        // Build the message in the format: "Intro: <intro>\n\nDescription: <description>\n\n<CTA> <url>"
        String message = String.format(
                "Intro: %s\n\nDescription: %s\n\n%s %s",
                introText,
                promotion.getDescription(),
                ctaText,
                promotion.getPromotionUrl()
        );
        
        embed.setDescription(message);
        
        // Add image if available
        if (promotion.getImageUrl() != null && !promotion.getImageUrl().isBlank()) {
            embed.setImage(promotion.getImageUrl());
        }
        
        embed.setFooter("Powered by MIKROS Marketing");
        embed.setTimestamp(Instant.now());
        
        channel.sendMessageEmbeds(embed.build()).queue(
                success -> logger.debug("Successfully posted promotion for game {}", promotion.getGameId()),
                error -> logger.error("Failed to send promotion message", error)
        );
    }
    
    /**
     * Randomly selects an intro template.
     * 
     * @return the selected intro template
     */
    private String selectIntroTemplate() {
        return INTRO_TEMPLATES[random.nextInt(INTRO_TEMPLATES.length)];
    }
    
    /**
     * Randomly selects a CTA template.
     * 
     * @return the selected CTA template
     */
    private String selectCtaTemplate() {
        return CTA_TEMPLATES[random.nextInt(CTA_TEMPLATES.length)];
    }
    
    /**
     * Stops the scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Game promotion scheduler stopped");
    }
}

