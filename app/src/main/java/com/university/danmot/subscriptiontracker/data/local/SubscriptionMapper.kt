package com.university.danmot.subscriptiontracker.data.local

import com.university.danmot.subscriptiontracker.domain.model.BillingCycle
import com.university.danmot.subscriptiontracker.domain.model.Subscription
import java.time.LocalDate

fun SubscriptionEntity.toDomain(): Subscription = Subscription(
    id = id,
    name = name,
    price = price,
    previousPrice = previousPrice,
    currency = currency,
    cycle = BillingCycle.valueOf(cycle),
    nextPaymentDate = LocalDate.parse(nextPaymentDate),
    ownerId = ownerId,
    familyId = familyId,
    colorHex = colorHex,
    note = note
)

fun Subscription.toEntity(): SubscriptionEntity = SubscriptionEntity(
    id = id,
    name = name,
    price = price,
    previousPrice = previousPrice,
    currency = currency,
    cycle = cycle.name,
    nextPaymentDate = nextPaymentDate.toString(),
    ownerId = ownerId,
    familyId = familyId,
    colorHex = colorHex,
    note = note
)