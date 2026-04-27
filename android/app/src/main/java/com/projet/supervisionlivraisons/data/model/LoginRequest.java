package com.projet.supervisionlivraisons.data.model;

/** Body for {@code POST /api/auth/login}. */
public class LoginRequest {
    public String login;
    public String password;

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
