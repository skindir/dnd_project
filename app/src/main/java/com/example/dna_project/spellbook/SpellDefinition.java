package com.example.dna_project.spellbook;

// Vienas burvestibas modelis: dati no spell tabulas vai veca SPELL_LIBRARY saraksta.
public class SpellDefinition {
    public final int id;
    public final String name;
    public final int level;
    public final String classes;
    public final String description;

    public SpellDefinition(String name, int level, String classes, String description) {
        this(-1, name, level, classes, description);
    }

    public SpellDefinition(int id, String name, int level, String classes, String description) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.classes = classes;
        this.description = description;
    }
}
