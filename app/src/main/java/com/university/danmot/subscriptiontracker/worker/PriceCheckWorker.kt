package com.university.danmot.subscriptiontracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.university.danmot.subscriptiontracker.MainActivity
import com.university.danmot.subscriptiontracker.R
import com.university.danmot.subscriptiontracker.data.local.DatabaseProvider
import com.university.danmot.subscriptiontracker.data.local.toDomain
import com.university.danmot.subscriptiontracker.data.local.toEntity
import com.university.danmot.subscriptiontracker.data.price.PriceSource
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class PriceCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "price_changes"
        const val CHANNEL_NAME = "Изменения цен"
    }

    override suspend fun doWork(): Result {
        return try {
            val db = DatabaseProvider.getDatabase(applicationContext)
            val dao = db.subscriptionDao()
            val historyDao = db.priceHistoryDao() // <-- ПОЛУЧИЛИ DAO ИСТОРИИ
            val entities = dao.getAllSubscriptions().first()

            entities.forEach { entity ->
                val subscription = entity.toDomain()
                val newPrice = PriceSource.checkPriceChange(subscription)

                if (newPrice != null) {
                    // 1. Сохраняем СТАРУЮ цену в историю
                    historyDao.insert(
                        com.university.danmot.subscriptiontracker.data.local.PriceHistoryEntity(
                            subscriptionId = subscription.id,
                            price = subscription.price, // Старая цена
                            date = LocalDate.now().toString()
                        )
                    )

                    // 2. Обновляем новую цену в основной таблице
                    val updatedSubscription = subscription.copy(
                        previousPrice = subscription.price,
                        price = newPrice
                    )
                    dao.update(updatedSubscription.toEntity())

                    // 3. Показываем уведомление
                    showPriceChangeNotification(subscription.name, subscription.price, newPrice)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showPriceChangeNotification(name: String, oldPrice: Double, newPrice: Double) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Цена изменилась!")
            .setContentText("$name: ${oldPrice.toInt()}₽ → ${newPrice.toInt()}₽")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(name.hashCode(), notification)
    }
}