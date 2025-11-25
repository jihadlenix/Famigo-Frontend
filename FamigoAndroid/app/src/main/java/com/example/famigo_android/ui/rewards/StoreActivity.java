package com.example.famigo_android.ui.rewards;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famigo_android.R;
import com.example.famigo_android.data.rewards.RewardOut;
import com.example.famigo_android.data.rewards.RewardRepository;
import com.example.famigo_android.data.rewards.SimpleRedeemResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreActivity extends AppCompatActivity {

    private RewardRepository repo;
    private RewardAdapter adapter;
    private String familyId; // you should pass this via Intent extras

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        repo = new RewardRepository(this);

        // TODO: get this from the place where family was created/joined
        familyId = getIntent().getStringExtra("FAMILY_ID");

        RecyclerView recycler = findViewById(R.id.rewardsRecyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RewardAdapter(new ArrayList<>(), reward -> redeemReward(reward));
        recycler.setAdapter(adapter);

        FloatingActionButton addBtn = findViewById(R.id.addRewardFab);

        addBtn.setOnClickListener(v -> showAddRewardDialog());

        loadRewards();
    }

    private void loadRewards() {
        repo.getRewards(familyId).enqueue(new Callback<List<RewardOut>>() {
            @Override
            public void onResponse(Call<List<RewardOut>> call, Response<List<RewardOut>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setRewards(response.body());
                } else {
                    Toast.makeText(StoreActivity.this, "Failed to load rewards", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RewardOut>> call, Throwable t) {
                Toast.makeText(StoreActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redeemReward(RewardOut reward) {
        repo.redeemRewardNow(reward.id).enqueue(new Callback<SimpleRedeemResponse>() {
            @Override
            public void onResponse(Call<SimpleRedeemResponse> call, Response<SimpleRedeemResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(StoreActivity.this, "Redeemed!", Toast.LENGTH_SHORT).show();
                    loadRewards();
                } else {
                    Toast.makeText(StoreActivity.this, "Not enough points", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleRedeemResponse> call, Throwable t) {
                Toast.makeText(StoreActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddRewardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Reward");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText titleInput = new EditText(this);
        titleInput.setHint("Reward name (e.g. Ice Cream)");
        titleInput.setPadding(0, 20, 0, 20);

        EditText descInput = new EditText(this);
        descInput.setHint("Description (optional)");

        EditText costInput = new EditText(this);
        costInput.setHint("Cost in points (e.g. 20)");
        costInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        layout.addView(titleInput);
        layout.addView(descInput);
        layout.addView(costInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            String costStr = costInput.getText().toString().trim();

            if (title.isEmpty() || costStr.isEmpty()) {
                Toast.makeText(this, "Fill title + cost", Toast.LENGTH_SHORT).show();
                return;
            }

            int cost = Integer.parseInt(costStr);

            repo.createReward(familyId, title, desc, cost)
                    .enqueue(new Callback<RewardOut>() {
                        @Override
                        public void onResponse(Call<RewardOut> call, Response<RewardOut> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(StoreActivity.this, "Reward added!", Toast.LENGTH_SHORT).show();
                                loadRewards();
                            } else {
                                Toast.makeText(StoreActivity.this, "Failed to add reward", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<RewardOut> call, Throwable t) {
                            Toast.makeText(StoreActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }

}

