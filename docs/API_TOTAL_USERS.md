# API: Total Users Tracked

## Feature Overview

Returns the total number of unique user profiles tracked across all MIKROS-enabled games, demonstrating network reach
and cross-game player base.

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/api/gamestats/total-users`

---

## Sample Response

```json
{
  "total_users": 3240567,
  "active_users_30d": 1852340,
  "new_users_30d": 284520,
  "growth_percentage": 18.4,
  "average_games_per_user": 2.7,
  "cross_game_users": 876234,
  "timestamp": "2025-10-07T22:00:00Z"
}
```

---

## Response Fields

| Field                    | Type    | Description                          |
|--------------------------|---------|--------------------------------------|
| `total_users`            | integer | Total unique users ever tracked      |
| `active_users_30d`       | integer | Users active in last 30 days         |
| `new_users_30d`          | integer | New users added in last 30 days      |
| `growth_percentage`      | number  | User growth rate                     |
| `average_games_per_user` | number  | Average games played per user        |
| `cross_game_users`       | integer | Users who play multiple MIKROS games |

---

**Version**: 1.0  
**Status**: 📋 Specification Complete

