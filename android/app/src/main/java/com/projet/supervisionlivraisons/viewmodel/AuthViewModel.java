package com.projet.supervisionlivraisons.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.projet.supervisionlivraisons.data.model.LoginResponse;
import com.projet.supervisionlivraisons.data.repository.AuthRepository;
import com.projet.supervisionlivraisons.utils.Resource;

/** Drives the login screen. */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repo;
    public  final MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application app) {
        super(app);
        this.repo = new AuthRepository(app);
    }

    public void login(String login, String password) {
        repo.login(login, password, result);
    }

    public void logout() { repo.logout(); }
}
