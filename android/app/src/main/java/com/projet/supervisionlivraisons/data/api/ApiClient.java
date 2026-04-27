package com.projet.supervisionlivraisons.data.api;

import android.content.Context;

import com.projet.supervisionlivraisons.BuildConfig;
import com.projet.supervisionlivraisons.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton holder for the Retrofit instance.
 * Uses {@link AuthInterceptor} so the Bearer token is added transparently.
 */
public final class ApiClient {

    private static volatile ApiService INSTANCE;

    private ApiClient() {}

    public static ApiService get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (ApiClient.class) {
                if (INSTANCE == null) INSTANCE = build(ctx.getApplicationContext());
            }
        }
        return INSTANCE;
    }

    private static ApiService build(Context appCtx) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(new SessionManager(appCtx)))
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(ApiService.class);
    }
}
