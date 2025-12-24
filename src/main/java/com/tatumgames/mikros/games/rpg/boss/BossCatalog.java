package com.tatumgames.mikros.games.rpg.boss;

import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.BossType;
import com.tatumgames.mikros.games.rpg.model.SuperBoss;

import java.util.*;

/**
 * Catalog of all bosses in the RPG system.
 * Contains 48 normal bosses (4 per level for levels 1-12) and 20 super bosses.
 */
public class BossCatalog {
    private static final Random random = new Random();

    // Normal bosses by level (2 per level for levels 1-12)
    private static final Map<Integer, List<BossDefinition>> NORMAL_BOSSES = new HashMap<>();

    // Super bosses
    private static final List<SuperBossDefinition> SUPER_BOSSES = new ArrayList<>();

    static {
        initializeNormalBosses();
        initializeSuperBosses();
    }

    /**
     * Gets a random boss for a specific level.
     *
     * @param level the boss level
     * @return a boss definition
     */
    public static BossDefinition getRandomNormalBoss(int level) {
        int actualLevel = Math.max(1, Math.min(12, level));
        List<BossDefinition> bosses = NORMAL_BOSSES.get(actualLevel);
        if (bosses == null || bosses.isEmpty()) {
            // Fallback to level 1
            bosses = NORMAL_BOSSES.get(1);
        }
        return bosses.get(random.nextInt(bosses.size()));
    }

    /**
     * Gets a super boss for a specific level.
     *
     * @param level the super boss level
     * @return a super boss definition
     */
    public static SuperBossDefinition getSuperBoss(int level) {
        int index = Math.min(level - 1, SUPER_BOSSES.size() - 1);
        return SUPER_BOSSES.get(Math.max(0, index));
    }

    /**
     * Creates a Boss instance from a definition.
     *
     * @param definition the boss definition
     * @param level      the boss level
     * @return a Boss instance
     */
    public static Boss createBoss(BossDefinition definition, int level) {
        int maxHp = 10000 * level;
        int attack = 50 + (level * 20);
        String bossId = "boss_" + definition.name.toLowerCase().replaceAll("\\s+", "_") + "_" + level;

        return new Boss(bossId, definition.name, definition.type, level, maxHp, attack);
    }

    /**
     * Creates a SuperBoss instance from a definition.
     *
     * @param definition the super boss definition
     * @param level      the super boss level
     * @return a SuperBoss instance
     */
    public static SuperBoss createSuperBoss(SuperBossDefinition definition, int level) {
        int maxHp = 50000 * level;
        int attack = 200 + (level * 50);
        String bossId = "superboss_" + definition.name.toLowerCase().replaceAll("\\s+", "_") + "_" + level;

        return new SuperBoss(
                bossId, definition.name, definition.type, level, maxHp, attack, definition.specialMechanic);
    }

