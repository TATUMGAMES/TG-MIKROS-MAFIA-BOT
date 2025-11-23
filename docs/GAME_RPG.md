# RPG System Documentation

## Description

The **RPG System** is a text-based role-playing game where players create characters, level up, and perform daily actions to progress. Players choose from three classes (Warrior, Mage, Rogue) and engage in exploration, training, and battles.

## How to Play

1. **Create Your Character:**
   - Use `/rpg-register name:<name> class:<class>`
   - Choose from: WARRIOR, MAGE, or ROGUE
   - One character per user

2. **View Your Profile:**
   - Use `/rpg-profile` to see your stats
   - Check cooldown status and XP progress

3. **Perform Daily Actions:**
   - Use `/rpg-action type:<action>`
   - Actions: `explore`, `train`, or `battle`
   - One action per cooldown period (default: 24 hours)

4. **Check Leaderboard:**
   - Use `/rpg-leaderboard` to see top players
   - Sorted by level, then XP

## Commands

### Player Commands

| Command | Description | Example |
|---------|-------------|---------|
| `/rpg-register` | Create your RPG character | `/rpg-register name:Aragorn class:WARRIOR` |
| `/rpg-profile` | View your character stats | `/rpg-profile` |
| `/rpg-profile user:<user>` | View another player's profile | `/rpg-profile user:@Player` |
| `/rpg-action` | Perform daily action | `/rpg-action type:explore` |
| `/rpg-leaderboard` | View top players | `/rpg-leaderboard` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/admin-rpg-config` | Configure RPG system | Administrator |

**Admin Subcommands:**
- `view` - View current configuration
- `toggle` - Enable/disable RPG system
- `set-channel` - Restrict RPG to specific channel
- `set-cooldown` - Set action cooldown (hours)
- `set-xp-multiplier` - Set XP multiplier (0.5x - 2.0x)

## Character Classes

### Warrior
- **Strengths:** High HP, High STR
- **Starting Stats:** HP: 100, STR: 15, AGI: 8, INT: 5, LUCK: 7
- **Best For:** Tanking, melee combat
- **Color:** Red

### Mage
- **Strengths:** High INT, Moderate HP
- **Starting Stats:** HP: 70, STR: 5, AGI: 7, INT: 18, LUCK: 5
- **Best For:** Magic damage, spellcasting
- **Color:** Cyan

### Rogue
- **Strengths:** High AGI, High LUCK
- **Starting Stats:** HP: 80, STR: 8, AGI: 15, INT: 7, LUCK: 12
- **Best For:** Critical hits, dodging
- **Color:** Orange

## Actions

### Explore
- **Type:** Safe exploration
- **XP Gain:** 30 + (level × 5) ± 10
- **Risk:** None (no damage)
- **Narratives:** 15 unique encounter stories
- **Best For:** Consistent XP without risk

### Train
- **Type:** Stat improvement
- **XP Gain:** 25 + (level × 4) ± 7
- **Stat Gain:** +1 to +3 random stat (STR, AGI, INT, or LUCK)
- **Risk:** None
- **Narratives:** 8 training scenarios
- **Best For:** Building stats over time

### Battle
- **Type:** Combat encounter
- **XP Gain (Victory):** 50 + (level × 10)
- **XP Gain (Defeat):** 20 + (level × 4)
- **Damage (Victory):** Low (5-15 HP)
- **Damage (Defeat):** High (20-40 HP)
- **Enemies:** 16 enemy types, level-scaled
- **Risk:** High damage on defeat
- **Best For:** High XP rewards, but risky

## Scoring Rules

### Level Progression
- **XP Formula:** Exponential growth
- **Level 1 → 2:** 100 XP
- **Level 2 → 3:** 200 XP
- **Level 3 → 4:** 400 XP
- **Each Level:** Previous × 2

### Stat Growth
- **Training:** +1 to +3 random stat per action
- **Level Up:** +5 HP, +1 to all stats
- **Class Bonuses:** Applied during combat

### Combat Calculation
- **Warrior:** STR-based damage
- **Mage:** INT-based damage
- **Rogue:** AGI-based damage, LUCK affects crits
- **Enemy Scaling:** Enemy level = player level ± 2

## Game Flow

1. **Character Creation:**
   - Player registers with name and class
   - Character starts at Level 1 with class-specific stats
   - Character is ready for actions

2. **Daily Actions:**
   - Player performs action (explore, train, battle)
   - Action outcome calculated
   - XP and stats updated
   - Cooldown timer starts

3. **Leveling Up:**
   - When XP threshold reached, character levels up
   - HP increases by 5
   - All stats increase by 1
   - New level unlocks more XP per action

4. **Leaderboard:**
   - Sorted by level (highest first)
   - Secondary sort by XP
   - Shows top 10 players

## Narrative/Explanations

**Character Registration:**
```
⚔️ Character Created! ⚔️

Name: Aragorn
Class: Warrior
Level: 1

Stats:
❤️ HP: 100/100
💪 STR: 15
🏃 AGI: 8
🧠 INT: 5
🍀 LUCK: 7

Use /rpg-action to start your adventure!
```

**Explore Action:**
```
🗺️ Exploration Complete!

You discovered an ancient temple...

XP Gained: +45
Total XP: 145/200 (Level 2)

Next action available in 24 hours.
```

**Battle Action (Victory):**
```
⚔️ Battle Won!

You defeated a Level 3 Goblin!

XP Gained: +80
Damage Taken: -12 HP
Current HP: 88/100

Next action available in 24 hours.
```

**Battle Action (Defeat):**
```
💀 Battle Lost!

You were defeated by a Level 5 Orc...

XP Gained: +28 (defeat bonus)
Damage Taken: -35 HP
Current HP: 65/100

Next action available in 24 hours.
```

## Configuration

**Per-Server Settings:**
- Enabled/disabled status
- Channel restriction (optional)
- Action cooldown (default: 24 hours)
- XP multiplier (default: 1.0x)

**Default Settings:**
- Cooldown: 24 hours
- XP Multiplier: 1.0x
- No channel restriction
- System enabled by default

## Future TODOs

- 🔮 **Inventory System:** Items, weapons, armor
- 🔮 **Quests:** Story-driven quest chains
- 🔮 **Multiplayer:** Party system, guild battles
- 🔮 **Prestige System:** Reset with bonuses
- 🔮 **Skills:** Class-specific abilities
- 🔮 **Crafting:** Create items from materials
- 🔮 **Dungeons:** Multi-stage challenges
- 🔮 **PvP:** Player vs player battles

---

**Last Updated:** 2025-10-08  
**Game Type:** Text-Based RPG  
**Command Prefix:** `rpg-*`





