package com.example.smartrecipe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.ImageUtils;

import java.util.List;
import java.util.Locale;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeActionListener {
        void onRecipeClick(Recipe recipe);
        void onFavoriteToggle(Recipe recipe, int position);
    }

    private final Context context;
    private final List<Recipe> recipes;
    private final boolean isHorizontal;
    private final OnRecipeActionListener listener;

    public RecipeAdapter(Context context, List<Recipe> recipes, boolean isHorizontal, OnRecipeActionListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.isHorizontal = isHorizontal;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isHorizontal ? R.layout.item_recipe_horizontal : R.layout.item_recipe;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.tvName.setText(recipe.getName());
        holder.tvCategory.setText(recipe.getCategoryName() != null ? recipe.getCategoryName() : "Recipe");
        holder.tvCookingTime.setText(recipe.getTotalTime() + " mins");
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f", recipe.getAverageRating()));

        ImageUtils.loadRecipeImage(context, recipe.getImage(), holder.ivImage);

        holder.btnFavorite.setImageResource(
                recipe.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClick(recipe);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteToggle(recipe, holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvCategory, tvName, tvCookingTime, tvRating;
        ImageButton btnFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivRecipeImage);
            tvCategory = itemView.findViewById(R.id.tvCategoryBadge);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvCookingTime = itemView.findViewById(R.id.tvCookingTime);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
