package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;

import java.util.Random;

/**
 * Rest action - player rests to fully restore HP.
 * Consumes 1 action charge.
 */
public class RestAction implements CharacterAction {
    private static final Random random = new Random();

    private static final String[] NARRATIVES = {
            "You rest beside a glowing icefire brazier, warmth filling you.",
            "You sleep beneath the twin moons of Nilfheim, dreaming of battle.",
            "You rest at a sacred spring that never freezes.",
            "You patch your wounds with herbal frost-salve.",
            "You meditate, feeling icy winds cleanse your spirit.",
            "You nap inside an abandoned hut — surprisingly cozy.",
            "You pray at a forgotten shrine, feeling renewed.",
            "You gather your strength near a crackling mana crystal.",
            "You curl up inside a warm sleeping roll beneath the stars.",
            "You relax by a frozen lake as mist forms calming patterns.",
            "You warm your hands at a crack in the earth where steam rises softly.",
            "You sleep atop a pile of pelts, comforted by their warmth.",
            "A soft snowfall lulls you into a peaceful slumber.",
            "You lean against a rune pillar that hums with soothing energy.",
            "You nap inside a hollow log insulated by frost moss.",
            "You share a quiet moment with your thoughts beside a calm ice pond.",
            "You build a small fire, watching sparks drift skyward.",
            "You stretch your limbs and breathe deeply, letting fatigue fade away.",
            "A passing traveler shares tea brewed from rare winter herbs.",
            "You rest in the shadow of a monolith said to repel nightmares.",
            "You wrap yourself tightly in furs and drift into a long, peaceful sleep.",
            "You sip a warm broth that restores your strength.",
            "You whisper a prayer to the ancient guardians before resting.",
            "You relax near glowing mushrooms that emit a comforting warmth.",
            "You fall asleep listening to the wind howl like distant wolves."
    };

    @Override
    public String getActionName() {
        return "rest";
    }

    @Override
    public String getActionEmoji() {
        return "💤";
    }

    @Override
    public String getDescription() {
        return "Rest to fully restore your HP";
    }

    @Override
    public RPGActionOutcome execute(RPGCharacter character, RPGConfig config) {
        // Select random narrative
        String narrative = NARRATIVES[random.nextInt(NARRATIVES.length)];

        // Get HP before rest
        int hpBefore = character.getStats().getCurrentHp();
        int maxHp = character.getStats().getMaxHp();

        // Fully restore HP
        character.getStats().fullHeal();

        int hpRestored = maxHp - hpBefore;

        // Record the action (uses a charge)
        character.recordAction();

        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(0) // Rest doesn't give XP
                .leveledUp(false)
                .hpRestored(hpRestored)
                .success(true)
                .build();
    }
}

