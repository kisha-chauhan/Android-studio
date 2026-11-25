package com.example.smartrecipe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    public interface OnReviewActionListener {
        void onEdit(Review review);
        void onDelete(Review review, int position);
    }

    private final Context context;
    private final List<Review> reviews;
    private final int currentUserId;
    private final boolean showRecipeName;
    private final OnReviewActionListener listener;

    public ReviewAdapter(Context context, List<Review> reviews, int currentUserId,
                         boolean showRecipeName, OnReviewActionListener listener) {
        this.context = context;
        this.reviews = reviews;
        this.currentUserId = currentUserId;
        this.showRecipeName = showRecipeName;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvReviewerName.setText(review.getUserName() != null ? review.getUserName() : "User");
        holder.rbRating.setRating(review.getRating());
        holder.tvReviewText.setText(review.getReviewText());

        if (showRecipeName && review.getRecipeName() != null) {
            holder.tvRecipeName.setVisibility(View.VISIBLE);
            holder.tvRecipeName.setText("On: " + review.getRecipeName());
        } else {
            holder.tvRecipeName.setVisibility(View.GONE);
        }

        boolean isOwner = review.getUserId() == currentUserId && listener != null;
        holder.btnEdit.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(review);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(review, holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewerName, tvRecipeName, tvReviewText;
        RatingBar rbRating;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvRecipeName = itemView.findViewById(R.id.tvReviewRecipeName);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            rbRating = itemView.findViewById(R.id.rbReviewRating);
            btnEdit = itemView.findViewById(R.id.btnEditReview);
            btnDelete = itemView.findViewById(R.id.btnDeleteReview);
        }
    }
}
