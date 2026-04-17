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
import com.tatumgames.mikros.games.rpg.training.RiskyTrainingMethodType;
import com.tatumgames.mikros.games.rpg.training.TrainingAccidentType;
import com.tatumgames.mikros.games.rpg.training.TrainingFailureType;
import java.util.List;
import java.util.Random;

/**
 * Train action - player trains to improve stats and gain XP. Guarantees stat increase along with
 * XP.
 */
public class TrainAction implements CharacterAction {
  private static final Random random = new Random();
  private static final String[] STAT_NAMES = {"STR", "AGI", "INT", "LUCK"};
  private static final String[] STAT_DISPLAY_NAMES = {
    "Strength", "Agility", "Intelligence", "Luck"
  };
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
  public TrainAction(
      NilfheimEventService nilfheimEventService,
      LoreRecognitionService loreRecognitionService,
      WorldCurseService worldCurseService) {
    this.nilfheimEventService = nilfheimEventService;
    this.loreRecognitionService = loreRecognitionService;
    this.worldCurseService = worldCurseService;
  }

  /** Creates a new TrainAction without WorldCurseService (backward compatibility). */
  public TrainAction(
      NilfheimEventService nilfheimEventService, LoreRecognitionService loreRecognitionService) {
    this(nilfheimEventService, loreRecognitionService, null);
  }

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

  /**
   * Rolls for a training accident. Base chance: 8-12% (randomized), reduced by STR (-0.15% per STR)
   * and AGI (-0.15% per AGI), minimum 2%.
   *
   * @param character the character training
   * @return the accident type if triggered, null otherwise
   */
  private TrainingAccidentType rollForTrainingAccident(RPGCharacter character) {
    // Random base chance between 8% and 12%
    double baseChance = 0.08 + (random.nextDouble() * 0.04); // 8-12%
    int strength = character.getStats().getStrength();
    int agility = character.getStats().getAgility();

    // STR reduction: -0.15% per STR (max -3% at 20 STR)
    double strReduction = Math.min(0.03, strength * 0.0015);

    // AGI reduction: -0.15% per AGI (max -3% at 20 AGI)
    double agiReduction = Math.min(0.03, agility * 0.0015);

    // Final chance: base - reductions, minimum 2%
    double finalChance = Math.max(0.02, baseChance - strReduction - agiReduction);

    if (random.nextDouble() < finalChance) {
      // Accident triggered - select tier (Tier 1: 70%, Tier 2: 25%, Tier 3: 5%)
      double tierRoll = random.nextDouble();
      if (tierRoll < 0.70) {
        return TrainingAccidentType.OVEREXERTION;
      } else if (tierRoll < 0.95) {
        return TrainingAccidentType.TRAINING_INJURY;
      } else {
        return TrainingAccidentType.MUSCLE_STRAIN;
      }
    }

    return null; // No accident
  }

  /**
   * Rolls for a training failure. Base chance: 5-8% (randomized), reduced by INT (-0.1% per INT)
   * and LUCK (-0.1% per LUCK), minimum 1%.
   *
   * @param character the character training
   * @return the failure type if triggered, null otherwise
   */
  private TrainingFailureType rollForTrainingFailure(RPGCharacter character) {
    // Random base chance between 5% and 8%
    double baseChance = 0.05 + (random.nextDouble() * 0.03); // 5-8%
    int intelligence = character.getStats().getIntelligence();
    int luck = character.getStats().getLuck();

    // INT reduction: -0.1% per INT (max -2% at 20 INT)
    double intReduction = Math.min(0.02, intelligence * 0.001);

    // LUCK reduction: -0.1% per LUCK (max -2% at 20 LUCK)
    double luckReduction = Math.min(0.02, luck * 0.001);

    // Final chance: base - reductions, minimum 1%
    double finalChance = Math.max(0.01, baseChance - intReduction - luckReduction);

    if (random.nextDouble() < finalChance) {
      // Failure triggered - select tier (Tier 1: 70%, Tier 2: 25%, Tier 3: 5%)
      double tierRoll = random.nextDouble();
      if (tierRoll < 0.70) {
        return TrainingFailureType.POOR_FORM;
      } else if (tierRoll < 0.95) {
        return TrainingFailureType.EXHAUSTION;
      } else {
        return TrainingFailureType.TRAINING_SETBACK;
      }
    }

    return null; // No failure
  }

