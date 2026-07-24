package com.okbatech.smartevents.feature.events.domain.usecase

import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWishlistedEventIdsUseCase @Inject constructor(private val repository: WishlistRepository) {
    operator fun invoke(userId: String): Flow<Set<String>> = repository.observeWishlistedEventIds(userId)
}

class ObserveWishlistedEventsUseCase @Inject constructor(private val repository: WishlistRepository) {
    operator fun invoke(userId: String): Flow<List<EventSummary>> = repository.observeWishlistedEvents(userId)
}

class ToggleWishlistUseCase @Inject constructor(private val repository: WishlistRepository) {
    suspend operator fun invoke(userId: String, eventId: String) = repository.toggleWishlist(userId, eventId)
}
