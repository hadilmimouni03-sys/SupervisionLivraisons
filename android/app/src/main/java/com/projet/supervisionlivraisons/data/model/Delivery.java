package com.projet.supervisionlivraisons.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Delivery row as returned by the controller-side list endpoints.
 * Mirrors the fields exposed by {@code /api/deliveries} and {@code /today}.
 */
public class Delivery {
    public int    nocde;
    public String dateliv;
    public String etatliv;
    public String modepay;

    @SerializedName("livreur_id")  public int    livreurId;
    @SerializedName("livreur_nom") public String livreurNom;
    @SerializedName("client_nom")  public String clientNom;

    public String villeclt;
    public String telclt;
    public double montant;

    public Integer ordre; // populated only by /my-today
}
