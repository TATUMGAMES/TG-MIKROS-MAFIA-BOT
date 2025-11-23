package com.tatumgames.mikros.rpg.actions;

import com.tatumgames.mikros.rpg.config.RPGConfig;
import com.tatumgames.mikros.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.rpg.model.RPGCharacter;

import java.util.Random;

/**
 * Train action - player trains to improve stats and gain XP.
 * Guarantees stat increase along with XP.
 */
public class TrainAction implements CharacterAction {
    private static final Random random = new Random();
    
    private static final String[] STAT_NAMES = {"STR", "AGI", "INT", "LUCK"};
    private static final String[] STAT_DISPLAY_NAMES = {"Strength", "Agility", "Intelligence", "Luck"};
    
    private static final String[] NARRATIVES_PREFIX = {
            "You spent hours in rigorous training",
            "Under the guidance of a master",
            "Through intense discipline",
            "By pushing your limits",
            "With unwavering dedication",
            "After countless repetitions",
            "Through focused meditation",
            "By testing yourself against challenges"
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
        String narrativePrefix = NARRATIVES_PREFIX[random.nextInt(NARRATIVES_PREFIX.length)];
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
                .success(true)
                .build();
    }
}

