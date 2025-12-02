# TASKS_22.md – Admin-Only Reputation System with API Integration

## Objective

Restructure the reputation system to be admin-only, update behavior categories and point values, integrate with
`/trackPlayerRating` and `/getUserScoreDetail` APIs, and create a new `/lookup` command for admins to view user
reputation scores.

---

## Current State Analysis

### What Exists:

1. **`/report` Command** (`src/main/java/com/tatumgames/mikros/commands/ReportCommand.java`)
    - Currently available to **Everyone** (not admin-only) ❌
    - Uses old behavior categories
    - Does NOT call `/trackPlayerRating` API
    - Only affects local reputation

2. **`/praise` Command** (`src/main/java/com/tatumgames/mikros/commands/PraiseCommand.java`)
    - Available to Everyone
    - Uses old behavior categories
    - Should remain for positive reporting (admin-only)

3. **`BehaviorCategory` Enum** (`src/main/java/com/tatumgames/mikros/models/BehaviorCategory.java`)
    - Old categories:
        - Negative: POOR_SPORTSMANSHIP (-1), TROLLING (-2), AFK_COMPLAINING (-3), BAD_LANGUAGE_CHEATING (-5)
        - Positive: GOOD_SPORTSMANSHIP (+1), GREAT_LEADERSHIP (+2), EXCELLENT_TEAMMATE (+3), MVP (+5)
    - Needs complete replacement with new categories

4. **`ReputationService`** (`src/main/java/com/tatumgames/mikros/services/ReputationService.java`)
    - Has `reportToExternalAPI()` method (TODO)
    - Needs implementation for `/trackPlayerRating` API

5. **`InMemoryReputationService`** (`src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`)
    - Currently calculates local reputation
    - Needs API integration methods

### What's Missing:

1. `/lookup` command for admins to view user scores
2. API integration for `/trackPlayerRating` (POST)
3. API integration for `/getUserScoreDetail` (GET)
4. Stub JSON files for both APIs
5. New behavior categories with correct point values
6. Admin-only permission enforcement for `/report` and `/praise`
7. Request/response models for API calls
8. Updated documentation

---

## Required Changes

### 1. Update BehaviorCategory Enum

**File:** `src/main/java/com/tatumgames/mikros/models/BehaviorCategory.java`

**New Positive Behaviors:**

- `ACTIVE_PARTICIPATE` (+5 points)
- `GOOD_HELPER` (+2 points)
- `POSITIVE_INFLUENCER` (+3 points)
- `FRIENDLY_GREETER` (+1 point)

**New Negative Behaviors:**

- `SPAMMER` (-1 point)
- `TOXIC_BEHAVIOR` (-2 points)
- `HARRASSING` (-3 points)
- `IGNORING_RULES` (-2 points)
- `BAN_EVASION` (-5 points)
- `TROLL` (-3 points)
- `EXCESSIVE_PINGING` (-3 points)
- `NSFW_IN_NON_NSFW_SPACE` (-5 points)

**Action:**

- Replace all enum values
- Update `getPositiveBehaviors()` and `getNegativeBehaviors()` methods
- Update labels and descriptions

---

### 2. Make `/report` Admin-Only

**File:** `src/main/java/com/tatumgames/mikros/commands/ReportCommand.java`

**Changes:**

- Add permission check: `Permission.MODERATE_MEMBERS` (or `ADMINISTRATOR`)
- Set default permissions in `getCommandData()`:
  ```java
  .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
  ```
- Add permission validation in `handle()` method:
  ```java
  if (member == null || !member.hasPermission(Permission.MODERATE_MEMBERS)) {
      event.reply("❌ You don't have permission to use this command.")
          .setEphemeral(true).queue();
      return;
  }
  ```
- Update command description to indicate admin-only

---

### 3. Make `/praise` Admin-Only

**File:** `src/main/java/com/tatumgames/mikros/commands/PraiseCommand.java`

