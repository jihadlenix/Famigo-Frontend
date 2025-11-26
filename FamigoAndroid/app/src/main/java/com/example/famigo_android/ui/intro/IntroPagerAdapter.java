package com.example.famigo_android.ui.intro;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class IntroPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 4;

    public IntroPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return IntroSlideFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
