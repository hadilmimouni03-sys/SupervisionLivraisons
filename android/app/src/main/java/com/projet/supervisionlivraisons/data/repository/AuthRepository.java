package com.projet.supervisionlivraisons.data.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.projet.supervisionlivraisons.data.api.ApiClient;
import com.projet.supervisionlivraisons.data.api.ApiService;
import com.projet.supervisionlivraisons.data.model.LoginRequest;
import com.projet.supervisionlivraisons.data.model.LoginResponse;
import com.projet.supervisionlivraisons.utils.Resource;
import com.projet.supervisionlivraisons.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Authenticates against the API and stores the resulting JWT in {@link SessionManager}. */
public class AuthRepository {

    private final ApiService     api;
    private final SessionManager session;

    public AuthRepository(Context ctx) {
        this.api     = ApiClient.get(ctx);
        this.session = new SessionManager(ctx);
    }

    public void login(String login, String password, MutableLiveData<Resource<LoginResponse>> out) {
        out.setValue(Resource.loading());
        api.login(new LoginRequest(login, password)).enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> c, Response<LoginResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().token != null) {
                    LoginResponse body = r.body();
                    session.save(body.token, body.user.id, body.user.role,
                                 body.user.nom, body.user.prenom);
                    out.postValue(Resource.success(body));
                } else {
                    out.postValue(Resource.error("Identifiants invalides"));
                }
            }
            @Override public void onFailure(Call<LoginResponse> c, Throwable t) {
                out.postValue(Resource.error(t.getMessage()));
            }
        });
    }

    public void logout() { session.clear(); }
}
