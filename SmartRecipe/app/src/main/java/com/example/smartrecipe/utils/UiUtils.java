package com.example.smartrecipe.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

public class UiUtils {

    /**
     * Vertical LinearLayoutManager for RecyclerViews nested inside NestedScrollView,
     * ensuring all vertical touch and mouse-wheel scroll events go to NestedScrollView.
     */
    public static LinearLayoutManager nonScrollableVerticalLayoutManager(Context context) {
        return new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
    }

    /**
     * Adds status bar top padding to a Toolbar so it never overlaps the clock/system icons
     * on Android 15/16/17 edge-to-edge devices.
     */
    public static void applyToolbarInsets(View toolbar) {
        if (toolbar == null) return;
        final int initialPaddingTop = toolbar.getPaddingTop();
        final int initialHeight = toolbar.getLayoutParams() != null ? toolbar.getLayoutParams().height : 0;

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), initialPaddingTop + bars.top,
                    v.getPaddingRight(), v.getPaddingBottom());
            if (initialHeight > 0 && v.getLayoutParams() != null) {
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                lp.height = initialHeight + bars.top;
                v.setLayoutParams(lp);
            }
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(toolbar);
    }

    /**
     * Adds bottom navigation bar padding to a root or bottom view on edge-to-edge devices.
     */
    public static void applyBottomBarInsets(View bottomView) {
        if (bottomView == null) return;
        final int initialPaddingBottom = bottomView.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(bottomView, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                    v.getPaddingRight(), initialPaddingBottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(bottomView);
    }
}
