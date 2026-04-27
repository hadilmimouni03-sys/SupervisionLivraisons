package com.projet.supervisionlivraisons.ui.controller;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.projet.supervisionlivraisons.databinding.ActivityDeliveriesListBinding;
import com.projet.supervisionlivraisons.utils.Resource;
import com.projet.supervisionlivraisons.viewmodel.DeliveriesViewModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Lists deliveries for the controller. Two modes:
 *   - {@code EXTRA_TODAY_ONLY = true}  -> /api/deliveries/today
 *   - default                          -> /api/deliveries with date / state filters
 */
public class DeliveriesListActivity extends AppCompatActivity {

    public static final String EXTRA_TODAY_ONLY = "today_only";

    private ActivityDeliveriesListBinding binding;
    private DeliveriesViewModel           vm;
    private DeliveryAdapter               adapter;
    private boolean                       todayOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveriesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        todayOnly = getIntent().getBooleanExtra(EXTRA_TODAY_ONLY, false);
        binding.toolbar.setTitle(todayOnly
                ? com.projet.supervisionlivraisons.R.string.menu_today
                : com.projet.supervisionlivraisons.R.string.menu_all);

        adapter = new DeliveryAdapter();
        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(DeliveriesViewModel.class);
        vm.list.observe(this, res -> {
            binding.swipe.setRefreshing(res.state == Resource.State.LOADING);
            if (res.state == Resource.State.SUCCESS) {
                adapter.submit(res.data);
                binding.tvEmpty.setVisibility(res.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        binding.swipe.setOnRefreshListener(this::reload);

        binding.etFilter.setOnEditorActionListener((v, actionId, event) -> { reload(); return true; });
        binding.btnApply.setOnClickListener(v -> reload());

        reload();
    }

    private void reload() {
        Map<String, String> filters = new HashMap<>();
        String etat   = String.valueOf(binding.etEtat.getText()).trim();
        String livreur= String.valueOf(binding.etLivreur.getText()).trim();
        String dateF  = String.valueOf(binding.etDateFrom.getText()).trim();
        String dateT  = String.valueOf(binding.etDateTo.getText()).trim();
        String nocde  = String.valueOf(binding.etFilter.getText()).trim();

        if (!etat.isEmpty())    filters.put("etat",     etat);
        if (!livreur.isEmpty()) filters.put("livreur",  livreur);
        if (!nocde.isEmpty())   filters.put("nocde",    nocde);
        if (todayOnly) {
            vm.loadToday(filters);
        } else {
            if (!dateF.isEmpty()) filters.put("dateFrom", dateF);
            if (!dateT.isEmpty()) filters.put("dateTo",   dateT);
            vm.loadList(filters);
        }
    }
}
