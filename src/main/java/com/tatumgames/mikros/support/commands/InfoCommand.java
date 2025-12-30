package com.tatumgames.mikros.support.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import com.tatumgames.mikros.support.BotInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;

/**
 * Command handler for /info.
 * Provides transparency and trust-building information about the MIKROS Bot.
 */
public class InfoCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(InfoCommand.class);

    @Override
    public CommandData getCommandData() {
        return Commands.slash("info", "Learn about the MIKROS Bot and MIKROS Ecosystem");
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();

        // Title and color
        embed.setTitle("ℹ️ About MIKROS Bot");
        embed.setColor(Color.CYAN);

        // Bot Purpose / Mission
        embed.addField("🎯 Our Mission",
                "MIKROS Bot is part of the MIKROS Ecosystem, helping game developers gain exposure, " +
                        "track campaigns, and connect with the community. Created by Tatum Games, our mission " +
                        "is to empower the next generation of game developers and game studios.",
                false);

        // Developer / Company
        embed.addField("👨‍💻 Bot Developer",
                "Tatum Games\n" +
                        BotInfo.SUPPORT_EMAIL,
                false);

        // Version
        embed.addField("📦 Version",
                BotInfo.VERSION,
                true);

        // Empty field for spacing
        embed.addField("\u200B", "\u200B", true);

        // Links section
        String links = """
                **For Game Developers:**
                👉 https://developer.tatumgames.com/
                
                📺 **Explainer Video:**
                👉 https://youtu.be/8-YXpsTEyAQ
                
                🌐 **Social Media:**
                Twitter: https://twitter.com/tatumgames
                Facebook: http://www.facebook.com/tatumgames
                LinkedIn: https://www.linkedin.com/company/tatum-games-llc
                Founder: https://www.linkedin.com/in/leonard-tatum-768850105/""";

        embed.addField("🔗 Links & Resources", links, false);

        // Optional subtle donation mention at the end
        embed.setFooter("💙 Support MIKROS: https://buy.stripe.com/7sI3cH8m6bmd5ck4gj");

        embed.setTimestamp(Instant.now());

        event.replyEmbeds(embed.build())
                .setEphemeral(true)
                .queue(
                        success -> logger.info("Sent info message to user {}", event.getUser().getId()),
                        error -> logger.error("Failed to send info message", error)
                );
    }

    @Override
    public String getCommandName() {
        return "info";
    }
}

