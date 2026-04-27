package com.projet.supervisionlivraisons.ui.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.projet.supervisionlivraisons.R;
import com.projet.supervisionlivraisons.databinding.ActivityDriverHomeBinding;
import com.projet.supervisionlivraisons.ui.auth.LoginActivity;
import com.projet.supervisionlivraisons.ui.common.MessagesActivity;
import com.projet.supervisionlivraisons.utils.RealtimeClient;
import com.projet.supervisionlivraisons.utils.Resource;
import com.projet.supervisionlivraisons.utils.SessionManager;
import com.projet.supervisionlivraisons.viewmodel.AuthViewModel;
import com.projet.supervisionlivraisons.viewmodel.DeliveriesViewModel;

/**
 * Driver home: today's deliveries list, offline-first via Room.
 *
 * On launch we trigger {@link DeliveriesViewModel#refreshMyDeliveries()} to
 * pull the current data once, then everything is observed from Room — so the
 * list still works without connectivity.
 */
public class DriverHomeActivity extends AppCompatActivity {

    private ActivityDriverHomeBinding binding;
    private DeliveriesViewModel       vm;
    private DriverDeliveryAdapter     adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager session = new SessionManager(this);
        binding.tvHello.setText(getString(R.string.driver_today_title,
                session.getPrenom() + " " + session.getNom()));

        adapter = new DriverDeliveryAdapter(row -> {
            Intent i = new Intent(this, DeliveryDetailActivity.class);
            i.putExtra(DeliveryDetailActivity.EXTRA_NOCDE, row.nocde);
            startActivity(i);
        });
        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(DeliveriesViewModel.class);
        vm.myDeliveries().observe(this, list -> {
            adapter.submit(list);
            binding.tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
        vm.refresh.observe(this, res -> {
            binding.swipe.setRefreshing(res.state == Resource.State.LOADING);
            if (res.state == Resource.State.ERROR) {
                Toast.makeText(this, getString(R.string.refresh_error, res.error), Toast.LENGTH_SHORT).show();
            }
        });

        binding.swipe.setOnRefreshListener(this::refresh);

        binding.btnSendUrgence.setOnClickListener(v -> startActivity(new Intent(this, SendUrgenceActivity.class)));
        binding.btnMessages.setOnClickListener(v -> startActivity(new Intent(this, MessagesActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            new ViewModelProvider(this).get(AuthViewModel.class).logout();
            RealtimeClient.disconnect();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });

        refresh();
    }

    private void refresh() {
        vm.refreshMyDeliveries();
        vm.syncPending();
    }
}
