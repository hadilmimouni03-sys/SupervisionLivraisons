package com.projet.supervisionlivraisons.data.model;

/** Response from {@code POST /api/auth/login}. */
public class LoginResponse {
    public String token;
    public User   user;

    public static class User {
        public int    id;
        public String nom;
        public String prenom;
        public String login;
        public String role; // "Controleur" | "Livreur"
    }
}