  /**
   * Rolls for risky training method. Base chance: 10% for high-risk, high-reward option.
   *
   * @param character the character training
   * @return the risky training method type if triggered, null otherwise
   */
  private RiskyTrainingMethodType rollForRiskyTraining(RPGCharacter character) {
    double baseChance = 0.10; // 10%

    if (random.nextDouble() < baseChance) {
      // Risky training triggered - select type (equal chance for each)
      RiskyTrainingMethodType[] riskyMethods = RiskyTrainingMethodType.values();
      return riskyMethods[random.nextInt(riskyMethods.length)];
    }

    return null; // No risky training
  }

  /**
   * Handles a training accident and returns the outcome details.
   *
   * @param accidentType the type of accident
   * @param character the character affected
   * @return array containing [damageTaken, narrative]
   */
  private Object[] handleTrainingAccident(
      TrainingAccidentType accidentType, RPGCharacter character) {
    int damageTaken = 0;
    String narrative = "";

    switch (accidentType) {
      case OVEREXERTION -> {
        // 3-7% HP loss (cannot kill)
        int maxHp = character.getStats().getMaxHp();
        int hpLoss = (int) (maxHp * (0.03 + random.nextDouble() * 0.04)); // 3-7%
        int currentHp = character.getStats().getCurrentHp();
        damageTaken = Math.min(hpLoss, currentHp - 1); // Ensure at least 1 HP remains
        character.getStats().takeDamage(damageTaken);
        narrative =
            "💥 **Overexertion:** You pushed yourself too hard during training, taking "
                + damageTaken
                + " damage. At least you're still alive!";
      }

      case TRAINING_INJURY -> {
        // Set flag to lose charge on next action (3% chance when this accident triggers, rare)
        if (random.nextDouble() < 0.03) {
          character.setLoseChargeOnNextAction(true);
          narrative =
              "🩹 **Training Injury:** You've injured yourself during training. You'll lose an extra action charge on your next action.";
        } else {
          narrative =
              "🩹 **Training Injury:** You've sustained a minor injury, but it doesn't affect your training.";
        }
      }

      case MUSCLE_STRAIN -> {
        // Apply temporary -1 to trained stat for 1 action (2% chance when this accident triggers,
        // very rare)
        if (random.nextDouble() < 0.02) {
          // This will be handled in execute() after we know which stat was trained
          narrative =
              "⚡ **Muscle Strain:** You've strained a muscle. Your next action will be affected.";
        } else {
          narrative =
              "⚡ **Muscle Strain:** You feel a slight strain, but it doesn't affect your training.";
        }
      }
    }

    return new Object[] {damageTaken, narrative};
  }

  /**
   * Handles a training failure and returns the outcome details.
   *
   * @param failureType the type of failure
   * @param statIncrease the original stat increase amount
   * @param character the character affected
   * @return array containing [statIncrease, xpReduction, narrative]
   */
  private Object[] handleTrainingFailure(
      TrainingFailureType failureType, int statIncrease, RPGCharacter character) {
    int finalStatIncrease = statIncrease;
    double xpReduction = 0.0;
    String narrative = "";

    switch (failureType) {
      case POOR_FORM -> {
        // No stat increase this action, XP reduced by 25%
        finalStatIncrease = 0;
        xpReduction = 0.25;
        narrative =
            "❌ **Poor Form:** Your training form was poor this session. You don't gain any stat points, and your XP gain is reduced by 25%.";
      }

      case EXHAUSTION -> {
        // Set flag for next action to cost double (2% chance when this failure triggers, rare)
        if (random.nextDouble() < 0.02) {
          character.setNextActionCostsDouble(true);
          narrative =
              "😴 **Exhaustion:** You're exhausted from training. Your next action will cost 2 charges instead of 1.";
        } else {
          narrative =
              "😴 **Exhaustion:** You feel exhausted, but manage to complete your training.";
        }
      }

      case TRAINING_SETBACK -> {
        // Stat increase reduced by 1 (minimum 1 point)
        finalStatIncrease = Math.max(1, statIncrease - 1);
        narrative =
            "📉 **Training Setback:** You experienced a setback during training. Your stat increase is reduced by 1 point.";
      }
    }

    return new Object[] {finalStatIncrease, xpReduction, narrative};
  }

