package com.university.danmot.subscriptiontracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [SubscriptionEntity::class, PriceHistoryEntity::class],
    version = 2, // ВАЖНО: Подняли версию с 1 до 2!
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun priceHistoryDao(): PriceHistoryDao // <-- ДОБАВИЛИ
}