    private static void initializeNormalBosses() {
        // Level 1 Bosses
        NORMAL_BOSSES.put(1, Arrays.asList(
                new BossDefinition("Frostbitten Troll", BossType.BEAST, "A sluggish brute cursed by the eternal frost. Its roars shake the snowy plains."),
                new BossDefinition("Spirit Wisp Horror", BossType.SPIRIT, "A cluster of lost souls, bound together by bitterness and cold moonlight."),
                new BossDefinition("Frostbite Shade", BossType.SPIRIT, "A wandering spirit from the Spirit Veil, drawn to the warmth of living souls."),
                new BossDefinition("Icebound Bandit", BossType.HUMANOID, "A desperate marauder who found refuge in the frozen wastes of Nilfheim.")
        ));

        // Level 2 Bosses
        NORMAL_BOSSES.put(2, Arrays.asList(
                new BossDefinition("Bonegnasher Ghoul", BossType.UNDEAD, "It hunts in packs but commands them alone — a ghoul of unnatural intellect."),
                new BossDefinition("Frostfang Direwolf", BossType.BEAST, "A monstrous wolf whose howl freezes blood and courage alike."),
                new BossDefinition("Glacial Wraith", BossType.SPIRIT, "A spirit bound to the ice, forever searching for its lost warmth."),
                new BossDefinition("Frozen Marauder", BossType.HUMANOID, "A warrior encased in ice, still fighting battles long forgotten.")
        ));

        // Level 3 Bosses
        NORMAL_BOSSES.put(3, Arrays.asList(
                new BossDefinition("Iceborne Sorcerer", BossType.HUMANOID, "A warlock whose soul fused with an ancient glacier."),
                new BossDefinition("Corpse Stitcher", BossType.UNDEAD, "A grotesque surgeon of death who creates abominations from fallen heroes."),
                new BossDefinition("Frostweaver", BossType.ELEMENTAL, "A master of the Frost element, weaving ice into deadly patterns."),
                new BossDefinition("Rimebound Berserker", BossType.HUMANOID, "A warrior driven mad by the endless cold, seeking warmth in battle.")
        ));

        // Level 4 Bosses
        NORMAL_BOSSES.put(4, Arrays.asList(
                new BossDefinition("Shadowblade Assassin", BossType.HUMANOID, "A killer whose blades thirst for notoriety in Nilfheim's darkness."),
                new BossDefinition("Void-Touched Servitor", BossType.ELDRITCH, "A servant of the deep void, speaking in fractal whispers."),
                new BossDefinition("Stormcaller", BossType.ELEMENTAL, "A being of pure Gale energy, commanding the winds of Nilfheim."),
                new BossDefinition("Grave Warden", BossType.UNDEAD, "Guardian of ancient burial grounds, risen to protect the dead.")
        ));

        // Level 5 Bosses
        NORMAL_BOSSES.put(5, Arrays.asList(
                new BossDefinition("Frost Titan", BossType.GIANT, "A towering mountain of ice and rage. Footsteps cause avalanches."),
                new BossDefinition("Eternal Frost Witch", BossType.HUMANOID, "Her heart froze a century ago—but her hatred burns steady."),
                new BossDefinition("Ember Drake", BossType.DRAGON, "A dragon of fire trapped in ice, its rage burning eternal."),
                new BossDefinition("Void Walker", BossType.ELDRITCH, "A creature that phases between the Mortal and Arcane Veils.")
        ));

        // Level 6 Bosses
        NORMAL_BOSSES.put(6, Arrays.asList(
                new BossDefinition("Crypt Sovereign", BossType.UNDEAD, "Rules over ancient burial chambers beneath Nilfheim's tundras."),
                new BossDefinition("Stormborn Gryphon", BossType.BEAST, "A majestic predator forged from lightning storms."),
                new BossDefinition("Frostborne Champion", BossType.HUMANOID, "A legendary warrior from the ancient Frostborne civilization."),
                new BossDefinition("Crystal Leviathan", BossType.CONSTRUCT, "A massive construct of living crystal, awakened by the Shattering.")
        ));

        // Level 7 Bosses
        NORMAL_BOSSES.put(7, Arrays.asList(
                new BossDefinition("Plague Herald", BossType.DEMON, "Bringer of pestilence, whisperer of corruption."),
                new BossDefinition("Shiverheart Basilisk", BossType.BEAST, "Its gaze freezes both flesh and courage."),
                new BossDefinition("Astral Seer", BossType.SPIRIT, "A spirit that glimpses possible futures through the Astral element."),
                new BossDefinition("Forge Master", BossType.CONSTRUCT, "An ancient automaton that repairs itself and others in battle.")
        ));

        // Level 8 Bosses
        NORMAL_BOSSES.put(8, Arrays.asList(
                new BossDefinition("Ironhide Juggernaut", BossType.CONSTRUCT, "An unstoppable machine of ancient origin, awakened by catastrophe."),
                new BossDefinition("Riftbreaker Harpy", BossType.BEAST, "A winged banshee whose scream can shatter sanity."),
                new BossDefinition("Soul Harvester", BossType.UNDEAD, "Steals the essence of fallen heroes, growing stronger with each soul."),
                new BossDefinition("Storm King", BossType.DRAGON, "A dragon that commands the Gale element, unleashing area-of-effect lightning.")
        ));

        // Level 9 Bosses
        NORMAL_BOSSES.put(9, Arrays.asList(
                new BossDefinition("Frostwraith Matriarch", BossType.SPIRIT, "A powerful mother-wraith mourning her long-lost children."),
                new BossDefinition("Dreadhorn Minotaur", BossType.BEAST, "A labyrinth guardian forged from icy rage."),
                new BossDefinition("Corrupted Titan", BossType.GIANT, "A titan twisted by dark magic, growing stronger as its wounds mount."),
                new BossDefinition("Void Archon", BossType.ELDRITCH, "A being of pure void energy, phasing in and out of reality.")
        ));

        // Level 10 Bosses
        NORMAL_BOSSES.put(10, Arrays.asList(
                new BossDefinition("Polar Hydra", BossType.DRAGON, "Three snapping heads, one frozen heart."),
                new BossDefinition("Soulflayer Acolyte", BossType.HUMANOID, "Consumes the souls of heroes to feed its god."),
                new BossDefinition("The Frostweaver", BossType.ELEMENTAL, "Manipulates the battlefield itself, creating and destroying terrain."),
                new BossDefinition("Grand Librarian", BossType.HUMANOID, "A keeper of forbidden knowledge from the Grand Library of Nil City.")
        ));

        // Level 11 Bosses
        NORMAL_BOSSES.put(11, Arrays.asList(
                new BossDefinition("Ashen Revenant", BossType.UNDEAD, "A resurrected hero twisted by hatred and regret."),
                new BossDefinition("Crystalbound Dragonspawn", BossType.DRAGON, "Fragments of an ancient dragon reforged by frozen magic."),
                new BossDefinition("Starfall Guardian", BossType.CONSTRUCT, "A construct from Starfall Ridge, powered by fallen star fragments."),
                new BossDefinition("Moonspire Keeper", BossType.SPIRIT, "Guardian of the Moonspire Obelisk, keeper of ancient runic magic.")
        ));

        // Level 12 Bosses
        NORMAL_BOSSES.put(12, Arrays.asList(
                new BossDefinition("The Rime Executioner", BossType.HUMANOID, "A sentient suit of armor possessed by an ice spirit."),
                new BossDefinition("Warden of the Shattered Gate", BossType.ELDRITCH, "Stands watch over a forgotten portal leading into nothingness."),
                new BossDefinition("Frostgate Warlord", BossType.HUMANOID, "A legendary warrior from Frostgate, master of the Frostborne techniques."),
                new BossDefinition("The Shattering Remnant", BossType.ELDRITCH, "A fragment of the cataclysm that created the Shattering of the First Winter.")
        ));
    }

