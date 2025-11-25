package com.example.famigo_android.data.network;

import com.example.famigo_android.data.auth.AuthApi;
import com.example.famigo_android.data.family.FamilyApi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.famigo_android.data.rewards.RewardApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8000";
    private static Retrofit retrofit = null;

    private static Retrofit getRetrofit() {
        if (retrofit == null) {

            // Create custom Gson to parse "yyyy-MM-dd'T'HH:mm:ss"
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")   // fixed format
                    .create();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // ← IMPORTANT
                    .build();
        }
        return retrofit;
    }


    public static AuthApi getAuthApi() {
        return getRetrofit().create(AuthApi.class);
    }

    public static FamilyApi getFamilyApi() {
        return getRetrofit().create(FamilyApi.class);
    }

    public static RewardApi getRewardApi() {
        return getRetrofit().create(RewardApi.class);
    }

}
