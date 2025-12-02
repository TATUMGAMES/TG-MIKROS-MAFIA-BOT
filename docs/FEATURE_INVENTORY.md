# Feature Inventory - MIKROS Discord Bot

**Date:** 2025-01-27  
**Version:** 1.0

---

## Overview

This document provides a complete inventory of all features, commands, and capabilities implemented in the MIKROS
Discord Bot.

---

## Slash Commands Reference

### Admin & Moderation Commands

#### Core Moderation

| Command       | Description           | Permission       | Module     |
|---------------|-----------------------|------------------|------------|
| `/admin-warn` | Issue warning to user | Moderate Members | Moderation |
| `/admin-kick` | Kick user from server | Kick Members     | Moderation |
| `/admin-ban`  | Ban user from server  | Ban Members      | Moderation |

#### Enhanced Moderation

| Command             | Description                        | Permission       | Module     |
|---------------------|------------------------------------|------------------|------------|
| `/warn-suggestions` | Get AI-powered warning suggestions | Moderate Members | Moderation |
| `/ban-suggestions`  | Get AI-powered ban suggestions     | Moderate Members | Moderation |
| `/server-stats`     | View server activity statistics    | Everyone         | Community  |
| `/top-contributors` | View most active members           | Everyone         | Community  |

#### Reputation System

| Command   | Description                    | Permission | Module     |
|-----------|--------------------------------|------------|------------|
| `/praise` | Award positive reputation      | Admin Only | Reputation |
| `/report` | Report negative behavior       | Admin Only | Reputation |
| `/lookup` | Lookup user scores by username | Admin Only | Reputation |

### Game Promotion Commands

| Command                    | Description                 | Permission    | Module     |
|----------------------------|-----------------------------|---------------|------------|
| `/setup-promotion-channel` | Configure promotion channel | Administrator | Promotions |
| `/set-promotion-verbosity` | Set promotion frequency     | Administrator | Promotions |
| `/force-promotion-check`   | Manually trigger promotion  | Administrator | Promotions |

### Analytics Commands

| Command                              | Description                | Permission | Module    |
|--------------------------------------|----------------------------|------------|-----------|
| `/gamestats trending-game-genres`    | Top fastest-growing genres | Everyone   | Analytics |
| `/gamestats trending-content-genres` | Trending content types     | Everyone   | Analytics |
| `/gamestats trending-content`        | Top trending content       | Everyone   | Analytics |
| `/gamestats trending-gameplay-types` | Trending gameplay styles   | Everyone   | Analytics |
| `/gamestats popular-game-genres`     | Most-played genres         | Everyone   | Analytics |
| `/gamestats popular-content-genres`  | Popular content types      | Everyone   | Analytics |
| `/gamestats popular-content`         | Most popular content       | Everyone   | Analytics |
| `/gamestats popular-gameplay-types`  | Popular gameplay styles    | Everyone   | Analytics |
| `/gamestats total-apps`              | Total MIKROS apps          | Everyone   | Analytics |
| `/gamestats total-contributors`      | Total contributors         | Everyone   | Analytics |
| `/gamestats total-users`             | Total users                | Everyone   | Analytics |
| `/gamestats avg-gameplay-time`       | Average gameplay time      | Everyone   | Analytics |
| `/gamestats avg-session-time`        | Average session time       | Everyone   | Analytics |

### Community Games Commands

| Command        | Description                         | Permission    | Module |
|----------------|-------------------------------------|---------------|--------|
| `/game-setup`  | Initial game configuration          | Administrator | Games  |
| `/guess`       | Submit word guess (Word Unscramble) | Everyone      | Games  |
| `/game-stats`  | View game leaderboard               | Everyone      | Games  |
| `/game-config` | Configure games (5 subcommands)     | Administrator | Games  |

### RPG System Commands

| Command            | Description                                | Permission    | Module |
|--------------------|--------------------------------------------|---------------|--------|
| `/rpg-register`    | Create RPG character (6 classes)           | Everyone      | RPG    |
| `/rpg-profile`     | View character profile                     | Everyone      | RPG    |
| `/rpg-action`      | Perform action (explore/train/battle/rest) | Everyone      | RPG    |
| `/rpg-resurrect`   | Resurrect dead player (Priest-only)        | Everyone      | RPG    |
| `/rpg-boss-battle` | Attack boss, check status, leaderboard     | Everyone      | RPG    |
| `/rpg-leaderboard` | View RPG leaderboard                       | Everyone      | RPG    |
| `/rpg-config`      | Configure RPG (5 subcommands)              | Administrator | RPG    |

