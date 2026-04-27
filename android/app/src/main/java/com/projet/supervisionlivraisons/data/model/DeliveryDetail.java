package com.projet.supervisionlivraisons.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Full delivery details returned by {@code GET /api/deliveries/:nocde}. */
public class DeliveryDetail {
    public int    nocde;
    public String dateliv;
    public String etatliv;
    public String modepay;
    public String remarque;

    public int    noclt;
    public String nomclt;
    public String prenomclt;
    public String adrclt;
    public String villeclt;

    @SerializedName("code_postal") public String codePostal;
    public String telclt;
    public String adrmail;

    public List<Article> articles;

    @SerializedName("nb_articles") public int    nbArticles;
    public double montant;

    @SerializedName("maps_url")    public String mapsUrl;

    public static class Article {
        public int    refart;
        public String designation;
        public int    qtecde;
        public double prixV;

        @SerializedName("sous_total") public double sousTotal;
    }
}
