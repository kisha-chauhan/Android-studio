package com.example.smartrecipe.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.Recipe;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeActionHelper {

    public interface FavoriteCallback {
        void onUpdated(boolean isFavorite);
    }

    public static void toggleFavorite(Context context, int userId, Recipe recipe, FavoriteCallback callback) {
        if (!NetworkUtils.isConnected(context)) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Integer> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("recipe_id", recipe.getId());

        RetrofitClient.getApiService(context).toggleOrAddFavorite(body)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                           Response<ApiResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> data = response.body().getData();
                            boolean nowFav = data != null && Boolean.TRUE.equals(data.get("is_favorite"));
                            recipe.setFavorite(nowFav);
                            Toast.makeText(context,
                                    nowFav ? "Added to favorites" : "Removed from favorites",
                                    Toast.LENGTH_SHORT).show();
                            if (callback != null) callback.onUpdated(nowFav);
                        } else {
                            Toast.makeText(context, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
