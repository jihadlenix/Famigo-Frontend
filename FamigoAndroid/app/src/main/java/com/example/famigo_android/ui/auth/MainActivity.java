package com.example.famigo_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.AuthRepository;
import com.example.famigo_android.data.auth.TokenOut;
import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.family.FamilyOut;
import com.example.famigo_android.data.family.FamilyRepository;
import com.example.famigo_android.ui.family.HomeActivity;          // 👈 import
import com.example.famigo_android.ui.rewards.StoreActivity;
import com.example.famigo_android.ui.utils.FamigoToast;

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

        // Set status bar color to green
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.famigo_green_dark));
        }

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
                FamigoToast.warning(this, "Fill all fields");
                return;
            }

            repo.login(email, pass).enqueue(new Callback<TokenOut>() {
                @Override
                public void onResponse(Call<TokenOut> call, Response<TokenOut> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        repo.persistTokens(response.body());
                        checkUserFamilies();   // 🔥 decide where to go next
                    } else {
                        FamigoToast.error(MainActivity.this, "Incorrect credentials");
                    }
                }

                @Override
                public void onFailure(Call<TokenOut> call, Throwable t) {
                    FamigoToast.error(MainActivity.this, t.getMessage() != null ? t.getMessage() : "Login failed");
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
                    FamigoToast.error(MainActivity.this, "Failed to load families");
                    return;
                }

                List<FamilyOut> families = response.body();
                TokenStore tokenStore = new TokenStore(MainActivity.this);

                // After login, always go to HomeActivity (which will show families if they have any)
                Intent i = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<List<FamilyOut>> call, Throwable t) {
                FamigoToast.error(MainActivity.this, t.getMessage() != null ? t.getMessage() : "Failed to load families");
            }
        });
    }
}
