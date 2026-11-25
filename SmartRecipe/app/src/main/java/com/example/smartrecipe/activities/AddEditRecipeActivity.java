package com.example.smartrecipe.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Category;
import com.example.smartrecipe.models.Ingredient;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.ImageUtils;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.SessionManager;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditRecipeActivity extends AppCompatActivity {

    private int editingRecipeId = -1;
    private SessionManager session;

    private ImageView ivPreview;
    private EditText etImageUrl;
    private TextInputEditText etName, etDescription, etPrepTime, etCookTime, etServings, etInstructions;
    private Spinner spinnerCategory;
    private LinearLayout llIngredientsContainer;
    private ProgressBar progressBar;

    private final List<Category> categories = new ArrayList<>();
    private String uploadedImagePath = "";

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    ivPreview.setImageURI(uri);
                    uploadImageToServer(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_recipe);

        session = new SessionManager(this);
        editingRecipeId = getIntent().getIntExtra(Constants.EXTRA_RECIPE_ID, -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(editingRecipeId > 0 ? "Edit Recipe" : "Add Recipe");
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        ivPreview = findViewById(R.id.ivRecipePreview);
        etImageUrl = findViewById(R.id.etImageUrl);
        etName = findViewById(R.id.etRecipeName);
        etDescription = findViewById(R.id.etDescription);
        etPrepTime = findViewById(R.id.etPrepTime);
        etCookTime = findViewById(R.id.etCookTime);
        etServings = findViewById(R.id.etServings);
        etInstructions = findViewById(R.id.etInstructions);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        llIngredientsContainer = findViewById(R.id.llIngredientsContainer);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnPickImage).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        findViewById(R.id.btnAddIngredientRow).setOnClickListener(v -> addIngredientRow(null));
        findViewById(R.id.btnSaveRecipe).setOnClickListener(v -> saveRecipe());

        loadCategories();
    }

    private void loadCategories() {
        RetrofitClient.getApiService(this).getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categories.clear();
                    categories.addAll(response.body().getData());
                    ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                            AddEditRecipeActivity.this, android.R.layout.simple_spinner_dropdown_item, categories);
                    spinnerCategory.setAdapter(adapter);

                    if (editingRecipeId > 0) {
                        loadExistingRecipe();
                    } else {
                        addIngredientRow(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                Toast.makeText(AddEditRecipeActivity.this, "Could not load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExistingRecipe() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getRecipeDetails(editingRecipeId, session.getUserId())
                .enqueue(new Callback<ApiResponse<Recipe>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Recipe>> call, Response<ApiResponse<Recipe>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            Recipe r = response.body().getData();
                            etName.setText(r.getName());
                            etDescription.setText(r.getDescription());
                            etPrepTime.setText(String.valueOf(r.getPreparationTime()));
                            etCookTime.setText(String.valueOf(r.getCookingTime()));
                            etServings.setText(String.valueOf(r.getServings()));
                            etInstructions.setText(r.getInstructions());
                            uploadedImagePath = r.getImage() != null ? r.getImage() : "";
                            etImageUrl.setText(uploadedImagePath);
                            ImageUtils.loadRecipeImage(AddEditRecipeActivity.this, uploadedImagePath, ivPreview);

                            for (int i = 0; i < categories.size(); i++) {
                                if (categories.get(i).getId() == r.getCategoryId()) {
                                    spinnerCategory.setSelection(i);
                                    break;
                                }
                            }

                            llIngredientsContainer.removeAllViews();
                            if (r.getIngredients() != null && !r.getIngredients().isEmpty()) {
                                for (Ingredient ing : r.getIngredients()) {
                                    addIngredientRow(ing);
                                }
                            } else {
                                addIngredientRow(null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Recipe>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void addIngredientRow(Ingredient ing) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_ingredient_input, llIngredientsContainer, false);
        EditText etIngName = row.findViewById(R.id.etIngName);
        EditText etIngQty = row.findViewById(R.id.etIngQty);
        EditText etIngUnit = row.findViewById(R.id.etIngUnit);
        if (ing != null) {
            etIngName.setText(ing.getIngredientName());
            etIngQty.setText(ing.getQuantity());
            etIngUnit.setText(ing.getUnit());
        }
        row.findViewById(R.id.btnRemoveIng).setOnClickListener(v -> llIngredientsContainer.removeView(row));
        llIngredientsContainer.addView(row);
    }

    private void uploadImageToServer(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            is.close();

            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), buffer.toByteArray());
            MultipartBody.Part part = MultipartBody.Part.createFormData("image", "recipe_" + System.currentTimeMillis() + ".jpg", reqFile);

            RetrofitClient.getApiService(this).uploadImage(part)
                    .enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Map<String, String>>> call,
                                               Response<ApiResponse<Map<String, String>>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                uploadedImagePath = response.body().getData().get("url");
                                etImageUrl.setText(uploadedImagePath);
                                Toast.makeText(AddEditRecipeActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                            Toast.makeText(AddEditRecipeActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Could not read image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRecipe() {
        String name = getText(etName);
        String desc = getText(etDescription);
        String prepStr = getText(etPrepTime);
        String cookStr = getText(etCookTime);
        String servingsStr = getText(etServings);
        String instructions = getText(etInstructions);

        if (name.isEmpty()) {
            etName.setError("Recipe name is required");
            return;
        }
        if (categories.isEmpty()) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (instructions.isEmpty()) {
            etInstructions.setError("Instructions are required");
            return;
        }

        List<Map<String, String>> ingredientsPayload = new ArrayList<>();
        for (int i = 0; i < llIngredientsContainer.getChildCount(); i++) {
            View row = llIngredientsContainer.getChildAt(i);
            String ingName = ((EditText) row.findViewById(R.id.etIngName)).getText().toString().trim();
            String ingQty = ((EditText) row.findViewById(R.id.etIngQty)).getText().toString().trim();
            String ingUnit = ((EditText) row.findViewById(R.id.etIngUnit)).getText().toString().trim();
            if (!ingName.isEmpty()) {
                Map<String, String> item = new HashMap<>();
                item.put("ingredient_name", ingName);
                item.put("quantity", ingQty.isEmpty() ? "1" : ingQty);
                item.put("unit", ingUnit.isEmpty() ? "pcs" : ingUnit);
                ingredientsPayload.add(item);
            }
        }
        if (ingredientsPayload.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCat = (Category) spinnerCategory.getSelectedItem();
        String img = etImageUrl.getText().toString().trim();
        if (img.isEmpty()) img = uploadedImagePath;

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", session.getUserId());
        body.put("category_id", selectedCat.getId());
        body.put("name", name);
        body.put("description", desc);
        body.put("image", img);
        body.put("preparation_time", parseIntOrDefault(prepStr, 10));
        body.put("cooking_time", parseIntOrDefault(cookStr, 20));
        body.put("servings", parseIntOrDefault(servingsStr, 2));
        body.put("instructions", instructions);
        body.put("ingredients", ingredientsPayload);

        progressBar.setVisibility(View.VISIBLE);
        Call<ApiResponse<Recipe>> call = editingRecipeId > 0
                ? RetrofitClient.getApiService(this).updateRecipe(editingRecipeId, body)
                : RetrofitClient.getApiService(this).createRecipe(body);

        call.enqueue(new Callback<ApiResponse<Recipe>>() {
            @Override
            public void onResponse(Call<ApiResponse<Recipe>> call, Response<ApiResponse<Recipe>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddEditRecipeActivity.this,
                            editingRecipeId > 0 ? "Recipe updated!" : "Recipe created!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddEditRecipeActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Recipe>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddEditRecipeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private int parseIntOrDefault(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }
}
