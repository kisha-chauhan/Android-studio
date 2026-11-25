package com.example.smartrecipe.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("recipe_id")
    private int recipeId;

    @SerializedName("rating")
    private float rating;

    @SerializedName("review_text")
    private String reviewText;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("user_image")
    private String userImage;

    @SerializedName("recipe_name")
    private String recipeName;

    @SerializedName("created_at")
    private String createdAt;

    public Review() {}

    public Review(int userId, int recipeId, float rating, String reviewText) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserImage() { return userImage; }
    public void setUserImage(String userImage) { this.userImage = userImage; }

    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
