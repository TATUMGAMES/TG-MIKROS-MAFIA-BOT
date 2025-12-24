package com.tatumgames.mikros.admin.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class AdminUtils {

    /**
     * Validates whether the given target user is invalid for moderation actions.
     * <p>
     * This method performs common checks for moderation commands:
     * <ul>
     *     <li>Whether the target user exists (non-null).</li>
     *     <li>Whether the target user is a bot.</li>
     *     <li>Whether the target user is the same as the executor (self-action).</li>
     * </ul>
     * If any check fails, the method sends an ephemeral error message via the provided event.
     *
     * @param member     The member performing the action (executor/moderator).
     * @param targetUser The target user to validate.
     * @param event      The slash command event used to send error messages.
     * @return {@code true} if the target user is invalid (and an error message has been sent),
     * {@code false} if the target user is valid.
     */
    public static boolean isInvalidTargetUser(Member member, User targetUser, SlashCommandInteractionEvent event) {
        if (targetUser == null) {
            event.reply("❌ You must specify a user.").setEphemeral(true).queue();
            return true;
        }

        if (targetUser.isBot()) {
            event.reply("❌ You cannot take action on a bot.").setEphemeral(true).queue();
            return true;
        }

        if (targetUser.getId().equals(member.getId())) {
            event.reply("❌ You cannot take action on yourself.").setEphemeral(true).queue();
            return true;
        }

        return false;
    }

    /**
     * Safely gets a MessageChannel (TextChannel or NewsChannel) from a slash command option and validates bot permissions.
     *
     * @param event      The slash command event
     * @param optionName The name of the channel option
     * @return The valid MessageChannel, or null if invalid (error reply is sent automatically)
     */
    public static MessageChannel getValidTextChannel(SlashCommandInteractionEvent event, String optionName) {
        OptionMapping channelOption = event.getOption(optionName);

        if (channelOption == null) {
            event.reply("❌ You must select a text channel or announcement channel.")
                    .setEphemeral(true)
                    .queue();
            return null;
        }

        GuildChannel guildChannel = channelOption.getAsChannel();
        MessageChannel messageChannel = null;
        
        if (guildChannel instanceof TextChannel textChannel) {
            messageChannel = textChannel;
        } else if (guildChannel instanceof NewsChannel newsChannel) {
            messageChannel = newsChannel;
        } else {
            event.reply("❌ Please select a text channel or announcement channel.")
                    .setEphemeral(true)
                    .queue();
            return null;
        }

        if (!messageChannel.canTalk()) {
            event.reply("❌ I don't have permission to send messages in " + messageChannel.getAsMention() + ".")
                    .setEphemeral(true)
                    .queue();
            return null;
        }

        return messageChannel;
    }

    /**
     * Checks if a user can play games based on role requirements.
     * <p>
     * A user can play if:
     * <ul>
     *     <li>They have at least one role (excluding @everyone), OR</li>
     *     <li>No-role users are allowed (allowNoRoleUsers = true)</li>
     * </ul>
     * <p>
     * Note: The @everyone role is not counted as a role for this check.
     *
     * @param member           The member to check (can be null)
     * @param allowNoRoleUsers Whether users without roles are allowed to play
     * @return {@code true} if the user can play, {@code false} otherwise
     */
    public static boolean canUserPlay(Member member, boolean allowNoRoleUsers) {
        // If member is null, cannot play
        if (member == null) {
            return false;
        }

        // Check if user has any roles (excluding @everyone)
        // getRoles() returns all roles except @everyone
        boolean hasRoles = !member.getRoles().isEmpty();

        // User can play if they have roles OR no-role users are allowed
        return hasRoles || allowNoRoleUsers;
    }
}
