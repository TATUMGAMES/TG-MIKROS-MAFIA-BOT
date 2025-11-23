# API: Total MIKROS Apps

## Feature Overview

Returns the total number of games and applications currently using MIKROS Analytics SDK, providing insight into the platform's growth and ecosystem size.

## Why API is Needed

- **Platform Growth Metrics**: Track MIKROS adoption over time
- **Developer Confidence**: Shows platform stability and adoption
- **Community Building**: Highlights ecosystem size for marketing
- **Partnership Opportunities**: Demonstrates network reach

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/total-apps`

---

## Sample Response

**Status: 200 OK**

```json
{
  "total_apps": 1247,
  "active_apps_30d": 1089,
  "new_apps_this_month": 37,
  "growth_month_over_month": 15.3,
  "breakdown_by_platform": {
    "pc": 543,
    "mobile": 482,
    "web": 178,
    "console": 44
  },
  "breakdown_by_category": {
    "indie_games": 892,
    "mobile_apps": 245,
    "web_games": 110
  },
  "timestamp": "2025-10-07T22:00:00Z"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `total_apps` | integer | Total apps with MIKROS SDK integrated |
| `active_apps_30d` | integer | Apps with activity in last 30 days |
| `new_apps_this_month` | integer | Apps added this month |
| `growth_month_over_month` | number | Percentage growth vs. last month |
| `breakdown_by_platform` | object | Apps per platform |
| `breakdown_by_category` | object | Apps per category |

---

**Version**: 1.0  
**Status**: 📋 Specification Complete

