package com.example.famigo_android.ui.rewards;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.family.FamilyMemberOut;
import com.example.famigo_android.data.family.FamilyOut;
import com.example.famigo_android.data.family.FamilyRepository;
import com.example.famigo_android.data.rewards.RewardOut;
import com.example.famigo_android.data.rewards.RewardRepository;
import com.example.famigo_android.data.rewards.SimpleRedeemResponse;
import com.example.famigo_android.data.user.MeOut;
import com.example.famigo_android.data.user.UserRepository;
import com.example.famigo_android.ui.NavigationHelper;
import com.example.famigo_android.ui.utils.FamigoToast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreActivity extends AppCompatActivity {

    private RewardRepository repo;
    private FamilyRepository familyRepo;
    private UserRepository userRepo;
    private RewardAdapter adapter;
    private String familyId;
    private String currentUserId;
    private View emptyRewards;
    private FloatingActionButton addBtn;
    private boolean isCurrentUserParent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        // Set status bar color to green
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.famigo_green_dark));
        }

        repo = new RewardRepository(this);
        familyRepo = new FamilyRepository(this);
        userRepo = new UserRepository(this);

        // Resolve familyId (from Intent or TokenStore)
        familyId = getIntent().getStringExtra("FAMILY_ID");
        if (familyId == null) {
            TokenStore store = new TokenStore(this);
            familyId = store.getFamilyId();
        }

        if (familyId == null) {
            FamigoToast.error(this, "Family ID missing!");
            finish();
            return;
        }

        // Always persist the latest familyId
        new TokenStore(this).saveFamilyId(familyId);

        // Setup unified bottom navigation
        NavigationHelper.setupBottomNavigation(this, NavigationHelper.Tab.STORE);

        // RecyclerView + FAB
        RecyclerView recycler = findViewById(R.id.rewardsRecyclerView);
        emptyRewards = findViewById(R.id.emptyRewards);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RewardAdapter(new ArrayList<>(), this::redeemReward);
        recycler.setAdapter(adapter);

        addBtn = findViewById(R.id.addRewardFab);
        addBtn.setOnClickListener(v -> showAddRewardDialog());

        // Load current user first, then check role and load rewards
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        userRepo.getMe().enqueue(new Callback<MeOut>() {
            @Override
            public void onResponse(Call<MeOut> call, Response<MeOut> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().id;
                    checkUserRole();
                } else {
                    // Still load rewards, just won't be able to check parent status
                    loadRewards();
                }
            }

            @Override
            public void onFailure(Call<MeOut> call, Throwable t) {
                // Still load rewards, just won't be able to check parent status
                loadRewards();
            }
        });
    }

    private void checkUserRole() {
        familyRepo.getFamilyById(familyId).enqueue(new Callback<FamilyOut>() {
            @Override
            public void onResponse(Call<FamilyOut> call, Response<FamilyOut> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FamilyOut family = response.body();
                    // Check if current user is a parent in this family
                    if (family.members != null && currentUserId != null) {
                        for (FamilyMemberOut member : family.members) {
                            if (currentUserId.equals(member.user_id) && "PARENT".equalsIgnoreCase(member.role)) {
                                isCurrentUserParent = true;
                                break;
                            }
                        }
                    }
                    // Show/hide FAB based on role
                    if (addBtn != null) {
                        addBtn.setVisibility(isCurrentUserParent ? View.VISIBLE : View.GONE);
                    }
                }
                // Load rewards regardless
                loadRewards();
            }

            @Override
            public void onFailure(Call<FamilyOut> call, Throwable t) {
                // Load rewards even if role check fails
        loadRewards();
            }
        });
    }

    private void loadRewards() {
        repo.getRewards(familyId).enqueue(new Callback<List<RewardOut>>() {
            @Override
            public void onResponse(Call<List<RewardOut>> call, Response<List<RewardOut>> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                RecyclerView recycler = findViewById(R.id.rewardsRecyclerView);
                if (recycler == null || emptyRewards == null) {
                    return;
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    List<RewardOut> rewards = response.body();
                    if (rewards.isEmpty()) {
                        emptyRewards.setVisibility(View.VISIBLE);
                        recycler.setVisibility(View.GONE);
                    } else {
                        emptyRewards.setVisibility(View.GONE);
                        recycler.setVisibility(View.VISIBLE);
                        adapter.setRewards(rewards);
                    }
                } else {
                    // Show empty state on error
                    emptyRewards.setVisibility(View.VISIBLE);
                    recycler.setVisibility(View.GONE);
                    
                    String msg = "Failed to load rewards";
                    try {
                        if (response.errorBody() != null) {
                            msg += " (" + response.code() + "): " + response.errorBody().string();
                        } else {
                            msg += " (" + response.code() + ")";
                        }
                    } catch (Exception ignored) {}

                    android.util.Log.e("StoreActivity", msg);
                    FamigoToast.error(StoreActivity.this, "Failed to load rewards");
                }
            }

            @Override
            public void onFailure(Call<List<RewardOut>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                RecyclerView recycler = findViewById(R.id.rewardsRecyclerView);
                if (recycler != null && emptyRewards != null) {
                    emptyRewards.setVisibility(View.VISIBLE);
                    recycler.setVisibility(View.GONE);
                }
                
                android.util.Log.e("StoreActivity", "Error loading rewards", t);
                FamigoToast.error(StoreActivity.this, t.getMessage() != null ? t.getMessage() : "Failed to load rewards");
            }
        });
    }

    private void redeemReward(RewardOut reward) {
        repo.redeemRewardNow(reward.id).enqueue(new Callback<SimpleRedeemResponse>() {
            @Override
            public void onResponse(Call<SimpleRedeemResponse> call, Response<SimpleRedeemResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    FamigoToast.success(StoreActivity.this, "Redeemed!");
                    loadRewards();
                } else {
                    String errorMsg = "Failed to redeem";
                    if (response.code() == 403) {
                        errorMsg = "Parents cannot redeem rewards";
                    } else if (response.code() == 400) {
                        errorMsg = "Not enough points";
                    }
                    FamigoToast.error(StoreActivity.this, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<SimpleRedeemResponse> call, Throwable t) {
                FamigoToast.error(StoreActivity.this, t.getMessage() != null ? t.getMessage() : "Redeem failed");
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
                FamigoToast.warning(this, "Fill title + cost");
                return;
            }

            int cost = Integer.parseInt(costStr);

            repo.createReward(familyId, title, desc, cost)
                    .enqueue(new Callback<RewardOut>() {
                        @Override
                        public void onResponse(Call<RewardOut> call, Response<RewardOut> response) {
                            if (response.isSuccessful()) {
                                FamigoToast.success(StoreActivity.this, "Reward added!");
                                loadRewards();
                            } else {
                                FamigoToast.error(StoreActivity.this, "Failed to add reward");
                            }
                        }

                        @Override
                        public void onFailure(Call<RewardOut> call, Throwable t) {
                            FamigoToast.error(StoreActivity.this, t.getMessage() != null ? t.getMessage() : "Failed to add reward");
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }
}
