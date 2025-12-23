# TASKS_29: Real API Integration for trackUserRating Endpoint

## Objective
Replace the stub implementation with real API calls to the `/trackUserRating` endpoint. All the logic for creating requests from behavior reports is already in place - we just need to make the actual HTTP call instead of reading from stub JSON.

## Overview
Currently:
- `InMemoryReputationService.trackPlayerRating()` reads from stub JSON file
- `reportToExternalAPI()` correctly creates `TrackPlayerRatingRequest` from `BehaviorReport`
- Request model has correct structure but `platform` is hardcoded to "ios" (should be "discord")
- Hardcoded values: `apiKeyType = "prod"`, `appVersion = "1.0.0"`, `platform = "discord"` (needs update)

We need to:
1. Update `TrackPlayerRatingRequest` to set `platform = "discord"` in `reportToExternalAPI()`
2. Create/use API client to make real HTTP POST requests
3. Update endpoint URL to the correct one
4. Use `X-Apikey` header (not `Authorization: Bearer`)
5. Handle response properly
6. Add API key and URL configuration

---

## Implementation Plan

### Phase 1: Update TrackPlayerRatingRequest Platform

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Changes:**
1. In `reportToExternalAPI()` method, after creating the request, set platform to "discord":
   ```java
   // Set platform to "discord" (currently defaults to "ios")
   request.setPlatform("discord");
   ```

**Location:** After line 94, before calling `trackPlayerRating(request)`

**Note:** The hardcoded values `apiKeyType = "prod"` and `appVersion = "1.0.0"` are already set in the model's default values, so they don't need to be explicitly set. Only `platform` needs to be updated.

---

### Phase 2: Create or Use API Client

**Option A: Use Centralized API Client (If TASKS_28 is Complete)**

If `TatumGamesApiClient` from TASKS_28 exists:
- Inject `TatumGamesApiClient` into `InMemoryReputationService`
- Use `apiClient.post()` method

**Option B: Create Simple HTTP Client (If TASKS_28 Not Complete)**

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Changes:**
1. Add HTTP client field:
   ```java
   private final HttpClient httpClient;
   ```

2. Initialize in constructor:
   ```java
   this.httpClient = HttpClient.newBuilder()
       .connectTimeout(Duration.ofSeconds(10))
       .build();
   ```

3. Add API configuration fields (or inject from ConfigLoader):
   ```java
   private final String reputationApiUrl;
   private final String reputationApiKey;
   ```

**Location:** Add fields after line 31, initialize in constructor after line 38

---

### Phase 3: Update Configuration Loader

**File:** `src/main/java/com/tatumgames/mikros/config/ConfigLoader.java`

**Changes:**
1. Add reputation API configuration fields:
   ```java
   private final String reputationApiKey;
   private final String reputationApiUrl;
   ```

2. Load from environment variables:
   ```java
   this.reputationApiKey = getEnv("REPUTATION_API_KEY", "");
   this.reputationApiUrl = getEnv("REPUTATION_API_URL", 
       "https://tg-api-new-stage.uc.r.appspot.com/mikros/marketing/discord");
   ```

3. Add getter methods:
   ```java
   public String getReputationApiKey() {
       return reputationApiKey;
   }
   
   public String getReputationApiUrl() {
       return reputationApiUrl;
   }
   ```

**Location:** Add after existing fields, around line 18

**Note:** API key is optional - can fall back to stub if not configured (for backward compatibility)

---

### Phase 4: Update InMemoryReputationService Constructor

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Changes:**
1. Add constructor parameters for API configuration:
   ```java
   private final String reputationApiUrl;
   private final String reputationApiKey;
   private final HttpClient httpClient;
   
   public InMemoryReputationService(String reputationApiUrl, String reputationApiKey) {
       this.reportStore = new ConcurrentHashMap<>();
       this.objectMapper = new ObjectMapper();
       this.reputationApiUrl = reputationApiUrl;
       this.reputationApiKey = reputationApiKey;
       this.httpClient = HttpClient.newBuilder()
           .connectTimeout(Duration.ofSeconds(10))
           .build();
       logger.info("InMemoryReputationService initialized");
   }
   ```

2. **OR** if using centralized API client:
   ```java
   private final TatumGamesApiClient apiClient;
   
   public InMemoryReputationService(TatumGamesApiClient apiClient) {
       this.reportStore = new ConcurrentHashMap<>();
       this.objectMapper = new ObjectMapper();
       this.apiClient = apiClient;
       logger.info("InMemoryReputationService initialized");
   }
   ```

**Location:** Update constructor starting at line 36

---

### Phase 5: Implement Real API Call

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Changes:**
1. Replace `trackPlayerRating()` method implementation:

