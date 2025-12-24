# Promo Commands Documentation

## Overview

The **Promo Commands** manage the smart promotional detection system. This system passively detects
launch-related phrases in messages and offers promotional assistance.

**Commands:**

- `/admin-promotion-setup` - Initial setup for promotion channel
- `/admin-promotion-config` - Configure promotion settings (subcommands: view, update-channel, set-verbosity, disable, force-check)

---

## Admin Commands

### `/admin-promotion-setup`

**Purpose:** Initial setup for game promotion system.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**

```
/admin-promotion-setup channel:<#channel>
```

**Parameters:**

- `channel` (required): Channel for promotion posts

**Behavior:**

- Configures promotion channel
- Validates bot permissions
- Stores per-server configuration
- Enables promotion system

**Example:**

```
/admin-promotion-setup channel:#promotions
```

**Output:**

```
✅ Promotion channel configured!

Promotions will now be posted in #promotions.

Next Steps:
• Use `/admin-promotion-config` to customize settings
```

---

### `/admin-promotion-config`

**Purpose:** Configure game promotion settings.

**Permission Required:** `ADMINISTRATOR`

**Subcommands:**

- `view` - View current promotion configuration
- `update-channel` - Update the promotion channel (requires setup first)
- `set-verbosity` - Set how often promotions are posted (QUIET/NORMAL/VERBOSE)
- `disable` - Disable promotional detection
- `force-check` - Manually trigger a promotion check

**Verbosity Options:**

- `QUIET` - Every 24 hours
- `NORMAL` - Every 12 hours
- `VERBOSE` - Every 6 hours

**Examples:**

```
/admin-promotion-config view
/admin-promotion-config update-channel channel:#new-promotions
/admin-promotion-config set-verbosity level:NORMAL
/admin-promotion-config disable
/admin-promotion-config force-check
```

---


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

**Channel Setup:**

- Set via `/admin-promotion-setup`
- Required before promotions can be posted

**Verbosity:**

- Set via `/admin-promotion-config set-verbosity`
- Options: QUIET (24h), NORMAL (12h), VERBOSE (6h)
- Default: NORMAL

**Enabled/Disabled:**

- Toggle via `/admin-promotion-config disable`
- Default: Enabled after channel setup

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

**Last Updated:** 2025-01-27  
**Admin Commands:** 
- `/admin-promotion-setup` - Channel setup
- `/admin-promotion-config` - Configuration (4 subcommands)
