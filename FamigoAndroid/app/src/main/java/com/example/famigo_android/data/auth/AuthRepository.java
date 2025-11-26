package com.example.famigo_android.data.auth;

import android.content.Context;

import com.example.famigo_android.data.network.ApiClient;

import retrofit2.Call;

public class AuthRepository {

    private final AuthApi api;
    private final TokenStore store;

    public AuthRepository(Context ctx) {
        api = ApiClient.getAuthApi();
        store = new TokenStore(ctx);
    }

    // 🔥 UPDATED — now includes username between fullName and email
    public Call<UserOut> signup(String fullName, String username, String email, String password) {   // ✅ added username parameter
        SignupRequest req = new SignupRequest(email, password, username, fullName);   // ✅ username supplied instead of null
        return api.signup(req);
    }

    public Call<TokenOut> login(String email, String password) {
        return api.token(email, password);
    }

    public void persistTokens(TokenOut tokenOut) {
        store.saveAccessToken(tokenOut.access_token);
        if (tokenOut.refresh_token != null) {
            store.saveRefreshToken(tokenOut.refresh_token);
        }
    }

    public TokenStore getStore() {
        return store;
    }
    // ⭐ NEW — makes your life easier
    public String getAccessToken() {
        return store.getAccessToken();
    }

    public String getBearerToken() {
        return "Bearer " + store.getAccessToken();
    }
}
