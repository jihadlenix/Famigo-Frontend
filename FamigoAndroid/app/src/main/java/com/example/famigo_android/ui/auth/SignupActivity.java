package com.example.famigo_android.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.AuthRepository;
import com.example.famigo_android.data.auth.UserOut;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameEt, emailEt, passwordEt, confirmPasswordEt;
    private CheckBox termsCheck;
    private Button signupBtn;
    private AuthRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        repo = new AuthRepository(this);

        fullNameEt = findViewById(R.id.fullNameEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        confirmPasswordEt = findViewById(R.id.confirmPasswordEt);
        termsCheck = findViewById(R.id.termsCheck);
        signupBtn = findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(v -> {
            String fullName = fullNameEt.getText().toString().trim();
            String email = emailEt.getText().toString().trim();
            String pass = passwordEt.getText().toString();
            String confirm = confirmPasswordEt.getText().toString();

            if (!termsCheck.isChecked()) {
                Toast.makeText(this, "You must accept the terms", Toast.LENGTH_SHORT).show();
                return;
            }
            if (fullName.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            repo.signup(fullName, email, pass).enqueue(new Callback<UserOut>() {
                @Override
                public void onResponse(Call<UserOut> call, Response<UserOut> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "Account created! Please login.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, "Signup failed", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UserOut> call, Throwable t) {
                    Toast.makeText(SignupActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
