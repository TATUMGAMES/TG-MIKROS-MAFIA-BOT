package com.tatumgames.mikros.promo.commands;

import com.tatumgames.mikros.commands.CommandHandler;
import com.tatumgames.mikros.promo.model.LeadSubmission;
import com.tatumgames.mikros.promo.service.PromoDetectionService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;

/**
 * Command handler for /promo-help.
 * Allows users to request promotional assistance from MIKROS.
 * 
 * TODO: Future Features
 * - Submit lead to API endpoint (see /docs/API_MIKROS_PROMO_SUBMISSION.md)
 * - Generate unique promo codes
 * - Integration with CRM systems
 * - Email collection and validation
 */
public class PromoHelpCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(PromoHelpCommand.class);
    
    /**
     * Creates a new PromoHelpCommand handler.
     * 
     * @param promoService the promotional detection service (unused, reserved for future API integration)
     */
    public PromoHelpCommand(PromoDetectionService promoService) {
        // promoService reserved for future lead submission API integration
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash("promo-help", "Get help promoting your game with MIKROS")
                .addOption(OptionType.STRING, "campaign", "Type of campaign (e.g., Game Launch, Beta Promo)", false)
                .addOption(OptionType.STRING, "email", "Your email (optional)", false)
                .setGuildOnly(true);
    }
    
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String guildId = event.getGuild().getId();
        
        // Get optional parameters
        String campaignType = event.getOption("campaign") != null
                ? event.getOption("campaign").getAsString()
                : "Game Launch";
        String email = event.getOption("email") != null
                ? event.getOption("email").getAsString()
                : null;
        
        // Create lead submission
        // TODO: Submit to lead-capture API
        // See /docs/API_MIKROS_PROMO_SUBMISSION.md for API specification
        @SuppressWarnings("unused")
        LeadSubmission lead = new LeadSubmission(
                user.getId(),
                user.getName(),
                guildId,
                campaignType,
                email,
                Instant.now()
        );
        
        // Send DM to user
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🚀 MIKROS Promotional Help");
        embed.setColor(Color.CYAN);
        embed.setDescription("Thanks for your interest in MIKROS promotional services!");
        
        embed.addField(
                "📋 Your Request",
                String.format(
                        "**Campaign Type:** %s\n" +
                        "**Email:** %s\n" +
                        "**Server:** %s",
                        campaignType,
                        email != null ? email : "Not provided",
                        event.getGuild().getName()
                ),
                false
        );
        
        embed.addField(
                "🎁 What's Next?",
                "Our team will review your request and get back to you soon!\n\n" +
                "**We can help with:**\n" +
                "• Game launch promotions\n" +
                "• Beta testing campaigns\n" +
                "• Store page optimization\n" +
                "• Social media marketing\n" +
                "• Community building\n\n" +
                "Stay tuned for a personalized promo code!",
                false
        );
        
        embed.setFooter("MIKROS - Empowering Game Developers");
        embed.setTimestamp(Instant.now());
        
        // Try to send DM
        user.openPrivateChannel().queue(
                channel -> {
                    channel.sendMessageEmbeds(embed.build()).queue(
                            success -> {
                                event.reply("✅ Check your DMs! I've sent you more information about MIKROS promotional help.")
                                        .setEphemeral(true)
                                        .queue();
                                logger.info("Sent promo help DM to user {} in guild {}", user.getId(), guildId);
                            },
                            error -> {
                                // Fallback to channel reply if DM fails
                                event.replyEmbeds(embed.build())
                                        .setEphemeral(true)
                                        .queue();
                                logger.warn("Failed to send DM to user {}, sent in channel instead", user.getId());
                            }
                    );
                },
                error -> {
                    // Fallback to channel reply if DM fails
                    event.replyEmbeds(embed.build())
                            .setEphemeral(true)
                            .queue();
                    logger.warn("Failed to open DM channel for user {}", user.getId());
                }
        );
    }
    
    @Override
    public String getCommandName() {
        return "promo-help";
    }
}
