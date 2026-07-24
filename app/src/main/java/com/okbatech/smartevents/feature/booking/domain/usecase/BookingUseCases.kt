package com.okbatech.smartevents.feature.booking.domain.usecase

import com.okbatech.smartevents.feature.booking.domain.model.BookingSummary
import com.okbatech.smartevents.feature.booking.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMyBookingsUseCase @Inject constructor(private val repository: BookingRepository) {
    operator fun invoke(userId: String): Flow<List<BookingSummary>> = repository.observeMyBookings(userId)
}

class ObserveBookingByIdUseCase @Inject constructor(private val repository: BookingRepository) {
    operator fun invoke(bookingId: String): Flow<BookingSummary?> = repository.observeBookingById(bookingId)
}

class ObserveAttendeeUserIdsUseCase @Inject constructor(private val repository: BookingRepository) {
    operator fun invoke(eventId: String): Flow<List<String>> = repository.observeAttendeeUserIds(eventId)
}

/** Free one-tap RSVP used from Home/Search/See All/Wish List cards. */
class JoinEventUseCase @Inject constructor(private val repository: BookingRepository) {
    suspend operator fun invoke(userId: String, eventId: String): Result<Unit> =
        repository.bookEvent(userId, eventId, ticketCount = 1).map { }
}

/** Full checkout flow (Buy Ticket -> Payment) — returns the new booking id to open the Ticket screen. */
class PurchaseTicketUseCase @Inject constructor(private val repository: BookingRepository) {
    suspend operator fun invoke(userId: String, eventId: String, ticketCount: Int): Result<String> =
        repository.bookEvent(userId, eventId, ticketCount)
}
