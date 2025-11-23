# Stats Commands Documentation

## Overview

The **Stats Commands** (`stats-*`) provide real-time game industry metrics powered by MIKROS Analytics. These commands are useful for game developers, marketers, server admins, and community members.

**Command:** `/stats` with subcommands

**Permission:** Everyone (no restrictions)

---

## Commands

### Trending Metrics

#### `/stats trending-game-genres`

**Description:** Shows the top 3 fastest-growing game genres based on player engagement.

**Syntax:**
```
/stats trending-game-genres
```

**Output:**
- Genre name
- Growth percentage vs last 30 days
- Formatted embed

**Example Output:**
```
🎮 Trending Game Genres

1. Roguelike (+43%)
2. Puzzle (+31%)
3. Sandbox (+29%)
```

**Status:** ⚠️ Mock data (TODO: API integration)

---

#### `/stats trending-content-genres`

**Description:** Shows trending content-type genres (e.g., action, story, co-op).

**Syntax:**
```
/stats trending-content-genres
```

**Output:**
- Content genre name
- Growth percentage
- Formatted embed

**Example Output:**
```
📊 Trending Content Genres

1. Action-Adventure (+38%)
2. Story-Driven (+27%)
3. Co-op Multiplayer (+24%)
```

**Status:** ⚠️ Mock data (TODO: API integration)

---

#### `/stats trending-content`

**Description:** Shows actual in-game content (e.g., dungeon levels, bosses, characters) seeing spikes in playtime.

**Syntax:**
```
/stats trending-content
```

**Output:**
- Top 5 trending content items
- Usage percentage
- Formatted embed

**Example Output:**
```
🔥 Trending Content

1. Dragon's Lair (Dungeon) - 12.5% usage
2. Final Boss Battle - 10.8% usage
3. Character Customization - 9.2% usage
4. PvP Arena - 8.7% usage
5. Crafting System - 7.9% usage
```

**Status:** ⚠️ Mock data (TODO: API integration)

---

#### `/stats trending-gameplay-types`

**Description:** Shows trending gameplay types (casual, competitive, hyper-casual).

**Syntax:**
```
/stats trending-gameplay-types
```

**Output:**
- Gameplay type
- Growth percentage
- Formatted embed

**Example Output:**
```
🎯 Trending Gameplay Types

1. Competitive (+35%)
2. Casual (+28%)
3. Hyper-Casual (+22%)
```

**Status:** ⚠️ Mock data (TODO: API integration)

---

### Popular Metrics

#### `/stats popular-game-genres`

**Description:** Shows the top 3 most-played game genres overall.

**Syntax:**
```
/stats popular-game-genres
```

**Output:**
- Genre name
- Total playtime or player count
- Formatted embed

**Example Output:**
```
🏆 Popular Game Genres

1. Action (2.5M players)
2. RPG (1.8M players)
3. Strategy (1.2M players)
```

**Status:** ⚠️ Mock data (TODO: API integration)

---

#### `/stats popular-content-genres`

**Description:** Shows most-played content genres.

**Syntax:**
```
/stats popular-content-genres
```

**Output:**
- Content genre
- Engagement metrics
- Formatted embed

**Status:** ⚠️ Mock data (TODO: API integration)

---

#### `/stats popular-content`

**Description:** Shows most popular in-game content experiences.

**Syntax:**
```
/stats popular-content
```

**Output:**
- Top 5 popular content items
- Engagement metrics
- Formatted embed

**Status:** ⚠️ Mock data (TODO: API integration)

---

#### `/stats popular-gameplay-types`

**Description:** Shows most popular gameplay types overall.

**Syntax:**
```
/stats popular-gameplay-types
```

**Output:**
- Gameplay type
- Player count or engagement
- Formatted embed

**Status:** ⚠️ Mock data (TODO: API integration)

---

### Platform Metrics

#### `/stats total-mikros-apps`

**Description:** Shows total number of apps using MIKROS Analytics.

**Syntax:**
```
/stats total-mikros-apps
```

**Output:**
- Total app count
- Growth metrics
- Formatted embed

**Example Output:**
```
📱 Total MIKROS Apps

Total: 1,247 apps
Growth: +23 this month
```

**Status:** ⚠️ Mock data (TODO: API integration)

---

#### `/stats total-mikros-contributors`

**Description:** Shows total number of MIKROS ecosystem contributors.

**Syntax:**
```
/stats total-mikros-contributors
```

**Output:**
- Total contributor count
- Formatted embed

**Status:** ⚠️ Mock data (TODO: API integration)

---

#### `/stats total-users`

**Description:** Shows unique user profiles tracked across MIKROS.

**Syntax:**
```
/stats total-users
```

**Output:**
- Total user count
- Growth metrics
- Formatted embed

**Example Output:**
```
👥 Total Users

Total: 15.8M users
Growth: +2.3M this quarter
```

**Status:** ⚠️ Mock data (TODO: API integration)

---

### Time Metrics

#### `/stats avg-gameplay-time`

**Description:** Shows average gameplay time per app.

**Syntax:**
```
/stats avg-gameplay-time [genre:<string>]
```

**Parameters:**
- `genre` (optional): Filter by specific genre

**Output:**
- Average gameplay time
- Genre-specific if filtered
- Formatted embed

**Example:**
```
/stats avg-gameplay-time
/stats avg-gameplay-time genre:Action
```

**Status:** ⚠️ Mock data (TODO: API integration)

---

#### `/stats avg-session-time`

**Description:** Shows average session length.

**Syntax:**
```
/stats avg-session-time [genre:<string>]
```

**Parameters:**
- `genre` (optional): Filter by specific genre

**Output:**
- Average session time
- Genre-specific if filtered
- Formatted embed

**Example:**
```
/stats avg-session-time
/stats avg-session-time genre:RPG
```

**Status:** ⚠️ Mock data (TODO: API integration)

---

## Behavior

### Command Structure

All stats commands:
- Use `/stats` as base command
- Have subcommands for specific metrics
- Return formatted Discord embeds
- Include emojis and color coding
- Show mock data (until API integration)

### Error Handling

**No Subcommand:**
```
❌ Please specify a subcommand.
```

**Unknown Subcommand:**
```
❌ Unknown subcommand.
```

**API Errors (Future):**
```
❌ An error occurred while fetching statistics.
```

---

## API Integration Status

**Current:** ⚠️ **Mock Data**

All commands currently return mock/dummy data with `TODO` markers for API integration.

**Planned Integration:**
- MIKROS Analytics API
- Real-time data fetching
- Caching for performance
- Error handling for API failures

**API Documentation:**
- See `/docs/API_*.md` files for API specifications
- 22 API documentation files available

---

## Use Cases

### For Game Developers
- Track genre trends
- Identify popular content
- Understand player engagement

### For Marketers
- Spot trending genres
- Identify growth opportunities
- Track platform growth

### For Server Admins
- Share industry insights
- Engage community with stats
- Provide value to members

### For Community Members
- Learn about gaming trends
- Discover popular games
- Stay informed about industry

---

## Future Enhancements

- 🔮 **Real API Integration:** Connect to MIKROS Analytics API
- 🔮 **Caching:** Cache results for performance
- 🔮 **Charts/Graphs:** Visual representations
- 🔮 **Historical Data:** Compare over time
- 🔮 **Custom Filters:** More filtering options
- 🔮 **Export:** Export stats to files

---

**Last Updated:** 2025-10-08  
**Command Prefix:** `stats-*`  
**Total Subcommands:** 13





