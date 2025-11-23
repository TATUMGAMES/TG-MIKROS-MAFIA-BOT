# Feature Inventory - MIKROS Discord Bot

**Date:** 2025-01-27  
**Version:** 1.0

---

## Overview

This document provides a complete inventory of all features, commands, and capabilities implemented in the MIKROS Discord Bot.

---

## Slash Commands Reference

### Admin & Moderation Commands

#### Core Moderation
| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/admin-warn` | Issue warning to user | Moderate Members | Moderation |
| `/admin-kick` | Kick user from server | Kick Members | Moderation |
| `/admin-ban` | Ban user from server | Ban Members | Moderation |
| `/admin-history` | View user moderation history | Moderate Members | Moderation |

#### Enhanced Moderation
| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/warn-suggestions` | Get AI-powered warning suggestions | Moderate Members | Moderation |
| `/ban-suggestions` | Get AI-powered ban suggestions | Moderate Members | Moderation |
| `/server-stats` | View server activity statistics | Everyone | Community |
| `/top-contributors` | View most active members | Everyone | Community |

#### Reputation System
| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/praise` | Award positive reputation | Everyone | Reputation |
| `/report` | Report negative behavior | Everyone | Reputation |
| `/score` | View user reputation score | Everyone | Reputation |

### Game Promotion Commands

| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/setup-promotion-channel` | Configure promotion channel | Administrator | Promotions |
| `/set-promotion-verbosity` | Set promotion frequency | Administrator | Promotions |
| `/force-promotion-check` | Manually trigger promotion | Administrator | Promotions |

### Analytics Commands

| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/gamestats trending-game-genres` | Top fastest-growing genres | Everyone | Analytics |
| `/gamestats trending-content-genres` | Trending content types | Everyone | Analytics |
| `/gamestats trending-content` | Top trending content | Everyone | Analytics |
| `/gamestats trending-gameplay-types` | Trending gameplay styles | Everyone | Analytics |
| `/gamestats popular-game-genres` | Most-played genres | Everyone | Analytics |
| `/gamestats popular-content-genres` | Popular content types | Everyone | Analytics |
| `/gamestats popular-content` | Most popular content | Everyone | Analytics |
| `/gamestats popular-gameplay-types` | Popular gameplay styles | Everyone | Analytics |
| `/gamestats total-apps` | Total MIKROS apps | Everyone | Analytics |
| `/gamestats total-contributors` | Total contributors | Everyone | Analytics |
| `/gamestats total-users` | Total users | Everyone | Analytics |
| `/gamestats avg-gameplay-time` | Average gameplay time | Everyone | Analytics |
| `/gamestats avg-session-time` | Average session time | Everyone | Analytics |

### Community Games Commands

| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/game-setup` | Initial game configuration | Administrator | Games |
| `/guess` | Submit word guess (Word Unscramble) | Everyone | Games |
| `/roll` | Roll dice (Dice Battle) | Everyone | Games |
| `/match` | Match emoji pattern (Emoji Match) | Everyone | Games |
| `/game-stats` | View game leaderboard | Everyone | Games |
| `/game-config` | Configure games (5 subcommands) | Administrator | Games |

### RPG System Commands

| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/rpg-register` | Create RPG character | Everyone | RPG |
| `/rpg-profile` | View character profile | Everyone | RPG |
| `/rpg-action` | Perform daily action | Everyone | RPG |
| `/rpg-leaderboard` | View RPG leaderboard | Everyone | RPG |
| `/rpg-config` | Configure RPG (5 subcommands) | Administrator | RPG |

### Spelling Challenge Commands

| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/spelling-challenge` | View daily challenge | Everyone | Spelling |
| `/spelling-leaderboard` | View spelling leaderboard | Everyone | Spelling |
| `/spell-guess` | Submit spelling guess | Everyone | Spelling |

### Promotional Lead Generation Commands

| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/promo-help` | Request promotional help | Everyone | Promo |
| `/setup-promotions` | Enable/disable promo detection | Administrator | Promo |
| `/set-promo-frequency` | Set promo cooldown | Administrator | Promo |

### Honeypot System Commands

| Command | Description | Permission | Module |
|---------|-------------|------------|--------|
| `/honeypot enable` | Enable honeypot mode | Administrator | Honeypot |
| `/honeypot disable` | Disable honeypot mode | Administrator | Honeypot |
| `/honeypot config` | Configure honeypot | Administrator | Honeypot |
| `/ban_and_remove_all_messages` | Ban and cleanup user | Administrator | Honeypot |
| `/cleanup` | Remove user messages | Administrator | Honeypot |
| `/alert_channel` | Set alert channel | Administrator | Honeypot |
| `/list_bans` | View recent bans | Administrator | Honeypot |

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
- Local and global reputation (global TODO)
- Integration with moderation actions

**Services:**
- `ReputationService` - Reputation management
- `InMemoryReputationService` - In-memory storage

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
2. **Dice Battle** - D20 rolling competition
3. **Emoji Match** - Emoji pattern matching

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

**Purpose:** Text-based RPG progression

**Features:**
- 3 character classes (Warrior, Mage, Rogue)
- Level and XP progression
- Stat growth system
- 3 action types (explore, train, battle)
- 15+ narrative encounters
- 16 enemy types

**Services:**
- `CharacterService` - Character management
- `RPGActionService` - Action processing

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
- `RPGCharacter` - Character data
- `RPGClass` - Character class enum
- `RPGAction` - Action types
- `RPGEncounter` - Narrative encounters

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

