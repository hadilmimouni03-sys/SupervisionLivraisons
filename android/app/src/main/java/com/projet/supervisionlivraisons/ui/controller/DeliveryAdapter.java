package com.projet.supervisionlivraisons.ui.controller;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projet.supervisionlivraisons.data.model.Delivery;
import com.projet.supervisionlivraisons.databinding.ItemDeliveryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** RecyclerView adapter rendering controller-side delivery rows. */
public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.VH> {

    private final List<Delivery> rows = new ArrayList<>();

    public void submit(List<Delivery> data) {
        rows.clear();
        if (data != null) rows.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeliveryBinding b = ItemDeliveryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Delivery d = rows.get(position);
        h.b.tvNocde.setText(String.format(Locale.getDefault(), "#%d", d.nocde));
        h.b.tvClient.setText(d.clientNom);
        h.b.tvLivreur.setText(d.livreurNom);
        h.b.tvDate.setText(d.dateliv);
        h.b.tvEtat.setText(d.etatliv);
        h.b.tvMontant.setText(String.format(Locale.getDefault(), "%.2f", d.montant));
    }

    @Override public int getItemCount() { return rows.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemDeliveryBinding b;
        VH(ItemDeliveryBinding b) { super(b.getRoot()); this.b = b; }
    }
}
