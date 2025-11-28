package com.example.famigo_android.ui;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.ui.auth.ProfileActivity;
import com.example.famigo_android.ui.rewards.StoreActivity;
import com.example.famigo_android.ui.tasks.TasksDashboardActivity;

public class NavigationHelper {

    public enum Tab {
        HOME, TASKS, STORE, PROFILE
    }

    public static void setupBottomNavigation(AppCompatActivity activity, Tab activeTab) {
        LinearLayout navTasks = activity.findViewById(R.id.navTasks);
        LinearLayout navStore = activity.findViewById(R.id.navStore);
        LinearLayout navProfile = activity.findViewById(R.id.navProfile);

        ImageView tasksIcon = activity.findViewById(R.id.navTasksIcon);
        ImageView storeIcon = activity.findViewById(R.id.navStoreIcon);
        ImageView profileIcon = activity.findViewById(R.id.navProfileIcon);

        TextView tasksText = activity.findViewById(R.id.navTasksText);
        TextView storeText = activity.findViewById(R.id.navStoreText);
        TextView profileText = activity.findViewById(R.id.navProfileText);

        TokenStore tokenStore = new TokenStore(activity);
        String familyId = tokenStore.getFamilyId();

        // Reset all icons and text colors
        if (tasksIcon != null) {
            tasksIcon.setColorFilter(activity.getColor(R.color.text_secondary));
        }
        if (storeIcon != null) {
            storeIcon.setColorFilter(activity.getColor(R.color.text_secondary));
        }
        if (profileIcon != null) {
            profileIcon.setColorFilter(activity.getColor(R.color.text_secondary));
        }
        if (tasksText != null) {
            tasksText.setTextColor(activity.getColor(R.color.text_secondary));
        }
        if (storeText != null) {
            storeText.setTextColor(activity.getColor(R.color.text_secondary));
        }
        if (profileText != null) {
            profileText.setTextColor(activity.getColor(R.color.text_secondary));
        }

        // Set active tab
        switch (activeTab) {
            case HOME:
            case TASKS:
                if (tasksIcon != null) tasksIcon.setColorFilter(activity.getColor(R.color.famigo_green));
                if (tasksText != null) tasksText.setTextColor(activity.getColor(R.color.famigo_green));
                break;
            case STORE:
                if (storeIcon != null) storeIcon.setColorFilter(activity.getColor(R.color.famigo_green));
                if (storeText != null) storeText.setTextColor(activity.getColor(R.color.famigo_green));
                break;
            case PROFILE:
                if (profileIcon != null) profileIcon.setColorFilter(activity.getColor(R.color.famigo_green));
                if (profileText != null) profileText.setTextColor(activity.getColor(R.color.famigo_green));
                break;
        }

        // Set up click listeners
        if (navTasks != null) {
            navTasks.setOnClickListener(v -> {
                if (!(activity instanceof TasksDashboardActivity)) {
                    Intent i = new Intent(activity, TasksDashboardActivity.class);
                    if (familyId != null) {
                        i.putExtra("FAMILY_ID", familyId);
                    }
                    activity.startActivity(i);
                    activity.finish();
                }
            });
        }

        if (navStore != null) {
            navStore.setOnClickListener(v -> {
                if (!(activity instanceof StoreActivity)) {
                    Intent i = new Intent(activity, StoreActivity.class);
                    if (familyId != null) {
                        i.putExtra("FAMILY_ID", familyId);
                    }
                    activity.startActivity(i);
                    activity.finish();
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (!(activity instanceof ProfileActivity)) {
                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                    activity.finish();
                }
            });
        }
    }
}

