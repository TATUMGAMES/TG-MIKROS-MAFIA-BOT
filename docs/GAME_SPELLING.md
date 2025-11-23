# Spelling Challenge Game Documentation

## Description

**Spelling Challenge** is a daily word unscrambling game where players solve a scrambled word from a gaming/fantasy-themed dictionary. Players get 3 attempts per day, and points are awarded based on solving order.

## How to Play

1. **View Today's Challenge:**
   - Use `/spell-challenge` to see today's scrambled word
   - Challenge shows letter count and participation stats

2. **Submit Your Guess:**
   - Use `/spell-guess word:<your_guess>`
   - You get **3 attempts per day**
   - First solver gets 3 points, others get 1 point

3. **Check Leaderboard:**
   - Use `/spell-leaderboard` to see all-time top players
   - Leaderboard tracks total points, solves, and first solves

## Commands

### Player Commands

| Command | Description | Example |
|---------|-------------|---------|
| `/spell-challenge` | View today's spelling challenge | `/spell-challenge` |
| `/spell-guess` | Submit your word guess | `/spell-guess word:GAMING` |
| `/spell-leaderboard` | View all-time spelling champions | `/spell-leaderboard` |

### Admin Commands

**Note:** Spelling Challenge uses the same admin commands as community games:
- `/admin-game-setup` - Configure game channel and reset time
- `/admin-game-config` - Configure game settings

## Scoring Rules

- **First Solver:** 3 points
- **Other Solvers:** 1 point each
- **Attempt Limit:** 3 attempts per player per day
- **Daily Reset:** New word every day at configured time
- **All-Time Leaderboard:** Cumulative points tracked

## Word Dictionary

Current dictionary includes **80 gaming/fantasy-themed words**:
- **4-5 letters:** Quick words (GAME, PLAY, DICE, ROLL, etc.)
- **6-7 letters:** Medium words (GAMING, PLAYER, STREAM, etc.)
- **8+ letters:** Challenging words (GAMEPLAY, ADVENTURE, etc.)

**Word Length Range:** 4-8 letters

## Game Flow

1. **Daily Reset:**
   - Bot selects random word from dictionary
   - Word is scrambled (e.g., "GAMING" → "GNIMAG")
   - Challenge session starts
   - All players can attempt again

2. **Player Participation:**
   - Players use `/spell-challenge` to see the word
   - Players submit guesses via `/spell-guess`
   - Incorrect guesses: Private message with attempts remaining
   - Correct guess: Public announcement with points awarded

3. **Leaderboard:**
   - All-time cumulative leaderboard
   - Shows top 10 players
   - Tracks total points, solves, first solves
   - View via `/spell-leaderboard`

## Narrative/Explanations

**Challenge Display:**
```
🧠 DAILY SPELLING CHALLENGE 🧠

Unscramble this word: GNIMAG (6 letters)

Use /spell-guess to submit your answer!
First solver gets 3 points! 🏆

Attempts: 0/3
Solvers: 0
```

**Correct Guess (First Solver):**
```
✅ CORRECT! (First solver! 🏆)

@Player solved it: GAMING!

+3 points awarded
Attempt: 1/3
```

**Correct Guess (Not First):**
```
✅ CORRECT!

@Player solved it: GAMING!

+1 point awarded
Attempt: 2/3
```

**Incorrect Guess:**
```
❌ Incorrect!

Your guess: GAMING
Attempts used: 2/3

Try again!
```

## Configuration

**Per-Server Settings:**
- Game channel (where challenges are posted)
- Reset time (daily reset hour, 0-23 UTC)
- Enabled/disabled status

**Default Settings:**
- Reset time: 00:00 UTC
- Attempt limit: 3 per day
- All challenges enabled by default

## Special Features

- **Attempt Tracking:** Players limited to 3 attempts per day
- **First Solver Bonus:** Extra points for being first
- **All-Time Leaderboard:** Cumulative tracking across all days
- **Participation Stats:** Shows attempts and solvers count
- **Smart Routing:** `/spell-guess` automatically routes to spelling challenge

## Future TODOs

- 🔮 **Hint System:** Optional hints (e.g., "Starts with G, 6 letters")
- 🔮 **Difficulty Levels:** Easy, medium, hard word pools
- 🔮 **RPG Integration:** Daily correct answer earns XP or loot
- 🔮 **Custom Word Lists:** Per-server custom dictionaries
- 🔮 **Streak Tracking:** Track consecutive daily solves
- 🔮 **Achievements:** Unlock achievements for milestones

---

**Last Updated:** 2025-10-08  
**Game Type:** Spelling Challenge  
**Command Prefix:** `spell-*`





