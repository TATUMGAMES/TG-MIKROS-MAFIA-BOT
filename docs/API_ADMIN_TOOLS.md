# Admin Tools API Documentation

## Overview

The Admin Tools module provides Discord server moderators with powerful commands to manage users and maintain order in
the community. All actions are logged and tracked for accountability.

## Features

- User moderation (warn, kick, ban)
- Comprehensive moderation history tracking
- Permission-based access control
- Role hierarchy enforcement
- Detailed logging

## Slash Commands

### `/warn`

Issues a warning to a user without removing them from the server.

**Syntax:**

```
/warn user:<@user> reason:<string>
```

**Parameters:**

- `user` (required): The user to warn (mention or ID)
- `reason` (required): The reason for the warning

**Required Permissions:**

- Moderate Members

**Behavior:**

- Records the warning in the moderation log
- Sends a confirmation message visible to everyone
- Checks permission and role hierarchy

**Example:**

```
/warn user:@JohnDoe reason:Spamming in general chat
```

---

### `/kick`

Kicks a user from the server (they can rejoin with a new invite).

**Syntax:**

```
/kick user:<@user> reason:<string>
```

**Parameters:**

- `user` (required): The user to kick (mention or ID)
- `reason` (required): The reason for the kick

**Required Permissions:**

- Kick Members

**Behavior:**

- Removes the user from the server
- Records the kick in the moderation log
- Logs the reason in Discord's audit log
- Checks permission and role hierarchy for both moderator and bot

**Example:**

```
/kick user:@JohnDoe reason:Repeated rule violations
```

---

### `/ban`

Permanently bans a user from the server.

**Syntax:**

```
/ban user:<@user> reason:<string> [delete_days:<integer>]
```

**Parameters:**

- `user` (required): The user to ban (mention or ID)
- `reason` (required): The reason for the ban
- `delete_days` (optional): Number of days of messages to delete (0-7, default: 0)

**Required Permissions:**

- Ban Members

**Behavior:**

- Permanently removes the user from the server
- Records the ban in the moderation log
- Optionally deletes recent messages from the user
- Logs the reason in Discord's audit log
- Checks permission and role hierarchy (if user is still in server)

**Example:**

```
/ban user:@JohnDoe reason:Severe harassment delete_days:7
```

---

### `/history`

Displays the moderation history for a specific user.

**Syntax:**

```
/history user:<@user>
```

**Parameters:**

- `user` (required): The user to check (mention or ID)

**Required Permissions:**

- Moderate Members

**Behavior:**

- Retrieves all moderation actions for the specified user
- Displays statistics (total actions, warns, kicks, bans)
- Shows the 5 most recent actions with full details
- Returns an embed with formatted information

**Example:**

```
/history user:@JohnDoe
```

**Response Format:**

- User information (name, ID)
- Action counts by type
- Recent actions with:
    - Action type and emoji
    - Timestamp
    - Reason
    - Moderator who performed the action

---

## Data Models

### ModerationAction

Represents a single moderation action taken against a user.

**Fields:**

- `targetUserId` (String): Discord ID of the user who was moderated
- `targetUsername` (String): Username of the moderated user
- `moderatorId` (String): Discord ID of the moderator
- `moderatorUsername` (String): Username of the moderator
- `actionType` (ActionType): Type of action (WARN, KICK, BAN)
- `reason` (String): Reason provided for the action
- `timestamp` (Instant): When the action occurred
- `guildId` (String): Discord ID of the server where action occurred

### ActionType Enum

- `WARN`: Warning action
- `KICK`: Kick action
- `BAN`: Ban action

---

## Service Layer

### ModerationLogService

Interface for managing moderation action logs.

**Methods:**

#### `logAction(ModerationAction action)`

Logs a new moderation action.

**Parameters:**

- `action`: The moderation action to log

**Throws:**

- `IllegalArgumentException` if action is null

---

#### `getUserHistory(String userId, String guildId)`

Retrieves all moderation actions for a user in a guild.

**Parameters:**

- `userId`: The Discord user ID
- `guildId`: The Discord guild ID

**Returns:**

- List of ModerationAction objects, sorted by timestamp (newest first)

**Throws:**

- `IllegalArgumentException` if userId or guildId is null/blank

---

