# TASKS_27: Enemy Kill Counter System

## Objective
Implement a kill counter system that tracks the number of enemies, bosses, and super bosses defeated by each player. Display concise kill counts whenever an enemy is defeated, and provide a dedicated stats command for detailed cumulative statistics.

## Overview
Currently, the RPG system tracks:
- **Regular Enemies**: Defeated in `BattleAction.java` when `victory == true`, but no kill count is tracked
- **Bosses**: Defeated in `BossService.java`, tracked at server level only (not per user)
- **Super Bosses**: Same as bosses - server level only

We need to:
1. Add kill counters to `RPGCharacter` model
2. Increment counters when enemies/bosses are defeated
3. Display **concise** kill counts in battle results (immediate gratification, minimal spam)
4. Create a new `/rpg-stats` command for detailed cumulative statistics

---

## Implementation Plan

### Phase 1: Add Kill Counters to RPGCharacter Model

**File:** `src/main/java/com/tatumgames/mikros/games/rpg/model/RPGCharacter.java`

**Changes:**
1. Add three new private fields:
   - `private int enemiesKilled = 0;` - Regular enemies defeated
   - `private int bossesKilled = 0;` - Normal bosses defeated
   - `private int superBossesKilled = 0;` - Super bosses defeated

2. Add getter methods:
   - `public int getEnemiesKilled()`
   - `public int getBossesKilled()`
   - `public int getSuperBossesKilled()`

3. Add increment methods:
   - `public void incrementEnemiesKilled()` - Increments enemiesKilled by 1
   - `public void incrementBossesKilled()` - Increments bossesKilled by 1
   - `public void incrementSuperBossesKilled()` - Increments superBossesKilled by 1

4. Initialize all counters to 0 in the constructor (already default, but explicit is better)

**Note:** These counters should persist with the character (currently in-memory, but will work with future database persistence).

---

### Phase 2: Track Regular Enemy Kills

**File:** `src/main/java/com/tatumgames/mikros/games/rpg/actions/BattleAction.java`

**Changes:**
1. In the `execute()` method, after determining `victory == true`:
   - Call `character.incrementEnemiesKilled()` when the player wins
   - This should be done right after the victory check (around line 80)

**Location:** After line 80, inside the `if (victory)` block

---

### Phase 3: Track Boss Kills

**File:** `src/main/java/com/tatumgames/mikros/games/rpg/service/BossService.java`

**Changes:**
1. Add `CharacterService` as a field:
   - `private final CharacterService characterService;`

2. Update constructor:
   - Add `CharacterService characterService` parameter
   - Store it: `this.characterService = characterService;`

3. Modify `handleBossDefeat()` method to accept `CharacterService`:
   - Change from: `private void handleBossDefeat(String guildId, boolean isNormalBoss)`
   - To: `private void handleBossDefeat(String guildId, boolean isNormalBoss)`
   - Use the field `this.characterService` instead of parameter

4. Get all players who dealt damage to the boss:
   - Use `damageTracking.get(guildId)` to get the map of userId -> damage
   - Iterate through all players who dealt damage

5. Increment kill counters for all participants:
   - For each userId in damage tracking, get their character using `characterService.getCharacter(userId)`
   - If character exists, call:
     - `character.incrementBossesKilled()` for normal bosses
     - `character.incrementSuperBossesKilled()` for super bosses

6. Update the call site in `attackBoss()`:
   - No changes needed - method signature stays the same, uses field

**Note:** Since bosses are community-wide, ALL players who dealt damage get credit for the kill. This is intentional - it rewards participation.

**Location:** In `handleBossDefeat()` method, after line 226 (before clearing damage tracking)

---

### Phase 4: Display Concise Kill Counts in Battle Results

**File:** `src/main/java/com/tatumgames/mikros/games/rpg/commands/RPGActionCommand.java`

**Changes:**
1. In the battle result display (around line 177-206):
   - When `actionType.equals("battle")` and `outcome.success() == true`
   - Add a concise kill count line to the results StringBuilder

2. Format (concise, minimal):
   ```java
   if (actionType.equals("battle") && outcome.success()) {
       results.append(String.format("\n💀 Enemies Defeated: %d", 
           character.getEnemiesKilled()));
   }
   ```

**Location:** Around line 189, after damage/hp restoration checks, before the level up check

---

### Phase 5: Display Concise Kill Counts in Boss Battle Results

**File:** `src/main/java/com/tatumgames/mikros/games/rpg/commands/RPGBossBattleCommand.java`

**Changes:**
1. In `handleAttack()` method, when boss is defeated (around line 177):
   - Add concise kill count information to the victory embed
   - Format: "🏆 Bosses Defeated: X" or "👹 Super Bosses Defeated: X"

2. Add to the embed when `defeated == true`:
   ```java
   if (defeated) {
       embed.setColor(Color.GREEN);
       embed.addField("🎉 Victory!",
           "The shadows spread across Nilfheim… but this boss has fallen! A heroic roar echoes through the realm as hope flickers brighter.",
           false);
       
       // Add concise kill count
       if (boss != null) {
           embed.addField("🏆 Bosses Defeated", 
               String.format("%d", character.getBossesKilled()),
               true);
       } else {
           embed.addField("👹 Super Bosses Defeated",
               String.format("%d", character.getSuperBossesKilled()),
               true);
       }
   }
   ```

**Location:** Around line 177-181, inside the `if (defeated)` block

---

### Phase 6: Create New `/rpg-stats` Command

