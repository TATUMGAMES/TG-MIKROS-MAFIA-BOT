# API: Reputation Score Update (POST)

## Purpose

Used to submit structured user behavior reports (positive or negative) tied to moderation commands issued in Discord via the MIKROS bot. This updates the user's MIKROS Reputation Score using weighted behavior categories based on Fibonacci values.

---

## Feature Overview

This API endpoint allows Discord bots to report user behavior to the central MIKROS reputation system. Each behavior is categorized and weighted, contributing to the user's global reputation score across all servers in the MIKROS network.

## Why API is Needed

- **Centralized Tracking**: Aggregates behavior data across multiple Discord servers
- **Consistent Scoring**: Uses standardized weighted categories for fair assessment
- **Network-Wide Moderation**: Helps identify patterns across communities
- **Data Integrity**: Single source of truth for reputation calculations
- **Real-Time Updates**: Immediate impact on user scores
- **Audit Trail**: Complete history of all reported behaviors

---

## Request Details

**Method**: `POST`  
**URL**: `https://api.tatumgames.com/reputation-score`

### Headers

```
Authorization: Bearer YOUR_API_KEY
Content-Type: application/json
```

### Body Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `discord_id` | string | Yes | Discord ID of the user being reported |
| `server_id` | string | Yes | Discord ID of the server where behavior occurred |
| `server_name` | string | No | Name of the server (for display purposes) |
| `reported_by_id` | string | Yes | Discord ID of the reporter/moderator |
| `reported_by_name` | string | No | Username of the reporter |
| `behavior_category` | string | Yes | Enum value (see Behavior Categories below) |
| `weight` | integer | Yes | Score weight based on category (-5 to +5) |
| `notes` | string | No | Additional context or details |
| `timestamp` | string | Yes | ISO 8601 UTC datetime of the behavior |
| `action_type` | string | No | Type of action: "warn", "kick", "ban", "praise", "report" |
| `message_link` | string | No | Discord message link if applicable |

---

## Behavior Categories (Enum)

### Negative Behaviors (Fibonacci-weighted)

| Category | Enum Value | Weight | Description |
|----------|-----------|--------|-------------|
| Poor Sportsmanship | `POOR_SPORTSMANSHIP` | -1 | Unsportsmanlike conduct |
| Trolling / Constant Pinging | `TROLLING` | -2 | Deliberately annoying others |
| AFK / Complaining | `AFK_COMPLAINING` | -3 | Frequently AFK or excessive complaining |
| Bad Language / Cheating | `BAD_LANGUAGE_CHEATING` | -5 | Profanity, slurs, or cheating |

### Positive Behaviors (Fibonacci-weighted)

| Category | Enum Value | Weight | Description |
|----------|-----------|--------|-------------|
| Good Sportsmanship | `GOOD_SPORTSMANSHIP` | +1 | Positive attitude and fair play |
| Great Leadership | `GREAT_LEADERSHIP` | +2 | Shows leadership qualities |
| Excellent Teammate | `EXCELLENT_TEAMMATE` | +3 | Supportive and cooperative |
| MVP | `MVP` | +5 | Exceptional contribution |

---

## Sample Request

### Example: Reporting Trolling Behavior

```json
{
  "discord_id": "293488128372",
  "server_id": "01929381238",
  "server_name": "Tatum Games Community",
  "reported_by_id": "1029381837",
  "reported_by_name": "ModeratorJoe",
  "behavior_category": "TROLLING",
  "weight": -2,
  "notes": "Spammed @everyone 3 times in a row despite warnings.",
  "timestamp": "2025-10-07T22:00:00Z",
  "action_type": "warn",
  "message_link": "https://discord.com/channels/01929381238/123456789/987654321"
}
```

### Example: Praising Good Behavior

```json
{
  "discord_id": "987654321",
  "server_id": "01929381238",
  "server_name": "Tatum Games Community",
  "reported_by_id": "1029381837",
  "reported_by_name": "CommunityMember",
  "behavior_category": "EXCELLENT_TEAMMATE",
  "weight": 3,
  "notes": "Spent 2 hours helping new players learn game mechanics.",
  "timestamp": "2025-10-07T20:30:00Z",
  "action_type": "praise"
}
```

---

## Sample Response

### Status: 200 OK (Success)

```json
{
  "success": true,
  "message": "Behavior report recorded successfully",
  "discord_id": "293488128372",
  "reputation_score": 82,
  "previous_score": 84,
  "score_change": -2,
  "behavior_category": "TROLLING",
  "weight_applied": -2,
  "total_reports": 15,
  "global_rank": 12043,
  "rank_change": -150,
  "is_flagged": false,
  "tier": "Good Standing",
  "timestamp": "2025-10-07T22:00:01Z"
}
```

### Status: 201 Created (New User)

```json
{
  "success": true,
  "message": "New user profile created and behavior recorded",
  "discord_id": "293488128372",
  "reputation_score": 99,
  "previous_score": 100,
  "score_change": -1,
  "is_new_user": true,
  "tier": "Good Standing",
  "timestamp": "2025-10-07T22:00:01Z"
}
```

### Status: 400 Bad Request

```json
{
  "success": false,
  "error": "Invalid behavior category",
  "message": "behavior_category must be one of: POOR_SPORTSMANSHIP, TROLLING, AFK_COMPLAINING, BAD_LANGUAGE_CHEATING, GOOD_SPORTSMANSHIP, GREAT_LEADERSHIP, EXCELLENT_TEAMMATE, MVP",
  "provided_value": "INVALID_CATEGORY"
}
```

### Status: 401 Unauthorized

```json
{
  "success": false,
  "error": "Unauthorized",
  "message": "Invalid or missing API key"
}
```

### Status: 429 Too Many Requests

