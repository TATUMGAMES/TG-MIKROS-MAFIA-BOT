# Admin Commands Documentation

## Overview

All admin commands use the `admin-*` prefix and require appropriate Discord permissions. Admin commands are used for
server moderation, configuration, and management.

---

## Moderation Commands

### `/admin-warn`

**Purpose:** Issue a warning to a user without removing them from the server.

**Permission Required:** `MODERATE_MEMBERS`

**Syntax:**

```
/admin-warn user:<@user> reason:<string>
```

**Parameters:**

- `user` (required): The user to warn
- `reason` (required): The reason for the warning

**Behavior:**

- Records warning in moderation log
- Sends confirmation message
- Validates permissions and target

**Example:**

```
/admin-warn user:@JohnDoe reason:Spamming in general chat
```

**Error Handling:**

- Permission denied: Ephemeral error message
- Invalid target: Clear feedback
- Execution errors: Logged with context

---

### `/admin-kick`

**Purpose:** Remove a user from the server (they can rejoin with a new invite).

**Permission Required:** `KICK_MEMBERS`

**Syntax:**

```
/admin-kick user:<@user> reason:<string>
```

**Parameters:**

- `user` (required): The user to kick
- `reason` (required): The reason for the kick

**Behavior:**

- Removes user from server
- Records kick in moderation log
- Logs reason in Discord audit log
- Validates role hierarchy

**Example:**

```
/admin-kick user:@JohnDoe reason:Repeated rule violations
```

**Error Handling:**

- Permission denied: Ephemeral error message
- Role hierarchy: Clear feedback if cannot interact
- Execution errors: Logged with context

---

### `/admin-ban`

**Purpose:** Permanently ban a user from the server.

**Permission Required:** `BAN_MEMBERS`

**Syntax:**

```
/admin-ban user:<@user> reason:<string> [delete_days:<0-7>]
```

**Parameters:**

- `user` (required): The user to ban
- `reason` (required): The reason for the ban
- `delete_days` (optional): Days of messages to delete (0-7, default: 0)

**Behavior:**

- Bans user from server
- Optionally deletes messages
- Records ban in moderation log
- Logs reason in Discord audit log
- Validates role hierarchy

**Example:**

```
/admin-ban user:@JohnDoe reason:Severe violations delete_days:7
```

**Error Handling:**

- Permission denied: Ephemeral error message
- Role hierarchy: Clear feedback if cannot interact
- Invalid delete_days: Validation error
- Execution errors: Logged with context

---

## Bot Detection Commands

### `/admin-bot-detection-setup`

**Purpose:** Initial setup for bot detection system.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**

```
/admin-bot-detection-setup enabled:<true/false>
```

**Parameters:**

- `enabled` (required): Enable or disable bot detection (true/false)

**Behavior:**

- Enables or disables bot detection for the server
- Creates default configuration if not already set
- Shows current configuration after setup

**Example:**

```
/admin-bot-detection-setup enabled:true
```

---

### `/admin-bot-detection-config`

**Purpose:** Configure bot detection settings.

**Permission Required:** `ADMINISTRATOR`

**Subcommands:**

- `view` - View current bot detection configuration
- `set-account-age-threshold` - Set account age threshold in days (1-365, default: 30)
- `set-link-restriction-minutes` - Set link restriction time after joining (1-1440, default: 20)
- `set-multi-channel-threshold` - Set multi-channel spam threshold (2-10, default: 3)
- `set-auto-action` - Set automatic action when bot detected (NONE, DELETE, WARN, MUTE, KICK, default: DELETE)
- `toggle-reputation-reporting` - Enable/disable auto-reporting to reputation system (default: true)
- `add-suspicious-domain` - Manually add a suspicious domain with risk score (1-10)
- `remove-suspicious-domain` - Remove a domain from suspicious list

**Detection Methods:**

1. **Account Age + Link:** Account < threshold days old AND message contains link → HIGH confidence
2. **Multi-Channel Spam:** Same message in ≥threshold channels within time window → HIGH confidence
3. **Join + Link:** Joined < time window seconds ago AND message contains link → HIGH confidence
4. **Suspicious Domain:** Message contains known suspicious TLD or URL shortener → MEDIUM confidence
5. **Dynamic Domain:** Domain in dynamic list with risk score ≥ 3 → MEDIUM confidence

