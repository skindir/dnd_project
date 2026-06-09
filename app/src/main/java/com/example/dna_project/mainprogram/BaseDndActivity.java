package com.example.dna_project;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;   
import android.widget.ScrollView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dna_project.data.CharacterRepository;
import com.example.dna_project.data.CharacterRepository.CharacterData;
import com.example.dna_project.data.InventoryRepository;
import com.example.dna_project.data.InventoryRepository.InventoryItem;
import com.example.dna_project.data.InventoryRepository.InventoryState;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.dna_project.data.DndProjectDatabaseHelper;
import com.example.dna_project.data.DndProjectDatabaseHelper.CharacterCreateData;
import com.example.dna_project.data.DndProjectDatabaseHelper.ClassBaseStats;
import com.example.dna_project.data.DndProjectDatabaseHelper.ClassOption;
import com.example.dna_project.data.DndProjectDatabaseHelper.DbOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class BaseDndActivity extends AppCompatActivity {

    // General app settings and character tab identifiers.
    protected static final int MAX_CHARACTERS = 15;
    protected static final int TOTAL_ABILITY_POINTS = 75;

    protected static final int TAB_INVENTORY = 1;
    protected static final int TAB_STATS = 2;
    protected static final int TAB_SPELLBOOK = 3;
    protected static final float EQUIPMENT_IMAGE_ASPECT_RATIO = 1754f / 1284f;
    protected static final float EQUIPMENT_DESIGN_WIDTH = 1284f;
    protected static final float EQUIPMENT_DESIGN_HEIGHT = 1754f;
    protected static final int INVENTORY_COLUMNS = 7;

    // Inventory: backpack categories, item types, and category tab icons.
    protected static final String[] INVENTORY_CATEGORIES = {
            "Weapons",
            "Armor",
            "Accessories",
            "Tools",
            "Materials",
            "Other",
            "Quest Items"
    };
    protected static final String[] INVENTORY_ITEM_TYPES = {
            InventoryRepository.TYPE_WEAPON,
            InventoryRepository.TYPE_ARMOR,
            InventoryRepository.TYPE_ACCESSORIES,
            InventoryRepository.TYPE_INSTRUMENTS,
            InventoryRepository.TYPE_MATERIAL,
            InventoryRepository.TYPE_OTHER,
            InventoryRepository.TYPE_QUEST
    };
    protected static final int[] INVENTORY_CATEGORY_ICONS = {
            R.drawable.tab_weapon,
            R.drawable.tab_armor,
            R.drawable.tab_accessories,
            R.drawable.tab_instruments,
            R.drawable.tab_materials,
            R.drawable.tab_other,
            R.drawable.tab_key_items
    };

    // Character creation: available classes and selection dictionaries.
    protected static final String[] CHARACTER_CLASSES = {
            "Bard",
            "Barbarian",
            "Fighter",
            "Wizard",
            "Druid",
            "Cleric",
            "Artificer",
            "Warlock",
            "Monk",
            "Paladin",
            "Rogue",
            "Ranger",
            "Sorcerer"
    };

    protected static final String[] RACE_OPTIONS = {
            "Hill Dwarf",
            "Mountain Dwarf",
            "High Elf",
            "Wood Elf",
            "Dark Elf (Drow)",
            "Lightfoot Halfling",
            "Stout Halfling",
            "Human",
            "Variant Human",
            "Dragonborn",
            "Forest Gnome",
            "Rock Gnome",
            "Half-Elf",
            "Half-Orc",
            "Tiefling"
    };
    protected static final String[] BACKGROUND_OPTIONS = {
            "Acolyte",
            "Entertainer",
            "Urchin",
            "Noble",
            "Guild Artisan",
            "Sailor",
            "Sage",
            "Folk Hero",
            "Hermit",
            "Criminal",
            "Soldier",
            "Outlander",
            "Charlatan"
    };
    protected static final String[] ALIGNMENT_OPTIONS = {
            "Lawful Good",
            "Neutral Good",
            "Chaotic Good",
            "Lawful Neutral",
            "True Neutral",
            "Chaotic Neutral",
            "Lawful Evil",
            "Neutral Evil",
            "Chaotic Evil"
    };
    protected static final String[] PERSONALITY_TRAIT_OPTIONS = {
            "I am always polite and respectful",
            "I trust my friends and protect them",
            "I am used to seeking profit in any situation",
            "I speak plainly, even when it is unpleasant",
            "I stay calm in the face of danger",
            "I love beautiful stories and glorious deeds",
            "I find it hard to trust strangers",
            "I am always seeking new knowledge"
    };
    protected static final String[] IDEAL_OPTIONS = {
            "Good",
            "Freedom",
            "Justice",
            "Honor",
            "Knowledge",
            "Power",
            "Tradition",
            "Redemption"
    };
    protected static final String[] BOND_OPTIONS = {
            "I protect my family",
            "I serve my people",
            "I owe my life to an old friend",
            "I seek a lost relic",
            "I must avenge the past",
            "I keep my mentor's secret",
            "I want to restore my lost honor",
            "I am bound by oath to an order"
    };
    protected static final String[] FLAW_OPTIONS = {
            "I am too trusting",
            "I am greedy for gold",
            "I am hot-tempered",
            "I fear losing control",
            "I often underestimate danger",
            "I envy the fame of others",
            "I have trouble admitting mistakes",
            "I easily give in to temptation"
    };
    protected static final String[] LANGUAGE_OPTIONS = {
            "Abyssal",
            "Druidic",
            "Giant",
            "Infernal",
            "Thieves' Cant",
            "Celestial",
            "Undercommon",
            "Primordial",
            "Common",
            "Orc",
            "Elvish",
            "Gnomish",
            "Goblin",
            "Halfling",
            "Sylvan",
            "Dwarvish",
            "Draconic",
            "Deep Speech"
    };
    protected static final String[][] SAVING_THROW_GROUPS = {
            {"Strength", "Saving Throw (Strength)\nAthletics"},
            {"Dexterity", "Saving Throw (Dexterity)\nAcrobatics\nSleight of Hand\nStealth"},
            {"Constitution", "Saving Throw (Constitution)"},
            {"Intelligence", "Saving Throw (Intelligence)\nArcana\nHistory\nInvestigation\nNature\nReligion"},
            {"Wisdom", "Saving Throw (Wisdom)\nAnimal Handling\nInsight\nMedicine\nPerception\nSurvival"},
            {"Charisma", "Saving Throw (Charisma)\nDeception\nIntimidation\nPerformance\nPersuasion"}
    };
    protected static final String[] SAVING_THROW_OPTIONS = {
            "Saving Throw (Strength)",
            "Athletics",
            "Saving Throw (Dexterity)",
            "Acrobatics",
            "Sleight of Hand",
            "Stealth",
            "Saving Throw (Constitution)",
            "Saving Throw (Intelligence)",
            "Arcana",
            "History",
            "Investigation",
            "Nature",
            "Religion",
            "Saving Throw (Wisdom)",
            "Animal Handling",
            "Insight",
            "Medicine",
            "Perception",
            "Survival",
            "Saving Throw (Charisma)",
            "Deception",
            "Intimidation",
            "Performance",
            "Persuasion"

    };


    // App state: all characters, the currently selected character, and inventory data.
    protected final List<DndCharacter> characters = new ArrayList<>();
    protected CharacterRepository characterRepository;
    protected DndProjectDatabaseHelper projectDatabase;
    protected FrameLayout root;
    protected DndCharacter selectedCharacter;
    protected int selectedTab = TAB_STATS;
    protected int selectedInventoryCategory = 0;
    protected InventoryRepository inventoryRepository;
    protected InventoryState inventoryState;
    protected List<InventoryItem> visibleInventorySlots = new ArrayList<>();
    protected Map<String, InventoryItem> equippedItems = new HashMap<>();

    protected abstract void showCharacterSelect();

    protected abstract void showCreateCharacter();

    protected abstract void showCharacterSheet();

    protected abstract void renderTab(FrameLayout content);

    protected int classImageResource(String characterClass) {
        switch (characterClass) {
            case "Bard":
                return R.drawable.class_bard;
            case "Barbarian":
                return R.drawable.class_barbarian;
            case "Fighter":
                return R.drawable.class_warrior;
            case "Wizard":
                return R.drawable.class_wizard;
            case "Druid":
                return R.drawable.class_druid;
            case "Cleric":
                return R.drawable.class_cleric;
            case "Artificer":
                return R.drawable.class_artificer;
            case "Warlock":
                return R.drawable.class_warlock;
            case "Monk":
                return R.drawable.class_monk;
            case "Paladin":
                return R.drawable.class_paladin;
            case "Rogue":
                return R.drawable.class_rogue;
            case "Ranger":
                return R.drawable.class_ranger;
            case "Sorcerer":
                return R.drawable.class_sorcerer;
            default:
                return R.drawable.avatar_1;
        }
    }

    protected TextInputEditText textInput(LinearLayout parent, String label, String hint) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(label);
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setSingleLine(true);
        input.setHint(hint);
        layout.addView(input);
        parent.addView(layout);
        return input;
    }

    protected TextInputEditText multilineTextInput(LinearLayout parent, String label, String hint) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(label);
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setMinLines(2);
        input.setMaxLines(5);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint(hint);
        layout.addView(input);
        parent.addView(layout);
        return input;
    }

    protected TextInputEditText numberInput(GridLayout parent, String label, int defaultValue) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(label);
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(defaultValue));
        layout.addView(input);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(0, dp(4), 0, dp(4));
        layout.setLayoutParams(params);
        parent.addView(layout);
        return input;
    }

    protected TextInputEditText numberInput(LinearLayout parent, String label, int defaultValue) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(label);
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(defaultValue));
        layout.addView(input);
        parent.addView(layout);
        return input;
    }

    protected LinearLayout verticalLayout(int spacing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        if (spacing > 0) {
            layout.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        }
        return layout;
    }

    protected TextView title(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(28);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setTextColor(0xFF2B2118);
        return view;
    }

    protected TextView sectionTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(19);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setTextColor(0xFF33261B);
        return view;
    }

    protected TextView bodyText(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(16);
        view.setTextColor(0xFF5F5043);
        return view;
    }

    protected Button primaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    protected Button secondaryButton(String text) {
        Button button = primaryButton(text);
        button.setBackgroundColor(0xFFE4D7C7);
        button.setTextColor(0xFF2B2118);
        return button;
    }

    protected String value(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    protected String valueOrDefault(EditText input, String defaultValue) {
        String value = value(input).trim();
        return value.isEmpty() ? defaultValue : value;
    }

    protected int intValue(EditText input, int defaultValue) {
        try {
            return Integer.parseInt(value(input).trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    protected static long moneyAsCopper(int platinum, int gold, int silver, int copper) {
        return (((long) platinum * 100 + gold) * 100 + silver) * 100 + copper;
    }

    protected int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    // Character persistence: loads all saved characters into the character list.
    protected void loadCharacters() {
        characters.clear();
        for (CharacterData data : characterRepository.loadCharacters()) {
            characters.add(fromDatabase(data));
        }
    }

    // Character persistence: saves the current character list to the repository.
    protected void saveCharacters() {
        for (DndCharacter character : characters) {
            character.databaseId = characterRepository.saveCharacter(toDatabase(character));
        }
    }

    // Character persistence: converts an in-memory character to database data.
    protected static CharacterData toDatabase(DndCharacter character) {
        CharacterData data = new CharacterData();
        data.id = character.databaseId;
        data.name = character.name;
        data.characterClass = character.characterClass;
        data.level = character.level;
        data.strength = character.strength;
        data.dexterity = character.dexterity;
        data.constitution = character.constitution;
        data.intelligence = character.intelligence;
        data.charisma = character.charisma;
        data.wisdom = character.wisdom;
        data.speed = character.speed;
        data.armorClass = character.armorClass;
        data.race = character.race;
        data.background = character.background;
        data.alignment = character.alignment;
        data.currentHp = character.currentHp;
        data.maxHp = character.maxHp;
        data.temporaryHp = character.temporaryHp;
        data.personalityTraits = character.personalityTraits;
        data.ideals = character.ideals;
        data.bonds = character.bonds;
        data.flaws = character.flaws;
        data.platinum = character.platinumCoins;
        data.gold = character.goldCoins;
        data.silver = character.silverCoins;
        data.copper = character.copperCoins;
        data.languages.addAll(character.languages);
        data.savingThrows.addAll(character.savingThrows);
        for (int level = 0; level < character.spellbook.size(); level++) {
            data.spellbook.get(level).addAll(character.spellbook.get(level));
        }
        return data;
    }

    // Character persistence: rebuilds an in-memory character from database data.
    protected static DndCharacter fromDatabase(CharacterData data) {
        DndCharacter character = new DndCharacter(
                data.name, data.characterClass, data.level, data.strength, data.dexterity,
                data.constitution, data.intelligence, data.charisma, data.wisdom, data.speed,
                data.armorClass, data.race, data.background, data.alignment, data.currentHp,
                data.maxHp, data.temporaryHp, data.hitDice, proficiencyBonusForLevel(data.level),
                10 + Math.floorDiv(data.wisdom - 10, 2), "None", data.personalityTraits,
                data.ideals, data.bonds, data.flaws, Math.floorDiv(data.dexterity - 10, 2),
                new ArrayList<>(data.languages), new ArrayList<>(data.savingThrows),
                copySpellbook(data.spellbook), DndCharacter.maxSpellUsesForLevel(data.level),
                DndCharacter.maxSpellUsesForLevel(data.level), data.platinum, data.gold,
                data.silver, data.copper
        );
        character.databaseId = data.id;
        return character;
    }

    protected static int proficiencyBonusForLevel(int level) {
        return 2 + Math.max(0, level - 1) / 4;
    }

    protected static List<List<String>> copySpellbook(List<List<String>> source) {
        List<List<String>> result = new ArrayList<>();
        for (List<String> spells : source) {
            result.add(new ArrayList<>(spells));
        }
        return result;
    }

    protected static class DndCharacter {
        long databaseId;
        final String name;
        final String characterClass;
        final int level;
        final int strength;
        final int dexterity;
        final int constitution;
        final int intelligence;
        final int charisma;
        final int wisdom;
        final int speed;
        final int armorClass;
        final String race;
        final String background;
        final String alignment;
        final int currentHp;
        final int maxHp;
        final int temporaryHp;
        final String hitDice;
        final int proficiencyBonus;
        final int perception;
        final String featuresAndTraits;
        final String personalityTraits;
        final String ideals;
        final String bonds;
        final String flaws;
        final int initiative;
        final List<String> languages;
        final List<String> savingThrows;
        final List<List<String>> spellbook;
        final int maxSpellUses;
        int currentSpellUses;
        int platinumCoins;
        int goldCoins;
        int silverCoins;
        int copperCoins;
        int databaseCharacterId;


        DndCharacter(
                String name,
                String characterClass,
                int level,
                int strength,
                int dexterity,
                int constitution,
                int intelligence,
                int charisma,
                int wisdom,
                int speed,
                int armorClass
        ) {
            this(
                    name,
                    characterClass,
                    level,
                    strength,
                    dexterity,
                    constitution,
                    intelligence,
                    charisma,
                    wisdom,
                    speed,
                    armorClass,
                    "Not specified",
                    "Not specified",
                    "Not specified",
                    10,
                    10,
                    0,
                    "d8",
                    2,
                    10,
                    "None",
                    "None",
                    "None",
                    "None",
                    "None",
                    0,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    createEmptySpellbook(),
                    maxSpellUsesForLevel(level),
                    maxSpellUsesForLevel(level),
                    0,
                    0,
                    0,
                    0
            );
        }

        DndCharacter(
                String name,
                String characterClass,
                int level,
                int strength,
                int dexterity,
                int constitution,
                int intelligence,
                int charisma,
                int wisdom,
                int speed,
                int armorClass,
                String race,
                String background,
                String alignment,
                int currentHp,
                int maxHp,
                int temporaryHp,
                String hitDice,
                int proficiencyBonus,
                int perception,
                String featuresAndTraits,
                String personalityTraits,
                String ideals,
                String bonds,
                String flaws,
                int initiative,
                List<String> languages,
                List<String> savingThrows
        ) {
            this(
                    name,
                    characterClass,
                    level,
                    strength,
                    dexterity,
                    constitution,
                    intelligence,
                    charisma,
                    wisdom,
                    speed,
                    armorClass,
                    race,
                    background,
                    alignment,
                    currentHp,
                    maxHp,
                    temporaryHp,
                    hitDice,
                    proficiencyBonus,
                    perception,
                    featuresAndTraits,
                    personalityTraits,
                    ideals,
                    bonds,
                    flaws,
                    initiative,
                    languages,
                    savingThrows,
                    createEmptySpellbook(),
                    maxSpellUsesForLevel(level),
                    maxSpellUsesForLevel(level),
                    0,
                    0,
                    0,
                    0
            );

        }

        DndCharacter(
                String name,
                String characterClass,
                int level,
                int strength,
                int dexterity,
                int constitution,
                int intelligence,
                int charisma,
                int wisdom,
                int speed,
                int armorClass,
                String race,
                String background,
                String alignment,
                int currentHp,
                int maxHp,
                int temporaryHp,
                String hitDice,
                int proficiencyBonus,
                int perception,
                String featuresAndTraits,
                String personalityTraits,
                String ideals,
                String bonds,
                String flaws,
                int initiative,
                List<String> languages,
                List<String> savingThrows,
                List<List<String>> spellbook
        ) {
            this(
                    name,
                    characterClass,
                    level,
                    strength,
                    dexterity,
                    constitution,
                    intelligence,
                    charisma,
                    wisdom,
                    speed,
                    armorClass,
                    race,
                    background,
                    alignment,
                    currentHp,
                    maxHp,
                    temporaryHp,
                    hitDice,
                    proficiencyBonus,
                    perception,
                    featuresAndTraits,
                    personalityTraits,
                    ideals,
                    bonds,
                    flaws,
                    initiative,
                    languages,
                    savingThrows,
                    spellbook,
                    maxSpellUsesForLevel(level),
                    maxSpellUsesForLevel(level),
                    0,
                    0,
                    0,
                    0
            );

        }

        DndCharacter(
                String name,
                String characterClass,
                int level,
                int strength,
                int dexterity,
                int constitution,

                int intelligence,
                int charisma,
                int wisdom,
                int speed,
                int armorClass,
                String race,
                String background,
                String alignment,
                int currentHp,
                int maxHp,
                int temporaryHp,
                String hitDice,
                int proficiencyBonus,
                int perception,
                String featuresAndTraits,
                String personalityTraits,
                String ideals,
                String bonds,
                String flaws,
                int initiative,
                List<String> languages,
                List<String> savingThrows,
                List<List<String>> spellbook,
                int maxSpellUses,
                int currentSpellUses,
                int platinumCoins,
                int goldCoins,
                int silverCoins,
                int copperCoins

        ) {
            this.name = name;
            this.characterClass = characterClass;
            this.level = level;
            this.strength = strength;
            this.dexterity = dexterity;
            this.constitution = constitution;
            this.intelligence = intelligence;
            this.charisma = charisma;
            this.wisdom = wisdom;
            this.speed = speed;
            this.armorClass = armorClass;
            this.race = race;
            this.background = background;
            this.alignment = alignment;
            this.currentHp = currentHp;
            this.maxHp = maxHp;
            this.temporaryHp = temporaryHp;
            this.hitDice = hitDice;
            this.proficiencyBonus = proficiencyBonus;
            this.perception = perception;
            this.featuresAndTraits = featuresAndTraits;
            this.personalityTraits = personalityTraits;
            this.ideals = ideals;
            this.bonds = bonds;
            this.flaws = flaws;
            this.initiative = initiative;
            this.languages = languages;
            this.savingThrows = savingThrows;
            this.spellbook = spellbook;
            this.maxSpellUses = maxSpellUses;
            this.currentSpellUses = Math.min(currentSpellUses, maxSpellUses);
            setMoneyFromCopper(BaseDndActivity.moneyAsCopper(platinumCoins, goldCoins, silverCoins, copperCoins));

        }


        protected static int maxSpellUsesForLevel(int level) {
            if (level >= 17) {
                return 14;
            } else if (level >= 13) {
                return 12;
            } else if (level >= 9) {
                return 10;
            } else if (level >= 5) {
                return 6;
            } else if (level >= 3) {
                return 4;
            }
            return 2;
        }

        long moneyAsCopper() {
            return BaseDndActivity.moneyAsCopper(platinumCoins, goldCoins, silverCoins, copperCoins);
        }

        void setMoneyFromCopper(long totalCopper) {
            long normalizedTotal = Math.max(0, totalCopper);
            platinumCoins = (int) (normalizedTotal / 1_000_000L);
            normalizedTotal %= 1_000_000L;
            goldCoins = (int) (normalizedTotal / 10_000L);
            normalizedTotal %= 10_000L;
            silverCoins = (int) (normalizedTotal / 100L);
            copperCoins = (int) (normalizedTotal % 100L);
        }

        protected static List<List<String>> createEmptySpellbook() {
            List<List<String>> spellbook = new ArrayList<>();
            for (int level = 0; level <= 9; level++) {
                spellbook.add(new ArrayList<>());
            }
            return spellbook;
        }

    }

    protected static class AspectRatioFrameLayout extends FrameLayout {
        AspectRatioFrameLayout(@NonNull android.content.Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = Math.round(width * EQUIPMENT_IMAGE_ASPECT_RATIO);
            int exactHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, exactHeight);
        }
    }
}

