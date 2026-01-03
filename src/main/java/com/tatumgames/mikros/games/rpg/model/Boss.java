package com.tatumgames.mikros.games.rpg.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a normal boss in the RPG system.
 * Community-wide boss battles with shared HP pool.
 */
public class Boss {
    private final String bossId;
    private final String name;
    private final BossType type;
    private final int level;
    private final int maxHp;
    private final int attack;
    private final boolean hasClassHarmonyMechanic;
    private final Instant spawnTime;
    private final Instant expiresAt;
    private int currentHp;
    private boolean defeated;

    /**
     * Creates a new Boss.
     *
     * @param bossId unique boss identifier
     * @param name   boss name
     * @param type   boss type (affects class bonuses)
     * @param level  boss level
     * @param maxHp  maximum HP (10,000 × level)
     * @param attack attack power
     */
    public Boss(String bossId, String name, BossType type, int level, int maxHp, int attack) {
        this(bossId, name, type, level, maxHp, attack, false);
    }

    /**
     * Creates a new Boss with class harmony mechanic flag.
     *
     * @param bossId unique boss identifier
     * @param name   boss name
     * @param type   boss type (affects class bonuses)
     * @param level  boss level
     * @param maxHp  maximum HP (10,000 × level)
     * @param attack attack power
     * @param hasClassHarmonyMechanic whether this boss uses the class harmony system
     */
    public Boss(String bossId, String name, BossType type, int level, int maxHp, int attack, boolean hasClassHarmonyMechanic) {
        this.bossId = Objects.requireNonNull(bossId);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.level = level;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.hasClassHarmonyMechanic = hasClassHarmonyMechanic;
        this.spawnTime = Instant.now();
        this.expiresAt = spawnTime.plusSeconds(24 * 3600); // 24 hours
        this.defeated = false;
    }

    /**
     * Applies damage to the boss.
     *
     * @param damage damage amount
     * @return true if boss is defeated
     */
    public boolean takeDamage(int damage) {
        this.currentHp = Math.max(0, this.currentHp - damage);
        if (this.currentHp <= 0) {
            this.defeated = true;
            return true;
        }
        return false;
    }

    /**
     * Checks if boss has expired (24 hours passed).
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    // Getters

    public String getBossId() {
        return bossId;
    }

    public String getName() {
        return name;
    }

    public BossType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getAttack() {
        return attack;
    }

    public Instant getSpawnTime() {
        return spawnTime;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    public boolean hasClassHarmonyMechanic() {
        return hasClassHarmonyMechanic;
    }
}

