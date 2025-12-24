# TASKS_08.md

## Objective

Implement a smart, respectful, opt-in-based promotional system inside Discord to promote MIKROS services to developers
and studios based on trigger phrases and smart detection.

---

## Feature Summary: Passive Smart Lead Generator

Detect when users say phrases like:

- “We’re launching our game…”
- “Our Steam page is live”
- “Kickstarter is ending in 2 days”
- “Need help promoting”
- “We go live Oct 20”

And *gently prompt*:

> “🚀 Looks like you’re launching a game! Want help promoting with MIKROS? Type `/promo-help` to get a free promo code or
> speak with a partner.”

---

## Technical Requirements

### 🔹 NLP-Powered Detection (TODO)

- Use Regex as fallback
- `TODO`: Integrate Google Generative AI API for NLP message classification
- If message matches a “launch” pattern:
    - Send opt-in message (in public or DM)
    - Avoid duplicates per user/session

---

### 🔹 Command: `/promo-help`

Allows user to request personalized promo help:

- Bot sends DM with:
    - A custom message (e.g. “Tell us more!”)
    - Option to submit email (if needed)
- Add `TODO` to submit to lead-capture API

---

### 🔹 Admin Controls

| Command                | Description                                    |
|------------------------|------------------------------------------------|
| `/setup-promotions`    | Enable/disable smart promo detection           |
| `/set-promo-frequency` | Set cooldown (e.g. 1 prompt per user per week) |

---

### 🔹 TODO: Create API Docs in `/docs`

When AI-based detection or lead submission is needed, Cursor AI must:

- Add a `TODO` in the code
- Generate an API request doc such as:

#### `/docs/API_MIKROS_PROMO_SUBMISSION.md`

```markdown
# API: mikros-promo-submission

## Overview
Submits a Discord user’s lead details when they request marketing help.

## Endpoint
POST https://api.tatumgames.com/promo-lead

## Parameters

| Field | Type | Description |
|-------|------|-------------|
| discord_id | string | Discord ID of user |
| username | string | Their username |
| server_id | string | Source server |
| campaign_interest | string | "Game Launch", "Beta Promo", etc |
| email | string | Optional |
| timestamp | ISO8601 | Time of request |

## Example
```json
{
  "discord_id": "123456789",
  "username": "devguy#9999",
  "server_id": "987654321",
  "campaign_interest": "Game Launch",
  "email": "dev@example.com",
  "timestamp": "2025-10-06T23:00:00Z"
}


---

## ✳️ Future Enhancements

- Connect `/promo-help` to generate unique MIKROS promo codes via backend
- Integrate with CRM like Hubspot (via middleware)
- Track leads by Discord server + tags

---

> Reminder: All NLP/AI use should include `TODO` markers and `/docs` files to help build the backend support later.
