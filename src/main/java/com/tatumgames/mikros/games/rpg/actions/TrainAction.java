package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.curse.WorldCurse;
import com.tatumgames.mikros.games.rpg.events.NilfheimEventType;
import com.tatumgames.mikros.games.rpg.model.InfusionType;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.LoreRecognitionService;
import com.tatumgames.mikros.games.rpg.service.NilfheimEventService;
import com.tatumgames.mikros.games.rpg.service.WorldCurseService;

import java.util.List;
import java.util.Random;

/**
 * Train action - player trains to improve stats and gain XP.
 * Guarantees stat increase along with XP.
 */
public class TrainAction implements CharacterAction {
    private static final Random random = new Random();
    private final NilfheimEventService nilfheimEventService;
    private final LoreRecognitionService loreRecognitionService;
    private final WorldCurseService worldCurseService;

    /**
     * Creates a new TrainAction.
     *
     * @param nilfheimEventService the Nilfheim event service for server-wide events
     * @param loreRecognitionService the lore recognition service for milestone checks
     * @param worldCurseService the world curse service for checking active curses
     */
    public TrainAction(NilfheimEventService nilfheimEventService, LoreRecognitionService loreRecognitionService, WorldCurseService worldCurseService) {
        this.nilfheimEventService = nilfheimEventService;
        this.loreRecognitionService = loreRecognitionService;
        this.worldCurseService = worldCurseService;
    }

    /**
     * Creates a new TrainAction without WorldCurseService (backward compatibility).
     */
    public TrainAction(NilfheimEventService nilfheimEventService, LoreRecognitionService loreRecognitionService) {
        this(nilfheimEventService, loreRecognitionService, null);
    }

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
            "You build strength by climbing the jagged peaks of Starfall Ridge",
            "You train by breaking through the ice barriers at Frostgate's training grounds",
            "You practice the Frostborne's signature war cry, channeling the Frost element's power",
            "You engage in a trial of strength with a Frost Titan, learning from its raw power",
            "You train with weapons forged in the fires of the Shattering, each swing building your might",
            "You practice the ancient Frostborne technique of shattering ice with pure force",
            "You build strength by carrying star fragments from Starfall Ridge to the Grand Library"
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
            "You train with the Stormwardens, mastering the Gale element's speed",
            "You practice the Stormwarden's wind-walking technique, moving with the Gale element",
            "You train by dodging lightning strikes during a Stormwarden's training session",
            "You learn to phase through the Spirit Veil, becoming temporarily intangible",
            "You practice the art of silent movement through Nil City's shadowy alleys",
            "You train with a master assassin from Frostgate, learning their evasive techniques",
            "You master the Gale element's speed, moving faster than the eye can follow"
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
            "You decipher the arcane inscriptions left by the first civilizations after the Shattering",
            "You master the Void element's secrets, learning to manipulate nothingness itself",
            "You study the Ember element's fire magic, understanding how it interacts with Frost",
            "You research the Astral element's connection to fate at the Grand Library's forbidden section",
            "You learn to weave multiple Elements together, creating powerful combined spells",
            "You decipher the Moonspire Obelisk's runes, unlocking knowledge of the first civilizations",
            "You study the Grand Library's archives on the Shattering, understanding the cataclysm's nature"
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
            "You practice navigating the Spirit Veil, where chance and memory intertwine",
            "You learn to read the Astral element's glimpses of possible futures at Starfall Ridge",
            "You practice interpreting the twin moons' alignment, reading omens in their light",
            "You train with an oracle from Nil City, learning to sense opportunities before they appear",
            "You study the patterns of the Shattering, learning to find luck in chaos",
            "You practice the art of being in the right place at the right time using Astral visions",
            "You learn to manipulate fate itself by channeling the Astral element's power"
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

        // Apply Nilfheim event effects
        String guildId = config.getGuildId();
        NilfheimEventType activeEvent = nilfheimEventService.getActiveEvent(guildId);
        if (activeEvent != null) {
            if (activeEvent.getEffectType() == NilfheimEventType.EventEffectType.TRAIN_STAT_BOOST) {
                // Grand Library Opens: +1 guaranteed stat point
                statIncrease += (int) activeEvent.getEffectValue();
            }
            if (activeEvent.getEffectType() == NilfheimEventType.EventEffectType.ALL_XP_BOOST) {
                // Starfall Ridge's Light: +15% XP on all actions (applied below)
            }
        }

        // Apply stat increase
        character.getStats().increaseStat(statName, statIncrease);

        // Calculate XP gain (slightly less than explore)
        int baseXp = 25 + (character.getLevel() * 4);
        int variance = random.nextInt(15) - 7;
        int xpGained = (int) ((baseXp + variance) * config.getXpMultiplier());

        // Apply Nilfheim event effects for XP
        if (activeEvent != null && activeEvent.getEffectType() == NilfheimEventType.EventEffectType.ALL_XP_BOOST) {
            // Starfall Ridge's Light: +15% XP on all actions
            xpGained = (int) (xpGained * (1.0 + activeEvent.getEffectValue()));
        }

        // Apply infusion effects
        InfusionType activeInfusion = character.getInventory().getActiveInfusion();
        boolean infusionConsumed = false;
        if (activeInfusion != null) {
            infusionConsumed = true;
            if (activeInfusion == InfusionType.FROST_CLARITY) {
                // Frost Clarity: +10% XP on next action
                xpGained = (int) (xpGained * 1.10);
            } else if (activeInfusion == InfusionType.ELEMENTAL_CONVERGENCE) {
                // Elemental Convergence: +15% XP on next action
                xpGained = (int) (xpGained * 1.15);
            }
        }

        // Add XP and check for level up
        boolean leveledUp = character.addXp(xpGained, loreRecognitionService);

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

        // Oathbreaker: Gain corruption from acting during world curses
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER && worldCurseService != null) {
            String guildId = config.getGuildId();
            List<WorldCurse> activeCurses = worldCurseService.getActiveCurses(guildId);
            if (!activeCurses.isEmpty()) {
                character.addCorruption(1);
                narrative += "\n\n⚔️💀 **Corruption:** The world's curses resonate with your broken oath, increasing your corruption.";
            }
        }

        // Consume active infusion if used
        if (infusionConsumed) {
            character.getInventory().consumeActiveInfusion();
        }

        // Record the action
        character.recordAction();
        
        // Track action type for achievements
        character.recordActionType("train");

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

