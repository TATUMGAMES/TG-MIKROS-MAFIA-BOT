# Promo Commands Documentation

## Overview

The **Promo Commands** (`promo-*` and `admin-promo-*`) manage the smart promotional lead generation system. This system passively detects launch-related phrases in messages and offers promotional assistance.

**Command Prefixes:**
- `promo-*` - User-facing commands
- `admin-promo-*` - Admin configuration commands

---

## User Commands

### `/promo-help`

**Purpose:** Request promotional assistance for your game.

**Permission:** Everyone

**Syntax:**
```
/promo-help [campaign:<string>] [email:<string>]
```

**Parameters:**
- `campaign` (optional): Type of campaign (e.g., "Game Launch", "Beta Promo")
- `email` (optional): Your email address

**Behavior:**
- Provides information about MIKROS promotional services
- Explains how to get help promoting games
- Optional lead submission (TODO: API integration)

**Example:**
```
/promo-help campaign:Game Launch email:dev@example.com
```

**Output:**
```
🚀 MIKROS Promotional Assistance

We can help you promote your game launch!

Services:
• Game promotion scheduling
• Marketing campaigns
• Community outreach
• Analytics integration

Contact us for more information!
```

**Status:** ⚠️ Lead submission API integration TODO

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

Use /promo-help to learn about MIKROS promotional services!
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

**API Errors (Future):**
- Graceful degradation
- Logged for review

---

## Lead Submission

**Status:** ⚠️ **TODO**

**Planned Features:**
- Google Generative AI NLP integration
- API submission to MIKROS Marketing
- Lead tracking and management
- Campaign type detection

**API Documentation:**
- See `/docs/API_MIKROS_PROMO_SUBMISSION.md`
- See `/docs/API_GOOGLE_GENERATIVE_AI.md`

---

## Use Cases

### For Game Developers
- Get help promoting game launches
- Learn about marketing services
- Connect with MIKROS marketing team

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
- 🔮 **API Integration:** Submit leads to MIKROS Marketing API
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

### For Users
1. **Use `/promo-help`:** Direct way to request assistance
2. **Provide Details:** Include campaign type and email if comfortable
3. **Respect Cooldown:** Understand prompts are rate-limited

---

**Last Updated:** 2025-10-08  
**User Commands:** 1 (`promo-*`)  
**Admin Commands:** 2 (`admin-promo-*`)





