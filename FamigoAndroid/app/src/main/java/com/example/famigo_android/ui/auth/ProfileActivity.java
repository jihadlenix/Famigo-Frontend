package com.example.famigo_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.user.FamilyDto;
import com.example.famigo_android.data.user.MeOut;
import com.example.famigo_android.data.user.MemberDto;
import com.example.famigo_android.data.user.UserRepository;
import com.example.famigo_android.ui.rewards.StoreActivity;
import com.example.famigo_android.ui.tasks.TasksDashboardActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView fullNameTv;
    private TextView usernameTv;
    private TextView emailTv;
    private TextView walletTv;
    private TextView familiesTv;

    private UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ---- bottom nav wiring ----
        ImageButton navHome = findViewById(R.id.nav_home);
        ImageButton navMessages = findViewById(R.id.nav_messages);
        ImageButton navProfile = findViewById(R.id.nav_profile);

        // profile is the active tab
        navProfile.setColorFilter(getColor(R.color.text_primary));
        navHome.setColorFilter(getColor(R.color.text_secondary));
        navMessages.setColorFilter(getColor(R.color.text_secondary));

        TokenStore tokenStore = new TokenStore(this);
        String familyId = tokenStore.getFamilyId();   // may be null if not set

        navHome.setOnClickListener(v -> {
            if (familyId == null) {
                Toast.makeText(this, "Family not selected", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(ProfileActivity.this, TasksDashboardActivity.class);
            i.putExtra("FAMILY_ID", familyId);
            startActivity(i);
        });

        navMessages.setOnClickListener(v -> {
            if (familyId == null) {
                Toast.makeText(this, "Family not selected", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(ProfileActivity.this, StoreActivity.class);
            i.putExtra("FAMILY_ID", familyId);
            startActivity(i);
        });

        // already on profile – do nothing
        navProfile.setOnClickListener(v -> { });

        // ---- profile UI views ----
        fullNameTv = findViewById(R.id.profileFullName);
        usernameTv = findViewById(R.id.profileUsername);
        emailTv    = findViewById(R.id.profileEmail);
        walletTv   = findViewById(R.id.profileWallet);
        familiesTv = findViewById(R.id.profileFamilies);

        // temporary placeholders
        fullNameTv.setText("Loading...");
        usernameTv.setText("");
        emailTv.setText("");
        walletTv.setText("");
        familiesTv.setText("Loading...");

        // ---- call /users/me ----
        userRepo = new UserRepository(this);
        loadMe();
    }

    private void loadMe() {
        userRepo.getMe().enqueue(new Callback<MeOut>() {
            @Override
            public void onResponse(Call<MeOut> call, Response<MeOut> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    return;
                }

                MeOut me = response.body();
                bindMe(me);
            }

            @Override
            public void onFailure(Call<MeOut> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindMe(MeOut me) {
        // basic fields
        if (me.full_name != null && !me.full_name.isEmpty()) {
            fullNameTv.setText(me.full_name);
        } else {
            fullNameTv.setText("No name set");
        }

        if (me.username != null && !me.username.isEmpty()) {
            usernameTv.setText("@" + me.username);
        } else {
            usernameTv.setText("");
        }

        emailTv.setText(me.email != null ? me.email : "");

        if (me.wallet != null) {
            walletTv.setText("Wallet: " + me.wallet.balance + " points");
        } else {
            walletTv.setText("Wallet: 0 points");
        }

        renderFamilies(me.families);
    }

    private void renderFamilies(List<FamilyDto> families) {
        if (families == null || families.isEmpty()) {
            familiesTv.setText("You are not in any family yet");
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (FamilyDto f : families) {
            if (sb.length() > 0) sb.append("\n\n");

            // family name
            String familyName = (f.name != null && !f.name.isEmpty())
                    ? f.name
                    : "Unnamed family";
            sb.append(familyName).append("\n");

            // members
            if (f.members != null && !f.members.isEmpty()) {
                for (MemberDto m : f.members) {
                    String label;

                    if (m.display_name != null && !m.display_name.isEmpty()) {
                        label = m.display_name;               // prefer display name
                    } else if (m.role != null && !m.role.isEmpty()) {
                        label = m.role.toLowerCase();        // parent / child
                    } else {
                        label = "member";
                    }

                    sb.append(" • ").append(label).append("\n");
                }
            } else {
                sb.append(" • No members yet\n");
            }
        }

        familiesTv.setText(sb.toString().trim());
    }
}
