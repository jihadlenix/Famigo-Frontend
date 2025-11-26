package com.example.famigo_android.ui.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.famigo_android.R;

public class IntroSlideFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private int position;

    public static IntroSlideFragment newInstance(int position) {
        IntroSlideFragment fragment = new IntroSlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view;

        if (position == 1) {
            view = inflater.inflate(R.layout.fragment_intro_slide_assign_tasks, container, false);
        } else if (position == 3) {
            view = inflater.inflate(R.layout.fragment_intro_slide_4, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_intro_slide, container, false);
        }

        View dot1 = view.findViewById(R.id.dot1);
        View dot2 = view.findViewById(R.id.dot2);
        View dot3 = view.findViewById(R.id.dot3);
        View dot4 = view.findViewById(R.id.dot4);

        if (position == 3) {
            // Set up last slide content
            TextView rewardTitle = view.findViewById(R.id.rewardTitle);
            TextView rewardDescription = view.findViewById(R.id.rewardDescription);
            if (rewardTitle != null) {
                rewardTitle.setText(R.string.intro_title_4);
            }
            if (rewardDescription != null) {
                rewardDescription.setText(R.string.intro_description_4);
            }
            
            // Set up Get Started button
            View getStartedButton = view.findViewById(R.id.getStartedButton);
            if (getStartedButton != null) {
                getStartedButton.setOnClickListener(v -> {
                    if (getActivity() instanceof IntroActivity) {
                        ((IntroActivity) getActivity()).navigateToNext();
                    }
                });
            }
            updateIndicators(dot1, dot2, dot3, dot4, 3);
        } else {
            View nextButton = view.findViewById(R.id.nextButton);
            if (nextButton != null) {
                nextButton.setOnClickListener(v -> {
                    if (getActivity() instanceof IntroActivity) {
                        ((IntroActivity) getActivity()).navigateToNext();
                    }
                });
            }
        }

        if (position == 1) {
            ImageView assignTasksImage = view.findViewById(R.id.assignTasksImage);
            assignTasksImage.setImageResource(R.drawable.intro_slide2_tasks);
            updateIndicators(dot1, dot2, dot3, dot4, 1);
        } else if (position != 3) {
            ImageView slideImage = view.findViewById(R.id.slideImage);
            TextView welcomeTitle = view.findViewById(R.id.welcomeTitle);
            TextView description = view.findViewById(R.id.description);

            switch (position) {
                case 0:
                    welcomeTitle.setText(R.string.intro_title_1);
                    description.setText(R.string.intro_description_1);
                    slideImage.setImageResource(R.drawable.intro_slide1_family);
                    updateIndicators(dot1, dot2, dot3, dot4, 0);
                    break;
                case 2:
                    welcomeTitle.setText(R.string.intro_title_3);
                    description.setText(R.string.intro_description_3);
                    slideImage.setImageResource(R.drawable.intro_slide3_ai);
                    updateIndicators(dot1, dot2, dot3, dot4, 2);
                    break;
            }
        }

        return view;
    }

    private void updateIndicators(View dot1, View dot2, View dot3, View dot4, int activePosition) {
        dot1.setBackgroundResource(activePosition == 0 ? 
            R.drawable.bg_page_indicator_active : R.drawable.bg_page_indicator_inactive);
        dot2.setBackgroundResource(activePosition == 1 ? 
            R.drawable.bg_page_indicator_active : R.drawable.bg_page_indicator_inactive);
        dot3.setBackgroundResource(activePosition == 2 ? 
            R.drawable.bg_page_indicator_active : R.drawable.bg_page_indicator_inactive);
        dot4.setBackgroundResource(activePosition == 3 ? 
            R.drawable.bg_page_indicator_active : R.drawable.bg_page_indicator_inactive);
    }
}
