package com.example.famigo_android.data.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {

    private final SharedPreferences prefs;

    public TokenStore(Context ctx) {
        prefs = ctx.getSharedPreferences("auth_store", Context.MODE_PRIVATE);
    }

    public void saveAccessToken(String token) {
        prefs.edit().putString("access_token", token).apply();
    }

    public void saveRefreshToken(String token) {
        prefs.edit().putString("refresh_token", token).apply();
    }

    public String getAccessToken() {
        return prefs.getString("access_token", null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public void saveFamilyId(String id) {
        prefs.edit().putString("current_family_id", id).apply();
    }

    public String getFamilyId() {
        return prefs.getString("current_family_id", null);
    }

}
