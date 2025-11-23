# API: Popular Content Genres

## Feature Overview

Returns the most engaging content genres (multiplayer, open world, story mode) by total player engagement and time spent.

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/popular-content-genres`

---

## Sample Response

```json
{
  "content_genres": [
    {
      "rank": 1,
      "genre_name": "Multiplayer",
      "player_count": 680000,
      "total_hours_played": 12400000,
      "growth_percentage": 15.2
    },
    {
      "rank": 2,
      "genre_name": "Open World",
      "player_count": 530000,
      "total_hours_played": 9850000,
      "growth_percentage": 11.8
    },
    {
      "rank": 3,
      "genre_name": "Story Mode",
      "player_count": 420000,
      "total_hours_played": 7200000,
      "growth_percentage": 9.4
    }
  ]
}
```

---

**Version**: 1.0  
**Status**: 📋 Specification Complete

