package com.example.famigo_android.data.family;

public class FamilyMemberOut {
    public String id;
    public String family_id;
    public String user_id;
    public String role;
    public String display_name;
    public String avatar_url;
    public int wallet_balance;  // Points/coins balance for this member
    public String username;  // Username from User model
    public String profile_pic;  // Profile picture from User model
    public String full_name;  // Full name from User model
    public String email;  // Email from User model (for fallback display)
}
