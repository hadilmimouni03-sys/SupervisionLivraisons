package com.projet.supervisionlivraisons.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.projet.supervisionlivraisons.databinding.ActivityLoginBinding;
import com.projet.supervisionlivraisons.ui.controller.ControllerHomeActivity;
import com.projet.supervisionlivraisons.ui.driver.DriverHomeActivity;
import com.projet.supervisionlivraisons.utils.RealtimeClient;
import com.projet.supervisionlivraisons.utils.Resource;
import com.projet.supervisionlivraisons.utils.SessionManager;
import com.projet.supervisionlivraisons.viewmodel.AuthViewModel;

/**
 * Login screen. On successful authentication routes the user to the
 * Controller or Driver home depending on the {@code role} field.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel        vm;
    private SessionManager       session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        if (session.isLogged()) { routeFor(session.getRole()); return; }

        vm = new ViewModelProvider(this).get(AuthViewModel.class);
        vm.result.observe(this, res -> {
            binding.progress.setVisibility(res.state == Resource.State.LOADING ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.btnLogin.setEnabled(res.state != Resource.State.LOADING);
            if (res.state == Resource.State.SUCCESS) {
                RealtimeClient.connect(res.data.token);
                routeFor(res.data.user.role);
            } else if (res.state == Resource.State.ERROR) {
                binding.tvError.setText(res.error);
                binding.tvError.setVisibility(android.view.View.VISIBLE);
            }
        });

        binding.btnLogin.setOnClickListener(v -> {
            String l = String.valueOf(binding.etLogin.getText()).trim();
            String p = String.valueOf(binding.etPassword.getText());
            if (l.isEmpty() || p.isEmpty()) {
                binding.tvError.setText("Login et mot de passe requis");
                binding.tvError.setVisibility(android.view.View.VISIBLE);
                return;
            }
            binding.tvError.setVisibility(android.view.View.GONE);
            vm.login(l, p);
        });
    }

    private void routeFor(String role) {
        Class<?> target = "Controleur".equals(role)
                ? ControllerHomeActivity.class
                : DriverHomeActivity.class;
        startActivity(new Intent(this, target));
        finish();
    }
}
