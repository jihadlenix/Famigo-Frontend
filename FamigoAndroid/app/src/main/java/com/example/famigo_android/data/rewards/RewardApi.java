package com.example.famigo_android.data.rewards;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RewardApi {

    // 1) Get all rewards for a family (store list)
    @GET("/families/{family_id}/rewards")
    Call<List<RewardOut>> getRewards(
            @Header("Authorization") String bearer,
            @Path("family_id") String familyId
    );

    // 2) Create a new reward (parent adds gift to store)
    @POST("/families/{family_id}/rewards")
    Call<RewardOut> createReward(
            @Header("Authorization") String bearer,
            @Path("family_id") String familyId,
            @Body RewardCreate body
    );

    // 3) Redeem a reward (member requests redemption)
    // 👉 IMPORTANT: confirm the exact URL with your backend friend.
    // I'm assuming: POST /rewards/{reward_id}/redeem
    @POST("/rewards/{reward_id}/redeem")
    Call<SimpleRedeemResponse> redeemInstant(
            @Header("Authorization") String bearer,
            @Path("reward_id") String rewardId
    );


}
