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

abstract class SpellbookActivity extends InventoryActivity {

    // Spellbook: built-in spell library used when adding spells to a character.
    protected static final SpellDefinition[] SPELL_LIBRARY = {
            new SpellDefinition(
                    "Fire Bolt",
                    0,
                    "Wizard,Sorcerer,Artificer",
                    "Ranged spell attack. Deals fire damage. Cantrip, does not spend spell uses."
            ),
            new SpellDefinition(
                    "Mage Hand",
                    0,
                    "Wizard,Sorcerer,Warlock,Bard,Artificer",
                    "Creates a spectral hand for simple interactions. Cantrip, does not spend spell uses."
            ),
            new SpellDefinition(
                    "Guidance",
                    0,
                    "Cleric,Druid,Artificer",
                    "Adds 1d4 to one ability check. Cantrip, does not spend spell uses."
            ),
            new SpellDefinition(
                    "Sacred Flame",
                    0,
                    "Cleric",
                    "Radiant flame forces a Dexterity save. Cantrip, does not spend spell uses."
            ),
            new SpellDefinition(
                    "Magic Missile",
                    1,
                    "Wizard,Sorcerer",
                    "Three darts automatically hit targets and deal force damage."
            ),
            new SpellDefinition(
                    "Cure Wounds",
                    1,
                    "Cleric,Druid,Bard,Paladin,Ranger,Artificer",
                    "Touch a creature to restore hit points."
            ),
            new SpellDefinition(
                    "Shield",
                    1,
                    "Wizard,Sorcerer,Artificer",
                    "Reaction. Gain +5 AC until the start of your next turn."
            ),
            new SpellDefinition(
                    "Thunderwave",
                    1,
                    "Wizard,Sorcerer,Bard,Druid",
                    "A wave of thunder pushes creatures and deals thunder damage."
            ),
            new SpellDefinition(
                    "Misty Step",
                    2,
                    "Wizard,Sorcerer,Warlock",
                    "Bonus action teleport up to 30 feet."
            ),
            new SpellDefinition(
                    "Scorching Ray",
                    2,
                    "Wizard,Sorcerer",
                    "Create three fire rays and make ranged spell attacks."
            ),
            new SpellDefinition(
                    "Lesser Restoration",
                    2,
                    "Cleric,Druid,Bard,Paladin,Ranger,Artificer",
                    "End one disease or condition on a creature."
            ),
            new SpellDefinition(
                    "Fireball",
                    3,
                    "Wizard,Sorcerer",
                    "A bright explosion deals fire damage in a large area."
            ),
            new SpellDefinition(
                    "Counterspell",
                    3,
                    "Wizard,Sorcerer,Warlock",
                    "Reaction. Interrupt a creature casting a spell."
            ),
            new SpellDefinition(
                    "Revivify",
                    3,
                    "Cleric,Paladin,Artificer",
                    "Return a recently dead creature to life."
            ),
            new SpellDefinition(
                    "Polymorph",
                    4,
                    "Wizard,Sorcerer,Bard,Druid",
                    "Transform a creature into a beast."
            ),
            new SpellDefinition(
                    "Wall of Fire",
                    4,
                    "Wizard,Sorcerer,Druid",
                    "Create a wall of flame that deals fire damage."
            ),
            new SpellDefinition(
                    "Cone of Cold",
                    5,
                    "Wizard,Sorcerer",
                    "A blast of cold air deals cold damage in a cone."
            ),
            new SpellDefinition(
                    "Mass Cure Wounds",
                    5,
                    "Cleric,Druid,Bard",
                    "Restore hit points to several creatures at once."
            ),
            new SpellDefinition(
                    "Disintegrate",
                    6,
                    "Wizard,Sorcerer",
                    "A green ray deals heavy force damage."
            ),
            new SpellDefinition(
                    "Heal",
                    6,
                    "Cleric,Druid",
                    "A creature regains a large amount of hit points."
            ),
            new SpellDefinition(
                    "Teleport",
                    7,
                    "Wizard,Sorcerer,Bard",
                    "Instantly transport yourself and companions."
            ),
            new SpellDefinition(
                    "Power Word Stun",
                    8,
                    "Wizard,Sorcerer,Bard,Warlock",
                    "Stun a creature with 150 hit points or fewer."
            ),
            new SpellDefinition(
                    "Wish",
                    9,
                    "Wizard,Sorcerer",
                    "The mightiest spell, capable of reshaping reality."
            )
    };

    // Spellbook: restores available spell uses after rest.
    protected void restoreSpellUses(String restName) {
        selectedCharacter.currentSpellUses = selectedCharacter.maxSpellUses;
        saveCharacters();
        Toast.makeText(this, restName + ": spell uses restored", Toast.LENGTH_SHORT).show();
        showCharacterSheet();
    }

    // Spellbook: indicator for remaining spell uses.
    protected void addSpellUseCells(LinearLayout parent) {
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

    // Spellbook: list of spell levels for the character.
    protected void addSpellLevels(LinearLayout parent) {
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

    // Spellbook: selects a new spell from the available library.
    protected void showAddSpellDialog(int presetLevel) {
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
}
