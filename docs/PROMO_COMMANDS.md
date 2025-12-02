# Promo Commands Documentation

## Overview

The **Promo Commands** (`admin-promo-*`) manage the smart promotional detection system. This system passively detects
launch-related phrases in messages and offers promotional assistance.

**Command Prefixes:**

- `admin-promo-*` - Admin configuration commands

---

## Admin Commands

### `/admin-setup-promotions`

**Purpose:** Enable or disable smart promotional detection.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**

```
/admin-setup-promotions enabled:<true|false>
```

**Parameters:**

- `enabled` (required): Enable or disable detection

**Behavior:**

- Enables/disables passive message monitoring
- Per-server configuration
- When enabled, bot monitors messages for launch-related phrases

**Example:**

```
/admin-setup-promotions enabled:true
```

**Output:**

```
✅ Promotional detection enabled for this server.

The bot will now detect launch-related phrases and offer assistance.
```

---

### `/admin-set-promo-frequency`

**Purpose:** Set how often users can receive promotional prompts.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**

```
/admin-set-promo-frequency days:<1-30>
```

**Parameters:**

- `days` (required): Cooldown in days (1-30, default: 7)

**Behavior:**

- Sets cooldown period between prompts
- Prevents spam and annoyance
- Per-user, per-server tracking

**Example:**

```
/admin-set-promo-frequency days:14
```

**Output:**

```
✅ Promotional prompt cooldown set to 14 days.

Users will receive prompts at most once every 14 days.
```

---

## Passive Detection

### How It Works

The bot passively monitors messages for launch-related phrases:

**Trigger Patterns:**

- "launching my game"
- "game release"
- "coming soon"
- "beta test"
- "early access"
- "kickstarter"
- "indiegogo"
- "steam release"
- "app store"
- "play store"

**Detection:**

- Case-insensitive regex matching
- 10 trigger patterns
- Monitors all text channels (when enabled)

**Response:**

- Sends gentle, opt-in prompt
- DM or channel delivery (configurable)
- Respects cooldown period

### Detection Example

**User Message:**

```
I'm launching my game next month! Excited to share it with everyone.
```

**Bot Response (if enabled and cooldown passed):**

```
🚀 Game Launch Detected!

I noticed you mentioned launching a game. Would you like help promoting it?

Contact MIKROS for promotional assistance!
```

---

## Configuration

### Per-Server Settings

**Enabled/Disabled:**

- Toggle via `/admin-setup-promotions`
- Default: Disabled (must be enabled by admin)

**Cooldown:**

- Set via `/admin-set-promo-frequency`
- Default: 7 days
- Range: 1-30 days

**Delivery Method:**

- Currently: DM or channel (configurable in future)
- Default: DM

---

## Behavior

### Detection Rules

1. **Message Monitoring:**
    - Only monitors guild messages (not DMs)
    - Skips bot messages
    - Case-insensitive matching

2. **Cooldown Enforcement:**
    - Tracks per-user, per-server
    - Prevents spam
    - Respects configured cooldown period

3. **Prompt Delivery:**
    - Gentle, non-intrusive
    - Opt-in approach
    - Clear value proposition

### Error Handling

**Detection Disabled:**

- No prompts sent
- Silent operation

**Cooldown Active:**

- No prompt sent
- Logged for debugging

---

## Use Cases

### For Server Admins

- Enable promotional assistance for community
- Control detection frequency
- Manage cooldown periods

### For Community

- Discover promotional opportunities
- Learn about game launches
- Support indie developers

---

## Future Enhancements

- 🔮 **NLP Integration:** Google Generative AI for better detection
- 🔮 **Campaign Types:** Automatic campaign type detection
- 🔮 **Analytics:** Track detection and conversion rates
- 🔮 **Custom Patterns:** Admin-defined trigger patterns
- 🔮 **Multi-Language:** Support for multiple languages

---

## Best Practices

### For Admins

1. **Enable Thoughtfully:** Only enable if community benefits
2. **Set Appropriate Cooldown:** Balance helpfulness with spam prevention
3. **Monitor Usage:** Check if prompts are well-received
4. **Adjust as Needed:** Modify cooldown based on feedback

---

**Last Updated:** 2025-10-08  
**Admin Commands:** 2 (`admin-promo-*`)
