package com.example.famigo_android.data.user;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserApi {

    // GET /users/me with Authorization: Bearer <token>
    @GET("users/me")
    Call<MeOut> getMe(@Header("Authorization") String bearer);

    // PATCH /users/me with Authorization: Bearer <token>
    @PATCH("users/me")
    Call<MeOut> updateMe(@Header("Authorization") String bearer, @Body UserUpdate update);

    // POST /users/me/profile-picture - Upload profile picture
    @Multipart
    @POST("users/me/profile-picture")
    Call<MeOut> uploadProfilePicture(
            @Header("Authorization") String bearer,
            @Part MultipartBody.Part file
    );
}
