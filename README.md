# MIKROS Discord Bot

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-8.5-blue.svg)](https://gradle.org/)
[![JDA](https://img.shields.io/badge/JDA-5.0.0--beta.20-5865F2.svg)](https://github.com/DV8FromTheWorld/JDA)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)

A comprehensive, modular Discord bot built with Java and JDA (Java Discord API) for the Tatum Games MIKROS ecosystem.
The bot provides moderation tools, community engagement features, game analytics, promotional lead generation, and
interactive gaming experiences.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Architecture](#architecture)
- [Installation & Setup](#installation--setup)
- [Commands & Usage](#commands--usage)
- [Configuration](#configuration)
- [Development Guidelines](#development-guidelines)
- [Documentation](#documentation)
- [Troubleshooting & FAQ](#troubleshooting--faq)
- [License & Credits](#license--credits)

---

## 🎯 Project Overview

The **MIKROS Discord Bot** is a production-ready Discord bot designed to serve game development communities with:

- **Moderation Tools** - Comprehensive admin commands for server management
- **Community Engagement** - Daily games, RPG system, and interactive features
- **Analytics Integration** - Real-time game statistics and industry metrics
- **Marketing Support** - Smart promotional lead generation
- **Reputation System** - User behavior tracking and scoring

The bot integrates with the **MIKROS Analytics & Marketing Platform**, providing insights and promotional opportunities
for game developers and studios.

### Goals

- Provide seamless server moderation and management
- Engage communities with fun, daily interactive games
- Offer valuable analytics and insights to developers
- Generate qualified leads for MIKROS marketing services
- Build long-term community engagement through RPG progression

---

## ✨ Features

### 🔨 Admin Tools (TASKS_01)

**Moderation Commands:**

- `/warn` - Issue warnings with logged reasons
- `/kick` - Remove users from the server
- `/ban` - Ban users with optional message deletion
- `/history` - View complete moderation history for any user

**Features:**

- Persistent moderation logging (in-memory, TODO: database)
- Role hierarchy validation
- Permission checks
- Detailed action timestamps

### 🛡️ Enhanced Moderation (TASKS_02)

**Smart Suggestions:**

- `/warn-suggestions` - AI-powered warning recommendations
- `/ban-suggestions` - Automated ban suggestions based on message analysis

**Community Features:**

- `/server-stats` - Server activity statistics and insights (includes bot prevention count)
- `/top-contributors` - Leaderboard of most active members
- `/praise` - Award positive reputation points (Admin only)
- `/report` - Report users for negative behavior (Admin only)
- `/lookup` - Lookup user reputation scores by username (Admin only)

**Reputation System:**

- Behavior tracking and scoring
- Positive/negative reputation points
- Integration with moderation actions
- Automatic bot detection reporting

### 🎮 Game Promotion System (TASKS_03)

**Admin Commands:**

- `/admin-promotion-setup` - Configure promotion posting channel
- `/admin-promotion-config` - Configure promotion settings (view, update-channel, set-verbosity, disable, force-check)

### 🔄 Auto-Bump System

**Admin Commands:**

- `/admin-bump-setup` - Set up automatic server bumping (channel, bots selection)
- `/admin-bump-config` - Configure auto-bump settings (view, set-interval, update-bots, disable)

**Features:**

- Automatic server bumping for Disboard and/or Disurl
- Configurable bump interval (1-24 hours, default: 4 hours)
- Per-server independent configuration
- Rate limit safety (respects external bot cooldowns)
- Bot presence verification before bumping

**Features:**

- Automated game promotion scheduling
- Configurable verbosity levels (LOW, MEDIUM, HIGH)
- Rich embed formatting
- Per-server configuration

**TODO:** Integration with MIKROS Game Promotion API

### 📊 MIKROS Ecosystem Analytics (TASKS_04)

**Setup:** Use `/admin-mikros-ecosystem-setup` to configure a channel for analytics commands.

**Command:** `/mikros-ecosystem` with 13 subcommands (requires channel setup):

**Trending Analytics:**
- `trending-game-genres` - Top 3 fastest-growing genres
- `trending-content-genres` - Trending content types
- `trending-content` - Top 5 trending in-game content
- `trending-gameplay-types` - Casual, competitive, etc.

**Popular Analytics:**
- `popular-game-genres` - Most played game genres
- `popular-content-genres` - Most engaging content genres
- `popular-content` - Top 5 in-game content experiences
- `popular-gameplay-types` - Most popular gameplay types

**Ecosystem Metrics:**
- `total-mikros-apps` - Total apps using MIKROS Analytics
- `total-mikros-contributors` - Total ecosystem contributors
- `total-users` - Unique user profiles tracked
- `avg-gameplay-time` - Average gameplay time (optional genre filter)
- `avg-session-time` - Average session length (optional genre filter)
- `popular-game-genres` - Most-played genres overall
- `popular-content-genres` - Most-played content types
- `popular-content` - Most popular in-game content
- `popular-gameplay-types` - Most popular gameplay styles

**Features:**

- Real-time industry metrics
- Growth percentage calculations
- Clean Discord embed formatting
- Mock data with TODO for API integration

**TODO:** Integration with MIKROS Analytics API (see `/docs/API_*.md`)

### 🤖 Bot Detection System (TASKS_31)

**Automated Bot & Spam Detection:**

- `/admin-bot-detection-setup` - Enable/disable bot detection system
- `/admin-bot-detection-config` - Configure bot detection settings (8 subcommands)

**Detection Methods:**

- **Account Age + Link:** Detects new accounts (< 30 days) posting links
- **Multi-Channel Spam:** Detects same message posted in multiple channels
- **Join + Link:** Detects users posting links immediately after joining
- **Suspicious Domains:** Detects known spam TLDs and URL shorteners
- **Dynamic Domain Tracking:** Learns and blocks suspicious domains over time

**Features:**

- Automatic reputation reporting (uses `BehaviorCategory.SPAMMER`)
- Configurable auto-actions (DELETE, WARN, MUTE, KICK)
- Per-guild configuration
- Bot prevention count tracking (visible in `/server-stats`)
- Admin exemption (administrators bypass detection)
- Cooldown system prevents spam reporting

**Integration:**

- Automatically reports detected bots to reputation system
- Builds negative reputation scores visible in `/lookup` and `/history`
- No positive reports for bots (only negative)

### 🎲 Community Games Engine (TASKS_05)

**Daily Reset Games:**

- **Word Unscramble** - Unscramble gaming-themed words

**Admin Commands:**

- `/admin-scramble-setup` - Initial game configuration
- `/admin-scramble-config` - Modify game settings (5 subcommands)

**Player Commands:**

- `/scramble-guess <word>` - Submit word unscramble guess
- `/scramble-stats` - View leaderboard and time remaining
- `/scramble-profile` - View your individual statistics (words solved, points, fastest time, accuracy, etc.)

**Features:**

- Daily automatic resets
- Per-server configuration
- Rich leaderboards with medals
- Time-based scoring

### ⚔️ Text-Based RPG System (TASKS_06, TASKS_23)

**Character System:**

- **Six classes:** Warrior, Knight, Mage, Rogue, Necromancer, Priest
- Persistent character progression
- Level and XP system
- **Balanced Stat System:** Every stat (STR, AGI, INT, LUCK, HP) has meaningful advantages AND disadvantages
  - Stat effectiveness against different enemy types (1.3x effective, 0.85x weak)
  - AGI: Defense + exploration bonuses + critical hits
  - INT: XP efficiency + crafting bonuses
  - LUCK: Item drops + rare items + XP floor
  - STR: Consistent physical damage
  - HP: Pure survivability
- **Action Charge System:** Dynamic charges (3-10 based on level), refresh every 12 hours
- **Death & Recovery:** Characters can die, Priests can resurrect

**Player Commands:**

- `/rpg-register` - Create your character (6 classes available)
- `/rpg-profile` - View character stats, charges, recovery status
- `/rpg-action` - Perform actions (explore, train, battle, rest)
- `/rpg-resurrect` - Resurrect dead players (Priest-only, free action)
- `/rpg-duel` - Challenge another player to a duel (free action, 3x/24h limit)
- `/rpg-boss-battle` - Attack community bosses (attack, status, leaderboard)
- `/rpg-leaderboard` - View top players
- `/rpg-inventory` - View your collected items and crafted bonuses
- `/rpg-craft` - Craft permanent stat-boosting items from materials

**Admin Commands:**

- `/admin-rpg-setup` - Initial RPG system setup
- `/admin-rpg-config` - Configure RPG system (5 subcommands)

**Features:**

- **Action Charge System:** Dynamic charges (3-10 based on level), refresh every 12 hours
- **Four Action Types:** Explore, Train, Battle, Rest, Donate
- **65+ narrative encounters** (Nilfheim-themed)
- **66 enemy types** for battles with stat effectiveness system
- **Death System:** Characters can die, enter recovery
- **Boss System:** 48 normal bosses + 20 super bosses
- **Community Boss Battles:** Shared HP pool, damage tracking, heroic charges (5 per boss)
- **Boss Progression:** Levels increase based on defeats
- **Boss XP Rewards:** Top 30% of participants receive proportional XP rewards (scales with participation)
- **Class Bonuses:** Each class gets +20% damage vs specific boss types
- **World Curse System:** Temporary world-wide curses when bosses expire undefeated (7 minor + 6 major curses)
- **Achievement System:** First-to achievements, pattern achievements, legendary auras, titles, story flags
- **Item & Crafting System:** Collect essences and catalysts, craft permanent stat bonuses (+5 per stat cap)
- **Player Duels:** Challenge other players (free action, 3x/24h limit)
- Exponential XP progression
- **Nilfheim Lore:** Full realm integration

**TODO:** Quests, multiplayer, prestige system

### 🧠 Word Unscramble Game (TASKS_05)

**Features:**

- Daily word unscrambling challenge
- Gaming/fantasy-themed words
- Hourly game resets
- Point-based scoring

**Commands:**

- `/scramble-guess <word>` - Submit word unscramble guess
- `/scramble-stats` - View game leaderboard and status
- `/admin-scramble-setup` - Setup word unscramble game (Admin only)

**Features:**

- Hourly game resets
- Leaderboard tracking
- Individual player statistics tracking (words solved, points, fastest time, accuracy)
- Beautiful embed formatting
- Branding words included: "MIKROS", "TATUM GAMES", "TATUM TECH"

**TODO:** Hint system, RPG integration, difficulty levels

### 🚀 Smart Promotional Lead Generator (TASKS_08)

**Passive Detection:**

- Monitors messages for launch-related phrases
- 10 regex patterns for detection
- Gentle, opt-in prompts
- Configurable cooldowns (1-30 days)

**Commands:**

- `/setup-promotions` - Enable/disable detection (admin)
- `/set-promo-frequency` - Set cooldown days (admin)

**Features:**

- DM or channel delivery options
- Per-server configuration
- Respectful, non-intrusive

**TODO:** Google Generative AI NLP integration

---

## 🏗️ Architecture

### High-Level System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Discord Server                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   Commands   │  │   Games      │  │   Listeners  │   │
│  └──────┬───────┘  └───────┬──────┘  └────────┬─────┘   │
└─────────┼──────────────────┼──────────────────┼─────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│              MIKROS Discord Bot (JDA)                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   Services   │  │   Schedulers │  │   Models     │   │
│  └──────┬───────┘  └───────┬──────┘  └────────┬─────┘   │
└─────────┼──────────────────┼──────────────────┼─────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│         In-Memory Storage (ConcurrentHashMap)           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Characters  │  │   Sessions   │  │   Configs    │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────┐
│         TODO: MIKROS Backend APIs                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Analytics   │  │  Promotions  │  │   Reputation │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### Component Layers

1. **Command Layer** (`commands/`)
    - Handles user interactions
    - Validates permissions
    - Formats responses
    - Delegates to services

2. **Service Layer** (`services/`)
    - Business logic
    - State management
    - Schedulers for automation
    - API integration points (TODO)

3. **Model Layer** (`models/`)
    - Data structures
    - Enums and constants
    - Configuration classes

4. **Feature Modules**
    - `communitygames/` - Daily games system
    - `rpg/` - Text-based RPG
    - `spelling/` - Spelling challenges
    - `promo/` - Promotional detection

### Data Persistence

**Current:** In-memory storage using `ConcurrentHashMap`

- Thread-safe implementations
- Per-server isolation
- Fast access

**Future (TODO):**

- Database persistence
- Google Cloud Storage
- Redis for caching
- Backup and restore

### Scheduled Tasks

- **Monthly Reports** - Automated moderation reports
- **Game Promotions** - Hourly promotion checks
- **Community Games** - Daily game resets
- **RPG Actions** - Cooldown tracking

---

## 🚀 Installation & Setup

### Prerequisites

- **Java 17 or higher** ([Download](https://adoptium.net/))
- **Gradle 7.x or higher** (or use included Gradle Wrapper)
- **Discord Bot Token** ([Get one here](https://discord.com/developers/applications))
- **Git** (for cloning repository)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/TATUMGAMES/TG-MIKROS-BOT-discord.git
   cd TG-MIKROS-BOT-discord
   ```

2. **Create `.env` file**
   ```bash
   # Create .env file in project root
   DISCORD_BOT_TOKEN=your_bot_token_here
   BOT_OWNER_ID=your_discord_user_id_here
   ```

3. **Build the project**
   ```bash
   # Windows
   gradlew.bat build

   # Linux/Mac
   ./gradlew build
   ```

4. **Run the bot**
   ```bash
   # Using Gradle
   ./gradlew run

   # Or using JAR
   java -jar build/libs/TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar
   ```

### Production Deployment

For production deployment on Google Cloud Platform, see:

- **[`/docs/DEPLOYMENT_GOOGLE_CLOUD.md`](docs/DEPLOYMENT_GOOGLE_CLOUD.md)** - Complete GCP deployment guide

Includes:

- VM setup instructions
- systemd service configuration
- Docker deployment
- Security best practices

---

## 🎮 Commands & Usage

### Command Reference

| Command                          | Category   | Description                                                                              | Permission       |
|----------------------------------|------------|------------------------------------------------------------------------------------------|------------------|
| `/warn`                          | Moderation | Warn a user with reason                                                                  | Moderate Members |
| `/kick`                          | Moderation | Kick a user from server                                                                  | Kick Members     |
| `/ban`                           | Moderation | Ban a user (optional message deletion)                                                   | Ban Members      |
| `/history`                       | Moderation | View user moderation history                                                             | Moderate Members |
| `/warn-suggestions`              | Moderation | Get AI-powered warning suggestions                                                       | Moderate Members |
| `/ban-suggestions`               | Moderation | Get AI-powered ban suggestions                                                           | Moderate Members |
| `/server-stats`                  | Community  | View server activity statistics (includes bot prevention count)                          | Moderate Members |
| `/top-contributors`              | Community  | View most active members                                                                 | Everyone         |
| `/praise`                        | Reputation | Award positive reputation                                                                | Admin Only       |
| `/report`                        | Reputation | Report negative behavior                                                                 | Admin Only       |
| `/lookup`                        | Reputation | Lookup user scores by username                                                           | Admin Only       |
| `/admin-mikros-ecosystem-setup`  | Analytics  | Setup MIKROS Ecosystem channel                                                           | Administrator    |
| `/mikros-ecosystem`              | Analytics  | View MIKROS Analytics (13 subcommands, requires channel setup)                           | Everyone         |
| `/admin-scramble-setup`          | Games      | Setup word unscramble game                                                               | Administrator    |
| `/admin-scramble-config`         | Games      | Configure games (5 subcommands)                                                          | Administrator    |
| `/scramble-guess`                | Games      | Submit word unscramble guess                                                             | Everyone         |
| `/scramble-stats`                | Games      | View game leaderboard                                                                    | Everyone         |
| `/scramble-profile`              | Games      | View your individual statistics (words solved, points, fastest time, etc.)               | Everyone         |
| `/rpg-register`                  | RPG        | Create RPG character (6 classes)                                                         | Everyone         |
| `/rpg-profile`                   | RPG        | View character profile                                                                   | Everyone         |
| `/rpg-action`                    | RPG        | Perform action (explore/train/battle/rest)                                               | Everyone         |
| `/rpg-resurrect`                 | RPG        | Resurrect dead player (Priest-only)                                                      | Everyone         |
| `/rpg-duel`                      | RPG        | Challenge another player to a duel (free, 3x/24h limit)                                  | Everyone         |
| `/rpg-boss-battle`               | RPG        | Attack boss, check status, leaderboard                                                   | Everyone         |
| `/rpg-leaderboard`               | RPG        | View RPG leaderboard                                                                     | Everyone         |
| `/rpg-inventory`                 | RPG        | View collected items and crafted bonuses                                                 | Everyone         |
| `/rpg-craft`                     | RPG        | Craft permanent stat-boosting items                                                      | Everyone         |
| `/rpg-stats`                     | RPG        | View detailed RPG statistics                                                             | Everyone         |
| `/admin-rpg-setup`               | RPG        | Setup RPG system                                                                         | Administrator    |
| `/admin-rpg-config`              | RPG        | Configure RPG (5 subcommands)                                                            | Administrator    |
| `/rpg-reset`                     | RPG        | Reset all RPG data for server                                                            | Administrator    |
| `/setup-promotions`              | Promo      | Enable/disable promo detection                                                           | Administrator    |
| `/set-promo-frequency`           | Promo      | Set promo cooldown                                                                       | Administrator    |
| `/admin-promotion-setup`         | Admin      | Configure game promotion channel                                                         | Administrator    |
| `/admin-promotion-config`        | Admin      | Configure promotion settings (view, update-channel, set-verbosity, disable, force-check) | Administrator    |
| `/admin-bump-setup`              | Admin      | Set up automatic server bumping (channel, bots selection)                               | Administrator    |
| `/admin-bump-config`             | Admin      | Configure auto-bump settings (view, set-interval, update-bots, disable)               | Administrator    |
| `/admin-bot-detection-setup`     | Moderation | Enable/disable bot detection system                                                      | Administrator    |
| `/admin-bot-detection-config`    | Moderation | Configure bot detection settings (8 subcommands)                                         | Administrator    |
| `/promo-request`                  | Promo      | Request MIKROS promotional services and schedule a demo                                  | Everyone         |
| `/support`                      | Support    | Learn how to support the MIKROS Bot development                                          | Everyone         |
| `/info`                         | Support    | Learn about the MIKROS Bot and MIKROS Ecosystem                                        | Everyone         |

**Total Commands:** 41+ (including subcommands)

### Example Usage

#### Moderation

```
/warn @user Spamming in general channel
/kick @user Inappropriate behavior
/ban @user Repeated violations delete_days:7
/history @user
```

#### Community Games

```
/admin-scramble-setup channel:#games reset_hour:0
/scramble-guess gameplay
/scramble-stats
/scramble-profile
```

#### RPG System

```
/rpg-register name:Aragorn class:WARRIOR
/rpg-profile
/rpg-action type:explore
/rpg-action type:battle
/rpg-action type:rest
/rpg-duel target:@Player
/rpg-inventory
/rpg-craft item:Ember Infusion
/rpg-boss-battle attack
/rpg-resurrect target:@Player
/rpg-leaderboard
```

#### Analytics

```
/mikros-ecosystem trending-game-genres
/mikros-ecosystem popular-content
/mikros-ecosystem trending-gameplay-types
```

#### Bot Detection

```
/admin-bot-detection-setup enabled:true
/admin-bot-detection-config view
/admin-bot-detection-config set-account-age-threshold days:30
/admin-bot-detection-config set-auto-action action:DELETE
/server-stats
```

#### Auto-Bump

```
/admin-bump-setup channel:#server-promo bots:both
/admin-bump-config view
/admin-bump-config set-interval interval:6
/admin-bump-config update-bots bots:disboard
/admin-bump-config disable
```

---

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
# Required
DISCORD_BOT_TOKEN=your_discord_bot_token_here

# Optional
BOT_OWNER_ID=your_discord_user_id_here

# Optional: MIKROS Mafia Server ID
# Used for RPG leaderboard "Mafia Member?" status check
# Get this by right-clicking your server name → Copy Server ID
# Or extract from Discord channel URL: https://discord.com/channels/<server_id>/<channel_id>
MIKROS_MAFIA_GUILD_ID=1213441992936390666
```

**Security:** Set file permissions to `600` (owner read/write only):

```bash
chmod 600 .env
```

### Per-Server Configuration

The bot supports per-server configuration for:

- **Community Games:** Channel, reset time, enabled games
- **RPG System:** Enable/disable, cooldown, XP multiplier, channel
- **Game Promotions:** Channel, verbosity level
- **Promotional Detection:** Enable/disable, cooldown, delivery method

All configurations are managed via admin commands and stored in-memory.

### Feature Toggles

- **RPG System:** Enable/disable per server
- **Promotional Detection:** Enable/disable per server
- **Community Games:** Enable/disable individual games
- **Game Promotions:** Verbosity levels (QUIET, NORMAL, VERBOSE)

---

## 👨‍💻 Development Guidelines

### Code Standards

This project follows strict coding standards defined in:

- **[`BEST_CODING_PRACTICES.md`](BEST_CODING_PRACTICES.md)** - Complete coding standards

**Key Principles:**

- Clean architecture with separation of concerns
- Javadoc comments for all public classes and methods
- JUnit 5 for testing with Mockito for mocking
- Proper error handling and logging
- DRY, KISS, YAGNI, SRP principles

### Project Structure

```
src/main/java/com/tatumgames/mikros/
├── bot/                    # Bot entry point
│   └── BotMain.java
├── commands/               # Command handlers
│   ├── CommandHandler.java (interface)
│   └── [33+ command files]
├── config/                 # Configuration
│   ├── ConfigLoader.java
│   └── ModerationConfig.java
├── models/                 # Data models
│   └── [20+ model files]
├── services/               # Business logic
│   └── [15+ service files]
├── communitygames/         # Community games feature
│   ├── commands/
│   ├── games/
│   ├── model/
│   └── service/
├── rpg/                    # RPG system
│   ├── actions/
│   ├── commands/
│   ├── config/
│   ├── model/
│   └── service/
├── spelling/               # Spelling challenge
│   ├── commands/
│   ├── model/
│   └── service/
└── promo/                  # Promotional detection
    ├── commands/
    ├── config/
    ├── listener/
    ├── model/
    └── service/
```

### Adding New Features

1. **Create feature package** (if new feature module)
2. **Implement service layer** for business logic
3. **Create command handlers** implementing `CommandHandler`
4. **Register in `BotMain.registerCommandHandlers()`**
5. **Add Javadoc** documentation
6. **Follow `BEST_CODING_PRACTICES.md`**

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "InMemoryModerationLogServiceTest"
```

### Building

```bash
# Clean build
./gradlew clean build

# Build without tests
./gradlew build -x test

# Create JAR
./gradlew jar
```

### Contributing

1. Follow `BEST_CODING_PRACTICES.md`
2. Write Javadoc for all public APIs
3. Add unit tests for new services
4. Update documentation as needed
5. Test commands in development server

---

## 📚 Documentation

### Project Documentation

- **[`BEST_CODING_PRACTICES.md`](BEST_CODING_PRACTICES.md)** - Coding standards and guidelines
- **[`QUICK_START.md`](QUICK_START.md)** - Quick setup guide
- **[`README.md`](README.md)** - This file

### API Documentation (`/docs/`)

**Deployment:**

- **[`DEPLOYMENT_GOOGLE_CLOUD.md`](docs/DEPLOYMENT_GOOGLE_CLOUD.md)** - GCP deployment guide

**API Specifications:**

- **[`API_MIKROS_PROMO_SUBMISSION.md`](docs/API_MIKROS_PROMO_SUBMISSION.md)** - Promo lead submission API
- **[`API_GOOGLE_GENERATIVE_AI.md`](docs/API_GOOGLE_GENERATIVE_AI.md)** - NLP integration API
- **[`admin-tools-api.md`](docs/admin-tools-api.md)** - Admin tools API

**Analytics APIs (TODO for integration):**

- `API_TRENDING_GAME_GENRES.md`
- `API_TRENDING_CONTENT_GENRES.md`
- `API_TRENDING_CONTENT.md`
- `API_TRENDING_GAMEPLAY_TYPES.md`
- `API_POPULAR_GAME_GENRES.md`
- `API_POPULAR_CONTENT_GENRES.md`
- `API_POPULAR_CONTENT.md`
- `API_POPULAR_GAMEPLAY_TYPES.md`
- `API_TOTAL_USERS.md`
- `API_TOTAL_MIKROS_APPS.md`
- `API_TOTAL_MIKROS_CONTRIBUTORS.md`
- `API_AVG_SESSION_TIME.md`
- `API_AVG_GAMEPLAY_TIME.md`
- `API_REPUTATION_SCORE.md`
- `API_REPUTATION_SCORE_UPDATE.md`
- `API_GAME_PROMOTION_SCHEDULE.md`
- `API_MIKROS_MARKETING_DISCOUNT_OFFER.md`
- `API_GLOBAL_USER_MODERATION_LOG.md`

**Testing Guides:**

- **[`COMMUNITY_GAMES_TESTING_GUIDE.md`](docs/COMMUNITY_GAMES_TESTING_GUIDE.md)** - Community games testing

**System Configuration:**

- **[`docs/systemd/mikros-bot.service`](docs/systemd/mikros-bot.service)** - systemd service file

---

## 🔧 Troubleshooting & FAQ

### Common Issues

#### Bot Not Responding to Commands

**Symptoms:** Commands don't appear or return "Unknown command"

**Solutions:**

1. Wait 1-5 minutes after bot startup (Discord command registration delay)
2. Check bot is online in Discord
3. Verify bot has proper permissions
4. Check logs for registration errors:
   ```bash
   tail -f logs/bot.log
   ```

#### Bot Disconnects Frequently

**Symptoms:** Bot goes offline randomly

**Solutions:**

1. Check VM resources (memory, CPU):
   ```bash
   free -h
   top
   ```
2. Upgrade VM instance if needed
3. Check network connectivity
4. Review logs for errors:
   ```bash
   sudo journalctl -u mikros-bot -n 100
   ```

#### Permission Errors

**Symptoms:** "You don't have permission" errors

**Solutions:**

1. Verify user has required Discord permissions
2. Check role hierarchy (bot role must be above target users)
3. Ensure bot has necessary permissions in server settings

#### Build Failures

**Symptoms:** `./gradlew build` fails

**Solutions:**

1. Verify Java 17+ is installed:
   ```bash
   java -version
   ```
2. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```
3. Check for dependency issues:
   ```bash
   ./gradlew dependencies
   ```

#### .env File Not Found

**Symptoms:** "Required configuration missing: DISCORD_BOT_TOKEN"

**Solutions:**

1. Create `.env` file in project root
2. Add `DISCORD_BOT_TOKEN=your_token`
3. Set proper permissions:
   ```bash
   chmod 600 .env
   ```

### Debugging Tips

1. **Enable Debug Logging:**
    - Edit `src/main/resources/logback.xml`
    - Set root level to `DEBUG`

2. **Check Bot Status:**
   ```bash
   # systemd
   sudo systemctl status mikros-bot
   
   # Docker
   docker ps
   docker logs mikros-bot
   ```

3. **View Real-Time Logs:**
   ```bash
   # systemd
   sudo journalctl -u mikros-bot -f
   
   # Direct execution
   tail -f logs/bot.log
   ```

4. **Test Commands:**
    - Use `/help` or type `/` in Discord to see available commands
    - Test in a private channel first
    - Check bot permissions in server settings

### FAQ

**Q: Can I run multiple instances of the bot?**  
A: Yes, but each should use a different bot token. Per-server configurations are independent.

**Q: How do I update the bot?**  
A: Pull latest changes, rebuild, and restart the service. See deployment guide for details.

**Q: Is data persisted?**  
A: Currently in-memory. Data is lost on restart. Database persistence is planned (TODO).

**Q: Can I customize game word lists?**  
A: Not yet. Custom word lists are planned (TODO). Currently uses built-in word pools.

**Q: How do I disable a feature?**  
A: Use admin configuration commands (`/admin-rpg-config`, `/setup-promotions`, etc.) or remove command registration in code.

**Q: Can I add custom commands?**  
A: Yes! Follow the `CommandHandler` interface pattern. See `BEST_CODING_PRACTICES.md`.

**Q: How do I integrate with MIKROS APIs?**  
A: See API documentation in `/docs/`. Implement HTTP clients in service classes. Mark with TODO until APIs are ready.

---

## 📄 License & Credits

### License

Copyright © 2025 Tatum Games. All rights reserved.

This software is proprietary and confidential. Unauthorized copying, distribution, or use is strictly prohibited.

### Credits

**Developed by:** Tatum Games Development Team

**Technologies:**

- [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA) - Discord bot framework
- [SLF4J & Logback](https://www.slf4j.org/) - Logging framework
- [Gradle](https://gradle.org/) - Build automation
- [Dotenv Java](https://github.com/cdimascio/dotenv-java) - Environment variable management

### Acknowledgments

- Discord API team for excellent documentation
- JDA contributors for the robust library
- Open source community for tools and inspiration

---

## 🆘 Support

For issues, questions, or contributions:

1. **Check Documentation:**
    - Review this README
    - Review API documentation in `/docs/`

2. **Check Logs:**
    - Application logs: `logs/bot.log`
    - systemd logs: `sudo journalctl -u mikros-bot`
    - Docker logs: `docker logs mikros-bot`

3. **Common Resources:**
    - [Discord Developer Portal](https://discord.com/developers/applications)
    - [JDA Documentation](https://docs.jda.wiki/)
    - [BEST_CODING_PRACTICES.md](BEST_CODING_PRACTICES.md)

4. **Contact:**
    - Development team
    - Repository issues (if applicable)

---

## 🗺️ Roadmap

### Completed Features ✅

- ✅ Admin moderation tools
- ✅ Enhanced moderation with AI suggestions
- ✅ Reputation system
- ✅ Game promotion system
- ✅ Game analytics commands
- ✅ Community games engine
- ✅ Text-based RPG system
- ✅ Word unscramble game
- ✅ Smart promotional lead generator
- ✅ GCP deployment documentation

### Planned Features 🔮

- 🔮 **Database Persistence** - Migrate from in-memory storage to PostgreSQL/MongoDB for production scalability
    - Character data persistence
    - Server configuration persistence
    - Boss progression persistence
    - Leaderboard caching and optimization
    - Support for 1M+ users across 10K+ servers
- 🔮 MIKROS Analytics API integration
- 🔮 MIKROS Marketing API integration
- 🔮 Google Generative AI NLP integration
- 🔮 Inventory system for RPG
- 🔮 Quest system for RPG
- 🔮 Multiplayer features
- 🔮 Custom word lists per server
- 🔮 Hint system for spelling challenge
- 🔮 Web dashboard

---

**Last Updated:** 2025-10-08  
**Version:** 1.0-SNAPSHOT  
**Status:** Production Ready ✅
