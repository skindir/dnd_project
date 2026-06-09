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

abstract class CharacterStatsActivity extends SpellbookActivity {

    // Character stats window: decorative sheet header.
    protected void addSheetHeader(LinearLayout parent) {
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

    // Character stats window: identity fields such as name, class, race, and background.
    protected void addIdentitySection(LinearLayout parent) {
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

    // Character stats window: armor class, hit points, speed, and other combat values.
    protected void addCombatSection(LinearLayout parent) {
        LinearLayout section = sheetSection("Combat Stats");
        GridLayout combatGrid = new GridLayout(this);
        combatGrid.setColumnCount(wideLayout() ? 5 : 2);
        combatGrid.setUseDefaultMargins(false);
        addCombatCell(
                combatGrid,
                "Armor Class",
                String.valueOf(selectedCharacter.armorClass),
                true
        );
        addCombatCell(
                combatGrid,
                "Initiative",
                String.valueOf(selectedCharacter.initiative),
                false
        );
        addCombatCell(combatGrid, "Speed", String.valueOf(selectedCharacter.speed), false);
        addCombatCell(
                combatGrid,
                "Proficiency Bonus",
                String.valueOf(selectedCharacter.proficiencyBonus),
                false
        );
        addCombatCell(
                combatGrid,
                "Passive Wisdom",
                String.valueOf(selectedCharacter.perception),
                false
        );
        section.addView(combatGrid);

        LinearLayout hp = new LinearLayout(this);
        hp.setOrientation(wideLayout() ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        hp.setPadding(0, dp(8), 0, 0);
        addHpPanel(
                hp,
                "Current Hit Points",
                String.valueOf(selectedCharacter.currentHp),
                "Maximum: " + selectedCharacter.maxHp
        );
        addHpPanel(
                hp,
                "Temporary Hit Points",
                String.valueOf(selectedCharacter.temporaryHp),
                "Protection beyond maximum"
        );
        addHpPanel(hp, "Hit Dice", selectedCharacter.hitDice, "Total: " + selectedCharacter.level);
        section.addView(hp);
        parent.addView(section);
    }

    // Character stats window: personality, ideals, bonds, flaws, and traits.
    protected void addCharacterNotesSection(LinearLayout parent) {
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

    // Character stats window: ability scores, saving throws, and skill proficiencies.
    protected void addAbilitySection(LinearLayout parent, List<String> selectedSavingThrows) {
        LinearLayout section = sheetSection("Ability Scores");

        addAbilityRow(
                section,
                "Strength",
                "STR",
                selectedCharacter.strength,
                new String[]{"Saving Throw (Strength)"},
                new String[]{"Athletics"},
                selectedSavingThrows
        );
        addAbilityRow(
                section,
                "Dexterity",
                "DEX",
                selectedCharacter.dexterity,
                new String[]{"Saving Throw (Dexterity)"},
                new String[]{"Acrobatics", "Sleight of Hand", "Stealth"},
                selectedSavingThrows
        );
        addAbilityRow(
                section,
                "Constitution",
                "CON",
                selectedCharacter.constitution,
                new String[]{"Saving Throw (Constitution)"},
                new String[]{},
                selectedSavingThrows
        );
        addAbilityRow(
                section,
                "Intelligence",
                "INT",
                selectedCharacter.intelligence,
                new String[]{"Saving Throw (Intelligence)"},
                new String[]{"Arcana", "History", "Investigation", "Nature", "Religion"},
                selectedSavingThrows
        );
        addAbilityRow(
                section,
                "Wisdom",
                "WIS",
                selectedCharacter.wisdom,
                new String[]{"Saving Throw (Wisdom)"},
                new String[]{"Animal Handling", "Insight", "Medicine", "Perception", "Survival"},
                selectedSavingThrows
        );
        addAbilityRow(
                section,
                "Charisma",
                "CHA",
                selectedCharacter.charisma,
                new String[]{"Saving Throw (Charisma)"},
                new String[]{"Deception", "Intimidation", "Performance", "Persuasion"},
                selectedSavingThrows
        );

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

    // Character stats window: known languages table.
    protected void addLanguagesTable(LinearLayout parent, List<String> languages) {
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
}
