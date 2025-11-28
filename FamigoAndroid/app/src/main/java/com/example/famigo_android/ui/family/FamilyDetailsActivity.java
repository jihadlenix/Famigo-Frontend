package com.example.famigo_android.ui.family;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
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
import com.example.famigo_android.data.rewards.RedemptionOut;
import com.example.famigo_android.data.rewards.RewardRepository;
import com.example.famigo_android.data.user.MeOut;
import com.example.famigo_android.data.user.UserRepository;
import com.example.famigo_android.ui.utils.FamigoToast;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FamilyDetailsActivity extends AppCompatActivity {

    private FamilyRepository familyRepo;
    private RewardRepository rewardRepo;
    private UserRepository userRepo;
    private String familyId;
    private String currentUserId;
    private TextView familyNameTv;
    private TextView memberCountTv;
    private RecyclerView membersRecyclerView;
    private MemberAdapter memberAdapter;
    private boolean isCurrentUserParent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_details);

        // Set status bar color to green
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.famigo_green_dark));
        }

        familyId = getIntent().getStringExtra("FAMILY_ID");
        if (familyId == null) {
            FamigoToast.error(this, "Family ID missing!");
            finish();
            return;
        }

        familyRepo = new FamilyRepository(this);
        rewardRepo = new RewardRepository(this);
        userRepo = new UserRepository(this);
        
        familyNameTv = findViewById(R.id.familyName);
        memberCountTv = findViewById(R.id.memberCount);
        membersRecyclerView = findViewById(R.id.membersRecyclerView);

        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(null);
        memberAdapter.setOnMemberClickListener(this::showMemberRedemptions);
        membersRecyclerView.setAdapter(memberAdapter);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Load current user info first, then family details
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        userRepo.getMe().enqueue(new Callback<MeOut>() {
            @Override
            public void onResponse(Call<MeOut> call, Response<MeOut> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().id;
                    loadFamilyDetails();
                } else {
                    loadFamilyDetails(); // Still load family, just won't be able to check parent status
                }
            }

            @Override
            public void onFailure(Call<MeOut> call, Throwable t) {
                loadFamilyDetails(); // Still load family, just won't be able to check parent status
            }
        });
    }

    private void loadFamilyDetails() {
        familyRepo.getFamilyById(familyId).enqueue(new Callback<FamilyOut>() {
            @Override
            public void onResponse(Call<FamilyOut> call, Response<FamilyOut> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FamilyOut family = response.body();
                    bindFamily(family);
                } else {
                    FamigoToast.error(FamilyDetailsActivity.this, "Failed to load family details");
                }
            }

            @Override
            public void onFailure(Call<FamilyOut> call, Throwable t) {
                FamigoToast.error(FamilyDetailsActivity.this, "Error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    private void bindFamily(FamilyOut family) {
        String familyName = (family.name != null && !family.name.isEmpty())
                ? family.name
                : "Unnamed family";
        familyNameTv.setText(familyName);

        int memberCount = (family.members != null) ? family.members.size() : 0;
        memberCountTv.setText(memberCount + " member" + (memberCount != 1 ? "s" : ""));

        // Check if current user is a parent in this family
        if (family.members != null && currentUserId != null) {
            for (FamilyMemberOut member : family.members) {
                if (currentUserId.equals(member.user_id) && "PARENT".equalsIgnoreCase(member.role)) {
                    isCurrentUserParent = true;
                    break;
                }
            }
        }

        if (family.members != null && !family.members.isEmpty()) {
            memberAdapter = new MemberAdapter(family.members);
            memberAdapter.setIsParent(isCurrentUserParent);
            memberAdapter.setOnMemberClickListener(this::showMemberRedemptions);
            membersRecyclerView.setAdapter(memberAdapter);
            membersRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.emptyMembers).setVisibility(View.GONE);
        } else {
            membersRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.emptyMembers).setVisibility(View.VISIBLE);
        }
    }

    private void showMemberRedemptions(FamilyMemberOut member) {
        if (!isCurrentUserParent || !"CHILD".equalsIgnoreCase(member.role)) {
            return;
        }

        // Load redemptions for this member
        rewardRepo.getMemberRedemptions(familyId, member.id).enqueue(new Callback<List<RedemptionOut>>() {
            @Override
            public void onResponse(Call<List<RedemptionOut>> call, Response<List<RedemptionOut>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showRedemptionsDialog(member, response.body());
                } else {
                    FamigoToast.error(FamilyDetailsActivity.this, "Failed to load redemptions");
                }
            }

            @Override
            public void onFailure(Call<List<RedemptionOut>> call, Throwable t) {
                FamigoToast.error(FamilyDetailsActivity.this, "Error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    private void showRedemptionsDialog(FamilyMemberOut member, List<RedemptionOut> redemptions) {
        String memberName = (member.display_name != null && !member.display_name.isEmpty())
                ? member.display_name
                : "Child";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(memberName + "'s Redeemed Items");

        if (redemptions.isEmpty()) {
            builder.setMessage("No items redeemed yet.");
            builder.setPositiveButton("OK", null);
        } else {
            StringBuilder message = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            
            for (RedemptionOut redemption : redemptions) {
                String rewardTitle = (redemption.reward_title != null && !redemption.reward_title.isEmpty())
                        ? redemption.reward_title
                        : "Reward";
                String dateStr = redemption.redeemed_at != null
                        ? sdf.format(redemption.redeemed_at)
                        : "Recently";
                message.append("• ").append(rewardTitle).append(" - Redeemed on ").append(dateStr).append("\n");
            }
            
            builder.setMessage(message.toString());
            builder.setPositiveButton("OK", null);
        }

        builder.show();
    }
}

