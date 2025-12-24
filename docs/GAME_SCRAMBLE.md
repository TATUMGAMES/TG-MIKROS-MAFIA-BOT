# Scramble Game Documentation

## Description

**Scramble** (Word Unscramble) is a daily community game where players unscramble gaming-themed words. Each day, a new
scrambled word is posted, and players compete to be the first to solve it.

## How to Play

1. **View Today's Challenge:**
    - Check `/scramble-stats` to see the current game
    - The bot posts the scrambled word in the game channel daily

2. **Submit Your Guess:**
    - Use `/scramble-guess word:<your_guess>`
    - First correct guess wins!

3. **Scoring:**
    - Points based on time to solve
    - First solver gets bonus points
    - Game ends when someone solves it

## Commands

### Player Commands

| Command           | Description                              | Example                         |
|-------------------|------------------------------------------|---------------------------------|
| `/scramble-guess` | Submit your word guess                   | `/scramble-guess word:GAMEPLAY` |
| `/scramble-stats` | View current game status and leaderboard | `/scramble-stats`               |

### Admin Commands

| Command                  | Description                              | Permission    |
|--------------------------|------------------------------------------|---------------|
| `/admin-scramble-setup`  | Initial game setup (channel, reset time) | Administrator |
| `/admin-scramble-config` | Configure game settings                  | Administrator |

**Admin Subcommands:**

- `view` - View current configuration
- `set-channel` - Change game channel
- `set-reset-time` - Change daily reset hour
- `enable-game` - Enable Scramble game
- `disable-game` - Disable Scramble game

## Scoring Rules

- **Time-Based Scoring:** Faster solves = more points
- **First Solver Bonus:** Extra points for being first
- **Game Ends:** When first correct guess is submitted
- **Daily Reset:** New word every day at configured time (default: 00:00 UTC)

## Word List

Current word pool includes 20 gaming-themed words:

- GAMEPLAY, STREAMER, GIVEAWAY, CHAMPION, TREASURE
- ADVENTURE, VICTORY, PLATFORM, CHALLENGE, COMMUNITY
- DISCORD, CREATIVE, STRATEGY, TOURNAMENT, LEGENDARY
- MULTIPLAYER, CAMPAIGN, CHARACTER, ACHIEVEMENT, DEVELOPER

**Word Length:** 7-11 characters

## Game Flow

1. **Daily Reset:**
    - Bot selects random word from pool
    - Word is scrambled (e.g., "GAMEPLAY" → "AEPLYGAM")
    - Game session starts

2. **Player Participation:**
    - Players see scrambled word in game channel
    - Players submit guesses via `/scramble-guess`
    - Incorrect guesses: Private ephemeral message
    - Correct guess: Public announcement, game ends

3. **Leaderboard:**
    - Shows winner with time and score
    - View via `/scramble-stats`

## Narrative/Explanations

**Game Announcement:**

```
🎮 WORD UNSCRAMBLE - Game of the Day 🎮

Unscramble this word: AEPLYGAM

Use /scramble-guess to submit your answer!
First correct solver wins! 🏆
```

**Correct Guess Response:**

```
🎉 CORRECT! 🎉

@Player guessed it right: GAMEPLAY!

Score: 150 points
Time: 12.5 seconds
```

**Incorrect Guess Response:**

```
❌ Incorrect!

Your guess: GAMING
Try again!
```

## Configuration

**Per-Server Settings:**

- Game channel (where games are posted)
- Reset time (daily reset hour, 0-23 UTC)
- Enabled/disabled status

**Default Settings:**

- Reset time: 00:00 UTC
- All games enabled by default

## Future TODOs

- 🔮 **Custom Word Lists:** Allow admins to upload custom word lists per guild
- 🔮 **Difficulty Levels:** Easy, medium, hard based on word length
- 🔮 **Themed Word Packs:** Gaming, tech, fantasy, etc.
- 🔮 **Statistics:** Track most difficult words (fewest correct guesses)
- 🔮 **Hints System:** Optional hints for struggling players
- 🔮 **Multi-Word Challenges:** Unscramble multiple words in sequence

---

**Last Updated:** 2025-10-08  
**Game Type:** Word Unscramble  
**Command Prefix:** `scramble-*`





