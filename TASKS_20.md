# Fix Promo Commands System

## Overview
The current promo commands system needs to be fixed to properly fetch active promotions from the Tatum Games API and post them based on frequency within campaign date ranges. The system should use intro message templates and support test messages while the API is being developed.

## Current State Analysis
- `GamePromotionService` and `GamePromotionScheduler` exist but use `deadline`/`isPushed` instead of `campaign_start_date`/`campaign_end_date`
- `fetchActivePromotions()` returns empty list (API not integrated)
- No frequency tracking per promotion per guild
- No intro message templates
- No test message support

## Changes Required

### 1. Update GamePromotion Model
**File:** `src/main/java/com/tatumgames/mikros/models/GamePromotion.java`

- Replace `deadline` and `isPushed` with:
  - `campaignStartDate` (Instant)
  - `campaignEndDate` (Instant)
  - `frequencyDays` (int) - how often to post (every X days, provided by backend)
- Update `isReadyToPromote()` to check if current time is within campaign date range
- Add getters/setters for new fields

### 2. Add Last Post Tracking
**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryGamePromotionService.java`

- Add tracking map: `Map<String, Map<Integer, Instant>>` for `guildId -> (gameId -> lastPostTime)`
- Add method `getLastPostTime(String guildId, int gameId)` returning Instant or null
- Add method `recordPostTime(String guildId, int gameId, Instant postTime)`
- Update `markAsPromoted()` to also record post time

### 3. Add API Call TODO
**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryGamePromotionService.java`

- In `fetchActivePromotions()`:
  - Add TODO comment noting that an API call is needed to fetch active promotions from the Tatum Games API
  - Add comment describing expected response structure with:
    - `game_name`, `game_description`, `url` (download link)
    - `campaign_start_date`, `campaign_end_date` (ISO 8601 strings)
    - `frequency_days` (provided by backend - how often to post this promotion)
  - For now, return test promotion with test metadata (see step 4)

### 4. Implement Test Message
**File:** `src/main/java/com/tatumgames/mikros/services/InMemoryGamePromotionService.java`

- In `fetchActivePromotions()`, return a test `GamePromotion` with test metadata:
  - `game_name = "Heroes Vs Villains: Rise of Nemesis"`
  - `game_description = "The core mechanics focus on guiding your character, a Guardian (superhero), through various levels where you face waves of enemies. As you progress through the game, you engage in combat, defeat enemies, and collect rewards like coins and gear."`
  - `url = "https://developer.tatumgames.com/"`
  - `campaign_start_date = Instant.now()` (today)
  - `campaign_end_date = Instant.now().plus(24, ChronoUnit.HOURS)` (24 hours later)
  - `frequencyDays = 1` (provided by backend, for testing set to 1 day)
- Add comment indicating this is temporary test data to showcase the feature

### 5. Add Intro Message Templates
**File:** `src/main/java/com/tatumgames/mikros/services/GamePromotionScheduler.java`

- Create method `selectIntroTemplate()` that randomly selects from 4 intro templates:
  1. "This game is making waves! Have you heard of <game_name>?"
  2. "If you have not checked out <game_name> do it now."
  3. "Here is one of MIKROS' favorite games, are you playing <game_name> already?"
  4. "Let's support <game_name>. Another amazing game within the MIKROS Ecosystem."
- Create method `selectCtaText()` that randomly selects from 4 CTA options:
  1. "Where to Get It?:"
  2. "Play It Today:"
  3. "Try It Out Today:"
  4. "Play It Here:"
- For test message, use: "This game is EPIC! Let's rally behind <game_name>" with CTA "Download It Today:"

### 6. Update Posting Logic
**File:** `src/main/java/com/tatumgames/mikros/services/GamePromotionScheduler.java`

- Update `postPromotionsToChannel()`:
  - Check if promotion is within campaign date range using `campaignStartDate` and `campaignEndDate`
  - Check if enough time has passed since last post (frequency check):
    - Get last post time for this promotion in this guild
    - Get `frequencyDays` from the promotion (provided by backend)
    - If null or `Instant.now().isAfter(lastPostTime.plus(frequencyDays, ChronoUnit.DAYS))`, allow posting
  - Use intro templates and CTA templates when building message
  - Replace `<game_name>`, `<game_description>`, `<url>` placeholders in templates
- Update `postPromotion()` to use new message format:
  - Format: "Intro: [selected intro]\n\nDescription: <game_description>\n\n[CTA text] <url>"
  - Structure: Three-line format with clear labels

### 7. Update Frequency Logic
**File:** `src/main/java/com/tatumgames/mikros/services/GamePromotionScheduler.java`

- Remove old `isReadyToPromote()` check
- Replace with:
  - Check if `Instant.now().isAfter(campaignStartDate) && Instant.now().isBefore(campaignEndDate)`
  - Check if frequency cooldown has passed (last post + frequencyDays from backend)
- Update `hasBeenPromoted()` logic - change to check last post time instead of boolean flag

## Implementation Notes

- Use Java's built-in `java.net.http.HttpClient` for future API calls (Java 21 has this)
- Use `java.time.Instant` for all date/time operations
- Use `java.time.temporal.ChronoUnit.DAYS` for frequency calculations
- Test message should be clearly marked in code comments
- All template placeholders should use angle brackets: `<game_name>`, `<game_description>`, `<url>`
- Message format: "Intro: <intro text>\n\nDescription: <description text>\n\n<CTA text> <url>"
- Frequency is provided by backend API, not configured per guild

## Testing Strategy

1. Test with test message to verify template formatting
2. Verify frequency logic (should post once, then wait for frequency period)
3. Verify campaign date range (should not post before start or after end)
4. Test with multiple guilds to ensure per-guild tracking works

## Files to Modify

1. `src/main/java/com/tatumgames/mikros/models/GamePromotion.java` - Update model
2. `src/main/java/com/tatumgames/mikros/services/InMemoryGamePromotionService.java` - Add API TODO, test data, tracking
3. `src/main/java/com/tatumgames/mikros/services/GamePromotionScheduler.java` - Update posting logic, add templates
4. `src/main/java/com/tatumgames/mikros/services/GamePromotionService.java` - Update interface if needed

