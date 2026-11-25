package com.example.smartrecipe.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.ImageUtils;

import java.util.List;
import java.util.Locale;

public class WhatCanICookAdapter extends RecyclerView.Adapter<WhatCanICookAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final Context context;
    private final List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public WhatCanICookAdapter(Context context, List<Recipe> recipes, OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_what_can_i_cook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.tvName.setText(recipe.getName());
        holder.tvMatchPercent.setText(recipe.getMatchPercentage() + "% Match");
        holder.tvCookingTime.setText(recipe.getTotalTime() + " mins");
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f", recipe.getAverageRating()));

        List<String> missing = recipe.getMissingIngredients();
        if (missing == null || missing.isEmpty()) {
            holder.tvMissing.setText("All ingredients available!");
            holder.tvMissing.setTextColor(context.getColor(R.color.success_green));
        } else {
            holder.tvMissing.setText("Missing: " + TextUtils.join(", ", missing));
            holder.tvMissing.setTextColor(context.getColor(R.color.warning_amber));
        }

        ImageUtils.loadRecipeImage(context, recipe.getImage(), holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClick(recipe);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvMatchPercent, tvCookingTime, tvRating, tvMissing;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivRecipeImage);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvMatchPercent = itemView.findViewById(R.id.tvMatchPercent);
            tvCookingTime = itemView.findViewById(R.id.tvCookingTime);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvMissing = itemView.findViewById(R.id.tvMissingIngredients);
        }
    }
}
