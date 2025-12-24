# TASKS_02.md

## Objective

Implement a set of advanced moderation and analytics features that go beyond Discord’s built-in capabilities. Prioritize
features that can be implemented using available data in the Discord API. For features requiring external APIs, include
a `TODO` in the code and generate a detailed API documentation file inside the `/docs/` folder.

Always follow `BEST_CODING_PRACTICES.md`.

---

## Features to Implement Now (Code Only)

### 1. `/history <@user>`

- Show history of moderation actions (warns, kicks, bans)
- Include timestamps, reasons, action type (as enum)
- Store in memory for now (expandable to DB later)
- Add a placeholder for Reputation Score (to be fetched via API in the future)

### 2. Auto-Escalation System

- Track how many times a user has been warned
- After 3 warnings, auto-kick or mute
- Use configuration class or constants to control thresholds
- Add setting toggle per server (in-memory for now)

### 3. `/warn-suggestions`

- Analyze recent messages (last 100–200)
- Flag those containing:
    - Profanity
    - Toxic keywords
    - Slurs, threats
- Output:
    - User
    - Snippet of message
    - Link to the message
- Use simple keyword filtering (expandable to NLP later)
- Provide message link, user, message snippet in the response

### 4. `/ban-suggestions`

- Similar to `/warn-suggestions`, but stricter rules
- Look for mass pinging, spamming, flagged words
- Allow admin to review and take action
- Provide suggestions for manual review by moderators

### 5. Monthly Moderation Report (Automated)

- On the 1st of each month:
    - Send summary to server owner or mod-only channel
    - Include stats: # of warnings, kicks, bans, top offenders
- Create a basic scheduling mechanism (e.g., `TimerTask`)
- Add `TODO` to upgrade with persistence or cron-style config

### 6. `/server-stats`

- Return:
    - Total users
    - Number of active users this month (based on messages)
    - Total messages this month
    - Average messages per user
    - Most active text channels (by message count)
- Use in-memory counters or Discord audit data

### 7. `/top-contributors`

- Show top 10 users based on message count
- Track activity per user and rank by volume
- Use leaderboard style format

---

## Features Requiring External APIs

For each feature below:

1. Add a `TODO` in code
2. Create a markdown file in `/docs` folder with the name:  
   `/docs/API_FEATURE_NAME.md`
3. Each doc should contain:
    - **Feature Overview**
    - **Why API is needed**
    - **Request details** (URL, method, parameters)
    - **Sample request and response payloads**
    - **Authentication method (if any)**
    - **Scalability and security notes**
    - **Future extensibility ideas**

---

## External-API Features to Document

### 1. Reputation Score Integration

- Command: `/reputation <@user>`
- Description:
    - Fetches user’s **global reputation score**
    - Based on historical behavior from MIKROS-powered bots
- Also display Reputation Score in `/history`
- API Name: `reputation-score`
- TODO: Document how to query score by user ID or Discord tag

### 2. Reputation Score Reporting

- Commands: `/warn`, `/ban`, `/praise`, `/report`
- Description:
    - Use enums for behavior categories with **Fibonacci-weighted values**
    - Automatically report user behavior to MIKROS via API
- API Doc: `/docs/API_REPUTATION_SCORE_UPDATE.md`
- Method: `POST`

### 3. Cross-Server User Behavior

- Allow tracking of bans/warns across servers using the bot
- API Name: `global-user-moderation-log`
- Purpose: Server owners can see if a user has a history elsewhere

### 4. MIKROS Campaign Tracker

- If user posts “My game releases on Nov 5”, bot asks:
    - "Would you like a MIKROS promo code?"
    - If yes, collect email and call `campaign-discount API`
- Detect phrases like:
    - “My game releases Nov 5”

Bot replies:

- “Would you like a MIKROS promo code?”
- Collect email + send to MIKROS API

- API Name: `mikros-marketing-discount-offer`
- Requires NLP/message parsing and webhook for events (future phase)

### 5. Add a /docs/API_MIKROS_MARKETING_DISCOUNT_OFFER.md that includes:

Feature: MIKROS Campaign Message Detector

