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
        values.put(DndDatabaseHelper.COLUMN_CLASS_NAME, className);
        values.put(DndDatabaseHelper.COLUMN_RANGE, range);
        values.put(DndDatabaseHelper.COLUMN_ATTACK_TYPE, attackType);
        values.put(DndDatabaseHelper.COLUMN_DAMAGE_TYPE, damageType);
        values.put(DndDatabaseHelper.COLUMN_DAMAGE, damage);
        return db.insert(DndDatabaseHelper.TABLE_SPELLS, null, values);
    }

    public List<Spell> getAllSpells() {
        databaseHelper.ensureSeedData();
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
                        cursor.getString(cursor.getColumnIndexOrThrow(DndDatabaseHelper.COLUMN_CLASS_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DndDatabaseHelper.COLUMN_RANGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DndDatabaseHelper.COLUMN_ATTACK_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DndDatabaseHelper.COLUMN_DAMAGE_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DndDatabaseHelper.COLUMN_DAMAGE))
                ));
            }
        }

        return spells;
    }
}
