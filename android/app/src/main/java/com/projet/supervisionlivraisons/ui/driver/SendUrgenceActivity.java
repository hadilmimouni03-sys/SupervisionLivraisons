package com.projet.supervisionlivraisons.ui.driver;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.projet.supervisionlivraisons.R;
import com.projet.supervisionlivraisons.databinding.ActivitySendUrgenceBinding;
import com.projet.supervisionlivraisons.utils.Resource;
import com.projet.supervisionlivraisons.viewmodel.MessagesViewModel;

/** Driver-only screen used to escalate an issue (client absent, refused, ...). */
public class SendUrgenceActivity extends AppCompatActivity {

    private ActivitySendUrgenceBinding binding;
    private MessagesViewModel          vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendUrgenceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        vm = new ViewModelProvider(this).get(MessagesViewModel.class);
        vm.sent.observe(this, res -> {
            binding.progress.setVisibility(res.state == Resource.State.LOADING ? View.VISIBLE : View.GONE);
            binding.btnSend.setEnabled(res.state != Resource.State.LOADING);
            if (res.state == Resource.State.SUCCESS) {
                Toast.makeText(this, R.string.urgence_sent, Toast.LENGTH_SHORT).show();
                finish();
            } else if (res.state == Resource.State.ERROR) {
                Toast.makeText(this, res.error, Toast.LENGTH_LONG).show();
            }
        });

        binding.btnSend.setOnClickListener(v -> {
            String nocdeStr = String.valueOf(binding.etNocde.getText()).trim();
            String content  = String.valueOf(binding.etContent.getText()).trim();
            if (nocdeStr.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                vm.sendUrgence(Integer.parseInt(nocdeStr), content);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.invalid_nocde, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
