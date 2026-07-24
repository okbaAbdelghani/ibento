package com.okbatech.smartevents.feature.events.data

import com.okbatech.smartevents.core.database.dao.WishlistDao
import com.okbatech.smartevents.core.database.entity.WishlistEntity
import com.okbatech.smartevents.feature.events.domain.model.EventSummary
import com.okbatech.smartevents.feature.events.domain.repository.EventRepository
import com.okbatech.smartevents.feature.events.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WishlistRepositoryImpl @Inject constructor(
    private val wishlistDao: WishlistDao,
    private val eventRepository: EventRepository,
) : WishlistRepository {

    override fun observeWishlistedEventIds(userId: String): Flow<Set<String>> =
        wishlistDao.observeEventIds(userId).map { it.toSet() }

    override fun observeWishlistedEvents(userId: String): Flow<List<EventSummary>> =
        combine(wishlistDao.observeEventIds(userId), eventRepository.observeAllEvents()) { ids, events ->
            val idSet = ids.toSet()
            events.filter { it.id in idSet }
        }

    override suspend fun toggleWishlist(userId: String, eventId: String) {
        val current = wishlistDao.observeEventIds(userId).first()
        if (eventId in current) {
            wishlistDao.delete(WishlistEntity(userId, eventId, addedAt = 0L))
        } else {
            wishlistDao.insert(WishlistEntity(userId, eventId, addedAt = System.currentTimeMillis()))
        }
    }
}
