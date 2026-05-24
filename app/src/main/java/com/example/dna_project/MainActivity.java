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
        navigation.getMenu().add(0, TAB_INVENTORY, 0, "Инвентарь").setIcon(android.R.drawable.ic_menu_agenda);
        navigation.getMenu().add(0, TAB_STATS, 1, "Характеристики").setIcon(android.R.drawable.ic_menu_info_details);
        navigation.getMenu().add(0, TAB_SPELLBOOK, 2, "Книга заклинаний").setIcon(android.R.drawable.ic_menu_upload);
        navigation.setSelectedItemId(selectedTab);
        navigation.setOnItemSelectedListener(item -> {
            selectedTab = item.getItemId();
            renderTab(content);
            return true;
        });
        screen.addView(navigation, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64)
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
        FrameLayout equipment = new FrameLayout(this);
        equipment.setBackgroundResource(R.drawable.dnd_slot_bg);

        ImageView background = new ImageView(this);
        background.setImageResource(classImageResource(selectedCharacter.characterClass));
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        background.setAlpha(0.92f);
        equipment.addView(background, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        addEquipmentSlot(equipment, "Шлем", 144, 16, 64, 52);
        addEquipmentSlot(equipment, "Наплечники", 104, 80, 144, 44);
        addEquipmentSlot(equipment, "Нагрудник", 128, 132, 96, 82);
        addEquipmentSlot(equipment, "Пояс", 128, 224, 96, 38);
        addEquipmentSlot(equipment, "Штаны", 136, 270, 80, 62);
        addEquipmentSlot(equipment, "Ботинки", 112, 340, 128, 48);
        addEquipmentSlot(equipment, "Перчатки", 52, 174, 56, 62);
        addEquipmentSlot(equipment, "Кольцо 1", 22, 48, 42, 42);
        addEquipmentSlot(equipment, "Кольцо 2", 288, 48, 42, 42);
        addEquipmentSlot(equipment, "Серьга 1", 72, 22, 36, 36);
        addEquipmentSlot(equipment, "Серьга 2", 244, 22, 36, 36);
        addEquipmentSlot(equipment, "Кулон", 154, 76, 44, 44);
        addEquipmentSlot(equipment, "Первичное оружие", 18, 250, 74, 120);
        addEquipmentSlot(equipment, "Вторичное оружие", 260, 250, 74, 120);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(420)
        );
        params.setMargins(0, dp(8), 0, dp(12));
        parent.addView(equipment, params);
    }

    private void addEquipmentSlot(FrameLayout equipment, String slot, int left, int top, int width, int height) {
        View slotView = new View(this);
        slotView.setBackgroundResource(R.drawable.dnd_empty_slot_bg);
        slotView.setAlpha(0.48f);
        slotView.setContentDescription(slot);
        slotView.setOnClickListener(view -> Toast.makeText(this, slot + ": пусто", Toast.LENGTH_SHORT).show());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(width), dp(height));
        params.leftMargin = dp(left);
        params.topMargin = dp(top);
        equipment.addView(slotView, params);
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
                frame.addView(bodyText("- " + spell));
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
            this(name, characterClass, level, strength, dexterity, intelligence, charisma, wisdom, speed, armorClass, createEmptySpellbook());
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
                    readSpellbook(object.optJSONArray("spellbook"))
            );
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
}
