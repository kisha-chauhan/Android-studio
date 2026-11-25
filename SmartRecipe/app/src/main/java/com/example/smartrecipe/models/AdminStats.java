package com.example.smartrecipe.models;

import com.google.gson.annotations.SerializedName;

public class AdminStats {
    @SerializedName("total_users")
    private int totalUsers;

    @SerializedName("total_recipes")
    private int totalRecipes;

    @SerializedName("total_categories")
    private int totalCategories;

    @SerializedName("total_reviews")
    private int totalReviews;

    @SerializedName("total_favorites")
    private int totalFavorites;

    public int getTotalUsers() { return totalUsers; }
    public int getTotalRecipes() { return totalRecipes; }
    public int getTotalCategories() { return totalCategories; }
    public int getTotalReviews() { return totalReviews; }
    public int getTotalFavorites() { return totalFavorites; }
}
