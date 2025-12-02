# TASKS_04.md

## Objective

Create slash commands that provide **real-time industry metrics** powered by MIKROS Analytics. These stats will be
useful to game developers, marketers, server admins, and community members.

All slash commands should return **cleanly formatted Discord embed messages** and be designed to allow for future API
integration.

Cursor AI should follow `BEST_CODING_PRACTICES.md` throughout development.

---

## Command Namespace

All analytics commands must live under the unified command:
/gamestats

yaml
Copy code

Each subcommand should represent a specific KPI listed below.

---

## Implement the Following Subcommands (with TODOs for API Integration)

Cursor AI should implement **the following slash subcommands**, return **mock/dummy data for now**, and mark each with a
`TODO` to call the backend API. Each `TODO` must trigger the creation of a corresponding API spec markdown inside the
`/docs/` folder.

---

### 1. `/gamestats trending-game-genres`

- Description: Shows the top 3 fastest-growing game genres based on player engagement
- Output:
    - Genre name
    - % growth vs last 30 days
- Example:

🎮 Trending Game Genres:

* Roguelike (+43%)
* Puzzle (+31%)
* Sandbox (+29%)

- API Doc: `/docs/API_TRENDING_GAME_GENRES.md`

---

### 2. `/gamestats trending-content-genres`

- Description: Content-type genres (e.g. action, story, co-op)
- Similar output and formatting as above
- API Doc: `/docs/API_TRENDING_CONTENT_GENRES.md`

---

### 3. `/gamestats trending-content`

- Description: Shows actual in-game content (e.g. dungeon levels, bosses, characters) that are seeing spikes in playtime
- Output top 5 by usage %
- API Doc: `/docs/API_TRENDING_CONTENT.md`

---

### 4. `/gamestats trending-gameplay-types`

- Description: Casual, competitive, hyper-casual trends
- Include growth %
- API Doc: `/docs/API_TRENDING_GAMEPLAY_TYPES.md`

---

### 5. `/gamestats popular-game-genres`

- Top 3 most-played game genres overall
- API Doc: `/docs/API_POPULAR_GAME_GENRES.md`

---

### 6. `/gamestats popular-content-genres`

- Top 3 content genres with most engagement
- API Doc: `/docs/API_POPULAR_CONTENT_GENRES.md`

---

### 7. `/gamestats popular-content`

- Top 5 in-game content types or experiences
- API Doc: `/docs/API_POPULAR_CONTENT.md`

---

### 8. `/gamestats popular-gameplay-types`

- Top 3 gameplay types (e.g. competitive, casual)
- API Doc: `/docs/API_POPULAR_GAMEPLAY_TYPES.md`

---

## Scheduled Posting (Optional Feature)

- Feature: `/gamestats schedule-report`
- Description: Configure the bot to post a trending KPI to a channel every Monday
- Scheduler posts a random stat (or rotates through stat types)
- Use existing scheduling infra
- Configuration can use:
- Frequency (weekly/monthly)
- Channel to post
- Stat type (or random)
- If scheduling logic isn’t yet reusable, leave a `TODO` in code.

---

## Future-Ready (Track with TODOs + API Docs)

These KPIs are not yet live but should be planned as TODOs:

### 9. `/gamestats total-mikros-apps`

- Total number of apps using MIKROS Analytics
- API Doc: `/docs/API_TOTAL_MIKROS_APPS.md`

### 10. `/gamestats total-mikros-contributors`

- Users signed up to MIKROS ecosystem (devs, testers, players)
- API Doc: `/docs/API_TOTAL_MIKROS_CONTRIBUTORS.md`

### 11. `/gamestats total-users`

- Unique user profiles tracked across MIKROS-enabled games
- API Doc: `/docs/API_TOTAL_USERS.md`

### 12. `/gamestats avg-gameplay-time`

- Average gameplay time per app
- Optional parameter: game genre
- API Doc: `/docs/API_AVG_GAMEPLAY_TIME.md`

### 13. `/gamestats avg-session-time`

- Average session length across all games or by genre
- API Doc: `/docs/API_AVG_SESSION_TIME.md`

---

## Example Embed Response

```plaintext
🔥 Top Trending Game Genres (30-day growth)

1. Roguelike — +43%
2. Puzzle — +31%
3. Sandbox — +29%
```

Data provided by MIKROS Analytics

### Docs Auto-Generation Instructions

For each API call marked as TODO, Cursor AI must generate a detailed markdown doc in /docs/:

Example:

/docs/API_TRENDING_GAME_GENRES.md

Each doc must contain:
| Section | Description |
| ----------------- | -------------------------------------------------------- |
| Feature Overview | What the API provides |
| Why API is needed | Why this data can’t be pulled client-side |
| Endpoint | Method + URL (e.g. `GET /api/gamestats/trending-genres`) |
| Parameters | e.g. `genre`, `dateRange`, `limit`                       |
| Sample Request | With mock values |
| Sample Response | JSON structure |
| Auth | Header/Token style |
| Rate Limits | Any limits or restrictions |
| Future Extensions | Ideas for scaling or enhancing |

Summary of /gamestats Commands

| Command                              | Description                       |
|--------------------------------------|-----------------------------------|
| `/gamestats trending-game-genres`    | Fastest-growing genres            |
| `/gamestats trending-content-genres` | Top growing content types         |
| `/gamestats trending-content`        | Top content seeing spikes         |
| `/gamestats trending-gameplay-types` | Competitive/casual growth         |
| `/gamestats popular-game-genres`     | Most played game genres           |
| `/gamestats popular-content-genres`  | Top content genre engagement      |
| `/gamestats popular-content`         | Top 5 in-game content experiences |
| `/gamestats popular-gameplay-types`  | Competitive/casual popularity     |
| `/gamestats total-users`             | Unique users tracked              |
| `/gamestats avg-session-time`        | Avg. session time by genre        |
| `/gamestats schedule-report`         | Schedule stat posts               |

### Final Notes

* All output should be styled using embed messages
* Use placeholder/mock data until APIs are connected
* Ensure proper command grouping, type-safe enums, and modular services
* Flag incomplete commands with TODOs and document missing APIs
