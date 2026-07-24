package com.okbatech.smartevents.feature.events.domain.repository

import com.okbatech.smartevents.feature.events.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeReviewsByOrganizer(organizerId: String): Flow<List<Review>>
    suspend fun submitReview(organizerId: String, authorId: String, rating: Float, comment: String): Result<Unit>
}
