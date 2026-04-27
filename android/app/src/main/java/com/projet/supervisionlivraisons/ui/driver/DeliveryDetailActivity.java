package com.projet.supervisionlivraisons.ui.driver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.projet.supervisionlivraisons.R;
import com.projet.supervisionlivraisons.data.model.DeliveryDetail;
import com.projet.supervisionlivraisons.databinding.ActivityDeliveryDetailBinding;
import com.projet.supervisionlivraisons.utils.Resource;
import com.projet.supervisionlivraisons.viewmodel.DeliveriesViewModel;

import java.util.Locale;

/**
 * Driver-side delivery detail screen.
 *
 * Surfaces every datum required by the spec: client contact, address +
 * Google Maps shortcut, item count, total amount, payment mode. The status
 * change action goes through the offline-first repository so it works even
 * when the API is unreachable.
 */
public class DeliveryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NOCDE = "nocde";
    private static final String[] STATES = { "EN_ATTENTE", "EN_COURS", "LIVREE", "NON_LIVREE" };

    private ActivityDeliveryDetailBinding binding;
    private DeliveriesViewModel           vm;
    private int                           nocde;
    private String                        clientPhone;
    private String                        mapsUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        nocde = getIntent().getIntExtra(EXTRA_NOCDE, -1);

        vm = new ViewModelProvider(this).get(DeliveriesViewModel.class);
        vm.detail.observe(this, res -> {
            binding.progress.setVisibility(res.state == Resource.State.LOADING ? View.VISIBLE : View.GONE);
            if (res.state == Resource.State.SUCCESS) bind(res.data);
            else if (res.state == Resource.State.ERROR) {
                Toast.makeText(this, res.error, Toast.LENGTH_LONG).show();
            }
        });
        vm.update.observe(this, res -> {
            if (res.state == Resource.State.SUCCESS) {
                Toast.makeText(this, R.string.status_updated, Toast.LENGTH_SHORT).show();
                vm.loadDetail(nocde);
            } else if (res.state == Resource.State.ERROR) {
                Toast.makeText(this, res.error, Toast.LENGTH_LONG).show();
            }
        });

        binding.btnCall.setOnClickListener(v -> {
            if (clientPhone != null) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clientPhone)));
            }
        });
        binding.btnMaps.setOnClickListener(v -> {
            if (mapsUrl != null) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)));
            }
        });
        binding.btnChangeStatus.setOnClickListener(v -> showStateDialog());

        vm.loadDetail(nocde);
    }

    private void bind(DeliveryDetail d) {
        binding.tvNocde.setText(String.format(Locale.getDefault(), "Commande #%d", d.nocde));
        binding.tvEtat.setText(d.etatliv);
        binding.tvClient.setText(String.format("%s %s", d.prenomclt, d.nomclt));
        binding.tvAdresse.setText(String.format(Locale.getDefault(),
                "%s\n%s %s", d.adrclt, d.codePostal != null ? d.codePostal : "", d.villeclt));
        binding.tvTel.setText(d.telclt);
        binding.tvMail.setText(d.adrmail != null ? d.adrmail : "-");
        binding.tvNbArticles.setText(String.valueOf(d.nbArticles));
        binding.tvMontant.setText(String.format(Locale.getDefault(), "%.2f", d.montant));
        binding.tvModePay.setText(d.modepay);
        binding.tvRemarque.setText(d.remarque != null ? d.remarque : "-");

        StringBuilder items = new StringBuilder();
        if (d.articles != null) {
            for (DeliveryDetail.Article a : d.articles) {
                items.append(String.format(Locale.getDefault(),
                        "%s  x%d  =  %.2f\n", a.designation, a.qtecde, a.sousTotal));
            }
        }
        binding.tvArticles.setText(items.toString().trim());

        clientPhone = d.telclt;
        mapsUrl     = d.mapsUrl;
    }

    /** Bottom-sheet style chooser for the four allowed states. */
    private void showStateDialog() {
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, STATES);
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_state)
                .setAdapter(a, (dialog, which) -> {
                    String state = STATES[which];
                    if ("NON_LIVREE".equals(state)) askRemarque(state);
                    else vm.updateStatus(nocde, state, null);
                })
                .show();
    }

    private void askRemarque(String state) {
        com.google.android.material.textfield.TextInputEditText input =
                new com.google.android.material.textfield.TextInputEditText(this);
        input.setHint(R.string.reason_hint);
        input.setMinLines(2);

        new AlertDialog.Builder(this)
                .setTitle(R.string.reason_required)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String r = String.valueOf(input.getText()).trim();
                    if (r.isEmpty()) {
                        Toast.makeText(this, R.string.reason_required, Toast.LENGTH_SHORT).show();
                    } else {
                        vm.updateStatus(nocde, state, r);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
