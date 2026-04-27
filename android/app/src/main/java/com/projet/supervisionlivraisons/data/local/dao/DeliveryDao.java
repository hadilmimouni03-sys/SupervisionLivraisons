package com.projet.supervisionlivraisons.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.projet.supervisionlivraisons.data.local.entity.DeliveryEntity;

import java.util.List;

/** Room DAO for the driver's cached daily deliveries. */
@Dao
public interface DeliveryDao {

    @Query("SELECT * FROM deliveries ORDER BY ordre ASC")
    LiveData<List<DeliveryEntity>> observeAll();

    @Query("SELECT * FROM deliveries WHERE nocde = :nocde LIMIT 1")
    DeliveryEntity findByNocde(int nocde);

    @Query("SELECT * FROM deliveries WHERE pendingEtatliv IS NOT NULL")
    List<DeliveryEntity> findPending();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<DeliveryEntity> rows);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(DeliveryEntity row);

    @Query("DELETE FROM deliveries")
    void clear();

    @Query("UPDATE deliveries SET pendingEtatliv = :etat, pendingRemarque = :remarque WHERE nocde = :nocde")
    void markPending(int nocde, String etat, String remarque);

    @Query("UPDATE deliveries SET etatliv = :etat, pendingEtatliv = NULL, pendingRemarque = NULL WHERE nocde = :nocde")
    void confirmSynced(int nocde, String etat);
}
