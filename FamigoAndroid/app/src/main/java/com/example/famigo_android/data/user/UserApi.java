package com.example.famigo_android.data.user;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserApi {

    // GET /users/me with Authorization: Bearer <token>
    @GET("users/me")
    Call<MeOut> getMe(@Header("Authorization") String bearer);
}
