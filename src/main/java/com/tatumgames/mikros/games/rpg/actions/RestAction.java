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
            "You fall asleep listening to the wind howl like distant wolves.",
            // New Nilfheim lore narratives (15 additional)
            "You rest at a Frostgate outpost, the Frostborne warriors sharing their warmth and stories.",
            "You find shelter in the Grand Library of Nil City, surrounded by ancient knowledge and peace.",
            "You rest beneath the Moonspire Obelisk, its runic energy soothing your weary body.",
            "You sleep in a hidden chamber at Starfall Ridge, star fragments providing gentle light.",
            "You rest at a Stormwarden's camp, the Gale element's energy invigorating your spirit.",
            "You find peace in the Spirit Veil's liminal space, where time moves differently.",
            "You rest near a portal to the Arcane Veil, its otherworldly energy healing your wounds.",
            "You sleep in the ruins of the first civilizations after the Shattering, feeling their history.",
            "You rest at a shrine to the Eight Elements, each one's energy restoring you in turn.",
            "You find comfort in a hidden alcove of the Grand Library, surrounded by forbidden tomes.",
            "You rest at Frostgate's training grounds, the Frostborne's discipline bringing you peace.",
            "You sleep beneath the twin moons, their alignment blessing you with restorative energy.",
            "You rest in a chamber filled with Astral element crystals, their power showing you peaceful futures.",
            "You find shelter in an ancient Frostborne fortress, its walls still strong after centuries.",
            "You rest at the edge of the Ice Wastes, watching the aurora lights dance across the sky."
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

        // Remove frostbite debuff (resting warms you up)
        boolean hadFrostbite = character.hasFrostbite();
        if (hadFrostbite) {
            character.setHasFrostbite(false);
            narrative += " The warmth of rest removes the lingering effects of frostbite.";
        }

        // Oathbreaker: Corruption decay on rest
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
            int corruptionBefore = character.getCorruption();
            if (corruptionBefore > 0) {
                character.removeCorruption(1);
                if (corruptionBefore >= 10) {
                    narrative += " The broken oath's weight lessens with rest. Some corruption fades away.";
                } else if (corruptionBefore >= 5) {
                    narrative += " Rest helps you resist the corruption's pull. You feel slightly more in control.";
                }
            }
        }

        // Fully restore HP
        character.getStats().fullHeal();

        int hpRestored = maxHp - hpBefore;

        // Record the action (uses a charge)
        character.recordAction();

        // Track action type and increment rest count for achievements
        character.recordActionType("rest");
        character.incrementRestCount();

        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(0) // Rest doesn't give XP
                .leveledUp(false)
                .hpRestored(hpRestored)
                .success(true)
                .build();
    }
}

