# API: Popular Content

## Feature Overview

Returns the most popular in-game content items (specific levels, bosses, modes) by total engagement across all
MIKROS-enabled games.

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/popular-content`

---

## Sample Response

```json
{
  "content_items": [
    {
      "rank": 1,
      "content_name": "Battle Royale Mode",
      "content_type": "Game Mode",
      "usage_count": 125000,
      "growth_percentage": 18.5,
      "games_featuring": 23
    },
    {
      "rank": 2,
      "content_name": "Ancient Temple",
      "content_type": "Level",
      "usage_count": 98000,
      "growth_percentage": 14.2,
      "games_featuring": 8
    },
    {
      "rank": 3,
      "content_name": "Knight Class",
      "content_type": "Character",
      "usage_count": 87000,
      "growth_percentage": 12.7,
      "games_featuring": 12
    }
  ]
}
```

---

**Version**: 1.0  
**Status**: 📋 Specification Complete

