# Scramble Game Documentation

## Description

**Scramble** (Word Unscramble) is a daily community game where players unscramble gaming-themed words. Each day, a new
scrambled word is posted, and players compete to be the first to solve it.

## How to Play

> **Important:** All Word Unscramble commands must be used in the channel assigned by administrators via
`/admin-scramble-setup`. If you try to use a command in the wrong channel, you'll receive a message directing you to the
> correct channel.

1. **View Today's Challenge:**
    - Check `/scramble-stats` to see the current game
    - The bot posts the scrambled word in the game channel daily

2. **Submit Your Guess:**
    - Use `/scramble-guess word:<your_guess>` to submit your answer
    - Use `/scramble-guess word:hint` to get a hint (one hint per word per player)
    - First correct guess wins!

3. **Scoring:**
    - Points based on time to solve
    - First solver gets bonus points
    - Game ends when someone solves it

## Commands

### Player Commands

| Command                 | Description                                   | Example                                                        |
|-------------------------|-----------------------------------------------|----------------------------------------------------------------|
| `/scramble-guess`       | Submit your word guess or request a hint      | `/scramble-guess word:GAMEPLAY` or `/scramble-guess word:hint` |
| `/scramble-stats`       | View current game status and leaderboard      | `/scramble-stats`                                              |
| `/scramble-profile`     | View your individual statistics               | `/scramble-profile`                                            |
| `/scramble-leaderboard` | View top players by total points (per server) | `/scramble-leaderboard page:1`                                 |

### Admin Commands

| Command                  | Description                              | Permission    |
|--------------------------|------------------------------------------|---------------|
| `/admin-scramble-setup`  | Initial game setup (channel, reset time) | Administrator |
| `/admin-scramble-config` | Configure game settings                  | Administrator |

**Admin Subcommands:**

- `view` - View current configuration
- `update-channel` - Update the game channel (requires setup first). **Full reset** for Word Unscramble on that server
  (stats, sessions, progression, used words), then a new game is posted in the new channel—same clean start as
  `/admin-scramble-setup`.
- `set-reset-time` - Change daily reset hour
- `enable-game` - Enable Scramble game
- `disable-game` - Disable Scramble game

## Scoring Rules

- **Time-Based Scoring:** Faster solves = more points (100-1000 points based on solve time)
- **Level Multipliers:** Score multipliers based on difficulty level:
    - Levels 1-5: ×1.0
    - Levels 6-10: ×1.2
    - Levels 11-14: ×1.5
    - Levels 15-20: ×2.0
- **First Solver Bonus:** Scaled bonus for being the first to solve:
    - Levels 1-5: +50 points
    - Levels 6-10: +100 points
    - Levels 11-14: +150 points
    - Levels 15-20: +200 points
- **Volume Bonuses:**
    - Every 10th word solved: +100-200 points (randomized)
    - 3 consecutive correct solves: +50 points
- **Accuracy Factor:** Final score is multiplied by accuracy percentage (wordsSolved / totalAttempts × 100)
- **Bonus Points:** Extra points for solving words that stumped others (based on wrong guesses from other players)
- **Game Ends:** When first correct guess is submitted
- **Reset Schedule:** A new word is posted every 4 hours (scheduler runs every 4 hours). The configured reset time (
  daily reset hour) is used per guild where applicable.
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
  - Current streak (consecutive correct solves)
  - Longest streak achieved

## Word List

The game features 20 levels with progressively longer words and phrases:

- **Levels 1-10:** Single words (4-12+ letters)
- **Levels 11-14:** Short phrases (2-3 words)
- **Levels 15-20:** Longer phrases (3-6+ words)

**Word Pool:** 500+ gaming-themed words and phrases across all levels

**Word Rotation System:**

- **Levels 1-5:** Words don't repeat for 60 days (2 months) per server. Used-word tracking is pruned every 4 hours (
  entries older than 60 days are removed) to limit memory use; leaderboards and stats are not affected.
- **Levels 6+:** Enhanced scrambling and partial reveals instead of full answers

**Level Isolation:** Each word appears only in its designated level - no cross-level contamination

**Branding Words Included:**
- "MIKROS" (Level 2)
- "TATUM GAMES" (Level 11)
- "TATUM TECH" (Level 11)

**Community Progression:** Server-wide level system (Level 1-20) determines word difficulty