  /**
   * Handles risky training and returns the outcome details.
   *
   * @param riskyMethod the type of risky training
   * @param statIncrease the original stat increase amount
   * @param baseXp the base XP before bonuses
   * @param character the character affected
   * @return array containing [statIncrease, xpMultiplier, damageTaken, narrative]
   */
  private Object[] handleRiskyTraining(
      RiskyTrainingMethodType riskyMethod, int statIncrease, int baseXp, RPGCharacter character) {
    int finalStatIncrease = statIncrease;
    double xpMultiplier = riskyMethod.getXpMultiplier();
    int damageTaken = 0;
    String narrative = "";

    // Apply stat bonus multiplier
    int statBonus = (int) (riskyMethod.getStatBonusMultiplier() * statIncrease);
    finalStatIncrease += statBonus;

    // Calculate HP loss
    int maxHp = character.getStats().getMaxHp();
    double hpLossPercent =
        riskyMethod.getMinHpLossPercent()
            + (random.nextDouble()
                * (riskyMethod.getMaxHpLossPercent() - riskyMethod.getMinHpLossPercent()));
    int hpLoss = (int) (maxHp * hpLossPercent);
    int currentHp = character.getStats().getCurrentHp();
    damageTaken = Math.min(hpLoss, currentHp - 1); // Ensure at least 1 HP remains
    character.getStats().takeDamage(damageTaken);

    switch (riskyMethod) {
      case PUSH_BEYOND_LIMITS -> {
        narrative =
            String.format(
                "🔥 **Push Beyond Limits:** You push yourself beyond your limits! You gain +%d extra stat point%s and %.0f%% bonus XP, but take %d damage.",
                statBonus, statBonus > 1 ? "s" : "", xpMultiplier * 100, damageTaken);

        // 10% chance to lose 1 action charge (for DANGEROUS_TECHNIQUE, but we'll handle it here for
        // consistency)
      }

      case DANGEROUS_TECHNIQUE -> {
        // 10% chance to lose 1 action charge
        if (random.nextDouble() < 0.10) {
          character.setLoseChargeOnNextAction(true);
          narrative =
              String.format(
                  "⚔️ **Dangerous Technique:** You attempt a dangerous training technique! You gain +%d extra stat point%s and %.0f%% bonus XP, but take %d damage and lose an extra charge on your next action.",
                  statBonus, statBonus > 1 ? "s" : "", xpMultiplier * 100, damageTaken);
        } else {
          narrative =
              String.format(
                  "⚔️ **Dangerous Technique:** You attempt a dangerous training technique! You gain +%d extra stat point%s and %.0f%% bonus XP, but take %d damage.",
                  statBonus, statBonus > 1 ? "s" : "", xpMultiplier * 100, damageTaken);
        }
      }

      case EXTREME_TRAINING -> {
        // 5% chance for temporary stat debuff
        if (random.nextDouble() < 0.05) {
          // This will be handled in execute() after we know which stat was trained
          narrative =
              String.format(
                  "💀 **Extreme Training:** You push yourself to the absolute limit! You gain +%d extra stat point%s and %.0f%% bonus XP, but take %d damage and suffer a temporary stat debuff.",
                  statBonus, statBonus > 1 ? "s" : "", xpMultiplier * 100, damageTaken);
        } else {
          narrative =
              String.format(
                  "💀 **Extreme Training:** You push yourself to the absolute limit! You gain +%d extra stat point%s and %.0f%% bonus XP, but take %d damage.",
                  statBonus, statBonus > 1 ? "s" : "", xpMultiplier * 100, damageTaken);
        }
      }
    }

    return new Object[] {finalStatIncrease, xpMultiplier, damageTaken, narrative};
  }

