# Quick Start Guide - TG-MIKROS Bot

## Prerequisites

- Java 17 or higher installed
- Discord Bot Token

## Setup Steps

### 1. Get Your Discord Bot Token

1. Go to https://discord.com/developers/applications
2. Click "New Application"
3. Give it a name (e.g., "TG-MIKROS")
4. Go to "Bot" section
5. Click "Add Bot"
6. Under "Token", click "Copy"
7. Save this token securely!

### 2. Configure Bot Token

Create a `.env` file in the project root:

```env
DISCORD_BOT_TOKEN=your_token_here
BOT_OWNER_ID=your_discord_user_id
MIKROS_MAFIA_GUILD_ID=1213441992936390666
```

**Important**: Never commit the `.env` file to git (it's already in .gitignore)

**Note**: `MIKROS_MAFIA_GUILD_ID` is optional. If set, the RPG leaderboard will show which players are members of the MIKROS Mafia server. To find your server ID, right-click your server name → Copy Server ID, or extract it from a Discord channel URL: `https://discord.com/channels/<server_id>/<channel_id>`

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run the Bot

```bash
./gradlew run
```

You should see:
```
Bot logged in as: YourBotName#1234
Bot is in X guilds
Successfully registered 4 slash commands
```

### 5. Invite Bot to Your Server

1. Go back to Discord Developer Portal
2. Go to OAuth2 → URL Generator
3. Select scopes:
   - ✅ `bot`
   - ✅ `applications.commands`
4. Select bot permissions:
   - ✅ Kick Members
   - ✅ Ban Members
   - ✅ Moderate Members
5. Copy the generated URL
6. Open it in your browser
7. Select your server and authorize

### 6. Test Commands

In your Discord server, try:
```
/warn @user reason:Testing the warn system
/history @user
```

## Troubleshooting

**Bot doesn't come online:**
- Check your token in .env file
- Ensure token doesn't have extra spaces
- Check console for error messages

**Commands don't appear:**
- Wait a few minutes (Discord caches commands)
- Try in a different channel
- Check bot has proper permissions

**Commands fail:**
- Ensure bot role is higher than target user's role
- Verify bot has the required permissions
- Check the logs/bot.log file

## Development

**Run tests:**
```bash
./gradlew test
```

**Clean build:**
```bash
./gradlew clean build
```

**View test reports:**
```
build/reports/tests/test/index.html
```

## Project Structure

```
src/main/java/com/tatumgames/mikros/
├── bot/        - Bot initialization
├── commands/   - Slash command handlers
├── config/     - Configuration loading
├── models/     - Data models
└── services/   - Business logic
```

## Available Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/warn` | Moderate Members | Warn a user |
| `/kick` | Kick Members | Kick a user |
| `/ban` | Ban Members | Ban a user |
| `/history` | Moderate Members | View mod history |

## Next Steps

- Read the full README.md for detailed information
- Check docs/admin-tools-api.md for API documentation
- Review BEST_CODING_PRACTICES.md for coding standards

## Support

For issues, check:
1. Console output
2. logs/bot.log file
3. Discord bot status at https://discord.com/developers/applications

Happy moderating! 🎮

