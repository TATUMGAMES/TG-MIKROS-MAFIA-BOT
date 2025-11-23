# Reputation System Documentation

## Overview

The **Reputation System** tracks user behavior and calculates reputation scores based on positive and negative actions. It provides a way to measure community standing and helps moderators identify problematic users.

**Status:** ✅ **Fully Functional** (Local reputation)  
**API Integration:** ⚠️ **TODO** (Global reputation)

---

## Commands

### `/praise`

**Purpose:** Praise a user for positive behavior.

**Permission:** Everyone

**Syntax:**
```
/praise user:<@user> behavior:<category> [notes:<string>]
```

**Parameters:**
- `user` (required): The user to praise
- `behavior` (required): Type of positive behavior (dropdown)
- `notes` (optional): Additional notes

**Positive Behaviors:**
- `GOOD_SPORTSMANSHIP` - Positive attitude and fair play (+1 point)
- `GREAT_LEADERSHIP` - Shows leadership and guides others (+2 points)
- `EXCELLENT_TEAMMATE` - Supportive and cooperative team player (+3 points)
- `MVP` - Exceptional contribution and behavior (+5 points)

**Example:**
```
/praise user:@JohnDoe behavior:EXCELLENT_TEAMMATE notes:Helped me debug my code
```

**Output:**
```
✨ Praise Recorded

User: @JohnDoe
Behavior: Excellent Teammate (+3 points)
Notes: Helped me debug my code
Reporter: @You
Local Reputation: 103
```

---

### `/report`

**Purpose:** Report a user for negative behavior.

**Permission:** Everyone

**Syntax:**
```
/report user:<@user> behavior:<category> [notes:<string>]
```

**Parameters:**
- `user` (required): The user to report
- `behavior` (required): Type of negative behavior (dropdown)
- `notes` (optional): Additional notes

**Negative Behaviors:**
- `POOR_SPORTSMANSHIP` - Unsportsmanlike conduct or attitude (-1 point)
- `TROLLING` - Deliberately annoying others or excessive mentions (-2 points)
- `AFK_COMPLAINING` - Frequently AFK or excessive complaining (-3 points)
- `BAD_LANGUAGE_CHEATING` - Profanity, slurs, or cheating (-5 points)

**Example:**
```
/report user:@BadUser behavior:TROLLING notes:Posting links repeatedly
```

**Output:**
```
🚨 Report Submitted

User: @BadUser
Behavior: Trolling / Constant Pinging (-2 points)
Notes: Posting links repeatedly
Reporter: @You
Local Reputation: 98

⚠️ This report has been recorded. Moderators will review if needed.
```

**Note:** Reports are sent as ephemeral (private) messages for privacy.

---

### `/score`

**Purpose:** Check a user's reputation score.

**Permission:** Everyone

**Syntax:**
```
/score [user:<@user>]
```

**Parameters:**
- `user` (optional): User to check (defaults to yourself)

**Example:**
```
/score
/score user:@JohnDoe
```

**Output:**
```
🎯 Reputation Score

Reputation for JohnDoe

📊 Local Reputation (This Server)
105 / 100
[██████████]

🌍 Global Reputation (MIKROS Network)
API not yet available
Local score is based on behavior in this server only.

✅ Positive Behaviors: 5
⚠️ Negative Behaviors: 2
📈 Total Reports: 7

📝 Interpretation
🌟 Excellent! This user is a model community member.
```

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
- `GOOD_SPORTSMANSHIP`: +1 point
- `GREAT_LEADERSHIP`: +2 points
- `EXCELLENT_TEAMMATE`: +3 points
- `MVP`: +5 points

**Negative Behaviors:**
- `POOR_SPORTSMANSHIP`: -1 point
- `TROLLING`: -2 points
- `AFK_COMPLAINING`: -3 points
- `BAD_LANGUAGE_CHEATING`: -5 points

### Score Calculation Formula

```
Score = 100 + Σ(behavior_weights)
Score = max(0, min(200, Score))
```

**Example:**
- Starting: 100
- +3 (EXCELLENT_TEAMMATE)
- +2 (GREAT_LEADERSHIP)
- -2 (TROLLING)
- = 103 points

