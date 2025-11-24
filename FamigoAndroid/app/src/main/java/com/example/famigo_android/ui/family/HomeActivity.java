package com.example.famigo_android.ui.family;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button registerBtn = findViewById(R.id.registerFamilyBtn);
        Button joinBtn = findViewById(R.id.joinFamilyBtn);

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterFamilyActivity.class))
        );

        joinBtn.setOnClickListener(v ->
                startActivity(new Intent(this, JoinFamilyActivity.class))
        );
    }
}
