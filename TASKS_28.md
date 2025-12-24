# TASKS_28: Real API Integration for /getAllApps with Dynamic Promotion Scheduling

## Objective
Integrate the real `/getAllApps` API endpoint and implement intelligent promotion scheduling that handles multiple apps gracefully. Create a centralized API client architecture to reduce redundancy and ensure code quality.

## Overview
Currently:
- `InMemoryGamePromotionService` uses stub JSON file (`stubs/getAllApps.json`)
- `GamePromotionScheduler` calls `fetchAllApps()` every 60 minutes
- All apps are processed immediately when fetched (can cause spam with 30+ apps)
- No centralized API client (will be needed for future API integrations)

We need to:
1. Create centralized API client architecture
2. Replace stub with real `/getAllApps` API calls
3. Implement dynamic cooldown algorithm for multiple apps
4. Implement game rotation queue system
5. Ensure campaign window compliance
6. Update configuration for API keys/URLs

---

## Implementation Plan

### Phase 1: Create Centralized API Client Architecture

**Goal:** Create a reusable HTTP client for all API calls to reduce redundancy and ensure consistency.

**File:** `src/main/java/com/tatumgames/mikros/api/TatumGamesApiClient.java` (NEW)

**Purpose:** Centralized HTTP client for all Tatum Games API endpoints. Handles authentication, error handling, retries, and rate limiting.

**Features:**
- HTTP client (Java 11+ `HttpClient` or OkHttp)
- Bearer token authentication
- Request/response logging
- Error handling with retries
- Rate limit respect
- Timeout configuration (10s recommended)
- JSON parsing with Jackson

**Structure:**
```java
public class TatumGamesApiClient {
    private final HttpClient httpClient;
    private final String apiBaseUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper;
    
    // Methods:
    - public <T> T get(String endpoint, Class<T> responseClass)
    - public <T> T post(String endpoint, Object requestBody, Class<T> responseClass)
    - private HttpRequest buildRequest(String method, String endpoint, Object body)
    - private <T> T executeRequest(HttpRequest request, Class<T> responseClass)
    - private void handleRateLimit(HttpResponse<?> response)
    - private void handleError(HttpResponse<?> response)
}
```

**Configuration:**
- Base URL: `https://api.tatumgames.com`
- API Key: From environment variable `MIKROS_API_KEY`
- Timeout: 10 seconds
- Retry: 3 attempts with exponential backoff

**Error Handling:**
- HTTP 401: Invalid API key → Log error, don't retry
- HTTP 429: Rate limit → Respect `retry_after` header, wait and retry
- HTTP 500: Server error → Retry with backoff
- Network errors: Retry with exponential backoff

**Location:** Create new package `com.tatumgames.mikros.api`

---

### Phase 2: Update Configuration Loader

**File:** `src/main/java/com/tatumgames/mikros/config/ConfigLoader.java`

**Changes:**
1. Add API configuration fields:
   - `private final String mikrosApiKey;`
   - `private final String mikrosApiUrl;`

2. Add getter methods:
   - `public String getMikrosApiKey()`
   - `public String getMikrosApiUrl()`

3. Load from environment variables:
   - `MIKROS_API_KEY` (optional - can be empty for mock mode)
   - `MIKROS_API_URL` (default: `https://api.tatumgames.com`)

4. Make API key optional (for backward compatibility with mock mode):
   - If not set, API client can work in mock/fallback mode
   - Log warning if API key is missing

**Location:** Add after existing fields, around line 18

---

### Phase 3: Create Real Game Promotion Service

**File:** `src/main/java/com/tatumgames/mikros/services/RealGamePromotionService.java` (NEW)

**Purpose:** Real implementation of `GamePromotionService` that uses API client.

**Implementation:**
- Implements `GamePromotionService` interface
- Uses `TatumGamesApiClient` for API calls
- Maintains same in-memory storage for guild configuration (channels, verbosity, step tracking)
- Replaces `fetchAllApps()` to call real API

**Key Methods:**
```java
@Override
public List<AppPromotion> fetchAllApps() {
    try {
        GetAllAppsResponse response = apiClient.get("/getAllApps", GetAllAppsResponse.class);
        if (response != null && response.getData() != null) {
            return response.getData().getApps();
        }
        return List.of();
    } catch (Exception e) {
        logger.error("Failed to fetch apps from API", e);
        // Fallback to stub if API fails (for resilience)
        return loadStubApps();
    }
}
```

**Fallback Strategy:**
- If API call fails, fall back to stub JSON (for resilience)
- Log error but don't crash
- Allows bot to continue operating if API is temporarily unavailable

