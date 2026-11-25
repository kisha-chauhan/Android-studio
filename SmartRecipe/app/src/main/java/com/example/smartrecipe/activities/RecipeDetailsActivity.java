package com.example.smartrecipe.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.adapters.IngredientDetailAdapter;
import com.example.smartrecipe.adapters.ReviewAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Ingredient;
import com.example.smartrecipe.models.MealPlan;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.models.Review;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.ImageUtils;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.RecipeActionHelper;
import com.example.smartrecipe.utils.SessionManager;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailsActivity extends AppCompatActivity {

    private int recipeId;
    private Recipe currentRecipe;
    private SessionManager session;

    private ProgressBar progressBar;
    private ImageView ivImage;
    private ImageButton btnFavorite;
    private TextView tvCategory, tvName, tvRatingSummary, tvDescription;
    private TextView tvPrepTime, tvCookTime, tvTotalTime, tvServings, tvInstructions, tvEmptyReviews;

    private final List<Ingredient> ingredientList = new ArrayList<>();
    private final List<Review> reviewList = new ArrayList<>();
    private IngredientDetailAdapter ingredientAdapter;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        session = new SessionManager(this);
        recipeId = getIntent().getIntExtra(Constants.EXTRA_RECIPE_ID, -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);
        UiUtils.applyBottomBarInsets(findViewById(R.id.scrollContent));

        progressBar = findViewById(R.id.progressBar);
        ivImage = findViewById(R.id.ivRecipeImage);
        btnFavorite = findViewById(R.id.btnFavorite);
        tvCategory = findViewById(R.id.tvCategory);
        tvName = findViewById(R.id.tvRecipeName);
        tvRatingSummary = findViewById(R.id.tvRatingSummary);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrepTime = findViewById(R.id.tvPrepTime);
        tvCookTime = findViewById(R.id.tvCookTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvServings = findViewById(R.id.tvServings);
        tvInstructions = findViewById(R.id.tvInstructions);
        tvEmptyReviews = findViewById(R.id.tvEmptyReviews);

        RecyclerView rvIngredients = findViewById(R.id.rvIngredients);
        rvIngredients.setLayoutManager(UiUtils.nonScrollableVerticalLayoutManager(this));
        rvIngredients.setFocusable(false);
        ingredientAdapter = new IngredientDetailAdapter(this, ingredientList);
        rvIngredients.setAdapter(ingredientAdapter);

        RecyclerView rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(UiUtils.nonScrollableVerticalLayoutManager(this));
        rvReviews.setFocusable(false);
        reviewAdapter = new ReviewAdapter(this, reviewList, session.getUserId(), false,
                new ReviewAdapter.OnReviewActionListener() {
                    @Override
                    public void onEdit(Review review) {
                        showReviewDialog(review);
                    }

                    @Override
                    public void onDelete(Review review, int position) {
                        deleteReview(review);
                    }
                });
        rvReviews.setAdapter(reviewAdapter);

        btnFavorite.setOnClickListener(v -> {
            if (currentRecipe != null) {
                RecipeActionHelper.toggleFavorite(this, session.getUserId(), currentRecipe, isFav ->
                        btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border));
            }
        });

        findViewById(R.id.btnAddToMealPlan).setOnClickListener(v -> showMealPlanDialog());
        findViewById(R.id.btnAddToShoppingList).setOnClickListener(v -> showAddIngredientsToShoppingDialog());
        findViewById(R.id.btnWriteReview).setOnClickListener(v -> showReviewDialog(null));

        loadRecipeDetails();
        loadReviews();
    }

    private void loadRecipeDetails() {
        if (recipeId <= 0) {
            finish();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getRecipeDetails(recipeId, session.getUserId())
                .enqueue(new Callback<ApiResponse<Recipe>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Recipe>> call, Response<ApiResponse<Recipe>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            currentRecipe = response.body().getData();
                            bindRecipe(currentRecipe);
                        } else {
                            Toast.makeText(RecipeDetailsActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Recipe>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RecipeDetailsActivity.this, "Error loading recipe", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindRecipe(Recipe r) {
        ImageUtils.loadRecipeImage(this, r.getImage(), ivImage);
        tvCategory.setText(r.getCategoryName() != null ? r.getCategoryName() : "Recipe");
        tvName.setText(r.getName());
        tvRatingSummary.setText(String.format(Locale.getDefault(), "%.1f (%d reviews)",
                r.getAverageRating(), r.getReviewCount()));
        tvDescription.setText(r.getDescription());
        tvPrepTime.setText(r.getPreparationTime() + " min");
        tvCookTime.setText(r.getCookingTime() + " min");
        tvTotalTime.setText(r.getTotalTime() + " min");
        tvServings.setText(String.valueOf(r.getServings()));
        tvInstructions.setText(r.getInstructions());
        btnFavorite.setImageResource(r.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        ingredientList.clear();
        if (r.getIngredients() != null) {
            ingredientList.addAll(r.getIngredients());
        }
        ingredientAdapter.notifyDataSetChanged();
    }

    private void loadReviews() {
        RetrofitClient.getApiService(this).getRecipeReviews(recipeId)
                .enqueue(new Callback<ApiResponse<List<Review>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Review>>> call, Response<ApiResponse<List<Review>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            reviewList.clear();
                            reviewList.addAll(response.body().getData());
                            reviewAdapter.notifyDataSetChanged();
                            tvEmptyReviews.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Review>>> call, Throwable t) {}
                });
    }

    private void showMealPlanDialog() {
        if (currentRecipe == null) return;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        final Calendar cal = Calendar.getInstance();
        TextView tvDate = new TextView(this);
        String defaultDate = String.format(Locale.US, "%04d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        tvDate.setText("Date: " + defaultDate);
        tvDate.setTag(defaultDate);
        tvDate.setPadding(0, 12, 0, 16);
        tvDate.setTextSize(15f);
        tvDate.setOnClickListener(v -> new DatePickerDialog(this, (view, y, m, d) -> {
            String picked = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            tvDate.setText("Date: " + picked + " (tap to change)");
            tvDate.setTag(picked);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show());

        Spinner spinnerMealType = new Spinner(this);
        spinnerMealType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Constants.MEAL_TYPES));

        layout.addView(tvDate);
        layout.addView(spinnerMealType);

        new AlertDialog.Builder(this)
                .setTitle("Add to Meal Planner")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("user_id", session.getUserId());
                    body.put("recipe_id", currentRecipe.getId());
                    body.put("meal_date", tvDate.getTag().toString());
                    body.put("meal_type", spinnerMealType.getSelectedItem().toString());

                    RetrofitClient.getApiService(this).createMealPlan(body)
                            .enqueue(new Callback<ApiResponse<MealPlan>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<MealPlan>> call, Response<ApiResponse<MealPlan>> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(RecipeDetailsActivity.this, "Added to Meal Planner!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RecipeDetailsActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<MealPlan>> call, Throwable t) {
                                    Toast.makeText(RecipeDetailsActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddIngredientsToShoppingDialog() {
        if (ingredientList.isEmpty()) {
            Toast.makeText(this, "No ingredients listed for this recipe.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[ingredientList.size()];
        boolean[] checked = new boolean[ingredientList.size()];
        for (int i = 0; i < ingredientList.size(); i++) {
            Ingredient ing = ingredientList.get(i);
            labels[i] = ing.getIngredientName() + " (" + ing.getFormattedQuantity() + ")";
            checked[i] = true;
        }

        new AlertDialog.Builder(this)
                .setTitle("Add Missing Ingredients to Shopping List")
                .setMultiChoiceItems(labels, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Add Selected", (dialog, which) -> {
                    List<Map<String, String>> itemsPayload = new ArrayList<>();
                    for (int i = 0; i < ingredientList.size(); i++) {
                        if (checked[i]) {
                            Ingredient ing = ingredientList.get(i);
                            Map<String, String> item = new HashMap<>();
                            item.put("item_name", ing.getIngredientName());
                            item.put("quantity", ing.getQuantity() != null ? ing.getQuantity() : "1");
                            item.put("unit", ing.getUnit() != null ? ing.getUnit() : "pcs");
                            itemsPayload.add(item);
                        }
                    }
                    if (itemsPayload.isEmpty()) return;

                    Map<String, Object> body = new HashMap<>();
                    body.put("user_id", session.getUserId());
                    body.put("items", itemsPayload);

                    RetrofitClient.getApiService(this).addBulkShoppingItems(body)
                            .enqueue(new Callback<ApiResponse<Object>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(RecipeDetailsActivity.this, "Ingredients added to Shopping List!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RecipeDetailsActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                    Toast.makeText(RecipeDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReviewDialog(Review existing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyle);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(existing != null ? existing.getRating() : 5.0f);
        LinearLayout.LayoutParams rbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ratingBar.setLayoutParams(rbParams);

        EditText etComment = new EditText(this);
        etComment.setHint("Write your review...");
        if (existing != null) etComment.setText(existing.getReviewText());

        layout.addView(ratingBar);
        layout.addView(etComment);

        new AlertDialog.Builder(this)
                .setTitle(existing != null ? "Update Review" : "Rate & Review Recipe")
                .setView(layout)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    if (rating < 1) rating = 1;
                    String text = etComment.getText().toString().trim();
                    Map<String, Object> body = new HashMap<>();
                    body.put("user_id", session.getUserId());
                    body.put("recipe_id", recipeId);
                    body.put("rating", rating);
                    body.put("review_text", text);

                    Call<ApiResponse<Review>> call = existing != null
                            ? RetrofitClient.getApiService(this).updateReview(existing.getId(), body)
                            : RetrofitClient.getApiService(this).addReview(body);

                    call.enqueue(new Callback<ApiResponse<Review>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Review>> call, Response<ApiResponse<Review>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(RecipeDetailsActivity.this, "Review saved!", Toast.LENGTH_SHORT).show();
                                loadReviews();
                                loadRecipeDetails();
                            } else {
                                Toast.makeText(RecipeDetailsActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Review>> call, Throwable t) {
                            Toast.makeText(RecipeDetailsActivity.this, "Error saving review", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReview(Review review) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Review")
                .setMessage("Are you sure you want to delete your review?")
                .setPositiveButton("Delete", (d, w) ->
                        RetrofitClient.getApiService(this).deleteReview(review.getId())
                                .enqueue(new Callback<ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                        if (response.isSuccessful()) {
                                            loadReviews();
                                            loadRecipeDetails();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
