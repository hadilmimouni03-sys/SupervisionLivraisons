package com.projet.supervisionlivraisons.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.projet.supervisionlivraisons.data.local.entity.DeliveryEntity;
import com.projet.supervisionlivraisons.data.model.DashboardRow;
import com.projet.supervisionlivraisons.data.model.Delivery;
import com.projet.supervisionlivraisons.data.model.DeliveryDetail;
import com.projet.supervisionlivraisons.data.model.NamedRef;
import com.projet.supervisionlivraisons.data.repository.DeliveryRepository;
import com.projet.supervisionlivraisons.utils.Resource;

import java.util.List;
import java.util.Map;

/** Single ViewModel covering both controller and driver screens. */
public class DeliveriesViewModel extends AndroidViewModel {

    private final DeliveryRepository repo;

    public final MutableLiveData<Resource<List<Delivery>>>      list      = new MutableLiveData<>();
    public final MutableLiveData<Resource<DeliveryDetail>>      detail    = new MutableLiveData<>();
    public final MutableLiveData<Resource<Boolean>>             refresh   = new MutableLiveData<>();
    public final MutableLiveData<Resource<Boolean>>             update    = new MutableLiveData<>();
    public final MutableLiveData<Resource<List<DashboardRow>>>  dashboard = new MutableLiveData<>();
    public final MutableLiveData<Resource<List<NamedRef>>>      drivers   = new MutableLiveData<>();
    public final MutableLiveData<Resource<List<NamedRef>>>      clients   = new MutableLiveData<>();

    public DeliveriesViewModel(@NonNull Application app) {
        super(app);
        this.repo = new DeliveryRepository(app);
    }

    /* Controller */
    public void loadList(Map<String, String> filters)  { repo.listDeliveries(filters, list); }
    public void loadToday(Map<String, String> filters) { repo.todayDeliveries(filters, list); }
    public void loadByDriver(Map<String, String> p)    { repo.byDriver(p, dashboard); }
    public void loadByClient(Map<String, String> p)    { repo.byClient(p, dashboard); }
    public void loadDrivers()                          { repo.listDrivers(drivers); }
    public void loadClients()                          { repo.listClients(clients); }

    /* Driver (offline-first) */
    public LiveData<List<DeliveryEntity>> myDeliveries() { return repo.observeMyDeliveries(); }
    public void refreshMyDeliveries()                    { repo.refreshMyDeliveries(refresh); }
    public void loadDetail(int nocde)                    { repo.getDetail(nocde, detail); }
    public void updateStatus(int nocde, String etat, String remarque) {
        repo.queueStatusUpdate(nocde, etat, remarque, update);
    }
    public void syncPending() { repo.syncPending(); }
}
