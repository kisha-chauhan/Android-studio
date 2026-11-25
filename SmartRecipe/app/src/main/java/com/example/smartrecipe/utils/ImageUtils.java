package com.example.smartrecipe.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.smartrecipe.R;

public class ImageUtils {

    public static String resolveUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return Constants.BASE_URL + trimmed.substring(1);
        }
        return Constants.BASE_URL + trimmed;
    }

    public static void loadRecipeImage(Context context, String path, ImageView imageView) {
        String url = resolveUrl(path);
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_recipe_placeholder)
                .error(R.drawable.ic_recipe_placeholder)
                .centerCrop()
                .into(imageView);
    }

    public static void loadProfileImage(Context context, String path, ImageView imageView) {
        String url = resolveUrl(path);
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(imageView);
    }
}
