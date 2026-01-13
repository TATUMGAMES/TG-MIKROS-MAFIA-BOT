package com.tatumgames.mikros.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Centralized HTTP client for all Tatum Games API endpoints.
 * Handles authentication, error handling, retries, and rate limiting.
 */
public class TatumGamesApiClient {
    private static final Logger logger = LoggerFactory.getLogger(TatumGamesApiClient.class);

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;

    private final HttpClient httpClient;
    private final String apiBaseUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new TatumGamesApiClient.
     *
     * @param apiBaseUrl the base URL for the API (e.g., "https://api.tatumgames.com")
     * @param apiKey     the API key for authentication (can be null/empty for mock mode)
     */
    public TatumGamesApiClient(String apiBaseUrl, String apiKey) {
        this.apiBaseUrl = apiBaseUrl != null && !apiBaseUrl.isBlank()
                ? apiBaseUrl
                : "https://api.tatumgames.com";
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .build();

        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("API key not configured - API client will operate in mock mode");
        } else {
            logger.info("TatumGamesApiClient initialized with base URL: {}", this.apiBaseUrl);
        }
    }

    /**
     * Performs a GET request to the API.
     *
     * @param endpoint      the endpoint path (e.g., "/getAllApps")
     * @param responseClass the expected response class
     * @param <T>           the response type
     * @return the parsed response, or null if request failed
     * @throws ApiException if the request fails after retries
     */
    public <T> T get(String endpoint, Class<T> responseClass) throws ApiException {
        return executeWithRetry(() -> {
            HttpRequest request = buildRequest("GET", endpoint, null);
            return executeRequest(request, responseClass);
        });
    }

    /**
     * Performs a POST request to the API.
     *
     * @param endpoint      the endpoint path
     * @param requestBody   the request body object
     * @param responseClass the expected response class
     * @param <T>           the response type
     * @return the parsed response, or null if request failed
     * @throws ApiException if the request fails after retries
     */
    public <T> T post(String endpoint, Object requestBody, Class<T> responseClass) throws ApiException {
        return executeWithRetry(() -> {
            HttpRequest request = buildRequest("POST", endpoint, requestBody);
            return executeRequest(request, responseClass);
        });
    }

    /**
     * Performs a POST request to a custom base URL with X-Apikey header.
     * Used for APIs that require X-Apikey header instead of Authorization: Bearer.
     *
     * @param baseUrl       the base URL for the API (can be different from default)
     * @param endpoint      the endpoint path
     * @param requestBody   the request body object
     * @param apiKey        the API key for X-Apikey header
     * @param responseClass the expected response class
     * @param <T>           the response type
     * @return the parsed response, or null if request failed
     * @throws ApiException if the request fails after retries
     */
    public <T> T postWithApiKey(String baseUrl, String endpoint, Object requestBody,
                                String apiKey, Class<T> responseClass) throws ApiException {
        return executeWithRetry(() -> {
            HttpRequest request = buildRequestWithApiKey("POST", baseUrl, endpoint, requestBody, apiKey);
            return executeRequest(request, responseClass);
        });
    }

    /**
     * Performs a GET request to a custom base URL with X-Apikey header.
     * Used for APIs that require X-Apikey header instead of Authorization: Bearer.
     *
     * @param baseUrl       the base URL for the API (can be different from default)
     * @param endpoint      the endpoint path
     * @param apiKey        the API key for X-Apikey header
     * @param responseClass the expected response class
     * @param <T>           the response type
     * @return the parsed response, or null if request failed
     * @throws ApiException if the request fails after retries
     */
    public <T> T getWithApiKey(String baseUrl, String endpoint, String apiKey, Class<T> responseClass) throws ApiException {
        return executeWithRetry(() -> {
            HttpRequest request = buildRequestWithApiKey("GET", baseUrl, endpoint, null, apiKey);
            return executeRequest(request, responseClass);
        });
    }

    /**
     * Builds an HTTP request with authentication and headers.
     *
     * @param method   the HTTP method
     * @param endpoint the endpoint path
     * @param body     the request body (null for GET requests)
     * @return the HTTP request
     * @throws ApiException if request body serialization fails
     */
    private HttpRequest buildRequest(String method, String endpoint, Object body) throws ApiException {
        String url = apiBaseUrl + endpoint;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .header("Content-Type", "application/json");

        // Add authentication if API key is configured
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }

        // Add request body for POST requests
        if (body != null) {
            try {
                String jsonBody = objectMapper.writeValueAsString(body);
                builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } catch (Exception e) {
                logger.error("Failed to serialize request body", e);
                throw new ApiException("Failed to serialize request body", e);
            }
        } else {
            builder.GET();
        }

        return builder.build();
    }

    /**
     * Builds an HTTP request with X-Apikey header for custom base URLs.
     *
     * @param method   the HTTP method
     * @param baseUrl  the base URL (can be different from default)
     * @param endpoint the endpoint path
     * @param body     the request body (null for GET requests)
     * @param apiKey   the API key for X-Apikey header
     * @return the HTTP request
     * @throws ApiException if request body serialization fails
     */
    private HttpRequest buildRequestWithApiKey(String method, String baseUrl, String endpoint,
                                               Object body, String apiKey) throws ApiException {
        String url = baseUrl + endpoint;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .header("Content-Type", "application/json");

        // Add X-Apikey header if provided
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("X-Apikey", apiKey);
        }

        // Add request body for POST requests
        if (body != null) {
            try {
                String jsonBody = objectMapper.writeValueAsString(body);
                builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } catch (Exception e) {
                logger.error("Failed to serialize request body", e);
                throw new ApiException("Failed to serialize request body", e);
            }
        } else {
            builder.GET();
        }

        return builder.build();
    }

    /**
     * Executes an HTTP request and parses the response.
     *
     * @param request       the HTTP request
     * @param responseClass the expected response class
     * @param <T>           the response type
     * @return the parsed response
     * @throws ApiException if the request fails
     */
    private <T> T executeRequest(HttpRequest request, Class<T> responseClass) throws ApiException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle rate limiting
            if (response.statusCode() == 429) {
                handleRateLimit(response);
                throw new ApiException("Rate limit exceeded", response.statusCode());
            }

            // Handle authentication errors
            if (response.statusCode() == 401) {
                logger.error("API authentication failed - invalid API key");
                throw new ApiException("Authentication failed - invalid API key", response.statusCode());
            }

            // Handle server errors (will be retried)
            if (response.statusCode() >= 500) {
                logger.warn("Server error {}: {}", response.statusCode(), response.body());
                throw new ApiException("Server error: " + response.statusCode(), response.statusCode());
            }

            // Handle client errors (don't retry)
            if (response.statusCode() >= 400) {
                logger.error("Client error {}: {}", response.statusCode(), response.body());
                throw new ApiException("Client error: " + response.statusCode() + " - " + response.body(),
                        response.statusCode());
            }

            // Parse successful response
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String responseBody = response.body();
                if (responseBody == null || responseBody.isBlank()) {
                    logger.warn("Empty response body from API");
                    return null;
                }

                try {
                    return objectMapper.readValue(responseBody, responseClass);
                } catch (Exception e) {
                    logger.error("Failed to parse API response", e);
                    throw new ApiException("Failed to parse response", e);
                }
            }

            throw new ApiException("Unexpected status code: " + response.statusCode(), response.statusCode());

        } catch (IOException e) {
            logger.error("IO error during API request", e);
            throw new ApiException("Network error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Request interrupted", e);
            throw new ApiException("Request interrupted", e);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during API request", e);
            throw new ApiException("Unexpected error", e);
        }
    }

    /**
     * Executes a request with retry logic and exponential backoff.
     *
     * @param requestSupplier the request supplier
     * @param <T>             the response type
     * @return the response
     * @throws ApiException if all retries fail
     */
    private <T> T executeWithRetry(RequestSupplier<T> requestSupplier) throws ApiException {
        ApiException lastException = null;

        // Loop from 1 to MAX_RETRIES (inclusive)
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return requestSupplier.get();
            } catch (ApiException e) {
                lastException = e;

                // Don't retry on client errors (4xx except 429)
                if (e.getStatusCode() >= 400 && e.getStatusCode() < 500 && e.getStatusCode() != 429) {
                    logger.debug("Client error, not retrying: {}", e.getMessage());
                    throw e;
                }

                // Don't retry on last attempt
                if (attempt >= MAX_RETRIES) {
                    break;
                }

                // Calculate exponential backoff delay
                long delayMs = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1);
                logger.debug("Request failed (attempt {}/{}), retrying in {}ms: {}",
                        attempt, MAX_RETRIES, delayMs, e.getMessage());

                try {
                    TimeUnit.MILLISECONDS.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ApiException("Retry interrupted", ie);
                }
            }
        }

        logger.error("Request failed after {} attempts", MAX_RETRIES);
        // lastException is guaranteed to be non-null here since we only reach this point if all attempts failed
        // (if any attempt succeeded, we would have returned earlier)
        throw lastException;
    }

    /**
     * Handles rate limit response by respecting Retry-After header.
     *
     * @param response the HTTP response
     */
    private void handleRateLimit(HttpResponse<?> response) {
        String retryAfter = response.headers().firstValue("Retry-After").orElse(null);
        if (retryAfter != null) {
            try {
                int seconds = Integer.parseInt(retryAfter);
                logger.warn("Rate limited - retry after {} seconds", seconds);
            } catch (NumberFormatException e) {
                logger.warn("Rate limited - invalid Retry-After header: {}", retryAfter);
            }
        } else {
            logger.warn("Rate limited - no Retry-After header provided");
        }
    }

    /**
     * Functional interface for request suppliers.
     */
    @FunctionalInterface
    private interface RequestSupplier<T> {
        T get() throws ApiException;
    }

    /**
     * Exception thrown when API requests fail.
     */
    public static class ApiException extends Exception {
        private final int statusCode;

        public ApiException(String message) {
            super(message);
            this.statusCode = 0;
        }

        public ApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
            this.statusCode = 0;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
