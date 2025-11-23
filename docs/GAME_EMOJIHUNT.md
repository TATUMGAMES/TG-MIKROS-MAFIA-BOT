# EmojiHunt Game Documentation

## Description

**EmojiHunt** (Emoji Match) is a daily community game where players match emoji patterns. Each day, a random 3-emoji pattern is generated, and players compete to be the first to match it exactly.

## How to Play

1. **View Today's Pattern:**
   - Check `/game-stats` to see the current game
   - The bot posts the emoji pattern in the game channel daily

2. **Match the Pattern:**
   - Use `/emojihunt-match emojis:<emoji_pattern>`
   - Pattern must match **exactly** (order matters!)
   - First correct match wins!

3. **Scoring:**
   - Points based on time to solve
   - First solver gets bonus points
   - Game ends when someone matches correctly

## Commands

### Player Commands

| Command | Description | Example |
|---------|-------------|---------|
| `/emojihunt-match` | Submit your emoji pattern match | `/emojihunt-match emojis:🎮🎲🎯` |
| `/game-stats` | View current game status and leaderboard | `/game-stats` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/admin-game-setup` | Initial game setup (channel, reset time) | Administrator |
| `/admin-game-config` | Configure game settings | Administrator |

**Admin Subcommands:**
- `view` - View current configuration
- `set-channel` - Change game channel
- `set-reset-time` - Change daily reset hour
- `enable-game` - Enable EmojiHunt game
- `disable-game` - Disable EmojiHunt game

## Scoring Rules

- **Time-Based Scoring:** Faster matches = more points
- **First Solver Bonus:** Extra points for being first
- **Exact Match Required:** Pattern must match exactly (order matters)
- **Game Ends:** When first correct match is submitted
- **Daily Reset:** New pattern every day at configured time (default: 00:00 UTC)

## Emoji Pool

Current emoji pool includes 32 gaming/fantasy-themed emojis:

**Gaming:** 🎮 🎲 🎯 🎪 🎨 🎭 🎬 🎤  
**Achievements:** 🏆 🏅 ⭐ ✨ 💎 🔥 ⚡ 🌟  
**Fantasy:** 🐉 🦁 🦅 🐺 🦊 🐻 🐯 🦈  
**Combat:** ⚔️ 🛡️ 🏹 🔱 💣 🧨 🎁 🎉

**Pattern Length:** 3 emojis (configurable in future)

## Game Flow

1. **Daily Reset:**
   - Bot generates random 3-emoji pattern
   - Pattern posted in game channel
   - Game session starts

2. **Player Participation:**
   - Players see emoji pattern in game channel
   - Players submit matches via `/emojihunt-match`
   - Incorrect matches: Private ephemeral message
   - Correct match: Public announcement, game ends

3. **Leaderboard:**
   - Shows winner with time and score
   - View via `/game-stats`

## Narrative/Explanations

**Game Announcement:**
```
🎯 EMOJI MATCH - Game of the Day 🎯

Match this pattern: 🎮🎲🎯

Use /emojihunt-match to submit your answer!
First correct match wins! 🏆
```

**Correct Match Response:**
```
🎉 CORRECT! 🎉

@Player matched it right: 🎮🎲🎯!

Score: 180 points
Time: 8.3 seconds
```

**Incorrect Match Response:**
```
❌ Incorrect!

Your match: 🎲🎮🎯
Pattern must match exactly!
Try again!
```

## Configuration

**Per-Server Settings:**
- Game channel (where games are posted)
- Reset time (daily reset hour, 0-23 UTC)
- Enabled/disabled status

**Default Settings:**
- Reset time: 00:00 UTC
- Pattern length: 3 emojis
- All games enabled by default

## Matching Rules

- **Exact Match Required:** Pattern must match character-for-character
- **Order Matters:** 🎮🎲🎯 ≠ 🎲🎮🎯
- **Case Sensitive:** Emojis must match exactly
- **No Partial Matches:** All 3 emojis must be correct

## Future TODOs

- 🔮 **Custom Emoji Sets:** Allow admins to define custom emoji pools per guild
- 🔮 **Difficulty Levels:** Vary pattern length (3, 4, 5 emojis)
- 🔮 **Themed Emoji Sets:** Animals, food, sports, etc.
- 🔮 **Time-Based Scoring:** Faster solves = more points
- 🔮 **Pattern Variations:** Rotating patterns, sequences
- 🔮 **Multi-Pattern Challenges:** Match multiple patterns in sequence

---

**Last Updated:** 2025-10-08  
**Game Type:** Emoji Match  
**Command Prefix:** `emojihunt-*`





