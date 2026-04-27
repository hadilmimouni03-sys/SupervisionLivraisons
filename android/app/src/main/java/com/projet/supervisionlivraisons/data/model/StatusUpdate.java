package com.projet.supervisionlivraisons.data.model;

/** Body for {@code PATCH /api/deliveries/:nocde/status}. */
public class StatusUpdate {
    public String etatliv;
    public String remarque;

    public StatusUpdate(String etatliv, String remarque) {
        this.etatliv = etatliv;
        this.remarque = remarque;
    }
}
