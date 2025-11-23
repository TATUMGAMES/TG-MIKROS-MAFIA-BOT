# TASKS_03.md

## Objective
Implement a **Game Promotion Feature** for the MIKROS Bot that automatically shares indie games running active MIKROS Marketing campaigns in selected Discord channels.

The bot will fetch campaign game data from an external API and post well-formatted promotional messages in Discord servers that have opted in. It will respect configured verbosity levels and future publishing dates.

Always follow `BEST_CODING_PRACTICES.md`.

---

## Bot Feature: Indie Game Campaign Promotion

### What Cursor AI Should Build Now

#### 1. `/setup-promotion-channel` Command
- Admin-only command.
- Lets a server admin set a designated text channel where promotions should be posted.
- Store this configuration in-memory (future: migrate to DB).
- Response should confirm the channel was saved.

#### 2. Promotion Verbosity Setting
- Introduce a setting per server: `promotion_verbosity`
- Acceptable values: `LOW`, `MEDIUM`, `HIGH`
- Default value: `MEDIUM`
- Create `/set-promotion-verbosity <LOW|MEDIUM|HIGH>` command
- Use an enum internally to map values to frequency (see below)

#### 3. Scheduled Game Promotion Service
- Create a service class (e.g., `GamePromotionScheduler`)
- Use `ScheduledExecutorService` or similar to run polling logic
- Frequency is based on current verbosity setting
- On schedule:
    - Retrieve game promotion data (via TODO API)
    - Only post if:
        - Current time is **after** `deadline`
        - `isPushed == false`
    - Format message and post to configured channel
- Include logic to avoid reposting already pushed items

#### 4. `/force-promotion-check` (Admin Command)
- Manually trigger the game fetch and posting logic (for testing/demo purposes)
- Restricted to server admins

---

## Part 2: What Requires an External API (Mark as TODO)

When building the scheduled fetch logic or posting logic, Cursor AI should **add a `TODO` comment** in code and create an API documentation file **only when necessary**.

For example:
```java
// TODO: Fetch campaign games from external marketing API
```

---

## TODOs — Features That Depend on External APIs

Cursor AI should:

1. Add clear `TODO` tags in relevant parts of the code where external data is required
2. Generate `/docs/API_GAME_PROMOTION_SCHEDULE.md` to define the external API and behavior

The document must include:
* Feature Overview
* Why this API is needed
* Request method, endpoint, and parameters
* Sample JSON response
* Authentication method (if applicable)
* How the bot is expected to use the data
* Scalability & future considerations

---

## External API Design (To Be Defined)

| Field | Description |
|-------|-------------|
| `game_id` | Unique ID of the game |
| `game_name` | Title of the game |
| `description` | Short marketing pitch or summary |
| `promotion_message` | *(Optional)* Pre-written post message |
| `promotion_url` | Steam link or MIKROS marketing link |
| `image_url` | Optional cover art or banner |
| `deadline` | UTC datetime — Only promote if current time is past this |
| `isPushed` | Bool — Set to `true` after bot posts (prevents duplicate posts) |

---

## Sample JSON from API

```json
[
  {
    "game_id": 1021,
    "game_name": "ShadowSprint",
    "description": "A neon-drenched, parkour runner set in a dystopian Tokyo.",
    "promotion_url": "https://store.steampowered.com/app/00000/shadowsprint",
    "promotion_message": null,
    "image_url": "https://cdn.example.com/shadowsprint.png",
    "deadline": "2025-10-08T18:00:00Z",
    "isPushed": false
  },
  {
    "game_id": 1022,
    "game_name": "Pixel Raiders",
    "description": "Squad up and raid dungeons in this SNES-style online RPG.",
    "promotion_url": "https://tatumgames.com/pixel-raiders",
    "promotion_message": "🔥 Pixel Raiders is now live! Team up, loot up, and dive into the pixel madness! 🎮 Play now: https://tatumgames.com/pixel-raiders",
    "image_url": null,
    "deadline": "2025-10-10T16:00:00Z",
    "isPushed": false
  }
]
```

What the Bot Should Do
* Every X hours (based on verbosity), make a request to GET /active-promotions

For each returned item:
* If isPushed == false AND current UTC time is after deadline
* Format and post promotion message in the configured server channel
* If promotion_message is provided, use it

Else, dynamically create a message like:

🚨 New indie gem alert!
🎮 ShadowSprint – A neon-drenched, parkour runner set in a dystopian Tokyo.
👉 Play it here: [Steam URL]

** (TODO) Mark the game as pushed via API if backend supports it
* Display embedded image (if image_url is provided)

/docs/API_GAME_PROMOTION_SCHEDULE.md Example

Cursor AI should create this file with the following content:

# API: Game Promotion Schedule

## Overview
This API delivers a list of active indie games that should be promoted by the MIKROS Bot. The data includes timing control, custom messages, and asset links.

## Method
`GET /active-promotions`

### Response Body

```json
[
  {
    "game_id": 1021,
    "game_name": "ShadowSprint",
    "description": "A neon-drenched, parkour runner set in a dystopian Tokyo.",
    "promotion_url": "https://store.steampowered.com/app/00000/shadowsprint",
    "promotion_message": null,
    "image_url": "https://cdn.example.com/shadowsprint.png",
    "deadline": "2025-10-08T18:00:00Z",
    "isPushed": false
  }
]
```

Notes
* If promotion_message is null, the bot builds a template from name/description
* If image_url is provided, it is shown in the embed
* Future: Add genre, platform, and user preferences

TODOs
* Auth mechanism (API key?)
* Allow bot to notify backend when a game is pushed (e.g. POST /mark-pushed)

---

## Future Features (Optional)

| Feature | Description |
|--------|-------------|
| `/game-promo-frequency` | Allows server admins to adjust verbosity |
| `/disable-promotions` | Unsubscribes server from game promotions |
| Game reactions leaderboard | Track which games get the most reactions/emojis in Discord |

---

## Summary of Commands to Implement

| Command | Purpose |
|---------|---------|
| `/setup-promotion-channel` | Set which channel receives promotions |
| `/set-promotion-verbosity` | Control how often bot posts |
| `/force-promotion-check` | Manually trigger promotion check |
| **TODO** `/disable-promotions` | Remove game promotion from server |

---

## Reminder

Cursor AI should **not attempt to build backend integration yet**.  
Instead, focus on:
- Command structure
- Scheduling system
- Message formatting logic
- API doc stub for backend implementation
