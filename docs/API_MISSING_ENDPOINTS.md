# API Blueprints for Missing Endpoints

This document provides comprehensive API specifications for all missing endpoints required by the MIKROS Discord Bot.
These endpoints need to be implemented by the MIKROS backend to enable full functionality.

---

## Table of Contents

1. [MIKROS Analytics APIs](#mikros-analytics-apis)
2. [MIKROS Marketing/Promotions APIs](#mikros-marketingpromotions-apis)
3. [Game Ecosystems APIs](#game-ecosystems-apis)
4. [RPG Progression APIs](#rpg-progression-apis)
5. [Leaderboard Persistence APIs](#leaderboard-persistence-apis)
6. [Scheduling Sync APIs](#scheduling-sync-apis)

---

## MIKROS Analytics APIs

### 1. Get Trending Game Genres

**Method:** `GET`  
**Route:** `/api/gamestats/trending-genres`

**Description:** Returns the top fastest-growing game genres based on player engagement over the last 30 days.

**Query Parameters:**

| Parameter | Type    | Required | Description                                      |
|-----------|---------|----------|--------------------------------------------------|
| `limit`   | integer | No       | Maximum number of results (default: 3, max: 10)  |
| `period`  | string  | No       | Time period: "7d", "30d", "90d" (default: "30d") |

**Example Request:**

```http
GET /api/gamestats/trending-genres?limit=3&period=30d HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
Content-Type: application/json
```

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "genre": "Roguelike",
      "growth_percentage": 43.2,
      "current_players": 185000,
      "previous_players": 129200,
      "rank": 1
    },
    {
      "genre": "Puzzle",
      "growth_percentage": 31.5,
      "current_players": 142000,
      "previous_players": 108000,
      "rank": 2
    },
    {
      "genre": "Sandbox",
      "growth_percentage": 29.1,
      "current_players": 198000,
      "previous_players": 153400,
      "rank": 3
    }
  ],
  "period": "30d",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 2. Get Trending Content Genres

**Method:** `GET`  
**Route:** `/api/gamestats/trending-content-genres`

**Description:** Returns trending content-type genres (e.g., co-op, story-driven, action RPG).

**Query Parameters:**

| Parameter | Type    | Required | Description                                      |
|-----------|---------|----------|--------------------------------------------------|
| `limit`   | integer | No       | Maximum number of results (default: 3, max: 10)  |
| `period`  | string  | No       | Time period: "7d", "30d", "90d" (default: "30d") |

**Example Request:**

```http
GET /api/gamestats/trending-content-genres?limit=3 HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
```

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "genre": "Co-op Multiplayer",
      "growth_percentage": 38.7,
      "current_engagement": 220000,
      "previous_engagement": 158600,
      "rank": 1
    },
    {
      "genre": "Story-Driven",
      "growth_percentage": 35.2,
      "current_engagement": 175000,
      "previous_engagement": 129400,
      "rank": 2
    },
    {
      "genre": "Action RPG",
      "growth_percentage": 27.8,
      "current_engagement": 210000,
      "previous_engagement": 164600,
      "rank": 3
    }
  ],
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 3. Get Trending Content

**Method:** `GET`  
**Route:** `/api/gamestats/trending-content`

**Description:** Returns specific in-game content (levels, bosses, characters) seeing spikes in playtime.

**Query Parameters:**

| Parameter      | Type    | Required | Description                                                          |
|----------------|---------|----------|----------------------------------------------------------------------|
| `limit`        | integer | No       | Maximum number of results (default: 5, max: 20)                      |
| `content_type` | string  | No       | Filter: "level", "boss", "character", "item", "all" (default: "all") |

**Example Request:**

```http
GET /api/gamestats/trending-content?limit=5&content_type=all HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
```

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "content_id": "level_1234",
      "content_name": "Dragon's Lair",
      "content_type": "level",
      "game_id": "game_567",
      "game_name": "Epic Quest",
      "usage_percentage": 45.3,
      "playtime_hours": 125000,
      "rank": 1
    },
    {
      "content_id": "boss_890",
      "content_name": "Shadow Dragon",
      "content_type": "boss",
      "game_id": "game_567",
      "game_name": "Epic Quest",
      "usage_percentage": 38.7,
      "playtime_hours": 107000,
      "rank": 2
    }
  ],
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 4. Get Trending Gameplay Types

