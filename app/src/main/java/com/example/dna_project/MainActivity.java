package com.example.dna_project;

import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "dnd_characters";
    private static final String KEY_CHARACTERS = "characters";
    private static final int MAX_CHARACTERS = 15;
    private static final int TOTAL_ABILITY_POINTS = 75;

    private static final int TAB_INVENTORY = 1;
    private static final int TAB_STATS = 2;
    private static final int TAB_SPELLBOOK = 3;
    private static final float EQUIPMENT_IMAGE_ASPECT_RATIO = 1754f / 1284f;
    private static final float EQUIPMENT_DESIGN_WIDTH = 1284f;
    private static final float EQUIPMENT_DESIGN_HEIGHT = 1754f;
    private static final int INVENTORY_COLUMNS = 7;
    private static final String[] INVENTORY_CATEGORIES = {
            "Weapons",
            "Armor",
            "Accessories",
            "Tools",
            "Materials",
            "Other",
            "Quest Items"
    };
    private static final String[] INVENTORY_ITEM_TYPES = {
            InventoryRepository.TYPE_WEAPON,
            InventoryRepository.TYPE_ARMOR,
            InventoryRepository.TYPE_ACCESSORIES,
            InventoryRepository.TYPE_INSTRUMENTS,
            InventoryRepository.TYPE_MATERIAL,
            InventoryRepository.TYPE_OTHER,
            InventoryRepository.TYPE_QUEST
    };
    private static final int[] INVENTORY_CATEGORY_ICONS = {
            R.drawable.tab_weapon,
            R.drawable.tab_armor,
            R.drawable.tab_accessories,
            R.drawable.tab_instruments,
            R.drawable.tab_materials,
            R.drawable.tab_other,
            R.drawable.tab_key_items
    };
    private static final String[] CHARACTER_CLASSES = {
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
    private static final SpellDefinition[] SPELL_LIBRARY = {
            new SpellDefinition("Fire Bolt", 0, "Wizard,Sorcerer,Artificer", "Ranged spell attack. Deals fire damage. Cantrip, does not spend spell uses."),
            new SpellDefinition("Mage Hand", 0, "Wizard,Sorcerer,Warlock,Bard,Artificer", "Creates a spectral hand for simple interactions. Cantrip, does not spend spell uses."),
            new SpellDefinition("Guidance", 0, "Cleric,Druid,Artificer", "Adds 1d4 to one ability check. Cantrip, does not spend spell uses."),
            new SpellDefinition("Sacred Flame", 0, "Cleric", "Radiant flame forces a Dexterity save. Cantrip, does not spend spell uses."),
            new SpellDefinition("Magic Missile", 1, "Wizard,Sorcerer", "Three darts automatically hit targets and deal force damage."),
            new SpellDefinition("Cure Wounds", 1, "Cleric,Druid,Bard,Paladin,Ranger,Artificer", "Touch a creature to restore hit points."),
            new SpellDefinition("Shield", 1, "Wizard,Sorcerer,Artificer", "Reaction. Gain +5 AC until the start of your next turn."),
            new SpellDefinition("Thunderwave", 1, "Wizard,Sorcerer,Bard,Druid", "A wave of thunder pushes creatures and deals thunder damage."),
            new SpellDefinition("Misty Step", 2, "Wizard,Sorcerer,Warlock", "Bonus action teleport up to 30 feet."),
            new SpellDefinition("Scorching Ray", 2, "Wizard,Sorcerer", "Create three fire rays and make ranged spell attacks."),
            new SpellDefinition("Lesser Restoration", 2, "Cleric,Druid,Bard,Paladin,Ranger,Artificer", "End one disease or condition on a creature."),
            new SpellDefinition("Fireball", 3, "Wizard,Sorcerer", "A bright explosion deals fire damage in a large area."),
            new SpellDefinition("Counterspell", 3, "Wizard,Sorcerer,Warlock", "Reaction. Interrupt a creature casting a spell."),
            new SpellDefinition("Revivify", 3, "Cleric,Paladin,Artificer", "Return a recently dead creature to life."),
            new SpellDefinition("Polymorph", 4, "Wizard,Sorcerer,Bard,Druid", "Transform a creature into a beast."),
            new SpellDefinition("Wall of Fire", 4, "Wizard,Sorcerer,Druid", "Create a wall of flame that deals fire damage."),
            new SpellDefinition("Cone of Cold", 5, "Wizard,Sorcerer", "A blast of cold air deals cold damage in a cone."),
            new SpellDefinition("Mass Cure Wounds", 5, "Cleric,Druid,Bard", "Restore hit points to several creatures at once."),
            new SpellDefinition("Disintegrate", 6, "Wizard,Sorcerer", "A green ray deals heavy force damage."),
            new SpellDefinition("Heal", 6, "Cleric,Druid", "A creature regains a large amount of hit points."),
            new SpellDefinition("Teleport", 7, "Wizard,Sorcerer,Bard", "Instantly transport yourself and companions."),
            new SpellDefinition("Power Word Stun", 8, "Wizard,Sorcerer,Bard,Warlock", "Stun a creature with 150 hit points or fewer."),
            new SpellDefinition("Wish", 9, "Wizard,Sorcerer", "The mightiest spell, capable of reshaping reality.")
    };
    private static final String[] RACE_OPTIONS = {
            "Human",
            "Dwarf",
            "Elf",
            "Halfling",
            "Gnome",
            "Half-Elf",
            "Half-Orc",
            "Tiefling",
            "Dragonborn"
    };
    private static final String[] BACKGROUND_OPTIONS = {
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
            "Retainer",
            "Soldier",
            "Outlander",
            "Charlatan"
    };
    private static final String[] ALIGNMENT_OPTIONS = {
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
    private static final String[] PERSONALITY_TRAIT_OPTIONS = {
            "I am always polite and respectful",
            "I trust my friends and protect them",
            "I am used to seeking profit in any situation",
            "I speak plainly, even when it is unpleasant",
            "I stay calm in the face of danger",
            "I love beautiful stories and glorious deeds",
            "I find it hard to trust strangers",
            "I am always seeking new knowledge"
    };
    private static final String[] IDEAL_OPTIONS = {
            "Good",
            "Freedom",
            "Justice",
            "Honor",
            "Knowledge",
            "Power",
            "Tradition",
            "Redemption"
    };
    private static final String[] BOND_OPTIONS = {
            "I protect my family",
            "I serve my people",
            "I owe my life to an old friend",
            "I seek a lost relic",
            "I must avenge the past",
            "I keep my mentor's secret",
            "I want to restore my lost honor",
            "I am bound by oath to an order"
    };
    private static final String[] FLAW_OPTIONS = {
            "I am too trusting",
            "I am greedy for gold",
            "I am hot-tempered",
            "I fear losing control",
            "I often underestimate danger",
            "I envy the fame of others",
            "I have trouble admitting mistakes",
            "I easily give in to temptation"
    };
    private static final String[] LANGUAGE_OPTIONS = {
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
            "Half-Elvish",
            "Halfling",
            "Sylvan",
            "Tiefling",
            "Aquan"
    };
    private static final String[][] SAVING_THROW_GROUPS = {
            {"Strength", "Saving Throw (Strength)\nAthletics"},
            {"Dexterity", "Saving Throw (Dexterity)\nAcrobatics\nSleight of Hand\nStealth"},
            {"Constitution", "Saving Throw (Constitution)"},
            {"Intelligence", "Saving Throw (Intelligence)\nArcana\nHistory\nInvestigation\nNature\nReligion"},
            {"Wisdom", "Saving Throw (Wisdom)\nAnimal Handling\nInsight\nMedicine\nPerception\nSurvival"},
            {"Charisma", "Saving Throw (Charisma)\nDeception\nIntimidation\nPerformance\nPersuasion"}
    };
    private static final String[] SAVING_THROW_OPTIONS = {
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

    private final List<DndCharacter> characters = new ArrayList<>();
    private SharedPreferences preferences;
    private DndProjectDatabaseHelper projectDatabase;
    private FrameLayout root;
    private DndCharacter selectedCharacter;
    private int selectedTab = TAB_STATS;
    private int selectedInventoryCategory = 0;
    private InventoryRepository inventoryRepository;
    private InventoryState inventoryState;
    private List<InventoryItem> visibleInventorySlots = new ArrayList<>();
    private Map<String, InventoryItem> equippedItems = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inventoryRepository = new InventoryRepository(this);
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        projectDatabase = new DndProjectDatabaseHelper(getApplicationContext());
        projectDatabase.ensureReady();
        root = new FrameLayout(this);
        root.setBackgroundResource(R.drawable.dnd_screen_bg);
        setContentView(root);
        loadCharacters();
        showCharacterSelect();
    }

    private void showCharacterSelect() {
        selectedCharacter = null;
        selectedTab = TAB_STATS;
        root.removeAllViews();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(dp(12), dp(12), dp(12), dp(12));
        scrollView.setClipToPadding(false);
        LinearLayout screen = verticalLayout(20);
        screen.setPadding(dp(20), dp(28), dp(20), dp(28));
        screen.setBackgroundResource(R.drawable.dnd_panel_bg);
        scrollView.addView(screen);

        TextView title = title("Character Selection");
        screen.addView(title);

        TextView counter = bodyText("Characters: " + characters.size() + " / " + MAX_CHARACTERS);
        counter.setPadding(0, dp(4), 0, dp(16));
        screen.addView(counter);

        Button createButton = primaryButton("Create New Character");
        createButton.setEnabled(characters.size() < MAX_CHARACTERS);
        createButton.setOnClickListener(view -> {
            if (characters.size() >= MAX_CHARACTERS) {
                Toast.makeText(this, "You can create a maximum of 15 characters", Toast.LENGTH_SHORT).show();
            } else {
                showCreateCharacter();
            }
        });
        screen.addView(createButton);

        if (characters.isEmpty()) {
            TextView empty = bodyText("There are no characters yet. Create your first hero for the campaign.");
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(40), 0, 0);
            screen.addView(empty);
        } else {
            for (DndCharacter character : characters) {
                screen.addView(characterRow(character));
            }
        }

        root.addView(scrollView);
    }

    private void showCreateCharacter() {
        root.removeAllViews();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(dp(12), dp(12), dp(12), dp(12));
        scrollView.setClipToPadding(false);
        LinearLayout screen = verticalLayout(14);
        screen.setPadding(dp(20), dp(28), dp(20), dp(28));
        screen.setBackgroundResource(R.drawable.dnd_panel_bg);
        scrollView.addView(screen);

        screen.addView(title("New Character"));

        List<ClassOption> classOptions = projectDatabase.getClasses();
        List<DbOption> raceOptions = projectDatabase.getOptions("race");
        List<DbOption> backgroundOptions = projectDatabase.getOptions("background");
        List<DbOption> languageOptions = projectDatabase.getOptions("language");
        List<DbOption> proficiencyOptions = projectDatabase.getOptions("proficiency");
        List<DbOption> alignmentOptions = projectDatabase.getCharacterDetailOptions("alignment");
        List<DbOption> personalityTraitOptions = projectDatabase.getCharacterDetailOptions("personality_traits");
        List<DbOption> idealOptions = projectDatabase.getCharacterDetailOptions("ideals");
        List<DbOption> bondOptions = projectDatabase.getCharacterDetailOptions("bonds");
        List<DbOption> flawOptions = projectDatabase.getCharacterDetailOptions("flaws");

        TextInputEditText nameInput = textInput(screen, "Character Name", "");
        TextView selectedClass = bodyText("No class selected");
        selectedClass.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedClass.setPadding(0, dp(4), 0, dp(4));
        Button classButton = secondaryButton("Add Class");
        final String[] classValue = {""};
        final int[] classIdValue = {0};
        final int[] selectedHitDice = {0};
        screen.addView(selectedClass);
        screen.addView(classButton);

        TextView levelLabel = bodyText("Level");
        levelLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        levelLabel.setPadding(0, dp(4), 0, dp(4));
        screen.addView(levelLabel);
        TextInputEditText level = numberInput(screen, "Level", 1);
        TextView selectedRace = bodyText("No race selected");
        selectedRace.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedRace.setPadding(0, dp(4), 0, dp(4));
        Button raceButton = secondaryButton("Choose Race");
        final String[] raceValue = {""};
        final int[] raceIdValue = {0};
        raceButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Choose a Race")
                .setItems(optionNames(raceOptions), (dialog, which) -> {
                    DbOption option = raceOptions.get(which);
                    raceIdValue[0] = option.id;
                    raceValue[0] = option.name;
                    selectedRace.setText("Race: " + raceValue[0]);
                })
                .show());
        screen.addView(selectedRace);
        screen.addView(raceButton);

        TextView selectedBackground = bodyText("No background selected");
        selectedBackground.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedBackground.setPadding(0, dp(4), 0, dp(4));
        Button backgroundButton = secondaryButton("Choose Background");
        final String[] backgroundValue = {""};
        final int[] backgroundIdValue = {0};
        backgroundButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Choose a Background")
                .setItems(optionNames(backgroundOptions), (dialog, which) -> {
                    DbOption option = backgroundOptions.get(which);
                    backgroundIdValue[0] = option.id;
                    backgroundValue[0] = option.name;
                    selectedBackground.setText("Background: " + backgroundValue[0]);
                })
                .show());
        screen.addView(selectedBackground);
        screen.addView(backgroundButton);

        TextView selectedAlignment = bodyText("No alignment selected");
        selectedAlignment.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedAlignment.setPadding(0, dp(4), 0, dp(4));
        Button alignmentButton = secondaryButton("Choose Alignment");
        final String[] alignmentValue = {""};
        alignmentButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Choose an Alignment")
                .setItems(optionNames(alignmentOptions), (dialog, which) -> {
                    alignmentValue[0] = alignmentOptions.get(which).name;
                    selectedAlignment.setText("Alignment: " + alignmentValue[0]);
                })
                .show());
        screen.addView(selectedAlignment);
        screen.addView(alignmentButton);

        TextView selectedPersonalityTraits = bodyText("No personality traits selected");
        selectedPersonalityTraits.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedPersonalityTraits.setPadding(0, dp(4), 0, dp(4));
        Button personalityTraitsButton = secondaryButton("Choose Personality Traits");
        final String[] personalityTraitsValue = {""};
        personalityTraitsButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Choose Personality Traits")
                .setItems(optionNames(personalityTraitOptions), (dialog, which) -> {
                    personalityTraitsValue[0] = personalityTraitOptions.get(which).name;
                    selectedPersonalityTraits.setText("Personality Traits: " + personalityTraitsValue[0]);
                })
                .show());
        screen.addView(selectedPersonalityTraits);
        screen.addView(personalityTraitsButton);

        TextView selectedIdeals = bodyText("No ideals selected");
        selectedIdeals.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedIdeals.setPadding(0, dp(4), 0, dp(4));
        Button idealsButton = secondaryButton("Choose Ideals");
        final String[] idealsValue = {""};
        idealsButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Choose Ideals")
                .setItems(optionNames(idealOptions), (dialog, which) -> {
                    idealsValue[0] = idealOptions.get(which).name;
                    selectedIdeals.setText("Ideals: " + idealsValue[0]);
                })
                .show());
        screen.addView(selectedIdeals);
        screen.addView(idealsButton);

        TextView selectedBonds = bodyText("No bonds selected");
        selectedBonds.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedBonds.setPadding(0, dp(4), 0, dp(4));
        Button bondsButton = secondaryButton("Choose Bonds");
        final String[] bondsValue = {""};
        bondsButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Choose Bonds")
                .setItems(optionNames(bondOptions), (dialog, which) -> {
                    bondsValue[0] = bondOptions.get(which).name;
                    selectedBonds.setText("Bonds: " + bondsValue[0]);
                })
                .show());
        screen.addView(selectedBonds);
        screen.addView(bondsButton);

        TextView selectedFlaws = bodyText("No flaws selected");
        selectedFlaws.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedFlaws.setPadding(0, dp(4), 0, dp(4));
        Button flawsButton = secondaryButton("Choose Flaws");
        final String[] flawsValue = {""};
        flawsButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Choose Flaws")
                .setItems(optionNames(flawOptions), (dialog, which) -> {
                    flawsValue[0] = flawOptions.get(which).name;
                    selectedFlaws.setText("Flaws: " + flawsValue[0]);
                })
                .show());
        screen.addView(selectedFlaws);
        screen.addView(flawsButton);
        TextView selectedLanguagesLabel = bodyText("No languages selected");
        selectedLanguagesLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedLanguagesLabel.setPadding(0, dp(4), 0, dp(4));
        Button languagesButton = secondaryButton("Choose Languages");
        final boolean[] languageSelections = new boolean[languageOptions.size()];
        languagesButton.setOnClickListener(view -> {
            boolean[] dialogSelections = languageSelections.clone();
            new AlertDialog.Builder(this)
                    .setTitle("Choose Languages")
                    .setMultiChoiceItems(optionNames(languageOptions), dialogSelections, (dialog, which, isChecked) ->
                            dialogSelections[which] = isChecked)
                    .setPositiveButton("Done", (dialog, which) -> {
                        System.arraycopy(dialogSelections, 0, languageSelections, 0, languageSelections.length);
                        selectedLanguagesLabel.setText("Languages: " + selectionSummary(collectSelectedNames(languageOptions, languageSelections)));
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        screen.addView(selectedLanguagesLabel);
        screen.addView(languagesButton);
        TextView selectedSavingThrowsLabel = bodyText("No saving throws selected");
        selectedSavingThrowsLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedSavingThrowsLabel.setPadding(0, dp(4), 0, dp(4));
        Button savingThrowsButton = secondaryButton("Choose Saving Throws");
        final boolean[] savingThrowSelections = new boolean[proficiencyOptions.size()];
        savingThrowsButton.setOnClickListener(view -> {
            boolean[] dialogSelections = savingThrowSelections.clone();
            new AlertDialog.Builder(this)
                    .setTitle("Choose Saving Throws")
                    .setMultiChoiceItems(optionNames(proficiencyOptions), dialogSelections, (dialog, which, isChecked) ->
                            dialogSelections[which] = isChecked)
                    .setPositiveButton("Done", (dialog, which) -> {
                        System.arraycopy(dialogSelections, 0, savingThrowSelections, 0, savingThrowSelections.length);
                        selectedSavingThrowsLabel.setText("Saving Throws: " + selectionSummary(collectSelectedNames(proficiencyOptions, savingThrowSelections)));
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        screen.addView(selectedSavingThrowsLabel);
        screen.addView(savingThrowsButton);

        TextView abilitiesLabel = bodyText("Ability Scores");
        abilitiesLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        abilitiesLabel.setPadding(0, dp(4), 0, dp(4));
        screen.addView(abilitiesLabel);

        final int[] abilityValues = new int[6];
        final int[] remainingAbilityPoints = {TOTAL_ABILITY_POINTS};
        final int[] perceptionValue = {10};
        final boolean[] abilityTouched = {false};
        List<TextView> abilityValueViews = new ArrayList<>();
        List<Button> abilityButtons = new ArrayList<>();
        TextView remainingAbilityPointsLabel = bodyText("Free Points: " + remainingAbilityPoints[0]);
        remainingAbilityPointsLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        remainingAbilityPointsLabel.setPadding(0, dp(4), 0, dp(12));

        GridLayout abilityGrid = new GridLayout(this);
        abilityGrid.setColumnCount(2);
        abilityGrid.setUseDefaultMargins(true);
        screen.addView(abilityGrid);

        addAbilityControl(abilityGrid, "Strength", abilityValues, 0, remainingAbilityPoints, remainingAbilityPointsLabel, abilityValueViews, abilityButtons, abilityTouched);
        addAbilityControl(abilityGrid, "Dexterity", abilityValues, 1, remainingAbilityPoints, remainingAbilityPointsLabel, abilityValueViews, abilityButtons, abilityTouched);
        addAbilityControl(abilityGrid, "Constitution", abilityValues, 2, remainingAbilityPoints, remainingAbilityPointsLabel, abilityValueViews, abilityButtons, abilityTouched);
        addAbilityControl(abilityGrid, "Intelligence", abilityValues, 3, remainingAbilityPoints, remainingAbilityPointsLabel, abilityValueViews, abilityButtons, abilityTouched);
        addAbilityControl(abilityGrid, "Charisma", abilityValues, 4, remainingAbilityPoints, remainingAbilityPointsLabel, abilityValueViews, abilityButtons, abilityTouched);
        addAbilityControl(abilityGrid, "Wisdom", abilityValues, 5, remainingAbilityPoints, remainingAbilityPointsLabel, abilityValueViews, abilityButtons, abilityTouched);
        addCounterControl(abilityGrid, "Perception", perceptionValue, 0, abilityButtons);
        setButtonsEnabled(abilityButtons, false);
        screen.addView(remainingAbilityPointsLabel);

        GridLayout statGrid = new GridLayout(this);
        statGrid.setColumnCount(2);
        statGrid.setUseDefaultMargins(true);
        screen.addView(statGrid);

        TextInputEditText speed = numberInput(statGrid, "Speed", 30);
        TextInputEditText armorClass = numberInput(statGrid, "Armor Class", 10);
        TextInputEditText currentHp = numberInput(statGrid, "Current HP", 10);
        TextInputEditText maxHp = numberInput(statGrid, "Max HP", 10);
        TextInputEditText temporaryHp = numberInput(statGrid, "Temporary HP", 0);
        TextInputEditText proficiencyBonus = numberInput(statGrid, "Proficiency Bonus", 2);
        TextInputEditText initiative = numberInput(statGrid, "Initiative", 0);

        TextView hitDice = bodyText("Hit Dice: -");
        hitDice.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        hitDice.setPadding(0, dp(4), 0, dp(4));
        screen.addView(hitDice);
        TextInputEditText featuresAndTraits = multilineTextInput(screen, "Features and Traits", "");

        Button saveButton = primaryButton("Create Character");
        saveButton.setEnabled(false);
        classButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Choose a Class")
                .setItems(classNames(classOptions), (dialog, which) -> {
                    ClassOption option = classOptions.get(which);
                    Runnable applySelection = () -> applyClassSelection(
                            option,
                            classValue,
                            classIdValue,
                            selectedHitDice,
                            selectedClass,
                            hitDice,
                            abilityValues,
                            remainingAbilityPoints,
                            remainingAbilityPointsLabel,
                            abilityValueViews,
                            abilityButtons,
                            abilityTouched,
                            saveButton
                    );

                    if (classIdValue[0] != 0 && classIdValue[0] != option.id && abilityTouched[0]) {
                        new AlertDialog.Builder(this)
                                .setTitle("Change class?")
                                .setMessage("Changing class will reset ability scores to the new class preset.")
                                .setPositiveButton("Change", (confirmDialog, confirmWhich) -> applySelection.run())
                                .setNegativeButton("Cancel", null)
                                .show();
                    } else {
                        applySelection.run();
                    }
                })
                .show());
        saveButton.setOnClickListener(view -> {
            String name = value(nameInput).trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter Character Name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (classIdValue[0] == 0) {
                Toast.makeText(this, "Choose a class before creating a character", Toast.LENGTH_SHORT).show();
                return;
            }
            if (abilitySum(abilityValues) > TOTAL_ABILITY_POINTS) {
                Toast.makeText(this, "Ability score total cannot exceed " + TOTAL_ABILITY_POINTS, Toast.LENGTH_SHORT).show();
                return;
            }

            CharacterCreateData data = new CharacterCreateData();
            data.name = name;
            data.level = intValue(level, 1);
            data.classId = classIdValue[0];
            data.raceId = raceIdValue[0];
            data.backgroundId = backgroundIdValue[0];
            data.alignment = alignmentValue[0].isEmpty() ? "Not specified" : alignmentValue[0];
            data.personalityTraits = personalityTraitsValue[0].isEmpty() ? "None" : personalityTraitsValue[0];
            data.ideals = idealsValue[0].isEmpty() ? "None" : idealsValue[0];
            data.bonds = bondsValue[0].isEmpty() ? "None" : bondsValue[0];
            data.flaws = flawsValue[0].isEmpty() ? "None" : flawsValue[0];
            data.strength = abilityValues[0];
            data.dexterity = abilityValues[1];
            data.constitution = abilityValues[2];
            data.intelligence = abilityValues[3];
            data.charisma = abilityValues[4];
            data.wisdom = abilityValues[5];
            data.languageIds = collectSelectedIds(languageOptions, languageSelections);
            data.proficiencyIds = collectSelectedIds(proficiencyOptions, savingThrowSelections);

            try {
                projectDatabase.createCharacter(data);
            } catch (RuntimeException exception) {
                Toast.makeText(this, "Could not create character in database", Toast.LENGTH_SHORT).show();
                return;
            }

            DndCharacter character = new DndCharacter(
                    name,
                    classValue[0],
                    data.level,
                    abilityValues[0],
                    abilityValues[1],
                    abilityValues[2],
                    abilityValues[3],
                    abilityValues[4],
                    abilityValues[5],
                    intValue(speed, 30),
                    intValue(armorClass, 10),
                    raceValue[0].isEmpty() ? "Not specified" : raceValue[0],
                    backgroundValue[0].isEmpty() ? "Not specified" : backgroundValue[0],
                    data.alignment,
                    intValue(currentHp, 10),
                    intValue(maxHp, 10),
                    intValue(temporaryHp, 0),
                    selectedHitDice[0] == 0 ? "-" : "d" + selectedHitDice[0],
                    intValue(proficiencyBonus, 2),
                    perceptionValue[0],
                    valueOrDefault(featuresAndTraits, "None"),
                    data.personalityTraits,
                    data.ideals,
                    data.bonds,
                    data.flaws,
                    intValue(initiative, 0),
                    collectSelectedNames(languageOptions, languageSelections),
                    collectSelectedNames(proficiencyOptions, savingThrowSelections)
            );
            characters.add(character);
            saveCharacters();
            selectedCharacter = character;
            selectedTab = TAB_STATS;
            selectedInventoryCategory = 0;
            showCharacterSheet();
        });
        screen.addView(saveButton);

        Button backButton = secondaryButton("Back to Selection");
        backButton.setOnClickListener(view -> showCharacterSelect());
        screen.addView(backButton);

        root.addView(scrollView);
    }

    private void showCharacterSheet() {
        if (selectedCharacter == null) {
            showCharacterSelect();
            return;
        }

        root.removeAllViews();
        LinearLayout screen = verticalLayout(0);

        FrameLayout content = new FrameLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        screen.addView(content, contentParams);

        BottomNavigationView navigation = new BottomNavigationView(this);
        navigation.getMenu().add(0, TAB_INVENTORY, 0, "Bag").setIcon(android.R.drawable.ic_menu_agenda);
        navigation.getMenu().add(0, TAB_STATS, 1, "Stats").setIcon(android.R.drawable.ic_menu_info_details);
        navigation.getMenu().add(0, TAB_SPELLBOOK, 2, "Spells").setIcon(android.R.drawable.ic_menu_upload);
        navigation.setSelectedItemId(selectedTab);
        navigation.setOnItemSelectedListener(item -> {
            selectedTab = item.getItemId();
            renderTab(content);
            return true;
        });
        screen.addView(navigation, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(72)
        ));

        Button selectOther = secondaryButton("Change Character");
        selectOther.setOnClickListener(view -> showCharacterSelect());
        screen.addView(selectOther, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        root.addView(screen);
        renderTab(content);
    }

    private void renderTab(FrameLayout content) {
        content.removeAllViews();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(dp(12), dp(12), dp(12), dp(12));
        scrollView.setClipToPadding(false);
        LinearLayout body = verticalLayout(14);
        body.setPadding(dp(20), dp(12), dp(20), dp(24));
        body.setBackgroundResource(R.drawable.dnd_panel_bg);
        scrollView.addView(body);

        if (selectedTab == TAB_STATS) {
            body.setBackgroundResource(R.drawable.dnd_character_sheet_bg);
            addSheetHeader(body);
            addIdentitySection(body);
            addCombatSection(body);
            addAbilitySection(body, selectedCharacter.savingThrows);
            addCharacterNotesSection(body);
            addLanguagesTable(body, selectedCharacter.languages);
        } else if (selectedTab == TAB_INVENTORY) {
            try {
                prepareInventoryTab();
                addEquipmentLayout(body);
                addBackpackHeader(body);
                addBackpackLayout(body, content);
            } catch (RuntimeException exception) {
                body.addView(sectionTitle("Inventory unavailable"));
                body.addView(bodyText(exception.getMessage() == null ? "Unable to open inventory database." : exception.getMessage()));
            }
        } else {
            body.addView(sectionTitle("Spellbook"));
            Button addSpell = primaryButton("Add Spell");
            body.addView(bodyText("Spell uses"));
            addSpellUseCells(body);
            LinearLayout restButtons = new LinearLayout(this);
            restButtons.setOrientation(LinearLayout.HORIZONTAL);
            Button shortRest = secondaryButton("Short rest");
            shortRest.setOnClickListener(view -> restoreSpellUses("Short rest"));
            Button longRest = secondaryButton("Long rest");
            longRest.setOnClickListener(view -> restoreSpellUses("Long rest"));
            restButtons.addView(shortRest, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            restButtons.addView(longRest, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            body.addView(restButtons);
            addSpell.setOnClickListener(view -> showAddSpellDialog(-1));
            body.addView(addSpell);
            addSpellLevels(body);
        }

        content.addView(scrollView);
    }

    private void prepareInventoryTab() {
        if (selectedCharacter == null) {
            return;
        }
        int characterId = inventoryRepository.ensureCharacter(
                selectedCharacter.name,
                selectedCharacter.characterClass,
                selectedCharacter.level,
                selectedCharacter.databaseCharacterId
        );
        if (selectedCharacter.databaseCharacterId != characterId) {
            selectedCharacter.databaseCharacterId = characterId;
            saveCharacters();
        }
        inventoryState = inventoryRepository.getInventoryForCharacter(characterId);
        selectedCharacter.platinumCoins = inventoryState.platinum;
        selectedCharacter.goldCoins = inventoryState.gold;
        selectedCharacter.silverCoins = inventoryState.silver;
        selectedCharacter.copperCoins = inventoryState.bronze;
        equippedItems = inventoryRepository.getEquippedItems(inventoryState.equipmentId);
        visibleInventorySlots = buildVisibleInventorySlots();
    }

    private List<InventoryItem> buildVisibleInventorySlots() {
        List<InventoryItem> realItems = inventoryRepository.getBagItemsByType(
                inventoryState.bagId,
                activeInventoryItemType(),
                inventoryState.equipmentId
        );
        int visibleRows = (realItems.size() / INVENTORY_COLUMNS) + 1;
        int visibleSlots = visibleRows * INVENTORY_COLUMNS;
        List<InventoryItem> slots = new ArrayList<>(visibleSlots);
        slots.addAll(realItems);
        while (slots.size() < visibleSlots) {
            slots.add(null);
        }
        return slots;
    }

    private String activeInventoryItemType() {
        return INVENTORY_ITEM_TYPES[selectedInventoryCategory];
    }

    private void addBackpackHeader(LinearLayout parent) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = sectionTitle("In Backpack");
        header.addView(title, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        LinearLayout moneyButton = new LinearLayout(this);
        moneyButton.setOrientation(LinearLayout.HORIZONTAL);
        moneyButton.setGravity(Gravity.CENTER);
        moneyButton.setPadding(dp(8), 0, dp(8), 0);
        moneyButton.setBackgroundColor(0xFFE4D7C7);
        moneyButton.setOnClickListener(view -> showMoneyDialog());

        TextView plus = bodyText("+");
        plus.setTextSize(22);
        plus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        plus.setGravity(Gravity.CENTER);
        moneyButton.addView(plus, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        ImageView coinIcon = new ImageView(this);
        coinIcon.setImageResource(R.drawable.coin_platinum);
        coinIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        moneyButton.addView(coinIcon, new LinearLayout.LayoutParams(dp(24), dp(24)));

        LinearLayout.LayoutParams moneyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(40)
        );
        moneyParams.setMargins(dp(8), 0, dp(6), 0);
        header.addView(moneyButton, moneyParams);

        Button addItemButton = secondaryButton("Add Item");
        addItemButton.setOnClickListener(view -> showAddItemDialog(InventoryRepository.TYPE_ALL));
        header.addView(addItemButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(40)
        ));

        parent.addView(header, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }

    private void showMoneyDialog() {
        LinearLayout dialogBody = verticalLayout(10);
        dialogBody.setPadding(dp(16), dp(8), dp(16), 0);

        LinearLayout firstRow = new LinearLayout(this);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);
        firstRow.setGravity(Gravity.CENTER_VERTICAL);
        EditText platinumInput = addMoneyInput(firstRow, R.drawable.coin_platinum);
        EditText goldInput = addMoneyInput(firstRow, R.drawable.coin_gold);
        dialogBody.addView(firstRow);

        LinearLayout secondRow = new LinearLayout(this);
        secondRow.setOrientation(LinearLayout.HORIZONTAL);
        secondRow.setGravity(Gravity.CENTER_VERTICAL);
        EditText silverInput = addMoneyInput(secondRow, R.drawable.coin_silver);
        EditText copperInput = addMoneyInput(secondRow, R.drawable.coin_copper);
        dialogBody.addView(secondRow);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);

        View subtract = moneyActionButton(R.drawable.coin_action_subtract, "Subtract");
        View add = moneyActionButton(R.drawable.coin_action_add, "Add");
        LinearLayout.LayoutParams subtractParams = new LinearLayout.LayoutParams(dp(66), dp(44));
        subtractParams.setMargins(0, 0, dp(8), 0);
        actions.addView(subtract, subtractParams);
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(dp(66), dp(44));
        addParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(add, addParams);
        dialogBody.addView(actions);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Money")
                .setView(dialogBody)
                .create();
        subtract.setOnClickListener(view -> applyMoneyChange(dialog, platinumInput, goldInput, silverInput, copperInput, -1));
        add.setOnClickListener(view -> applyMoneyChange(dialog, platinumInput, goldInput, silverInput, copperInput, 1));
        dialog.show();
    }

    private void applyMoneyChange(
            AlertDialog dialog,
            EditText platinumInput,
            EditText goldInput,
            EditText silverInput,
            EditText copperInput,
            int direction
    ) {
        long delta = moneyAsCopper(
                intValue(platinumInput, 0),
                intValue(goldInput, 0),
                intValue(silverInput, 0),
                intValue(copperInput, 0)
        ) * direction;
        long currentTotal = selectedCharacter.moneyAsCopper();
        long updatedTotal = currentTotal + delta;
        if (updatedTotal < 0) {
            Toast toast = Toast.makeText(this, "Not enough money", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }
        selectedCharacter.setMoneyFromCopper(updatedTotal);
        if (inventoryState != null) {
            inventoryRepository.updateMoney(
                    inventoryState.inventoryId,
                    selectedCharacter.platinumCoins,
                    selectedCharacter.goldCoins,
                    selectedCharacter.silverCoins,
                    selectedCharacter.copperCoins
            );
        }
        saveCharacters();
        dialog.dismiss();
        showCharacterSheet();
    }

    private View moneyActionButton(int iconResource, String description) {
        FrameLayout button = new FrameLayout(this);
        button.setBackgroundColor(0xFFE4D7C7);
        button.setContentDescription(description);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconResource);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.addView(icon, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        ));
        return button;
    }

    private EditText addMoneyInput(LinearLayout parent, int coinResource) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.HORIZONTAL);
        cell.setGravity(Gravity.CENTER_VERTICAL);
        cell.setPadding(dp(4), 0, dp(4), 0);

        ImageView icon = new ImageView(this);
        icon.setImageResource(coinResource);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        cell.addView(icon, new LinearLayout.LayoutParams(dp(34), dp(34)));

        EditText amount = new EditText(this);
        amount.setInputType(InputType.TYPE_CLASS_NUMBER);
        amount.setSingleLine(true);
        amount.setText("0");
        amount.setSelectAllOnFocus(true);
        cell.addView(amount, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        parent.addView(cell, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));
        return amount;
    }

    private void addBackpackLayout(LinearLayout parent, FrameLayout content) {
        LinearLayout backpack = verticalLayout(8);
        backpack.setPadding(dp(8), dp(8), dp(8), dp(8));
        backpack.setBackgroundResource(R.drawable.dnd_panel_bg);

        addInventoryCategoryTabs(backpack, content);
        addInventoryGrid(backpack);
        addCoinBelt(backpack);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(12));
        parent.addView(backpack, params);
    }

    private void addInventoryCategoryTabs(LinearLayout parent, FrameLayout content) {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER);

        for (int index = 0; index < INVENTORY_CATEGORIES.length; index++) {
            FrameLayout tab = new FrameLayout(this);
            tab.setPadding(dp(2), dp(2), dp(2), dp(2));
            tab.setBackgroundColor(index == selectedInventoryCategory ? 0xFFC8A86A : 0xFFE4D7C7);
            tab.setContentDescription(INVENTORY_CATEGORIES[index]);

            ImageView icon = new ImageView(this);
            icon.setImageResource(INVENTORY_CATEGORY_ICONS[index]);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            tab.addView(icon, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
            ));

            final int categoryIndex = index;
            tab.setOnClickListener(view -> {
                selectedInventoryCategory = categoryIndex;
                renderTab(content);
            });

            LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
            );
            tabParams.setMargins(dp(2), 0, dp(2), 0);
            tabs.addView(tab, tabParams);
        }

        parent.addView(tabs, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
        ));
    }

    private void addInventoryGrid(LinearLayout parent) {
        RecyclerView grid = new RecyclerView(this);
        grid.setPadding(0, dp(4), 0, dp(4));
        grid.setLayoutManager(new GridLayoutManager(this, INVENTORY_COLUMNS));
        grid.setNestedScrollingEnabled(false);
        grid.setAdapter(new InventorySlotAdapter(visibleInventorySlots));

        int rows = Math.max(1, visibleInventorySlots.size() / INVENTORY_COLUMNS);
        parent.addView(grid, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(82) * rows
        ));
    }

    private void addCoinBelt(LinearLayout parent) {
        LinearLayout belt = new LinearLayout(this);
        belt.setOrientation(LinearLayout.HORIZONTAL);
        belt.setGravity(Gravity.CENTER_VERTICAL);
        belt.setPadding(dp(6), dp(4), dp(6), dp(4));
        belt.setBackgroundResource(R.drawable.dnd_slot_bg);

        addCoinCell(belt, R.drawable.coin_platinum, String.valueOf(selectedCharacter.platinumCoins));
        addCoinCell(belt, R.drawable.coin_gold, String.valueOf(selectedCharacter.goldCoins));
        addCoinCell(belt, R.drawable.coin_silver, String.valueOf(selectedCharacter.silverCoins));
        addCoinCell(belt, R.drawable.coin_copper, String.valueOf(selectedCharacter.copperCoins));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
        );
        params.setMargins(0, dp(4), 0, 0);
        parent.addView(belt, params);
    }

    private void addCoinCell(LinearLayout parent, int iconResource, String amount) {
        LinearLayout coin = new LinearLayout(this);
        coin.setOrientation(LinearLayout.HORIZONTAL);
        coin.setGravity(Gravity.CENTER);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconResource);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        coin.addView(icon, new LinearLayout.LayoutParams(dp(28), dp(28)));

        TextView value = bodyText(amount);
        value.setTextColor(0xFFF0E1C7);
        value.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        value.setGravity(Gravity.CENTER);
        value.setPadding(dp(4), 0, 0, 0);
        coin.addView(value, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        parent.addView(coin, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));
    }

    private void showAddItemDialog(String defaultType) {
        LinearLayout body = verticalLayout(10);
        body.setPadding(dp(16), dp(8), dp(16), 0);

        EditText searchInput = new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setHint("Search by item name...");
        body.addView(searchInput);

        Spinner categorySpinner = new Spinner(this);
        String[] categories = addItemCategories();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setSelection(categoryIndexForType(defaultType));
        body.addView(categorySpinner);

        ScrollView resultsScroll = new ScrollView(this);
        LinearLayout results = verticalLayout(8);
        resultsScroll.addView(results);
        body.addView(resultsScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(420)
        ));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setView(body)
                .setNegativeButton("Cancel", null)
                .create();

        Handler searchHandler = new Handler(Looper.getMainLooper());
        Runnable[] searchRunnable = new Runnable[1];
        Runnable refresh = () -> {
            results.removeAllViews();
            String selectedType = categories[categorySpinner.getSelectedItemPosition()];
            List<InventoryItem> items = inventoryRepository.searchItems(searchInput.getText().toString(), selectedType);
            for (InventoryItem item : items) {
                results.addView(addItemResultRow(item, dialog));
            }
        };
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable[0] != null) {
                    searchHandler.removeCallbacks(searchRunnable[0]);
                }
                searchRunnable[0] = refresh;
                searchHandler.postDelayed(refresh, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                refresh.run();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        dialog.setOnShowListener(d -> refresh.run());
        dialog.show();
    }

    private View addItemResultRow(InventoryItem item, AlertDialog dialog) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));
        row.setBackgroundResource(R.drawable.dnd_slot_bg);

        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (item.icon != null) {
            icon.setImageBitmap(item.icon);
        }
        row.addView(icon, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout text = verticalLayout(2);
        text.setPadding(dp(10), 0, dp(10), 0);
        TextView name = sectionTitle(item.name);
        name.setTextSize(16);
        TextView type = bodyText(displayItemType(item.itemType));
        TextView description = bodyText(shortDescription(item.description));
        text.addView(name);
        text.addView(type);
        text.addView(description);
        row.addView(text, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button add = secondaryButton("Add");
        add.setOnClickListener(view -> {
            inventoryRepository.addBagItem(inventoryState.bagId, item.itemId);
            dialog.dismiss();
            showCharacterSheet();
        });
        row.addView(add, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42)));
        return row;
    }

    private void showEquippedItemDetails(int itemId, String slotColumn) {
        int bagItemId = inventoryRepository.findBagItemIdForItem(inventoryState.bagId, itemId);
        if (bagItemId > 0) {
            showItemDetails(bagItemId);
        } else {
            InventoryItem item = inventoryRepository.getItem(itemId);
            if (item != null) {
                Toast.makeText(this, item.name, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showItemDetails(int bagItemId) {
        InventoryItem item = inventoryRepository.getBagItemDetails(bagItemId, inventoryState.equipmentId);
        if (item == null) {
            return;
        }
        LinearLayout body = verticalLayout(8);
        body.setPadding(dp(16), dp(8), dp(16), 0);
        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (item.icon != null) {
            icon.setImageBitmap(item.icon);
        }
        body.addView(icon, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(140)));
        body.addView(sectionTitle(item.name));
        body.addView(bodyText(displayItemType(item.itemType)));
        body.addView(bodyText(item.description == null ? "" : item.description));
        body.addView(bodyText("Quantity: " + item.quantity));
        if (item.equipped) {
            body.addView(bodyText("Equipped"));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Item Details")
                .setView(body)
                .setNegativeButton("Close", null)
                .setNeutralButton("Remove", (dialog, which) -> confirmRemoveItem(item));
        if (item.equipped) {
            builder.setPositiveButton("Unequip", (dialog, which) -> {
                    inventoryRepository.unequipItem(inventoryState.equipmentId, item.equippedSlot);
                    showCharacterSheet();
            });
        } else if (canEquip(item)) {
            builder.setPositiveButton("Equip", (dialog, which) -> chooseEquipSlot(item));
        }
        builder.show();
    }

    private void confirmRemoveItem(InventoryItem item) {
        if (item.equipped) {
            new AlertDialog.Builder(this)
                    .setTitle("This item is currently equipped.")
                    .setMessage("Unequip and remove it from the inventory?")
                    .setPositiveButton("Unequip and remove", (dialog, which) -> {
                        inventoryRepository.unequipAndRemoveItem(inventoryState.equipmentId, item.equippedSlot, item.bagItemId);
                        showCharacterSheet();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }
        if (InventoryRepository.TYPE_QUEST.equals(item.itemType)) {
            new AlertDialog.Builder(this)
                    .setTitle("This is a quest item.")
                    .setMessage("Are you sure you want to remove it?")
                    .setPositiveButton("Remove quest item", (dialog, which) -> {
                        inventoryRepository.removeBagItem(item.bagItemId);
                        showCharacterSheet();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }
        if (item.quantity > 1) {
            showQuantityRemoveDialog(item);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Remove this item from the inventory?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    inventoryRepository.removeBagItem(item.bagItemId);
                    showCharacterSheet();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showQuantityRemoveDialog(InventoryItem item) {
        LinearLayout body = verticalLayout(10);
        body.setPadding(dp(16), dp(8), dp(16), 0);
        body.addView(bodyText("Quantity in inventory: " + item.quantity));
        body.addView(bodyText("How many items should be removed?"));
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        Button minus = secondaryButton("-");
        Button plus = secondaryButton("+");
        TextView value = sectionTitle("1");
        final int[] amount = {1};
        minus.setOnClickListener(v -> {
            amount[0] = Math.max(1, amount[0] - 1);
            value.setText(String.valueOf(amount[0]));
        });
        plus.setOnClickListener(v -> {
            amount[0] = Math.min(item.quantity, amount[0] + 1);
            value.setText(String.valueOf(amount[0]));
        });
        row.addView(minus);
        row.addView(value, new LinearLayout.LayoutParams(dp(80), LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(plus);
        body.addView(row);
        new AlertDialog.Builder(this)
                .setTitle("Remove")
                .setView(body)
                .setPositiveButton("Remove selected", (dialog, which) -> {
                    if (amount[0] >= item.quantity) {
                        inventoryRepository.removeBagItem(item.bagItemId);
                    } else {
                        inventoryRepository.decreaseBagItemQuantity(item.bagItemId, amount[0]);
                    }
                    showCharacterSheet();
                })
                .setNeutralButton("Remove all", (dialog, which) -> {
                    inventoryRepository.removeBagItem(item.bagItemId);
                    showCharacterSheet();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void chooseEquipSlot(InventoryItem item) {
        List<String> labels = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        switch (item.equipmentSlot == null ? "" : item.equipmentSlot) {
            case "hand":
                labels.add("Main hand"); columns.add("main_hand_id");
                if (!item.twoHanded) {
                    labels.add("Off hand"); columns.add("off_hand_id");
                }
                break;
            case "off_hand":
                labels.add("Off hand"); columns.add("off_hand_id");
                break;
            case "pauldron":
                labels.add("Left pauldron"); columns.add("pauldron_l_id");
                labels.add("Right pauldron"); columns.add("pauldron_r_id");
                break;
            case "glove":
                labels.add("Left glove"); columns.add("glove_l_id");
                labels.add("Right glove"); columns.add("glove_r_id");
                break;
            case "earring":
                labels.add("Left earring"); columns.add("earring_l_id");
                labels.add("Right earring"); columns.add("earring_r_id");
                break;
            case "ring":
                labels.add("Left ring"); columns.add("ring_l_id");
                labels.add("Right ring"); columns.add("ring_r_id");
                break;
            case "head":
                labels.add("Helmet"); columns.add("head_id");
                break;
            case "neck":
                labels.add("Necklace"); columns.add("neck_id");
                break;
            case "chest":
                labels.add("Chest armor"); columns.add("chest_id");
                break;
            case "belt":
                labels.add("Belt"); columns.add("belt_id");
                break;
            case "pants":
                labels.add("Pants"); columns.add("pants_id");
                break;
            case "boots":
                labels.add("Boots"); columns.add("boots_id");
                break;
            default:
                Toast.makeText(this, "This item has no equipment slot.", Toast.LENGTH_SHORT).show();
                return;
        }
        if (columns.size() == 1) {
            confirmEquip(item, columns.get(0));
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Equip to:")
                .setItems(labels.toArray(new String[0]), (dialog, which) -> confirmEquip(item, columns.get(which)))
                .show();
    }

    private void confirmEquip(InventoryItem item, String slotColumn) {
        if ("off_hand_id".equals(slotColumn) && isMainHandTwoHanded()) {
            new AlertDialog.Builder(this)
                    .setTitle("Off hand is unavailable.")
                    .setMessage("The main-hand weapon uses both hands. Replace it?")
                    .setPositiveButton("Replace", (dialog, which) -> {
                        inventoryRepository.unequipItem(inventoryState.equipmentId, "main_hand_id");
                        inventoryRepository.equipItem(inventoryState.equipmentId, slotColumn, item.itemId);
                        showCharacterSheet();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }
        if (item.twoHanded && "main_hand_id".equals(slotColumn)) {
            int offHandItemId = inventoryRepository.getEquippedItemId(inventoryState.equipmentId, "off_hand_id");
            if (offHandItemId > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("This weapon uses both hands.")
                        .setMessage("The off-hand slot will be cleared. Equip this weapon?")
                        .setPositiveButton("Equip", (dialog, which) -> {
                            inventoryRepository.unequipItem(inventoryState.equipmentId, "off_hand_id");
                            inventoryRepository.equipItem(inventoryState.equipmentId, "main_hand_id", item.itemId);
                            showCharacterSheet();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return;
            }
        }
        int occupiedItemId = inventoryRepository.getEquippedItemId(inventoryState.equipmentId, slotColumn);
        if (occupiedItemId > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("This slot is already occupied.")
                    .setMessage("Replace the equipped item?")
                    .setPositiveButton("Replace", (dialog, which) -> {
                        inventoryRepository.equipItem(inventoryState.equipmentId, slotColumn, item.itemId);
                        showCharacterSheet();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            inventoryRepository.equipItem(inventoryState.equipmentId, slotColumn, item.itemId);
            showCharacterSheet();
        }
    }

    private boolean canEquip(InventoryItem item) {
        return item.equipmentSlot != null
                && (InventoryRepository.TYPE_WEAPON.equals(item.itemType)
                || InventoryRepository.TYPE_ARMOR.equals(item.itemType)
                || InventoryRepository.TYPE_ACCESSORIES.equals(item.itemType));
    }

    private String[] addItemCategories() {
        return new String[]{
                InventoryRepository.TYPE_ALL,
                InventoryRepository.TYPE_WEAPON,
                InventoryRepository.TYPE_ARMOR,
                InventoryRepository.TYPE_ACCESSORIES,
                InventoryRepository.TYPE_INSTRUMENTS,
                InventoryRepository.TYPE_MATERIAL,
                InventoryRepository.TYPE_OTHER,
                InventoryRepository.TYPE_QUEST
        };
    }

    private int categoryIndexForType(String type) {
        String[] categories = addItemCategories();
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(type)) {
                return i;
            }
        }
        return 0;
    }

    private String displayItemType(String itemType) {
        if (InventoryRepository.TYPE_ACCESSORIES.equals(itemType)) {
            return "Accessories";
        }
        if (InventoryRepository.TYPE_MATERIAL.equals(itemType)) {
            return "Materials";
        }
        if (InventoryRepository.TYPE_QUEST.equals(itemType)) {
            return "Quest Items";
        }
        return itemType == null ? "" : itemType;
    }

    private String shortDescription(String description) {
        if (description == null) {
            return "";
        }
        String normalized = description.replace('\n', ' ');
        return normalized.length() > 96 ? normalized.substring(0, 93) + "..." : normalized;
    }

    private View characterRow(DndCharacter character) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackgroundResource(R.drawable.dnd_panel_bg);

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(classImageResource(character.characterClass));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        row.addView(avatar, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout textBox = verticalLayout(2);
        textBox.setPadding(dp(14), 0, 0, 0);
        TextView name = sectionTitle(character.name);
        TextView details = bodyText(character.characterClass + " | Level " + character.level);
        textBox.addView(name);
        textBox.addView(details);
        row.addView(textBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button deleteButton = secondaryButton("Delete");
        deleteButton.setOnClickListener(view -> confirmDeleteCharacter(character));
        row.addView(deleteButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        row.setOnClickListener(view -> {
            selectedCharacter = character;
            selectedTab = TAB_STATS;
            selectedInventoryCategory = 0;
            showCharacterSheet();
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(12), 0, 0);
        row.setLayoutParams(params);
        return row;
    }

    private void confirmDeleteCharacter(DndCharacter character) {
        new AlertDialog.Builder(this)
                .setTitle("Delete character?")
                .setMessage(character.name + " will be removed from the list.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    characters.remove(character);
                    saveCharacters();
                    showCharacterSelect();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addEquipmentLayout(LinearLayout parent) {
        FrameLayout equipment = new AspectRatioFrameLayout(this);
        equipment.setBackgroundResource(R.drawable.dnd_slot_bg);

        ImageView background = new ImageView(this);
        background.setImageResource(classImageResource(selectedCharacter.characterClass));
        background.setScaleType(ImageView.ScaleType.FIT_XY);
        equipment.addView(background, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        ImageView frame = new ImageView(this);
        frame.setImageResource(R.drawable.equipment_frame);
        frame.setScaleType(ImageView.ScaleType.FIT_XY);
        equipment.addView(frame, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        addEquipmentSlot(equipment, "Helmet", "head_id", 560, 94, 168, 162);
        addEquipmentSlot(equipment, "Left Pauldron", "pauldron_l_id", 210, 406, 184, 168);
        addEquipmentSlot(equipment, "Right Pauldron", "pauldron_r_id", 900, 401, 182, 167);
        addEquipmentSlot(equipment, "Left Earring", "earring_l_id", 384, 167, 126, 130);
        addEquipmentSlot(equipment, "Right Earring", "earring_r_id", 772, 160, 128, 130);
        addEquipmentSlot(equipment, "Necklace", "neck_id", 577, 316, 130, 116);
        addEquipmentSlot(equipment, "Left Glove", "glove_l_id", 214, 704, 185, 175);
        addEquipmentSlot(equipment, "Right Glove", "glove_r_id", 897, 694, 183, 174);
        addEquipmentSlot(equipment, "Chest Armor", "chest_id", 540, 482, 195, 212);
        addEquipmentSlot(equipment, "Belt", "belt_id", 555, 892, 155, 116);
        addEquipmentSlot(equipment, "Main Hand", "main_hand_id", 121, 1099, 161, 470);
        addEquipmentSlot(equipment, "Off Hand", "off_hand_id", 1000, 1102, 161, 477);
        addEquipmentSlot(equipment, "Left Ring", "ring_l_id", 309, 973, 93, 100);
        addEquipmentSlot(equipment, "Right Ring", "ring_r_id", 867, 962, 96, 100);
        addEquipmentSlot(equipment, "Pants", "pants_id", 544, 1137, 195, 228);
        addEquipmentSlot(equipment, "Boots", "boots_id", 543, 1467, 201, 188);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(12));
        parent.addView(equipment, params);
    }

    private void addEquipmentSlot(FrameLayout equipment, String slot, String slotColumn, int left, int top, int width, int height) {
        ImageView slotView = new ImageView(this);
        slotView.setBackgroundColor(0x66D0D0D0);
        slotView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        InventoryItem item = equippedItems.get(slotColumn);
        if ("off_hand_id".equals(slotColumn) && isMainHandTwoHanded() && item == null) {
            slotView.setBackgroundColor(0x88A64242);
            slotView.setContentDescription(slot + ": unavailable while using a two-handed weapon");
            slotView.setOnClickListener(view -> Toast.makeText(
                    this,
                    "Off hand is unavailable while using a two-handed weapon.",
                    Toast.LENGTH_SHORT
            ).show());
        } else if (item != null && item.icon != null) {
            slotView.setImageBitmap(item.icon);
            slotView.setBackgroundResource(R.drawable.dnd_empty_slot_bg);
            slotView.setContentDescription(slot + ": " + item.name);
            slotView.setOnClickListener(view -> showEquippedItemDetails(item.itemId, slotColumn));
        } else {
            slotView.setContentDescription(slot);
            slotView.setOnClickListener(view -> Toast.makeText(this, slot + ": empty", Toast.LENGTH_SHORT).show());
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1, 1);
        equipment.addView(slotView, params);
        equipment.addOnLayoutChangeListener((view, leftEdge, topEdge, rightEdge, bottomEdge, oldLeft, oldTop, oldRight, oldBottom) -> {
            int equipmentWidth = rightEdge - leftEdge;
            int equipmentHeight = bottomEdge - topEdge;
            FrameLayout.LayoutParams updatedParams = (FrameLayout.LayoutParams) slotView.getLayoutParams();
            updatedParams.width = Math.round(equipmentWidth * (width / EQUIPMENT_DESIGN_WIDTH));
            updatedParams.height = Math.round(equipmentHeight * (height / EQUIPMENT_DESIGN_HEIGHT));
            updatedParams.leftMargin = Math.round(equipmentWidth * (left / EQUIPMENT_DESIGN_WIDTH));
            updatedParams.topMargin = Math.round(equipmentHeight * (top / EQUIPMENT_DESIGN_HEIGHT));
            slotView.setLayoutParams(updatedParams);
        });
    }

    private boolean isMainHandTwoHanded() {
        InventoryItem mainHand = equippedItems.get("main_hand_id");
        return mainHand != null && mainHand.twoHanded;
    }

    private int classImageResource(String characterClass) {
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

    private void restoreSpellUses(String restName) {
        selectedCharacter.currentSpellUses = selectedCharacter.maxSpellUses;
        saveCharacters();
        Toast.makeText(this, restName + ": spell uses restored", Toast.LENGTH_SHORT).show();
        showCharacterSheet();
    }

    private void addSpellUseCells(LinearLayout parent) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(4), 0, dp(8));

        for (int index = 0; index < selectedCharacter.maxSpellUses; index++) {
            TextView cell = new TextView(this);
            cell.setText("");
            cell.setBackgroundColor(index < selectedCharacter.currentSpellUses ? 0xFF4CAF50 : 0xFFE53935);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(18), 1f);
            params.setMargins(dp(2), 0, dp(2), 0);
            row.addView(cell, params);
        }

        parent.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }

    private int maxLearnableSpellLevel() {
        int level = selectedCharacter.level;
        if (level >= 17) {
            return 9;
        } else if (level >= 15) {
            return 8;
        } else if (level >= 13) {
            return 7;
        } else if (level >= 11) {
            return 6;
        } else if (level >= 9) {
            return 5;
        } else if (level >= 7) {
            return 4;
        } else if (level >= 5) {
            return 3;
        } else if (level >= 3) {
            return 2;
        }
        return 1;
    }

    private void addSpellLevels(LinearLayout parent) {
        for (int level = 0; level <= 9; level++) {
            parent.addView(spellLevelFrame(level));
        }
    }

    private View spellLevelFrame(int spellLevel) {
        LinearLayout frame = verticalLayout(8);
        frame.setPadding(dp(12), dp(10), dp(12), dp(12));
        frame.setBackgroundResource(R.drawable.dnd_panel_bg);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = sectionTitle("Level " + spellLevel);
        header.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button addButton = secondaryButton("+");
        addButton.setTextSize(22);
        addButton.setOnClickListener(view -> showAddSpellDialog(spellLevel));
        header.addView(addButton, new LinearLayout.LayoutParams(dp(48), dp(48)));
        frame.addView(header);

        List<String> spells = selectedCharacter.spellbook.get(spellLevel);
        if (spells.isEmpty()) {
            frame.addView(bodyText("The list is empty."));
        } else {
            for (String spell : spells) {
                TextView spellView = bodyText("- " + displaySpellName(spell));
                spellView.setPadding(0, dp(6), 0, dp(6));
                spellView.setOnClickListener(view -> showSpellDetails(spellLevel, displaySpellName(spell)));
                frame.addView(spellView);
            }
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(8));
        frame.setLayoutParams(params);
        return frame;
    }

    private void showAddSpellDialog(int presetLevel) {
        List<SpellDefinition> availableSpells = availableSpellsFor(presetLevel);
        if (availableSpells.isEmpty()) {
            Toast.makeText(this, "No available spells to add", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] spellNames = new String[availableSpells.size()];
        for (int index = 0; index < availableSpells.size(); index++) {
            SpellDefinition spell = availableSpells.get(index);
            spellNames[index] = "Level " + spell.level + " - " + spell.name;
        }

        new AlertDialog.Builder(this)
                .setTitle(presetLevel >= 0 ? "Available spell level " + presetLevel : "Available spell")
                .setItems(spellNames, (dialog, which) -> {
                    SpellDefinition spell = availableSpells.get(which);
                    selectedCharacter.spellbook.get(spell.level).add(spell.name);
                    saveCharacters();
                    showCharacterSheet();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void oldManualAddSpellDialog(int presetLevel) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout form = verticalLayout(12);
        form.setPadding(dp(22), dp(10), dp(22), dp(18));
        scrollView.addView(form, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        EditText spellName = spellDialogInput(form, "1. Spell Name", "For example: Fireball", false);
        EditText spellLevel = null;
        if (presetLevel < 0) {
            spellLevel = spellDialogInput(form, "2. Spell Level", "0-9", true);
            spellLevel.setText("0");
        } else {
            TextView fixedLevel = bodyText("2. Spell Level: " + presetLevel);
            fixedLevel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            fixedLevel.setPadding(0, dp(8), 0, dp(8));
            form.addView(fixedLevel);
        }

        EditText spellClass = spellDialogInput(form, "3. Character Class", "For example: Wizard", false);
        EditText spellRange = spellDialogInput(form, "4. Range", "For example: 150 ft", false);
        EditText spellAttackType = spellDialogInput(form, "5. Attack Type", "For example: saving throw", false);
        EditText spellDamageType = spellDialogInput(form, "6. Damage Type", "For example: Fire", false);
        EditText spellDamage = spellDialogInput(form, "7. Damage Formula", "For example: 8d6", false);

        EditText finalSpellLevel = spellLevel;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(presetLevel >= 0 ? "Add to level " + presetLevel + " - version 3" : "Add Spell - version 3")
                .setView(scrollView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(openDialog -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String name = value(spellName).trim();
                if (name.isEmpty()) {
                    spellName.setError("Enter name");
                    return;
                }

                int level = presetLevel >= 0 ? presetLevel : intValue(finalSpellLevel, 0);
                if (level < 0 || level > 9) {
                    if (finalSpellLevel != null) {
                        finalSpellLevel.setError("Level must be from 0 to 9");
                    }
                    return;
                }

                String className = valueOrDefault(spellClass, "No Class");
                String range = valueOrDefault(spellRange, "No Range");
                String attackType = valueOrDefault(spellAttackType, "No Attack Type");
                String damageType = valueOrDefault(spellDamageType, "No Damage Type");
                String damage = valueOrDefault(spellDamage, "No Damage");
                String spellDetails = name + " | " + className + " | " + range + " | " + attackType + " | " + damage + " " + damageType;

                selectedCharacter.spellbook.get(level).add(spellDetails);
                saveCharacters();
                dialog.dismiss();
                showCharacterSheet();
            });

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.96f),
                        (int) (getResources().getDisplayMetrics().heightPixels * 0.88f)
                );
            }
        });
        dialog.show();
    }

    private void oldShowAddSpellDialog(int presetLevel) {
        LinearLayout form = verticalLayout(10);
        form.setPadding(dp(20), dp(8), dp(20), 0);

        TextInputEditText spellName = textInput(form, "Spell Name", "Fireball");
        TextInputEditText spellLevel = null;
        if (presetLevel < 0) {
            spellLevel = numberInput(form, "Spell Level 0-9", 0);
        }

        TextInputEditText finalSpellLevel = spellLevel;
        new AlertDialog.Builder(this)
                .setTitle(presetLevel >= 0 ? "Add to level " + presetLevel : "Add Spell")
                .setView(form)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = value(spellName).trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Enter spell name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int level = presetLevel >= 0 ? presetLevel : intValue(finalSpellLevel, 0);
                    if (level < 0 || level > 9) {
                        Toast.makeText(this, "Level must be from 0 to 9", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedCharacter.spellbook.get(level).add(name);
                    saveCharacters();
                    showCharacterSheet();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addStat(LinearLayout parent, String label, int value) {
        addStat(parent, label, String.valueOf(value));
    }

    private List<SpellDefinition> availableSpellsFor(int presetLevel) {
        List<SpellDefinition> result = new ArrayList<>();
        int maxLevel = maxLearnableSpellLevel();
        for (SpellDefinition spell : SPELL_LIBRARY) {
            if (presetLevel >= 0 && spell.level != presetLevel) {
                continue;
            }
            if (spell.level > maxLevel) {
                continue;
            }
            if (learnedSpell(spell.name)) {
                continue;
            }
            if (canLearnSpell(spell)) {
                result.add(spell);
            }
        }
        return result;
    }

    private boolean learnedSpell(String spellName) {
        for (List<String> levelSpells : selectedCharacter.spellbook) {
            for (String learned : levelSpells) {
                if (displaySpellName(learned).equalsIgnoreCase(spellName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canLearnSpell(SpellDefinition spell) {
        String characterClass = selectedCharacter.characterClass.toLowerCase();
        if (characterClass.contains("wizard") || characterClass.contains("wizard")) {
            return spell.classes.contains("Wizard");
        } else if (characterClass.contains("sorcerer") || characterClass.contains("sorcerer")) {
            return spell.classes.contains("Sorcerer");
        } else if (characterClass.contains("warlock") || characterClass.contains("warlock")) {
            return spell.classes.contains("Warlock");
        } else if (characterClass.contains("cleric") || characterClass.contains("cleric")) {
            return spell.classes.contains("Cleric");
        } else if (characterClass.contains("druid") || characterClass.contains("druid")) {
            return spell.classes.contains("Druid");
        } else if (characterClass.contains("bard") || characterClass.contains("bard")) {
            return spell.classes.contains("Bard");
        } else if (characterClass.contains("paladin") || characterClass.contains("paladin")) {
            return spell.classes.contains("Paladin");
        } else if (characterClass.contains("ranger") || characterClass.contains("ranger")) {
            return spell.classes.contains("Ranger");
        } else if (characterClass.contains("artificer") || characterClass.contains("artificer")) {
            return spell.classes.contains("Artificer");
        }
        return true;
    }

    private String displaySpellName(String savedSpell) {
        int separator = savedSpell.indexOf("|");
        if (separator >= 0) {
            return savedSpell.substring(0, separator).trim();
        }
        return savedSpell.trim();
    }

    private SpellDefinition findSpell(String spellName) {
        for (SpellDefinition spell : SPELL_LIBRARY) {
            if (spell.name.equalsIgnoreCase(spellName)) {
                return spell;
            }
        }
        return null;
    }

    private void showSpellDetails(int spellLevel, String spellName) {
        SpellDefinition spell = findSpell(spellName);
        String description = spell == null
                ? "Description is unavailable for an old spell."
                : spell.description;
        boolean cantrip = spellLevel == 0;
        String message = description + "\n\nLevel: " + spellLevel + "\nUses: "
                + selectedCharacter.currentSpellUses + " / " + selectedCharacter.maxSpellUses
                + (cantrip ? "\nCantrip: does not spend uses." : "");

        new AlertDialog.Builder(this)
                .setTitle(spellName)
                .setMessage(message)
                .setPositiveButton("Apply", (dialog, which) -> castSpell(spellLevel, spellName))
                .setNegativeButton("Close", null)
                .show();
    }

    private void castSpell(int spellLevel, String spellName) {
        if (spellLevel > 0) {
            if (selectedCharacter.currentSpellUses <= 0) {
                Toast.makeText(this, "No spell uses available. Take a rest.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedCharacter.currentSpellUses--;
        }
        saveCharacters();
        Toast.makeText(this, spellName + " cast", Toast.LENGTH_SHORT).show();
        showCharacterSheet();
    }

    private EditText spellDialogInput(LinearLayout parent, String label, String hint, boolean numberOnly) {
        TextView labelView = bodyText(label);
        labelView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        labelView.setPadding(0, dp(10), 0, dp(4));
        parent.addView(labelView);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(hint);
        input.setTextSize(16);
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setBackgroundColor(0xFFE4E0DC);
        input.setInputType(numberOnly ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
        parent.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
        ));
        return input;
    }

    private void addStat(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackgroundColor(0xFFF7F2EA);

        boolean isLongValue = value.length() > 32 || value.contains("\n");
        row.setOrientation(isLongValue ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        row.setGravity(isLongValue ? Gravity.START : Gravity.CENTER_VERTICAL);

        TextView name = bodyText(label);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        if (isLongValue) {
            row.addView(name);
        } else {
            row.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        }

        TextView number = sectionTitle(value);
        number.setGravity(isLongValue ? Gravity.START : Gravity.END);
        row.addView(number);

        parent.addView(row);
    }

    private void addSheetHeader(LinearLayout parent) {
        LinearLayout header = verticalLayout(0);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, dp(2), 0, dp(8));

        TextView brand = title("DUNGEONS & DRAGONS");
        brand.setTextSize(24);
        brand.setGravity(Gravity.CENTER);
        header.addView(brand);

        LinearLayout flourish = new LinearLayout(this);
        flourish.setOrientation(LinearLayout.HORIZONTAL);
        flourish.setGravity(Gravity.CENTER_VERTICAL);
        flourish.setPadding(0, dp(4), 0, 0);

        View left = new View(this);
        left.setBackgroundColor(0xFF8A633A);
        flourish.addView(left, new LinearLayout.LayoutParams(0, dp(1), 1f));

        TextView mark = bodyText("◆");
        mark.setTextSize(12);
        mark.setTextColor(0xFF8A633A);
        mark.setGravity(Gravity.CENTER);
        mark.setPadding(dp(10), 0, dp(10), 0);
        flourish.addView(mark);

        View right = new View(this);
        right.setBackgroundColor(0xFF8A633A);
        flourish.addView(right, new LinearLayout.LayoutParams(0, dp(1), 1f));
        header.addView(flourish);

        parent.addView(header);
    }

    private void addIdentitySection(LinearLayout parent) {
        LinearLayout section = sheetSection("Character Details");
        section.setPadding(dp(12), dp(9), dp(12), dp(10));
        String[][] fields = {
                {"Character Name", selectedCharacter.name},
                {"Class", selectedCharacter.characterClass},
                {"Level", String.valueOf(selectedCharacter.level)},
                {"Race", selectedCharacter.race},
                {"Background", selectedCharacter.background},
                {"Alignment", selectedCharacter.alignment}
        };
        addCompactIdentityGrid(section, fields, wideLayout() ? 3 : 2);
        parent.addView(section);
    }

    private void addCombatSection(LinearLayout parent) {
        LinearLayout section = sheetSection("Combat Stats");
        GridLayout combatGrid = new GridLayout(this);
        combatGrid.setColumnCount(wideLayout() ? 5 : 2);
        combatGrid.setUseDefaultMargins(false);
        addCombatCell(combatGrid, "Armor Class", String.valueOf(selectedCharacter.armorClass), true);
        addCombatCell(combatGrid, "Initiative", String.valueOf(selectedCharacter.initiative), false);
        addCombatCell(combatGrid, "Speed", String.valueOf(selectedCharacter.speed), false);
        addCombatCell(combatGrid, "Proficiency Bonus", String.valueOf(selectedCharacter.proficiencyBonus), false);
        addCombatCell(combatGrid, "Passive Wisdom", String.valueOf(selectedCharacter.perception), false);
        section.addView(combatGrid);

        LinearLayout hp = new LinearLayout(this);
        hp.setOrientation(wideLayout() ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        hp.setPadding(0, dp(8), 0, 0);
        addHpPanel(hp, "Current Hit Points", String.valueOf(selectedCharacter.currentHp), "Maximum: " + selectedCharacter.maxHp);
        addHpPanel(hp, "Temporary Hit Points", String.valueOf(selectedCharacter.temporaryHp), "Protection beyond maximum");
        addHpPanel(hp, "Hit Dice", selectedCharacter.hitDice, "Total: " + selectedCharacter.level);
        section.addView(hp);
        parent.addView(section);
    }

    private void addCharacterNotesSection(LinearLayout parent) {
        LinearLayout section = sheetSection("Description");
        String[][] notes = {
                {"Features and Traits", selectedCharacter.featuresAndTraits},
                {"Personality Traits", selectedCharacter.personalityTraits},
                {"Ideals", selectedCharacter.ideals},
                {"Bonds", selectedCharacter.bonds},
                {"Flaws", selectedCharacter.flaws}
        };
        addFieldGrid(section, notes, wideLayout() ? 2 : 1);
        parent.addView(section);
    }

    private LinearLayout sheetSection(String titleText) {
        LinearLayout section = verticalLayout(0);
        section.setPadding(dp(14), dp(12), dp(14), dp(14));
        section.setBackgroundResource(R.drawable.dnd_sheet_section_bg);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        sectionParams.setMargins(0, 0, 0, dp(12));
        section.setLayoutParams(sectionParams);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(10));

        View leftLine = new View(this);
        leftLine.setBackgroundColor(0xFFD7BFA6);
        header.addView(leftLine, new LinearLayout.LayoutParams(0, dp(1), 1f));

        TextView title = sectionTitle(titleText.toUpperCase());
        title.setTextSize(15);
        title.setGravity(Gravity.CENTER);
        title.setPadding(dp(12), 0, dp(12), 0);
        header.addView(title);

        View rightLine = new View(this);
        rightLine.setBackgroundColor(0xFFD7BFA6);
        header.addView(rightLine, new LinearLayout.LayoutParams(0, dp(1), 1f));
        section.addView(header);

        return section;
    }

    private void addFieldGrid(LinearLayout parent, String[][] fields, int columns) {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(columns);
        grid.setUseDefaultMargins(false);

        for (String[] field : fields) {
            addFieldCell(grid, field[0], field[1], columns);
        }

        parent.addView(grid);
    }

    private void addCompactIdentityGrid(LinearLayout parent, String[][] fields, int columns) {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(columns);
        grid.setUseDefaultMargins(false);

        for (String[] field : fields) {
            LinearLayout cell = verticalLayout(0);
            cell.setPadding(dp(10), dp(6), dp(10), dp(7));
            cell.setMinimumHeight(dp(46));
            cell.setBackgroundResource(R.drawable.dnd_field_bg);

            TextView label = bodyText(field[0].toUpperCase());
            label.setTextSize(10);
            label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            label.setTextColor(0xFF80664F);
            cell.addView(label);

            TextView value = sectionTitle(field[1] == null || field[1].trim().isEmpty() ? "—" : field[1]);
            value.setTextSize(14);
            value.setSingleLine(false);
            value.setPadding(0, dp(1), 0, 0);
            cell.addView(value);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dp(2), dp(2), dp(2), dp(2));
            cell.setLayoutParams(params);
            grid.addView(cell);
        }

        parent.addView(grid);
    }

    private void addCombatCell(GridLayout grid, String labelText, String valueText, boolean shield) {
        LinearLayout cell = verticalLayout(0);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(8), dp(9), dp(8), dp(10));
        cell.setMinimumHeight(dp(92));
        cell.setBackgroundResource(R.drawable.dnd_combat_cell_bg);

        TextView value = sectionTitle(valueText);
        value.setTextSize(shield ? 22 : 24);
        value.setGravity(Gravity.CENTER);
        if (shield) {
            value.setBackgroundResource(R.drawable.dnd_armor_shield_bg);
            cell.addView(value, new LinearLayout.LayoutParams(dp(52), dp(56)));
        } else {
            value.setBackgroundResource(R.drawable.dnd_combat_value_bg);
            cell.addView(value, new LinearLayout.LayoutParams(dp(56), dp(46)));
        }

        TextView label = bodyText(labelText.toUpperCase());
        label.setTextSize(11);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setTextColor(0xFF6E5743);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, dp(6), 0, 0);
        cell.addView(label);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(3), dp(3), dp(3), dp(3));
        cell.setLayoutParams(params);
        grid.addView(cell);
    }

    private void addFieldCell(GridLayout grid, String labelText, String valueText, int columns) {
        LinearLayout cell = verticalLayout(0);
        cell.setPadding(dp(12), dp(8), dp(12), dp(9));
        cell.setMinimumHeight(dp(58));
        cell.setBackgroundResource(R.drawable.dnd_field_bg);

        TextView label = bodyText(labelText.toUpperCase());
        label.setTextSize(12);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setTextColor(0xFF7B624D);
        cell.addView(label);

        TextView value = sectionTitle(valueText == null || valueText.trim().isEmpty() ? "—" : valueText);
        value.setTextSize(16);
        value.setPadding(0, dp(3), 0, 0);
        cell.addView(value);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(3), dp(3), dp(3), dp(3));
        cell.setLayoutParams(params);
        grid.addView(cell);
    }

    private void addHpPanel(LinearLayout parent, String labelText, String valueText, String hintText) {
        LinearLayout panel = verticalLayout(0);
        panel.setPadding(dp(12), dp(10), dp(12), dp(12));
        panel.setMinimumHeight(dp(96));
        panel.setBackgroundResource(R.drawable.dnd_field_bg);

        TextView label = bodyText(labelText.toUpperCase());
        label.setTextSize(12);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setTextColor(0xFF7B624D);
        label.setGravity(Gravity.CENTER);
        panel.addView(label);

        TextView value = sectionTitle(valueText);
        value.setTextSize(25);
        value.setGravity(Gravity.CENTER);
        value.setPadding(0, dp(4), 0, dp(3));
        panel.addView(value);

        TextView hint = bodyText(hintText);
        hint.setTextSize(13);
        hint.setGravity(Gravity.CENTER);
        panel.addView(hint);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                wideLayout() ? 0 : LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                wideLayout() ? 1f : 0f
        );
        params.setMargins(dp(3), dp(3), dp(3), dp(3));
        parent.addView(panel, params);
    }

    private void addAbilitySection(LinearLayout parent, List<String> selectedSavingThrows) {
        LinearLayout section = sheetSection("Ability Scores");

        addAbilityRow(section, "Strength", "STR", selectedCharacter.strength,
                new String[]{"Saving Throw (Strength)"}, new String[]{"Athletics"}, selectedSavingThrows);
        addAbilityRow(section, "Dexterity", "DEX", selectedCharacter.dexterity,
                new String[]{"Saving Throw (Dexterity)"}, new String[]{"Acrobatics", "Sleight of Hand", "Stealth"}, selectedSavingThrows);
        addAbilityRow(section, "Constitution", "CON", selectedCharacter.constitution,
                new String[]{"Saving Throw (Constitution)"}, new String[]{}, selectedSavingThrows);
        addAbilityRow(section, "Intelligence", "INT", selectedCharacter.intelligence,
                new String[]{"Saving Throw (Intelligence)"}, new String[]{"Arcana", "History", "Investigation", "Nature", "Religion"}, selectedSavingThrows);
        addAbilityRow(section, "Wisdom", "WIS", selectedCharacter.wisdom,
                new String[]{"Saving Throw (Wisdom)"}, new String[]{"Animal Handling", "Insight", "Medicine", "Perception", "Survival"}, selectedSavingThrows);
        addAbilityRow(section, "Charisma", "CHA", selectedCharacter.charisma,
                new String[]{"Saving Throw (Charisma)"}, new String[]{"Deception", "Intimidation", "Performance", "Persuasion"}, selectedSavingThrows);

        parent.addView(section);
    }

    private void addAbilityRow(
            LinearLayout parent,
            String name,
            String shortName,
            int value,
            String[] savingThrows,
            String[] skills,
            List<String> selectedSavingThrows
    ) {
        LinearLayout row = new LinearLayout(this);
        boolean compact = !wideLayout();
        row.setOrientation(compact ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        row.setGravity(compact ? Gravity.START : Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackgroundResource(R.drawable.dnd_ability_row_bg);

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);

        TextView score = sectionTitle(String.valueOf(value));
        score.setTextSize(22);
        score.setGravity(Gravity.CENTER);
        score.setBackgroundResource(R.drawable.dnd_ability_score_bg);
        heading.addView(score, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout labelBox = verticalLayout(0);
        labelBox.setPadding(dp(10), 0, dp(8), 0);
        TextView label = sectionTitle(name.toUpperCase());
        label.setTextSize(16);
        TextView abbreviation = bodyText(shortName);
        abbreviation.setTextSize(14);
        abbreviation.setTextColor(0xFF2B2118);
        labelBox.addView(label);
        labelBox.addView(abbreviation);
        heading.addView(labelBox, new LinearLayout.LayoutParams(
                compact ? 0 : dp(108),
                LinearLayout.LayoutParams.WRAP_CONTENT,
                compact ? 1f : 0f
        ));

        if (compact) {
            row.addView(heading, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        } else {
            row.addView(heading, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        }

        LinearLayout savingBox = verticalLayout(0);
        savingBox.setPadding(dp(10), 0, dp(10), 0);
        for (String savingThrow : savingThrows) {
            savingBox.addView(proficiencyLine("Saving Throw", selectedSavingThrows.contains(savingThrow)));
        }
        if (compact) {
            savingBox.setPadding(0, dp(10), 0, 0);
            row.addView(savingBox, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        } else {
            row.addView(savingBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f));
        }

        View divider = new View(this);
        divider.setBackgroundColor(0xFFD9C5AF);
        row.addView(divider, new LinearLayout.LayoutParams(
                compact ? LinearLayout.LayoutParams.MATCH_PARENT : dp(1),
                compact ? dp(1) : LinearLayout.LayoutParams.MATCH_PARENT
        ));

        LinearLayout skillsBox = verticalLayout(0);
        skillsBox.setPadding(compact ? 0 : dp(14), compact ? dp(8) : 0, 0, 0);
        if (skills.length == 0) {
            TextView empty = bodyText("");
            skillsBox.addView(empty, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(20)
            ));
        } else {
            for (String skill : skills) {
                skillsBox.addView(proficiencyLine(skill, selectedSavingThrows.contains(skill)));
            }
        }
        row.addView(skillsBox, new LinearLayout.LayoutParams(
                compact ? LinearLayout.LayoutParams.MATCH_PARENT : 0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                compact ? 0f : 1.05f
        ));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(8));
        parent.addView(row, params);
    }

    private TextView proficiencyLine(String text, boolean selected) {
        TextView line = bodyText((selected ? "●  " : "○  ") + text);
        line.setTextSize(14);
        line.setTextColor(0xFF2F261D);
        line.setPadding(0, dp(2), 0, dp(2));
        return line;
    }

    private boolean wideLayout() {
        return getResources().getConfiguration().screenWidthDp >= 600;
    }

    private void addLanguagesTable(LinearLayout parent, List<String> languages) {
        LinearLayout table = verticalLayout(0);
        table.setPadding(dp(14), dp(12), dp(14), dp(12));
        table.setBackgroundColor(0xFFF7F2EA);

        table.addView(sectionTitle("Languages"));
        if (languages.isEmpty()) {
            TextView empty = bodyText("Not selected");
            empty.setPadding(0, dp(8), 0, 0);
            table.addView(empty);
        } else {
            GridLayout languageGrid = new GridLayout(this);
            languageGrid.setColumnCount(2);
            languageGrid.setUseDefaultMargins(true);
            languageGrid.setPadding(0, dp(8), 0, 0);

            addLanguageCell(languageGrid, "#", true, 0.25f);
            addLanguageCell(languageGrid, "Language", true, 1f);
            for (int index = 0; index < languages.size(); index++) {
                addLanguageCell(languageGrid, String.valueOf(index + 1), false, 0.25f);
                addLanguageCell(languageGrid, languages.get(index), false, 1f);
            }
            table.addView(languageGrid);
        }

        parent.addView(table);
    }

    private void addSavingThrowsTable(LinearLayout parent, List<String> selectedSavingThrows) {
        LinearLayout table = verticalLayout(0);
        table.setPadding(dp(14), dp(12), dp(14), dp(12));
        table.setBackgroundColor(0xFFF7F2EA);

        table.addView(sectionTitle("Ability Saving Throws"));

        GridLayout savingThrowGrid = new GridLayout(this);
        savingThrowGrid.setColumnCount(2);
        savingThrowGrid.setUseDefaultMargins(true);
        savingThrowGrid.setPadding(0, dp(8), 0, 0);

        addLanguageCell(savingThrowGrid, "Ability", true, 0.75f);
        addLanguageCell(savingThrowGrid, "List", true, 1.25f);
        for (String[] group : SAVING_THROW_GROUPS) {
            addLanguageCell(savingThrowGrid, group[0], false, 0.75f);
            addSavingThrowOptionsCell(savingThrowGrid, group[1].split("\n"), selectedSavingThrows, 1.25f);
        }

        table.addView(savingThrowGrid);
        parent.addView(table);
    }

    private void addSavingThrowOptionsCell(
            GridLayout table,
            String[] savingThrows,
            List<String> selectedSavingThrows,
            float weight
    ) {
        LinearLayout cell = verticalLayout(0);
        cell.setPadding(dp(8), dp(2), dp(8), dp(2));
        for (String savingThrow : savingThrows) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));

            TextView indicator = bodyText(selectedSavingThrows.contains(savingThrow) ? "●" : "○");
            indicator.setGravity(Gravity.CENTER);
            row.addView(indicator, new LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView name = bodyText(savingThrow);
            row.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            cell.addView(row);
        }

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, weight);
        cell.setLayoutParams(params);
        table.addView(cell);
    }

    private void addLanguageCell(GridLayout table, String text, boolean header, float weight) {
        TextView cell = header ? sectionTitle(text) : bodyText(text);
        cell.setPadding(dp(8), dp(6), dp(8), dp(6));
        if (header) {
            cell.setBackgroundColor(0xFFE4D7C7);
        }

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, weight);
        cell.setLayoutParams(params);
        table.addView(cell);
    }

    private String languagesSummary(boolean[] languageSelections) {
        List<String> languages = collectSelectedLanguages(languageSelections);
        if (languages.isEmpty()) {
            return "not selected";
        }

        StringBuilder summary = new StringBuilder();
        for (String language : languages) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(language);
        }
        return summary.toString();
    }

    private List<String> collectSelectedLanguages(boolean[] languageSelections) {
        List<String> languages = new ArrayList<>();
        for (int index = 0; index < LANGUAGE_OPTIONS.length && index < languageSelections.length; index++) {
            if (languageSelections[index]) {
                languages.add(LANGUAGE_OPTIONS[index]);
            }
        }
        return languages;
    }

    private String savingThrowsSummary(boolean[] savingThrowSelections) {
        List<String> savingThrows = collectSelectedSavingThrows(savingThrowSelections);
        if (savingThrows.isEmpty()) {
            return "not selected";
        }

        StringBuilder summary = new StringBuilder();
        for (String savingThrow : savingThrows) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(savingThrow);
        }
        return summary.toString();
    }

    private List<String> collectSelectedSavingThrows(boolean[] savingThrowSelections) {
        List<String> savingThrows = new ArrayList<>();
        for (int index = 0; index < SAVING_THROW_OPTIONS.length && index < savingThrowSelections.length; index++) {
            if (savingThrowSelections[index]) {
                savingThrows.add(SAVING_THROW_OPTIONS[index]);
            }
        }
        return savingThrows;
    }

    private String[] optionNames(List<DbOption> options) {
        String[] names = new String[options.size()];
        for (int index = 0; index < options.size(); index++) {
            names[index] = options.get(index).name;
        }
        return names;
    }

    private String[] classNames(List<ClassOption> options) {
        String[] names = new String[options.size()];
        for (int index = 0; index < options.size(); index++) {
            names[index] = options.get(index).name;
        }
        return names;
    }

    private List<String> collectSelectedNames(List<DbOption> options, boolean[] selections) {
        List<String> names = new ArrayList<>();
        for (int index = 0; index < options.size() && index < selections.length; index++) {
            if (selections[index]) {
                names.add(options.get(index).name);
            }
        }
        return names;
    }

    private String selectionSummary(List<String> values) {
        if (values.isEmpty()) {
            return "none selected";
        }

        StringBuilder summary = new StringBuilder();
        for (String value : values) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(value);
        }
        return summary.toString();
    }

    private List<Integer> collectSelectedIds(List<DbOption> options, boolean[] selections) {
        List<Integer> ids = new ArrayList<>();
        for (int index = 0; index < options.size() && index < selections.length; index++) {
            if (selections[index]) {
                ids.add(options.get(index).id);
            }
        }
        return ids;
    }

    private void applyClassSelection(
            ClassOption selectedClassOption,
            String[] classValue,
            int[] classIdValue,
            int[] selectedHitDice,
            TextView selectedClass,
            TextView hitDice,
            int[] abilityValues,
            int[] remainingAbilityPoints,
            TextView remainingAbilityPointsLabel,
            List<TextView> abilityValueViews,
            List<Button> abilityButtons,
            boolean[] abilityTouched,
            Button saveButton
    ) {
        ClassBaseStats baseStats = projectDatabase.getClassBaseStats(selectedClassOption.id);
        classValue[0] = selectedClassOption.name;
        classIdValue[0] = selectedClassOption.id;
        selectedHitDice[0] = selectedClassOption.hitDice;
        selectedClass.setText("Class: " + classValue[0]);
        hitDice.setText("Hit Dice: d" + selectedHitDice[0]);

        abilityValues[0] = baseStats.strength;
        abilityValues[1] = baseStats.dexterity;
        abilityValues[2] = baseStats.constitution;
        abilityValues[3] = baseStats.intelligence;
        abilityValues[4] = baseStats.charisma;
        abilityValues[5] = baseStats.wisdom;
        remainingAbilityPoints[0] = Math.max(0, TOTAL_ABILITY_POINTS - abilitySum(abilityValues));
        refreshAbilityValueViews(abilityValues, abilityValueViews);
        remainingAbilityPointsLabel.setText("Free Points: " + remainingAbilityPoints[0]);
        abilityTouched[0] = false;
        setButtonsEnabled(abilityButtons, true);
        saveButton.setEnabled(true);
    }

    private void refreshAbilityValueViews(int[] abilityValues, List<TextView> abilityValueViews) {
        for (int index = 0; index < abilityValues.length && index < abilityValueViews.size(); index++) {
            abilityValueViews.get(index).setText(String.valueOf(abilityValues[index]));
        }
    }

    private int abilitySum(int[] abilityValues) {
        int sum = 0;
        for (int value : abilityValues) {
            sum += value;
        }
        return sum;
    }

    private void setButtonsEnabled(List<Button> buttons, boolean enabled) {
        for (Button button : buttons) {
            button.setEnabled(enabled);
        }
    }

    private void addAbilityControl(
            GridLayout parent,
            String label,
            int[] abilityValues,
            int abilityIndex,
            int[] remainingPoints,
            TextView remainingPointsLabel,
            List<TextView> abilityValueViews,
            List<Button> abilityButtons,
            boolean[] abilityTouched
    ) {
        LinearLayout cell = verticalLayout(0);
        cell.setPadding(dp(6), dp(6), dp(6), dp(6));

        TextView name = bodyText(label);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        cell.addView(name);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);

        Button minusButton = secondaryButton("-");
        TextView value = bodyText(String.valueOf(abilityValues[abilityIndex]));
        value.setTextSize(20);
        value.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        value.setGravity(Gravity.CENTER);
        Button plusButton = secondaryButton("+");
        abilityValueViews.add(value);
        abilityButtons.add(minusButton);
        abilityButtons.add(plusButton);

        minusButton.setOnClickListener(view -> {
            if (abilityValues[abilityIndex] <= 0) {
                return;
            }
            abilityValues[abilityIndex]--;
            remainingPoints[0]++;
            abilityTouched[0] = true;
            value.setText(String.valueOf(abilityValues[abilityIndex]));
            remainingPointsLabel.setText("Free Points: " + remainingPoints[0]);
        });

        plusButton.setOnClickListener(view -> {
            if (remainingPoints[0] <= 0) {
                return;
            }
            abilityValues[abilityIndex]++;
            remainingPoints[0]--;
            abilityTouched[0] = true;
            value.setText(String.valueOf(abilityValues[abilityIndex]));
            remainingPointsLabel.setText("Free Points: " + remainingPoints[0]);
        });

        controls.addView(minusButton, new LinearLayout.LayoutParams(dp(44), dp(44)));
        controls.addView(value, new LinearLayout.LayoutParams(dp(54), dp(44)));
        controls.addView(plusButton, new LinearLayout.LayoutParams(dp(44), dp(44)));
        cell.addView(controls);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(0, dp(4), 0, dp(4));
        cell.setLayoutParams(params);
        parent.addView(cell);
    }

    private void addCounterControl(GridLayout parent, String label, int[] values, int valueIndex) {
        addCounterControl(parent, label, values, valueIndex, null);
    }

    private void addCounterControl(GridLayout parent, String label, int[] values, int valueIndex, List<Button> counterButtons) {
        LinearLayout cell = verticalLayout(0);
        cell.setPadding(dp(6), dp(6), dp(6), dp(6));

        TextView name = bodyText(label);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        cell.addView(name);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);

        Button minusButton = secondaryButton("-");
        TextView value = bodyText(String.valueOf(values[valueIndex]));
        value.setTextSize(20);
        value.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        value.setGravity(Gravity.CENTER);
        Button plusButton = secondaryButton("+");
        if (counterButtons != null) {
            counterButtons.add(minusButton);
            counterButtons.add(plusButton);
        }

        minusButton.setOnClickListener(view -> {
            if (values[valueIndex] <= 0) {
                return;
            }
            values[valueIndex]--;
            value.setText(String.valueOf(values[valueIndex]));
        });

        plusButton.setOnClickListener(view -> {
            values[valueIndex]++;
            value.setText(String.valueOf(values[valueIndex]));
        });

        controls.addView(minusButton, new LinearLayout.LayoutParams(dp(44), dp(44)));
        controls.addView(value, new LinearLayout.LayoutParams(dp(54), dp(44)));
        controls.addView(plusButton, new LinearLayout.LayoutParams(dp(44), dp(44)));
        cell.addView(controls);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(0, dp(4), 0, dp(4));
        cell.setLayoutParams(params);
        parent.addView(cell);
    }

    private TextInputEditText textInput(LinearLayout parent, String label, String hint) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(label);
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setSingleLine(true);
        input.setHint(hint);
        layout.addView(input);
        parent.addView(layout);
        return input;
    }

    private TextInputEditText multilineTextInput(LinearLayout parent, String label, String hint) {
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

    private TextInputEditText numberInput(GridLayout parent, String label, int defaultValue) {
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

    private TextInputEditText numberInput(LinearLayout parent, String label, int defaultValue) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(label);
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(defaultValue));
        layout.addView(input);
        parent.addView(layout);
        return input;
    }

    private LinearLayout verticalLayout(int spacing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        if (spacing > 0) {
            layout.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        }
        return layout;
    }

    private TextView title(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(28);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setTextColor(0xFF2B2118);
        return view;
    }

    private TextView sectionTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(19);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setTextColor(0xFF33261B);
        return view;
    }

    private TextView bodyText(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(16);
        view.setTextColor(0xFF5F5043);
        return view;
    }

    private Button primaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = primaryButton(text);
        button.setBackgroundColor(0xFFE4D7C7);
        button.setTextColor(0xFF2B2118);
        return button;
    }

    private String value(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private String valueOrDefault(EditText input, String defaultValue) {
        String value = value(input).trim();
        return value.isEmpty() ? defaultValue : value;
    }

    private int intValue(EditText input, int defaultValue) {
        try {
            return Integer.parseInt(value(input).trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private static long moneyAsCopper(int platinum, int gold, int silver, int copper) {
        return (((long) platinum * 100 + gold) * 100 + silver) * 100 + copper;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void loadCharacters() {
        characters.clear();
        String rawCharacters = preferences.getString(KEY_CHARACTERS, "[]");
        try {
            JSONArray array = new JSONArray(rawCharacters);
            for (int i = 0; i < array.length(); i++) {
                characters.add(DndCharacter.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException exception) {
            preferences.edit().remove(KEY_CHARACTERS).apply();
        }
    }

    private void saveCharacters() {
        JSONArray array = new JSONArray();
        for (DndCharacter character : characters) {
            array.put(character.toJson());
        }
        preferences.edit().putString(KEY_CHARACTERS, array.toString()).apply();
    }

    @Override
    public void onBackPressed() {
        if (selectedCharacter != null) {
            showCharacterSelect();
        } else {
            super.onBackPressed();
        }
    }

    private class InventorySlotAdapter extends RecyclerView.Adapter<InventorySlotAdapter.SlotViewHolder> {
        private final List<InventoryItem> slots;

        InventorySlotAdapter(List<InventoryItem> slots) {
            this.slots = slots;
        }

        @NonNull
        @Override
        public SlotViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            FrameLayout root = new FrameLayout(MainActivity.this);
            root.setBackgroundResource(R.drawable.dnd_empty_slot_bg);
            root.setPadding(dp(4), dp(4), dp(4), dp(4));

            ImageView icon = new ImageView(MainActivity.this);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            root.addView(icon, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
            ));

            TextView badge = new TextView(MainActivity.this);
            badge.setTextColor(0xFFFFFFFF);
            badge.setTextSize(11);
            badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            badge.setGravity(Gravity.CENTER);
            badge.setBackgroundColor(0xCC5F5043);
            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(dp(30), dp(20), Gravity.BOTTOM | Gravity.RIGHT);
            root.addView(badge, badgeParams);

            TextView equipped = new TextView(MainActivity.this);
            equipped.setText("E");
            equipped.setTextColor(0xFFFFFFFF);
            equipped.setTextSize(11);
            equipped.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            equipped.setGravity(Gravity.CENTER);
            equipped.setBackgroundColor(0xCC3F7A3F);
            root.addView(equipped, new FrameLayout.LayoutParams(dp(20), dp(20), Gravity.TOP | Gravity.LEFT));

            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    dp(78)
            );
            params.setMargins(dp(3), dp(3), dp(3), dp(3));
            root.setLayoutParams(params);
            return new SlotViewHolder(root, icon, badge, equipped);
        }

        @Override
        public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
            InventoryItem item = slots.get(position);
            holder.icon.setImageDrawable(null);
            holder.badge.setVisibility(View.GONE);
            holder.equipped.setVisibility(View.GONE);
            if (item == null) {
                holder.root.setContentDescription(INVENTORY_CATEGORIES[selectedInventoryCategory] + " empty slot");
                holder.root.setOnClickListener(view -> showAddItemDialog(activeInventoryItemType()));
                return;
            }
            if (item.icon != null) {
                holder.icon.setImageBitmap(item.icon);
            }
            holder.root.setContentDescription(item.name);
            if (item.quantity > 1) {
                holder.badge.setText("x" + item.quantity);
                holder.badge.setVisibility(View.VISIBLE);
            }
            if (item.equipped) {
                holder.equipped.setVisibility(View.VISIBLE);
            }
            holder.root.setOnClickListener(view -> showItemDetails(item.bagItemId));
        }

        @Override
        public int getItemCount() {
            return slots.size();
        }

        class SlotViewHolder extends RecyclerView.ViewHolder {
            final FrameLayout root;
            final ImageView icon;
            final TextView badge;
            final TextView equipped;

            SlotViewHolder(FrameLayout root, ImageView icon, TextView badge, TextView equipped) {
                super(root);
                this.root = root;
                this.icon = icon;
                this.badge = badge;
                this.equipped = equipped;
            }
        }
    }

    private static class SpellDefinition {
        final String name;
        final int level;
        final String classes;
        final String description;

        SpellDefinition(String name, int level, String classes, String description) {
            this.name = name;
            this.level = level;
            this.classes = classes;
            this.description = description;
        }
    }

    private static class DndCharacter {
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
            setMoneyFromCopper(MainActivity.moneyAsCopper(platinumCoins, goldCoins, silverCoins, copperCoins));

        }

        JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {
                object.put("name", name);
                object.put("class", characterClass);
                object.put("level", level);
                object.put("strength", strength);
                object.put("dexterity", dexterity);
                object.put("constitution", constitution);
                object.put("intelligence", intelligence);
                object.put("charisma", charisma);
                object.put("wisdom", wisdom);
                object.put("speed", speed);
                object.put("armorClass", armorClass);
                object.put("race", race);
                object.put("background", background);
                object.put("alignment", alignment);
                object.put("currentHp", currentHp);
                object.put("maxHp", maxHp);
                object.put("temporaryHp", temporaryHp);
                object.put("hitDice", hitDice);
                object.put("proficiencyBonus", proficiencyBonus);
                object.put("perception", perception);
                object.put("featuresAndTraits", featuresAndTraits);
                object.put("personalityTraits", personalityTraits);
                object.put("ideals", ideals);
                object.put("bonds", bonds);
                object.put("flaws", flaws);
                object.put("initiative", initiative);
                object.put("platinumCoins", platinumCoins);
                object.put("goldCoins", goldCoins);
                object.put("silverCoins", silverCoins);
                object.put("copperCoins", copperCoins);
                object.put("databaseCharacterId", databaseCharacterId);
                JSONArray languagesArray = new JSONArray();
                for (String language : languages) {
                    languagesArray.put(language);
                }
                object.put("languages", languagesArray);
                JSONArray savingThrowsArray = new JSONArray();
                for (String savingThrow : savingThrows) {
                    savingThrowsArray.put(savingThrow);
                }
                object.put("savingThrows", savingThrowsArray);

                JSONArray spellbookArray = new JSONArray();
                for (List<String> levelSpells : spellbook) {
                    JSONArray spellsArray = new JSONArray();
                    for (String spell : levelSpells) {
                        spellsArray.put(spell);
                    }
                    spellbookArray.put(spellsArray);
                }
                object.put("spellbook", spellbookArray);
                object.put("maxSpellUses", maxSpellUses);
                object.put("currentSpellUses", currentSpellUses);
            } catch (JSONException ignored) {
                // Values are local primitive fields, so JSON errors are not expected here.
            }
            return object;
        }

        static DndCharacter fromJson(@NonNull JSONObject object) {
            DndCharacter character = new DndCharacter(
                    object.optString("name", "No Name"),
                    object.optString("class", "No Class"),
                    object.optInt("level", 1),
                    object.optInt("strength", 10),
                    object.optInt("dexterity", 10),
                    object.optInt("constitution", 10),
                    object.optInt("intelligence", 10),
                    object.optInt("charisma", 10),
                    object.optInt("wisdom", 10),
                    object.optInt("speed", 30),
                    object.optInt("armorClass", 10),
                    object.optString("race", "Not specified"),
                    object.optString("background", "Not specified"),
                    object.optString("alignment", "Not specified"),
                    object.optInt("currentHp", 10),
                    object.optInt("maxHp", 10),
                    object.optInt("temporaryHp", 0),
                    object.optString("hitDice", "d8"),
                    object.optInt("proficiencyBonus", 2),
                    object.optInt("perception", 10),
                    object.optString("featuresAndTraits", "None"),
                    object.optString("personalityTraits", "None"),
                    object.optString("ideals", "None"),
                    object.optString("bonds", "None"),
                    object.optString("flaws", "None"),
                    object.optInt("initiative", 0),
                    readLanguages(object.optJSONArray("languages")),
                    readSavingThrows(object.optJSONArray("savingThrows")),
                    readSpellbook(object.optJSONArray("spellbook")),
                    object.optInt("maxSpellUses", maxSpellUsesForLevel(object.optInt("level", 1))),
                    object.optInt("currentSpellUses", object.optInt("maxSpellUses", maxSpellUsesForLevel(object.optInt("level", 1)))),
                    object.optInt("platinumCoins", 0),
                    object.optInt("goldCoins", 0),
                    object.optInt("silverCoins", 0),
                    object.optInt("copperCoins", 0)
            );
            character.databaseCharacterId = object.optInt("databaseCharacterId", 0);
            return character;
        }

        private static int maxSpellUsesForLevel(int level) {
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
            return MainActivity.moneyAsCopper(platinumCoins, goldCoins, silverCoins, copperCoins);
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

        private static List<String> readSavingThrows(JSONArray savedSavingThrows) {
            List<String> savingThrows = new ArrayList<>();
            if (savedSavingThrows == null) {
                return savingThrows;
            }

            for (int index = 0; index < savedSavingThrows.length(); index++) {
                String savingThrow = savedSavingThrows.optString(index, "").trim();
                if (!savingThrow.isEmpty()) {
                    if ("Constitution".equals(savingThrow)) {
                        savingThrow = "Saving Throw (Constitution)";
                    }
                    savingThrows.add(savingThrow);
                }
            }
            return savingThrows;
        }

        private static List<String> readLanguages(JSONArray savedLanguages) {
            List<String> languages = new ArrayList<>();
            if (savedLanguages == null) {
                return languages;
            }

            for (int index = 0; index < savedLanguages.length(); index++) {
                String language = savedLanguages.optString(index, "").trim();
                if (!language.isEmpty()) {
                    languages.add(language);
                }
            }
            return languages;

        }

        private static List<List<String>> createEmptySpellbook() {
            List<List<String>> spellbook = new ArrayList<>();
            for (int level = 0; level <= 9; level++) {
                spellbook.add(new ArrayList<>());
            }
            return spellbook;
        }

        private static List<List<String>> readSpellbook(JSONArray savedSpellbook) {
            List<List<String>> spellbook = createEmptySpellbook();
            if (savedSpellbook == null) {
                return spellbook;
            }

            for (int level = 0; level <= 9 && level < savedSpellbook.length(); level++) {
                JSONArray spells = savedSpellbook.optJSONArray(level);
                if (spells == null) {
                    continue;
                }
                for (int index = 0; index < spells.length(); index++) {
                    String spell = spells.optString(index, "").trim();
                    if (!spell.isEmpty()) {
                        spellbook.get(level).add(spell);
                    }
                }
            }
            return spellbook;
        }
    }

    private static class AspectRatioFrameLayout extends FrameLayout {
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
