package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.curse.WorldCurse;
import com.tatumgames.mikros.games.rpg.events.NilfheimEventType;
import com.tatumgames.mikros.games.rpg.model.*;
import com.tatumgames.mikros.games.rpg.service.*;

import java.time.Instant;
import java.util.*;

/**
 * Battle action - player fights an AI enemy.
 * Can result in victory (high XP) or defeat (damage taken, some XP).
 * <p>
 * TODO: Future Features
 * - Enemy variety with different stats and abilities
 * - Boss battles with special rewards
 * - PvP battles between players
 * - Battle items and consumables
 */
public class BattleAction implements CharacterAction {
    private static final Random random = new Random();
    
    // Narrative format strings
    private static final String VICTORY_NARRATIVE_FORMAT = "You encountered %s (Level %d) and emerged victorious!%s%s%s " +
            "Your combat prowess proved superior, though you sustained minor wounds.%s%s%s";
    private static final String DEFEAT_NARRATIVE_FORMAT = "You encountered %s (Level %d) but were defeated.%s%s " +
            "Learn from this experience!";
    
    private static final String[] ENEMY_NAMES = {
            // Original 16
            "Goblin Scout", "Wild Wolf", "Bandit Thief", "Slime Monster",
            "Skeleton Warrior", "Dark Mage", "Forest Troll", "Shadow Assassin",
            "Fire Elemental", "Ice Golem", "Corrupted Knight", "Venomous Spider",
            "Orc Berserker", "Necromancer", "Dragon Whelp", "Demon Imp",
            // Additional 20 (Nilfheim-specific)
            "Frost Goblin", "Ice Stalker", "Wailing Wisp", "Wandering Revenant",
            "Dire Bat", "Frost-Bitten Bear", "Marauder", "Crystal Spider",
            "Corrupted Elk", "Shade Assassin", "Blighted Serpent", "Spirit Knight",
            "Snow Golem", "Frost Wisp", "Enraged Wendigo", "Frostfang Lynx",
            "Glacial Slime", "Death-Rattle Skeleton", "Storm Raven", "Shrieking Banshee",
            "Frost Troll", "Rime Drifter", "Blight Raven", "Possessed Armor",
            "Coldshade Phantom", "Bone Warg", "Frostbound Sorcerer", "Mutated Frost Boar",
            "Wraithling", "Corrupted Dryad", "Hollow Knight", "Frozen Ghoul",
            "Spirit Snake", "Skeletal Horse", "Frost Sprite Cluster",
            // New 30 enemies (Content Expansion)
            // PHYSICAL (5)
            "Frostbound Berserker", "Ironclad Marauder", "Glacial Brute", "Stonefist Warrior", "Frozen Knight",
            // MAGICAL (5)
            "Void Whisperer", "Arcane Wraith", "Frost Sorcerer", "Shadow Mage", "Crystal Enchanter",
            // AGILE (5)
            "Shadow Stalker", "Wind Dancer", "Frost Sprite", "Swift Reaper", "Blade Phantom",
            // UNDEAD (5)
            "Bone Reaver", "Soul Eater", "Grave Wight", "Necrotic Horror", "Frozen Lich",
            // BEAST (5)
            "Ice Wolf Pack", "Frost Bear", "Dire Frost Wolf", "Glacial Predator", "Tundra Beast",
            // CONSTRUCT (5)
            "Runic Golem", "Ice Sentinel", "Crystal Guardian", "Frost Automaton", "Stone Guardian"
    };
    // Enemy type mappings
    private static final Map<String, EnemyType> ENEMY_TYPE_MAP = new HashMap<>();
    private static final String[] ELITE_DETECTION_NARRATIVES = {
            "You sense this foe is stronger than usual... something is wrong.",
            "The air freezes with each breath it takes. This creature has survived many hunters.",
            "Its movements are too precise... too deliberate. This is no ordinary enemy.",
            "Ancient runes shimmer faintly across the enemy's form. Power radiates from it.",
            "The enemy's eyes burn with an otherworldly light. You feel a chill down your spine.",
            "Dark energy crackles around the creature. This battle will be different.",
            "The ground frosts over as it approaches. Nilfheim itself seems to favor this foe.",
            "You recognize the signs - this enemy has been touched by something greater.",
            "The creature's very presence distorts the air. This is an elite of Nilfheim.",
            "Something ancient and powerful has marked this enemy. You prepare for a true test."
    };
    private static final String[] ELITE_VICTORY_NARRATIVES = {
            "You barely survived the elite's onslaught, but emerge victorious! The creature's power was overwhelming, yet your resolve proved stronger.",
            "Against all odds, you defeated the elite enemy. Its enhanced strength made every moment a struggle, but you prevailed.",
            "The elite falls, its enhanced form finally broken. You stand victorious, though the battle left you shaken.",
            "Through skill and determination, you overcame the elite's superior power. This victory will be remembered.",
            "The elite's enhanced abilities pushed you to your limits, but you emerged triumphant. Your legend grows.",
            "Against the elite's overwhelming strength, you fought with everything you had. Victory is yours, hard-won and well-deserved.",
            "The elite's power was immense, but your combat prowess proved superior. You stand victorious over a truly dangerous foe.",
            "You defeated the elite through sheer will and skill. The creature's enhanced abilities made this a battle for the ages.",
            "The elite fought with unnatural strength, but you matched it blow for blow. Victory tastes all the sweeter for the challenge.",
            "Against the elite's superior power, you found openings and exploited them. The creature falls, and you remain standing.",
            "The elite's enhanced form made every strike count, but you fought smarter, not harder. Victory is yours.",
            "You overcame the elite's overwhelming power through determination and skill. This was a true test of your abilities.",
            "The elite's superior strength made this a desperate battle, but you emerged victorious. Your resolve never wavered.",
            "Against the elite's enhanced abilities, you fought with everything you had. The creature falls, and you stand triumphant.",
            "The elite's power was immense, but you matched it with skill and determination. Victory belongs to the prepared."
    };
    private static final String[] ELITE_DEFEAT_NARRATIVES = {
            "The elite's strength was too much. Its enhanced power overwhelmed you, and you were forced to retreat.",
            "You underestimated this foe's power. The elite's superior abilities proved too much to handle.",
            "The elite's enhanced form made every attack devastating. You were no match for its overwhelming strength.",
            "Against the elite's superior power, you fought valiantly but were ultimately defeated. The creature's enhanced abilities were too great.",
            "The elite's overwhelming strength broke through your defenses. You fell, defeated by a truly powerful enemy.",
            "You faced the elite with courage, but its enhanced abilities proved insurmountable. Defeat came swiftly and painfully.",
            "The elite's superior power made this battle one-sided. You were defeated, but you learned from the experience.",
            "Against the elite's enhanced form, you struggled valiantly but were ultimately overcome. The creature's power was too great.",
            "The elite's overwhelming strength shattered your defenses. You fell, defeated by a truly dangerous foe.",
            "You fought the elite with everything you had, but its superior abilities proved too much. Defeat was inevitable.",
            "The elite's enhanced power made every moment a struggle. In the end, you were defeated, but you survived to fight another day.",
            "Against the elite's superior strength, you fought bravely but were ultimately overwhelmed. The creature's power was immense.",
            "The elite's enhanced abilities made this a one-sided battle. You were defeated, but you gained valuable experience.",
            "You faced the elite with determination, but its overwhelming power proved too much. Defeat came, but you learned from it.",
            "The elite's superior strength broke your defenses. You fell, defeated by a truly powerful enemy of Nilfheim."
    };
    private static final String[] WITHDRAWAL_SUCCESS_NARRATIVES = {
            "You quickly retreat, leaving the elite behind. Your agility allowed you to escape before the creature could strike.",
            "Recognizing the danger, you withdraw with practiced speed. The elite watches you go, unable to catch your swift retreat.",
            "Your instincts scream danger, and you listen. You escape the elite's presence, leaving it behind in the frozen wastes.",
            "With a burst of speed, you retreat from the elite. Your quick thinking saved you from a potentially fatal encounter.",
            "You recognize the elite's power and choose discretion over valor. Your swift retreat leaves the creature behind.",
            "Your agility allows you to escape the elite's reach. You withdraw, knowing that some battles are better avoided.",
            "You quickly assess the situation and retreat. The elite's enhanced power makes this a battle you're not ready for.",
            "With practiced movements, you withdraw from the elite. Your speed and luck combine to ensure a clean escape.",
            "You recognize the elite's superior strength and choose to retreat. Your quick thinking prevents a dangerous encounter.",
            "The elite's power is too great, and you know it. You withdraw swiftly, leaving the creature to hunt other prey."
    };
    private static final String[] WITHDRAWAL_FAILURE_NARRATIVES = {
            "You attempt to flee, but the elite anticipates your retreat. It strikes first, catching you off guard!",
            "Your attempt to withdraw fails as the elite moves with unnatural speed. It gets the first strike!",
            "You try to retreat, but the elite is faster. It strikes before you can escape, catching you unprepared.",
            "The elite sees your intent to flee and strikes first. Your withdrawal attempt fails, and the battle begins with you at a disadvantage.",
            "You attempt to escape, but the elite's enhanced speed allows it to strike first. The battle begins with you on the defensive.",
            "Your retreat is anticipated by the elite. It strikes before you can escape, gaining the advantage.",
            "You try to withdraw, but the elite moves too quickly. It gets the first strike, and the battle begins with you at a disadvantage.",
            "The elite recognizes your intent to flee and strikes first. Your withdrawal attempt fails, and you're caught off guard.",
            "You attempt to escape, but the elite's superior speed allows it to strike before you can retreat. The battle begins poorly.",
            "Your withdrawal attempt is anticipated. The elite strikes first, catching you unprepared and gaining the advantage."
    };
    private static final String[] GOD_TOUCHED_SAME_DEITY_NARRATIVES = {
            "The elite bears the mark of your patron deity. There's a strange recognition between you - this enemy has been blessed by the same power you serve.",
            "You sense your deity's presence on this elite. The creature has been touched by your patron, creating an eerie connection.",
            "The elite carries the blessing of your deity. There's a moment of recognition - this enemy shares your divine connection.",
            "Your patron's mark is visible on the elite. The creature has been blessed by the same power that guides you.",
            "The elite bears your deity's blessing. There's a strange familiarity - this enemy has been touched by your patron.",
            "You recognize your deity's influence on the elite. The creature has been blessed by the same power you serve.",
            "The elite carries the mark of your patron. There's an unsettling connection - this enemy shares your divine blessing.",
            "Your deity's presence is strong on this elite. The creature has been touched by your patron, creating a moment of recognition.",
            "The elite bears the blessing of your deity. There's a strange familiarity as you recognize your patron's mark.",
            "You sense your patron's influence on the elite. The creature has been blessed by the same power that guides you."
    };
    private static final String[] GOD_TOUCHED_DIFFERENT_DEITY_NARRATIVES = {
            "The elite bears the mark of a rival deity. Dark energy crackles - this enemy has been blessed by a power opposed to yours.",
            "You sense a rival deity's presence on this elite. The creature has been touched by a power that opposes your patron.",
            "The elite carries the blessing of an enemy god. There's hostility in the air - this enemy serves a rival power.",
            "A rival deity's mark is visible on the elite. The creature has been blessed by a power that stands against yours.",
            "The elite bears a rival god's blessing. Dark energy radiates from it - this enemy has been touched by an opposing force.",
            "You recognize an enemy deity's influence on the elite. The creature has been blessed by a power that opposes your patron.",
            "The elite carries the mark of a rival power. There's animosity in the air - this enemy serves a deity opposed to yours.",
            "A rival deity's presence is strong on this elite. The creature has been touched by a power that stands against your patron.",
            "The elite bears the blessing of an enemy god. Dark energy crackles as you recognize the mark of a rival power.",
            "You sense a rival deity's influence on the elite. The creature has been blessed by a power that opposes yours."
    };
    private static final String[] CURSED_ELITE_DETECTION_NARRATIVES = {
            "The elite bears the mark of the world's curses. Dark energy flows through it, making it more dangerous than normal.",
            "You sense the world's curses have touched this elite. The creature has been corrupted by Nilfheim's dark magic.",
            "The elite carries the taint of active curses. Dark power radiates from it, making this encounter especially perilous.",
            "Cursed energy crackles around the elite. The world's curses have enhanced this creature, making it far more dangerous.",
            "The elite has been marked by the world's curses. Dark magic flows through it, amplifying its already considerable power.",
            "You recognize the signs - this elite has been touched by active world curses. The creature's power is amplified by dark magic.",
            "The elite bears the taint of Nilfheim's curses. Cursed energy radiates from it, making this a truly dangerous encounter.",
            "Dark energy flows through the elite, a sign of the world's active curses. This creature has been enhanced by dark magic.",
            "The elite carries the mark of the world's curses. Cursed power crackles around it, making it more dangerous than usual.",
            "You sense the world's curses have corrupted this elite. Dark energy flows through it, amplifying its power.",
            "The elite has been touched by active world curses. Dark magic radiates from it, making this encounter especially perilous.",
            "Cursed energy crackles around the elite. The world's curses have enhanced this creature, making it far more dangerous than normal.",
            "The elite bears the taint of Nilfheim's active curses. Dark power flows through it, amplifying its already considerable strength.",
            "You recognize the signs - this elite has been marked by the world's curses. The creature's power is enhanced by dark magic.",
            "The elite carries the mark of active world curses. Cursed energy radiates from it, making this a truly dangerous foe."
    };

