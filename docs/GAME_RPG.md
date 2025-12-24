# RPG System Documentation

## Description

The **RPG System** is a text-based role-playing game set in the realm of **Nilfheim** — a world wrapped in cold
twilight, plagued by rising horrors. Players create characters, level up, perform actions, and battle community bosses.
The system features 6 character classes, a dynamic action charge system (3-10 charges based on level), death and recovery
mechanics, epic boss battles, player vs player duels, and an item crafting system for permanent stat bonuses.

## How to Play

1. **Create Your Character:**
    - Use `/rpg-register name:<name> class:<class>`
    - Choose from: **WARRIOR**, **KNIGHT**, **MAGE**, **ROGUE**, **NECROMANCER**, or **PRIEST**
    - One character per user
    - Your soul awakens in Nilfheim...

2. **View Your Profile:**
    - Use `/rpg-profile` to see your stats, charges, and recovery status
    - Check XP progress and action charges remaining

3. **Perform Actions:**
    - Use `/rpg-action type:<action>`
    - Actions: `explore`, `train`, `battle`, or `rest`
    - **Dynamic action charges** (3-10 based on level) that refresh every **12 hours**
    - Use them however you want: all at once or spread out
    - Actions may drop collectible items (essences and catalysts)

4. **Battle Community Bosses:**
    - Use `/rpg-boss-battle attack` to attack the current boss
    - Bosses spawn every 24 hours
    - Community-wide HP pool — everyone fights together!
    - Super bosses appear every 3 normal boss defeats

5. **Check Leaderboard:**
    - Use `/rpg-leaderboard` to see top players
    - Sorted by level, then XP

6. **Challenge Players to Duels:**
    - Use `/rpg-duel target:@player` to challenge another player
    - Free action (no charge cost), but limited to 3 duels per 24 hours
    - Narrative-driven outcomes based on character stats and classes
    - Win/loss tracking for bragging rights

7. **Collect Items and Craft Bonuses:**
    - Items drop from explore, battle, and boss actions
    - Use `/rpg-inventory` to view your collected essences, catalysts, and crafted bonuses
    - Use `/rpg-craft item:<name>` to craft permanent stat bonuses
    - Hard cap: +5 per stat (STR, AGI, INT, LUCK, HP)

## Commands

### Player Commands

| Command                    | Description                                    | Example                                    |
|----------------------------|------------------------------------------------|--------------------------------------------|
| `/rpg-register`            | Create your RPG character                      | `/rpg-register name:Aragorn class:WARRIOR` |
| `/rpg-profile`             | View your character stats                      | `/rpg-profile`                             |
| `/rpg-profile user:<user>` | View another player's profile                  | `/rpg-profile user:@Player`                |
| `/rpg-action`              | Perform an action (uses 1 charge)              | `/rpg-action type:explore`                 |
| `/rpg-resurrect`           | Resurrect a dead player (Priest-only, free)    | `/rpg-resurrect target:@Player`            |
| `/rpg-duel`                | Challenge another player to a duel (free, 3x/24h) | `/rpg-duel target:@Player`              |
| `/rpg-boss-battle`         | Attack boss, check status, or view leaderboard | `/rpg-boss-battle attack`                  |
| `/rpg-leaderboard`         | View top players                               | `/rpg-leaderboard`                         |
| `/rpg-inventory`           | View your collected items and crafted bonuses  | `/rpg-inventory`                           |
| `/rpg-craft`               | Craft permanent stat-boosting items             | `/rpg-craft item:Ember Infusion`           |

**Boss Battle Subcommands:**

- `attack` - Attack the current boss (default)
- `status` - View boss status and progression
- `leaderboard` - View top damage dealers for current boss

### Admin Commands

| Command              | Description          | Permission    |
|----------------------|----------------------|---------------|
| `/admin-rpg-setup`   | Setup RPG system     | Administrator |
| `/admin-rpg-config`  | Configure RPG system | Administrator |

