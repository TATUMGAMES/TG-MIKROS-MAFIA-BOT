package com.tatumgames.mikros.promo.commands;

import com.tatumgames.mikros.admin.handler.CommandHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handler for /promo-request.
 * Sends a private message with Calendly link for scheduling demos.
 */
public class PromoRequestCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(PromoRequestCommand.class);

    @Override
    public CommandData getCommandData() {
        return Commands.slash("promo-request", "Request MIKROS promotional services and schedule a demo");
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String message = """
                Want a quick MIKROS product walkthrough or campaign setup demo?
                
                📅 Book a 30-min demo here:
                www.calendly.com/tatumgames
                """;

        // Send ephemeral reply (only visible to the user who ran the command)
        event.reply(message)
                .setEphemeral(true)
                .queue(
                        success -> logger.info("Sent promo-request message to user {}", event.getUser().getId()),
                        error -> logger.error("Failed to send promo-request message", error)
                );
    }

    @Override
    public String getCommandName() {
        return "promo-request";
    }
}

