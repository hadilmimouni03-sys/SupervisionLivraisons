package com.projet.supervisionlivraisons.ui.common;

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

import com.projet.supervisionlivraisons.R;
import com.projet.supervisionlivraisons.data.model.Message;
import com.projet.supervisionlivraisons.databinding.ActivityMessagesBinding;
import com.projet.supervisionlivraisons.utils.RealtimeClient;
import com.projet.supervisionlivraisons.utils.Resource;
import com.projet.supervisionlivraisons.viewmodel.MessagesViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Inbox shared by Controllers and Drivers.
 *
 * Subscribes to the {@code message:new} Socket.IO event so a freshly-arrived
 * message appears at the top of the list without a manual refresh.
 */
public class MessagesActivity extends AppCompatActivity {

    private ActivityMessagesBinding binding;
    private MessagesViewModel       vm;
    private final Adapter           adapter = new Adapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(MessagesViewModel.class);
        vm.inbox.observe(this, res -> {
            binding.swipe.setRefreshing(res.state == Resource.State.LOADING);
            if (res.state == Resource.State.SUCCESS) {
                adapter.submit(res.data);
                binding.tvEmpty.setVisibility(res.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        binding.swipe.setOnRefreshListener(() -> vm.loadInbox());

        // Real-time refresh on every incoming message.
        RealtimeClient rt = RealtimeClient.connect(
                new com.projet.supervisionlivraisons.utils.SessionManager(this).getToken());
        if (rt != null) rt.on("message:new", args -> runOnUiThread(() -> vm.loadInbox()));

        vm.loadInbox();
    }

    /* -------- inline adapter -------- */
    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        final List<Message> rows = new ArrayList<>();
        void submit(List<Message> data) { rows.clear(); if (data != null) rows.addAll(data); notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Message m = rows.get(position);
            h.tvType.setText(m.type);
            boolean urgence = "URGENCE".equals(m.type);
            h.tvType.setTextColor(urgence ? 0xFFB00020 : 0xFF1565C0);
            h.tvSender.setText(m.senderNom != null ? m.senderNom : ("#" + m.senderId));
            h.tvDate.setText(m.createdAt != null ? m.createdAt.replace("T", " ").substring(0, Math.min(16, m.createdAt.length())) : "");
            h.tvContent.setText(m.content);
        }
        @Override public int getItemCount() { return rows.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvType, tvSender, tvDate, tvContent;
            VH(View v) {
                super(v);
                tvType    = v.findViewById(R.id.tvType);
                tvSender  = v.findViewById(R.id.tvSender);
                tvDate    = v.findViewById(R.id.tvDate);
                tvContent = v.findViewById(R.id.tvContent);
            }
        }
    }
}
