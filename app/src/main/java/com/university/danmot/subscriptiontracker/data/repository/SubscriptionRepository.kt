package com.university.danmot.subscriptiontracker.data.repository

import com.university.danmot.subscriptiontracker.data.local.PriceHistoryDao
import com.university.danmot.subscriptiontracker.data.local.PriceHistoryEntity
import com.university.danmot.subscriptiontracker.data.local.SubscriptionDao
import com.university.danmot.subscriptiontracker.data.local.toDomain
import com.university.danmot.subscriptiontracker.data.local.toEntity
import com.university.danmot.subscriptiontracker.domain.model.PriceHistory
import com.university.danmot.subscriptiontracker.domain.model.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class SubscriptionRepository(
    private val dao: SubscriptionDao,
    private val historyDao: PriceHistoryDao // <-- ДОБАВИЛИ
) {
    fun getAllSubscriptions(): Flow<List<Subscription>> =
        dao.getAllSubscriptions().map { it.map { e -> e.toDomain() } }

    fun getSubscriptionById(id: Long): Flow<Subscription?> =
        dao.getSubscriptionById(id).map { it?.toDomain() }

    // НОВОЕ: Получение истории цен
    fun getPriceHistory(subscriptionId: Long): Flow<List<PriceHistory>> =
        historyDao.getHistoryBySubscriptionId(subscriptionId).map { entities ->
            entities.map { e ->
                PriceHistory(
                    id = e.id,
                    subscriptionId = e.subscriptionId,
                    price = e.price,
                    date = LocalDate.parse(e.date)
                )
            }
        }

    suspend fun insert(subscription: Subscription): Long = dao.insert(subscription.toEntity())
    suspend fun update(subscription: Subscription) = dao.update(subscription.toEntity())
    suspend fun delete(subscription: Subscription) = dao.delete(subscription.toEntity())

    // НОВОЕ: Сохранение записи в историю
    suspend fun insertPriceHistory(subscriptionId: Long, price: Double, date: LocalDate) {
        historyDao.insert(
            PriceHistoryEntity(
                subscriptionId = subscriptionId,
                price = price,
                date = date.toString()
            )
        )
    }
}