## Game Flow

1. **Reset (every 4 hours):**
    - Bot selects random word from pool (respects word rotation for levels 1-5)
    - Word is scrambled with level-appropriate algorithm:
        - Levels 1-5: Simple shuffle
        - Levels 6+: Enhanced scrambling with minimum displacement and multiple passes
        - Phrases (Levels 6+): Word order also shuffled
    - Game session starts

2. **Player Participation:**
    - Players see scrambled word in game channel
   - For levels 6+, hints are included in the announcement
   - Players can request a hint using `/scramble-guess word:hint` (one hint per word per player)
   - Players submit guesses via `/scramble-guess word:<guess>`
    - Each player gets **3 incorrect guesses per word**
    - Incorrect guesses: Private ephemeral message showing remaining guesses
    - After 3 incorrect guesses: Further attempts blocked (private message)
    - Correct guess: Public announcement, game ends
   - For levels 6+, answer is shown as hints (first/last letter, length) instead of full word
    - Limit resets automatically when a new word starts

3. **Leaderboard:**
    - Shows winner with time and score
    - View current game via `/scramble-stats`
    - View all-time rankings via `/scramble-leaderboard`

## Narrative/Explanations

**Game Announcement (Levels 1-5):**

```
⏰ It's that time again! ⏰

🔤 New Unscramble Challenge!

Level 3 | Unscramble this word: AEPLYGAM (8 letters)

Use /scramble-guess to submit your answer!
First correct player wins! 🏆
```

**Game Announcement (Levels 6+):**

```
⏰ It's that time again! ⏰

🔤 New Unscramble Challenge!

Level 8 | Unscramble this word: AEPLYGAM (8 letters)

💡 Hint: Starts with G

Use /scramble-guess to submit your answer!
First correct player wins! 🏆
```

**Correct Guess Response (Levels 1-5):**

```
🎉 CORRECT! 🎉

@Player guessed it right: GAMEPLAY!

Score: 150 points
Time: 12 seconds

Progression: 5 more words needed to reach Level 4
```

**Correct Guess Response (Levels 6+):**

```
🎉 CORRECT! 🎉

@Player guessed it right: Starts with G, ends with Y, 8 letters!

Score: 180 points (150 base + 30 bonus)
Time: 12 seconds

Progression: 5 more words needed to reach Level 9
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

## Hint System

Players can request hints using `/scramble-guess word:hint` (one hint per word per player).

**Hint Types (randomly selected):**

- First letter reveal
- Last letter reveal
- Word length or word count (for phrases)
- Vowel positions
- Category hint (gaming-related categories)

**Hint Examples:**

- "Starts with **G**"
- "Ends with **Y**"
- "**8 letters** long"
- "Contains vowels: **A, E**"
- "Related to **gameplay or modes**"

## Enhanced Features

**Word Rotation (Levels 1-5):**

- Words don't repeat for 60 days (2 months) per server
- Ensures variety and prevents memorization
- Automatically resets when all words are used

**Partial Reveals (Levels 6+):**

- Announcements include hints automatically
- Solved answers shown as hints (first/last letter, length) instead of full word
- Prevents future players from seeing exact answers if word repeats

**Enhanced Scrambling (Levels 6+):**

- Multiple shuffle passes for better randomization
- Minimum character displacement
- For phrases: word order also shuffled

## Future TODOs

- 🔮 **Custom Word Lists:** Allow admins to upload custom word lists per guild
- 🔮 **Themed Word Packs:** Gaming, tech, fantasy, etc.
- 🔮 **Statistics:** Track most difficult words (fewest correct guesses)
- 🔮 **Multi-Word Challenges:** Unscramble multiple words in sequence

---

**Last Updated:** 2025-01-XX  
**Game Type:** Word Unscramble  
**Command Prefix:** `scramble-*`

**Recent Updates:**

- ✅ Word rotation system for levels 1-5 (60-day cooldown)
- ✅ Enhanced scoring with level multipliers and volume bonuses
- ✅ Accuracy factor in scoring calculation
- ✅ Scaled first solver bonuses
- ✅ Hint system (one hint per word per player)
- ✅ Partial reveals for levels 6+ (hints instead of full answers)
- ✅ Enhanced scrambling algorithm for levels 6+
- ✅ Level isolation (no duplicate words across levels)