**Changes:**

- Same permission changes as `/report`
- Add `Permission.MODERATE_MEMBERS` check
- Update description

---

### 4. Create `/lookup` Command

**File:** `src/main/java/com/tatumgames/mikros/commands/LookupCommand.java` (NEW)

**Features:**

- Admin-only command (`Permission.MODERATE_MEMBERS`)
- Accepts multiple usernames (comma-separated or space-separated)
- Calls `/getUserScoreDetail` API with usernames
- Displays results in embed format showing:
    - Discord username
    - Discord user ID
    - Reputation score
    - List of Discord servers they're tracked on
    - Email (if available)

**Command Structure:**

```
/lookup usernames:<username1> [username2] [username3] ...
```

**Example:**

```
/lookup usernames:drxeno02 usernameA usernameB
```

**Response Format:**

- Embed with fields for each user found
- Show "User not found" for usernames not in system
- Handle partial results (some found, some not)

---

### 5. Create API Request/Response Models

**New Files:**

#### 5.1 `TrackPlayerRatingRequest.java`

**Location:** `src/main/java/com/tatumgames/mikros/models/api/TrackPlayerRatingRequest.java`

**Structure:**

```java
{
  "appGameId": "tg-644a4ae401486",
  "apiKeyType": "prod",
  "appVersion": "1.0.0",
  "sdkVersion": "1.0.0",
  "platform": "ios",
  "deviceId": "nfg547e5-184f-1g1b-b6ra-dfnyrt6565",
  "timestamp": "2023-04-19 20:47:15",
  "sender": {
    "discordUserId": "123456789012345678",
    "discordUsername": "Admin"
  },
  "participants": [
    {
      "discordUserId": "023456789012345678",
      "discordUsername": "drxeno02",
      "value": -3
    }
  ]
}
```

**Fields:**

- `appGameId` (String) - Hardcoded to "tg-644a4ae401486"
- `apiKeyType` (String) - Hardcoded to "prod"
- `appVersion` (String) - Hardcoded to "1.0.0"
- `sdkVersion` (String) - Hardcoded to "1.0.0"
- `platform` (String) - Hardcoded to "ios"
- `deviceId` (String) - Hardcoded to "nfg547e5-184f-1g1b-b6ra-dfnyrt6565"
- `timestamp` (String) - Current timestamp in format "yyyy-MM-dd HH:mm:ss"
- `sender` (Sender object) - Admin who made the report
- `participants` (List<Participant>) - List of users being reported (usually 1)

#### 5.2 `TrackPlayerRatingResponse.java`

**Location:** `src/main/java/com/tatumgames/mikros/models/api/TrackPlayerRatingResponse.java`

**Structure:**

```java
{
  "status": {
    "statusCode": 200,
    "statusMessage": "SUCCESS"
  }
}
```

#### 5.3 `GetUserScoreDetailRequest.java`

**Location:** `src/main/java/com/tatumgames/mikros/models/api/GetUserScoreDetailRequest.java`

**Note:** This is a GET request, so we'll pass usernames as query parameters or in the request body depending on API
design. For now, assume query parameters.

#### 5.4 `GetUserScoreDetailResponse.java`

**Location:** `src/main/java/com/tatumgames/mikros/models/api/GetUserScoreDetailResponse.java`

**Structure:**

```java
{
  "status": {
    "statusCode": 200,
    "statusMessage": "SUCCESS"
  },
  "data": {
    "scores": [
      {
        "id": "1",
        "email": "abc@gmail.com",
        "discordUserId": "123456789012345678",
        "discordUsername": "drxeno02",
        "reputationScore": 0,
        "discordServers": [
          987654321098765432,
          887654321098765432,
          787654321098765432,
          687654321098765432
        ]
      }
    ]
  }
}
```

**Nested Classes:**

- `Status` (statusCode, statusMessage)
- `Data` (scores list)
- `UserScore` (id, email, discordUserId, discordUsername, reputationScore, discordServers)

