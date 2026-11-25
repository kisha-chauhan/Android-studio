package com.example.smartrecipe.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.activities.RecipeDetailsActivity;
import com.example.smartrecipe.adapters.MealPlanAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.MealPlan;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MealPlannerFragment extends Fragment {

    private TextView tvSelectedDate, tvEmpty;
    private ProgressBar progressBar;
    private SessionManager session;
    private String filterDate = null;

    private final List<MealPlan> mealPlans = new ArrayList<>();
    private final List<Recipe> allRecipes = new ArrayList<>();
    private MealPlanAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_planner, container, false);
        session = new SessionManager(requireContext());

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);

        RecyclerView rvMealPlans = view.findViewById(R.id.rvMealPlans);
        rvMealPlans.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MealPlanAdapter(requireContext(), mealPlans, new MealPlanAdapter.OnMealPlanListener() {
            @Override
            public void onViewRecipe(MealPlan mealPlan) {
                Intent intent = new Intent(requireContext(), RecipeDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_RECIPE_ID, mealPlan.getRecipeId());
                startActivity(intent);
            }

            @Override
            public void onEdit(MealPlan mealPlan) {
                showMealPlanDialog(mealPlan);
            }

            @Override
            public void onDelete(MealPlan mealPlan, int position) {
                deleteMealPlan(mealPlan, position);
            }
        });
        rvMealPlans.setAdapter(adapter);

        view.findViewById(R.id.btnPickDate).setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
                filterDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                tvSelectedDate.setText("Date: " + filterDate);
                loadMealPlans();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        view.findViewById(R.id.btnShowAllDates).setOnClickListener(v -> {
            filterDate = null;
            tvSelectedDate.setText("All Upcoming Meals");
            loadMealPlans();
        });

        view.findViewById(R.id.fabAddMealPlan).setOnClickListener(v -> showMealPlanDialog(null));

        preloadRecipes();
        loadMealPlans();
        return view;
    }

    private void preloadRecipes() {
        RetrofitClient.getApiService(requireContext()).getRecipes("latest", session.getUserId(), null)
                .enqueue(new Callback<ApiResponse<List<Recipe>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Recipe>>> call, Response<ApiResponse<List<Recipe>>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            allRecipes.clear();
                            allRecipes.addAll(response.body().getData());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Recipe>>> call, Throwable t) {}
                });
    }

    private void loadMealPlans() {
        if (!NetworkUtils.isConnected(requireContext())) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).getMealPlans(session.getUserId(), filterDate)
                .enqueue(new Callback<ApiResponse<List<MealPlan>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<MealPlan>>> call, Response<ApiResponse<List<MealPlan>>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            mealPlans.clear();
                            mealPlans.addAll(response.body().getData());
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(mealPlans.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<MealPlan>>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed to load meal plans", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showMealPlanDialog(MealPlan existing) {
        if (allRecipes.isEmpty()) {
            Toast.makeText(requireContext(), "Recipes still loading, please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        Calendar cal = Calendar.getInstance();
        String initialDate = existing != null && existing.getMealDate() != null
                ? existing.getMealDate().substring(0, Math.min(10, existing.getMealDate().length()))
                : (filterDate != null ? filterDate : String.format(Locale.US, "%04d-%02d-%02d",
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));

        TextView tvDate = new TextView(requireContext());
        tvDate.setText("Date: " + initialDate + " (tap to change)");
        tvDate.setTag(initialDate);
        tvDate.setPadding(0, 8, 0, 14);
        tvDate.setOnClickListener(v -> new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
            String picked = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            tvDate.setText("Date: " + picked + " (tap to change)");
            tvDate.setTag(picked);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show());

        Spinner spMealType = new Spinner(requireContext());
        spMealType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, Constants.MEAL_TYPES));
        if (existing != null) {
            for (int i = 0; i < Constants.MEAL_TYPES.length; i++) {
                if (Constants.MEAL_TYPES[i].equalsIgnoreCase(existing.getMealType())) {
                    spMealType.setSelection(i);
                    break;
                }
            }
        }

        List<String> recipeNames = new ArrayList<>();
        int selectedRecipeIndex = 0;
        for (int i = 0; i < allRecipes.size(); i++) {
            recipeNames.add(allRecipes.get(i).getName());
            if (existing != null && allRecipes.get(i).getId() == existing.getRecipeId()) {
                selectedRecipeIndex = i;
            }
        }
        Spinner spRecipe = new Spinner(requireContext());
        spRecipe.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, recipeNames));
        spRecipe.setSelection(selectedRecipeIndex);

        layout.addView(tvDate);
        layout.addView(spMealType);
        layout.addView(spRecipe);

        new AlertDialog.Builder(requireContext())
                .setTitle(existing != null ? "Edit Meal Plan" : "Plan a Meal")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    Recipe chosenRecipe = allRecipes.get(spRecipe.getSelectedItemPosition());
                    Map<String, Object> body = new HashMap<>();
                    body.put("user_id", session.getUserId());
                    body.put("recipe_id", chosenRecipe.getId());
                    body.put("meal_date", tvDate.getTag().toString());
                    body.put("meal_type", spMealType.getSelectedItem().toString());

                    Call<ApiResponse<MealPlan>> call = existing != null
                            ? RetrofitClient.getApiService(requireContext()).updateMealPlan(existing.getId(), body)
                            : RetrofitClient.getApiService(requireContext()).createMealPlan(body);

                    call.enqueue(new Callback<ApiResponse<MealPlan>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<MealPlan>> call, Response<ApiResponse<MealPlan>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Meal plan saved!", Toast.LENGTH_SHORT).show();
                                loadMealPlans();
                            } else {
                                Toast.makeText(requireContext(), NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<MealPlan>> call, Throwable t) {
                            Toast.makeText(requireContext(), "Failed to save meal plan", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMealPlan(MealPlan mealPlan, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Meal")
                .setMessage("Remove \"" + mealPlan.getRecipeName() + "\" from meal plan?")
                .setPositiveButton("Remove", (d, w) ->
                        RetrofitClient.getApiService(requireContext()).deleteMealPlan(mealPlan.getId())
                                .enqueue(new Callback<ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                        if (response.isSuccessful()) {
                                            mealPlans.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            tvEmpty.setVisibility(mealPlans.isEmpty() ? View.VISIBLE : View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
