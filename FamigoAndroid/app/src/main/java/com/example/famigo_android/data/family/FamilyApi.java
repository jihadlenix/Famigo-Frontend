package com.example.famigo_android.data.family;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FamilyApi {

    // create family
    @POST("/families/")
    Call<FamilyOut> createFamily(
            @Header("Authorization") String bearer,
            @Body FamilyCreate body
    );

    // join by secret code
    @POST("/families/join/secret/{code}")
    Call<MemberOut> joinBySecret(
            @Header("Authorization") String bearer,
            @Path("code") String code
    );
}
