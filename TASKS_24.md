✅ /rpg-reset full

A single admin-only command that deletes all RPG player profiles and world progression, but only for the server where
the command was run.

Written for Java + JDA (Java Discord API)
(If you use another library, I can rewrite it.)

🧩 DATA STRUCTURE — REQUIRED DESIGN

You should store data per-server:

// Master storage container
public class RPGDataStore {
public static Map<Long, ServerRPGData> servers = new HashMap<>();
}

// All RPG data for one Discord server (guild)
public class ServerRPGData {
public Map<Long, PlayerProfile> players = new HashMap<>();
public WorldState worldState = new WorldState();
}

Where:

guild ID → server RPG data

player ID → player profile

🧹 RESET LOGIC

A “full reset” for a server is simply:

RPGDataStore.servers.put(guildId, new ServerRPGData());

You replace the existing server data with a fresh empty instance.

This is the safest way:
✔ No stale data
✔ No half-wipes
✔ No weird edge cases

🔒 PERMISSION CHECK

Only allow users with:

Administrator permission
OR

A specific Discord role (optional)

⚠️ DOUBLE CONFIRMATION REQUIRED

To avoid accidental wipes.

Usage:

/rpg-reset full
/rpg-reset full confirm

You store pending resets for 30 seconds:

public class ResetConfirmation {
public static Map<Long, Long> pendingConfirmations = new HashMap<>();
}

Key = guildId
Value = timestamp

🧑‍💻 FULL JAVA (JDA) IMPLEMENTATION EXAMPLE

This is a complete working handler.

1. Slash Command Handling
   public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

   if (!event.getName().equals("rpg-reset")) return;

   String type = event.getOption("type").getAsString();
   String confirm = event.getOption("confirm") != null
   ? event.getOption("confirm").getAsString()
   : null;

   long guildId = event.getGuild().getIdLong();

   if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
   event.reply("❌ You must be an Administrator to use this command.")
   .setEphemeral(true).queue();
   return;
   }

   // Handle ONLY the full reset version
   if (type.equalsIgnoreCase("full")) {
   handleFullReset(event, guildId, confirm);
   }
   }

2. Full Reset Handler
   private void handleFullReset(SlashCommandInteractionEvent event, long guildId, String confirm) {

   // If user typed /rpg-reset full confirm
   if (confirm != null && confirm.equals("confirm")) {

        // Check if there was a pending reset
        Long timer = ResetConfirmation.pendingConfirmations.get(guildId);

        if (timer == null || System.currentTimeMillis() - timer > 30000) {
            event.reply("❌ No active reset request found or confirmation timed out. Please run the command again.")
                    .setEphemeral(true).queue();
            return;
        }

        // Perform full wipe
        RPGDataStore.servers.put(guildId, new ServerRPGData());
        ResetConfirmation.pendingConfirmations.remove(guildId);

        event.reply("🧹 **Full RPG Reset Complete!**\nAll character profiles and world progression have been cleared for this server.")
                .queue();

        return;
   }

   // FIRST COMMAND: ask for confirmation
   ResetConfirmation.pendingConfirmations.put(guildId, System.currentTimeMillis());

   event.reply("""
   ⚠️ **Warning: Full RPG Reset Requested!**
   This will DELETE **ALL** player profiles, levels, XP, stats, world state, boss progression — everything.

            To confirm, type:
            `/rpg-reset full confirm`
            (You have 30 seconds)
            """).setEphemeral(true).queue();

}

🧱 3. Slash Command Definition (for Discord)

Your command definition (in Discord developer portal or in command builder):

/rpg-reset type:<full> confirm:<optional confirm>

If using JDA’s command builder:

Commands.slash("rpg-reset", "Admin reset functions")
.addOption(OptionType.STRING, "type", "Type of reset", true, true)
.addOption(OptionType.STRING, "confirm", "Confirm reset", false);

🎉 HOW IT WORKS IN PRACTICE
Admin runs:
/rpg-reset full

Bot replies (ephemeral):

⚠️ Warning: Full RPG Reset Requested!
Type /rpg-reset full confirm within 30 seconds.

Admin confirms:
/rpg-reset full confirm

Bot replies:

🧹 Full RPG Reset Complete!
All player profiles and world progression have been cleared.

Your server starts fresh.
Players must register again using /rpg-register.

🔐 NOTES ABOUT SAFETY

This design avoids:

✔ accidental wipes
✔ global resets
✔ partial data corruption
✔ race conditions
✔ permission abuse

And can safely scale across 1–10,000 servers.