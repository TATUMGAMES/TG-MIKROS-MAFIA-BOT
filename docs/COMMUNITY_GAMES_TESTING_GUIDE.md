# Community Games Testing Guide

## 🎮 Overview

This guide will help you test the Community Games Engine implemented in TASKS_05.

---

## 🚀 Setup

### 1. Ensure Bot Has Proper Permissions
The bot needs these permissions in your test server:
- Send Messages
- Embed Links
- Use Slash Commands
- View Channels

### 2. Start the Bot
```bash
# Set your Discord bot token in .env file
DISCORD_BOT_TOKEN=your_token_here

# Run the bot
./gradlew run
```

### 3. Wait for Bot to Come Online
The bot will register all slash commands when it starts up.

---

## ✅ Testing Checklist

### Phase 1: Initial Setup (Admin)

1. **Test `/game-setup`**
   - Run: `/game-setup channel:#game-channel reset_hour:0`
   - ✅ Verify: Bot confirms setup with embed
   - ✅ Verify: First game is posted immediately in the channel
   - ✅ Verify: Game announcement includes instructions

2. **Test `/game-config view`**
   - Run: `/game-config view`
   - ✅ Verify: Shows channel, reset time, and enabled games
   - ✅ Verify: All three games are enabled by default

3. **Test `/game-config set-channel`**
   - Run: `/game-config set-channel channel:#different-channel`
   - ✅ Verify: Channel is updated
   - ✅ Verify: View shows new channel

4. **Test `/game-config set-reset-time`**
   - Run: `/game-config set-reset-time hour:12`
   - ✅ Verify: Reset time is updated to 12:00 UTC
   - ✅ Verify: View shows new time

5. **Test `/game-config disable-game`**
   - Run: `/game-config disable-game game:DICE_ROLL`
   - ✅ Verify: Game is disabled
   - ✅ Verify: View shows updated status
   - Try to disable all games: Should prevent disabling last game

6. **Test `/game-config enable-game`**
   - Run: `/game-config enable-game game:DICE_ROLL`
   - ✅ Verify: Game is re-enabled
   - ✅ Verify: View shows updated status

---

### Phase 2: Word Unscramble Game

**Setup:** Ensure Word Unscramble is the active game (or wait/reset for it)

1. **Test `/guess` with correct answer**
   - Look at the scrambled word
   - Figure out the correct word
   - Run: `/guess word:CORRECT_WORD`
   - ✅ Verify: Public announcement with emoji
   - ✅ Verify: Shows player mention, score, and time
   - ✅ Verify: Game is marked as won

2. **Test `/guess` with incorrect answer**
   - Run: `/guess word:WRONG`
   - ✅ Verify: Ephemeral error message (only you see it)
   - ✅ Verify: No public announcement
   - ✅ Verify: Encourages trying again

3. **Test `/guess` after game is won**
   - Run: `/guess word:anything`
   - ✅ Verify: Game ended message
   - ✅ Verify: Tells user to wait for reset

4. **Test `/guess` for wrong game type**
   - Start a different game type
   - Run: `/guess word:test`
   - ✅ Verify: Error message about no active word game

5. **Test `/game-stats` during word game**
   - Run: `/game-stats`
   - ✅ Verify: Shows Word Unscramble as active game
   - ✅ Verify: Shows time until reset
   - ✅ Verify: Shows winner if won, or attempt count if not

---

### Phase 3: Dice Battle Game

**Setup:** Ensure Dice Roll is the active game

1. **Test `/roll` first time**
   - Run: `/roll`
   - ✅ Verify: Public announcement of roll result
   - ✅ Verify: Shows die value (1-20)
   - ✅ Verify: Special message for natural 20
   - ✅ Verify: Shows if you're the new leader

2. **Test `/roll` second time (same user)**
   - Run: `/roll` again
   - ✅ Verify: Ephemeral error: already rolled today
   - ✅ Verify: No public announcement

3. **Test `/roll` with multiple users**
   - Have multiple users roll
   - ✅ Verify: Each gets their roll announced
   - ✅ Verify: Leader changes are announced
   - ✅ Verify: Higher rolls show leader indicator