**Auto Actions:**

- `NONE` - No action, only log detection
- `DELETE` - Delete message and warn user (default)
- `WARN` - Send warning message to user
- `MUTE` - Timeout user for 1 hour
- `KICK` - Remove user from server

**Reputation Integration:**

- Detected bots are automatically reported using `BehaviorCategory.SPAMMER`
- Reports appear in `/history` command
- Reputation scores visible in `/lookup` command
- Bot prevention count displayed in `/server-stats`

**Example:**

```
/admin-bot-detection-config view
/admin-bot-detection-config set-account-age-threshold days:30
/admin-bot-detection-config set-link-restriction-minutes minutes:20
/admin-bot-detection-config set-multi-channel-threshold threshold:3
/admin-bot-detection-config set-auto-action action:DELETE
/admin-bot-detection-config toggle-reputation-reporting enabled:true
/admin-bot-detection-config add-suspicious-domain domain:example.ru risk-score:5
/admin-bot-detection-config remove-suspicious-domain domain:example.ru
```

---

## Configuration Commands

### `/admin-scramble-setup`

**Purpose:** Initial setup for Word Unscramble game.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**

```
/admin-scramble-setup channel:<#channel> [reset_hour:<0-23>]
```

**Parameters:**

- `channel` (required): Channel for daily games
- `reset_hour` (optional): Daily reset hour UTC (0-23, default: 0)

**Behavior:**

- Configures game channel
- Sets daily reset time
- Enables Word Unscramble game
- Posts first game immediately

**Example:**

```
/admin-scramble-setup channel:#games reset_hour:0
```

---

### `/admin-scramble-config`

**Purpose:** Configure Word Unscramble game settings.

**Permission Required:** `ADMINISTRATOR`

**Subcommands:**

- `view` - View current configuration
- `update-channel` - Update the game channel (requires setup first)
- `set-reset-time` - Change daily reset hour
- `enable-game` - Enable Word Unscramble game
- `disable-game` - Disable Word Unscramble game

**Example:**

```
/admin-scramble-config view
/admin-scramble-config update-channel channel:#new-games
/admin-scramble-config enable-game
```

---

### `/admin-rpg-setup`

**Purpose:** Initial setup for RPG system.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**

```
/admin-rpg-setup channel:<#channel>
```

**Parameters:**

- `channel` (required): Channel for RPG commands and boss spawns

**Behavior:**

- Configures RPG channel
- Enables RPG system
- Sets up initial configuration

**Example:**

```
/admin-rpg-setup channel:#rpg
```

---

### `/admin-rpg-config`

**Purpose:** Configure RPG system settings.

**Permission Required:** `ADMINISTRATOR`

**Subcommands:**

- `view` - View current configuration
- `toggle` - Enable/disable RPG system
- `update-channel` - Update RPG channel (requires setup first)
- `set-charge-refresh` - Set charge refresh period (hours, default: 12)
- `set-xp-multiplier` - Set XP multiplier (0.5x - 2.0x)

**Example:**

```
/admin-rpg-config view
/admin-rpg-config toggle enabled:true
/admin-rpg-config set-cooldown hours:12
```

---

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

**Example:**

```
/admin-promotion-config view
/admin-promotion-config update-channel channel:#new-promotions
/admin-promotion-config set-verbosity level:NORMAL
/admin-promotion-config disable
/admin-promotion-config force-check
```

---

### `/admin-set-promo-frequency`

**Purpose:** Set promotional prompt cooldown.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**

```
/admin-set-promo-frequency days:<1-30>
```

**Parameters:**

- `days` (required): Cooldown in days (1-30, default: 7)

**Behavior:**

- Sets how often users can receive promotional prompts
- Prevents spam

**Example:**

```
/admin-set-promo-frequency days:14
```

---

## Honeypot Commands

### `/honeypot`

**Purpose:** Manage honeypot system for spam detection.

**Permission Required:** `ADMINISTRATOR`

**Subcommands:**

- `enable` - Enable honeypot and create channel
- `disable` - Disable honeypot (optional channel deletion)
- `config` - View or modify configuration

