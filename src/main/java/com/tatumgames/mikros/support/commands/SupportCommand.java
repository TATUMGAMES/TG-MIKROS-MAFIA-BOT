package com.tatumgames.mikros.support.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * Command handler for /support.
 * Provides information about supporting the MIKROS Bot development.
 * Randomly rotates between three message versions.
 */
public class SupportCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SupportCommand.class);
    // Message version 1: Detailed explanation with hosting costs mention
    private static final String MIKROS_BOT_DONATION_MSG_1 = """
            💙 Support the MIKROS Bot
            
            Thank you for being a participant of the MIKROS Bot!
            This bot was created to help indie game developers and small game studios gain exposure while bringing fun, engagement, and moderation tools to Discord communities.
            
            If you'd like to support ongoing development and hosting costs, you have two options:
            
            🚀 MIKROS Mafia Members
            Upgraded roles and perks are available for active members of MIKROS Mafia:
            👉 https://payrole.io/app/store?id=65e729cff968f40012f47835&nojoin
            
            ☕ One-Time Support
            Not part of MIKROS Mafia? You can still support development with a one-time donation:
            👉 https://buy.stripe.com/7sI3cH8m6bmd5ck4gj
            
            Every bit of support helps keep the MIKROS Ecosystem alive, improving, and growing. Thank you! 🙏
            """;
    // Message version 2: Concise and friendly
    private static final String MIKROS_BOT_DONATION_MSG_2 = """
            ⭐ Support MIKROS
            
            The MIKROS Ecosystem is built to support game communities, indie game developers, and creators. 
            
            If you'd like to help:
            
            🔓 MIKROS Mafia (Roles & Perks)
            👉 https://payrole.io/app/store?id=65e729cff968f40012f47835&nojoin
            
            (Applies to active Mafia members only)
            
            ☕ Buy Us a Coffee
            👉 https://buy.stripe.com/7sI3cH8m6bmd5ck4gj
            
            Support is always optional, but deeply appreciated 💙
            """;
    // Message version 3: Feature-focused with clear value proposition
    private static final String MIKROS_BOT_DONATION_MSG_3 = """
            🎯 Why Support the MIKROS Bot?
            
            MIKROS exists to:
            • Promote indie games & small game studios
            • Improve server moderation & engagement
            • Provide industry insights & analytics
            • Deliver fun systems like RPGs, daily games, and reputation tracking
            
            Running and improving MIKROS Bot requires ongoing hosting and development time.
            If you'd like to help keep it alive and growing:
            
            🛡️ MIKROS Mafia Support (Roles & Benefits)
            👉 https://payrole.io/app/store?id=65e729cff968f40012f47835&nojoin
            
            ☕ One-Time Donation
            👉 https://buy.stripe.com/7sI3cH8m6bmd5ck4gj
            
            Supporting is optional. Using MIKROS is always free ❤️
            """;
    private static final List<String> SUPPORT_MESSAGES = List.of(
            MIKROS_BOT_DONATION_MSG_1,
            MIKROS_BOT_DONATION_MSG_2,
            MIKROS_BOT_DONATION_MSG_3
    );
    private final Random random = new Random();

    @Override
    public CommandData getCommandData() {
        return Commands.slash("support", "Learn how to support the MIKROS Bot development");
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Randomly select one of the three message versions
        String selectedMessage = SUPPORT_MESSAGES.get(random.nextInt(SUPPORT_MESSAGES.size()));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setDescription(selectedMessage);
        embed.setColor(Color.CYAN);
        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build())
                .setEphemeral(true)
                .queue(
                        success -> logger.info("Sent support message (version {}) to user {}",
                                SUPPORT_MESSAGES.indexOf(selectedMessage) + 1, event.getUser().getId()),
                        error -> logger.error("Failed to send support message", error)
                );
    }

    @Override
    public String getCommandName() {
        return "support";
    }
}

