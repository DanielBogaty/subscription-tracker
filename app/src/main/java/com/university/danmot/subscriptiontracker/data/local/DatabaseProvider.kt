package com.university.danmot.subscriptiontracker.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "subscription_tracker_db"
            )
                .fallbackToDestructiveMigration() // <-- ДОБАВИЛИ (чтобы не писать сложные миграции)
                .build()
            INSTANCE = instance
            instance
        }
    }
}