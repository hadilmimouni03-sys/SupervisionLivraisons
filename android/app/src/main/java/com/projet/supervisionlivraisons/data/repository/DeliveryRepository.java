package com.projet.supervisionlivraisons.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.projet.supervisionlivraisons.data.api.ApiClient;
import com.projet.supervisionlivraisons.data.api.ApiService;
import com.projet.supervisionlivraisons.data.local.AppDatabase;
import com.projet.supervisionlivraisons.data.local.dao.DeliveryDao;
import com.projet.supervisionlivraisons.data.local.entity.DeliveryEntity;
import com.projet.supervisionlivraisons.data.model.DashboardRow;
import com.projet.supervisionlivraisons.data.model.Delivery;
import com.projet.supervisionlivraisons.data.model.DeliveryDetail;
import com.projet.supervisionlivraisons.data.model.NamedRef;
import com.projet.supervisionlivraisons.data.model.StatusUpdate;
import com.projet.supervisionlivraisons.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository orchestrating the Retrofit API and the Room cache.
 *
 * Drivers consume the offline-first APIs ({@link #observeMyDeliveries()},
 * {@link #queueStatusUpdate}); controllers go straight to the network.
 */
public class DeliveryRepository {

    private final ApiService     api;
    private final DeliveryDao    dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public DeliveryRepository(Context ctx) {
        this.api = ApiClient.get(ctx);
        this.dao = AppDatabase.get(ctx).deliveryDao();
    }

    /* ------------------------------------------------------------------ */
    /*  CONTROLLER FLOWS (network-only)                                   */
    /* ------------------------------------------------------------------ */

    public void listDeliveries(Map<String, String> filters, MutableLiveData<Resource<List<Delivery>>> out) {
        out.setValue(Resource.loading());
        api.listDeliveries(filters).enqueue(forwardList(out));
    }

    public void todayDeliveries(Map<String, String> filters, MutableLiveData<Resource<List<Delivery>>> out) {
        out.setValue(Resource.loading());
        api.todayDeliveries(filters).enqueue(forwardList(out));
    }

    public void byDriver(Map<String, String> period, MutableLiveData<Resource<List<DashboardRow>>> out) {
        out.setValue(Resource.loading());
        api.byDriver(period).enqueue(forwardList(out));
    }

    public void byClient(Map<String, String> period, MutableLiveData<Resource<List<DashboardRow>>> out) {
        out.setValue(Resource.loading());
        api.byClient(period).enqueue(forwardList(out));
    }

    public void listDrivers(MutableLiveData<Resource<List<NamedRef>>> out) {
        out.setValue(Resource.loading());
        api.listDrivers().enqueue(forwardList(out));
    }

    public void listClients(MutableLiveData<Resource<List<NamedRef>>> out) {
        out.setValue(Resource.loading());
        api.listClients().enqueue(forwardList(out));
    }

    /* ------------------------------------------------------------------ */
    /*  DRIVER FLOWS (offline-first via Room)                             */
    /* ------------------------------------------------------------------ */

    /** LiveData backed by Room — survives offline use. */
    public LiveData<List<DeliveryEntity>> observeMyDeliveries() {
        return dao.observeAll();
    }

    /** Pulls today's deliveries from the API and replaces the local cache. */
    public void refreshMyDeliveries(MutableLiveData<Resource<Boolean>> out) {
        out.setValue(Resource.loading());
        api.myToday().enqueue(new Callback<List<Delivery>>() {
            @Override public void onResponse(Call<List<Delivery>> c, Response<List<Delivery>> r) {
                if (!r.isSuccessful() || r.body() == null) {
                    out.postValue(Resource.error("HTTP " + r.code()));
                    return;
                }
                List<DeliveryEntity> rows = new ArrayList<>();
                for (Delivery d : r.body()) {
                    DeliveryEntity e = new DeliveryEntity();
                    e.nocde = d.nocde;
                    e.ordre = d.ordre != null ? d.ordre : d.nocde;
                    e.dateliv = d.dateliv;
                    e.etatliv = d.etatliv;
                    e.modepay = d.modepay;
                    e.clientNom = d.clientNom;
                    e.telclt = d.telclt;
                    e.villeclt = d.villeclt;
                    rows.add(e);
                }
                io.execute(() -> {
                    dao.clear();
                    dao.upsertAll(rows);
                    out.postValue(Resource.success(true));
                });
            }
            @Override public void onFailure(Call<List<Delivery>> c, Throwable t) {
                out.postValue(Resource.error(t.getMessage()));
            }
        });
    }

    public void getDetail(int nocde, MutableLiveData<Resource<DeliveryDetail>> out) {
        out.setValue(Resource.loading());
        api.getDetail(nocde).enqueue(new Callback<DeliveryDetail>() {
            @Override public void onResponse(Call<DeliveryDetail> c, Response<DeliveryDetail> r) {
                if (r.isSuccessful() && r.body() != null) out.postValue(Resource.success(r.body()));
                else out.postValue(Resource.error("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<DeliveryDetail> c, Throwable t) {
                out.postValue(Resource.error(t.getMessage()));
            }
        });
    }

    /**
     * Persists the new state locally (instant UI feedback) and attempts to
     * push it to the API. If the call fails the row stays "pending" so the
     * next {@link #syncPending()} call can retry.
     */
    public void queueStatusUpdate(int nocde, String etatliv, String remarque,
                                  MutableLiveData<Resource<Boolean>> out) {
        out.setValue(Resource.loading());
        io.execute(() -> {
            dao.markPending(nocde, etatliv, remarque);
            api.updateStatus(nocde, new StatusUpdate(etatliv, remarque))
               .enqueue(new Callback<Map<String, Object>>() {
                @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                    if (r.isSuccessful()) {
                        io.execute(() -> dao.confirmSynced(nocde, etatliv));
                        out.postValue(Resource.success(true));
                    } else {
                        out.postValue(Resource.error("HTTP " + r.code() + " - retry pending"));
                    }
                }
                @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {
                    out.postValue(Resource.error("Offline - change saved locally"));
                }
            });
        });
    }

    /** Replays every pending local change against the API. Best-effort. */
    public void syncPending() {
        io.execute(() -> {
            for (DeliveryEntity row : dao.findPending()) {
                final int n = row.nocde;
                final String e = row.pendingEtatliv;
                api.updateStatus(n, new StatusUpdate(e, row.pendingRemarque))
                   .enqueue(new Callback<Map<String, Object>>() {
                    @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {
                        if (r.isSuccessful()) io.execute(() -> dao.confirmSynced(n, e));
                    }
                    @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) { /* try later */ }
                });
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /*  Helpers                                                           */
    /* ------------------------------------------------------------------ */

    private static <T> Callback<List<T>> forwardList(MutableLiveData<Resource<List<T>>> out) {
        return new Callback<List<T>>() {
            @Override public void onResponse(Call<List<T>> c, Response<List<T>> r) {
                if (r.isSuccessful() && r.body() != null) out.postValue(Resource.success(r.body()));
                else out.postValue(Resource.error("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<T>> c, Throwable t) {
                out.postValue(Resource.error(t.getMessage()));
            }
        };
    }
}
