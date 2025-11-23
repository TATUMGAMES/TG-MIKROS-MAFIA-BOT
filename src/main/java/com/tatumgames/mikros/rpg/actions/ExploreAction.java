package com.tatumgames.mikros.rpg.actions;

import com.tatumgames.mikros.rpg.config.RPGConfig;
import com.tatumgames.mikros.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.rpg.model.RPGCharacter;

import java.util.Random;

/**
 * Explore action - player explores the world and encounters random events.
 * Rewards XP and generates narrative encounters.
 */
public class ExploreAction implements CharacterAction {
    private static final Random random = new Random();
    
    private static final String[] NARRATIVES = {
            "You ventured into an ancient forest and discovered a hidden shrine. The spirits blessed your journey.",
            "While exploring a misty valley, you helped a lost traveler find their way home.",
            "You stumbled upon a mysterious cave filled with glowing crystals. Their energy invigorates you.",
            "In a bustling market square, you learned valuable techniques from a traveling merchant.",
            "You climbed a towering mountain and witnessed a breathtaking sunrise. Clarity washes over you.",
            "Deep in a dungeon, you found ancient texts that expanded your knowledge.",
            "You crossed paths with a friendly bard who shared tales of legendary heroes.",
            "While resting by a enchanted spring, you felt your potential grow stronger.",
            "You discovered ruins of an old civilization and learned from their forgotten wisdom.",
            "A chance encounter with a wise hermit taught you valuable life lessons.",
            "You found a peaceful grove where time seems to stand still. Meditation here brings growth.",
            "In the depths of a library, you uncovered secrets that broaden your understanding.",
            "You helped villagers defend against bandits, earning their gratitude and respect.",
            "While foraging in the wilderness, you found rare herbs that boost your vitality.",
            "You solved an ancient puzzle in a long-forgotten temple, unlocking hidden knowledge."
    };
    
    @Override
    public String getActionName() {
        return "explore";
    }
    
    @Override
    public String getActionEmoji() {
        return "🧭";
    }
    
    @Override
    public String getDescription() {
        return "Explore the world and encounter random events";
    }
    
    @Override
    public RPGActionOutcome execute(RPGCharacter character, RPGConfig config) {
        // Select random narrative
        String narrative = NARRATIVES[random.nextInt(NARRATIVES.length)];
        
        // Calculate XP gain (scales with level and config multiplier)
        int baseXp = 30 + (character.getLevel() * 5);
        int variance = random.nextInt(20) - 10; // +/- 10
        int xpGained = (int) ((baseXp + variance) * config.getXpMultiplier());
        
        // Add XP and check for level up
        boolean leveledUp = character.addXp(xpGained);
        
        // Record the action
        character.recordAction();
        
        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(xpGained)
                .leveledUp(leveledUp)
                .success(true)
                .build();
    }
}

