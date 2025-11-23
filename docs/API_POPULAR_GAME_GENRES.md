# API: Popular Game Genres

## Feature Overview

Returns the most-played game genres overall by total active player count, providing insights into the current gaming landscape and established market leaders.

## Why API is Needed

- **Market Size Analysis**: Understand which genres dominate the market
- **Investment Decisions**: Help developers choose viable genres
- **Benchmark Performance**: Compare your game against genre leaders
- **Portfolio Planning**: Studios can diversify based on popular genres

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/popular-genres`

### Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `limit` | integer | No | Number of genres to return (default: 3, max: 20) |
| `platform` | string | No | Filter: "pc", "mobile", "web", "all" (default: "all") |

---

## Sample Response

```json
{
  "timestamp": "2025-10-07T22:00:00Z",
  "genres": [
    {
      "rank": 1,
      "genre_name": "Action",
      "player_count": 520000,
      "market_share": 28.3,
      "growth_percentage": 12.5,
      "games_count": 342
    },
    {
      "rank": 2,
      "genre_name": "RPG",
      "player_count": 485000,
      "market_share": 26.4,
      "growth_percentage": 8.3,
      "games_count": 278
    },
    {
      "rank": 3,
      "genre_name": "Strategy",
      "player_count": 340000,
      "market_share": 18.5,
      "growth_percentage": 5.7,
      "games_count": 195
    }
  ]
}
```

---

**Version**: 1.0  
**Status**: 📋 Specification Complete

