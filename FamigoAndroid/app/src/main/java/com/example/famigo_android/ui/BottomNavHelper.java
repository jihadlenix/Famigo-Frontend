package com.example.famigo_android.ui;

import android.content.Intent;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.ui.auth.ProfileActivity;
import com.example.famigo_android.ui.rewards.StoreActivity;

public class BottomNavHelper {

    public static void setup(final AppCompatActivity activity) {
        ImageButton homeBtn     = activity.findViewById(R.id.nav_home);
        ImageButton messagesBtn = activity.findViewById(R.id.nav_messages);
        ImageButton profileBtn  = activity.findViewById(R.id.nav_profile);

        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                // Example: home goes to StoreActivity
                if (!(activity instanceof StoreActivity)) {
                    Intent i = new Intent(activity, StoreActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(i);
                }
            });
        }

        // You can wire messagesBtn later if you want

        if (profileBtn != null) {
            profileBtn.setOnClickListener(v -> {
                // Avoid reopening if already on profile
                if (!(activity instanceof ProfileActivity)) {
                    Intent i = new Intent(activity, ProfileActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(i);
                }
            });
        }
    }
}
