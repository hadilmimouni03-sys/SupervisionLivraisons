package com.projet.supervisionlivraisons.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.projet.supervisionlivraisons.data.model.Message;
import com.projet.supervisionlivraisons.data.repository.MessageRepository;
import com.projet.supervisionlivraisons.utils.Resource;

import java.util.List;

/** Drives the inbox screen and the two send-message flows. */
public class MessagesViewModel extends AndroidViewModel {

    private final MessageRepository repo;

    public final MutableLiveData<Resource<Message>>       sent   = new MutableLiveData<>();
    public final MutableLiveData<Resource<List<Message>>> inbox  = new MutableLiveData<>();

    public MessagesViewModel(@NonNull Application app) {
        super(app);
        this.repo = new MessageRepository(app);
    }

    public void sendInfo(Integer receiverId, String content) { repo.sendInfo(receiverId, content, sent); }
    public void sendUrgence(int nocde, String content)       { repo.sendUrgence(nocde, content, sent); }
    public void loadInbox()                                  { repo.inbox(inbox); }
}