**Method:** `GET`  
**Route:** `/api/gamestats/trending-gameplay-types`

**Description:** Returns trending gameplay types (casual, competitive, hyper-casual).

**Query Parameters:**

| Parameter | Type    | Required | Description                                     |
|-----------|---------|----------|-------------------------------------------------|
| `limit`   | integer | No       | Maximum number of results (default: 3, max: 10) |

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "type": "Competitive",
      "growth_percentage": 52.1,
      "current_players": 450000,
      "rank": 1
    },
    {
      "type": "Casual",
      "growth_percentage": 28.3,
      "current_players": 320000,
      "rank": 2
    }
  ],
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 5. Get Popular Game Genres

**Method:** `GET`  
**Route:** `/api/gamestats/popular-genres`

**Description:** Returns most-played game genres overall (not growth-based).

**Query Parameters:**

| Parameter | Type    | Required | Description                                     |
|-----------|---------|----------|-------------------------------------------------|
| `limit`   | integer | No       | Maximum number of results (default: 3, max: 10) |

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "genre": "Action",
      "total_players": 2100000,
      "market_share": 18.5,
      "rank": 1
    },
    {
      "genre": "RPG",
      "total_players": 1800000,
      "market_share": 15.8,
      "rank": 2
    }
  ],
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 6. Get Popular Content Genres

**Method:** `GET`  
**Route:** `/api/gamestats/popular-content-genres`

**Description:** Returns content genres with most engagement overall.

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "genre": "Multiplayer",
      "total_engagement": 3500000,
      "rank": 1
    }
  ],
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 7. Get Popular Content

**Method:** `GET`  
**Route:** `/api/gamestats/popular-content`

**Description:** Returns top in-game content experiences by usage.

**Query Parameters:**

| Parameter | Type    | Required | Description                                     |
|-----------|---------|----------|-------------------------------------------------|
| `limit`   | integer | No       | Maximum number of results (default: 5, max: 20) |

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "content_id": "content_123",
      "content_name": "Main Campaign",
      "content_type": "campaign",
      "total_playtime": 2500000,
      "unique_players": 500000,
      "rank": 1
    }
  ],
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 8. Get Popular Gameplay Types

**Method:** `GET`  
**Route:** `/api/gamestats/popular-gameplay-types`

**Description:** Returns most popular gameplay types overall.

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "type": "Casual",
      "total_players": 5200000,
      "rank": 1
    }
  ],
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 9. Get Total MIKROS Apps

**Method:** `GET`  
**Route:** `/api/gamestats/total-apps`

**Description:** Returns total number of apps using MIKROS Analytics.

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "total_apps": 1247,
    "active_apps": 1156,
    "inactive_apps": 91,
    "timestamp": "2025-01-27T12:00:00Z"
  }
}
```

---

### 10. Get Total MIKROS Contributors

**Method:** `GET`  
**Route:** `/api/gamestats/total-contributors`

**Description:** Returns total users signed up to MIKROS ecosystem (devs, testers, players).

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "total_contributors": 45823,
    "developers": 1234,
    "testers": 5678,
    "players": 38911,
    "timestamp": "2025-01-27T12:00:00Z"
  }
}
```

---

### 11. Get Total Users

**Method:** `GET`  
**Route:** `/api/gamestats/total-users`

