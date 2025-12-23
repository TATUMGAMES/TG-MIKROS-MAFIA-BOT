# TASKS_30: Environment-Based API Configuration and getUserScoreDetail Integration

## Objective
Implement environment-based configuration (dev/prod) for API keys and tokens, and integrate the real `/getUserScoreDetail` API endpoint. Update all stubs to reflect actual API request/response structures.

## Overview
Currently:
- API keys are hardcoded or use single environment variable
- `getUserScoreDetail()` uses stub JSON file
- `trackUserRating()` uses hardcoded `apiKeyType = "prod"`
- Stubs may not match actual API response structures
- No environment distinction between dev and prod

We need to:
1. Set up dev/prod environment system
2. Use dev keys/tokens when testing, prod keys/tokens when live
3. Implement real `/getUserScoreDetail` API call
4. Update `trackUserRating` to use environment-based `apiKeyType`
5. Update all stubs to match actual API structures
6. Create `GetUserScoreDetailRequest` model
7. Update `GetUserScoreDetailResponse` model to match actual API

---

## API Key Configuration

### Environment API Keys

**Dev Environment:**
- X-Apikey: `e12afd908f7c19a64bca41e6657fae90e001bd55`

**Prod Environment:**
- X-Apikey: `a37f9c1de42b5089f6c2d8e14bb97f30e5ab47cc`

### API Endpoints

**Reputation API Base URL:**
- `https://tg-api-new-stage.uc.r.appspot.com/mikros/marketing/discord`

**Endpoints:**
- `/getUserScoreDetails` (POST) - Note: actual endpoint name has "Details" plural
- `/trackUserRating` (POST)

---

## Implementation Plan

### Phase 1: Add Environment Configuration

**File:** `src/main/java/com/tatumgames/mikros/config/ConfigLoader.java`

**Changes:**
1. Add environment field and enum:
   ```java
   public enum Environment {
       DEV("dev"),
       PROD("prod");
       
       private final String value;
       
       Environment(String value) {
           this.value = value;
       }
       
       public String getValue() {
           return value;
       }
       
       public static Environment fromString(String value) {
           if (value == null || value.isBlank()) {
               return PROD; // Default to prod for safety
           }
           String lower = value.toLowerCase().trim();
           return "dev".equals(lower) ? DEV : PROD;
       }
   }
   
   private final Environment environment;
   ```

2. Add dev/prod API key fields:
   ```java
   private final String reputationApiKeyDev;
   private final String reputationApiKeyProd;
   private final String reputationApiKey; // Selected based on environment
   ```

3. Load environment variable:
   ```java
   // In constructor, after loading other config
   String envValue = getEnv("ENVIRONMENT", "prod");
   this.environment = Environment.fromString(envValue);
   
   // Load dev and prod keys
   this.reputationApiKeyDev = getEnv("REPUTATION_API_KEY_DEV", 
       "e12afd908f7c19a64bca41e6657fae90e001bd55");
   this.reputationApiKeyProd = getEnv("REPUTATION_API_KEY_PROD", 
       "a37f9c1de42b5089f6c2d8e14bb97f30e5ab47cc");
   
   // Select key based on environment
   this.reputationApiKey = environment == Environment.DEV 
       ? reputationApiKeyDev 
       : reputationApiKeyProd;
   
   logger.info("Environment: {} | Using {} API key", 
       environment.getValue(), environment == Environment.DEV ? "DEV" : "PROD");
   ```

4. Add getter methods:
   ```java
   public Environment getEnvironment() {
       return environment;
   }
   
   public String getApiKeyType() {
       return environment.getValue();
   }
   ```

**Location:** Add enum after class declaration, add fields around line 20, update constructor around line 56

**Note:** Default to "prod" for safety if environment not specified.

---

### Phase 2: Create GetUserScoreDetailRequest Model

**File:** `src/main/java/com/tatumgames/mikros/models/api/GetUserScoreDetailRequest.java` (NEW)

**Structure:**
Based on the curl example:
```json
{
    "apiKeyType": "prod",
    "discordUserId": "023456789012345678",
    "discordUsernames": ["123456789012345678", "923456789012345678"]
}
```

