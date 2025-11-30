# Admin Commands Documentation

## Overview

All admin commands use the `admin-*` prefix and require appropriate Discord permissions. Admin commands are used for server moderation, configuration, and management.

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

## Configuration Commands

### `/admin-game-setup`

**Purpose:** Initial setup for community games.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**
```
/admin-game-setup channel:<#channel> [reset_hour:<0-23>]
```

**Parameters:**
- `channel` (required): Channel for daily games
- `reset_hour` (optional): Daily reset hour UTC (0-23, default: 0)

**Behavior:**
- Configures game channel
- Sets daily reset time
- Enables all games by default
- Posts first game immediately

**Example:**
```
/admin-game-setup channel:#games reset_hour:0
```

---

### `/admin-game-config`

**Purpose:** Configure community game settings.

**Permission Required:** `ADMINISTRATOR`

**Subcommands:**
- `view` - View current configuration
- `set-channel` - Change game channel
- `set-reset-time` - Change daily reset hour
- `enable-game` - Enable a specific game
- `disable-game` - Disable a specific game

**Example:**
```
/admin-game-config view
/admin-game-config set-channel channel:#new-games
/admin-game-config enable-game game:SCRAMBLE
```

---

### `/admin-rpg-config`

**Purpose:** Configure RPG system settings.

**Permission Required:** `ADMINISTRATOR`

**Subcommands:**
- `view` - View current configuration
- `toggle` - Enable/disable RPG system
- `set-channel` - Restrict RPG to specific channel
- `set-cooldown` - Set action cooldown (hours)
- `set-xp-multiplier` - Set XP multiplier (0.5x - 2.0x)

**Example:**
```
/admin-rpg-config view
/admin-rpg-config toggle enabled:true
/admin-rpg-config set-cooldown hours:12
```

---

### `/admin-setup-promotion-channel`

**Purpose:** Set the channel for game promotion posts.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**
```
/admin-setup-promotion-channel channel:<#channel>
```

**Parameters:**
- `channel` (required): Channel for promotion posts

**Behavior:**
- Configures promotion channel
- Validates bot permissions
- Stores per-server configuration

**Example:**
```
/admin-setup-promotion-channel channel:#promotions
```

---

### `/admin-set-promotion-verbosity`

**Purpose:** Set how often game promotions are posted.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**
```
/admin-set-promotion-verbosity verbosity:<QUIET|NORMAL|VERBOSE>
```

**Options:**
- `QUIET` - Every 24 hours
- `NORMAL` - Every 12 hours
- `VERBOSE` - Every 6 hours

**Example:**
```
/admin-set-promotion-verbosity verbosity:NORMAL
```

---

### `/admin-force-promotion-check`

**Purpose:** Manually trigger a game promotion check.

**Permission Required:** `ADMINISTRATOR`

**Syntax:**
```
/admin-force-promotion-check
```

**Behavior:**
- Immediately checks for promotions
- Posts if promotions are available
- Respects verbosity settings

**Example:**
```
/admin-force-promotion-check
```

---

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
- Enables/disables promotional message detection
- Per-server configuration

**Example:**
```
/admin-setup-promotions enabled:true
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

| Permission | Commands |
|------------|----------|
| `MODERATE_MEMBERS` | `/admin-warn`, `/list_bans` |
| `KICK_MEMBERS` | `/admin-kick` |
| `BAN_MEMBERS` | `/admin-ban`, `/ban_and_remove_all_messages` |
| `MESSAGE_MANAGE` | `/cleanup` |
| `ADMINISTRATOR` | All configuration commands |

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

**Note:** Warn/Kick/Ban commands are separate from reputation and do not affect reputation scores. They use Discord's native moderation features.

---

## Best Practices

1. **Always Provide Reasons:** Clear reasons help with accountability
2. **Respect Role Hierarchy:** Bot role must be above target users
4. **Use Appropriate Commands:** Choose the right tool for the situation
5. **Document Actions:** Clear reasons make moderation history useful

---

**Last Updated:** 2025-10-08  
**Total Admin Commands:** 15+ commands





