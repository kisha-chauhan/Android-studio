package com.example.smartrecipe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.adapters.MyRecipeAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManageRecipesActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<Recipe> recipes = new ArrayList<>();
    private MyRecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Manage Recipes");
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvAdminList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecipeAdapter(this, recipes, new MyRecipeAdapter.OnMyRecipeListener() {
            @Override
            public void onView(Recipe recipe) {
                Intent intent = new Intent(AdminManageRecipesActivity.this, RecipeDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }

            @Override
            public void onEdit(Recipe recipe) {
                Intent intent = new Intent(AdminManageRecipesActivity.this, AddEditRecipeActivity.class);
                intent.putExtra(Constants.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Recipe recipe, int position) {
                deleteRecipe(recipe, position);
            }
        });
        rv.setAdapter(adapter);

        findViewById(R.id.fabAdminAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditRecipeActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void loadRecipes() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getRecipes("latest", null, null)
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
                        Toast.makeText(AdminManageRecipesActivity.this, "Failed to load recipes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteRecipe(Recipe recipe, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Recipe")
                .setMessage("Delete recipe \"" + recipe.getName() + "\"?")
                .setPositiveButton("Delete", (d, w) ->
                        RetrofitClient.getApiService(this).deleteRecipe(recipe.getId())
                                .enqueue(new Callback<ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                        if (response.isSuccessful()) {
                                            recipes.remove(position);
                                            adapter.notifyItemRemoved(position);
                                        } else {
                                            Toast.makeText(AdminManageRecipesActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