**Implementation:**
```java
package com.tatumgames.mikros.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request model for /getUserScoreDetails API endpoint.
 */
public class GetUserScoreDetailRequest {
    @JsonProperty("apiKeyType")
    private String apiKeyType;
    
    @JsonProperty("discordUserId")
    private String discordUserId;
    
    @JsonProperty("discordUsernames")
    private List<String> discordUsernames;
    
    public String getApiKeyType() {
        return apiKeyType;
    }
    
    public void setApiKeyType(String apiKeyType) {
        this.apiKeyType = apiKeyType;
    }
    
    public String getDiscordUserId() {
        return discordUserId;
    }
    
    public void setDiscordUserId(String discordUserId) {
        this.discordUserId = discordUserId;
    }
    
    public List<String> getDiscordUsernames() {
        return discordUsernames;
    }
    
    public void setDiscordUsernames(List<String> discordUsernames) {
        this.discordUsernames = discordUsernames;
    }
}
```

**Location:** Create new file in `src/main/java/com/tatumgames/mikros/models/api/`

---

### Phase 3: Update GetUserScoreDetailResponse Model

**File:** `src/main/java/com/tatumgames/mikros/models/api/GetUserScoreDetailResponse.java`

**Issue:** Current model expects `data.scores[]` but actual API returns `data[]` directly with simpler structure.

**Actual API Response:**
```json
{
    "status": {
        "statusCode": 200,
        "statusMessage": "SUCCESS"
    },
    "data": [
        {
            "username": "123456789012345678",
            "reputationScore": 10
        },
        {
            "username": "023456789012345678",
            "reputationScore": 1
        }
    ]
}
```

**Changes:**
1. Update `Data` class to be a list directly, or keep wrapper but change structure:
   ```java
   public static class Data {
       // Change from List<UserScore> scores to direct list
       @JsonProperty("data")
       private List<UserScore> scores; // Keep name for backward compatibility
       
       // OR if API returns data as array directly:
       // Remove Data wrapper, make response.data a List<UserScore>
   }
   ```

2. Update `UserScore` class to match actual API:
   ```java
   public static class UserScore {
       @JsonProperty("username")
       private String username; // Changed from discordUsername
       
       @JsonProperty("reputationScore")
       private int reputationScore;
       
       // Remove fields not in actual API:
       // - id
       // - email
       // - discordUserId
       // - discordServers
       
       public String getUsername() {
           return username;
       }
       
       public void setUsername(String username) {
           this.username = username;
       }
       
       public int getReputationScore() {
           return reputationScore;
       }
       
       public void setReputationScore(int reputationScore) {
           this.reputationScore = reputationScore;
       }
   }
   ```

**Alternative Approach:** Keep backward compatibility by supporting both structures, or create a new model and migrate.

**Location:** Update existing model starting at line 57

**Note:** This is a breaking change. Consider:
- Option A: Update model to match API exactly (breaking change)
- Option B: Create adapter to map API response to existing model
- Option C: Support both structures with Jackson annotations

**Recommendation:** Option A - update to match actual API, simpler and cleaner.

---

### Phase 4: Update InMemoryReputationService Constructor

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Changes:**
1. Add `apiKeyType` parameter:
   ```java
   private final String apiKeyType;
   
   public InMemoryReputationService(TatumGamesApiClient apiClient, 
                                    String reputationApiUrl, 
                                    String reputationApiKey,
                                    String apiKeyType) {
       this.reportStore = new ConcurrentHashMap<>();
       this.objectMapper = new ObjectMapper();
       this.apiClient = apiClient;
       this.reputationApiUrl = reputationApiUrl;
       this.reputationApiKey = reputationApiKey;
       this.apiKeyType = apiKeyType;
       logger.info("InMemoryReputationService initialized");
   }
   ```

**Location:** Update constructor starting at line 44

---

### Phase 5: Update reportToExternalAPI to Use Environment apiKeyType

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Changes:**
1. Update `reportToExternalAPI()` to set `apiKeyType` from field:
   ```java
   // Set platform to "discord"
   request.setPlatform("discord");
   
   // Set apiKeyType based on environment
   request.setApiKeyType(apiKeyType);
   ```

**Location:** Update around line 104-105

---

