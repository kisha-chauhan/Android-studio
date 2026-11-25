package com.example.smartrecipe.models;

import com.google.gson.annotations.SerializedName;

public class Favorite {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("recipe_id")
    private int recipeId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("recipe")
    private Recipe recipe;

    public Favorite() {}

    public Favorite(int userId, int recipeId) {
        this.userId = userId;
        this.recipeId = recipeId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
}
