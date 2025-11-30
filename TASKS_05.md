# TASKS_05.md

## Objective

Implement a **Community Games Engine** to run short, fun, daily-reset games that any user in a Discord server can join instantly.

Games are designed to:
- Require **no registration**
- Be playable by **any user** in a configured game channel
- **Reset daily** to maintain freshness and routine engagement
- Be **configurable per server** (game type, game channel, reset time, etc.)

All work must follow `BEST_CODING_PRACTICES.md`.

---

## Feature: Daily Community Game System

This engine supports rotating or fixed games per server. Initial implementation should support:

- Word Guess Game (e.g., "Unscramble")
- Emoji Reaction Game

Future games can be added modularly.

---

## Phase 1 – Implement the Core Game System

### 1. `/game-setup`
- Admin-only command
- Steps:
    1. Select a channel for the daily game
    2. Choose which games are enabled (checkbox or dropdown)
    3. Set daily reset time (UTC or server local time)
- Store these settings in memory (TODO: persist later)

---

### 2. Game: Word Unscramble
- Every day, bot posts a new scrambled word
- Anyone can guess using `/guess <word>`
- Bot gives feedback (correct/incorrect)
- First correct guess wins; their name and time recorded
- Example:

Unscramble: "PNGAEML" (7 letters)
🎉 @Jade guessed it right: "AMPLENG"!


---

---

---

### 5. Game Reset System
- Daily reset of game state (scores, answers, etc.)
- Implement basic scheduler (e.g., Java `TimerTask`)
- At reset time:
- Clear state
- Announce new daily challenge in configured channel

---

### 6. `/game-stats`
- Show current day's:
- Leaderboard (who guessed/rolled/won)
- Time remaining until next reset
- Use Discord Embed formatting

---

### 7. `/game-config`
- Admin-only command
- Change:
- Game types (enable/disable)
- Reset time
- Game channel
- Difficulty level (if applicable)

---

### 8. Code Requirements

Cursor AI must:
- Use a modular structure:
- `GameService`, `GameSession`, `GameConfig`, `GameResult` classes
- Implement:
- Enum for game types
- Interface for game behavior (start, reset, handleCommand)
- Design with extensibility in mind (future games must be plug-and-play)

---

## Slash Commands to Implement

| Command | Description |
|---------|-------------|
| `/game-setup` | Configure which games are enabled and where |
| `/guess <word>` | Submit guess for word game |
| `/roll` | Roll a dice for the dice game |
| `/game-stats` | View game of the day and leaderboard |
| `/game-config` | Modify settings (admin only) |

---

## Future Features (Mark TODOs in code)

| Feature | Description |
|---------|-------------|
| Game Rotation | Randomize daily game or rotate between enabled games |
| Reward System | MIKROS discounts or Discord roles for winners |
| Server Persistence | Store settings in database per server |
| Emoji Leaderboard | Track cumulative wins over time |
| Custom Games | Admins can define their own word lists or emoji sets |

---

## Example Game Flow

1. Admin types `/game-setup` → selects channel + games
2. Every day at configured reset time:
- Bot posts the game of the day
3. Users join by typing the appropriate command
4. Bot tracks results and shows leaderboard
5. At reset, bot clears results and starts a new round

---

## Example Folder Structure

src/
├── communitygames/
│ ├── model/
│ ├── service/
│ ├── games/
│ │ ├── WordUnscrambleGame.java
│ └── config/


---

## Notes

- No user registration is required
- Bot tracks **Discord user IDs** automatically
- Cursor AI must document all classes with proper comments
- Modular structure is a priority
- All features must respect channel permissions and be rate-limited







