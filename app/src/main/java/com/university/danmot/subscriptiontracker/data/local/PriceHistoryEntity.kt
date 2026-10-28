package com.university.danmot.subscriptiontracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Связываем историю с подпиской. Если подписка удалится, удалится и её история (CASCADE).
@Entity(
    tableName = "price_history",
    foreignKeys = [ForeignKey(
        entity = SubscriptionEntity::class,
        parentColumns = ["id"],
        childColumns = ["subscriptionId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long,
    val price: Double,
    val date: String // Храним как строку LocalDate
)