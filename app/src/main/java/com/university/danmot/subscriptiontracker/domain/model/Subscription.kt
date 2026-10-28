package com.university.danmot.subscriptiontracker.domain.model

import java.time.LocalDate

data class Subscription(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val previousPrice: Double? = null,
    val currency: String = "RUB",
    val cycle: BillingCycle,
    val nextPaymentDate: LocalDate,
    val ownerId: String = "me",
    val familyId: String? = null,
    val colorHex: String = "#FF6B6B",
    val note: String = ""
) {
    val priceChanged: Boolean
        get() = previousPrice != null && previousPrice != price

    fun monthlyCost(): Double = when (cycle) {
        BillingCycle.WEEKLY -> price * 4.33
        BillingCycle.MONTHLY -> price
        BillingCycle.QUARTERLY -> price / 3
        BillingCycle.YEARLY -> price / 12
    }
}

enum class BillingCycle(val label: String, val days: Int) {
    WEEKLY("Неделя", 7),
    MONTHLY("Месяц", 30),
    QUARTERLY("Квартал", 90),
    YEARLY("Год", 365)
}