```json
{
  "success": false,
  "error": "Rate limit exceeded",
  "message": "You have exceeded the rate limit of 100 requests per minute",
  "retry_after": 45,
  "limit": 100,
  "remaining": 0,
  "reset_time": "2025-10-07T22:05:00Z"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `success` | boolean | Whether the operation succeeded |
| `message` | string | Human-readable status message |
| `discord_id` | string | User's Discord ID |
| `reputation_score` | integer | Updated reputation score |
| `previous_score` | integer | Score before this report |
| `score_change` | integer | Change in score (can be negative) |
| `behavior_category` | string | The behavior category that was reported |
| `weight_applied` | integer | The weight that was applied |
| `total_reports` | integer | Total number of reports for this user |
| `global_rank` | integer | User's rank among all MIKROS users |
| `rank_change` | integer | Change in rank (negative = dropped) |
| `is_flagged` | boolean | Whether user is now flagged |
| `tier` | string | Reputation tier after update |
| `is_new_user` | boolean | Whether this created a new user profile |
| `timestamp` | ISO 8601 string | When the update was processed |

---

## Authentication Method

**Bearer Token Authentication**

Include your bot's API key in the Authorization header:

```
Authorization: Bearer YOUR_API_KEY
```

Each bot has a unique API key tied to its Discord application ID. Keys can be:
- Generated in the MIKROS Bot Dashboard
- Rotated for security
- Revoked if compromised

---

## Rate Limiting

- **Standard Tier**: 100 requests/minute, 1,000 requests/hour
- **Premium Tier**: 500 requests/minute, 10,000 requests/hour
- **Burst Limit**: 10 requests/second

Headers included in all responses:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1696723200
X-RateLimit-Tier: standard
```

When rate limited:
- HTTP 429 status code
- `Retry-After` header indicates seconds to wait
- Requests are queued (premium tier only)

---

## Scalability and Security Notes

### Scalability
- **Asynchronous Processing**: Reports are queued for processing
- **Database Sharding**: User data distributed across multiple databases
- **Caching Layer**: Redis cache for frequently accessed scores
- **Load Balancing**: Requests distributed across multiple API servers
- **CDN**: Global edge locations for low latency
- **Auto-Scaling**: Horizontal scaling based on load

### Security
- **HTTPS Only**: All communication encrypted with TLS 1.3
- **API Key Validation**: Keys validated against whitelist
- **IP Whitelisting**: Optional IP restriction per key
- **Request Signing**: Optional HMAC signature verification
- **SQL Injection Prevention**: Parameterized queries
- **Rate Limiting**: Prevents abuse and DDoS
- **Audit Logging**: All requests logged with timestamps
- **Data Encryption**: User data encrypted at rest (AES-256)
- **GDPR Compliance**: Right to deletion, data export

### Data Validation
- Discord ID format validation (snowflake)
- Timestamp validation (not in future)
- Weight validation (must match category)
- Enum validation (valid behavior categories)
- String length limits (notes: 1000 chars)
- Duplicate detection (same report within 60 seconds)

---

## Idempotency

To prevent duplicate reports, the API supports idempotency keys:

```json
{
  "idempotency_key": "unique-key-123",
  "discord_id": "293488128372",
  ...
}
```

- Same idempotency key within 24 hours returns cached response
- Prevents accidental duplicate reports
- Key can be any unique string (UUID recommended)

---

## Webhooks (Future)

Subscribe to reputation events:

```json
{
  "webhook_url": "https://your-bot.com/webhooks/reputation",
  "events": ["score_changed", "user_flagged", "threshold_reached"],
  "server_id": "01929381238"
}
```

---

## Future Extensibility Ideas

1. **Batch Reporting**: Submit multiple reports in one request
2. **AI Moderation**: Automatic behavior categorization using NLP
3. **Appeal System**: Users can contest reports
4. **Weighted Time Decay**: Older reports count less
5. **Context Analysis**: Consider server rules and culture
6. **Pattern Detection**: Identify coordinated bad behavior
7. **Cross-Platform**: Extend to other platforms (Twitch, Steam)
8. **Reputation Badges**: Visual indicators in Discord profiles
9. **Automated Actions**: Trigger actions based on thresholds
10. **Machine Learning**: Predict future behavior patterns

---

## Example Implementation (Discord Bot)

```java
public boolean reportToExternalAPI(BehaviorReport report) {
    try {
        JsonObject payload = new JsonObject();
        payload.addProperty("discord_id", report.getTargetUserId());
        payload.addProperty("server_id", report.getGuildId());
        payload.addProperty("reported_by_id", report.getReporterId());
        payload.addProperty("behavior_category", report.getBehaviorCategory().name());
        payload.addProperty("weight", report.getBehaviorCategory().getWeight());
        payload.addProperty("notes", report.getNotes());
        payload.addProperty("timestamp", report.getTimestamp().toString());
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tatumgames.com/reputation-score"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();
        
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            logger.info("Successfully reported behavior to MIKROS API");
            return true;
        } else {
            logger.error("Failed to report behavior: HTTP {}", response.statusCode());
            return false;
        }
    } catch (Exception e) {
        logger.error("Error reporting to MIKROS API", e);
        return false;
    }
}
```

---

## Error Handling Best Practices

```java
try {
    boolean success = reportToExternalAPI(report);
    if (!success) {
        // Log locally as fallback
        logger.warn("API report failed, storing locally only");
    }
} catch (Exception e) {
    // Never fail the bot command due to API issues
    logger.error("API communication error", e);
}
```

---

**Version**: 1.0  
**Last Updated**: October 7, 2025  
**Status**: 📋 Specification Complete - Implementation Pending  
**Integration Status**: TODOs placed in bot code for future implementation

