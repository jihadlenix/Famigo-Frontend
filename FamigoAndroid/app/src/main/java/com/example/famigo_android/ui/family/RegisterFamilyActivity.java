package com.example.famigo_android.ui.family;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.data.family.FamilyOut;
import com.example.famigo_android.data.family.FamilyRepository;
import com.example.famigo_android.ui.rewards.StoreActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFamilyActivity extends AppCompatActivity {

    private EditText familyNameEt;
    private Button registerBtn;
    private FamilyRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_family);

        repo = new FamilyRepository(this);

        familyNameEt = findViewById(R.id.familyNameEt);
        registerBtn = findViewById(R.id.registerFamilyBtn);

        registerBtn.setOnClickListener(v -> {
            String name = familyNameEt.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter family name", Toast.LENGTH_SHORT).show();
                return;
            }

            repo.createFamily(name).enqueue(new Callback<FamilyOut>() {
                @Override
                public void onResponse(Call<FamilyOut> call, Response<FamilyOut> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        FamilyOut fam = response.body();

                        Toast.makeText(RegisterFamilyActivity.this,
                                "Family created. Secret code: " + fam.secret_code,
                                Toast.LENGTH_LONG).show();

                        // ⭐ DIRECTLY OPEN STORE PAGE
                        Intent i = new Intent(RegisterFamilyActivity.this, StoreActivity.class);
                        i.putExtra("FAMILY_ID", fam.id);
                        startActivity(i);

                        finish();
                    } else {
                        Toast.makeText(RegisterFamilyActivity.this,
                                "Failed to create family", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<FamilyOut> call, Throwable t) {
                    Toast.makeText(RegisterFamilyActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
