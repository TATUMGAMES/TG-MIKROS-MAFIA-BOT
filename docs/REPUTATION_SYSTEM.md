# Reputation System Documentation

## Overview

The **Reputation System** tracks user behavior and calculates reputation scores based on positive and negative actions. It provides a way to measure community standing and helps moderators identify problematic users.

**Status:** ✅ **Fully Functional** (API-integrated)  
**API Integration:** ✅ **Integrated** (Uses `/trackPlayerRating` and `/getUserScoreDetail` APIs)

---

## Commands

### `/praise`

**Purpose:** Praise a user for positive behavior.

**Permission:** Admin Only (MODERATE_MEMBERS)

**Syntax:**
```
/praise user:<@user> behavior:<category> [notes:<string>]
```

**Parameters:**
- `user` (required): The user to praise
- `behavior` (required): Type of positive behavior (dropdown)
- `notes` (optional): Additional notes

**Positive Behaviors:**
- `ACTIVE_PARTICIPATE` - Actively participating in community activities (+5 points)
- `GOOD_HELPER` - Helping other community members (+2 points)
- `POSITIVE_INFLUENCER` - Positively influencing the community (+3 points)
- `FRIENDLY_GREETER` - Welcoming new members in a friendly manner (+1 point)

**Example:**
```
/praise user:@JohnDoe behavior:EXCELLENT_TEAMMATE notes:Helped me debug my code
```

**Output:**
```
✨ Praise Recorded

User: @JohnDoe
Behavior: Positive Influencer (+3 points)
Notes: Helped me debug my code
Reporter: @You

✅ Praise has been recorded and sent to the reputation system.
```

---

### `/report`

**Purpose:** Report a user for negative behavior.

**Permission:** Admin Only (MODERATE_MEMBERS)

**Syntax:**
```
/report user:<@user> behavior:<category> [notes:<string>]
```

**Parameters:**
- `user` (required): The user to report
- `behavior` (required): Type of negative behavior (dropdown)
- `notes` (optional): Additional notes

**Negative Behaviors:**
- `SPAMMER` - Posting spam or unwanted content (-1 point)
- `TOXIC_BEHAVIOR` - Engaging in toxic or harmful behavior (-2 points)
- `HARRASSING` - Harassing or bullying other users (-3 points)
- `IGNORING_RULES` - Repeatedly ignoring server rules (-2 points)
- `BAN_EVASION` - Attempting to evade a ban (-5 points)
- `TROLL` - Trolling or deliberately annoying others (-3 points)
- `EXCESSIVE_PINGING` - Excessive use of mentions or pings (-3 points)
- `NSFW_IN_NON_NSFW_SPACE` - Posting NSFW content in inappropriate channels (-5 points)

**Example:**
```
/report user:@BadUser behavior:TROLLING notes:Posting links repeatedly
```

**Output:**
```
🚨 Report Submitted

User: @BadUser
Behavior: Troll (-3 points)
Notes: Posting links repeatedly
Reporter: @You

✅ Report has been recorded and sent to the reputation system.
```

**Note:** Reports are sent as ephemeral (private) messages for privacy.

---

### `/lookup`

**Purpose:** Lookup user reputation scores by username.

**Permission:** Admin Only (MODERATE_MEMBERS)

**Syntax:**
```
/lookup usernames:<username1> [username2] [username3] ...
```

**Parameters:**
- `usernames` (required): Comma or space-separated list of Discord usernames to lookup

**Example:**
```
/lookup usernames:drxeno02 usernameA usernameB
```

**Output:**
```
🔍 Reputation Score Lookup

Results for 3 user(s)

👤 drxeno02
Discord ID: 123456789012345678
Reputation Score: 0
Email: abc@gmail.com
Servers: 987654321098765432, 887654321098765432, ...

👤 usernameA
Discord ID: 023456789012345678
Reputation Score: 10
Email: xyz@gmail.com
Servers: 987654321098765432

⚠️ Not Found
The following usernames were not found in the system:
usernameZ
```

**Note:** This command calls the `/getUserScoreDetail` API to retrieve reputation information from the server database.

---

## Scoring System

### Score Calculation

**Starting Score:** 100 points (neutral)

**Score Range:** 0-200 points
- **0-79:** Poor reputation (concern)
- **80-99:** Below average (caution)
- **100:** Neutral (default)
- **101-119:** Good standing
- **120-200:** Excellent reputation

### Behavior Weights

**Positive Behaviors:**
- `ACTIVE_PARTICIPATE`: +5 points
- `GOOD_HELPER`: +2 points
- `POSITIVE_INFLUENCER`: +3 points
- `FRIENDLY_GREETER`: +1 point

**Negative Behaviors:**
- `SPAMMER`: -1 point
- `TOXIC_BEHAVIOR`: -2 points
- `HARRASSING`: -3 points
- `IGNORING_RULES`: -2 points
- `BAN_EVASION`: -5 points
- `TROLL`: -3 points
- `EXCESSIVE_PINGING`: -3 points
- `NSFW_IN_NON_NSFW_SPACE`: -5 points

### Score Calculation Formula

```
Score = 100 + Σ(behavior_weights)
Score = max(0, min(200, Score))
```

