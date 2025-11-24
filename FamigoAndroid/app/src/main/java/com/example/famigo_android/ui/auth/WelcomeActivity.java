package com.example.famigo_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famigo_android.R;
import com.example.famigo_android.ui.family.HomeActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button goLogin = findViewById(R.id.goLoginBtn);
        Button goSignup = findViewById(R.id.goSignupBtn);

        goLogin.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class))
        );

        goSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );
    }
}
