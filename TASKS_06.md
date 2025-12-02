# TASKS_06.md

## Objective

Build the foundation for a **long-form, opt-in text RPG game** within Discord. Users will create characters, explore a
game world, and progress through experience, stats, items, and storylines.

This system is **modular**, **scalable**, and designed for long-term community retention. Features will evolve over
time (quests, enemies, inventory, multiplayer, etc.).

---

## Game Philosophy

- ⚔️ Persistent character per user (1 per Discord ID)
- 🧭 Choose your adventure: explore, battle, level-up
- 🎁 Optional reward integrations with MIKROS
- 💾 Game state must be saved per user
- 💡 Admins can disable or limit RPG access per server

---

## Gameplay Overview

Initial gameplay is simple:

- Users **create characters**
- Characters have **base stats** (HP, STR, AGI, LUCK, etc.)
- They can choose an **action each day**: explore, battle, train
- Bot responds with outcomes (dice roll style), grants XP
- Leveling up increases stats
- Rewards and story events unlocked through progression

---

## Phase 1: Core RPG Engine

### 1. `/rpg-register`

- Command to opt-in and create a new character
- Steps:
    - Choose name
    - Choose class (e.g. Warrior, Mage, Rogue – using ENUM)
- Stores character data (in-memory initially)
- **TODO**: Persistence layer in later phase

### 2. `/rpg-profile`

- View your character stats, class, level, XP, inventory
- Includes an RPG-style embed card with:
    - Character Name
    - Class
    - Level
    - XP
    - Stats (HP, STR, AGI, etc.)
    - Inventory items (if applicable)

### 3. `/rpg-action`

- Player chooses an action from:
    - `explore` (random story/narrative encounter)
    - `train` (gain XP, increase random stat)
    - `battle` (fight a basic enemy – AI only in Phase 1)
- Each action has cooldown (e.g. 1 per day)

### 4. `/rpg-leaderboard`

- Show top characters by level / XP
- Server-wide leaderboard with formatting

---

## Game Data & Models

### Character Class (ENUM)

- `WARRIOR`, `ROGUE`, `MAGE`

### RPGCharacter

| Field          | Type                 | Description            |
|----------------|----------------------|------------------------|
| discordId      | String               | Unique Discord user ID |
| name           | String               | Chosen character name  |
| class          | Enum                 | Character class        |
| level          | int                  | Starts at 1            |
| xp             | int                  | Experience points      |
| stats          | Map<String, Integer> | HP, STR, AGI, etc.     |
| lastActionTime | Timestamp            | Used for cooldown      |

### RPGActionOutcome

- Result of user actions (text, XP gained, changes)
- Used to generate Discord message responses

---

## Internal Game Logic

| Action      | Logic                              |
|-------------|------------------------------------|
| **Explore** | Random story encounter, XP gain    |
| **Train**   | XP + possible stat increase        |
| **Battle**  | Random AI enemy, HP loss, win = XP |

---

## Configuration

- Per-server RPG settings stored in memory:
    - Is RPG enabled?
    - RPG channel (optional)
    - Cooldown time (default: 24h)
    - Game difficulty (scales XP/stats)

Slash Command: `/rpg-config` (admin only)

---

## Code Guidelines

Cursor AI must:

- Use **interfaces** for action types (ICharacterAction)
- Organize logic under `/rpg/` package
- Store character data in `CharacterService` class
- Use K&R brace style, thorough documentation
- Prepare system for expansion (quests, inventory, etc.)
- Follow `BEST_CODING_PRACTICES.md`

---

## Slash Commands to Implement

| Command              | Description                             |
|----------------------|-----------------------------------------|
| `/rpg-register`      | Create a new character                  |
| `/rpg-profile`       | View your RPG stats                     |
| `/rpg-action <type>` | Perform a daily action                  |
| `/rpg-leaderboard`   | View top players                        |
| `/rpg-config`        | Admin config for game toggle, cooldowns |

---

## Future Phases (Mark as TODOs in code)

| Feature         | Description                                     |
|-----------------|-------------------------------------------------|
| Inventory       | Collect loot, equip weapons, use potions        |
| Quests          | Scripted storylines with objectives and rewards |
| Multiplayer     | Party-based exploration or PvP                  |
| Reputation Ties | Link RPG behavior to MIKROS Reputation Score    |
| Web Dashboard   | View character/profile outside Discord          |

---

## Notes

- RPG Game logic must be **stateless in Discord**, but **stateful internally**
- Game state saved per user (memory first, DB later)
- Bot should notify users if they try to act before cooldown ends
- All player progress tied to Discord ID (no accounts needed)

---

## Suggested Folder Structure

src/
├── rpg/
│ ├── model/
│ │ ├── RPGCharacter.java
│ │ ├── CharacterClass.java
│ │ ├── RPGStats.java
│ ├── service/
│ │ ├── CharacterService.java
│ │ ├── ActionService.java
│ ├── actions/
│ │ ├── ExploreAction.java
│ │ ├── TrainAction.java
│ │ ├── BattleAction.java
│ ├── config/
│ │ └── RPGConfig.java

1. Narrative Quests (Storyline Engine)

Add a section like this:

## TODO: Narrative Quests Engine

Introduce story-based gameplay:

- Players unlock chapters or quests at specific levels
- Each quest presents a situation and choices (like a CYOA)
- Choices affect XP, stats, or inventory
- Some quests branch into multiple outcomes

Command: `/rpg-quest begin`  
Auto-triggers next chapter if conditions met (level, XP)

---

Example:

🧙 “You find a broken amulet glowing with power. Do you…”

1. Take it to the mage
2. Sell it at the black market
3. Wear it yourself

Each choice results in a unique consequence.

Future phase will store quest progress per user.

2. Inventory System & Loot

## TODO: Inventory & Loot

Add a lightweight inventory system:

- Characters can find weapons, potions, and artifacts
- Each has stats/bonuses (e.g. Sword of Fire +5 STR)

Commands:

- `/rpg-inventory` – View items
- `/rpg-equip <item>` – Equip something
- `/rpg-loot` – View most recent item drop

Can be tied into actions and quests.

Use `InventoryService`, `Item`, `ItemType` enums.

3. Boss Battles or World Events

## TODO: Boss Battles & Events

Introduce weekly boss fights:

- Every Sunday, players can `/rpg-bossfight`
- All players contribute damage
- Boss has health bar (persisted)
- If defeated = global rewards

Stats are calculated from character stats + equipped items.

Future expansion: Multi-stage raids.

4. Endgame Ideas (Conclusion + Prestige)

## TODO: Endgame / Prestige System

When a character hits Level 50 (or 100):

- They can “ascend” – reset to Level 1 but gain a permanent bonus (e.g. +5% XP gain)
- Prestige levels displayed in leaderboard
- Unlock cosmetic class skins (flair in embeds)

Purpose:

- Prevent burnout at high levels
- Encourage replayability

5. Integration with MIKROS Ecosystem

## TODO: MIKROS Integration

Use player progress to:

- Unlock MIKROS discount codes
- Reward active players with Reputation Score boosts
- Connect to marketing campaigns (e.g. quest rewards include promo offers)

Requires API coordination – handled via TODOs and `/docs` API planning.

Conclusion
> Note: This RPG system is designed to grow over time. The current phase focuses on daily actions and leveling. Future
> phases will add quests, inventory, boss battles, and prestige to increase depth and replayability.