---

### 6. Create Stub JSON Files

#### 6.1 `trackPlayerRating.json` (Response)

**Location:** `src/main/resources/stubs/trackPlayerRating.json`

**Content:**

```json
{
  "status": {
    "statusCode": 200,
    "statusMessage": "SUCCESS"
  }
}
```

#### 6.2 `getUserScoreDetail.json` (Response)

**Location:** `src/main/resources/stubs/getUserScoreDetail.json`

**Content:**

```json
{
  "status": {
    "statusCode": 200,
    "statusMessage": "SUCCESS"
  },
  "data": {
    "scores": [
      {
        "id": "1",
        "email": "abc@gmail.com",
        "discordUserId": "123456789012345678",
        "discordUsername": "drxeno02",
        "reputationScore": 0,
        "discordServers": [
          987654321098765432,
          887654321098765432,
          787654321098765432,
          687654321098765432
        ]
      },
      {
        "id": "12",
        "email": "xyz@gmail.com",
        "discordUserId": "123456789012345678",
        "discordUsername": "drxeno02",
        "reputationScore": 10,
        "discordServers": [
          987654321098765432
        ]
      }
    ]
  }
}
```

---

### 7. Update ReputationService Interface

**File:** `src/main/java/com/tatumgames/mikros/services/ReputationService.java`

**New Methods:**

```java
/**
 * Tracks player rating by calling /trackPlayerRating API.
 * 
 * @param request the track player rating request
 * @return true if successful, false otherwise
 */
boolean trackPlayerRating(TrackPlayerRatingRequest request);

/**
 * Gets user score details by calling /getUserScoreDetail API.
 * 
 * @param usernames list of Discord usernames to lookup
 * @return response containing user scores, or null if error
 */
GetUserScoreDetailResponse getUserScoreDetail(List<String> usernames);
```

---

### 8. Update InMemoryReputationService

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Changes:**

1. Implement `trackPlayerRating()`:
    - Load stub JSON from `stubs/trackPlayerRating.json`
    - Parse response
    - Return true if statusCode == 200
    - Log the request for debugging

2. Implement `getUserScoreDetail()`:
    - Load stub JSON from `stubs/getUserScoreDetail.json`
    - Parse response
    - Filter results by requested usernames (if stub has multiple)
    - Return `GetUserScoreDetailResponse` object
    - Handle case where username not found (return empty scores list)

3. Update `reportToExternalAPI()`:
    - Create `TrackPlayerRatingRequest` from `BehaviorReport`
    - Call `trackPlayerRating()` instead of placeholder
    - Map behavior category to point value

---

### 9. Update ReportCommand to Call API

**File:** `src/main/java/com/tatumgames/mikros/commands/ReportCommand.java`

**Changes:**

1. After creating `BehaviorReport`, call `reputationService.reportToExternalAPI(report)`
2. This should internally call `/trackPlayerRating` API
3. Show success/failure message to admin
4. Remove local reputation calculation (server handles it now)

**Updated Flow:**

1. Admin runs `/report user:@drxeno02 behavior:HARRASSING`
2. Validate permissions
3. Create `BehaviorReport` object
4. Build `TrackPlayerRatingRequest`:
    - Set sender to admin
    - Set participant to target user with value = -3 (HARRASSING weight)
    - Set timestamp to current time
5. Call `reputationService.trackPlayerRating(request)`
6. Show confirmation to admin

---

### 10. Update PraiseCommand to Call API

**File:** `src/main/java/com/tatumgames/mikros/commands/PraiseCommand.java`

**Changes:**

- Same as ReportCommand
- Call API after creating report
- Use positive point values

---

### 11. Create LookupCommand Implementation

**File:** `src/main/java/com/tatumgames/mikros/commands/LookupCommand.java` (NEW)

**Implementation:**

