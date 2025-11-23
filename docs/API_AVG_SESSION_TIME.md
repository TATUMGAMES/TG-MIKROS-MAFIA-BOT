# API: Average Session Time

## Feature Overview

Returns average session length across games or filtered by genre. Indicates typical play session duration and helps optimize game design for target session lengths.

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/avg-session-time`

### Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `genre` | string | No | Filter by game genre |

---

## Sample Response

```json
{
  "average_session_minutes": 32.4,
  "genre": null,
  "sample_size": 3240567,
  "median_session_minutes": 28.7,
  "breakdown_by_genre": {
    "rpg": 45.2,
    "strategy": 38.7,
    "action": 28.5,
    "puzzle": 15.3,
    "hyper_casual": 8.1
  },
  "breakdown_by_platform": {
    "pc": 42.3,
    "mobile": 18.2,
    "web": 15.7
  },
  "timestamp": "2025-10-07T22:00:00Z"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `average_session_minutes` | number | Average session length in minutes |
| `genre` | string | Genre filter applied (null if all genres) |
| `sample_size` | integer | Number of sessions analyzed |
| `median_session_minutes` | number | Median session length |
| `breakdown_by_genre` | object | Session times per genre |
| `breakdown_by_platform` | object | Session times per platform |

---

**Version**: 1.0  
**Status**: 📋 Specification Complete

