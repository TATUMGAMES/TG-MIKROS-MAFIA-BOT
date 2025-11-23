# API: Trending Content

## Feature Overview

This API returns specific in-game content (levels, bosses, characters, game modes) that are experiencing engagement spikes. This helps developers understand which content resonates with players and informs future development priorities.

## Why API is Needed

- **Granular Insights**: Track individual content performance, not just genres
- **Content ROI**: Measure which content investments pay off
- **Player Psychology**: Understand what drives player engagement
- **Live Operations**: Inform event planning and content updates
- **Cross-Game Learning**: See what works across different titles

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/trending-content`

### Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `limit` | integer | No | Number of content items to return (default: 5, max: 50) |
| `content_type` | string | No | Filter: "boss", "level", "character", "mode", "all" (default: "all") |
| `period` | string | No | "7d", "30d" (default: "30d") |

---

## Sample Response

**Status: 200 OK**

```json
{
  "period": "30d",
  "content_items": [
    {
      "rank": 1,
      "content_name": "Nightmare Dungeon",
      "content_type": "Boss",
      "game_name": "Shadow Quest",
      "growth_percentage": 52.3,
      "usage_count": 45000,
      "completion_rate": 34.2,
      "average_attempts": 3.8
    },
    {
      "rank": 2,
      "content_name": "Crystal Caves",
      "content_type": "Level",
      "game_name": "Gem Hunter",
      "growth_percentage": 47.1,
      "usage_count": 38000,
      "completion_rate": 67.5,
      "average_attempts": 1.5
    },
    {
      "rank": 3,
      "content_name": "Shadow Assassin",
      "content_type": "Character",
      "game_name": "Hero Arena",
      "growth_percentage": 41.8,
      "usage_count": 32000,
      "pick_rate": 28.5,
      "win_rate": 52.1
    }
  ]
}
```

---

**Version**: 1.0  
**Status**: 📋 Specification Complete - Implementation Pending