**File:** `src/main/java/com/tatumgames/mikros/games/rpg/commands/RPGStatsCommand.java` (NEW FILE)

**Purpose:** Dedicated command for detailed cumulative statistics. Keeps profile command focused on character info, and provides comprehensive stats view.

**Implementation:**
1. Create new class `RPGStatsCommand` implementing `CommandHandler`
2. Command name: `rpg-stats`
3. Description: "View detailed RPG statistics including kill counts and character progress"
4. Optional parameter: `user` (User) - View another user's stats

**Display Format:**
```
Embed Title: "📊 RPG Statistics - [Character Name]"

Fields:
1. "⚔️ Combat Statistics"
   - 💀 Enemies Defeated: X
   - 🐲 Bosses Defeated: X
   - 👹 Super Bosses Defeated: X

2. "📈 Character Progress"
   - Level: X
   - XP: X / X (X%)
   - XP to Next Level: X

3. "❤️ Current Status"
   - HP: X / X
   - Action Charges: X / 3
   - Status: [Alive/Dead/Recovering]

4. "📊 Stats"
   - ⚔️ STR: X
   - 🏃 AGI: X
   - 🧠 INT: X
   - 🍀 LUCK: X

Footer: Character created [timestamp]
```

**Structure:**
- Follow pattern from `RPGProfileCommand.java`
- Use `CharacterService` to get character
- Support optional `user` parameter to view others' stats
- Use EmbedBuilder for formatting
- Color based on character class (reuse logic from RPGProfileCommand)

**Location:** Create new file in `src/main/java/com/tatumgames/mikros/games/rpg/commands/`

---

### Phase 7: Register New Command

**File:** `src/main/java/com/tatumgames/mikros/bot/BotMain.java` (or wherever commands are registered)

**Changes:**
1. Find where RPG commands are registered
2. Add `RPGStatsCommand` to the command handlers list
3. Ensure it's initialized with `CharacterService` dependency

**Note:** Search for where `RPGProfileCommand`, `RPGActionCommand`, etc. are instantiated and registered.

---

### Phase 8: Update BossService Instantiation

**File:** Wherever `BossService` is created

**Changes:**
1. Find where `new BossService()` is called
2. Update to pass `CharacterService` instance:
   - `new BossService(characterService)`

**Files to check:**
- Search for `new BossService()` in the codebase
- Likely in `BotMain.java` or a service initialization class

---

## Testing Checklist

- [ ] Regular enemy kills increment when player wins a battle
- [ ] Concise kill count displays in battle result message (format: "💀 Enemies Defeated: X")
- [ ] Boss kills increment for all participants when boss is defeated
- [ ] Super boss kills increment for all participants when super boss is defeated
- [ ] Concise kill count displays in boss battle victory message
- [ ] `/rpg-stats` command displays all statistics correctly
- [ ] `/rpg-stats user:@someone` works to view others' stats
- [ ] Counters persist (test by performing multiple actions)
- [ ] Counters start at 0 for new characters
- [ ] Multiple players attacking same boss all get credit
- [ ] Profile command remains focused (no kill counts added there)

---

## Edge Cases to Handle

1. **Character is null**: When crediting boss kills, check if character exists before incrementing
2. **Boss expires without defeat**: No kills should be credited (already handled - only on defeat)
3. **Player defeats enemy but dies**: Still count as kill (victory happened before death)
4. **Multiple battles in same session**: Counters should accumulate correctly
5. **User doesn't have character**: `/rpg-stats` should show appropriate error message

---

## Implementation Order

1. ✅ Phase 1: Add fields and methods to RPGCharacter
2. ✅ Phase 2: Track regular enemy kills in BattleAction
3. ✅ Phase 8: Update BossService constructor and dependencies
4. ✅ Phase 3: Track boss kills in BossService
5. ✅ Phase 4: Display concise kills in battle results
6. ✅ Phase 5: Display concise kills in boss battle results
7. ✅ Phase 6: Create new RPGStatsCommand
8. ✅ Phase 7: Register new command

---

## Design Decisions

### Why Separate Stats Command?
- **Profile** (`/rpg-profile`): Focused on character identity, current status, and basic info
- **Stats** (`/rpg-stats`): Comprehensive cumulative statistics, kill counts, and detailed progress
- This separation keeps commands focused and prevents information overload

### Why Concise Messages?
- Battle results should provide immediate gratification without flooding the channel
- Detailed stats are available on-demand via `/rpg-stats`
- Balances engagement with minimal spam
- Scales well for future features (leaderboards, achievements)

### Kill Count Format
- **Battle Results**: "💀 Enemies Defeated: 12" (single line, concise)
- **Boss Results**: "🏆 Bosses Defeated: 3" or "👹 Super Bosses Defeated: 1" (inline field)
- **Stats Command**: Full breakdown with all statistics

---

## Notes

- Kill counters are per-character (per Discord user)
- Boss kills are credited to ALL participants (community effort)
- Regular enemy kills are only credited to the player who won
- Counters start at 0 and increment only on successful defeats
- Future database persistence will automatically include these fields if RPGCharacter is serialized
- Profile command remains unchanged (no kill counts added) to keep it focused

---

## Future Enhancements (Optional)

- Leaderboard for most kills (`/rpg-leaderboard` with kill-based sorting)
- Achievement system based on kill milestones
- Kill streaks (consecutive victories)
- Special titles for high kill counts
- Weekly/monthly kill leaderboards
- Gold/currency system (track in stats)
- Inventory system (track items in stats)

