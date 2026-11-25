package com.example.smartrecipe.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.adapters.ReviewAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Review;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.SessionManager;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReviewsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<Review> reviews = new ArrayList<>();
    private ReviewAdapter adapter;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        session = new SessionManager(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvMyReviews);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(this, reviews, session.getUserId(), true,
                new ReviewAdapter.OnReviewActionListener() {
                    @Override
                    public void onEdit(Review review) {
                        showEditDialog(review);
                    }

                    @Override
                    public void onDelete(Review review, int position) {
                        deleteReview(review, position);
                    }
                });
        rv.setAdapter(adapter);

        loadMyReviews();
    }

    private void loadMyReviews() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getUserReviews(session.getUserId())
                .enqueue(new Callback<ApiResponse<List<Review>>>() {
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
                        Toast.makeText(MyReviewsActivity.this, "Error loading reviews", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEditDialog(Review review) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyle);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(review.getRating());
        ratingBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText etComment = new EditText(this);
        etComment.setText(review.getReviewText());

        layout.addView(ratingBar);
        layout.addView(etComment);

        new AlertDialog.Builder(this)
                .setTitle("Edit Review")
                .setView(layout)
                .setPositiveButton("Update", (d, w) -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("user_id", session.getUserId());
                    body.put("rating", Math.max(1f, ratingBar.getRating()));
                    body.put("review_text", etComment.getText().toString().trim());

                    RetrofitClient.getApiService(this).updateReview(review.getId(), body)
                            .enqueue(new Callback<ApiResponse<Review>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Review>> call, Response<ApiResponse<Review>> response) {
                                    if (response.isSuccessful()) {
                                        loadMyReviews();
                                    } else {
                                        Toast.makeText(MyReviewsActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<Review>> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReview(Review review, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Review")
                .setMessage("Delete your review for " + review.getRecipeName() + "?")
                .setPositiveButton("Delete", (d, w) ->
                        RetrofitClient.getApiService(this).deleteReview(review.getId())
                                .enqueue(new Callback<ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                        if (response.isSuccessful()) {
                                            reviews.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            tvEmpty.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