    private static void initializeSuperBosses() {
        SUPER_BOSSES.add(new SuperBossDefinition(
                "Ymir the Winterbound Colossus", BossType.GIANT,
                "Said to be the first creature to ever walk Nilfheim's tundras.",
                "Every 20% HP, unleashes 'Avalanche Crash.'"));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Skorn, Devourer of Hope", BossType.DEMON,
                "A demon whose presence extinguishes courage itself.",
                "Steals HP from highest-damage attacker each round."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Vespera, Queen of Frostwraiths", BossType.SPIRIT,
                "Mother of spirits, draped in sorrow and moonlight.",
                "High dodge → 30% player attacks miss."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "The Eternal Maw", BossType.ELDRITCH,
                "A floating, many-jawed void creature feeding endlessly.",
                "50% of damage is delayed (DoT effect on boss)."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Draugr King Halrom", BossType.UNDEAD,
                "Once a beloved ruler; now a tyrant of the dead.",
                "Summons spectral guards (flavor-only)."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Stormlord Valkyrios", BossType.DRAGON,
                "A dragon embodying the storm's will.",
                "Random lightning strikes hit all attackers."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "The Obsidian Reaper", BossType.CONSTRUCT,
                "A reaper forged from cursed metal.",
                "Reflects small % of damage back at attackers."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Níðhollow Serpent", BossType.ELDRITCH,
                "Coils through the void beneath Nilfheim.",
                "Can 'swallow' a random attacker (flavor stun)."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Bloodmoon Matron", BossType.DEMON,
                "On the night of the Bloodmoon, she hunts for heroes' hearts.",
                "Heals from total player damage dealt."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "The Rune-Eater Behemoth", BossType.CONSTRUCT,
                "Consumes magic itself until it becomes unstoppable.",
                "Reduces all magic-based player damage."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Frostwind Chimera", BossType.BEAST,
                "A fusion of lion, ram, serpent — born of cursed magic.",
                "Random multi-type damage."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Kalgorath, Harbinger of the Void Star", BossType.ELDRITCH,
                "A cosmic destroyer drawn to the suffering within Nilfheim. Defeating it 'delays the end of all things'… temporarily.",
                "Phased fight: At 75%, 50%, 25%, attacks become more violent."));

        // New Super Bosses (Content Expansion)
        SUPER_BOSSES.add(new SuperBossDefinition(
                "The Frostweaver", BossType.ELEMENTAL,
                "An ancient master of the Frost element who can manipulate the very battlefield itself.",
                "Creates and destroys terrain, changing the flow of battle."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Corrupted Titan", BossType.GIANT,
                "A titan from before the Shattering, twisted by dark magic and endless suffering.",
                "Grows stronger as HP decreases - damage increases by 10% for every 10% HP lost."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Soul Harvester", BossType.UNDEAD,
                "A being that feeds on the essence of heroes, growing stronger with each soul consumed.",
                "Steals 5% of XP from each player's battle gains."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Void Walker", BossType.ELDRITCH,
                "A creature that exists between the Mortal and Arcane Veils, phasing in and out of reality.",
                "30% chance to phase out each turn, becoming immune to damage."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "Storm King", BossType.DRAGON,
                "A dragon that commands the Gale element, its very presence electrifying the air.",
                "Area-of-effect lightning strikes hit all attackers for 20% of their damage."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "The Forge Master", BossType.CONSTRUCT,
                "An ancient automaton from the first civilizations, capable of repairing itself and others.",
                "Repairs 5% of max HP each turn, and can repair other constructs."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "The Grand Archivist", BossType.HUMANOID,
                "Keeper of all knowledge from the Grand Library of Nil City, wielding forbidden magic.",
                "Can cast spells from all Eight Elements, adapting to player strategies."));

        SUPER_BOSSES.add(new SuperBossDefinition(
                "The Shattering Echo", BossType.ELDRITCH,
                "A remnant of the cataclysm that created the Shattering of the First Winter.",
                "Recreates fragments of the original Shattering, dealing massive area damage."));
    }

    /**
     * Boss definition for normal bosses.
     */
    public record BossDefinition(String name, BossType type, String lore) {
    }

    /**
     * Super boss definition.
     */
    public record SuperBossDefinition(String name, BossType type, String lore, String specialMechanic) {
    }
}

