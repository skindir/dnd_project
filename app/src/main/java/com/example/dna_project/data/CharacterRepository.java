package com.example.dna_project.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stores character-sheet data locally.
 *
 * The inventory itself is handled separately by InventoryRepository. This repository
 * only stores the character fields required by MainActivity so they survive app restarts.
 */
public class CharacterRepository {

    private static final String PREFS_NAME = "dnd_character_repository";
    private static final String KEY_CHARACTERS = "characters";
    private static final String KEY_NEXT_ID = "next_id";
    private static final int SPELL_LEVEL_COUNT = 10;

    private final SharedPreferences preferences;

    public CharacterRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized List<CharacterData> loadCharacters() {
        List<CharacterData> characters = new ArrayList<>();
        String json = preferences.getString(KEY_CHARACTERS, "[]");

        try {
            JSONArray array = new JSONArray(json == null ? "[]" : json);
            for (int index = 0; index < array.length(); index++) {
                JSONObject object = array.optJSONObject(index);
                if (object != null) {
                    characters.add(CharacterData.fromJson(object));
                }
            }
        } catch (JSONException ignored) {
            // If locally stored data is damaged, return an empty list instead of crashing.
        }

        return characters;
    }

    public synchronized long saveCharacter(CharacterData character) {
        if (character == null) {
            throw new IllegalArgumentException("Character data must not be null");
        }

        List<CharacterData> characters = loadCharacters();
        if (character.id <= 0) {
            character.id = nextId();
        }

        boolean replaced = false;
        for (int index = 0; index < characters.size(); index++) {
            if (characters.get(index).id == character.id) {
                characters.set(index, character.copy());
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            characters.add(character.copy());
        }

        writeCharacters(characters);
        return character.id;
    }

    public synchronized void deleteCharacter(long characterId) {
        List<CharacterData> characters = loadCharacters();
        Iterator<CharacterData> iterator = characters.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().id == characterId) {
                iterator.remove();
                break;
            }
        }
        writeCharacters(characters);
    }

    private long nextId() {
        long id = preferences.getLong(KEY_NEXT_ID, 1L);
        preferences.edit().putLong(KEY_NEXT_ID, id + 1L).apply();
        return id;
    }

    private void writeCharacters(List<CharacterData> characters) {
        JSONArray array = new JSONArray();
        for (CharacterData character : characters) {
            array.put(character.toJson());
        }
        preferences.edit().putString(KEY_CHARACTERS, array.toString()).apply();
    }

    public static class CharacterData {
        public long id;
        public String name = "No Name";
        public String characterClass = "No Class";
        public int level = 1;
        public int strength = 10;
        public int dexterity = 10;
        public int constitution = 10;
        public int intelligence = 10;
        public int charisma = 10;
        public int wisdom = 10;
        public int speed = 30;
        public int armorClass = 10;
        public String race = "Not specified";
        public String background = "Not specified";
        public String alignment = "Not specified";
        public int currentHp = 10;
        public int maxHp = 10;
        public int temporaryHp;
        public String hitDice = "d8";
        public String personalityTraits = "None";
        public String ideals = "None";
        public String bonds = "None";
        public String flaws = "None";
        public int platinum;
        public int gold;
        public int silver;
        public int copper;
        public final List<String> languages = new ArrayList<>();
        public final List<String> savingThrows = new ArrayList<>();
        public final List<List<String>> spellbook = createEmptySpellbook();

        public CharacterData() {
        }

        private CharacterData copy() {
            CharacterData copy = new CharacterData();
            copy.id = id;
            copy.name = name;
            copy.characterClass = characterClass;
            copy.level = level;
            copy.strength = strength;
            copy.dexterity = dexterity;
            copy.constitution = constitution;
            copy.intelligence = intelligence;
            copy.charisma = charisma;
            copy.wisdom = wisdom;
            copy.speed = speed;
            copy.armorClass = armorClass;
            copy.race = race;
            copy.background = background;
            copy.alignment = alignment;
            copy.currentHp = currentHp;
            copy.maxHp = maxHp;
            copy.temporaryHp = temporaryHp;
            copy.hitDice = hitDice;
            copy.personalityTraits = personalityTraits;
            copy.ideals = ideals;
            copy.bonds = bonds;
            copy.flaws = flaws;
            copy.platinum = platinum;
            copy.gold = gold;
            copy.silver = silver;
            copy.copper = copper;
            copy.languages.addAll(languages);
            copy.savingThrows.addAll(savingThrows);
            copySpellbook(spellbook, copy.spellbook);
            return copy;
        }

        private JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {
                object.put("id", id);
                object.put("name", valueOrDefault(name, "No Name"));
                object.put("class", valueOrDefault(characterClass, "No Class"));
                object.put("level", level);
                object.put("strength", strength);
                object.put("dexterity", dexterity);
                object.put("constitution", constitution);
                object.put("intelligence", intelligence);
                object.put("charisma", charisma);
                object.put("wisdom", wisdom);
                object.put("speed", speed);
                object.put("armorClass", armorClass);
                object.put("race", valueOrDefault(race, "Not specified"));
                object.put("background", valueOrDefault(background, "Not specified"));
                object.put("alignment", valueOrDefault(alignment, "Not specified"));
                object.put("currentHp", currentHp);
                object.put("maxHp", maxHp);
                object.put("temporaryHp", temporaryHp);
                object.put("hitDice", valueOrDefault(hitDice, "d8"));
                object.put("personalityTraits", valueOrDefault(personalityTraits, "None"));
                object.put("ideals", valueOrDefault(ideals, "None"));
                object.put("bonds", valueOrDefault(bonds, "None"));
                object.put("flaws", valueOrDefault(flaws, "None"));
                object.put("platinum", platinum);
                object.put("gold", gold);
                object.put("silver", silver);
                object.put("copper", copper);
                object.put("languages", stringListToJson(languages));
                object.put("savingThrows", stringListToJson(savingThrows));

                JSONArray spellbookArray = new JSONArray();
                for (List<String> levelSpells : spellbook) {
                    spellbookArray.put(stringListToJson(levelSpells));
                }
                object.put("spellbook", spellbookArray);
            } catch (JSONException ignored) {
                // Local primitive values and arrays are expected to serialize successfully.
            }
            return object;
        }

        private static CharacterData fromJson(JSONObject object) {
            CharacterData data = new CharacterData();
            data.id = object.optLong("id", 0L);
            data.name = object.optString("name", "No Name");
            data.characterClass = object.optString("class", "No Class");
            data.level = object.optInt("level", 1);
            data.strength = object.optInt("strength", 10);
            data.dexterity = object.optInt("dexterity", 10);
            data.constitution = object.optInt("constitution", 10);
            data.intelligence = object.optInt("intelligence", 10);
            data.charisma = object.optInt("charisma", 10);
            data.wisdom = object.optInt("wisdom", 10);
            data.speed = object.optInt("speed", 30);
            data.armorClass = object.optInt("armorClass", 10);
            data.race = object.optString("race", "Not specified");
            data.background = object.optString("background", "Not specified");
            data.alignment = object.optString("alignment", "Not specified");
            data.currentHp = object.optInt("currentHp", 10);
            data.maxHp = object.optInt("maxHp", 10);
            data.temporaryHp = object.optInt("temporaryHp", 0);
            data.hitDice = object.optString("hitDice", "d8");
            data.personalityTraits = object.optString("personalityTraits", "None");
            data.ideals = object.optString("ideals", "None");
            data.bonds = object.optString("bonds", "None");
            data.flaws = object.optString("flaws", "None");
            data.platinum = object.optInt("platinum", 0);
            data.gold = object.optInt("gold", 0);
            data.silver = object.optInt("silver", 0);
            data.copper = object.optInt("copper", 0);
            readStringList(object.optJSONArray("languages"), data.languages);
            readStringList(object.optJSONArray("savingThrows"), data.savingThrows);
            readSpellbook(object.optJSONArray("spellbook"), data.spellbook);
            return data;
        }

        private static JSONArray stringListToJson(List<String> values) {
            JSONArray array = new JSONArray();
            for (String value : values) {
                array.put(value);
            }
            return array;
        }

        private static void readStringList(JSONArray source, List<String> target) {
            if (source == null) {
                return;
            }
            for (int index = 0; index < source.length(); index++) {
                String value = source.optString(index, "");
                if (!value.isEmpty()) {
                    target.add(value);
                }
            }
        }

        private static void readSpellbook(JSONArray source, List<List<String>> target) {
            if (source == null) {
                return;
            }
            for (int level = 0; level < Math.min(source.length(), target.size()); level++) {
                readStringList(source.optJSONArray(level), target.get(level));
            }
        }

        private static List<List<String>> createEmptySpellbook() {
            List<List<String>> result = new ArrayList<>();
            for (int level = 0; level < SPELL_LEVEL_COUNT; level++) {
                result.add(new ArrayList<>());
            }
            return result;
        }

        private static void copySpellbook(List<List<String>> source, List<List<String>> target) {
            for (int level = 0; level < Math.min(source.size(), target.size()); level++) {
                target.get(level).addAll(source.get(level));
            }
        }

        private static String valueOrDefault(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
