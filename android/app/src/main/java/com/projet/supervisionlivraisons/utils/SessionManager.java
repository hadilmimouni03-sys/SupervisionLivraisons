package com.projet.supervisionlivraisons.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persists the JWT and the authenticated user's profile in SharedPreferences.
 * Centralised so that {@link com.projet.supervisionlivraisons.data.api.AuthInterceptor}
 * and the UI layer can both read/write the session.
 */
public class SessionManager {

    private static final String PREFS = "supervision_session";
    private static final String K_TOKEN  = "token";
    private static final String K_ID     = "user_id";
    private static final String K_ROLE   = "role";
    private static final String K_NOM    = "nom";
    private static final String K_PRENOM = "prenom";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        this.prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(String token, int id, String role, String nom, String prenom) {
        prefs.edit()
                .putString(K_TOKEN, token)
                .putInt(K_ID, id)
                .putString(K_ROLE, role)
                .putString(K_NOM, nom)
                .putString(K_PRENOM, prenom)
                .apply();
    }

    public void clear() { prefs.edit().clear().apply(); }

    public String  getToken()  { return prefs.getString(K_TOKEN, null); }
    public int     getUserId() { return prefs.getInt(K_ID, -1); }
    public String  getRole()   { return prefs.getString(K_ROLE, null); }
    public String  getNom()    { return prefs.getString(K_NOM, ""); }
    public String  getPrenom() { return prefs.getString(K_PRENOM, ""); }

    public boolean isLogged()      { return getToken() != null; }
    public boolean isController()  { return "Controleur".equals(getRole()); }
    public boolean isDriver()      { return "Livreur".equals(getRole()); }
}
