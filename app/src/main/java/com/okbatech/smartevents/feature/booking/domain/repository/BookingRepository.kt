package com.okbatech.smartevents.feature.booking.domain.repository

import com.okbatech.smartevents.feature.booking.domain.model.BookingSummary
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun observeMyBookings(userId: String): Flow<List<BookingSummary>>
    fun observeBookingById(bookingId: String): Flow<BookingSummary?>
    fun observeAttendeeUserIds(eventId: String): Flow<List<String>>
    suspend fun bookEvent(userId: String, eventId: String, ticketCount: Int): Result<String>
}