    static {
        // PHYSICAL enemies (armored, brute force)
        ENEMY_TYPE_MAP.put("Goblin Scout", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Orc Berserker", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Corrupted Knight", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Spirit Knight", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Hollow Knight", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Possessed Armor", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Forest Troll", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Frost Troll", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Marauder", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Bandit Thief", EnemyType.PHYSICAL);

        // MAGICAL enemies (spellcasters, magical creatures)
        ENEMY_TYPE_MAP.put("Dark Mage", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Necromancer", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Frostbound Sorcerer", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Ice Golem", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Frost Wisp", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Wailing Wisp", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Fire Elemental", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Wraithling", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Shrieking Banshee", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Frost Sprite Cluster", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Corrupted Dryad", EnemyType.MAGICAL);

        // AGILE enemies (fast, evasive)
        ENEMY_TYPE_MAP.put("Shadow Assassin", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Shade Assassin", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Frostfang Lynx", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Dire Bat", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Venomous Spider", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Storm Raven", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Blight Raven", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Rime Drifter", EnemyType.AGILE);

        // UNDEAD enemies
        ENEMY_TYPE_MAP.put("Skeleton Warrior", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Wandering Revenant", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Death-Rattle Skeleton", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Frozen Ghoul", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Coldshade Phantom", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Skeletal Horse", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Spirit Snake", EnemyType.UNDEAD);

        // BEAST enemies (animalistic)
        ENEMY_TYPE_MAP.put("Wild Wolf", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Frost-Bitten Bear", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Frost Goblin", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Ice Stalker", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Enraged Wendigo", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Bone Warg", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Mutated Frost Boar", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Blighted Serpent", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Corrupted Elk", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Dragon Whelp", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Demon Imp", EnemyType.BEAST);

        // CONSTRUCT enemies (mechanical/stone)
        ENEMY_TYPE_MAP.put("Snow Golem", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Crystal Spider", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Slime Monster", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Glacial Slime", EnemyType.CONSTRUCT);

        // New PHYSICAL enemies
        ENEMY_TYPE_MAP.put("Frostbound Berserker", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Ironclad Marauder", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Glacial Brute", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Stonefist Warrior", EnemyType.PHYSICAL);
        ENEMY_TYPE_MAP.put("Frozen Knight", EnemyType.PHYSICAL);

        // New MAGICAL enemies
        ENEMY_TYPE_MAP.put("Void Whisperer", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Arcane Wraith", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Frost Sorcerer", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Shadow Mage", EnemyType.MAGICAL);
        ENEMY_TYPE_MAP.put("Crystal Enchanter", EnemyType.MAGICAL);

        // New AGILE enemies
        ENEMY_TYPE_MAP.put("Shadow Stalker", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Wind Dancer", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Frost Sprite", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Swift Reaper", EnemyType.AGILE);
        ENEMY_TYPE_MAP.put("Blade Phantom", EnemyType.AGILE);

        // New UNDEAD enemies
        ENEMY_TYPE_MAP.put("Bone Reaver", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Soul Eater", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Grave Wight", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Necrotic Horror", EnemyType.UNDEAD);
        ENEMY_TYPE_MAP.put("Frozen Lich", EnemyType.UNDEAD);

        // New BEAST enemies
        ENEMY_TYPE_MAP.put("Ice Wolf Pack", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Frost Bear", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Dire Frost Wolf", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Glacial Predator", EnemyType.BEAST);
        ENEMY_TYPE_MAP.put("Tundra Beast", EnemyType.BEAST);

        // New CONSTRUCT enemies
        ENEMY_TYPE_MAP.put("Runic Golem", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Ice Sentinel", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Crystal Guardian", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Frost Automaton", EnemyType.CONSTRUCT);
        ENEMY_TYPE_MAP.put("Stone Guardian", EnemyType.CONSTRUCT);

        // Default fallback for any unmapped enemies
    }

    private final WorldCurseService worldCurseService;
    private final AuraService auraService;

    // Elite Enemy System - Narrative Arrays
    private final NilfheimEventService nilfheimEventService;
    private final LoreRecognitionService loreRecognitionService;
    private final BossService bossService; // Optional, can be null

    /**
     * Creates a new BattleAction.
     *
     * @param worldCurseService      the world curse service for applying curse effects
     * @param auraService            the aura service for Song of Nilfheim curse reduction
     * @param nilfheimEventService   the Nilfheim event service for server-wide events
     * @param loreRecognitionService the lore recognition service for milestone checks
     * @param bossService            the boss service for checking active bosses (can be null)
     */
    public BattleAction(WorldCurseService worldCurseService, AuraService auraService, NilfheimEventService nilfheimEventService, LoreRecognitionService loreRecognitionService, BossService bossService) {
        this.worldCurseService = worldCurseService;
        this.auraService = auraService;
        this.nilfheimEventService = nilfheimEventService;
        this.loreRecognitionService = loreRecognitionService;
        this.bossService = bossService;
    }

    /**
     * Creates a new BattleAction without BossService (backward compatibility).
     */
    public BattleAction(WorldCurseService worldCurseService, AuraService auraService, NilfheimEventService nilfheimEventService, LoreRecognitionService loreRecognitionService) {
        this(worldCurseService, auraService, nilfheimEventService, loreRecognitionService, null);
    }

    @Override
    public String getActionName() {
        return "battle";
    }

    @Override
    public String getActionEmoji() {
        return "⚔️";
    }

    @Override
    public String getDescription() {
        return "Battle an enemy for high XP rewards";
    }

    @Override
    public RPGActionOutcome execute(RPGCharacter character, RPGConfig config) {
        // Get active curses for this guild
        String guildId = config.getGuildId();
        List<WorldCurse> activeCurses = worldCurseService.getActiveCurses(guildId);

        // Check for Song of Nilfheim aura (reduces curse penalties by 1-2%)
        double songReduction = auraService.getSongOfNilfheimCurseReduction(guildId);

        // Select random enemy (March of the Dead increases undead chance)
        String enemyName;
        if (activeCurses.contains(WorldCurse.MAJOR_MARCH_OF_THE_DEAD)) {
            // 50% chance for undead enemy when March of the Dead is active
            if (random.nextDouble() < 0.50) {
                // Select from undead enemies
                String[] undeadEnemies = ENEMY_TYPE_MAP.entrySet().stream()
                        .filter(e -> e.getValue() == EnemyType.UNDEAD)
                        .map(Map.Entry::getKey)
                        .toArray(String[]::new);
                if (undeadEnemies.length > 0) {
                    enemyName = undeadEnemies[random.nextInt(undeadEnemies.length)];
                } else {
                    enemyName = ENEMY_NAMES[random.nextInt(ENEMY_NAMES.length)];
                }
            } else {
                enemyName = ENEMY_NAMES[random.nextInt(ENEMY_NAMES.length)];
            }
        } else {
            enemyName = ENEMY_NAMES[random.nextInt(ENEMY_NAMES.length)];
        }
        EnemyType enemyType = ENEMY_TYPE_MAP.getOrDefault(enemyName, EnemyType.PHYSICAL); // Default fallback

        // Check if this is a pack enemy (pack enemies are rare but more dangerous)
        boolean isPack = isPackEnemy(enemyName);

        // Elite Enemy System - Check for elite spawn
        boolean isElite = false;
        boolean isGodTouched = false;
        boolean isCursedElite = false;
        List<EliteTrait> eliteTraits = new ArrayList<>();
        double eliteHpModifier = 1.0;
        double eliteDamageModifier = 1.0;
        double eliteAccuracyModifier = 1.0;
        double eliteResistanceModifier = 1.0;
        String eliteDetectionNarrative = "";

        // Oathbreaker: Increased elite spawn chance (+10%)
        double eliteSpawnChanceBase = character.getLevel() >= 15 ? 0.08 : 0.05;
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
            eliteSpawnChanceBase *= 1.10; // +10% relative increase (5% -> 5.5%, 8% -> 8.8%)
        }

        // Elite spawn conditions
        if (character.getLevel() >= 6 && !isPack) {
            // Check if boss is active (skip elites during boss battles)
            boolean bossActive = false;
            if (bossService != null) {
                BossService.ServerBossState state = bossService.getState(guildId);
                if (state != null && (state.getCurrentBoss() != null || state.getCurrentSuperBoss() != null)) {
                    bossActive = true;
                }
            }

            // Note: We don't skip elites during events, only during "calm" periods
            // If there's no active event, it's considered calm, but we'll still allow elites
            // TODO: Future enhancement - check for world calm events to skip elites

            if (!bossActive) {
                // Use eliteSpawnChanceBase (already calculated with Oathbreaker bonus above)
                if (random.nextDouble() < eliteSpawnChanceBase) {
                    isElite = true;

                    // Generate elite detection narrative
                    if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
                        eliteDetectionNarrative = "The elite's eyes lock onto you. It recognizes the broken oath within you. " +
                                ELITE_DETECTION_NARRATIVES[random.nextInt(ELITE_DETECTION_NARRATIVES.length)];
                    } else {
                        eliteDetectionNarrative = ELITE_DETECTION_NARRATIVES[random.nextInt(ELITE_DETECTION_NARRATIVES.length)];
                    }

                    // Apply elite modifiers (randomized within ranges)
                    eliteHpModifier = 1.0 + (0.40 + random.nextDouble() * 0.20); // 40-60%
                    eliteDamageModifier = 1.0 + (0.25 + random.nextDouble() * 0.15); // 25-40%
                    eliteAccuracyModifier = 1.0 + (0.10 + random.nextDouble() * 0.05); // 10-15%
                    eliteResistanceModifier = 1.10; // 10% resistance to weak stat

                    // Assign elite traits (1-2 traits)
                    int traitCount = random.nextDouble() < 0.7 ? 1 : 2; // 70% chance for 1 trait, 30% for 2
                    eliteTraits = selectEliteTraits(traitCount, enemyType);

                    // Check for God-Touched elite (Level 15+, character has deity blessing)
                    if (character.getLevel() >= 15 && character.getDeityBlessing() != null) {
                        if (random.nextDouble() < 0.20) { // 20% chance
                            isGodTouched = true;
                            // TODO: Future enhancement - check if same deity or different for narrative variations
                        }
                    }

                    // Check for Cursed elite (active world curses)
                    if (!activeCurses.isEmpty()) {
                        if (random.nextDouble() < 0.30) { // 30% chance
                            isCursedElite = true;
                            // Add Cursed Mark trait
                            eliteTraits.add(EliteTrait.CURSED_BLOOD); // Using CURSED_BLOOD as cursed mark
                        }
                    }
                }
            }
        }

        // Calculate enemy strength based on character level
        int enemyLevel = Math.max(1, character.getLevel() + random.nextInt(3) - 1);
        int enemyPower = calculateEnemyPower(enemyLevel);

        // Apply elite modifiers to enemy power
        if (isElite) {
            enemyPower = (int) (enemyPower * eliteHpModifier); // HP modifier affects base power calculation
        }

        // Calculate player power with stat effectiveness
        RPGStats stats = character.getStats();

        // Apply stat modifiers from irrevocable encounters
        java.util.Map<String, Double> statModifiers = character.getStatModifiers();
        double strModifier = statModifiers.getOrDefault("STR_EFFECTIVENESS", 1.0);
        double agiModifier = statModifiers.getOrDefault("AGI_EFFECTIVENESS", 1.0);
        double intModifier = statModifiers.getOrDefault("INT_EFFECTIVENESS", 1.0);
        double luckModifier = statModifiers.getOrDefault("LUCK_EFFECTIVENESS", 1.0);

        // Create temporary stats with modifiers applied
        RPGStats effectiveStats = new RPGStats(
                stats.getMaxHp(),
                stats.getCurrentHp(),
                (int) stats.getEffectiveStrength(strModifier),
                (int) stats.getEffectiveAgility(agiModifier),
                (int) stats.getEffectiveIntelligence(intModifier),
                (int) stats.getEffectiveLuck(luckModifier)
        );

        // Elite Withdrawal Option (before battle calculations)
        boolean withdrewFromElite = false;
        if (isElite) {
            // Check withdrawal (AGI or LUCK, whichever is higher)
            int agiOrLuck = Math.max(effectiveStats.getAgility(), effectiveStats.getLuck());
            double withdrawalChance = 0.60 + (agiOrLuck / 2.0 / 100.0); // 60% base + (AGI or LUCK)/2%

            if (random.nextDouble() < withdrawalChance) {
                // Successful withdrawal
                withdrewFromElite = true;
                int escapeDamage = (int) (stats.getMaxHp() * (0.05 + random.nextDouble() * 0.05)); // 5-10% HP
                stats.takeDamage(escapeDamage);

                String withdrawalNarrative = WITHDRAWAL_SUCCESS_NARRATIVES[random.nextInt(WITHDRAWAL_SUCCESS_NARRATIVES.length)];
                List<String> traitNames = formatTraitNames(eliteTraits);
                String traitInfo = eliteTraits.isEmpty() ? "" : "\n\n**Elite Traits:** " + String.join(", ", traitNames);

                return RPGActionOutcome.builder()
                        .narrative(eliteDetectionNarrative + "\n\n" + withdrawalNarrative + traitInfo)
                        .xpGained(0)
                        .leveledUp(false)
                        .damageTaken(escapeDamage)
                        .hpRestored(0)
                        .success(false)
                        .isElite(true)
                        .eliteTraits(traitNames)
                        .withdrewFromElite(true)
                        .build();
            } else {
                // Failed withdrawal - enemy gets first strike bonus
                eliteDamageModifier *= 1.25; // +25% damage on first turn
                String withdrawalFailureNarrative = WITHDRAWAL_FAILURE_NARRATIVES[random.nextInt(WITHDRAWAL_FAILURE_NARRATIVES.length)];
                eliteDetectionNarrative += "\n\n" + withdrawalFailureNarrative;
            }
        }

        // Oathbreaker: Check for backlash event before battle
        boolean backlashTriggered = false;
        BacklashEventType backlashEvent = null;
        String backlashNarrative = "";
        double backlashStatPenalty = 1.0; // Multiplier for stat penalty
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
            int corruption = character.getCorruption();
            double backlashChance = 0.0;
            if (corruption >= 20) {
                backlashChance = 0.25; // 25% at max corruption (Embraced)
            } else if (corruption >= 15) {
                backlashChance = 0.15; // 15% at high corruption
            }

            if (backlashChance > 0 && random.nextDouble() < backlashChance) {
                backlashTriggered = true;
                backlashEvent = getRandomBacklashEvent();
                character.incrementBacklashEvents();
                backlashNarrative = handleBacklashEvent(backlashEvent, character, stats);

                // Apply stat penalty if GODS_WRATH
                if (backlashEvent == BacklashEventType.GODS_WRATH) {
                    backlashStatPenalty = 0.95; // -5% to all stats
                }
            }
        }

        int basePlayerPower = calculatePlayerPower(effectiveStats, character.getCharacterClass().name());

        // Apply backlash stat penalty if triggered
        if (backlashTriggered && backlashStatPenalty < 1.0) {
            basePlayerPower = (int) (basePlayerPower * backlashStatPenalty);
        }

        double effectiveness = getStatEffectiveness(character.getCharacterClass().name(), enemyType);

        // Apply elite resistance modifier (reduces effectiveness)
        if (isElite && effectiveness < 1.0) {
            effectiveness = 1.0 - ((1.0 - effectiveness) * (1.0 / eliteResistanceModifier)); // Reduces weakness
        }

        // Apply Shattered Reality curse (reduces effectiveness multipliers)
        if (activeCurses.contains(WorldCurse.MAJOR_SHATTERED_REALITY)) {
            if (effectiveness > 1.0) {
                // Reduce from 1.3x to 1.25x
                effectiveness = 1.0 + ((effectiveness - 1.0) * (1.25 / 1.3));
            } else if (effectiveness < 1.0) {
                // Reduce from 0.85x to 0.8x
                effectiveness = 1.0 - ((1.0 - effectiveness) * (0.2 / 0.15));
            }
        }

        // Apply Curse of Weakness (-10% STR effectiveness for physical classes)
        // Song of Nilfheim reduces the penalty
        if (activeCurses.contains(WorldCurse.MINOR_CURSE_OF_WEAKNESS)) {
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.WARRIOR ||
                    character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.KNIGHT) {
                double weaknessPenalty = 0.90 * songReduction; // Apply Song reduction
                basePlayerPower = (int) (basePlayerPower * weaknessPenalty);
            }
        }

        int playerPower = (int) (basePlayerPower * effectiveness);

        // Apply Oathbreaker corruption damage bonus
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
            int corruption = character.getCorruption();
            int corruptionCap = character.getCorruptionCap();
            int effectiveCorruption = Math.min(corruption, corruptionCap);
            double corruptionBonus = 1.0 + (effectiveCorruption * 0.01); // +1% per corruption stack
            playerPower = (int) (playerPower * corruptionBonus);
        }

        // Apply elite trait effects to player power
        if (isElite) {
            for (EliteTrait trait : eliteTraits) {
                switch (trait.getEffectType()) {
                    case DAMAGE_REDUCTION_STR:
                        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.WARRIOR ||
                                character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.KNIGHT) {
                            playerPower = (int) (playerPower * 0.85); // 15% reduction
                        }
                        break;
                    case DAMAGE_REDUCTION_STR_HEAVY:
                        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.WARRIOR ||
                                character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.KNIGHT) {
                            playerPower = (int) (playerPower * 0.80); // 20% reduction
                        }
                        break;
                    case RESISTANCE_INT:
                        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.MAGE ||
                                character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.PRIEST ||
                                character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.NECROMANCER) {
                            playerPower = (int) (playerPower * 0.90); // 10% reduction
                        }
                        break;
                    case RESISTANCE_AGI:
                        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.ROGUE) {
                            playerPower = (int) (playerPower * 0.90); // 10% reduction
                        }
                        break;
                    case UNIVERSAL_RESISTANCE:
                        playerPower = (int) (playerPower * 0.85); // 15% reduction to all
                        break;
                    case MAGICAL_AMPLIFICATION:
                        // INT attacks are stronger but also resisted - net effect depends on class
                        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.MAGE ||
                                character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.PRIEST ||
                                character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.NECROMANCER) {
                            // 15% stronger but 10% resisted = net +5% (simplified)
                            playerPower = (int) (playerPower * 1.05);
                        }
                        break;
                }
            }
        }

