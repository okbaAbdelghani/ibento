package com.okbatech.smartevents.feature.events.domain.usecase

import com.okbatech.smartevents.feature.events.domain.model.EventDetail
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.model.Review
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import com.okbatech.smartevents.feature.events.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveReviewsForOrganizerUseCase @Inject constructor(private val repository: ReviewRepository) {
    operator fun invoke(organizerId: String): Flow<List<Review>> = repository.observeReviewsByOrganizer(organizerId)
}

class ObserveEventDetailUseCase @Inject constructor(private val repository: EventRepository) {
    operator fun invoke(eventId: String): Flow<EventDetail?> = repository.observeEventDetail(eventId)
}

class ObserveEventsByOrganizerUseCase @Inject constructor(private val repository: EventRepository) {
    operator fun invoke(organizerId: String): Flow<List<EventSummary>> = repository.observeEventsByOrganizer(organizerId)
}

data class EventFormInput(
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val startDateTime: Long,
    val endDateTime: Long,
    val venueName: String,
    val city: String,
    val priceAmount: Double,
    val capacity: Int,
)

class CreateEventUseCase @Inject constructor(private val repository: EventRepository) {
    suspend operator fun invoke(input: EventFormInput, organizerId: String): Result<String> = repository.createEvent(
        title = input.title,
        description = input.description,
        category = input.category,
        imageUrl = input.imageUrl,
        startDateTime = input.startDateTime,
        endDateTime = input.endDateTime,
        venueName = input.venueName,
        city = input.city,
        priceAmount = input.priceAmount,
        capacity = input.capacity,
        organizerId = organizerId,
    )
}

class UpdateEventUseCase @Inject constructor(private val repository: EventRepository) {
    suspend operator fun invoke(eventId: String, input: EventFormInput): Result<Unit> = repository.updateEvent(
        eventId = eventId,
        title = input.title,
        description = input.description,
        category = input.category,
        imageUrl = input.imageUrl,
        startDateTime = input.startDateTime,
        endDateTime = input.endDateTime,
        venueName = input.venueName,
        city = input.city,
        priceAmount = input.priceAmount,
        capacity = input.capacity,
    )
}
