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
    private static final float EQUIPMENT_IMAGE_ASPECT_RATIO = 1754f / 1284f;
    private static final float EQUIPMENT_DESIGN_WIDTH = 1284f;
    private static final float EQUIPMENT_DESIGN_HEIGHT = 1754f;
    private static final int INVENTORY_COLUMNS = 7;
    private static final String[] INVENTORY_CATEGORIES = {
            "Оружие",
            "Броня",
            "Аксесуары",
            "Инструменты",
            "Материалы",
            "Прочее",
            "Квестовые предметы"
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
    private static final String[] LANGUAGE_OPTIONS = {
            "Абиссал",
            "Друидский",
            "Гигантский",
            "Инфернальный",
            "Язык воров (ксант)",
            "Небесный",
            "Подземный",
            "Первичный",
            "Общий",
            "Орочий",
            "Эльфийский",
            "Гномий",
            "Гоблинский",
            "Полуэльфийский",
            "Полуросликовый",
            "Сильванский",
            "Тифлингский",
            "Подводный"
    };
    private static final String[][] SAVING_THROW_GROUPS = {
            {"Сила", "Спасбросок (Сила)\nАтлетика"},
            {"Ловкость", "Спасбросок (Ловкость)\nАкробатика\nЛовкость рук\nСкрытность"},
            {"Телосложение", "Спасбросок (Телосложение)"},
            {"Интеллект", "Спасбросок (Интеллект)\nМагия\nИстория\nРасследование\nПрирода\nРелигия"},
            {"Мудрость", "Спасбросок (Мудрость)\nДрессировка Животных\nПроницательность\nМедицина\nВнимательность\nВыживание"},
            {"Харизма", "Спасбросок (Харизма)\nОбман\nЗапугивание\nВыступление\nУбеждение"}
    };
    private static final String[] SAVING_THROW_OPTIONS = {
            "Спасбросок (Сила)",
            "Атлетика",
            "Спасбросок (Ловкость)",
            "Акробатика",
            "Ловкость рук",
            "Скрытность",
            "Спасбросок (Телосложение)",
            "Спасбросок (Интеллект)",
            "Магия",
            "История",
            "Расследование",
            "Природа",
            "Религия",
            "Спасбросок (Мудрость)",
            "Дрессировка Животных",
            "Проницательность",
            "Медицина",
            "Внимательность",
            "Выживание",
            "Спасбросок (Харизма)",
            "Обман",
            "Запугивание",
            "Выступление",
            "Убеждение"
    };

    private final List<DndCharacter> characters = new ArrayList<>();
    private SharedPreferences preferences;
    private FrameLayout root;
    private DndCharacter selectedCharacter;
    private int selectedTab = TAB_STATS;
    private int selectedInventoryCategory = 0;

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
        TextInputEditText race = textInput(screen, "Раса", "Человек");
        TextInputEditText background = textInput(screen, "Предыстория", "Солдат");
        TextInputEditText alignment = textInput(screen, "Мировоззрение", "Нейтральное");
        TextView selectedLanguagesLabel = bodyText("Языки не выбраны");
        selectedLanguagesLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedLanguagesLabel.setPadding(0, dp(4), 0, dp(4));
        Button languagesButton = secondaryButton("Выбрать языки");
        final boolean[] languageSelections = new boolean[LANGUAGE_OPTIONS.length];
        languagesButton.setOnClickListener(view -> {
            boolean[] dialogSelections = languageSelections.clone();
            new AlertDialog.Builder(this)
                    .setTitle("Выберите языки")
                    .setMultiChoiceItems(LANGUAGE_OPTIONS, dialogSelections, (dialog, which, isChecked) ->
                            dialogSelections[which] = isChecked)
                    .setPositiveButton("Готово", (dialog, which) -> {
                        System.arraycopy(dialogSelections, 0, languageSelections, 0, languageSelections.length);
                        selectedLanguagesLabel.setText("Языки: " + languagesSummary(languageSelections));
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
        screen.addView(selectedLanguagesLabel);
        screen.addView(languagesButton);
        TextView selectedSavingThrowsLabel = bodyText("Спасброски не выбраны");
        selectedSavingThrowsLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selectedSavingThrowsLabel.setPadding(0, dp(4), 0, dp(4));
        Button savingThrowsButton = secondaryButton("Выбрать спасброски");
        final boolean[] savingThrowSelections = new boolean[SAVING_THROW_OPTIONS.length];
        savingThrowsButton.setOnClickListener(view -> {
            boolean[] dialogSelections = savingThrowSelections.clone();
            new AlertDialog.Builder(this)
                    .setTitle("Выберите спасброски")
                    .setMultiChoiceItems(SAVING_THROW_OPTIONS, dialogSelections, (dialog, which, isChecked) ->
                            dialogSelections[which] = isChecked)
                    .setPositiveButton("Готово", (dialog, which) -> {
                        System.arraycopy(dialogSelections, 0, savingThrowSelections, 0, savingThrowSelections.length);
                        selectedSavingThrowsLabel.setText("Спасброски: " + savingThrowsSummary(savingThrowSelections));
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
        screen.addView(selectedSavingThrowsLabel);
        screen.addView(savingThrowsButton);

        GridLayout statGrid = new GridLayout(this);
        statGrid.setColumnCount(2);
        statGrid.setUseDefaultMargins(true);
        screen.addView(statGrid);

        TextInputEditText strength = numberInput(statGrid, "Сила", 10);
        TextInputEditText dexterity = numberInput(statGrid, "Ловкость", 10);
        TextInputEditText constitution = numberInput(statGrid, "Телосложение", 10);
        TextInputEditText intelligence = numberInput(statGrid, "Интеллект", 10);
        TextInputEditText charisma = numberInput(statGrid, "Харизма", 10);
        TextInputEditText wisdom = numberInput(statGrid, "Мудрость", 10);
        TextInputEditText speed = numberInput(statGrid, "Скорость", 30);
        TextInputEditText armorClass = numberInput(statGrid, "Класс брони", 10);
        TextInputEditText currentHp = numberInput(statGrid, "Текущие ХП", 10);
        TextInputEditText maxHp = numberInput(statGrid, "Максимум ХП", 10);
        TextInputEditText temporaryHp = numberInput(statGrid, "Временные ХП", 0);
        TextInputEditText proficiencyBonus = numberInput(statGrid, "Бонус мастерства", 2);
        TextInputEditText perception = numberInput(statGrid, "Восприятие", 10);
        TextInputEditText initiative = numberInput(statGrid, "Инициатива", 0);

        TextInputEditText hitDice = textInput(screen, "Кубик хитов", "d8");
        TextInputEditText featuresAndTraits = multilineTextInput(screen, "Особенности и черты", "");
        TextInputEditText personalityTraits = multilineTextInput(screen, "Черты характера", "");
        TextInputEditText ideals = multilineTextInput(screen, "Идеалы", "");
        TextInputEditText bonds = multilineTextInput(screen, "Узы", "");
        TextInputEditText flaws = multilineTextInput(screen, "Недостатки", "");

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
                    intValue(constitution, 10),
                    intValue(intelligence, 10),
                    intValue(charisma, 10),
                    intValue(wisdom, 10),
                    intValue(speed, 30),
                    intValue(armorClass, 10),
                    valueOrDefault(race, "Не указана"),
                    valueOrDefault(background, "Не указана"),
                    valueOrDefault(alignment, "Не указано"),
                    intValue(currentHp, 10),
                    intValue(maxHp, 10),
                    intValue(temporaryHp, 0),
                    valueOrDefault(hitDice, "d8"),
                    intValue(proficiencyBonus, 2),
                    intValue(perception, 10),
                    valueOrDefault(featuresAndTraits, "Нет"),
                    valueOrDefault(personalityTraits, "Нет"),
                    valueOrDefault(ideals, "Нет"),
                    valueOrDefault(bonds, "Нет"),
                    valueOrDefault(flaws, "Нет"),
                    intValue(initiative, 0),
                    collectSelectedLanguages(languageSelections),
                    collectSelectedSavingThrows(savingThrowSelections)
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
        selectedInventoryCategory = 0;
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
            body.setBackgroundResource(R.drawable.dnd_character_sheet_bg);
            addSheetHeader(body);
            addIdentitySection(body);
            addCombatSection(body);
            addAbilitySection(body, selectedCharacter.savingThrows);
            addCharacterNotesSection(body);
            addLanguagesTable(body, selectedCharacter.languages);
        } else if (selectedTab == TAB_INVENTORY) {
            addEquipmentLayout(body);
            addBackpackHeader(body);
            addBackpackLayout(body, content);
        } else {
            body.addView(sectionTitle("Книга заклинаний"));
            Button addSpell = primaryButton("Добавить заклинание");
            addSpell.setOnClickListener(view -> showAddSpellDialog(-1));
            body.addView(addSpell);
            addSpellLevels(body);
        }

        content.addView(scrollView);
    }

    private void addBackpackHeader(LinearLayout parent) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = sectionTitle("В рюкзаке");
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
        addItemButton.setOnClickListener(view ->
                Toast.makeText(this, "Add Item", Toast.LENGTH_SHORT).show());
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

        View subtract = moneyActionButton(R.drawable.coin_action_subtract, "Отнять");
        View add = moneyActionButton(R.drawable.coin_action_add, "Добавить");
        LinearLayout.LayoutParams subtractParams = new LinearLayout.LayoutParams(dp(66), dp(44));
        subtractParams.setMargins(0, 0, dp(8), 0);
        actions.addView(subtract, subtractParams);
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(dp(66), dp(44));
        addParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(add, addParams);
        dialogBody.addView(actions);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Деньги")
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
            Toast toast = Toast.makeText(this, "Не хватает денег", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }
        selectedCharacter.setMoneyFromCopper(updatedTotal);
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
        LinearLayout grid = verticalLayout(6);
        grid.setPadding(0, dp(4), 0, dp(4));

        int itemCount = 0;
        int rows = Math.max(1, (itemCount / INVENTORY_COLUMNS) + 1);
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int columnIndex = 0; columnIndex < INVENTORY_COLUMNS; columnIndex++) {
                int slotIndex = rowIndex * INVENTORY_COLUMNS + columnIndex;
                TextView slot = new TextView(this);
                slot.setBackgroundResource(R.drawable.dnd_empty_slot_bg);
                slot.setTextColor(0xFF5F5043);
                slot.setGravity(Gravity.CENTER);
                slot.setContentDescription(INVENTORY_CATEGORIES[selectedInventoryCategory] + " " + (slotIndex + 1));
                slot.setOnClickListener(view -> Toast.makeText(
                        this,
                        INVENTORY_CATEGORIES[selectedInventoryCategory] + ": пустая ячейка",
                        Toast.LENGTH_SHORT
                ).show());

                LinearLayout.LayoutParams slotParams = new LinearLayout.LayoutParams(
                        0,
                        dp(76),
                        1f
                );
                slotParams.setMargins(dp(3), dp(3), dp(3), dp(3));
                row.addView(slot, slotParams);
            }

            grid.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        }

        parent.addView(grid, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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

        ImageView frame = new ImageView(this);
        frame.setImageResource(R.drawable.equipment_frame);
        frame.setScaleType(ImageView.ScaleType.FIT_XY);
        equipment.addView(frame, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        addEquipmentSlot(equipment, "Шлем", 560, 94, 168, 162);
        addEquipmentSlot(equipment, "Наплечник левый", 210, 406, 184, 168);
        addEquipmentSlot(equipment, "Наплечник правый", 900, 401, 182, 167);
        addEquipmentSlot(equipment, "Серьга 1", 384, 167, 126, 130);
        addEquipmentSlot(equipment, "Серьга 2", 772, 160, 128, 130);
        addEquipmentSlot(equipment, "Кулон", 577, 316, 130, 116);
        addEquipmentSlot(equipment, "Перчатка левая", 214, 704, 185, 175);
        addEquipmentSlot(equipment, "Перчатка правая", 897, 694, 183, 174);
        addEquipmentSlot(equipment, "Нагрудник", 540, 482, 195, 212);
        addEquipmentSlot(equipment, "Пояс", 555, 892, 155, 116);
        addEquipmentSlot(equipment, "Первичное оружие", 121, 1099, 161, 470);
        addEquipmentSlot(equipment, "Вторичное оружие", 1000, 1102, 161, 477);
        addEquipmentSlot(equipment, "Кольцо 1", 309, 973, 93, 100);
        addEquipmentSlot(equipment, "Кольцо 2", 867, 962, 96, 100);
        addEquipmentSlot(equipment, "Штаны", 544, 1137, 195, 228);
        addEquipmentSlot(equipment, "Ботинки", 543, 1467, 201, 188);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(12));
        parent.addView(equipment, params);
    }

    private void addEquipmentSlot(FrameLayout equipment, String slot, int left, int top, int width, int height) {
        View slotView = new View(this);
        slotView.setBackgroundColor(0x66D0D0D0);
        slotView.setContentDescription(slot);
        slotView.setOnClickListener(view -> Toast.makeText(this, slot + ": пусто", Toast.LENGTH_SHORT).show());

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
        LinearLayout section = sheetSection("Анкета персонажа");
        section.setPadding(dp(12), dp(9), dp(12), dp(10));
        String[][] fields = {
                {"Имя персонажа", selectedCharacter.name},
                {"Класс", selectedCharacter.characterClass},
                {"Уровень", String.valueOf(selectedCharacter.level)},
                {"Раса", selectedCharacter.race},
                {"Предыстория", selectedCharacter.background},
                {"Мировоззрение", selectedCharacter.alignment}
        };
        addCompactIdentityGrid(section, fields, wideLayout() ? 3 : 2);
        parent.addView(section);
    }

    private void addCombatSection(LinearLayout parent) {
        LinearLayout section = sheetSection("Боевые показатели");
        GridLayout combatGrid = new GridLayout(this);
        combatGrid.setColumnCount(wideLayout() ? 5 : 2);
        combatGrid.setUseDefaultMargins(false);
        addCombatCell(combatGrid, "Класс брони", String.valueOf(selectedCharacter.armorClass), true);
        addCombatCell(combatGrid, "Инициатива", String.valueOf(selectedCharacter.initiative), false);
        addCombatCell(combatGrid, "Скорость", String.valueOf(selectedCharacter.speed), false);
        addCombatCell(combatGrid, "Бонус мастерства", String.valueOf(selectedCharacter.proficiencyBonus), false);
        addCombatCell(combatGrid, "Пассивная мудрость", String.valueOf(selectedCharacter.perception), false);
        section.addView(combatGrid);

        LinearLayout hp = new LinearLayout(this);
        hp.setOrientation(wideLayout() ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        hp.setPadding(0, dp(8), 0, 0);
        addHpPanel(hp, "Текущие хиты", String.valueOf(selectedCharacter.currentHp), "Максимум: " + selectedCharacter.maxHp);
        addHpPanel(hp, "Временные хиты", String.valueOf(selectedCharacter.temporaryHp), "Защита сверх максимума");
        addHpPanel(hp, "Кубики хитов", selectedCharacter.hitDice, "Всего: " + selectedCharacter.level);
        section.addView(hp);
        parent.addView(section);
    }

    private void addCharacterNotesSection(LinearLayout parent) {
        LinearLayout section = sheetSection("Описание");
        String[][] notes = {
                {"Особенности и черты", selectedCharacter.featuresAndTraits},
                {"Черты характера", selectedCharacter.personalityTraits},
                {"Идеалы", selectedCharacter.ideals},
                {"Узы", selectedCharacter.bonds},
                {"Недостатки", selectedCharacter.flaws}
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
        LinearLayout section = sheetSection("Характеристики");

        addAbilityRow(section, "Сила", "STR", selectedCharacter.strength,
                new String[]{"Спасбросок (Сила)"}, new String[]{"Атлетика"}, selectedSavingThrows);
        addAbilityRow(section, "Ловкость", "DEX", selectedCharacter.dexterity,
                new String[]{"Спасбросок (Ловкость)"}, new String[]{"Акробатика", "Ловкость рук", "Скрытность"}, selectedSavingThrows);
        addAbilityRow(section, "Телосложение", "CON", selectedCharacter.constitution,
                new String[]{"Спасбросок (Телосложение)"}, new String[]{}, selectedSavingThrows);
        addAbilityRow(section, "Интеллект", "INT", selectedCharacter.intelligence,
                new String[]{"Спасбросок (Интеллект)"}, new String[]{"Магия", "История", "Расследование", "Природа", "Религия"}, selectedSavingThrows);
        addAbilityRow(section, "Мудрость", "WIS", selectedCharacter.wisdom,
                new String[]{"Спасбросок (Мудрость)"}, new String[]{"Дрессировка Животных", "Проницательность", "Медицина", "Внимательность", "Выживание"}, selectedSavingThrows);
        addAbilityRow(section, "Харизма", "CHA", selectedCharacter.charisma,
                new String[]{"Спасбросок (Харизма)"}, new String[]{"Обман", "Запугивание", "Выступление", "Убеждение"}, selectedSavingThrows);

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
            savingBox.addView(proficiencyLine("Спасбросок", selectedSavingThrows.contains(savingThrow)));
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

        table.addView(sectionTitle("Языки"));
        if (languages.isEmpty()) {
            TextView empty = bodyText("Не выбраны");
            empty.setPadding(0, dp(8), 0, 0);
            table.addView(empty);
        } else {
            GridLayout languageGrid = new GridLayout(this);
            languageGrid.setColumnCount(2);
            languageGrid.setUseDefaultMargins(true);
            languageGrid.setPadding(0, dp(8), 0, 0);

            addLanguageCell(languageGrid, "#", true, 0.25f);
            addLanguageCell(languageGrid, "Язык", true, 1f);
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

        table.addView(sectionTitle("Спасброски к характеристикам"));

        GridLayout savingThrowGrid = new GridLayout(this);
        savingThrowGrid.setColumnCount(2);
        savingThrowGrid.setUseDefaultMargins(true);
        savingThrowGrid.setPadding(0, dp(8), 0, 0);

        addLanguageCell(savingThrowGrid, "Характеристика", true, 0.75f);
        addLanguageCell(savingThrowGrid, "Список", true, 1.25f);
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
            return "не выбраны";
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
            return "не выбраны";
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
        int platinumCoins;
        int goldCoins;
        int silverCoins;
        int copperCoins;

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
                    "Не указана",
                    "Не указана",
                    "Не указано",
                    10,
                    10,
                    0,
                    "d8",
                    2,
                    10,
                    "Нет",
                    "Нет",
                    "Нет",
                    "Нет",
                    "Нет",
                    0,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    createEmptySpellbook(),
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
                    object.optInt("constitution", 10),
                    object.optInt("intelligence", 10),
                    object.optInt("charisma", 10),
                    object.optInt("wisdom", 10),
                    object.optInt("speed", 30),
                    object.optInt("armorClass", 10),
                    object.optString("race", "Не указана"),
                    object.optString("background", "Не указана"),
                    object.optString("alignment", "Не указано"),
                    object.optInt("currentHp", 10),
                    object.optInt("maxHp", 10),
                    object.optInt("temporaryHp", 0),
                    object.optString("hitDice", "d8"),
                    object.optInt("proficiencyBonus", 2),
                    object.optInt("perception", 10),
                    object.optString("featuresAndTraits", "Нет"),
                    object.optString("personalityTraits", "Нет"),
                    object.optString("ideals", "Нет"),
                    object.optString("bonds", "Нет"),
                    object.optString("flaws", "Нет"),
                    object.optInt("initiative", 0),
                    readLanguages(object.optJSONArray("languages")),
                    readSavingThrows(object.optJSONArray("savingThrows")),
                    readSpellbook(object.optJSONArray("spellbook")),
                    object.optInt("platinumCoins", 0),
                    object.optInt("goldCoins", 0),
                    object.optInt("silverCoins", 0),
                    object.optInt("copperCoins", 0)
            );
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
                    if ("Телосложение".equals(savingThrow)) {
                        savingThrow = "Спасбросок (Телосложение)";
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
