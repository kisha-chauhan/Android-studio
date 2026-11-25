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
import com.example.smartrecipe.utils.SessionManager;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyRecipesActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<Recipe> myRecipes = new ArrayList<>();
    private MyRecipeAdapter adapter;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        session = new SessionManager(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rvMyRecipes = findViewById(R.id.rvMyRecipes);
        rvMyRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecipeAdapter(this, myRecipes, new MyRecipeAdapter.OnMyRecipeListener() {
            @Override
            public void onView(Recipe recipe) {
                Intent intent = new Intent(MyRecipesActivity.this, RecipeDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }

            @Override
            public void onEdit(Recipe recipe) {
                Intent intent = new Intent(MyRecipesActivity.this, AddEditRecipeActivity.class);
                intent.putExtra(Constants.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Recipe recipe, int position) {
                confirmDelete(recipe, position);
            }
        });
        rvMyRecipes.setAdapter(adapter);

        findViewById(R.id.fabAddRecipe).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditRecipeActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyRecipes();
    }

    private void loadMyRecipes() {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getRecipes("latest", session.getUserId(), session.getUserId())
                .enqueue(new Callback<ApiResponse<List<Recipe>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Recipe>>> call, Response<ApiResponse<List<Recipe>>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            myRecipes.clear();
                            myRecipes.addAll(response.body().getData());
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(myRecipes.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Recipe>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyRecipesActivity.this, "Error loading your recipes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDelete(Recipe recipe, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete \"" + recipe.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) ->
                        RetrofitClient.getApiService(this).deleteRecipe(recipe.getId())
                                .enqueue(new Callback<ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                        if (response.isSuccessful()) {
                                            myRecipes.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            tvEmpty.setVisibility(myRecipes.isEmpty() ? View.VISIBLE : View.GONE);
                                            Toast.makeText(MyRecipesActivity.this, "Recipe deleted", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MyRecipesActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                        Toast.makeText(MyRecipesActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                    }
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