**Location:** Create in `src/main/java/com/tatumgames/mikros/services/`

---

### Phase 4: Implement Dynamic Cooldown Algorithm

**File:** `src/main/java/com/tatumgames/mikros/services/scheduler/GamePromotionScheduler.java`

**Goal:** Implement intelligent cooldown that scales with number of active games.

**Algorithm Implementation:**

1. **Calculate Dynamic Cooldown:**
   ```java
   private long calculateDynamicCooldown(int activeGameCount) {
       int minIntervalMinutes = 5;
       int maxIntervalMinutes = 60;
       int maxGamesThreshold = 50;
       int targetPromotionsPerDay = 25;
       
       // Base interval calculation
       double baseInterval = minIntervalMinutes + 
           ((maxIntervalMinutes - minIntervalMinutes) * 
            (activeGameCount / (double) maxGamesThreshold));
       
       // Clamp to min/max
       baseInterval = Math.max(minIntervalMinutes, 
                      Math.min(maxIntervalMinutes, baseInterval));
       
       // Add randomization (±20%)
       double randomFactor = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
       long actualInterval = (long) (baseInterval * randomFactor);
       
       return actualInterval;
   }
   ```

2. **Update Scheduler Logic:**
   - Calculate cooldown based on active game count
   - Store per-guild last promotion time
   - Check if enough time has passed before posting next promotion

**Location:** Add new method, update `postPromotionsToChannel()` method

---

### Phase 5: Implement Game Rotation Queue

**File:** `src/main/java/com/tatumgames/mikros/services/scheduler/GamePromotionScheduler.java`

**Goal:** Rotate through games instead of posting all at once.

**Implementation:**

1. **Create Rotation Queue System:**
   ```java
   // Per-guild rotation state
   private final Map<String, GameRotationState> rotationStates = new ConcurrentHashMap<>();
   
   private static class GameRotationState {
       Queue<String> gameQueue;  // Queue of appIds to promote
       Instant lastPromotionTime;
       long currentCooldownMinutes;
   }
   ```

2. **Initialize Queue:**
   - When apps are fetched, create/update queue for each guild
   - Shuffle queue for randomness
   - Filter to only active campaigns

3. **Rotation Logic:**
   ```java
   private AppPromotion getNextGameToPromote(String guildId, List<AppPromotion> activeApps) {
       GameRotationState state = rotationStates.computeIfAbsent(guildId, k -> {
           GameRotationState newState = new GameRotationState();
           newState.gameQueue = new LinkedList<>();
           newState.lastPromotionTime = null;
           newState.currentCooldownMinutes = 5; // Default
           return newState;
       });
       
       // Rebuild queue if empty or apps changed
       if (state.gameQueue.isEmpty()) {
           rebuildQueue(state, activeApps);
       }
       
       // Check if cooldown has passed
       if (state.lastPromotionTime != null) {
           long minutesSinceLast = ChronoUnit.MINUTES.between(
               state.lastPromotionTime, Instant.now());
           if (minutesSinceLast < state.currentCooldownMinutes) {
               return null; // Not time yet
           }
       }
       
       // Get next game from queue
       String nextAppId = state.gameQueue.poll();
       if (nextAppId == null) {
           rebuildQueue(state, activeApps);
           nextAppId = state.gameQueue.poll();
       }
       
       // Find app and update state
       AppPromotion app = activeApps.stream()
           .filter(a -> a.getAppId().equals(nextAppId))
           .findFirst()
           .orElse(null);
       
       if (app != null) {
           state.lastPromotionTime = Instant.now();
           state.currentCooldownMinutes = calculateDynamicCooldown(activeApps.size());
           // Re-add to end of queue for rotation
           state.gameQueue.offer(nextAppId);
       }
       
       return app;
   }
   ```

4. **Update Posting Logic:**
   - Instead of posting all apps, post one at a time
   - Use rotation queue to determine which app to post next
   - Respect dynamic cooldown between posts

**Location:** Add new methods and update `postPromotionsToChannel()`

---

### Phase 6: Ensure Campaign Window Compliance

**File:** `src/main/java/com/tatumgames/mikros/services/scheduler/GamePromotionScheduler.java`

**Goal:** Only promote games that are within their campaign window.

**Implementation:**

1. **Campaign Window Check:**
   ```java
   private boolean isWithinCampaignWindow(AppPromotion app, Instant now) {
       if (app.getCampaign() == null) {
           return false;
       }
       
       Instant startDate = app.getCampaign().getStartDate();
       Instant endDate = app.getCampaign().getEndDate();
       
       if (startDate == null || endDate == null) {
           return false;
       }
       
       return !now.isBefore(startDate) && !now.isAfter(endDate);
   }
   ```

