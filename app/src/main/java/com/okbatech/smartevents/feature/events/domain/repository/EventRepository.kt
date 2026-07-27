package com.okbatech.smartevents.feature.events.domain.repository

import com.okbatech.smartevents.feature.events.domain.model.EventDetail
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.model.EventsLoadState
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeFeaturedEvents(): Flow<List<EventSummary>>
    fun observeAllEvents(): Flow<List<EventSummary>>
    fun observeEventDetail(eventId: String): Flow<EventDetail?>
    fun observeEventsByOrganizer(organizerId: String): Flow<List<EventSummary>>
    fun observeEventsLoadState(): Flow<EventsLoadState>

    /** Re-fetches events from the backend, e.g. after a failed load or a pull-to-refresh. */
    suspend fun refreshEvents()

    suspend fun createEvent(
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
    ): Result<String>

    suspend fun updateEvent(
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
    ): Result<Unit>
}
