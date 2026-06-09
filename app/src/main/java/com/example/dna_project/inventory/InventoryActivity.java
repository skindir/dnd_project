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

abstract class InventoryActivity extends CharacterCreationActivity {

    // Inventory: loads backpack state, money, and equipped items for the selected character.
    protected void prepareInventoryTab() {
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

    // Inventory: backpack header with money controls and the add-item button.
    protected void addBackpackHeader(LinearLayout parent) {
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

    // Inventory: dialog for changing the character's coin amounts.
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

    // Inventory: category tabs, item grid, and bottom coin row.
    protected void addBackpackLayout(LinearLayout parent, FrameLayout content) {
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

    // Inventory: searches database items and adds selected items to the backpack.
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

    // Inventory: item details card with equip, unequip, and remove actions.
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

    // Inventory: visual equipment layout for the selected character.
    protected void addEquipmentLayout(LinearLayout parent) {
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

    private class InventorySlotAdapter extends RecyclerView.Adapter<InventorySlotAdapter.SlotViewHolder> {
        private final List<InventoryItem> slots;

        InventorySlotAdapter(List<InventoryItem> slots) {
            this.slots = slots;
        }

        @NonNull
        @Override
        public SlotViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            FrameLayout root = new FrameLayout(InventoryActivity.this);
            root.setBackgroundResource(R.drawable.dnd_empty_slot_bg);
            root.setPadding(dp(4), dp(4), dp(4), dp(4));

            ImageView icon = new ImageView(InventoryActivity.this);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            root.addView(icon, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
            ));

            TextView badge = new TextView(InventoryActivity.this);
            badge.setTextColor(0xFFFFFFFF);
            badge.setTextSize(11);
            badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            badge.setGravity(Gravity.CENTER);
            badge.setBackgroundColor(0xCC5F5043);
            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(dp(30), dp(20), Gravity.BOTTOM | Gravity.RIGHT);
            root.addView(badge, badgeParams);

            TextView equipped = new TextView(InventoryActivity.this);
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
}
