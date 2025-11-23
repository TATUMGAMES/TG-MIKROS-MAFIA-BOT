# API: Trending Game Genres

## Feature Overview

This API provides real-time data on the fastest-growing game genres based on 30-day player engagement trends. It helps developers, marketers, and community managers identify emerging opportunities in the gaming industry.

## Why API is Needed

- **Real-Time Market Intelligence**: Game trends change rapidly; real-time data is essential
- **Cross-Platform Aggregation**: Data aggregated from multiple games across the MIKROS network
- **Computational Intensity**: Calculating growth rates requires processing millions of data points
- **Centralized Analytics**: Single source of truth for industry trends
- **Historical Comparison**: Requires access to historical data warehouse

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/trending-genres`

### Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `limit` | integer | No | Number of genres to return (default: 3, max: 20) |
| `period` | string | No | Time period: "7d", "30d", "90d" (default: "30d") |
| `min_player_count` | integer | No | Minimum player threshold (default: 1000) |

### Headers

```
Authorization: Bearer YOUR_API_KEY
Content-Type: application/json
```

---

## Sample Request

```http
GET /api/gamestats/trending-genres?limit=3&period=30d HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
```

---

## Sample Response

**Status: 200 OK**

```json
{
  "period": "30d",
  "timestamp": "2025-10-07T22:00:00Z",
  "genres": [
    {
      "rank": 1,
      "genre_name": "Roguelike",
      "growth_percentage": 43.2,
      "current_player_count": 185000,
      "previous_player_count": 129000,
      "trend": "accelerating",
      "notable_games": ["Dungeon Drifter", "Shadow Loop", "Eternal Run"]
    },
    {
      "rank": 2,
      "genre_name": "Puzzle",
      "growth_percentage": 31.5,
      "current_player_count": 142000,
      "previous_player_count": 108000,
      "trend": "steady",
      "notable_games": ["Block Master", "Mind Maze", "Cube Quest"]
    },
    {
      "rank": 3,
      "genre_name": "Sandbox",
      "growth_percentage": 29.1,
      "current_player_count": 198000,
      "previous_player_count": 153000,
      "trend": "accelerating",
      "notable_games": ["Creative World", "Build Empire", "Planet Maker"]
    }
  ],
  "metadata": {
    "total_genres_tracked": 42,
    "data_points_analyzed": 2450000,
    "last_updated": "2025-10-07T21:55:00Z"
  }
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `period` | string | Time period for the analysis |
| `timestamp` | ISO 8601 string | When the data was generated |
| `genres` | array | List of trending genres |
| `genres[].rank` | integer | Ranking position (1 = top) |
| `genres[].genre_name` | string | Name of the genre |
| `genres[].growth_percentage` | number | Growth rate percentage |
| `genres[].current_player_count` | integer | Current active players |
| `genres[].previous_player_count` | integer | Players at start of period |
| `genres[].trend` | string | "accelerating", "steady", "decelerating" |
| `genres[].notable_games` | array | Top games in this genre |
| `metadata` | object | Additional context information |

---

## Authentication Method

**Bearer Token**: Include bot API key in Authorization header

```
Authorization: Bearer YOUR_API_KEY
```

---

## Rate Limiting

- **Standard Tier**: 60 requests/hour
- **Premium Tier**: 300 requests/hour
- **Data Updates**: Every 6 hours

---

## How Bot Uses This Data

```java
List<GenreStat> genres = gameStatsService.getTrendingGameGenres(3);

EmbedBuilder embed = new EmbedBuilder();
embed.setTitle("🔥 Top Trending Game Genres");

for (GenreStat genre : genres) {
    embed.appendDescription(String.format(
        "%d. %s — +%.1f%%\n",
        genre.getRank(),
        genre.getGenreName(),
        genre.getGrowthPercentage()
    ));
}
```

---

## Future Extensions

1. **Predictive Analytics**: Forecast which genres will trend next
2. **Regional Breakdowns**: Trends by geographic region
3. **Platform Filters**: Steam vs. Mobile vs. Web
4. **Demographic Data**: Age groups preferring each genre
5. **Seasonal Patterns**: Holiday/seasonal trend analysis
6. **Webhook Notifications**: Alert when a genre spikes
7. **Custom Alerts**: Notify when specific genres reach thresholds
8. **Historical Charts**: API returns chart data for visualization

---

**Version**: 1.0  
**Last Updated**: October 7, 2025  
**Status**: 📋 Specification Complete - Implementation Pending

