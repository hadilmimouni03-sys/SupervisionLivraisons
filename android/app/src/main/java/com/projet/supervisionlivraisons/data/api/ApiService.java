package com.projet.supervisionlivraisons.data.api;

import com.projet.supervisionlivraisons.data.model.DashboardRow;
import com.projet.supervisionlivraisons.data.model.Delivery;
import com.projet.supervisionlivraisons.data.model.DeliveryDetail;
import com.projet.supervisionlivraisons.data.model.LoginRequest;
import com.projet.supervisionlivraisons.data.model.LoginResponse;
import com.projet.supervisionlivraisons.data.model.Message;
import com.projet.supervisionlivraisons.data.model.NamedRef;
import com.projet.supervisionlivraisons.data.model.StatusUpdate;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Retrofit binding to the ExpressJS backend.
 * All endpoints (except /auth/login) require a Bearer token, automatically
 * attached by {@link AuthInterceptor}.
 */
public interface ApiService {

    // ---------- Auth ----------
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    // ---------- Deliveries (Controller) ----------
    @GET("api/deliveries")
    Call<List<Delivery>> listDeliveries(@QueryMap Map<String, String> filters);

    @GET("api/deliveries/today")
    Call<List<Delivery>> todayDeliveries(@QueryMap Map<String, String> filters);

    // ---------- Deliveries (Driver) ----------
    @GET("api/deliveries/my-today")
    Call<List<Delivery>> myToday();

    @GET("api/deliveries/{nocde}")
    Call<DeliveryDetail> getDetail(@Path("nocde") int nocde);

    @PATCH("api/deliveries/{nocde}/status")
    Call<Map<String, Object>> updateStatus(@Path("nocde") int nocde, @Body StatusUpdate body);

    // ---------- Dashboard ----------
    @GET("api/dashboard/by-driver")
    Call<List<DashboardRow>> byDriver(@QueryMap Map<String, String> period);

    @GET("api/dashboard/by-client")
    Call<List<DashboardRow>> byClient(@QueryMap Map<String, String> period);

    // ---------- Lookups ----------
    @GET("api/drivers")
    Call<List<NamedRef>> listDrivers();

    @GET("api/clients")
    Call<List<NamedRef>> listClients();

    // ---------- Messages ----------
    @POST("api/messages/info")
    Call<Message> sendInfo(@Body Map<String, Object> body);

    @POST("api/messages/urgence")
    Call<Message> sendUrgence(@Body Map<String, Object> body);

    @GET("api/messages")
    Call<List<Message>> inbox();
}