**Example:**

```
/honeypot enable channel_name:do-not-post
/honeypot config setting:silent_mode value:false
```

---

### `/ban_and_remove_all_messages`

**Purpose:** Ban a user and delete all their messages.

**Permission Required:** `BAN_MEMBERS`

**Syntax:**

```
/ban_and_remove_all_messages user:<@user> reason:<string> [delete_days:<0-7|-1>]
```

**Parameters:**

- `user` (required): The user to ban
- `reason` (required): The reason for the ban
- `delete_days` (optional): Days to delete (0-7, or -1 for all)

**Behavior:**

- Bans user
- Deletes all user messages across all channels
- Logs action
- Respects rate limits

**Example:**

```
/ban_and_remove_all_messages user:@SpamBot reason:Spamming delete_days:-1
```

---

### `/cleanup`

**Purpose:** Remove user messages without banning.

**Permission Required:** `MESSAGE_MANAGE`

**Syntax:**

```
/cleanup user:<@user> [days:<0-7|-1>]
```

**Parameters:**

- `user` (required): The user whose messages to remove
- `days` (optional): Days to look back (0-7, or -1 for all)

**Behavior:**

- Removes messages without banning
- Useful for spam cleanup
- Respects rate limits

**Example:**

```
/cleanup user:@Spammer days:7
```

---

### `/alert_channel`

**Purpose:** Set admin alert channel for bot notifications.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**

```
/alert_channel [channel:<#channel>]
```

**Parameters:**

- `channel` (optional): Channel for alerts (leave empty to clear)

**Behavior:**

- Sets alert channel for honeypot triggers
- Clears if no channel provided

**Example:**

```
/alert_channel channel:#admin-alerts
```

---

### `/list_bans`

**Purpose:** Display recent bans and reasons.

**Permission Required:** `MODERATE_MEMBERS`

**Syntax:**

```
/list_bans
```

**Behavior:**

- Shows last 20 bans
- Includes reason, moderator, timestamp
- Formatted embed display

**Example:**

```
/list_bans
```

---

## Permissions

### Permission Levels

| Permission         | Commands                                     |
|--------------------|----------------------------------------------|
| `MODERATE_MEMBERS` | `/admin-warn`, `/list_bans`                  |
| `KICK_MEMBERS`     | `/admin-kick`                                |
| `BAN_MEMBERS`      | `/admin-ban`, `/ban_and_remove_all_messages` |
| `MESSAGE_MANAGE`   | `/cleanup`                                   |
| `ADMINISTRATOR`    | All configuration commands                   |

### Permission Checks

All commands validate:

1. **User Permission:** Command user has required permission
2. **Bot Permission:** Bot has required permission
3. **Role Hierarchy:** For moderation commands, checks if moderator can interact with target
4. **Self-Moderation:** Prevents users from moderating themselves
5. **Bot Protection:** Prevents moderating bots

---

## Error Handling

### Common Errors

**Permission Denied:**

```
❌ You don't have permission to use this command.
```

**Role Hierarchy:**

```
❌ You cannot [action] this user due to role hierarchy.
```

**Invalid Target:**

```
❌ You cannot [action] a bot.
❌ You cannot [action] yourself.
```

**Validation Errors:**

```
❌ [Field] must be between [min] and [max].
```

### Error Logging

All errors are:

- Logged to application logs with full context
- Sent to user as ephemeral messages (private)
- Include actionable feedback

---

## Moderation Logging

All moderation actions are logged:

- **Application Logs:** Detailed logs with context
- **Moderation Service:** In-memory storage (TODO: database)
- **Discord Audit Log:** Native Discord logging (for kick/ban)

**Log Format:**

```
[timestamp] User {userId} {action} by {moderatorId} in guild {guildId}: {reason}
```

---

## Reputation Integration

**Status:** ✅ **Implemented**

Reputation system:

- ✅ Admin-only `/report` and `/praise` commands
- ✅ `/lookup` command for checking user scores
- ✅ API integration with `/trackPlayerRating` and `/getUserScoreDetail`
- ✅ Server-side reputation calculation and storage