**Description:** Returns unique user profiles tracked across MIKROS-enabled games.

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "total_users": 3240567,
    "active_last_30d": 1850000,
    "active_last_7d": 890000,
    "timestamp": "2025-01-27T12:00:00Z"
  }
}
```

---

### 12. Get Average Gameplay Time

**Method:** `GET`  
**Route:** `/api/gamestats/avg-gameplay-time`

**Description:** Returns average gameplay time per app, optionally filtered by genre.

**Query Parameters:**

| Parameter | Type   | Required | Description                             |
|-----------|--------|----------|-----------------------------------------|
| `genre`   | string | No       | Filter by genre (e.g., "Action", "RPG") |

**Example Request:**

```http
GET /api/gamestats/avg-gameplay-time?genre=Action HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
```

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "average_minutes": 87,
    "median_minutes": 65,
    "genre": "Action",
    "sample_size": 45000,
    "timestamp": "2025-01-27T12:00:00Z"
  }
}
```

---

### 13. Get Average Session Time

**Method:** `GET`  
**Route:** `/api/gamestats/avg-session-time`

**Description:** Returns average session length across all games or by genre.

**Query Parameters:**

| Parameter | Type   | Required | Description     |
|-----------|--------|----------|-----------------|
| `genre`   | string | No       | Filter by genre |

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "average_minutes": 25,
    "median_minutes": 18,
    "genre": "Action",
    "sample_size": 125000,
    "timestamp": "2025-01-27T12:00:00Z"
  }
}
```

---

## MIKROS Marketing/Promotions APIs

### 14. Get Active Promotions

**Method:** `GET`  
**Route:** `/api/promotions/active`

**Description:** Returns list of active game promotions that should be posted to Discord servers.

**Query Parameters:**

| Parameter  | Type    | Required | Description                                             |
|------------|---------|----------|---------------------------------------------------------|
| `limit`    | integer | No       | Maximum number of promotions (default: 50)              |
| `since_id` | integer | No       | Return only promotions with game_id greater than this   |
| `platform` | string  | No       | Filter: "steam", "itch", "epic", "all" (default: "all") |

**Example Request:**

```http
GET /api/promotions/active?limit=10 HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
```

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "game_id": 1021,
      "game_name": "ShadowSprint",
      "description": "A neon-drenched, parkour runner set in a dystopian Tokyo.",
      "promotion_url": "https://store.steampowered.com/app/00000/shadowsprint",
      "promotion_message": null,
      "image_url": "https://cdn.example.com/shadowsprint.png",
      "deadline": "2025-10-08T18:00:00Z",
      "is_pushed": false,
      "platform": "steam",
      "genre": "Action",
      "release_date": "2025-10-08T00:00:00Z"
    }
  ],
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 15. Mark Promotion as Pushed

**Method:** `POST`  
**Route:** `/api/promotions/mark-pushed`

**Description:** Marks a game promotion as pushed to prevent duplicate posts.

**Request Body Schema:**

```json
{
  "game_id": 1021,
  "server_id": "123456789012345678",
  "channel_id": "987654321098765432",
  "pushed_at": "2025-01-27T12:00:00Z",
  "bot_instance_id": "bot_instance_123"
}
```

**Example Request:**

```http
POST /api/promotions/mark-pushed HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
Content-Type: application/json

