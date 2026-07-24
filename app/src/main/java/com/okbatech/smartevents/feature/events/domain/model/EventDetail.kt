package com.okbatech.smartevents.feature.events.domain.model

data class EventDetail(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val startDateTime: Long,
    val endDateTime: Long,
    val venueName: String,
    val city: String,
    val priceAmount: Double,
    val currency: String,
    val organizerId: String,
    val capacity: Int,
    val attendeeCount: Int,
)
