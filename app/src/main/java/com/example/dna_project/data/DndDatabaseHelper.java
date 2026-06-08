package com.example.dna_project.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DndDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "dnd_project.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_SPELLS = "spell";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_DESCRIPTION = "description";

    private static final String DATABASE_ASSET_PATH = "databases/" + DATABASE_NAME;

    public DndDatabaseHelper(Context context) {
        super(context, prepareDatabase(context.getApplicationContext()), null, DATABASE_VERSION);
    }

    private static String prepareDatabase(Context context) {
        File databaseFile = context.getDatabasePath(DATABASE_NAME);
        if (databaseFile.exists()) {
            return DATABASE_NAME;
        }

        File parent = databaseFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Could not create the database directory");
        }

        try (InputStream input = context.getAssets().open(DATABASE_ASSET_PATH);
             OutputStream output = new FileOutputStream(databaseFile)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not copy the bundled database", exception);
        }

        return DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_SPELLS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_LEVEL + " INTEGER, " +
                        COLUMN_DESCRIPTION + " TEXT" +
                        ")"
        );
        createStatsTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createStatsTables(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 3 && newVersion == 2) {
            db.execSQL("DROP TABLE IF EXISTS app_character_state");
            return;
        }
        super.onDowngrade(db, oldVersion, newVersion);
    }

    private static void createStatsTables(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS character_combat_state (" +
                        "character_id INTEGER PRIMARY KEY, " +
                        "armor_class INTEGER NOT NULL DEFAULT 10, " +
                        "speed INTEGER NOT NULL DEFAULT 30, " +
                        "current_hit_points INTEGER NOT NULL DEFAULT 0, " +
                        "maximum_hit_points INTEGER NOT NULL DEFAULT 0, " +
                        "temporary_hit_points INTEGER NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE" +
                        ")"
        );
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS character_detail_option (" +
                        "id INTEGER PRIMARY KEY, " +
                        "option_type TEXT NOT NULL, " +
                        "background_id INTEGER, " +
                        "value TEXT NOT NULL, " +
                        "description TEXT, " +
                        "sort_order INTEGER NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY (background_id) REFERENCES background(id) ON DELETE CASCADE" +
                        ")"
        );
    }

}
