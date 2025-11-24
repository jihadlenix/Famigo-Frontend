package com.example.famigo_android.data.auth;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/auth/signup")
    Call<UserOut> signup(@Body SignupRequest body);

    @FormUrlEncoded
    @POST("/auth/token")
    Call<TokenOut> token(
            @Field("username") String email,   // backend uses form.username as email
            @Field("password") String password
    );
}
