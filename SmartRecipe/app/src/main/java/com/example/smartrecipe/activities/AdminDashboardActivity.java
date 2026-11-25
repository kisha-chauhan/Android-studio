package com.example.smartrecipe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.AdminStats;
import com.example.smartrecipe.utils.SessionManager;
import com.example.smartrecipe.utils.UiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvUsers, tvRecipes, tvCategories, tvReviews, tvFavorites;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        UiUtils.applyToolbarInsets(findViewById(R.id.toolbar));

        session = new SessionManager(this);
        tvUsers = findViewById(R.id.tvTotalUsers);
        tvRecipes = findViewById(R.id.tvTotalRecipes);
        tvCategories = findViewById(R.id.tvTotalCategories);
        tvReviews = findViewById(R.id.tvTotalReviews);
        tvFavorites = findViewById(R.id.tvTotalFavorites);

        findViewById(R.id.btnManageUsers).setOnClickListener(v ->
                startActivity(new Intent(this, AdminManageUsersActivity.class)));
        findViewById(R.id.btnManageRecipes).setOnClickListener(v ->
                startActivity(new Intent(this, AdminManageRecipesActivity.class)));
        findViewById(R.id.btnManageCategories).setOnClickListener(v ->
                startActivity(new Intent(this, AdminManageCategoriesActivity.class)));
        findViewById(R.id.btnManageReviews).setOnClickListener(v ->
                startActivity(new Intent(this, AdminManageReviewsActivity.class)));

        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        RetrofitClient.getApiService(this).getAdminStats().enqueue(new Callback<ApiResponse<AdminStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<AdminStats>> call, Response<ApiResponse<AdminStats>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AdminStats s = response.body().getData();
                    tvUsers.setText(String.valueOf(s.getTotalUsers()));
                    tvRecipes.setText(String.valueOf(s.getTotalRecipes()));
                    tvCategories.setText(String.valueOf(s.getTotalCategories()));
                    tvReviews.setText(String.valueOf(s.getTotalReviews()));
                    tvFavorites.setText(String.valueOf(s.getTotalFavorites()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AdminStats>> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load dashboard stats", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
