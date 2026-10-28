package com.university.danmot.subscriptiontracker.data.price

import com.university.danmot.subscriptiontracker.domain.model.Subscription
import kotlin.random.Random

/**
 * Имитирует проверку цен подписок.
 * В реальном проекте здесь был бы API-вызов или парсинг.
 */
object PriceSource {

    /**
     * Проверяет, изменилась ли цена подписки.
     * Возвращает новую цену с вероятностью 30%, иначе null (без изменений).
     */
    fun checkPriceChange(subscription: Subscription): Double? {
        // 30% шанс, что цена изменилась
        if (Random.nextDouble() < 0.3) {
            // Генерируем новую цену: ±10-30% от текущей
            val changePercent = Random.nextDouble(0.7, 1.3)
            val newPrice = subscription.price * changePercent

            return newPrice
        }

        // В 70% случаев цена остается прежней
        return null
    }
}