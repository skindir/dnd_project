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

abstract class CharacterCreationActivity extends CharacterListActivity {

    // Character creation: name, class, race, background, skills, and ability score form.
    protected void showCreateCharacter() {
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
                    .setMultiChoiceItems(
                            optionNames(languageOptions),
                            dialogSelections,
                            (dialog, which, isChecked) -> dialogSelections[which] = isChecked
                    )
                    .setPositiveButton("Done", (dialog, which) -> {
                        System.arraycopy(
                                dialogSelections,
                                0,
                                languageSelections,
                                0,
                                languageSelections.length
                        );
                        selectedLanguagesLabel.setText(
                                "Languages: "
                                        + selectionSummary(collectSelectedNames(languageOptions, languageSelections))
                        );
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
                    .setMultiChoiceItems(
                            optionNames(proficiencyOptions),
                            dialogSelections,
                            (dialog, which, isChecked) -> dialogSelections[which] = isChecked
                    )
                    .setPositiveButton("Done", (dialog, which) -> {
                        System.arraycopy(
                                dialogSelections,
                                0,
                                savingThrowSelections,
                                0,
                                savingThrowSelections.length
                        );
                        selectedSavingThrowsLabel.setText(
                                "Saving Throws: "
                                        + selectionSummary(collectSelectedNames(
                                                proficiencyOptions,
                                                savingThrowSelections
                                        ))
                        );
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

        addAbilityControl(
                abilityGrid,
                "Strength",
                abilityValues,
                0,
                remainingAbilityPoints,
                remainingAbilityPointsLabel,
                abilityValueViews,
                abilityButtons,
                abilityTouched
        );
        addAbilityControl(
                abilityGrid,
                "Dexterity",
                abilityValues,
                1,
                remainingAbilityPoints,
                remainingAbilityPointsLabel,
                abilityValueViews,
                abilityButtons,
                abilityTouched
        );
        addAbilityControl(
                abilityGrid,
                "Constitution",
                abilityValues,
                2,
                remainingAbilityPoints,
                remainingAbilityPointsLabel,
                abilityValueViews,
                abilityButtons,
                abilityTouched
        );
        addAbilityControl(
                abilityGrid,
                "Intelligence",
                abilityValues,
                3,
                remainingAbilityPoints,
                remainingAbilityPointsLabel,
                abilityValueViews,
                abilityButtons,
                abilityTouched
        );
        addAbilityControl(
                abilityGrid,
                "Charisma",
                abilityValues,
                4,
                remainingAbilityPoints,
                remainingAbilityPointsLabel,
                abilityValueViews,
                abilityButtons,
                abilityTouched
        );
        addAbilityControl(
                abilityGrid,
                "Wisdom",
                abilityValues,
                5,
                remainingAbilityPoints,
                remainingAbilityPointsLabel,
                abilityValueViews,
                abilityButtons,
                abilityTouched
        );
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
}