**Setup Command:**
- `/admin-rpg-setup channel:#channel` - Initial setup to configure RPG channel

**Config Subcommands:**

- `view` - View current configuration
- `toggle` - Enable/disable RPG system
- `set-channel` - Restrict RPG to specific channel
- `set-cooldown` - Set charge refresh period (hours, default: 12)
- `set-xp-multiplier` - Set XP multiplier (0.5x - 2.0x)

## Character Classes

### Warrior ⚔️

- **Role:** Bruiser / Tank
- **Strengths:** High HP, High STR
- **Weaknesses:** INT
- **Starting Stats:** HP: 110, STR: 17, AGI: 8, INT: 5, LUCK: 7
- **Best For:** Tanking, melee combat
- **Boss Bonus:** +20% damage vs Beasts

### Knight 🛡️

- **Role:** Full Tank
- **Strengths:** Massive HP, Defense
- **Weaknesses:** Low AGI, Low LUCK
- **Starting Stats:** HP: 135, STR: 13, AGI: 6, INT: 6, LUCK: 5
- **Special:** 15% damage reduction in combat
- **Boss Bonus:** +20% damage vs Giants & Undead

### Mage 🔮

- **Role:** Glass Cannon
- **Strengths:** High INT
- **Weaknesses:** HP
- **Starting Stats:** HP: 70, STR: 5, AGI: 7, INT: 20, LUCK: 5
- **Best For:** Magic damage, spellcasting
- **Boss Bonus:** +20% damage vs Spirits & Elementals

### Rogue 🗡️

- **Role:** Crit / Dodge specialist
- **Strengths:** AGI + LUCK
- **Weaknesses:** Low STR, Low HP
- **Starting Stats:** HP: 85, STR: 8, AGI: 16, INT: 7, LUCK: 12
- **Best For:** Critical hits, dodging
- **Boss Bonus:** +20% damage vs Humanoids & Beasts

### Necromancer 💀

- **Role:** Damage-over-time + crit-magic
- **Strengths:** INT + LUCK
- **Weaknesses:** HP
- **Starting Stats:** HP: 75, STR: 6, AGI: 10, INT: 15, LUCK: 10
- **Special:** 10% chance to apply "Decay" (DoT), doubling XP from battles if triggered
- **Boss Bonus:** +20% damage vs Spirits & Undead

### Priest 🙏

- **Role:** Healer + Resurrector
- **Strengths:** INT + supportive utility
- **Weaknesses:** Offense is weak
- **Starting Stats:** HP: 90, STR: 5, AGI: 6, INT: 15, LUCK: 10
- **Special:** Can resurrect dead players (free action, no charge cost)
- **Boss Bonus:** +20% damage vs Undead & Demonic enemies

## Actions

### Explore 🧭

- **Type:** Safe exploration
- **XP Gain:** 30 + (level × 5) ± 10
- **Risk:** None (no damage)
- **Narratives:** 40+ unique encounter stories (Nilfheim-themed)
- **Item Drops:** 10-15% chance to find 1-2 random essences
- **Best For:** Consistent XP without risk, item collection
- **Charge Cost:** 1

### Train 💪

- **Type:** Stat improvement
- **XP Gain:** 25 + (level × 4) ± 7
- **Stat Gain:** +1 to +3 random stat (STR, AGI, INT, or LUCK)
- **Risk:** None
- **Narratives:** 18 training scenarios
- **Best For:** Building stats over time
- **Charge Cost:** 1

### Battle ⚔️

- **Type:** Combat encounter
- **XP Gain (Victory):** 50 + (level × 10)
- **XP Gain (Defeat):** 20 + (level × 4)
- **XP Gain (Death):** 50% of victory XP
- **Damage (Victory):** Low (5-15 HP)
- **Damage (Defeat):** High (20-40 HP)
- **Death:** Characters can now die (HP reaches 0)
- **Enemies:** 36 enemy types, level-scaled
- **Item Drops:** 20% chance on victory, 5% chance on defeat (1 essence)
- **Risk:** High damage on defeat, possible death
- **Best For:** High XP rewards, but risky
- **Charge Cost:** 1

