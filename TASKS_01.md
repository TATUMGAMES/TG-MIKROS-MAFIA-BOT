# TASKS_01.md

## Objective

Set up the Discord bot project in Java using the JDA (Java Discord API) library and begin implementation of the first
modular feature: Admin Tools.

> Follow all best practices outlined in `BEST_CODING_PRACTICES.md`.

---

## Project Setup

- Add JDA (Java Discord API) as a Gradle dependency
- Configure basic bot startup code:
    - Read the bot token from a `.env` or config file
    - Set up a main class (e.g., `BotMain`) to launch the bot
    - Log when the bot is ready
- Add `.gitignore` for `.env`, compiled files, etc.

---

## Feature Module 1: Admin Tools — Phase 1

> We will add traditional moderation commands **but with enhanced features**.

### Create the following slash commands:

- `/warn <@user> <reason>` – Warns a user and logs the warning in a persistent store (in-memory for now, will expand to
  DB later)
- `/kick <@user> <reason>` – Kicks user, logs action
- `/ban <@user> <reason>` – Bans user, logs action
- `/history <@user>` – Lists previous moderation actions for the user (warns, kicks, bans, reasons, and timestamps)

### Feature Extension Ideas:

- Add TODO comments to:
    - Call the Tatum Games Reputation Score API (not connected yet)
    - Track Reputation score logic locally (as a mock or stub)
- Implement `ModerationLogService` (interface + impl) to manage history of moderation actions (store in memory for now)

---

## Reminders

- All code must follow `BEST_CODING_PRACTICES.md`
- Add Javadoc-style comments
- Use clean architecture: services, commands, models
- Use enums for action types (e.g., `ActionType.WARN`, `ActionType.BAN`)
- Use interfaces and separate command logic into individual classes
- Self-check work and add basic unit tests for logic