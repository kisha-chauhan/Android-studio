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
import com.example.smartrecipe.models.MealPlan;
import com.example.smartrecipe.utils.ImageUtils;

import java.util.List;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.ViewHolder> {

    public interface OnMealPlanListener {
        void onViewRecipe(MealPlan mealPlan);
        void onEdit(MealPlan mealPlan);
        void onDelete(MealPlan mealPlan, int position);
    }

    private final Context context;
    private final List<MealPlan> mealPlans;
    private final OnMealPlanListener listener;

    public MealPlanAdapter(Context context, List<MealPlan> mealPlans, OnMealPlanListener listener) {
        this.context = context;
        this.mealPlans = mealPlans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealPlan plan = mealPlans.get(position);
        holder.tvMealType.setText(plan.getMealType());
        holder.tvRecipeName.setText(plan.getRecipeName() != null ? plan.getRecipeName() : "Recipe #" + plan.getRecipeId());
        String dateText = plan.getMealDate() != null && plan.getMealDate().length() >= 10
                ? plan.getMealDate().substring(0, 10) : plan.getMealDate();
        holder.tvMealDate.setText(dateText + " • " + plan.getCookingTime() + " mins");
        ImageUtils.loadRecipeImage(context, plan.getRecipeImage(), holder.ivImage);

        holder.itemView.setOnClickListener(v -> listener.onViewRecipe(plan));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(plan));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(plan, holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return mealPlans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvMealType, tvRecipeName, tvMealDate;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivMealImage);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvRecipeName = itemView.findViewById(R.id.tvMealRecipeName);
            tvMealDate = itemView.findViewById(R.id.tvMealDate);
            btnEdit = itemView.findViewById(R.id.btnEditMeal);
            btnDelete = itemView.findViewById(R.id.btnDeleteMeal);
        }
    }
}
