package com.example.smartrecipe.models;

import com.google.gson.annotations.SerializedName;

public class Ingredient {
    @SerializedName("id")
    private int id;

    @SerializedName("recipe_id")
    private int recipeId;

    @SerializedName("ingredient_name")
    private String ingredientName;

    @SerializedName("quantity")
    private String quantity;

    @SerializedName("unit")
    private String unit;

    public Ingredient() {}

    public Ingredient(String ingredientName, String quantity, String unit) {
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getFormattedQuantity() {
        String q = quantity != null ? quantity.trim() : "";
        String u = unit != null ? unit.trim() : "";
        return (q + " " + u).trim();
    }
}