        // Check for critical hit (AGI-based) - use effective stats
        boolean isCrit = false;
        double critChance = effectiveStats.getAgility() / 2.0 / 100.0; // AGI/2% chance

        // Mage Arcane Precision: +5% crit chance
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.MAGE) {
            critChance += 0.05;
        }

        if (random.nextDouble() < critChance) {
            isCrit = true;
            // Rogue Lethal Strikes: 2.0x damage instead of 1.5x
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.ROGUE) {
                playerPower = (int) (playerPower * 2.0);
            } else {
                playerPower = (int) (playerPower * 1.5); // 1.5x damage on crit
            }
        }

        // Warrior Berserker Rage: +10% damage when HP < 50%
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.WARRIOR) {
            double hpPercent = (double) stats.getCurrentHp() / stats.getMaxHp();
            if (hpPercent < 0.50) {
                playerPower = (int) (playerPower * 1.10); // +10% damage
            }
        }

        // Apply Nilfheim event effects
        NilfheimEventType activeEvent = nilfheimEventService.getActiveEvent(guildId);
        if (activeEvent != null) {
            if (activeEvent.getEffectType() == NilfheimEventType.EventEffectType.BATTLE_DAMAGE_BOOST) {
                // Stormwarden's Blessing: +5% damage on Battle
                playerPower = (int) (playerPower * (1.0 + activeEvent.getEffectValue()));
            }
        }

        // Apply infusion effects for damage
        InfusionType activeInfusion = character.getInventory().getActiveInfusion();
        boolean infusionConsumed = false;
        if (activeInfusion != null) {
            infusionConsumed = true;
            if (activeInfusion == InfusionType.VOID_PRECISION) {
                // Void Precision: +8% damage on next battle
                playerPower = (int) (playerPower * 1.08);
            }
        }

        // Apply elite damage modifier to enemy power (for damage calculation)
        int effectiveEnemyPower = enemyPower;
        if (isElite) {
            effectiveEnemyPower = (int) (effectiveEnemyPower * eliteDamageModifier);

            // Apply trait effects to enemy power
            for (EliteTrait trait : eliteTraits) {
                switch (trait.getEffectType()) {
                    case FIRST_STRIKE_BOOST:
                        // Already applied via eliteDamageModifier on withdrawal failure
                        break;
                    case DAMAGE_BOOST_LOW_HP:
                        // Will be applied if enemy is below 50% HP (simulated as always active for elites)
                        effectiveEnemyPower = (int) (effectiveEnemyPower * 1.10); // +10% damage
                        break;
                    case CURSED_POWER:
                        effectiveEnemyPower = (int) (effectiveEnemyPower * 1.05); // +5% damage
                        break;
                }
            }
        }

        // Determine battle outcome - use effective stats for luck
        int playerRoll = random.nextInt(playerPower) + ((int) effectiveStats.getLuck() * 2);
        int enemyRoll = random.nextInt(effectiveEnemyPower);

        // Apply elite accuracy modifier
        if (isElite) {
            enemyRoll = (int) (enemyRoll * eliteAccuracyModifier);
        }

        boolean victory = playerRoll > enemyRoll;

        // Calculate results
        int xpGained;
        int damageTaken;
        String narrative = "";

        if (victory) {
            // Victory: high XP, minimal damage
            int baseXp = (int) ((50 + (enemyLevel * 10)) * config.getXpMultiplier());

            // Apply elite XP bonus (+30-50%)
            if (isElite) {
                double eliteXpBonus = 1.30 + (random.nextDouble() * 0.20); // 30-50% bonus
                baseXp = (int) (baseXp * eliteXpBonus);
            }

            // Apply INT-based XP bonus (INT/10% bonus, capped at 15%) - use effective stats
            double intBonus = Math.min(0.15, effectiveStats.getIntelligence() * 0.01);
            xpGained = (int) (baseXp * (1 + intBonus));

            // Apply LUCK-based XP floor (prevents extremely bad rolls) - use effective stats
            int minXp = (int) (baseXp * (1 + effectiveStats.getLuck() / 20.0));
            xpGained = Math.max(minXp, xpGained);

            // Apply Dark Relic XP bonus (+5% XP for 3 actions)
            if (character.getDarkRelicActionsRemaining() > 0) {
                xpGained = (int) (xpGained * (1.0 + character.getDarkRelicXpBonus()));
                character.decrementDarkRelicActions();
            }

            // Apply Curse of Clouded Mind (-5% XP, but ensure minimum 90%)
            // Song of Nilfheim reduces the penalty
            if (activeCurses.contains(WorldCurse.MINOR_CURSE_OF_CLOUDED_MIND)) {
                double cloudedPenalty = 0.95 * songReduction; // Apply Song reduction
                xpGained = (int) (xpGained * cloudedPenalty);
                // Ensure minimum 90% of original
                int minXpWithCurse = (int) (baseXp * 0.90);
                xpGained = Math.max(minXpWithCurse, xpGained);
            }

            // Apply Curse of Waning Resolve (XP variance shifts lower)
            // Song of Nilfheim reduces the penalty
            if (activeCurses.contains(WorldCurse.MINOR_CURSE_OF_WANING_RESOLVE)) {
                // Reduce XP by 5-10% randomly (shifts variance lower)
                double reduction = (0.05 + (random.nextDouble() * 0.05)) * songReduction; // Apply Song reduction
                xpGained = (int) (xpGained * (1 - reduction));
            }

            // Apply Nilfheim event effects for XP
            if (activeEvent != null) {
                if (activeEvent.getEffectType() == NilfheimEventType.EventEffectType.ALL_XP_BOOST) {
                    // Starfall Ridge's Light: +15% XP on all actions
                    xpGained = (int) (xpGained * (1.0 + activeEvent.getEffectValue()));
                }
            }

            // Apply infusion effects for XP
            if (activeInfusion != null) {
                if (activeInfusion == InfusionType.FROST_CLARITY) {
                    // Frost Clarity: +10% XP on next action
                    xpGained = (int) (xpGained * 1.10);
                } else if (activeInfusion == InfusionType.ELEMENTAL_CONVERGENCE) {
                    // Elemental Convergence: +15% XP on next action
                    xpGained = (int) (xpGained * 1.15);
                }
            }

            // Necromancer Decay: 10% chance to double XP on victory
            boolean decayTriggered = false;
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.NECROMANCER) {
                if (random.nextDouble() < 0.10) { // 10% chance
                    xpGained *= 2; // Double XP
                    decayTriggered = true;
                }
            }

            // Increment kill counter
            character.incrementEnemiesKilled();

            // Track elite kill
            if (isElite) {
                character.incrementEliteKills();

                // Oathbreaker: Gain corruption from elite kill
                if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
                    character.addCorruption(1);

                    // 15% chance to drop Oath Fragment
                    if (random.nextDouble() < 0.15) {
                        character.incrementOathFragments();
                        narrative += "\n\n💀 **Oath Fragment:** A fragment of your broken oath materializes from the elite's essence.";
                    }
                }
            }

            // Oathbreaker: Gain corruption from acting during world curses
            String curseCorruptionNote = "";
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER && !activeCurses.isEmpty()) {
                character.addCorruption(1);
                curseCorruptionNote = "\n\n⚔️💀 **Corruption:** The world's curses resonate with your broken oath, increasing your corruption.";
            }

            // Generate narrative with stat effectiveness and crit mentions
            String effectivenessNote = "";
            if (effectiveness > 1.0) {
                effectivenessNote = getEffectivenessNarrative(character.getCharacterClass(), enemyType, true);
            } else if (effectiveness < 1.0) {
                effectivenessNote = getEffectivenessNarrative(character.getCharacterClass(), enemyType, false);
            }

            String critNote = "";
            if (isCrit) {
                critNote = getCritNarrative(character.getCharacterClass());
            }

            String classNote = "";
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.WARRIOR) {
                double hpPercent = (double) stats.getCurrentHp() / stats.getMaxHp();
                if (hpPercent < 0.50) {
                    classNote = " Your rage intensifies as your wounds mount!";
                }
            }

            String decayNote = decayTriggered ?
                    "\n\n💀 **Decay Effect:** Your dark magic saps the enemy's essence, doubling your XP gain!" : "";

            String agilityNote = stats.getAgility() >= 15 ?
                    " Your swift movements helped you avoid the worst of the enemy's attacks." : "";

            // Format enemy name for narrative (handle pack enemies)
            String formattedEnemyName = formatEnemyNameForNarrative(enemyName, isPack);
            String packNote = isPack ?
                    " The pack's coordinated attacks made the battle more challenging, but you prevailed!" : "";

            // Elite-specific narrative
            String eliteNarrative = "";
            String traitInfo = "";
            if (isElite) {
                eliteNarrative = "\n\n" + ELITE_VICTORY_NARRATIVES[random.nextInt(ELITE_VICTORY_NARRATIVES.length)];
                if (!eliteTraits.isEmpty()) {
                    List<String> traitNames = formatTraitNames(eliteTraits);
                    traitInfo = "\n\n**Elite Traits:** " + String.join(", ", traitNames);

                    // Oathbreaker: Special trait interaction narratives
                    if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
                        String traitInteraction = getOathbreakerTraitInteraction(eliteTraits);
                        if (!traitInteraction.isEmpty()) {
                            traitInfo += "\n\n" + traitInteraction;
                        }
                    }
                }
                if (isGodTouched) {
                    String godNarrative = character.getDeityBlessing() != null ?
                            GOD_TOUCHED_SAME_DEITY_NARRATIVES[random.nextInt(GOD_TOUCHED_SAME_DEITY_NARRATIVES.length)] :
                            GOD_TOUCHED_DIFFERENT_DEITY_NARRATIVES[random.nextInt(GOD_TOUCHED_DIFFERENT_DEITY_NARRATIVES.length)];
                    eliteNarrative += "\n\n" + godNarrative;
                }
                if (isCursedElite) {
                    eliteNarrative += "\n\n" + CURSED_ELITE_DETECTION_NARRATIVES[random.nextInt(CURSED_ELITE_DETECTION_NARRATIVES.length)];
                }
            }

            // Oathbreaker: Add corruption threshold narrative
            String corruptionNote = "";
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
                int corruption = character.getCorruption();
                if (corruption >= 20) {
                    corruptionNote = "\n\n⚔️💀 **Corruption (Max):** You have fully embraced the broken oath. Demons whisper your name, and power flows through you like dark fire.";
                } else if (corruption >= 15) {
                    corruptionNote = "\n\n⚔️💀 **Corruption (High):** The broken oath screams in your mind. You walk a dangerous edge, but power answers your call.";
                } else if (corruption >= 10) {
                    corruptionNote = "\n\n⚔️💀 **Corruption:** Corruption seeps into your bones. Every strike costs more, but hits harder.";
                } else if (corruption >= 5) {
                    corruptionNote = "\n\n⚔️💀 **Corruption:** You feel the weight of your broken oath. Power flows through you, tainted but potent.";
                }
            }

            // Add backlash narrative if triggered
            if (backlashTriggered && !backlashNarrative.isEmpty()) {
                corruptionNote += "\n\n" + backlashNarrative;
            }

            String victoryNarrative = String.format(VICTORY_NARRATIVE_FORMAT,
                    formattedEnemyName, enemyLevel, critNote, effectivenessNote, classNote, agilityNote, decayNote, packNote);
            narrative = isElite ?
                    eliteDetectionNarrative + "\n\n" + victoryNarrative + eliteNarrative + traitInfo + corruptionNote + curseCorruptionNote :
                    victoryNarrative + corruptionNote + curseCorruptionNote;
        } else {
            // Defeat: moderate XP, significant damage
            int baseXp = (int) ((20 + (enemyLevel * 4)) * config.getXpMultiplier());

            // Apply INT-based XP bonus (INT/10% bonus, capped at 15%)
            double intBonus = Math.min(0.15, stats.getIntelligence() * 0.01);
            xpGained = (int) (baseXp * (1 + intBonus));

            // Apply LUCK-based XP floor
            int minXp = (int) (baseXp * (1 + stats.getLuck() / 20.0));
            xpGained = Math.max(minXp, xpGained);

            // Apply Curse of Clouded Mind (-5% XP, but ensure minimum 90%)
            if (activeCurses.contains(WorldCurse.MINOR_CURSE_OF_CLOUDED_MIND)) {
                xpGained = (int) (xpGained * 0.95);
                // Ensure minimum 90% of original
                int minXpWithCurse = (int) (baseXp * 0.90);
                xpGained = Math.max(minXpWithCurse, xpGained);
            }

            // Elite defeat penalties
            if (isElite) {
                // 15% chance to lose action charge on next refresh
                if (random.nextDouble() < 0.15) {
                    character.setLoseChargeOnNextRefresh(true);
                }

                // 10% chance for temporary curse (12 hours)
                if (random.nextDouble() < 0.10) {
                    character.setTemporaryCurseExpiresAt(Instant.now().plusSeconds(12 * 3600));
                }

                // Oathbreaker: Gain corruption even on elite defeat
                if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
                    character.addCorruption(1);
                }
            }

            // Oathbreaker: Gain corruption from acting during world curses (defeat)
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER && !activeCurses.isEmpty()) {
                character.addCorruption(1);
            }

            // Generate narrative emphasizing injury severity
            String injurySeverity = enemyLevel >= 5 ?
                    " You suffered severe injuries in the encounter." :
                    " You managed to escape, but not without significant injury.";

            // Format enemy name for narrative (handle pack enemies)
            String formattedEnemyName = formatEnemyNameForNarrative(enemyName, isPack);
            String packNote = isPack ?
                    " The pack's overwhelming numbers proved too much to handle." : "";

            // Elite-specific defeat narrative
            String eliteDefeatNarrative = "";
            String traitInfo = "";
            if (isElite) {
                eliteDefeatNarrative = "\n\n" + ELITE_DEFEAT_NARRATIVES[random.nextInt(ELITE_DEFEAT_NARRATIVES.length)];
                if (!eliteTraits.isEmpty()) {
                    List<String> traitNames = formatTraitNames(eliteTraits);
                    traitInfo = "\n\n**Elite Traits:** " + String.join(", ", traitNames);

                    // Oathbreaker: Special trait interaction narratives
                    if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
                        String traitInteraction = getOathbreakerTraitInteraction(eliteTraits);
                        if (!traitInteraction.isEmpty()) {
                            traitInfo += "\n\n" + traitInteraction;
                        }
                    }
                }
                if (character.isLoseChargeOnNextRefresh()) {
                    eliteDefeatNarrative += "\n\n⚠️ **Elite Defeat Penalty:** You will lose an action charge on your next refresh.";
                }
                if (character.hasTemporaryCurse()) {
                    eliteDefeatNarrative += "\n\n💀 **Temporary Curse:** A dark curse lingers for 12 hours, weakening your resolve.";
                }
            }

            String defeatNarrative = String.format(DEFEAT_NARRATIVE_FORMAT,
                    formattedEnemyName, enemyLevel, injurySeverity, packNote);
            narrative = isElite ?
                    eliteDetectionNarrative + "\n\n" + defeatNarrative + eliteDefeatNarrative + traitInfo :
                    defeatNarrative;
        }

        // Calculate base damage with variance
        int baseDamage = calculateBaseDamage(enemyPower, enemyLevel, victory);

        // Apply ±25% random variance
        // Variance range: baseDamage * 0.75 to baseDamage * 1.25
        int minDamage = (int) (baseDamage * 0.75);
        int maxDamage = (int) (baseDamage * 1.25);
        int damageWithVariance = minDamage + random.nextInt(maxDamage - minDamage + 1);
        damageWithVariance = Math.max(1, damageWithVariance); // Ensure at least 1 damage

        // Apply pack enemy damage multiplier (pack enemies are rare but more dangerous)
        if (isPack) {
            damageWithVariance = (int) (damageWithVariance * 1.15); // Pack enemies deal 15% more damage
        }

        // Apply agility-based defense (1% per agility point, capped at 30%)
        double agilityReduction = Math.min(0.30, stats.getAgility() * 0.01);

        // Apply Curse of Sluggish Steps (reduces AGI defense cap: 30% → 25%)
        // Song of Nilfheim reduces the penalty
        if (activeCurses.contains(WorldCurse.MINOR_CURSE_OF_SLUGGISH_STEPS)) {
            double sluggishPenalty = 0.25 * songReduction; // Apply Song reduction
            agilityReduction = Math.min(sluggishPenalty, agilityReduction);
        }

        damageTaken = (int) (damageWithVariance * (1 - agilityReduction));

        // Apply Eclipse of Nilfheim (+10% enemy damage)
        // Song of Nilfheim reduces the penalty
        if (activeCurses.contains(WorldCurse.MAJOR_ECLIPSE_OF_NILFHEIM)) {
            double eclipseBonus = 1.10 / songReduction; // Reduce the bonus (divide by reduction)
            damageTaken = (int) (damageTaken * eclipseBonus);
        }

        // Apply Curse of Bleeding Wounds (+10% defeat damage)
        // Song of Nilfheim reduces the penalty
        if (!victory && activeCurses.contains(WorldCurse.MINOR_CURSE_OF_BLEEDING_WOUNDS)) {
            double bleedingBonus = 1.10 / songReduction; // Reduce the bonus
            damageTaken = (int) (damageTaken * bleedingBonus);
        }

        // Apply March of the Dead (+15% defeat damage)
        // Song of Nilfheim reduces the penalty
        if (!victory && activeCurses.contains(WorldCurse.MAJOR_MARCH_OF_THE_DEAD)) {
            double marchBonus = 1.15 / songReduction; // Reduce the bonus
            damageTaken = (int) (damageTaken * marchBonus);
        }

        // Apply class bonus (Knight gets 10% additional reduction, stacks with agility)
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.KNIGHT) {
            damageTaken = (int) (damageTaken * 0.90); // 10% reduction (balanced from 15%)
        }

        // Apply Oathbreaker corruption damage penalty
        if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
            int corruption = character.getCorruption();
            if (corruption >= 20) {
                // Embraced path at max corruption
                damageTaken = (int) (damageTaken * 1.20); // +20% incoming damage
            } else if (corruption >= 15) {
                damageTaken = (int) (damageTaken * 1.15); // +15% incoming damage
            } else if (corruption >= 10) {
                damageTaken = (int) (damageTaken * 1.10); // +10% incoming damage
            }

            // Apply Purge path damage reduction
            if ("PURGE".equals(character.getOathbreakerPath())) {
                damageTaken = (int) (damageTaken * 0.95); // -5% damage reduction
            }

            // Apply Purge path curse penalty reduction
            if ("PURGE".equals(character.getOathbreakerPath()) && !activeCurses.isEmpty()) {
                // -10% curse penalties (applied to curse effects above)
                // This is handled in individual curse applications
            }
        }

        // Apply Dark Relic damage penalty (+10% damage taken until rested)
        if (character.getDarkRelicActionsRemaining() > 0) {
            damageTaken = (int) (damageTaken * (1.0 + character.getDarkRelicDamagePenalty()));
        }

        // Apply infusion effects for defeat damage reduction
        if (!victory && activeInfusion != null) {
            if (activeInfusion == InfusionType.EMBER_ENDURANCE) {
                // Ember Endurance: -20% defeat damage
                damageTaken = (int) (damageTaken * 0.80);
            }
        }

        // Capture HP before damage for story flags
        int hpBefore = stats.getCurrentHp();

        // Apply damage (can now kill character)
        boolean isAlive = stats.takeDamage(damageTaken);

        // Check for Unstable Essence trait (explosion on death)
        if (isElite && eliteTraits.contains(EliteTrait.UNSTABLE_ESSENCE) && victory) {
            // Elite died, explosion deals minor unavoidable damage
            int explosionDamage = (int) (stats.getMaxHp() * 0.05); // 5% max HP
            stats.takeDamage(explosionDamage);
            narrative += "\n\n💥 **Unstable Essence:** The elite's essence explodes violently, dealing " + explosionDamage + " unavoidable damage!";
        }

        // Check if character died
        if (!isAlive || stats.getCurrentHp() <= 0) {
            character.die();
            character.incrementDeathCount();

            // Oathbreaker: Unique death mechanics
            if (character.getCharacterClass() == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER) {
                double deathRoll = random.nextDouble();
                if (deathRoll < 0.30) {
                    // 30% chance: Lose 2-3 corruption (despair purges some)
                    int corruptionLoss = 2 + random.nextInt(2); // 2-3
                    character.removeCorruption(corruptionLoss);
                    narrative += "\n\n⚔️💀 **Death's Purge:** In death's embrace, some corruption is purged. You lose " + corruptionLoss + " corruption.";
                } else if (deathRoll < 0.50) {
                    // 20% chance: Gain 1-2 corruption (despair strengthens oath)
                    int corruptionGain = 1 + random.nextInt(2); // 1-2
                    character.addCorruption(corruptionGain);
                    narrative += "\n\n⚔️💀 **Death's Embrace:** Despair strengthens the broken oath. You gain " + corruptionGain + " corruption.";
                } else if (deathRoll < 0.60) {
                    // 10% chance: Vision encounter (special narrative)
                    narrative += "\n\n⚔️💀 **Vision of the Broken Oath:** In death's threshold, you see visions of the oath you broke. The memory is both curse and blessing.";
                }
                // 40% chance: Normal death (no corruption change)
            }

            // Check for story flag: "Survived death at 1 HP" (if HP was exactly 1 before death)
            if (hpBefore == 1) {
                character.addStoryFlag("Survived death at 1 HP");
            }

            // Check for story flag: "Once fled from battle in terror" (defeat with very low damage)
            if (!victory && playerPower < 10) {
                character.addStoryFlag("Once fled from battle in terror");
            }

            narrative += "\n\n💀 **You have fallen in battle!** A Priest can resurrect you.";
        }

        // Add XP and check for level up (only if alive)
        boolean leveledUp;
        if (isAlive) {
            leveledUp = character.addXp(xpGained, loreRecognitionService);
        } else {
            // Dead characters still get some XP (defeat bonus)
            xpGained = (int) (xpGained * 0.5); // Half XP on death
            leveledUp = character.addXp(xpGained, loreRecognitionService);
        }

        // Elite rewards (guaranteed on victory)
        if (isElite && victory) {
            // Guaranteed crafting material drop
            boolean isCatalyst = random.nextDouble() < 0.5; // 50% chance for catalyst
            if (isCatalyst) {
                CatalystType catalyst = getRandomCatalyst();
                character.getInventory().addCatalyst(catalyst, 1);
            } else {
                EssenceType essence = getRandomEssence();
                character.getInventory().addEssence(essence, 1);
            }

            // 5-8% chance for rare drop (infusion, catalyst, or rare essence)
            double rareDropChance = 0.05 + (random.nextDouble() * 0.03); // 5-8%
            if (random.nextDouble() < rareDropChance) {
                int rareType = random.nextInt(3);
                if (rareType == 0) {
                    // Rare infusion (if we had a way to grant infusions directly, for now just catalyst)
                    CatalystType rareCatalyst = getRandomCatalyst();
                    character.getInventory().addCatalyst(rareCatalyst, 1);
                } else if (rareType == 1) {
                    CatalystType rareCatalyst = getRandomCatalyst();
                    character.getInventory().addCatalyst(rareCatalyst, 1);
                } else {
                    EssenceType rareEssence = getRandomEssence();
                    character.getInventory().addEssence(rareEssence, 1);
                }
            }
        }

        // Roll for item drops with LUCK bonus
        RPGActionOutcome.Builder outcomeBuilder = RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .damageTaken(damageTaken)
                .hpRestored(0)
                .success(victory)
                .isElite(isElite)
                .eliteTraits(formatTraitNames(eliteTraits))
                .withdrewFromElite(withdrewFromElite);

        // Base drop chance: Victory 20%, Defeat 5%
        // Note: Elite victories already got guaranteed drop above, so this is additional
        double baseDropChance = victory ? 0.20 : 0.05;
        // LUCK bonus: +0.3% per LUCK point, capped at +10%
        double luckBonus = Math.min(0.10, stats.getLuck() * 0.003);
        double dropChance = baseDropChance + luckBonus;

        if (random.nextDouble() < dropChance) {
            // LUCK-based rare item chance: LUCK/10% chance for catalyst instead of essence
            boolean isCatalyst = random.nextDouble() < (stats.getLuck() / 10.0 / 100.0);

            if (isCatalyst) {
                CatalystType catalyst = getRandomCatalyst();
                outcomeBuilder.addCatalystDrop(catalyst, 1);
                character.getInventory().addCatalyst(catalyst, 1);
            } else {
                EssenceType essence = getRandomEssence();
                outcomeBuilder.addItemDrop(essence, 1);
                character.getInventory().addEssence(essence, 1);
            }
        }

        // Add elite guaranteed drops to outcome builder
        if (isElite && victory) {
            // The drops were already added to inventory above, but we should show them in the outcome
            // For now, they're included in the narrative, so we don't need to duplicate here
        }

        // Consume active infusion if used
        if (infusionConsumed) {
            character.getInventory().consumeActiveInfusion();
        }

        // Record the action
        character.recordAction();

        // Track action type for achievements
        character.recordActionType("battle");

        return outcomeBuilder.build();
    }

    /**
     * Gets a random essence type.
     *
     * @return random essence type
     */
    private EssenceType getRandomEssence() {
        EssenceType[] essences = EssenceType.values();
        return essences[random.nextInt(essences.length)];
    }

    /**
     * Gets a random catalyst type.
     *
     * @return random catalyst type
     */
    private CatalystType getRandomCatalyst() {
        CatalystType[] catalysts = CatalystType.values();
        return catalysts[random.nextInt(catalysts.length)];
    }

    /**
     * Calculates enemy power based on level.
     */
    private int calculateEnemyPower(int level) {
        return 20 + (level * 8);
    }

    /**
     * Calculates player power from stats.
     */
    private int calculatePlayerPower(RPGStats stats, String className) {
        return switch (className) {
            case "WARRIOR" -> (stats.getStrength() * 2) + stats.getLuck();
            case "KNIGHT" -> (stats.getStrength() * 2) + stats.getAgility();
            case "MAGE", "PRIEST" -> (stats.getIntelligence() * 2) + stats.getAgility();
            case "ROGUE" -> (stats.getAgility() * 2) + stats.getStrength();
            case "NECROMANCER" -> (stats.getIntelligence() * 2) + stats.getLuck();
            case "OATHBREAKER" -> (stats.getStrength() * 2) + stats.getLuck();
            default -> stats.getStrength() + stats.getAgility() + stats.getIntelligence();
        };
    }

    /**
     * Gets stat effectiveness multiplier based on class and enemy type.
     *
     * @param className the character class name
     * @param enemyType the enemy type
     * @return effectiveness multiplier (1.3x for effective, 0.85x for weak, 1.0x for neutral)
     */
    private double getStatEffectiveness(String className, EnemyType enemyType) {
        // STR-based classes (Warrior, Knight, Oathbreaker)
        if (className.equals("WARRIOR") || className.equals("KNIGHT") || className.equals("OATHBREAKER")) {
            return switch (enemyType) {
                case PHYSICAL, CONSTRUCT -> 1.3; // STR effective
                case MAGICAL, AGILE -> 0.85; // STR weak
                default -> 1.0; // Neutral
            };
        }
        // INT-based classes (Mage, Priest, Necromancer)
        else if (className.equals("MAGE") || className.equals("PRIEST") || className.equals("NECROMANCER")) {
            return switch (enemyType) {
                case MAGICAL, UNDEAD -> 1.3; // INT effective
                case CONSTRUCT, PHYSICAL -> 0.85; // INT weak
                default -> 1.0; // Neutral
            };
        }
        // AGI-based classes (Rogue)
        else if (className.equals("ROGUE")) {
            return switch (enemyType) {
                case AGILE, BEAST -> 1.3; // AGI effective
                case PHYSICAL, CONSTRUCT -> 0.85; // AGI weak
                default -> 1.0; // Neutral
            };
        }
        return 1.0; // Default neutral
    }

    /**
     * Gets effectiveness-based narrative text.
     *
     * @param characterClass the character's class
     * @param enemyType      the enemy type
     * @param effective      whether the attack was effective
     * @return narrative text
     */
    private String getEffectivenessNarrative(com.tatumgames.mikros.games.rpg.model.CharacterClass characterClass, EnemyType enemyType, boolean effective) {
        if (effective) {
            return switch (characterClass) {
                case WARRIOR, KNIGHT, OATHBREAKER -> switch (enemyType) {
                    case PHYSICAL -> " Your brute strength shattered the enemy's armor!";
                    case CONSTRUCT -> " Your physical might crushed through the construct's defenses!";
                    default -> " Your strength proved highly effective!";
                };
                case MAGE, PRIEST, NECROMANCER -> switch (enemyType) {
                    case MAGICAL -> " Your magic pierced through the enemy's magical barriers!";
                    case UNDEAD -> " Your magic burned through the undead's dark essence!";
                    default -> " Your magical attacks were highly effective!";
                };
                case ROGUE -> switch (enemyType) {
                    case AGILE -> " Your swift strikes found gaps in the enemy's defenses!";
                    case BEAST -> " Your precision strikes overwhelmed the beast!";
                    default -> " Your agility proved highly effective!";
                };
            };
        } else {
            return switch (characterClass) {
                case WARRIOR, KNIGHT, OATHBREAKER -> switch (enemyType) {
                    case MAGICAL -> " Your physical attacks struggled against the enemy's magical defenses.";
                    case AGILE -> " The enemy's speed made your heavy strikes difficult to land.";
                    default -> " Your attacks were less effective against this enemy type.";
                };
                case MAGE, PRIEST, NECROMANCER -> switch (enemyType) {
                    case CONSTRUCT -> " Your magic had little effect on the construct's mechanical nature.";
                    case PHYSICAL -> " The enemy's heavy armor resisted your magical attacks.";
                    default -> " Your attacks were less effective against this enemy type.";
                };
                case ROGUE -> switch (enemyType) {
                    case PHYSICAL -> " The enemy's heavy armor deflected your precision strikes.";
                    case CONSTRUCT -> " Your blades couldn't pierce the construct's stone shell.";
                    default -> " Your attacks were less effective against this enemy type.";
                };
            };
        }
    }

    /**
     * Gets critical hit narrative text based on class.
     *
     * @param characterClass the character's class
     * @return crit narrative text
     */
    private String getCritNarrative(com.tatumgames.mikros.games.rpg.model.CharacterClass characterClass) {
        return switch (characterClass) {
            case ROGUE -> " A lethal strike finds the perfect opening!";
            case MAGE -> " Your magical precision finds a critical weakness!";
            case WARRIOR -> " A devastating critical blow!";
            case KNIGHT -> " Your shield bash finds a critical opening!";
            case NECROMANCER -> " Your dark magic strikes at the enemy's very soul!";
            case PRIEST -> " Your holy magic finds a critical weakness in the enemy's defenses!";
            case OATHBREAKER -> " The broken oath's power surges, finding a critical weakness!";
        };
    }

    /**
     * Checks if an enemy is a pack enemy (contains "Pack" in the name).
     *
     * @param enemyName the enemy name
     * @return true if it's a pack enemy
     */
    private boolean isPackEnemy(String enemyName) {
        return enemyName != null && enemyName.toLowerCase().contains("pack");
    }

    /**
     * Formats enemy name for narrative, handling pack enemies specially.
     *
     * @param enemyName the enemy name
     * @param isPack    whether it's a pack enemy
     * @return formatted enemy name for narrative
     */
    private String formatEnemyNameForNarrative(String enemyName, boolean isPack) {
        if (isPack) {
            // Remove "Pack" suffix and format as "a pack of [enemy type]"
            String baseName = enemyName.replaceAll("(?i)\\s*pack\\s*$", "");
            // Get emoji from enemy type
            EnemyType enemyType = ENEMY_TYPE_MAP.getOrDefault(enemyName, EnemyType.PHYSICAL);
            return String.format("**%s a pack of %s**", enemyType.getEmoji(), baseName);
        } else {
            // Regular enemy - get emoji from enemy type
            EnemyType enemyType = ENEMY_TYPE_MAP.getOrDefault(enemyName, EnemyType.PHYSICAL);
            return String.format("a **%s %s**", enemyType.getEmoji(), enemyName);
        }
    }

    /**
     * Calculates base damage based on enemy power and level.
     * Scales with enemy strength and applies different formulas for victory vs defeat.
     *
     * @param enemyPower the calculated enemy power
     * @param enemyLevel the enemy's level
     * @param victory    whether the player won the battle
     * @return base damage before variance and reductions
     */
    private int calculateBaseDamage(int enemyPower, int enemyLevel, boolean victory) {
        if (victory) {
            // Victory: base damage scales with enemy power (15% of power)
            int baseDamage = (int) (enemyPower * 0.15);
            // Minimum: 8 + (enemyLevel * 2) to ensure meaningful damage even at low levels
            int minDamage = 8 + (enemyLevel * 2);
            // Maximum: 25% of enemy power to prevent extreme values
            int maxDamage = (int) (enemyPower * 0.25);
            // Clamp base damage between minimum and maximum
            return Math.max(minDamage, Math.min(baseDamage, maxDamage));
        } else {
            // Defeat: base damage scales with enemy power (35% of power)
            int baseDamage = (int) (enemyPower * 0.35);
            // Minimum: 15 + (enemyLevel * 4) to ensure defeat is costly
            int minDamage = 15 + (enemyLevel * 4);
            // Maximum: 50% of enemy power to prevent extreme values
            int maxDamage = (int) (enemyPower * 0.50);
            // Clamp base damage between minimum and maximum
            return Math.max(minDamage, Math.min(baseDamage, maxDamage));
        }
    }

    /**
     * Selects random elite traits for an elite enemy.
     *
     * @param count     the number of traits to select (1-2)
     * @param enemyType the enemy type (for trait weighting)
     * @return list of selected traits
     */
    private List<EliteTrait> selectEliteTraits(int count, EnemyType enemyType) {
        List<EliteTrait> availableTraits = new ArrayList<>(Arrays.asList(EliteTrait.values()));
        List<EliteTrait> selected = new ArrayList<>();

        // Weight traits based on enemy type
        // Physical enemies more likely to have physical traits, etc.
        for (int i = 0; i < count && !availableTraits.isEmpty(); i++) {
            EliteTrait selectedTrait = availableTraits.get(random.nextInt(availableTraits.size()));
            selected.add(selectedTrait);
            availableTraits.remove(selectedTrait); // Don't allow duplicate traits
        }

        return selected;
    }

    /**
     * Formats elite trait names for display.
     *
     * @param traits the list of elite traits
     * @return formatted string of trait names
     */
    private List<String> formatTraitNames(List<EliteTrait> traits) {
        List<String> traitNames = new ArrayList<>();
        for (EliteTrait trait : traits) {
            traitNames.add(trait.getDisplayName());
        }
        return traitNames;
    }

    /**
     * Gets a random backlash event type.
     *
     * @return random backlash event
     */
    private BacklashEventType getRandomBacklashEvent() {
        BacklashEventType[] events = BacklashEventType.values();
        return events[random.nextInt(events.length)];
    }

    /**
     * Handles a backlash event and returns narrative.
     *
     * @param event     the backlash event type
     * @param character the character
     * @param stats     the character's stats
     * @return narrative describing the backlash
     */
    private String handleBacklashEvent(BacklashEventType event, RPGCharacter character, RPGStats stats) {
        String narrative = "";

        switch (event.getEffectType()) {
            case ELITE_SPAWN:
                // Note: This would spawn an additional elite, but for simplicity, we'll just add narrative
                narrative = "⚔️💀 **" + event.getDisplayName() + ":** " + event.getDescription() +
                        " The broken oath draws another elite to the fight!";
                break;
            case TEMPORARY_CURSE:
                character.setTemporaryCurseExpiresAt(Instant.now().plusSeconds(12 * 3600));
                narrative = "⚔️💀 **" + event.getDisplayName() + ":** " + event.getDescription() +
                        " A temporary curse afflicts you for 12 hours.";
                break;
            case POWER_OFFER:
                character.addCorruption(1);
                // Apply +5% damage bonus for this battle (handled via corruption bonus)
                narrative = "⚔️💀 **" + event.getDisplayName() + ":** " + event.getDescription() +
                        " You accept the demon's offer, gaining corruption and power for this battle.";
                break;
            case DAMAGE:
                int surgeDamage = (int) (stats.getMaxHp() * (0.05 + random.nextDouble() * 0.05)); // 5-10% max HP
                stats.takeDamage(surgeDamage);
                narrative = "⚔️💀 **" + event.getDisplayName() + ":** " + event.getDescription() +
                        " You take " + surgeDamage + " damage from the corruption surge.";
                break;
            case STAT_PENALTY:
                // Applied via effectiveness reduction (handled in narrative)
                narrative = "⚔️💀 **" + event.getDisplayName() + ":** " + event.getDescription() +
                        " Your power is weakened this battle.";
                break;
        }

        return narrative;
    }

    /**
     * Gets Oathbreaker-specific narrative for elite trait interactions.
     *
     * @param eliteTraits the elite traits
     * @return narrative snippet, or empty string if no relevant traits
     */
    private String getOathbreakerTraitInteraction(List<EliteTrait> eliteTraits) {
        for (EliteTrait trait : eliteTraits) {
            switch (trait) {
                case CURSED_BLOOD:
                    return "⚔️💀 The elite's cursed blood resonates with your broken oath. Dark power recognizes dark power.";
                case RUNE_TOUCHED:
                    return "⚔️💀 Ancient runes react to your presence, recognizing a kindred spirit. The broken oath and the runes share a connection.";
                case UNSTABLE_ESSENCE:
                    return "⚔️💀 The unstable essence recognizes the broken oath within you. There's a moment of... understanding?";
                default:
                    break;
            }
        }
        return "";
    }
}