**Example:**
- Starting: 100
- +3 (POSITIVE_INFLUENCER)
- +2 (GOOD_HELPER)
- -3 (TROLL)
- = 102 points

---

## Behavior Categories

### Positive Behaviors

| Category | Points | Description |
|----------|--------|-------------|
| `ACTIVE_PARTICIPATE` | +5 | Actively participating in community activities |
| `GOOD_HELPER` | +2 | Helping other community members |
| `POSITIVE_INFLUENCER` | +3 | Positively influencing the community |
| `FRIENDLY_GREETER` | +1 | Welcoming new members in a friendly manner |

### Negative Behaviors

| Category | Points | Description |
|----------|--------|-------------|
| `SPAMMER` | -1 | Posting spam or unwanted content |
| `TOXIC_BEHAVIOR` | -2 | Engaging in toxic or harmful behavior |
| `HARRASSING` | -3 | Harassing or bullying other users |
| `IGNORING_RULES` | -2 | Repeatedly ignoring server rules |
| `BAN_EVASION` | -5 | Attempting to evade a ban |
| `TROLL` | -3 | Trolling or deliberately annoying others |
| `EXCESSIVE_PINGING` | -3 | Excessive use of mentions or pings |
| `NSFW_IN_NON_NSFW_SPACE` | -5 | Posting NSFW content in inappropriate channels |

---

## Integration with Moderation

### Automatic Updates

**Status:** ✅ **Implemented**

**Current:**
- All reputation calculations are handled by the server via API
- Reports are sent to `/trackPlayerRating` API endpoint
- Scores are retrieved from `/getUserScoreDetail` API endpoint
- All calculations and storage are server-side

**Note:** Warn/Kick/Ban commands are separate from reputation and do not affect reputation scores. They use Discord's native moderation features.

---

## API Integration

### Reputation Tracking

**Status:** ✅ **Implemented**

**API Endpoints:**
- `/trackPlayerRating` (POST) - Records behavior reports and updates reputation scores
- `/getUserScoreDetail` (GET) - Retrieves user reputation scores and server information

**How It Works:**
1. Admin uses `/report` or `/praise` command
2. Command creates a `BehaviorReport` object
3. Report is converted to `TrackPlayerRatingRequest` format
4. Request is sent to `/trackPlayerRating` API
5. Server processes the request and updates reputation scores
6. Admin receives confirmation of successful submission

**Score Lookup:**
1. Admin uses `/lookup` command with usernames
2. Command calls `/getUserScoreDetail` API
3. Server returns reputation scores and server information
4. Results are displayed in an embed format

**API Documentation:**
- See `/docs/API_REPUTATION_SCORE.md`
- See `/docs/API_REPUTATION_SCORE_UPDATE.md`

---

## Use Cases

### For Users
- **Track Standing:** See your reputation score using `/score` command
- **Build Reputation:** Accumulate positive reports from admins

### For Admins
- **Report Behavior:** Use `/report` to record negative behavior
- **Praise Behavior:** Use `/praise` to record positive behavior
- **Lookup Scores:** Use `/lookup` to check reputation scores for multiple users
- **Identify Problem Users:** Low scores indicate issues
- **Context for Actions:** See behavior history via API
- **Track Improvements:** Monitor score changes
- **Make Informed Decisions:** Use scores in moderation decisions
- **Community Health:** Monitor overall reputation trends
- **Identify Leaders:** High scores indicate valuable members
- **Prevent Issues:** Catch problems early with low scores

---

## Data Storage

### Current Implementation

**Storage:** Server-side via API
- All reputation data is stored on the server
- Data is persistent and survives bot restarts
- Cross-server reputation tracking
- Centralized database

**API Integration:**
- `/trackPlayerRating` - Stores behavior reports and updates scores
- `/getUserScoreDetail` - Retrieves reputation data from server database

**Local Caching:**
- Bot may cache some data temporarily for performance
- All authoritative data comes from the server API

---

## Privacy & Security

### Report Privacy

- **Reports:** Ephemeral (private) messages
- **Scores:** Public (anyone can check)
- **History:** Only visible to moderators

### Validation

- **Self-Reporting:** Prevented (cannot praise/report yourself)
- **Bot Protection:** Bots cannot be reported
- **Spam Prevention:** No rate limiting currently (TODO)

---

## Future Enhancements

- 🔮 **Reputation Decay:** Scores decay over time
- 🔮 **Achievements:** Unlock achievements for high scores
- 🔮 **Leaderboards:** Top reputation scores
- 🔮 **Badges:** Visual badges for reputation tiers
- 🔮 **Notifications:** Notify on significant score changes
- 🔮 **Batch Operations:** Report multiple users at once

---

## Best Practices

### For Users
1. **Be Helpful:** Help others to build positive reputation
2. **Build Trust:** Consistent positive behavior

### For Admins
1. **Report Appropriately:** Only report genuine issues using `/report`
2. **Praise Good Behavior:** Use `/lookup` to check scores, then `/praise` for positive behavior
3. **Use Context:** Consider reputation in moderation decisions
4. **Monitor Trends:** Use `/lookup` to check multiple users' scores
5. **Review Regularly:** Check behavior reports and scores regularly

---

**Last Updated:** 2025-01-27  
**Commands:** 3 (`praise`, `report`, `lookup`)  
**Status:** ✅ Fully functional with API integration

