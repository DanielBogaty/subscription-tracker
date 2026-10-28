package com.university.danmot.subscriptiontracker.domain.model

import java.time.LocalDate

data class PriceHistory(
    val id: Long = 0,
    val subscriptionId: Long,
    val price: Double,
    val date: LocalDate
)