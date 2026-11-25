package com.example.smartrecipe.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smartrecipe.R;
import com.example.smartrecipe.activities.RecipeDetailsActivity;
import com.example.smartrecipe.adapters.RecipeAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Favorite;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.RecipeActionHelper;
import com.example.smartrecipe.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<Recipe> favoriteRecipes = new ArrayList<>();
    private RecipeAdapter adapter;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        session = new SessionManager(requireContext());

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressFavorites);
        tvEmpty = view.findViewById(R.id.tvEmptyFavorites);

        RecyclerView rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecipeAdapter(requireContext(), favoriteRecipes, false, new RecipeAdapter.OnRecipeActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(requireContext(), RecipeDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteToggle(Recipe recipe, int position) {
                RecipeActionHelper.toggleFavorite(requireContext(), session.getUserId(), recipe, isFav -> {
                    if (!isFav && position >= 0 && position < favoriteRecipes.size()) {
                        favoriteRecipes.remove(position);
                        adapter.notifyItemRemoved(position);
                        tvEmpty.setVisibility(favoriteRecipes.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }
        });
        rvFavorites.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadFavorites);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        if (!NetworkUtils.isConnected(requireContext())) {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).getFavorites(session.getUserId())
                .enqueue(new Callback<ApiResponse<List<Favorite>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Favorite>>> call,
                                           Response<ApiResponse<List<Favorite>>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            favoriteRecipes.clear();
                            for (Favorite fav : response.body().getData()) {
                                if (fav.getRecipe() != null) {
                                    fav.getRecipe().setFavorite(true);
                                    favoriteRecipes.add(fav.getRecipe());
                                }
                            }
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(favoriteRecipes.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Favorite>>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
