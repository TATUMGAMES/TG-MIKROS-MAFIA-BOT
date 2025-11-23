# API: Popular Gameplay Types

## Feature Overview

Returns the most popular gameplay types (casual, competitive, hyper-casual) by total active players and market share.

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/popular-gameplay-types`

---

## Sample Response

```json
{
  "gameplay_types": [
    {
      "rank": 1,
      "gameplay_type": "Casual",
      "player_count": 750000,
      "market_share": 52.3,
      "growth_percentage": 8.5,
      "average_session_minutes": 22.4
    },
    {
      "rank": 2,
      "gameplay_type": "Competitive",
      "player_count": 480000,
      "market_share": 33.5,
      "growth_percentage": 6.2,
      "average_session_minutes": 45.8
    },
    {
      "rank": 3,
      "gameplay_type": "Hyper-Casual",
      "player_count": 205000,
      "market_share": 14.2,
      "growth_percentage": 3.1,
      "average_session_minutes": 8.1
    }
  ]
}
```

---

**Version**: 1.0  
**Status**: 📋 Specification Complete

