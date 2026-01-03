package com.tatumgames.mikros.games.rpg.actions;

import com.tatumgames.mikros.games.rpg.config.RPGConfig;
import com.tatumgames.mikros.games.rpg.model.RPGActionOutcome;
import com.tatumgames.mikros.games.rpg.model.RPGCharacter;
import com.tatumgames.mikros.games.rpg.service.CharacterService;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Donate action - player donates an action charge to a random active player with fewer charges.
 * Level 10+ required. Only consumes charge if a recipient is found.
 */
public class DonateAction implements CharacterAction {
    private static final Random random = new Random();
    private static final long ACTIVE_THRESHOLD_HOURS = 24;

    private final CharacterService characterService;

    /**
     * Creates a new DonateAction.
     *
     * @param characterService the character service to access all characters
     */
    public DonateAction(CharacterService characterService) {
        this.characterService = characterService;
    }

    @Override
    public String getActionName() {
        return "donate";
    }

    @Override
    public String getActionEmoji() {
        return "💝";
    }

    @Override
    public String getDescription() {
        return "Donate an action charge to a random active player (Level 10+ required)";
    }

    @Override
    public RPGActionOutcome execute(RPGCharacter donor, RPGConfig config) {
        // Check level requirement (Level 10+)
        if (donor.getLevel() < 10) {
            return RPGActionOutcome.builder()
                    .narrative("You must be level 10 or higher to donate charges to others.")
                    .xpGained(0)
                    .leveledUp(false)
                    .hpRestored(0)
                    .success(false)
                    .build();
        }

        // Check if donor has charges
        if (donor.getActionCharges() <= 0) {
            return RPGActionOutcome.builder()
                    .narrative("You don't have any action charges to donate!")
                    .xpGained(0)
                    .leveledUp(false)
                    .hpRestored(0)
                    .success(false)
                    .build();
        }

        // Get all characters
        Collection<RPGCharacter> allCharacters = characterService.getAllCharacters();

        // Filter eligible recipients
        List<RPGCharacter> eligible = allCharacters.stream()
                .filter(c -> !c.getDiscordId().equals(donor.getDiscordId())) // Not the donor
                .filter(this::isActive) // Active within last 24 hours
                .filter(c -> canReceiveDonation(c, donor)) // Haven't received donation this cycle
                .filter(c -> !c.isDead() && !c.isRecovering()) // Not dead or recovering
                .sorted(Comparator.comparingInt(RPGCharacter::getActionCharges)) // Sort by charge count (ascending)
                .collect(Collectors.toList());

        // If no eligible recipients, don't consume charge
        if (eligible.isEmpty()) {
            return RPGActionOutcome.builder()
                    .narrative("You look around, but everyone seems well-rested. Your generosity will have to wait.")
                    .xpGained(0)
                    .leveledUp(false)
                    .hpRestored(0)
                    .success(true) // Success but no charge consumed
                    .build();
        }

        // Get bottom 50% (those with fewer charges)
        int bottomHalfStart = eligible.size() / 2;
        List<RPGCharacter> bottomHalf = eligible.subList(bottomHalfStart, eligible.size());

        // Randomly select from bottom half
        RPGCharacter recipient = bottomHalf.get(random.nextInt(bottomHalf.size()));

        // Perform donation
        donor.setActionCharges(donor.getActionCharges() - 1);
        recipient.addTemporaryCharge();
        recipient.setLastDonationReceived(Instant.now());
        donor.incrementChargesDonated();

        // Record the action (consumes charge)
        donor.recordAction();

        // Track action type for achievements
        donor.recordActionType("donate");

        String narrative = String.format(
                "You share your energy with a fellow adventurer. **%s** feels reinvigorated!",
                recipient.getName()
        );

        return RPGActionOutcome.builder()
                .narrative(narrative)
                .xpGained(0) // No XP for donation
                .leveledUp(false)
                .hpRestored(0)
                .success(true)
                .build();
    }

    /**
     * Checks if a character is active (has performed an action within the threshold).
     *
     * @param character the character to check
     * @return true if active
     */
    private boolean isActive(RPGCharacter character) {
        if (character.getLastActionTime() == null) {
            return false;
        }

        long hoursSinceAction = Duration.between(
                character.getLastActionTime(),
                Instant.now()
        ).toHours();

        return hoursSinceAction <= ACTIVE_THRESHOLD_HOURS;
    }

    /**
     * Checks if a character can receive a donation (hasn't received one this refresh cycle).
     *
     * @param recipient the potential recipient
     * @param donor     the donor
     * @return true if can receive donation
     */
    private boolean canReceiveDonation(RPGCharacter recipient, RPGCharacter donor) {
        Instant lastDonation = recipient.getLastDonationReceived();
        Instant donorRefresh = donor.getLastChargeRefreshTime();

        // If recipient never received donation, they can receive one
        if (lastDonation == null) {
            return true;
        }

        // If donor's refresh time is null, allow donation (edge case)
        if (donorRefresh == null) {
            return true;
        }

        // Recipient can receive donation if their last donation was before donor's last refresh
        return !lastDonation.isAfter(donorRefresh);
    }
}

