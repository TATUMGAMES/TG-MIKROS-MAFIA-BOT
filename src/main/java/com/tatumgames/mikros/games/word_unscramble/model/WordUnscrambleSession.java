package com.tatumgames.mikros.games.word_unscramble.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an active Word Unscramble game session for a specific hour.
 */
public class WordUnscrambleSession {
    private final String guildId;
    private final WordUnscrambleType gameType;
    private final Instant startTime;
    private final List<WordUnscrambleResult> results;
    private String correctAnswer;
    private boolean isActive;

    /**
     * Creates a new WordUnscrambleSession.
     *
     * @param guildId       the guild ID
     * @param gameType      the type of game
     * @param startTime     when the session started
     * @param correctAnswer the correct answer for this session (if applicable)
     */
    public WordUnscrambleSession(String guildId, WordUnscrambleType gameType, Instant startTime, String correctAnswer) {
        this.guildId = Objects.requireNonNull(guildId);
        this.gameType = Objects.requireNonNull(gameType);
        this.startTime = Objects.requireNonNull(startTime);
        this.correctAnswer = correctAnswer;
        this.results = new ArrayList<>();
        this.isActive = true;
    }

    public String getGuildId() {
        return guildId;
    }

    public WordUnscrambleType getGameType() {
        return gameType;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<WordUnscrambleResult> getResults() {
        return new ArrayList<>(results);
    }

    public void addResult(WordUnscrambleResult result) {
        results.add(result);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Gets the winner (first correct result).
     *
     * @return the winning result, or null if none
     */
    public WordUnscrambleResult getWinner() {
        return results.stream()
                .filter(WordUnscrambleResult::isCorrect)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the top scorer (highest score result).
     *
     * @return the top scoring result, or null if none
     */
    public WordUnscrambleResult getTopScorer() {
        return results.stream()
                .max((r1, r2) -> Integer.compare(r1.score(), r2.score()))
                .orElse(null);
    }
}


