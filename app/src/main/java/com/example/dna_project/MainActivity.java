package com.example.dna_project;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "dnd_characters";
    private static final String KEY_CHARACTERS = "characters";
    private static final int MAX_CHARACTERS = 15;

    private static final int TAB_INVENTORY = 1;
    private static final int TAB_STATS = 2;
    private static final int TAB_SPELLBOOK = 3;
    private static final float EQUIPMENT_IMAGE_ASPECT_RATIO = 1536f / 1024f;
    private static final String[] CHARACTER_CLASSES = {
            "Бард",
            "Варвар",
            "Воин",
            "Волшебник",
            "Друид",
            "Жрец",
            "Изобретатель",
            "Колдун",
            "Монах",
            "Паладин",
            "Плут",
            "Следопыт",
            "Чародей"
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

    private final List<DndCharacter> characters = new ArrayList<>();
    private SharedPreferences preferences;
    private FrameLayout root;
    private DndCharacter selectedCharacter;
    private int selectedTab = TAB_STATS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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

        TextView title = title("Выбор персонажа");
        screen.addView(title);

        TextView counter = bodyText("Персонажей: " + characters.size() + " / " + MAX_CHARACTERS);
        counter.setPadding(0, dp(4), 0, dp(16));
        screen.addView(counter);

        Button createButton = primaryButton("Создать нового персонажа");
        createButton.setEnabled(characters.size() < MAX_CHARACTERS);
        createButton.setOnClickListener(view -> {
            if (characters.size() >= MAX_CHARACTERS) {
                Toast.makeText(this, "Можно создать максимум 15 персонажей", Toast.LENGTH_SHORT).show();
            } else {
                showCreateCharacter();
            }
        });
        screen.addView(createButton);

        if (characters.isEmpty()) {
            TextView empty = bodyText("Пока нет персонажей. Создайте первого героя для кампании.");
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

        screen.addView(title("Новый персонаж"));

        TextInputEditText nameInput = textInput(screen, "Имя персонажа", "Элара");
        TextView selectedClass = bodyText("Класс не выбран");
        selectedClass.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedClass.setPadding(0, dp(4), 0, dp(4));
        Button classButton = secondaryButton("Добавить класс");
        final String[] classValue = {""};
        classButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Выберите класс")
                .setItems(CHARACTER_CLASSES, (dialog, which) -> {
                    classValue[0] = CHARACTER_CLASSES[which];
                    selectedClass.setText("Класс: " + classValue[0]);
                })
                .show());
        screen.addView(selectedClass);
        screen.addView(classButton);
        TextInputEditText level = numberInput(screen, "Уровень", 1);

        GridLayout statGrid = new GridLayout(this);
        statGrid.setColumnCount(2);
        statGrid.setUseDefaultMargins(true);
        screen.addView(statGrid);

        TextInputEditText strength = numberInput(statGrid, "Сила", 10);
        TextInputEditText dexterity = numberInput(statGrid, "Ловкость", 10);
        TextInputEditText intelligence = numberInput(statGrid, "Интеллект", 10);
        TextInputEditText charisma = numberInput(statGrid, "Харизма", 10);
        TextInputEditText wisdom = numberInput(statGrid, "Мудрость", 10);
        TextInputEditText speed = numberInput(statGrid, "Скорость", 30);
        TextInputEditText armorClass = numberInput(statGrid, "Класс брони", 10);

        Button saveButton = primaryButton("Сохранить персонажа");
        saveButton.setOnClickListener(view -> {
            String name = value(nameInput).trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Введите имя персонажа", Toast.LENGTH_SHORT).show();
                return;
            }

            DndCharacter character = new DndCharacter(
                    name,
                    classValue[0].isEmpty() ? "Без класса" : classValue[0],
                    intValue(level, 1),
                    intValue(strength, 10),
                    intValue(dexterity, 10),
                    intValue(intelligence, 10),
                    intValue(charisma, 10),
                    intValue(wisdom, 10),
                    intValue(speed, 30),
                    intValue(armorClass, 10)
            );
            characters.add(character);
            saveCharacters();
            selectedCharacter = character;
            selectedTab = TAB_STATS;
            showCharacterSheet();
        });
        screen.addView(saveButton);

        Button backButton = secondaryButton("Назад к выбору");
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

        Button selectOther = secondaryButton("Сменить персонажа");
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
            body.addView(sectionTitle("Характеристики"));
            addStat(body, "Имя", selectedCharacter.name);
            addStat(body, "Класс", selectedCharacter.characterClass);
            addStat(body, "Уровень", selectedCharacter.level);
            addStat(body, "Ловкость", selectedCharacter.dexterity);
            addStat(body, "Сила", selectedCharacter.strength);
            addStat(body, "Интеллект", selectedCharacter.intelligence);
            addStat(body, "Харизма", selectedCharacter.charisma);
            addStat(body, "Мудрость", selectedCharacter.wisdom);
            addStat(body, "Скорость", selectedCharacter.speed);
            addStat(body, "Класс брони", selectedCharacter.armorClass);
        } else if (selectedTab == TAB_INVENTORY) {
            body.addView(sectionTitle("Инвентарь"));
            body.addView(sectionTitle("Надето на персонаже"));
            addEquipmentLayout(body);
            body.addView(sectionTitle("В рюкзаке"));
            body.addView(bodyText("Сейчас инвентарь пуст."));
        } else {
            body.addView(sectionTitle("Книга заклинаний"));
            Button addSpell = primaryButton("Добавить заклинание");
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
        TextView details = bodyText(character.characterClass + " | Уровень " + character.level);
        textBox.addView(name);
        textBox.addView(details);
        row.addView(textBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button deleteButton = secondaryButton("Удалить");
        deleteButton.setOnClickListener(view -> confirmDeleteCharacter(character));
        row.addView(deleteButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        row.setOnClickListener(view -> {
            selectedCharacter = character;
            selectedTab = TAB_STATS;
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
                .setTitle("Удалить персонажа?")
                .setMessage(character.name + " будет удалён из списка.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    characters.remove(character);
                    saveCharacters();
                    showCharacterSelect();
                })
                .setNegativeButton("Отмена", null)
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

        addEquipmentSlot(equipment, "Шлем", 419, 52, 160, 145);
        addEquipmentSlot(equipment, "Наплечник левый", 102, 215, 162, 146);
        addEquipmentSlot(equipment, "Наплечник правый", 760, 215, 162, 146);
        addEquipmentSlot(equipment, "Серьга 1", 72, 407, 128, 126);
        addEquipmentSlot(equipment, "Серьга 2", 784, 407, 128, 126);
        addEquipmentSlot(equipment, "Кулон", 428, 384, 138, 120);
        addEquipmentSlot(equipment, "Перчатка левая", 56, 620, 118, 126);
        addEquipmentSlot(equipment, "Перчатка правая", 850, 620, 118, 126);
        addEquipmentSlot(equipment, "Нагрудник", 407, 555, 174, 154);
        addEquipmentSlot(equipment, "Пояс", 423, 790, 138, 112);
        addEquipmentSlot(equipment, "Первичное оружие", 53, 860, 121, 470);
        addEquipmentSlot(equipment, "Вторичное оружие", 852, 860, 120, 470);
        addEquipmentSlot(equipment, "Кольцо 1", 210, 956, 86, 108);
        addEquipmentSlot(equipment, "Кольцо 2", 753, 956, 88, 108);
        addEquipmentSlot(equipment, "Штаны", 425, 1047, 142, 138);
        addEquipmentSlot(equipment, "Ботинки", 429, 1296, 138, 114);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(12));
        parent.addView(equipment, params);
    }

    private void addEquipmentSlot(FrameLayout equipment, String slot, int left, int top, int width, int height) {
        View slotView = new View(this);
        slotView.setBackgroundResource(R.drawable.dnd_empty_slot_bg);
        slotView.setAlpha(0.42f);
        slotView.setContentDescription(slot);
        slotView.setOnClickListener(view -> Toast.makeText(this, slot + ": пусто", Toast.LENGTH_SHORT).show());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1, 1);
        equipment.addView(slotView, params);
        equipment.addOnLayoutChangeListener((view, leftEdge, topEdge, rightEdge, bottomEdge, oldLeft, oldTop, oldRight, oldBottom) -> {
            int equipmentWidth = rightEdge - leftEdge;
            int equipmentHeight = bottomEdge - topEdge;
            FrameLayout.LayoutParams updatedParams = (FrameLayout.LayoutParams) slotView.getLayoutParams();
            updatedParams.width = Math.round(equipmentWidth * (width / 1024f));
            updatedParams.height = Math.round(equipmentHeight * (height / 1536f));
            updatedParams.leftMargin = Math.round(equipmentWidth * (left / 1024f));
            updatedParams.topMargin = Math.round(equipmentHeight * (top / 1536f));
            slotView.setLayoutParams(updatedParams);
        });
    }

    private int classImageResource(String characterClass) {
        switch (characterClass) {
            case "Бард":
                return R.drawable.class_bard;
            case "Варвар":
                return R.drawable.class_barbarian;
            case "Воин":
                return R.drawable.class_warrior;
            case "Волшебник":
                return R.drawable.class_wizard;
            case "Друид":
                return R.drawable.class_druid;
            case "Жрец":
                return R.drawable.class_cleric;
            case "Изобретатель":
                return R.drawable.class_artificer;
            case "Колдун":
                return R.drawable.class_warlock;
            case "Монах":
                return R.drawable.class_monk;
            case "Паладин":
                return R.drawable.class_paladin;
            case "Плут":
                return R.drawable.class_rogue;
            case "Следопыт":
                return R.drawable.class_ranger;
            case "Чародей":
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

        TextView title = sectionTitle("Уровень " + spellLevel);
        header.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button addButton = secondaryButton("+");
        addButton.setTextSize(22);
        addButton.setOnClickListener(view -> showAddSpellDialog(spellLevel));
        header.addView(addButton, new LinearLayout.LayoutParams(dp(48), dp(48)));
        frame.addView(header);

        List<String> spells = selectedCharacter.spellbook.get(spellLevel);
        if (spells.isEmpty()) {
            frame.addView(bodyText("Список пуст."));
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
            Toast.makeText(this, "Нет доступных заклинаний для добавления", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] spellNames = new String[availableSpells.size()];
        for (int index = 0; index < availableSpells.size(); index++) {
            SpellDefinition spell = availableSpells.get(index);
            spellNames[index] = "Level " + spell.level + " - " + spell.name;
        }

        new AlertDialog.Builder(this)
                .setTitle(presetLevel >= 0 ? "Доступные spell level " + presetLevel : "Доступные spell")
                .setItems(spellNames, (dialog, which) -> {
                    SpellDefinition spell = availableSpells.get(which);
                    selectedCharacter.spellbook.get(spell.level).add(spell.name);
                    saveCharacters();
                    showCharacterSheet();
                })
                .setNegativeButton("Отмена", null)
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

        EditText spellName = spellDialogInput(form, "1. Название заклинания", "Например: Огненный шар", false);
        EditText spellLevel = null;
        if (presetLevel < 0) {
            spellLevel = spellDialogInput(form, "2. Уровень заклинания", "0-9", true);
            spellLevel.setText("0");
        } else {
            TextView fixedLevel = bodyText("2. Уровень заклинания: " + presetLevel);
            fixedLevel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            fixedLevel.setPadding(0, dp(8), 0, dp(8));
            form.addView(fixedLevel);
        }

        EditText spellClass = spellDialogInput(form, "3. Класс персонажа", "Например: Wizard", false);
        EditText spellRange = spellDialogInput(form, "4. Дистанция действия", "Например: 150 ft", false);
        EditText spellAttackType = spellDialogInput(form, "5. Тип атаки", "Например: saving throw", false);
        EditText spellDamageType = spellDialogInput(form, "6. Тип урона", "Например: Fire", false);
        EditText spellDamage = spellDialogInput(form, "7. Формула урона", "Например: 8d6", false);

        EditText finalSpellLevel = spellLevel;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(presetLevel >= 0 ? "Добавить в уровень " + presetLevel + " - версия 3" : "Добавить заклинание - версия 3")
                .setView(scrollView)
                .setPositiveButton("Добавить", null)
                .setNegativeButton("Отмена", null)
                .create();

        dialog.setOnShowListener(openDialog -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String name = value(spellName).trim();
                if (name.isEmpty()) {
                    spellName.setError("Введите название");
                    return;
                }

                int level = presetLevel >= 0 ? presetLevel : intValue(finalSpellLevel, 0);
                if (level < 0 || level > 9) {
                    if (finalSpellLevel != null) {
                        finalSpellLevel.setError("Уровень должен быть от 0 до 9");
                    }
                    return;
                }

                String className = valueOrDefault(spellClass, "Без класса");
                String range = valueOrDefault(spellRange, "Без дистанции");
                String attackType = valueOrDefault(spellAttackType, "Без типа атаки");
                String damageType = valueOrDefault(spellDamageType, "Без типа урона");
                String damage = valueOrDefault(spellDamage, "Без урона");
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

        TextInputEditText spellName = textInput(form, "Название заклинания", "Огненный шар");
        TextInputEditText spellLevel = null;
        if (presetLevel < 0) {
            spellLevel = numberInput(form, "Уровень заклинания 0-9", 0);
        }

        TextInputEditText finalSpellLevel = spellLevel;
        new AlertDialog.Builder(this)
                .setTitle(presetLevel >= 0 ? "Добавить в уровень " + presetLevel : "Добавить заклинание")
                .setView(form)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String name = value(spellName).trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Введите название заклинания", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int level = presetLevel >= 0 ? presetLevel : intValue(finalSpellLevel, 0);
                    if (level < 0 || level > 9) {
                        Toast.makeText(this, "Уровень должен быть от 0 до 9", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedCharacter.spellbook.get(level).add(name);
                    saveCharacters();
                    showCharacterSheet();
                })
                .setNegativeButton("Отмена", null)
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
        if (characterClass.contains("wizard") || characterClass.contains("волшеб")) {
            return spell.classes.contains("Wizard");
        } else if (characterClass.contains("sorcerer") || characterClass.contains("чарод")) {
            return spell.classes.contains("Sorcerer");
        } else if (characterClass.contains("warlock") || characterClass.contains("колдун")) {
            return spell.classes.contains("Warlock");
        } else if (characterClass.contains("cleric") || characterClass.contains("жрец")) {
            return spell.classes.contains("Cleric");
        } else if (characterClass.contains("druid") || characterClass.contains("друид")) {
            return spell.classes.contains("Druid");
        } else if (characterClass.contains("bard") || characterClass.contains("бард")) {
            return spell.classes.contains("Bard");
        } else if (characterClass.contains("paladin") || characterClass.contains("палад")) {
            return spell.classes.contains("Paladin");
        } else if (characterClass.contains("ranger") || characterClass.contains("следоп")) {
            return spell.classes.contains("Ranger");
        } else if (characterClass.contains("artificer") || characterClass.contains("изобрет")) {
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
                ? "Описание недоступно для старого заклинания."
                : spell.description;
        boolean cantrip = spellLevel == 0;
        String message = description + "\n\nLevel: " + spellLevel + "\nUses: "
                + selectedCharacter.currentSpellUses + " / " + selectedCharacter.maxSpellUses
                + (cantrip ? "\nCantrip: does not spend uses." : "");

        new AlertDialog.Builder(this)
                .setTitle(spellName)
                .setMessage(message)
                .setPositiveButton("Применить", (dialog, which) -> castSpell(spellLevel, spellName))
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void castSpell(int spellLevel, String spellName) {
        if (spellLevel > 0) {
            if (selectedCharacter.currentSpellUses <= 0) {
                Toast.makeText(this, "Нет доступных spell uses. Сделайте rest.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedCharacter.currentSpellUses--;
        }
        saveCharacters();
        Toast.makeText(this, spellName + " применен", Toast.LENGTH_SHORT).show();
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
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackgroundColor(0xFFF7F2EA);

        TextView name = bodyText(label);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        row.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView number = sectionTitle(value);
        number.setGravity(Gravity.END);
        row.addView(number);

        parent.addView(row);
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
        final int intelligence;
        final int charisma;
        final int wisdom;
        final int speed;
        final int armorClass;
        final List<List<String>> spellbook;
        final int maxSpellUses;
        int currentSpellUses;

        DndCharacter(
                String name,
                String characterClass,
                int level,
                int strength,
                int dexterity,
                int intelligence,
                int charisma,
                int wisdom,
                int speed,
                int armorClass
        ) {
            this(name, characterClass, level, strength, dexterity, intelligence, charisma, wisdom, speed, armorClass, createEmptySpellbook(), maxSpellUsesForLevel(level), maxSpellUsesForLevel(level));
        }

        DndCharacter(
                String name,
                String characterClass,
                int level,
                int strength,
                int dexterity,
                int intelligence,
                int charisma,
                int wisdom,
                int speed,
                int armorClass,
                List<List<String>> spellbook
        ) {
            this(name, characterClass, level, strength, dexterity, intelligence, charisma, wisdom, speed, armorClass, spellbook, maxSpellUsesForLevel(level), maxSpellUsesForLevel(level));
        }

        DndCharacter(
                String name,
                String characterClass,
                int level,
                int strength,
                int dexterity,
                int intelligence,
                int charisma,
                int wisdom,
                int speed,
                int armorClass,
                List<List<String>> spellbook,
                int maxSpellUses,
                int currentSpellUses
        ) {
            this.name = name;
            this.characterClass = characterClass;
            this.level = level;
            this.strength = strength;
            this.dexterity = dexterity;
            this.intelligence = intelligence;
            this.charisma = charisma;
            this.wisdom = wisdom;
            this.speed = speed;
            this.armorClass = armorClass;
            this.spellbook = spellbook;
            this.maxSpellUses = maxSpellUses;
            this.currentSpellUses = Math.min(currentSpellUses, maxSpellUses);
        }

        JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {
                object.put("name", name);
                object.put("class", characterClass);
                object.put("level", level);
                object.put("strength", strength);
                object.put("dexterity", dexterity);
                object.put("intelligence", intelligence);
                object.put("charisma", charisma);
                object.put("wisdom", wisdom);
                object.put("speed", speed);
                object.put("armorClass", armorClass);

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
            return new DndCharacter(
                    object.optString("name", "Без имени"),
                    object.optString("class", "Без класса"),
                    object.optInt("level", 1),
                    object.optInt("strength", 10),
                    object.optInt("dexterity", 10),
                    object.optInt("intelligence", 10),
                    object.optInt("charisma", 10),
                    object.optInt("wisdom", 10),
                    object.optInt("speed", 30),
                    object.optInt("armorClass", 10),
                    readSpellbook(object.optJSONArray("spellbook")),
                    object.optInt("maxSpellUses", maxSpellUsesForLevel(object.optInt("level", 1))),
                    object.optInt("currentSpellUses", object.optInt("maxSpellUses", maxSpellUsesForLevel(object.optInt("level", 1))))
            );
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
