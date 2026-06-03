package com.example.dna_project.ui.reflow;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dna_project.R;
import com.example.dna_project.data.Spell;
import com.example.dna_project.data.SpellRepository;
import com.example.dna_project.databinding.DialogSpellEditorBinding;
import com.example.dna_project.databinding.FragmentReflowBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReflowFragment extends Fragment {
    private static final int MIN_SPELL_LEVEL = 0;
    private static final int MAX_SPELL_LEVEL = 9;
    private static final float ADD_DIALOG_WIDTH_PERCENT = 0.96f;
    private static final float ADD_DIALOG_HEIGHT_PERCENT = 0.98f;

    private FragmentReflowBinding binding;
    private SpellRepository repository;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReflowBinding.inflate(inflater, container, false);
        repository = new SpellRepository(requireContext());

        binding.buttonAddSpell.setOnClickListener(view -> showAddSpellDialog(null));
        loadSpellLevels();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }

    private void loadSpellLevels() {
        databaseExecutor.execute(() -> {
            List<Spell> spells = repository.getAllSpells();
            mainHandler.post(() -> {
                if (binding != null) {
                    renderSpellLevels(spells);
                }
            });
        });
    }

    private void renderSpellLevels(List<Spell> spells) {
        binding.spellLevelsContainer.removeAllViews();

        for (int level = MIN_SPELL_LEVEL; level <= MAX_SPELL_LEVEL; level++) {
            binding.spellLevelsContainer.addView(createLevelCard(level, filterByLevel(spells, level)));
        }
    }

    private View createLevelCard(int level, List<Spell> spells) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.spell_card_gap));
        card.setLayoutParams(cardParams);
        card.setCardElevation(0f);
        card.setStrokeColor(Color.rgb(130, 78, 34));
        card.setStrokeWidth(1);
        card.setCardBackgroundColor(Color.rgb(247, 229, 199));
        card.setRadius(getResources().getDimension(R.dimen.spell_card_radius));

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.spell_card_padding);
        content.setPadding(padding, padding, padding, padding);

        LinearLayout header = new LinearLayout(requireContext());
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = new TextView(requireContext());
        title.setText(getString(R.string.spell_level_title_format, level));
        title.setTextColor(Color.rgb(36, 29, 24));
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(16f);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        MaterialButton addButton = new MaterialButton(
                requireContext(),
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
        );
        addButton.setText(R.string.add_spell_short_action);
        addButton.setMinWidth(48);
        addButton.setMinHeight(48);
        addButton.setInsetTop(0);
        addButton.setInsetBottom(0);
        addButton.setOnClickListener(view -> showAddSpellDialog(level));

        header.addView(title);
        header.addView(addButton);
        content.addView(header);

        if (spells.isEmpty()) {
            content.addView(createBodyText(getString(R.string.empty_spell_list)));
        } else {
            for (Spell spell : spells) {
                content.addView(createBodyText(getString(
                        R.string.spell_row_format,
                        spell.getName(),
                        spell.getClassName(),
                        spell.getDamage(),
                        spell.getDamageType()
                )));
            }
        }

        card.addView(content);
        return card;
    }

    private TextView createBodyText(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(Color.rgb(36, 29, 24));
        textView.setTextSize(14f);
        textView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spell_text_gap), 0, 0);
        return textView;
    }

    private static List<Spell> filterByLevel(List<Spell> spells, int level) {
        List<Spell> result = new ArrayList<>();
        for (Spell spell : spells) {
            if (spell.getLevel() == level) {
                result.add(spell);
            }
        }
        return result;
    }

    private void showAddSpellDialog(Integer fixedLevel) {
        DialogSpellEditorBinding dialogBinding = DialogSpellEditorBinding.inflate(getLayoutInflater());
        if (fixedLevel != null) {
            dialogBinding.editSpellLevel.setText(String.valueOf(fixedLevel));
            dialogBinding.editSpellLevel.setEnabled(false);
        }

        String title = fixedLevel == null
                ? getString(R.string.add_spell_title)
                : getString(R.string.add_spell_to_level_title, fixedLevel);
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBinding.textDialogTitle.setText(title);
        dialogBinding.buttonCancel.setOnClickListener(view -> dialog.dismiss());
        dialogBinding.buttonSave.setOnClickListener(view -> saveSpell(dialog, dialogBinding));
        resizeDialogContent(dialogBinding);
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();
        resizeAddSpellDialog(dialog);
    }

    private void resizeDialogContent(DialogSpellEditorBinding dialogBinding) {
        int width = (int) (getResources().getDisplayMetrics().widthPixels * ADD_DIALOG_WIDTH_PERCENT);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * ADD_DIALOG_HEIGHT_PERCENT);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        dialogBinding.getRoot().setLayoutParams(params);
        dialogBinding.getRoot().setMinimumHeight(height);
    }

    private void resizeAddSpellDialog(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        int width = (int) (getResources().getDisplayMetrics().widthPixels * ADD_DIALOG_WIDTH_PERCENT);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * ADD_DIALOG_HEIGHT_PERCENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        window.setLayout(width, height);

        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = width;
        attributes.height = height;
        window.setAttributes(attributes);
        window.getDecorView().setMinimumHeight(height);
    }

    private void saveSpell(Dialog dialog, DialogSpellEditorBinding dialogBinding) {
        String name = getText(dialogBinding.editSpellName);
        String levelText = getText(dialogBinding.editSpellLevel);
        String className = getText(dialogBinding.editSpellClass);
        String range = getText(dialogBinding.editSpellRange);
        String attackType = getText(dialogBinding.editSpellAttackType);
        String damageType = getText(dialogBinding.editSpellDamageType);
        String damage = getText(dialogBinding.editSpellDamage);

        if (name.isEmpty()) {
            dialogBinding.editSpellName.setError(getString(R.string.required_field_error));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(levelText);
        } catch (NumberFormatException exception) {
            dialogBinding.editSpellLevel.setError(getString(R.string.number_field_error));
            return;
        }

        if (level < MIN_SPELL_LEVEL || level > MAX_SPELL_LEVEL) {
            dialogBinding.editSpellLevel.setError(getString(R.string.spell_level_range_error));
            return;
        }

        databaseExecutor.execute(() -> {
            repository.addSpell(name, level, className, range, attackType, damageType, damage);
            List<Spell> spells = repository.getAllSpells();
            mainHandler.post(() -> {
                if (binding == null) {
                    return;
                }
                renderSpellLevels(spells);
                dialog.dismiss();
                Snackbar.make(binding.getRoot(), R.string.spell_added_message, Snackbar.LENGTH_SHORT).show();
            });
        });
    }

    private static String getText(EditText editText) {
        return editText.getText().toString().trim();
    }
}
