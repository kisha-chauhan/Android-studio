package com.example.smartrecipe.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.smartrecipe.R;
import com.example.smartrecipe.activities.ChangePasswordActivity;
import com.example.smartrecipe.activities.EditProfileActivity;
import com.example.smartrecipe.activities.LoginActivity;
import com.example.smartrecipe.activities.MainActivity;
import com.example.smartrecipe.activities.MyRecipesActivity;
import com.example.smartrecipe.activities.MyReviewsActivity;
import com.example.smartrecipe.activities.ShoppingListActivity;
import com.example.smartrecipe.utils.ImageUtils;
import com.example.smartrecipe.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private SessionManager session;
    private ImageView ivProfile;
    private TextView tvName, tvEmail, tvPhone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        session = new SessionManager(requireContext());

        ivProfile = view.findViewById(R.id.ivProfileImage);
        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvPhone = view.findViewById(R.id.tvProfilePhone);

        view.findViewById(R.id.rowMyRecipes).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MyRecipesActivity.class)));
        view.findViewById(R.id.rowFavorites).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_favorites);
            }
        });
        view.findViewById(R.id.rowMealPlanner).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_meal_planner);
            }
        });
        view.findViewById(R.id.rowShoppingList).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ShoppingListActivity.class)));
        view.findViewById(R.id.rowMyReviews).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MyReviewsActivity.class)));

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        view.findViewById(R.id.btnChangePassword).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshProfileHeader();
    }

    private void refreshProfileHeader() {
        tvName.setText(session.getName());
        tvEmail.setText(session.getEmail());
        tvPhone.setText(session.getPhone());
        ImageUtils.loadProfileImage(requireContext(), session.getProfileImage(), ivProfile);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    session.logout();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
