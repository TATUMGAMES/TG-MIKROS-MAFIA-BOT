# API: Average Gameplay Time

## Feature Overview

Returns average total gameplay time per app, optionally filtered by genre. Helps developers benchmark player engagement
and retention.

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/avg-gameplay-time`

### Query Parameters

| Name    | Type   | Required | Description          |
|---------|--------|----------|----------------------|
| `genre` | string | No       | Filter by game genre |

---

## Sample Response

```json
{
  "average_gameplay_hours": 15.4,
  "genre": null,
  "sample_size": 1247,
  "median_gameplay_hours": 12.3,
  "breakdown_by_genre": {
    "rpg": 24.5,
    "strategy": 18.3,
    "action": 12.7,
    "puzzle": 8.2,
    "hyper_casual": 3.1
  },
  "timestamp": "2025-10-07T22:00:00Z"
}
```

---

**Version**: 1.0  
**Status**: 📋 Specification Complete