**Note:** Warn/Kick/Ban commands are separate from reputation and do not affect reputation scores. They use Discord's
native moderation features.

---

## Best Practices

1. **Always Provide Reasons:** Clear reasons help with accountability
2. **Respect Role Hierarchy:** Bot role must be above target users
4. **Use Appropriate Commands:** Choose the right tool for the situation
5. **Document Actions:** Clear reasons make moderation history useful

---

## Auto-Bump Commands

### `/admin-bump-setup`

**Purpose:** Set up automatic server bumping for advertising bots (Disboard, Disurl).

**Permission Required:** `ADMINISTRATOR`

**Syntax:**

```
/admin-bump-setup channel:<#channel> bots:<disboard|disurl|both>
```

**Parameters:**

- `channel` (required): The text channel where bump commands will be sent
- `bots` (required): Which bots to bump - "disboard", "disurl", or "both"

**Behavior:**

- Configures the bump channel for the server
- Sets which external bots to bump
- Default interval: 4 hours
- Automatically starts bumping at the configured interval

**Example:**

```
/admin-bump-setup channel:#server-promo bots:both
```

**Error Handling:**

- Permission denied: Ephemeral error message
- Invalid channel: Clear feedback
- Bot not present: Gracefully skipped (logged)

---

### `/admin-bump-config`

**Purpose:** Configure auto-bump settings for your server.

**Permission Required:** `ADMINISTRATOR`

**Subcommands:**

#### `view`

View current bump configuration.

**Example:**

```
/admin-bump-config view
```

**Displays:**

- Status (enabled/disabled)
- Bump channel
- Enabled bots
- Bump interval
- Last bump times per bot

#### `set-interval`

Set the bump interval (1-24 hours).

**Syntax:**

```
/admin-bump-config set-interval interval:<1-24>
```

**Example:**

```
/admin-bump-config set-interval interval:6
```

**Behavior:**

- Updates the bump interval for the server
- Minimum: 1 hour
- Maximum: 24 hours
- Default: 4 hours

#### `update-bots`

Update which bots to bump.

**Syntax:**

```
/admin-bump-config update-bots bots:<disboard|disurl|both>
```

**Example:**

```
/admin-bump-config update-bots bots:disboard
```

**Behavior:**

- Updates the enabled bots list
- Options: "disboard", "disurl", or "both"

#### `disable`

Disable auto-bump for the server.

**Example:**

```
/admin-bump-config disable
```

**Behavior:**

- Clears all bump configuration
- Stops automatic bumping
- Removes all tracking data

**Error Handling:**

- Not set up: Prompts to use `/admin-bump-setup` first
- Invalid interval: Must be 1-24 hours

---

### `/bump-stats`

**Purpose:** View server bump statistics and history.

**Permission Required:** None (everyone can use)

**Example:**

```
/bump-stats
```

**Displays:**

- **Overall Statistics:**
  - Total bumps (all time)
  - Bumps this month
  - Bumps this week

- **Per-Bot Statistics:**
  - Bump count per bot (Disboard, Disurl)
  - Last bump time per bot

- **Recent Bumps:**
  - Last 5 bumps with timestamps
  - Shows which bot was bumped and when

**Features:**

- Ephemeral reply (only visible to you)
- Tracks all successful bumps automatically
- Shows monthly and weekly trends
- Helps track server promotion activity

**Note:** Bump detection automatically tracks when Disboard/Disurl bots confirm successful bumps. Statistics are calculated from detected bumps.
- Permission denied: Ephemeral error message

---

## Auto-Bump System Details

### Rate Limiting

The bot respects external bot rate limits:

- **Disboard:** Minimum 2 hours between bumps
- **Disurl:** Minimum 1 hour between bumps

The scheduler checks every 15 minutes and only bumps when:
- The configured interval has passed
- The external bot's minimum cooldown has passed
- The external bot is present in the server

### Safety Features

- ✅ Bot presence verification before bumping
- ✅ Rate limit tracking per bot per server
- ✅ Graceful error handling (bot offline, rate limited, etc.)
- ✅ Per-server independent configuration
- ✅ Admin-only access

---

**Last Updated:** 2025-12-24  
**Total Admin Commands:** 17+ commands