---

## Behavior Categories

### Positive Behaviors

| Category | Points | Description |
|----------|--------|-------------|
| `GOOD_SPORTSMANSHIP` | +1 | Positive attitude and fair play |
| `GREAT_LEADERSHIP` | +2 | Shows leadership and guides others |
| `EXCELLENT_TEAMMATE` | +3 | Supportive and cooperative team player |
| `MVP` | +5 | Exceptional contribution and behavior |

### Negative Behaviors

| Category | Points | Description |
|----------|--------|-------------|
| `POOR_SPORTSMANSHIP` | -1 | Unsportsmanlike conduct or attitude |
| `TROLLING` | -2 | Deliberately annoying others or excessive mentions |
| `AFK_COMPLAINING` | -3 | Frequently AFK or excessive complaining |
| `BAD_LANGUAGE_CHEATING` | -5 | Profanity, slurs, or cheating |

---

## Integration with Moderation

### Moderation History

Reputation scores are displayed in `/admin-history`:
- Shows reputation score alongside moderation actions
- Provides context for moderation decisions
- Helps identify patterns

### Automatic Updates

**Status:** ⚠️ **TODO**

**Planned:**
- Moderation actions automatically affect reputation
- Warn: -2 points
- Kick: -5 points
- Ban: -10 points
- Integration with Tatum Games Reputation Score API

---

## Local vs Global Reputation

### Local Reputation

**Current:** ✅ **Implemented**
- Calculated per-server
- Based on behavior reports in that server
- Stored in-memory (TODO: database)

**Calculation:**
- Starts at 100 points
- Adjusted by behavior reports
- Bounded between 0-200

### Global Reputation

**Status:** ⚠️ **TODO**

**Planned:**
- Aggregated across all MIKROS servers
- API integration with Tatum Games
- Cross-server reputation tracking
- Persistent storage

**API Documentation:**
- See `/docs/API_REPUTATION_SCORE.md`
- See `/docs/API_REPUTATION_SCORE_UPDATE.md`

---

## Use Cases

### For Users
- **Track Standing:** See your reputation score
- **Recognize Good Behavior:** Praise helpful users
- **Report Issues:** Report negative behavior
- **Build Reputation:** Accumulate positive reports

### For Moderators
- **Identify Problem Users:** Low scores indicate issues
- **Context for Actions:** See behavior history
- **Track Improvements:** Monitor score changes
- **Make Informed Decisions:** Use scores in moderation

### For Admins
- **Community Health:** Monitor overall reputation trends
- **Identify Leaders:** High scores indicate valuable members
- **Prevent Issues:** Catch problems early with low scores

---

## Data Storage

### Current Implementation

**Storage:** In-memory (`ConcurrentHashMap`)
- Per-server isolation
- Thread-safe
- Fast access

**Limitations:**
- Data lost on restart
- No persistence
- No cross-server sharing

### Future Implementation

**Planned:**
- Database persistence
- Cross-server aggregation
- API integration
- Backup and restore

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

- 🔮 **API Integration:** Connect to Tatum Games Reputation Score API
- 🔮 **Automatic Moderation:** Actions affect reputation automatically
- 🔮 **Reputation Decay:** Scores decay over time
- 🔮 **Achievements:** Unlock achievements for high scores
- 🔮 **Leaderboards:** Top reputation scores
- 🔮 **Badges:** Visual badges for reputation tiers
- 🔮 **Notifications:** Notify on significant score changes

---

## Best Practices

### For Users
1. **Be Helpful:** Help others to build positive reputation
2. **Report Appropriately:** Only report genuine issues
3. **Check Scores:** Monitor your own reputation
4. **Build Trust:** Consistent positive behavior

### For Moderators
1. **Review Reports:** Check behavior reports regularly
2. **Use Context:** Consider reputation in moderation decisions
3. **Encourage Positive:** Praise good behavior
4. **Monitor Trends:** Watch for score patterns

---

**Last Updated:** 2025-10-08  
**Commands:** 3 (`praise`, `report`, `score`)  
**Status:** ✅ Local reputation functional, ⚠️ Global reputation TODO

