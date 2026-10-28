package com.university.danmot.subscriptiontracker

import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

class TrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupWorkManager()
    }

    private fun setupWorkManager() {
        // 1. Периодическая проверка цен (раз в день)
        val priceCheckRequest = PeriodicWorkRequestBuilder<com.university.danmot.subscriptiontracker.worker.PriceCheckWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true) // Не работать, если батарея садится
                    .build()
            )
            .build()

        // 2. Напоминания об оплате (раз в день)
        val reminderRequest = PeriodicWorkRequestBuilder<com.university.danmot.subscriptiontracker.worker.PaymentReminderWorker>(
            1, TimeUnit.DAYS
        ).build()

        // Запускаем уникальные задачи, чтобы не дублировать их при каждом старте приложения
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "price_check",
            ExistingPeriodicWorkPolicy.KEEP, // KEEP означает: если уже есть, не создавать новую
            priceCheckRequest
        )

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "payment_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}