2. **Filter Before Rotation:**
   - Filter active apps to only those within campaign window
   - Update queue when campaigns expire
   - Skip games outside campaign window

3. **Resilience:**
   - If bot restarts, re-check campaign windows on next run
   - Remove expired campaigns from queue automatically

**Location:** Add method, update `postPromotionsToChannel()` and queue building logic

**Note:** `AppPromotion.isCampaignActive()` already exists - verify it checks campaign window correctly.

---

### Phase 7: Update BotMain to Use Real Service

**File:** `src/main/java/com/tatumgames/mikros/bot/BotMain.java`

**Changes:**
1. Initialize `TatumGamesApiClient`:
   ```java
   private final TatumGamesApiClient apiClient;
   
   // In constructor:
   ConfigLoader config = new ConfigLoader();
   this.apiClient = new TatumGamesApiClient(
       config.getMikrosApiUrl(),
       config.getMikrosApiKey()
   );
   ```

2. Replace `InMemoryGamePromotionService` with `RealGamePromotionService`:
   ```java
   // Replace:
   this.gamePromotionService = new InMemoryGamePromotionService();
   
   // With:
   this.gamePromotionService = new RealGamePromotionService(apiClient);
   ```

3. **Optional:** Keep both and switch based on config:
   ```java
   if (config.getMikrosApiKey() != null && !config.getMikrosApiKey().isBlank()) {
       this.gamePromotionService = new RealGamePromotionService(apiClient);
   } else {
       logger.warn("MIKROS_API_KEY not set, using mock game promotion service");
       this.gamePromotionService = new InMemoryGamePromotionService();
   }
   ```

**Location:** Around line 84-85, update service initialization

---

### Phase 8: Update Scheduler to Use New Logic

**File:** `src/main/java/com/tatumgames/mikros/services/scheduler/GamePromotionScheduler.java`

**Changes:**
1. Update `postPromotionsToChannel()` method:
   - Remove loop that posts all apps
   - Use `getNextGameToPromote()` to get one app
   - Check campaign window before posting
   - Respect dynamic cooldown
   - Post only one promotion per check cycle

2. **New Flow:**
   ```java
   private int postPromotionsToChannel(Guild guild, TextChannel channel) {
       // ... existing verbosity checks ...
       
       // Fetch apps
       List<AppPromotion> allApps = gamePromotionService.fetchAllApps();
       
       // Filter to active campaigns within window
       List<AppPromotion> activeApps = allApps.stream()
           .filter(app -> isWithinCampaignWindow(app, now))
           .filter(AppPromotion::isCampaignActive)
           .collect(Collectors.toList());
       
       if (activeApps.isEmpty()) {
           return 0;
       }
       
       // Get next game to promote (respects cooldown and rotation)
       AppPromotion nextApp = getNextGameToPromote(guildId, activeApps);
       if (nextApp == null) {
           // Cooldown not expired yet
           return 0;
       }
       
       // Determine which step to post
       int lastStep = gamePromotionService.getLastPromotionStep(guildId, nextApp.getAppId());
       Instant lastPostTime = gamePromotionService.getLastAppPostTime(guildId, nextApp.getAppId());
       int nextStep = stepManager.determineNextStep(nextApp, lastStep, lastPostTime, activeApps, now);
       
       if (nextStep == 0) {
           return 0; // No step ready
       }
       
       // Post promotion
       if (nextStep == 3) {
           // Multi-game promotion (special handling)
           postMultiGamePromotion(channel, activeApps);
           // Record for all apps
           for (AppPromotion app : activeApps) {
               gamePromotionService.recordPromotionStep(guildId, app.getAppId(), 3, now);
           }
       } else {
           postAppPromotion(channel, nextApp, nextStep, activeApps);
           gamePromotionService.recordPromotionStep(guildId, nextApp.getAppId(), nextStep, now);
       }
       
       return 1; // Posted one promotion
   }
   ```

**Location:** Replace entire `postPromotionsToChannel()` method logic

---

### Phase 9: Code Cleanup and Best Practices

**Tasks:**
1. **Remove Unused Code:**
   - Remove any unused imports
   - Remove commented-out code
   - Remove dead code paths

2. **Update Logging:**
   - Add appropriate log levels (INFO for important events, DEBUG for detailed flow)
   - Log API calls (without sensitive data)
   - Log rotation queue updates
   - Log cooldown calculations

3. **Error Handling:**
   - Ensure all API calls have try-catch
   - Log errors appropriately
   - Don't crash on API failures (use fallback)