1. Check admin permissions
2. Parse usernames from command option (accept multiple)
3. Call `reputationService.getUserScoreDetail(usernames)`
4. Build embed response:
    - Title: "🔍 Reputation Score Lookup"
    - For each user in response:
        - Field: Username, Discord ID, Score
        - Field: Servers (list of server IDs)
        - Field: Email (if available)
    - Show "User not found" for usernames not in results
5. Handle errors gracefully

**Command Options:**

- `usernames` (String, required) - Comma or space-separated list

**Example Embed:**

```
🔍 Reputation Score Lookup

User: drxeno02
Discord ID: 123456789012345678
Reputation Score: 0
Email: abc@gmail.com
Servers: 987654321098765432, 887654321098765432, ...

─────────────────────────

User: usernameA
Discord ID: 023456789012345678
Reputation Score: 10
Email: xyz@gmail.com
Servers: 987654321098765432
```

---

### 12. Register LookupCommand in BotMain

**File:** `src/main/java/com/tatumgames/mikros/bot/BotMain.java`

**Changes:**

- Add: `registerHandler(new LookupCommand(reputationService));`
- Place with other reputation commands

---

### 13. Update Documentation

#### 13.1 `docs/REPUTATION_SYSTEM.md`

**Changes:**

- Update behavior categories table
- Mark `/report` and `/praise` as admin-only
- Add `/lookup` command documentation
- Remove local reputation calculation details (server handles it)
- Update API integration status
- Remove references to local vs global (all handled by API)

#### 13.2 `docs/API_REPUTATION_SCORE_UPDATE.md`

**Changes:**

- Update to reflect `/trackPlayerRating` API structure
- Update request/response examples
- Update behavior categories

#### 13.3 `docs/API_REPUTATION_SCORE.md`

**Changes:**

- Update to reflect `/getUserScoreDetail` API structure
- Update request/response examples

#### 13.4 `docs/ADMIN_COMMANDS.md`

**Changes:**

- Add `/lookup` command
- Mark `/report` and `/praise` as admin-only
- Update behavior categories

#### 13.5 `docs/FEATURE_INVENTORY.md`

**Changes:**

- Update reputation commands section
- Mark commands as admin-only

#### 13.6 `README.md`

**Changes:**

- Update command list
- Mark `/report` and `/praise` as admin-only
- Add `/lookup` command

---

## Implementation Steps

### Step 1: Update BehaviorCategory Enum

1. Replace all enum values with new categories
2. Update point values
3. Update helper methods
4. Test enum compilation

### Step 2: Create API Models

1. Create `TrackPlayerRatingRequest.java` with nested classes
2. Create `TrackPlayerRatingResponse.java`
3. Create `GetUserScoreDetailResponse.java` with nested classes
4. Add Jackson annotations for JSON serialization/deserialization

### Step 3: Create Stub JSON Files

1. Create `stubs/trackPlayerRating.json`
2. Create `stubs/getUserScoreDetail.json`
3. Test JSON parsing

### Step 4: Update ReputationService

1. Add new method signatures
2. Update `InMemoryReputationService` implementations
3. Add JSON loading logic (similar to `InMemoryGamePromotionService`)

### Step 5: Make Commands Admin-Only

1. Update `ReportCommand` permissions
2. Update `PraiseCommand` permissions
3. Test permission checks

### Step 6: Create LookupCommand

1. Create command class
2. Implement permission check
3. Implement API call
4. Implement embed formatting
5. Register in `BotMain`

### Step 7: Update ReportCommand and PraiseCommand

1. Remove local reputation calculation
2. Add API call logic
3. Build `TrackPlayerRatingRequest` from `BehaviorReport`
4. Handle API response

### Step 8: Update Documentation

1. Update all markdown files
2. Verify command lists
3. Update API documentation

### Step 9: Testing