* Detects when a Discord user mentions their game’s launch/release.
* Uses Google Generative AI API (Gemini or PaLM) to understand message intent.
* If intent is confirmed, replies offering a promo code for MIKROS Marketing.
* Collects email, calls the appropriate MIKROS API.

❗️Do not attempt to implement this feature yet.
It relies on Google GenAI APIs for natural language understanding.
Instead, create a full API spec and message detection logic overview in
/docs/API_MIKROS_MARKETING_DISCOUNT_OFFER.md.

#### Behavior Category Enum Reference

```java
public enum BehaviorCategory {
    // NEGATIVE BEHAVIOR
    POOR_SPORTSMANSHIP(-1, "Poor Sportsmanship"),
    TROLLING(-2, "Trolling / Constant Pinging"),
    AFK_COMPLAINING(-3, "AFK / Complaining"),
    BAD_LANGUAGE_CHEATING(-5, "Bad Language / Cheating"),

    // POSITIVE BEHAVIOR
    GOOD_SPORTSMANSHIP(1, "Good Sportsmanship"),
    GREAT_LEADERSHIP(2, "Great Leadership"),
    EXCELLENT_TEAMMATE(3, "Excellent Teammate"),
    MVP(5, "MVP");

    public final int weight;
    public final String label;

    BehaviorCategory(int weight, String label) {
        this.weight = weight;
        this.label = label;
    }
}
```

Must integrate this enum into moderation commands as dropdown or numbered options (instead of free text reasons)

---

Here’s an example of what Cursor AI should auto-generate into /docs/API_REPUTATION_SCORE.md

# API: reputation-score

## Feature Overview

This API allows the Discord bot to retrieve a user's current **Reputation Score** based on their behavior across
multiple Discord servers using the MIKROS bot. The score is affected by warnings, bans, and kicks.

---

# API: reputation-score-update

## Purpose

Used to submit structured user behavior reports (positive or negative) tied to moderation commands issued in Discord via
the MIKROS bot.
This updates the user’s MIKROS Reputation Score using weighted behavior categories.

---

## Request

**Method**: `POST`  
**URL**: `https://api.tatumgames.com/reputation-score`

### Body Parameters

| Name                | Type            | Description                                  |
|---------------------|-----------------|----------------------------------------------|
| `discord_id`        | string          | ID of the user being scored                  |
| `server_id`         | string          | ID of the server this report originates from |
| `reported_by_id`    | string          | Moderator or user who submitted the report   |
| `behavior_category` | string          | Enum value (e.g., `POOR_SPORTSMANSHIP`)      |
| `weight`            | int             | Score weight (based on enum)                 |
| `notes`             | string          | Optional additional context                  |
| `timestamp`         | ISO 8601 string | UTC datetime of report                       |

### Example JSON

```json
{
  "discord_id": "293488128372",
  "server_id": "01929381238",
  "reported_by_id": "1029381837",
  "behavior_category": "TROLLING",
  "weight": -2,
  "notes": "Spammed @everyone 3 times in a row.",
  "timestamp": "2025-10-06T22:00:00Z"
}
```

## Response

**Status: 200 OK**

Example JSON:

```
{
"discord_id": "72930183918",
"reputation_score": 82,
"history": [
{ "type": "warn", "reason": "spam", "timestamp": "2025-09-01" },
{ "type": "ban", "reason": "hate speech", "timestamp": "2025-08-20" }
],
"global_rank": 1204,
"is_flagged": true
}
```

---

- Modify `/warn`, `/ban`, `/report`, `/praise` commands to use behavior enums
- Apply the `BehaviorCategory` logic
- In each command:
    - Log behavior locally
    - Add `TODO` to call `reputation-score-update` API
- Create `/docs/API_REPUTATION_SCORE_UPDATE.md` with full schema (as above)

---

## Optional Features Using Same System Later

| Command                      | Description                                                  |
|------------------------------|--------------------------------------------------------------|
| `/praise <@user> <behavior>` | Boosts rep score for helpful/good behavior                   |
| `/report <@user> <behavior>` | Non-admin users submit behavior reports                      |
| `/score <@user>`             | Check current MIKROS Reputation Score (calls `GET` endpoint) |

* All code must include `TODO` comments where API calls would occur.
* All API-dependent features must have a companion doc in `/docs`.