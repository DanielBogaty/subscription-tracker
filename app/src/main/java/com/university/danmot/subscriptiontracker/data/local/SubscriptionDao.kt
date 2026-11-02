package com.university.danmot.subscriptiontracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    // Изменили запрос: выбираем только подписки конкретного пользователя
    @Query("SELECT * FROM subscriptions WHERE ownerId = :ownerId ORDER BY nextPaymentDate ASC")
    fun getSubscriptionsForOwner(ownerId: String): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM subscriptions")
    suspend fun getAllSubscriptionsList(): List<SubscriptionEntity>
}