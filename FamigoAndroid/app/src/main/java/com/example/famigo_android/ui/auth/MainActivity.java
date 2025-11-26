package com.example.famigo_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.AuthRepository;
import com.example.famigo_android.data.auth.TokenOut;
import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.family.FamilyOut;
import com.example.famigo_android.data.family.FamilyRepository;
import com.example.famigo_android.ui.family.HomeActivity;          // 👈 import
import com.example.famigo_android.ui.rewards.StoreActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText emailEt, passwordEt;
    private Button loginBtn;
    private TextView toSignupBtn;
    private AuthRepository repo;
    private FamilyRepository familyRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repo = new AuthRepository(this);
        familyRepo = new FamilyRepository(this);

        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        loginBtn = findViewById(R.id.loginBtn);
        toSignupBtn = findViewById(R.id.toSignupBtn);

        loginBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString().trim();
            String pass = passwordEt.getText().toString();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            repo.login(email, pass).enqueue(new Callback<TokenOut>() {
                @Override
                public void onResponse(Call<TokenOut> call, Response<TokenOut> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        repo.persistTokens(response.body());
                        checkUserFamilies();   // 🔥 decide where to go next
                    } else {
                        Toast.makeText(MainActivity.this, "Incorrect credentials", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<TokenOut> call, Throwable t) {
                    Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        toSignupBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SignupActivity.class))
        );
    }

    private void checkUserFamilies() {
        familyRepo.getMyFamilies().enqueue(new Callback<List<FamilyOut>>() {
            @Override
            public void onResponse(Call<List<FamilyOut>> call, Response<List<FamilyOut>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MainActivity.this, "Failed to load families", Toast.LENGTH_LONG).show();
                    return;
                }

                List<FamilyOut> families = response.body();
                TokenStore tokenStore = new TokenStore(MainActivity.this);

                if (families.isEmpty()) {
                    // 👇 CHANGE: if user has NO family, go to HomeActivity
                    // where they can choose Register OR Join.
                    Intent i = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    // user already has at least one family → keep current behaviour
                    tokenStore.saveFamilyId(families.get(0).id);

                    Intent i = new Intent(MainActivity.this, StoreActivity.class);
                    i.putExtra("FAMILY_ID", families.get(0).id);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<FamilyOut>> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
