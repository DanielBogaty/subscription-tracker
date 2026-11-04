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
import com.university.danmot.subscriptiontracker.R // <--- ДОБАВЛЕНО
import com.university.danmot.subscriptiontracker.data.local.DatabaseProvider
import com.university.danmot.subscriptiontracker.data.local.toDomain // <--- ДОБАВЛЕНО
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit // <--- ДОБАВЛЕНО

class PaymentReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "payment_reminders"
        const val CHANNEL_NAME = "Напоминания об оплате"
    }

    override suspend fun doWork(): Result {
        return try {
            val dao = DatabaseProvider.getDatabase(applicationContext).subscriptionDao()
            val entities = dao.getAllSubscriptionsList()
            val today = LocalDate.now()

            // Находим подписки, у которых следующая оплата в ближайшие 3 дня
            val upcomingPayments = entities.filter { entity ->
                val subscription = entity.toDomain()
                val daysUntilPayment = ChronoUnit.DAYS.between(today, subscription.nextPaymentDate)
                daysUntilPayment in 0..3
            }

            upcomingPayments.forEach { entity ->
                val subscription = entity.toDomain()
                val daysLeft = ChronoUnit.DAYS.between(today, subscription.nextPaymentDate).toInt()
                showReminderNotification(subscription.name, subscription.price, daysLeft)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showReminderNotification(name: String, price: Double, daysLeft: Int) {
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
            .setContentTitle("Скоро списание!")
            .setContentText("$name: ${price.toInt()}₽ через $daysLeft дн.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(name.hashCode() + 1000, notification)
    }
}