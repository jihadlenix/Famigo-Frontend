package com.example.famigo_android.ui.intro;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.AuthRepository;
import com.example.famigo_android.ui.auth.WelcomeActivity;
import com.example.famigo_android.ui.family.HomeActivity;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private IntroPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new IntroPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Optional: Add page change callback to handle next button visibility
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Handle last page navigation to WelcomeActivity
                if (position == 3) {
                    // You can show a different button or auto-navigate
                }
            }
        });
    }

    public void navigateToNext() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < pagerAdapter.getItemCount() - 1) {
            viewPager.setCurrentItem(currentItem + 1);
        } else {
            // Check if user is already logged in
            AuthRepository authRepo = new AuthRepository(this);
            String accessToken = authRepo.getStore().getAccessToken();
            
            if (accessToken != null && !accessToken.isEmpty()) {
                // User is logged in, go directly to HomeActivity
                startActivity(new Intent(this, HomeActivity.class));
            } else {
                // User is not logged in, go to WelcomeActivity
                startActivity(new Intent(this, WelcomeActivity.class));
            }
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
}
