package com.example.famigo_android.data.reward;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RewardApi {

    // Get all rewards for a family
    @GET("/rewards/families/{family_id}/rewards")
    Call<java.util.List<RewardOut>> getFamilyRewards(
            @Header("Authorization") String bearer,
            @Path("family_id") String familyId
    );

    // Redeem a reward
    @POST("/rewards/rewards/{reward_id}/redeem")
    Call<RedemptionOut> redeemReward(
            @Header("Authorization") String bearer,
            @Path("reward_id") String rewardId
    );
}

