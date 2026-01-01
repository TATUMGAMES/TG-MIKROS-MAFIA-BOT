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

| Command             | Description                              | Example                         |
|---------------------|------------------------------------------|---------------------------------|
| `/scramble-guess`   | Submit your word guess                   | `/scramble-guess word:GAMEPLAY` |
| `/scramble-stats`   | View current game status and leaderboard | `/scramble-stats`               |
| `/scramble-profile` | View your individual statistics          | `/scramble-profile`             |
| `/scramble-leaderboard` | View top players by total points (per server) | `/scramble-leaderboard page:1` |

### Admin Commands

| Command                  | Description                              | Permission    |
|--------------------------|------------------------------------------|---------------|
| `/admin-scramble-setup`  | Initial game setup (channel, reset time) | Administrator |
| `/admin-scramble-config` | Configure game settings                  | Administrator |

**Admin Subcommands:**

- `view` - View current configuration
- `update-channel` - Update the game channel (requires setup first)
- `set-reset-time` - Change daily reset hour
- `enable-game` - Enable Scramble game
- `disable-game` - Disable Scramble game

## Scoring Rules

- **Time-Based Scoring:** Faster solves = more points (100-1000 points based on solve time)
- **Bonus Points:** Extra points for solving words that stumped others (based on wrong guesses from other players)
- **Game Ends:** When first correct guess is submitted
- **Hourly Reset:** New word every hour at the top of the hour (00:00 UTC format)
- **Guess Limit:** Each player gets **3 incorrect guesses per word** to prevent spam
  - Remaining guesses are shown after each incorrect attempt
  - Limit resets automatically when a new word starts
  - Correct guesses always work, even after incorrect attempts
- **Individual Statistics:** All players have persistent stats tracking:
  - Total words solved
  - Total points earned
  - Highest single-word score
  - Fastest solve time
  - Total attempts and wrong guesses
  - Accuracy percentage
  - Average score per word

## Word List

The game features 20 levels with progressively longer words and phrases:

- **Levels 1-10:** Single words (4-12+ letters)
- **Levels 11-14:** Short phrases (2-3 words)
- **Levels 15-20:** Longer phrases (3-6+ words)

**Word Pool:** 500+ gaming-themed words and phrases across all levels

**Branding Words Included:**
- "MIKROS" (Level 2)
- "TATUM GAMES" (Level 11)
- "TATUM TECH" (Level 11)

**Community Progression:** Server-wide level system (Level 1-20) determines word difficulty

## Game Flow

1. **Daily Reset:**
    - Bot selects random word from pool
    - Word is scrambled (e.g., "GAMEPLAY" → "AEPLYGAM")
    - Game session starts

2. **Player Participation:**
    - Players see scrambled word in game channel
    - Players submit guesses via `/scramble-guess`
    - Each player gets **3 incorrect guesses per word**
    - Incorrect guesses: Private ephemeral message showing remaining guesses
    - After 3 incorrect guesses: Further attempts blocked (private message)
    - Correct guess: Public announcement, game ends
    - Limit resets automatically when a new word starts

3. **Leaderboard:**
    - Shows winner with time and score
    - View current game via `/scramble-stats`
    - View all-time rankings via `/scramble-leaderboard`

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

Remaining guesses: 2 out of 3
Try again!
```

**Limit Exceeded Response:**

```
❌ No More Guesses Remaining

You've used all 3 incorrect guesses for this word.

Wait for the next word to get 3 more guesses!
```

## Configuration

**Per-Server Settings:**

- Game channel (where games are posted)
- Reset time (daily reset hour, 0-23 UTC)
- Enabled/disabled status

**Default Settings:**

- Reset time: 00:00 UTC
- All games enabled by default

## Individual Player Statistics

Each player has persistent statistics tracked across all game sessions:

**Available via `/scramble-profile`:**

- **Total Words Solved** - Cumulative correct answers
- **Total Points Earned** - Sum of all scores
- **Highest Score** - Best single-word score achieved
- **Fastest Time** - Quickest solve time (formatted as minutes/seconds)
- **Total Attempts** - All guesses (correct + incorrect)
- **Wrong Guesses** - Total incorrect attempts
- **Accuracy Percentage** - (Words Solved / Total Attempts) × 100
- **Average Score** - Total Points / Words Solved

**Note:** Statistics are tracked per-guild, so each server maintains separate stats for players.

## Leaderboards

View the top Word Unscramble players in your server with `/scramble-leaderboard`.

### Features

- **Per-Server Rankings:** Leaderboards are specific to each Discord server
- **Comprehensive Stats:** Shows total points, words solved, high score, accuracy, and attempts
- **Pagination:** View 25 players per page with easy navigation
- **Active Players Only:** Only shows players who have attempted at least one word

### Sorting

Players are ranked by:
1. Total Points (descending)
2. Words Solved (descending)
3. Highest Score (descending)

### Viewing Your Stats

Use `/scramble-profile` to see your personal statistics including:
- Total words solved
- Total points earned
- Highest single-word score
- Fastest solve time
- Accuracy percentage
- Total attempts and wrong guesses

## Future TODOs

- 🔮 **Custom Word Lists:** Allow admins to upload custom word lists per guild
- 🔮 **Difficulty Levels:** Easy, medium, hard based on word length
- 🔮 **Themed Word Packs:** Gaming, tech, fantasy, etc.
- 🔮 **Statistics:** Track most difficult words (fewest correct guesses)
- 🔮 **Hints System:** Optional hints for struggling players
- 🔮 **Multi-Word Challenges:** Unscramble multiple words in sequence

---

**Last Updated:** 2025-12-24  
**Game Type:** Word Unscramble  
**Command Prefix:** `scramble-*`





