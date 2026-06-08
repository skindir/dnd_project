package com.example.dna_project.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SpellRepository {
    private final DndDatabaseHelper databaseHelper;

    public SpellRepository(Context context) {
        databaseHelper = new DndDatabaseHelper(context.getApplicationContext());
    }

    public long addSpell(String name, int level, String className, String range,
                         String attackType, String damageType, String damage) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DndDatabaseHelper.COLUMN_NAME, name);
        values.put(DndDatabaseHelper.COLUMN_LEVEL, level);
        values.put(DndDatabaseHelper.COLUMN_DESCRIPTION, buildDescription(
                className, range, attackType, damageType, damage
        ));
        return db.insert(DndDatabaseHelper.TABLE_SPELLS, null, values);
    }

    public List<Spell> getAllSpells() {
        List<Spell> spells = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                DndDatabaseHelper.TABLE_SPELLS,
                null,
                null,
                null,
                null,
                null,
                DndDatabaseHelper.COLUMN_LEVEL + ", " + DndDatabaseHelper.COLUMN_NAME
        )) {
            while (cursor.moveToNext()) {
                spells.add(new Spell(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DndDatabaseHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DndDatabaseHelper.COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DndDatabaseHelper.COLUMN_LEVEL)),
                        "",
                        "",
                        "",
                        "",
                        ""
                ));
            }
        }

        return spells;
    }

    private static String buildDescription(String className, String range, String attackType,
                                           String damageType, String damage) {
        return "Class: " + className + "\n" +
                "Range: " + range + "\n" +
                "Attack type: " + attackType + "\n" +
                "Damage type: " + damageType + "\n" +
                "Damage: " + damage;
    }
}
