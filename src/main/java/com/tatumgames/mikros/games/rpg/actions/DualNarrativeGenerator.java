package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.model.CharacterClass;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;

/**
 * Generates narrative descriptions for dual outcomes based on class matchups.
 */
public class DualNarrativeGenerator {

    /**
     * Generates a narrative for a dual outcome.
     *
     * @param challenger the challenging character
     * @param target     the target character
     * @param challengerWins whether the challenger won
     * @return narrative description
     */
    public String generateNarrative(RPGCharacter challenger, RPGCharacter target, boolean challengerWins) {
        CharacterClass challengerClass = challenger.getCharacterClass();
        CharacterClass targetClass = target.getCharacterClass();
        String challengerName = challenger.getName();
        String targetName = target.getName();

        if (challengerWins) {
            return generateVictoryNarrative(challengerClass, targetClass, challengerName, targetName);
        } else {
            return generateDefeatNarrative(challengerClass, targetClass, challengerName, targetName);
        }
    }

    private String generateVictoryNarrative(CharacterClass challengerClass, CharacterClass targetClass,
                                            String challengerName, String targetName) {
        return switch (challengerClass) {
            case WARRIOR -> switch (targetClass) {
                case MAGE, NECROMANCER, PRIEST -> String.format(
                        "**%s**'s brute strength overwhelmed **%s**'s magical defenses. " +
                                "In a clash of steel and sorcery, the Warrior's relentless assault proved victorious!",
                        challengerName, targetName);
                case ROGUE -> String.format(
                        "**%s**'s heavy strikes found their mark despite **%s**'s agility. " +
                                "The Warrior's raw power triumphed over the Rogue's speed!",
                        challengerName, targetName);
                case KNIGHT -> String.format(
                        "**%s** and **%s** engaged in an epic battle of tanks. " +
                                "After a grueling exchange, the Warrior's offensive might claimed victory!",
                        challengerName, targetName);
                default -> String.format(
                        "**%s**'s combat prowess proved superior, claiming victory over **%s**!",
                        challengerName, targetName);
            };
            case MAGE -> switch (targetClass) {
                case WARRIOR, KNIGHT -> String.format(
                        "**%s**'s arcane mastery kept **%s** at bay with devastating spells. " +
                                "The Mage's strategic magic overwhelmed the warrior's defenses!",
                        challengerName, targetName);
                case ROGUE -> String.format(
                        "**%s**'s magical barriers deflected **%s**'s swift attacks. " +
                                "The Mage's intelligence outmaneuvered the Rogue's agility!",
                        challengerName, targetName);
                case NECROMANCER, PRIEST -> String.format(
                        "**%s** and **%s** engaged in a battle of wits and magic. " +
                                "The Mage's pure arcane power emerged victorious!",
                        challengerName, targetName);
                default -> String.format(
                        "**%s**'s spellcasting prowess proved superior, claiming victory over **%s**!",
                        challengerName, targetName);
            };
            case ROGUE -> switch (targetClass) {
                case WARRIOR, KNIGHT -> String.format(
                        "**%s**'s agility allowed them to dodge **%s**'s heavy strikes. " +
                                "After a series of precise counter-attacks, the Rogue's speed triumphed!",
                        challengerName, targetName);
                case MAGE, NECROMANCER, PRIEST -> String.format(
                        "**%s** closed the distance before **%s** could cast. " +
                                "The Rogue's critical strikes overwhelmed the caster!",
                        challengerName, targetName);
                default -> String.format(
                        "**%s**'s agility and precision proved superior, claiming victory over **%s**!",
                        challengerName, targetName);
            };
            case KNIGHT -> switch (targetClass) {
                case WARRIOR -> String.format(
                        "**%s**'s superior defense weathered **%s**'s onslaught. " +
                                "The Knight's resilience and tactical skill claimed victory!",
                        challengerName, targetName);
                case MAGE, NECROMANCER, PRIEST -> String.format(
                        "**%s**'s heavy armor deflected **%s**'s spells. " +
                                "The Knight's defensive mastery triumphed over magic!",
                        challengerName, targetName);
                case ROGUE -> String.format(
                        "**%s**'s shield blocked **%s**'s swift attacks. " +
                                "The Knight's defense proved too strong for the Rogue's speed!",
                        challengerName, targetName);
                default -> String.format(
                        "**%s**'s defensive prowess proved superior, claiming victory over **%s**!",
                        challengerName, targetName);
            };
            case NECROMANCER -> switch (targetClass) {
                case WARRIOR, KNIGHT -> String.format(
                        "**%s**'s dark magic and decay effects wore down **%s**'s defenses. " +
                                "The Necromancer's sinister power claimed victory!",
                        challengerName, targetName);
                case MAGE, PRIEST -> String.format(
                        "**%s**'s necromantic arts clashed with **%s**'s magic. " +
                                "The Necromancer's dark power and luck triumphed!",
                        challengerName, targetName);
                case ROGUE -> String.format(
                        "**%s**'s magical barriers and decay countered **%s**'s speed. " +
                                "The Necromancer's hybrid power claimed victory!",
                        challengerName, targetName);
                default -> String.format(
                        "**%s**'s dark magic proved superior, claiming victory over **%s**!",
                        challengerName, targetName);
            };
            case PRIEST -> switch (targetClass) {
                case WARRIOR, KNIGHT -> String.format(
                        "**%s**'s holy magic and defensive spells outlasted **%s**'s attacks. " +
                                "The Priest's supportive power and resilience triumphed!",
                        challengerName, targetName);
                case MAGE, NECROMANCER -> String.format(
                        "**%s**'s divine magic clashed with **%s**'s arcane arts. " +
                                "The Priest's holy power emerged victorious!",
                        challengerName, targetName);
                case ROGUE -> String.format(
                        "**%s**'s protective barriers deflected **%s**'s swift strikes. " +
                                "The Priest's defensive magic triumphed over speed!",
                        challengerName, targetName);
                default -> String.format(
                        "**%s**'s holy power proved superior, claiming victory over **%s**!",
                        challengerName, targetName);
            };
        };
    }

    private String generateDefeatNarrative(CharacterClass challengerClass, CharacterClass targetClass,
                                            String challengerName, String targetName) {
        // Reverse the roles for defeat narrative
        return generateVictoryNarrative(targetClass, challengerClass, targetName, challengerName);
    }
}

