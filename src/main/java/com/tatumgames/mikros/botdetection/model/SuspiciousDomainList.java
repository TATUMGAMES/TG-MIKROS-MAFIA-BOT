package com.tatumgames.mikros.botdetection.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages lists of suspicious domains and TLDs.
 * Includes static lists and dynamic domain tracking.
 */
public class SuspiciousDomainList {
    private static final SuspiciousDomainList INSTANCE = new SuspiciousDomainList();

    // Static list of suspicious TLDs
    private static final Set<String> SUSPICIOUS_TLDS = Set.of(
            ".ru", ".xyz", ".top", ".click", ".tk", ".ml", ".ga", ".cf",
            ".gq", ".pw", ".bid", ".download", ".stream", ".review"
    );

    // Static list of known URL shorteners
    private static final Set<String> URL_SHORTENERS = Set.of(
            "bit.ly", "tinyurl.com", "goo.gl", "t.co", "short.link",
            "goo.su", "tiny.cc", "is.gd", "ow.ly", "buff.ly", "rebrand.ly",
            "shorturl.at", "cutt.ly", "v.gd"
    );

    // Dynamic domain risk scoring: domain -> risk score
    private final ConcurrentHashMap<String, Integer> domainRiskScores;

    private SuspiciousDomainList() {
        this.domainRiskScores = new ConcurrentHashMap<>();
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static SuspiciousDomainList getInstance() {
        return INSTANCE;
    }

    /**
     * Checks if a domain has a suspicious TLD.
     *
     * @param domain the domain to check
     * @return true if domain has suspicious TLD, false otherwise
     */
    public boolean isSuspiciousTld(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        String lowerDomain = domain.toLowerCase();
        return SUSPICIOUS_TLDS.stream().anyMatch(lowerDomain::endsWith);
    }

    /**
     * Checks if a domain is a known URL shortener.
     *
     * @param domain the domain to check
     * @return true if domain is a URL shortener, false otherwise
     */
    public boolean isUrlShortener(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        String lowerDomain = domain.toLowerCase();
        return URL_SHORTENERS.contains(lowerDomain) ||
                URL_SHORTENERS.stream().anyMatch(lowerDomain::contains);
    }

    /**
     * Adds a suspicious domain with a risk score.
     *
     * @param domain     the domain
     * @param riskScore  the risk score (higher = more suspicious)
     */
    public void addSuspiciousDomain(String domain, int riskScore) {
        if (domain != null && !domain.isBlank()) {
            domainRiskScores.put(domain.toLowerCase(), riskScore);
        }
    }

    /**
     * Gets the risk score for a domain.
     *
     * @param domain the domain
     * @return the risk score, or 0 if not found
     */
    public int getDomainRiskScore(String domain) {
        if (domain == null || domain.isBlank()) {
            return 0;
        }
        return domainRiskScores.getOrDefault(domain.toLowerCase(), 0);
    }

    /**
     * Checks if a domain is suspicious (either static list or dynamic with high risk).
     *
     * @param domain the domain to check
     * @return true if domain is suspicious, false otherwise
     */
    public boolean isSuspicious(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        return isSuspiciousTld(domain) ||
                isUrlShortener(domain) ||
                getDomainRiskScore(domain) >= 3;
    }

    /**
     * Removes a domain from the dynamic list.
     *
     * @param domain the domain to remove
     */
    public void removeSuspiciousDomain(String domain) {
        if (domain != null && !domain.isBlank()) {
            domainRiskScores.remove(domain.toLowerCase());
        }
    }
}

