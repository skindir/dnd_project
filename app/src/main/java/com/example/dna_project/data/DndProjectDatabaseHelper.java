package com.example.dna_project.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DndProjectDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "dnd_project.db";
    private static final int DATABASE_VERSION = 1;

    public DndProjectDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSchema(db);
        seedReferenceData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createSchema(db);
        seedReferenceData(db);
    }

    public void ensureReady() {
        SQLiteDatabase db = getWritableDatabase();
        createSchema(db);
        seedReferenceData(db);
    }

    public List<DbOption> getOptions(String tableName) {
        ensureReady();
        List<DbOption> options = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT id, name FROM " + tableName + " ORDER BY id", null)) {
            while (cursor.moveToNext()) {
                options.add(new DbOption(cursor.getInt(0), cursor.getString(1)));
            }
        }
        return options;
    }

    public List<ClassOption> getClasses() {
        ensureReady();
        List<ClassOption> classes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT id, name, hit_dice FROM class ORDER BY id", null)) {
            while (cursor.moveToNext()) {
                classes.add(new ClassOption(cursor.getInt(0), cursor.getString(1), cursor.getInt(2)));
            }
        }
        return classes;
    }

    public List<DbOption> getCharacterDetailOptions(String optionType) {
        ensureReady();
        List<DbOption> options = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT id, value FROM character_detail_option WHERE option_type = ? ORDER BY id",
                new String[]{optionType}
        )) {
            while (cursor.moveToNext()) {
                options.add(new DbOption(cursor.getInt(0), cursor.getString(1)));
            }
        }
        return options;
    }

    public ClassBaseStats getClassBaseStats(int classId) {
        ensureReady();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT strength, constitution, dexterity, intelligence, wisdom, charisma " +
                        "FROM class_base_stats WHERE class_id = ?",
                new String[]{String.valueOf(classId)}
        )) {
            if (cursor.moveToFirst()) {
                return new ClassBaseStats(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getInt(5)
                );
            }
        }
        return new ClassBaseStats(0, 0, 0, 0, 0, 0);
    }

    public long createCharacter(@NonNull CharacterCreateData data) {
        ensureReady();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long equipmentId = db.insertOrThrow("equipment", "head_id", new ContentValues());
            long bagId = db.insertOrThrow("bag", "id", new ContentValues());

            ContentValues inventoryValues = new ContentValues();
            inventoryValues.put("platinum", 0);
            inventoryValues.put("gold", 0);
            inventoryValues.put("silver", 0);
            inventoryValues.put("bronze", 0);
            inventoryValues.put("equipment_id", equipmentId);
            inventoryValues.put("bag_id", bagId);
            long inventoryId = db.insertOrThrow("inventory", null, inventoryValues);

            ContentValues statsValues = new ContentValues();
            statsValues.put("strength", data.strength);
            statsValues.put("constitution", data.constitution);
            statsValues.put("dexterity", data.dexterity);
            statsValues.put("intelligence", data.intelligence);
            statsValues.put("wisdom", data.wisdom);
            statsValues.put("charisma", data.charisma);
            long statsId = db.insertOrThrow("stats", null, statsValues);

            ContentValues characterValues = new ContentValues();
            characterValues.put("name", data.name);
            characterValues.put("level", data.level);
            characterValues.put("inventory_id", inventoryId);
            characterValues.put("class_id", data.classId);
            putNullableId(characterValues, "race_id", data.raceId);
            putNullableId(characterValues, "background_id", data.backgroundId);
            characterValues.put("base_stats_id", statsId);
            characterValues.put("alignment", data.alignment);
            characterValues.put("personality_traits", data.personalityTraits);
            characterValues.put("ideals", data.ideals);
            characterValues.put("bonds", data.bonds);
            characterValues.put("flaws", data.flaws);
            long characterId = db.insertOrThrow("characters", null, characterValues);

            insertCharacterLanguages(db, characterId, data.languageIds);
            insertCharacterProficiencies(db, characterId, data.classId, data.proficiencyIds);

            db.setTransactionSuccessful();
            return characterId;
        } finally {
            db.endTransaction();
        }
    }

    private static void insertCharacterLanguages(SQLiteDatabase db, long characterId, List<Integer> languageIds) {
        for (Integer languageId : languageIds) {
            ContentValues values = new ContentValues();
            values.put("language_id", languageId);
            values.put("character_id", characterId);
            db.insertWithOnConflict("language_char", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private static void insertCharacterProficiencies(
            SQLiteDatabase db,
            long characterId,
            int classId,
            List<Integer> selectedProficiencyIds
    ) {
        Set<Integer> proficiencyIds = new LinkedHashSet<>(selectedProficiencyIds);
        try (Cursor cursor = db.rawQuery(
                "SELECT proficiency_id FROM class_proficiency WHERE class_id = ?",
                new String[]{String.valueOf(classId)}
        )) {
            while (cursor.moveToNext()) {
                proficiencyIds.add(cursor.getInt(0));
            }
        }

        for (Integer proficiencyId : proficiencyIds) {
            ContentValues values = new ContentValues();
            values.put("proficiency_id", proficiencyId);
            values.put("character_id", characterId);
            db.insertWithOnConflict("prof_char", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private static void putNullableId(ContentValues values, String key, int id) {
        if (id > 0) {
            values.put(key, id);
        } else {
            values.putNull(key);
        }
    }

    private static void createSchema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS race (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE, description TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS background (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE, description TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS trait (id INTEGER PRIMARY KEY, name TEXT NOT NULL, description TEXT, uses INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS spell (id INTEGER PRIMARY KEY, name TEXT NOT NULL, level INTEGER, description TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS language (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS proficiency (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS item (id INTEGER PRIMARY KEY, name TEXT NOT NULL, item_type TEXT, description TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS bag (id INTEGER PRIMARY KEY)");
        db.execSQL("CREATE TABLE IF NOT EXISTS stats (id INTEGER PRIMARY KEY, strength INTEGER, constitution INTEGER, dexterity INTEGER, intelligence INTEGER, wisdom INTEGER, charisma INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS equipment (id INTEGER PRIMARY KEY, head_id INTEGER REFERENCES item(id), earring_r_id INTEGER REFERENCES item(id), earring_l_id INTEGER REFERENCES item(id), neck_id INTEGER REFERENCES item(id), pauldron_r_id INTEGER REFERENCES item(id), pauldron_l_id INTEGER REFERENCES item(id), glove_r_id INTEGER REFERENCES item(id), glove_l_id INTEGER REFERENCES item(id), ring_r_id INTEGER REFERENCES item(id), ring_l_id INTEGER REFERENCES item(id), chest_id INTEGER REFERENCES item(id), belt_id INTEGER REFERENCES item(id), pants_id INTEGER REFERENCES item(id), boots_id INTEGER REFERENCES item(id), main_hand_id INTEGER REFERENCES item(id), off_hand_id INTEGER REFERENCES item(id))");
        db.execSQL("CREATE TABLE IF NOT EXISTS inventory (id INTEGER PRIMARY KEY, platinum INTEGER, gold INTEGER, silver INTEGER, bronze INTEGER, equipment_id INTEGER REFERENCES equipment(id), bag_id INTEGER REFERENCES bag(id))");
        db.execSQL("CREATE TABLE IF NOT EXISTS class (id INTEGER PRIMARY KEY, caster_type TEXT, name TEXT NOT NULL UNIQUE, hit_dice INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS characters (id INTEGER PRIMARY KEY, name TEXT NOT NULL, level INTEGER, gender TEXT, inventory_id INTEGER REFERENCES inventory(id), class_id INTEGER REFERENCES class(id), race_id INTEGER REFERENCES race(id), background_id INTEGER REFERENCES background(id), base_stats_id INTEGER REFERENCES stats(id))");
        addColumnIfMissing(db, "characters", "alignment", "TEXT");
        addColumnIfMissing(db, "characters", "personality_traits", "TEXT");
        addColumnIfMissing(db, "characters", "ideals", "TEXT");
        addColumnIfMissing(db, "characters", "bonds", "TEXT");
        addColumnIfMissing(db, "characters", "flaws", "TEXT");
        db.execSQL("CREATE TABLE IF NOT EXISTS language_char (language_id INTEGER NOT NULL, character_id INTEGER NOT NULL, PRIMARY KEY (language_id, character_id), FOREIGN KEY (language_id) REFERENCES language(id) ON DELETE CASCADE, FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS prof_char (proficiency_id INTEGER NOT NULL, character_id INTEGER NOT NULL, PRIMARY KEY (proficiency_id, character_id), FOREIGN KEY (proficiency_id) REFERENCES proficiency(id) ON DELETE CASCADE, FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS bag_item (id INTEGER PRIMARY KEY, bag_id INTEGER NOT NULL, item_id INTEGER NOT NULL, quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity >= 0), FOREIGN KEY (bag_id) REFERENCES bag(id) ON DELETE CASCADE, FOREIGN KEY (item_id) REFERENCES item(id))");
        db.execSQL("CREATE TABLE IF NOT EXISTS background_trait (background_id INTEGER NOT NULL, trait_id INTEGER NOT NULL, level_req INTEGER, PRIMARY KEY (background_id, trait_id), FOREIGN KEY (background_id) REFERENCES background(id) ON DELETE CASCADE, FOREIGN KEY (trait_id) REFERENCES trait(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS race_trait (race_id INTEGER NOT NULL, trait_id INTEGER NOT NULL, level_req INTEGER, PRIMARY KEY (race_id, trait_id), FOREIGN KEY (race_id) REFERENCES race(id) ON DELETE CASCADE, FOREIGN KEY (trait_id) REFERENCES trait(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS class_trait (class_id INTEGER NOT NULL, trait_id INTEGER NOT NULL, level_req INTEGER, PRIMARY KEY (class_id, trait_id), FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE, FOREIGN KEY (trait_id) REFERENCES trait(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS multi_trait (trait_a_id INTEGER NOT NULL, trait_b_id INTEGER NOT NULL, PRIMARY KEY (trait_a_id, trait_b_id), CHECK (trait_a_id <> trait_b_id), FOREIGN KEY (trait_a_id) REFERENCES trait(id) ON DELETE CASCADE, FOREIGN KEY (trait_b_id) REFERENCES trait(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS trait_spell (trait_id INTEGER NOT NULL, spell_id INTEGER NOT NULL, level_req INTEGER, PRIMARY KEY (trait_id, spell_id), FOREIGN KEY (trait_id) REFERENCES trait(id) ON DELETE CASCADE, FOREIGN KEY (spell_id) REFERENCES spell(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS class_spells (class_id INTEGER NOT NULL, spell_id INTEGER NOT NULL, level_req INTEGER, PRIMARY KEY (class_id, spell_id), FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE, FOREIGN KEY (spell_id) REFERENCES spell(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS known_spell (class_id INTEGER NOT NULL, level INTEGER NOT NULL, cantrips INTEGER, other INTEGER, PRIMARY KEY (class_id, level), FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS spell_slots (caster_type TEXT NOT NULL, level INTEGER NOT NULL, level_1 INTEGER, level_2 INTEGER, level_3 INTEGER, level_4 INTEGER, level_5 INTEGER, level_6 INTEGER, level_7 INTEGER, level_8 INTEGER, level_9 INTEGER, PRIMARY KEY (caster_type, level))");
        db.execSQL("CREATE TABLE IF NOT EXISTS class_base_stats (class_id INTEGER PRIMARY KEY, strength INTEGER NOT NULL, constitution INTEGER NOT NULL, dexterity INTEGER NOT NULL, intelligence INTEGER NOT NULL, wisdom INTEGER NOT NULL, charisma INTEGER NOT NULL, FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS class_proficiency (class_id INTEGER NOT NULL, proficiency_id INTEGER NOT NULL, PRIMARY KEY (class_id, proficiency_id), FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE, FOREIGN KEY (proficiency_id) REFERENCES proficiency(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS character_detail_option (id INTEGER PRIMARY KEY, option_type TEXT NOT NULL, value TEXT NOT NULL, UNIQUE(option_type, value))");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS characters_class_required_insert BEFORE INSERT ON characters WHEN NEW.class_id IS NULL BEGIN SELECT RAISE(ABORT, 'characters.class_id is required'); END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS characters_class_required_update BEFORE UPDATE OF class_id ON characters WHEN NEW.class_id IS NULL BEGIN SELECT RAISE(ABORT, 'characters.class_id is required'); END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS characters_max_count_insert BEFORE INSERT ON characters WHEN (SELECT COUNT(*) FROM characters) >= 20 BEGIN SELECT RAISE(ABORT, 'maximum character count is 20'); END");
    }

    private static void addColumnIfMissing(SQLiteDatabase db, String table, String column, String type) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (cursor.moveToNext()) {
                if (column.equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                    return;
                }
            }
        }
        db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
    }

    private static void seedReferenceData(SQLiteDatabase db) {
        insertClasses(db);
        insertNamedRows(db, "race", new String[]{"Human", "Dwarf", "Elf", "Halfling", "Gnome", "Half-Elf", "Half-Orc", "Tiefling", "Dragonborn"});
        insertNamedRows(db, "background", new String[]{"Acolyte", "Entertainer", "Urchin", "Noble", "Guild Artisan", "Sailor", "Sage", "Folk Hero", "Hermit", "Criminal", "Retainer", "Soldier", "Outlander", "Charlatan"});
        insertNamedRows(db, "language", new String[]{"Abyssal", "Druidic", "Giant", "Infernal", "Thieves' Cant", "Celestial", "Undercommon", "Primordial", "Common", "Orc", "Elvish", "Gnomish", "Goblin", "Half-Elvish", "Halfling", "Sylvan", "Tiefling", "Aquan"});
        insertNamedRows(db, "proficiency", new String[]{"Saving Throw (Strength)", "Athletics", "Saving Throw (Dexterity)", "Acrobatics", "Sleight of Hand", "Stealth", "Saving Throw (Constitution)", "Saving Throw (Intelligence)", "Arcana", "History", "Investigation", "Nature", "Religion", "Saving Throw (Wisdom)", "Animal Handling", "Insight", "Medicine", "Perception", "Survival", "Saving Throw (Charisma)", "Deception", "Intimidation", "Performance", "Persuasion"});
        insertDetailOptions(db, "alignment", new String[]{"Lawful Good", "Neutral Good", "Chaotic Good", "Lawful Neutral", "True Neutral", "Chaotic Neutral", "Lawful Evil", "Neutral Evil", "Chaotic Evil"});
        insertDetailOptions(db, "personality_traits", new String[]{"I am always polite and respectful", "I trust my friends and protect them", "I am used to seeking profit in any situation", "I speak plainly, even when it is unpleasant", "I stay calm in the face of danger", "I love beautiful stories and glorious deeds", "I find it hard to trust strangers", "I am always seeking new knowledge"});
        insertDetailOptions(db, "ideals", new String[]{"Good", "Freedom", "Justice", "Honor", "Knowledge", "Power", "Tradition", "Redemption"});
        insertDetailOptions(db, "bonds", new String[]{"I protect my family", "I serve my people", "I owe my life to an old friend", "I seek a lost relic", "I must avenge the past", "I keep my mentor's secret", "I want to restore my lost honor", "I am bound by oath to an order"});
        insertDetailOptions(db, "flaws", new String[]{"I am too trusting", "I am greedy for gold", "I am hot-tempered", "I fear losing control", "I often underestimate danger", "I envy the fame of others", "I have trouble admitting mistakes", "I easily give in to temptation"});
        insertClassBaseStats(db);
        insertClassProficiencies(db);
    }

    private static void insertClasses(SQLiteDatabase db) {
        insertClass(db, 1, "Bard", "full", 8);
        insertClass(db, 2, "Barbarian", "none", 12);
        insertClass(db, 3, "Fighter", "none", 10);
        insertClass(db, 4, "Wizard", "full", 6);
        insertClass(db, 5, "Druid", "full", 8);
        insertClass(db, 6, "Cleric", "full", 8);
        insertClass(db, 7, "Artificer", "half", 8);
        insertClass(db, 8, "Warlock", "pact", 8);
        insertClass(db, 9, "Monk", "none", 8);
        insertClass(db, 10, "Paladin", "half", 10);
        insertClass(db, 11, "Rogue", "none", 8);
        insertClass(db, 12, "Ranger", "half", 10);
        insertClass(db, 13, "Sorcerer", "full", 6);
    }

    private static void insertClass(SQLiteDatabase db, int id, String name, String casterType, int hitDice) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("caster_type", casterType);
        values.put("hit_dice", hitDice);
        db.insertWithOnConflict("class", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private static void insertNamedRows(SQLiteDatabase db, String table, String[] names) {
        for (int index = 0; index < names.length; index++) {
            ContentValues values = new ContentValues();
            values.put("id", index + 1);
            values.put("name", names[index]);
            db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private static void insertDetailOptions(SQLiteDatabase db, String optionType, String[] values) {
        for (String value : values) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("option_type", optionType);
            contentValues.put("value", value);
            db.insertWithOnConflict("character_detail_option", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private static void insertClassBaseStats(SQLiteDatabase db) {
        insertClassBaseStats(db, 1, 10, 12, 14, 12, 13, 14);
        insertClassBaseStats(db, 2, 16, 16, 12, 8, 10, 13);
        insertClassBaseStats(db, 3, 16, 14, 14, 10, 10, 11);
        insertClassBaseStats(db, 4, 8, 14, 12, 16, 14, 11);
        insertClassBaseStats(db, 5, 8, 14, 12, 12, 16, 13);
        insertClassBaseStats(db, 6, 10, 14, 10, 12, 16, 13);
        insertClassBaseStats(db, 7, 10, 14, 12, 16, 12, 11);
        insertClassBaseStats(db, 8, 8, 14, 14, 12, 12, 15);
        insertClassBaseStats(db, 9, 12, 13, 16, 10, 16, 8);
        insertClassBaseStats(db, 10, 16, 14, 10, 8, 12, 15);
        insertClassBaseStats(db, 11, 10, 12, 16, 14, 10, 13);
        insertClassBaseStats(db, 12, 12, 14, 16, 10, 14, 9);
        insertClassBaseStats(db, 13, 8, 14, 12, 12, 13, 16);
    }

    private static void insertClassBaseStats(
            SQLiteDatabase db,
            int classId,
            int strength,
            int constitution,
            int dexterity,
            int intelligence,
            int wisdom,
            int charisma
    ) {
        ContentValues values = new ContentValues();
        values.put("class_id", classId);
        values.put("strength", strength);
        values.put("constitution", constitution);
        values.put("dexterity", dexterity);
        values.put("intelligence", intelligence);
        values.put("wisdom", wisdom);
        values.put("charisma", charisma);
        db.insertWithOnConflict("class_base_stats", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private static void insertClassProficiencies(SQLiteDatabase db) {
        insertClassProficiencies(db, 1, 4, 20);
        insertClassProficiencies(db, 2, 1, 7);
        insertClassProficiencies(db, 3, 1, 7);
        insertClassProficiencies(db, 4, 8, 14);
        insertClassProficiencies(db, 5, 4, 14);
        insertClassProficiencies(db, 6, 14, 20);
        insertClassProficiencies(db, 7, 7, 8);
        insertClassProficiencies(db, 8, 14, 20);
        insertClassProficiencies(db, 9, 1, 3);
        insertClassProficiencies(db, 10, 14, 20);
        insertClassProficiencies(db, 11, 3, 8);
        insertClassProficiencies(db, 12, 1, 3);
        insertClassProficiencies(db, 13, 7, 20);
    }

    private static void insertClassProficiencies(SQLiteDatabase db, int classId, int firstProficiencyId, int secondProficiencyId) {
        insertClassProficiency(db, classId, firstProficiencyId);
        insertClassProficiency(db, classId, secondProficiencyId);
    }

    private static void insertClassProficiency(SQLiteDatabase db, int classId, int proficiencyId) {
        ContentValues values = new ContentValues();
        values.put("class_id", classId);
        values.put("proficiency_id", proficiencyId);
        db.insertWithOnConflict("class_proficiency", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static class DbOption {
        public final int id;
        public final String name;

        public DbOption(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class ClassOption extends DbOption {
        public final int hitDice;

        public ClassOption(int id, String name, int hitDice) {
            super(id, name);
            this.hitDice = hitDice;
        }
    }

    public static class ClassBaseStats {
        public final int strength;
        public final int constitution;
        public final int dexterity;
        public final int intelligence;
        public final int wisdom;
        public final int charisma;

        public ClassBaseStats(int strength, int constitution, int dexterity, int intelligence, int wisdom, int charisma) {
            this.strength = strength;
            this.constitution = constitution;
            this.dexterity = dexterity;
            this.intelligence = intelligence;
            this.wisdom = wisdom;
            this.charisma = charisma;
        }
    }

    public static class CharacterCreateData {
        public String name;
        public int level;
        public int classId;
        public int raceId;
        public int backgroundId;
        public String alignment;
        public String personalityTraits;
        public String ideals;
        public String bonds;
        public String flaws;
        public int strength;
        public int constitution;
        public int dexterity;
        public int intelligence;
        public int wisdom;
        public int charisma;
        public List<Integer> languageIds = new ArrayList<>();
        public List<Integer> proficiencyIds = new ArrayList<>();
    }
}
