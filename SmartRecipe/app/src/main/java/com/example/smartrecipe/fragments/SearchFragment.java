package com.example.smartrecipe.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.activities.RecipeDetailsActivity;
import com.example.smartrecipe.activities.WhatCanICookActivity;
import com.example.smartrecipe.adapters.RecipeAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Category;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.RecipeActionHelper;
import com.example.smartrecipe.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private TextInputEditText etSearchQuery;
    private Spinner spinnerCategory;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private final List<Category> categories = new ArrayList<>();
    private final List<Recipe> results = new ArrayList<>();
    private RecipeAdapter adapter;
    private SessionManager session;
    private Integer selectedCategoryId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        session = new SessionManager(requireContext());

        etSearchQuery = view.findViewById(R.id.etSearchQuery);
        spinnerCategory = view.findViewById(R.id.spinnerCategoryFilter);
        progressBar = view.findViewById(R.id.progressSearch);
        tvEmpty = view.findViewById(R.id.tvEmptySearch);

        RecyclerView rvResults = view.findViewById(R.id.rvSearchResults);
        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecipeAdapter(requireContext(), results, false, new RecipeAdapter.OnRecipeActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(requireContext(), RecipeDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteToggle(Recipe recipe, int position) {
                RecipeActionHelper.toggleFavorite(requireContext(), session.getUserId(), recipe,
                        isFav -> adapter.notifyItemChanged(position));
            }
        });
        rvResults.setAdapter(adapter);

        view.findViewById(R.id.btnSearch).setOnClickListener(v -> performSearch());
        view.findViewById(R.id.btnOpenWhatCanICook).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), WhatCanICookActivity.class)));

        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        loadCategories();
        performSearch();
        return view;
    }

    private void loadCategories() {
        RetrofitClient.getApiService(requireContext()).getCategories()
                .enqueue(new Callback<ApiResponse<List<Category>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                        if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                        categories.clear();
                        Category all = new Category(0, "All Categories", "", "");
                        categories.add(all);
                        if (response.body().getData() != null) {
                            categories.addAll(response.body().getData());
                        }
                        ArrayAdapter<Category> spinnerAdapter = new ArrayAdapter<>(
                                requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
                        spinnerCategory.setAdapter(spinnerAdapter);
                        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                Category chosen = categories.get(pos);
                                selectedCategoryId = chosen.getId() > 0 ? chosen.getId() : null;
                                performSearch();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {}
                });
    }

    private void performSearch() {
        if (!NetworkUtils.isConnected(requireContext())) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        String query = etSearchQuery.getText() != null ? etSearchQuery.getText().toString().trim() : "";
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        RetrofitClient.getApiService(requireContext())
                .searchRecipes(query.isEmpty() ? null : query, selectedCategoryId, null, session.getUserId())
                .enqueue(new Callback<ApiResponse<List<Recipe>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Recipe>>> call, Response<ApiResponse<List<Recipe>>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            results.clear();
                            results.addAll(response.body().getData());
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Toast.makeText(requireContext(), NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Recipe>>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
