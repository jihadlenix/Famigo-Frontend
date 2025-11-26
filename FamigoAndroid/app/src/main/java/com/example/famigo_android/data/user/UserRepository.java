package com.example.famigo_android.data.user;

import android.content.Context;

import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.network.ApiClient;

import retrofit2.Call;

public class UserRepository {

    private final UserApi api;
    private final TokenStore store;

    public UserRepository(Context ctx) {
        api = ApiClient.getUserApi();   // same pattern as RewardRepository
        store = new TokenStore(ctx);
    }

    private String bearer() {
        String t = store.getAccessToken();
        return "Bearer " + t;
    }

    public Call<MeOut> getMe() {
        return api.getMe(bearer());
    }
}
