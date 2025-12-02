package com.tatumgames.mikros.services;

import com.tatumgames.mikros.models.MessageSuggestion;
import com.tatumgames.mikros.models.SuggestionSeverity;
import net.dv8tion.jda.api.entities.Message;

import java.util.*;

/**
 * Service for analyzing messages for toxic content, profanity, and other violations.
 * Uses keyword-based filtering (expandable to NLP in the future).
 */
public class MessageAnalysisService {

    // Critical severity - immediate ban consideration
    private static final Set<String> CRITICAL_KEYWORDS = Set.of(
            "n-word", "f-slur", "kys", "kill yourself",
            "extreme-hate", "doxx", "swat"
    );

    // High severity - kick consideration
    private static final Set<String> HIGH_SEVERITY_KEYWORDS = Set.of(
            "retard", "rape", "nazi", "hitler",
            "gore", "die", "death threat"
    );

    // Medium severity - warning consideration
    private static final Set<String> MEDIUM_SEVERITY_KEYWORDS = Set.of(
            "fuck", "shit", "bitch", "ass",
            "damn", "crap", "piss", "idiot",
            "stupid", "dumb", "loser"
    );

    // Spam patterns
    private static final int MASS_PING_THRESHOLD = 5;
    private static final int REPEATED_MESSAGE_THRESHOLD = 3;

    /**
     * Analyzes a message for toxic content.
     *
     * @param message the message to analyze
     * @return a MessageSuggestion if the message is flagged, null otherwise
     */
    public MessageSuggestion analyzeMessage(Message message) {
        if (message == null || message.getAuthor().isBot()) {
            return null;
        }

        String content = message.getContentRaw().toLowerCase();

        // Check for critical violations
        for (String keyword : CRITICAL_KEYWORDS) {
            if (content.contains(keyword)) {
                return createSuggestion(message, keyword, SuggestionSeverity.CRITICAL,
                        "Contains severely inappropriate content: " + keyword);
            }
        }

        // Check for high severity violations
        for (String keyword : HIGH_SEVERITY_KEYWORDS) {
            if (content.contains(keyword)) {
                return createSuggestion(message, keyword, SuggestionSeverity.HIGH,
                        "Contains inappropriate content: " + keyword);
            }
        }

        // Check for mass mentions
        int mentionCount = message.getMentions().getUsers().size() +
                message.getMentions().getRoles().size();
        if (mentionCount >= MASS_PING_THRESHOLD) {
            return createSuggestion(message, "@everyone/@role spam", SuggestionSeverity.HIGH,
                    String.format("Mass pinging (%d mentions)", mentionCount));
        }

        // Check for medium severity violations
        for (String keyword : MEDIUM_SEVERITY_KEYWORDS) {
            if (content.contains(keyword)) {
                return createSuggestion(message, keyword, SuggestionSeverity.MEDIUM,
                        "Contains profanity: " + keyword);
            }
        }

        // Check for all caps (yelling)
        if (isAllCaps(content) && content.length() > 20) {
            return createSuggestion(message, "ALL CAPS", SuggestionSeverity.LOW,
                    "Excessive use of capital letters");
        }

        return null;
    }

    /**
     * Analyzes a list of messages and returns suggestions.
     *
     * @param messages       the messages to analyze
     * @param maxSuggestions maximum number of suggestions to return
     * @return list of message suggestions
     */
    public List<MessageSuggestion> analyzeMessages(List<Message> messages, int maxSuggestions) {
        List<MessageSuggestion> suggestions = new ArrayList<>();
        Map<String, Integer> userMessageCount = new HashMap<>();
        Map<String, String> userLastMessage = new HashMap<>();

        for (Message message : messages) {
            if (message.getAuthor().isBot()) {
                continue;
            }

            String userId = message.getAuthor().getId();
            String content = message.getContentRaw().toLowerCase();

            // Check for spam (repeated messages)
            userMessageCount.merge(userId, 1, Integer::sum);
            String lastMessage = userLastMessage.get(userId);
            if (lastMessage != null && lastMessage.equals(content) &&
                    userMessageCount.get(userId) >= REPEATED_MESSAGE_THRESHOLD) {
                suggestions.add(createSuggestion(message, "repeated message", SuggestionSeverity.HIGH,
                        "Spamming repeated messages"));
                if (suggestions.size() >= maxSuggestions) {
                    break;
                }
            }
            userLastMessage.put(userId, content);

            // Analyze individual message
            MessageSuggestion suggestion = analyzeMessage(message);
            if (suggestion != null) {
                suggestions.add(suggestion);
                if (suggestions.size() >= maxSuggestions) {
                    break;
                }
            }
        }

        // Sort by severity (critical first)
        suggestions.sort(Comparator.comparing(MessageSuggestion::severity));

        return suggestions;
    }

    /**
     * Creates a MessageSuggestion from a flagged message.
     */
    private MessageSuggestion createSuggestion(
            Message message,
            String flaggedContent,
            SuggestionSeverity severity,
            String reason
    ) {
        String messageLink = String.format(
                "https://discord.com/channels/%s/%s/%s",
                message.getGuild().getId(),
                message.getChannel().getId(),
                message.getId()
        );

        String content = message.getContentRaw();
        String snippet = content.length() > 100 ? content.substring(0, 97) + "..." : content;

        return new MessageSuggestion(
                message.getId(),
                message.getAuthor().getId(),
                message.getAuthor().getName(),
                message.getChannel().getId(),
                message.getChannel().getName(),
                content,
                snippet,
                messageLink,
                severity,
                reason
        );
    }

    /**
     * Checks if a message is all caps (excluding punctuation and numbers).
     */
    private boolean isAllCaps(String content) {
        String lettersOnly = content.replaceAll("[^a-zA-Z]", "");
        if (lettersOnly.length() < 10) {
            return false;
        }
        return lettersOnly.equals(lettersOnly.toUpperCase());
    }
}

