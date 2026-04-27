package com.projet.supervisionlivraisons.data.model;

import com.google.gson.annotations.SerializedName;

/** A single row of the analytics dashboard. */
public class DashboardRow {
    @SerializedName("livreur_id")  public Integer livreurId;
    @SerializedName("livreur_nom") public String  livreurNom;

    public Integer noclt;
    @SerializedName("client_nom")  public String  clientNom;

    public String  etatliv;
    public int     nb;

    /** Convenience: returns whichever name field is populated. */
    public String label() {
        return livreurNom != null ? livreurNom : clientNom;
    }
}
