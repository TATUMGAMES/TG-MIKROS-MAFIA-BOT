# DiceFury Game Documentation

## Description

**DiceFury** (Dice Battle) is a daily community game where players roll a D20 die to compete for the highest roll of the day. Players get one roll per day, and the highest roller wins!

## How to Play

1. **Check Today's Game:**
   - Use `/game-stats` to see if DiceFury is active
   - View current leaderboard

2. **Roll the Dice:**
   - Use `/dicefury-roll`
   - You get **one roll per day**
   - Roll a D20 (1-20)

3. **Win Conditions:**
   - Highest roll of the day wins
   - Natural 20 (critical hit) gets special recognition
   - Leaderboard updates in real-time

## Commands

### Player Commands

| Command | Description | Example |
|---------|-------------|---------|
| `/dicefury-roll` | Roll your D20 die | `/dicefury-roll` |
| `/game-stats` | View current leaderboard and time remaining | `/game-stats` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/admin-game-setup` | Initial game setup (channel, reset time) | Administrator |
| `/admin-game-config` | Configure game settings | Administrator |

**Admin Subcommands:**
- `view` - View current configuration
- `set-channel` - Change game channel
- `set-reset-time` - Change daily reset hour
- `enable-game` - Enable DiceFury game
- `disable-game` - Disable DiceFury game

## Scoring Rules

- **One Roll Per Day:** Each player gets exactly one roll
- **Highest Roll Wins:** Player with highest number wins
- **Critical Hit:** Natural 20 gets special recognition (🔥)
- **Tie Handling:** First player to roll the highest number wins
- **Daily Reset:** New competition every day at configured time

## Dice Mechanics

- **Die Type:** D20 (20-sided die)
- **Range:** 1-20
- **Critical Hit:** Natural 20 (automatic recognition)
- **Roll Limit:** One roll per player per day

## Game Flow

1. **Daily Reset:**
   - New game session starts
   - Leaderboard resets
   - All players can roll again

2. **Player Participation:**
   - Players use `/dicefury-roll`
   - Roll result is announced publicly
   - Leaderboard updates automatically

3. **Leaderboard:**
   - Shows top 10 players
   - Sorted by roll value (highest first)
   - Medals for top 3 (🥇🥈🥉)
   - Critical hits marked with 🔥

## Narrative/Explanations

**Game Announcement:**
```
🎲 DICE BATTLE - Game of the Day 🎲

Roll your D20 and compete for the highest roll!

Use /dicefury-roll to roll!
Highest roller wins! 🏆
```

**Roll Result:**
```
🎲 @Player rolled a **20**! 🔥

Critical Hit! Natural 20!
New leader! 🏆
```

**Leaderboard Display:**
```
🏆 Leaderboard (Highest Roll)

🥇 #1 - Player1: 20 🔥
🥈 #2 - Player2: 19
🥉 #3 - Player3: 18
   #4 - Player4: 17
   #5 - Player5: 16
```

## Configuration

**Per-Server Settings:**
- Game channel (where games are posted)
- Reset time (daily reset hour, 0-23 UTC)
- Enabled/disabled status

**Default Settings:**
- Reset time: 00:00 UTC
- All games enabled by default

## Special Features

- **Critical Hit Detection:** Natural 20 automatically detected
- **Leader Change Announcements:** Bot announces when new leader emerges
- **One Roll Limit:** Prevents spam, ensures fair competition
- **Real-Time Leaderboard:** Updates immediately after each roll

## Future TODOs

- 🔮 **Multiple Dice:** Roll 2D20, 3D6, etc.
- 🔮 **Dice Types:** D4, D6, D8, D10, D12, D20 selection
- 🔮 **Bonus Modifiers:** Add stat bonuses to rolls
- 🔮 **Streak Tracking:** Track consecutive high rolls
- 🔮 **Achievements:** Unlock achievements for special rolls
- 🔮 **Team Competitions:** Guild vs guild dice battles

---

**Last Updated:** 2025-10-08  
**Game Type:** Dice Roll  
**Command Prefix:** `dicefury-*`