### Phase 6: Implement Real getUserScoreDetail API Call

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Changes:**
1. Replace `getUserScoreDetail()` method implementation:

```java
@Override
public GetUserScoreDetailResponse getUserScoreDetail(List<String> usernames) {
    // If API key not configured, fall back to stub
    if (reputationApiKey == null || reputationApiKey.isBlank()) {
        logger.warn("Reputation API key not configured, using stub response");
        return loadStubResponse(usernames);
    }
    
    try {
        // Build request
        GetUserScoreDetailRequest request = new GetUserScoreDetailRequest();
        request.setApiKeyType(apiKeyType);
        
        // Set discordUserId (optional - can be null or empty)
        // For now, we'll leave it null or use first username if available
        if (usernames != null && !usernames.isEmpty()) {
            // Option: Use first username as discordUserId if it's a user ID format
            // For now, leave null as API may not require it
            request.setDiscordUserId(null);
        }
        
        request.setDiscordUsernames(usernames);
        
        // Make API call
        GetUserScoreDetailResponse response = apiClient.postWithApiKey(
                reputationApiUrl,
                "/getUserScoreDetails", // Note: plural "Details"
                request,
                reputationApiKey,
                GetUserScoreDetailResponse.class
        );
        
        if (response != null && response.getStatus() != null &&
                response.getStatus().getStatusCode() == 200) {
            logger.info("Successfully retrieved user score details for {} usernames", 
                    usernames != null ? usernames.size() : 0);
            return response;
        } else {
            logger.warn("API returned non-200 status: {}",
                    response != null ? response.getStatus() : "null response");
            return null;
        }
        
    } catch (TatumGamesApiClient.ApiException e) {
        logger.error("Error calling getUserScoreDetails API (status: {}): {}",
                e.getStatusCode(), e.getMessage(), e);
        return null;
    } catch (Exception e) {
        logger.error("Unexpected error calling getUserScoreDetails API", e);
        return null;
    }
}

private GetUserScoreDetailResponse loadStubResponse(List<String> usernames) {
    // Existing stub loading logic
    try {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("stubs/getUserScoreDetail.json");
        
        if (inputStream == null) {
            logger.error("Could not find stub JSON file: stubs/getUserScoreDetail.json");
            return null;
        }
        
        GetUserScoreDetailResponse response = objectMapper.readValue(
                inputStream, GetUserScoreDetailResponse.class);
        
        // Filter results by requested usernames if provided
        if (usernames != null && !usernames.isEmpty() && 
            response.getData() != null && response.getData().getScores() != null) {
            // Filter logic here
            // ...
        }
        
        logger.info("Loaded user score details from stub JSON");
        return response;
        
    } catch (Exception e) {
        logger.error("Failed to load stub JSON file for getUserScoreDetail", e);
        return null;
    }
}
```

**Location:** Replace entire `getUserScoreDetail()` method starting at line 180

**Key Points:**
- Endpoint: `/getUserScoreDetails` (plural "Details")
- Use `postWithApiKey()` method
- Set `apiKeyType` from environment
- Handle errors gracefully, fall back to stub if needed

---

### Phase 7: Update BotMain to Pass apiKeyType

**File:** `src/main/java/com/tatumgames/mikros/bot/BotMain.java`

**Changes:**
1. Update `InMemoryReputationService` initialization:
   ```java
   this.reputationService = new InMemoryReputationService(
           apiClient,
           config.getReputationApiUrl(),
           config.getReputationApiKey(),
           config.getApiKeyType() // Add apiKeyType parameter
   );
   ```

**Location:** Update around line 90-94

---

### Phase 8: Update Stub JSON Files

#### 8.1 Update getUserScoreDetail.json

**File:** `src/main/resources/stubs/getUserScoreDetail.json`

**Current Structure:** Has `data.scores[]` with full UserScore objects

**New Structure:** Match actual API response:
```json
{
  "status": {
    "statusCode": 200,
    "statusMessage": "SUCCESS"
  },
  "data": [
    {
      "username": "123456789012345678",
      "reputationScore": 10
    },
    {
      "username": "023456789012345678",
      "reputationScore": 1
    },
    {
      "username": "923456789012345678",
      "reputationScore": 10
    }
  ]
}
```

**Changes:** Replace entire file content