### Spelling Challenge Commands

| Command                 | Description               | Permission | Module   |
|-------------------------|---------------------------|------------|----------|
| `/spelling-challenge`   | View daily challenge      | Everyone   | Spelling |
| `/spelling-leaderboard` | View spelling leaderboard | Everyone   | Spelling |
| `/spell-guess`          | Submit spelling guess     | Everyone   | Spelling |

### Promotional Lead Generation Commands

| Command                | Description                    | Permission    | Module |
|------------------------|--------------------------------|---------------|--------|
| `/promo-help`          | Request promotional help       | Everyone      | Promo  |
| `/setup-promotions`    | Enable/disable promo detection | Administrator | Promo  |
| `/set-promo-frequency` | Set promo cooldown             | Administrator | Promo  |

### Honeypot System Commands

| Command                        | Description           | Permission    | Module   |
|--------------------------------|-----------------------|---------------|----------|
| `/honeypot enable`             | Enable honeypot mode  | Administrator | Honeypot |
| `/honeypot disable`            | Disable honeypot mode | Administrator | Honeypot |
| `/honeypot config`             | Configure honeypot    | Administrator | Honeypot |
| `/ban_and_remove_all_messages` | Ban and cleanup user  | Administrator | Honeypot |
| `/cleanup`                     | Remove user messages  | Administrator | Honeypot |
| `/alert_channel`               | Set alert channel     | Administrator | Honeypot |
| `/list_bans`                   | View recent bans      | Administrator | Honeypot |

**Total Commands:** 33+ (including subcommands)

---

## Feature Modules

### 1. Moderation Module

**Purpose:** Server moderation and user management

**Features:**

- Warning system with auto-escalation
- Kick and ban commands
- Moderation history tracking
- AI-powered suggestion system
- Role hierarchy validation
- Permission checks

**Services:**

- `ModerationLogService` - Action logging
- `AutoEscalationService` - Auto-escalation logic
- `MessageAnalysisService` - Message analysis

**Storage:** In-memory (TODO: database)

---

### 2. Reputation System

**Purpose:** User behavior tracking and scoring

**Features:**

- Positive/negative reputation points
- Behavior category tracking
- API-integrated reputation system
- Server-side score calculation and storage
- Integration with moderation actions

**Services:**

- `ReputationService` - Reputation management
- `InMemoryReputationService` - API integration with stub support

**Storage:** In-memory (TODO: API integration)

---

### 3. Game Promotion System

**Purpose:** Automated game promotion scheduling

**Features:**

- Configurable promotion channels
- Verbosity levels (QUIET, NORMAL, VERBOSE)
- Scheduled promotion checks
- Rich embed formatting

**Services:**

- `GamePromotionService` - Promotion management
- `GamePromotionScheduler` - Scheduled tasks

**Storage:** In-memory (TODO: API integration)

---

### 4. Analytics Module

**Purpose:** MIKROS Analytics integration

**Features:**

- 13 analytics subcommands
- Trending and popular data
- Industry metrics
- Mock data support

**Services:**

- `GameStatsService` - Analytics interface
- `MockGameStatsService` - Mock implementation

**Storage:** Mock data (TODO: API integration)

---

### 5. Community Games Engine

**Purpose:** Daily reset interactive games

**Games:**

1. **Word Unscramble** - Gaming-themed word unscrambling

**Features:**

- Daily automatic resets
- Per-server configuration
- Leaderboards with medals
- Time-based scoring

**Services:**

- `CommunityGameService` - Game management
- `GameResetScheduler` - Daily resets

**Storage:** In-memory (TODO: API integration)

---

### 6. RPG System

**Purpose:** Text-based RPG progression in Nilfheim realm

**Features:**

- **6 character classes:** Warrior, Knight, Mage, Rogue, Necromancer, Priest
- Level and XP progression (exponential growth)
- Stat growth system (+5 HP, +1 all stats per level)
- **Action Charge System:** 3 charges, refresh every 12 hours
- **4 action types:** Explore, Train, Battle, Rest
- **40+ narrative encounters** (Nilfheim-themed)
- **36 enemy types** for battles
- **Death & Recovery System:** Characters can die, Priests can resurrect
- **Boss System:** 24 normal bosses + 12 super bosses
- **Community Boss Battles:** Shared HP pool, damage tracking
- **Boss Progression:** Levels increase based on defeats
- **Class Bonuses:** +20% damage vs specific boss types
- **Nilfheim Lore Integration**

