package com.example.famigo_android.ui.family;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.family.FamilyRepository;
import com.example.famigo_android.data.family.MemberOut;
import com.example.famigo_android.ui.rewards.StoreActivity;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinFamilyActivity extends AppCompatActivity {

    private EditText secretCodeEt;
    private Button joinBtn;
    private FamilyRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_family);

        repo = new FamilyRepository(this);

        secretCodeEt = findViewById(R.id.secretCodeEt);
        joinBtn = findViewById(R.id.joinFamilyBtn);

        joinBtn.setOnClickListener(v -> {
            String code = secretCodeEt.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Enter secret code", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log the code being sent for debugging
            Log.d("JoinFamily", "Attempting to join with code: '" + code + "' (length: " + code.length() + ")");

            repo.joinSecret(code).enqueue(new Callback<MemberOut>() {
                @Override
                public void onResponse(Call<MemberOut> call, Response<MemberOut> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MemberOut member = response.body();
                        Toast.makeText(JoinFamilyActivity.this, "Joined family!", Toast.LENGTH_LONG).show();
                        
                        // Finish activity after successful join
                        finish();
                    } else {
                        // Try to get error message from response body
                        String errorMessage = "Invalid code";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e("JoinFamily", "Error response: " + response.code() + " - " + errorBody);
                                // Try to extract error message from JSON
                                if (errorBody.contains("\"detail\"")) {
                                    int detailStart = errorBody.indexOf("\"detail\"") + 9;
                                    int detailEnd = errorBody.indexOf("\"", detailStart);
                                    if (detailEnd > detailStart) {
                                        errorMessage = errorBody.substring(detailStart, detailEnd);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            Log.e("JoinFamily", "Error reading response body", e);
                        }
                        
                        // Show specific error messages
                        if (response.code() == 404) {
                            errorMessage = "Invalid secret code";
                        } else if (response.code() == 409) {
                            errorMessage = "Already a member of this family";
                        }
                        
                        Toast.makeText(JoinFamilyActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<MemberOut> call, Throwable t) {
                    Log.e("JoinFamily", "Network error", t);
                    String errorMsg = t.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "Network error. Check your connection and backend URL.";
                    }
                    Toast.makeText(JoinFamilyActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
