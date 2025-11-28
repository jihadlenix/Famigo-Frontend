package com.example.famigo_android.ui.family;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.family.FamilyOut;
import com.example.famigo_android.data.family.FamilyRepository;
import com.example.famigo_android.ui.tasks.TasksDashboardActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private FamilyRepository familyRepo;
    private RecyclerView familiesRecyclerView;
    private FamilyListAdapter familyAdapter;
    private View yourFamiliesSection;
    private View noFamilySection;
    private TextView yourFamiliesTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set status bar color to green
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.famigo_green_dark));
        }

        familyRepo = new FamilyRepository(this);
        
        familiesRecyclerView = findViewById(R.id.familiesRecyclerView);
        yourFamiliesSection = findViewById(R.id.yourFamiliesSection);
        noFamilySection = findViewById(R.id.noFamilySection);
        yourFamiliesTitle = findViewById(R.id.yourFamiliesTitle);

        familiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        familyAdapter = new FamilyListAdapter(new ArrayList<>(), this::onFamilyClick);
        familiesRecyclerView.setAdapter(familyAdapter);

        Button registerBtn = findViewById(R.id.registerFamilyBtn);
        Button joinBtn = findViewById(R.id.joinFamilyBtn);

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterFamilyActivity.class))
        );

        joinBtn.setOnClickListener(v ->
                startActivity(new Intent(this, JoinFamilyActivity.class))
        );

        // Load user's families
        loadFamilies();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload families when returning to this activity
        loadFamilies();
    }

    private void loadFamilies() {
        familyRepo.getMyFamilies().enqueue(new Callback<List<FamilyOut>>() {
            @Override
            public void onResponse(Call<List<FamilyOut>> call, Response<List<FamilyOut>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FamilyOut> families = response.body();
                    updateUI(families);
                } else {
                    // Show no family section on error
                    updateUI(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<FamilyOut>> call, Throwable t) {
                // Show no family section on error
                updateUI(new ArrayList<>());
            }
        });
    }

    private void updateUI(List<FamilyOut> families) {
        if (families.isEmpty()) {
            // No families - show message and hide families section
            yourFamiliesSection.setVisibility(View.GONE);
            noFamilySection.setVisibility(View.VISIBLE);
        } else {
            // Has families - show families section and hide no family message
            yourFamiliesSection.setVisibility(View.VISIBLE);
            noFamilySection.setVisibility(View.GONE);
            familyAdapter.setFamilies(families);
        }
    }

    private void onFamilyClick(FamilyOut family) {
        // Save family ID and navigate to dashboard
        TokenStore tokenStore = new TokenStore(this);
        tokenStore.saveFamilyId(family.id);

        Intent i = new Intent(this, TasksDashboardActivity.class);
        i.putExtra("FAMILY_ID", family.id);
        startActivity(i);
    }
}
