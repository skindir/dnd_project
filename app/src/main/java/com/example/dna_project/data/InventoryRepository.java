package com.example.dna_project.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InventoryRepository {
    private static final String DATABASE_NAME = "dnd_project.db";
    private static final String ASSET_PATH = "database/dnd_project.db";

    public static final String TYPE_WEAPON = "Weapon";
    public static final String TYPE_ARMOR = "Armor";
    public static final String TYPE_ACCESSORIES = "Acessories";
    public static final String TYPE_INSTRUMENTS = "Instruments";
    public static final String TYPE_MATERIAL = "Material";
    public static final String TYPE_OTHER = "Other";
    public static final String TYPE_QUEST = "Quest item";
    public static final String TYPE_ALL = "All categories";

    private final Context context;
    private SQLiteDatabase database;

    public InventoryRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public SQLiteDatabase db() {
        if (database == null || !database.isOpen()) {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (!dbFile.exists()) {
                copyAssetDatabase(dbFile);
            }
            database = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
            database.execSQL("PRAGMA foreign_keys=ON");
            if (!hasRequiredSchema(database)) {
                database.close();
                if (!dbFile.delete()) {
                    throw new IllegalStateException("Unable to replace invalid inventory database");
                }
                copyAssetDatabase(dbFile);
                database = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
                database.execSQL("PRAGMA foreign_keys=ON");
            }
            migrate(database);
        }
        return database;
    }

    private void copyAssetDatabase(File dbFile) {
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (InputStream input = context.getAssets().open(ASSET_PATH);
             FileOutputStream output = new FileOutputStream(dbFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to copy inventory database", exception);
        }
    }

    private void migrate(SQLiteDatabase db) {
        if (!columnExists(db, "item", "equipment_slot")) {
            db.execSQL("ALTER TABLE item ADD COLUMN equipment_slot TEXT");
            fillEquipmentSlots(db);
        }
    }

    private boolean hasRequiredSchema(SQLiteDatabase db) {
        String[] tables = {"characters", "inventory", "equipment", "bag", "bag_item", "item"};
        for (String table : tables) {
            if (!tableExists(db, table)) {
                return false;
            }
        }
        return columnExists(db, "characters", "inventory_id")
                && columnExists(db, "inventory", "equipment_id")
                && columnExists(db, "inventory", "bag_id")
                && columnExists(db, "item", "icon");
    }

    private boolean tableExists(SQLiteDatabase db, String table) {
        try (Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
                new String[]{table})) {
            return cursor.moveToFirst();
        }
    }

    private boolean columnExists(SQLiteDatabase db, String table, String column) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (cursor.moveToNext()) {
                if (column.equals(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void fillEquipmentSlots(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT id, name, item_type, description FROM item", null)) {
            while (cursor.moveToNext()) {
                int itemId = cursor.getInt(0);
                String name = cursor.getString(1);
                String itemType = cursor.getString(2);
                String description = cursor.getString(3);
                String slot = inferEquipmentSlot(name, itemType, description);
                ContentValues values = new ContentValues();
                values.put("equipment_slot", slot);
                db.update("item", values, "id = ?", new String[]{String.valueOf(itemId)});
            }
        }
    }

    @Nullable
    private String inferEquipmentSlot(String name, String itemType, String description) {
        String text = ((name == null ? "" : name) + " " + (description == null ? "" : description))
                .toLowerCase(Locale.US);
        if (TYPE_WEAPON.equals(itemType)) {
            if (text.contains("shield") || text.contains("buckler")) {
                return "off_hand";
            }
            return "hand";
        }
        if (!TYPE_ARMOR.equals(itemType) && !TYPE_ACCESSORIES.equals(itemType)) {
            return null;
        }
        if (text.contains("earring")) return "earring";
        if (text.contains("ring")) return "ring";
        if (text.contains("necklace") || text.contains("amulet") || text.contains("pendant")
                || text.contains("charm") || text.contains("medallion")) return "neck";
        if (text.contains("helmet") || text.contains("helm") || text.contains("hood")
                || text.contains("cap") || text.contains("crown") || text.contains("hat")) return "head";
        if (text.contains("pauldron") || text.contains("shoulder")) return "pauldron";
        if (text.contains("glove") || text.contains("gauntlet")) return "glove";
        if (text.contains("belt") || text.contains("girdle") || text.contains("sash")) return "belt";
        if (text.contains("pants") || text.contains("trouser") || text.contains("leggings")) return "pants";
        if (text.contains("boot") || text.contains("sandal") || text.contains("shoe")) return "boots";
        if (text.contains("chest") || text.contains("breastplate") || text.contains("armor")
                || text.contains("mail") || text.contains("robe") || text.contains("cuirass")
                || text.contains("vest") || text.contains("brigandine") || text.contains("plate")) return "chest";
        return null;
    }

    public InventoryState getInventoryForCharacter(int characterId) {
        SQLiteDatabase db = db();
        try (Cursor cursor = db.rawQuery(
                "SELECT ch.inventory_id, inv.equipment_id, inv.bag_id, " +
                        "COALESCE(inv.platinum, 0), COALESCE(inv.gold, 0), COALESCE(inv.silver, 0), COALESCE(inv.bronze, 0) " +
                        "FROM characters ch LEFT JOIN inventory inv ON inv.id = ch.inventory_id WHERE ch.id = ?",
                new String[]{String.valueOf(characterId)})) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return new InventoryState(
                        characterId,
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6)
                );
            }
        }
        return createInventoryForCharacter(characterId);
    }

    public InventoryState createInventoryForCharacter(int characterId) {
        SQLiteDatabase db = db();
        db.beginTransaction();
        try {
            long equipmentId = db.insertOrThrow("equipment", "head_id", new ContentValues());
            long bagId = db.insertOrThrow("bag", "id", new ContentValues());
            ContentValues inventory = new ContentValues();
            inventory.put("platinum", 0);
            inventory.put("gold", 0);
            inventory.put("silver", 0);
            inventory.put("bronze", 0);
            inventory.put("equipment_id", equipmentId);
            inventory.put("bag_id", bagId);
            long inventoryId = db.insertOrThrow("inventory", null, inventory);
            ContentValues character = new ContentValues();
            character.put("inventory_id", inventoryId);
            db.update("characters", character, "id = ?", new String[]{String.valueOf(characterId)});
            db.setTransactionSuccessful();
            return new InventoryState(characterId, (int) inventoryId, (int) equipmentId, (int) bagId, 0, 0, 0, 0);
        } finally {
            db.endTransaction();
        }
    }

    public int ensureCharacter(String name, String characterClass, int level, int characterId) {
        SQLiteDatabase db = db();
        if (characterId > 0) {
            try (Cursor cursor = db.rawQuery("SELECT id FROM characters WHERE id = ?", new String[]{String.valueOf(characterId)})) {
                if (cursor.moveToFirst()) {
                    return characterId;
                }
            }
        }
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("level", level);
        values.put("gender", "");
        values.put("class_id", findIdByName(db, "class", characterClass));
        long id = db.insertOrThrow("characters", null, values);
        return (int) id;
    }

    @Nullable
    private Integer findIdByName(SQLiteDatabase db, String table, String name) {
        try (Cursor cursor = db.rawQuery("SELECT id FROM " + table + " WHERE name = ? LIMIT 1", new String[]{name})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return null;
    }

    public Map<String, InventoryItem> getEquippedItems(int equipmentId) {
        Map<String, InventoryItem> result = new HashMap<>();
        try (Cursor equipment = db().rawQuery("SELECT * FROM equipment WHERE id = ?", new String[]{String.valueOf(equipmentId)})) {
            if (!equipment.moveToFirst()) {
                return result;
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                int index = equipment.getColumnIndex(slot.column);
                if (index >= 0 && !equipment.isNull(index)) {
                    InventoryItem item = getItem(equipment.getInt(index));
                    if (item != null) {
                        result.put(slot.column, item);
                    }
                }
            }
        }
        return result;
    }

    @Nullable
    public InventoryItem getItem(int itemId) {
        try (Cursor cursor = db().rawQuery(
                "SELECT id, name, item_type, description, icon, equipment_slot FROM item WHERE id = ?",
                new String[]{String.valueOf(itemId)})) {
            if (cursor.moveToFirst()) {
                return readItem(cursor, -1, 1, false);
            }
        }
        return null;
    }

    public List<InventoryItem> getBagItemsByType(int bagId, String itemType, int equipmentId) {
        List<InventoryItem> result = new ArrayList<>();
        Map<Integer, String> equipped = getEquippedSlotsByItemId(equipmentId);
        try (Cursor cursor = db().rawQuery(
                "SELECT bi.id, bi.quantity, i.id, i.name, i.item_type, i.description, i.icon, i.equipment_slot " +
                        "FROM bag_item bi JOIN item i ON i.id = bi.item_id " +
                        "WHERE bi.bag_id = ? AND i.item_type = ? ORDER BY i.name, bi.id",
                new String[]{String.valueOf(bagId), itemType})) {
            while (cursor.moveToNext()) {
                int bagItemId = cursor.getInt(0);
                int quantity = cursor.getInt(1);
                InventoryItem item = readItem(cursor, bagItemId, quantity, false, 2);
                item.equipped = equipped.containsKey(item.itemId);
                item.equippedSlot = equipped.get(item.itemId);
                result.add(item);
            }
        }
        return result;
    }

    public List<InventoryItem> searchItems(String searchText, String itemType) {
        String query = searchText == null ? "" : searchText.trim();
        String type = itemType == null ? TYPE_ALL : itemType;
        List<InventoryItem> result = new ArrayList<>();
        try (Cursor cursor = db().rawQuery(
                "SELECT id, name, item_type, description, icon, equipment_slot FROM item " +
                        "WHERE (? = '' OR LOWER(name) LIKE '%' || LOWER(?) || '%') " +
                        "AND (? = ? OR item_type = ?) ORDER BY name",
                new String[]{query, query, type, TYPE_ALL, type})) {
            while (cursor.moveToNext()) {
                result.add(readItem(cursor, -1, 1, false));
            }
        }
        return result;
    }

    @Nullable
    public InventoryItem getBagItemDetails(int bagItemId, int equipmentId) {
        Map<Integer, String> equipped = getEquippedSlotsByItemId(equipmentId);
        try (Cursor cursor = db().rawQuery(
                "SELECT bi.id, bi.quantity, i.id, i.name, i.item_type, i.description, i.icon, i.equipment_slot " +
                        "FROM bag_item bi JOIN item i ON i.id = bi.item_id WHERE bi.id = ?",
                new String[]{String.valueOf(bagItemId)})) {
            if (cursor.moveToFirst()) {
                InventoryItem item = readItem(cursor, bagItemId, cursor.getInt(1), false, 2);
                item.equipped = equipped.containsKey(item.itemId);
                item.equippedSlot = equipped.get(item.itemId);
                return item;
            }
        }
        return null;
    }

    public int findBagItemIdForItem(int bagId, int itemId) {
        try (Cursor cursor = db().rawQuery(
                "SELECT id FROM bag_item WHERE bag_id = ? AND item_id = ? ORDER BY id LIMIT 1",
                new String[]{String.valueOf(bagId), String.valueOf(itemId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public void addBagItem(int bagId, int itemId) {
        InventoryItem item = getItem(itemId);
        if (item != null && isStackable(item.itemType)) {
            try (Cursor cursor = db().rawQuery(
                    "SELECT id, quantity FROM bag_item WHERE bag_id = ? AND item_id = ? LIMIT 1",
                    new String[]{String.valueOf(bagId), String.valueOf(itemId)})) {
                if (cursor.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put("quantity", cursor.getInt(1) + 1);
                    db().update("bag_item", values, "id = ?", new String[]{String.valueOf(cursor.getInt(0))});
                    return;
                }
            }
        }
        ContentValues values = new ContentValues();
        values.put("bag_id", bagId);
        values.put("item_id", itemId);
        values.put("quantity", 1);
        db().insertOrThrow("bag_item", null, values);
    }

    public void removeBagItem(int bagItemId) {
        db().delete("bag_item", "id = ?", new String[]{String.valueOf(bagItemId)});
    }

    public void decreaseBagItemQuantity(int bagItemId, int quantity) {
        db().execSQL("UPDATE bag_item SET quantity = quantity - ? WHERE id = ?",
                new Object[]{quantity, bagItemId});
    }

    public void updateMoney(int inventoryId, int platinum, int gold, int silver, int bronze) {
        ContentValues values = new ContentValues();
        values.put("platinum", platinum);
        values.put("gold", gold);
        values.put("silver", silver);
        values.put("bronze", bronze);
        db().update("inventory", values, "id = ?", new String[]{String.valueOf(inventoryId)});
    }

    public int getEquippedItemId(int equipmentId, String slotColumn) {
        try (Cursor cursor = db().rawQuery("SELECT " + slotColumn + " FROM equipment WHERE id = ?",
                new String[]{String.valueOf(equipmentId)})) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public void equipItem(int equipmentId, String slotColumn, int itemId) {
        ContentValues values = new ContentValues();
        values.put(slotColumn, itemId);
        db().update("equipment", values, "id = ?", new String[]{String.valueOf(equipmentId)});
    }

    public void unequipItem(int equipmentId, String slotColumn) {
        ContentValues values = new ContentValues();
        values.putNull(slotColumn);
        db().update("equipment", values, "id = ?", new String[]{String.valueOf(equipmentId)});
    }

    public void unequipAndRemoveItem(int equipmentId, String slotColumn, int bagItemId) {
        SQLiteDatabase db = db();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.putNull(slotColumn);
            db.update("equipment", values, "id = ?", new String[]{String.valueOf(equipmentId)});
            db.delete("bag_item", "id = ?", new String[]{String.valueOf(bagItemId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private Map<Integer, String> getEquippedSlotsByItemId(int equipmentId) {
        Map<Integer, String> result = new HashMap<>();
        try (Cursor cursor = db().rawQuery("SELECT * FROM equipment WHERE id = ?", new String[]{String.valueOf(equipmentId)})) {
            if (!cursor.moveToFirst()) {
                return result;
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                int index = cursor.getColumnIndex(slot.column);
                if (index >= 0 && !cursor.isNull(index)) {
                    result.put(cursor.getInt(index), slot.column);
                }
            }
        }
        return result;
    }

    private boolean isStackable(String itemType) {
        return TYPE_MATERIAL.equals(itemType) || TYPE_OTHER.equals(itemType);
    }

    private InventoryItem readItem(Cursor cursor, int bagItemId, int quantity, boolean equipped) {
        return readItem(cursor, bagItemId, quantity, equipped, 0);
    }

    private InventoryItem readItem(Cursor cursor, int bagItemId, int quantity, boolean equipped, int offset) {
        InventoryItem item = new InventoryItem();
        item.bagItemId = bagItemId;
        item.quantity = quantity;
        item.itemId = cursor.getInt(offset);
        item.name = cursor.getString(offset + 1);
        item.itemType = cursor.getString(offset + 2);
        item.description = cursor.getString(offset + 3);
        byte[] icon = cursor.getBlob(offset + 4);
        item.icon = icon == null ? null : BitmapFactory.decodeByteArray(icon, 0, icon.length);
        item.equipmentSlot = cursor.getString(offset + 5);
        item.twoHanded = isTwoHandedWeapon(item.name, item.description, item.itemType);
        item.equipped = equipped;
        return item;
    }

    private boolean isTwoHandedWeapon(String name, String description, String itemType) {
        if (!TYPE_WEAPON.equals(itemType)) {
            return false;
        }
        String text = ((name == null ? "" : name) + " " + (description == null ? "" : description))
                .toLowerCase(Locale.US);
        return text.contains("two-handed")
                || text.contains("longbow")
                || text.contains("shortbow")
                || text.contains("bow")
                || text.contains("heavy crossbow")
                || text.contains("greatsword")
                || text.contains("greataxe")
                || text.contains("maul")
                || text.contains("glaive")
                || text.contains("halberd")
                || text.contains("pike");
    }

    public static class InventoryState {
        public final int characterId;
        public final int inventoryId;
        public final int equipmentId;
        public final int bagId;
        public final int platinum;
        public final int gold;
        public final int silver;
        public final int bronze;

        InventoryState(int characterId, int inventoryId, int equipmentId, int bagId,
                       int platinum, int gold, int silver, int bronze) {
            this.characterId = characterId;
            this.inventoryId = inventoryId;
            this.equipmentId = equipmentId;
            this.bagId = bagId;
            this.platinum = platinum;
            this.gold = gold;
            this.silver = silver;
            this.bronze = bronze;
        }
    }

    public static class InventoryItem {
        public int bagItemId;
        public int itemId;
        public String name;
        public String itemType;
        public String description;
        public Bitmap icon;
        public int quantity;
        public boolean equipped;
        public String equippedSlot;
        public String equipmentSlot;
        public boolean twoHanded;
    }

    public enum EquipmentSlot {
        HEAD("head_id"),
        EARRING_RIGHT("earring_r_id"),
        EARRING_LEFT("earring_l_id"),
        NECK("neck_id"),
        PAULDRON_RIGHT("pauldron_r_id"),
        PAULDRON_LEFT("pauldron_l_id"),
        GLOVE_RIGHT("glove_r_id"),
        GLOVE_LEFT("glove_l_id"),
        RING_RIGHT("ring_r_id"),
        RING_LEFT("ring_l_id"),
        CHEST("chest_id"),
        BELT("belt_id"),
        PANTS("pants_id"),
        BOOTS("boots_id"),
        MAIN_HAND("main_hand_id"),
        OFF_HAND("off_hand_id");

        public final String column;

        EquipmentSlot(String column) {
            this.column = column;
        }
    }
}
