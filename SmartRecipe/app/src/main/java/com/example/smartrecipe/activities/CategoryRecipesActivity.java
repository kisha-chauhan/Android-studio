package com.example.smartrecipe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.adapters.RecipeAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.RecipeActionHelper;
import com.example.smartrecipe.utils.SessionManager;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRecipesActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<Recipe> recipes = new ArrayList<>();
    private RecipeAdapter adapter;
    private SessionManager session;
    private int categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_recipes);

        session = new SessionManager(this);
        categoryId = getIntent().getIntExtra(Constants.EXTRA_CATEGORY_ID, 0);
        String categoryName = getIntent().getStringExtra(Constants.EXTRA_CATEGORY_NAME);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(categoryName != null ? categoryName : "Recipes");
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rvRecipes = findViewById(R.id.rvRecipes);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, recipes, false, new RecipeAdapter.OnRecipeActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(CategoryRecipesActivity.this, RecipeDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteToggle(Recipe recipe, int position) {
                RecipeActionHelper.toggleFavorite(CategoryRecipesActivity.this, session.getUserId(), recipe,
                        isFav -> adapter.notifyItemChanged(position));
            }
        });
        rvRecipes.setAdapter(adapter);

        loadCategoryRecipes();
    }

    private void loadCategoryRecipes() {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getRecipesByCategory(categoryId, session.getUserId())
                .enqueue(new Callback<ApiResponse<List<Recipe>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Recipe>>> call, Response<ApiResponse<List<Recipe>>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            recipes.clear();
                            recipes.addAll(response.body().getData());
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(recipes.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Recipe>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CategoryRecipesActivity.this, "Failed to load recipes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