  @Override
  public RPGActionOutcome execute(RPGCharacter character, RPGConfig config) {
    String guildId = config.getGuildId();
    NilfheimEventType activeEvent = nilfheimEventService.getActiveEvent(guildId);

    // Check for temporary stat debuff and decrement/clear if needed
    character.decrementTemporaryStatDebuffActions();

    // Check for risky training method (10% chance, high-risk, high-reward)
    RiskyTrainingMethodType riskyMethod = rollForRiskyTraining(character);
    boolean isRiskyTraining = riskyMethod != null;

    // Select random stat to increase
    int statIndex = random.nextInt(STAT_NAMES.length);
    String statName = STAT_NAMES[statIndex];
    String statDisplayName = STAT_DISPLAY_NAMES[statIndex];

    // Track stat imbalance (check if training same stat 3+ times in a row)
    boolean statImbalancePenalty = false;
    double xpImbalanceReduction = 0.0;
    if (character.getLastTrainedStat() != null && character.getLastTrainedStat().equals(statName)) {
      character.setConsecutiveSameStatTraining(character.getConsecutiveSameStatTraining() + 1);
      if (character.getConsecutiveSameStatTraining() >= 3) {
        // 15% chance for -1 to opposite stat
        if (random.nextDouble() < 0.15) {
          // Determine opposite stat
          String oppositeStat = null;
          switch (statName) {
            case "STR" -> oppositeStat = "AGI";
            case "AGI" -> oppositeStat = "STR";
            case "INT" -> oppositeStat = "LUCK";
            case "LUCK" -> oppositeStat = "INT";
          }
          if (oppositeStat != null) {
            character.getStats().increaseStat(oppositeStat, -1);
            statImbalancePenalty = true;
          }
        }
        // 10-20% XP reduction until different stat is trained
        xpImbalanceReduction = 0.10 + (random.nextDouble() * 0.10); // 10-20%
      }
    } else {
      character.setConsecutiveSameStatTraining(1);
    }
    character.setLastTrainedStat(statName);

    // Calculate stat increase (1-3 points)
    int statIncrease = 1 + random.nextInt(3);

    // Apply Nilfheim event effects
    if (activeEvent != null) {
      if (activeEvent.getEffectType() == NilfheimEventType.EventEffectType.TRAIN_STAT_BOOST) {
        // Grand Library Opens: +1 guaranteed stat point
        statIncrease += (int) activeEvent.getEffectValue();
      }
      if (activeEvent.getEffectType() == NilfheimEventType.EventEffectType.ALL_XP_BOOST) {
        // Starfall Ridge's Light: +15% XP on all actions (applied below)
      }
    }

    // Check for training accident (8-12% base chance, reduced by STR/AGI)
    TrainingAccidentType accident = null;
    int accidentDamage = 0;
    String accidentNarrative = "";
    if (!isRiskyTraining) {
      accident = rollForTrainingAccident(character);
      if (accident != null) {
        Object[] accidentResult = handleTrainingAccident(accident, character);
        accidentDamage = (int) accidentResult[0];
        accidentNarrative = (String) accidentResult[1];

        // Handle MUSCLE_STRAIN temporary debuff
        if (accident == TrainingAccidentType.MUSCLE_STRAIN && random.nextDouble() < 0.02) {
          character.setTemporaryStatDebuffStat(statName);
          character.setTemporaryStatDebuffActionsRemaining(1);
          character.setTemporaryStatDebuffAmount(1);
        }
      }
    }

    // Check for training failure (5-8% base chance, reduced by INT/LUCK)
    TrainingFailureType failure = null;
    double failureXpReduction = 0.0;
    String failureNarrative = "";
    if (!isRiskyTraining) {
      failure = rollForTrainingFailure(character);
      if (failure != null) {
        Object[] failureResult = handleTrainingFailure(failure, statIncrease, character);
        statIncrease = (int) failureResult[0];
        failureXpReduction = (double) failureResult[1];
        failureNarrative = (String) failureResult[2];
      }
    }

    // Handle risky training
    double riskyXpMultiplier = 1.0;
    int riskyDamage = 0;
    String riskyNarrative = "";
    if (isRiskyTraining) {
      int baseXp = 30 + (character.getLevel() * 5);
      Object[] riskyResult = handleRiskyTraining(riskyMethod, statIncrease, baseXp, character);
      statIncrease = (int) riskyResult[0];
      riskyXpMultiplier = (double) riskyResult[1];
      riskyDamage = (int) riskyResult[2];
      riskyNarrative = (String) riskyResult[3];

      // Handle EXTREME_TRAINING temporary stat debuff
      if (riskyMethod == RiskyTrainingMethodType.EXTREME_TRAINING && random.nextDouble() < 0.05) {
        character.setTemporaryStatDebuffStat(statName);
        character.setTemporaryStatDebuffActionsRemaining(3);
        character.setTemporaryStatDebuffAmount(1);
      }
    }

    // Apply stat increase (only if not prevented by failure)
    if (statIncrease > 0) {
      character.getStats().increaseStat(statName, statIncrease);
    }

    // Calculate XP gain (increased base and variance)
    int baseXp = 30 + (character.getLevel() * 5);
    int variance = random.nextInt(21) - 10; // ±10
    int xpGained = (int) ((baseXp + variance) * config.getXpMultiplier());

    // Apply level scaling bonus: +2% XP per level above 5, capped at +30% at level 20
    int level = character.getLevel();
    if (level > 5) {
      double levelScaling = Math.min(0.30, (level - 5) * 0.02); // +2% per level above 5, max +30%
      xpGained = (int) (xpGained * (1.0 + levelScaling));
    }

    // Apply stat imbalance XP reduction
    if (xpImbalanceReduction > 0) {
      xpGained = (int) (xpGained * (1.0 - xpImbalanceReduction));
    }

    // Apply training failure XP reduction
    if (failureXpReduction > 0) {
      xpGained = (int) (xpGained * (1.0 - failureXpReduction));
    }

    // Apply risky training XP multiplier
    if (isRiskyTraining) {
      xpGained = (int) (xpGained * (1.0 + riskyXpMultiplier));
    }

    // Apply Nilfheim event effects for XP
    if (activeEvent != null
        && activeEvent.getEffectType() == NilfheimEventType.EventEffectType.ALL_XP_BOOST) {
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
      default -> narrativePrefix = "You train diligently";
    }

    String narrative;
    if (statIncrease > 0) {
      narrative =
          String.format(
              "%s, you improved your %s by %d point%s!",
              narrativePrefix, statDisplayName, statIncrease, statIncrease > 1 ? "s" : "");
    } else {
      narrative =
          String.format("%s, but your training was ineffective this time.", narrativePrefix);
    }

    // Add risk narratives
    if (isRiskyTraining && !riskyNarrative.isEmpty()) {
      narrative += "\n\n" + riskyNarrative;
    }
    if (accident != null && !accidentNarrative.isEmpty()) {
      narrative += "\n\n" + accidentNarrative;
    }
    if (failure != null && !failureNarrative.isEmpty()) {
      narrative += "\n\n" + failureNarrative;
    }
    if (statImbalancePenalty) {
      narrative +=
          "\n\n⚠️ **Stat Imbalance:** Training the same stat repeatedly has caused an imbalance. One of your other stats has been reduced.";
    }
    if (xpImbalanceReduction > 0 && !statImbalancePenalty) {
      narrative +=
          String.format(
              "\n\n⚠️ **Stat Imbalance:** Training the same stat repeatedly reduces your XP gain by %.0f%%. Train a different stat to restore full XP gain.",
              xpImbalanceReduction * 100);
    }

    // Oathbreaker: Gain corruption from acting during world curses
    if (character.getCharacterClass()
            == com.tatumgames.mikros.games.rpg.model.CharacterClass.OATHBREAKER
        && worldCurseService != null) {
      List<WorldCurse> activeCurses = worldCurseService.getActiveCurses(guildId);
      if (!activeCurses.isEmpty()) {
        character.addCorruption(1);
        narrative +=
            "\n\n⚔️💀 **Corruption:** The world's curses resonate with your broken oath, increasing your corruption.";
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

    // Calculate total damage taken
    int totalDamage = accidentDamage + riskyDamage;

    return RPGActionOutcome.builder()
        .narrative(narrative)
        .xpGained(xpGained)
        .leveledUp(leveledUp)
        .statIncreased(statIncrease > 0 ? statDisplayName : null, statIncrease)
        .damageTaken(totalDamage)
        .hpRestored(0)
        .success(true)
        .build();
  }
}
