# TASKS_21.md – MIKROS Bot Promotion Feature Update

## Objective

Update the MIKROS Discord Bot so it automatically promotes games using the `/getAllApps` endpoint (stubbed for now) and schedules promotions intelligently. Promotions should follow the 4-step story format while respecting campaign dates and avoiding spam.

---

## Current State Analysis

### What Exists:
1. **GamePromotion Model** (`src/main/java/com/tatumgames/mikros/models/GamePromotion.java`)
   - Uses `gameId` (int), `gameName`, `description`, `promotionUrl`
   - Has `campaignStartDate`, `campaignEndDate`, `frequencyDays`
   - Does NOT match new API structure (missing `appId`, `appGameId`, campaign CTAs, social media)

2. **GamePromotionScheduler** (`src/main/java/com/tatumgames/mikros/services/GamePromotionScheduler.java`)
   - Checks every hour (60 minutes) ✓
   - Has 4 intro templates and 4 CTA templates
   - Does NOT track promotion step (1-4)
   - Does NOT support multiple games in one promotion
   - Does NOT use new API structure

3. **InMemoryGamePromotionService** (`src/main/java/com/tatumgames/mikros/services/InMemoryGamePromotionService.java`)
   - Returns test data with gameId 999
   - Tracks last post time per guild per gameId
   - Does NOT load from stub JSON file
   - Does NOT use appId (String) for tracking

4. **Commands** (all exist):
   - `/admin-setup-promotion-channel` ✓
   - `/admin-set-promotion-verbosity` ✓
   - `/admin-force-promotion-check` ✓
   - `/disable-promotions` - TODO (mentioned but not implemented)

### What's Missing:
1. Stub JSON file for `/getAllApps` response
2. New data model matching `/getAllApps` API structure
3. 4-step promotion tracking (which step has been posted for each app)
4. 20 message templates (currently only 4 intro templates)
5. Support for CTAs (multiple store links)
6. Support for social media links
7. Multi-game promotion (step 3: "MIKROS Top Picks")
8. Campaign date validation using new structure
9. Minimum 24-hour interval enforcement (respecting PromotionVerbosity)

---

## Required Changes

### 1. Create Stub JSON File

**File:** `src/main/resources/stubs/getAllApps.json`

Create this file with the exact structure provided in the requirements. This will serve as the data source until the real API is available.

**Key Points:**
- Two games: "hv-nemesis" and "hv-nervo"
- Each has `appId`, `appGameId`, `appName`, descriptions
- Campaign includes `startDate` (Unix timestamp), `endDate`, `ctas`, `socialMedia`
- Add TODO comment: "Replace stub URLs and placeholders when API is live"

---

### 2. Create New Data Models

**File:** `src/main/java/com/tatumgames/mikros/models/AppPromotion.java` (NEW)

Create a new model to match the `/getAllApps` API structure with:
- `appId`, `appGameId`, `appName`, descriptions
- Nested `Campaign` class with `startDate`, `endDate`, `ctas`, `socialMedia`
- JSON deserialization support

---

### 3. Update GamePromotionService Interface

**File:** `src/main/java/com/tatumgames/mikros/services/GamePromotionService.java`

Add new methods:
- `List<AppPromotion> fetchAllApps()` - Load from stub JSON (TODO: replace with API)
- `int getLastPromotionStep(String guildId, String appId)` - Get last step posted (0-4)
- `void recordPromotionStep(String guildId, String appId, int step, Instant postTime)` - Record step
- `boolean hasAppBeenPromoted(String guildId, String appId)` - Check if app promoted

---

### 4. Update InMemoryGamePromotionService

