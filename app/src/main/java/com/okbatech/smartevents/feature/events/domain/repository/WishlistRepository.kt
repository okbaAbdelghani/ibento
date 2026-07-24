package com.okbatech.smartevents.feature.events.domain.repository

import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import kotlinx.coroutines.flow.Flow

interface WishlistRepository {
    fun observeWishlistedEventIds(userId: String): Flow<Set<String>>
    fun observeWishlistedEvents(userId: String): Flow<List<EventSummary>>
    suspend fun toggleWishlist(userId: String, eventId: String)
}
