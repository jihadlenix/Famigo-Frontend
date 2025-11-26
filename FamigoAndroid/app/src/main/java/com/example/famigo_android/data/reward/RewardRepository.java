package com.example.famigo_android.data.reward;

import android.content.Context;

import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.network.ApiClient;

import retrofit2.Call;

public class RewardRepository {

    private final RewardApi api;
    private final TokenStore store;

    public RewardRepository(Context ctx) {
        api = ApiClient.getRewardApi();
        store = new TokenStore(ctx);
    }

    private String bearer() {
        String t = store.getAccessToken();
        return "Bearer " + t;
    }

    public Call<java.util.List<RewardOut>> getFamilyRewards(String familyId) {
        return api.getFamilyRewards(bearer(), familyId);
    }

    public Call<RedemptionOut> redeemReward(String rewardId) {
        return api.redeemReward(bearer(), rewardId);
    }
}

