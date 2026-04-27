package com.projet.supervisionlivraisons.data.model;

/** Lightweight {id, nom} pair used by lookup endpoints (drivers, clients). */
public class NamedRef {
    public int    id;
    public String nom;

    @Override public String toString() { return nom; }
}
