package com.university.danmot.subscriptiontracker.feature.subscriptions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.university.danmot.subscriptiontracker.data.local.DatabaseProvider
import com.university.danmot.subscriptiontracker.data.local.PriceHistoryEntity
import com.university.danmot.subscriptiontracker.data.local.toDomain
import com.university.danmot.subscriptiontracker.data.local.toEntity
import com.university.danmot.subscriptiontracker.data.price.PriceSource
import com.university.danmot.subscriptiontracker.data.repository.SubscriptionRepository
import com.university.danmot.subscriptiontracker.domain.model.BillingCycle
import com.university.danmot.subscriptiontracker.domain.model.PriceHistory
import com.university.danmot.subscriptiontracker.domain.model.Subscription
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = SubscriptionRepository(db.subscriptionDao(), db.priceHistoryDao())

    val subscriptions: StateFlow<List<Subscription>> = repository.getAllSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSubscriptionById(id: Long): StateFlow<Subscription?> =
        repository.getSubscriptionById(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getPriceHistory(subscriptionId: Long): StateFlow<List<PriceHistory>> =
        repository.getPriceHistory(subscriptionId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubscription(name: String, price: Double, cycle: BillingCycle, date: String) {
        viewModelScope.launch {
            repository.insert(Subscription(0, name, price, null, "RUB", cycle, LocalDate.parse(date), UUID.randomUUID().toString()))
        }
    }

    fun updateSubscription(subscription: Subscription) {
        viewModelScope.launch { repository.update(subscription) }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch { repository.delete(subscription) }
    }

    // НОВАЯ ФУНКЦИЯ: Прямой запуск проверки цен в обход WorkManager для тестирования
    fun forcePriceChangeTest() {
        viewModelScope.launch {
            val dao = db.subscriptionDao()
            val historyDao = db.priceHistoryDao()
            val entities = dao.getAllSubscriptions().first()

            entities.forEach { entity ->
                val sub = entity.toDomain()
                val newPrice = PriceSource.checkPriceChange(sub)

                if (newPrice != null) {
                    // 1. Сохраняем старую цену в историю
                    historyDao.insert(
                        PriceHistoryEntity(
                            subscriptionId = sub.id,
                            price = sub.price,
                            date = LocalDate.now().toString()
                        )
                    )
                    // 2. Обновляем новую цену в основной таблице
                    dao.update(sub.copy(previousPrice = sub.price, price = newPrice).toEntity())
                }
            }
        }
    }
}