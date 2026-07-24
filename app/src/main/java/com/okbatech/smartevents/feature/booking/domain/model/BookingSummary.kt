package com.okbatech.smartevents.feature.booking.domain.model

import com.okbatech.smartevents.feature.events.domain.model.EventSummary

data class BookingSummary(
    val bookingId: String,
    val event: EventSummary,
    val ticketCount: Int,
    val totalPrice: Double,
    val bookedAt: Long,
    val qrCode: String,
) {
    val isUpcoming: Boolean get() = event.startDateTime >= System.currentTimeMillis()
}