{
  "game_id": 1021,
  "server_id": "123456789012345678",
  "channel_id": "987654321098765432",
  "pushed_at": "2025-01-27T12:00:00Z"
}
```

**Example Response Schema:**

```json
{
  "success": true,
  "message": "Promotion marked as pushed",
  "game_id": 1021,
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 16. Submit Promotional Lead

**Method:** `POST`  
**Route:** `/api/promotions/submit-lead`

**Description:** Submits a lead when a Discord user requests promotional help.

**Request Body Schema:**

```json
{
  "discord_id": "123456789012345678",
  "username": "devguy#9999",
  "server_id": "987654321098765432",
  "campaign_interest": "Game Launch",
  "email": "dev@example.com",
  "timestamp": "2025-01-27T12:00:00Z",
  "message_context": "We're launching our game next month!"
}
```

**Example Response Schema:**

```json
{
  "success": true,
  "lead_id": "lead_abc123",
  "promo_code": "MIKROS2025-ABC123",
  "message": "Lead submitted successfully. Promo code sent to email.",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

## Game Ecosystems APIs

### 17. Sync Game Configuration

**Method:** `POST`  
**Route:** `/api/games/sync-config`

**Description:** Syncs community game configuration from Discord server to backend for persistence.

**Request Body Schema:**

```json
{
  "server_id": "123456789012345678",
  "game_channel_id": "987654321098765432",
  "reset_time_utc": "00:00",
  "enabled_games": ["word_unscramble"],
  "difficulty": "medium",
  "updated_at": "2025-01-27T12:00:00Z"
}
```

**Example Response Schema:**

```json
{
  "success": true,
  "config_id": "config_xyz789",
  "message": "Configuration synced successfully",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 18. Get Game Session State

**Method:** `GET`  
**Route:** `/api/games/session-state`

**Description:** Retrieves current game session state for a Discord server.

**Query Parameters:**

| Parameter   | Type   | Required | Description               |
|-------------|--------|----------|---------------------------|
| `server_id` | string | Yes      | Discord server ID         |
| `game_type` | string | No       | Filter: "word_unscramble" |

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "server_id": "123456789012345678",
    "game_type": "word_unscramble",
    "session_id": "session_abc123",
    "started_at": "2025-01-27T00:00:00Z",
    "word": "GAMING",
    "scrambled_word": "PNGAEML",
    "solved": false,
    "solver_count": 0,
    "attempts": []
  },
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

## RPG Progression APIs

### 19. Save RPG Character

**Method:** `POST`  
**Route:** `/api/rpg/characters`

**Description:** Saves or updates an RPG character's state.

**Request Body Schema:**

```json
{
  "discord_id": "123456789012345678",
  "server_id": "987654321098765432",
  "character_name": "Aragorn",
  "character_class": "WARRIOR",
  "level": 5,
  "xp": 1250,
  "stats": {
    "hp": 120,
    "str": 18,
    "agi": 10,
    "int": 6,
    "luck": 9
  },
  "last_action_time": "2025-01-27T10:00:00Z",
  "inventory": [],
  "updated_at": "2025-01-27T12:00:00Z"
}
```

**Example Response Schema:**

```json
{
  "success": true,
  "character_id": "char_xyz789",
  "message": "Character saved successfully",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 20. Get RPG Character

**Method:** `GET`  
**Route:** `/api/rpg/characters/{discord_id}`

**Description:** Retrieves an RPG character by Discord user ID.

**Path Parameters:**

| Parameter    | Type   | Required | Description     |
|--------------|--------|----------|-----------------|
| `discord_id` | string | Yes      | Discord user ID |

**Query Parameters:**

| Parameter   | Type   | Required | Description                 |
|-------------|--------|----------|-----------------------------|
| `server_id` | string | No       | Filter by server (optional) |

**Example Request:**

```http
GET /api/rpg/characters/123456789012345678?server_id=987654321098765432 HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
```

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "discord_id": "123456789012345678",
    "server_id": "987654321098765432",
    "character_name": "Aragorn",
    "character_class": "WARRIOR",
    "level": 5,
    "xp": 1250,
    "stats": {
      "hp": 120,
      "str": 18,
      "agi": 10,
      "int": 6,
      "luck": 9
    },
    "last_action_time": "2025-01-27T10:00:00Z",
    "created_at": "2025-01-20T08:00:00Z",
    "updated_at": "2025-01-27T12:00:00Z"
  },
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 21. Get RPG Leaderboard

**Method:** `GET`  
**Route:** `/api/rpg/leaderboard`

**Description:** Returns RPG leaderboard for a server or globally.

**Query Parameters:**

| Parameter   | Type    | Required | Description                                          |
|-------------|---------|----------|------------------------------------------------------|
| `server_id` | string  | No       | Filter by server (omit for global)                   |
| `limit`     | integer | No       | Maximum results (default: 10, max: 100)              |
| `sort_by`   | string  | No       | Sort: "level", "xp", "created_at" (default: "level") |

**Example Request:**

```http
GET /api/rpg/leaderboard?server_id=987654321098765432&limit=10&sort_by=level HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
```

**Example Response Schema:**

```json
{
  "success": true,
  "data": [
    {
      "rank": 1,
      "discord_id": "123456789012345678",
      "character_name": "Aragorn",
      "character_class": "WARRIOR",
      "level": 15,
      "xp": 5000,
      "server_id": "987654321098765432"
    },
    {
      "rank": 2,
      "discord_id": "234567890123456789",
      "character_name": "Gandalf",
      "character_class": "MAGE",
      "level": 12,
      "xp": 3800,
      "server_id": "987654321098765432"
    }
  ],
  "total_players": 45,
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 22. Log RPG Action

**Method:** `POST`  
**Route:** `/api/rpg/actions`

**Description:** Logs an RPG action for analytics and progression tracking.

**Request Body Schema:**

```json
{
  "discord_id": "123456789012345678",
  "server_id": "987654321098765432",
  "action_type": "explore",
  "character_level": 5,
  "xp_gained": 35,
  "outcome": "success",
  "narrative_encounter": "You discovered an ancient treasure!",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

**Example Response Schema:**

```json
{
  "success": true,
  "action_id": "action_abc123",
  "message": "Action logged successfully",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

## Leaderboard Persistence APIs

### 23. Save Community Game Leaderboard

**Method:** `POST`  
**Route:** `/api/leaderboards/community-games`

**Description:** Saves community game leaderboard state (word unscramble).

**Request Body Schema:**

```json
{
  "server_id": "123456789012345678",
  "game_type": "word_unscramble",
  "session_id": "session_abc123",
  "leaderboard": [
    {
      "discord_id": "123456789012345678",
      "username": "Player1",
      "score": 150,
      "wins": 5,
      "first_solves": 2,
      "rank": 1
    }
  ],
  "updated_at": "2025-01-27T12:00:00Z"
}
```

**Example Response Schema:**

```json
{
  "success": true,
  "leaderboard_id": "lb_xyz789",
  "message": "Leaderboard saved successfully",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 24. Get Community Game Leaderboard

**Method:** `GET`  
**Route:** `/api/leaderboards/community-games`

**Description:** Retrieves community game leaderboard for a server.

**Query Parameters:**

| Parameter   | Type    | Required | Description                                                                 |
|-------------|---------|----------|-----------------------------------------------------------------------------|
| `server_id` | string  | Yes      | Discord server ID                                                           |
| `game_type` | string  | No       | Filter: "word_unscramble", "all"                                            |
| `period`    | string  | No       | Time period: "daily", "weekly", "monthly", "all_time" (default: "all_time") |
| `limit`     | integer | No       | Maximum results (default: 10, max: 100)                                     |

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "server_id": "123456789012345678",
    "game_type": "word_unscramble",
    "period": "all_time",
    "leaderboard": [
      {
        "rank": 1,
        "discord_id": "123456789012345678",
        "username": "Player1",
        "score": 150,
        "wins": 5,
        "first_solves": 2
      }
    ],
    "total_players": 25
  },
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 25. Save Spelling Challenge Leaderboard

**Method:** `POST`  
**Route:** `/api/leaderboards/spelling`

**Description:** Saves spelling challenge leaderboard state.

**Request Body Schema:**

```json
{
  "server_id": "123456789012345678",
  "leaderboard": [
    {
      "discord_id": "123456789012345678",
      "username": "Speller1",
      "total_points": 45,
      "total_solves": 12,
      "first_solves": 5,
      "rank": 1
    }
  ],
  "updated_at": "2025-01-27T12:00:00Z"
}
```

**Example Response Schema:**

```json
{
  "success": true,
  "leaderboard_id": "lb_spell_xyz789",
  "message": "Spelling leaderboard saved successfully",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 26. Get Spelling Challenge Leaderboard

**Method:** `GET`  
**Route:** `/api/leaderboards/spelling`

**Description:** Retrieves spelling challenge leaderboard.

**Query Parameters:**

| Parameter   | Type    | Required | Description                             |
|-------------|---------|----------|-----------------------------------------|
| `server_id` | string  | Yes      | Discord server ID                       |
| `limit`     | integer | No       | Maximum results (default: 10, max: 100) |

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "server_id": "123456789012345678",
    "leaderboard": [
      {
        "rank": 1,
        "discord_id": "123456789012345678",
        "username": "Speller1",
        "total_points": 45,
        "total_solves": 12,
        "first_solves": 5
      }
    ],
    "total_players": 18
  },
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

## Scheduling Sync APIs

### 27. Sync Scheduler Configuration

**Method:** `POST`  
**Route:** `/api/schedulers/sync-config`

**Description:** Syncs scheduler configurations (game resets, monthly reports, promotion checks) to backend.

**Request Body Schema:**

```json
{
  "server_id": "123456789012345678",
  "scheduler_type": "game_reset",
  "config": {
    "reset_time_utc": "00:00",
    "enabled": true,
    "timezone": "UTC"
  },
  "updated_at": "2025-01-27T12:00:00Z"
}
```

**Example Response Schema:**

```json
{
  "success": true,
  "config_id": "sched_config_xyz789",
  "message": "Scheduler configuration synced",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 28. Get Scheduler Status

**Method:** `GET`  
**Route:** `/api/schedulers/status`

**Description:** Retrieves current status of all schedulers for a server.

**Query Parameters:**

| Parameter   | Type   | Required | Description       |
|-------------|--------|----------|-------------------|
| `server_id` | string | Yes      | Discord server ID |

**Example Response Schema:**

```json
{
  "success": true,
  "data": {
    "server_id": "123456789012345678",
    "schedulers": [
      {
        "type": "game_reset",
        "enabled": true,
        "next_run": "2025-01-28T00:00:00Z",
        "last_run": "2025-01-27T00:00:00Z"
      },
      {
        "type": "monthly_report",
        "enabled": true,
        "next_run": "2025-02-01T09:00:00Z",
        "last_run": "2025-01-01T09:00:00Z"
      },
      {
        "type": "promotion_check",
        "enabled": true,
        "next_run": "2025-01-27T13:00:00Z",
        "last_run": "2025-01-27T12:00:00Z"
      }
    ]
  },
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

### 29. Trigger Scheduler Manually

**Method:** `POST`  
**Route:** `/api/schedulers/trigger`

**Description:** Manually triggers a scheduler (for testing/admin purposes).

**Request Body Schema:**

```json
{
  "server_id": "123456789012345678",
  "scheduler_type": "game_reset",
  "triggered_by": "admin_user_123",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

**Example Response Schema:**

```json
{
  "success": true,
  "message": "Scheduler triggered successfully",
  "execution_id": "exec_abc123",
  "timestamp": "2025-01-27T12:00:00Z"
}
```

---

## Authentication

All API endpoints require authentication via Bearer Token:

```http
Authorization: Bearer YOUR_API_KEY
```

**API Key Format:**

- Generated by MIKROS backend
- Stored securely in bot configuration
- Rotated periodically for security

## Error Responses

All endpoints return consistent error responses:

**Example Error Response:**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Missing required parameter: server_id",
    "details": {}
  },
  "timestamp": "2025-01-27T12:00:00Z"
}
```

**Common Error Codes:**

- `INVALID_REQUEST` - Missing or invalid parameters
- `UNAUTHORIZED` - Invalid or missing API key
- `NOT_FOUND` - Resource not found
- `RATE_LIMIT_EXCEEDED` - Too many requests
- `INTERNAL_ERROR` - Server error

## Rate Limiting

- **Default:** 100 requests per minute per API key
- **Burst:** Up to 200 requests in a 10-second window
- **Headers:** Rate limit info returned in response headers:
    - `X-RateLimit-Limit`: Maximum requests per window
    - `X-RateLimit-Remaining`: Remaining requests
    - `X-RateLimit-Reset`: Time when limit resets

## Base URL

**Production:** `https://api.tatumgames.com`  
**Staging:** `https://staging-api.tatumgames.com`  
**Development:** `https://dev-api.tatumgames.com`

---

**Document Version:** 1.0  
**Last Updated:** 2025-01-27  
**Status:** Blueprint - Awaiting Implementation

