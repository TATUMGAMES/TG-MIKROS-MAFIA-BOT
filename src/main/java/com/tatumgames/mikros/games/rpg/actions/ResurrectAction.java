package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.curse.WorldCurse;
import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.WorldCurseService;

import java.util.List;
import java.util.Random;

/**
 * Resurrect action - Priest-only free action to revive dead players.
 * Does not consume action charges.
 */
public class ResurrectAction implements CharacterAction {
    private static final Random random = new Random();
    private final WorldCurseService worldCurseService;

    /**
     * Creates a new ResurrectAction.
     *
     * @param worldCurseService the world curse service for applying curse effects
     */
    public ResurrectAction(WorldCurseService worldCurseService) {
        this.worldCurseService = worldCurseService;
    }

    // Messages when target is ALIVE (blessing instead)
    private static final String[] BLESSING_MESSAGES = {
            "✨ {priest} meditates and divine energies swirl… but {target} is already alive. A soft blessing settles upon them.",
            "🌟 {priest} raises his hands. The spirits whisper: 'This soul still walks.' {target} is lightly blessed instead.",
            "🙏 {priest} calls to the heavens, but {target} breathes strongly. Warm light surrounds them.",
            "✨ {priest} closes his eyes. A halo forms… then fades. {target} stands untouched by death, and receives a gentle blessing.",
            "🌬 {priest} whispers under his breadth. A breeze of holy magic brushes past {target}, who is very much alive.",
            "💫 {priest} begins to tremble. The resurrection fails softly — {target} is already among the living. A blessing remains behind.",
            "🌸 {priest} invokes ancient rites, only to find {target}'s soul still tethered. They are blessed instead.",
            "⛅ {priest} takes a deep breadth. A sacred radiance descends, confirming {target}'s life. The light leaves them empowered.",
            "⭐ {priest} smiles. 'No fallen soul found,' the spirits sigh. Still, {target} is touched by holiness.",
            "🔮 {priest} cracks his knuckles and looks off to the distance. Light gathers… then dissipates harmlessly. {target} receives a calm, serene blessing."
    };

    // Messages when target is DEAD (true resurrection)
    private static final String[] RESURRECTION_MESSAGES = {
            "✨ {priest} calls forth ancient power — and {target} gasps back to life, restored at half strength.",
            "🌟 {priest} starts jumping around frantically! Then stops. A surge of holy brilliance erupts! {target} rises from death's grasp, weak but alive.",
            "🙏 {priest}'s eyes turn white. Did he go blind? 'Return,' whispers the spirit choir — {target} stirs, reborn but fragile.",
            "💫 {priest} picks up dirt from the ground and rubs it all over his face. He says it has begun. A sacred wind sweeps through the realm… {target}'s soul snaps back into their body!",
            "🔥 {priest} cries out in pain! Resurrection succeeds! {target} awakens, trembling, halfway between life and death.",
            "🌙 Death loosens its hold as {priest} intervenes. {target} returns to life, needing time to recover.",
            "⛅ {priest} pulls out an ancient tome. Divine warmth refills {target}'s chest. Their eyes open once more.",
            "🕊 {priest} stares at the lifeless body for a while. The veil parts — {target} returns from the beyond at 50% health.",
            "⭐ {priest} whispers to himself. The spirits relent. {target} rises, weakened but living again.",
            "🌈 {priest} looks to the skies. A beam of radiant light pierces the dark… {target} lives anew, though recovery awaits."
    };

    @Override
    public String getActionName() {
        return "resurrect";
    }

    @Override
    public String getActionEmoji() {
        return "✨";
    }

    @Override
    public String getDescription() {
        return "Resurrect a dead player (Priest-only, free action)";
    }

    @Override
    public RPGActionOutcome execute(RPGCharacter character, RPGConfig config) {
        // This action requires a target, so it should be handled differently
        // For now, return a generic outcome - the command handler will provide the target
        throw new UnsupportedOperationException("ResurrectAction requires a target character. Use ResurrectAction.executeWithTarget() instead.");
    }

    /**
     * Executes resurrection with a target character.
     *
     * @param priest the Priest performing the resurrection
     * @param target the target character to resurrect
     * @param config the guild's RPG configuration
     * @return the outcome of the resurrection
     */
    public RPGActionOutcome executeWithTarget(RPGCharacter priest, RPGCharacter target, RPGConfig config) {
        // Check if priest is actually a Priest
        if (priest.getCharacterClass() != CharacterClass.PRIEST) {
            throw new IllegalArgumentException("Only Priests can perform resurrection!");
        }

        // Get active curses for this guild
        String guildId = config.getGuildId();
        List<WorldCurse> activeCurses = worldCurseService.getActiveCurses(guildId);

        // Apply Fading Hope curse (24h → 36h recovery)
        int recoveryHours = 24;
        if (activeCurses.contains(WorldCurse.MAJOR_FADING_HOPE)) {
            recoveryHours = 36;
        }

        String narrative;
        int xpGained = 0;
        boolean success = false;

        if (target.isDead()) {
            // Target is dead - perform resurrection
            target.resurrect(recoveryHours);
            narrative = RESURRECTION_MESSAGES[random.nextInt(RESURRECTION_MESSAGES.length)]
                    .replace("{priest}", priest.getName())
                    .replace("{target}", target.getName());

            // Priest gets +5 XP for successful resurrection (doubled during Fading Hope)
            xpGained = 5;
            if (activeCurses.contains(WorldCurse.MAJOR_FADING_HOPE)) {
                xpGained = 10; // Double XP during curse
            }
            
            // Track cursed resurrection for Lightbearer title (Priest only)
            if (!activeCurses.isEmpty() && priest.getCharacterClass() == CharacterClass.PRIEST) {
                priest.incrementCursedResurrections();
            }
            
            success = true;
        } else {
            // Target is alive - give blessing instead
            narrative = BLESSING_MESSAGES[random.nextInt(BLESSING_MESSAGES.length)]
                    .replace("{priest}", priest.getName())
                    .replace("{target}", target.getName());

            // Small XP bonus for blessing (doubled during Fading Hope)
            xpGained = 2;
            if (activeCurses.contains(WorldCurse.MAJOR_FADING_HOPE)) {
                xpGained = 4; // Double XP during curse
            }
            success = true;
        }

        // Add XP to priest (doesn't level up from this small amount typically)
        boolean leveledUp = priest.addXp(xpGained);

        // Note: This action does NOT consume a charge (free action)
        // Do NOT call character.recordAction()

        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .hpRestored(0)
                .success(success)
                .build();
    }
}

