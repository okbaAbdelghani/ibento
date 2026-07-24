package com.okbatech.smartevents.feature.events.data

import com.okbatech.smartevents.core.network.ApiService
import com.okbatech.smartevents.core.network.EventDto
import com.okbatech.smartevents.core.network.EventInputRequest
import com.okbatech.smartevents.core.network.safeApiCall
import com.okbatech.smartevents.feature.events.domain.model.EventDetail
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * Backed by a single in-memory cache of every event, refreshed from the backend on first
 * collection and after any mutation. Unlike Room's live-query [Flow]s, this does not push
 * updates from other clients/devices — callers see fresh data on their next collection or
 * after a local create/update, not the instant something changes server-side.
 */
class EventRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : EventRepository {

    private val eventsCache = MutableStateFlow<List<EventDto>>(emptyList())
    private var hasFetched = false

    private suspend fun ensureFetched() {
        if (!hasFetched) refresh()
    }

    private suspend fun refresh() {
        eventsCache.value = runCatching { api.listEvents() }.getOrDefault(eventsCache.value)
        hasFetched = true
    }

    override fun observeFeaturedEvents(): Flow<List<EventSummary>> =
        eventsCache.onStart { ensureFetched() }.map { list -> list.filter { it.isFeatured }.map { it.toSummary() } }

    override fun observeAllEvents(): Flow<List<EventSummary>> =
        eventsCache.onStart { ensureFetched() }.map { list -> list.map { it.toSummary() } }

    override fun observeEventDetail(eventId: String): Flow<EventDetail?> =
        eventsCache.onStart { ensureFetched() }.map { list -> list.find { it.id == eventId }?.toDetail() }

    override fun observeEventsByOrganizer(organizerId: String): Flow<List<EventSummary>> =
        eventsCache.onStart { ensureFetched() }
            .map { list -> list.filter { it.organizerId == organizerId }.map { it.toSummary() } }

    override suspend fun createEvent(
        title: String,
        description: String,
        category: String,
        imageUrl: String,
        startDateTime: Long,
        endDateTime: Long,
        venueName: String,
        city: String,
        priceAmount: Double,
        capacity: Int,
        organizerId: String,
    ): Result<String> =
        safeApiCall {
            api.createEvent(
                EventInputRequest(
                    title = title,
                    description = description,
                    category = category,
                    imageUrl = imageUrl,
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    venueName = venueName,
                    city = city,
                    priceAmount = priceAmount,
                    capacity = capacity,
                ),
            )
        }.onSuccess { refresh() }.map { it.id }

    override suspend fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        category: String,
        imageUrl: String,
        startDateTime: Long,
        endDateTime: Long,
        venueName: String,
        city: String,
        priceAmount: Double,
        capacity: Int,
    ): Result<Unit> =
        safeApiCall {
            api.updateEvent(
                eventId,
                EventInputRequest(
                    title = title,
                    description = description,
                    category = category,
                    imageUrl = imageUrl,
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    venueName = venueName,
                    city = city,
                    priceAmount = priceAmount,
                    capacity = capacity,
                ),
            )
        }.onSuccess { refresh() }.map { }
}

private fun EventDto.toSummary() = EventSummary(
    id = id,
    title = title,
    category = category,
    imageUrl = imageUrl,
    startDateTime = startDateTime,
    venueName = venueName,
    city = city,
    priceAmount = priceAmount,
    currency = currency,
    attendeeCount = attendeeCount,
)

private fun EventDto.toDetail() = EventDetail(
    id = id,
    title = title,
    description = description,
    category = category,
    imageUrl = imageUrl,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    venueName = venueName,
    city = city,
    priceAmount = priceAmount,
    currency = currency,
    organizerId = organizerId,
    capacity = capacity,
    attendeeCount = attendeeCount,
)