**Location:** `src/main/resources/stubs/getUserScoreDetail.json`

---

#### 8.2 Verify trackPlayerRating.json

**File:** `src/main/resources/stubs/trackPlayerRating.json`

**Current Structure:** Already correct (simple status response)

**Verification:** Ensure it matches:
```json
{
  "status": {
    "statusCode": 200,
    "statusMessage": "SUCCESS"
  }
}
```

**Status:** ✅ Already correct, no changes needed

---

#### 8.3 Verify getAllApps.json

**File:** `src/main/resources/stubs/getAllApps.json`

**Current Structure:** Has complex nested structure with campaigns, CTAs, etc.

**Action:** Verify against actual API response structure. If API documentation shows different structure, update accordingly.

**Note:** This stub is used by `RealGamePromotionService` which already makes real API calls, so stub is only fallback. Verify structure matches actual API.

**Status:** ⚠️ Verify against actual API, update if needed

---

### Phase 9: Update LookupCommand to Handle New Response Structure

**File:** `src/main/java/com/tatumgames/mikros/admin/commands/LookupCommand.java`

**Changes:**
1. Update to use new response structure:
   ```java
   // Old: response.getData().getScores()
   // New: response.getData() (if Data is now a List directly)
   // OR: response.getData().getScores() (if we keep wrapper)
   ```

2. Update field access:
   ```java
   // Old: score.getDiscordUsername()
   // New: score.getUsername()
   
   // Remove fields not in API:
   // - score.getEmail()
   // - score.getDiscordUserId() (if not in response)
   // - score.getDiscordServers()
   ```

**Location:** Update around lines 101-144

**Note:** This depends on how we structure the response model in Phase 3.

---

### Phase 10: Add Environment Variable Documentation

**File:** Update `.env.example` or create documentation

**Environment Variables:**
```env
# Environment: "dev" or "prod" (defaults to "prod" if not set)
ENVIRONMENT=dev

# Reputation API Configuration
REPUTATION_API_URL=https://tg-api-new-stage.uc.r.appspot.com/mikros/marketing/discord

# Dev API Key (used when ENVIRONMENT=dev)
REPUTATION_API_KEY_DEV=e12afd908f7c19a64bca41e6657fae90e001bd55

# Prod API Key (used when ENVIRONMENT=prod)
REPUTATION_API_KEY_PROD=a37f9c1de42b5089f6c2d8e14bb97f30e5ab47cc

# Legacy (optional - will use ENVIRONMENT-based key if not set)
# REPUTATION_API_KEY=your_key_here
```

**Note:** For backward compatibility, if `REPUTATION_API_KEY` is set, it can override environment-based selection.

---

## API Details

### getUserScoreDetails Endpoint

**URL:** `https://tg-api-new-stage.uc.r.appspot.com/mikros/marketing/discord/getUserScoreDetails`  
**Method:** `POST`  
**Header:** `X-Apikey: {api_key}` (dev or prod based on environment)  
**Content-Type:** `application/json`

**Request Structure:**
```json
{
    "apiKeyType": "dev" | "prod",
    "discordUserId": "023456789012345678",  // Optional
    "discordUsernames": ["123456789012345678", "923456789012345678"]
}
```

**Response Structure:**
```json
{
    "status": {
        "statusCode": 200,
        "statusMessage": "SUCCESS"
    },
    "data": [
        {
            "username": "123456789012345678",
            "reputationScore": 10
        }
    ]
}
```

**Note:** Endpoint name is `/getUserScoreDetails` (plural "Details"), not `/getUserScoreDetail`.

---

### trackUserRating Endpoint

**URL:** `https://tg-api-new-stage.uc.r.appspot.com/mikros/marketing/discord/trackUserRating`  
**Method:** `POST`  
**Header:** `X-Apikey: {api_key}` (dev or prod based on environment)  
**Content-Type:** `application/json`

**Request Structure:**
- `apiKeyType`: Now set from environment ("dev" or "prod")
- All other fields remain the same

**Response Structure:**
- Unchanged (simple status response)

---

## Testing Checklist

