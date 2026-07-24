package com.okbatech.smartevents.feature.booking.data

import com.okbatech.smartevents.core.database.dao.NotificationDao
import com.okbatech.smartevents.core.database.entity.NotificationEntity
import com.okbatech.smartevents.core.network.ApiService
import com.okbatech.smartevents.core.network.BookEventRequest
import com.okbatech.smartevents.core.network.BookingDto
import com.okbatech.smartevents.core.network.safeApiCall
import com.okbatech.smartevents.feature.booking.domain.model.BookingSummary
import com.okbatech.smartevents.feature.booking.domain.repository.BookingRepository
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import java.util.UUID
import javax.inject.Inject

/** Notifications stay local-only for this phase — a successful remote booking still writes
 * a local [NotificationEntity] exactly as the old Room-backed implementation did. */
class BookingRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val eventRepository: EventRepository,
    private val notificationDao: NotificationDao,
) : BookingRepository {

    private val bookingsCache = MutableStateFlow<List<BookingDto>>(emptyList())
    private var hasFetched = false

    private suspend fun ensureFetched() {
        if (!hasFetched) refresh()
    }

    private suspend fun refresh() {
        bookingsCache.value = runCatching { api.myBookings() }.getOrDefault(bookingsCache.value)
        hasFetched = true
    }

    override fun observeMyBookings(userId: String): Flow<List<BookingSummary>> =
        combine(bookingsCache.onStart { ensureFetched() }, eventRepository.observeAllEvents()) { bookings, events ->
            val eventsById = events.associateBy { it.id }
            bookings.filter { it.userId == userId }.mapNotNull { booking ->
                eventsById[booking.eventId]?.let { booking.toSummary(it) }
            }
        }

    override fun observeBookingById(bookingId: String): Flow<BookingSummary?> =
        combine(bookingsCache.onStart { ensureFetched() }, eventRepository.observeAllEvents()) { bookings, events ->
            val booking = bookings.find { it.id == bookingId } ?: return@combine null
            val event = events.find { it.id == booking.eventId } ?: return@combine null
            booking.toSummary(event)
        }

    override fun observeAttendeeUserIds(eventId: String): Flow<List<String>> = flow {
        emit(runCatching { api.getAttendees(eventId) }.getOrDefault(emptyList()))
    }

    override suspend fun bookEvent(userId: String, eventId: String, ticketCount: Int): Result<String> =
        safeApiCall { api.createBooking(BookEventRequest(eventId, ticketCount)) }
            .onSuccess { booking ->
                refresh()
                val eventTitle = eventRepository.observeAllEvents().first().find { it.id == eventId }?.title ?: "the event"
                notificationDao.insertAll(
                    listOf(
                        NotificationEntity(
                            id = "n-${UUID.randomUUID()}",
                            userId = userId,
                            title = "You're going!",
                            body = "Your $ticketCount ticket${if (ticketCount > 1) "s" else ""} for $eventTitle " +
                                "${if (ticketCount > 1) "are" else "is"} confirmed.",
                            type = "BOOKING",
                            createdAt = System.currentTimeMillis(),
                        ),
                    ),
                )
            }
            .map { it.id }
}

private fun BookingDto.toSummary(event: EventSummary) = BookingSummary(
    bookingId = id,
    event = event,
    ticketCount = ticketCount,
    totalPrice = totalPrice,
    bookedAt = bookedAt,
    qrCode = qrCode,
)
