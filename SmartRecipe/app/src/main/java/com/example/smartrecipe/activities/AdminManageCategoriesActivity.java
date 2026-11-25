package com.example.smartrecipe.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.adapters.AdminRowAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Category;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManageCategoriesActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<Category> categories = new ArrayList<>();
    private AdminRowAdapter<Category> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Manage Categories");
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvAdminList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRowAdapter<>(this, categories, new AdminRowAdapter.RowBinder<Category>() {
            @Override
            public String getPrimaryText(Category item) {
                return item.getName();
            }

            @Override
            public String getSecondaryText(Category item) {
                return item.getDescription() != null ? item.getDescription() : "";
            }

            @Override
            public boolean allowEdit(Category item) {
                return true;
            }

            @Override
            public void onEdit(Category item) {
                showCategoryDialog(item);
            }

            @Override
            public void onDelete(Category item, int position) {
                deleteCategory(item, position);
            }
        });
        rv.setAdapter(adapter);

        findViewById(R.id.fabAdminAdd).setOnClickListener(v -> showCategoryDialog(null));
        loadCategories();
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categories.clear();
                    categories.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showCategoryDialog(Category existing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        EditText etName = new EditText(this);
        etName.setHint("Category Name");
        EditText etDesc = new EditText(this);
        etDesc.setHint("Description");
        EditText etImg = new EditText(this);
        etImg.setHint("Image URL (optional)");

        if (existing != null) {
            etName.setText(existing.getName());
            etDesc.setText(existing.getDescription());
            etImg.setText(existing.getImage());
        }

        layout.addView(etName);
        layout.addView(etDesc);
        layout.addView(etImg);

        new AlertDialog.Builder(this)
                .setTitle(existing != null ? "Edit Category" : "Add Category")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("name", etName.getText().toString().trim());
                    body.put("description", etDesc.getText().toString().trim());
                    body.put("image", etImg.getText().toString().trim());

                    Call<ApiResponse<Category>> call = existing != null
                            ? RetrofitClient.getApiService(this).updateCategory(existing.getId(), body)
                            : RetrofitClient.getApiService(this).createCategory(body);

                    call.enqueue(new Callback<ApiResponse<Category>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Category>> call, Response<ApiResponse<Category>> response) {
                            if (response.isSuccessful()) {
                                loadCategories();
                            } else {
                                Toast.makeText(AdminManageCategoriesActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Category>> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCategory(Category cat, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Delete category \"" + cat.getName() + "\"?")
                .setPositiveButton("Delete", (d, w) ->
                        RetrofitClient.getApiService(this).deleteCategory(cat.getId())
                                .enqueue(new Callback<ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                        if (response.isSuccessful()) {
                                            categories.remove(position);
                                            adapter.notifyItemRemoved(position);
                                        } else {
                                            Toast.makeText(AdminManageCategoriesActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
