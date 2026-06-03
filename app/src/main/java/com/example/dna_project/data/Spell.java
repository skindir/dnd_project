package com.example.dna_project.data;

public class Spell {
    private final long id;
    private final String name;
    private final int level;
    private final String className;
    private final String range;
    private final String attackType;
    private final String damageType;
    private final String damage;

    public Spell(long id, String name, int level, String className, String range,
                 String attackType, String damageType, String damage) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.className = className;
        this.range = range;
        this.attackType = attackType;
        this.damageType = damageType;
        this.damage = damage;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public String getClassName() {
        return className;
    }

    public String getRange() {
        return range;
    }

    public String getAttackType() {
        return attackType;
    }

    public String getDamageType() {
        return damageType;
    }

    public String getDamage() {
        return damage;
    }
}
