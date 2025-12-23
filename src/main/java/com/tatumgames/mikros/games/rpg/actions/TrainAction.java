package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;

import java.util.Random;

/**
 * Train action - player trains to improve stats and gain XP.
 * Guarantees stat increase along with XP.
 */
public class TrainAction implements CharacterAction {
    private static final Random random = new Random();

    private static final String[] STAT_NAMES = {"STR", "AGI", "INT", "LUCK"};
    private static final String[] STAT_DISPLAY_NAMES = {"Strength", "Agility", "Intelligence", "Luck"};

    // Strength narratives - Fantasy-themed physical training and combat
    private static final String[] STRENGTH_NARRATIVES = {
            "You wrestle with a fierce orc warrior",
            "You train with a battle master, learning ancient combat techniques",
            "You push massive enchanted boulders up a frozen mountainside",
            "You engage in intense sparring with a troll",
            "You practice breaking through magical ice barriers with your bare hands",
            "You train your grip by crushing enchanted crystals",
            "You perform grueling exercises while carrying heavy magical artifacts",
            "You practice wielding a massive warhammer against training dummies",
            "You build strength by dragging a frozen dragon scale across the tundra",
            "You train by pushing against a powerful magical force field",
            "You test your might against a Frostbeast in the Ice Wastes",
            "You train with the Frostborne warriors of Frostgate, learning their ancient techniques",
            "You practice lifting fragments from the Shattering of the First Winter",
            "You build strength by climbing the jagged peaks of Starfall Ridge"
    };

    // Agility narratives - Fantasy-themed speed training, dodging, and acrobatics
    private static final String[] AGILITY_NARRATIVES = {
            "You practice dodging magical projectiles fired by a training golem",
            "You sprint through a maze of shifting ice platforms at breakneck speed",
            "You train your reflexes by evading a shadow assassin's strikes",
            "You leap across floating ice chunks with the grace of a snow leopard",
            "You practice acrobatic maneuvers while dodging dragon fire",
            "You train your speed by racing against a wind spirit",
            "You practice evasive techniques in a hall of magical traps",
            "You refine your footwork through an intricate dance with a phantom",
            "You train your balance by walking across a narrow bridge over a chasm",
            "You practice quick-draw techniques against a master thief",
            "You train your agility by evading Frostwraiths in the Spirit Veil",
            "You practice swift movements through the shifting paths of Starfall Ridge",
            "You learn to move between the Mortal and Arcane Veils with grace",
            "You train with the Stormwardens, mastering the Gale element's speed"
    };

    // Intelligence narratives - Fantasy-themed study, research, and magical learning
    private static final String[] INTELLIGENCE_NARRATIVES = {
            "You study ancient tomes filled with arcane knowledge in a frozen library",
            "You research magical relics discovered in the depths of Nilfheim",
            "You learn new spells through careful study with a master wizard",
            "You analyze runic patterns carved into ancient ice walls",
            "You decipher ancient scrolls containing lost battle strategies",
            "You study the works of legendary scholars and archmages",
            "You practice spell shaping by carving intricate patterns in the frost",
            "You solve complex magical puzzles left by ancient enchanters",
            "You meditate on philosophical texts while communing with the spirits",
            "You experiment with magical formulas in a hidden alchemy laboratory",
            "You study the Eight Elements at the Grand Library of Nil City",
            "You research the ancient Frostborne civilization's runic magic at the Moonspire Obelisk",
            "You learn to channel the Astral element, glimpsing fragments of possible futures",
            "You decipher the arcane inscriptions left by the first civilizations after the Shattering"
    };

    // Luck narratives - Fantasy-themed fortune training and fate manipulation
    private static final String[] LUCK_NARRATIVES = {
            "You practice with enchanted lucky charms and mystical talismans",
            "You study fortune-telling methods with a wise oracle",
            "You train your intuition through meditation under the northern lights",
            "You practice reading omens in the stars and ancient runes",
            "You learn to recognize patterns in chance events by playing dice with a trickster god",
            "You train by playing games of skill and luck with a fortune teller",
            "You study the art of being in the right place at the right time with a seer",
            "You practice trusting your instincts while navigating a maze of illusions",
            "You learn to sense opportunities before they appear by consulting an oracle",
            "You train your luck by taking calculated risks in a game of chance with spirits",
            "You study the Astral element's connection to fate at Starfall Ridge",
            "You learn to read the echoes of the Shattering, sensing patterns in chaos",
            "You train with the Selenites, learning to interpret the moon's omens",
            "You practice navigating the Spirit Veil, where chance and memory intertwine"
    };

    @Override
    public String getActionName() {
        return "train";
    }

    @Override
    public String getActionEmoji() {
        return "💪";
    }

    @Override
    public String getDescription() {
        return "Train to improve your stats and gain experience";
    }

    @Override
    public RPGActionOutcome execute(RPGCharacter character, RPGConfig config) {
        // Select random stat to increase
        int statIndex = random.nextInt(STAT_NAMES.length);
        String statName = STAT_NAMES[statIndex];
        String statDisplayName = STAT_DISPLAY_NAMES[statIndex];

        // Calculate stat increase (1-3 points)
        int statIncrease = 1 + random.nextInt(3);

        // Apply stat increase
        character.getStats().increaseStat(statName, statIncrease);

        // Calculate XP gain (slightly less than explore)
        int baseXp = 25 + (character.getLevel() * 4);
        int variance = random.nextInt(15) - 7;
        int xpGained = (int) ((baseXp + variance) * config.getXpMultiplier());

        // Add XP and check for level up
        boolean leveledUp = character.addXp(xpGained);

        // Build narrative based on stat trained
        String narrativePrefix;
        switch (statIndex) {
            case 0 -> // STR
                    narrativePrefix = STRENGTH_NARRATIVES[random.nextInt(STRENGTH_NARRATIVES.length)];
            case 1 -> // AGI
                    narrativePrefix = AGILITY_NARRATIVES[random.nextInt(AGILITY_NARRATIVES.length)];
            case 2 -> // INT
                    narrativePrefix = INTELLIGENCE_NARRATIVES[random.nextInt(INTELLIGENCE_NARRATIVES.length)];
            case 3 -> // LUCK
                    narrativePrefix = LUCK_NARRATIVES[random.nextInt(LUCK_NARRATIVES.length)];
            default ->
                    narrativePrefix = "You train diligently";
        }
        String narrative = String.format("%s, you improved your %s by %d point%s!",
                narrativePrefix,
                statDisplayName,
                statIncrease,
                statIncrease > 1 ? "s" : "");

        // Record the action
        character.recordAction();

        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .statIncreased(statDisplayName, statIncrease)
                .hpRestored(0)
                .success(true)
                .build();
    }
}

