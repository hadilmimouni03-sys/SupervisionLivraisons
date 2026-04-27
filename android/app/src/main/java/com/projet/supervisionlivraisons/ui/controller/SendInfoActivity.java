package com.projet.supervisionlivraisons.ui.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.projet.supervisionlivraisons.R;
import com.projet.supervisionlivraisons.data.model.NamedRef;
import com.projet.supervisionlivraisons.databinding.ActivitySendInfoBinding;
import com.projet.supervisionlivraisons.utils.Resource;
import com.projet.supervisionlivraisons.viewmodel.DeliveriesViewModel;
import com.projet.supervisionlivraisons.viewmodel.MessagesViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows the controller to broadcast info messages to all drivers, or target
 * a specific one selected from the {@link com.projet.supervisionlivraisons.viewmodel.DeliveriesViewModel#drivers}
 * lookup.
 */
public class SendInfoActivity extends AppCompatActivity {

    private ActivitySendInfoBinding binding;
    private MessagesViewModel       messagesVM;
    private DeliveriesViewModel     deliveriesVM;
    private List<NamedRef>          drivers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        messagesVM   = new ViewModelProvider(this).get(MessagesViewModel.class);
        deliveriesVM = new ViewModelProvider(this).get(DeliveriesViewModel.class);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        adapter.add(getString(R.string.broadcast_all));
        binding.spinnerDriver.setAdapter(adapter);

        deliveriesVM.drivers.observe(this, res -> {
            if (res.state == Resource.State.SUCCESS) {
                drivers = res.data;
                for (NamedRef d : drivers) adapter.add(d.nom);
            }
        });
        deliveriesVM.loadDrivers();

        messagesVM.sent.observe(this, res -> {
            binding.progress.setVisibility(res.state == Resource.State.LOADING ? View.VISIBLE : View.GONE);
            binding.btnSend.setEnabled(res.state != Resource.State.LOADING);
            if (res.state == Resource.State.SUCCESS) {
                Toast.makeText(this, R.string.message_sent, Toast.LENGTH_SHORT).show();
                finish();
            } else if (res.state == Resource.State.ERROR) {
                Toast.makeText(this, res.error, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSend.setOnClickListener(v -> {
            String content = String.valueOf(binding.etContent.getText()).trim();
            if (content.isEmpty()) {
                binding.tilContent.setError(getString(R.string.required));
                return;
            }
            int idx = binding.spinnerDriver.getSelectedItemPosition();
            Integer receiverId = (idx == 0) ? null : drivers.get(idx - 1).id;
            messagesVM.sendInfo(receiverId, content);
        });
    }
}