#### `getUserHistoryByType(String userId, String guildId, ActionType actionType)`

Retrieves moderation actions of a specific type.

**Parameters:**

- `userId`: The Discord user ID
- `guildId`: The Discord guild ID
- `actionType`: The type of action to filter by

**Returns:**

- Filtered list of ModerationAction objects

**Throws:**

- `IllegalArgumentException` if any parameter is null/blank

---

#### `getUserActionCount(String userId, String guildId)`

Gets the total number of moderation actions for a user.

**Parameters:**

- `userId`: The Discord user ID
- `guildId`: The Discord guild ID

**Returns:**

- Integer count of total actions

---

#### `clearAllHistory()`

Clears all moderation history (primarily for testing).

**Warning:** This operation cannot be undone.

---

## Implementation Notes

### Current Implementation: InMemoryModerationLogService

The current implementation stores all moderation data in memory using a `ConcurrentHashMap` for thread safety.

**Characteristics:**

- Thread-safe for concurrent operations
- Fast read/write operations
- Data is lost on bot restart
- Suitable for testing and development

**Storage Key Format:**

```
{guildId}:{userId}
```

**Future Enhancements:**

- Database persistence (planned)
- Integration with Tatum Games Reputation Score API (planned)
- Automated reputation score adjustments based on moderation actions

---

## Security & Validation

### Permission Checks

All commands perform the following checks:

1. **User Permission**: Verifies the command user has the required Discord permission
2. **Bot Permission**: Ensures the bot has the required permission to perform the action
3. **Role Hierarchy**: For kick/ban, verifies both moderator and bot can interact with the target
4. **Self-Moderation Prevention**: Users cannot moderate themselves
5. **Bot Protection**: Bots cannot be moderated

### Error Handling

All commands handle errors gracefully:

- Permission errors: Ephemeral (private) error messages
- Validation errors: Clear feedback on what went wrong
- Execution errors: Logged with full context, user sees generic error message

---

## Logging

All moderation actions are logged at multiple levels:

1. **Application Logs**: Detailed logs with full context
2. **Moderation Service**: In-memory/database storage
3. **Discord Audit Log**: Native Discord logging with reasons

**Log Format:**

```
[timestamp] [level] [class] - User {userId} {action} by {moderatorId} in guild {guildId}: {reason}
```

---

## Future Enhancements

### Planned Features

1. **Database Integration**
    - Persistent storage of moderation history
    - Migration from in-memory to database

2. **Reputation System Integration**
    - Automatic reputation score adjustments
    - API calls to Tatum Games Reputation Score service
    - Reputation-based automated actions

3. **Additional Commands**
    - `/mute` - Temporarily mute users
    - `/unmute` - Remove mute
    - `/unban` - Remove a ban
    - `/warnings clear` - Clear warnings for a user

4. **Enhanced History**
    - Pagination for long histories
    - Export functionality
    - Statistics and reporting

5. **Automated Moderation**
    - Threshold-based automatic actions
    - Escalation policies
    - Time-based warning expiration

---

## Error Codes

| Code                | Description                              |
|---------------------|------------------------------------------|
| `PERMISSION_DENIED` | User lacks required permissions          |
| `INVALID_TARGET`    | Target user is invalid (bot, self, etc.) |
| `HIERARCHY_ERROR`   | Role hierarchy prevents action           |
| `EXECUTION_ERROR`   | Discord API error during execution       |
| `VALIDATION_ERROR`  | Invalid parameter values                 |

---

## Best Practices

### For Moderators

1. Always provide clear, specific reasons for actions
2. Review user history before taking serious actions
3. Escalate actions appropriately (warn → kick → ban)
4. Document all actions in external tools if needed
5. Coordinate with other moderators

### For Administrators

1. Regularly review moderation logs
2. Set up proper role hierarchies
3. Grant moderation permissions carefully
4. Establish clear moderation guidelines
5. Backup moderation data regularly (once database is implemented)

---

## Support & Maintenance

For issues, bugs, or feature requests related to Admin Tools:

1. Check the logs for detailed error information
2. Review Discord audit logs for action confirmation
3. Contact the development team
4. Submit issues in the project repository

---

**Version:** 1.0 (Phase 1)  
**Last Updated:** October 7, 2025  
**Module Status:** ✅ Implemented, ⏳ Database integration pending

