package com.example.famigo_android.data.family;

import android.content.Context;

import com.example.famigo_android.data.auth.TokenStore;
import com.example.famigo_android.data.network.ApiClient;

import java.util.List;

import retrofit2.Call;

public class FamilyRepository {

    private final FamilyApi api;
    private final TokenStore store;

    public FamilyRepository(Context ctx){
        api = ApiClient.getFamilyApi();
        store = new TokenStore(ctx);
    }

    private String bearer(){
        String t = store.getAccessToken();
        return "Bearer " + t;
    }

    public Call<FamilyOut> createFamily(String name){
        return api.createFamily(bearer(), new FamilyCreate(name));
    }

    public Call<MemberOut> joinSecret(String code){
        return api.joinBySecret(bearer(), code.trim().toUpperCase());
    }
    public Call<List<FamilyOut>> getMyFamilies() {
        return api.getMyFamilies(bearer());
    }

}
