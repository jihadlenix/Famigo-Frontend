package com.example.famigo_android.ui.family;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.family.FamilyRepository;
import com.example.famigo_android.data.family.MemberOut;

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

            repo.joinSecret(code).enqueue(new Callback<MemberOut>() {
                @Override
                public void onResponse(Call<MemberOut> call, Response<MemberOut> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(JoinFamilyActivity.this, "Joined family!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(JoinFamilyActivity.this, "Invalid code", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<MemberOut> call, Throwable t) {
                    Toast.makeText(JoinFamilyActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
