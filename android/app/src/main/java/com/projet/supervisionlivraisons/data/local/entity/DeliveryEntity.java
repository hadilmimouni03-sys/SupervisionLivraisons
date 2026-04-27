package com.projet.supervisionlivraisons.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Cached delivery row used by the driver's offline-first daily list.
 * Holds the same shape as {@code Delivery}, plus pending-sync metadata.
 */
@Entity(tableName = "deliveries")
public class DeliveryEntity {

    @PrimaryKey
    public int    nocde;
    public int    ordre;
    public String dateliv;
    public String etatliv;
    public String modepay;
    public String clientNom;
    public String telclt;
    public String villeclt;

    /** When non-null, the local row holds an unsynced change to push to the API. */
    @Nullable public String pendingEtatliv;
    @Nullable public String pendingRemarque;

    public DeliveryEntity() {}

    @NonNull
    public String displayState() {
        return pendingEtatliv != null ? pendingEtatliv : etatliv;
    }
}
