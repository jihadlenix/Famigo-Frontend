package com.example.famigo_android.ui.auth;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.auth.AuthRepository;
import com.example.famigo_android.data.auth.UserOut;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameEt, emailEt, passwordEt, confirmPasswordEt, ageEt;
    private CheckBox termsCheck;
    private Button signupBtn;
    private AuthRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        repo = new AuthRepository(this);

        fullNameEt = findViewById(R.id.fullNameEt);
        usernameEt = findViewById(R.id.usernameEt);   // ✅ added
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        confirmPasswordEt = findViewById(R.id.confirmPasswordEt);
        ageEt = findViewById(R.id.ageEt);
        termsCheck = findViewById(R.id.termsCheck);
        signupBtn = findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(v -> {
            String fullName = fullNameEt.getText().toString().trim();
            String username = usernameEt.getText().toString().trim();   // ✅ added
            String email = emailEt.getText().toString().trim();
            String pass = passwordEt.getText().toString();
            String confirm = confirmPasswordEt.getText().toString();
            String ageStr = ageEt.getText().toString().trim();

            if (!termsCheck.isChecked()) {
                Toast.makeText(this, "You must accept the terms", Toast.LENGTH_SHORT).show();
                return;
            }
            if (fullName.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse age (required field)
            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 0 || age > 120) {
                    Toast.makeText(this, "Please enter a valid age (0-120)", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("SignupActivity", "Attempting signup - Email: " + email + ", Age: " + age);
            
            repo.signup(fullName, email, pass, age).enqueue(new Callback<UserOut>() {
                @Override
                public void onResponse(Call<UserOut> call, Response<UserOut> response) {
                    if (response.isSuccessful()) {
                        Log.d("SignupActivity", "Signup successful");
                        Toast.makeText(SignupActivity.this, "Account created! Please login.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        // Log error details
                        String errorMessage = "Signup failed";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e("SignupActivity", "Signup failed - Status: " + response.code() + ", Body: " + errorBody);
                                
                                // Try to extract error message from JSON
                                if (errorBody.contains("\"detail\"")) {
                                    int detailStart = errorBody.indexOf("\"detail\"") + 9;
                                    int detailEnd = errorBody.indexOf("\"", detailStart);
                                    if (detailEnd > detailStart) {
                                        errorMessage = errorBody.substring(detailStart, detailEnd);
                                    } else {
                                        // Try to find detail value after colon
                                        detailStart = errorBody.indexOf(":", errorBody.indexOf("\"detail\"")) + 1;
                                        detailEnd = errorBody.indexOf("\"", detailStart + 1);
                                        if (detailEnd > detailStart) {
                                            errorMessage = errorBody.substring(detailStart + 1, detailEnd);
                                        }
                                    }
                                } else {
                                    errorMessage = "Error " + response.code() + ": " + errorBody;
                                }
                            } else {
                                Log.e("SignupActivity", "Signup failed - Status: " + response.code() + ", No error body");
                                errorMessage = "Signup failed (Status: " + response.code() + ")";
                            }
                        } catch (IOException e) {
                            Log.e("SignupActivity", "Error reading response body", e);
                            errorMessage = "Signup failed (Status: " + response.code() + ")";
                        }
                        
                        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UserOut> call, Throwable t) {
                    Log.e("SignupActivity", "Signup network error", t);
                    String errorMsg = t.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "Network error. Check your connection and backend URL.";
                    }
                    Toast.makeText(SignupActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
