# Death Mechanics Documentation

## Overview

This document explains how death works in the RPG system, what happens when a player dies, how resurrection works, and
what messages are displayed to players.

## How Death Occurs

Death occurs when a character's HP reaches 0 during battle. This happens in the following ways:

1. **Battle Action**: When a player uses `/rpg-action type:battle` and loses HP that brings them to 0 or below
2. **Enemy Damage**: Damage from enemies during battle can reduce HP to 0
3. **Elite Enemy Explosions**: Elite enemies with the "Unstable Essence" trait explode on death, dealing 5% max HP as
   unavoidable damage

## What Happens When a Player Dies

When a character's HP reaches 0:

1. **Character State**:
    - Character is marked as dead (`isDead = true`)
    - HP is set to 0 (`stats.setCurrentHp(0)`)
    - Death count is incremented (`incrementDeathCount()`)

2. **Death Message**:
    - Displayed in the action result embed
    - **Message**: "💀 **YOU HAVE DIED!** A Priest can resurrect you."
    - **Visibility**: Public (shown in the `/rpg-action` result message)
    - **Location**: Shown in the "📊 Results" field of the action embed

3. **XP Gain**:
    - Dead characters still receive XP, but at a reduced rate (50% of normal XP)
    - This applies even if the character died during the battle

4. **Oathbreaker Special Mechanics**:
    - Oathbreaker characters have special death mechanics:
        - **30% chance**: Lose 2-3 corruption (despair purges some)
        - **20% chance**: Gain 1-2 corruption (despair strengthens oath)
        - **10% chance**: Vision encounter (special narrative)
        - **40% chance**: Normal death (no corruption change)

5. **Character Restrictions**:
    - Dead characters cannot perform actions
    - Dead characters cannot use `/rpg-action` commands
    - Dead characters cannot use `/rpg-boss-battle` commands
    - Dead characters cannot duel
    - Dead characters can still view their profile and inventory

## Resurrection Process

### Who Can Resurrect

Only **Priests** can resurrect dead players using the `/rpg-resurrect` command.

### Resurrection Command

- **Command**: `/rpg-resurrect target:@Player`
- **Cost**: FREE (does not consume action charges)
- **Requirement**: Only Priests can use this command
- **Usage**: Priest must use the command and target a dead player

### Resurrection Effects

When a character is resurrected:

1. **Character State**:
    - Character is marked as alive (`isDead = false`)
    - Character enters recovery period (`isRecovering = true`)
    - HP is restored to 50% of max HP (`stats.setCurrentHp(maxHp / 2)`)
    - Recovery period is set to 24 hours by default

2. **Recovery Period**:
    - **Default**: 24 hours
    - **With "Fading Hope" Curse**: 36 hours
    - During recovery, the character cannot perform actions
    - Recovery timer: `recoverUntil` is set to current time + recovery hours

3. **Resurrection Message**:
    - **Format**: "✨ Resurrection" embed
    - **Visibility**: Public (shown in the `/rpg-resurrect` command result)
    - **Content**:
        - Narrative describing the resurrection
        - XP reward for the Priest (if any)
        - Recovery status (24 hours or 36 hours)
    - **Priest Reward**: Priest receives XP for performing resurrection

4. **Resurrection Tracking**:
    - Resurrection count is incremented (`incrementResurrectionCount()`)
    - This is tracked for achievements and lore recognition

### Resurrection Failure Cases

- If target is not dead: Resurrection fails, no changes occur
- If target is already recovering: Resurrection fails (character is not dead)
- If Priest is not a Priest class: Command fails (should not be possible due to command checks)

## What Happens If Not Resurrected

Currently, there is **no automatic character deletion** or **24-hour timer** for dead characters.

- Dead characters remain dead indefinitely
- They cannot perform actions while dead
- They can still view their profile, inventory, and other information
- There is no penalty for staying dead (except inability to play)

**Future Considerations:**

- Should there be a 24-hour timer before character is permanently lost?
- Should dead characters be able to view profile/inventory?
- Should there be alternative resurrection methods?

## Message Examples

### Death Message (Public)

When a character dies during battle, this message appears in the action result:

```
💝 Donate - Action Complete!
[Other battle results...]

📊 Results
✨ +0 XP
💔 -50 HP

💀 **YOU HAVE DIED!** A Priest can resurrect you.

Character Status
Level 5 • 1086/1118 XP
❤️ HP: 0/155
```

### Resurrection Message (Public)

When a Priest successfully resurrects a character:

```
✨ Resurrection

Dark sigils flare as {priest} channels forbidden magic...
{target}'s soul is pulled back from the void, returning to the realm of the living.

⚠️ Status
Target is now in recovery for 24 hours

Resurrection is a free action (no charge cost)
```

## Recovery Period

During the recovery period:

- Character is marked as `isRecovering = true`
- Recovery ends when `recoverUntil` time is reached
- Character cannot perform actions during recovery
- Recovery is automatically cleared when the timer expires (checked on action attempts)

## Code References

### Death Implementation

- **Death Method**: `RPGCharacter.die()` (line 425)
    - Sets `isDead = true`
    - Sets `stats.setCurrentHp(0)`
    - Called when HP reaches 0 during battle

- **Death Check**: `BattleAction.java` (line 1079-1114)
    - Checks if character HP <= 0 after damage
    - Calls `character.die()` if dead
    - Adds death narrative to outcome

- **Death Display**: `RPGActionCommand.java` (line 245-247)
    - Checks `character.isDead()` after action
    - Displays death message in results field

### Resurrection Implementation

- **Resurrection Method**: `RPGCharacter.resurrect(int recoveryHours)` (line 435)
    - Sets `isDead = false`
    - Sets `isRecovering = true`
    - Sets HP to 50% of max
    - Sets recovery timer

- **Resurrection Command**: `RPGResurrectCommand.java`
    - Handles `/rpg-resurrect` command
    - Validates Priest class
    - Calls resurrection action

- **Resurrection Action**: `ResurrectAction.java`
    - Executes resurrection logic
    - Applies Fading Hope curse extension (24h → 36h)
    - Returns XP reward for Priest

## Summary

- **Death**: Occurs when HP reaches 0 in battle
- **Death Message**: Public, shown in action result
- **Resurrection**: Priest-only, free action, restores to 50% HP, 24h recovery
- **If Not Resurrected**: Character stays dead indefinitely (no auto-deletion)
- **Recovery Period**: 24 hours (36 hours with Fading Hope curse)
- **Restrictions**: Dead characters cannot perform actions

---

**Last Updated**: Based on code analysis of RPG system implementation.
