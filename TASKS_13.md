STANDARDIZE COMMAND PREFIXING (NOW USING DASHES)
🎯 Goal

Adopt a consistent, global command naming scheme using dashes instead of underscores.

1.1 — Apply These Prefix Rules
Admin Commands → admin-<command>

Examples:

/warn → /admin-warn

/kick → /admin-kick

/ban → /admin-ban

/history → /admin-history

/setup-promotion-channel → /admin-setup-promotion-channel

/rpg-config → /admin-rpg-config

RPG Commands → rpg-<command>

Examples:

/rpg-register → /rpg-register (already good)

/rpg-profile → /rpg-profile

/rpg-action → /rpg-action

/rpg-leaderboard → /rpg-leaderboard

No conversion needed — RPG already uses dashes properly.

Community Games → Each game gets its own namespace using game names

Cursor invents and uses these simple, memorable names:

Word Unscramble → Scramble

Dice Battle → DiceFury

Emoji Match → EmojiHunt

Commands must use dashed prefixes:

Scramble

/scramble-guess

/scramble-stats

/scramble-info (Cursor can introduce this as needed)

DiceFury

/dicefury-roll

/dicefury-stats

/dicefury-info

EmojiHunt

/emojihunt-match

/emojihunt-stats

/emojihunt-info

Routing rules:

The original /guess must be replaced internally with /scramble-guess and /spell-guess.

The shared router must still route appropriately.

Spelling Challenge → spell-<command>

Examples:

/spelling-challenge → /spell-challenge

/spelling-leaderboard → /spell-leaderboard

/guess (spelling) → /spell-guess

Promotion System → promo-<command>

Examples:

/promo-help → /promo-help

/setup-promotions → /admin-setup-promotions

/set-promo-frequency → /admin-set-promo-frequency

Game Analytics → stats-<subcommand>

Examples:

/gamestats trending-game-genres → /stats-trending-game-genres

/gamestats trending-content → /stats-trending-content

------------------------------------
APPLY COMMAND PREFIX RENAMING IN CODEBASE

Cursor must:

1. Update every single command definition

2. Update all services referencing old names

3. Update slash command registration logic

4. Update help menus

5. Update routing behavior

6. Ensure Discord slash commands regenerate cleanly

7. Ensure no duplicates or collisions exist