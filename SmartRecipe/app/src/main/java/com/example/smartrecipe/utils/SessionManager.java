package com.example.smartrecipe.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartrecipe.models.User;

public class SessionManager {

    private static final String PREF_NAME = "SmartRecipeSession";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_TOKEN = "jwt_token";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(User user, String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ROLE, user.getRole() != null ? user.getRole() : Constants.ROLE_USER);
        editor.putString(KEY_PROFILE_IMAGE, user.getProfileImage());
        if (token != null) {
            editor.putString(KEY_TOKEN, token);
        }
        editor.apply();
    }

    public void updateUserProfile(User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_PHONE, user.getPhone());
        if (user.getEmail() != null) {
            editor.putString(KEY_EMAIL, user.getEmail());
        }
        if (user.getProfileImage() != null) {
            editor.putString(KEY_PROFILE_IMAGE, user.getProfileImage());
        }
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getUserId() > 0;
    }

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equalsIgnoreCase(getRole());
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "Chef");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, Constants.ROLE_USER);
    }

    public String getProfileImage() {
        return prefs.getString(KEY_PROFILE_IMAGE, "");
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
