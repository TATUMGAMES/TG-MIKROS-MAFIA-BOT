package com.tatumgames.mikros.botdetection.util;

import com.tatumgames.mikros.botdetection.model.SuspiciousDomainList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for detecting and extracting links from messages.
 */
public class LinkDetectionUtil {
    // Pattern to match URLs: http://, https://, or discord.gg
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?i)(https?://[^\\s]+|discord\\.gg/[^\\s]+)",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern to extract domain from URL
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "(?i)(?:https?://)?(?:www\\.)?([^/\\s:]+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Checks if a message contains any links.
     *
     * @param message the message content
     * @return true if message contains a link, false otherwise
     */
    public static boolean containsLink(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        return URL_PATTERN.matcher(message).find();
    }

    /**
     * Extracts all URLs from a message.
     *
     * @param message the message content
     * @return list of URLs found in the message
     */
    public static List<String> extractUrls(String message) {
        List<String> urls = new ArrayList<>();
        if (message == null || message.isBlank()) {
            return urls;
        }

        Matcher matcher = URL_PATTERN.matcher(message);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        return urls;
    }

    /**
     * Extracts the domain from a URL.
     *
     * @param url the URL
     * @return the domain, or null if extraction fails
     */
    public static String extractDomain(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        Matcher matcher = DOMAIN_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        return null;
    }

    /**
     * Checks if a domain is a known URL shortener.
     *
     * @param domain the domain to check
     * @return true if domain is a URL shortener, false otherwise
     */
    public static boolean isUrlShortener(String domain) {
        if (domain == null) {
            return false;
        }
        return SuspiciousDomainList.getInstance().isUrlShortener(domain);
    }

    /**
     * Checks if a domain has a suspicious TLD.
     *
     * @param domain the domain to check
     * @return true if domain has suspicious TLD, false otherwise
     */
    public static boolean isSuspiciousTld(String domain) {
        if (domain == null) {
            return false;
        }
        return SuspiciousDomainList.getInstance().isSuspiciousTld(domain);
    }
}