### Rest 💤

- **Type:** Full HP restore
- **XP Gain:** 0
- **HP Restored:** Full HP
- **Risk:** None
- **Narratives:** 25 rest scenarios
- **Best For:** Recovering after battles
- **Charge Cost:** 1

### Resurrect ✨ (Priest-only, Free Action)

- **Type:** Revive dead players
- **XP Gain:** +5 XP (Priest gets XP for successful resurrection)
- **Target Effect:** Revived at 50% HP, enters 24-hour recovery
- **Charge Cost:** **FREE** (does not consume action charges)
- **Usage:** `/rpg-resurrect target:@Player`

### Duel ⚔️ (Free Action, Rate Limited)

- **Type:** Player vs Player combat
- **XP Gain:** None
- **HP Damage:** None (no damage or death from duels)
- **Rate Limit:** 3 duels per 24 hours
- **Charge Cost:** **FREE** (does not consume action charges)
- **Outcome:** Narrative-driven based on character stats, classes, and luck
- **Rewards:** Win/loss record tracking only
- **Usage:** `/rpg-duel target:@Player`

## Dynamic Action Charge System

**NEW:** The action system has been overhauled with dynamic charges based on level!

- **Old System:** 1 action per 24 hours (cooldown-based)
- **New System:** Dynamic action charges (3-10 based on level), refresh every 12 hours

### How It Works:

- Start with **3 action charges** (levels 1-2)
- Maximum charges increase as you level up following the **Fibonacci sequence**
- Each action (explore, train, battle, rest) consumes **1 charge**
- Charges refresh every **12 hours** (all charges restored to current max)
- Use charges however you want:
    - All at once: `explore → train → battle`
    - Spread out: `battle` (wait) → `rest` (wait) → `explore`
    - Any combination you prefer!

### Charge Progression (Fibonacci Sequence):

| Level | Max Charges | Notes |
|-------|-------------|-------|
| 1-2   | 3           | Starting charges |
| 3     | 4           | +1 charge bonus on level up |
| 4     | 4           | |
| 5     | 5           | +1 charge bonus on level up |
| 6-7   | 5           | |
| 8     | 6           | +1 charge bonus on level up |
| 9-12  | 6           | |
| 13    | 7           | +1 charge bonus on level up |
| 14-20 | 7           | |
| 21    | 8           | +1 charge bonus on level up |
| 22-33 | 8           | |
| 34    | 9           | +1 charge bonus on level up |
| 35-54 | 9           | |
| 55    | 10          | +1 charge bonus on level up (maximum) |

**Key Points:**
- When you level up to a Fibonacci threshold (3, 5, 8, 13, 21, 34, 55), you immediately receive +1 charge as a bonus
- Maximum charges cap at **10** (reached at level 55)
- This system prevents power creep while rewarding long-term players

### Charge Status:

- View charges in `/rpg-profile`
- Shows: "Charges: 2/5" (current/max) and time until next refresh
- Cannot act if dead or in recovery (even with charges)

## Dual System

The Dual System allows players to challenge each other to narrative-driven PvP duels. These are friendly competitions that don't affect HP or XP, but track win/loss records.

### Mechanics

- **No Charge Cost:** Duels are free actions and don't consume action charges
- **Rate Limit:** Maximum 3 duels per 24 hours per player
- **No HP Damage:** Duels don't cause HP loss or death
- **No XP Rewards:** Duels are purely for bragging rights
- **No Level Restrictions:** Any player can challenge any other player (if both agree)

### How Duels Work