### Environment Configuration
- [ ] `ENVIRONMENT=dev` uses dev API key
- [ ] `ENVIRONMENT=prod` uses prod API key
- [ ] Default (no ENVIRONMENT set) uses prod API key
- [ ] Environment is logged at startup
- [ ] API key selection is logged

### getUserScoreDetails API
- [ ] Request is made with correct URL (`/getUserScoreDetails`)
- [ ] `X-Apikey` header uses correct environment key
- [ ] `apiKeyType` in request body matches environment
- [ ] Request body structure is correct
- [ ] Response is correctly parsed
- [ ] HTTP 200 response is handled correctly
- [ ] HTTP error responses (4xx, 5xx) are handled gracefully
- [ ] Network errors are handled gracefully
- [ ] Fallback to stub works when API key not configured
- [ ] `/lookup` command still works correctly
- [ ] Response model matches actual API structure

### trackUserRating API
- [ ] `apiKeyType` in request matches environment
- [ ] `X-Apikey` header uses correct environment key
- [ ] All other functionality still works

### Stub Updates
- [ ] `getUserScoreDetail.json` matches actual API response
- [ ] `trackPlayerRating.json` is correct (already verified)
- [ ] `getAllApps.json` is verified/updated if needed
- [ ] Stubs load correctly when API unavailable

### Integration
- [ ] `/lookup` command works with new response structure
- [ ] `/report` command still works
- [ ] `/praise` command still works
- [ ] Error messages are logged appropriately
- [ ] User receives confirmation even if API call fails

---

## Edge Cases to Handle

1. **Environment Not Set:** Default to "prod" for safety
2. **API Key Not Configured:** Fall back to stub, log warning, continue operation
3. **Network Timeout:** Log error, return null, don't crash
4. **HTTP 401 (Unauthorized):** Log error, don't retry, return null
5. **HTTP 429 (Rate Limited):** Log warning, API client should handle retry
6. **HTTP 500 (Server Error):** Log error, API client should retry
7. **Malformed Response:** Log error, return null, don't crash
8. **Response Structure Mismatch:** Log error, handle gracefully
9. **Empty Usernames List:** Handle gracefully, return empty response
10. **Username Not Found:** API returns partial results, display appropriately

---

## Migration Notes

### Breaking Changes

1. **GetUserScoreDetailResponse Model:** Structure changed from `data.scores[]` to `data[]`
   - **Impact:** `LookupCommand` needs updates
   - **Mitigation:** Update model and command together

2. **UserScore Model:** Fields changed
   - **Removed:** `id`, `email`, `discordUserId`, `discordServers`
   - **Changed:** `discordUsername` → `username`
   - **Impact:** `LookupCommand` display logic needs updates

3. **Environment Variable:** New required variable `ENVIRONMENT`
   - **Impact:** Existing deployments need to set this
   - **Mitigation:** Defaults to "prod" if not set

### Backward Compatibility

- Stub fallback still works if API key not configured
- Existing commands continue to work (with updates)
- Environment defaults to prod for safety

---

## Implementation Order

1. ✅ Phase 1: Add environment configuration
2. ✅ Phase 2: Create GetUserScoreDetailRequest model
3. ✅ Phase 3: Update GetUserScoreDetailResponse model
4. ✅ Phase 4: Update InMemoryReputationService constructor
5. ✅ Phase 5: Update reportToExternalAPI to use environment apiKeyType
6. ✅ Phase 6: Implement real getUserScoreDetail API call
7. ✅ Phase 7: Update BotMain to pass apiKeyType
8. ✅ Phase 8: Update stub JSON files
9. ✅ Phase 9: Update LookupCommand for new response structure
10. ✅ Phase 10: Add environment variable documentation

---

## Notes

- **Endpoint Name:** `/getUserScoreDetails` (plural "Details") - note the "s" at the end
- **Environment Default:** Defaults to "prod" if not specified (safer default)
- **API Key Selection:** Environment-based selection happens at ConfigLoader initialization
- **Stub Updates:** Critical to match actual API responses for testing
- **Response Model:** Consider creating adapter if backward compatibility is critical
- **Testing:** Test with both dev and prod environments before deployment

---

**Status:** 📋 Ready for Implementation  
**Priority:** HIGH  
**Estimated Effort:** 6-8 hours  
**Dependencies:** None (can be implemented independently)

