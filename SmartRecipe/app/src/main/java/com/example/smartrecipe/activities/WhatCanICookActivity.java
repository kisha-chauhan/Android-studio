package com.example.smartrecipe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.adapters.WhatCanICookAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.SessionManager;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhatCanICookActivity extends AppCompatActivity {

    private TextInputEditText etIngredientInput;
    private ChipGroup chipGroup;
    private TextView tvSelected, tvEmpty;
    private ProgressBar progressBar;

    private final Set<String> selectedIngredients = new LinkedHashSet<>();
    private final List<Recipe> matchedRecipes = new ArrayList<>();
    private WhatCanICookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_can_i_cook);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        etIngredientInput = findViewById(R.id.etIngredientInput);
        chipGroup = findViewById(R.id.chipGroupQuickIngredients);
        tvSelected = findViewById(R.id.tvSelectedIngredients);
        tvEmpty = findViewById(R.id.tvEmptyResults);
        progressBar = findViewById(R.id.progressBar);

        RecyclerView rvMatched = findViewById(R.id.rvMatchedRecipes);
        rvMatched.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WhatCanICookAdapter(this, matchedRecipes, recipe -> {
            Intent intent = new Intent(this, RecipeDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        rvMatched.setAdapter(adapter);

        setupQuickChips();

        findViewById(R.id.btnAddIngredientChip).setOnClickListener(v -> {
            String raw = etIngredientInput.getText() != null ? etIngredientInput.getText().toString().trim() : "";
            if (raw.isEmpty()) return;
            for (String part : raw.split(",")) {
                String clean = part.trim();
                if (!clean.isEmpty()) {
                    addOrSelectIngredient(clean);
                }
            }
            etIngredientInput.setText("");
        });

        findViewById(R.id.btnFindRecipes).setOnClickListener(v -> searchByIngredients());
    }

    private void setupQuickChips() {
        for (String ing : Constants.QUICK_INGREDIENTS) {
            Chip chip = new Chip(this);
            chip.setText(ing);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedIngredients.add(ing);
                } else {
                    selectedIngredients.remove(ing);
                }
                updateSelectedLabel();
            });
            chipGroup.addView(chip);
        }
    }

    private void addOrSelectIngredient(String name) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip c = (Chip) chipGroup.getChildAt(i);
            if (c.getText().toString().equalsIgnoreCase(name)) {
                c.setChecked(true);
                return;
            }
        }
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setCheckable(true);
        chip.setChecked(true);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedIngredients.add(name);
            else selectedIngredients.remove(name);
            updateSelectedLabel();
        });
        chipGroup.addView(chip, 0);
        selectedIngredients.add(name);
        updateSelectedLabel();
    }

    private void updateSelectedLabel() {
        if (selectedIngredients.isEmpty()) {
            tvSelected.setText("Selected: None");
        } else {
            tvSelected.setText("Selected: " + TextUtils.join(", ", selectedIngredients));
        }
    }

    private void searchByIngredients() {
        if (selectedIngredients.isEmpty()) {
            Toast.makeText(this, "Please select or add at least one ingredient", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        Map<String, Object> body = new HashMap<>();
        body.put("ingredients", new ArrayList<>(selectedIngredients));
        body.put("user_id", new SessionManager(this).getUserId());

        RetrofitClient.getApiService(this).getRecipesByIngredients(body)
                .enqueue(new Callback<ApiResponse<List<Recipe>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Recipe>>> call,
                                           Response<ApiResponse<List<Recipe>>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            matchedRecipes.clear();
                            matchedRecipes.addAll(response.body().getData());
                            adapter.notifyDataSetChanged();
                            if (matchedRecipes.isEmpty()) {
                                tvEmpty.setText("No matching recipes found for these ingredients.");
                                tvEmpty.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(WhatCanICookActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Recipe>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(WhatCanICookActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
