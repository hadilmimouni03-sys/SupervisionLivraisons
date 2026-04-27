package com.projet.supervisionlivraisons.ui.driver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projet.supervisionlivraisons.data.local.entity.DeliveryEntity;
import com.projet.supervisionlivraisons.databinding.ItemDriverDeliveryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for the driver's daily list. Uses larger fonts and "fat" tap targets
 * so the row is easy to read and tap while in motion under the sun.
 */
public class DriverDeliveryAdapter extends RecyclerView.Adapter<DriverDeliveryAdapter.VH> {

    public interface OnRowClick { void open(DeliveryEntity row); }

    private final List<DeliveryEntity> rows = new ArrayList<>();
    private final OnRowClick onClick;

    public DriverDeliveryAdapter(OnRowClick onClick) { this.onClick = onClick; }

    public void submit(List<DeliveryEntity> data) {
        rows.clear();
        if (data != null) rows.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDriverDeliveryBinding b = ItemDriverDeliveryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DeliveryEntity r = rows.get(position);
        h.b.tvOrdre.setText(String.format(Locale.getDefault(), "%d", r.ordre));
        h.b.tvNocde.setText(String.format(Locale.getDefault(), "Cmd #%d", r.nocde));
        h.b.tvClient.setText(r.clientNom);
        h.b.tvVille.setText(r.villeclt);
        h.b.tvTel.setText(r.telclt);
        h.b.tvEtat.setText(r.displayState());
        h.b.tvSync.setVisibility(r.pendingEtatliv != null ? View.VISIBLE : View.GONE);
        h.itemView.setOnClickListener(v -> onClick.open(r));
    }

    @Override public int getItemCount() { return rows.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemDriverDeliveryBinding b;
        VH(ItemDriverDeliveryBinding b) { super(b.getRoot()); this.b = b; }
    }
}
