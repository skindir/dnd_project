package com.example.dna_project.spellbook;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// Atsevisks SpellBook slanis: seit ir tikai darbs ar SQLite, bez pogu un ekranu veidosanas.
public class SpellBookRepository {
    private static final String DB_NAME = "dnd_project.db";

    private final Context context;
    private SQLiteDatabase database;
    private boolean spellBookTablesReady;

    public SpellBookRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public String casterType(String className) {
        // caster_type nosaka, kuru slotu tabulas tipu izmantot: full, half, third, pact vai none.
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return "none";
            }
            try (Cursor cursor = db.rawQuery(
                    "SELECT caster_type FROM class WHERE name = ? LIMIT 1",
                    new String[]{className}
            )) {
                if (cursor.moveToFirst()) {
                    return cursor.getString(0);
                }
            }
        } catch (Exception ignored) {
            return "none";
        }
        return "none";
    }

    public int[] maximumSlots(String casterType, int characterLevel) {
        // Atgriez maksimalo burvestibu slotu skaitu konkreta personaza limenim.
        int[] slots = new int[10];
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return slots;
            }
            try (Cursor cursor = db.rawQuery(
                    "SELECT level_1, level_2, level_3, level_4, level_5, level_6, level_7, level_8, level_9 " +
                            "FROM spell_slots WHERE caster_type = ? AND level = ? LIMIT 1",
                    new String[]{casterType, String.valueOf(characterLevel)}
            )) {
                if (cursor.moveToFirst()) {
                    for (int level = 1; level <= 9; level++) {
                        slots[level] = cursor.getInt(level - 1);
                    }
                }
            }
        } catch (Exception ignored) {
            return slots;
        }
        return slots;
    }

    public int[] usedSlots(int characterId) {
        // Nolasa jau izlietotos slotus konkreta personaza SpellBook.
        int[] slots = new int[10];
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return slots;
            }
            try (Cursor cursor = db.rawQuery(
                    "SELECT spell_level, used_slots FROM character_spell_slots WHERE character_id = ?",
                    new String[]{String.valueOf(characterId)}
            )) {
                while (cursor.moveToNext()) {
                    int level = cursor.getInt(0);
                    if (level >= 1 && level <= 9) {
                        slots[level] = cursor.getInt(1);
                    }
                }
            }
        } catch (Exception ignored) {
            return slots;
        }
        return slots;
    }

    public List<String> learnedSpellsForLevel(int characterId, int spellLevel) {
        // Saraksts ar burvestibam, ko personazs jau ir pievienojis SpellBook.
        List<String> spells = new ArrayList<>();
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return spells;
            }
            try (Cursor cursor = db.rawQuery(
                    "SELECT s.name FROM character_spell cs " +
                            "JOIN spell s ON s.id = cs.spell_id " +
                            "WHERE cs.character_id = ? AND s.level = ? " +
                            "ORDER BY s.name",
                    new String[]{String.valueOf(characterId), String.valueOf(spellLevel)}
            )) {
                while (cursor.moveToNext()) {
                    spells.add(cursor.getString(0));
                }
            }
        } catch (Exception ignored) {
            spells.clear();
        }
        return spells;
    }

    public List<List<String>> learnedSpellsByLevel(int characterId) {
        List<List<String>> spellsByLevel = new ArrayList<>();
        for (int level = 0; level <= 9; level++) {
            spellsByLevel.add(new ArrayList<>());
        }
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return spellsByLevel;
            }
            try (Cursor cursor = db.rawQuery(
                    "SELECT s.level, s.name FROM character_spell cs " +
                            "JOIN spell s ON s.id = cs.spell_id " +
                            "WHERE cs.character_id = ? " +
                            "ORDER BY s.level, s.name",
                    new String[]{String.valueOf(characterId)}
            )) {
                while (cursor.moveToNext()) {
                    int level = cursor.getInt(0);
                    if (level >= 0 && level <= 9) {
                        spellsByLevel.get(level).add(cursor.getString(1));
                    }
                }
            }
        } catch (Exception ignored) {
            for (List<String> spells : spellsByLevel) {
                spells.clear();
            }
        }
        return spellsByLevel;
    }

    public List<SpellDefinition> availableSpells(
            int characterId,
            String className,
            int characterLevel,
            int maxLearnableSpellLevel,
            int presetLevel
    ) {
        // Atgriez tikai tas burvestibas, ko klase var iemacities un kas vel nav pievienotas.
        List<SpellDefinition> result = new ArrayList<>();
        if (presetLevel < 0) {
            return result;
        }
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return result;
            }

            String levelFilter = presetLevel >= 0 ? " AND s.level = ?" : "";
            List<String> args = new ArrayList<>();
            args.add(className);
            args.add(String.valueOf(characterLevel));
            args.add(String.valueOf(characterId));
            if (presetLevel >= 0) {
                args.add(String.valueOf(presetLevel));
            }

            String sql = "SELECT s.id, s.name, s.level, NULL " +
                    "FROM class c " +
                    "JOIN class_spells cs ON cs.class_id = c.id " +
                    "JOIN spell s ON s.id = cs.spell_id " +
                    "WHERE c.name = ? AND cs.level_req <= ? " +
                    "AND NOT EXISTS (" +
                    "SELECT 1 FROM character_spell learned " +
                    "WHERE learned.character_id = ? AND learned.spell_id = s.id" +
                    ")" + levelFilter +
                    " ORDER BY s.level, s.name";
            readSpells(db, result, sql, args, className);

            if (!result.isEmpty()) {
                return result;
            }

            // Ja class_spells neatrod burvestibas, izmantojam kopejo spell tabulas sarakstu.
            List<String> fallbackArgs = new ArrayList<>();
            fallbackArgs.add(String.valueOf(maxLearnableSpellLevel));
            fallbackArgs.add(String.valueOf(characterId));
            if (presetLevel >= 0) {
                fallbackArgs.add(String.valueOf(presetLevel));
            }
            String fallbackLevelFilter = presetLevel >= 0 ? " AND level = ?" : "";
            String fallbackSql = "SELECT id, name, level, NULL FROM spell " +
                    "WHERE level <= ? " +
                    "AND NOT EXISTS (" +
                    "SELECT 1 FROM character_spell learned " +
                    "WHERE learned.character_id = ? AND learned.spell_id = spell.id" +
                    ")" + fallbackLevelFilter +
                    " ORDER BY level, name";
            readSpells(db, result, fallbackSql, fallbackArgs, "Any");
        } catch (Exception ignored) {
            result.clear();
        }
        return result;
    }

    public SpellDefinition findSpell(String spellName) {
        // Mekle burvestibu pec nosaukuma, lai detalas loga paraditu aprakstu.
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return null;
            }
            try (Cursor cursor = db.rawQuery(
                    "SELECT id, name, level, description FROM spell WHERE lower(name) = lower(?) LIMIT 1",
                    new String[]{spellName}
            )) {
                if (cursor.moveToFirst()) {
                    return new SpellDefinition(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getInt(2),
                            "Database",
                            cursor.getString(3)
                    );
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    public boolean addKnownSpell(int characterId, SpellDefinition spell) {
        // Saglaba izveleto burvestibu personazam character_spell tabula.
        if (spell.id < 0) {
            return false;
        }
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return false;
            }
            db.execSQL(
                    "INSERT OR IGNORE INTO character_spell (character_id, spell_id, is_prepared, source) VALUES (?, ?, 1, 'class')",
                    new Object[]{characterId, spell.id}
            );
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean learnedSpell(int characterId, String spellName) {
        // Parbauda, vai personazam sada burvestiba jau ir pievienota.
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return false;
            }
            try (Cursor cursor = db.rawQuery(
                    "SELECT 1 FROM character_spell cs " +
                            "JOIN spell s ON s.id = cs.spell_id " +
                            "WHERE cs.character_id = ? AND lower(s.name) = lower(?) LIMIT 1",
                    new String[]{String.valueOf(characterId), spellName}
            )) {
                return cursor.moveToFirst();
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean castSpell(int characterId, int spellLevel) {
        // Kad burvestiba tiek izmantota, palielinam izlietoto slotu skaitu saja limeni.
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return false;
            }
            db.execSQL(
                    "INSERT OR IGNORE INTO character_spell_slots (character_id, spell_level, used_slots) VALUES (?, ?, 0)",
                    new Object[]{characterId, spellLevel}
            );
            db.execSQL(
                    "UPDATE character_spell_slots SET used_slots = used_slots + 1 WHERE character_id = ? AND spell_level = ?",
                    new Object[]{characterId, spellLevel}
            );
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean restoreSpellUses(int characterId) {
        // Long rest/Short rest notira personaza izlietotos burvestibu slotus.
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null) {
                return false;
            }
            db.delete("character_spell_slots", "character_id = ?", new String[]{String.valueOf(characterId)});
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private SQLiteDatabase openDatabase() {
        if (database != null && database.isOpen()) {
            ensureSpellBookTables(database);
            return database;
        }

        // Pirmaja palaisana Android kope datubazi no assets uz lietotnes ieksejo mapi.
        File databaseFile = context.getDatabasePath(DB_NAME);
        if (!databaseFile.exists()) {
            File parent = databaseFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (InputStream input = context.getAssets().open(DB_NAME);
                 FileOutputStream output = new FileOutputStream(databaseFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            } catch (IOException exception) {
                return null;
            }
        }
        database = SQLiteDatabase.openDatabase(databaseFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        ensureSpellBookTables(database);
        return database;
    }

    private void ensureSpellBookTables(SQLiteDatabase db) {
        if (spellBookTablesReady) {
            return;
        }
        // Sis tabulas glaba izveletas burvestibas un izmantotos spell slots.
        db.execSQL("CREATE TABLE IF NOT EXISTS character_spell (" +
                "character_id INTEGER NOT NULL, " +
                "spell_id INTEGER NOT NULL, " +
                "is_prepared INTEGER DEFAULT 1, " +
                "source TEXT DEFAULT 'class', " +
                "PRIMARY KEY (character_id, spell_id))");
        db.execSQL("CREATE TABLE IF NOT EXISTS character_spell_slots (" +
                "character_id INTEGER NOT NULL, " +
                "spell_level INTEGER NOT NULL, " +
                "used_slots INTEGER DEFAULT 0, " +
                "PRIMARY KEY (character_id, spell_level))");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_character_spell_character ON character_spell(character_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_character_spell_spell ON character_spell(spell_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_spell_level_name ON spell(level, name)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_class_spells_class_level ON class_spells(class_id, level_req)");
        spellBookTablesReady = true;
    }

    private void readSpells(
            SQLiteDatabase db,
            List<SpellDefinition> result,
            String sql,
            List<String> args,
            String className
    ) {
        // Kopeja metode, kas Cursor rindas parvers par SpellDefinition objektiem.
        try (Cursor cursor = db.rawQuery(sql, args.toArray(new String[0]))) {
            while (cursor.moveToNext()) {
                result.add(new SpellDefinition(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        className,
                        cursor.getString(3)
                ));
            }
        }
    }
}
