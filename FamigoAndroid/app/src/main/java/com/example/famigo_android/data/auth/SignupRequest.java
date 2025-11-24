package com.example.famigo_android.data.auth;

public class SignupRequest {
    public String email;
    public String password;
    public String username;   // nullable
    public String full_name;  // nullable

    public SignupRequest(String email, String password, String username, String full_name) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.full_name = full_name;
    }
}
