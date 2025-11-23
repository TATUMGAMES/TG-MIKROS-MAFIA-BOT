# API: Reputation Score (GET)

## Feature Overview

This API allows the Discord bot to retrieve a user's current **Reputation Score** based on their behavior across multiple Discord servers using the MIKROS bot. The score is affected by warnings, bans, kicks, and community-reported behaviors.

## Why API is Needed

- **Cross-Server Tracking**: User behavior shouldn't be isolated to a single server. Bad actors can move between servers, and good community members should be recognized universally.
- **Centralized Database**: A single source of truth for reputation scores across the MIKROS network.
- **Historical Context**: Provides moderators with valuable context when dealing with new users who may have a history elsewhere.
- **Fair Moderation**: Helps identify repeat offenders while also recognizing positive contributors.

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/reputation-score/{discord_id}`

### Path Parameters

| Name | Type | Description |
|------|------|-------------|
| `discord_id` | string | Discord user ID (snowflake) |

### Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `server_id` | string | No | If provided, includes server-specific breakdown |
| `include_history` | boolean | No | Whether to include full history (default: false) |

---

## Sample Request

```http
GET /reputation-score/293488128372?include_history=true HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_API_KEY
Content-Type: application/json
```

---

## Sample Response

### Status: 200 OK

```json
{
  "discord_id": "293488128372",
  "username": "GamerPro123",
  "reputation_score": 82,
  "tier": "Good Standing",
  "global_rank": 12043,
  "total_actions": 15,
  "positive_actions": 12,
  "negative_actions": 3,
  "servers_active": 5,
  "last_updated": "2025-10-07T16:30:00Z",
  "is_flagged": false,
  "flagged_reason": null,
  "history": [
    {
      "server_id": "01929381238",
      "server_name": "Tatum Games Community",
      "action_type": "warn",
      "behavior_category": "POOR_SPORTSMANSHIP",
      "weight": -1,
      "reason": "Complained excessively after loss",
      "reported_by": "moderator_id",
      "timestamp": "2025-09-15T18:22:00Z"
    },
    {
      "server_id": "01929381238",
      "server_name": "Tatum Games Community",
      "action_type": "praise",
      "behavior_category": "EXCELLENT_TEAMMATE",
      "weight": 3,
      "reason": "Helped new players learn the game",
      "reported_by": "user_id_123",
      "timestamp": "2025-09-20T14:15:00Z"
    }
  ],
  "breakdown_by_server": [
    {
      "server_id": "01929381238",
      "server_name": "Tatum Games Community",
      "local_score": 98,
      "actions_count": 10
    }
  ]
}
```

### Status: 404 Not Found

```json
{
  "error": "User not found",
  "discord_id": "293488128372",
  "message": "No reputation data exists for this user. They may be new to the MIKROS network."
}
```

### Status: 401 Unauthorized

```json
{
  "error": "Unauthorized",
  "message": "Invalid or missing API key"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `discord_id` | string | User's Discord ID |
| `username` | string | User's current Discord username |
| `reputation_score` | integer | Overall reputation (0-200, default 100) |
| `tier` | string | Reputation tier: "Excellent", "Good Standing", "Caution", "Flagged" |
| `global_rank` | integer | User's rank among all MIKROS users |
| `total_actions` | integer | Total behavior reports |
| `positive_actions` | integer | Count of positive behaviors |
| `negative_actions` | integer | Count of negative behaviors |
| `servers_active` | integer | Number of servers user is tracked on |
| `last_updated` | ISO 8601 string | When the score was last updated |
| `is_flagged` | boolean | Whether user is flagged for severe violations |
| `flagged_reason` | string | Reason for flagging (if applicable) |
| `history` | array | Full history of behavior reports (if requested) |
| `breakdown_by_server` | array | Per-server reputation breakdown (if requested) |

---

## Authentication Method

**Bearer Token Authentication**

Include your bot's API key in the Authorization header:

```
Authorization: Bearer YOUR_API_KEY
```

API keys are generated in the MIKROS Bot Dashboard at:  
`https://dashboard.tatumgames.com/bots/api-keys`

---

## Rate Limiting

- **Rate Limit**: 100 requests per minute per bot
- **Burst Limit**: 10 requests per second

Rate limit headers are included in responses:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1696723200
```

---

## Scalability and Security Notes

### Scalability
- API uses caching with 5-minute TTL to reduce database load
- Batch endpoints available for checking multiple users at once
- CDN-backed for global low-latency access
- Horizontal scaling with load balancing

### Security
- All requests must use HTTPS
- API keys are tied to specific bot IDs
- Rate limiting prevents abuse
- Audit logs track all API access
- User data is encrypted at rest and in transit
- GDPR-compliant with data deletion endpoints

### Privacy
- Users can request their data via `/score` command
- Server owners can opt out of global tracking
- Sensitive notes are only visible to moderators
- Public API excludes private moderation notes

---

## Future Extensibility Ideas

1. **WebSocket API**: Real-time reputation updates
2. **Batch Operations**: Check multiple users in one request
3. **Webhooks**: Get notified when a flagged user joins your server
4. **Analytics Dashboard**: Visualize reputation trends over time
5. **Appeal System**: Allow users to contest flagged status
6. **Trust Score**: Additional metric based on account age and verification
7. **Integration**: Connect with other anti-abuse services
8. **Machine Learning**: Predict problematic behavior patterns
9. **Reputation Decay**: Older negative actions count less over time
10. **Server Reputation**: Aggregate scores for entire communities

---

## Example Implementation (Discord Bot)

```java
public int getGlobalReputation(String userId) {
    try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tatumgames.com/reputation-score/" + userId))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            return json.get("reputation_score").getAsInt();
        } else if (response.statusCode() == 404) {
            // User not found - new user
            return 100; // Default score
        }
        
        logger.error("Failed to fetch reputation: HTTP {}", response.statusCode());
        return -1;
    } catch (Exception e) {
        logger.error("Error fetching reputation score", e);
        return -1;
    }
}
```

---

**Version**: 1.0  
**Last Updated**: October 7, 2025  
**Status**: 📋 Specification Complete - Implementation Pending

