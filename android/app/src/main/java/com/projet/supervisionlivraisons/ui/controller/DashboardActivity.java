package com.projet.supervisionlivraisons.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.projet.supervisionlivraisons.R;
import com.projet.supervisionlivraisons.data.model.DashboardRow;
import com.projet.supervisionlivraisons.databinding.ActivityDashboardBinding;
import com.projet.supervisionlivraisons.utils.Resource;
import com.projet.supervisionlivraisons.viewmodel.DeliveriesViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/** Aggregated stats: deliveries by driver/state and by client/state. */
public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private DeliveriesViewModel      vm;
    private final RowsAdapter        adapter = new RowsAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(DeliveriesViewModel.class);
        vm.dashboard.observe(this, res -> {
            binding.progress.setVisibility(res.state == Resource.State.LOADING ? View.VISIBLE : View.GONE);
            if (res.state == Resource.State.SUCCESS) adapter.submit(res.data);
        });

        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.tab_by_driver));
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.tab_by_client));
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { reload(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        reload(0);
    }

    private void reload(int tab) {
        if (tab == 0) vm.loadByDriver(new HashMap<>());
        else          vm.loadByClient(new HashMap<>());
    }

    /* -------- inline adapter -------- */
    static class RowsAdapter extends RecyclerView.Adapter<RowsAdapter.VH> {
        private final List<DashboardRow> rows = new ArrayList<>();
        void submit(List<DashboardRow> data) { rows.clear(); if (data != null) rows.addAll(data); notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            v.setPadding(32, 24, 32, 24);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            DashboardRow r = rows.get(position);
            h.t1.setText(r.label());
            h.t1.setTextSize(18);
            h.t2.setText(String.format(Locale.getDefault(), "%s : %d", r.etatliv, r.nb));
            h.t2.setTextSize(15);
        }
        @Override public int getItemCount() { return rows.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView t1, t2;
            VH(View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
