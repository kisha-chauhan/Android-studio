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

public class MyRecipeAdapter extends RecyclerView.Adapter<MyRecipeAdapter.ViewHolder> {

    public interface OnMyRecipeListener {
        void onView(Recipe recipe);
        void onEdit(Recipe recipe);
        void onDelete(Recipe recipe, int position);
    }

    private final Context context;
    private final List<Recipe> recipes;
    private final OnMyRecipeListener listener;

    public MyRecipeAdapter(Context context, List<Recipe> recipes, OnMyRecipeListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.tvName.setText(recipe.getName());
        String cat = recipe.getCategoryName() != null ? recipe.getCategoryName() : "Recipe";
        holder.tvCatTime.setText(cat + " • " + recipe.getTotalTime() + " mins");
        ImageUtils.loadRecipeImage(context, recipe.getImage(), holder.ivImage);

        holder.itemView.setOnClickListener(v -> listener.onView(recipe));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(recipe));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(recipe, holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvCatTime;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivRecipeImage);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvCatTime = itemView.findViewById(R.id.tvCategoryAndTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