1. Test `/report` with admin permissions
2. Test `/report` without admin permissions (should fail)
3. Test `/praise` with admin permissions
4. Test `/lookup` with single username
5. Test `/lookup` with multiple usernames
6. Test stub JSON loading
7. Verify API request structure matches requirements

---

## Key Details

### Behavior Category Mapping

**Positive:**

- `ACTIVE_PARTICIPATE` → +5
- `GOOD_HELPER` → +2
- `POSITIVE_INFLUENCER` → +3
- `FRIENDLY_GREETER` → +1

**Negative:**

- `SPAMMER` → -1
- `TOXIC_BEHAVIOR` → -2
- `HARRASSING` → -3
- `IGNORING_RULES` → -2
- `BAN_EVASION` → -5
- `TROLL` → -3
- `EXCESSIVE_PINGING` → -3
- `NSFW_IN_NON_NSFW_SPACE` → -5

### API Request Format for `/trackPlayerRating`

**Hardcoded Values:**

- `appGameId`: "tg-644a4ae401486"
- `apiKeyType`: "prod"
- `appVersion`: "1.0.0"
- `sdkVersion`: "1.0.0"
- `platform`: "ios"
- `deviceId`: "nfg547e5-184f-1g1b-b6ra-dfnyrt6565"

**Dynamic Values:**

- `timestamp`: Current time in "yyyy-MM-dd HH:mm:ss" format
- `sender.discordUserId`: Admin's Discord ID
- `sender.discordUsername`: Admin's Discord username
- `participants[0].discordUserId`: Target user's Discord ID
- `participants[0].discordUsername`: Target user's Discord username
- `participants[0].value`: Behavior category weight (e.g., -3 for HARRASSING)

### API Response Handling

**`/trackPlayerRating`:**

- Check `status.statusCode == 200`
- Log success/failure
- Return boolean

**`/getUserScoreDetail`:**

- Parse `data.scores` array
- Filter by requested usernames
- Return full response object
- Handle empty results gracefully

### Permission Model

- `/report`: `Permission.MODERATE_MEMBERS` (or `ADMINISTRATOR`)
- `/praise`: `Permission.MODERATE_MEMBERS` (or `ADMINISTRATOR`)
- `/lookup`: `Permission.MODERATE_MEMBERS` (or `ADMINISTRATOR`)
- `/score`: Everyone (unchanged - users can check their own score)

---

## Testing Checklist

- [ ] `/report` command requires admin permissions
- [ ] `/praise` command requires admin permissions
- [ ] `/lookup` command requires admin permissions
- [ ] `/report` with valid behavior category calls API
- [ ] `/praise` with valid behavior category calls API
- [ ] `/lookup` with single username returns results
- [ ] `/lookup` with multiple usernames returns all results
- [ ] `/lookup` with non-existent username shows "not found"
- [ ] Behavior categories have correct point values
- [ ] API request structure matches specification
- [ ] Stub JSON files load correctly
- [ ] Error handling works for API failures
- [ ] Documentation updated correctly
- [ ] All commands registered in BotMain

---

## Notes

1. **Warn/Kick/Ban Commands**: These are separate from reputation and should remain as-is. They use Discord's native
   moderation features and do NOT affect reputation scores.

2. **Local Reputation**: The old local reputation calculation is removed. All reputation is now handled by the server
   via API.

3. **Stub Files**: Stub files are temporary until real APIs are available. The code should be structured to easily swap
   stub loading with real HTTP calls.

4. **Username vs User ID**: The `/lookup` command accepts usernames, but internally the API may use Discord user IDs.
   Ensure proper mapping.

5. **Multiple Users in One Report**: The `/trackPlayerRating` API supports multiple participants, but for now we'll only
   send one user per report (the target user).

6. **Timestamp Format**: Use "yyyy-MM-dd HH:mm:ss" format for timestamps in API requests (not ISO 8601).

---

**Status:** 📋 Ready for Implementation  
**Priority:** HIGH  
**Estimated Effort:** 4-6 hours

