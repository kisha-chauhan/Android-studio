package com.example.smartrecipe.models;

import com.google.gson.annotations.SerializedName;

public class MealPlan {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("recipe_id")
    private int recipeId;

    @SerializedName("meal_date")
    private String mealDate;

    @SerializedName("meal_type")
    private String mealType;

    @SerializedName("recipe_name")
    private String recipeName;

    @SerializedName("recipe_image")
    private String recipeImage;

    @SerializedName("cooking_time")
    private int cookingTime;

    @SerializedName("category_name")
    private String categoryName;

    public MealPlan() {}

    public MealPlan(int userId, int recipeId, String mealDate, String mealType) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.mealDate = mealDate;
        this.mealType = mealType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

    public String getMealDate() { return mealDate; }
    public void setMealDate(String mealDate) { this.mealDate = mealDate; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public String getRecipeImage() { return recipeImage; }
    public void setRecipeImage(String recipeImage) { this.recipeImage = recipeImage; }

    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