1. **Challenge:** Use `/rpg-duel target:@player` to challenge another player
2. **Validation:** Both players must have characters and be alive/not recovering
3. **Calculation:** Winner determined by:
   - Character stats (STR, AGI, INT, LUCK)
   - Character class bonuses
   - Luck modifiers (LUCK × 2 added to power)
4. **Narrative:** A story is generated explaining the outcome based on classes and stats
5. **Tracking:** Win/loss records are updated for both players

### Duel Records

- View your duel record in `/rpg-profile`
- Shows: "Duels: X Wins | Y Losses"
- Records persist through death/resurrection
- Rate limit resets every 24 hours

### Example Duel Outcome

> A fierce duel erupted between **Aragorn** (⚔️ Warrior) and **Gandalf** (🔮 Mage)!
>
> The Warrior's brute strength proved superior to the Mage's lack of magical defense.
>
> **Outcome:** Aragorn emerged victorious!

## Item & Crafting System

The Item & Crafting System allows players to collect essences and catalysts during gameplay, then craft them into permanent stat bonuses.

### Item Types

#### Essences (Common Collectibles)

Essences are stat-aligned collectibles that drop from actions:

| Essence | Emoji | Stat Alignment | Source |
|---------|-------|---------------|--------|
| **Ember Shard** | 🔥 | STR | Explore, Battle, Bosses |
| **Gale Fragment** | 🌪️ | AGI | Explore, Battle, Bosses |
| **Mind Crystal** | 🔮 | INT | Explore, Battle, Bosses |
| **Fate Clover** | 🍀 | LUCK | Explore, Battle, Bosses |
| **Vital Ash** | 🩸 | HP | Explore, Battle, Bosses |

#### Catalysts (Rare Items)

Catalysts are rarer items required for crafting:

| Catalyst | Emoji | Primary Source |
|----------|-------|----------------|
| **Ancient Vial** | ⚗️ | Explore, Bosses |
| **Runic Binding** | 📜 | Bosses |
| **Monster Core** | 💎 | Bosses |
| **Frozen Reagent** | ❄️ | Explore, Bosses |

### Drop Rates

**Explore Action:**
- 10-15% chance to find 1-2 random essences

**Battle Action:**
- 20% chance on victory (1 essence)
- 5% chance on defeat (1 essence)

**Boss Battles:**
- **Normal Boss Victory:** Guaranteed 1 essence + 25% chance for 1 catalyst
- **Super Boss Victory:** Guaranteed 1 catalyst + 1-3 essences
- **Note:** Boss drops only occur on victory

### Craftable Items

Players can craft permanent stat bonuses using essences and catalysts:

| Crafted Item | Emoji | Recipe | Stat Bonus |
|--------------|-------|--------|------------|
| **Ember Infusion** | 🔥 | 5x Ember Shard + 1x Ancient Vial | +1 STR |
| **Gale Etching** | 🌪️ | 5x Gale Fragment + 1x Ancient Vial | +1 AGI |
| **Mind Sigil** | 🔮 | 4x Mind Crystal + 1x Runic Binding | +1 INT |
| **Charm of Fortune** | 🍀 | 4x Fate Clover + 1x Runic Binding | +1 LUCK |
| **Vital Rune** | 🩸 | 3x Vital Ash + 1x Monster Core | +5 HP |

### Crafting Rules

- **Hard Cap:** +5 per stat (STR, AGI, INT, LUCK, HP)
- **No Global Cap:** Each stat has its own +5 cap (total possible: +5 STR, +5 AGI, +5 INT, +5 LUCK, +5 HP)
- **Permanent:** Crafted bonuses persist through death/resurrection
- **No Level Requirement:** Any player can craft if they have materials
- **Materials Consumed:** Crafting consumes the required essences and catalysts

### Commands

- `/rpg-inventory` - View your collected essences, catalysts, and current crafted bonuses
- `/rpg-craft item:<name>` - Craft a permanent stat-boosting item

### Item Display

Items are displayed inline in action result messages:

> 🧭 You wander the frozen remains of an alchemist's hut...
> 
> Amid shattered glass, something glimmers.
> 
> **Found:** 🔥 Ember Shard ×2

## Death and Recovery System

### Death

- Characters can die when HP reaches 0 in battle
- Dead characters:
    - Cannot perform actions
    - Cannot attack bosses
    - Can be resurrected by Priests

### Recovery

- After resurrection, characters enter **Recovery** for 24 hours
- During recovery:
    - HP set to 50% of max
    - Cannot perform actions
    - Cannot attack bosses
    - Recovery timer shown in profile

### Resurrection

- **Priest-only** action (free, no charge cost)
- If target is **alive:** Priest gives a blessing (+2 XP)
- If target is **dead:** Target is resurrected at 50% HP, enters recovery
- Priest gets +5 XP for successful resurrection

## Boss System

### Normal Bosses

- **Spawn:** One boss every 24 hours
- **HP:** 10,000 × boss level
- **Community Battle:** Everyone shares the same HP pool
- **Progression:** Boss level increases when `TotalDefeated >= 6 × currentBossLevel`
    - Level 1 → 2: Need 6 defeats
    - Level 2 → 3: Need 12 defeats
    - Level 3 → 4: Need 18 defeats
    - And so on...

### Super Bosses

- **Spawn:** Every 3 normal boss defeats
- **HP:** 50,000 × super boss level
- **Special Mechanics:** Each super boss has unique abilities
- **Progression:** Super boss level increases when `SuperBossesDefeated >= 2 × superBossLevel`
    - Level 1 → 2: Need 2 defeats
    - Level 2 → 3: Need 4 defeats
    - And so on...

### Boss Types

Bosses have types that determine class bonuses:

- **Beast** 🐺 - Warrior, Rogue bonus
- **Giant** 👹 - Warrior, Knight bonus
- **Undead** 💀 - Knight, Priest, Necromancer bonus
- **Spirit** 👻 - Mage, Priest bonus
- **Elemental** ⚡ - Mage bonus
- **Humanoid** ⚔️ - Rogue bonus
- **Eldritch** 🌌 - Mage, Priest bonus
- **Construct** 🤖 - Mage, Priest bonus
- **Dragon** 🐉 - Mage, Warrior bonus
- **Demon** 😈 - Priest bonus

### Boss Battle Commands

- `/rpg-boss-battle attack` - Attack the current boss
- `/rpg-boss-battle status` - View boss HP, level, and progression
- `/rpg-boss-battle leaderboard` - View top damage dealers

### Boss Catalog

- **24 Normal Bosses:** 2 per level (levels 1-12)
- **12 Super Bosses:** Epic world-tier threats
- Each boss has unique lore, type, and mechanics

### Boss Rewards

**Normal Boss Victory:**
- Guaranteed 1 essence drop
- 25% chance for 1 catalyst drop
- XP rewards based on damage dealt

**Super Boss Victory:**
- Guaranteed 1 catalyst drop
- 1-3 random essence drops
- XP rewards based on damage dealt

**Note:** Boss drops only occur on victory, not defeat.

## Scoring Rules

### Level Progression

- **XP Formula:** Exponential growth (100 × level^1.5)
- **Level 1 → 2:** ~100 XP
- **Level 2 → 3:** ~283 XP
- **Level 3 → 4:** ~520 XP
- Each level requires more XP than the previous

### Stat Growth

- **Training:** +1 to +3 random stat per action
- **Level Up:** +5 HP, +1 to all stats
- **Crafting:** Permanent stat bonuses from crafted items (capped at +5 per stat)
- **Class Bonuses:** Applied during combat and boss battles

### Combat Calculation

- **Warrior/Knight:** STR-based damage
- **Mage/Necromancer/Priest:** INT-based damage
- **Rogue:** AGI-based damage, LUCK affects crits
- **Enemy Scaling:** Enemy level = player level ± 2
- **Knight Special:** 15% damage reduction