**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryGamePromotionService.java`

**Changes Required:**
- Add JSON loading method `loadStubApps()` that reads from `stubs/getAllApps.json`
- Add promotion step tracking: `Map<String, Map<String, PromotionStepRecord>>`
- Implement new methods from interface
- Add TODO comment for API integration

---

### 5. Create Promotion Step Manager

**File:** `src/main/java/com/tatumgames/mikros/services/PromotionStepManager.java` (NEW)

Manages 4-step promotion logic:
- Step 1: Introduce the game (at campaign start)
- Step 2: Add more details (33% through campaign)
- Step 3: Multiple games promotion (66% through campaign, only if multiple games exist)
- Step 4: Final chance (90% through campaign)
- Enforces minimum 24-hour interval between promotions

---

### 6. Create 20 Message Templates

**File:** `src/main/java/com/tatumgames/mikros/services/PromotionMessageTemplates.java` (NEW)

Create templates organized by promotion step:
- Step 1: 5 templates (introduce game)
- Step 2: 5 templates (add details)
- Step 3: 5 templates (multiple games)
- Step 4: 5 templates (final chance)

**Note:** Cursor AI creates 10 templates, developer adds 10 more to reach 20 total.

**Placeholders:**
- `<app_name>`, `<short_description>`, `<long_description>`
- `<game_list>` (for step 3)
- CTA and social media placeholders

---

### 7. Update GamePromotionScheduler

**File:** `src/main/java/com/tatumgames/mikros/services/GamePromotionScheduler.java`

**Major Changes:**
- Use `fetchAllApps()` instead of `fetchActivePromotions()`
- Integrate `PromotionStepManager` to determine next step
- Use `PromotionMessageTemplates` for message generation
- Support multi-game promotion for step 3
- Include at least one CTA per message
- Optionally include social media links (~30% chance)
- Track promotions by `appId` instead of `gameId`

---

### 8. Implement /disable-promotions Command

**File:** `src/main/java/com/tatumgames/mikros/commands/DisablePromotionsCommand.java` (NEW)

Admin-only command that:
- Removes promotion channel configuration
- Clears all promotion tracking data
- Confirms promotions are disabled

---

## Implementation Steps (In Order)

1. **Create Stub JSON File** - `src/main/resources/stubs/getAllApps.json`
2. **Create AppPromotion Model** - New model matching API structure
3. **Add JSON Parsing** - Load stub JSON in service
4. **Add Promotion Step Tracking** - Track which step (1-4) has been posted
5. **Create Promotion Step Manager** - Logic for 4-step distribution
6. **Create Message Templates** - 10 templates (developer adds 10 more)
7. **Update Services** - Update interface and implementation
8. **Update Scheduler** - Refactor to use new structure
9. **Create Disable Command** - Implement `/admin-disable-promotions`
10. **Register Command** - Add to BotMain

---

## Key Implementation Details

### Campaign Date Validation
```java
Instant now = Instant.now();
if (now.isBefore(campaign.getStartDate()) || now.isAfter(campaign.getEndDate())) {
    // Skip this app
    continue;
}
```

### Minimum 24-Hour Interval
```java
Instant lastPostTime = getLastPostTime(guildId, appId);
if (lastPostTime != null) {
    Instant nextAllowedTime = lastPostTime.plus(24, ChronoUnit.HOURS);
    if (now.isBefore(nextAllowedTime)) {
        // Too soon, skip
        continue;
    }
}
```

### Promotion Step Distribution
- Step 1: At campaign start (or as soon as possible)
- Step 2: 33% through campaign period
- Step 3: 66% through campaign period (only if multiple games exist)
- Step 4: 90% through campaign period (near end)

### CTA Selection
- Always include at least one CTA
- Prefer `website` or `google_store` if available
- Randomly select from available CTAs
- Format as: `[Store Name](URL)`

### Social Media Links
- Include with ~30% probability
- Format as: `[Platform](URL)`
- Add to embed footer or as separate field

---

## Notes / TODOs

1. **Stub JSON** serves until real `/getAllApps` API is available
2. **Respect campaign dates** - only promote during active campaign period
3. **Respect verbosity** - minimum 24 hours between promotions (enforced by PromotionVerbosity)
4. **Track promotions per appId** - prevent duplicates using `appId` as UUID
5. **Each promotion message must include at least one CTA**
6. **Some messages may include social media links** (30% chance)
7. **4 promotions per game** - distributed across campaign period
8. **Step 3 is special** - only post if multiple games exist in campaign
9. **20 message templates** - Cursor AI creates 10, developer adds 10 more
10. **Automatic promotions start** once promotion channel is set

---

## Files to Create/Modify

### New Files:
1. `src/main/resources/stubs/getAllApps.json`
2. `src/main/java/com/tatumgames/mikros/models/AppPromotion.java`
3. `src/main/java/com/tatumgames/mikros/services/PromotionStepManager.java`
4. `src/main/java/com/tatumgames/mikros/services/PromotionMessageTemplates.java`
5. `src/main/java/com/tatumgames/mikros/commands/DisablePromotionsCommand.java`

### Modified Files:
1. `src/main/java/com/tatumgames/mikros/services/GamePromotionService.java` - Add new methods
2. `src/main/java/com/tatumgames/mikros/services/InMemoryGamePromotionService.java` - Add JSON loading, step tracking
3. `src/main/java/com/tatumgames/mikros/services/GamePromotionScheduler.java` - Major refactor for new structure
4. `src/main/java/com/tatumgames/mikros/bot/BotMain.java` - Register DisablePromotionsCommand

---

## Testing Checklist

- [ ] Stub JSON loads correctly
- [ ] AppPromotion model deserializes correctly
- [ ] Campaign date validation works
- [ ] Promotion step 1 posts at campaign start
- [ ] Promotion step 2 posts at 33% through campaign
- [ ] Promotion step 3 posts only if multiple games exist
- [ ] Promotion step 4 posts near campaign end
- [ ] Minimum 24-hour interval is enforced
- [ ] Each message includes at least one CTA
- [ ] Social media links appear ~30% of the time
- [ ] Multi-game promotion (step 3) formats correctly
- [ ] Tracking uses appId correctly
- [ ] Commands work as expected
- [ ] Automatic promotions start when channel is set

