package com.projet.supervisionlivraisons.ui.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.projet.supervisionlivraisons.databinding.ActivityControllerHomeBinding;
import com.projet.supervisionlivraisons.ui.auth.LoginActivity;
import com.projet.supervisionlivraisons.ui.common.MessagesActivity;
import com.projet.supervisionlivraisons.utils.RealtimeClient;
import com.projet.supervisionlivraisons.utils.SessionManager;
import com.projet.supervisionlivraisons.viewmodel.AuthViewModel;

/** Hub for the Controller user. */
public class ControllerHomeActivity extends AppCompatActivity {

    private ActivityControllerHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityControllerHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager session = new SessionManager(this);
        binding.tvHello.setText(getString(com.projet.supervisionlivraisons.R.string.greeting,
                session.getPrenom() + " " + session.getNom()));

        binding.cardToday.setOnClickListener(v -> {
            Intent i = new Intent(this, DeliveriesListActivity.class);
            i.putExtra(DeliveriesListActivity.EXTRA_TODAY_ONLY, true);
            startActivity(i);
        });
        binding.cardAll.setOnClickListener(v -> startActivity(new Intent(this, DeliveriesListActivity.class)));
        binding.cardDashboard.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        binding.cardSendInfo.setOnClickListener(v -> startActivity(new Intent(this, SendInfoActivity.class)));
        binding.cardMessages.setOnClickListener(v -> startActivity(new Intent(this, MessagesActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            new ViewModelProvider(this).get(AuthViewModel.class).logout();
            RealtimeClient.disconnect();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });
    }
}
