package com.example.smartrecipe.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smartrecipe.R;
import com.example.smartrecipe.activities.CategoryRecipesActivity;
import com.example.smartrecipe.activities.MainActivity;
import com.example.smartrecipe.activities.RecipeDetailsActivity;
import com.example.smartrecipe.activities.ShoppingListActivity;
import com.example.smartrecipe.activities.WhatCanICookActivity;
import com.example.smartrecipe.adapters.CategoryAdapter;
import com.example.smartrecipe.adapters.RecipeAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Category;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.RecipeActionHelper;
import com.example.smartrecipe.utils.SessionManager;
import com.example.smartrecipe.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private SessionManager session;

    private final List<Category> categoryList = new ArrayList<>();
    private final List<Recipe> popularList = new ArrayList<>();
    private final List<Recipe> latestList = new ArrayList<>();

    private CategoryAdapter categoryAdapter;
    private RecipeAdapter popularAdapter;
    private RecipeAdapter latestAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        session = new SessionManager(requireContext());

        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        tvGreeting.setText("Hello, " + session.getName() + "!");

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressHome);

        setupRecyclerViews(view);
        setupShortcuts(view);

        swipeRefresh.setOnRefreshListener(this::loadHomeData);
        loadHomeData();
        return view;
    }

    private void setupRecyclerViews(View view) {
        RecyclerView rvCategories = view.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(requireContext(), categoryList, category -> {
            Intent intent = new Intent(requireContext(), CategoryRecipesActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
            intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
            startActivity(intent);
        });
        rvCategories.setAdapter(categoryAdapter);

        RecyclerView rvPopular = view.findViewById(R.id.rvPopularRecipes);
        rvPopular.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        popularAdapter = createRecipeAdapter(popularList, true);
        rvPopular.setAdapter(popularAdapter);

        RecyclerView rvLatest = view.findViewById(R.id.rvLatestRecipes);
        rvLatest.setLayoutManager(UiUtils.nonScrollableVerticalLayoutManager(requireContext()));
        rvLatest.setFocusable(false);
        latestAdapter = createRecipeAdapter(latestList, false);
        rvLatest.setAdapter(latestAdapter);
    }

    private RecipeAdapter createRecipeAdapter(List<Recipe> list, boolean horizontal) {
        return new RecipeAdapter(requireContext(), list, horizontal, new RecipeAdapter.OnRecipeActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(requireContext(), RecipeDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteToggle(Recipe recipe, int position) {
                RecipeActionHelper.toggleFavorite(requireContext(), session.getUserId(), recipe, isFav -> {
                    popularAdapter.notifyDataSetChanged();
                    latestAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void setupShortcuts(View view) {
        view.findViewById(R.id.cardSearchBar).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_search);
            }
        });
        view.findViewById(R.id.cardWhatCanICook).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), WhatCanICookActivity.class)));
        view.findViewById(R.id.btnShortcutFavorites).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_favorites);
            }
        });
        view.findViewById(R.id.btnShortcutPlanner).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_meal_planner);
            }
        });
        view.findViewById(R.id.btnShortcutShopping).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ShoppingListActivity.class)));
    }

    private void loadHomeData() {
        if (!NetworkUtils.isConnected(requireContext())) {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        int userId = session.getUserId();

        RetrofitClient.getApiService(requireContext()).getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body().getData());
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {}
        });

        RetrofitClient.getApiService(requireContext()).getRecipes("popular", userId, null)
                .enqueue(new Callback<ApiResponse<List<Recipe>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Recipe>>> call, Response<ApiResponse<List<Recipe>>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            popularList.clear();
                            popularList.addAll(response.body().getData());
                            popularAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Recipe>>> call, Throwable t) {}
                });

        RetrofitClient.getApiService(requireContext()).getRecipes("latest", userId, null)
                .enqueue(new Callback<ApiResponse<List<Recipe>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Recipe>>> call, Response<ApiResponse<List<Recipe>>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            latestList.clear();
                            latestList.addAll(response.body().getData());
                            latestAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Recipe>>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Unable to load recipes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
