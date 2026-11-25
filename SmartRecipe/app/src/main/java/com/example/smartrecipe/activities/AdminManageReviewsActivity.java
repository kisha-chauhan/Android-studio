package com.example.smartrecipe.activities;

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
import com.example.smartrecipe.adapters.AdminRowAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Review;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManageReviewsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<Review> reviews = new ArrayList<>();
    private AdminRowAdapter<Review> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Moderate Reviews");
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        findViewById(R.id.fabAdminAdd).setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvAdminList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRowAdapter<>(this, reviews, new AdminRowAdapter.RowBinder<Review>() {
            @Override
            public String getPrimaryText(Review item) {
                return item.getUserName() + " • ★ " + item.getRating() + " (" + item.getRecipeName() + ")";
            }

            @Override
            public String getSecondaryText(Review item) {
                return item.getReviewText();
            }

            @Override
            public boolean allowEdit(Review item) {
                return false;
            }

            @Override
            public void onEdit(Review item) {}

            @Override
            public void onDelete(Review item, int position) {
                deleteReview(item, position);
            }
        });
        rv.setAdapter(adapter);

        loadReviews();
    }

    private void loadReviews() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getAllReviews().enqueue(new Callback<ApiResponse<List<Review>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Review>>> call, Response<ApiResponse<List<Review>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    reviews.clear();
                    reviews.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Review>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void deleteReview(Review review, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Review")
                .setMessage("Delete this review by " + review.getUserName() + "?")
                .setPositiveButton("Delete", (d, w) ->
                        RetrofitClient.getApiService(this).deleteReview(review.getId())
                                .enqueue(new Callback<ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                        if (response.isSuccessful()) {
                                            reviews.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            tvEmpty.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
                                        } else {
                                            Toast.makeText(AdminManageReviewsActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
