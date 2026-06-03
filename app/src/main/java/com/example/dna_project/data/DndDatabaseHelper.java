package com.example.dna_project.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DndDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "dnd_module.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_SPELLS = "spells";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_CLASS_NAME = "class_name";
    public static final String COLUMN_RANGE = "spell_range";
    public static final String COLUMN_ATTACK_TYPE = "attack_type";
    public static final String COLUMN_DAMAGE_TYPE = "damage_type";
    public static final String COLUMN_DAMAGE = "damage";

    private static final String CREATE_SPELLS_TABLE =
            "CREATE TABLE " + TABLE_SPELLS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_LEVEL + " INTEGER NOT NULL, " +
                    COLUMN_CLASS_NAME + " TEXT NOT NULL, " +
                    COLUMN_RANGE + " TEXT NOT NULL, " +
                    COLUMN_ATTACK_TYPE + " TEXT NOT NULL, " +
                    COLUMN_DAMAGE_TYPE + " TEXT NOT NULL, " +
                    COLUMN_DAMAGE + " TEXT NOT NULL" +
                    ")";

    public DndDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SPELLS_TABLE);
        insertStarterSpell(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPELLS);
        onCreate(db);
    }

    public void ensureSeedData() {
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SPELLS, null)) {
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                insertStarterSpell(db);
            }
        }
    }

    private static void insertStarterSpell(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, "Banshee's Scream");
        values.put(COLUMN_LEVEL, 2);
        values.put(COLUMN_CLASS_NAME, "Necromancer");
        values.put(COLUMN_RANGE, "Close");
        values.put(COLUMN_ATTACK_TYPE, "6 ft cone");
        values.put(COLUMN_DAMAGE_TYPE, "Necrotic");
        values.put(COLUMN_DAMAGE, "6d4");
        db.insert(TABLE_SPELLS, null, values);
    }
}
