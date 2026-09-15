package com.nicolascarmona.paytrack.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nicolascarmona.paytrack.model.PayTrackItem
import kotlinx.coroutines.flow.Flow

// Definimos las acciones (CRUD) que podemos hacer. Al devolver un Flow,
// la lista se actualizará en tiempo real

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<PayTrackItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubscription(item: PayTrackItem)

    @Update
    fun updateSubscription(item: PayTrackItem)

    @Query("DELETE FROM subscriptions WHERE id = :itemId")
    fun deleteSubscriptionsById(itemId: String)

    // Consulta rápìda de un solo uso para la alarma
    @Query("SELECT * FROM subscriptions WHERE day = :todayDay")
    fun getSubscriptionsForDay(todayDay: Int): List<PayTrackItem>

}