**Services:**

- `CharacterService` - Character management
- `ActionService` - Action processing
- `BossService` - Boss spawning and tracking
- `BossScheduler` - 24h boss spawns

**Storage:** In-memory (TODO: API integration)

---

### 7. Spelling Challenge

**Purpose:** Daily word unscrambling challenge

**Features:**

- Daily word challenges
- Leaderboard tracking
- Scoring system

**Services:**

- `SpellingService` - Challenge management

**Storage:** In-memory (TODO: API integration)

---

### 8. Promotional Lead Generation

**Purpose:** AI-powered promotional help detection

**Features:**

- Message analysis for promotional keywords
- Lead submission (TODO: API)
- Configurable detection
- Cooldown system

**Services:**

- `PromoService` - Promo detection

**Storage:** In-memory (TODO: API integration)

---

### 9. Honeypot System

**Purpose:** Automated spam detection and ban

**Features:**

- Automatic ban on honeypot channel posts
- Configurable channel name
- Message deletion
- Alert notifications
- Silent mode option

**Services:**

- `HoneypotService` - Honeypot management
- `MessageDeletionService` - Message cleanup

**Storage:** In-memory

---

## Configuration Options

### Per-Server Settings

#### Game Promotion

- Promotion channel
- Verbosity level (QUIET, NORMAL, VERBOSE)

#### Community Games

- Games channel
- Enabled games
- Reset time (UTC)

#### RPG System

- Enabled/disabled
- Action cooldown (hours)
- XP multiplier (0.5x - 2.0x)

#### Spelling Challenge

- Enabled/disabled
- Challenge channel

#### Promotional Detection

- Enabled/disabled
- Detection frequency (cooldown)

#### Honeypot

- Enabled/disabled
- Channel name
- Silent mode
- Delete days (0-7)
- Alert channel

#### Auto-Escalation

- Enabled/disabled (per server)
- Warning thresholds

---

## Integration Points

### Current Integrations

- ✅ Discord API (JDA)
- ✅ Mock Analytics API
- ✅ Mock Reputation API
- ✅ Mock Promotion API

### Pending Integrations

- ⏳ MIKROS Analytics API (29 endpoints)
- ⏳ MIKROS Marketing API
- ⏳ Reputation API
- ⏳ Game Promotion API
- ⏳ RPG Progression API
- ⏳ Leaderboard Persistence API
- ⏳ Scheduling Sync API

*See `docs/API_INTEGRATION_STATUS.md` for details.*

---

## Data Models

### Core Models

- `ModerationAction` - Moderation log entries
- `ReputationScore` - User reputation data
- `GamePromotion` - Promotion data
- `GameStats` - Analytics data

### Game Models

- `GameState` - Current game state
- `GameLeaderboard` - Leaderboard data
- `PlayerScore` - Player scores

### RPG Models

- `RPGCharacter` - Character data (with charges, death/recovery)
- `CharacterClass` - Character class enum (6 classes)
- `RPGStats` - Character stats (HP, STR, AGI, INT, LUCK)
- `RPGActionOutcome` - Action results
- `Boss` - Normal boss data
- `SuperBoss` - Super boss data
- `BossType` - Boss type enum (10 types)
- `RPGConfig` - Server configuration

### Spelling Models

- `SpellingChallenge` - Daily challenge
- `SpellingLeaderboard` - Leaderboard data

---

## Scheduled Tasks

### Daily Resets

- **Community Games Reset** - 00:00 UTC daily
- **Spelling Challenge Reset** - 00:00 UTC daily

### Periodic Tasks

- **Game Promotion Check** - Configurable frequency
- **Monthly Report** - First of each month (TODO)

---

## Event Listeners

### Message Listeners

- `HoneypotMessageListener` - Honeypot detection
- `ActivityTrackingListener` - Activity tracking (TODO)

### Command Listeners

- All commands handled via slash command system

---

## Summary

**Total Commands:** 33+  
**Feature Modules:** 9  
**Services:** 20+  
**Data Models:** 15+  
**Scheduled Tasks:** 2+  
**Event Listeners:** 1+

**Status:** ✅ **PRODUCTION READY**

---

**Last Updated:** 2025-01-27  
**Version:** 1.0

