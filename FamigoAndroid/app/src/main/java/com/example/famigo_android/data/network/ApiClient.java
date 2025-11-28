package com.example.famigo_android.data.network;

import com.example.famigo_android.data.auth.AuthApi;
import com.example.famigo_android.data.family.FamilyApi;
import com.example.famigo_android.data.rewards.RewardApi;
import com.example.famigo_android.data.tasks.TaskApi;
import com.example.famigo_android.data.user.UserApi;   // 👈 NEW

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiClient {
    
   // For Android Emulator: use 10.0.2.2 to access host machine's localhost
    // For Physical Device: use your computer's IP address (e.g., "http://192.168.1.100:8000/")
    private static final String BASE_URL = "http://localhost:8000/";
    private static Retrofit retrofit = null;

    // ---------------------------
    //   SHARED RETROFIT INSTANCE
    // ---------------------------
    private static Retrofit getRetrofit() {
        if (retrofit == null) {

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }

    // ---------------------------
    //       API ACCESSORS
    // ---------------------------

    public static AuthApi getAuthApi() {
        return getRetrofit().create(AuthApi.class);
    }

    public static FamilyApi getFamilyApi() {
        return getRetrofit().create(FamilyApi.class);
    }

    public static RewardApi getRewardApi() {
        return getRetrofit().create(RewardApi.class);
    }

    public static TaskApi getTasksApi() {
        return getRetrofit().create(TaskApi.class);
    }

    // ⭐ NEW → Users API
    public static UserApi getUserApi() {
        return getRetrofit().create(UserApi.class);
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}
