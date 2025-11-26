package com.example.famigo_android.data.auth;

import com.google.gson.annotations.SerializedName;

public class SignupRequest {
    @SerializedName("email")
    public String email;
    
    @SerializedName("password")
    public String password;
    
    @SerializedName("username")
    public String username;   // nullable
    
    @SerializedName("full_name")
    public String full_name;  // nullable
    
    @SerializedName("age")
    public int age;           // required, age in years

    public SignupRequest(String email, String password, String username, String full_name, int age) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.full_name = full_name;
        this.age = age;
    }
}
