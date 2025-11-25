package com.example.famigo_android.data.rewards;

import android.content.Context;

import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.network.ApiClient;

import java.util.List;

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

    public Call<List<RewardOut>> getRewards(String familyId) {
        return api.getRewards(bearer(), familyId);
    }

    public Call<RewardOut> createReward(String familyId, String title, String description, int costPoints) {
        RewardCreate body = new RewardCreate(title, description, costPoints);
        return api.createReward(bearer(), familyId, body);
    }


    public Call<SimpleRedeemResponse> redeemRewardNow(String rewardId) {
        return api.redeemInstant(bearer(), rewardId);
    }
}
