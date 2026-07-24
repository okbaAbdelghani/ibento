package com.okbatech.smartevents.feature.events.domain.model

data class EventSummary(
    val id: String,
    val title: String,
    val category: String,
    val imageUrl: String,
    val startDateTime: Long,
    val venueName: String,
    val city: String,
    val priceAmount: Double,
    val currency: String,
    val attendeeCount: Int,
)
