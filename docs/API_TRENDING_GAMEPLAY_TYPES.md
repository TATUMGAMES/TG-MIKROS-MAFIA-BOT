# API: Trending Gameplay Types

## Feature Overview

This API tracks trends in gameplay styles (casual, competitive, hyper-casual) to help developers understand shifting
player preferences and market dynamics.

## Why API is Needed

- **Market Positioning**: Understand where to position new games
- **Monetization Strategy**: Different gameplay types require different approaches
- **Player Retention**: Match gameplay style to target audience
- **Competitive Landscape**: See which styles are gaining traction

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/trending-gameplay-types`

### Query Parameters

| Name     | Type    | Required | Description                                     |
|----------|---------|----------|-------------------------------------------------|
| `limit`  | integer | No       | Number of types to return (default: 3, max: 10) |
| `period` | string  | No       | "7d", "30d", "90d" (default: "30d")             |

---

## Sample Response

**Status: 200 OK**

```json
{
  "period": "30d",
  "gameplay_types": [
    {
      "rank": 1,
      "gameplay_type": "Competitive",
      "growth_percentage": 41.5,
      "current_player_count": 320000,
      "market_share": 35.2,
      "average_session_minutes": 45.3,
      "retention_rate": 68.5
    },
    {
      "rank": 2,
      "gameplay_type": "Casual",
      "growth_percentage": 28.3,
      "current_player_count": 450000,
      "market_share": 49.5,
      "average_session_minutes": 22.7,
      "retention_rate": 52.1
    },
    {
      "rank": 3,
      "gameplay_type": "Hyper-Casual",
      "growth_percentage": 22.1,
      "current_player_count": 140000,
      "market_share": 15.3,
      "average_session_minutes": 8.2,
      "retention_rate": 38.4
    }
  ]
}
```

---

**Version**: 1.0  
**Status**: 📋 Specification Complete - Implementation Pending

