package com.example.dna_project;

import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.dna_project.data.CharacterRepository;
import com.example.dna_project.data.DndProjectDatabaseHelper;
import com.example.dna_project.data.InventoryRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends CharacterStatsActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        characterRepository = new CharacterRepository(this);
        inventoryRepository = new InventoryRepository(this);
        projectDatabase = new DndProjectDatabaseHelper(getApplicationContext());
        projectDatabase.ensureReady();
        root = new FrameLayout(this);
        root.setBackgroundResource(R.drawable.dnd_screen_bg);
        setContentView(root);
        loadCharacters();
        showCharacterSelect();
    }

    // Character window: shared container with bottom navigation for inventory, stats, and spells.
    protected void showCharacterSheet() {
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

    // Character tabs: renders stats, inventory, or spellbook content.
    protected void renderTab(FrameLayout content) {
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

    public void onBackPressed() {
        if (selectedCharacter != null) {
            showCharacterSelect();
        } else {
            super.onBackPressed();
        }
    }
}
