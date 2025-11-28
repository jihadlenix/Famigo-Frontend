package com.example.famigo_android.data.user;

import android.content.Context;

import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.network.ApiClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    public Call<MeOut> updateMe(UserUpdate update) {
        return api.updateMe(bearer(), update);
    }

    public Call<MeOut> uploadProfilePicture(File imageFile) {
        // Create request body for file
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                imageFile
        );

        // Create multipart body part
        MultipartBody.Part body = MultipartBody.Part.createFormData(
                "file",
                imageFile.getName(),
                requestFile
        );

        return api.uploadProfilePicture(bearer(), body);
    }
}
