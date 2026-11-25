package com.example.smartrecipe.api;

import com.example.smartrecipe.models.AdminStats;
import com.example.smartrecipe.models.AuthResponse;
import com.example.smartrecipe.models.Category;
import com.example.smartrecipe.models.Favorite;
import com.example.smartrecipe.models.MealPlan;
import com.example.smartrecipe.models.Recipe;
import com.example.smartrecipe.models.Review;
import com.example.smartrecipe.models.ShoppingItem;
import com.example.smartrecipe.models.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==================== AUTHENTICATION ====================
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body Map<String, String> body);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body Map<String, String> body);

    @POST("api/auth/forgot-password")
    Call<ApiResponse<Object>> forgotPassword(@Body Map<String, String> body);

    // ==================== USERS ====================
    @GET("api/users/{id}")
    Call<ApiResponse<User>> getUser(@Path("id") int userId);

    @PUT("api/users/{id}")
    Call<ApiResponse<User>> updateUser(@Path("id") int userId, @Body Map<String, String> body);

    @PUT("api/users/{id}/password")
    Call<ApiResponse<Object>> changePassword(@Path("id") int userId, @Body Map<String, String> body);

    @DELETE("api/users/{id}")
    Call<ApiResponse<Object>> deleteUser(@Path("id") int userId);

    // ==================== UPLOAD ====================
    @Multipart
    @POST("api/upload")
    Call<ApiResponse<Map<String, String>>> uploadImage(@Part MultipartBody.Part image);

    // ==================== CATEGORIES ====================
    @GET("api/categories")
    Call<ApiResponse<List<Category>>> getCategories();

    @POST("api/categories")
    Call<ApiResponse<Category>> createCategory(@Body Map<String, String> body);

    @PUT("api/categories/{id}")
    Call<ApiResponse<Category>> updateCategory(@Path("id") int id, @Body Map<String, String> body);

    @DELETE("api/categories/{id}")
    Call<ApiResponse<Object>> deleteCategory(@Path("id") int id);

    // ==================== RECIPES ====================
    @GET("api/recipes")
    Call<ApiResponse<List<Recipe>>> getRecipes(
            @Query("sort") String sort,
            @Query("user_id") Integer viewerUserId,
            @Query("author_id") Integer authorId
    );

    @GET("api/recipes/{id}")
    Call<ApiResponse<Recipe>> getRecipeDetails(@Path("id") int recipeId, @Query("user_id") Integer viewerUserId);

    @POST("api/recipes")
    Call<ApiResponse<Recipe>> createRecipe(@Body Map<String, Object> body);

    @PUT("api/recipes/{id}")
    Call<ApiResponse<Recipe>> updateRecipe(@Path("id") int recipeId, @Body Map<String, Object> body);

    @DELETE("api/recipes/{id}")
    Call<ApiResponse<Object>> deleteRecipe(@Path("id") int recipeId);

    // ==================== SEARCH & WHAT CAN I COOK ====================
    @GET("api/recipes/search")
    Call<ApiResponse<List<Recipe>>> searchRecipes(
            @Query("q") String query,
            @Query("category_id") Integer categoryId,
            @Query("ingredient") String ingredient,
            @Query("user_id") Integer viewerUserId
    );

    @GET("api/recipes/category/{categoryId}")
    Call<ApiResponse<List<Recipe>>> getRecipesByCategory(
            @Path("categoryId") int categoryId,
            @Query("user_id") Integer viewerUserId
    );

    @POST("api/recipes/by-ingredients")
    Call<ApiResponse<List<Recipe>>> getRecipesByIngredients(@Body Map<String, Object> body);

    // ==================== FAVORITES ====================
    @POST("api/favorites")
    Call<ApiResponse<Map<String, Object>>> toggleOrAddFavorite(@Body Map<String, Integer> body);

    @GET("api/favorites/{userId}")
    Call<ApiResponse<List<Favorite>>> getFavorites(@Path("userId") int userId);

    @DELETE("api/favorites/{id}")
    Call<ApiResponse<Object>> deleteFavorite(@Path("id") int favoriteId);

    // ==================== MEAL PLANNER ====================
    @GET("api/meal-plans/{userId}")
    Call<ApiResponse<List<MealPlan>>> getMealPlans(
            @Path("userId") int userId,
            @Query("date") String mealDate
    );

    @POST("api/meal-plans")
    Call<ApiResponse<MealPlan>> createMealPlan(@Body Map<String, Object> body);

    @PUT("api/meal-plans/{id}")
    Call<ApiResponse<MealPlan>> updateMealPlan(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("api/meal-plans/{id}")
    Call<ApiResponse<Object>> deleteMealPlan(@Path("id") int id);

    // ==================== SHOPPING LIST ====================
    @GET("api/shopping-list/{userId}")
    Call<ApiResponse<List<ShoppingItem>>> getShoppingList(@Path("userId") int userId);

    @POST("api/shopping-list")
    Call<ApiResponse<ShoppingItem>> addShoppingItem(@Body Map<String, Object> body);

    @POST("api/shopping-list/bulk")
    Call<ApiResponse<Object>> addBulkShoppingItems(@Body Map<String, Object> body);

    @PUT("api/shopping-list/{id}")
    Call<ApiResponse<ShoppingItem>> updateShoppingItem(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("api/shopping-list/{id}")
    Call<ApiResponse<Object>> deleteShoppingItem(@Path("id") int id);

    // ==================== REVIEWS ====================
    @GET("api/reviews/recipe/{recipeId}")
    Call<ApiResponse<List<Review>>> getRecipeReviews(@Path("recipeId") int recipeId);

    @GET("api/reviews/user/{userId}")
    Call<ApiResponse<List<Review>>> getUserReviews(@Path("userId") int userId);

    @POST("api/reviews")
    Call<ApiResponse<Review>> addReview(@Body Map<String, Object> body);

    @PUT("api/reviews/{id}")
    Call<ApiResponse<Review>> updateReview(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("api/reviews/{id}")
    Call<ApiResponse<Object>> deleteReview(@Path("id") int id);

    // ==================== ADMIN ENDPOINTS ====================
    @GET("api/admin/dashboard")
    Call<ApiResponse<AdminStats>> getAdminStats();

    @GET("api/admin/users")
    Call<ApiResponse<List<User>>> getAllUsers();

    @POST("api/admin/users")
    Call<ApiResponse<User>> adminCreateUser(@Body Map<String, String> body);

    @GET("api/admin/reviews")
    Call<ApiResponse<List<Review>>> getAllReviews();
}
