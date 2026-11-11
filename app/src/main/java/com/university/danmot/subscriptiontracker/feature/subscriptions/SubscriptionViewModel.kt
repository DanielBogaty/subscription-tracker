package com.university.danmot.subscriptiontracker.feature.subscriptions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.university.danmot.subscriptiontracker.data.local.DatabaseProvider
import com.university.danmot.subscriptiontracker.data.repository.AuthRepository
import com.university.danmot.subscriptiontracker.data.repository.Family
import com.university.danmot.subscriptiontracker.data.repository.FamilyRepository
import com.university.danmot.subscriptiontracker.data.repository.SubscriptionRepository
import com.university.danmot.subscriptiontracker.domain.model.BillingCycle
import com.university.danmot.subscriptiontracker.domain.model.PriceHistory
import com.university.danmot.subscriptiontracker.domain.model.Subscription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = SubscriptionRepository(db.subscriptionDao(), db.priceHistoryDao())
    private val authRepository = AuthRepository()
    private val familyRepository = FamilyRepository()

    val userId: String
        get() = authRepository.currentUserId ?: "me"

    val currentFamily: StateFlow<Family?> = familyRepository
        .observeUserFamily(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val familySubscriptions: Flow<List<Subscription>> = currentFamily.flatMapLatest { family ->
        if (family != null) {
            familyRepository.observeFamilySubscriptions(family.familyId)
        } else {
            flowOf(emptyList())
        }
    }

    // Объединяем личные (Room) и семейные (Firestore) подписки в один список
    val subscriptions: StateFlow<List<Subscription>> = combine(
        repository.getSubscriptionsForOwner(userId),
        familySubscriptions
    ) { personal, family ->
        (personal + family).sortedBy { it.nextPaymentDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSubscriptionById(id: Long): StateFlow<Subscription?> =
        subscriptions.map { list -> list.find { it.id == id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getPriceHistory(subscriptionId: Long): StateFlow<List<PriceHistory>> =
        repository.getPriceHistory(subscriptionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubscription(
        name: String,
        price: Double,
        cycle: BillingCycle,
        date: String,
        isFamily: Boolean = false
    ) {
        viewModelScope.launch {
            val famId = if (isFamily) currentFamily.value?.familyId else null
            val sub = Subscription(
                id = if (famId != null) System.currentTimeMillis() else 0L,
                name = name,
                price = price,
                previousPrice = null,
                currency = "RUB",
                cycle = cycle,
                nextPaymentDate = LocalDate.parse(date),
                ownerId = userId,
                familyId = famId
            )
            if (famId != null) {
                familyRepository.addFamilySubscription(sub, famId)
            } else {
                repository.insert(sub)
            }
        }
    }

    fun updateSubscription(subscription: Subscription) {
        viewModelScope.launch {
            if (subscription.familyId != null) {
                familyRepository.updateFamilySubscription(subscription)
            } else {
                repository.update(subscription)
            }
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            if (subscription.familyId != null) {
                familyRepository.deleteFamilySubscription(subscription.id)
            } else {
                repository.delete(subscription)
            }
        }
    }
}