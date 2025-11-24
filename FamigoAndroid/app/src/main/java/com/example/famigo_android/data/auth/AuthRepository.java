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

    public Call<UserOut> signup(String fullName, String email, String password) {
        SignupRequest req = new SignupRequest(email, password, null, fullName);
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
}
