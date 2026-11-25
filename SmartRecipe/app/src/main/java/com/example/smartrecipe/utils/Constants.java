package com.example.smartrecipe.utils;

public class Constants {
    // Use 10.0.2.2 for Android Emulator -> localhost on host machine.
    // Change to your PC's local Wi-Fi IP (e.g., http://192.168.1.5:5000/) when testing on a physical phone.
    public static final String BASE_URL = "http://10.0.2.2:5000/";

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    public static final String[] MEAL_TYPES = {"Breakfast", "Lunch", "Dinner", "Snacks"};
    public static final String[] SHOPPING_UNITS = {"pcs", "g", "kg", "ml", "L", "cup", "tbsp", "tsp", "packet"};

    public static final String[] QUICK_INGREDIENTS = {
            "Tomato", "Potato", "Onion", "Cheese", "Rice",
            "Paneer", "Garlic", "Ginger", "Butter", "Milk",
            "Bread", "Pasta", "Flour", "Eggs", "Spinach", "Capsicum"
    };
}
