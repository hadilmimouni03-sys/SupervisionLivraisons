package com.projet.supervisionlivraisons.data.api;

import com.projet.supervisionlivraisons.utils.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Adds {@code Authorization: Bearer <token>} to every outgoing request. */
public class AuthInterceptor implements Interceptor {

    private final SessionManager session;

    public AuthInterceptor(SessionManager session) {
        this.session = session;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = session.getToken();
        if (token == null || token.isEmpty()) return chain.proceed(original);

        Request authed = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authed);
    }
}
