package com.tatumgames.mikros.games.rpg.boss;

import com.tatumgames.mikros.games.rpg.model.Boss;
import com.tatumgames.mikros.games.rpg.model.BossType;
import com.tatumgames.mikros.games.rpg.model.SuperBoss;

import java.util.*;

/**
 * Catalog of all bosses in the RPG system.
 * Contains 24 normal bosses (2 per level for levels 1-12) and 12 super bosses.
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
     * @param level the boss level
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
     * @param level the super boss level
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
                new BossDefinition("Spirit Wisp Horror", BossType.SPIRIT, "A cluster of lost souls, bound together by bitterness and cold moonlight.")
        ));
        
        // Level 2 Bosses
        NORMAL_BOSSES.put(2, Arrays.asList(
                new BossDefinition("Bonegnasher Ghoul", BossType.UNDEAD, "It hunts in packs but commands them alone — a ghoul of unnatural intellect."),
                new BossDefinition("Frostfang Direwolf", BossType.BEAST, "A monstrous wolf whose howl freezes blood and courage alike.")
        ));
        
        // Level 3 Bosses
        NORMAL_BOSSES.put(3, Arrays.asList(
                new BossDefinition("Iceborne Sorcerer", BossType.HUMANOID, "A warlock whose soul fused with an ancient glacier."),
                new BossDefinition("Corpse Stitcher", BossType.UNDEAD, "A grotesque surgeon of death who creates abominations from fallen heroes.")
        ));
        
        // Level 4 Bosses
        NORMAL_BOSSES.put(4, Arrays.asList(
                new BossDefinition("Shadowblade Assassin", BossType.HUMANOID, "A killer whose blades thirst for notoriety in Nilfheim's darkness."),
                new BossDefinition("Void-Touched Servitor", BossType.ELDRITCH, "A servant of the deep void, speaking in fractal whispers.")
        ));
        
        // Level 5 Bosses
        NORMAL_BOSSES.put(5, Arrays.asList(
                new BossDefinition("Frost Titan", BossType.GIANT, "A towering mountain of ice and rage. Footsteps cause avalanches."),
                new BossDefinition("Eternal Frost Witch", BossType.HUMANOID, "Her heart froze a century ago—but her hatred burns steady.")
        ));
        
        // Level 6 Bosses
        NORMAL_BOSSES.put(6, Arrays.asList(
                new BossDefinition("Crypt Sovereign", BossType.UNDEAD, "Rules over ancient burial chambers beneath Nilfheim's tundras."),
                new BossDefinition("Stormborn Gryphon", BossType.BEAST, "A majestic predator forged from lightning storms.")
        ));
        
        // Level 7 Bosses
        NORMAL_BOSSES.put(7, Arrays.asList(
                new BossDefinition("Plague Herald", BossType.DEMON, "Bringer of pestilence, whisperer of corruption."),
                new BossDefinition("Shiverheart Basilisk", BossType.BEAST, "Its gaze freezes both flesh and courage.")
        ));
        
        // Level 8 Bosses
        NORMAL_BOSSES.put(8, Arrays.asList(
                new BossDefinition("Ironhide Juggernaut", BossType.CONSTRUCT, "An unstoppable machine of ancient origin, awakened by catastrophe."),
                new BossDefinition("Riftbreaker Harpy", BossType.BEAST, "A winged banshee whose scream can shatter sanity.")
        ));
        
        // Level 9 Bosses
        NORMAL_BOSSES.put(9, Arrays.asList(
                new BossDefinition("Frostwraith Matriarch", BossType.SPIRIT, "A powerful mother-wraith mourning her long-lost children."),
                new BossDefinition("Dreadhorn Minotaur", BossType.BEAST, "A labyrinth guardian forged from icy rage.")
        ));
        
        // Level 10 Bosses
        NORMAL_BOSSES.put(10, Arrays.asList(
                new BossDefinition("Polar Hydra", BossType.DRAGON, "Three snapping heads, one frozen heart."),
                new BossDefinition("Soulflayer Acolyte", BossType.HUMANOID, "Consumes the souls of heroes to feed its god.")
        ));
        
        // Level 11 Bosses
        NORMAL_BOSSES.put(11, Arrays.asList(
                new BossDefinition("Ashen Revenant", BossType.UNDEAD, "A resurrected hero twisted by hatred and regret."),
                new BossDefinition("Crystalbound Dragonspawn", BossType.DRAGON, "Fragments of an ancient dragon reforged by frozen magic.")
        ));
        
        // Level 12 Bosses
        NORMAL_BOSSES.put(12, Arrays.asList(
                new BossDefinition("The Rime Executioner", BossType.HUMANOID, "A sentient suit of armor possessed by an ice spirit."),
                new BossDefinition("Warden of the Shattered Gate", BossType.ELDRITCH, "Stands watch over a forgotten portal leading into nothingness.")
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
    }
    
    /**
     * Boss definition for normal bosses.
     */
    public static class BossDefinition {
        public final String name;
        public final BossType type;
        public final String lore;
        
        public BossDefinition(String name, BossType type, String lore) {
            this.name = name;
            this.type = type;
            this.lore = lore;
        }
    }
    
    /**
     * Super boss definition.
     */
    public static class SuperBossDefinition {
        public final String name;
        public final BossType type;
        public final String lore;
        public final String specialMechanic;
        
        public SuperBossDefinition(String name, BossType type, String lore, String specialMechanic) {
            this.name = name;
            this.type = type;
            this.lore = lore;
            this.specialMechanic = specialMechanic;
        }
    }
}

