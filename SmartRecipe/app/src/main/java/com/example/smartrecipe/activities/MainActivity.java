package com.example.smartrecipe.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartrecipe.R;
import com.example.smartrecipe.fragments.FavoritesFragment;
import com.example.smartrecipe.fragments.HomeFragment;
import com.example.smartrecipe.fragments.MealPlannerFragment;
import com.example.smartrecipe.fragments.ProfileFragment;
import com.example.smartrecipe.fragments.SearchFragment;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNav);
        UiUtils.applyToolbarInsets(toolbar);
        UiUtils.applyBottomBarInsets(bottomNav);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_shopping_list) {
                startActivity(new Intent(this, ShoppingListActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_my_recipes) {
                startActivity(new Intent(this, MyRecipesActivity.class));
                return true;
            }
            return false;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment(), "SmartRecipe");
                return true;
            } else if (id == R.id.nav_search) {
                loadFragment(new SearchFragment(), "Search Recipes");
                return true;
            } else if (id == R.id.nav_favorites) {
                loadFragment(new FavoritesFragment(), "Favorites");
                return true;
            } else if (id == R.id.nav_meal_planner) {
                loadFragment(new MealPlannerFragment(), "Meal Planner");
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment(), "Profile");
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    public void navigateToTab(int menuItemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(menuItemId);
        }
    }

    private void loadFragment(Fragment fragment, String title) {
        toolbar.setTitle(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