**Option A: Using Simple HTTP Client**
```java
@Override
public boolean trackPlayerRating(TrackPlayerRatingRequest request) {
    // If API key not configured, fall back to stub
    if (reputationApiKey == null || reputationApiKey.isBlank()) {
        logger.warn("Reputation API key not configured, using stub response");
        return loadStubResponse();
    }
    
    try {
        // Build request URL
        String endpoint = reputationApiUrl + "/trackUserRating";
        URI uri = URI.create(endpoint);
        
        // Serialize request to JSON
        String requestBody = objectMapper.writeValueAsString(request);
        
        // Build HTTP request
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(uri)
            .header("X-Apikey", reputationApiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(10))
            .build();
        
        // Execute request
        HttpResponse<String> response = httpClient.send(httpRequest, 
            HttpResponse.BodyHandlers.ofString());
        
        // Parse response
        if (response.statusCode() == 200) {
            TrackPlayerRatingResponse apiResponse = objectMapper.readValue(
                response.body(), TrackPlayerRatingResponse.class);
            
            if (apiResponse.getStatus() != null && 
                apiResponse.getStatus().getStatusCode() == 200) {
                logger.info("Successfully tracked player rating via API: {}", request);
                return true;
            } else {
                logger.warn("API returned non-200 status: {}", apiResponse.getStatus());
                return false;
            }
        } else {
            logger.error("HTTP error {} when calling trackUserRating: {}", 
                response.statusCode(), response.body());
            return false;
        }
        
    } catch (IOException e) {
        logger.error("IO error calling trackUserRating API", e);
        return false;
    } catch (InterruptedException e) {
        logger.error("Request interrupted when calling trackUserRating API", e);
        Thread.currentThread().interrupt();
        return false;
    } catch (Exception e) {
        logger.error("Unexpected error calling trackUserRating API", e);
        return false;
    }
}

private boolean loadStubResponse() {
    // Existing stub loading logic
    try {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("stubs/trackPlayerRating.json");
        // ... rest of stub logic
    } catch (Exception e) {
        logger.error("Failed to load stub JSON file for trackPlayerRating", e);
        return false;
    }
}
```

**Option B: Using Centralized API Client**
```java
@Override
public boolean trackPlayerRating(TrackPlayerRatingRequest request) {
    try {
        TrackPlayerRatingResponse response = apiClient.post(
            "/trackUserRating", 
            request, 
            TrackPlayerRatingResponse.class
        );
        
        if (response != null && response.getStatus() != null && 
            response.getStatus().getStatusCode() == 200) {
            logger.info("Successfully tracked player rating via API: {}", request);
            return true;
        } else {
            logger.warn("API returned non-200 status: {}", 
                response != null ? response.getStatus() : "null response");
            return false;
        }
        
    } catch (Exception e) {
        logger.error("Error calling trackUserRating API", e);
        return false;
    }
}
```

**Location:** Replace entire `trackPlayerRating()` method starting at line 101

**Key Points:**
- Use `X-Apikey` header (not `Authorization: Bearer`)
- Endpoint: `/trackUserRating` (note: actual endpoint name, not `trackPlayerRating`)
- Full URL: `{reputationApiUrl}/trackUserRating`
- Handle HTTP errors gracefully
- Fall back to stub if API key not configured
- Log all errors appropriately

---

### Phase 6: Update BotMain to Pass Configuration

**File:** `src/main/java/com/tatumgames/mikros/bot/BotMain.java`

**Changes:**
1. Update `InMemoryReputationService` initialization:

**Option A: Simple HTTP Client**
```java
// In constructor or initialization method
ConfigLoader config = new ConfigLoader();
this.reputationService = new InMemoryReputationService(
    config.getReputationApiUrl(),
    config.getReputationApiKey()
);
```

**Option B: Centralized API Client**
```java
// If using centralized API client
this.reputationService = new InMemoryReputationService(apiClient);
```

**Location:** Around line 79, update service initialization

---

### Phase 7: Handle API Client Header Configuration

**Important:** If using centralized API client from TASKS_28, ensure it supports custom headers.

**If TatumGamesApiClient uses `Authorization: Bearer`:**
- Either update `TatumGamesApiClient` to support `X-Apikey` header
- Or create a specialized method for reputation API calls
- Or add header configuration option to API client

**Alternative:** Create a separate method in API client for reputation endpoints that use `X-Apikey`:
```java
public <T> T postWithApiKey(String endpoint, Object requestBody, 
                           String apiKey, Class<T> responseClass) {
    // Similar to post() but uses X-Apikey header instead of Authorization
}
```

---

### Phase 8: Error Handling and Resilience

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Changes:**
1. Ensure graceful degradation:
   - If API call fails, log error but don't crash
   - Command should still succeed (report is stored locally)
   - User gets confirmation message

