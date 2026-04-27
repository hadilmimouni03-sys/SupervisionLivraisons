package com.projet.supervisionlivraisons.data.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.projet.supervisionlivraisons.data.api.ApiClient;
import com.projet.supervisionlivraisons.data.api.ApiService;
import com.projet.supervisionlivraisons.data.model.Message;
import com.projet.supervisionlivraisons.utils.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Sends/receives messages between Controllers and Drivers. */
public class MessageRepository {

    private final ApiService api;

    public MessageRepository(Context ctx) { this.api = ApiClient.get(ctx); }

    public void sendInfo(Integer receiverId, String content,
                         MutableLiveData<Resource<Message>> out) {
        out.setValue(Resource.loading());
        Map<String, Object> body = new HashMap<>();
        if (receiverId != null) body.put("receiver_id", receiverId);
        body.put("content", content);
        api.sendInfo(body).enqueue(forward(out));
    }

    public void sendUrgence(int nocde, String content,
                            MutableLiveData<Resource<Message>> out) {
        out.setValue(Resource.loading());
        Map<String, Object> body = new HashMap<>();
        body.put("nocde", nocde);
        body.put("content", content);
        api.sendUrgence(body).enqueue(forward(out));
    }

    public void inbox(MutableLiveData<Resource<List<Message>>> out) {
        out.setValue(Resource.loading());
        api.inbox().enqueue(new Callback<List<Message>>() {
            @Override public void onResponse(Call<List<Message>> c, Response<List<Message>> r) {
                if (r.isSuccessful() && r.body() != null) out.postValue(Resource.success(r.body()));
                else out.postValue(Resource.error("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<Message>> c, Throwable t) {
                out.postValue(Resource.error(t.getMessage()));
            }
        });
    }

    private static Callback<Message> forward(MutableLiveData<Resource<Message>> out) {
        return new Callback<Message>() {
            @Override public void onResponse(Call<Message> c, Response<Message> r) {
                if (r.isSuccessful() && r.body() != null) out.postValue(Resource.success(r.body()));
                else out.postValue(Resource.error("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<Message> c, Throwable t) {
                out.postValue(Resource.error(t.getMessage()));
            }
        };
    }
}
