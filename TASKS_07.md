# TASKS_07.md

## Objective

Introduce a lightweight, daily, community-wide game that:

- Requires no registration
- Encourages repeat visits
- Resets daily
- Is easy to configure per server

This game is focused on accessibility, engagement, and low-friction fun.

---

## Feature: `/spelling-challenge`

### Game Type: Daily Word Guessing Challenge

Each day, the bot:

- Picks a random word (length 4–8 letters) from a dictionary
- Posts a scrambled version in the assigned game channel
- Users try to unscramble it using `/guess <word>`
- Points awarded for correct answers
- Leaderboard tracks players who solve it first

---

### System Requirements

- Use `SpellingChallengeService` to manage:
    - Daily word
    - Scrambled version
    - Time of posting
    - User attempts

- Configurable via `/setup-games`:
    - Enable/disable specific games
    - Assign game channels
    - Set difficulty (easy, medium, hard = word lengths)

---

### Commands

| Command                 | Description                           |
|-------------------------|---------------------------------------|
| `/spelling-challenge`   | Posts today’s scrambled word          |
| `/guess <word>`         | Allows user to submit a guess         |
| `/spelling-leaderboard` | Shows top players (by correct solves) |

- Game resets every 24 hours (configurable)
- Players only get 3 attempts per day

---

### Scoring

- First correct solver: +3 pts
- Others who solve it: +1 pt
- Leaderboard tracked in memory or simple file for now

---

## TODOs & Future Work

- Add `TODO` for persistent storage for leaderboard
- Add `TODO` for web dashboard export in the future
- Add `/hint` system (e.g. “Starts with C, 7 letters”) — optional
- Add `/rpg-reward` integration: daily correct answer earns XP or loot in the RPG system (cross-game synergy)

---

## Example Interaction

Bot posts in #game-hall:

🧠 DAILY SPELLING CHALLENGE 🧠
Unscramble this word: gamin

Use /guess to try! First correct solver gets bonus points!

User: `/guess aming`  
Bot: ❌  
User: `/guess gaming`  
Bot: ✅ Correct! You earned 3 points.

---

> Reminder: Keep BEST_CODING_PRACTICES.md in mind when implementing game state management, rate limiting, and
> configuration per server.