2. Add retry logic (optional but recommended):
   ```java
   private boolean trackPlayerRatingWithRetry(TrackPlayerRatingRequest request) {
       int maxRetries = 3;
       for (int attempt = 1; attempt <= maxRetries; attempt++) {
           boolean success = trackPlayerRating(request);
           if (success) {
               return true;
           }
           
           if (attempt < maxRetries) {
               try {
                   Thread.sleep(1000 * attempt); // Exponential backoff
               } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
                   return false;
               }
           }
       }
       return false;
   }
   ```

3. Update `reportToExternalAPI()` to use retry version if implemented

**Location:** Add retry method, update `reportToExternalAPI()` if using retries

---

### Phase 9: Update Response Model (If Needed)

**File:** `src/main/java/com/tatumgames/mikros/models/api/TrackPlayerRatingResponse.java`

**Verify:**
- Response model matches actual API response structure
- Status code parsing works correctly
- Error handling covers all response scenarios

**If response structure differs:**
- Update model fields to match actual API response
- Ensure Jackson annotations are correct

---

### Phase 10: Code Cleanup

**Tasks:**
1. Remove unused stub loading code (or keep as fallback)
2. Remove unused imports
3. Update Javadoc comments to reflect real API usage
4. Update logging messages to indicate real API calls
5. Remove TODO comments that are now implemented

---

## Configuration Requirements

### Environment Variables

Add to `.env` file:
```env
# Reputation API Configuration (optional - falls back to stub if not set)
REPUTATION_API_KEY=your_api_key_here
REPUTATION_API_URL=https://tg-api-new-stage.uc.r.appspot.com/mikros/marketing/discord
```

**Note:** If `REPUTATION_API_KEY` is not set, the service will fall back to stub JSON for backward compatibility.

---

## API Details

### Endpoint
- **URL:** `https://tg-api-new-stage.uc.r.appspot.com/mikros/marketing/discord/trackUserRating`
- **Method:** `POST`
- **Header:** `X-Apikey: {api_key}`
- **Content-Type:** `application/json`

### Request Structure
The `TrackPlayerRatingRequest` model already matches the API structure:
- `apiKeyType`: "prod" (hardcoded)
- `appVersion`: "1.0.0" (hardcoded)
- `platform`: "discord" (needs to be set in code)
- `timestamp`: Formatted as "yyyy-MM-dd HH:mm:ss"
- `sender`: Contains `discordUserId` and `discordUsername`
- `participants`: Array of participants with `discordUserId`, `discordUsername`, and `value`

### Response Structure
Expected response matches `TrackPlayerRatingResponse` model with status object containing `statusCode` and `statusMessage`.

---

## Testing Checklist

- [ ] API call is made with correct URL
- [ ] `X-Apikey` header is set correctly
- [ ] `platform` is set to "discord" in request
- [ ] Request body is correctly serialized to JSON
- [ ] Response is correctly parsed
- [ ] HTTP 200 response is handled correctly
- [ ] HTTP error responses (4xx, 5xx) are handled gracefully
- [ ] Network errors are handled gracefully
- [ ] Fallback to stub works when API key not configured
- [ ] Commands (`/praise`, `/report`) still work correctly
- [ ] Error messages are logged appropriately
- [ ] User receives confirmation even if API call fails

---

## Edge Cases to Handle

1. **API Key Not Configured:** Fall back to stub, log warning, continue operation
2. **Network Timeout:** Log error, return false, don't crash
3. **HTTP 401 (Unauthorized):** Log error, don't retry, return false
4. **HTTP 429 (Rate Limited):** Log warning, could implement retry with backoff
5. **HTTP 500 (Server Error):** Log error, could implement retry
6. **Malformed Response:** Log error, return false, don't crash
7. **Request Serialization Error:** Log error, return false

---

## Notes

- All the logic for creating requests from `BehaviorReport` is already implemented in `reportToExternalAPI()`
- The request model structure matches the API requirements
- Only need to replace stub JSON reading with real HTTP call
- Platform needs to be set to "discord" (currently defaults to "ios")
- Hardcoded values (`apiKeyType`, `appVersion`) are already correct
- API uses `X-Apikey` header, not `Authorization: Bearer`
- Endpoint name is `/trackUserRating` (not `/trackPlayerRating`)

---

## Implementation Order

1. ✅ Phase 1: Update platform to "discord"
2. ✅ Phase 3: Update configuration loader
3. ✅ Phase 4: Update service constructor
4. ✅ Phase 2: Create/use API client
5. ✅ Phase 5: Implement real API call
6. ✅ Phase 6: Update BotMain
7. ✅ Phase 7: Handle header configuration
8. ✅ Phase 8: Error handling and resilience
9. ✅ Phase 9: Verify response model
10. ✅ Phase 10: Code cleanup