4. **Documentation:**
   - Add Javadoc to new classes
   - Document algorithm parameters
   - Document configuration requirements

5. **Testing Considerations:**
   - Test with 0 apps
   - Test with 1 app
   - Test with 30+ apps
   - Test campaign window expiration
   - Test API failure fallback

---

## Configuration Requirements

### Environment Variables

Add to `.env` file:
```env
# MIKROS API Configuration (optional - falls back to mock if not set)
MIKROS_API_KEY=your_api_key_here
MIKROS_API_URL=https://api.tatumgames.com
```

### Algorithm Parameters (Configurable Constants)

In `GamePromotionScheduler`:
```java
private static final int MIN_INTERVAL_MINUTES = 5;
private static final int MAX_INTERVAL_MINUTES = 60;
private static final int MAX_GAMES_THRESHOLD = 50;
private static final int TARGET_PROMOTIONS_PER_DAY = 25;
private static final double RANDOMIZATION_FACTOR_MIN = 0.8;
private static final double RANDOMIZATION_FACTOR_MAX = 1.2;
```

---

## Testing Checklist

- [ ] API client makes successful GET request to `/getAllApps`
- [ ] API client handles 401 (invalid key) gracefully
- [ ] API client handles 429 (rate limit) with retry
- [ ] API client handles 500 (server error) with retry
- [ ] API client falls back to stub on failure
- [ ] Dynamic cooldown calculates correctly for 1 game
- [ ] Dynamic cooldown calculates correctly for 30 games
- [ ] Dynamic cooldown calculates correctly for 50+ games
- [ ] Randomization adds ±20% variability
- [ ] Game rotation queue builds correctly
- [ ] Game rotation queue rotates through all games
- [ ] Campaign window check filters expired campaigns
- [ ] Campaign window check allows active campaigns
- [ ] Only one promotion posted per check cycle
- [ ] Cooldown prevents posting too frequently
- [ ] Scheduler respects verbosity settings
- [ ] Bot continues operating if API is down (fallback)

---

## Edge Cases to Handle

1. **API Returns Empty List:** Queue should be empty, no promotions posted
2. **All Campaigns Expired:** Queue should be empty, no promotions posted
3. **API Temporarily Down:** Fall back to stub, log warning, continue operation
4. **Bot Restart:** Queue rebuilds on next check, campaign windows re-validated
5. **New Apps Added:** Queue updates on next fetch, new apps added to rotation
6. **Campaign Expires Mid-Rotation:** Removed from queue on next check
7. **Single Active Game:** Cooldown should be minimum (5 minutes)
8. **50+ Active Games:** Cooldown should be maximum (60 minutes)

---

## Implementation Order

1. ✅ Phase 1: Create centralized API client
2. ✅ Phase 2: Update configuration loader
3. ✅ Phase 7: Update BotMain (initialize API client)
4. ✅ Phase 3: Create real game promotion service
5. ✅ Phase 4: Implement dynamic cooldown algorithm
6. ✅ Phase 5: Implement game rotation queue
7. ✅ Phase 6: Ensure campaign window compliance
8. ✅ Phase 8: Update scheduler to use new logic
9. ✅ Phase 9: Code cleanup and best practices

---

## Architecture Benefits

### Centralized API Client
- **Reduces Redundancy:** All API calls use same client
- **Consistent Error Handling:** Same retry/backoff logic everywhere
- **Easier Maintenance:** Update authentication/error handling in one place
- **Future-Proof:** Easy to add new API endpoints

### Dynamic Cooldown
- **Scales Automatically:** Fewer games = more frequent posts, more games = slower posts
- **Prevents Spam:** Never posts too frequently
- **Maintains Visibility:** Ensures all games get promoted over time

### Game Rotation
- **Fair Distribution:** All games get equal promotion opportunity
- **No Duplicates:** Each game promoted once per rotation cycle
- **Predictable:** Users see variety, not same game repeatedly

---

## Notes

- API key is optional - bot can run in mock mode if not configured
- Fallback to stub ensures resilience if API is temporarily unavailable
- Dynamic cooldown prevents spam while ensuring all games are promoted
- Rotation queue ensures fair distribution of promotions
- Campaign window compliance prevents expired campaigns from being posted
- All changes maintain backward compatibility with existing functionality

---

## Future Enhancements (Optional)

- Priority-based rotation (weight games by priority)
- Analytics tracking (which promotions perform best)
- A/B testing (different message templates)
- Scheduled promotions (promote specific games at specific times)
- Per-guild customization (different cooldowns per server)