## Game Flow

1. **Character Creation:**
    - Player registers with name and class
    - Character starts at Level 1 with class-specific stats
    - Character receives 3 action charges
    - Welcome message includes Nilfheim lore

2. **Action System:**
    - Player performs actions using charges
    - Charges refresh every 12 hours
    - Can use all 3 at once or spread them out
    - Actions: explore, train, battle, rest

3. **Boss Battles:**
    - Boss spawns every 24 hours
    - Community attacks boss together
    - Damage tracked per player
    - Boss defeated when HP reaches 0
    - Progression tracked per server

4. **Leveling Up:**
    - When XP threshold reached, character levels up
    - HP increases by 5
    - All stats increase by 1
    - If leveling to a Fibonacci threshold (3, 5, 8, 13, 21, 34, 55), max charges increase by 1
    - New level unlocks more XP per action

5. **Death and Recovery:**
    - Character dies if HP reaches 0
    - Priest can resurrect (free action)
    - Resurrected characters enter 24h recovery
    - Cannot act during recovery

6. **Leaderboard:**
    - Sorted by level (highest first)
    - Secondary sort by XP
    - Shows top 10 players

7. **Item Collection & Crafting:**
    - Collect essences and catalysts from actions
    - View inventory with `/rpg-inventory`
    - Craft permanent stat bonuses with `/rpg-craft`
    - Bonuses persist through death/resurrection

8. **Player Duels:**
    - Challenge other players with `/rpg-duel`
    - Free action (no charge cost)
    - Rate limited to 3 per 24 hours
    - Win/loss records tracked in profile

## Nilfheim Lore

The RPG is set in **Nilfheim** — a realm wrapped in cold twilight, plagued by rising horrors. Heroes are few. Legends
are fewer. Yet fate stirs… and your journey begins.

### Lore Integration:

- Character registration mentions Nilfheim
- Boss failure: "The shadows spread across Nilfheim…"
- Boss victory: "A heroic roar echoes through Nilfheim as the monster falls. Hope flickers brighter."
- All narratives themed around frozen tundras, spirits, and ancient magic

## Configuration

**Per-Server Settings:**

- Enabled/disabled status
- Channel restriction (optional)
- Charge refresh period (default: 12 hours)
- XP multiplier (default: 1.0x)

**Default Settings:**

- Charge Refresh: 12 hours
- XP Multiplier: 1.0x
- No channel restriction
- System enabled by default

## Boss Progression Examples

### Normal Boss Progression:

- **Boss Level 1:** Need 6 defeats → Level 2
- **Boss Level 2:** Need 12 defeats → Level 3
- **Boss Level 3:** Need 18 defeats → Level 4
- Continues indefinitely

### Super Boss Progression:

- **Super Boss Level 1:** Need 2 defeats → Level 2
- **Super Boss Level 2:** Need 4 defeats → Level 3
- **Super Boss Level 3:** Need 6 defeats → Level 4
- Continues indefinitely

### Super Boss Spawning:

- After every 3 normal boss defeats, a super boss spawns
- After super boss battle (win or lose), counter resets
- Next 3 normal bosses → another super boss

## Future TODOs

- 🔮 **Quests:** Story-driven quest chains
- 🔮 **Multiplayer:** Party system, guild battles
- 🔮 **Prestige System:** Reset with bonuses
- 🔮 **Skills:** Class-specific abilities
- 🔮 **Dungeons:** Multi-stage challenges
- 🔮 **Weapons & Armor:** Equipment system with stat bonuses

---

**Last Updated:** 2025-01-27  
**Game Type:** Text-Based RPG  
**Realm:** Nilfheim  
**Command Prefix:** `rpg-*`  
**Action System:** Dynamic charges (3-10 based on level), 12h refresh  
**Boss System:** 24 normal + 12 super bosses  
**Features:** Dual System, Item & Crafting System, Dynamic Charges
