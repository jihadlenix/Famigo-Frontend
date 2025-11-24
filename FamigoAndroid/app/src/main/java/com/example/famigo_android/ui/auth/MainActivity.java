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
import com.example.famigo_android.ui.family.HomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText emailEt, passwordEt;
    private Button loginBtn;
    private TextView toSignupBtn;
    private AuthRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repo = new AuthRepository(this);

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

                        Intent i = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(i);
                        finish();

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
}