4. **Test `/game-stats` during dice game**
   - Run: `/game-stats`
   - ✅ Verify: Shows Dice Battle as active game
   - ✅ Verify: Shows full leaderboard (top 10)
   - ✅ Verify: Sorted by highest roll
   - ✅ Verify: Shows medals (🥇🥈🥉) for top 3
   - ✅ Verify: Critical hits (20) have fire emoji

---

### Phase 4: Emoji Match Game

**Setup:** Ensure Emoji Match is the active game

1. **Test `/match` with correct pattern**
   - Look at the emoji pattern shown
   - Copy it exactly
   - Run: `/match emojis:🎮🎲🎯` (use actual pattern)
   - ✅ Verify: Public announcement of win
   - ✅ Verify: Shows player mention and score
   - ✅ Verify: Game is marked as won

2. **Test `/match` with incorrect pattern**
   - Run: `/match emojis:❌❌❌`
   - ✅ Verify: Ephemeral error message
   - ✅ Verify: Encourages trying again

3. **Test `/match` after game is won**
   - Run: `/match emojis:anything`
   - ✅ Verify: Game ended message

4. **Test `/game-stats` during emoji game**
   - Run: `/game-stats`
   - ✅ Verify: Shows Emoji Match as active game
   - ✅ Verify: Shows winner if won, or attempt count

---

### Phase 5: Daily Reset System

**Note:** This is difficult to test in real-time. Options:

#### Option A: Wait for scheduled reset
- Set reset time to soon (e.g., next hour)
- Wait for reset time
- ✅ Verify: Previous game winner is announced
- ✅ Verify: New game is posted
- ✅ Verify: Game type may be different

#### Option B: Modify reset time to be immediate
- Use `/game-config set-reset-time hour:CURRENT_HOUR`
- Wait up to an hour for reset check
- ✅ Verify: Reset occurs

#### Option C: Use `/force-promotion-check` as inspiration
- Could implement a `/force-game-reset` admin command for testing
- This is not part of TASKS_05, but useful for debugging

---

### Phase 6: Edge Cases

1. **Test commands without setup**
   - In a fresh server, try `/guess`, `/roll`, `/match`
   - ✅ Verify: Error message directing to `/game-setup`

2. **Test `/game-stats` with no active game**
   - After reset, before new game starts (if this state exists)
   - ✅ Verify: Appropriate error message

3. **Test admin commands as non-admin**
   - As regular user, try `/game-setup` or `/game-config`
   - ✅ Verify: Permission error

4. **Test with invalid bot permissions**
   - Remove bot's send message permission
   - Try `/game-setup`
   - ✅ Verify: Error about missing permissions

---

## 🐛 Common Issues & Solutions

### Issue: Commands not appearing
- **Solution:** Bot may still be registering commands. Wait 1-5 minutes.

### Issue: Bot doesn't respond
- **Solution:** Check bot has proper permissions and is online.

### Issue: Game doesn't reset
- **Solution:** Scheduler checks every hour on the hour. Reset must fall within that hour.

### Issue: Multiple games active
- **Solution:** Only one game should be active per guild. Check logs for errors.

---

## 📊 Expected Behavior Summary

### Word Unscramble
- **Win Condition:** First correct guess
- **Scoring:** Time-based (faster = higher score)
- **Attempts:** Unlimited until someone wins

### Dice Battle
- **Win Condition:** Highest roll at end of day
- **Scoring:** Dice value (1-20)
- **Attempts:** One per user per day

### Emoji Match
- **Win Condition:** First correct pattern match
- **Scoring:** Time-based (faster = higher score)
- **Attempts:** Unlimited until someone wins

---

## 🎯 Success Criteria

All tests pass when:
- ✅ All commands execute without errors
- ✅ Games function as designed
- ✅ Leaderboards display correctly
- ✅ Reset system works (if tested)
- ✅ Admin controls work properly
- ✅ Error messages are helpful
- ✅ Public vs ephemeral messages are correct
- ✅ Multi-user scenarios work
- ✅ Edge cases are handled gracefully

---

## 📝 Notes

- Each guild has independent game state
- Games are stored in memory (will reset on bot restart)
- Reset scheduler runs continuously after bot starts
- All times are in UTC

---

**Happy Testing! 🎮**

