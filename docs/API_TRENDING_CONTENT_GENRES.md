# API: Trending Content Genres

## Feature Overview

This API provides data on the fastest-growing content-type genres (e.g., action, story-driven, co-op multiplayer) based on player engagement and playtime metrics over a 30-day period.

## Why API is Needed

- **Content Strategy Insights**: Helps developers decide what type of content to create
- **Market Gaps**: Identify underserved but growing content types
- **Player Preferences**: Understand what players are gravitating toward
- **Competitive Analysis**: See what competitors are doing successfully
- **Data Aggregation**: Requires analyzing millions of gameplay sessions

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/trending-content-genres`

### Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `limit` | integer | No | Number of content genres to return (default: 3, max: 20) |
| `period` | string | No | Time period: "7d", "30d", "90d" (default: "30d") |

---

## Sample Response

**Status: 200 OK**

```json
{
  "period": "30d",
  "timestamp": "2025-10-07T22:00:00Z",
  "content_genres": [
    {
      "rank": 1,
      "genre_name": "Co-op Multiplayer",
      "growth_percentage": 38.7,
      "current_player_count": 220000,
      "previous_player_count": 158000,
      "average_session_minutes": 42.5,
      "trend": "accelerating"
    },
    {
      "rank": 2,
      "genre_name": "Story-Driven",
      "growth_percentage": 35.2,
      "current_player_count": 175000,
      "previous_player_count": 129000,
      "average_session_minutes": 38.3,
      "trend": "steady"
    },
    {
      "rank": 3,
      "genre_name": "Action RPG",
      "growth_percentage": 27.8,
      "current_player_count": 210000,
      "previous_player_count": 164000,
      "average_session_minutes": 35.7,
      "trend": "accelerating"
    }
  ]
}
```

---

**Version**: 1.0  
**Status**: 📋 Specification Complete - Implementation Pending

