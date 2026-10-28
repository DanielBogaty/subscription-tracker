package com.university.danmot.subscriptiontracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.university.danmot.subscriptiontracker.domain.model.BillingCycle

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val previousPrice: Double?,
    val currency: String,
    val cycle: String,
    val nextPaymentDate: String,
    val ownerId: String,
    val familyId: String?,
    val colorHex: String,
    val note: String
)