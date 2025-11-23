# Local Testing Guide - TG-MIKROS Discord Bot

This document provides comprehensive instructions for testing the MIKROS Discord Bot in a local development environment.

---

## Table of Contents

1. [Running the Bot Locally](#51--running-the-bot-locally)
2. [Full Testing Matrix](#52--full-testing-matrix)
3. [Automated Tests](#53--automated-tests)
4. [Local-Only Mock Mode](#54--local-only-mock-mode)

---

## 5.1 — Running the Bot Locally

### Prerequisites

- **Java 17 or higher** installed and configured
- **Gradle** (included via Gradle Wrapper - `./gradlew`)
- **Discord Bot Token** from Discord Developer Portal
- **Git** (for cloning repository)

### Environment Variables

The bot requires environment variables to be set. You can configure them in two ways:

#### Option 1: `.env` File (Recommended)

Create a `.env` file in the project root directory:

```env
# Required
DISCORD_BOT_TOKEN=your_discord_bot_token_here

# Optional
BOT_OWNER_ID=your_discord_user_id_here
```

**Security Note:** The `.env` file is already in `.gitignore`. Never commit your bot token to version control.

**File Permissions (Linux/Mac):**
```bash
chmod 600 .env
```

#### Option 2: System Environment Variables

You can also set environment variables directly in your system:

**Windows (PowerShell):**
```powershell
$env:DISCORD_BOT_TOKEN="your_token_here"
$env:BOT_OWNER_ID="your_user_id_here"
```

**Linux/Mac:**
```bash
export DISCORD_BOT_TOKEN="your_token_here"
export BOT_OWNER_ID="your_user_id_here"
```

### How to Set Up a Discord Bot

1. **Go to Discord Developer Portal**
   - Visit: https://discord.com/developers/applications
   - Log in with your Discord account

2. **Create a New Application**
   - Click "New Application"
   - Enter a name (e.g., "MIKROS Bot - Test")
   - Click "Create"

3. **Create a Bot**
   - Navigate to the "Bot" section in the left sidebar
   - Click "Add Bot"
   - Confirm by clicking "Yes, do it!"

4. **Get Your Bot Token**
   - Under the "Token" section, click "Reset Token" or "Copy"
   - **Important:** Save this token securely - you won't be able to see it again!
   - Copy the token to your `.env` file as `DISCORD_BOT_TOKEN`

5. **Configure Bot Settings**
   - Under "Privileged Gateway Intents", enable:
     - ✅ **MESSAGE CONTENT INTENT** (Required for message analysis)
     - ✅ **SERVER MEMBERS INTENT** (Required for member tracking)
   - Click "Save Changes"

6. **Get Your User ID (Optional)**
   - Enable Developer Mode in Discord (User Settings → Advanced → Developer Mode)
   - Right-click your username → "Copy ID"
   - Add to `.env` as `BOT_OWNER_ID`

7. **Invite Bot to Your Test Server**
   - Go to "OAuth2" → "URL Generator"
   - Select scopes:
     - ✅ `bot`
     - ✅ `applications.commands`
   - Select bot permissions:
     - ✅ **Administrator** (for testing) OR individually:
       - ✅ Kick Members
       - ✅ Ban Members
       - ✅ Moderate Members
       - ✅ Manage Messages
       - ✅ Read Message History
       - ✅ Send Messages
       - ✅ Embed Links
       - ✅ Attach Files
   - Copy the generated URL
   - Open in browser and select your test server
   - Click "Authorize"

### Gradle Commands

The project uses **Gradle** (not npm) for build and run tasks. Use the Gradle Wrapper (`./gradlew` on Linux/Mac, `gradlew.bat` on Windows):

#### Build Commands

```bash
# Build the project
./gradlew build

# Clean and rebuild
./gradlew clean build

# Build without running tests
./gradlew build -x test
```

#### Run Commands

```bash
# Run the bot
./gradlew run

# Run with specific JVM options
./gradlew run --args="-Xmx512m"

# Run in background (Linux/Mac)
nohup ./gradlew run > bot.log 2>&1 &
```

#### Test Commands

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "InMemoryModerationLogServiceTest"

# Run tests with verbose output
./gradlew test --info

# View test reports
# Open: build/reports/tests/test/index.html
```

#### Other Useful Commands

```bash
# Check for dependency updates
./gradlew dependencies

# Generate JAR file
./gradlew jar

# View all available tasks
./gradlew tasks
```

### Enabling Mock API Mode

The bot **automatically runs in mock mode** by default. All external API calls are handled by mock/in-memory services:

- **Game Stats:** `MockGameStatsService` - Returns placeholder data
- **Reputation:** `InMemoryReputationService` - Local reputation tracking
- **Game Promotions:** `InMemoryGamePromotionService` - In-memory promotion storage
- **Moderation Logs:** `InMemoryModerationLogService` - In-memory action storage

**No configuration needed** - mock mode is the default behavior. When real APIs are integrated, you can switch by:

1. Implementing real service classes
2. Updating `BotMain.java` to use real services instead of mock ones
3. Adding environment variables for API keys/endpoints

**Current Mock Services:**
- ✅ All analytics commands return mock data
- ✅ Reputation scores calculated locally
- ✅ Game promotions stored in memory
- ✅ Moderation actions logged in memory
- ✅ No external API calls are made

---

## 5.2 — Full Testing Matrix

### Admin Flow Testing

#### Moderation Commands

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/admin-warn` | Warn a user with reason | Warning logged, confirmation message | Check `/admin-history` to verify |
| `/admin-warn` | Warn same user 3 times | Auto-escalation triggers (kick) | Requires 3 warnings threshold |
| `/admin-kick` | Kick a user | User removed, action logged | Bot must have KICK_MEMBERS permission |
| `/admin-ban` | Ban a user | User banned, action logged | Bot must have BAN_MEMBERS permission |
| `/admin-ban` | Ban with delete_days:7 | Messages deleted, user banned | Deletes last 7 days of messages |
| `/admin-history` | View user history | Shows all actions, reputation score | Includes warnings, kicks, bans |

#### Enhanced Moderation

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/warn-suggestions` | Analyze channel messages | List of concerning messages | Analyzes last 200 messages by default |
| `/ban-suggestions` | Analyze for severe violations | High/Critical severity messages only | Stricter filtering than warn-suggestions |
| `/server-stats` | View server statistics | Activity stats, top channels | Requires MODERATE_MEMBERS permission |
| `/top-contributors` | View top 10 users | Leaderboard by message count | Based on tracked activity |

#### Reputation System

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/praise` | Praise a user | Reputation score increases | Uses BehaviorCategory enum |
| `/report` | Report negative behavior | Reputation score decreases | Uses BehaviorCategory enum |
| `/score` | Check user reputation | Shows local and global score | Global shows "API not available" in mock mode |

### Games Testing

#### Community Games Setup

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/admin-game-setup` | Configure games channel | Channel set, games enabled | Admin only |
| `/admin-game-setup` | Set reset time to 00:00 UTC | Daily reset scheduled | Time in UTC |
| `/admin-game-config` | Enable/disable specific games | Game status updated | Can toggle individual games |

#### Word Unscramble Game

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/guess gameplay` | Correct guess | Public announcement, game ends | First correct guess wins |
| `/guess wrongword` | Incorrect guess | Private error message | Can try again |
| `/game-stats` | View game status | Shows current game, leaderboard | Displays time until reset |

#### Dice Battle Game

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/roll` | Roll dice | D20 result shown | Tracks highest roll of day |
| `/roll` | Multiple rolls | Each roll tracked | Leaderboard shows top roller |
| `/game-stats` | View dice leaderboard | Top rollers displayed | Resets daily |

#### Emoji Match Game

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/match 🎮🎲🎯` | Correct match | Public announcement, game ends | First correct match wins |
| `/match ❌❌❌` | Incorrect match | Private error message | Can try again |

### RPG Testing

#### Character Creation

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/rpg-register name:Aragorn class:WARRIOR` | Create character | Character created with stats | One character per user |
| `/rpg-register` | Try to register twice | Error: already registered | Must use `/rpg-profile` to view |
| `/rpg-register` | Invalid class name | Error: invalid class | Must be WARRIOR, MAGE, or ROGUE |

#### Character Management

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/rpg-profile` | View own profile | Character stats, level, XP | Shows cooldown status |
| `/rpg-profile user:@Player` | View other player | Their character profile | Public profile view |
| `/rpg-leaderboard` | View top players | Sorted by level, then XP | Server-wide leaderboard |

#### RPG Actions

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/rpg-action type:explore` | Explore action | Narrative encounter, XP gained | Cooldown: 24 hours default |
| `/rpg-action type:train` | Train action | Stat increase, XP gained | Random stat boosted |
| `/rpg-action type:battle` | Battle action | Combat outcome, XP/HP changes | Can win or lose |
| `/rpg-action` | Action before cooldown | Error: cooldown active | Shows time remaining |

#### RPG Configuration

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/admin-rpg-config toggle enable` | Enable RPG | RPG system enabled | Admin only |
| `/admin-rpg-config set-cooldown 12` | Set cooldown | Cooldown changed to 12 hours | Affects all players |
| `/admin-rpg-config set-xp-multiplier 1.5` | Set XP multiplier | XP gains increased by 50% | 0.5x to 2.0x range |

### Spelling Testing

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/spell-challenge` | View daily challenge | Scrambled word displayed | Auto-creates if none exists |
| `/guess gaming` | Correct guess | Points awarded (3 or 1) | First solver gets 3 pts |
| `/guess wrong` | Incorrect guess | Error, attempt counted | 3 attempts per day |
| `/guess` | 4th attempt | Error: no attempts remaining | Resets daily |
| `/spell-leaderboard` | View rankings | Top 10 players by points | All-time cumulative |

### Analytics Testing

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/gamestats trending-game-genres` | Get trending genres | Top 3 genres with growth % | Returns mock data |
| `/gamestats popular-content` | Get popular content | Top 5 content types | Returns mock data |
| `/gamestats total-users` | Get total users | User count displayed | Returns mock data (3,240,567) |
| `/gamestats avg-session-time genre:Action` | Get avg session | Average time for genre | Returns mock data |

**Note:** All analytics commands return **mock/placeholder data** in local testing mode. Real API integration is marked with TODO comments.

### Promotions Testing

#### Promotional Detection

| Test Case | Expected Result | Notes |
|-----------|-----------------|-------|
| User posts: "We're launching our game next week" | Bot prompts: "Want MIKROS promo code?" | Passive detection enabled |
| User posts: "My Steam page is live" | Bot prompts with promo offer | Regex pattern matching |
| User posts: "Need help promoting" | Bot prompts | Multiple trigger phrases |
| `/setup-promotions toggle disable` | Detection disabled | Admin command |
| `/set-promo-frequency 7` | Cooldown set to 7 days | Per-user cooldown |

#### Promotional Commands

| Command | Test Case | Expected Result | Notes |
|---------|-----------|-----------------|-------|
| `/promo-help` | Request help | DM with lead form | Collects email (TODO: API) |
| `/setup-promotions` | Configure detection | Settings updated | Admin only |
| `/set-promo-frequency` | Set cooldown | Cooldown updated | 1-30 days range |

---

## 5.3 — Automated Tests

### Existing Tests

#### InMemoryModerationLogServiceTest

**Location:** `src/test/java/com/tatumgames/mikros/services/InMemoryModerationLogServiceTest.java`

**Test Coverage:**
- ✅ Logging moderation actions
- ✅ Retrieving user history
- ✅ Filtering by action type
- ✅ Guild isolation
- ✅ User isolation
- ✅ Concurrent operations
- ✅ Null/blank validation
- ✅ History sorting (newest first)
- ✅ Action counting

**Run Tests:**
```bash
./gradlew test --tests "InMemoryModerationLogServiceTest"
```

**Test Results:**
- View HTML report: `build/reports/tests/test/index.html`
- All tests should pass ✅

### Test Structure

```
src/test/java/com/tatumgames/mikros/
└── services/
    └── InMemoryModerationLogServiceTest.java
```

### Running All Tests

```bash
# Run all tests
./gradlew test

# Run with verbose output
./gradlew test --info

# Run specific test class
./gradlew test --tests "InMemoryModerationLogServiceTest"

# Run tests matching pattern
./gradlew test --tests "*Moderation*"

# Skip tests during build
./gradlew build -x test
```

### Test Framework

- **JUnit 5** (Jupiter) - Test framework
- **Mockito** - Mocking framework (available but not yet used extensively)
- **JUnit Platform Launcher** - Test execution

### Adding New Tests

To add a new test:

1. Create test class in `src/test/java/` mirroring the source structure
2. Use JUnit 5 annotations:
   ```java
   @Test
   @DisplayName("Test description")
   void testMethodName() {
       // Test code
   }
   ```
3. Run with: `./gradlew test --tests "YourTestClass"`

### Test Best Practices

- ✅ One test per scenario
- ✅ Use descriptive test names
- ✅ Test both success and failure cases
- ✅ Test edge cases (null, empty, boundary values)
- ✅ Use `@BeforeEach` for setup
- ✅ Use `@DisplayName` for readable test descriptions

---

## 5.4 — Local-Only Mock Mode

### Overview

The bot runs in **mock mode by default** for local testing. All external API dependencies are replaced with in-memory or mock implementations that return placeholder data.

### Mock Services

#### 1. MockGameStatsService

**Purpose:** Provides mock analytics data

**Location:** `src/main/java/com/tatumgames/mikros/services/MockGameStatsService.java`

**Mock Data Returned:**
- Trending genres: Roguelike (+43.2%), Puzzle (+31.5%), Sandbox (+29.1%)
- Popular genres: Action (2.1M), RPG (1.8M), Strategy (1.5M)
- Total MIKROS Apps: 1,247
- Total Contributors: 45,823
- Total Users: 3,240,567
- Average gameplay time: 45-120 minutes (varies by genre)
- Average session time: 15-35 minutes (varies by genre)

**How to Test:**
```bash
# Run bot
./gradlew run

# In Discord, try:
/gamestats trending-game-genres
/gamestats total-users
/gamestats avg-gameplay-time genre:Action
```

**Note:** All data is static mock data. Real API integration will replace this service.

#### 2. InMemoryReputationService

**Purpose:** Tracks reputation scores locally

**Location:** `src/main/java/com/tatumgames/mikros/services/InMemoryReputationService.java`

**Features:**
- Calculates local reputation based on moderation actions
- Tracks positive/negative behavior
- Returns "API not available" for global reputation (expected in mock mode)

**How to Test:**
```bash
# Run bot
./gradlew run

# In Discord:
/praise @user behavior:GOOD_SPORTSMANSHIP
/report @user behavior:TROLLING
/score @user
/admin-history @user  # Shows reputation score
```

#### 3. InMemoryGamePromotionService

**Purpose:** Stores game promotion configuration in memory

**Location:** `src/main/java/com/tatumgames/mikros/services/InMemoryGamePromotionService.java`

**Features:**
- Stores promotion channel per server
- Stores verbosity settings
- Returns empty list for active promotions (no API integration)

**How to Test:**
```bash
# Run bot
./gradlew run

# In Discord:
/setup-promotion-channel channel:#promotions
/set-promotion-verbosity level:HIGH
/force-promotion-check  # Will show "No promotions available" (expected)
```

#### 4. InMemoryModerationLogService

**Purpose:** Stores moderation actions in memory

**Location:** `src/main/java/com/tatumgames/mikros/services/InMemoryModerationLogService.java`

**Features:**
- Stores all moderation actions
- Per-guild isolation
- Per-user history tracking
- Thread-safe operations

**How to Test:**
```bash
# Run bot
./gradlew run

# In Discord:
/admin-warn @user reason:Test warning
/admin-history @user  # Shows the warning
```

**Note:** Data is lost when bot restarts (in-memory storage).

### Testing Without Live APIs

#### Benefits of Mock Mode

1. **No External Dependencies:** Test without internet connection
2. **Fast Testing:** No network latency
3. **Predictable Results:** Mock data is consistent
4. **Safe Testing:** No risk of affecting production APIs
5. **Offline Development:** Develop without API access

#### What Works in Mock Mode

✅ **All Commands:** Every command works with mock data  
✅ **Moderation:** Full moderation system (in-memory)  
✅ **Games:** All games functional  
✅ **RPG:** Complete RPG system  
✅ **Spelling:** Spelling challenge works  
✅ **Analytics:** Returns mock statistics  
✅ **Promotions:** Detection and commands work (no API submission)  

#### What Doesn't Work (Expected)

❌ **Real Analytics Data:** Returns placeholder data  
❌ **Global Reputation:** Shows "API not available"  
❌ **Game Promotions:** No real promotions fetched  
❌ **API Submissions:** Lead submissions not sent (TODO in code)  
❌ **Persistence:** Data lost on bot restart (in-memory)  

### Switching to Real APIs (Future)

When APIs are ready:

1. **Implement Real Services:**
   - Create `RealGameStatsService` implementing `GameStatsService`
   - Create `RealReputationService` implementing `ReputationService`
   - Create `RealGamePromotionService` implementing `GamePromotionService`

2. **Update BotMain.java:**
   ```java
   // Replace:
   this.gameStatsService = new MockGameStatsService();
   
   // With:
   this.gameStatsService = new RealGameStatsService(apiKey, apiUrl);
   ```

3. **Add Environment Variables:**
   ```env
   MIKROS_API_KEY=your_api_key
   MIKROS_API_URL=https://api.tatumgames.com
   REPUTATION_API_URL=https://api.tatumgames.com/reputation
   ```

4. **Update ConfigLoader:**
   - Add methods to load API configuration
   - Add validation for API keys

### Mock Mode Verification

To verify you're in mock mode:

1. **Check Logs:**
   ```
   MockGameStatsService initialized (using placeholder data)
   InMemoryReputationService initialized
   InMemoryGamePromotionService initialized
   ```

2. **Test Analytics:**
   - Run `/gamestats trending-game-genres`
   - Should return same data every time (mock data)

3. **Test Reputation:**
   - Run `/score @user`
   - Global reputation should show "API not available"

---

## Troubleshooting

### Bot Won't Start

**Issue:** `Required configuration missing: DISCORD_BOT_TOKEN`

**Solution:**
- Check `.env` file exists in project root
- Verify `DISCORD_BOT_TOKEN` is set
- Ensure no extra spaces in token
- Try setting as system environment variable

### Commands Don't Appear

**Issue:** Slash commands not showing in Discord

**Solution:**
- Wait 1-5 minutes (Discord caches commands)
- Try in different channel
- Check bot has `applications.commands` scope
- Re-invite bot with correct permissions
- Restart Discord client

### Tests Fail

**Issue:** Tests fail with errors

**Solution:**
- Run `./gradlew clean test`
- Check Java version: `java -version` (must be 17+)
- Verify Gradle wrapper: `./gradlew --version`
- Check test output in `build/reports/tests/test/index.html`

### Mock Data Not Working

**Issue:** Commands return errors instead of mock data

**Solution:**
- Verify `MockGameStatsService` is used in `BotMain.java`
- Check logs for service initialization messages
- Ensure no real API services are configured
- Restart bot

---

## Quick Reference

### Essential Commands

```bash
# Build
./gradlew build

# Run
./gradlew run

# Test
./gradlew test

# Clean
./gradlew clean
```

### Environment Setup

```env
DISCORD_BOT_TOKEN=your_token
BOT_OWNER_ID=your_user_id
```

### Test Server Setup

1. Create test Discord server
2. Invite bot with Administrator permissions (for testing)
3. Create test channels:
   - `#general` - General testing
   - `#games` - Game commands
   - `#rpg` - RPG commands (optional)
   - `#admin` - Admin commands

---

**Last Updated:** 2025-01-27  
**Version:** 1.0

