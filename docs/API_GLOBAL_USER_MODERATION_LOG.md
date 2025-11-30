# API: Global User Moderation Log

## Feature Overview

This API provides access to cross-server moderation history for users across the entire MIKROS bot network. Server owners and moderators can query whether a user has been warned, kicked, or banned in other servers, enabling informed moderation decisions.

## Why API is Needed

- **Informed Decisions**: Moderators can see if a new member has a history of violations elsewhere
- **Prevent Server Hopping**: Bad actors who get banned often join new servers; this helps identify them
- **Context Awareness**: Understanding a user's behavior pattern across communities
- **Privacy Balanced**: Provides essential moderation data while respecting user privacy
- **Network Protection**: Helps the entire MIKROS community maintain standards
- **Early Warning System**: Identify potential problems before they escalate

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/moderation-log/user/{discord_id}`

### Path Parameters

| Name | Type | Description |
|------|------|-------------|
| `discord_id` | string | Discord user ID (snowflake) |

### Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `server_id` | string | No | Filter by specific server |
| `action_type` | string | No | Filter by action: "warn", "kick", "ban", "all" (default: "all") |
| `severity` | string | No | Filter by severity: "low", "medium", "high", "critical" |
| `days` | integer | No | Limit to actions within last N days (default: 365) |
| `limit` | integer | No | Maximum number of results (default: 50, max: 500) |
| `include_server_names` | boolean | No | Include server names (default: false for privacy) |

---

## Sample Request

```http
GET /moderation-log/user/293488128372?action_type=ban&days=180&limit=10 HTTP/1.1
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
  "username": "TroubleMaker99",
  "total_actions": 15,
  "servers_with_actions": 5,
  "first_action_date": "2025-01-15T10:00:00Z",
  "last_action_date": "2025-09-28T15:30:00Z",
  "summary": {
    "warns": 8,
    "kicks": 4,
    "bans": 3
  },
  "severity_breakdown": {
    "low": 3,
    "medium": 5,
    "high": 4,
    "critical": 3
  },
  "is_flagged": true,
  "flagged_reason": "Multiple bans for hate speech across network",
  "actions": [
    {
      "action_id": "act_abc123",
      "server_id": "01929381238",
      "server_name": null,
      "action_type": "ban",
      "behavior_category": "BAD_LANGUAGE_CHEATING",
      "severity": "critical",
      "reason": "Repeated use of hate speech after warnings",
      "moderator_id": "1029381837",
      "timestamp": "2025-09-28T15:30:00Z",
      "days_ago": 9
    },
    {
      "action_id": "act_def456",
      "server_id": "98765432109",
      "server_name": null,
      "action_type": "kick",
      "behavior_category": "TROLLING",
      "severity": "high",
      "reason": "Mass pinging and spam",
      "moderator_id": "5551234567",
      "timestamp": "2025-08-15T12:00:00Z",
      "days_ago": 53
    }
  ],
  "recommendation": "HIGH_RISK",
  "recommendation_reason": "User has 3 bans and multiple critical severity violations across different servers. Recommend close monitoring or preemptive restrictions.",
  "patterns_detected": [
    "repeat_offender",
    "escalating_behavior",
    "multiple_servers"
  ],
  "privacy_note": "Server names hidden to protect community privacy. Only action types, reasons, and timestamps are shown."
}
```

### Status: 404 Not Found

```json
{
  "discord_id": "293488128372",
  "message": "No moderation actions found for this user",
  "total_actions": 0,
  "servers_with_actions": 0,
  "recommendation": "NO_HISTORY",
  "is_new_user": true
}
```

### Status: 403 Forbidden

```json
{
  "error": "Access Denied",
  "message": "Your server has opted out of global moderation sharing",
  "server_id": "01929381238",
  "can_request": "contact_support"
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `discord_id` | string | User's Discord ID |
| `username` | string | User's current username |
| `total_actions` | integer | Total moderation actions across all servers |
| `servers_with_actions` | integer | Number of unique servers with actions |
| `first_action_date` | ISO 8601 string | Date of first recorded action |
| `last_action_date` | ISO 8601 string | Date of most recent action |
| `summary` | object | Count of each action type |
| `severity_breakdown` | object | Count by severity level |
| `is_flagged` | boolean | Whether user is globally flagged |
| `flagged_reason` | string | Reason for global flag |
| `actions` | array | List of moderation actions |
| `recommendation` | string | Risk level: "NO_HISTORY", "LOW_RISK", "MODERATE_RISK", "HIGH_RISK", "CRITICAL" |
| `recommendation_reason` | string | Explanation for the recommendation |
| `patterns_detected` | array | Behavioral patterns identified |
| `privacy_note` | string | Information about privacy protections |

### Action Object Fields

| Field | Type | Description |
|-------|------|-------------|
| `action_id` | string | Unique identifier for the action |
| `server_id` | string | Discord server ID (always provided) |
| `server_name` | string | Server name (null if privacy enabled) |
| `action_type` | string | "warn", "kick", or "ban" |
| `behavior_category` | string | Behavior category enum value |
| `severity` | string | Severity level of the action |
| `reason` | string | Moderator-provided reason |
| `moderator_id` | string | ID of moderator (hashed for privacy) |
| `timestamp` | ISO 8601 string | When action occurred |
| `days_ago` | integer | Days since the action |

---

## Authentication Method

**Bearer Token Authentication**

```
Authorization: Bearer YOUR_API_KEY
```

**Special Permissions Required:**
- Bot must be verified
- Bot must have at least 100 servers
- Server must have opted into global moderation sharing
- API key must have `moderation:read` scope

---

## Privacy & Ethics

### What Is Shared
✅ Action types (warn, kick, ban)  
✅ Behavior categories (TROLLING, etc.)  
✅ Severity levels  
✅ Timestamps  
✅ Anonymized moderator IDs  
✅ Patterns and recommendations  

### What Is NOT Shared
❌ Server names (unless opted in)  
❌ Specific message content  
❌ Personal information  
❌ Moderator names  
❌ Server invite links  
❌ Private moderation notes  

### User Rights (GDPR Compliant)
- Admins can lookup user data via `/lookup` command
- Users can request data deletion
- Users can appeal flagged status
- Servers can opt out of sharing
- Data retention: 2 years for active users

---

## Risk Recommendation System

### NO_HISTORY
- User has no moderation actions
- New to the MIKROS network
- No special considerations needed

### LOW_RISK
- 1-2 warnings only
- No kicks or bans
- Isolated incidents
- **Action**: Standard welcome procedures

### MODERATE_RISK
- Multiple warnings or 1 kick
- No critical violations
- Pattern across 2+ servers
- **Action**: Monitor activity, clarify rules

### HIGH_RISK
- 1+ bans or multiple kicks
- High severity violations
- Pattern of escalating behavior
- **Action**: Probation period, strict monitoring

### CRITICAL
- Multiple bans for severe violations
- Hate speech, harassment, threats
- Network-wide flagged status
- **Action**: Consider preemptive ban or severe restrictions

---

## Rate Limiting

- **Standard Tier**: 50 requests/minute
- **Verified Bot Tier**: 200 requests/minute
- **Burst Limit**: 5 requests/second

This API has stricter rate limits due to the sensitive nature of the data.

---

## Scalability and Security Notes

### Security Measures
- End-to-end encryption (TLS 1.3)
- API key validation with scope checking
- Audit logging of all access
- Anomaly detection for abuse
- Data anonymization (moderator IDs hashed)
- Server consent verification
- IP whitelisting available
- Two-factor authentication for key generation

### Data Protection
- Encrypted at rest (AES-256)
- Access logs retained for 90 days
- Regular security audits
- GDPR and CCPA compliant
- Data breach notification system
- Regular penetration testing

### Ethical Use
- No selling or sharing of data
- Transparent privacy policy
- User-friendly appeal process
- Regular review of flagged users
- Community oversight board (planned)

---

## Opt-In/Opt-Out

### Server Opt-In
Servers must explicitly opt into global moderation sharing:

```
/admin-config set global-moderation-sharing enabled
```

### Server Opt-Out
Servers can opt out at any time:

```
/admin-config set global-moderation-sharing disabled
```

When opted out:
- Your server's actions are not shared with other servers
- You cannot query other servers' actions
- Your server's actions are still visible to the flagged user
- Existing data is retained but not shared (per retention policy)

---

## Future Extensibility Ideas

1. **AI Pattern Recognition**: Detect coordinated attacks across servers
2. **Appeal System API**: Allow users to contest actions programmatically
3. **Trust Network**: Servers can form trust circles to share more detailed data
4. **Real-Time Alerts**: WebSocket notifications when flagged users join
5. **Reputation Badges**: Visual indicators of user standing
6. **Cross-Platform Integration**: Connect with other gaming platforms
7. **Community Notes**: Verified moderators can add context to actions
8. **Behavior Prediction**: ML models to predict future violations
9. **Automated Recommendations**: Suggest appropriate actions based on history
10. **Federation**: Allow custom moderation networks independent of MIKROS

---

## Example Implementation

```java
public String checkUserGlobalHistory(String userId) {
    try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tatumgames.com/moderation-log/user/" + userId + "?days=180"))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String recommendation = json.get("recommendation").getAsString();
            int totalBans = json.getAsJsonObject("summary").get("bans").getAsInt();
            
            if ("CRITICAL".equals(recommendation) || totalBans >= 2) {
                return "⚠️ **WARNING**: This user has been banned from " + totalBans + 
                       " other servers in the network. Exercise caution.";
            } else if ("HIGH_RISK".equals(recommendation)) {
                return "⚠️ This user has a history of moderation actions. Monitor closely.";
            }
        } else if (response.statusCode() == 404) {
            return "✅ No global moderation history found.";
        }
        
        return null;
    } catch (Exception e) {
        logger.error("Error checking global history", e);
        return null;
    }
}
```

---

**Version**: 1.0  
**Last Updated**: October 7, 2025  
**Status**: 📋 Specification Complete - Implementation Pending  
**Ethics Review**: ✅ Approved with privacy safeguards

